// Resource 实现（简化版，不依赖 ecore）
// 对齐 org.eclipse.emf.ecore.resource.Resource
#include "emf/common/Resource.h"
#include "emf/common/EList.h"

namespace emf::common {

// 静态成员定义
URIConverter Resource::uriConverter_;



// 基础实现：直接返回 nullptr
// XMIResource 会重写这些方法
EObject* Resource::getEObject(const std::string& /*fragment*/) {
    return nullptr;
}

std::string Resource::getURIFragment(EObject* /*obj*/) {
    return "";
}

EObject* Resource::resolvePositionPath(const std::string& /*path*/) {
    return nullptr;
}

bool Resource::findFragmentRecursive(const std::vector<EObject*>& /*objs*/, EObject* /*target*/, std::string& /*frag*/) {
    return false;
}

std::vector<EObject*> Resource::anyToEObjectList(const std::any& v) {
    std::vector<EObject*> r;
    if (v.type() == typeid(EObject*)) {
        if (auto* p = std::any_cast<EObject*>(v)) r.push_back(p);
    } else if (v.type() == typeid(std::vector<EObject*>)) {
        for (auto* p : std::any_cast<std::vector<EObject*>>(v)) r.push_back(p);
    } else if (v.type() == typeid(EList<EObject*>*)) {
        auto* p = std::any_cast<EList<EObject*>*>(v);
        if (p) for (size_t i = 0; i < p->size(); ++i) r.push_back((*p)[i]);
    }
    return r;
}

}  // namespace emf::common
