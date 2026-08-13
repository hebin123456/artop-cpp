// emf::xmi —— XMIHandler 实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIHandler
#include "emf/xmi/XMIHandler.h"
#include "emf/xmi/XMIResource.h"

#include <cstdio>

namespace emf::xmi {

XMIHandler::XMIHandler(emf::common::Resource* res, XMLHelper* helper,
                       const std::unordered_map<std::string, std::any>& options)
    : XMLHandler(res, helper, options) {}

std::string XMIHandler::getXMIId() const { return lastXmiId_; }
std::string XMIHandler::getXMIType() const { return lastXmiType_; }
std::string XMIHandler::getXMIIdref() const { return lastXmiIdref_; }

void XMIHandler::recordXmiVersion(const std::string& nsURI) {
    // XMI 命名空间形式 "http://schema.omg.org/spec/XMI/2.0"
    const std::string prefix = "http://schema.omg.org/spec/XMI/";
    if (nsURI.size() > prefix.size() && nsURI.substr(0, prefix.size()) == prefix) {
        if (resource_) {
            if (auto* xmiRes = dynamic_cast<XMIResource*>(resource_)) {
                xmiRes->setXmiVersion(nsURI.substr(prefix.size()));
            }
        }
    }
}

void XMIHandler::handleStartElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs) {
    (void)qName;
    // 提取 xmi:id / xmi:type / xmi:idref
    lastXmiId_ = attrs.getValue("http://schema.omg.org/spec/XMI/2.0", "id");
    if (lastXmiId_.empty()) lastXmiId_ = attrs.getValue("xmi:id");

    lastXmiType_ = attrs.getValue("http://schema.omg.org/spec/XMI/2.0", "type");
    if (lastXmiType_.empty()) lastXmiType_ = attrs.getValue("xmi:type");

    lastXmiIdref_ = attrs.getValue("xmi:idref");

    // 记录 XMI 命名空间 / 版本
    if (isXmiElement(localName) && isXMINamespace(uri)) {
        recordXmiVersion(uri);
        types_.push_back("XMI_ELEMENT");
    } else {
        types_.push_back("OBJECT");
    }
    isRoot_ = false;
}

void XMIHandler::handleEndElement(const std::string& /*uri*/, const std::string& /*localName*/, const std::string& /*qName*/) {
    if (!types_.empty()) types_.pop_back();
    if (!features_.empty()) features_.pop_back();
    if (!objects_.empty()) objects_.pop_back();
}

void XMIHandler::handleCharacters(const std::string& /*text*/) {
    // XMIHandler 默认忽略文本节点
}

}  // namespace emf::xmi
