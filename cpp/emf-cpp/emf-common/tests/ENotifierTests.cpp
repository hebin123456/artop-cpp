// ENotifier 单元测试
// 对齐 org.eclipse.emf.common.notify.Notifier / BasicNotifierImpl (Java) 行为
// 测试 emf::common::Notifier（emf/common/ENotifier.h）的 adapter 列表与通知投递
#include "test_main.h"
#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"
#include "emf/common/EObject.h"

#include <any>
#include <string>
#include <vector>

using emf::common::Notifier;
using emf::common::EAdapter;
using emf::common::Notification;
using emf::common::EObject;

namespace {

// 录音型 Adapter：记录收到的通知
class RecordingAdapter : public EAdapter {
public:
    int notifyCount = 0;
    std::vector<Notification> received;

    void notifyChanged(const Notification& notification) override {
        ++notifyCount;
        received.push_back(notification);
    }
};

// 占位用的 Notifier 子类（Notifier 是抽象基类——实际无纯虚，但默认构造即可用）
class TestNotifier : public Notifier {};

}  // namespace

// ===== 默认状态 =====
EMF_TEST(ENotifier_DefaultEDeliverTrue) {
    TestNotifier n;
    // 对齐 Java BasicNotifierImpl.eDeliver 默认 true
    EXPECT_TRUE(n.eDeliver());
}

EMF_TEST(ENotifier_DefaultEmptyAdapters) {
    TestNotifier n;
    EXPECT_TRUE(n.eAdapters().empty());
    EXPECT_EQ(n.eAdapters().size(), (size_t)0);
}

// ===== addAdapter / removeAdapter =====
EMF_TEST(ENotifier_AddAdapter_IncreasesSize) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    EXPECT_EQ(n.eAdapters().size(), (size_t)1);
    EXPECT_EQ(n.eAdapters()[0], &a);
}

EMF_TEST(ENotifier_AddAdapter_Null_Ignored) {
    TestNotifier n;
    n.addAdapter(nullptr);
    EXPECT_TRUE(n.eAdapters().empty());
}

EMF_TEST(ENotifier_AddAdapter_Duplicate_NotAdded) {
    // 对齐 Java：同一 adapter 不重复加入
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    n.addAdapter(&a);
    EXPECT_EQ(n.eAdapters().size(), (size_t)1);
}

EMF_TEST(ENotifier_AddMultipleAdapters) {
    TestNotifier n;
    RecordingAdapter a1, a2, a3;
    n.addAdapter(&a1);
    n.addAdapter(&a2);
    n.addAdapter(&a3);
    EXPECT_EQ(n.eAdapters().size(), (size_t)3);
    EXPECT_EQ(n.eAdapters()[0], &a1);
    EXPECT_EQ(n.eAdapters()[1], &a2);
    EXPECT_EQ(n.eAdapters()[2], &a3);
}

EMF_TEST(ENotifier_RemoveAdapter_DecreasesSize) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    EXPECT_EQ(n.eAdapters().size(), (size_t)1);
    n.removeAdapter(&a);
    EXPECT_EQ(n.eAdapters().size(), (size_t)0);
}

EMF_TEST(ENotifier_RemoveAdapter_NotPresent_NoChange) {
    TestNotifier n;
    RecordingAdapter a1, a2;
    n.addAdapter(&a1);
    // a2 未加入，移除不应改变列表
    n.removeAdapter(&a2);
    EXPECT_EQ(n.eAdapters().size(), (size_t)1);
    EXPECT_EQ(n.eAdapters()[0], &a1);
}

EMF_TEST(ENotifier_RemoveAdapter_Null_NoChange) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    n.removeAdapter(nullptr);
    EXPECT_EQ(n.eAdapters().size(), (size_t)1);
}

EMF_TEST(ENotifier_RemoveAdapter_Middle_PreservesOrder) {
    TestNotifier n;
    RecordingAdapter a1, a2, a3;
    n.addAdapter(&a1);
    n.addAdapter(&a2);
    n.addAdapter(&a3);
    n.removeAdapter(&a2);
    EXPECT_EQ(n.eAdapters().size(), (size_t)2);
    EXPECT_EQ(n.eAdapters()[0], &a1);
    EXPECT_EQ(n.eAdapters()[1], &a3);
}

// ===== eDeliver / eSetDeliver =====
EMF_TEST(ENotifier_ESetDeliver_Toggle) {
    TestNotifier n;
    EXPECT_TRUE(n.eDeliver());
    n.eSetDeliver(false);
    EXPECT_FALSE(n.eDeliver());
    n.eSetDeliver(true);
    EXPECT_TRUE(n.eDeliver());
}

EMF_TEST(ENotifier_ENotify_EDeliverFalse_NoDelivery) {
    // 对齐 Java eNotify：eDeliver=false 时直接跳过整个投递循环
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    n.eSetDeliver(false);
    Notification evt(Notification::EventType::SET, &n, nullptr, -1,
                     std::any{}, std::any{});
    n.eNotify(evt);
    EXPECT_EQ(a.notifyCount, 0);
}

EMF_TEST(ENotifier_ENotify_EDeliverTrue_DeliveredToAllAdapters) {
    TestNotifier n;
    RecordingAdapter a1, a2;
    n.addAdapter(&a1);
    n.addAdapter(&a2);
    n.eSetDeliver(true);
    Notification evt(Notification::EventType::SET, &n, nullptr, -1,
                     std::any{std::string{"old"}}, std::any{std::string{"new"}});
    n.eNotify(evt);
    EXPECT_EQ(a1.notifyCount, 1);
    EXPECT_EQ(a2.notifyCount, 1);
}

EMF_TEST(ENotifier_ENotify_NoAdapters_NoCrash) {
    TestNotifier n;
    n.eSetDeliver(true);
    Notification evt(Notification::EventType::ADD, &n, nullptr, -1,
                     std::any{}, std::any{});
    n.eNotify(evt);  // 无 adapter，不应崩溃
}

EMF_TEST(ENotifier_ENotify_PreservesEventFields) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    n.eSetDeliver(true);
    int featureID = 42;
    Notification evt(Notification::EventType::ADD, &n, nullptr, featureID,
                     std::any{std::string{"x"}}, std::any{std::string{"y"}}, 3);
    n.eNotify(evt);
    EXPECT_EQ(a.received.size(), (size_t)1);
    const auto& r = a.received[0];
    EXPECT_TRUE(r.eventType() == Notification::EventType::ADD);
    EXPECT_EQ(r.notifier(), &n);
    EXPECT_EQ(r.featureID(), featureID);
    EXPECT_EQ(r.position(), 3);
    EXPECT_EQ(std::any_cast<std::string>(r.newValue()), std::string("y"));
    EXPECT_EQ(std::any_cast<std::string>(r.oldValue()), std::string("x"));
}

// ===== eNotificationRequired 语义（eDeliver && !eAdapters().empty()）=====
// 注：eNotificationRequired 定义在 EObject 上，Notifier 未提供；
//     此处直接验证等价语义：eDeliver() && !eAdapters().empty()
EMF_TEST(ENotifier_ENotificationRequired_NoAdapters_False) {
    TestNotifier n;
    EXPECT_FALSE(n.eDeliver() && !n.eAdapters().empty());
}

EMF_TEST(ENotifier_ENotificationRequired_WithAdaptersButDeliverFalse_False) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    n.eSetDeliver(false);
    EXPECT_FALSE(n.eDeliver() && !n.eAdapters().empty());
}

EMF_TEST(ENotifier_ENotificationRequired_WithAdaptersAndDeliverTrue_True) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    n.eSetDeliver(true);
    EXPECT_TRUE(n.eDeliver() && !n.eAdapters().empty());
}

// ===== removeAdapter 触发 REMOVING_ADAPTER 通知 =====
// 对齐 Java BasicNotifierImpl.removeAdapter：先发 REMOVING_ADAPTER 通知再从列表移除
EMF_TEST(ENotifier_RemoveAdapter_NotifiesAdapter) {
    TestNotifier n;
    n.eSetDeliver(true);
    RecordingAdapter a1, a2;
    n.addAdapter(&a1);
    n.addAdapter(&a2);
    // 移除 a1 时，a2 应当收到 REMOVING_ADAPTER 通知
    n.removeAdapter(&a1);
    EXPECT_EQ(a2.notifyCount, 1);
    EXPECT_TRUE(a2.received[0].eventType() == Notification::EventType::REMOVING_ADAPTER);
}

EMF_TEST(ENotifier_RemoveAdapter_EDeliverFalse_NoRemovalNotification) {
    // eDeliver=false 时，removeAdapter 内部的 eNotify 不应投递
    TestNotifier n;
    n.eSetDeliver(false);
    RecordingAdapter a1, a2;
    n.addAdapter(&a1);
    n.addAdapter(&a2);
    n.removeAdapter(&a1);
    EXPECT_EQ(a2.notifyCount, 0);
    // 列表仍应正确更新
    EXPECT_EQ(n.eAdapters().size(), (size_t)1);
    EXPECT_EQ(n.eAdapters()[0], &a2);
}

// ===== adapter.target 管理（对齐 EAdapter.setTarget）=====
EMF_TEST(EAdapter_DefaultTargetNull) {
    RecordingAdapter a;
    EXPECT_NULL(a.getTarget());
}

EMF_TEST(EAdapter_SetTarget_ReturnsSame) {
    RecordingAdapter a;
    TestNotifier n;
    a.setTarget(&n);
    EXPECT_EQ(a.getTarget(), &n);
}

// ===== B1/B4: addAdapter/removeAdapter 联动 setTarget =====
// 对齐 Java BasicNotifierImpl.eBasicAddAdapter/eBasicRemoveAdapter：
// addAdapter 调 adapter.setTarget(this)，removeAdapter 调 adapter.setTarget(nullptr)。
EMF_TEST(ENotifier_AddAdapter_SetsTarget) {
    TestNotifier n;
    RecordingAdapter a;
    EXPECT_NULL(a.getTarget());
    n.addAdapter(&a);
    EXPECT_EQ(a.getTarget(), &n);
}

EMF_TEST(ENotifier_RemoveAdapter_ClearsTarget) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    EXPECT_EQ(a.getTarget(), &n);
    n.removeAdapter(&a);
    EXPECT_NULL(a.getTarget());
}

// 最小 EObjectImpl 子类（用于 NotificationChain 抵消测试需要 EObject* 值）
class TestEObject : public emf::common::EObjectImpl {
public:
    emf::ecore::EClass* eClass() const override { return nullptr; }
};

// ===== A4: NotificationChain ADD+REMOVE 抵消 =====
// 对齐 Java NotificationChainImpl.add：同 notifier/feature/position/对象 的
// ADD + REMOVE 抵消为 no-op。
EMF_TEST(NotificationChain_AddRemove_Cancels) {
    TestNotifier n;
    TestEObject obj;
    emf::common::NotificationChain chain;
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::ADD, &n, nullptr, -1,
        std::any(), std::any(static_cast<emf::common::EObject*>(&obj)), 0));
    EXPECT_EQ(chain.size(), (size_t)1);
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::REMOVE, &n, nullptr, -1,
        std::any(static_cast<emf::common::EObject*>(&obj)), std::any(), 0));
    EXPECT_EQ(chain.size(), (size_t)0);  // 抵消
}

EMF_TEST(NotificationChain_AddRemove_DifferentObject_NotCancelled) {
    TestNotifier n;
    TestEObject obj1, obj2;
    emf::common::NotificationChain chain;
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::ADD, &n, nullptr, -1,
        std::any(), std::any(static_cast<emf::common::EObject*>(&obj1)), 0));
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::REMOVE, &n, nullptr, -1,
        std::any(static_cast<emf::common::EObject*>(&obj2)), std::any(), 0));
    EXPECT_EQ(chain.size(), (size_t)2);  // 不同对象不抵消
}

// ===== A4: NotificationChain SET+SET 合并 =====
EMF_TEST(NotificationChain_SetSet_Merges) {
    TestNotifier n;
    emf::common::NotificationChain chain;
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::SET, &n, nullptr, -1,
        std::any(std::string("first")), std::any(std::string("a")), -1));
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::SET, &n, nullptr, -1,
        std::any(std::string("b")), std::any(std::string("second")), -1));
    EXPECT_EQ(chain.size(), (size_t)1);  // 合并
}

// ===== Gap 6: NotificationChain.merge() 合并另一条链（vector<Notification>）=====
// 对齐 Java NotificationChainImpl.add(NotificationChain)：逐条 add，复用合并/抵消语义。
EMF_TEST(NotificationChain_Merge_Vector_MergesSetSet) {
    TestNotifier n;
    emf::common::NotificationChain chain;
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::SET, &n, nullptr, -1,
        std::any(std::string("first")), std::any(std::string("a")), -1));
    // 另一条链（vector 形式，兼容 EObjectNotificationChain）
    std::vector<emf::common::Notification> other;
    other.emplace_back(
        emf::common::Notification::EventType::SET, &n, nullptr, -1,
        std::any(std::string("b")), std::any(std::string("second")), -1);
    chain.merge(std::move(other));
    EXPECT_EQ(chain.size(), (size_t)1);  // SET+SET 合并
    EXPECT_TRUE(other.empty());  // merge 后源链被清空
}

EMF_TEST(NotificationChain_Merge_Vector_AddRemoveCancels) {
    TestNotifier n;
    TestEObject obj;
    emf::common::NotificationChain chain;
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::ADD, &n, nullptr, -1,
        std::any(), std::any(static_cast<emf::common::EObject*>(&obj)), 0));
    std::vector<emf::common::Notification> other;
    other.emplace_back(
        emf::common::Notification::EventType::REMOVE, &n, nullptr, -1,
        std::any(static_cast<emf::common::EObject*>(&obj)), std::any(), 0);
    chain.merge(std::move(other));
    EXPECT_EQ(chain.size(), (size_t)0);  // ADD+REMOVE 抵消
}

EMF_TEST(NotificationChain_Merge_Vector_DifferentNotifiers_AppendsAll) {
    TestNotifier n1, n2;
    emf::common::NotificationChain chain;
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::SET, &n1, nullptr, -1,
        std::any(), std::any(), -1));
    std::vector<emf::common::Notification> other;
    other.emplace_back(
        emf::common::Notification::EventType::SET, &n2, nullptr, -1,
        std::any(), std::any(), -1);
    chain.merge(std::move(other));
    EXPECT_EQ(chain.size(), (size_t)2);  // 不同 notifier，不合并
}

// ===== Gap 6: NotificationChain.merge() + dispatch 派发合并后的通知 =====
EMF_TEST(NotificationChain_Merge_ThenDispatch_DeliversMerged) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);
    n.eSetDeliver(true);
    emf::common::NotificationChain chain;
    chain.add(emf::common::Notification(
        emf::common::Notification::EventType::SET, &n, nullptr, -1,
        std::any(std::string("old1")), std::any(std::string("v1")), -1));
    std::vector<emf::common::Notification> other;
    other.emplace_back(
        emf::common::Notification::EventType::SET, &n, nullptr, -1,
        std::any(std::string("v1")), std::any(std::string("v2")), -1);
    chain.merge(std::move(other));
    EXPECT_EQ(chain.size(), (size_t)1);  // 合并为 1 条
    chain.dispatch();
    EXPECT_EQ(a.notifyCount, 1);  // 只派发 1 条合并后的通知
    EXPECT_TRUE(std::any_cast<std::string>(a.received[0].oldValue()) == std::string("old1"));
    EXPECT_TRUE(std::any_cast<std::string>(a.received[0].newValue()) == std::string("v2"));
}

// ===== A2: Notification wasSet 字段 =====
EMF_TEST(Notification_WasSet_DefaultFalse) {
    TestNotifier n;
    emf::common::Notification n1(emf::common::Notification::EventType::SET, &n,
                                  nullptr, -1, std::any(), std::any(), -1);
    EXPECT_FALSE(n1.wasSet());
}

EMF_TEST(Notification_WasSet_PassedThroughConstructor) {
    TestNotifier n;
    emf::common::Notification n1(emf::common::Notification::EventType::SET, &n,
                                  nullptr, -1, std::any(), std::any(), -1, true);
    EXPECT_TRUE(n1.wasSet());
    n1.setWasSet(false);
    EXPECT_FALSE(n1.wasSet());
}

// ===== C2/I3: setEContainer 发反向 ADD/REMOVE 通知 =====
// 对齐 Java BasicEObjectImpl.eBasicSetContainer：child 侧 adapter 收到 container 变化。
// 反向通知 feature=nullptr（区别于正向 containment ADD 通知）。
EMF_TEST(EObjectImpl_SetEContainer_FiresReverseAdd) {
    TestEObject parent;
    TestEObject child;
    RecordingAdapter a;
    child.addAdapter(&a);
    child.setEContainer(&parent);
    EXPECT_EQ(a.notifyCount, 1);
    EXPECT_TRUE(a.received[0].eventType() == Notification::EventType::ADD);
    EXPECT_EQ(std::any_cast<emf::common::EObject*>(a.received[0].newValue()), &parent);
    EXPECT_NULL(a.received[0].feature());  // 反向通知 feature=nullptr
}

EMF_TEST(EObjectImpl_SetEContainer_Switch_FiresRemoveThenAdd) {
    TestEObject parent1, parent2;
    TestEObject child;
    RecordingAdapter a;
    child.addAdapter(&a);
    child.setEContainer(&parent1);  // ADD(parent1)
    child.setEContainer(&parent2);  // REMOVE(parent1) + ADD(parent2)
    EXPECT_EQ(a.notifyCount, 3);
    // 第 2 条是 REMOVE(parent1)
    EXPECT_TRUE(a.received[1].eventType() == Notification::EventType::REMOVE);
    EXPECT_EQ(std::any_cast<emf::common::EObject*>(a.received[1].oldValue()), &parent1);
    // 第 3 条是 ADD(parent2)
    EXPECT_TRUE(a.received[2].eventType() == Notification::EventType::ADD);
    EXPECT_EQ(std::any_cast<emf::common::EObject*>(a.received[2].newValue()), &parent2);
}

EMF_TEST(EObjectImpl_SetEContainer_SameContainer_NoNotification) {
    TestEObject parent;
    TestEObject child;
    RecordingAdapter a;
    child.addAdapter(&a);
    child.setEContainer(&parent);
    EXPECT_EQ(a.notifyCount, 1);
    child.setEContainer(&parent);  // 相同 container，不发通知
    EXPECT_EQ(a.notifyCount, 1);
}
