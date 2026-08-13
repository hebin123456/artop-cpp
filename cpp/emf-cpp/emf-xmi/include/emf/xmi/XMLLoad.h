// emf::xmi —— XMLLoad 接口（对齐 Java XMLLoad / XMLSave）
// XML 反序列化入口；当前 C++ 实现等价物是 XMILoader::loadInto。
#pragma once

#include <iosfwd>
#include <memory>

namespace emf::xmi {

class XMIResource;
struct XMIOptions;

// ===== XMLLoad 抽象（对齐 Java: org.eclipse.emf.ecore.xmi.XMLLoad）=====
class XMLLoad {
public:
    virtual ~XMLLoad() = default;
    // 把 input 流解析进 resource。options 控制各种 XMI 行为。
    virtual void load(XMIResource* resource, std::istream& input, const XMIOptions& options) = 0;
};

// ===== XMLSave 抽象（对齐 Java: org.eclipse.emf.ecore.xmi.XMLSave）=====
class XMLSave {
public:
    virtual ~XMLSave() = default;
    // 把 resource 序列化到 output 流。
    virtual void save(const XMIResource* resource, std::ostream& output, const XMIOptions& options) = 0;
};

// ===== XMLLoad 默认实现（对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl）=====
// 委托给 free function loadInto()（XMILoader.cpp 提供）。
class XMLLoadImpl : public XMLLoad {
public:
    void load(XMIResource* resource, std::istream& input, const XMIOptions& options) override;
};

// ===== XMLSave 默认实现（对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl）=====
class XMLSaveImpl : public XMLSave {
public:
    void save(const XMIResource* resource, std::ostream& output, const XMIOptions& options) override;
};

}  // namespace emf::xmi
