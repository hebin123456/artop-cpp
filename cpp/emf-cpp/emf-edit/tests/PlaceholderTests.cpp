// PlaceholderTests.cpp —— emf-edit 框架占位测试
// 状态：emf-edit 仅搭好代码框架，方法实现均为 TODO。
// 此文件仅验证：1) 框架可链接；2) 基本类型 / 单例可构造 / 命名空间可访问。

#include "test_main.h"
#include "emf/edit/EMFEditPlugin.h"
#include "emf/edit/command/AddCommand.h"
#include "emf/edit/command/RemoveCommand.h"
#include "emf/edit/command/SetCommand.h"
#include "emf/edit/command/ReplaceCommand.h"
#include "emf/edit/command/MoveCommand.h"
#include "emf/edit/domain/EditingDomain.h"
#include "emf/edit/domain/AdapterFactoryEditingDomain.h"
#include "emf/edit/provider/IItemProvider.h"
#include "emf/edit/provider/IItemLabelProvider.h"
#include "emf/edit/provider/IStructuredItemContentProvider.h"
#include "emf/edit/provider/ITreeItemContentProvider.h"
#include "emf/edit/provider/ComposedAdapterFactory.h"
#include "emf/edit/tree/TreeNode.h"
#include "emf/edit/util/EditUtil.h"

EMF_TEST(Placeholder_FrameworkLoads) {
    // 框架加载冒烟：单例能取、命名空间能找到符号。
    auto* plugin = emf::edit::EMFEditPlugin::instance();
    EXPECT_NOT_NULL(plugin);
}

EMF_TEST(Placeholder_NamespacesAreAccessible) {
    // 验证所有子命名空间的符号至少可解析。
    using AddCmd    = emf::edit::command::AddCommand;
    using RemoveCmd = emf::edit::command::RemoveCommand;
    using SetCmd    = emf::edit::command::SetCommand;
    using ReplaceCmd= emf::edit::command::ReplaceCommand;
    using MoveCmd   = emf::edit::command::MoveCommand;

    using Domain    = emf::edit::EditingDomain;
    using AFEDomain = emf::edit::AdapterFactoryEditingDomain;

    using IItemProvider        = emf::edit::provider::IItemProvider;
    using IItemLabelProvider   = emf::edit::provider::IItemLabelProvider;
    using IStructuredICP       = emf::edit::provider::IStructuredItemContentProvider;
    using ITreeICP             = emf::edit::provider::ITreeItemContentProvider;
    using ComposedAdapterFactory = emf::edit::provider::ComposedAdapterFactory;

    using TreeNode  = emf::edit::tree::TreeNode;
    using EditUtil  = emf::edit::util::EditUtil;

    EXPECT_TRUE(true);
    (void)sizeof(AddCmd); (void)sizeof(RemoveCmd); (void)sizeof(SetCmd);
    (void)sizeof(ReplaceCmd); (void)sizeof(MoveCmd);
    (void)sizeof(Domain); (void)sizeof(AFEDomain);
    (void)sizeof(IItemProvider); (void)sizeof(IItemLabelProvider);
    (void)sizeof(IStructuredICP); (void)sizeof(ITreeICP);
    (void)sizeof(ComposedAdapterFactory);
    (void)sizeof(TreeNode); (void)sizeof(EditUtil);
}
