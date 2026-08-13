// IdentityCommand.h
// 对齐 Java org.eclipse.emf.common.command.IdentityCommand
// 单例 INSTANCE；所有方法 do nothing；返回 result 集合。
#pragma once

#include "emf/common/command/AbstractCommand.h"
#include "emf/common/command/Command.h"

#include <vector>
#include <any>
#include <string>

namespace emf::common::command {

class IdentityCommand : public AbstractCommand {
public:
    // Java: public static final IdentityCommand INSTANCE
    static IdentityCommand& instance() {
        static IdentityCommand inst;
        return inst;
    }

    IdentityCommand() : result_(emptyList()) {}
    explicit IdentityCommand(const std::any& result) : result_(singleton(result)) {}
    explicit IdentityCommand(Collection result) : result_(std::move(result)) {}

    explicit IdentityCommand(const std::string& label)
        : AbstractCommand(label), result_(emptyList()) {}
    IdentityCommand(const std::string& label, const std::any& result)
        : AbstractCommand(label), result_(singleton(result)) {}
    IdentityCommand(const std::string& label, Collection result)
        : AbstractCommand(label), result_(std::move(result)) {}

    IdentityCommand(const std::string& label, const std::string& description)
        : AbstractCommand(label, description), result_(emptyList()) {}
    IdentityCommand(const std::string& label, const std::string& description, const std::any& result)
        : AbstractCommand(label, description), result_(singleton(result)) {}
    IdentityCommand(const std::string& label, const std::string& description, Collection result)
        : AbstractCommand(label, description), result_(std::move(result)) {}

    bool canExecute() override { return true; }
    void execute() override { /* Do nothing. */ }
    void undo() override { /* Do nothing. */ }
    void redo() override { /* Do nothing. */ }

    Collection getResult() override { return result_; }

    std::string getLabel() override;
    std::string getDescription() override;

    // 单例：禁用拷贝/赋值
    IdentityCommand(const IdentityCommand&) = delete;
    IdentityCommand& operator=(const IdentityCommand&) = delete;

private:
    static Collection emptyList() { return {}; }
    static Collection singleton(const std::any& a) {
        Collection c;
        c.push_back(a);
        return c;
    }

    Collection result_;
};

}  // namespace emf::common::command
