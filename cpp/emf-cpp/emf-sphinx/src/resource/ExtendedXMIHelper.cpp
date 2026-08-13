// ExtendedXMIHelper.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMIHelperImpl
#include "emf/sphinx/resource/ExtendedXMIHelper.h"
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/xmi/XMIResource.h"

namespace emf::sphinx::resource {

std::string ExtendedXMIHelper::getXmiID(emf::common::EObject* obj) const {
    // 返回 obj 在关联 XMIResource 中的 xmi:id。
    // 对齐 Java: XMIResource.getID(EObject)。
    if (obj == nullptr) return "";
    auto* xmiRes = dynamic_cast<emf::xmi::XMIResource*>(resource_);
    if (xmiRes == nullptr) return "";
    return xmiRes->getID(obj);
}

}  // namespace emf::sphinx::resource
