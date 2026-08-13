// CppTemplates.cpp —— 模板字符串 + 替换实现
// 对齐 Java: org.eclipse.emf.codegen.ecore.templates.model.* (JET templates)
#include "emf/ecore/codegen/CppTemplates.h"
#include "emf/ecore/codegen/StringUtils.h"
#include "emf/ecore/codegen/TypeMapper.h"
#include "emf/ecore/EcorePackage.h"

#include <algorithm>
#include <cctype>
#include <sstream>
#include <stdexcept>
#include <regex>

namespace emf::ecore::codegen {

namespace {

std::string nsPath(const GenPackage* gp) {
    if (!gp) return "";
    if (gp->basePackage.empty()) return gp->prefix;
    return gp->basePackage + "::" + gp->prefix;
}

std::string toUpperStr(const std::string& s) {
    std::string out = s;
    for (auto& c : out) c = static_cast<char>(std::toupper(static_cast<unsigned char>(c)));
    return out;
}

std::string collectClassesBlock(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        out += "    emf::ecore::EClass* get" + gc->getClassName() + "() const { return " + gc->getClassName() + "_class_; }\n";
    }
    return out;
}

std::string collectFeatureAccessors(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        for (auto& gf : gc->genFeatures) {
            std::string cap = capitalizeFirst(gf->getFeatureName());
            if (gf->attribute) {
                out += "    emf::ecore::EAttribute* get" + gc->getClassName() + "_" + cap + "() const { return "
                    + gc->getClassName() + "_" + gf->getFeatureName() + "_attr_; }\n";
            } else {
                out += "    emf::ecore::EReference* get" + gc->getClassName() + "_" + cap + "() const { return "
                    + gc->getClassName() + "_" + gf->getFeatureName() + "_ref_; }\n";
            }
        }
    }
    return out;
}

std::string collectClassIndices(const GenPackage* gp) {
    std::string out;
    for (size_t i = 0; i < gp->genClasses.size(); ++i) {
        out += "    static const int " + toUpperStr(gp->genClasses[i]->getClassName())
             + " = " + std::to_string(i) + ";\n";
    }
    return out;
}

std::string collectFeatureStatics(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        out += "    static emf::ecore::EClass* " + gc->getClassName() + "_class_;\n";
        for (auto& gf : gc->genFeatures) {
            if (gf->attribute) {
                out += "    static emf::ecore::EAttribute* " + gc->getClassName() + "_" + gf->getFeatureName() + "_attr_;\n";
            } else {
                out += "    static emf::ecore::EReference* " + gc->getClassName() + "_" + gf->getFeatureName() + "_ref_;\n";
            }
        }
    }
    return out;
}

}  // namespace

// ===== 占位符替换 =====
std::string renderTemplate(const std::string& tmpl, const std::map<std::string, std::string>& vars) {
    // 走 renderJetTemplate，lists 为空 —— 与 renderJetTemplate 行为一致。
    return renderJetTemplate(tmpl, vars, {});
}

// ===== JET 子集渲染 =====
namespace {

// 在 s 中找 start_idx 之后第一个不嵌套的 control block（#each / #if / #unless），返回 (blockType, key, endOfOpen)；
// 若没找到，返回 ("", "", npos)。
//  该函数只考虑 {{#...}}...{{/...}} 配对的最外层；嵌套由外层 #each 内的 #each 处理。
struct BlockMatch {
    std::string type;       // "each" / "if" / "unless"
    std::string key;        // e.g. "features"
    size_t      openEnd;    // open block 结束位置（指向 '}}' 之后）
    size_t      bodyStart;  // body 起始
    size_t      closeStart; // close block '{{/...}}' 起始
    size_t      closeEnd;   // close block 结束位置（npos）
};

BlockMatch findFirstControlBlock(const std::string& s, size_t start) {
    BlockMatch m;
    size_t pos = start;
    while (pos < s.size()) {
        size_t openBrace = s.find("{{#", pos);
        if (openBrace == std::string::npos) break;
        size_t closeOpenBrace = s.find("}}", openBrace + 3);
        if (closeOpenBrace == std::string::npos) break;
        std::string header = s.substr(openBrace + 3, closeOpenBrace - (openBrace + 3));
        // header 形如 "each features" / "if cond" / "unless cond"
        size_t sp = header.find(' ');
        if (sp == std::string::npos) { pos = closeOpenBrace + 2; continue; }
        std::string type = header.substr(0, sp);
        if (type != "each" && type != "if" && type != "unless") { pos = closeOpenBrace + 2; continue; }
        std::string key = header.substr(sp + 1);
        // 找对应的 {{/type}}
        std::string closeMarker = "{{/" + type + "}}";
        size_t closePos = s.find(closeMarker, closeOpenBrace + 2);
        if (closePos == std::string::npos) break;
        m.type = type;
        m.key = key;
        m.openEnd = closeOpenBrace + 2;
        m.bodyStart = closeOpenBrace + 2;
        m.closeStart = closePos;
        m.closeEnd = closePos + closeMarker.size();
        return m;
    }
    return m;
}

}  // namespace

std::string renderJetTemplate(
    const std::string& tmpl,
    const std::map<std::string, std::string>& vars,
    const std::map<std::string, std::vector<std::map<std::string, std::string>>>& lists) {
    std::string out;
    out.reserve(tmpl.size());

    size_t pos = 0;
    while (pos < tmpl.size()) {
        BlockMatch bm = findFirstControlBlock(tmpl, pos);
        if (bm.type.empty()) {
            // 没有 control block，复制剩余并替换 {{var}}
            std::string rest = tmpl.substr(pos);
            // 复用单行替换
            size_t i = 0;
            while (i < rest.size()) {
                if (i + 1 < rest.size() && rest[i] == '{' && rest[i+1] == '{') {
                    size_t end = rest.find("}}", i + 2);
                    if (end == std::string::npos) { out += rest[i++]; continue; }
                    std::string key = rest.substr(i + 2, end - (i + 2));
                    auto it = vars.find(key);
                    if (it != vars.end()) out += it->second;
                    else out += "{{" + key + "}}";
                    i = end + 2;
                } else {
                    out += rest[i++];
                }
            }
            break;
        }

        // 1) 复制 open block 之前的静态部分（含 {{var}} 占位符替换）
        //  openBraceLoc = "{{#" 起始位置，pos = 当前扫描起点
        size_t openBraceLoc = tmpl.rfind("{{#", bm.openEnd - 2);
        std::string prefixText = tmpl.substr(pos, openBraceLoc - pos);
        for (size_t i = 0; i < prefixText.size(); ) {
            if (i + 1 < prefixText.size() && prefixText[i] == '{' && prefixText[i+1] == '{') {
                size_t end = prefixText.find("}}", i + 2);
                if (end == std::string::npos) { out += prefixText[i++]; continue; }
                std::string key = prefixText.substr(i + 2, end - (i + 2));
                auto it = vars.find(key);
                if (it != vars.end()) out += it->second;
                else out += "{{" + key + "}}";
                i = end + 2;
            } else {
                out += prefixText[i++];
            }
        }

        // 2) 处理 block body
        std::string body = tmpl.substr(bm.bodyStart, bm.closeStart - bm.bodyStart);
        if (bm.type == "each") {
            auto it = lists.find(bm.key);
            if (it != lists.end()) {
                for (const auto& row : it->second) {
                    // 合并 vars + row（row 字段优先）
                    std::map<std::string, std::string> merged = vars;
                    for (const auto& kv : row) merged[kv.first] = kv.second;
                    out += renderJetTemplate(body, merged, lists);
                }
            }
        } else if (bm.type == "if") {
            auto it = vars.find(bm.key);
            bool cond = (it != vars.end() && !it->second.empty());
            if (cond) out += renderJetTemplate(body, vars, lists);
        } else if (bm.type == "unless") {
            auto it = vars.find(bm.key);
            bool cond = (it != vars.end() && !it->second.empty());
            if (!cond) out += renderJetTemplate(body, vars, lists);
        }

        // 3) 跳过 close block
        pos = bm.closeEnd;
    }
    return out;
}

// ============================================================================
// Package Header Template
// ============================================================================
const std::string& packageHeaderTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
// EMF C++ Code Generator -- {{prefix}}Package
// GenModel: {{modelName}} (model plugin: {{modelPluginID}})
// EPackage: {{prefix}} (nsURI={{nsURI}})

#pragma once
#include "emf/common/EPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EList.h"
#include <string>
#include <vector>

namespace {{ns}} {

class {{prefix}}Package : public emf::ecore::EPackageImpl {
public:
    {{prefix}}Package();
    ~{{prefix}}Package() override = default;

    // 元信息常量（对齐 Java: <Pkg>Package.eNAME / eNS_URI / eNS_PREFIX）
    static const std::string eNAME;
    static const std::string eNS_URI;
    static const std::string eNS_PREFIX;

    // 单例 + 初始化
    static {{prefix}}Package* instance();
    static {{prefix}}Package* eINSTANCE;
    static void initialize();

    // EClass 索引常量
{{classIndices}}

    // EClass 访问器
{{classAccessors}}

    // EAttribute / EReference 访问器
{{featureAccessors}}

    // 内部：EClass / EFeature 静态字段
{{featureStatics}}
};

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitPackageHeader(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["modelName"] = ctx.genModel ? ctx.genModel->modelName : "";
    v["modelPluginID"] = ctx.genModel ? ctx.genModel->modelPluginID : "";
    v["nsURI"] = gp->ecorePackage ? gp->ecorePackage->getNsURI() : "";
    v["ns"] = nsPath(gp);
    v["classIndices"] = collectClassIndices(gp);
    v["classAccessors"] = collectClassesBlock(gp);
    v["featureAccessors"] = collectFeatureAccessors(gp);
    v["featureStatics"] = collectFeatureStatics(gp);
    return renderTemplate(packageHeaderTemplate(), v);
}

// ============================================================================
// Package Source Template
// ============================================================================
const std::string& packageSourceTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#include "{{prefix}}Package.h"
#include "{{prefix}}Factory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"

namespace {{ns}} {

const std::string {{prefix}}Package::eNAME = "{{ecoreName}}";
const std::string {{prefix}}Package::eNS_URI = "{{nsURI}}";
const std::string {{prefix}}Package::eNS_PREFIX = "{{nsPrefix}}";

{{classStatics}}
{{featureStatics}}

{{prefix}}Package* {{prefix}}Package::eINSTANCE = nullptr;

{{prefix}}Package::{{prefix}}Package() {
    setName(eNAME);
    setNsURI(eNS_URI);
    setNsPrefix(eNS_PREFIX);
}

{{prefix}}Package* {{prefix}}Package::instance() { initialize(); return eINSTANCE; }

void {{prefix}}Package::initialize() {
    if (eINSTANCE) return;

    // 1. 创建 EClass
{{createClasses}}
    // 2. 创建 EAttribute / EReference
{{createFeatures}}
    // 3. 把 features 挂到 EClass
{{attachFeatures}}
    // 4. 注册 Package
    eINSTANCE = new {{prefix}}Package();
    for (auto* c : { {{classPtrList}} }) {
        eINSTANCE->addEClassifier(c);
    }
    // 5. 关联 Factory
    {{prefix}}Factory::initialize();
    eINSTANCE->setEFactoryInstance({{prefix}}Factory::eINSTANCE);

    emf::common::EPackageRegistry::instance().put(eNS_URI, eINSTANCE);
}

}  // namespace {{ns}}
)CTPL";
    return t;
}

namespace {
std::string classStaticDefs(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        out += "emf::ecore::EClass* " + gp->prefix + "Package::" + gc->getClassName() + "_class_ = nullptr;\n";
    }
    return out;
}
std::string featureStaticDefs(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        for (auto& gf : gc->genFeatures) {
            std::string cls = gc->getClassName();
            if (gf->attribute) {
                out += "emf::ecore::EAttribute* " + gp->prefix + "Package::" + cls + "_" + gf->getFeatureName() + "_attr_ = nullptr;\n";
            } else {
                out += "emf::ecore::EReference* " + gp->prefix + "Package::" + cls + "_" + gf->getFeatureName() + "_ref_ = nullptr;\n";
            }
        }
    }
    return out;
}
std::string createClassesBlock(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        out += "    " + gc->getClassName() + "_class_ = emf::ecore::EcoreFactory::instance().createEClass();\n";
        out += "    " + gc->getClassName() + "_class_->setName(\"" + gc->getClassName() + "\");\n";
    }
    return out;
}
std::string createFeaturesBlock(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        for (auto& gf : gc->genFeatures) {
            std::string cls = gc->getClassName();
            std::string fname = gf->getFeatureName();
            if (gf->attribute) {
                out += "    " + cls + "_" + fname + "_attr_ = emf::ecore::EcoreFactory::instance().createEAttribute();\n";
                out += "    " + cls + "_" + fname + "_attr_->setName(\"" + fname + "\");\n";
                out += "    " + cls + "_" + fname + "_attr_->setLowerBound(" + std::to_string(gf->lowerBound) + ");\n";
                out += "    " + cls + "_" + fname + "_attr_->setUpperBound(" + std::to_string(gf->upperBound) + ");\n";
                out += "    " + cls + "_" + fname + "_attr_->setEAttributeType(emf::ecore::EcorePackage::instance().getEDataType_" + gf->type + "());\n";
            } else {
                out += "    " + cls + "_" + fname + "_ref_ = emf::ecore::EcoreFactory::instance().createEReference();\n";
                out += "    " + cls + "_" + fname + "_ref_->setName(\"" + fname + "\");\n";
                out += "    " + cls + "_" + fname + "_ref_->setLowerBound(" + std::to_string(gf->lowerBound) + ");\n";
                out += "    " + cls + "_" + fname + "_ref_->setUpperBound(" + std::to_string(gf->upperBound) + ");\n";
                out += "    " + cls + "_" + fname + "_ref_->setContainment(" + std::string(gf->containment ? "true" : "false") + ");\n";
                if (!gf->type.empty()) {
                    out += "    " + cls + "_" + fname + "_ref_->setEReferenceType(" + gf->type + "_class_);\n";
                }
            }
        }
    }
    return out;
}
std::string attachFeaturesBlock(const GenPackage* gp) {
    std::string out;
    for (auto& gc : gp->genClasses) {
        for (auto& gf : gc->genFeatures) {
            std::string cls = gc->getClassName();
            if (gf->attribute) {
                out += "    " + cls + "_class_->addEStructuralFeature(" + cls + "_" + gf->getFeatureName() + "_attr_);\n";
            } else {
                out += "    " + cls + "_class_->addEStructuralFeature(" + cls + "_" + gf->getFeatureName() + "_ref_);\n";
            }
        }
    }
    return out;
}
std::string classPtrList(const GenPackage* gp) {
    std::string out;
    for (size_t i = 0; i < gp->genClasses.size(); ++i) {
        if (i) out += ", ";
        out += gp->genClasses[i]->getClassName() + "_class_";
    }
    return out;
}
}  // namespace

std::string emitPackageSource(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["ecoreName"] = gp->ecorePackage ? gp->ecorePackage->getName() : "";
    v["nsURI"] = gp->ecorePackage ? gp->ecorePackage->getNsURI() : "";
    v["nsPrefix"] = gp->ecorePackage ? gp->ecorePackage->getNsPrefix() : "";
    v["ns"] = nsPath(gp);
    v["classStatics"] = classStaticDefs(gp);
    v["featureStatics"] = featureStaticDefs(gp);
    v["createClasses"] = createClassesBlock(gp);
    v["createFeatures"] = createFeaturesBlock(gp);
    v["attachFeatures"] = attachFeaturesBlock(gp);
    v["classPtrList"] = classPtrList(gp);
    return renderTemplate(packageSourceTemplate(), v);
}

// ============================================================================
// Factory Header / Source
// ============================================================================
const std::string& factoryHeaderTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#pragma once
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/EPackage.h"

namespace {{ns}} {

class {{prefix}}Factory : public emf::ecore::EFactoryImpl {
public:
    {{prefix}}Factory();
    ~{{prefix}}Factory() override = default;

{{createMethods}}
    static {{prefix}}Factory* instance();
    static {{prefix}}Factory* eINSTANCE;
    static void initialize();
};

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitFactoryHeader(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["ns"] = nsPath(gp);
    std::string cm;
    for (auto& gc : gp->genClasses) {
        cm += "    emf::common::EObject* create" + gc->getClassName() + "();\n";
    }
    v["createMethods"] = cm;
    return renderTemplate(factoryHeaderTemplate(), v);
}

const std::string& factorySourceTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#include "{{prefix}}Factory.h"
#include "{{prefix}}Package.h"
{{createImpls}}
namespace {{ns}} {

{{prefix}}Factory* {{prefix}}Factory::eINSTANCE = nullptr;

{{prefix}}Factory::{{prefix}}Factory() {
    setEPackage({{prefix}}Package::eINSTANCE);
}

{{createBodies}}

{{prefix}}Factory* {{prefix}}Factory::instance() { initialize(); return eINSTANCE; }

void {{prefix}}Factory::initialize() {
    if (eINSTANCE) return;
    eINSTANCE = new {{prefix}}Factory();
}

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitFactorySource(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["ns"] = nsPath(gp);
    std::string ci, cb;
    for (auto& gc : gp->genClasses) {
        // 单类单继承方案：include <ClassName>.h，create 返回 new <ClassName>()
        ci += "#include \"" + gc->getClassName() + ".h\"\n";
        cb += "emf::common::EObject* " + gp->prefix + "Factory::create" + gc->getClassName() + "() {\n"
            + "    return new " + gc->getClassName() + "();\n"
            + "}\n\n";
    }
    v["createImpls"] = ci;
    v["createBodies"] = cb;
    return renderTemplate(factorySourceTemplate(), v);
}

// ============================================================================
// Switch Header / Source
// ============================================================================
const std::string& switchHeaderTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#pragma once
#include "emf/common/EObject.h"
#include "emf/common/EList.h"
{{includes}}

namespace {{ns}} {

// <Pkg>Switch: 类型分发（对齐 Java <Pkg>Switch<T> extends Switch<T>）
template <typename T>
class {{prefix}}Switch {
public:
    {{prefix}}Switch() = default;
    virtual ~{{prefix}}Switch() = default;

    T doSwitch(emf::common::EObject* obj) {
        if (!obj) return defaultCase(nullptr);
{{doSwitchBody}}
        return defaultCase(obj);
    }

{{caseMethods}}
    virtual T defaultCase(emf::common::EObject* object) { (void)object; return T{}; }
};

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitSwitchHeader(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["ns"] = nsPath(gp);
    std::string inc, body, cases;
    for (auto& gc : gp->genClasses) {
        inc += "#include \"" + gc->getClassName() + ".h\"\n";
        body += "        { auto* t = dynamic_cast<" + gc->getClassName() + "*>(obj); if (t) return case"
              + gc->getClassName() + "(t); }\n";
        cases += "    virtual T case" + gc->getClassName() + "(" + gc->getClassName()
              + "* object) { return defaultCase(object); }\n";
    }
    v["includes"] = inc;
    v["doSwitchBody"] = body;
    v["caseMethods"] = cases;
    return renderTemplate(switchHeaderTemplate(), v);
}

const std::string& switchSourceTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
// Switch is template; implementation lives in {{prefix}}Switch.h
)CTPL";
    return t;
}

std::string emitSwitchSource(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    std::map<std::string, std::string> v;
    v["prefix"] = ctx.genPackage->prefix;
    return renderTemplate(switchSourceTemplate(), v);
}

// ============================================================================
// AdapterFactory Header / Source
// ============================================================================
const std::string& adapterFactoryHeaderTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#pragma once
#include "emf/common/Adapter.h"
#include "emf/common/AdapterFactory.h"

namespace {{ns}} {

class {{prefix}}AdapterFactory : public emf::common::AdapterFactory {
public:
    {{prefix}}AdapterFactory() = default;
    ~{{prefix}}AdapterFactory() override = default;

    emf::common::Adapter* createAdapter(emf::common::EObject* target) override;
    bool isFactoryForType(emf::common::EObject* type) const override;

    static {{prefix}}AdapterFactory* instance();
    static {{prefix}}AdapterFactory* eINSTANCE;
    static void initialize();
};

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitAdapterFactoryHeader(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["ns"] = nsPath(gp);
    return renderTemplate(adapterFactoryHeaderTemplate(), v);
}

const std::string& adapterFactorySourceTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#include "{{prefix}}AdapterFactory.h"
#include "{{prefix}}Package.h"
{{includes}}

namespace {{ns}} {

{{prefix}}AdapterFactory* {{prefix}}AdapterFactory::eINSTANCE = nullptr;

emf::common::Adapter* {{prefix}}AdapterFactory::createAdapter(emf::common::EObject* target) {
    (void)target;
    return nullptr;
}

bool {{prefix}}AdapterFactory::isFactoryForType(emf::common::EObject* type) const {
    if (!type) return false;
    auto* cls = type->eClass();
    if (!cls || !cls->getEPackage()) return false;
    return cls->getEPackage()->getName() == "{{ecoreName}}";
}

{{prefix}}AdapterFactory* {{prefix}}AdapterFactory::instance() { initialize(); return eINSTANCE; }

void {{prefix}}AdapterFactory::initialize() {
    if (eINSTANCE) return;
    eINSTANCE = new {{prefix}}AdapterFactory();
}

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitAdapterFactorySource(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["ecoreName"] = gp->ecorePackage ? gp->ecorePackage->getName() : "";
    std::string inc;
    for (auto& gc : gp->genClasses) {
        inc += "#include \"" + gc->getClassName() + ".h\"\n";
    }
    v["includes"] = inc;
    return renderTemplate(adapterFactorySourceTemplate(), v);
}

// ============================================================================
// Validator Header / Source
// ============================================================================
const std::string& validatorHeaderTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#pragma once
#include "emf/ecore/util/EObjectValidator.h"

namespace {{ns}} {

class {{prefix}}Validator : public emf::ecore::util::EObjectValidator {
public:
    {{prefix}}Validator() = default;
    ~{{prefix}}Validator() override = default;

    bool validate(emf::common::EObject* object, std::vector<emf::common::Diagnostic>& diagnostics) override;
{{validateMethods}}
    static {{prefix}}Validator* instance();
    static {{prefix}}Validator* eINSTANCE;
    static void initialize();
};

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitValidatorHeader(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    v["ns"] = nsPath(gp);
    std::string vm;
    for (auto& gc : gp->genClasses) {
        vm += "    bool validate" + gc->getClassName() + "(" + gc->getClassName()
           + "*, std::vector<emf::common::Diagnostic>&);\n";
    }
    v["validateMethods"] = vm;
    return renderTemplate(validatorHeaderTemplate(), v);
}

const std::string& validatorSourceTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#include "{{prefix}}Validator.h"
#include "{{prefix}}Package.h"
{{includes}}

namespace {{ns}} {

{{prefix}}Validator* {{prefix}}Validator::eINSTANCE = nullptr;

bool {{prefix}}Validator::validate(emf::common::EObject* object, std::vector<emf::common::Diagnostic>& diagnostics) {
    if (!object) return true;
    bool ok = true;
{{validateBody}}
    return ok;
}

{{prefix}}Validator* {{prefix}}Validator::instance() { initialize(); return eINSTANCE; }

void {{prefix}}Validator::initialize() {
    if (eINSTANCE) return;
    eINSTANCE = new {{prefix}}Validator();
}

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitValidatorSource(const TemplateContext& ctx) {
    if (!ctx.genPackage) return "";
    GenPackage* gp = ctx.genPackage;
    std::map<std::string, std::string> v;
    v["prefix"] = gp->prefix;
    std::string inc, body;
    for (auto& gc : gp->genClasses) {
        inc += "#include \"" + gc->getClassName() + ".h\"\n";
        body += "    { auto* t = dynamic_cast<" + gc->getClassName() + "*>(object); if (t) ok &= validate"
              + gc->getClassName() + "(t, diagnostics); }\n";
    }
    v["includes"] = inc;
    v["validateBody"] = body;
    return renderTemplate(validatorSourceTemplate(), v);
}

// ============================================================================
// 单类单继承方案（替代旧 Interface + Impl 双类方案）：
//   - 每个 EClass 只生成 <ClassName>.h + <ClassName>.cpp（无 Impl 后缀）
//   - 类名 = EClass 名，直接继承 emf::ecore::EObjectImpl
//   - 保留 EMF 运行时行为：typed getter/setter、字段、eStaticClass
// ============================================================================
const std::string& classHeaderTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
// EClass: {{className}} (single-class, single-inheritance)
#pragma once
#include "{{prefix}}Package.h"
#include "emf/common/EObject.h"
#include "emf/common/Notification.h"
{{extraIncludes}}

namespace {{ns}} {

class {{className}} : public emf::ecore::EObjectImpl {
public:
    {{className}}();
    ~{{className}}() override = default;

{{accessors}}

    emf::ecore::EClass* eStaticClass() const override;
{{fields}}
};

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitInterfaceHeader(const TemplateContext& ctx) {
    if (!ctx.genClass) return "";
    GenClass* gc = ctx.genClass;
    std::map<std::string, std::string> v;
    v["className"] = gc->getClassName();
    v["prefix"] = ctx.genPackage ? ctx.genPackage->prefix : "";
    v["ns"] = ctx.genPackage ? nsPath(ctx.genPackage) : "";
    std::string acc, fld, inc;
    for (auto& gf : gc->genFeatures) {
        std::string rt = gf->getCppType();
        std::string cap = capitalizeFirst(gf->getFeatureName());
        std::string lname = gf->getUncapSafeName();
        if (gf->attribute) {
            acc += "    " + rt + " get" + cap + "() const { return " + lname + "_; }\n";
            acc += "    void set" + cap + "(" + rt + " v) { " + lname + "_ = v; }\n";
            fld += "    " + rt + " " + lname + "_" + (gf->getCppDefaultValue().empty() ? "" : (" = " + gf->getCppDefaultValue())) + ";\n";
        } else {
            acc += "    " + rt + " get" + cap + "() const { return " + lname + "_; }\n";
            if (!gf->many) {
                acc += "    void set" + cap + "(" + rt + " v) { " + lname + "_ = v; }\n";
            } else {
                std::string inner = gf->type.empty() ? std::string("emf::common::EObject*") : gf->type;
                acc += "    void add" + cap + "(" + inner + " v) { " + lname + "_.push_back(v); }\n";
            }
            fld += "    " + rt + " " + lname + "_;\n";
        }
    }
    v["accessors"] = acc;
    v["fields"] = fld;
    v["extraIncludes"] = inc;
    return renderTemplate(classHeaderTemplate(), v);
}

// ============================================================================
// 单类实现（替代旧 Impl Header / Source）：输出 <ClassName>.h + <ClassName>.cpp
// ============================================================================
const std::string& classSourceTemplate() {
    static const std::string t = R"CTPL(// <auto-generated/>
#include "{{className}}.h"
#include "{{prefix}}Package.h"

namespace {{ns}} {

{{className}}::{{className}}() {
    // 初始化
}

emf::ecore::EClass* {{className}}::eStaticClass() const {
    return {{prefix}}Package::instance()->get{{className}}();
}

}  // namespace {{ns}}
)CTPL";
    return t;
}

std::string emitImplSource(const TemplateContext& ctx) {
    if (!ctx.genClass) return "";
    GenClass* gc = ctx.genClass;
    std::map<std::string, std::string> v;
    v["className"] = gc->getClassName();
    v["prefix"] = ctx.genPackage ? ctx.genPackage->prefix : "";
    v["ns"] = ctx.genPackage ? nsPath(ctx.genPackage) : "";
    return renderTemplate(classSourceTemplate(), v);
}

}  // namespace emf::ecore::codegen
