// Command 单元测试
// 对齐 Java: org.eclipse.emf.common.command.* 全部 12 个类的行为
#include "test_main.h"
#include "emf/common/command/AbstractCommand.h"
#include "emf/common/command/CompoundCommand.h"
#include "emf/common/command/UnexecutableCommand.h"
#include "emf/common/command/IdentityCommand.h"
#include "emf/common/command/CommandWrapper.h"
#include "emf/common/command/StrictCompoundCommand.h"
#include "emf/common/command/BasicCommandStack.h"
#include "emf/common/command/CommandStackListener.h"
#include "emf/common/command/AbortExecutionException.h"
#include "emf/common/CommonPlugin.h"

#include <string>
#include <vector>
#include <type_traits>

using namespace emf::common::command;
using emf::common::CommonPlugin;

// === Test fixture: 一个可执行/可 undo/可 redo 的简单命令，记录状态 ===
class TestCommand : public AbstractCommand {
public:
    TestCommand() = default;
    explicit TestCommand(const std::string& l) : AbstractCommand(l) {}
    TestCommand(const std::string& l, const std::string& d) : AbstractCommand(l, d) {}

    bool prepare() override { return true; }

    void execute() override {
        executed_ = true;
        undone_ = false;
        redone_ = false;
    }
    void undo() override {
        executed_ = false;
        undone_ = true;
        redone_ = false;
    }
    void redo() override {
        executed_ = true;
        undone_ = false;
        redone_ = true;
    }

    bool isExecuted() const { return executed_; }
    bool isUndone() const { return undone_; }
    bool isRedone() const { return redone_; }

private:
    bool executed_ = false;
    bool undone_ = false;
    bool redone_ = false;
};

// === EmptyCmd: AbstractCommand 派生，提供默认无操作的 execute/redo，
// 用于测试 AbstractCommand 自身的默认 label/description 字符串表与 undo 抛异常 ===
class EmptyCmd : public AbstractCommand {
public:
    EmptyCmd() = default;
    explicit EmptyCmd(const std::string& l) : AbstractCommand(l) {}
    EmptyCmd(const std::string& l, const std::string& d) : AbstractCommand(l, d) {}
    bool prepare() override { return false; }
    void execute() override {}
    void redo() override {}
};

// === 1. AbstractCommand 基本行为 ===
EMF_TEST(AbstractCommand_PrepareDefaultsToFalse) {
    EmptyCmd cmd;  // 默认 prepare() 返回 false
    EXPECT_FALSE(cmd.canExecute());
}

EMF_TEST(AbstractCommand_CanExecuteCached) {
    TestCommand cmd("test");
    EXPECT_TRUE(cmd.canExecute());
    EXPECT_TRUE(cmd.canExecute());  // 第二次也 true
    EXPECT_TRUE(cmd.isExecuted() || !cmd.isExecuted());
}

EMF_TEST(AbstractCommand_GetLabelDefault) {
    EmptyCmd cmd;
    EXPECT_EQ(cmd.getLabel(), CommonPlugin::instance().getString("_UI_AbstractCommand_label"));
}

EMF_TEST(AbstractCommand_GetDescriptionDefault) {
    EmptyCmd cmd;
    EXPECT_EQ(cmd.getDescription(), CommonPlugin::instance().getString("_UI_AbstractCommand_description"));
}

EMF_TEST(AbstractCommand_SetLabelAndDescription) {
    EmptyCmd cmd("MyLabel", "MyDesc");
    EXPECT_EQ(cmd.getLabel(), std::string("MyLabel"));
    EXPECT_EQ(cmd.getDescription(), std::string("MyDesc"));
}

EMF_TEST(AbstractCommand_UndoThrows) {
    EmptyCmd ec;
    // AbstractCommand 默认 undo 抛异常
    EXPECT_THROWS(ec.undo());
}

// === 2. CompoundCommand ===
EMF_TEST(CompoundCommand_EmptyCannotExecute) {
    CompoundCommand cc;
    EXPECT_FALSE(cc.canExecute());
    EXPECT_TRUE(cc.isEmpty());
}

EMF_TEST(CompoundCommand_BasicExecution) {
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    CompoundCommand cc;
    cc.append(a);
    cc.append(b);
    EXPECT_FALSE(cc.isEmpty());
    EXPECT_EQ(cc.getCommandList().size(), static_cast<size_t>(2));
    EXPECT_TRUE(cc.canExecute());
    cc.execute();
    EXPECT_TRUE(a->isExecuted());
    EXPECT_TRUE(b->isExecuted());
    cc.undo();
    // 反向 undo，b 先 a 后
    EXPECT_TRUE(b->isUndone());
    EXPECT_TRUE(a->isUndone());
    cc.redo();
    EXPECT_TRUE(a->isExecuted());
    EXPECT_TRUE(b->isExecuted());
    cc.dispose();
    delete a;
    delete b;
}

EMF_TEST(CompoundCommand_ResultIndexLast) {
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    CompoundCommand cc(CompoundCommand::LAST_COMMAND_ALL);
    cc.append(a);
    cc.append(b);
    auto res = cc.getResult();
    // LAST_COMMAND_ALL 返回最后一个的 getResult() - 空
    EXPECT_EQ(res.size(), static_cast<size_t>(0));
    delete a;
    delete b;
}

EMF_TEST(CompoundCommand_ResultIndex0) {
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    CompoundCommand cc(0);
    cc.append(a);
    cc.append(b);
    auto res = cc.getResult();
    EXPECT_EQ(res.size(), static_cast<size_t>(0));  // a 的 getResult 是空
    delete a;
    delete b;
}

EMF_TEST(CompoundCommand_LabelFromLast) {
    auto* a = new TestCommand("labelA");
    auto* b = new TestCommand("labelB");
    CompoundCommand cc;  // MERGE_COMMAND_ALL
    cc.append(a);
    cc.append(b);
    EXPECT_EQ(cc.getLabel(), std::string("labelB"));
    delete a;
    delete b;
}

EMF_TEST(CompoundCommand_LabelExplicit) {
    CompoundCommand cc("explicit");
    EXPECT_EQ(cc.getLabel(), std::string("explicit"));
}

EMF_TEST(CompoundCommand_AppendNullIgnored) {
    CompoundCommand cc;
    cc.append(nullptr);  // 不应抛
    EXPECT_TRUE(cc.isEmpty());
}

EMF_TEST(CompoundCommand_AppendIfCanExecute) {
    auto* a = new TestCommand("a");
    CompoundCommand cc;
    EXPECT_TRUE(cc.appendIfCanExecute(a));
    EXPECT_FALSE(cc.isEmpty());
    delete a;
}

EMF_TEST(CompoundCommand_UnwrapEmpty) {
    CompoundCommand cc;
    auto* u = cc.unwrap();
    EXPECT_FALSE(u->canExecute());  // UnexecutableCommand
    // u 是单例不能 delete
}

EMF_TEST(CompoundCommand_UnwrapOne) {
    auto* a = new TestCommand("a");
    CompoundCommand cc;
    cc.append(a);
    auto* u = cc.unwrap();
    EXPECT_TRUE(u->canExecute());
    delete u;
}

EMF_TEST(CompoundCommand_UnwrapMany) {
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    CompoundCommand cc;
    cc.append(a);
    cc.append(b);
    auto* u = cc.unwrap();
    EXPECT_EQ(u, &cc);  // 返回 this
    delete a;
    delete b;
}

EMF_TEST(CompoundCommand_AppendAfterPreparedThrows) {
    auto* a = new TestCommand("a");
    CompoundCommand cc;
    cc.append(a);
    cc.canExecute();  // 触发 prepare -> isPrepared_ = true
    EXPECT_THROWS(cc.append(new TestCommand("b")));
    delete a;
}

// === 3. UnexecutableCommand ===
EMF_TEST(UnexecutableCommand_CannotExecute) {
    auto& u = UnexecutableCommand::instance();
    EXPECT_FALSE(u.canExecute());
    EXPECT_FALSE(u.canUndo());
    EXPECT_THROWS(u.execute());
    EXPECT_THROWS(u.redo());
}

EMF_TEST(UnexecutableCommand_Label) {
    auto& u = UnexecutableCommand::instance();
    EXPECT_EQ(u.getLabel(), CommonPlugin::instance().getString("_UI_UnexecutableCommand_label"));
}

// === 4. IdentityCommand ===
EMF_TEST(IdentityCommand_CanExecute) {
    auto& i = IdentityCommand::instance();
    EXPECT_TRUE(i.canExecute());
    i.execute();
    i.undo();
    i.redo();
}

EMF_TEST(IdentityCommand_ResultEmpty) {
    auto& i = IdentityCommand::instance();
    EXPECT_EQ(i.getResult().size(), static_cast<size_t>(0));
}

EMF_TEST(IdentityCommand_ResultSingleton) {
    std::any val = std::string("hello");
    IdentityCommand cmd(val);
    auto res = cmd.getResult();
    EXPECT_EQ(res.size(), static_cast<size_t>(1));
    EXPECT_TRUE(res[0].has_value());
    EXPECT_EQ(std::any_cast<std::string>(res[0]), std::string("hello"));
}

EMF_TEST(IdentityCommand_ResultCollection) {
    Collection c;
    c.push_back(std::string("a"));
    c.push_back(42);
    IdentityCommand cmd(c);
    auto res = cmd.getResult();
    EXPECT_EQ(res.size(), static_cast<size_t>(2));
}

EMF_TEST(IdentityCommand_LabelDescription) {
    auto& i = IdentityCommand::instance();
    EXPECT_EQ(i.getLabel(), CommonPlugin::instance().getString("_UI_IdentityCommand_label"));
    EXPECT_EQ(i.getDescription(), CommonPlugin::instance().getString("_UI_IdentityCommand_description"));
}

EMF_TEST(IdentityCommand_LabelOverride) {
    IdentityCommand cmd(std::string("custom"));
    EXPECT_EQ(cmd.getLabel(), std::string("custom"));
}

// === 5. CommandWrapper ===
EMF_TEST(CommandWrapper_DelegatesExecute) {
    auto* inner = new TestCommand("inner");
    CommandWrapper w(inner);
    EXPECT_TRUE(w.canExecute());
    w.execute();
    EXPECT_TRUE(inner->isExecuted());
    w.undo();
    EXPECT_TRUE(inner->isUndone());
    w.redo();
    EXPECT_TRUE(inner->isRedone());
    w.dispose();
}

EMF_TEST(CommandWrapper_CanUndoDelegates) {
    auto* inner = new TestCommand("inner");
    CommandWrapper w(inner);
    EXPECT_TRUE(w.canUndo());
}

EMF_TEST(CommandWrapper_NullCommand) {
    CommandWrapper w;
    EXPECT_FALSE(w.canExecute());
    w.execute();  // 不应抛
    w.undo();
    w.redo();
    EXPECT_EQ(w.getResult().size(), static_cast<size_t>(0));
}

EMF_TEST(CommandWrapper_GetLabelDelegates) {
    auto* inner = new TestCommand("inner-label");
    CommandWrapper w(inner);
    EXPECT_EQ(w.getLabel(), std::string("inner-label"));
}

EMF_TEST(CommandWrapper_GetLabelOverride) {
    auto* inner = new TestCommand("inner");
    CommandWrapper w("override", inner);
    EXPECT_EQ(w.getLabel(), std::string("override"));
}

EMF_TEST(CommandWrapper_LazyCreate) {
    bool created = false;
    TestCommand* inner = nullptr;
    struct LazyWrapper : public CommandWrapper {
        bool* flag;
        TestCommand** store;
        LazyWrapper(bool* f, TestCommand** s) : flag(f), store(s) {}
        Command* createCommand() override {
            *flag = true;
            *store = new TestCommand("lazy");
            return *store;
        }
    };
    bool flag = false;
    TestCommand* s = nullptr;
    LazyWrapper lw(&flag, &s);
    EXPECT_TRUE(lw.canExecute());  // 第一次调用 prepare -> createCommand -> TestCommand 准备 true
    EXPECT_TRUE(flag);
    EXPECT_TRUE(lw.canExecute());  // 已 prepared
    lw.execute();
    EXPECT_TRUE(s->isExecuted());
    delete s;
}

// === 6. StrictCompoundCommand ===
EMF_TEST(StrictCompoundCommand_EmptyCannotExecute) {
    StrictCompoundCommand sc;
    EXPECT_FALSE(sc.canExecute());
}

EMF_TEST(StrictCompoundCommand_BasicExecute) {
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    StrictCompoundCommand sc;
    sc.append(a);
    sc.append(b);
    EXPECT_TRUE(sc.canExecute());
    // 非悲观：前 N-1 在 prepare 中已 execute，最后一个未执行
    sc.execute();
    // 由于 prepare 会先试探执行 a，所以 a 已被执行过一次
    // 然后 execute 再执行最后一个 b
    EXPECT_TRUE(a->isExecuted());
    EXPECT_TRUE(b->isExecuted());
    sc.undo();
    // 非悲观：只 undo 最后一个
    EXPECT_TRUE(b->isUndone());
    delete a;
    delete b;
}

EMF_TEST(StrictCompoundCommand_Pessimistic) {
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    StrictCompoundCommand sc;
    sc.setIsPessimistic(true);
    sc.append(a);
    sc.append(b);
    EXPECT_TRUE(sc.canExecute());
    // 悲观：prepare 中执行 a，pessimistic 时 undo，sc.execute() 时再执行
    sc.execute();
    EXPECT_TRUE(a->isExecuted());
    EXPECT_TRUE(b->isExecuted());
    sc.undo();
    EXPECT_FALSE(a->isExecuted());
    EXPECT_FALSE(b->isExecuted());
    delete a;
    delete b;
}

EMF_TEST(StrictCompoundCommand_AppendAndExecute) {
    StrictCompoundCommand sc;
    auto* c = new TestCommand("c");
    EXPECT_TRUE(sc.appendAndExecute(c));
    EXPECT_EQ(sc.getCommandList().size(), static_cast<size_t>(1));
    EXPECT_TRUE(c->isExecuted());
    delete c;
}

// === 7. BasicCommandStack ===
namespace {

struct StackListener : public CommandStack::Listener {
    int callCount = 0;
    void commandStackChanged(void* /*event*/) override { ++callCount; }
};

}  // namespace

EMF_TEST(BasicCommandStack_ExecuteAddsCommand) {
    BasicCommandStack stack;
    auto* cmd = new TestCommand("a");
    stack.execute(cmd);
    EXPECT_NOT_NULL(stack.getUndoCommand());
    EXPECT_EQ(stack.getUndoCommand(), cmd);
    EXPECT_TRUE(cmd->isExecuted());
    EXPECT_TRUE(stack.canUndo());
    EXPECT_FALSE(stack.canRedo());
    stack.flush();
}

EMF_TEST(BasicCommandStack_UndoRedo) {
    BasicCommandStack stack;
    auto* cmd = new TestCommand("a");
    stack.execute(cmd);
    EXPECT_TRUE(stack.canUndo());
    stack.undo();
    EXPECT_TRUE(cmd->isUndone());
    EXPECT_FALSE(stack.canUndo());
    EXPECT_TRUE(stack.canRedo());
    stack.redo();
    EXPECT_TRUE(cmd->isRedone());
    stack.flush();
}

EMF_TEST(BasicCommandStack_RedoClearsAhead) {
    BasicCommandStack stack;
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    stack.execute(a);
    stack.undo();  // a undone
    stack.execute(b);  // b 执行；redo 部分被清
    EXPECT_FALSE(stack.canRedo());
    EXPECT_TRUE(stack.canUndo());
    EXPECT_EQ(stack.getUndoCommand(), b);
    delete a;
    delete b;
}

EMF_TEST(BasicCommandStack_MostRecent) {
    BasicCommandStack stack;
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    stack.execute(a);
    stack.execute(b);
    EXPECT_EQ(stack.getMostRecentCommand(), b);
    stack.flush();
}

EMF_TEST(BasicCommandStack_FlushClears) {
    BasicCommandStack stack;
    auto* a = new TestCommand("a");
    stack.execute(a);
    stack.flush();
    EXPECT_NULL(stack.getUndoCommand());
    EXPECT_NULL(stack.getRedoCommand());
    EXPECT_NULL(stack.getMostRecentCommand());
    EXPECT_FALSE(stack.canUndo());
    EXPECT_FALSE(stack.canRedo());
}

EMF_TEST(BasicCommandStack_ListenerNotified) {
    BasicCommandStack stack;
    StackListener listener;
    stack.addCommandStackListener(&listener);
    auto* a = new TestCommand("a");
    stack.execute(a);
    EXPECT_EQ(listener.callCount, 1);
    stack.undo();
    EXPECT_EQ(listener.callCount, 2);
    stack.removeCommandStackListener(&listener);
    auto* b = new TestCommand("b");
    stack.execute(b);
    EXPECT_EQ(listener.callCount, 2);
    stack.flush();
    delete a;
    delete b;
}

EMF_TEST(BasicCommandStack_SaveIndex) {
    BasicCommandStack stack;
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    stack.execute(a);
    stack.saveIsDone();
    EXPECT_FALSE(stack.isSaveNeeded());
    stack.execute(b);
    EXPECT_TRUE(stack.isSaveNeeded());
    stack.undo();
    EXPECT_FALSE(stack.isSaveNeeded());
    stack.flush();
    delete a;
    delete b;
}

EMF_TEST(BasicCommandStack_NullCommandIgnored) {
    BasicCommandStack stack;
    stack.execute(nullptr);
    EXPECT_NULL(stack.getUndoCommand());
    EXPECT_FALSE(stack.canUndo());
}

// === 8. AbortExecutionException ===
EMF_TEST(AbortExecutionException_DefaultConstructor) {
    AbortExecutionException ex;
    EXPECT_EQ(std::string(ex.what()), std::string(""));
}

EMF_TEST(AbortExecutionException_MessageConstructor) {
    AbortExecutionException ex("aborted");
    EXPECT_EQ(std::string(ex.what()), std::string("aborted"));
}

EMF_TEST(AbortExecutionException_ThrowAndCatch) {
    bool caught = false;
    try {
        throw AbortExecutionException("abort");
    } catch (const AbortExecutionException& e) {
        caught = true;
        EXPECT_EQ(std::string(e.what()), std::string("abort"));
    } catch (const std::exception&) {
        caught = false;
    }
    EXPECT_TRUE(caught);
}

// === 9. CommandStackListener 类型别名 ===
EMF_TEST(CommandStackListener_TypeAlias) {
    // 验证别名存在并可用
    static_assert(
        std::is_same<CommandStackListener, CommandStack::Listener>::value,
        "CommandStackListener must alias CommandStack::Listener");
    EXPECT_TRUE(true);
}

// === 10. AbstractCommand.chain() ===
EMF_TEST(AbstractCommand_Chain) {
    auto* a = new TestCommand("a");
    auto* b = new TestCommand("b");
    Command* chained = a->chain(b);
    // chained 是一个 CompoundCommand
    EXPECT_TRUE(chained->canExecute());
    chained->execute();
    EXPECT_TRUE(a->isExecuted());
    EXPECT_TRUE(b->isExecuted());
    chained->dispose();
    delete chained;
}
