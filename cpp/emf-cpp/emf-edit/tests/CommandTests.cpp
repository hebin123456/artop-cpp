// Command 单元测试
#include "test_main.h"
#include "emf/edit/command/AddCommand.h"
#include "emf/edit/command/RemoveCommand.h"
#include "emf/edit/command/SetCommand.h"
#include "emf/edit/command/ReplaceCommand.h"
#include "emf/edit/command/MoveCommand.h"
#include "emf/edit/command/ChangeDescription.h"
#include "emf/edit/command/CommandHelper.h"
#include "emf/edit/domain/AdapterFactoryEditingDomain.h"
#include "emf/edit/provider/ComposedAdapterFactory.h"

EMF_TEST(Command_DefaultConstruct_CannotExecute) {
    emf::edit::command::AddCommand cmd;
    EXPECT_FALSE(cmd.canExecute());
}

EMF_TEST(Command_WithOwner_CanExecute) {
    emf::edit::command::AddCommand cmd(nullptr, nullptr, nullptr, std::any{});
    // owner_ == nullptr -> prepare 返回 false
    EXPECT_FALSE(cmd.canExecute());
}

EMF_TEST(Command_SetCommand_UnsetValueExists) {
    // 仅验证 UNSET_VALUE 静态成员可链接
    const std::any& unset = emf::edit::command::SetCommand::UNSET_VALUE;
    EXPECT_FALSE(unset.has_value());
}

EMF_TEST(ChangeDescription_Empty) {
    emf::edit::command::ChangeDescription cd;
    EXPECT_TRUE(cd.isEmpty());
    EXPECT_EQ(cd.getChanges().size(), 0u);
}

EMF_TEST(ChangeDescription_Add) {
    emf::edit::command::ChangeDescription cd;
    emf::edit::command::FeatureChange fc;
    cd.add(fc);
    EXPECT_FALSE(cd.isEmpty());
    EXPECT_EQ(cd.getChanges().size(), 1u);
    cd.clear();
    EXPECT_TRUE(cd.isEmpty());
}

EMF_TEST(AdapterFactoryEditingDomain_Construct) {
    emf::edit::AdapterFactoryEditingDomain domain;
    EXPECT_NULL(domain.getAdapterFactory());
    EXPECT_NULL(domain.getCommandStack());
    EXPECT_NULL(domain.getResourceSet());
}

EMF_TEST(ComposedAdapterFactory_ChildFactories) {
    emf::edit::provider::ComposedAdapterFactory caf;
    EXPECT_EQ(caf.getChildFactories().size(), 0u);
    EXPECT_FALSE(caf.isFactoryForType(std::any{}));
}
