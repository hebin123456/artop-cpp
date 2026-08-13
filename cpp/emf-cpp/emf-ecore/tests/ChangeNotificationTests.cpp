// ChangeNotification 单元测试
// 对齐 org.eclipse.emf.common.notify.Notification / NotificationChain
// 覆盖：Notification 构造与访问器、EventType、wasTouched/touch、eventTypeName、
//       NotificationChain（std::vector<Notification>）聚合、eNotify 投递、
//       eDeliver 开关、多 adapter 广播、REMOVING_ADAPTER 隐式通知
#include "test_main.h"
#include "emf/common/Notification.h"
#include "emf/common/ENotifier.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include <any>
#include <string>
#include <vector>

using emf::common::Notification;
using emf::common::Notifier;
using emf::common::EAdapter;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EStructuralFeature;

namespace {

// 记录收到通知的 adapter
class RecordingAdapter : public EAdapter {
public:
    void notifyChanged(const Notification& n) override {
        received_.push_back(n);
    }
    int count() const { return (int)received_.size(); }
    const std::vector<Notification>& received() const { return received_; }
    const Notification* last() const {
        return received_.empty() ? nullptr : &received_.back();
    }
private:
    std::vector<Notification> received_;
};

// 暴露 protected notify() 的 Notifier 子类（用于直接测试投递，绕过 eDeliver）
class TestNotifier : public Notifier {
public:
    using Notifier::notify;
};

// 构建带一个 name attribute 的 EClass（提供 EStructuralFeature 供 Notification 引用）
EClass* makeClassWithName() {
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Notified");
    auto* name = EcoreFactory::instance().createEAttribute();
    name->setName("name");
    name->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    name->setFeatureID(0);
    cls->addEStructuralFeature(name);
    return cls;
}

}  // namespace

// ===== Notification 构造与访问器 =====

EMF_TEST(Notification_Construct_SetAccessors) {
    TestNotifier notifier;
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeClassWithName();
    auto* feature = cls->getEStructuralFeature("name");
    Notification n(Notification::EventType::SET, &notifier, feature, 0,
                   std::any{std::string{"old"}}, std::any{std::string{"new"}});
    EXPECT_EQ((int)n.eventType(), (int)Notification::EventType::SET);
    EXPECT_EQ(n.notifier(), &notifier);
    EXPECT_EQ(n.feature(), feature);
    EXPECT_EQ(n.featureID(), 0);
    EXPECT_EQ(*std::any_cast<std::string>(&n.oldValue()), std::string("old"));
    EXPECT_EQ(*std::any_cast<std::string>(&n.newValue()), std::string("new"));
    EXPECT_EQ(n.position(), -1);
}

EMF_TEST(Notification_Construct_WithPosition) {
    TestNotifier notifier;
    Notification n(Notification::EventType::ADD, &notifier, nullptr, 5,
                   std::any{}, std::any{42}, 3);
    EXPECT_EQ((int)n.eventType(), (int)Notification::EventType::ADD);
    EXPECT_EQ(n.position(), 3);
    EXPECT_EQ(n.featureID(), 5);
}

EMF_TEST(Notification_DefaultPosition_IsMinusOne) {
    Notification n(Notification::EventType::CREATE, nullptr, nullptr, -1,
                   std::any{}, std::any{});
    EXPECT_EQ(n.position(), -1);
}

// ===== EventType 覆盖 =====

EMF_TEST(Notification_EventType_Values) {
    EXPECT_EQ((int)Notification::EventType::CREATE, 0);
    EXPECT_EQ((int)Notification::EventType::SET, 1);
    EXPECT_EQ((int)Notification::EventType::UNSET, 2);
    EXPECT_EQ((int)Notification::EventType::ADD, 3);
    EXPECT_EQ((int)Notification::EventType::ADD_MANY, 5);
    EXPECT_EQ((int)Notification::EventType::REMOVE, 4);
    EXPECT_EQ((int)Notification::EventType::REMOVE_MANY, 6);
    EXPECT_EQ((int)Notification::EventType::MOVE, 7);
    EXPECT_EQ((int)Notification::EventType::REMOVING_ADAPTER, 8);
    EXPECT_EQ((int)Notification::EventType::RESOLVE, 9);
    EXPECT_EQ((int)Notification::EventType::CONTENT_TYPE, 10);
}

EMF_TEST(Notification_EventType_RoundTrip) {
    Notification::EventType types[] = {
        Notification::EventType::CREATE,
        Notification::EventType::SET,
        Notification::EventType::UNSET,
        Notification::EventType::ADD,
        Notification::EventType::ADD_MANY,
        Notification::EventType::REMOVE,
        Notification::EventType::REMOVE_MANY,
        Notification::EventType::MOVE,
        Notification::EventType::REMOVING_ADAPTER,
        Notification::EventType::RESOLVE,
        Notification::EventType::CONTENT_TYPE
    };
    for (auto t : types) {
        Notification n(t, nullptr, nullptr, -1, std::any{}, std::any{});
        EXPECT_EQ((int)n.eventType(), (int)t);
    }
}

// ===== eventTypeName =====

EMF_TEST(Notification_EventTypeName_KnownTypes) {
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::SET)),
              std::string("SET"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::ADD)),
              std::string("ADD"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::REMOVE)),
              std::string("REMOVE"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::UNSET)),
              std::string("UNSET"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::MOVE)),
              std::string("MOVE"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::CREATE)),
              std::string("CREATE"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::REMOVING_ADAPTER)),
              std::string("REMOVING_ADAPTER"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::RESOLVE)),
              std::string("RESOLVE"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::ADD_MANY)),
              std::string("ADD_MANY"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::REMOVE_MANY)),
              std::string("REMOVE_MANY"));
    EXPECT_EQ(std::string(Notification::eventTypeName(Notification::EventType::CONTENT_TYPE)),
              std::string("CONTENT_TYPE"));
}

// ===== wasTouched / touch =====

EMF_TEST(Notification_WasTouched_DefaultFalse) {
    Notification n(Notification::EventType::SET, nullptr, nullptr, 0, std::any{}, std::any{});
    EXPECT_FALSE(n.wasTouched());
}

EMF_TEST(Notification_Touch_MarksTouched) {
    Notification n(Notification::EventType::SET, nullptr, nullptr, 0, std::any{}, std::any{});
    n.touch();
    EXPECT_TRUE(n.wasTouched());
}

// ===== NotificationChain（std::vector<Notification>）聚合 =====

EMF_TEST(NotificationChain_Aggregate) {
    TestNotifier notifier;
    Notification n1(Notification::EventType::ADD, &notifier, nullptr, 1, std::any{}, std::any{});
    Notification n2(Notification::EventType::REMOVE, &notifier, nullptr, 2, std::any{}, std::any{});
    Notification n3(Notification::EventType::SET, &notifier, nullptr, 3, std::any{}, std::any{});

    std::vector<Notification> chain;
    chain.push_back(n1);
    chain.push_back(n2);
    chain.push_back(n3);

    EXPECT_EQ(chain.size(), (size_t)3);
    EXPECT_EQ((int)chain[0].eventType(), (int)Notification::EventType::ADD);
    EXPECT_EQ((int)chain[1].eventType(), (int)Notification::EventType::REMOVE);
    EXPECT_EQ((int)chain[2].eventType(), (int)Notification::EventType::SET);
    // 各 featureID 对应
    EXPECT_EQ(chain[0].featureID(), 1);
    EXPECT_EQ(chain[1].featureID(), 2);
    EXPECT_EQ(chain[2].featureID(), 3);
}

EMF_TEST(NotificationChain_Empty) {
    std::vector<Notification> chain;
    EXPECT_EQ(chain.size(), (size_t)0);
}

EMF_TEST(NotificationChain_Merge) {
    TestNotifier notifier;
    std::vector<Notification> chain1;
    chain1.push_back(Notification(Notification::EventType::ADD, &notifier, nullptr, 1,
                                  std::any{}, std::any{}));
    std::vector<Notification> chain2;
    chain2.push_back(Notification(Notification::EventType::REMOVE, &notifier, nullptr, 2,
                                  std::any{}, std::any{}));
    chain2.push_back(Notification(Notification::EventType::SET, &notifier, nullptr, 3,
                                  std::any{}, std::any{}));

    // 合并：把 chain2 追加到 chain1
    for (auto& n : chain2) chain1.push_back(n);
    EXPECT_EQ(chain1.size(), (size_t)3);
}

// ===== eNotify 投递 =====

EMF_TEST(ENotify_DeliversToAdapter) {
    TestNotifier notifier;
    RecordingAdapter adapter;
    notifier.addAdapter(&adapter);

    Notification n(Notification::EventType::SET, &notifier, nullptr, 0,
                   std::any{}, std::any{});
    notifier.eNotify(n);
    EXPECT_EQ(adapter.count(), 1);
    EXPECT_EQ((int)adapter.last()->eventType(), (int)Notification::EventType::SET);
}

EMF_TEST(ENotify_EDeliverFalse_Suppresses) {
    TestNotifier notifier;
    notifier.eSetDeliver(false);
    RecordingAdapter adapter;
    notifier.addAdapter(&adapter);

    Notification n(Notification::EventType::SET, &notifier, nullptr, 0,
                   std::any{}, std::any{});
    notifier.eNotify(n);
    EXPECT_EQ(adapter.count(), 0);
}

EMF_TEST(ENotify_EDeliverTrue_Default) {
    TestNotifier notifier;
    EXPECT_TRUE(notifier.eDeliver());  // 默认 true
}

EMF_TEST(ENotify_NoAdapters_NoOp) {
    TestNotifier notifier;
    Notification n(Notification::EventType::SET, &notifier, nullptr, 0,
                   std::any{}, std::any{});
    // 无 adapter，不应崩溃
    notifier.eNotify(n);
}

EMF_TEST(ENotify_MultipleAdapters_AllReceive) {
    TestNotifier notifier;
    RecordingAdapter a1;
    RecordingAdapter a2;
    RecordingAdapter a3;
    notifier.addAdapter(&a1);
    notifier.addAdapter(&a2);
    notifier.addAdapter(&a3);

    Notification n(Notification::EventType::ADD, &notifier, nullptr, 1,
                   std::any{}, std::any{});
    notifier.eNotify(n);
    EXPECT_EQ(a1.count(), 1);
    EXPECT_EQ(a2.count(), 1);
    EXPECT_EQ(a3.count(), 1);
}

EMF_TEST(ENotify_DuplicateAdapter_AddedOnce) {
    TestNotifier notifier;
    RecordingAdapter adapter;
    notifier.addAdapter(&adapter);
    notifier.addAdapter(&adapter);  // 重复
    EXPECT_EQ(notifier.eAdapters().size(), (size_t)1);

    Notification n(Notification::EventType::SET, &notifier, nullptr, 0,
                   std::any{}, std::any{});
    notifier.eNotify(n);
    EXPECT_EQ(adapter.count(), 1);  // 只收到一次
}

// ===== eNotificationRequired 语义（eDeliver && !eAdapters().empty()）=====
// 注：eNotificationRequired 定义在 EObject 上，Notifier 未提供；
//     此处直接验证等价语义：eDeliver() && !eAdapters().empty()
EMF_TEST(ENotify_ENotificationRequired_NoAdapters_False) {
    TestNotifier notifier;
    EXPECT_FALSE(notifier.eDeliver() && !notifier.eAdapters().empty());
}

EMF_TEST(ENotify_ENotificationRequired_WithAdapterAndDeliver_True) {
    TestNotifier notifier;
    RecordingAdapter adapter;
    notifier.addAdapter(&adapter);
    EXPECT_TRUE(notifier.eDeliver() && !notifier.eAdapters().empty());
}

EMF_TEST(ENotify_ENotificationRequired_DeliverFalse_False) {
    TestNotifier notifier;
    notifier.eSetDeliver(false);
    RecordingAdapter adapter;
    notifier.addAdapter(&adapter);
    EXPECT_FALSE(notifier.eDeliver() && !notifier.eAdapters().empty());
}

// ===== removeAdapter 触发 REMOVING_ADAPTER 隐式通知 =====

EMF_TEST(RemoveAdapter_TriggersRemovingAdapterNotification) {
    TestNotifier notifier;
    RecordingAdapter adapter;
    RecordingAdapter observer;
    notifier.addAdapter(&adapter);
    notifier.addAdapter(&observer);

    // 移除 adapter 时，observer 应收到 REMOVING_ADAPTER 通知
    notifier.removeAdapter(&adapter);
    bool foundRemoving = false;
    for (auto& n : observer.received()) {
        if (n.eventType() == Notification::EventType::REMOVING_ADAPTER) {
            foundRemoving = true;
            break;
        }
    }
    EXPECT_TRUE(foundRemoving);
    EXPECT_EQ(notifier.eAdapters().size(), (size_t)1);
}

EMF_TEST(RemoveAdapter_NotInList_NoOp) {
    TestNotifier notifier;
    RecordingAdapter adapter;
    RecordingAdapter observer;
    notifier.addAdapter(&observer);
    // adapter 不在列表中 —— 移除不应崩溃，observer 也不应收到关于 adapter 的通知
    notifier.removeAdapter(&adapter);
    EXPECT_EQ(notifier.eAdapters().size(), (size_t)1);
}

EMF_TEST(RemoveAdapter_DuringNotify_SafeIteration) {
    // 移除 adapter 期间，notify 复制 adapter 列表，避免迭代失效
    TestNotifier notifier;
    RecordingAdapter observer;
    notifier.addAdapter(&observer);
    notifier.removeAdapter(&observer);
    // observer 在被移除时仍会收到 REMOVING_ADAPTER（notify 先于 erase 调用）
    EXPECT_EQ(observer.count(), 1);
    EXPECT_EQ((int)observer.last()->eventType(),
              (int)Notification::EventType::REMOVING_ADAPTER);
    EXPECT_EQ(notifier.eAdapters().size(), (size_t)0);
}

// ===== EAdapter target 管理 =====

EMF_TEST(EAdapter_SetGetTarget) {
    TestNotifier notifier;
    RecordingAdapter adapter;  // EAdapter 是抽象类（notifyChanged 纯虚），用具体子类
    EXPECT_NULL(adapter.getTarget());
    adapter.setTarget(&notifier);
    EXPECT_EQ(adapter.getTarget(), &notifier);
}

EMF_TEST(EAdapter_IsAdapterForType_DefaultFalse) {
    RecordingAdapter adapter;  // EAdapter 抽象，用具体子类
    EXPECT_FALSE(adapter.isAdapterForType(nullptr));
}
