// ExtendedSAXXMIHandler.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedSAXXMIHandler
#pragma once

#include "emf/sphinx/resource/ExtendedSAXXMLHandler.h"

namespace emf::sphinx::resource {

class ExtendedSAXXMIHandler : public ExtendedSAXXMLHandler {
public:
    ExtendedSAXXMIHandler() = default;
    ~ExtendedSAXXMIHandler() override = default;
};

}  // namespace emf::sphinx::resource
