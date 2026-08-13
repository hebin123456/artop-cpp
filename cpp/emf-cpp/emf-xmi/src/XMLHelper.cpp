// emf::xmi —— XMLHelper 实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl
#include "emf/xmi/XMLHelper.h"
#include "emf/xmi/XMIResource.h"

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreMetadata.h"
#include "emf/common/EPackage.h"
#include "emf/common/EList.h"

#include <algorithm>
#include <cctype>
#include <sstream>

namespace emf::xmi {

// ===== XMLHelperImpl =====

XMLHelperImpl::XMLHelperImpl() = default;

XMLHelperImpl::XMLHelperImpl(emf::common::Resource* res) {
    setResource(res);
}

void XMLHelperImpl::setOptions(const std::unordered_map<std::string, std::any>& options) {
    // 占位：Java 端会读 OPTION_LAX_FEATURE_PROCESSING / OPTION_URI_HANDLER 等
    (void)options;
}

void XMLHelperImpl::setNoNamespacePackage(::emf::ecore::EPackage* pkg) {
    noNamespacePackage_ = pkg;
}

::emf::ecore::EPackage* XMLHelperImpl::getNoNamespacePackage() const {
    return noNamespacePackage_;
}

void XMLHelperImpl::setResource(emf::common::Resource* res) {
    resource_ = res;
    if (!res) {
        resourceURI_ = emf::common::URI();
        deresolve_ = false;
    } else {
        resourceURI_ = res->getURI();
        deresolve_ = !resourceURI_.isEmpty() && !resourceURI_.isRelative() && resourceURI_.isHierarchical();
    }
}

std::string XMLHelperImpl::getXMLEncoding(const std::string& javaEncoding) const {
    return javaEncoding;
}

std::string XMLHelperImpl::getJavaEncoding(const std::string& xmlEncoding) const {
    return xmlEncoding;
}

// ===== 命名空间上下文（对齐 Java NamespaceSupport） =====

void XMLHelperImpl::pushContext() {
    nsCtx_.contextMarks.push_back(static_cast<int>(nsCtx_.table.size()));
    nsCtx_.currentContext = static_cast<int>(nsCtx_.contextMarks.size()) - 1;
}

void XMLHelperImpl::popContext() {
    popContextImpl(false);
}

void XMLHelperImpl::popContextImpl(bool /*removeFromFactories*/) {
    if (nsCtx_.contextMarks.empty()) return;
    int mark = nsCtx_.contextMarks.back();
    nsCtx_.contextMarks.pop_back();
    // 删除当前 context 之后加入的条目
    if (static_cast<size_t>(mark) < nsCtx_.table.size()) {
        nsCtx_.table.resize(mark);
    }
    nsCtx_.currentContext = static_cast<int>(nsCtx_.contextMarks.size()) - 1;
}

void XMLHelperImpl::addPrefix(const std::string& prefix, const std::string& uri) {
    if (prefix == "xml" || prefix == "xmlns") return;
    std::string u = uri.empty() ? std::string() : uri;
    declarePrefix(prefix, u);
    prefixesToURIs_[prefix] = u;
    if (!u.empty()) urisToPrefixes_[u] = prefix;
}

void XMLHelperImpl::declarePrefix(const std::string& prefix, const std::string& uri) {
    // 在当前 context 范围内覆盖或新增
    if (nsCtx_.contextMarks.empty()) {
        // 没有 push，直接放入 table
        nsCtx_.table.push_back(prefix);
        nsCtx_.table.push_back(uri);
        return;
    }
    int ctxStart = nsCtx_.contextMarks.back();
    // 在当前 context 内查找是否已经声明过该 prefix
    for (size_t i = static_cast<size_t>(ctxStart); i + 1 < nsCtx_.table.size(); i += 2) {
        if (nsCtx_.table[i] == prefix) {
            nsCtx_.table[i + 1] = uri;
            return;
        }
    }
    nsCtx_.table.push_back(prefix);
    nsCtx_.table.push_back(uri);
}

std::string XMLHelperImpl::lookupURI(const std::string& prefix) const {
    // table = [prefix, uri, prefix, uri, ...] in forward order
    // 从尾部向前迭代时，pair 顺序变为 (uri, prefix)
    for (auto it = nsCtx_.table.rbegin(); it != nsCtx_.table.rend(); ) {
        if (it == nsCtx_.table.rend()) break;
        const std::string& u = *it++;  // u 在前（原 uri）
        if (it == nsCtx_.table.rend()) break;
        const std::string& p = *it++;  // p 在后（原 prefix）
        if (p == prefix) return u;
    }
    return std::string();
}

std::string XMLHelperImpl::lookupPrefix(const std::string& uri) const {
    // 同 lookupURI：反向迭代时 pair 顺序翻转
    for (auto it = nsCtx_.table.rbegin(); it != nsCtx_.table.rend(); ) {
        if (it == nsCtx_.table.rend()) break;
        const std::string& u = *it++;
        if (it == nsCtx_.table.rend()) break;
        const std::string& p = *it++;
        if (u == uri) return p;
    }
    return std::string();
}

std::string XMLHelperImpl::getURI(const std::string& prefix) const {
    if (prefix == "xml") return "http://www.w3.org/XML/1998/namespace";
    if (prefix == "xmlns") return "http://www.w3.org/2000/xmlns/";
    std::string u = lookupURI(prefix);
    return u;
}

std::string XMLHelperImpl::getPrefix(const std::string& namespaceURI) const {
    if (namespaceURI == "http://www.w3.org/XML/1998/namespace") return "xml";
    if (namespaceURI == "http://www.w3.org/2000/xmlns/") return "xmlns";
    std::string p = lookupPrefix(namespaceURI);
    return p;
}

std::string XMLHelperImpl::getNamespaceURI(const std::string& prefix) const {
    return getURI(prefix);
}

void XMLHelperImpl::recordPrefixToURIMapping() {
    // 把当前 prefixesToURIs_ 同步为当前的表
    prefixesToURIs_.clear();
    for (size_t i = 0; i + 1 < nsCtx_.table.size(); i += 2) {
        const std::string& p = nsCtx_.table[i];
        const std::string& u = nsCtx_.table[i + 1];
        prefixesToURIs_[p] = u;
        if (!u.empty()) urisToPrefixes_[u] = p;
    }
}

void XMLHelperImpl::setPrefixToNamespaceMap(const std::unordered_map<std::string, std::string>& m) {
    prefixesToURIs_ = m;
    urisToPrefixes_.clear();
    for (const auto& kv : m) {
        if (!kv.second.empty()) urisToPrefixes_[kv.second] = kv.first;
    }
}

// ===== ID / HREF =====

std::string XMLHelperImpl::getID(::emf::common::EObject* obj) const {
    if (!obj || !resource_) return std::string();
    auto* xmiRes = dynamic_cast<XMIResource*>(resource_);
    return xmiRes ? xmiRes->getID(obj) : std::string();
}

std::string XMLHelperImpl::getIDREF(::emf::common::EObject* obj) const {
    if (!obj || !resource_) return std::string();
    auto* xmiRes = dynamic_cast<XMIResource*>(resource_);
    return xmiRes ? xmiRes->getID(obj) : std::string();  // 退化为 getID（基础实现）
}

std::string XMLHelperImpl::getHREF(::emf::common::EObject* obj) const {
    if (!obj) return std::string();
    if (!resource_) return std::string();
    auto* xmiRes = dynamic_cast<XMIResource*>(resource_);
    if (!xmiRes) return std::string();
    // 对齐 Java: XMIResource.getURIFragment(EObject) —— 在基础实现中退化为 getID
    std::string frag = xmiRes->getID(obj);
    if (frag.empty()) return std::string();
    emf::common::URI u = resourceURI_;
    if (u.isEmpty()) return std::string();
    return u.appendFragment(frag).toString();
}

// ===== Feature 查询 / 类型 =====

::emf::ecore::EStructuralFeature* XMLHelperImpl::getFeature(::emf::ecore::EClass* eClass,
                                                            const std::string& /*namespaceURI*/,
                                                            const std::string& name) {
    if (!eClass) return nullptr;
    return eClass->getEStructuralFeature(name);
}

int XMLHelperImpl::computeFeatureKind(::emf::ecore::EStructuralFeature* feature) {
    if (!feature) return OTHER;
    auto* eType = feature->getEType();
    auto* ec = dynamic_cast<::emf::ecore::EDataType*>(eType);
    bool many = (feature->getUpperBound() != 1);
    if (ec) {
        return many ? DATATYPE_IS_MANY : DATATYPE_SINGLE;
    }
    // EReference
    if (many) {
        auto* ref = dynamic_cast<::emf::ecore::EReference*>(feature);
        if (ref) {
            auto* opp = ref->getEOpposite();
            if (opp == nullptr || opp->isTransient() || (opp->getUpperBound() != 1)) {
                return IS_MANY_ADD;
            }
            return IS_MANY_MOVE;
        }
    }
    return OTHER;
}

int XMLHelperImpl::getFeatureKind(::emf::ecore::EStructuralFeature* feature) {
    auto it = featuresToKinds_.find(feature);
    if (it != featuresToKinds_.end()) return it->second;
    int kind = computeFeatureKind(feature);
    featuresToKinds_[feature] = kind;
    return kind;
}

// ===== 值操作 =====

std::any XMLHelperImpl::getValue(::emf::common::EObject* obj,
                                  ::emf::ecore::EStructuralFeature* feature) {
    if (!obj || !feature) return std::any{};
    return obj->eGet(feature);
}

void XMLHelperImpl::setValue(::emf::common::EObject* obj,
                             ::emf::ecore::EStructuralFeature* feature,
                             const std::any& value, int position) {
    if (!obj || !feature) return;
    int kind = getFeatureKind(feature);
    switch (kind) {
        case DATATYPE_SINGLE: {
            auto* dt = dynamic_cast<::emf::ecore::EDataType*>(feature->getEType());
            if (dt) {
                std::any coerced = ::emf::ecore::DataTypeUtil::coerce(value, dt->getName());
                obj->eSet(feature, coerced);
            } else {
                obj->eSet(feature, value);
            }
            break;
        }
        case DATATYPE_IS_MANY: {
            auto* dt = dynamic_cast<::emf::ecore::EDataType*>(feature->getEType());
            std::any v = obj->eGet(feature);
            if (v.type() == typeid(::emf::common::EList<emf::common::EObject*>*)) {
                auto* list = std::any_cast<::emf::common::EList<emf::common::EObject*>*>(&v);
                if (list && *list) {
                    emf::common::EObject* eobj = std::any_cast<emf::common::EObject*>(value);
                    (*list)->add(eobj);
                }
            } else if (v.type() == typeid(std::vector<emf::common::EObject*>*)) {
                auto* vec = std::any_cast<std::vector<emf::common::EObject*>*>(&v);
                if (vec && *vec) {
                    emf::common::EObject* eobj = std::any_cast<emf::common::EObject*>(value);
                    (*vec)->push_back(eobj);
                }
            } else if (v.type() == typeid(std::vector<std::string>)) {
                std::vector<std::string> lst = std::any_cast<std::vector<std::string>>(v);
                std::string s = std::any_cast<std::string>(value);
                lst.push_back(s);
                obj->eSet(feature, lst);
            } else {
                obj->eSet(feature, value);
            }
            (void)dt;
            break;
        }
        case IS_MANY_ADD:
        case IS_MANY_MOVE: {
            std::any v = obj->eGet(feature);
            emf::common::EObject* eobj = std::any_cast<emf::common::EObject*>(value);
            if (v.type() == typeid(emf::common::EObjectRefView)) {
                // EObjectRefView 零拷贝视图（codegen 多值 reference eGet fast-path）
                auto view = std::any_cast<emf::common::EObjectRefView>(v);
                std::vector<emf::common::EObject*> lst;
                lst.reserve(view.size());
                for (auto* p : view) lst.push_back(p);
                lst.push_back(eobj);
                obj->eSet(feature, lst);
            } else if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
                auto lst = std::any_cast<std::vector<emf::common::EObject*>>(v);
                lst.push_back(eobj);
                obj->eSet(feature, lst);
            } else if (v.type() == typeid(::emf::common::EList<emf::common::EObject*>*)) {
                auto* lst = std::any_cast<::emf::common::EList<emf::common::EObject*>*>(v);
                if (lst) lst->add(eobj);
            }
            break;
        }
        default: {
            obj->eSet(feature, value);
            break;
        }
    }
    (void)position;
}

std::string XMLHelperImpl::convertToString(::emf::ecore::EFactory* factory,
                                          ::emf::ecore::EDataType* dataType,
                                          const std::any& value) {
    if (!factory || !dataType) return std::string();
    return factory->convertToString(dataType, value);
}

// ===== URI 解析 =====

emf::common::URI XMLHelperImpl::deresolve(const emf::common::URI& uri) {
    if (deresolve_ && !uri.isRelative()) {
        emf::common::URI d = uri.deresolve(resourceURI_);
        if (d.hasRelativePath()) return d;
    }
    return uri;
}

emf::common::URI XMLHelperImpl::resolve(const emf::common::URI& relative,
                                        const emf::common::URI& base) {
    return relative.resolve(base);
}

// ===== 工厂方法 =====

::emf::common::EObject* XMLHelperImpl::createObject(::emf::ecore::EFactory* factory,
                                                   ::emf::ecore::EClassifier* type) {
    if (!factory || !type) return nullptr;
    auto* cls = dynamic_cast<::emf::ecore::EClass*>(type);
    if (!cls) return nullptr;
    if (cls->isAbstract()) return nullptr;
    return factory->create(cls);
}

::emf::ecore::EClassifier* XMLHelperImpl::getType(::emf::ecore::EFactory* factory,
                                                  const std::string& typeName) {
    if (!factory) return nullptr;
    auto* pkg = factory->getEPackage();
    if (!pkg) return nullptr;
    return pkg->getEClassifier(typeName);
}

}  // namespace emf::xmi
