// IItemProvider.h
// 对齐 Java: org.eclipse.emf.edit.provider.IItemProvider
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include "emf/common/ENotifier.h"
#include "emf/common/EList.h"

#include <any>
#include <string>
#include <vector>

namespace emf::edit::provider {

// Java 端基础 item provider 接口，根接口。
// IItemPropertySource / IItemLabelProvider / IItemColorProvider / IItemFontProvider 都派生它。
class IItemProvider : public emf::common::Adapter {  // emf::common::Adapter = EAdapter (ENotifier.h)
public:
    ~IItemProvider() override = default;

    // 返回该 provider 管理的子对象列表（Java: getChildren）
    virtual std::vector<std::any> getChildren(std::any object) = 0;

    // 父对象（Java: getParent）
    virtual std::any getParent(std::any object) = 0;

    // 创建 CreateChildCommand / CreateSiblingCommand / CreateCopyCommand / InitializeCopyCommand 等
    virtual std::any createCommand(std::any object, /*EditingDomain*/void* editingDomain,
                                   /*Class*/void* commandClass,
                                   const std::vector<std::any>& parameters) = 0;
};

}  // namespace emf::edit::provider
