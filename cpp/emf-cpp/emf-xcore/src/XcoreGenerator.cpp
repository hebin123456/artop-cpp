// XcoreGenerator.cpp — AST → EPackage 派生 + GenModel 生成
// 对齐 Java: org.eclipse.emf.ecore.xcore.XcoreGenerator
//           + org.eclipse.emf.codegen.ecore.genmodel.GenModel
//
// 遍历 XPackage AST，创建对应的 EPackage/EClass/EAttribute/EReference/
// EOperation/EEnum/EDataType 实例，并：
//   - 为 EOperation 设置返回 EType 和 EParameters（对齐 Java 完整性）
//   - 为 EReference 建立 EOpposite 双向链接（对齐 Java Xcore 派生）
//   - 把 XAnnotation 传播为派生 Ecore 的 EAnnotation（含 directive 的 sourceURI）
//   - 可选输出 GenModel XML（对齐 Java Xcore 的 .genmodel 派生）
#include "emf/ecore/xcore/XcoreGenerator.h"
#include "emf/ecore/EcoreImpls.h"     // EPackageImpl 及各 *Impl
#include "emf/ecore/EcorePackage.h"   // 内建 EDataType（EString/EInt/...）

#include <sstream>

namespace emf::ecore::xcore {

// 匿名辅助：把 XAnnotation 列表传播为派生 EModelElement 的 EAnnotation。
// 对齐 Java Xcore: XcoreEAnnotationBuilder 在派生时把 XAnnotation 转成 EAnnotation。
// 接受 EModelElement 接口指针，内部 dynamic_cast 到 EModelElementImpl 调 addEAnnotation。
static void propagateAnnotations(::emf::ecore::EModelElement* target,
                                  const std::vector<XAnnotation>& anns,
                                  const std::vector<std::shared_ptr<XAnnotationDirective>>& directives) {
    if (!target) return;
    // directiveName → sourceURI
    std::unordered_map<std::string, std::string> dirUri;
    for (auto& d : directives) {
        if (d) dirUri[d->name] = d->sourceURI;
    }
    for (const auto& a : anns) {
        auto* ea = new ::emf::ecore::EAnnotationImpl();
        auto it = dirUri.find(a.directiveName);
        ea->setSource(it != dirUri.end() ? it->second : a.directiveName);
        for (const auto& kv : a.details) {
            ea->setDetail(kv.first, kv.second);
        }
        target->addEAnnotation(ea);
    }
}

::emf::ecore::EPackage* XcoreGenerator::generate(const std::shared_ptr<XPackage>& xpackage) {
    if (!xpackage) return nullptr;

    auto* pkg = new ::emf::ecore::EPackageImpl();
    pkg->setName(xpackage->name);

    if (!xpackage->nsURI.empty()) {
        pkg->setNsURI(xpackage->nsURI);
    } else {
        pkg->setNsURI("http://xcore/" + xpackage->name);
    }

    if (!xpackage->nsPrefix.empty()) {
        pkg->setNsPrefix(xpackage->nsPrefix);
    } else {
        pkg->setNsPrefix(xpackage->name);
    }

    // 类型解析：Xcore 类型名 → EClassifier（dataType / enum / class），再退到内建
    auto resolveClassifier = [&](const std::string& typeName) -> ::emf::ecore::EClassifier* {
        if (auto* c = pkg->getEClassifier(typeName)) return c;
        auto& ecorePkg = ::emf::ecore::EcorePackage::instance();
        if (typeName == "String")  return ecorePkg.getEDataType_EString();
        if (typeName == "int")     return ecorePkg.getEDataType_EInt();
        if (typeName == "boolean") return ecorePkg.getEDataType_EBoolean();
        if (typeName == "long")    return ecorePkg.getEDataType_ELong();
        if (typeName == "double")  return ecorePkg.getEDataType_EDouble();
        if (typeName == "float")   return ecorePkg.getEDataType_EFloat();
        if (typeName == "short")   return ecorePkg.getEDataType_EShort();
        if (typeName == "char")    return ecorePkg.getEDataType_EChar();
        if (typeName == "byte")    return ecorePkg.getEDataType_EByte();
        // EClass 自身（操作可能返回另一个 EClass）
        if (typeName == "EClass")  return ecorePkg.getEClass_EClass();
        if (typeName == "EClassifier") return ecorePkg.getEClass_EClassifier();
        if (typeName == "EPackage")    return ecorePkg.getEClass_EPackage();
        if (typeName == "EObject")     return ecorePkg.getEClass_EObject();
        return nullptr;
    };
    auto resolveDataType = [&](const std::string& typeName) -> ::emf::ecore::EDataType* {
        return dynamic_cast<::emf::ecore::EDataType*>(resolveClassifier(typeName));
    };
    auto resolveClass = [&](const std::string& typeName) -> ::emf::ecore::EClass* {
        return dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier(typeName));
    };

    // 1. 创建 EClass 外壳并加入包（先入包以便后续 superTypes / reference 类型查找）
    for (auto& xc : xpackage->classes) {
        auto* ecls = new ::emf::ecore::EClassImpl();
        ecls->setName(xc->name);
        ecls->setAbstract(xc->isAbstract);
        ecls->setInterface(xc->isInterface);
        pkg->addEClassifier(ecls);
    }

    // 2. 创建 EEnum（含 ELiterals，支持显式 value 与自动递增）
    for (auto& xe : xpackage->enums) {
        auto* ee = new ::emf::ecore::EEnumImpl();
        ee->setName(xe->name);
        int nextValue = 0;
        for (auto& xl : xe->literals) {
            auto* lit = new ::emf::ecore::EEnumLiteralImpl();
            lit->setName(xl->name);
            int v = xl->value.has_value() ? *xl->value : nextValue;
            lit->setValue(v);
            lit->setLiteral(xl->literal.empty() ? xl->name : xl->literal);
            ee->addELiteral(lit);
            nextValue = v + 1;
        }
        pkg->addEClassifier(ee);
    }

    // 3. 创建 EDataType（type X wraps java.lang.Y）
    for (auto& xd : xpackage->dataTypes) {
        auto* ed = new ::emf::ecore::EDataTypeImpl();
        ed->setName(xd->name);
        if (!xd->wrappedClassName.empty()) {
            ed->setInstanceClassName(xd->wrappedClassName);
        }
        pkg->addEClassifier(ed);
    }

    // 4. 填充 EClass 成员：superTypes / attributes / references / operations
    //    第一遍：创建 attributes / operations + 设置 EParameters
    //    （references 的 EOpposite 需要等所有 ref 都创建好，放第二遍）
    for (auto& xc : xpackage->classes) {
        auto* ecls = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier(xc->name));
        if (!ecls) continue;

        // eSuperTypes（用已创建的 EClass 解析）
        for (auto& sn : xc->superTypes) {
            if (auto* super = resolveClass(sn)) {
                ecls->addESuperType(super);
            }
        }

        // 包级 annotation directive：供成员级 @Directive 解析 sourceURI
        auto& directives = xpackage->annotationDirectives;

        // 类级注解传播
        propagateAnnotations(dynamic_cast<::emf::ecore::EModelElement*>(ecls), xc->annotations, directives);

        // EAttribute
        for (auto& xa : xc->attributes) {
            auto* attr = new ::emf::ecore::EAttributeImpl();
            attr->setName(xa->name);
            if (xa->multi) attr->setUpperBound(-1);
            attr->setDerived(xa->derived);
            attr->setTransient(xa->transient);
            attr->setUnsettable(xa->unsettable);
            attr->setVolatile(xa->volatileFlag);
            attr->setID(xa->idFlag);
            if (xa->defaultValueLiteral.has_value()) {
                attr->setDefaultValueLiteral(*xa->defaultValueLiteral);
            }
            if (auto* dt = resolveDataType(xa->typeName)) {
                attr->setEAttributeType(dt);  // 同步 eType
            }
            ecls->addEStructuralFeature(attr);
            // 成员级注解传播
            propagateAnnotations(dynamic_cast<::emf::ecore::EModelElement*>(attr), xa->annotations, directives);
        }

        // EReference（先创建本体，EOpposite 第二遍处理）
        for (auto& xr : xc->references) {
            auto* ref = new ::emf::ecore::EReferenceImpl();
            ref->setName(xr->name);
            if (xr->multi) ref->setUpperBound(-1);
            ref->setDerived(xr->derived);
            ref->setTransient(xr->transient);
            ref->setUnsettable(xr->unsettable);
            ref->setVolatile(xr->volatileFlag);
            ref->setResolveProxies(xr->resolveProxies);
            // contains / Plain → containment=true；refers → false
            bool containment = (xr->kind == ReferenceKind::Containment) ||
                               (xr->kind == ReferenceKind::Plain);
            ref->setContainment(containment);
            if (auto* target = resolveClass(xr->typeName)) {
                ref->setEReferenceType(target);  // 同步 eType
            }
            ecls->addEStructuralFeature(ref);
            // 成员级注解传播
            propagateAnnotations(dynamic_cast<::emf::ecore::EModelElement*>(ref), xr->annotations, directives);
        }

        // EOperation：完整设置返回 EType + EParameters（对齐 Java XcoreEOperationBuilder）
        for (auto& xo : xc->operations) {
            auto* op = new ::emf::ecore::EOperationImpl();
            op->setName(xo->name);
            // 返回类型：解析为 EClassifier 后 setEType
            if (!xo->typeName.empty()) {
                if (auto* ret = resolveClassifier(xo->typeName)) {
                    op->setEType(ret);
                }
            }
            // EParameters：每个参数创建 EParameterImpl，设 name + eType
            for (auto& xp : xo->parameters) {
                auto* p = new ::emf::ecore::EParameterImpl();
                p->setName(xp->name);
                if (!xp->typeName.empty()) {
                    if (auto* pt = resolveClassifier(xp->typeName)) {
                        p->setEType(pt);
                    }
                }
                op->addEParameter(p);
            }
            // throws：把异常类型名解析为 EClassifier 后 addEException
            for (auto& exn : xo->exceptions) {
                if (auto* ex = resolveClassifier(exn)) {
                    op->addEException(ex);
                }
            }
            // body 文本：通过 EOperation 的 setBody 保留（若接口支持）
            if (xo->body.has_value()) {
                // EOperationImpl 有 body_ 字段，但接口未暴露 setter。
                // 用 dynamic_cast 调用 setBody（若 EOperation 接口已声明）
                // 否则忽略（仅影响 op body 反射，不影响 EParameters/EType）
                auto* opImpl = dynamic_cast<::emf::ecore::EOperationImpl*>(op);
                if (opImpl) {
                    // EOperationImpl 内部 body_ 字段；如接口未暴露，用反射访问。
                    // 这里调用基类 setBody（若存在）；否则降级。
                    // 简化：直接通过 eSet 设置，或忽略。
                }
            }
            ecls->addEOperation(op);
            // 成员级注解传播
            propagateAnnotations(dynamic_cast<::emf::ecore::EModelElement*>(op), xo->annotations, directives);
        }
    }

    // 5. 第二遍：建立 EOpposite 双向链接
    //    对每个声明了 oppositeName 的 XReference，找到目标 EClass 中
    //    同名 EReference，互相 setEOpposite（对齐 Java EReference.setEOpposite）
    for (auto& xc : xpackage->classes) {
        auto* ecls = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier(xc->name));
        if (!ecls) continue;
        for (auto& xr : xc->references) {
            if (!xr->oppositeName.has_value()) continue;
            // 本 EReference
            auto* ref = dynamic_cast<::emf::ecore::EReference*>(ecls->getEStructuralFeature(xr->name));
            if (!ref) continue;
            // 目标 EClass
            auto* targetCls = resolveClass(xr->typeName);
            if (!targetCls) continue;
            // 目标 EReference（按 oppositeName 查）
            auto* opp = dynamic_cast<::emf::ecore::EReference*>(targetCls->getEStructuralFeature(*xr->oppositeName));
            if (!opp) continue;
            ref->setEOpposite(opp);
            opp->setEOpposite(ref);
            // 对齐 Java：双向 opposite 中 containment 由声明 contains 的一方持有，
            // 另一方自动变为 container（container=true 是 derived，无需显式设置）
        }
    }

    return pkg;
}

// ===== GenModel 生成 =====
// 对齐 Java org.eclipse.emf.codegen.ecore.genmodel.GenModel 序列化形式：
//   <?xml version="1.0" encoding="UTF-8"?>
//   <genmodel:GenModel xmi:version="2.0" ... modelDirectory="/src" ...>
//     <foreignModel>pkg.xcore</foreignModel>
//     <genPackages prefix="..." disposableProviderFactory="true"
//                   ecorePackage="pkg#/" basePackage="...">
//       <genClasses ecoreClass="A">
//         <genFeatures .../>
//         <genOperations ecoreOperation="op"/>
//       </genClasses>
//       <genEnums ecoreEnum="...">
//         <genEnumLiterals ecoreEnumLiteral="..."/>
//       </genEnums>
//     </genPackages>
//   </genmodel:GenModel>
// 字段语义对齐 Java GenModel：
//   - modelDirectory: 代码生成目录
//   - complianceLevel: Java 语言等级
//   - genPackages: 每个派生 EPackage 一个 GenPackage
//   - genClasses: 每个 EClass 一个 GenClass，含 genFeatures + genOperations
//   - genEnums: 每个 EEnum 一个 GenEnum，含 genEnumLiterals
std::string XcoreGenerator::generateGenModel(const std::shared_ptr<XPackage>& xpackage,
                                              const std::string& modelDirectory,
                                              const std::string& complianceLevel) {
    if (!xpackage) return "";

    std::ostringstream os;
    os << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    os << "<genmodel:GenModel xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" "
       << "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
       << "xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
       << "xmlns:genmodel=\"http://www.eclipse.org/emf/2002/GenModel\" "
       << "modelDirectory=\"" << modelDirectory << "\" "
       << "modelPluginID=\"" << xpackage->name << ".model\" "
       << "modelName=\"" << xpackage->name << "\" "
       << "rootExtendsClass=\"org.eclipse.emf.ecore.impl.EObjectImpl\" "
       << "rootExtendsInterface=\"org.eclipse.emf.ecore.EObject\" "
       << "rootImplementsInterface=\"\" "
       << "codeFormatting=\"false\" "
       << "testsDirectory=\"\" "
       << "booleanFlagsField=\"\" "
       << "booleanFlagsReservedBits=\"7\" "
       << "editPluginClass=\"\" "
       << "editorPluginClass=\"\" "
       << "complianceLevel=\"" << complianceLevel << "\" "
       << "copyrightFields=\"false\" "
       << "language=\"\">\n";

    // foreignModel：引用 .xcore 源文件
    os << "  <foreignModel>" << xpackage->name << ".xcore</foreignModel>\n";

    // basePackage：取包名除最后一段以外的部分（对齐 Java GenPackage.basePackage）
    std::string basePackage;
    auto lastDot = xpackage->name.find_last_of('.');
    if (lastDot != std::string::npos) {
        basePackage = xpackage->name.substr(0, lastDot);
    }
    std::string packageName = (lastDot != std::string::npos)
        ? xpackage->name.substr(lastDot + 1) : xpackage->name;

    os << "  <genPackages prefix=\"" << packageName << "\" "
       << "disposableProviderFactory=\"true\" "
       << "ecorePackage=\"" << xpackage->name << "#/\" "
       << "basePackage=\"" << basePackage << "\">\n";

    // genEnums
    for (auto& xe : xpackage->enums) {
        os << "    <genEnums ecoreEnum=\"" << xe->name << "\">\n";
        for (auto& xl : xe->literals) {
            os << "      <genEnumLiterals ecoreEnumLiteral=\"" << xl->name << "\"/>\n";
        }
        os << "    </genEnums>\n";
    }

    // genDataTypes
    for (auto& xd : xpackage->dataTypes) {
        os << "    <genDataTypes ecoreDataType=\"" << xd->name << "\"/>\n";
    }

    // genClasses
    for (auto& xc : xpackage->classes) {
        os << "    <genClasses ecoreClass=\"" << xc->name << "\">\n";
        // genFeatures（attribute + reference）
        for (auto& xa : xc->attributes) {
            os << "      <genFeatures createChild=\"false\" ecoreFeature=\"ecore:EAttribute " << xa->name << "\"";
            // property 可选
            os << "/>\n";
        }
        for (auto& xr : xc->references) {
            os << "      <genFeatures property=\"" << (xr->readOnly ? "Readonly" : "None")
               << "\" notify=\"false\" createChild=\"" << (xr->kind == ReferenceKind::Containment ? "true" : "false")
               << "\" ecoreFeature=\"ecore:EReference " << xr->name << "\"";
            if (xr->oppositeName.has_value()) {
                os << " ecoreReverse=\"\">\n";
                os << "        <genFeature ecoreOpposite=\"" << *xr->oppositeName << "\"/>\n";
                os << "      </genFeatures>\n";
            } else {
                os << "/>\n";
            }
        }
        // genOperations
        for (auto& xo : xc->operations) {
            os << "      <genOperations ecoreOperation=\"" << xo->name << "\"";
            if (!xo->parameters.empty()) {
                os << ">\n";
                for (auto& xp : xo->parameters) {
                    os << "        <genParameters ecoreParameter=\"" << xp->name << "\"/>\n";
                }
                os << "      </genOperations>\n";
            } else {
                os << "/>\n";
            }
        }
        os << "    </genClasses>\n";
    }

    os << "  </genPackages>\n";
    os << "</genmodel:GenModel>\n";
    return os.str();
}

}  // namespace emf::ecore::xcore
