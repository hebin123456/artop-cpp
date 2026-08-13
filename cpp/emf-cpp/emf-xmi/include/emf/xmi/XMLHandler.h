// emf::xmi —— XMLHandler 抽象基类
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLHandler
//
// XML 事件处理的通用接口：
//   - startElement / endElement / characters 事件分发
//   - 内部 id -> EObject 映射（getIDToEObjectMap / setID / getEObjectByID）
//   - 通过 helper.getURI / helper.getID / helper.getIDREF 代理到 XMLHelper
//   - 错误/警告回调
#pragma once

#include "emf/xmi/XMLHelper.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"

#include <string>
#include <vector>
#include <unordered_map>
#include <any>
#include <functional>

namespace emf::ecore { class EStructuralFeature; class EObject; class EClass; class EFactory; class EPackage; }
namespace emf::xmi {

// Attributes：SAX-style attributes 抽象（提供 getURI/getValue/getQName）
class Attributes {
public:
    virtual ~Attributes() = default;
    virtual int getLength() const = 0;
    virtual std::string getQName(int i) const = 0;
    virtual std::string getURI(int i) const = 0;
    virtual std::string getLocalName(int i) const = 0;
    virtual std::string getValue(int i) const = 0;
    virtual std::string getValue(const std::string& qname) const = 0;
    virtual std::string getValue(const std::string& uri, const std::string& localName) const = 0;
};

// 简单 Attributes 实现（用于把 SAX-like 列表包装起来）
class SimpleAttributes : public Attributes {
public:
    struct Item {
        std::string qname;
        std::string uri;
        std::string local;
        std::string value;
    };
    void add(const std::string& qname, const std::string& uri, const std::string& local, const std::string& value) {
        items_.push_back({qname, uri, local, value});
    }
    int getLength() const override { return static_cast<int>(items_.size()); }
    std::string getQName(int i) const override { return items_.at(i).qname; }
    std::string getURI(int i) const override { return items_.at(i).uri; }
    std::string getLocalName(int i) const override { return items_.at(i).local; }
    std::string getValue(int i) const override { return items_.at(i).value; }
    std::string getValue(const std::string& qname) const override {
        for (const auto& it : items_) if (it.qname == qname) return it.value;
        return std::string();
    }
    std::string getValue(const std::string& uri, const std::string& localName) const override {
        for (const auto& it : items_) if (it.uri == uri && it.local == localName) return it.value;
        return std::string();
    }
    const std::vector<Item>& items() const { return items_; }
private:
    std::vector<Item> items_;
};

// XMLHandler：抽象基类（对齐 Java XMLHandler）
// 子类实现 processStartElement / processEndElement / processCharacters 等钩子。
class XMLHandler {
public:
    XMLHandler(emf::common::Resource* res, XMLHelper* helper, const std::unordered_map<std::string, std::any>& options);
    virtual ~XMLHandler() = default;

    // 资源访问
    emf::common::Resource* getResource() const { return resource_; }
    XMLHelper*  getHelper()  const { return helper_; }

    // id -> EObject 映射（Java XMLHandler.idToEObjectMap）
    const std::unordered_map<std::string, emf::common::EObject*>& getIDToEObjectMap() const { return idToEObject_; }
    std::unordered_map<std::string, emf::common::EObject*>& getIDToEObjectMap() { return idToEObject_; }

    // 命名空间前缀 -> URI
    void setPrefixToUri(const std::string& prefix, const std::string& uri);
    std::string getURI(const std::string& prefix) const;
    std::string getPrefix(const std::string& nsURI) const;
    const std::unordered_map<std::string, std::string>& getPrefixToUriMap() const { return prefixToUri_; }

    // 由 SAX wrapper 调用的入口
    void startDocument();
    void endDocument();
    void startElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs);
    void endElement(const std::string& uri, const std::string& localName, const std::string& qName);
    void characters(const std::string& text);
    void ignorableWhitespace(const std::string&) {}
    void skippedEntity(const std::string&) {}

    // 子类钩子
    virtual void handleStartElement(const std::string& uri, const std::string& localName, const std::string& qName, const Attributes& attrs) = 0;
    virtual void handleEndElement(const std::string& uri, const std::string& localName, const std::string& qName) = 0;
    virtual void handleCharacters(const std::string& text) = 0;
    virtual void handleStartDocument() {}
    virtual void handleEndDocument() {}

    // 错误/警告收集
    const std::vector<std::string>& getErrors() const { return errors_; }
    const std::vector<std::string>& getWarnings() const { return warnings_; }
    void error(const std::string& msg) { errors_.push_back(msg); }
    void warning(const std::string& msg) { warnings_.push_back(msg); }

    // ID/IDREF/HREF 通过 helper 委托
    std::string getID(emf::common::EObject* obj) const;
    std::string getIDREF(emf::common::EObject* obj) const;
    std::string getHREF(emf::common::EObject* obj) const;
    emf::common::EObject* getEObjectByID(const std::string& id) const;

    // reset 用于复用
    virtual void reset();

protected:
    emf::common::Resource* resource_;
    XMLHelper* helper_;
    std::unordered_map<std::string, std::any> options_;
    std::unordered_map<std::string, emf::common::EObject*> idToEObject_;
    std::unordered_map<std::string, std::string> prefixToUri_;
    std::vector<std::string> errors_;
    std::vector<std::string> warnings_;

    // 状态
    bool isRoot_ = true;
    std::string textBuffer_;
    std::vector<std::string> elementStack_;
    std::vector<emf::common::EObject*> objectStack_;
};

}  // namespace emf::xmi
