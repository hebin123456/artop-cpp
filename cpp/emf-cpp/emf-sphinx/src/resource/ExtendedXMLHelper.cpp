// ExtendedXMLHelper.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMLHelperImpl
#include "emf/sphinx/resource/ExtendedXMLHelper.h"
#include "emf/sphinx/resource/ExtendedResource.h"
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/xmi/XMIResource.h"

namespace emf::sphinx::resource {

ExtendedXMLHelper::ExtendedXMLHelper(ExtendedResource* extendedResource)
    : extendedResource_(extendedResource) {}

std::string ExtendedXMLHelper::getHREF(emf::common::EObject* obj) {
    if (obj == nullptr) return "";

    emf::common::URI objectURI;

    if (!obj->eIsProxy()) {
        emf::common::Resource* otherResource = obj->eResource();
        if (otherResource == nullptr) {
            // 悬空对象：仅当有关联 XMIResource 且对象有 id，且有 ExtendedResource
            // 可委托时才解析；否则按悬空 HREF 处理。
            // 对齐 Java: resource.getID(obj) != null 分支。
            bool hasId = false;
            auto* xmiRes = dynamic_cast<emf::xmi::XMIResource*>(resource_);
            if (xmiRes != nullptr && !xmiRes->getID(obj).empty()) {
                hasId = true;
            }
            if (hasId && extendedResource_ != nullptr) {
                objectURI = extendedResource_->getHREF(obj);
            } else {
                objectURI = handleDanglingHREF(obj);
                if (objectURI.isEmpty()) return "";
            }
        } else {
            // 对象属于某个 resource：优先委托 ExtendedResource，
            // 否则基于对象所在 resource 的 URI + fragment 构造 HREF。
            if (extendedResource_ != nullptr) {
                objectURI = extendedResource_->getHREF(obj);
            } else {
                objectURI = otherResource->getURI().appendFragment(
                    otherResource->getURIFragment(obj));
            }
        }
    } else {
        // Proxy：委托 ExtendedResource 并修剪 proxy 上下文信息。
        // 对齐 Java: extendedResource.getHREF(obj) + trimProxyContextInfo。
        if (extendedResource_ != nullptr) {
            objectURI = extendedResource_->getHREF(obj);
            objectURI = extendedResource_->trimProxyContextInfo(objectURI);
        } else {
            return "";
        }
    }

    objectURI = deresolve(objectURI);
    return objectURI.toString();
}

emf::common::URI ExtendedXMLHelper::deresolve(const emf::common::URI& uri) const {
    if (resource_ != nullptr) {
        emf::common::URI base = resource_->getURI();
        if (!base.isEmpty()) {
            return uri.deresolve(base);
        }
    }
    return uri;
}

emf::common::URI ExtendedXMLHelper::handleDanglingHREF(emf::common::EObject* /*obj*/) {
    // 对齐 Java: XMLHelperImpl.handleDanglingHREF —— 默认返回空 URI。
    return emf::common::URI();
}

}  // namespace emf::sphinx::resource
