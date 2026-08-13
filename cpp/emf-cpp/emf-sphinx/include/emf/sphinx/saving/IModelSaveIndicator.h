// IModelSaveIndicator.h
// 对齐 Java org.eclipse.sphinx.emf.saving.IModelSaveIndicator
#pragma once

#include "emf/sphinx/model/IModelDescriptor.h"

namespace emf::sphinx::saving {

class IModelSaveIndicator {
public:
    virtual ~IModelSaveIndicator() = default;
    virtual bool isDirty(emf::sphinx::model::IModelDescriptor* md) const = 0;
    virtual void markSaving(emf::sphinx::model::IModelDescriptor* md) = 0;
    virtual void markSaved(emf::sphinx::model::IModelDescriptor* md) = 0;
};

}  // namespace emf::sphinx::saving
