// ExtendedBasicExtendedMetaData.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedBasicExtendedMetaData
#include "emf/sphinx/resource/ExtendedBasicExtendedMetaData.h"

namespace emf::sphinx::resource {

// 拼装 ns + "|" + loc 作为缓存键
// 对齐 Java: ExtendedBasicExtendedMetaData.getCacheKey()
std::string ExtendedBasicExtendedMetaData::getCacheKey(const std::string& ns, const std::string& loc) {
    if (ns.empty()) return loc;
    return ns + "|" + loc;
}

}  // namespace emf::sphinx::resource
