// EditingDomainTests.cpp
// 测试标准命令真实改模型 + undo/redo + 事务通知延迟
// 对齐 Java EMF Edit/Transaction 框架行为
#include "test_main.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/DynamicEObject.h"
#include "emf/common/EObject.h"
#include "emf/common/EList.h"
#include "emf/common/ENotifier.h"
#include "emf/common/command/BasicCommandStack.h"
#include "emf/edit/command/SetCommand.h"
#include "emf/edit/command/AddCommand.h"
#include "emf/edit/command/RemoveCommand.h"
#include "emf/edit/command/MoveCommand.h"
#include "emf/edit/command/ReplaceCommand.h"
#include "emf/edit/domain/TransactionalEditingDomain.h"

#include <any>
#include <string>
#include <vector>

using emf::common::EObject;
using emf::common::EList;
using emf::common::EAdapter;
using emf::common::Notification;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;
using emf::ecore::DynamicEObject;

// 测试模型：Node { name: EString, children: Node[*] containment }
struct NodeModel {
    EClass* nodeCls = nullptr;
    EAttribute* name = nullptr;
    EReference* children = nullptr;
};

static NodeModel makeNodeModel() {
    NodeModel m;
    EcoreFactory::initialize();
    EcorePackage::initialize();
    m.nodeCls = EcoreFactory::instance().createEClass();
    m.nodeCls->setName("Node");

    m.name = EcoreFactory::instance().createEAttribute();
    m.name->setName("name");
    m.name->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    m.name->setFeatureID(0);
    m.name->setLowerBound(0);
    m.name->setUpperBound(1);
    m.nodeCls->addEStructuralFeature(m.name);

    m.children = EcoreFactory::instance().createEReference();
    m.children->setName("children");
    m.children->setContainment(true);
    m.children->setEReferenceType(m.nodeCls);
    m.children->setFeatureID(3);
    m.children->setLowerBound(0);
    m.children->setUpperBound(-1);
    m.nodeCls->addEStructuralFeature(m.children);
    return m;
}

// 计数 adapter：统计收到的通知数
class CountingAdapter : public EAdapter {
public:
    int count = 0;
    void notifyChanged(const Notification& /*n*/) override { ++count; }
};

// 记录型 adapter：捕获通知详情，用于验证跨对象去重（dedup）语义
class RecordingAdapter : public EAdapter {
public:
    std::vector<Notification> events;
    void notifyChanged(const Notification& n) override { events.push_back(n); }
    int countFor(const EStructuralFeature* f) const {
        int c = 0;
        for (const auto& e : events) if (e.feature() == f) ++c;
        return c;
    }
    void clear() { events.clear(); }
};

// 辅助：从 eGet 提取 EList<EObject*>*
static EList<EObject*>* getList(EObject* obj, EReference* ref) {
    auto v = obj->eGet(ref);
    return std::any_cast<EList<EObject*>*>(v);
}

// ===== SetCommand：单值属性 execute/undo/redo =====
EMF_TEST(SetCommand_SingleValue_ExecuteUndoRedo) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);

    emf::edit::command::SetCommand cmd(nullptr, obj, m.name, std::any{std::string{"root"}});
    EXPECT_TRUE(cmd.canExecute());
    cmd.execute();
    {
        auto v = obj->eGet(m.name);
        EXPECT_EQ(*std::any_cast<std::string>(&v), std::string("root"));
    }
    EXPECT_TRUE(obj->eIsSet(m.name));

    cmd.undo();
    EXPECT_FALSE(obj->eIsSet(m.name));

    cmd.redo();
    {
        auto v = obj->eGet(m.name);
        EXPECT_EQ(*std::any_cast<std::string>(&v), std::string("root"));
    }
    delete obj;
}

// ===== SetCommand：覆盖已有值，undo 恢复旧值 =====
EMF_TEST(SetCommand_Overwrite_UndoRestoresOldValue) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"old"}});

    emf::edit::command::SetCommand cmd(nullptr, obj, m.name, std::any{std::string{"new"}});
    cmd.execute();
    EXPECT_EQ(std::any_cast<std::string>(obj->eGet(m.name)), std::string("new"));

    cmd.undo();
    EXPECT_EQ(std::any_cast<std::string>(obj->eGet(m.name)), std::string("old"));

    cmd.redo();
    EXPECT_EQ(std::any_cast<std::string>(obj->eGet(m.name)), std::string("new"));
    delete obj;
}

// ===== SetCommand：UNSET_VALUE =====
EMF_TEST(SetCommand_UnsetValue_ExecuteUndoRedo) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"root"}});
    EXPECT_TRUE(obj->eIsSet(m.name));

    emf::edit::command::SetCommand cmd(nullptr, obj, m.name,
                                        emf::edit::command::SetCommand::UNSET_VALUE);
    cmd.execute();
    EXPECT_FALSE(obj->eIsSet(m.name));

    cmd.undo();
    EXPECT_TRUE(obj->eIsSet(m.name));
    EXPECT_EQ(std::any_cast<std::string>(obj->eGet(m.name)), std::string("root"));

    cmd.redo();
    EXPECT_FALSE(obj->eIsSet(m.name));
    delete obj;
}

// ===== AddCommand：多值引用 execute/undo/redo =====
EMF_TEST(AddCommand_MultiValue_ExecuteUndoRedo) {
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);

    EXPECT_EQ(getList(parent, m.children)->size(), (size_t)0);

    emf::edit::command::AddCommand cmd(nullptr, parent, m.children,
                                        std::any{(EObject*)c1});
    cmd.execute();
    EXPECT_EQ(getList(parent, m.children)->size(), (size_t)1);
    EXPECT_EQ((*getList(parent, m.children))[0], (EObject*)c1);

    cmd.undo();
    EXPECT_EQ(getList(parent, m.children)->size(), (size_t)0);

    cmd.redo();
    EXPECT_EQ(getList(parent, m.children)->size(), (size_t)1);
    EXPECT_EQ((*getList(parent, m.children))[0], (EObject*)c1);

    delete parent; delete c1;
}

// ===== AddCommand：批量添加 =====
EMF_TEST(AddCommand_Collection_ExecuteUndoRedo) {
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto* c2 = new DynamicEObject(m.nodeCls);

    std::vector<std::any> coll;
    coll.push_back(std::any{(EObject*)c1});
    coll.push_back(std::any{(EObject*)c2});
    emf::edit::command::AddCommand cmd(nullptr, parent, m.children, std::move(coll));
    cmd.execute();
    EXPECT_EQ(getList(parent, m.children)->size(), (size_t)2);

    cmd.undo();
    EXPECT_EQ(getList(parent, m.children)->size(), (size_t)0);

    cmd.redo();
    EXPECT_EQ(getList(parent, m.children)->size(), (size_t)2);
    EXPECT_EQ((*getList(parent, m.children))[0], (EObject*)c1);
    EXPECT_EQ((*getList(parent, m.children))[1], (EObject*)c2);

    delete parent; delete c1; delete c2;
}

// ===== RemoveCommand：execute/undo/redo =====
EMF_TEST(RemoveCommand_ExecuteUndoRedo) {
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto* c2 = new DynamicEObject(m.nodeCls);

    auto* list = getList(parent, m.children);
    list->add(c1);
    list->add(c2);

    emf::edit::command::RemoveCommand cmd(nullptr, parent, m.children,
                                           std::any{(EObject*)c1});
    cmd.execute();
    EXPECT_EQ(list->size(), (size_t)1);
    EXPECT_EQ((*list)[0], (EObject*)c2);

    cmd.undo();
    EXPECT_EQ(list->size(), (size_t)2);
    EXPECT_EQ((*list)[0], (EObject*)c1);
    EXPECT_EQ((*list)[1], (EObject*)c2);

    cmd.redo();
    EXPECT_EQ(list->size(), (size_t)1);
    EXPECT_EQ((*list)[0], (EObject*)c2);

    delete parent; delete c1; delete c2;
}

// ===== MoveCommand：execute/undo/redo =====
EMF_TEST(MoveCommand_ExecuteUndoRedo) {
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto* c2 = new DynamicEObject(m.nodeCls);
    auto* c3 = new DynamicEObject(m.nodeCls);

    auto* list = getList(parent, m.children);
    list->add(c1);
    list->add(c2);
    list->add(c3);
    // [c1, c2, c3] → move c3 to index 0 → [c3, c1, c2]
    emf::edit::command::MoveCommand cmd(nullptr, parent, m.children,
                                         std::any{(EObject*)c3}, 0);
    cmd.execute();
    EXPECT_EQ((*list)[0], (EObject*)c3);
    EXPECT_EQ((*list)[1], (EObject*)c1);
    EXPECT_EQ((*list)[2], (EObject*)c2);

    cmd.undo();
    EXPECT_EQ((*list)[0], (EObject*)c1);
    EXPECT_EQ((*list)[1], (EObject*)c2);
    EXPECT_EQ((*list)[2], (EObject*)c3);

    cmd.redo();
    EXPECT_EQ((*list)[0], (EObject*)c3);
    EXPECT_EQ((*list)[1], (EObject*)c1);
    EXPECT_EQ((*list)[2], (EObject*)c2);

    delete parent; delete c1; delete c2; delete c3;
}

// ===== ReplaceCommand：execute/undo/redo =====
EMF_TEST(ReplaceCommand_ExecuteUndoRedo) {
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto* c2 = new DynamicEObject(m.nodeCls);
    auto* c3 = new DynamicEObject(m.nodeCls);

    auto* list = getList(parent, m.children);
    list->add(c1);
    list->add(c2);

    // replace c1 with c3
    emf::edit::command::ReplaceCommand cmd(nullptr, parent, m.children,
                                            std::any{(EObject*)c1},
                                            std::any{(EObject*)c3});
    cmd.execute();
    EXPECT_EQ(list->size(), (size_t)2);
    EXPECT_EQ((*list)[0], (EObject*)c3);
    EXPECT_EQ((*list)[1], (EObject*)c2);

    cmd.undo();
    EXPECT_EQ((*list)[0], (EObject*)c1);
    EXPECT_EQ((*list)[1], (EObject*)c2);

    cmd.redo();
    EXPECT_EQ((*list)[0], (EObject*)c3);
    EXPECT_EQ((*list)[1], (EObject*)c2);

    delete parent; delete c1; delete c2; delete c3;
}

// ===== BasicCommandStack：多命令 undo/redo 序列 =====
EMF_TEST(BasicCommandStack_MultipleCommands_UndoRedo) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);

    emf::common::command::BasicCommandStack stack;

    auto* cmd1 = new emf::edit::command::SetCommand(nullptr, obj, m.name, std::any{std::string{"a"}});
    auto* cmd2 = new emf::edit::command::SetCommand(nullptr, obj, m.name, std::any{std::string{"b"}});

    auto getName = [&]() { return std::any_cast<std::string>(obj->eGet(m.name)); };

    stack.execute(cmd1);
    EXPECT_EQ(getName(), std::string("a"));

    stack.execute(cmd2);
    EXPECT_EQ(getName(), std::string("b"));

    // undo cmd2 -> "a"
    stack.undo();
    EXPECT_EQ(getName(), std::string("a"));

    // undo cmd1 -> unset
    stack.undo();
    EXPECT_FALSE(obj->eIsSet(m.name));

    // redo cmd1 -> "a"
    stack.redo();
    EXPECT_EQ(getName(), std::string("a"));

    // redo cmd2 -> "b"
    stack.redo();
    EXPECT_EQ(getName(), std::string("b"));

    delete obj;
}

// ===== TransactionalEditingDomain：runWrite 通知延迟 =====
EMF_TEST(TransactionalEditingDomain_WriteNotificationDeferral) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);

    CountingAdapter adapter;
    obj->addAdapter(&adapter);

    emf::edit::TransactionalEditingDomain domain;

    // 事务前：直接通知
    obj->eSet(m.name, std::any{std::string{"before"}});
    EXPECT_TRUE(adapter.count >= 1);
    int countBeforeTx = adapter.count;

    // 事务内：通知延迟，adapter 不应立即收到
    domain.runWrite(nullptr, [&]() {
        obj->eSet(m.name, std::any{std::string{"during"}});
        EXPECT_EQ(adapter.count, countBeforeTx);
    });

    // 事务提交后：累积通知批量投递
    EXPECT_TRUE(adapter.count > countBeforeTx);

    delete obj;
}

// ===== TransactionalEditingDomain：runExclusive 读事务 =====
EMF_TEST(TransactionalEditingDomain_RunExclusive_ReadAllowed) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"value"}});

    emf::edit::TransactionalEditingDomain domain;
    std::string captured;
    domain.runExclusive(nullptr, [&]() {
        captured = std::any_cast<std::string>(obj->eGet(m.name));
    });
    EXPECT_EQ(captured, std::string("value"));

    delete obj;
}

// ===== TransactionalEditingDomain：嵌套事务（重入安全）=====
EMF_TEST(TransactionalEditingDomain_NestedTransaction_Reentrant) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    CountingAdapter adapter;
    obj->addAdapter(&adapter);

    emf::edit::TransactionalEditingDomain domain;
    int countBeforeTx = adapter.count;

    domain.runWrite(nullptr, [&]() {
        obj->eSet(m.name, std::any{std::string{"outer"}});
        // 嵌套事务：不应死锁，通知仍延迟
        domain.runWrite(nullptr, [&]() {
            obj->eSet(m.name, std::any{std::string{"inner"}});
            EXPECT_EQ(adapter.count, countBeforeTx);
        });
        // 内层事务结束（非最外层），不应投递
        EXPECT_EQ(adapter.count, countBeforeTx);
    });
    // 最外层事务结束，批量投递
    EXPECT_TRUE(adapter.count > countBeforeTx);

    delete obj;
}

// ===== 跨对象去重：同对象同 feature 多次 SET 合并为一条通知 =====
// 对齐 Java EMF Transaction NotificationManager：事务内对同一属性多次 SET，
// 提交时合并为一条 SET 通知（oldValue=最早值，newValue=最新值）。
EMF_TEST(TransactionalEditingDomain_CrossObjectDedup_MultipleSetMerged) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    // 先设初值，使事务内第一次 SET 有非空 oldValue
    obj->eSet(m.name, std::any{std::string{"initial"}});

    RecordingAdapter adapter;
    obj->addAdapter(&adapter);
    // 清除挂载 adapter 前的历史通知（addAdapter 本身不触发通知，保险起见）
    adapter.clear();

    emf::edit::TransactionalEditingDomain domain;

    domain.runWrite(nullptr, [&]() {
        obj->eSet(m.name, std::any{std::string{"a"}});
        obj->eSet(m.name, std::any{std::string{"b"}});
        obj->eSet(m.name, std::any{std::string{"c"}});
    });

    // 三次 SET 同 feature → 合并为 1 条
    EXPECT_EQ(adapter.countFor(m.name), 1);
    EXPECT_EQ(adapter.events.size(), (size_t)1);
    // 合并后 oldValue 应为最早值 "initial"，newValue 应为最新值 "c"
    EXPECT_EQ(std::any_cast<std::string>(adapter.events[0].oldValue()), std::string("initial"));
    EXPECT_EQ(std::any_cast<std::string>(adapter.events[0].newValue()), std::string("c"));

    delete obj;
}

// ===== 跨对象去重：不同 feature 的 SET 不合并 =====
EMF_TEST(TransactionalEditingDomain_Dedup_DifferentFeaturesNotMerged) {
    auto m = makeNodeModel();
    // 增加 second 属性
    auto* second = EcoreFactory::instance().createEAttribute();
    second->setName("second");
    second->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    second->setFeatureID(1);
    second->setLowerBound(0);
    second->setUpperBound(1);
    m.nodeCls->addEStructuralFeature(second);

    auto* obj = new DynamicEObject(m.nodeCls);
    RecordingAdapter adapter;
    obj->addAdapter(&adapter);

    emf::edit::TransactionalEditingDomain domain;
    domain.runWrite(nullptr, [&]() {
        obj->eSet(m.name, std::any{std::string{"n1"}});
        obj->eSet(second, std::any{std::string{"s1"}});
        obj->eSet(m.name, std::any{std::string{"n2"}});
    });

    // name: 2 次 SET 合并为 1；second: 1 次 SET 保留 → 共 2 条
    EXPECT_EQ(adapter.countFor(m.name), 1);
    EXPECT_EQ(adapter.countFor(second), 1);
    EXPECT_EQ(adapter.events.size(), (size_t)2);

    delete obj;
}

// ===== 跨对象去重：多对象同 feature 各自独立合并 =====
EMF_TEST(TransactionalEditingDomain_Dedup_MultiObjectsIndependentMerge) {
    auto m = makeNodeModel();
    auto* a = new DynamicEObject(m.nodeCls);
    auto* b = new DynamicEObject(m.nodeCls);
    RecordingAdapter adapter;
    a->addAdapter(&adapter);
    b->addAdapter(&adapter);

    emf::edit::TransactionalEditingDomain domain;
    domain.runWrite(nullptr, [&]() {
        a->eSet(m.name, std::any{std::string{"a1"}});
        b->eSet(m.name, std::any{std::string{"b1"}});
        a->eSet(m.name, std::any{std::string{"a2"}});
        b->eSet(m.name, std::any{std::string{"b2"}});
    });

    // a 和 b 各自合并为 1 条，共 2 条
    EXPECT_EQ(adapter.events.size(), (size_t)2);
    int aCount = 0, bCount = 0;
    for (const auto& e : adapter.events) {
        if (e.notifier() == a) ++aCount;
        if (e.notifier() == b) ++bCount;
    }
    EXPECT_EQ(aCount, 1);
    EXPECT_EQ(bCount, 1);

    delete a; delete b;
}

// ===== 跨对象去重：ADD + REMOVE 同对象同位置抵消 =====
// 注：DynamicEObject 的 ContainmentEList 继承自普通 EList（非 NotifyingListImpl），
// 列表操作不产生通知；生成类用 NotifyingListImpl 时才会触发 ADD/REMOVE 通知。
// 此测试用 eSet 单值引用模拟 ADD/REMOVE 抵消路径（单值 SET 不走该路径，此处
// 仅验证非 SET 事件原样保留、不被误合并）。
EMF_TEST(TransactionalEditingDomain_Dedup_NonSetEventsPreserved) {
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    RecordingAdapter adapter;
    obj->addAdapter(&adapter);

    emf::edit::TransactionalEditingDomain domain;
    domain.runWrite(nullptr, [&]() {
        // UNSET 不参与 SET 合并，应原样保留
        obj->eUnset(m.name);
        obj->eSet(m.name, std::any{std::string{"x"}});
    });

    // UNSET + SET：不同事件类型不合并，共 2 条
    EXPECT_EQ(adapter.events.size(), (size_t)2);

    delete obj;
}
