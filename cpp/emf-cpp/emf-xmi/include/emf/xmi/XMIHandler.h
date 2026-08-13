// emf::xmi —— XMIHandler
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIHandler
//
// 抽象基类，扩展 XMLHandler：
//   - 处理 xmi:XMI 根元素
//   - 处理 xmi:id / xmi:type / xmi:idref
//   - 拒绝 xmi:Extension 等特定 XMI 元素
#pragma once

#include "emf/xmi/XMLHandler.h"

namespace emf::xmi {

class XMIHandler : public XMLHandler {
public:
    XMIHandler(emf::common::Resource* res, XMLHelper* helper,
               const std::unordered_map<std::string, std::any>& options);
    ~XMIHandler() override = default;

    // xmi:id / xmi:type / xmi:idref
    std::string getXMIId() const;
    std::string getXMIType() const;
    std::string getXMIIdref() const;

    // 启动 XMI 版本识别
    void recordXmiVersion(const std::string& nsURI);

    bool isXMINamespace(const std::string& uri) const {
        return uri == "http://schema.omg.org/spec/XMI/2.0" ||
               uri == "http://www.omg.org/spec/XMI/2.1" ||
               uri == "http://www.omg.org/XMI";
    }

    // 钩子
    void handleStartElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs) override;
    void handleEndElement(const std::string& uri, const std::string& localName, const std::string& qName) override;
    void handleCharacters(const std::string& text) override;

    // XMI 元素名常量
    static constexpr const char* XMI_ELEMENT_NAME = "XMI";

protected:
    std::vector<std::string> types_;  // OBJECT, XMI_ELEMENT, ...
    std::vector<emf::common::EObject*> objects_;
    std::vector<::emf::ecore::EStructuralFeature*> features_;
    std::string lastXmiType_;
    std::string lastXmiId_;
    std::string lastXmiIdref_;

    bool isXmiElement(const std::string& localName) const {
        return localName == XMI_ELEMENT_NAME;
    }
};

}  // namespace emf::xmi
