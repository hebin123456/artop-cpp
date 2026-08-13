// EClassEmitter.cpp —— 为单个 EClass 生成 <ClassName>.h/.cpp
// 对齐 Java: <ClassName>.java / <ClassName>Impl.java（合并为单类单继承方案）
//
// 设计（对齐 ARCHITECTURE.md 与 Java EMF codegen，参考 .build_cache golden reference）：
//   - 单类：class <Name> : public emf::common::EObjectImpl（无 Impl 后缀、无多继承）
//   - attribute:  <cppType> name() const; / void setName(<cppType> value);
//   - reference multi (upperBound>1 || -1):  EList<T*>& get<Name>();
//   - reference single:                      T* get<Name>() const; / void set<Name>(T* value);
//   - 反射 override: eGet/eSet/eIsSet/eUnset（feature 与 int 两套）+ eContents/eClass
//   - eStaticFeature(int): 按 featureID 返回 Package 静态 feature（继承 feature 委托基类 Package）
//   - feature ID 跨继承链累积（先 ESuperType features，再本类 features）
//   - isSet_ 标志：每个 attribute 配 bool <name>_isSet_ = false;
//   - ARTOP 元数据（模型驱动，来自 EAnnotation）：eStaticXmlName/eStaticFeatureXmlName/... + override 虚函数
//   - containment multi reference：构造函数用 EList(cb, this, feat) 维护 eContainer（函数指针回调）
//   - containment single reference：setter 维护 eContainer/eContainingFeature
//   - 跨包 EReference：namespace 嵌套前向声明 + 全限定类型引用
//
// 签名适配当前 EObject.h（golden 基于更完整 EObject.h，此处做必要适配）：
//   - ARTOP 方法返回 std::string（by value）匹配 EObject.h（非 const std::string&）
//   - eSet(int, const std::any& value) 匹配 EObject.h（非 by value）
//   - eFeatureID 不加 override（EObject.h 中非虚函数）
//   - eContents 返回 const std::vector<EObject*>&（用 mutable cache，非 by value）
#include "emf/ecore/codegen/EClassEmitter.h"
#include "emf/ecore/codegen/EAnnotationReader.h"
#include "emf/ecore/codegen/IndentedWriter.h"
#include "emf/ecore/codegen/StringUtils.h"
#include "emf/ecore/codegen/TypeMapper.h"
#include "emf/ecore/codegen/PackageEmitter.h"
#include "emf/ecore/EcoreImpls.h"
#include <string>
#include <vector>
#include <unordered_set>
#include <unordered_map>

namespace emf::ecore::codegen {

namespace {

// 计算 EClass 所在 namespace：baseNamespace::parentPath::pkgName（"::" 分隔）
std::string classNs(emf::ecore::EClass* cls, const std::string& base, const std::string& parentPath) {
    auto* pkg = cls->getEPackage();
    std::string pkgName = pkg ? pkg->getName() : "";
    std::string fullName = parentPath.empty() ? pkgName : (parentPath + "/" + pkgName);
    std::string r;
    r.reserve(fullName.size() * 2);
    for (char c : fullName) {
        if (c == '/') r += "::";
        else r += c;
    }
    if (base.empty()) return r;
    return base + "::" + r;
}

// 给定 EPackage，计算其完整 namespace（baseNamespace + epackageFullPath 转 ::）
std::string nsForPackage(emf::ecore::EPackage* pkg, const std::string& baseNamespace) {
    std::string full = epackageFullPath(pkg);   // "parent/pkg"
    std::string ns = pathToNs(full);            // "parent::pkg"
    if (baseNamespace.empty()) return ns;
    return baseNamespace + "::" + ns;
}

std::string pkgClassName(emf::ecore::EClass* cls) {
    auto* pkg = cls->getEPackage();
    std::string pkgName = pkg ? pkg->getName() : "";
    return capitalizeFirst(pkgName) + "Package";
}

// 在继承链中查找声明某 feature 的 EClass
emf::ecore::EClass* findDeclaringClass(emf::ecore::EClass* cls, emf::ecore::EStructuralFeature* sf) {
    if (!cls) return nullptr;
    for (auto* local : cls->getEStructuralFeatures()) {
        if (local == sf) return cls;
    }
    for (auto* super : cls->getESuperTypes()) {
        auto* found = findDeclaringClass(super, sf);
        if (found) return found;
    }
    return nullptr;
}

// C++ 关键字集合：当 EAttribute 名为关键字时，getter 方法名需加下划线后缀避免语法冲突
// （对齐 Java EMF codegen 的 GenBase.escapeIdentifier，但 C++ 关键字集与 Java 不同）
const std::unordered_set<std::string>& cppKeywords() {
    static const std::unordered_set<std::string> kw = {
        "alignas","alignand","asm","auto","bool","break","case","catch","char","char8_t",
        "char16_t","char32_t","class","compl","concept","const","consteval","constexpr",
        "constinit","const_cast","continue","co_await","co_return","co_yield","decltype",
        "default","delete","do","double","dynamic_cast","else","enum","explicit","export",
        "extern","false","float","for","friend","goto","if","inline","int","long","mutable",
        "namespace","new","noexcept","nullptr","operator","private","protected","public",
        "register","reinterpret_cast","requires","return","short","signed","sizeof","static",
        "static_assert","static_cast","struct","switch","template","this","thread_local",
        "throw","true","try","typedef","typeid","typename","union","unsigned","using",
        "virtual","void","volatile","wchar_t","while",
    };
    return kw;
}

// 如果 name 是 C++ 关键字，返回 name + "_"；否则原样返回
// （目前仅 cppKeywords() 集合被使用，getter 转义在 emitHeader/emitImplementation 中内联处理）

struct FeatureInfo {
    emf::ecore::EStructuralFeature* sf;
    bool isAttribute;
    bool isReference;
    bool isContainment;
    bool isMulti;
    bool isInherited;          // true 表示来自 ESuperType
    std::string rawName;       // 原始 feature 名（用于字符串字面量，如 eStaticFeatureName）
    std::string name;          // 安全化后的 C++ 标识符（safeIdent(rawName)，如 SHORT_NAME）
    std::string capName;       // 首字母大写
    std::string fieldName;     // name + "_"
    std::string idConstName;   // NAME__ / BOOKS__
    std::string cppType;       // attribute 的 C++ 类型
    std::string targetName;       // reference 目标类名（裸名）
    std::string targetFullName;   // reference 目标全限定名（跨包时含 namespace）
    bool targetCrossPackage;       // 目标类在不同包
    bool isBuiltinEObject;         // 目标是 Ecore EObject（用 emf::common::EObject，无需前向声明/include）
    std::string targetIncludePath; // 跨包目标 include 路径（.cpp 用）
    std::string defLit;        // " = <literal>" 或空
    std::string pkgGetter;     // get<ClassName>_<CapFeature>
    std::string declaringPkgClass;   // 声明类的 Package 类名
    std::string declaringPkgNs;      // 声明类所在 namespace（全限定）
    std::string declaringClassName;  // 声明 EClass 名
    int  featureID;
    // ARTOP 元数据
    std::string xmlName;
    std::string xmlNamePlural;
    int sequenceOffset;
    bool isRoleElement;
    bool isRoleWrapperElement;
    bool isTypeElement;
    bool isTypeWrapperElement;
    bool isXmlAttribute;
    int aprxmlRule;
};

// 收集所有 feature（getEAllStructuralFeatures，含继承），feature ID 跨继承链累积
std::vector<FeatureInfo> collectFeatures(emf::ecore::EClass* cls, const std::string& baseNamespace) {
    std::vector<FeatureInfo> out;
    const auto& localFeatures = cls->getEStructuralFeatures();
    auto isLocal = [&](emf::ecore::EStructuralFeature* sf) {
        for (auto* l : localFeatures) if (l == sf) return true;
        return false;
    };

    int id = 0;
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        FeatureInfo fi;
        fi.sf = sf;
        fi.isAttribute = (dynamic_cast<emf::ecore::EAttribute*>(sf) != nullptr);
        fi.isReference = (dynamic_cast<emf::ecore::EReference*>(sf) != nullptr);
        fi.isContainment = false;
        fi.isMulti = false;
        fi.rawName = sf->getName();
        fi.name = safeIdent(fi.rawName);
        fi.capName = capitalizeFirst(fi.name);
        fi.fieldName = fi.name + "_";
        fi.featureID = id++;
        fi.idConstName = toUpper(fi.name) + "__";
        fi.defLit = "";
        fi.targetCrossPackage = false;
        fi.isBuiltinEObject = false;
        fi.isInherited = !isLocal(sf);

        // 确定声明类（用于跨包/继承 feature 的 eStaticFeature 委托）
        emf::ecore::EClass* declCls = fi.isInherited ? findDeclaringClass(cls, sf) : cls;
        if (!declCls) declCls = cls;
        fi.declaringClassName = declCls->getName();
        fi.declaringPkgClass = pkgClassName(declCls);
        fi.declaringPkgNs = nsForPackage(declCls->getEPackage(), baseNamespace);
        fi.pkgGetter = "get" + fi.declaringClassName + "_" + fi.capName;

        if (fi.isAttribute) {
            auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
            auto* attrType = attr ? attr->getEAttributeType() : nullptr;
            // 模型驱动类型映射：所有 EAttribute（内建 Ecore + 用户自定义 + EEnum）
            // 统一从 EDataType.instanceClassName 决定 C++ 类型，无特判。
            // 对齐 Java EMF codegen：EAttribute 的运行时类型由 EDataType.instanceClassName 决定。
            //   - java.lang.String → std::string
            //   - java.lang.Boolean → bool
            //   - java.lang.Integer → int32_t
            //   - java.lang.Long → int64_t
            //   - java.lang.Double → double
            //   - 其他复杂类型 → std::string
            //   - EEnum（instanceClassName 为空）→ std::string（存枚举字面量名）
            //   - 未解析 eType（attrType 为空）→ std::string（保守默认）
            std::string icn = attrType ? attrType->getInstanceClassName() : "";
            fi.cppType = TypeMapper::cppTypeFromInstanceClass(icn);
            // 多值 EAttribute：upperBound > 1 或 -1 → isMulti=true（对齐 EReference 处理）
            int ub = attr ? attr->getUpperBound() : 1;
            fi.isMulti = (ub == -1 || ub > 1);
            const std::string& dl = sf->getDefaultValueLiteral();
            if (!dl.empty()) {
                fi.defLit = " = " + TypeMapper::defaultValueLiteral(fi.cppType, dl);
            }
        } else if (fi.isReference) {
            auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
            emf::ecore::EClass* target = ref ? ref->getEReferenceType() : nullptr;
            int ub = ref ? ref->getUpperBound() : 1;
            fi.isMulti = (ub == -1 || ub > 1);
            fi.isContainment = ref ? ref->isContainment() : false;
            // 判断目标是否为 Ecore 内建元类（EObject / EClass / EStringToStringMapEntry 等）
            // 或未解析（eType 指向 ecore:EClass #//EObject）。
            // 所有 Ecore 包内的元类统一用 emf::common::EObject* 作为 C++ 类型
            // （C++ runtime 没有为每个 Ecore 元类生成具体 C++ 类，只有元 EClass 对象）。
            bool isBuiltinEcore = false;
            if (!target) {
                isBuiltinEcore = true;
            } else {
                auto* tp = target->getEPackage();
                if (tp && std::string(tp->getNsURI()) == std::string(emf::ecore::EcorePackage::eNS_URI)) {
                    isBuiltinEcore = true;
                }
            }
            fi.isBuiltinEObject = isBuiltinEcore;
            if (isBuiltinEcore) {
                // 用 emf::common::EObject，无需前向声明/include（EObject.h 已含）
                fi.targetName = "emf::common::EObject";
                fi.targetFullName = "emf::common::EObject";
                fi.targetCrossPackage = false;
            } else {
                fi.targetName = target ? target->getName() : "emf::common::EObject";
                // 跨包判断
                auto* targetPkg = target ? target->getEPackage() : nullptr;
                auto* selfPkg = cls->getEPackage();
                if (targetPkg && selfPkg && targetPkg != selfPkg) {
                    fi.targetCrossPackage = true;
                    fi.targetFullName = nsForPackage(targetPkg, baseNamespace) + "::" + fi.targetName;
                    fi.targetIncludePath = epackageFullPath(targetPkg) + "/" + fi.targetName + ".h";
                } else {
                    fi.targetFullName = fi.targetName;
                }
            }
        }

        // ARTOP 元数据（模型驱动，来自 EAnnotation）
        auto fm = EAnnotationReader::readFeatureMeta(sf);
        fi.xmlName = fm.xmlName;
        fi.xmlNamePlural = fm.xmlNamePlural;
        fi.sequenceOffset = fm.sequenceOffset;
        fi.isRoleElement = fm.isRoleElement;
        fi.isRoleWrapperElement = fm.isRoleWrapperElement;
        fi.isTypeElement = fm.isTypeElement;
        fi.isTypeWrapperElement = fm.isTypeWrapperElement;
        fi.isXmlAttribute = fm.isXmlAttribute;
        fi.aprxmlRule = fm.aprxmlRule;

        out.push_back(std::move(fi));
    }
    return out;
}

// 跨包前向声明信息（去重）
struct CrossPkgRef {
    std::string fullName;   // 全限定类名
    std::string ns;         // 全限定 namespace
    std::string includePath;
    bool operator==(const CrossPkgRef& o) const { return fullName == o.fullName; }
};

// 收集继承 feature 的声明包（跨包时需 include 声明包的 Package.h，
// 供 eStaticFeature 委托调用 declaringPkgClass::eINSTANCE->get...）
std::vector<std::string> collectInheritedDeclaringPkgIncludes(emf::ecore::EClass* cls,
                                                               const std::string& baseNamespace,
                                                               const std::vector<FeatureInfo>& features) {
    std::vector<std::string> out;
    auto* selfPkg = cls->getEPackage();
    for (auto& fi : features) {
        if (!fi.isInherited) continue;
        auto* declCls = findDeclaringClass(cls, fi.sf);
        if (!declCls) continue;
        auto* declPkg = declCls->getEPackage();
        if (!declPkg || declPkg == selfPkg) continue;
        std::string inc = epackageFullPath(declPkg) + "/" + pkgClassName(declCls) + ".h";
        bool dup = false;
        for (auto& e : out) if (e == inc) { dup = true; break; }
        if (!dup) out.push_back(std::move(inc));
    }
    return out;
}

std::vector<CrossPkgRef> collectCrossPkgRefs(emf::ecore::EClass* cls, const std::string& baseNamespace,
                                             const std::vector<FeatureInfo>& features) {
    std::vector<CrossPkgRef> out;
    for (auto& fi : features) {
        if (fi.isReference && fi.targetCrossPackage) {
            CrossPkgRef r;
            r.fullName = fi.targetFullName;
            // ns = targetFullName 去掉最后的 ::ClassName
            auto pos = r.fullName.rfind("::");
            r.ns = (pos != std::string::npos) ? r.fullName.substr(0, pos) : r.fullName;
            r.includePath = fi.targetIncludePath;
            bool dup = false;
            for (auto& e : out) if (e == r) { dup = true; break; }
            if (!dup) out.push_back(std::move(r));
        }
    }
    return out;
}

// 同包兄弟类（去重，不含自身，跳过 Ecore EObject）
std::vector<std::string> referencedSiblings(emf::ecore::EClass* cls, const std::vector<FeatureInfo>& features) {
    std::vector<std::string> out;
    for (auto& fi : features) {
        if (fi.isReference && !fi.targetCrossPackage && !fi.isBuiltinEObject) {
            if (fi.targetName != cls->getName()) {
                bool dup = false;
                for (auto& e : out) if (e == fi.targetName) { dup = true; break; }
                if (!dup) out.push_back(fi.targetName);
            }
        }
    }
    return out;
}

// attribute 的 "未设置" 默认值（用于 eUnset 重置）
std::string unsetValue(const std::string& cppType) {
    if (cppType == "std::string") return "std::string()";
    if (cppType == "bool")        return "false";
    if (cppType == "float" || cppType == "double" || cppType == "char") return "0";
    if (cppType.find("int") != std::string::npos) return "0";
    return std::string();
}

}  // namespace

EClassEmitter::EClassEmitter(emf::ecore::EClass* eClass, const std::string& baseNamespace,
                             const std::string& parentPath)
    : eClass_(eClass), baseNamespace_(baseNamespace), parentPath_(parentPath) {}

std::string EClassEmitter::emitHeader() const {
    IndentedWriter w;
    auto ns = classNs(eClass_, baseNamespace_, parentPath_);
    auto features = collectFeatures(eClass_, baseNamespace_);
    auto siblings = referencedSiblings(eClass_, features);
    auto crossRefs = collectCrossPkgRefs(eClass_, baseNamespace_, features);
    const std::string& clsName = eClass_->getName();
    std::string pkgCls = pkgClassName(eClass_);
    int featureCount = static_cast<int>(features.size());

    // 收集 attribute 需要的标准头
    std::unordered_set<std::string> stdIncludes;
    for (auto& fi : features) {
        if (fi.isAttribute) {
            std::string inc = TypeMapper::includeFor(fi.cppType);
            if (!inc.empty()) stdIncludes.insert(inc);
        }
    }

    w.line("// <auto-generated/>");
    w.line("// EClass: " + clsName + " (single-class, single-inheritance)");
    w.line("#pragma once");
    w.line("#include \"emf/common/EObject.h\"");
    w.line("#include \"emf/common/EList.h\"");
    w.line("#include \"emf/common/Notification.h\"");
    w.line("#include <any>");
    w.line("#include <string>");
    w.line("#include <vector>");
    for (auto& inc : stdIncludes) w.line("#include " + inc);
    w.line();

    // 跨包前向声明（namespace 嵌套）
    for (auto& r : crossRefs) {
        w.line("namespace " + r.ns + " { class " + r.fullName.substr(r.fullName.rfind("::") + 2) + "; }");
    }
    if (!crossRefs.empty()) w.line();

    w.line("namespace " + ns + " {");
    w.line();
    // 同包兄弟类前向声明
    for (auto& s : siblings) w.line("class " + s + ";");
    w.line("class " + pkgCls + ";");
    w.line();
    w.line("class " + clsName + " : public emf::common::EObjectImpl {");
    w.line("public:");
    {
        IndentScope s(w);
        w.line(clsName + "();");
        w.line("~" + clsName + "() override;");
        w.line();
        // getters / setters
        for (auto& fi : features) {
            if (fi.isAttribute) {
                if (fi.isMulti) {
                    // 多值 EAttribute：EList<cppType>& getter（对齐多值 EReference）
                    w.line("emf::common::EList<" + fi.cppType + ">& get" + fi.capName + "();");
                } else {
                    // 关键字 feature 或与类名同名 feature 的 getter 用 get<Name> 前缀
                    // （避免 C++ 关键字冲突、与构造函数同名冲突、与成员变量 name_ 冲突）
                    // 非关键字 feature 的 getter 直接用 name（对齐 Java EMF 风格）
                    bool needGet = cppKeywords().count(fi.name) || (fi.name == clsName);
                    std::string getterM = needGet ? ("get" + fi.capName) : fi.name;
                    w.line(fi.cppType + " " + getterM + "() const;");
                    w.line("void set" + fi.capName + "(" + fi.cppType + " value);");
                }
            } else if (fi.isReference) {
                if (fi.isMulti) {
                    w.line("emf::common::EList<" + fi.targetFullName + "*>& get" + fi.capName + "();");
                } else {
                    w.line(fi.targetFullName + "* get" + fi.capName + "() const;");
                    w.line("void set" + fi.capName + "(" + fi.targetFullName + "* value);");
                }
            }
        }
        w.line();
        // 反射 override（对齐 Java EObjectImpl 反射方法）
        w.line("emf::ecore::EClass* eClass() const override;");
        w.line("std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;");
        w.line("void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;");
        w.line("bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;");
        w.line("void eUnset(const emf::ecore::EStructuralFeature* feature) override;");
        w.line("std::any eGet(int featureID) const override;");
        w.line("void eSet(int featureID, std::any value) override;");
        w.line("bool eIsSet(int featureID) const override;");
        w.line("void eUnset(int featureID) override;");
        // 类型化 eGet（方案 B 子集）：单值 attribute/reference 直接返回字段，避免 std::any 装箱
        w.line("bool eGetString(int featureID, std::string& out) const override;");
        w.line("bool eGetInt64(int featureID, int64_t& out) const override;");
        w.line("bool eGetBool(int featureID, bool& out) const override;");
        w.line("bool eGetDouble(int featureID, double& out) const override;");
        w.line("bool eGetEObject(int featureID, emf::common::EObject*& out) const override;");
        w.line("std::vector<emf::common::EObject*> eContents() const override;");
        w.line();
        w.line("static emf::ecore::EClass* eStaticClass();");
        w.line("static int eStaticFeatureCount();");
        w.line("static const emf::ecore::EStructuralFeature* eStaticFeature(int id);");
        w.line("static const std::string& eStaticFeatureName(int id);");
        w.line("static int eStaticFeatureID(const std::string& name);");
        w.line("// XML 元数据（来自 eAnnotation，模型驱动）");
        w.line("static const std::string& eStaticXmlName();");
        w.line("static const std::string& eStaticXmlNamePlural();");
        w.line("static const std::string& eStaticContentKind();");
        w.line("static const std::string& eStaticNsPrefix();");
        w.line("static const std::string& eStaticFeatureXmlName(int id);");
        w.line("static int eStaticFeatureSequenceOffset(int id);");
        w.line("static bool eStaticFeatureIsRoleElement(int id);");
        w.line("static bool eStaticFeatureIsRoleWrapperElement(int id);");
        w.line("static bool eStaticFeatureIsTypeElement(int id);");
        w.line("static bool eStaticFeatureIsTypeWrapperElement(int id);");
        w.line("static bool eStaticFeatureIsXmlAttribute(int id);");
        w.line("static int eStaticFeatureAprxmlRule(int id);");
        w.line("// XML 元数据虚函数 override（供运行时序列化器通过 EObject* 调用）");
        w.line("const std::string& eXmlName() const override { return eStaticXmlName(); }");
        w.line("const std::string& eXmlNamePlural() const override { return eStaticXmlNamePlural(); }");
        w.line("const std::string& eContentKind() const override { return eStaticContentKind(); }");
        w.line("const std::string& eNsPrefix() const override { return eStaticNsPrefix(); }");
        w.line("const std::string& eFeatureXmlName(int id) const override { return eStaticFeatureXmlName(id); }");
        w.line("int eFeatureSequenceOffset(int id) const override { return eStaticFeatureSequenceOffset(id); }");
        w.line("bool eFeatureIsRoleElement(int id) const override { return eStaticFeatureIsRoleElement(id); }");
        w.line("bool eFeatureIsRoleWrapperElement(int id) const override { return eStaticFeatureIsRoleWrapperElement(id); }");
        w.line("bool eFeatureIsTypeElement(int id) const override { return eStaticFeatureIsTypeElement(id); }");
        w.line("bool eFeatureIsTypeWrapperElement(int id) const override { return eStaticFeatureIsTypeWrapperElement(id); }");
        w.line("bool eFeatureIsXmlAttribute(int id) const override { return eStaticFeatureIsXmlAttribute(id); }");
        w.line("int eFeatureAprxmlRule(int id) const override { return eStaticFeatureAprxmlRule(id); }");
        w.line("int eFeatureCount() const override { return eStaticFeatureCount(); }");
        // eFeatureID 非虚（EObject.h 未声明为 virtual），不加 override
        w.line("int eFeatureID(const std::string& name) const { return eStaticFeatureID(name); }");
        w.line();
        // feature ID 常量
        w.line("// feature ID 常量（对齐 Java 中的 <Class>Package.<FEATURE>）");
        for (auto& fi : features) {
            w.line("static constexpr int " + fi.idConstName + " = " + std::to_string(fi.featureID) + ";");
        }
        w.line("static constexpr int FEATURE_COUNT__ = " + std::to_string(featureCount) + ";");
    }
    w.line();
    w.line("protected:");
    {
        IndentScope s(w);
        for (auto& fi : features) {
            std::string ftype;
            std::string init;
            if (fi.isAttribute) {
                if (fi.isMulti) {
                    ftype = "emf::common::EList<" + fi.cppType + ">*";
                    init = " = nullptr";
                } else {
                    ftype = fi.cppType;
                    init = fi.defLit;
                }
            } else if (fi.isReference) {
                if (fi.isMulti) {
                    ftype = "emf::common::EList<" + fi.targetFullName + "*>*";
                } else {
                    ftype = fi.targetFullName + "*";
                }
                init = " = nullptr";
            }
            w.line(ftype + " " + fi.fieldName + init + ";");
            if (fi.isAttribute && !fi.isMulti) {
                w.line("bool " + fi.name + "_isSet_ = false;");
            }
        }
        // eContents cache（适配 EObject.h 的 by-reference 返回签名）
        w.line("mutable std::vector<emf::common::EObject*> eContentsCache_;");
    }
    w.line("};");
    w.line();
    w.line("}  // namespace " + ns);
    return w.str();
}

std::string EClassEmitter::emitSource() const {
    IndentedWriter w;
    auto ns = classNs(eClass_, baseNamespace_, parentPath_);
    auto features = collectFeatures(eClass_, baseNamespace_);
    auto siblings = referencedSiblings(eClass_, features);
    auto crossRefs = collectCrossPkgRefs(eClass_, baseNamespace_, features);
    auto inheritedPkgIncludes = collectInheritedDeclaringPkgIncludes(eClass_, baseNamespace_, features);
    const std::string& clsName = eClass_->getName();
    std::string pkgCls = pkgClassName(eClass_);
    auto classMeta = EAnnotationReader::readClassMeta(eClass_);
    int featureCount = static_cast<int>(features.size());

    w.line("// <auto-generated/>");
    w.line("#include \"" + clsName + ".h\"");
    w.line("#include \"" + pkgCls + ".h\"");
    // 兄弟类头：eContents/eGet 的 static_cast 需要 complete type
    for (auto& s : siblings) w.line("#include \"" + s + ".h\"");
    // 跨包目标头
    for (auto& r : crossRefs) w.line("#include \"" + r.includePath + "\"");
    // 继承 feature 的声明包头：eStaticFeature 委托需要 declaringPkgClass complete type
    for (auto& inc : inheritedPkgIncludes) w.line("#include \"" + inc + "\"");
    w.line("#include \"emf/ecore/EcorePackage.h\"");
    w.line("#include \"emf/ecore/EcoreImpls.h\"");
    w.line("#include \"emf/common/ENotifier.h\"");
    w.line("#include \"emf/common/Notification.h\"");
    w.line("#include <unordered_map>");
    w.line();
    w.line("namespace " + ns + " {");
    w.line();

    // 轻量级 EList 回调函数（函数指针，替代 3 个 std::function 装箱，节省 75% 内存）：
    // 每个 multi-value feature 生成一个 static 函数，处理 add/remove/set 三种事件。
    // 在构造函数之前声明，构造函数中取地址传给 EList(cb, this, feat)。
    for (auto& fi : features) {
        if (!fi.isMulti) continue;
        std::string elemType = fi.isReference ? (fi.targetFullName + "*") : fi.cppType;
        std::string cbName = "__elistCb_" + clsName + "_" + fi.fieldName;
        w.line("static void " + cbName + "(void* ctx, const emf::ecore::EStructuralFeature* feat,");
        w.line("    emf::common::EListEvent ev, int pos, " + elemType + " oldV, " + elemType + " newV) {");
        {
            IndentScope s(w);
            w.line("auto* self = static_cast<" + clsName + "*>(ctx);");
            if (fi.isReference && fi.isContainment) {
                // containment：eContainer 维护 + 通知
                w.line("if (ev == emf::common::EListEvent::Add) {");
                {
                    IndentScope s2(w);
                    w.line("if (newV) { newV->setEContainer(self); newV->setEContainingFeature(feat); }");
                    w.line("emf::common::Notification n(emf::common::Notification::EventType::ADD, self, feat, -1, std::any(), std::any(static_cast<emf::common::EObject*>(newV)), pos);");
                    w.line("self->eNotify(n);");
                }
                w.line("} else if (ev == emf::common::EListEvent::Remove) {");
                {
                    IndentScope s2(w);
                    w.line("if (oldV) { if (oldV->eContainer() == self) oldV->setEContainer(nullptr); }");
                    w.line("emf::common::Notification n(emf::common::Notification::EventType::REMOVE, self, feat, -1, std::any(static_cast<emf::common::EObject*>(oldV)), std::any(), pos);");
                    w.line("self->eNotify(n);");
                }
                w.line("} else {");
                {
                    IndentScope s2(w);
                    w.line("emf::common::Notification n(emf::common::Notification::EventType::SET, self, feat, -1, std::any(static_cast<emf::common::EObject*>(oldV)), std::any(static_cast<emf::common::EObject*>(newV)), pos);");
                    w.line("self->eNotify(n);");
                }
                w.line("}");
            } else if (fi.isReference) {
                // 非 containment multi reference：仅通知（无 eContainer 维护）
                w.line("emf::common::Notification::EventType type =");
                w.line("    (ev == emf::common::EListEvent::Add) ? emf::common::Notification::EventType::ADD :");
                w.line("    (ev == emf::common::EListEvent::Remove) ? emf::common::Notification::EventType::REMOVE :");
                w.line("    emf::common::Notification::EventType::SET;");
                w.line("emf::common::Notification n(type, self, feat, -1, std::any(static_cast<emf::common::EObject*>(oldV)), std::any(static_cast<emf::common::EObject*>(newV)), pos);");
                w.line("self->eNotify(n);");
            } else {
                // 多值 EAttribute：值类型，无 EObject 转型
                w.line("emf::common::Notification::EventType type =");
                w.line("    (ev == emf::common::EListEvent::Add) ? emf::common::Notification::EventType::ADD :");
                w.line("    (ev == emf::common::EListEvent::Remove) ? emf::common::Notification::EventType::REMOVE :");
                w.line("    emf::common::Notification::EventType::SET;");
                w.line("emf::common::Notification n(type, self, feat, -1, std::any(oldV), std::any(newV), pos);");
                w.line("self->eNotify(n);");
            }
        }
        w.line("}");
        w.line();
    }

    // 构造函数：multi-value feature 用 EList(cb, this, feat) 初始化（函数指针，无 std::function 装箱）
    w.line(clsName + "::" + clsName + "() {");
    {
        IndentScope s(w);
        bool hasMulti = false;
        for (auto& fi : features) {
            if (!fi.isMulti) continue;
            hasMulti = true;
            std::string elemType = fi.isReference ? (fi.targetFullName + "*") : fi.cppType;
            std::string cbName = "__elistCb_" + clsName + "_" + fi.fieldName;
            w.line(fi.fieldName + " = new emf::common::EList<" + elemType +
                   ">(" + cbName + ", this, eStaticFeature(" + fi.idConstName + "));");
        }
        if (!hasMulti) {
            w.line("(void)0;  // 无 multi 字段，构造函数体留空");
        }
    }
    w.line("}");
    w.line();

    // 析构：delete 多值 feature 的 EList* 成员（构造时 new，=default 析构不释放会泄漏）。
    // 单值成员（string/int/指针）是 RAII 或非 owning，无需 delete。
    {
        bool hasAnyMulti = false;
        for (auto& fi : features) {
            if (fi.isMulti) hasAnyMulti = true;
        }
        w.line(clsName + "::~" + clsName + "() {");
        {
            IndentScope s(w);
            if (hasAnyMulti) {
                for (auto& fi : features) {
                    if (fi.isMulti) {
                        w.line("delete " + fi.fieldName + ";");
                    }
                }
            } else {
                w.line("(void)0;");
            }
        }
        w.line("}");
        w.line();
    }

    // eStaticClass / eClass
    w.line("emf::ecore::EClass* " + clsName + "::eStaticClass() {");
    { IndentScope s(w); w.line(pkgCls + "::initialize();"); w.line("return " + pkgCls + "::eINSTANCE->get" + clsName + "();"); }
    w.line("}");
    w.line();
    w.line("emf::ecore::EClass* " + clsName + "::eClass() const { return eStaticClass(); }");
    w.line();

    // eStaticFeatureCount
    w.line("int " + clsName + "::eStaticFeatureCount() { return FEATURE_COUNT__; }");
    w.line();

    // eStaticFeature(int)
    w.line("const emf::ecore::EStructuralFeature* " + clsName + "::eStaticFeature(int id) {");
    {
        IndentScope s(w);
        w.line("switch (id) {");
        {
            IndentScope s2(w);
            for (auto& fi : features) {
                std::string getter;
                if (fi.isInherited) {
                    getter = fi.declaringPkgNs + "::" + fi.declaringPkgClass + "::eINSTANCE->" + fi.pkgGetter + "()";
                } else {
                    getter = pkgCls + "::eINSTANCE->" + fi.pkgGetter + "()";
                }
                w.line("case " + fi.idConstName + ": return " + getter + ";");
            }
            w.line("default: return nullptr;");
        }
        w.line("}");
    }
    w.line("}");
    w.line();

    // eStaticFeatureName
    w.line("const std::string& " + clsName + "::eStaticFeatureName(int id) {");
    {
        IndentScope s(w);
        w.line("static const std::string n[] = {");
        {
            IndentScope s2(w);
            for (auto& fi : features) w.line("\"" + fi.rawName + "\",");
        }
        w.line("};");
        w.line("static const std::string empty;");
        w.line("if (id < 0 || id >= FEATURE_COUNT__) return empty;");
        w.line("return n[id];");
    }
    w.line("}");
    w.line();

    // eStaticFeatureID
    w.line("int " + clsName + "::eStaticFeatureID(const std::string& name) {");
    {
        IndentScope s(w);
        w.line("static const std::unordered_map<std::string, int> kMap = {");
        {
            IndentScope s2(w);
            for (auto& fi : features) {
                w.line("{\"" + fi.rawName + "\", " + fi.idConstName + "},");
            }
        }
        w.line("};");
        w.line("auto it = kMap.find(name);");
        w.line("return it != kMap.end() ? it->second : -1;");
    }
    w.line("}");
    w.line();

    // ===== ARTOP 类级元数据 =====
    auto emitClassStrMeta = [&](const std::string& method, const std::string& value) {
        w.line("const std::string& " + clsName + "::" + method + "() {");
        { IndentScope s(w); w.line("static const std::string v = \"" + value + "\";"); w.line("return v;"); }
        w.line("}");
        w.line();
    };
    emitClassStrMeta("eStaticXmlName", classMeta.xmlName.empty() ? clsName : classMeta.xmlName);
    emitClassStrMeta("eStaticXmlNamePlural", classMeta.xmlNamePlural.empty() ? (classMeta.xmlName.empty() ? clsName : classMeta.xmlName) : classMeta.xmlNamePlural);
    emitClassStrMeta("eStaticContentKind", classMeta.contentKind);
    emitClassStrMeta("eStaticNsPrefix", classMeta.nsPrefix);

    // ===== ARTOP feature 级元数据（static 数组）=====
    auto emitFeatureStrArr = [&](const std::string& method, const std::vector<std::string>& vals) {
        w.line("const std::string& " + clsName + "::" + method + "(int id) {");
        {
            IndentScope s(w);
            w.line("static const std::string n[] = {");
            { IndentScope s2(w); for (auto& v : vals) w.line("\"" + v + "\","); }
            w.line("};");
            w.line("static const std::string empty;");
            w.line("if (id < 0 || id >= FEATURE_COUNT__) return empty;");
            w.line("return n[id];");
        }
        w.line("}");
        w.line();
    };
    auto emitFeatureBoolArr = [&](const std::string& method, const std::vector<std::string>& vals) {
        w.line("bool " + clsName + "::" + method + "(int id) {");
        {
            IndentScope s(w);
            w.line("static const bool n[] = {");
            { IndentScope s2(w); for (auto& v : vals) w.line(v + ","); }
            w.line("};");
            w.line("if (id < 0 || id >= FEATURE_COUNT__) return false;");
            w.line("return n[id];");
        }
        w.line("}");
        w.line();
    };
    auto emitFeatureIntArr = [&](const std::string& method, const std::vector<std::string>& vals, bool zeroDefault) {
        w.line("int " + clsName + "::" + method + "(int id) {");
        {
            IndentScope s(w);
            w.line("static const int n[] = {");
            { IndentScope s2(w); for (auto& v : vals) w.line(v + ","); }
            w.line("};");
            w.line("if (id < 0 || id >= FEATURE_COUNT__) return " + std::string(zeroDefault ? "0" : "0") + ";");
            w.line("return n[id];");
        }
        w.line("}");
        w.line();
    };

    std::vector<std::string> xmlNames, seqOffs, roleE, roleW, typeE, typeW, xmlAttr, apr;
    for (auto& fi : features) {
        xmlNames.push_back(fi.xmlName);
        seqOffs.push_back(std::to_string(fi.sequenceOffset));
        roleE.push_back(fi.isRoleElement ? "true" : "false");
        roleW.push_back(fi.isRoleWrapperElement ? "true" : "false");
        typeE.push_back(fi.isTypeElement ? "true" : "false");
        typeW.push_back(fi.isTypeWrapperElement ? "true" : "false");
        xmlAttr.push_back(fi.isXmlAttribute ? "true" : "false");
        apr.push_back(std::to_string(fi.aprxmlRule));
    }
    emitFeatureStrArr("eStaticFeatureXmlName", xmlNames);
    emitFeatureIntArr("eStaticFeatureSequenceOffset", seqOffs, true);
    emitFeatureBoolArr("eStaticFeatureIsRoleElement", roleE);
    emitFeatureBoolArr("eStaticFeatureIsRoleWrapperElement", roleW);
    emitFeatureBoolArr("eStaticFeatureIsTypeElement", typeE);
    emitFeatureBoolArr("eStaticFeatureIsTypeWrapperElement", typeW);
    emitFeatureBoolArr("eStaticFeatureIsXmlAttribute", xmlAttr);
    emitFeatureIntArr("eStaticFeatureAprxmlRule", apr, true);

    // ===== getters / setters =====
    for (auto& fi : features) {
        if (fi.isAttribute) {
            if (fi.isMulti) {
                w.line("emf::common::EList<" + fi.cppType + ">& " + clsName +
                       "::get" + fi.capName + "() { return *" + fi.fieldName + "; }");
                w.line();
            } else {
                bool needGet = cppKeywords().count(fi.name) || (fi.name == clsName);
                std::string getterM = needGet ? ("get" + fi.capName) : fi.name;
                w.line(fi.cppType + " " + clsName + "::" + getterM + "() const { return " + fi.fieldName + "; }");
                w.line("void " + clsName + "::set" + fi.capName + "(" + fi.cppType + " value) {");
                {
                    IndentScope s(w);
                    // 捕获 oldValue（对齐 Java codegen：先取旧值再发 SET 通知）
                    w.line("std::any __old;");
                    w.line("if (eNotificationRequired()) __old = std::any(" + fi.fieldName + ");");
                    w.line(fi.fieldName + " = std::move(value);");
                    w.line(fi.name + "_isSet_ = true;");
                    w.line("emf::common::Notification n(emf::common::Notification::EventType::SET, this, eStaticFeature(" +
                           fi.idConstName + "), -1, __old, std::any(" + fi.fieldName + "));");
                    w.line("eNotify(n);");
                }
                w.line("}");
                w.line();
            }
        } else if (fi.isReference) {
            if (fi.isMulti) {
                w.line("emf::common::EList<" + fi.targetFullName + "*>& " + clsName +
                       "::get" + fi.capName + "() { return *" + fi.fieldName + "; }");
                w.line();
            } else {
                w.line(fi.targetFullName + "* " + clsName + "::get" + fi.capName + "() const { return " + fi.fieldName + "; }");
                w.line("void " + clsName + "::set" + fi.capName + "(" + fi.targetFullName + "* value) {");
                {
                    IndentScope s(w);
                    w.line("if (" + fi.fieldName + " != value) {");
                    {
                        IndentScope s2(w);
                        // 捕获 oldValue（对齐 Java codegen）
                        w.line("emf::common::EObject* __old = " + fi.fieldName + ";");
                        if (fi.isContainment) {
                            w.line("if (" + fi.fieldName + ") " + fi.fieldName + "->setEContainer(nullptr);");
                        }
                        w.line(fi.fieldName + " = value;");
                        if (fi.isContainment) {
                            w.line("if (" + fi.fieldName + ") {");
                            {
                                IndentScope s3(w);
                                w.line(fi.fieldName + "->setEContainer(this);");
                                w.line(fi.fieldName + "->setEContainingFeature(eStaticFeature(" + fi.idConstName + "));");
                            }
                            w.line("}");
                        }
                        w.line("emf::common::Notification n(emf::common::Notification::EventType::SET, this, eStaticFeature(" +
                               fi.idConstName + "), -1, std::any(static_cast<emf::common::EObject*>(__old)), std::any(static_cast<emf::common::EObject*>(value)));");
                        w.line("eNotify(n);");
                    }
                    w.line("}");
                }
                w.line("}");
                w.line();
            }
        }
    }

    // eContents（仅 containment 引用）
    w.line("std::vector<emf::common::EObject*> " + clsName + "::eContents() const {");
    {
        IndentScope s(w);
        w.line("eContentsCache_.clear();");
        for (auto& fi : features) {
            if (fi.isReference && fi.isContainment) {
                if (fi.isMulti) {
                    w.line("if (" + fi.fieldName + ") {");
                    {
                        IndentScope s2(w);
                        w.line("for (size_t i = 0; i < " + fi.fieldName + "->size(); ++i) {");
                        w.line("    eContentsCache_.push_back(static_cast<emf::common::EObject*>(" + fi.fieldName + "->get(i)));");
                        w.line("}");
                    }
                    w.line("}");
                } else {
                    w.line("if (" + fi.fieldName + ") eContentsCache_.push_back(static_cast<emf::common::EObject*>(" + fi.fieldName + "));");
                }
            }
        }
        w.line("return eContentsCache_;");
    }
    w.line("}");
    w.line();

    // eGet(feature) → 路由到 eGet(int)
    w.line("std::any " + clsName + "::eGet(const emf::ecore::EStructuralFeature* feature) const {");
    {
        IndentScope s(w);
        w.line("if (!feature) return std::any{};");
        w.line("int id = eStaticFeatureID(feature->getName());");
        w.line("if (id >= 0) return eGet(id);");
        w.line("return emf::common::EObjectImpl::eGet(feature);");
    }
    w.line("}");
    w.line();

    // eGet(int)
    w.line("std::any " + clsName + "::eGet(int featureID) const {");
    {
        IndentScope s(w);
        w.line("switch (featureID) {");
        {
            IndentScope s2(w);
            for (auto& fi : features) {
                if (fi.isAttribute) {
                    if (fi.isMulti) {
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            w.line("std::vector<" + fi.cppType + "> __v;");
                            w.line("if (" + fi.fieldName + ") {");
                            w.line("    for (size_t i = 0; i < " + fi.fieldName + "->size(); ++i) {");
                            w.line("        __v.push_back(" + fi.fieldName + "->get(i));");
                            w.line("    }");
                            w.line("}");
                            w.line("return std::any(__v);");
                        }
                        w.line("}");
                    } else {
                        w.line("case " + fi.idConstName + ": return std::any{" + fi.fieldName + "};");
                    }
                } else if (fi.isReference) {
                    if (fi.isMulti) {
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            // EObjectRefView 零拷贝：直接指向 EList 内部 vector 的指针数组。
                            // 安全性：T 单一非虚继承 EObjectImpl（offset 0），T* == EObjectImpl* 比特，
                            // 故 reinterpret_cast 到 const EObjectImpl* const* 安全。
                            // EObject 是 EObjectImpl 的虚基类，不能再 reinterpret_cast 到 EObject* const*
                            // （缺虚基类偏移调整，会指向错误子对象）。EObjectRefView 内部访问时
                            // 用 static_cast<const EObject*>(EObjectImpl*) 完成偏移调整。
                            w.line("if (!" + fi.fieldName + ") return std::any();");
                            w.line("auto& __v = " + fi.fieldName + "->data();");
                            w.line("return std::any(emf::common::EObjectRefView(");
                            w.line("    reinterpret_cast<const emf::common::EObjectImpl* const*>(__v.data()), __v.size()));");
                        }
                        w.line("}");
                    } else {
                        w.line("case " + fi.idConstName + ": return std::any(static_cast<emf::common::EObject*>(" + fi.fieldName + "));");
                    }
                }
            }
            w.line("default: return emf::common::EObject::eGet(featureID);");
        }
        w.line("}");
    }
    w.line("}");
    w.line();

    // 类型化 eGet（方案 B 子集）：单值 attribute/reference 直接返回字段，避免 std::any 装箱。
    // 每个 override 用 switch(featureID)，仅对匹配类型且单值的字段 out = field_; return true;
    // 不匹配/多值/default 返回 false，调用方 fallback 到 eGet(featureID)+std::any。
    // cppType → 选择哪个 override：
    //   std::string → eGetString（含 enum，enum 在 codegen 存为 std::string）
    //   bool        → eGetBool
    //   int32_t/int64_t/uint*/int*/long → eGetInt64
    //   double      → eGetDouble（float 不生成，避免 float→double 精度差异）
    auto isIntCppType = [](const std::string& t) {
        return t == "int64_t" || t == "int32_t" || t == "int16_t" || t == "int8_t" ||
               t == "uint64_t" || t == "uint32_t" || t == "uint16_t" || t == "uint8_t" ||
               t == "long" || t == "int";
    };
    // 生成单个类型化 override。kindTag: "String"/"Int64"/"Bool"。cppTypeMatch 判断该字段是否匹配本 override。
    auto emitTypedGet = [&](const std::string& kindTag, const std::string& outType,
                            const std::string& cppTypeMatch) {
        w.line("bool " + clsName + "::eGet" + kindTag + "(int featureID, " + outType + " out) const {");
        {
            IndentScope s(w);
            w.line("switch (featureID) {");
            {
                IndentScope s2(w);
                bool any = false;
                for (auto& fi : features) {
                    if (!fi.isAttribute || fi.isMulti) continue;
                    if (fi.cppType != cppTypeMatch) continue;
                    w.line("case " + fi.idConstName + ": out = " + fi.fieldName + "; return true;");
                    any = true;
                }
                (void)any;
                w.line("default: return false;");
            }
            w.line("}");
        }
        w.line("}");
        w.line();
    };
    emitTypedGet("String", "std::string&", "std::string");
    emitTypedGet("Bool", "bool&", "bool");
    // eGetInt64：匹配所有整型 cppType
    {
        w.line("bool " + clsName + "::eGetInt64(int featureID, int64_t& out) const {");
        {
            IndentScope s(w);
            w.line("switch (featureID) {");
            {
                IndentScope s2(w);
                for (auto& fi : features) {
                    if (!fi.isAttribute || fi.isMulti) continue;
                    if (!isIntCppType(fi.cppType)) continue;
                    w.line("case " + fi.idConstName + ": out = static_cast<int64_t>(" + fi.fieldName + "); return true;");
                }
                w.line("default: return false;");
            }
            w.line("}");
        }
        w.line("}");
        w.line();
    }
    // eGetEObject：单值 reference（containment 或非 containment），直接返回字段指针
    {
        w.line("bool " + clsName + "::eGetEObject(int featureID, emf::common::EObject*& out) const {");
        {
            IndentScope s(w);
            w.line("switch (featureID) {");
            {
                IndentScope s2(w);
                for (auto& fi : features) {
                    if (!fi.isReference || fi.isMulti) continue;
                    w.line("case " + fi.idConstName + ": out = static_cast<emf::common::EObject*>(" + fi.fieldName + "); return true;");
                }
                w.line("default: return false;");
            }
            w.line("}");
        }
        w.line("}");
        w.line();
    }
    // eGetDouble：单值 double attribute（float 不生成，避免 float→double 精度差异破坏 round-trip）
    {
        w.line("bool " + clsName + "::eGetDouble(int featureID, double& out) const {");
        {
            IndentScope s(w);
            w.line("switch (featureID) {");
            {
                IndentScope s2(w);
                for (auto& fi : features) {
                    if (!fi.isAttribute || fi.isMulti) continue;
                    if (fi.cppType != "double") continue;
                    w.line("case " + fi.idConstName + ": out = " + fi.fieldName + "; return true;");
                }
                w.line("default: return false;");
            }
            w.line("}");
        }
        w.line("}");
        w.line();
    }

    // eSet(feature) → 路由到 eSet(int)
    w.line("void " + clsName + "::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {");
    {
        IndentScope s(w);
        w.line("if (!feature) return;");
        w.line("int id = eStaticFeatureID(feature->getName());");
        w.line("if (id >= 0) eSet(id, value);");
        w.line("else emf::common::EObjectImpl::eSet(feature, std::move(value));");
    }
    w.line("}");
    w.line();

    // eSet(int, std::any)
    w.line("void " + clsName + "::eSet(int featureID, std::any value) {");
    {
        IndentScope s(w);
        w.line("switch (featureID) {");
        {
            IndentScope s2(w);
            for (auto& fi : features) {
                if (fi.isAttribute) {
                    if (fi.isMulti) {
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            // 多值 EAttribute：vector<T> = 替换（先 clear），T = 追加（不 clear）
                            // 对齐 Java EList.add（单值追加）vs EList.set/addAll（批量替换）
                            w.line("if (value.type() == typeid(std::vector<" + fi.cppType + ">)) {");
                            w.line("    if (" + fi.fieldName + ") " + fi.fieldName + "->clear();");
                            w.line("    auto __vec = std::any_cast<std::vector<" + fi.cppType + ">>(value);");
                            w.line("    if (" + fi.fieldName + ") for (auto& __x : __vec) " + fi.fieldName + "->add(__x);");
                            w.line("} else if (value.type() == typeid(" + fi.cppType + ")) {");
                            w.line("    if (" + fi.fieldName + ") " + fi.fieldName + "->add(std::any_cast<" + fi.cppType + ">(value));");
                            w.line("}");
                        }
                        w.line("} break;");
                    } else {
                        w.line("case " + fi.idConstName + ": set" + fi.capName + "(std::any_cast<" + fi.cppType + ">(value)); break;");
                    }
                } else if (fi.isReference) {
                    if (fi.isMulti) {
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            // 多值 EReference：EList*/vector = 替换（先 clear），单值 = 追加（不 clear）
                            // 对齐 Java EList.add（单值追加）vs EList.addAll（批量替换）
                            // 关键：单值追加分支不 clear，避免 Loader addOrSet 触发 clear+re-add 循环
                            w.line("if (value.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {");
                            w.line("    if (" + fi.fieldName + ") " + fi.fieldName + "->clear();");
                            w.line("    auto* __lst = std::any_cast<emf::common::EList<emf::common::EObject*>*>(value);");
                            w.line("    if (__lst) {");
                            w.line("        for (size_t i = 0; i < __lst->size(); ++i) {");
                            w.line("            auto* __b = dynamic_cast<" + fi.targetFullName + "*>(__lst->get(i));");
                            // 扁平继承：所有生成类直接继承 EObjectImpl（offset 0），但 EObject 是虚基类。
                            // reinterpret_cast 不调整虚基类偏移，导致指针指向错误子对象。
                            // 修复：先 dynamic_cast<EObjectImpl*> 正确调整虚基类偏移，再 reinterpret_cast（安全，布局一致）。
                            w.line("            if (!__b) __b = reinterpret_cast<" + fi.targetFullName + "*>(dynamic_cast<emf::common::EObjectImpl*>(__lst->get(i)));");
                            w.line("            if (__b && " + fi.fieldName + ") " + fi.fieldName + "->add(__b);");
                            w.line("        }");
                            w.line("    }");
                            w.line("} else if (value.type() == typeid(std::vector<emf::common::EObject*>)) {");
                            w.line("    if (" + fi.fieldName + ") " + fi.fieldName + "->clear();");
                            w.line("    auto __vec = std::any_cast<std::vector<emf::common::EObject*>>(value);");
                            w.line("    for (auto* __e : __vec) {");
                            w.line("        auto* __b = dynamic_cast<" + fi.targetFullName + "*>(__e);");
                            w.line("        if (!__b) __b = reinterpret_cast<" + fi.targetFullName + "*>(dynamic_cast<emf::common::EObjectImpl*>(__e));");
                            w.line("        if (__b && " + fi.fieldName + ") " + fi.fieldName + "->add(__b);");
                            w.line("    }");
                            w.line("} else if (value.type() == typeid(" + fi.targetFullName + "*)) {");
                            w.line("    auto* __b = std::any_cast<" + fi.targetFullName + "*>(value);");
                            w.line("    if (__b && " + fi.fieldName + ") " + fi.fieldName + "->add(__b);");
                            w.line("} else if (value.type() == typeid(emf::common::EObject*)) {");
                            w.line("    auto* __o = std::any_cast<emf::common::EObject*>(value);");
                            w.line("    auto* __b = __o ? dynamic_cast<" + fi.targetFullName + "*>(__o) : nullptr;");
                            w.line("    if (!__b && __o) __b = reinterpret_cast<" + fi.targetFullName + "*>(dynamic_cast<emf::common::EObjectImpl*>(__o));");
                            w.line("    if (__b && " + fi.fieldName + ") " + fi.fieldName + "->add(__b);");
                            w.line("}");
                        }
                        w.line("} break;");
                    } else {
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            w.line("if (value.type() == typeid(" + fi.targetFullName + "*)) {");
                            w.line("    set" + fi.capName + "(std::any_cast<" + fi.targetFullName + "*>(value));");
                            w.line("} else if (value.type() == typeid(emf::common::EObject*)) {");
                            w.line("    auto* __o = std::any_cast<emf::common::EObject*>(value);");
                            w.line("    auto* __t = __o ? dynamic_cast<" + fi.targetFullName + "*>(__o) : nullptr;");
                            w.line("    if (!__t && __o) __t = reinterpret_cast<" + fi.targetFullName + "*>(dynamic_cast<emf::common::EObjectImpl*>(__o));");
                            w.line("    set" + fi.capName + "(__t);");
                            w.line("}");
                        }
                        w.line("} break;");
                    }
                }
            }
            w.line("default: emf::common::EObject::eSet(featureID, value); break;");
        }
        w.line("}");
    }
    w.line("}");
    w.line();

    // eIsSet(feature) → 路由到 eIsSet(int)
    w.line("bool " + clsName + "::eIsSet(const emf::ecore::EStructuralFeature* feature) const {");
    {
        IndentScope s(w);
        w.line("if (!feature) return false;");
        w.line("int id = eStaticFeatureID(feature->getName());");
        w.line("if (id >= 0) return eIsSet(id);");
        w.line("return emf::common::EObjectImpl::eIsSet(feature);");
    }
    w.line("}");
    w.line();

    // eIsSet(int)
    w.line("bool " + clsName + "::eIsSet(int featureID) const {");
    {
        IndentScope s(w);
        w.line("switch (featureID) {");
        {
            IndentScope s2(w);
            for (auto& fi : features) {
                if (fi.isAttribute) {
                    if (fi.isMulti) {
                        w.line("case " + fi.idConstName + ": return " + fi.fieldName + " && " + fi.fieldName + "->size() > 0;");
                    } else {
                        w.line("case " + fi.idConstName + ": return " + fi.name + "_isSet_;");
                    }
                } else if (fi.isReference) {
                    if (fi.isMulti) {
                        w.line("case " + fi.idConstName + ": return " + fi.fieldName + " && " + fi.fieldName + "->size() > 0;");
                    } else {
                        w.line("case " + fi.idConstName + ": return " + fi.fieldName + " != nullptr;");
                    }
                }
            }
            w.line("default: return emf::common::EObject::eIsSet(featureID);");
        }
        w.line("}");
    }
    w.line("}");
    w.line();

    // eUnset(feature) → 路由到 eUnset(int)
    w.line("void " + clsName + "::eUnset(const emf::ecore::EStructuralFeature* feature) {");
    {
        IndentScope s(w);
        w.line("if (!feature) return;");
        w.line("int id = eStaticFeatureID(feature->getName());");
        w.line("if (id >= 0) eUnset(id);");
        w.line("else emf::common::EObjectImpl::eUnset(feature);");
    }
    w.line("}");
    w.line();

    // eUnset(int) —— 对齐 Java codegen：发 UNSET 通知（而非调 setter 发 SET）
    w.line("void " + clsName + "::eUnset(int featureID) {");
    {
        IndentScope s(w);
        w.line("switch (featureID) {");
        {
            IndentScope s2(w);
            for (auto& fi : features) {
                if (fi.isAttribute) {
                    if (fi.isMulti) {
                        // 多值 attribute eUnset：clear + 发 UNSET（对齐 Java codegen）
                        // 注意：多值 attribute 没有 _isSet_ 标志（eIsSet 检查 size()>0）
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            w.line("if (" + fi.fieldName + ") " + fi.fieldName + "->clear();");
                            w.line("emf::common::Notification n(emf::common::Notification::EventType::UNSET, this, eStaticFeature(" +
                                   fi.idConstName + "), -1, std::any(), std::any());");
                            w.line("eNotify(n);");
                        }
                        w.line("} break;");
                    } else {
                        std::string uv = unsetValue(fi.cppType);
                        if (uv.empty()) uv = fi.cppType + "()";
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            w.line("std::any __old = eNotificationRequired() ? std::any(" + fi.fieldName + ") : std::any();");
                            w.line(fi.fieldName + " = " + uv + ";");
                            w.line(fi.name + "_isSet_ = false;");
                            w.line("emf::common::Notification n(emf::common::Notification::EventType::UNSET, this, eStaticFeature(" +
                                   fi.idConstName + "), -1, __old, std::any());");
                            w.line("eNotify(n);");
                        }
                        w.line("} break;");
                    }
                } else if (fi.isReference) {
                    if (fi.isMulti) {
                        // 多值 reference eUnset：clear + 发 UNSET（对齐 Java codegen）
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            w.line("if (" + fi.fieldName + ") " + fi.fieldName + "->clear();");
                            w.line("emf::common::Notification n(emf::common::Notification::EventType::UNSET, this, eStaticFeature(" +
                                   fi.idConstName + "), -1, std::any(), std::any());");
                            w.line("eNotify(n);");
                        }
                        w.line("} break;");
                    } else {
                        w.line("case " + fi.idConstName + ": {");
                        {
                            IndentScope s3(w);
                            w.line("emf::common::EObject* __old = " + fi.fieldName + ";");
                            if (fi.isContainment) {
                                w.line("if (" + fi.fieldName + ") " + fi.fieldName + "->setEContainer(nullptr);");
                            }
                            w.line(fi.fieldName + " = nullptr;");
                            w.line("emf::common::Notification n(emf::common::Notification::EventType::UNSET, this, eStaticFeature(" +
                                   fi.idConstName + "), -1, std::any(static_cast<emf::common::EObject*>(__old)), std::any());");
                            w.line("eNotify(n);");
                        }
                        w.line("} break;");
                    }
                }
            }
            w.line("default: emf::common::EObject::eUnset(featureID); break;");
        }
        w.line("}");
    }
    w.line("}");
    w.line();

    w.line("}  // namespace " + ns);
    return w.str();
}

}  // namespace emf::ecore::codegen
