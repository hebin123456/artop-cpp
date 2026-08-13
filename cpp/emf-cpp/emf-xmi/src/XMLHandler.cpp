// emf::xmi —— XMLHandler 实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLHandler
#include "emf/xmi/XMLHandler.h"

namespace emf::xmi {

XMLHandler::XMLHandler(emf::common::Resource* res, XMLHelper* helper,
                       const std::unordered_map<std::string, std::any>& options)
    : resource_(res), helper_(helper), options_(options) {
    if (helper_) helper_->setResource(res);
}

void XMLHandler::setPrefixToUri(const std::string& prefix, const std::string& uri) {
    prefixToUri_[prefix] = uri;
    if (helper_) helper_->addPrefix(prefix, uri);
}

std::string XMLHandler::getURI(const std::string& prefix) const {
    auto it = prefixToUri_.find(prefix);
    if (it != prefixToUri_.end()) return it->second;
    if (helper_) return helper_->getURI(prefix);
    return std::string();
}

std::string XMLHandler::getPrefix(const std::string& nsURI) const {
    for (const auto& kv : prefixToUri_) {
        if (kv.second == nsURI) return kv.first;
    }
    if (helper_) return helper_->getPrefix(nsURI);
    return std::string();
}

void XMLHandler::startDocument() {
    isRoot_ = true;
    textBuffer_.clear();
    elementStack_.clear();
    objectStack_.clear();
    idToEObject_.clear();
    prefixToUri_.clear();
    errors_.clear();
    warnings_.clear();
    if (helper_) helper_->pushContext();
    handleStartDocument();
}

void XMLHandler::endDocument() {
    handleEndDocument();
    if (helper_) helper_->popContext();
    if (resource_) resource_->setLoaded(true);
}

void XMLHandler::startElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs) {
    if (helper_) helper_->pushContext();
    // 收集 xmlns 声明
    for (int i = 0; i < attrs.getLength(); ++i) {
        const std::string& aqn = attrs.getQName(i);
        if (aqn == "xmlns") {
            setPrefixToUri("", attrs.getValue(i));
        } else if (aqn.size() > 6 && aqn.substr(0, 6) == "xmlns:") {
            setPrefixToUri(aqn.substr(6), attrs.getValue(i));
        } else {
            std::string attrUri = attrs.getURI(i);
            if (!attrUri.empty()) {
                // 命名空间感知的属性
                std::string prefix;
                auto colon = aqn.find(':');
                if (colon != std::string::npos) prefix = aqn.substr(0, colon);
                if (!prefix.empty()) {
                    setPrefixToUri(prefix, attrUri);
                }
            }
        }
    }
    elementStack_.push_back(qName);
    handleStartElement(uri, localName, qName, attrs);
}

void XMLHandler::endElement(const std::string& uri, const std::string& localName, const std::string& qName) {
    handleEndElement(uri, localName, qName);
    if (!elementStack_.empty()) elementStack_.pop_back();
    if (helper_) helper_->popContext();
}

void XMLHandler::characters(const std::string& text) {
    textBuffer_ += text;
    handleCharacters(text);
}

void XMLHandler::reset() {
    isRoot_ = true;
    textBuffer_.clear();
    elementStack_.clear();
    objectStack_.clear();
    idToEObject_.clear();
    prefixToUri_.clear();
    errors_.clear();
    warnings_.clear();
}

std::string XMLHandler::getID(emf::common::EObject* obj) const {
    if (helper_) return helper_->getID(obj);
    return std::string();
}

std::string XMLHandler::getIDREF(emf::common::EObject* obj) const {
    if (helper_) return helper_->getIDREF(obj);
    return std::string();
}

std::string XMLHandler::getHREF(emf::common::EObject* obj) const {
    if (helper_) return helper_->getHREF(obj);
    return std::string();
}

emf::common::EObject* XMLHandler::getEObjectByID(const std::string& id) const {
    auto it = idToEObject_.find(id);
    if (it != idToEObject_.end()) return it->second;
    if (resource_) {
        // Resource 基类没有 getEObjectByID；委托给 XMIResource（dynamic_cast）
        auto* xmiRes = dynamic_cast<XMIResource*>(resource_);
        if (xmiRes) {
            if (auto* p = xmiRes->getEObjectByID(id)) return p;
        }
    }
    return nullptr;
}

}  // namespace emf::xmi
