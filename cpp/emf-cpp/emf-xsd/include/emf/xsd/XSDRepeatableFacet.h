// EMF XSD: XSDRepeatableFacet
// 对齐 Java: org.eclipse.xsd.XSDRepeatableFacet
#pragma once

#include "emf/xsd/XSDConstrainingFacet.h"
#include "emf/common/EList.h"
#include <vector>

namespace emf::xsd {

class XSDAnnotation;

// Repeatable Facet（abstract）: pattern 和 enumeration 可重复
class XSDRepeatableFacet : public XSDConstrainingFacet {
public:
    XSDRepeatableFacet() = default;
    ~XSDRepeatableFacet() override = default;

    virtual emf::common::EList<XSDAnnotation*>& getAnnotations() { return annotations_; }
    virtual const emf::common::EList<XSDAnnotation*>& getAnnotations() const { return annotations_; }

protected:
    emf::common::EList<XSDAnnotation*> annotations_;
};

}  // namespace emf::xsd
