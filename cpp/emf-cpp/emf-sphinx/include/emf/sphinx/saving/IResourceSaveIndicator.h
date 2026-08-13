// IResourceSaveIndicator.h
// 对齐 Java org.eclipse.sphinx.emf.saving.IResourceSaveIndicator
#pragma once

#include "emf/common/Resource.h"

namespace emf::sphinx::saving {

class IResourceSaveIndicator {
public:
    virtual ~IResourceSaveIndicator() = default;
    virtual bool isDirty(emf::common::Resource* res) const = 0;
    virtual void markSaving(emf::common::Resource* res) = 0;
    virtual void markSaved(emf::common::Resource* res) = 0;
};

}  // namespace emf::sphinx::saving
