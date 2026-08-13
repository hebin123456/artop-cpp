// emf::xmi —— SAXXMIHandler
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler
//
// XMI 2.0 / 2.1 加载器：
//   - 继承 XMIHandler
//   - handleObjectAttribs 处理 xmi:id / xmi:type / xmi:idref
//   - xsi:type 优先，xmi:type 兜底
#pragma once

#include "emf/xmi/XMIHandler.h"

namespace emf::xmi {

class SAXXMIHandler : public XMIHandler {
public:
    SAXXMIHandler(emf::common::Resource* res, XMLHelper* helper,
                  const std::unordered_map<std::string, std::any>& options);
    ~SAXXMIHandler() override = default;

    // xsi:type + xmi:type 联合获取
    std::string getXSIType() const;

    // 属性处理
    void handleObjectAttribs(emf::common::EObject* obj, const Attributes& attrs);

    // 事件
    void handleStartElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs) override;
    void handleEndElement(const std::string& uri, const std::string& localName, const std::string& qName) override;

    // 把 xmi:id 注册到 id 映射
    void registerObjectId(emf::common::EObject* obj, const std::string& id);
};

}  // namespace emf::xmi
