// PackageEmitter.cpp —— 生成 <Pkg>Package.h/.cpp
// 对齐 Java: <Pkg>Package.java / <Pkg>PackageImpl.java
//
// 设计：生成的 Package 继承 emf::ecore::EPackageImpl（对齐 Java PackageImpl extends EPackageImpl）
//   - setEFactoryInstance 继承自 EPackageImpl
//   - EPackageRegistry::put 接受 EPackage*（LibraryPackage IS-A EPackage）
//   - 静态成员：<ClassName>_class_ / <ClassName>_<Feature>_attr_ / <ClassName>_<Feature>_ref_
//
// 修复点（对齐 .build_cache golden reference）：
//   M3: 防循环依赖（static bool initializing_ + 提前注册 Package）
//   M4: EAnnotation 写入（重建 source/detail）
//   M5: addESuperType（跨包全限定引用）
//   S2: 跨包 EReference / EAttribute 类型全限定引用 + 依赖包 initialize + include
//   S8: EEnum / EDataType 生成
#include "emf/ecore/codegen/PackageEmitter.h"
#include "emf/ecore/codegen/EAnnotationReader.h"
#include "emf/ecore/codegen/StringUtils.h"
#include "emf/ecore/codegen/TypeMapper.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include <algorithm>
#include <set>
#include <string>
#include <unordered_map>
#include <vector>

namespace emf::ecore::codegen {

// ===== 自由函数（header 中声明）=====

std::string epackageFullPath(emf::ecore::EPackage* pkg) {
    if (!pkg) return "";
    std::string path = pkg->getName();
    auto* parent = pkg->getESuperPackage();
    while (parent) {
        path = parent->getName() + "/" + path;
        parent = parent->getESuperPackage();
    }
    return path;
}

std::string pathToNs(const std::string& slashPath) {
    std::string r;
    r.reserve(slashPath.size() * 2);
    for (char c : slashPath) {
        if (c == '/') r += "::";
        else r += c;
    }
    return r;
}

// ===== PackageEmitter 实现 =====

namespace {

// 当前包的 C++ namespace：baseNamespace::parentPath::pkgName
std::string computeNsPath(emf::ecore::EPackage* p, const std::string& base, const std::string& parentPath) {
    std::string fullName = parentPath.empty() ? p->getName()
                                              : (parentPath + "/" + p->getName());
    std::string r;
    r.reserve(fullName.size() * 2);
    for (char c : fullName) {
        if (c == '/') r += "::";
        else r += c;
    }
    if (base.empty()) return r;
    return base + "::" + r;
}

// 任意目标包的 C++ namespace：baseNamespace::pathToNs(epackageFullPath(pkg))
// 用 epackageFullPath 走 super package 链，得到从根到目标包的完整路径（不含 baseNamespace）。
std::string targetNsPath(emf::ecore::EPackage* p, const std::string& base) {
    std::string ns = pathToNs(epackageFullPath(p));
    if (base.empty()) return ns;
    return base + "::" + ns;
}

// 目标包类名（capitalizeFirst(pkg.name) + "Package"）
std::string targetPkgClassName(emf::ecore::EPackage* p) {
    return capitalizeFirst(p->getName()) + "Package";
}

// 目标包头文件 include 路径（disk path + PkgClass.h）
std::string targetIncludePath(emf::ecore::EPackage* p) {
    return epackageFullPath(p) + "/" + targetPkgClassName(p) + ".h";
}

// 判断是否内建 Ecore 类型（EString/EInt 等属于 EcorePackage）
bool isBuiltinEcoreType(emf::ecore::EDataType* dt) {
    if (!dt) return false;
    auto* pkg = dt->getEPackage();
    if (!pkg) return false;
    return std::string(pkg->getNsURI()) == std::string(emf::ecore::EcorePackage::eNS_URI);
}

// 字符串字面量转义（处理 " 和 \ 和换行符）
// 对齐 Java: GenModel 字符串字面量生成 —— 换行符必须转义为 \n，否则生成的 C++ 代码语法错误
std::string escapeStr(const std::string& s) {
    std::string r;
    r.reserve(s.size() + 2);
    for (char c : s) {
        if (c == '"' || c == '\\') r += '\\';
        else if (c == '\n') { r += '\\'; r += 'n'; continue; }
        else if (c == '\r') { r += '\\'; r += 'r'; continue; }
        else if (c == '\t') { r += '\\'; r += 't'; continue; }
        r += c;
    }
    return r;
}

std::vector<emf::ecore::EClass*> collectClasses(emf::ecore::EPackage* p) {
    std::vector<emf::ecore::EClass*> out;
    for (auto* c : p->getEClassifiers()) {
        if (auto* cls = dynamic_cast<emf::ecore::EClass*>(c)) out.push_back(cls);
    }
    return out;
}

// S8: 收集用户自定义 EEnum
std::vector<emf::ecore::EEnum*> collectEEnums(emf::ecore::EPackage* p) {
    std::vector<emf::ecore::EEnum*> out;
    for (auto* c : p->getEClassifiers()) {
        if (auto* e = dynamic_cast<emf::ecore::EEnum*>(c)) out.push_back(e);
    }
    return out;
}

// S8: 收集用户自定义 EDataType（非 EEnum、非内建 Ecore）
std::vector<emf::ecore::EDataType*> collectEDataTypes(emf::ecore::EPackage* p) {
    std::vector<emf::ecore::EDataType*> out;
    for (auto* c : p->getEClassifiers()) {
        auto* d = dynamic_cast<emf::ecore::EDataType*>(c);
        if (!d) continue;
        if (dynamic_cast<emf::ecore::EEnum*>(c)) continue;  // EEnum 单独处理
        if (isBuiltinEcoreType(d)) continue;                // 内建类型用 EcorePackage
        out.push_back(d);
    }
    return out;
}

// 收集所有 EStructuralFeature（按类分组）
struct FeatureInfo {
    emf::ecore::EClass* cls;
    emf::ecore::EStructuralFeature* sf;
    bool isAttribute;
    bool isReference;
    std::string memberName;   // <ClassName>_<Feature>_attr_ 或 _ref_
    std::string getterName;   // get<ClassName>_<Feature>
};

// PackageEmitter 的 feature 名 sanitize 复用 StringUtils::safeIdent（更完善）。
// 部分 ecore 模型（如 autosar448 顶层包）的 EStructuralFeature.name 含 '-'（如 "SHORT-NAME"），
// 直接用于 getter/member 名会产出非法标识符（getAUTOSAR_SHORT-NAME）。
// safeIdent 将非 [A-Za-z0-9_] 字符替换为 '_'，仅影响 C++ 标识符；
// setName 仍用原始 sf->getName()，反射语义不变。

std::vector<FeatureInfo> collectFeatures(emf::ecore::EPackage* p) {
    std::vector<FeatureInfo> out;
    for (auto* c : collectClasses(p)) {
        std::string clsName = c->getName();
        for (auto* sf : c->getEStructuralFeatures()) {
            FeatureInfo fi;
            fi.cls = c;
            fi.sf = sf;
            fi.isAttribute = (dynamic_cast<emf::ecore::EAttribute*>(sf) != nullptr);
            fi.isReference = (dynamic_cast<emf::ecore::EReference*>(sf) != nullptr);
            // sanitize：feature 名含 '-' 时替换为 '_'，保证 getter/member 名合法
            std::string featName = capitalizeFirst(safeIdent(sf->getName()));
            std::string suffix = fi.isReference ? "_ref_" : "_attr_";
            fi.memberName = clsName + "_" + safeIdent(sf->getName()) + suffix;
            fi.getterName = "get" + clsName + "_" + featName;
            out.push_back(fi);
        }
    }
    return out;
}

// 内建 EDataType 名 → EcorePackage getter 调用
std::string dataTypeGetter(const std::string& typeName) {
    static const std::unordered_map<std::string, std::string> m = {
        {"EString",     "emf::ecore::EcorePackage::instance().getEDataType_EString()"},
        {"EBoolean",    "emf::ecore::EcorePackage::instance().getEDataType_EBoolean()"},
        {"EInt",        "emf::ecore::EcorePackage::instance().getEDataType_EInt()"},
        {"ELong",       "emf::ecore::EcorePackage::instance().getEDataType_ELong()"},
        {"EDouble",     "emf::ecore::EcorePackage::instance().getEDataType_EDouble()"},
        {"EFloat",      "emf::ecore::EcorePackage::instance().getEDataType_EFloat()"},
        {"EShort",      "emf::ecore::EcorePackage::instance().getEDataType_EShort()"},
        {"EByte",       "emf::ecore::EcorePackage::instance().getEDataType_EByte()"},
        {"EChar",       "emf::ecore::EcorePackage::instance().getEDataType_EChar()"},
        {"EDate",       "emf::ecore::EcorePackage::instance().getEDataType_EDate()"},
        {"EBigInteger", "emf::ecore::EcorePackage::instance().getEDataType_EBigInteger()"},
        {"EBigDecimal", "emf::ecore::EcorePackage::instance().getEDataType_EBigDecimal()"},
        {"EJavaObject", "emf::ecore::EcorePackage::instance().getEDataType_EJavaObject()"},
    };
    auto it = m.find(typeName);
    if (it != m.end()) return it->second;
    // 未知内建类型：通过 getEPackage()->getEClassifier 查找
    return "dynamic_cast<emf::ecore::EDataType*>("
           "emf::ecore::EcorePackage::instance().getEPackage()->getEClassifier(\"" + typeName + "\"))";
}

// EAttribute 类型的引用表达式（S2: 跨包全限定）
std::string attributeTypeExpr(emf::ecore::EAttribute* attr, emf::ecore::EPackage* self,
                              const std::string& base) {
    auto* dt = attr->getEAttributeType();
    if (!dt) return dataTypeGetter("EString");
    if (isBuiltinEcoreType(dt)) return dataTypeGetter(dt->getName());
    auto* dtPkg = dt->getEPackage();
    std::string typeName = dt->getName();
    // EEnum 的静态字段名为 <Name>_enum_（而非 _dt_），见 collectEEnums/emitHeader
    bool isEnum = (dynamic_cast<emf::ecore::EEnum*>(dt) != nullptr);
    std::string suffix = isEnum ? "_enum_" : "_dt_";
    if (!dtPkg || dtPkg == self) {
        // 同包：用本包静态成员
        return typeName + suffix;
    }
    // 跨包：全限定 getter（getter 名对 EEnum/EDataType 一致：get<CapName>）
    return targetNsPath(dtPkg, base) + "::" + targetPkgClassName(dtPkg) +
           "::eINSTANCE->get" + capitalizeFirst(typeName) + "()";
}

// EReference 目标的引用表达式（S2: 跨包全限定）
std::string referenceTargetExpr(emf::ecore::EReference* ref, emf::ecore::EPackage* self,
                                const std::string& base) {
    auto* tgt = ref->getEReferenceType();
    if (!tgt) return "nullptr";
    std::string tgtName = tgt->getName();
    auto* tgtPkg = tgt->getEPackage();
    // Ecore EObject/EClass 等元类：用 emf::ecore::EcorePackage 的标准 getter
    // （不生成跨包 artop::ecore namespace，对齐 EClassEmitter 的 isBuiltinEObject 逻辑）
    // EcorePackage 类本身没有 getEClassifier(name)，通过 getEPackage() 拿到 EPackage 接口再查。
    if (tgtPkg && std::string(tgtPkg->getNsURI()) ==
                  std::string(emf::ecore::EcorePackage::eNS_URI)) {
        // getEPackage()->getEClassifier 返回 EClassifier*，setEReferenceType 需要 EClass*
        return "dynamic_cast<emf::ecore::EClass*>(emf::ecore::EcorePackage::instance().getEPackage()->getEClassifier(\"" + tgtName + "\"))";
    }
    if (!tgtPkg || tgtPkg == self) {
        return tgtName + "_class_";
    }
    return targetNsPath(tgtPkg, base) + "::" + targetPkgClassName(tgtPkg) + "::" + tgtName + "_class_";
}

// ESuperType 的引用表达式（M5: 跨包全限定）
// 与 referenceTargetExpr 同样：Ecore EObject/EClass 等元类用 emf::ecore::EcorePackage 的标准 getter，
// 不生成跨包 artop::ecore namespace。
std::string superTypeExpr(emf::ecore::EClass* sup, emf::ecore::EPackage* self,
                          const std::string& base) {
    auto* supPkg = sup->getEPackage();
    std::string supName = sup->getName();
    if (supPkg && std::string(supPkg->getNsURI()) ==
                  std::string(emf::ecore::EcorePackage::eNS_URI)) {
        // getEPackage()->getEClassifier 返回 EClassifier*，addESuperType 需要 EClass*
        return "dynamic_cast<emf::ecore::EClass*>(emf::ecore::EcorePackage::instance().getEPackage()->getEClassifier(\"" + supName + "\"))";
    }
    if (!supPkg || supPkg == self) {
        return supName + "_class_";
    }
    return targetNsPath(supPkg, base) + "::" + targetPkgClassName(supPkg) + "::" + supName + "_class_";
}

// S2/M5: 收集依赖包（跨包 EReference 目标、ESuperType、用户自定义 EAttribute 类型）
// 跳过内建 Ecore 包与自身。结果按 nsPath 排序保证确定性。
std::vector<emf::ecore::EPackage*> collectDependencyPackages(emf::ecore::EPackage* self,
                                                              const std::string& base) {
    std::vector<emf::ecore::EPackage*> deps;
    std::set<emf::ecore::EPackage*> seen;
    auto addDep = [&](emf::ecore::EPackage* p) {
        if (!p || p == self || seen.count(p)) return;
        if (std::string(p->getNsURI()) == std::string(emf::ecore::EcorePackage::eNS_URI)) return;
        seen.insert(p);
        deps.push_back(p);
    };
    for (auto* c : collectClasses(self)) {
        for (auto* sup : c->getESuperTypes()) {
            if (sup) addDep(sup->getEPackage());
        }
        for (auto* sf : c->getEStructuralFeatures()) {
            if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
                if (auto* tgt = ref->getEReferenceType()) addDep(tgt->getEPackage());
            } else if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                auto* dt = attr->getEAttributeType();
                if (dt && !isBuiltinEcoreType(dt)) addDep(dt->getEPackage());
            }
        }
    }
    std::sort(deps.begin(), deps.end(), [&](emf::ecore::EPackage* a, emf::ecore::EPackage* b) {
        return targetNsPath(a, base) < targetNsPath(b, base);
    });
    return deps;
}

// M4: 发射 EAnnotation 重建块（对 element 的所有 annotations 重建 source/detail）
// 每个 annotation 用独立 { } 作用域，避免 __ann 变量名冲突（多个 annotation 块）
void emitAnnotations(IndentedWriter& w, emf::ecore::EModelElement* el,
                     const std::string& memberExpr) {
    if (!el) return;
    auto& anns = el->getEAnnotations();
    if (anns.empty()) return;
    for (auto* ann : anns) {
        if (!ann) continue;
        w.line("{");
        {
            IndentScope s(w);
            w.line("auto* __ann = emf::ecore::EcoreFactory::instance().createEAnnotation();");
            w.line("__ann->setSource(\"" + escapeStr(ann->getSource()) + "\");");
            for (auto& kv : ann->getDetails()) {
                w.line("__ann->setDetail(\"" + escapeStr(kv.first) + "\", \"" +
                       escapeStr(kv.second) + "\");");
            }
            w.line(memberExpr + "->addEAnnotation(__ann);");
        }
        w.line("}");
    }
}

}  // namespace

PackageEmitter::PackageEmitter(emf::ecore::EPackage* package, const std::string& baseNamespace,
                               const std::string& parentPath)
    : package_(package), baseNamespace_(baseNamespace), parentPath_(parentPath) {}

std::string PackageEmitter::includeGuard() const {
    auto ns = computeNsPath(package_, baseNamespace_, parentPath_);
    std::string guard;
    for (char c : ns) {
        if (c == ':') continue;
        guard += static_cast<char>(std::toupper(static_cast<unsigned char>(c)));
        if (c == ':') guard += '_';
    }
    guard += "_";
    std::string cn = className();
    for (char c : cn) guard += static_cast<char>(std::toupper(static_cast<unsigned char>(c)));
    guard += "_H";
    return guard;
}

std::string PackageEmitter::nsPath() const {
    return computeNsPath(package_, baseNamespace_, parentPath_);
}

std::string PackageEmitter::emitHeader() const {
    IndentedWriter w;
    auto ns = nsPath();
    auto classes = collectClasses(package_);
    auto features = collectFeatures(package_);
    auto enums = collectEEnums(package_);
    auto datatypes = collectEDataTypes(package_);
    std::string factoryClass = capitalizeFirst(package_->getName()) + "Factory";

    w.line("// <auto-generated/>");
    w.line("#pragma once");
    w.line("#include \"emf/common/EPackage.h\"");
    w.line("#include \"emf/ecore/EcoreImpls.h\"");
    w.line("#include \"emf/common/EList.h\"");
    w.line("#include <string>");
    w.line("#include <vector>");
    w.line();
    w.line("namespace " + ns + " {");
    w.line();
    w.line("class " + factoryClass + ";");
    w.line();
    w.line("class " + className() + " : public emf::ecore::EPackageImpl {");
    w.line("public:");
    {
        IndentScope s(w);
        w.line(className() + "();");
        w.line("~" + className() + "() override = default;");
        w.line();
        w.line("// 元信息常量（对齐 Java: <Pkg>Package.eNAME / eNS_URI / eNS_PREFIX）");
        w.line("static const std::string eNAME;");
        w.line("static const std::string eNS_URI;");
        w.line("static const std::string eNS_PREFIX;");
        w.line();
        w.line("// 单例 + 初始化");
        w.line("static " + className() + "* instance();");
        w.line("static " + className() + "* eINSTANCE;");
        w.line("static bool initializing_;  // 防止循环依赖导致的无限递归");
        w.line("static void initialize();");
        w.line();
        // EClass 索引常量（对齐 Java <Pkg>Package.<NAME> = <i>）
        if (!classes.empty()) {
            for (size_t i = 0; i < classes.size(); ++i) {
                std::string up;
                up.reserve(classes[i]->getName().size());
                for (char c : classes[i]->getName()) {
                    up += static_cast<char>(std::toupper(static_cast<unsigned char>(c)));
                }
                w.line("static const int " + up + " = " + std::to_string(i) + ";");
            }
            w.line();
        }
        // EClass 访问器
        for (auto* c : classes) {
            w.line("emf::ecore::EClass* get" + c->getName() + "() const { return " +
                   c->getName() + "_class_; }");
        }
        if (!classes.empty()) w.line();
        // EAttribute / EReference 访问器
        for (auto& fi : features) {
            std::string retType = fi.isReference ? "emf::ecore::EReference*" : "emf::ecore::EAttribute*";
            w.line(retType + " " + fi.getterName + "() const { return " + fi.memberName + "; }");
        }
        // S8: EEnum / EDataType 访问器
        for (auto* e : enums) {
            w.line("emf::ecore::EEnum* get" + capitalizeFirst(e->getName()) +
                   "() const { return " + e->getName() + "_enum_; }");
        }
        for (auto* d : datatypes) {
            w.line("emf::ecore::EDataType* get" + capitalizeFirst(d->getName()) +
                   "() const { return " + d->getName() + "_dt_; }");
        }
    }
    w.line();
    w.line("// 内部：EClass / EFeature / EEnum / EDataType 静态字段");
    {
        IndentScope s(w);
        for (auto* c : classes) {
            w.line("static emf::ecore::EClass* " + c->getName() + "_class_;");
        }
        for (auto& fi : features) {
            std::string type = fi.isReference ? "emf::ecore::EReference*" : "emf::ecore::EAttribute*";
            w.line("static " + type + " " + fi.memberName + ";");
        }
        for (auto* e : enums) {
            w.line("static emf::ecore::EEnum* " + e->getName() + "_enum_;");
        }
        for (auto* d : datatypes) {
            w.line("static emf::ecore::EDataType* " + d->getName() + "_dt_;");
        }
    }
    w.line("};");
    w.line();
    w.line("}  // namespace " + ns);
    return w.str();
}

std::string PackageEmitter::emitSource() const {
    IndentedWriter w;
    auto ns = nsPath();
    auto classes = collectClasses(package_);
    auto features = collectFeatures(package_);
    auto enums = collectEEnums(package_);
    auto datatypes = collectEDataTypes(package_);
    auto deps = collectDependencyPackages(package_, baseNamespace_);
    std::string factoryClass = capitalizeFirst(package_->getName()) + "Factory";

    w.line("// <auto-generated/>");
    w.line("#include \"" + className() + ".h\"");
    w.line("#include \"" + factoryClass + ".h\"");
    w.line("#include \"emf/ecore/EcorePackage.h\"");
    w.line("#include \"emf/ecore/EcoreImpls.h\"");
    w.line("#include \"emf/common/EPackageRegistry.h\"");
    // S2: 跨包依赖 include
    for (auto* dp : deps) {
        w.line("#include \"" + targetIncludePath(dp) + "\"");
    }
    // 直接子包 include（根包级联初始化子包需要子包的完整类型）
    for (auto* sub : package_->getESubpackages()) {
        if (!sub) continue;
        w.line("#include \"" + targetIncludePath(sub) + "\"");
    }
    w.line();
    w.line("namespace " + ns + " {");
    w.line();
    // 静态成员定义
    w.line("const std::string " + className() + "::eNAME = \"" + package_->getName() + "\";");
    w.line("const std::string " + className() + "::eNS_URI = \"" + package_->getNsURI() + "\";");
    w.line("const std::string " + className() + "::eNS_PREFIX = \"" + package_->getNsPrefix() + "\";");
    w.line();
    for (auto* c : classes) {
        w.line("emf::ecore::EClass* " + className() + "::" + c->getName() + "_class_ = nullptr;");
    }
    for (auto& fi : features) {
        std::string type = fi.isReference ? "emf::ecore::EReference*" : "emf::ecore::EAttribute*";
        w.line(type + " " + className() + "::" + fi.memberName + " = nullptr;");
    }
    for (auto* e : enums) {
        w.line("emf::ecore::EEnum* " + className() + "::" + e->getName() + "_enum_ = nullptr;");
    }
    for (auto* d : datatypes) {
        w.line("emf::ecore::EDataType* " + className() + "::" + d->getName() + "_dt_ = nullptr;");
    }
    w.line();
    w.line(className() + "* " + className() + "::eINSTANCE = nullptr;");
    w.line("bool " + className() + "::initializing_ = false;");
    w.line();
    // 构造函数：设置元信息
    w.line(className() + "::" + className() + "() {");
    {
        IndentScope s(w);
        w.line("setName(eNAME);");
        w.line("setNsURI(eNS_URI);");
        w.line("setNsPrefix(eNS_PREFIX);");
    }
    w.line("}");
    w.line();
    w.line(className() + "* " + className() + "::instance() { initialize(); return eINSTANCE; }");
    w.line();
    // initialize()
    w.line("void " + className() + "::initialize() {");
    {
        IndentScope s(w);
        w.line("if (eINSTANCE) return;");
        w.line("if (initializing_) return;  // 防止循环依赖导致的无限递归");
        w.line("initializing_ = true;");
        w.line();
        // 0. 创建用户自定义 EEnum / EDataType（S8）
        if (!enums.empty() || !datatypes.empty()) {
            for (auto* e : enums) {
                std::string m = e->getName() + "_enum_";
                w.line(m + " = emf::ecore::EcoreFactory::instance().createEEnum();");
                w.line(m + "->setName(\"" + escapeStr(e->getName()) + "\");");
                for (auto* lit : e->getELiterals()) {
                    if (!lit) continue;
                    w.line("{");
                    {
                        IndentScope s2(w);
                        w.line("auto* __lit = emf::ecore::EcoreFactory::instance().createEEnumLiteral();");
                        w.line("__lit->setName(\"" + escapeStr(lit->getName()) + "\");");
                        w.line("__lit->setValue(" + std::to_string(lit->getValue()) + ");");
                        w.line("__lit->setLiteral(\"" + escapeStr(lit->getLiteral()) + "\");");
                        // addELiteral 已在 EEnum 接口上提供（默认空实现，EEnumImpl override）
                        w.line(m + "->addELiteral(__lit);");
                    }
                    w.line("}");
                }
            }
            for (auto* d : datatypes) {
                std::string m = d->getName() + "_dt_";
                w.line(m + " = emf::ecore::EcoreFactory::instance().createEDataType();");
                w.line(m + "->setName(\"" + escapeStr(d->getName()) + "\");");
                w.line(m + "->setInstanceClassName(\"" + escapeStr(d->getInstanceClassName()) + "\");");
            }
            w.line();
        }
        // 1. 创建 EClass（先创建空 EClass，确保跨包引用非空）
        for (auto* c : classes) {
            std::string m = c->getName() + "_class_";
            w.line(m + " = emf::ecore::EcoreFactory::instance().createEClass();");
            w.line(m + "->setName(\"" + escapeStr(c->getName()) + "\");");
            if (c->isAbstract()) {
                w.line(m + "->setAbstract(true);");
            }
            // M4: EClass 注解
            emitAnnotations(w, c, m);
            w.line();
        }
        // 1.5 提前注册 Package（打破循环依赖）
        w.line("eINSTANCE = new " + className() + "();");
        {
            // addEClassifier 列表：EClass + EEnum + EDataType
            std::string list;
            for (auto* c : classes) {
                if (!list.empty()) list += ", ";
                list += "static_cast<emf::ecore::EClassifier*>(" + c->getName() + "_class_)";
            }
            for (auto* e : enums) {
                if (!list.empty()) list += ", ";
                list += "static_cast<emf::ecore::EClassifier*>(" + e->getName() + "_enum_)";
            }
            for (auto* d : datatypes) {
                if (!list.empty()) list += ", ";
                list += "static_cast<emf::ecore::EClassifier*>(" + d->getName() + "_dt_)";
            }
            if (!list.empty()) {
                w.line("for (auto* c : { " + list + " }) {");
                {
                    IndentScope s2(w);
                    w.line("eINSTANCE->addEClassifier(c);");
                }
                w.line("}");
            }
        }
        w.line("emf::common::EPackageRegistry::instance().put(eNS_URI, eINSTANCE);");
        w.line();
        // 2. 初始化依赖的子包（确保跨包 EClass/EDataType 引用非空）
        if (!deps.empty()) {
            for (auto* dp : deps) {
                w.line(targetNsPath(dp, baseNamespace_) + "::" + targetPkgClassName(dp) +
                       "::initialize();");
            }
            w.line();
        }
        // 2.5 初始化自身的直接子包（根包级联初始化所有子包）
        // 对齐 Java: EPackageImpl.eInitialize() 会递归初始化子包
        // 根包通常没有自己的 EClass，所有 EClass 在子包中，必须级联初始化才能加载完整元模型
        auto& subs = package_->getESubpackages();
        if (!subs.empty()) {
            for (auto* sub : subs) {
                if (!sub) continue;
                std::string subNs = targetNsPath(sub, baseNamespace_);
                std::string subCls = targetPkgClassName(sub);
                w.line(subNs + "::" + subCls + "::initialize();");
                // 把子包加入根包的 subpackages_ 列表,供 Loader 遍历查找 EClass
                w.line("eINSTANCE->addESubpackage(" + subNs + "::" + subCls + "::eINSTANCE);");
                // 设置子包的 superPackage 指针（对齐 Java EPackage.eSetSuperPackage）
                w.line(subNs + "::" + subCls + "::eINSTANCE->setESuperPackage(eINSTANCE);");
            }
            w.line();
        }
        // 3. 创建 EAttribute / EReference + 注解 + 挂到 EClass
        for (auto& fi : features) {
            std::string member = fi.memberName;
            if (fi.isAttribute) {
                auto* attr = dynamic_cast<emf::ecore::EAttribute*>(fi.sf);
                w.line(member + " = emf::ecore::EcoreFactory::instance().createEAttribute();");
                w.line(member + "->setName(\"" + escapeStr(fi.sf->getName()) + "\");");
                w.line(member + "->setLowerBound(" + std::to_string(fi.sf->getLowerBound()) + ");");
                w.line(member + "->setUpperBound(" + std::to_string(fi.sf->getUpperBound()) + ");");
                w.line(member + "->setEAttributeType(" + attributeTypeExpr(attr, package_, baseNamespace_) + ");");
                // M4: EAttribute 注解
                emitAnnotations(w, attr, member);
                w.line(fi.cls->getName() + "_class_->addEStructuralFeature(" + member + ");");
            } else if (fi.isReference) {
                auto* ref = dynamic_cast<emf::ecore::EReference*>(fi.sf);
                w.line(member + " = emf::ecore::EcoreFactory::instance().createEReference();");
                w.line(member + "->setName(\"" + escapeStr(fi.sf->getName()) + "\");");
                w.line(member + "->setLowerBound(" + std::to_string(ref->getLowerBound()) + ");");
                w.line(member + "->setUpperBound(" + std::to_string(ref->getUpperBound()) + ");");
                if (ref->isContainment()) {
                    w.line(member + "->setContainment(true);");
                }
                // S2: 跨包全限定 setEReferenceType
                w.line(member + "->setEReferenceType(" + referenceTargetExpr(ref, package_, baseNamespace_) + ");");
                // M4: EReference 注解
                emitAnnotations(w, ref, member);
                w.line(fi.cls->getName() + "_class_->addEStructuralFeature(" + member + ");");
            }
            w.line();
        }
        // 3.5. 关联 EClass 继承关系（M5: addESuperType，跨包全限定）
        bool anySuper = false;
        for (auto* c : classes) {
            for (auto* sup : c->getESuperTypes()) {
                if (!sup) continue;
                if (!anySuper) { w.line("// 关联 EClass 继承关系（Java EMF 行为）"); anySuper = true; }
                w.line(c->getName() + "_class_->addESuperType(" +
                       superTypeExpr(sup, package_, baseNamespace_) + ");");
            }
        }
        if (anySuper) w.line();
        // 4. 关联 Factory
        w.line(factoryClass + "::initialize();");
        w.line("eINSTANCE->setEFactoryInstance(" + factoryClass + "::eINSTANCE);");
    }
    w.line("}");
    w.line();
    w.line("}  // namespace " + ns);
    return w.str();
}

}  // namespace emf::ecore::codegen
