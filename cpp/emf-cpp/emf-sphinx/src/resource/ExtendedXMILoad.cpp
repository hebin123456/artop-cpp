// ExtendedXMILoad.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMILoadImpl
#include "emf/sphinx/resource/ExtendedXMILoad.h"
#include "emf/sphinx/resource/ExtendedSAXXMIHandler.h"
#include "emf/sphinx/resource/ExtendedSAXXMLHandler.h"

namespace emf::sphinx::resource {

ExtendedSAXXMLHandler* ExtendedXMILoad::makeDefaultHandler() {
    // 对齐 Java: return new ExtendedSAXXMIHandler(resource, helper, options);
    // 创建 ExtendedSAXXMIHandler（继承自 ExtendedSAXXMLHandler）而非 SAXXMLHandler。
    return new ExtendedSAXXMIHandler();
}

}  // namespace emf::sphinx::resource
