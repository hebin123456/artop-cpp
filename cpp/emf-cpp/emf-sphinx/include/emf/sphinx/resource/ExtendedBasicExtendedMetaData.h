// ExtendedBasicExtendedMetaData.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedBasicExtendedMetaData
#pragma once

#include <string>

namespace emf::sphinx::resource {

class ExtendedBasicExtendedMetaData {
public:
    static ExtendedBasicExtendedMetaData& instance() {
        static ExtendedBasicExtendedMetaData inst;
        return inst;
    }

    // 取得某个 schemaLocation 的本地缓存键
    std::string getCacheKey(const std::string& ns, const std::string& loc);

private:
    ExtendedBasicExtendedMetaData() = default;
};

}  // namespace emf::sphinx::resource
