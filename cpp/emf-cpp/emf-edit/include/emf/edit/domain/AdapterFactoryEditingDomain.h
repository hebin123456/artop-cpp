// AdapterFactoryEditingDomain.h
// 对齐 Java: org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
// 状态: 框架骨架（最小可编译实现）
#pragma once

#include "emf/edit/domain/EditingDomain.h"
#include "emf/common/AdapterFactory.h"
#include "emf/common/command/CommandStack.h"

#include <any>
#include <vector>

namespace emf::edit {

// AdapterFactoryEditingDomain：EditingDomain 的默认实现，
// 持有 AdapterFactory + CommandStack（对齐 Java AdapterFactoryEditingDomain）
class AdapterFactoryEditingDomain : public EditingDomain {
public:
    AdapterFactoryEditingDomain();
    AdapterFactoryEditingDomain(emf::common::AdapterFactory* factory,
                                emf::common::command::CommandStack* stack = nullptr);
    ~AdapterFactoryEditingDomain() override;

    // EditingDomain 接口实现
    class ResourceSet* getResourceSet() const override;
    emf::common::command::CommandStack* getCommandStack() const override;
    emf::common::AdapterFactory* getAdapterFactory() const override;
    emf::common::command::Command* createCommand(std::any commandClass,
                                                 const std::vector<std::any>& parameters) override;

    void setAdapterFactory(emf::common::AdapterFactory* factory) { factory_ = factory; }
    void setCommandStack(emf::common::command::CommandStack* stack) { stack_ = stack; }

private:
    emf::common::AdapterFactory* factory_ = nullptr;
    emf::common::command::CommandStack* stack_ = nullptr;
    class ResourceSet* resourceSet_ = nullptr;
};

}  // namespace emf::edit
