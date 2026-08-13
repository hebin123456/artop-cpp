// EMF Common: Notification
// 对齐 org.eclipse.emf.common.notify.Notification (Java)
#pragma once

#include <any>
#include <string>
#include <vector>

namespace emf::ecore {
class EStructuralFeature;
}  // namespace emf::ecore

namespace emf::common {

class Notifier;
class EObject;

// 复用 emf::ecore::EStructuralFeature —— EMF 的元模型在 emf::ecore 命名空间，
// 但通用通知机制在 emf::common 命名空间，所以这里借名引用。
using ::emf::ecore::EStructuralFeature;

class Notification {
public:
    enum class EventType : int {
        CREATE = 0,
        SET = 1,
        UNSET = 2,
        ADD = 3,
        REMOVE = 4,
        ADD_MANY = 5,
        REMOVE_MANY = 6,
        MOVE = 7,
        REMOVING_ADAPTER = 8,
        RESOLVE = 9,
        CONTENT_TYPE = 10
    };

    Notification(EventType type, Notifier* notifier, const EStructuralFeature* feature,
                 int featureID, std::any oldValue, std::any newValue, int position = -1,
                 bool wasSet = false)
        : type_(type), notifier_(notifier), feature_(feature), featureID_(featureID),
          oldValue_(std::move(oldValue)), newValue_(std::move(newValue)), position_(position),
          wasSet_(wasSet) {}

    EventType eventType() const { return type_; }
    Notifier* notifier() const { return notifier_; }
    const EStructuralFeature* feature() const { return feature_; }
    int featureID() const { return featureID_; }
    const std::any& oldValue() const { return oldValue_; }
    const std::any& newValue() const { return newValue_; }
    int position() const { return position_; }
    // 对齐 Java Notification.wasSet()：表示本次通知是否改变了 isSet 状态。
    // SET/UNSET 通知里 wasSet=true 表示 isSet 状态从 false→true 或 true→false；
    // EList 的 ADD/REMOVE 通知里 wasSet 反映 list 的 isSet 边沿。
    bool wasSet() const { return wasSet_; }
    void setWasSet(bool v) { wasSet_ = v; }
    bool wasTouched() const { return touched_; }
    void touch() { touched_ = true; }

    static const char* eventTypeName(EventType t) {
        switch (t) {
            case EventType::CREATE: return "CREATE";
            case EventType::SET: return "SET";
            case EventType::UNSET: return "UNSET";
            case EventType::ADD: return "ADD";
            case EventType::ADD_MANY: return "ADD_MANY";
            case EventType::REMOVE: return "REMOVE";
            case EventType::REMOVE_MANY: return "REMOVE_MANY";
            case EventType::MOVE: return "MOVE";
            case EventType::REMOVING_ADAPTER: return "REMOVING_ADAPTER";
            case EventType::RESOLVE: return "RESOLVE";
            case EventType::CONTENT_TYPE: return "CONTENT_TYPE";
        }
        return "UNKNOWN";
    }

private:
    EventType type_;
    Notifier* notifier_ = nullptr;
    const EStructuralFeature* feature_ = nullptr;
    int featureID_ = -1;
    std::any oldValue_;
    std::any newValue_;
    int position_ = -1;
    bool wasSet_ = false;
    bool touched_ = false;
};

// NotificationChain —— 通知链，累积多个 Notification 后统一 dispatch。
// 对齐 org.eclipse.emf.common.notify.impl.NotificationChainImpl (Java)
//
// 职责（对齐 Java）：
//   - add(Notification)：累积通知；对同 notifier 同 feature 的连续 SET 做合并（保留最后一个，旧值取最早）
//   - dispatch()：遍历累积的通知，逐个调 notifier->eNotify(n)
//   - 析构时若未 dispatch，自动 dispatch（防泄漏，对齐 Java NotificationChainImpl 析构行为）
//
// 现状：此前仅 typedef std::vector<Notification>，eInverseAdd/eInverseRemove 累积的通知被丢弃。
// 本类替代该 typedef，使双向 EReference 的反向端通知能正确发射。
class NotificationChain {
public:
    NotificationChain() = default;
    ~NotificationChain() { dispatch(); }

    NotificationChain(const NotificationChain&) = delete;
    NotificationChain& operator=(const NotificationChain&) = delete;
    NotificationChain(NotificationChain&& o) noexcept
        : notifications_(std::move(o.notifications_)), dispatched_(o.dispatched_) {
        o.dispatched_ = true;
    }
    NotificationChain& operator=(NotificationChain&& o) noexcept {
        if (this != &o) {
            dispatch();
            notifications_ = std::move(o.notifications_);
            dispatched_ = o.dispatched_;
            o.dispatched_ = true;
        }
        return *this;
    }

    // 累积一条通知。对齐 Java NotificationChainImpl.add 的合并语义：
    //   - SET + SET（同 notifier/feature）→ 合并，保留最早 oldValue，更新 newValue
    //   - ADD + REMOVE（同 notifier/feature/position/对象）→ 抵消为 no-op
    //   - 其余 → 追加
    void add(Notification n) {
        dispatched_ = false;
        if (!notifications_.empty()) {
            auto& last = notifications_.back();
            // SET + SET 合并：保留最早 oldValue，更新 newValue
            if (n.eventType() == Notification::EventType::SET &&
                last.eventType() == Notification::EventType::SET &&
                last.notifier() == n.notifier() && last.feature() == n.feature()) {
                last = Notification(Notification::EventType::SET,
                                    n.notifier(), n.feature(), n.featureID(),
                                    std::move(const_cast<std::any&>(last.oldValue())),
                                    std::any(n.newValue()), n.position());
                return;
            }
            // ADD + REMOVE 抵消：同 notifier/feature/position，且 ADD 的 newValue
            // 与 REMOVE 的 oldValue 是同一 EObject*。
            if (n.eventType() == Notification::EventType::REMOVE &&
                last.eventType() == Notification::EventType::ADD &&
                last.notifier() == n.notifier() && last.feature() == n.feature() &&
                last.position() == n.position()) {
                const std::any& addNew = last.newValue();
                const std::any& removeOld = n.oldValue();
                if (addNew.type() == removeOld.type() &&
                    addNew.type() == typeid(EObject*)) {
                    if (std::any_cast<EObject*>(addNew) ==
                        std::any_cast<EObject*>(removeOld)) {
                        notifications_.pop_back();
                        return;
                    }
                }
            }
        }
        notifications_.push_back(std::move(n));
    }

    // 合并另一条通知链（对齐 Java NotificationChainImpl.add(NotificationChain)）。
    // 逐条调 add()，复用 SET+SET 合并 / ADD+REMOVE 抵消语义。
    // other 在合并后清空（move 语义）。支持 vector<Notification> 以兼容
    // EObject::EObjectNotificationChain（typedef std::vector<Notification>）。
    void merge(std::vector<Notification> other) {
        for (auto& n : other) {
            add(std::move(n));
        }
    }

    // 派发所有累积的通知。可重复调用（第二次空操作）。
    // 实现在 ENotifier.cpp（需 Notifier 完整定义以调用 eNotify）
    void dispatch();

    bool empty() const { return notifications_.empty(); }
    size_t size() const { return notifications_.size(); }

private:
    std::vector<Notification> notifications_;
    bool dispatched_ = true;  // 初始无通知，视为已派发
};

}  // namespace emf::common
