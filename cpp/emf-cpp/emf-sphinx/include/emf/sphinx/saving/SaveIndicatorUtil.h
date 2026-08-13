// SaveIndicatorUtil.h
// 对齐 Java org.eclipse.sphinx.emf.saving.SaveIndicatorUtil
// headless 工具：判断 resource / model 是否 dirty
#pragma once

#include "emf/common/Resource.h"

#include <functional>

namespace emf::sphinx::saving {

class SaveIndicatorUtil {
public:
    static SaveIndicatorUtil& instance() {
        static SaveIndicatorUtil inst;
        return inst;
    }

    bool isDirty(emf::common::Resource* res) const;
    void markSaving(emf::common::Resource* res);
    void markSaved(emf::common::Resource* res);

    // 触发回调
    void addSaveListener(std::function<void(emf::common::Resource*)> l);
    void removeSaveListener(std::function<void(emf::common::Resource*)> l);

private:
    SaveIndicatorUtil() = default;
    std::vector<std::function<void(emf::common::Resource*)>> listeners_;
};

}  // namespace emf::sphinx::saving
