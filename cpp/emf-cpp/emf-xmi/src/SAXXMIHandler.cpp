// emf::xmi —— SAXXMIHandler 实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler
#include "emf/xmi/SAXXMIHandler.h"
#include "emf/xmi/XMIResource.h"

namespace emf::xmi {

SAXXMIHandler::SAXXMIHandler(emf::common::Resource* res, XMLHelper* helper,
                             const std::unordered_map<std::string, std::any>& options)
    : XMIHandler(res, helper, options) {}

std::string SAXXMIHandler::getXSIType() const {
    // xsi:type
    auto it = std::find_if(elementStack_.rbegin(), elementStack_.rend(),
                            [](const std::string&) { return true; });
    (void)it;
    // 我们依靠 XMIHandler 缓存的 lastXmiType_
    // 优先 xsi:type
    if (helper_) {
        std::string xsi = helper_->getURI("xsi");
        if (xsi.empty()) xsi = "http://www.w3.org/2001/XMLSchema-instance";
    }
    // 简化：直接用 XMIHandler 缓存值
    return lastXmiType_.empty() ? std::string() : lastXmiType_;
}

void SAXXMIHandler::handleObjectAttribs(emf::common::EObject* obj, const Attributes& attrs) {
    if (!obj) return;
    // xmi:id
    std::string id = attrs.getValue("xmi:id");
    if (id.empty()) id = attrs.getValue("http://schema.omg.org/spec/XMI/2.0", "id");
    if (!id.empty()) {
        registerObjectId(obj, id);
        // Resource 基类没有 setID；委托给 XMIResource（dynamic_cast）
        if (resource_) {
            auto* xmiRes = dynamic_cast<XMIResource*>(resource_);
            if (xmiRes) xmiRes->setID(obj, id);
        }
    }
    // 其它属性暂时由 XMIResource 加载管线处理
    (void)obj;
    (void)attrs;
}

void SAXXMIHandler::registerObjectId(emf::common::EObject* obj, const std::string& id) {
    if (!obj || id.empty()) return;
    getIDToEObjectMap()[id] = obj;
}

void SAXXMIHandler::handleStartElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs) {
    XMIHandler::handleStartElement(uri, localName, qName, attrs);
}

void SAXXMIHandler::handleEndElement(const std::string& uri, const std::string& localName, const std::string& qName) {
    XMIHandler::handleEndElement(uri, localName, qName);
}

}  // namespace emf::xmi
