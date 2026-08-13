// emf::xmi —— SAXXMLHandler 实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.SAXXMLHandler
#include "emf/xmi/SAXXMLHandler.h"
#include "emf/xmi/XMLHelper.h"

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xmi {

SAXXMLHandler::SAXXMLHandler(emf::common::Resource* res, XMLHelper* helper,
                             const std::unordered_map<std::string, std::any>& options)
    : XMLHandler(res, helper, options) {
    // 默认从 option 读 namespace-aware 标志
    auto it = options.find("NAMESPACE_AWARE");
    if (it != options.end()) {
        if (it->second.type() == typeid(bool)) isNamespaceAware_ = std::any_cast<bool>(it->second);
        else if (it->second.type() == typeid(int)) isNamespaceAware_ = (std::any_cast<int>(it->second) != 0);
    }
}

std::string SAXXMLHandler::getXSIType() const {
    if (!currentAttributes_) return std::string();
    if (isNamespaceAware_) {
        return currentAttributes_->getValue("http://www.w3.org/2001/XMLSchema-instance", "type");
    }
    return currentAttributes_->getValue("xsi:type");
}

void SAXXMLHandler::handleStartElement(const std::string& /*uri*/, const std::string& localName, const std::string& qName, const Attributes& attrs) {
    currentAttributes_ = &attrs;
    lastXSIType_ = getXSIType();
    // 简易实现：仅记录元素名称和对象栈
    (void)qName;
    (void)localName;
    isRoot_ = false;
}

void SAXXMLHandler::handleEndElement(const std::string& /*uri*/, const std::string& /*localName*/, const std::string& /*qName*/) {
    if (!types_.empty()) types_.pop_back();
    if (!features_.empty() && (types_.empty() || types_.back() != "OBJECT")) features_.pop_back();
    currentAttributes_ = nullptr;
}

void SAXXMLHandler::handleCharacters(const std::string& text) {
    // 默认实现：忽略（SAXXMLHandler 主要在 XMI 场景下使用）
    (void)text;
}

void SAXXMLHandler::handleObjectAttribs(emf::common::EObject* /*obj*/, const Attributes& /*attrs*/) {
    // 默认空实现，子类可覆写处理 id/href/xmi:id 等
}

void SAXXMLHandler::setTextValue(emf::common::EObject* /*obj*/, ::emf::ecore::EStructuralFeature* /*feature*/, const std::string& /*text*/) {
    // 默认空实现
}

emf::common::EObject* SAXXMLHandler::peekObject() const {
    if (objects_.empty()) return nullptr;
    return objects_.back();
}

::emf::ecore::EStructuralFeature* SAXXMLHandler::peekFeature() const {
    if (features_.empty()) return nullptr;
    return features_.back();
}

void SAXXMLHandler::pushObject(emf::common::EObject* obj) {
    objects_.push_back(obj);
    types_.push_back("OBJECT");
}

void SAXXMLHandler::popObject() {
    if (!objects_.empty()) objects_.pop_back();
    if (!types_.empty()) types_.pop_back();
}

}  // namespace emf::xmi
