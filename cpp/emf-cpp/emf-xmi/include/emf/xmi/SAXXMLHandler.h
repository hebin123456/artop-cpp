// emf::xmi —— SAXXMLHandler
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.SAXXMLHandler
//
// 处理 XML（非 XMI）SAX 事件：
//   - 解析 xsi:type 决定 EObject 实际 EClass
//   - 处理对象属性（id、href、其他属性）
//   - 维护 element/object/text 栈
#pragma once

#include "emf/xmi/XMLHandler.h"

namespace emf::xmi {

class SAXXMLHandler : public XMLHandler {
public:
    SAXXMLHandler(emf::common::Resource* res, XMLHelper* helper,
                  const std::unordered_map<std::string, std::any>& options);
    ~SAXXMLHandler() override = default;

    // Java SAXXMLHandler.getXSIType
    std::string getXSIType() const;
    bool isNamespaceAware() const { return isNamespaceAware_; }
    void setNamespaceAware(bool b) { isNamespaceAware_ = b; }

    // 钩子实现
    void handleStartElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs) override;
    void handleEndElement(const std::string& uri, const std::string& localName, const std::string& qName) override;
    void handleCharacters(const std::string& text) override;

    // id 处理（子类覆写以处理 xmi:id 等）
    virtual void handleObjectAttribs(emf::common::EObject* obj, const Attributes& attrs);
    // 文本处理：默认把字符缓冲写到当前对象的最后一个 datatype feature
    virtual void setTextValue(emf::common::EObject* obj, ::emf::ecore::EStructuralFeature* feature, const std::string& text);

    // 栈管理
    emf::common::EObject* peekObject() const;
    emf::ecore::EStructuralFeature* peekFeature() const;
    void pushObject(emf::common::EObject* obj);
    void popObject();

    // 当前属性
    const Attributes* currentAttributes() const { return currentAttributes_; }
    void setCurrentAttributes(const Attributes* a) { currentAttributes_ = a; }

protected:
    // 解析 xsi:type 时所需的 state
    std::vector<emf::common::EObject*> objects_;
    std::vector<::emf::ecore::EStructuralFeature*> features_;
    std::vector<std::string> types_;
    const Attributes* currentAttributes_ = nullptr;
    bool isNamespaceAware_ = true;
    std::string lastXSIType_;
};

}  // namespace emf::xmi
