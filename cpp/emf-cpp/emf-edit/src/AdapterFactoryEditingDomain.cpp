// AdapterFactoryEditingDomain.cpp
// 对齐 Java: org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
#include "emf/edit/domain/AdapterFactoryEditingDomain.h"

#include "emf/edit/command/AddCommand.h"
#include "emf/edit/command/MoveCommand.h"
#include "emf/edit/command/RemoveCommand.h"
#include "emf/edit/command/ReplaceCommand.h"
#include "emf/edit/command/SetCommand.h"

#include <any>
#include <string>
#include <vector>

namespace emf::edit {

// 前向声明占位 ResourceSet（与 EditingDomain.h 中 class ResourceSet* 一致）
class ResourceSet;

AdapterFactoryEditingDomain::AdapterFactoryEditingDomain() = default;

AdapterFactoryEditingDomain::AdapterFactoryEditingDomain(emf::common::AdapterFactory* factory,
                                                        emf::common::command::CommandStack* stack)
    : factory_(factory), stack_(stack) {}

AdapterFactoryEditingDomain::~AdapterFactoryEditingDomain() = default;

class ResourceSet* AdapterFactoryEditingDomain::getResourceSet() const {
    return resourceSet_;
}

emf::common::command::CommandStack* AdapterFactoryEditingDomain::getCommandStack() const {
    return stack_;
}

emf::common::AdapterFactory* AdapterFactoryEditingDomain::getAdapterFactory() const {
    return factory_;
}

namespace {
// 从 std::any commandClass 提取命令名称字符串（支持 std::string / const char*）
std::string commandName(const std::any& commandClass) {
    if (commandClass.type() == typeid(std::string)) {
        return std::any_cast<std::string>(commandClass);
    }
    if (commandClass.type() == typeid(const char*)) {
        return std::string(std::any_cast<const char*>(commandClass));
    }
    return {};
}

// 安全 any_cast 指针类型参数：类型匹配则返回转换结果，否则 nullptr
template <typename T>
T castParam(const std::any& p) {
    if (p.type() == typeid(T)) {
        return std::any_cast<T>(p);
    }
    return nullptr;
}
}  // namespace

emf::common::command::Command* AdapterFactoryEditingDomain::createCommand(
    std::any commandClass,
    const std::vector<std::any>& parameters) {
    // 解析参数：对齐 Java CommandParameter，parameters[0]=domain, [1]=owner,
    // [2]=feature, [3..]=value/collection/index/replacement
    if (parameters.size() < 3) return nullptr;

    auto* domain = castParam<emf::edit::EditingDomain*>(parameters[0]);
    auto* owner = castParam<emf::common::EObject*>(parameters[1]);
    auto* feature = castParam<emf::ecore::EStructuralFeature*>(parameters[2]);
    // domain 允许为 nullptr（命令可不绑定 domain）；owner/feature 必须有
    (void)domain;

    const std::string name = commandName(commandClass);

    if (name == "Set") {
        if (parameters.size() > 3) {
            return new emf::edit::command::SetCommand(domain, owner, feature, parameters[3]);
        }
        return new emf::edit::command::SetCommand(domain, owner, feature, std::any{});
    }
    if (name == "Add") {
        // parameters[3] = value or std::vector<std::any>
        if (parameters.size() > 3) {
            // 优先尝试集合（vector<any>）
            if (parameters[3].type() == typeid(std::vector<std::any>)) {
                return new emf::edit::command::AddCommand(
                    domain, owner, feature,
                    std::any_cast<std::vector<std::any>>(parameters[3]));
            }
            return new emf::edit::command::AddCommand(domain, owner, feature, parameters[3]);
        }
        return nullptr;
    }
    if (name == "Remove") {
        if (parameters.size() > 3) {
            if (parameters[3].type() == typeid(std::vector<std::any>)) {
                return new emf::edit::command::RemoveCommand(
                    domain, owner, feature,
                    std::any_cast<std::vector<std::any>>(parameters[3]));
            }
            return new emf::edit::command::RemoveCommand(domain, owner, feature, parameters[3]);
        }
        return nullptr;
    }
    if (name == "Move") {
        // parameters[3] = value, parameters[4] = index
        if (parameters.size() > 4) {
            int idx = 0;
            if (parameters[4].type() == typeid(int)) {
                idx = std::any_cast<int>(parameters[4]);
            }
            return new emf::edit::command::MoveCommand(domain, owner, feature, parameters[3], idx);
        }
        return nullptr;
    }
    if (name == "Replace") {
        // parameters[3] = value, parameters[4] = replacement
        if (parameters.size() > 4) {
            return new emf::edit::command::ReplaceCommand(
                domain, owner, feature, parameters[3], parameters[4]);
        }
        return nullptr;
    }
    return nullptr;
}

}  // namespace emf::edit
