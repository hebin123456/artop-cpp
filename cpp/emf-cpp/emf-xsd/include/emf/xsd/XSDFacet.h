// EMF XSD: XSDFacet 基接口
// 对齐 Java: org.eclipse.xsd.XSDFacet
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include <string>

namespace emf::xsd {

class XSDSimpleTypeDefinition;
class XSDAnnotation;

// Facet 的基接口（abstract）
// 对应 EMF 元模型中所有 XSD facet 类型的根
class XSDFacet : public XSDConcreteComponent {
public:
    XSDFacet() = default;
    ~XSDFacet() override = default;

    // LexicalValue: facet @value 属性的字符串表示
    virtual std::string getLexicalValue() const { return lexicalValue_; }
    virtual void setLexicalValue(const std::string& v) { lexicalValue_ = v; }

    // FacetName: facet 类型名（volatile，如 "length", "pattern"）
    virtual std::string getFacetName() const;

    // EffectiveValue: 类型化后的实际值（volatile；按 facet 不同返回 int/String/Object 等）
    virtual std::any getEffectiveValue() const;

    // Annotation: containment
    virtual XSDAnnotation* getAnnotation() const { return annotation_; }
    virtual void setAnnotation(XSDAnnotation* a);
    void setAnnotationPointer(XSDAnnotation* a) { annotation_ = a; }

    // SimpleTypeDefinition: 反向引用，volatile
    virtual XSDSimpleTypeDefinition* getSimpleTypeDefinition() const;

    emf::ecore::EClass* eClass() const override = 0;

protected:
    std::string lexicalValue_;
    XSDAnnotation* annotation_ = nullptr;
};

}  // namespace emf::xsd
