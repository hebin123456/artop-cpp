// GenModelLoader.cpp —— 加载 .genmodel XMI 到内存中的 GenModel 数据结构
// 对齐 Java: org.eclipse.emf.codegen.ecore.genmodel.util.GenModelUtil + .impl.GenModelImpl
//
// Java 是把 .genmodel 当 Ecore 模型用反射加载；我们直接 XML 解析。
#include "emf/ecore/codegen/GenModelLoader.h"
#include "emf/ecore/codegen/GenModel.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"

#include <algorithm>
#include <cctype>
#include <cstring>
#include <fstream>
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <stdexcept>

namespace emf::ecore::codegen {

namespace {

// 极简 XML 解析器（只支持 XMI / ecore / genmodel 实际使用的子集）
// 不处理 DTD / external entity / processing instructions（XMI 也不需要）。
// 仅生成 DOM 树（element + attribute + 文本子节点）。
struct XmlNode {
    std::string qname;        // 不带 namespace 前缀的元素名
    std::string prefix;       // namespace 前缀（可能为空）
    std::string local;        // 去掉前缀后的元素名
    std::vector<std::pair<std::string, std::string>> attrs;  // qname -> value
    std::vector<XmlNode> children;
    std::string text;

    std::string attr(const std::string& name) const {
        for (auto& a : attrs) if (a.first == name) return a.second;
        return "";
    }
};

class XmlParser {
public:
    explicit XmlParser(const std::string& s) : in_(s) {}

    XmlNode parse() {
        skipProlog();
        XmlNode root = parseElement();
        return root;
    }

private:
    const std::string& in_;
    size_t pos_ = 0;

    void skipProlog() {
        while (pos_ < in_.size() && std::isspace(static_cast<unsigned char>(in_[pos_]))) ++pos_;
        if (pos_ + 5 <= in_.size() && in_[pos_] == '<' && in_[pos_+1] == '?') {
            // <?xml ... ?>
            while (pos_ < in_.size() && !(in_[pos_] == '?' && pos_+1 < in_.size() && in_[pos_+1] == '>')) ++pos_;
            if (pos_+1 < in_.size()) pos_ += 2;
        }
        while (pos_ < in_.size() && std::isspace(static_cast<unsigned char>(in_[pos_]))) ++pos_;
    }

    void skipWhitespaceAndComments() {
        while (pos_ < in_.size()) {
            if (std::isspace(static_cast<unsigned char>(in_[pos_]))) { ++pos_; continue; }
            if (pos_ + 4 <= in_.size() && in_[pos_] == '<' && in_[pos_+1] == '!') {
                // comment <!-- ... -->
                if (pos_ + 4 <= in_.size() && in_[pos_+2] == '-' && in_[pos_+3] == '-') {
                    pos_ += 4;
                    while (pos_ + 2 < in_.size() &&
                           !(in_[pos_] == '-' && in_[pos_+1] == '-' && in_[pos_+2] == '>')) ++pos_;
                    if (pos_ + 2 < in_.size()) pos_ += 3;
                    continue;
                }
            }
            break;
        }
    }

    XmlNode parseElement() {
        skipWhitespaceAndComments();
        if (pos_ >= in_.size() || in_[pos_] != '<') {
            throw std::runtime_error("XmlParser: expected '<' at pos " + std::to_string(pos_));
        }
        ++pos_;  // skip '<'
        XmlNode node;
        std::string name = readName();
        splitQName(name, node.prefix, node.local);
        node.qname = name;
        // attributes
        while (pos_ < in_.size()) {
            skipInlineWs();
            if (pos_ < in_.size() && in_[pos_] == '>') { ++pos_; break; }
            if (pos_ + 1 < in_.size() && in_[pos_] == '/' && in_[pos_+1] == '>') {
                pos_ += 2;
                return node;  // self-closing
            }
            std::string an = readName();
            skipInlineWs();
            if (pos_ < in_.size() && in_[pos_] == '=') ++pos_;
            skipInlineWs();
            std::string v = readAttrValue();
            node.attrs.push_back({an, v});
        }
        // children / text
        std::string textBuf;
        while (pos_ < in_.size()) {
            if (in_[pos_] == '<') {
                if (pos_+1 < in_.size() && in_[pos_+1] == '/') {
                    // closing tag
                    pos_ += 2;
                    std::string en = readName();
                    (void)en;
                    while (pos_ < in_.size() && in_[pos_] != '>') ++pos_;
                    if (pos_ < in_.size()) ++pos_;
                    break;
                }
                if (pos_+1 < in_.size() && in_[pos_+1] == '!') {
                    // comment or CDATA: skip
                    if (pos_+4 <= in_.size() && in_[pos_+2] == '-' && in_[pos_+3] == '-') {
                        pos_ += 4;
                        while (pos_ + 2 < in_.size() &&
                               !(in_[pos_] == '-' && in_[pos_+1] == '-' && in_[pos_+2] == '>')) ++pos_;
                        if (pos_ + 2 < in_.size()) pos_ += 3;
                        continue;
                    }
                }
                XmlNode child = parseElement();
                node.children.push_back(std::move(child));
            } else {
                textBuf += in_[pos_++];
            }
        }
        node.text = textBuf;
        return node;
    }

    void skipInlineWs() {
        while (pos_ < in_.size() && std::isspace(static_cast<unsigned char>(in_[pos_]))) ++pos_;
    }

    std::string readName() {
        std::string out;
        while (pos_ < in_.size()) {
            char c = in_[pos_];
            if (std::isalnum(static_cast<unsigned char>(c)) || c == '_' || c == ':' || c == '-' || c == '.') {
                out += c; ++pos_;
            } else break;
        }
        return out;
    }

    std::string readAttrValue() {
        skipInlineWs();
        if (pos_ >= in_.size()) return "";
        char q = in_[pos_];
        if (q != '"' && q != '\'') return "";
        ++pos_;
        std::string out;
        while (pos_ < in_.size() && in_[pos_] != q) {
            if (in_[pos_] == '&') {
                // decode &amp; &lt; &gt; &quot; &apos;
                ++pos_;
                if (pos_ + 3 < in_.size() && in_[pos_] == 'a' && in_[pos_+1] == 'm' && in_[pos_+2] == 'p') {
                    out += '&'; pos_ += 4;
                } else if (pos_ < in_.size() && in_[pos_] == 'l' && in_[pos_+1] == 't') {
                    out += '<'; pos_ += 3;
                } else if (pos_ < in_.size() && in_[pos_] == 'g' && in_[pos_+1] == 't') {
                    out += '>'; pos_ += 3;
                } else if (pos_ < in_.size() && in_[pos_] == 'q' && in_[pos_+1] == 'u' && in_[pos_+2] == 'o' && in_[pos_+3] == 't') {
                    out += '"'; pos_ += 5;
                } else if (pos_ < in_.size() && in_[pos_] == 'a' && in_[pos_+1] == 'p' && in_[pos_+2] == 'o' && in_[pos_+3] == 's') {
                    out += '\''; pos_ += 5;
                } else {
                    out += '&';
                }
            } else {
                out += in_[pos_++];
            }
        }
        if (pos_ < in_.size()) ++pos_;  // skip closing quote
        return out;
    }

    static void splitQName(const std::string& q, std::string& prefix, std::string& local) {
        auto p = q.find(':');
        if (p == std::string::npos) { prefix = ""; local = q; }
        else { prefix = q.substr(0, p); local = q.substr(p+1); }
    }
};

// 解析 href="path#fragment"，返回 {path, fragment}，fragment 可空
struct HrefRef { std::string path; std::string fragment; bool empty() const { return path.empty() && fragment.empty(); } };
HrefRef splitHref(const std::string& href) {
    HrefRef r;
    auto hash = href.find('#');
    if (hash == std::string::npos) { r.path = href; r.fragment = ""; }
    else { r.path = href.substr(0, hash); r.fragment = href.substr(hash+1); }
    return r;
}

bool parseBool(const std::string& s, bool defaultVal = false) {
    if (s.empty()) return defaultVal;
    return s == "true" || s == "1" || s == "TRUE";
}

// 根据 .genmodel 文件所在的目录 + href，拼出 ecore 文件的绝对路径
std::string resolveEcorePath(const std::string& baseDir, const std::string& href) {
    HrefRef h = splitHref(href);
    if (h.path.empty()) return baseDir;
    if (h.path[0] == '/') return h.path;
    return baseDir + "/" + h.path;
}

// 根据 ecore 文件路径 + fragment 拿到 EPackage / EClass / EDataType
emf::ecore::EPackage* loadEcorePackage(const std::string& ecorePath) {
    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();
    auto uri = emf::common::URI::createFileURI(ecorePath);
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(uri);
    if (!res) {
        std::cerr << "[GenModelLoader] cannot create resource for " << ecorePath << std::endl;
        return nullptr;
    }
    std::ifstream ifs(ecorePath);
    if (!ifs.is_open()) {
        std::cerr << "[GenModelLoader] cannot open " << ecorePath << std::endl;
        return nullptr;
    }
    res->load(ifs);
    if (res->getContents().empty()) {
        std::cerr << "[GenModelLoader] no contents in " << ecorePath << std::endl;
        return nullptr;
    }
    auto* root = res->getContents()[0];
    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(root);
    return pkg;
}

// 在 EPackage 里找名字为 name 的 EClass（或 EDataType / EEnum）
emf::ecore::EClassifier* findClassifier(emf::ecore::EPackage* pkg, const std::string& name) {
    for (auto* c : pkg->getEClassifiers()) {
        if (c->getName() == name) return c;
    }
    return nullptr;
}

// 从 ecoreFeature 字符串（如 "ecore:EAttribute GenModel.ecore#//GenModel/copyrightText"）里
// 解析出 EPackage（暂时不需要）+ EStructuralFeature path
struct EcoreRef { std::string kind; std::string filePath; std::string containerPath; std::string featureName; };
EcoreRef parseEcoreRef(const std::string& s) {
    EcoreRef r;
    // 形式："<ecore:EReference|EAttribute|EClass|...> <file>#//<Container>/<Name>"
    auto sp = s.find(' ');
    if (sp == std::string::npos) { r.kind = ""; r.filePath = s; return r; }
    r.kind = s.substr(0, sp);
    std::string rest = s.substr(sp+1);
    HrefRef h = splitHref(rest);
    r.filePath = h.path;
    // fragment: "//GenModel/copyrightText"
    std::string frag = h.fragment;
    if (frag.size() >= 2 && frag[0] == '/' && frag[1] == '/') frag = frag.substr(2);
    auto slash = frag.find('/');
    if (slash == std::string::npos) { r.containerPath = ""; r.featureName = frag; }
    else { r.containerPath = frag.substr(0, slash); r.featureName = frag.substr(slash+1); }
    return r;
}

}  // namespace

// ===== public: load from file =====
std::shared_ptr<GenModel> GenModelLoader::load(const std::string& genModelPath) {
    std::ifstream f(genModelPath);
    if (!f.is_open()) {
        std::cerr << "[GenModelLoader] cannot open " << genModelPath << std::endl;
        return nullptr;
    }
    std::stringstream ss; ss << f.rdbuf();
    // baseDir: 取 .genmodel 文件所在目录
    std::string baseDir;
    auto slash = genModelPath.find_last_of('/');
    baseDir = (slash == std::string::npos) ? "." : genModelPath.substr(0, slash);
    return loadFromString(ss.str(), baseDir);
}

std::shared_ptr<GenModel> GenModelLoader::loadFromString(const std::string& xml,
                                                          const std::string& baseDir) {
    XmlParser parser(xml);
    XmlNode root = parser.parse();
    // 期望根节点为 <genmodel:GenModel>
    if (root.local != "GenModel" && root.qname != "GenModel") {
        std::cerr << "[GenModelLoader] expected <GenModel>, got <" << root.qname << ">" << std::endl;
        return nullptr;
    }
    auto gm = std::make_shared<GenModel>();
    gm->modelDirectory       = root.attr("modelDirectory");
    gm->modelPluginID        = root.attr("modelPluginID");
    gm->modelName            = root.attr("modelName");
    gm->editPluginID         = root.attr("editPluginID");
    gm->editorPluginID       = root.attr("editorPluginID");
    gm->complianceLevel      = root.attr("complianceLevel");
    gm->creationCommands     = parseBool(root.attr("creationCommands"), true);
    gm->creationIcons        = parseBool(root.attr("creationIcons"), true);
    gm->creationSubmenus     = parseBool(root.attr("creationSubmenus"), false);
    gm->runtimeJar           = parseBool(root.attr("runtimeJar"), false);
    gm->forceOverwrite       = parseBool(root.attr("forceOverwrite"), true);
    gm->codeFormatting       = parseBool(root.attr("codeFormatting"), false);
    gm->commentFormatting    = parseBool(root.attr("commentFormatting"), false);
    gm->bundleManifest       = parseBool(root.attr("bundleManifest"), true);
    gm->updateClasspath      = parseBool(root.attr("updateClasspath"), true);
    gm->suppressInterfaces   = parseBool(root.attr("suppressInterfaces"), false);
    gm->runtimeCompatibility = parseBool(root.attr("runtimeCompatibility"), true);
    gm->publicConstructors   = parseBool(root.attr("publicConstructors"), true);
    gm->copyrightFields      = parseBool(root.attr("copyrightFields"), true);
    gm->arrayAccessors       = parseBool(root.attr("arrayAccessors"), true);
    gm->containmentProxies   = parseBool(root.attr("containmentProxies"), true);
    gm->minimalReflectiveMethods = parseBool(root.attr("minimalReflectiveMethods"), true);

    // 遍历 <genPackages> 和 <nestedGenPackages>（gautosar.genmodel 用 genPackages 顶层 + nestedGenPackages 嵌套）
    // 对齐 Java: GenPackageImpl 列表从 GenModelImpl.eAllGenPackages 拉平
    std::function<void(GenModel*, const XmlNode&)> addGenPackage =
        [&](GenModel* model, const XmlNode& gpNode) {
        auto gp = std::make_shared<GenPackage>();
        gp->prefix = gpNode.attr("prefix");
        gp->basePackage = gpNode.attr("basePackage");
        gp->disposableProviderFactory = gpNode.attr("disposableProviderFactory");
        gp->fileExtensions = gpNode.attr("fileExtensions");
        gp->contentTypeIdentifier = gpNode.attr("contentTypeIdentifier");
        gp->genModel = model;

        // 找 ecorePackage（可能是 attribute 或子元素）
        emf::ecore::EPackage* ecorePkg = nullptr;
        // 1) attribute: ecorePackage="path#//fragment"
        std::string ecoreAttr = gpNode.attr("ecorePackage");
        if (!ecoreAttr.empty()) {
            std::string ecorePath = resolveEcorePath(baseDir, ecoreAttr);
            ecorePkg = loadEcorePackage(ecorePath);
        }
        // 2) 子元素: <ecorePackage href="...">
        if (!ecorePkg) {
            for (auto& sub : gpNode.children) {
                if (sub.local == "ecorePackage") {
                    std::string href = sub.attr("href");
                    HrefRef h = splitHref(href);
                    std::string ecorePath = resolveEcorePath(baseDir, href);
                    ecorePkg = loadEcorePackage(ecorePath);
                    break;
                }
            }
        }
        if (!ecorePkg) {
            std::cerr << "[GenModelLoader] genPackages has no ecorePackage" << std::endl;
            return;
        }
        // EPackage 所有权交给 shared_ptr
        gp->ecorePackage.reset(ecorePkg);

        // 遍历 <genClasses> / <genEnums> / <genDataTypes> / <nestedGenPackages>
        for (auto& sub : gpNode.children) {
            if (sub.local == "genClasses") {
                // ecoreClass="path#//Name"
                std::string ref = sub.attr("ecoreClass");
                HrefRef h = splitHref(ref);
                std::string frag = h.fragment;
                if (frag.size() >= 2 && frag[0] == '/' && frag[1] == '/') frag = frag.substr(2);
                auto cls = dynamic_cast<emf::ecore::EClass*>(findClassifier(ecorePkg, frag));
                if (!cls) continue;
                auto gc = std::make_shared<GenClass>();
                gc->ecoreClass = std::shared_ptr<emf::ecore::EClass>(cls, [](emf::ecore::EClass*){});
                gc->name = cls->getName();
                gc->type = cls->getName();
                gc->ecoreClassifier = std::shared_ptr<emf::ecore::EClassifier>(cls, [](emf::ecore::EClassifier*){});
                gc->genPackage = gp.get();
                gc->genModel = model;
                gc->instanceType = cls->getName() + "Impl";
                gc->instanceClassName = cls->getName();
                // genFeatures
                for (auto& gf : sub.children) {
                    if (gf.local != "genFeatures") continue;
                    EcoreRef eref = parseEcoreRef(gf.attr("ecoreFeature"));
                    // 通过 container 路径找 feature
                    emf::ecore::EStructuralFeature* sf = nullptr;
                    for (auto* c : ecorePkg->getEClassifiers()) {
                        if (auto* ec = dynamic_cast<emf::ecore::EClass*>(c)) {
                            if (ec->getName() == eref.containerPath) {
                                for (auto* f : ec->getEStructuralFeatures()) {
                                    if (f->getName() == eref.featureName) { sf = f; break; }
                                }
                            }
                            if (sf) break;
                        }
                    }
                    if (!sf) continue;
                    auto gfeat = std::make_shared<GenFeature>();
                    gfeat->ecoreFeature = std::shared_ptr<emf::ecore::EStructuralFeature>(sf, [](emf::ecore::EStructuralFeature*){});
                    gfeat->name = sf->getName();
                    gfeat->type = sf->getEType() ? sf->getEType()->getName() : "";
                    gfeat->genClass = gc.get();
                    gfeat->genPackage = gp.get();
                    gfeat->genModel = model;
                    gfeat->lowerBound = sf->getLowerBound();
                    gfeat->upperBound = sf->getUpperBound();
                    gfeat->changeable = sf->isChangeable();
                    gfeat->many = sf->getUpperBound() < 0 || sf->getUpperBound() > 1;
                    gfeat->defaultValueLiteral = sf->getDefaultValueLiteral();
                    gfeat->unsettable = sf->isUnsettable();
                    gfeat->ordered = sf->isOrdered();
                    gfeat->unique = sf->isUnique();
                    if (auto* a = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                        gfeat->attribute = true;
                        gfeat->reference = false;
                        gfeat->ecoreType = std::shared_ptr<emf::ecore::EClassifier>(a->getEAttributeType(), [](emf::ecore::EClassifier*){});
                    } else if (auto* r = dynamic_cast<emf::ecore::EReference*>(sf)) {
                        gfeat->attribute = false;
                        gfeat->reference = true;
                        gfeat->containment = r->isContainment();
                        gfeat->container = r->isContainer();
                        gfeat->resolveProxies = r->isResolveProxies();
                        gfeat->ecoreType = std::shared_ptr<emf::ecore::EClassifier>(r->getEReferenceType(), [](emf::ecore::EClassifier*){});
                    }
                    gc->genFeatures.push_back(gfeat);
                }
                gp->genClasses.push_back(gc);
            } else if (sub.local == "genEnums") {
                std::string ref = sub.attr("ecoreEnum");
                HrefRef h = splitHref(ref);
                std::string frag = h.fragment;
                if (frag.size() >= 2 && frag[0] == '/' && frag[1] == '/') frag = frag.substr(2);
                auto* en = dynamic_cast<emf::ecore::EEnum*>(findClassifier(ecorePkg, frag));
                if (!en) continue;
                auto ge = std::make_shared<GenEnum>();
                ge->ecoreClassifier = std::shared_ptr<emf::ecore::EClassifier>(en, [](emf::ecore::EClassifier*){});
                ge->name = en->getName();
                ge->type = en->getName();
                ge->genPackage = gp.get();
                ge->genModel = gm.get();
                ge->instanceType = en->getName();
                ge->instanceClassName = en->getName();
                for (auto* lit : en->getELiterals()) {
                    auto gl = std::make_shared<GenEnumLiteral>();
                    gl->name = lit->getName();
                    gl->literal = lit->getLiteral();
                    gl->value = lit->getValue();
                    gl->genPackage = gp.get();
                    gl->genModel = gm.get();
                    ge->genEnumLiterals.push_back(gl);
                }
                gp->genEnums.push_back(ge);
            } else if (sub.local == "genDataTypes") {
                std::string ref = sub.attr("ecoreDataType");
                HrefRef h = splitHref(ref);
                std::string frag = h.fragment;
                if (frag.size() >= 2 && frag[0] == '/' && frag[1] == '/') frag = frag.substr(2);
                auto* dt = dynamic_cast<emf::ecore::EDataType*>(findClassifier(ecorePkg, frag));
                if (!dt) continue;
                auto gd = std::make_shared<GenDataType>();
                gd->ecoreClassifier = std::shared_ptr<emf::ecore::EClassifier>(dt, [](emf::ecore::EClassifier*){});
                gd->name = dt->getName();
                gd->type = dt->getName();
                gd->genPackage = gp.get();
                gd->genModel = gm.get();
                gd->instanceType = dt->getName();
                gd->instanceClassName = dt->getName();
                gp->genDataTypes.push_back(gd);
            } else if (sub.local == "nestedGenPackages") {
                // 递归处理 nestedGenPackages
                addGenPackage(model, sub);
            }
        }
        model->genPackages.push_back(gp);
    };

    for (auto& child : root.children) {
        if (child.local == "genPackages" || child.local == "nestedGenPackages") {
            addGenPackage(gm.get(), child);
        }
    }
    return gm;
}

// ===== public: wrapEcore =====
// 没有 .genmodel 时使用：从一个已加载的 EPackage 直接包装成 GenModel
// （保留向后兼容 —— 旧 CppGenerator.cpp 走的就是这条路）
std::shared_ptr<GenModel> GenModelLoader::wrapEcore(emf::ecore::EPackage* ecorePkg,
                                                     const std::string& baseNamespace) {
    auto gm = std::make_shared<GenModel>();
    gm->modelDirectory = "";  // 由调用方填
    gm->modelName = ecorePkg ? ecorePkg->getName() : "";
    gm->modelPluginID = baseNamespace + "::" + gm->modelName;
    auto gp = std::make_shared<GenPackage>();
    gp->prefix = ecorePkg ? ecorePkg->getName() : "";
    // 首字母大写
    if (!gp->prefix.empty()) gp->prefix[0] = static_cast<char>(std::toupper(static_cast<unsigned char>(gp->prefix[0])));
    gp->basePackage = baseNamespace;
    gp->genModel = gm.get();
    if (ecorePkg) {
        // 注意：ecorePkg 由 EcoreFactory 拥有（不要 reset 接管所有权，会导致 double-free）
        // 用 aliasing 构造：shared_ptr 共享 factory 的所有权，但不删除
        // 更简单：直接传一个 non-owning weak_ptr 风格的自定义 deleter
        gp->ecorePackage = std::shared_ptr<emf::ecore::EPackage>(ecorePkg,
            [](emf::ecore::EPackage*) { /* no-op: 所有权归 EcoreFactory */ });
    }
    if (ecorePkg) {
        for (auto* c : ecorePkg->getEClassifiers()) {
            if (auto* ec = dynamic_cast<emf::ecore::EClass*>(c)) {
                auto gc = std::make_shared<GenClass>();
                gc->ecoreClass = std::shared_ptr<emf::ecore::EClass>(ec, [](emf::ecore::EClass*){});
                gc->name = ec->getName();
                gc->type = ec->getName();
                gc->genPackage = gp.get();
                gc->genModel = gm.get();
                gc->ecoreClassifier = std::shared_ptr<emf::ecore::EClassifier>(ec, [](emf::ecore::EClassifier*){});
                gc->instanceType = ec->getName() + "Impl";
                gc->instanceClassName = ec->getName();
                for (auto* sf : ec->getEStructuralFeatures()) {
                    auto gfeat = std::make_shared<GenFeature>();
                    gfeat->ecoreFeature = std::shared_ptr<emf::ecore::EStructuralFeature>(sf, [](emf::ecore::EStructuralFeature*){});
                    gfeat->name = sf->getName();
                    gfeat->type = sf->getEType() ? sf->getEType()->getName() : "";
                    gfeat->genClass = gc.get();
                    gfeat->genPackage = gp.get();
                    gfeat->genModel = gm.get();
                    gfeat->lowerBound = sf->getLowerBound();
                    gfeat->upperBound = sf->getUpperBound();
                    gfeat->changeable = sf->isChangeable();
                    gfeat->many = sf->getUpperBound() < 0 || sf->getUpperBound() > 1;
                    gfeat->defaultValueLiteral = sf->getDefaultValueLiteral();
                    gfeat->unsettable = sf->isUnsettable();
                    if (auto* a = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                        gfeat->attribute = true;
                        gfeat->reference = false;
                        gfeat->ecoreType = std::shared_ptr<emf::ecore::EClassifier>(a->getEAttributeType(), [](emf::ecore::EClassifier*){});
                    } else if (auto* r = dynamic_cast<emf::ecore::EReference*>(sf)) {
                        gfeat->attribute = false;
                        gfeat->reference = true;
                        gfeat->containment = r->isContainment();
                        gfeat->resolveProxies = r->isResolveProxies();
                        gfeat->ecoreType = std::shared_ptr<emf::ecore::EClassifier>(r->getEReferenceType(), [](emf::ecore::EClassifier*){});
                    }
                    gc->genFeatures.push_back(gfeat);
                }
                gp->genClasses.push_back(gc);
            } else if (auto* en = dynamic_cast<emf::ecore::EEnum*>(c)) {
                auto ge = std::make_shared<GenEnum>();
                ge->ecoreClassifier = std::shared_ptr<emf::ecore::EClassifier>(en, [](emf::ecore::EClassifier*){});
                ge->name = en->getName();
                ge->type = en->getName();
                ge->genPackage = gp.get();
                ge->genModel = gm.get();
                ge->instanceType = en->getName();
                ge->instanceClassName = en->getName();
                for (auto* lit : en->getELiterals()) {
                    auto gl = std::make_shared<GenEnumLiteral>();
                    gl->name = lit->getName();
                    gl->literal = lit->getLiteral();
                    gl->value = lit->getValue();
                    gl->genPackage = gp.get();
                    gl->genModel = gm.get();
                    ge->genEnumLiterals.push_back(gl);
                }
                gp->genEnums.push_back(ge);
            } else if (auto* dt = dynamic_cast<emf::ecore::EDataType*>(c)) {
                auto gd = std::make_shared<GenDataType>();
                gd->ecoreClassifier = std::shared_ptr<emf::ecore::EClassifier>(dt, [](emf::ecore::EClassifier*){});
                gd->name = dt->getName();
                gd->type = dt->getName();
                gd->genPackage = gp.get();
                gd->genModel = gm.get();
                gd->instanceType = dt->getName();
                gd->instanceClassName = dt->getName();
                gp->genDataTypes.push_back(gd);
            }
        }
    }
    gm->genPackages.push_back(gp);
    return gm;
}

}  // namespace emf::ecore::codegen
