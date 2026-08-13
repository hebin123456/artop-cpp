// XMISaver.cpp —— XMI / XML 序列化实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl + XMIHelperImpl
//
// 把 XMIResource::contents 中的 EObject 树序列化成 XML 字符串。
//
// 支持两种输出形态（与 loader 对称）：
//   (1) Ecore 元模型文档：<ecore:EPackage ...>...</ecore:EPackage>
//       —— contents[0] 是 EPackage 时使用。
//   (2) XMI 实例文档：<xmi:XMI xmi:version="2.0">...</xmi:XMI>
//       —— 其它情况包裹在 <xmi:XMI> 中。
//
// 设计：递归遍历 EObject，按 EClass 反射输出 attribute / reference。
// 不依赖外部 XML 库；输出经 escapeXmlAttr/escapeXmlText 转义。
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/xmi/XMIHelper.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"
#include "emf/ecore/util/BasicExtendedMetaData.h"

#include <sstream>
#include <string>
#include <vector>
#include <cstdio>
#include <cstdlib>

namespace emf::xmi {

namespace {

// 判断 EObject 是否为 EPackage（ecore 元模型根）
bool isEPackageObject(emf::common::EObject* obj) {
    return dynamic_cast<emf::ecore::EPackage*>(obj) != nullptr;
}

// 把 EAttribute 的值转成字符串（对齐 Java EFactory.convertToString）
std::string attrValueToString(emf::ecore::EAttribute* attr, const std::any& value,
                              emf::ecore::EPackage* pkg) {
    if (!value.has_value()) return "";
    // 优先用 factory.convertToString
    auto* dt = attr->getEAttributeType();
    if (dt && pkg) {
        auto* factory = pkg->getEFactoryInstance();
        if (factory) {
            try { return factory->convertToString(dt, value); }
            catch (...) {}
        }
    }
    // fallback：常见类型直接 any_cast
    if (value.type() == typeid(std::string)) {
        return std::any_cast<std::string>(value);
    }
    if (value.type() == typeid(int)) {
        return std::to_string(std::any_cast<int>(value));
    }
    if (value.type() == typeid(int32_t)) {
        return std::to_string(std::any_cast<int32_t>(value));
    }
    if (value.type() == typeid(int64_t)) {
        return std::to_string(std::any_cast<int64_t>(value));
    }
    if (value.type() == typeid(double)) {
        // 对齐 Java Double.toString（主路径走 DataTypeUtil，此处为 fallback）
        return emf::ecore::formatJavaDouble(std::any_cast<double>(value));
    }
    if (value.type() == typeid(float)) {
        return emf::ecore::formatJavaFloat(std::any_cast<float>(value));
    }
    if (value.type() == typeid(bool)) {
        return std::any_cast<bool>(value) ? "true" : "false";
    }
    if (value.type() == typeid(const char*)) {
        return std::any_cast<const char*>(value);
    }
    return "";
}

// 构造 EClassifier 的 href（对齐 Java XMLHelperImpl.getHREF + saveEObjectSingle）
// 判断依据：eType 指向的 classifier 的 eResource 是否等于当前正在保存的 resource
//   同 resource（如 Ecore.ecore 内的 EBoolean 引用同文件的 EBoolean）：#//ClassName（短形式）
//   不同 resource（如 Ecore.ecore 引用 EcorePackage 内建类型 EString）：prefix:Type nsURI#//Name（长形式）
// 此自由函数版本用于 EcoreSaver 外部（如 InstanceSaver），用 nsURI 粗略判断
std::string buildEClassifierHref(emf::ecore::EClassifier* cls) {
    if (!cls) return "";
    auto* ownerPkg = cls->getEPackage();
    std::string nsURI = ownerPkg ? ownerPkg->getNsURI() : "";
    // 无法访问当前 resource，用 nsURI 粗略判断：nsURI==EcoreNsURI 且 classifier 不属于当前加载的包时用长形式
    // 简化：跨包（nsURI 是 EcoreNsURI）用长形式，同包用短形式
    if (nsURI == kEcoreNsURI && !ownerPkg->getEClassifiers().empty()) {
        // 仍需判断是否在当前 resource —— 此函数无 resource 上下文，保守用长形式
        std::string kind;
        if (dynamic_cast<emf::ecore::EClass*>(cls))           kind = "ecore:EClass";
        else if (dynamic_cast<emf::ecore::EEnum*>(cls))       kind = "ecore:EEnum";
        else if (dynamic_cast<emf::ecore::EDataType*>(cls))   kind = "ecore:EDataType";
        return kind + " " + kEcoreNsURI + "#//" + cls->getName();
    }
    return "#//" + cls->getName();
}

// 构造 ETypeParameter 的 href（对齐 Java EGenericTypeImpl.eTypeParameter 反射序列化）
//   类级：#//ClassName/TypeName
//   操作级：#//ClassName/OperationName/TypeName
std::string buildETypeParameterHref(emf::ecore::ETypeParameter* tp) {
    if (!tp) return "";
    auto* tpImpl = dynamic_cast<emf::ecore::ETypeParameterImpl*>(tp);
    if (!tpImpl) return "";
    auto* container = tpImpl->getEContainer();
    if (!container) return "";
    if (auto* clsr = dynamic_cast<emf::ecore::EClassifier*>(container)) {
        return "#//" + clsr->getName() + "/" + tp->getName();
    }
    if (auto* op = dynamic_cast<emf::ecore::EOperation*>(container)) {
        auto* ecls = op->getEContainingClass();
        if (ecls) {
            return "#//" + ecls->getName() + "/" + op->getName() + "/" + tp->getName();
        }
    }
    return "";
}

// ===== Ecore 元模型序列化（<ecore:EPackage> 风格） =====
struct EcoreSaver {
    std::ostream& os;
    const XMIOptions& opts;
    const XMIResource& res;
    std::string indentStr;
    std::string encoding_;  // 跟随输入文件的 encoding（对齐 Java）
    int mapLimit_;  // 非 ASCII 转义阈值（对齐 Java mappableLimit）
    // 行宽换行状态（对齐 Java XMLString.currentLineWidth / lineWidth）
    // currentLineWidth_ 累加所有写入字符串的长度；addLine 重置为 0
    // 属性写入前检查 currentLineWidth_ > lineWidth_，若是则换行 + 属性缩进
    int currentLineWidth_ = 0;
    int currentDepth_ = 0;
    int lineWidth_ = 80;

    EcoreSaver(std::ostream& o, const XMIOptions& op, const XMIResource& r)
        : os(o), opts(op), res(r),
          indentStr(op.indent.empty() ? "  " : op.indent),
          encoding_(!op.encoding.empty() ? op.encoding : (r.getEncoding().empty() ? std::string("UTF-8") : r.getEncoding())),
          mapLimit_(mappableLimitForEncoding(encoding_)),
          lineWidth_(op.lineWidth) {}

    // 构造 EClassifier href（带 resource 上下文，对齐 Java getHREF + saveEObjectSingle）
    // 判断依据：cls 是否属于当前正在保存的 resource（通过 EPackage 包含关系判断）
    //   同 resource（如 Ecore.ecore 内的 EBoolean 引用同文件的 EBoolean）：#//ClassName（短形式）
    //   不同 resource（如 Ecore.ecore 引用 EcorePackage 内建类型 EString）：prefix:Type nsURI#//Name（长形式）
    std::string buildEClassifierHref(emf::ecore::EClassifier* cls) {
        if (!cls) return "";
        // 判断 cls 是否在当前正在保存的 resource 内
        // 方法：沿 cls 的 EPackage 链向上，若根 EPackage 是 res.contents 中的某个 EPackage 则同 resource
        emf::ecore::EPackage* pkg = cls->getEPackage();
        bool sameResource = false;
        if (pkg) {
            // 沿 EPackage 的 eContainer 链向上找根 EPackage
            emf::ecore::EPackage* rootPkg = pkg;
            while (rootPkg) {
                emf::common::EObject* cont = rootPkg->eContainer();
                emf::ecore::EPackage* parentPkg = dynamic_cast<emf::ecore::EPackage*>(cont);
                if (parentPkg) {
                    rootPkg = parentPkg;
                } else {
                    break;
                }
            }
            // 检查 rootPkg 是否在 res.contents 中
            auto& contents = const_cast<XMIResource&>(res).getContents();
            for (auto* rootObj : contents) {
                if (rootObj == rootPkg) { sameResource = true; break; }
            }
        }
        if (sameResource) {
            return "#//" + cls->getName();
        }
        // 跨 resource：长形式 prefix:Type nsURI#//Name
        std::string nsURI = pkg ? pkg->getNsURI() : "";
        std::string kind;
        if (dynamic_cast<emf::ecore::EClass*>(cls))           kind = "ecore:EClass";
        else if (dynamic_cast<emf::ecore::EEnum*>(cls))       kind = "ecore:EEnum";
        else if (dynamic_cast<emf::ecore::EDataType*>(cls))   kind = "ecore:EDataType";
        return kind + " " + (nsURI.empty() ? kEcoreNsURI : nsURI) + "#//" + cls->getName();
    }

    // 带 owner 上下文的 EClassifier href 构造（对齐 Java deresolve 输出）。
    // 优先使用 loader 保留的原始跨文档 href（文件路径形式），否则回退到默认逻辑。
    // owner 是持有 eType/eClassifier 引用的 EObject（ETypedElement / EGenericType）。
    std::string buildEClassifierHrefOwner(emf::common::EObject* owner, emf::ecore::EClassifier* cls) {
        if (owner) {
            auto& hrefs = res.crossDocHrefs();
            auto it = hrefs.find(owner);
            if (it != hrefs.end()) return it->second;
        }
        return buildEClassifierHref(cls);
    }

    // 属性换行缩进 = (depth + 2) * 2 空格
    // 对齐 Java XMLString.getAttributeIndent: indents[depth+1] = (depth+1)*2
    // Java depth after startElement = C++ depth + 1，所以 (C++ depth + 1 + 1) * 2
    std::string getAttributeIndent(int depth) const {
        return std::string((depth + 2) * 2, ' ');
    }

    // 写元素开标签（indent + <tagName），初始化 currentLineWidth_
    // 对齐 Java XMLString.startElement: add(getElementIndent()) + add("<") + add(name)
    void beginElement(int depth, const std::string& tagName) {
        currentDepth_ = depth;
        std::string ind(depth * 2, ' ');
        os << ind << "<" << tagName;
        // 对齐 Java: add(indent) + add("<") + add(tagName) → currentLineWidth 累加
        currentLineWidth_ = (int)ind.size() + 1 + (int)tagName.size();
    }

    // 带行宽检查的属性写入（对齐 Java XMLString.addAttribute）
    // 检查 currentLineWidth_ > lineWidth_，若是则换行 + 属性缩进（重置 currentLineWidth_）
    void addAttribute(const std::string& name, const std::string& value) {
        if (value.empty()) return;
        std::string escaped = escapeXmlAttr(value, mapLimit_);
        if (lineWidth_ > 0 && currentLineWidth_ > lineWidth_) {
            // 换行 + 属性缩进
            std::string attrInd = getAttributeIndent(currentDepth_);
            os << "\n" << attrInd;
            currentLineWidth_ = (int)attrInd.size();
        } else {
            os << " ";
            currentLineWidth_ += 1;
        }
        os << name << "=\"" << escaped << "\"";
        currentLineWidth_ += (int)name.size() + 2 + (int)escaped.size() + 1;
    }

    // 带行宽检查的属性写入（允许空值，对齐 Java 对"显式设置为空字符串"属性的处理）
    // 用于 EAnnotation details 的 value 和 EStructuralFeature 的 defaultValueLiteral：
    // 这些属性可能显式设为空字符串（非 null），Java EMF 会输出 attr=""。
    // addAttribute 会跳过空值（用于未设置属性），此方法保留空值。
    void addAttributeAllowEmpty(const std::string& name, const std::string& value) {
        std::string escaped = escapeXmlAttr(value, mapLimit_);
        if (lineWidth_ > 0 && currentLineWidth_ > lineWidth_) {
            std::string attrInd = getAttributeIndent(currentDepth_);
            os << "\n" << attrInd;
            currentLineWidth_ = (int)attrInd.size();
        } else {
            os << " ";
            currentLineWidth_ += 1;
        }
        os << name << "=\"" << escaped << "\"";
        currentLineWidth_ += (int)name.size() + 2 + (int)escaped.size() + 1;
    }

    // 带行宽检查的命名空间属性写入（对齐 Java XMLString.addAttributeNS）
    void addAttributeNS(const std::string& prefix, const std::string& localName, const std::string& value) {
        if (value.empty()) return;
        std::string escaped = escapeXmlAttr(value, mapLimit_);
        if (lineWidth_ > 0 && currentLineWidth_ > lineWidth_) {
            std::string attrInd = getAttributeIndent(currentDepth_);
            os << "\n" << attrInd;
            currentLineWidth_ = (int)attrInd.size();
        } else {
            os << " ";
            currentLineWidth_ += 1;
        }
        os << prefix << ":" << localName << "=\"" << escaped << "\"";
        currentLineWidth_ += (int)prefix.size() + 1 + (int)localName.size() + 2 + (int)escaped.size() + 1;
    }

    // 不带行宽检查的原始属性写入（用于根元素的常规属性，在 ns 声明之后追加）
    // 对齐 Java mark/resetToMark 机制：常规属性的换行判断在 saveFeatures 时已完成
    void addRawAttribute(const std::string& name, const std::string& value) {
        if (value.empty()) return;
        std::string escaped = escapeXmlAttr(value, mapLimit_);
        os << " " << name << "=\"" << escaped << "\"";
    }

    void endEmptyElement() {
        os << "/>\n";
    }

    void endElement(int depth, const std::string& tagName) {
        std::string ind(depth * 2, ' ');
        os << ind << "</" << tagName << ">\n";
    }

    void saveEPackage(emf::ecore::EPackage* pkg) {
        // XML 声明（encoding 跟随输入文件，对齐 Java XMLResourceImpl）
        if (opts.xmlDeclaration) {
            os << "<?xml version=\"1.0\" encoding=\"" << encoding_ << "\"?>\n";
        }
        writeEPackage(pkg, 0);
    }

    // 根 EPackage：对齐 Java writeTopObject + traverse 的 mark/resetToMark 机制
    // Java 流程：
    //   1. startElement(name) → currentLineWidth = indent + 1 + name.len
    //   2. mark() → 记录 currentLineWidth 到 markedLineWidth
    //   3. saveFeatures(常规属性) → 常规属性带行宽检查写入，currentLineWidth 累加
    //   4. resetToMark(mark) → currentLineWidth = markedLineWidth（恢复到 startElement 后）
    //      ++depth
    //   5. addNamespaceDeclarations(ns 声明) → ns 声明带行宽检查写入，currentLineWidth 累加
    //   6. 常规属性的 segment 直接追加到输出（其换行判断已在 step 3 完成）
    //
    // 关键：常规属性的换行判断在 step 3（基于 startElement 后的 currentLineWidth），
    //       ns 声明的换行判断在 step 5（也基于 startElement 后的 currentLineWidth，因 resetToMark）。
    //       两者独立计算换行，但共享 startElement 后的起点。
    //       常规属性 segment 在 ns 声明之后追加，但其内容（含换行）已在 step 3 确定。
    void writeEPackage(emf::ecore::EPackage* pkg, int depth) {
        // Step 1: startElement
        beginElement(depth, "ecore:EPackage");
        int savedLineWidth = currentLineWidth_;  // mark 点

        // Step 2: saveFeatures — 计算常规属性字符串（带行宽检查）
        // 对齐 Java saveFeatures 遍历 EPackage 的 EAllStructuralFeatures
        // 注意：EcoreSaver 含引用成员，不能用拷贝构造 + rdbuf 重定向（会破坏原始 os）
        // 改为构造全新的 EcoreSaver 写入临时 buffer，手动复制行宽状态
        std::string regularAttrs;
        {
            std::ostringstream attrBuf;
            EcoreSaver tmp(attrBuf, opts, res);
            tmp.currentLineWidth_ = currentLineWidth_;
            tmp.currentDepth_ = currentDepth_;
            tmp.lineWidth_ = lineWidth_;
            tmp.addAttribute("name", pkg->getName());
            tmp.addAttribute("nsURI", pkg->getNsURI());
            tmp.addAttribute("nsPrefix", pkg->getNsPrefix());
            regularAttrs = attrBuf.str();
        }

        // Step 3: resetToMark — 恢复 currentLineWidth 到 startElement 后
        currentLineWidth_ = savedLineWidth;

        // Step 4: addNamespaceDeclarations（带行宽检查）
        // 顺序对齐 XMISaveImpl.addNamespaceDeclarations: xmi:version, xmlns:xmi, xmlns:xsi, xmlns:ecore
        addAttribute("xmi:version", opts.xmiVersion);
        addAttributeNS("xmlns", "xmi", kXmiNsURI);
        addAttributeNS("xmlns", "xsi", kXsiNsURI);
        addAttributeNS("xmlns", "ecore", kEcoreNsURI);

        // Step 5: 追加常规属性（不带行宽检查，对齐 Java segment 拼接）
        // 常规属性的换行判断已在 step 2 完成（基于 startElement 后的 currentLineWidth）
        os << regularAttrs;

        auto& annotations = pkg->getEAnnotations();
        auto& classifiers = pkg->getEClassifiers();
        auto& subpkgs = pkg->getESubPackages();
        if (annotations.empty() && classifiers.empty() && subpkgs.empty()) {
            endEmptyElement();
            return;
        }
        os << ">\n";
        // eAnnotations 在前（对齐 Java EModelElement.eAnnotations featureID=0）
        for (auto* ann : annotations) {
            if (ann) writeEAnnotation(ann, depth + 1);
        }
        for (auto* c : classifiers) {
            writeClassifier(c, depth + 1);
        }
        for (auto* sp : subpkgs) {
            if (sp) writeEPackage(sp, depth + 1);
        }
        // 未知 XML 内容原样回写（对齐 Java AnyType round-trip）
        // buildEPackage 在 recordUnknownFeature_=true 时记录的未知子元素片段
        for (const auto& [owner, fragment] : res.unknownContents()) {
            if (owner == pkg && !fragment.empty()) {
                os << fragment;
            }
        }
        endElement(depth, "ecore:EPackage");
    }

    // ===== EAnnotation 序列化（对齐 Java EAnnotationImpl：source 属性 + details 子元素）=====
    // 输出：<eAnnotations source="..."><details key="..." value="..."/></eAnnotations>
    void writeEAnnotation(emf::ecore::EAnnotation* ann, int depth) {
        if (!ann) return;
        beginElement(depth, "eAnnotations");
        // source 属性（EAnnotation featureID=1）
        addAttribute("source", ann->getSource());
        // details 子元素（EAnnotation featureID=2，EMap<String,String>，元素名 "details"）
        const auto& details = ann->getDetails();
        if (details.empty()) {
            endEmptyElement();
            return;
        }
        os << ">\n";
        for (const auto& kv : details) {
            beginElement(depth + 1, "details");
            addAttribute("key", kv.first);
            // value 可能显式设为空字符串（非 null），Java EMF 输出 value=""，需保留空值
            addAttributeAllowEmpty("value", kv.second);
            endEmptyElement();
        }
        endElement(depth, "eAnnotations");
    }

    void writeClassifier(emf::ecore::EClassifier* c, int depth) {
        if (!c) return;
        // 判断 xsi:type
        std::string xsiType;
        if (dynamic_cast<emf::ecore::EClass*>(c))      xsiType = "ecore:EClass";
        else if (dynamic_cast<emf::ecore::EEnum*>(c))  xsiType = "ecore:EEnum";
        else if (dynamic_cast<emf::ecore::EDataType*>(c)) xsiType = "ecore:EDataType";
        else return;

        beginElement(depth, "eClassifiers");
        // xsi:type 属性（对齐 Java XMLSaveImpl.saveTypeAttribute，总是输出）
        addAttribute("xsi:type", xsiType);
        // xmi:id（对齐 Java XMLHelper：classifier 若有 id 则输出）
        {
            std::string id = res.getID(c);
            if (!id.empty()) addAttribute("xmi:id", id);
        }
        addAttribute("name", c->getName());

        // eAnnotations（featureID=0，是第一个子元素，对齐 Java EModelElement.eAnnotations）
        auto& annotations = c->getEAnnotations();

        if (auto* cls = dynamic_cast<emf::ecore::EClass*>(c)) {
            if (cls->isAbstract()) addAttribute("abstract", "true");
            if (cls->isInterface()) addAttribute("interface", "true");
            // instanceClassName
            const auto& icn = cls->getInstanceClassName();
            if (!icn.empty()) addAttribute("instanceClassName", icn);
            // eSuperTypes 属性：仅当 !isSetEGenericSuperTypes() 时写（对齐 Java isSet 互斥）
            if (!cls->isSetEGenericSuperTypes()) {
                auto& supers = cls->getESuperTypes();
                if (!supers.empty()) {
                    std::string val;
                    for (size_t i = 0; i < supers.size(); ++i) {
                        if (i) val += " ";
                        // 优先用 loader 保留的跨文档 href（对齐 Java deresolve 文件路径形式，
                        // 如 "base.ecore#//Library"）；否则按 sameResource 判断：
                        //   同 resource → "#//Name"；跨 resource → nsURI 长形式
                        auto it = res.crossDocHrefs().find(supers[i]);
                        if (it != res.crossDocHrefs().end()) {
                            val += it->second;
                        } else {
                            val += buildEClassifierHref(supers[i]);
                        }
                    }
                    addAttribute("eSuperTypes", val);
                }
            }
            // 子元素顺序（对齐 Java EcorePackageImpl feature 声明）：
            //   eAnnotations(0) → eTypeParameters(7) → eOperations(11) → eStructuralFeatures(21) → eGenericSuperTypes(22)
            auto& typeParams = cls->getETypeParameters();
            auto& operations = cls->getEOperations();
            auto& features = cls->getEStructuralFeatures();
            auto& genericSupers = cls->getEGenericSuperTypes();
            bool hasGenericSupers = cls->isSetEGenericSuperTypes();
            if (annotations.empty() && typeParams.empty() && operations.empty() && features.empty() && !hasGenericSupers) {
                endEmptyElement();
                return;
            }
            os << ">\n";
            for (auto* ann : annotations) {
                if (ann) writeEAnnotation(ann, depth + 1);
            }
            for (auto* tp : typeParams) {
                if (tp) writeETypeParameter(tp, depth + 1);
            }
            for (auto* op : operations) {
                if (op) writeEOperation(op, depth + 1);
            }
            for (auto* sf : features) {
                writeStructuralFeature(sf, depth + 1);
            }
            if (hasGenericSupers) {
                for (auto* gt : genericSupers) {
                    if (gt) writeEGenericType(gt, "eGenericSuperTypes", depth + 1);
                }
            }
            endElement(depth, "eClassifiers");
        } else if (auto* en = dynamic_cast<emf::ecore::EEnum*>(c)) {
            const auto& icn = en->getInstanceClassName();
            if (!icn.empty()) addAttribute("instanceClassName", icn);
            auto& lits = en->getELiterals();
            if (annotations.empty() && lits.empty()) {
                endEmptyElement();
                return;
            }
            os << ">\n";
            for (auto* ann : annotations) {
                if (ann) writeEAnnotation(ann, depth + 1);
            }
            for (size_t li = 0; li < lits.size(); ++li) {
                auto* lit = lits[li];
                if (!lit) continue;
                beginElement(depth + 1, "eLiterals");
                addAttribute("name", lit->getName());
                // value 默认等于声明顺序索引（对齐 Java EEnumLiteral.value 自动递增）
                // 仅在 value != 索引位置时输出（对齐 Java EEnumLiteralSerializer）
                if (lit->getValue() != static_cast<int>(li)) {
                    addAttribute("value", std::to_string(lit->getValue()));
                }
                if (!lit->getLiteral().empty()) addAttribute("literal", lit->getLiteral());
                // eAnnotations 子元素（对齐 Java EModelElement.eAnnotations，featureID=0）
                auto& litAnnotations = lit->getEAnnotations();
                if (litAnnotations.empty()) {
                    endEmptyElement();
                } else {
                    os << ">\n";
                    for (auto* ann : litAnnotations) {
                        if (ann) writeEAnnotation(ann, depth + 2);
                    }
                    endElement(depth + 1, "eLiterals");
                }
            }
            endElement(depth, "eClassifiers");
        } else if (auto* dt = dynamic_cast<emf::ecore::EDataType*>(c)) {
            const auto& icn = dt->getInstanceClassName();
            if (!icn.empty()) addAttribute("instanceClassName", icn);
            // serializable 属性（对齐 Java EDataType.serializable，默认 true，false 时输出）
            if (!dt->isSerializable()) addAttribute("serializable", "false");
            // eTypeParameters 子元素（对齐 Java EClassifier.eTypeParameters containment）
            auto& typeParams = dt->getETypeParameters();
            if (annotations.empty() && typeParams.empty()) {
                endEmptyElement();
                return;
            }
            os << ">\n";
            for (auto* ann : annotations) {
                if (ann) writeEAnnotation(ann, depth + 1);
            }
            for (auto* tp : typeParams) {
                if (tp) writeETypeParameter(tp, depth + 1);
            }
            endElement(depth, "eClassifiers");
        }
    }

    // ===== EGenericType 递归序列化（对齐 Java EGenericTypeImpl 反射序列化）=====
    void writeEGenericType(emf::ecore::EGenericType* gt, const std::string& elementName, int depth) {
        if (!gt) return;
        beginElement(depth, elementName);
        // eClassifier 属性（与 eTypeParameter 互斥）
        if (auto* clsr = gt->getEClassifier()) {
            addAttribute("eClassifier", buildEClassifierHrefOwner(gt, clsr));
        }
        // eTypeParameter 属性（与 eClassifier 互斥）
        if (auto* tp = gt->getETypeParameter()) {
            addAttribute("eTypeParameter", buildETypeParameterHref(tp));
        }
        // 子元素：eUpperBound → eTypeArguments(multiple) → eLowerBound
        auto* ub = gt->getEUpperBound();
        auto& typeArgs = gt->getETypeArguments();
        auto* lb = gt->getELowerBound();
        if (!ub && typeArgs.empty() && !lb) {
            endEmptyElement();
            return;
        }
        os << ">\n";
        if (ub) writeEGenericType(ub, "eUpperBound", depth + 1);
        for (auto* arg : typeArgs) {
            if (arg) writeEGenericType(arg, "eTypeArguments", depth + 1);
        }
        if (lb) writeEGenericType(lb, "eLowerBound", depth + 1);
        endElement(depth, elementName);
    }

    // ===== ETypeParameter 序列化（对齐 Java ETypeParameterImpl）=====
    void writeETypeParameter(emf::ecore::ETypeParameter* tp, int depth) {
        if (!tp) return;
        beginElement(depth, "eTypeParameters");
        addAttribute("name", tp->getName());
        // xmi:id
        {
            std::string id = res.getID(tp);
            if (!id.empty()) addAttribute("xmi:id", id);
        }
        auto& bounds = tp->getEBounds();
        if (bounds.empty()) {
            endEmptyElement();
            return;
        }
        os << ">\n";
        for (auto* bound : bounds) {
            if (bound) writeEGenericType(bound, "eBounds", depth + 1);
        }
        endElement(depth, "eTypeParameters");
    }

    // ===== EOperation 序列化（对齐 Java EOperationImpl + ETypedElement isSet 互斥）=====
    // 属性顺序对齐 Java ETypedElement feature 声明：name → lowerBound → upperBound → eType
    // 子元素顺序（对齐 Java feature 声明）：eGenericType → eTypeParameters → eParameters
    void writeEOperation(emf::ecore::EOperation* op, int depth) {
        if (!op) return;
        beginElement(depth, "eOperations");
        addAttribute("name", op->getName());
        // xmi:id
        {
            std::string id = res.getID(op);
            if (!id.empty()) addAttribute("xmi:id", id);
        }
        // bounds（在 eType 之前，对齐 Java ETypedElement feature 声明顺序）
        if (op->getLowerBound() != 0) addAttribute("lowerBound", std::to_string(op->getLowerBound()));
        if (op->getUpperBound() != 1) {
            if (op->getUpperBound() == -1) addAttribute("upperBound", "-1");
            else addAttribute("upperBound", std::to_string(op->getUpperBound()));
        }
        // eType 属性：仅当 eGenericType 未参数化时写（对齐 Java isSet 互斥）
        auto* teImpl = dynamic_cast<emf::ecore::ETypedElementImpl*>(op);
        bool isParam = teImpl && teImpl->isEGenericTypeParameterized();
        if (!isParam) {
            auto* et = op->getEType();
            if (et) addAttribute("eType", buildEClassifierHrefOwner(op, et));
        }
        // eExceptions 属性（对齐 Java EOperation.eExceptions，多值用空格分隔）
        // 在 eType 之后输出（对齐 Java EOperation featureID 顺序：eType 在前，eExceptions 在后）
        auto& excs = op->getEExceptions();
        if (!excs.empty()) {
            std::string val;
            for (size_t i = 0; i < excs.size(); ++i) {
                if (i) val += " ";
                // eExceptions 引用 EClassifier，用短形式 #//Name（同 resource 时）
                val += buildEClassifierHref(excs[i]);
            }
            addAttribute("eExceptions", val);
        }
        auto* gt = op->getEGenericType();
        auto& typeParams = op->getETypeParameters();
        auto& params = op->getEParameters();
        bool hasChildren = (isParam && gt) || !typeParams.empty() || !params.empty();
        if (!hasChildren) {
            endEmptyElement();
            return;
        }
        os << ">\n";
        if (isParam && gt) writeEGenericType(gt, "eGenericType", depth + 1);
        for (auto* tp : typeParams) {
            if (tp) writeETypeParameter(tp, depth + 1);
        }
        for (auto* p : params) {
            if (p) writeEParameter(p, depth + 1);
        }
        endElement(depth, "eOperations");
    }

    // ===== EParameter 序列化（对齐 Java EParameterImpl + ETypedElement isSet 互斥）=====
    // 属性顺序：name → lowerBound → upperBound → eType
    void writeEParameter(emf::ecore::EParameter* p, int depth) {
        if (!p) return;
        beginElement(depth, "eParameters");
        addAttribute("name", p->getName());
        // xmi:id
        {
            std::string id = res.getID(p);
            if (!id.empty()) addAttribute("xmi:id", id);
        }
        // bounds（在 eType 之前）
        if (p->getLowerBound() != 0) addAttribute("lowerBound", std::to_string(p->getLowerBound()));
        if (p->getUpperBound() != 1) {
            if (p->getUpperBound() == -1) addAttribute("upperBound", "-1");
            else addAttribute("upperBound", std::to_string(p->getUpperBound()));
        }
        // eType 属性：仅当 eGenericType 未参数化时写（对齐 Java isSet 互斥）
        auto* teImpl = dynamic_cast<emf::ecore::ETypedElementImpl*>(p);
        bool isParam = teImpl && teImpl->isEGenericTypeParameterized();
        if (!isParam) {
            auto* et = p->getEType();
            if (et) addAttribute("eType", buildEClassifierHrefOwner(p, et));
        }
        auto* gt = p->getEGenericType();
        if (!isParam || !gt) {
            endEmptyElement();
            return;
        }
        os << ">\n";
        writeEGenericType(gt, "eGenericType", depth + 1);
        endElement(depth, "eParameters");
    }

    // ===== EStructuralFeature 序列化（对齐 Java EStructuralFeatureImpl + ETypedElement isSet 互斥）=====
    // 属性顺序对齐 Java EStructuralFeature featureID 声明：
    //   name(1) → ordered(2) → unique(3) → lowerBound(4) → upperBound(5) → eType(8)
    //   → changeable(10) → volatile(11) → transient(12) → defaultValueLiteral(13)
    //   → unsettable(15) → derived(16)
    void writeStructuralFeature(emf::ecore::EStructuralFeature* sf, int depth) {
        if (!sf) return;
        std::string xsiType;
        if (dynamic_cast<emf::ecore::EAttribute*>(sf)) xsiType = "ecore:EAttribute";
        else if (dynamic_cast<emf::ecore::EReference*>(sf)) xsiType = "ecore:EReference";
        else return;

        beginElement(depth, "eStructuralFeatures");
        addAttribute("xsi:type", xsiType);
        addAttribute("name", sf->getName());
        // ordered(2)/unique(3)：默认 true，false 时输出（对齐 Java ETypedElement feature 声明）
        if (!sf->isOrdered()) addAttribute("ordered", "false");
        if (!sf->isUnique()) addAttribute("unique", "false");
        // bounds（在 eType 之前，对齐 Java ETypedElement feature 声明顺序）
        if (sf->getLowerBound() != 0) addAttribute("lowerBound", std::to_string(sf->getLowerBound()));
        if (sf->getUpperBound() != 1) {
            if (sf->getUpperBound() == -1) addAttribute("upperBound", "-1");
            else addAttribute("upperBound", std::to_string(sf->getUpperBound()));
        }
        // eType 属性：仅当 eGenericType 未参数化时写（对齐 Java isSet 互斥）
        auto* teImpl = dynamic_cast<emf::ecore::ETypedElementImpl*>(sf);
        bool isParam = teImpl && teImpl->isEGenericTypeParameterized();
        if (!isParam) {
            auto* et = sf->getEType();
            if (et) addAttribute("eType", buildEClassifierHrefOwner(sf, et));
        }
        // 对齐 Java EStructuralFeature featureID 顺序：changeable(10)→volatile(11)→transient(12)→defaultValueLiteral(13)→unsettable(15)→derived(16)
        // 非默认值时输出：changeable 默认 true（false 时输出），其余默认 false（true 时输出）
        if (!sf->isChangeable()) addAttribute("changeable", "false");
        if (sf->isVolatile()) addAttribute("volatile", "true");
        if (sf->isTransient()) addAttribute("transient", "true");
        // defaultValueLiteral(13)：可能显式设为空字符串（非 null），Java EMF 输出 defaultValueLiteral=""。
        // 用 isSet 标志区分"未设置"（不输出）与"显式设置为空"（输出 defaultValueLiteral=""）。
        auto* sfImpl = dynamic_cast<emf::ecore::EStructuralFeatureImpl*>(sf);
        if (sfImpl && sfImpl->isSetDefaultValueLiteral()) {
            addAttributeAllowEmpty("defaultValueLiteral", sf->getDefaultValueLiteral());
        }
        if (sf->isUnsettable()) addAttribute("unsettable", "true");
        if (sf->isDerived()) addAttribute("derived", "true");

        // EAttribute 特有
        if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
            if (attr->isID()) addAttribute("iD", "true");
        }
        // EReference 特有
        if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
            if (ref->isContainment()) addAttribute("containment", "true");
            if (!ref->isResolveProxies()) addAttribute("resolveProxies", "false");
            auto* opp = ref->getEOpposite();
            if (opp) {
                // eOpposite 格式：#//ClassName/featureName（对齐 Java href 短形式）
                auto* oppClass = opp->getEContainingClass();
                if (oppClass) {
                    std::string val = "#//" + oppClass->getName() + "/" + opp->getName();
                    addAttribute("eOpposite", val);
                }
            }
        }
        // eAnnotations 子元素（对齐 Java EModelElement.eAnnotations，featureID=0）
        auto& annotations = sf->getEAnnotations();
        // eGenericType 子元素（仅参数化时写，对齐 Java isSet 互斥）
        auto* gt = sf->getEGenericType();
        bool hasChildren = (isParam && gt) || !annotations.empty();
        if (!hasChildren) {
            endEmptyElement();
            return;
        }
        os << ">\n";
        // eAnnotations 在前（featureID=0）
        for (auto* ann : annotations) {
            if (ann) writeEAnnotation(ann, depth + 1);
        }
        if (isParam && gt) {
            writeEGenericType(gt, "eGenericType", depth + 1);
        }
        endElement(depth, "eStructuralFeatures");
    }
};

// ===== 实例文档序列化（<xmi:XMI> 包裹） =====
struct InstanceSaver {
    std::ostream& os;
    const XMIOptions& opts;
    const XMIResource& res;
    std::string indentStr;
    std::string encoding_;  // 跟随输入文件的 encoding（对齐 Java）
    // 根对象列表（用于 position-path 生成，对齐 Java XMLHelperImpl）
    std::vector<emf::common::EObject*> roots_;
    int mapLimit_;  // 非 ASCII 转义阈值（对齐 Java mappableLimit）
    // ExtendedMetaData 查询器（对齐 Java XMLSaveImpl.extendedMetaData）
    emf::ecore::util::BasicExtendedMetaData extMetaData_;

    InstanceSaver(std::ostream& o, const XMIOptions& op, const XMIResource& r)
        : os(o), opts(op), res(r),
          indentStr(op.indent.empty() ? "  " : op.indent),
          encoding_(!op.encoding.empty() ? op.encoding : (r.getEncoding().empty() ? std::string("UTF-8") : r.getEncoding())),
          mapLimit_(mappableLimitForEncoding(encoding_)) {}

    // 判断 feature 是否应输出为子元素（对齐 Java XMLSaveImpl 基于 ExtendedMetaData.getFeatureKind 决策）。
    // 决策顺序：
    //   1. containment EReference 始终为子元素（树结构必须）
    //   2. useEncodedAttributeStyle=true 时强制 attribute 风格（覆盖 kind=element 注解）
    //   3. ExtendedMetaData kind 注解：element→子元素, attribute→属性
    //   4. 无注解时默认：EAttribute→属性, EReference(non-containment)→属性(href)
    bool shouldSaveAsElement(emf::ecore::EStructuralFeature* sf) {
        if (!sf) return false;
        auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
        // containment 引用始终为子元素（无法把 containment 树序列化为属性）
        if (ref && ref->isContainment()) return true;
        // useEncodedAttributeStyle=true：强制 attribute 风格（对齐 Java OPTION_USE_ENCODED_ATTRIBUTE_STYLE）
        if (opts.useEncodedAttributeStyle) return false;
        // 查询 ExtendedMetaData kind 注解（仅有注解时才覆盖默认行为）
        auto* a = extMetaData_.getAnnotation(sf, false);
        if (a) {
            std::string kind = a->getDetail("kind");
            if (kind == "element") return true;
            if (kind == "attribute") return false;
        }
        // 默认：EAttribute→属性, EReference(non-containment)→属性(href)
        return false;
    }

    void saveContents(const std::vector<emf::common::EObject*>& contents) {
        roots_ = contents;
        if (opts.xmlDeclaration) {
            os << "<?xml version=\"1.0\" encoding=\"" << encoding_ << "\"?>\n";
        }
        if (contents.empty()) {
            os << "<xmi:XMI xmi:version=\"" << opts.xmiVersion
               << "\" xmlns:xmi=\"" << kXmiNsURI << "\"/>\n";
            return;
        }
        // 收集所有被跨引用但不在 containment 树中的对象（对齐 Java XMLResourceImpl
        // 的 top-level non-containment objects，它们会作为额外根元素输出）
        std::vector<emf::common::EObject*> topLevel = contents;
        std::vector<emf::common::EObject*> extra = collectExtraRoots(contents);
        for (auto* e : extra) topLevel.push_back(e);
        roots_ = topLevel;

        // 单根且非 XMI 包裹需求：直接写根元素（Java 风格 library.xmi）
        // 多根：用 <xmi:XMI> 包裹
        bool wrapInXmi = (topLevel.size() > 1);
        if (wrapInXmi) {
            os << "<xmi:XMI xmi:version=\"" << opts.xmiVersion
               << "\" xmlns:xmi=\"" << kXmiNsURI << "\">\n";
        }
        for (auto* obj : topLevel) {
            writeEObject(obj, 0);
        }
        if (wrapInXmi) {
            os << "</xmi:XMI>\n";
        }
    }

    // 收集所有被跨引用但不在 containment 树中的对象
    // （非 containment EReference 指向的目标，且目标不是根或任何根的 containment 子对象）
    std::vector<emf::common::EObject*> collectExtraRoots(const std::vector<emf::common::EObject*>& roots) {
        std::vector<emf::common::EObject*> extra;
        // 先建立 containment 树中所有对象的集合
        std::vector<emf::common::EObject*> allInContainment = roots;
        for (size_t i = 0; i < allInContainment.size(); ++i) {
            auto* obj = allInContainment[i];
            auto* cls = obj ? obj->eClass() : nullptr;
            if (!cls) continue;
            for (auto* ref : cls->getEAllContainments()) {
                if (!ref) continue;
                std::any v = obj->eGet(ref);
                std::vector<emf::common::EObject*> children = extractList(v);
                for (auto* c : children) if (c) allInContainment.push_back(c);
            }
        }
        if (std::getenv("EMF_DEBUG_XMI")) {
            std::fprintf(stderr, "[DBG-collectExtraRoots] allInContainment.size=%zu\n", allInContainment.size());
        }
        // 收集非 containment 引用的目标
        for (auto* obj : allInContainment) {
            auto* cls = obj ? obj->eClass() : nullptr;
            if (!cls) continue;
            for (auto* sf : cls->getEAllStructuralFeatures()) {
                if (!sf) continue;
                if (sf->isDerived() || sf->isTransient()) continue;
                auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
                if (!ref || ref->isContainment()) continue;
                std::any v = obj->eGet(ref);
                // 单值 reference：直接取 EObject*
                std::vector<emf::common::EObject*> targets;
                if (!ref->isMany()) {
                    if (v.type() == typeid(emf::common::EObject*)) {
                        auto* t = std::any_cast<emf::common::EObject*>(v);
                        if (t) targets.push_back(t);
                    }
                } else {
                    targets = extractList(v);
                }
                for (auto* t : targets) {
                    if (!t) continue;
                    // 检查 t 是否已在 containment 树中
                    bool inTree = false;
                    for (auto* e : allInContainment) {
                        if (e == t) { inTree = true; break; }
                    }
                    if (!inTree) {
                        // 检查是否已加入 extra
                        bool already = false;
                        for (auto* e : extra) {
                            if (e == t) { already = true; break; }
                        }
                        if (!already) extra.push_back(t);
                    }
                }
            }
        }
        if (std::getenv("EMF_DEBUG_XMI")) {
            std::fprintf(stderr, "[DBG-collectExtraRoots] extra.size=%zu\n", extra.size());
        }
        return extra;
    }

    // 输出 element 风格 feature（ExtendedMetaData kind=element 的 EAttribute / non-containment EReference）。
    // 对齐 Java XMLSaveImpl.saveElementFeature：
    //   EAttribute → <featName>value</featName>（文本内容元素）
    //   non-containment EReference → <featName href="..."/>（href 元素）
    void writeElementFeature(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf, int depth) {
        if (!obj || !sf) return;
        if (!obj->eIsSet(sf)) return;
        std::string ind(depth * 2, ' ');
        auto* pkg = obj->eClass() ? obj->eClass()->getEPackage() : nullptr;
        if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
            // EAttribute-as-element：<featName>value</featName>
            std::any v = obj->eGet(sf);
            std::string valStr = attrValueToString(attr, v, pkg);
            if (!valStr.empty()) {
                os << ind << "<" << sf->getName() << ">" << escapeXmlText(valStr, mapLimit_)
                   << "</" << sf->getName() << ">\n";
            }
        } else if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
            // non-containment EReference-as-element：<featName href="..."/>
            std::any v = obj->eGet(ref);
            if (!ref->isMany()) {
                if (v.type() == typeid(emf::common::EObject*)) {
                    auto* target = std::any_cast<emf::common::EObject*>(v);
                    if (target) {
                        std::string href = buildHref(target);
                        if (!href.empty()) {
                            os << ind << "<" << sf->getName() << " href=\""
                               << escapeXmlAttr(href, mapLimit_) << "\"/>\n";
                        }
                    }
                }
            } else {
                std::vector<emf::common::EObject*> targets = extractList(v);
                for (auto* target : targets) {
                    if (!target) continue;
                    std::string href = buildHref(target);
                    if (!href.empty()) {
                        os << ind << "<" << sf->getName() << " href=\""
                           << escapeXmlAttr(href, mapLimit_) << "\"/>\n";
                    }
                }
            }
        }
    }

    void writeEObject(emf::common::EObject* obj, int depth) {
        if (!obj) return;
        auto* cls = obj->eClass();
        if (!cls) return;
        std::string ind(depth * 2, ' ');
        // 找 EPackage nsPrefix / nsURI
        std::string prefix = "xmi";
        std::string nsURI;
        auto* pkg = cls->getEPackage();
        if (pkg) {
            if (!pkg->getNsPrefix().empty()) prefix = pkg->getNsPrefix();
            nsURI = pkg->getNsURI();
        }
        std::string tagName = prefix + ":" + cls->getName();
        os << ind << "<" << tagName;
        // 命名空间声明（仅根元素）
        if (depth == 0 && !nsURI.empty()) {
            os << " xmi:version=\"" << opts.xmiVersion << "\"";
            os << " xmlns:xmi=\"" << kXmiNsURI << "\"";
            os << " xmlns:xsi=\"" << kXsiNsURI << "\"";
            os << " xmlns:" << prefix << "=\"" << nsURI << "\"";
        }
        // xmi:id（如果有）
        std::string id = res.getID(obj);
        if (!id.empty()) {
            os << " xmi:id=\"" << escapeXmlAttr(id, mapLimit_) << "\"";
        }
        // 输出 attribute features
        std::vector<emf::ecore::EReference*> containmentRefs;
        std::vector<emf::ecore::EReference*> nonContainmentRefs;
        // ExtendedMetaData kind=element 的 feature（EAttribute 或 non-containment EReference）→ 输出为子元素
        std::vector<emf::ecore::EStructuralFeature*> elementFeatures;
        for (auto* sf : cls->getEAllStructuralFeatures()) {
            if (!sf) continue;
            // 对齐 Java XMIResourceImpl：默认不保存 derived/transient feature
            if (sf->isDerived() || sf->isTransient()) continue;
            // 查询 ExtendedMetaData 决定输出形式（对齐 Java XMLSaveImpl）
            if (shouldSaveAsElement(sf)) {
                auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
                if (ref && ref->isContainment()) {
                    containmentRefs.push_back(ref);
                } else {
                    // EAttribute-as-element 或 non-containment-ref-as-element
                    elementFeatures.push_back(sf);
                }
                continue;
            }
            if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                if (!obj->eIsSet(sf)) continue;
                std::any v = obj->eGet(sf);
                std::string valStr = attrValueToString(attr, v, pkg);
                if (!valStr.empty()) {
                    os << " " << sf->getName() << "=\"" << escapeXmlAttr(valStr, mapLimit_) << "\"";
                }
            } else if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
                nonContainmentRefs.push_back(ref);
            }
        }
        // 非包含引用作为 href 属性输出
        for (auto* ref : nonContainmentRefs) {
            if (!obj->eIsSet(ref)) continue;
            std::any v = obj->eGet(ref);
            // 单值引用：输出 href
            if (!ref->isMany()) {
                if (v.type() == typeid(emf::common::EObject*)) {
                    auto* target = std::any_cast<emf::common::EObject*>(v);
                    if (target) {
                        std::string href = buildHref(target);
                        if (!href.empty()) {
                            os << " " << ref->getName() << "=\"" << escapeXmlAttr(href, mapLimit_) << "\"";
                        }
                    }
                }
            } else {
                // 多值引用：合并到 "//@ref.0 //@ref.1"
                std::vector<emf::common::EObject*> targets = extractList(v);
                if (!targets.empty()) {
                    std::string combined;
                    for (size_t i = 0; i < targets.size(); ++i) {
                        if (i) combined += " ";
                        combined += buildHref(targets[i]);
                    }
                    os << " " << ref->getName() << "=\"" << escapeXmlAttr(combined, mapLimit_) << "\"";
                }
            }
        }
        // 包含引用 / element 风格 feature 作为子元素
        if (containmentRefs.empty() && elementFeatures.empty()) {
            os << "/>\n";
            return;
        }
        os << ">\n";
        // element 风格 feature（EAttribute-as-element, non-containment-ref-as-element）
        for (auto* sf : elementFeatures) {
            writeElementFeature(obj, sf, depth + 1);
        }
        for (auto* ref : containmentRefs) {
            if (!obj->eIsSet(ref)) continue;
            std::any v = obj->eGet(ref);
            if (!ref->isMany()) {
                if (v.type() == typeid(emf::common::EObject*)) {
                    auto* child = std::any_cast<emf::common::EObject*>(v);
                    if (child) {
                        // 子元素用 feature 名作为 tag
                        writeContainmentChild(child, ref->getName(), ref->getEReferenceType(), depth + 1);
                    }
                }
            } else {
                std::vector<emf::common::EObject*> children = extractList(v);
                for (auto* child : children) {
                    if (child) writeContainmentChild(child, ref->getName(), ref->getEReferenceType(), depth + 1);
                }
            }
        }
        // 未知 XML 内容原样回写（对齐 Java AnyType round-trip）
        writeUnknownContents(obj, depth);
        os << ind << "</" << tagName << ">\n";
    }

    // 包含子元素：tag 用 feature 名（Java 风格 <books .../>）
    // declaredType: 该 containment reference 的 EReferenceType（用于判断是否需要 xsi:type）
    void writeContainmentChild(emf::common::EObject* obj, const std::string& featName,
                                emf::ecore::EClass* declaredType, int depth) {
        if (!obj) return;
        auto* cls = obj->eClass();
        if (!cls) return;
        std::string ind(depth * 2, ' ');
        os << ind << "<" << featName;
        // xsi:type 当对象类型与声明类型不一致时输出（对齐 Java XMLSaveImpl）
        if (declaredType && cls != declaredType) {
            std::string prefix = "xmi";
            auto* pkg = cls->getEPackage();
            if (pkg && !pkg->getNsPrefix().empty()) prefix = pkg->getNsPrefix();
            os << " xsi:type=\"" << prefix << ":" << cls->getName() << "\"";
        }
        // xmi:id
        std::string id = res.getID(obj);
        if (!id.empty()) {
            os << " xmi:id=\"" << escapeXmlAttr(id, mapLimit_) << "\"";
        }
        // attributes + 非 containment 引用（对齐 writeEObject：cross-ref 作为属性输出）
        auto* pkg = cls->getEPackage();
        std::vector<emf::ecore::EReference*> containmentRefs;
        std::vector<emf::ecore::EReference*> nonContainmentRefs;
        // ExtendedMetaData kind=element 的 feature → 输出为子元素
        std::vector<emf::ecore::EStructuralFeature*> elementFeatures;
        for (auto* sf : cls->getEAllStructuralFeatures()) {
            if (!sf) continue;
            // 对齐 Java XMIResourceImpl：默认不保存 derived/transient feature
            if (sf->isDerived() || sf->isTransient()) continue;
            // 查询 ExtendedMetaData 决定输出形式（对齐 Java XMLSaveImpl）
            if (shouldSaveAsElement(sf)) {
                auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
                if (ref && ref->isContainment()) {
                    containmentRefs.push_back(ref);
                } else {
                    elementFeatures.push_back(sf);
                }
                continue;
            }
            if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                if (!obj->eIsSet(sf)) continue;
                std::any v = obj->eGet(sf);
                std::string valStr = attrValueToString(attr, v, pkg);
                if (!valStr.empty()) {
                    os << " " << sf->getName() << "=\"" << escapeXmlAttr(valStr, mapLimit_) << "\"";
                }
            } else if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
                nonContainmentRefs.push_back(ref);
            }
        }
        // 非 containment 引用作为 href 属性输出（对齐 Java XMLSaveImpl）
        for (auto* ref : nonContainmentRefs) {
            if (!obj->eIsSet(ref)) continue;
            std::any v = obj->eGet(ref);
            if (!ref->isMany()) {
                if (v.type() == typeid(emf::common::EObject*)) {
                    auto* target = std::any_cast<emf::common::EObject*>(v);
                    if (target) {
                        std::string href = buildHref(target);
                        if (!href.empty()) {
                            os << " " << ref->getName() << "=\"" << escapeXmlAttr(href, mapLimit_) << "\"";
                        }
                    }
                }
            } else {
                std::vector<emf::common::EObject*> targets = extractList(v);
                if (!targets.empty()) {
                    std::string combined;
                    for (size_t i = 0; i < targets.size(); ++i) {
                        if (i) combined += " ";
                        combined += buildHref(targets[i]);
                    }
                    os << " " << ref->getName() << "=\"" << escapeXmlAttr(combined, mapLimit_) << "\"";
                }
            }
        }
        if (containmentRefs.empty() && elementFeatures.empty()) {
            os << "/>\n";
            return;
        }
        os << ">\n";
        // element 风格 feature（EAttribute-as-element, non-containment-ref-as-element）
        for (auto* sf : elementFeatures) {
            writeElementFeature(obj, sf, depth + 1);
        }
        for (auto* ref : containmentRefs) {
            if (!obj->eIsSet(ref)) continue;
            std::any v = obj->eGet(ref);
            if (!ref->isMany()) {
                if (v.type() == typeid(emf::common::EObject*)) {
                    auto* child = std::any_cast<emf::common::EObject*>(v);
                    if (child) writeContainmentChild(child, ref->getName(), ref->getEReferenceType(), depth + 1);
                }
            } else {
                std::vector<emf::common::EObject*> children = extractList(v);
                for (auto* child : children) {
                    if (child) writeContainmentChild(child, ref->getName(), ref->getEReferenceType(), depth + 1);
                }
            }
        }
        // 未知 XML 内容原样回写（对齐 Java AnyType round-trip）
        writeUnknownContents(obj, depth);
        os << ind << "</" << featName << ">\n";
    }

    // 输出 EObject 关联的未知 XML 内容（对齐 Java EMF AnyType + FeatureMap round-trip）。
    // unknownContents_ 存的是 loader 在 recordUnknownFeature_=true 时记录的原始 XML 片段
    // （<未知标签 ...>...</未知标签>），saver 在该 obj 的所有 feature 子元素输出完后
    // 原样追加这些片段，保证未知标签 round-trip 保持。
    void writeUnknownContents(emf::common::EObject* obj, int depth) {
        if (!obj) return;
        const auto& unknown = res.unknownContents();
        for (const auto& [owner, fragment] : unknown) {
            if (owner != obj) continue;
            if (fragment.empty()) continue;
            // XML 片段已含缩进/换行（loader nodeToXmlString 产出），按原样追加。
            // 对齐 Java XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot() 的 mixed FeatureMap
            // 保存逻辑：未知元素原样写回。
            os << fragment;
        }
    }

    // 构造 href（对齐 Java XMLHelperImpl.getHREF）：
    //   - 优先 xmi:id：返回 "//<id>"（Java XMLResourceImpl 在 id-based 模式下的片段形式）
    //   - 否则尝试 position path "//@feat.idx/@feat.idx..."（Java 默认形式，如 //@publishers.1）
    //   - 若 position path 找不到（目标不在 containment 树中，如多根场景），
    //     自动分配合成 xmi:id 并返回 "//<id>"（对齐 Java XMLResourceImpl 对无 id 对象的处理）
    std::string buildHref(emf::common::EObject* target) {
        if (!target) return "";
        std::string id = res.getID(target);
        if (!id.empty()) return "//" + id;
        // 生成 position path：从 roots_ 出发，DFS 找到 target 的 containment 路径
        std::string path = buildPositionPath(target);
        // path 形如 "//@feat.idx"（在 containment 子树中）或 "/<idx>"（是顶层根）
        // 只接受 containment 子树路径（含 "@"），顶层根用 xmi:id 引用更标准
        if (!path.empty() && path.find("@") != std::string::npos) return path;
        // 目标不在 containment 子树中：分配合成 xmi:id（对齐 Java 对无 id 的非 containment 目标的处理）
        std::string autoId = assignSyntheticID(target);
        if (!autoId.empty()) return "//" + autoId;
        // 最终回退：返回 position path（即使是顶层根 /<idx>）
        if (!path.empty()) return path;
        return "";
    }

    // 分配合成 xmi:id（对齐 Java XMLResourceImpl 在序列化时为无 id 对象分配 id 的行为）
    // 用对象的内存地址生成稳定 id（单次序列化内唯一即可）
    std::string assignSyntheticID(emf::common::EObject* obj) {
        if (!obj) return "";
        // 已有 id 直接返回
        std::string existing = res.getID(obj);
        if (!existing.empty()) return existing;
        // 生成合成 id：用对象指针地址转 hex（单次序列化内稳定）
        char buf[32];
        std::snprintf(buf, sizeof(buf), "_o%p", (void*)obj);
        std::string sid = buf;
        const_cast<XMIResource&>(res).setID(obj, sid);
        return sid;
    }

    // DFS 查找 target 在 containment 树中的位置路径
    // 返回 "//@feat.idx" 或 "//@feat.idx/@feat2.idx2" 形式
    std::string buildPositionPath(emf::common::EObject* target) {
        for (size_t rootIdx = 0; rootIdx < roots_.size(); ++rootIdx) {
            auto* root = roots_[rootIdx];
            if (root == target) {
                // 根对象本身：多根时用 "/<rootIdx>"，单根无路径（不应通过 href 引用根）
                return roots_.size() > 1 ? "/" + std::to_string(rootIdx) : "//";
            }
            // 在 root 的 containment 子树中查找
            std::string sub = findInContainment(root, target);
            if (!sub.empty()) {
                return "//" + sub;
            }
        }
        return "";
    }

    // 在 obj 的 containment 子树中查找 target，返回 "@feat.idx[@feat.idx...]" 路径
    // （不含前导 "//"）；找不到返回 ""
    std::string findInContainment(emf::common::EObject* obj, emf::common::EObject* target) {
        auto* cls = obj ? obj->eClass() : nullptr;
        if (!cls) return "";
        for (auto* ref : cls->getEAllContainments()) {
            if (!ref) continue;
            std::any v = obj->eGet(ref);
            std::vector<emf::common::EObject*> children = extractList(v);
            for (size_t i = 0; i < children.size(); ++i) {
                auto* child = children[i];
                if (!child) continue;
                if (child == target) {
                    return "@" + ref->getName() + "." + std::to_string(i);
                }
                // 递归
                std::string deeper = findInContainment(child, target);
                if (!deeper.empty()) {
                    return "@" + ref->getName() + "." + std::to_string(i) + "/" + deeper;
                }
            }
        }
        return "";
    }

    std::vector<emf::common::EObject*> extractList(const std::any& v) {
        std::vector<emf::common::EObject*> r;
        if (!v.has_value()) return r;
        // EObjectRefView 零拷贝视图（codegen 多值 reference eGet fast-path）
        if (v.type() == typeid(emf::common::EObjectRefView)) {
            auto view = std::any_cast<emf::common::EObjectRefView>(v);
            r.reserve(view.size());
            for (auto* p : view) r.push_back(p);
            return r;
        }
        if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
            return std::any_cast<std::vector<emf::common::EObject*>>(v);
        }
        // DynamicEObject::eGet 对多值引用返回 vector<EObject*>*（内部 list 指针）
        if (v.type() == typeid(std::vector<emf::common::EObject*>*)) {
            auto* p = std::any_cast<std::vector<emf::common::EObject*>*>(v);
            if (p) return *p;
        }
        if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
            auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
            if (p) for (size_t i = 0; i < p->size(); ++i) r.push_back((*p)[i]);
        }
        return r;
    }
};

}  // namespace

// ===== 公开入口（XMLSaveImpl / XMIResource 通过此函数委托）=====
void saveInto(std::ostream& os, const XMIResource& res, const XMIOptions& opts) {
    auto& contents = const_cast<XMIResource&>(res).getContents();
    if (contents.empty()) {
        if (opts.xmlDeclaration) {
            std::string enc = !opts.encoding.empty() ? opts.encoding : (res.getEncoding().empty() ? std::string("UTF-8") : res.getEncoding());
            os << "<?xml version=\"1.0\" encoding=\"" << enc << "\"?>\n";
        }
        os << "<xmi:XMI xmi:version=\"" << opts.xmiVersion
           << "\" xmlns:xmi=\"" << kXmiNsURI << "\"/>\n";
        return;
    }
    // 单根 EPackage：走 ecore 元模型序列化
    if (contents.size() == 1 && isEPackageObject(contents[0])) {
        EcoreSaver s(os, opts, res);
        s.saveEPackage(dynamic_cast<emf::ecore::EPackage*>(contents[0]));
        return;
    }
    // 其它：实例文档序列化
    InstanceSaver s(os, opts, res);
    s.saveContents(contents);
}

}  // namespace emf::xmi
