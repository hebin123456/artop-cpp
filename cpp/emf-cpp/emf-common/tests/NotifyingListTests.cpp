// NotifyingListTests.cpp
// 对齐 Java org.eclipse.emf.common.notify.impl.NotifyingListImpl 的单元测试
#include "test_main.h"
#include "emf/common/util/NotifyingListImpl.h"
#include "emf/common/util/NotifyingList.h"
#include "emf/common/util/BasicEList.h"
#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"

#include <any>
#include <string>
#include <vector>

using emf::common::util::NotifyingListImpl;
using emf::common::util::NotifyingList;
using emf::common::util::BasicEList;
using emf::common::Notifier;
using emf::common::EAdapter;
using emf::common::Notification;

namespace {

class RecordingAdapter : public EAdapter {
public:
    std::vector<Notification> events;
    void notifyChanged(const Notification& n) override {
        events.push_back(n);
    }
};

class TestNotifier : public Notifier {
public:
    int* getRef() { return &counter_; }
private:
    int counter_ = 0;
};

template <typename E>
class ActiveList : public NotifyingListImpl<E> {
public:
    ActiveList() = default;
    explicit ActiveList(TestNotifier* n) : notifier_(n) {}

    Notifier* getNotifier() override { return notifier_; }
    const Notifier* getNotifier() const override { return notifier_; }
    const void* getFeature() const override { return &feature_; }
    int getFeatureID() const override { return 42; }
    bool isNotificationRequired() const override { return true; }

private:
    TestNotifier* notifier_ = nullptr;
    int feature_ = 0;
};

template <typename E>
class PassiveList : public NotifyingListImpl<E> {
public:
    PassiveList() = default;
};

}  // namespace

EMF_TEST(NotifyingList_BasicAddRemove) {
    PassiveList<int> list;
    list.add(1);
    list.add(2);
    list.add(3);
    EXPECT_EQ(list.size(), 3);
    EXPECT_EQ(list.get(0), 1);
    EXPECT_EQ(list.get(2), 3);
    int r = list.remove(1);
    EXPECT_EQ(r, 2);
    EXPECT_EQ(list.size(), 2);
}

EMF_TEST(NotifyingList_DispatchAddNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<int> list(&n);
    list.add(100);

    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::ADD);
    EXPECT_EQ(a.events[0].position(), 0);
    EXPECT_EQ(std::any_cast<int>(a.events[0].newValue()), 100);
}

EMF_TEST(NotifyingList_DispatchAddAtIndexNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<int> list(&n);
    list.add(1);
    list.add(2);
    a.events.clear();

    list.add(1, 99);

    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::ADD);
    EXPECT_EQ(a.events[0].position(), 1);
    EXPECT_EQ(std::any_cast<int>(a.events[0].newValue()), 99);
}

EMF_TEST(NotifyingList_DispatchRemoveNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<std::string> list(&n);
    list.add(std::string("hello"));
    list.add(std::string("world"));
    a.events.clear();

    std::string r = list.remove(0);
    EXPECT_EQ(r, "hello");
    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::REMOVE);
    EXPECT_EQ(a.events[0].position(), 0);
    EXPECT_EQ(std::any_cast<std::string>(a.events[0].oldValue()), std::string("hello"));
}

EMF_TEST(NotifyingList_DispatchSetNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<int> list(&n);
    list.add(1);
    list.add(2);
    a.events.clear();

    int old = list.set(1, 200);
    EXPECT_EQ(old, 2);
    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::SET);
    EXPECT_EQ(a.events[0].position(), 1);
    EXPECT_EQ(std::any_cast<int>(a.events[0].oldValue()), 2);
    EXPECT_EQ(std::any_cast<int>(a.events[0].newValue()), 200);
}

EMF_TEST(NotifyingList_DispatchMoveNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<int> list(&n);
    list.add(10);
    list.add(20);
    list.add(30);
    a.events.clear();

    int moved = list.move(0, 2);
    EXPECT_EQ(moved, 30);
    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::MOVE);
    EXPECT_EQ(a.events[0].position(), 0);
    EXPECT_EQ(std::any_cast<int>(a.events[0].newValue()), 30);
}

EMF_TEST(NotifyingList_DispatchClearNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<int> list(&n);
    list.add(1);
    list.add(2);
    list.add(3);
    a.events.clear();

    list.clear();
    EXPECT_TRUE(list.isEmpty());
    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::REMOVE_MANY);
}

EMF_TEST(NotifyingList_NoNotificationWhenNotRequired) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    PassiveList<int> list;
    list.add(1);
    list.add(2);
    list.add(3);
    list.remove(0);
    list.set(0, 99);
    list.clear();
    EXPECT_EQ((int)a.events.size(), 0);
}

EMF_TEST(NotifyingList_DefaultGetNotifierIsNull) {
    PassiveList<int> list;
    EXPECT_NULL(list.getNotifier());
    EXPECT_EQ(list.getFeatureID(), -1);
}

EMF_TEST(NotifyingList_DispatchAddAllManyNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<int> list(&n);
    std::vector<int> data = {7, 8, 9};
    list.addAll(data);

    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::ADD_MANY);
    EXPECT_EQ(a.events[0].position(), 0);
    auto vec = std::any_cast<std::vector<int>>(a.events[0].newValue());
    EXPECT_EQ((int)vec.size(), 3);
    EXPECT_EQ(vec[0], 7);
    EXPECT_EQ(vec[2], 9);
}

EMF_TEST(NotifyingList_DispatchAddAllSingleNotification) {
    TestNotifier n;
    RecordingAdapter a;
    n.addAdapter(&a);

    ActiveList<int> list(&n);
    std::vector<int> data = {42};
    list.addAll(data);

    EXPECT_EQ((int)a.events.size(), 1);
    EXPECT_EQ((int)a.events[0].eventType(), (int)Notification::EventType::ADD);
    EXPECT_EQ(std::any_cast<int>(a.events[0].newValue()), 42);
}
