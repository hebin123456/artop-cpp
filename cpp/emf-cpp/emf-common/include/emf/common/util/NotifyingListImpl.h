// NotifyingListImpl.h
// 对齐 Java org.eclipse.emf.common.notify.impl.NotifyingListImpl
#pragma once

#include "emf/common/util/BasicEList.h"
#include "emf/common/util/NotifyingList.h"
#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"

#include <any>
#include <stdexcept>
#include <vector>

namespace emf::common::util {

template <typename E>
class NotifyingListImpl : public BasicEList<E>, public NotifyingList<E> {
public:
    static constexpr int NO_FEATURE_ID = -1;
    static constexpr int NO_INDEX = -1;

    NotifyingListImpl() = default;
    explicit NotifyingListImpl(int initialCapacity) : BasicEList<E>(initialCapacity) {}
    explicit NotifyingListImpl(const std::vector<E>& collection) : BasicEList<E>(collection) {}

    emf::common::Notifier* getNotifier() override { return nullptr; }
    const emf::common::Notifier* getNotifier() const override { return nullptr; }
    const void* getFeature() const override { return nullptr; }
    int getFeatureID() const override { return NO_FEATURE_ID; }

    virtual bool isNotificationRequired() const { return false; }
    virtual bool isSet() const { return !this->isEmpty(); }
    virtual void unset() { this->clear(); }

    virtual void dispatchNotification(const emf::common::Notification& notification) {
        auto* n = this->getNotifier();
        if (n) n->eNotify(notification);
    }

    emf::common::Notification createNotification(emf::common::Notification::EventType type,
                                                   std::any oldValue,
                                                   std::any newValue,
                                                   int index,
                                                   bool /*wasSet*/) {
        return emf::common::Notification(
            type, this->getNotifier(), nullptr, this->getFeatureID(),
            std::move(oldValue), std::move(newValue), index);
    }

    void addUnique(E object) override {
        if (isNotificationRequired()) {
            int index = this->size_;
            bool oldIsSet = isSet();
            BasicEList<E>::addUnique(object);
            auto n = createNotification(emf::common::Notification::EventType::ADD,
                                        std::any{}, std::any{object}, index, oldIsSet);
            dispatchNotification(n);
        } else {
            BasicEList<E>::addUnique(object);
        }
    }

    void addUnique(int index, E object) override {
        if (isNotificationRequired()) {
            bool oldIsSet = isSet();
            BasicEList<E>::addUnique(index, object);
            auto n = createNotification(emf::common::Notification::EventType::ADD,
                                        std::any{}, std::any{object}, index, oldIsSet);
            dispatchNotification(n);
        } else {
            BasicEList<E>::addUnique(index, object);
        }
    }

    bool addAllUnique(const std::vector<E>& collection) override {
        return addAllUnique(this->size_, collection);
    }

    bool addAllUnique(int index, const std::vector<E>& collection) override {
        int growth = static_cast<int>(collection.size());
        if (growth == 0) return false;
        if (isNotificationRequired()) {
            bool oldIsSet = isSet();
            BasicEList<E>::addAllUnique(index, collection);
            emf::common::Notification::EventType type =
                (growth == 1) ? emf::common::Notification::EventType::ADD
                              : emf::common::Notification::EventType::ADD_MANY;
            std::any newValue;
            if (growth == 1) {
                newValue = std::any{collection[static_cast<std::size_t>(0)]};
            } else {
                std::vector<E> copy = collection;
                newValue = std::any{std::move(copy)};
            }
            auto n = createNotification(type, std::any{}, std::move(newValue), index, oldIsSet);
            dispatchNotification(n);
        } else {
            BasicEList<E>::addAllUnique(index, collection);
        }
        return true;
    }

    bool addAllUnique(const std::vector<E>& objects, int start, int end) override {
        return addAllUnique(this->size_, objects, start, end);
    }

    bool addAllUnique(int index, const std::vector<E>& objects, int start, int end) override {
        int growth = end - start;
        if (growth == 0) return false;
        if (isNotificationRequired()) {
            bool oldIsSet = isSet();
            BasicEList<E>::addAllUnique(index, objects, start, end);
            emf::common::Notification::EventType type =
                (growth == 1) ? emf::common::Notification::EventType::ADD
                              : emf::common::Notification::EventType::ADD_MANY;
            std::any newValue;
            if (growth == 1) {
                newValue = std::any{objects[static_cast<std::size_t>(start)]};
            } else {
                std::vector<E> sub(objects.begin() + start, objects.begin() + end);
                newValue = std::any{std::move(sub)};
            }
            auto n = createNotification(type, std::any{}, std::move(newValue), index, oldIsSet);
            dispatchNotification(n);
        } else {
            BasicEList<E>::addAllUnique(index, objects, start, end);
        }
        return true;
    }

    E remove(int index) override {
        if (isNotificationRequired()) {
            bool oldIsSet = isSet();
            E oldObject = BasicEList<E>::remove(index);
            auto n = createNotification(emf::common::Notification::EventType::REMOVE,
                                        std::any{oldObject}, std::any{}, index, oldIsSet);
            dispatchNotification(n);
            return oldObject;
        } else {
            return BasicEList<E>::remove(index);
        }
    }

    void clear() override {
        if (isNotificationRequired()) {
            bool oldIsSet = isSet();
            int collectionSize = this->size_;
            std::vector<E> oldData = this->data_;
            BasicEList<E>::clear();
            emf::common::Notification::EventType type =
                (collectionSize == 1) ? emf::common::Notification::EventType::REMOVE
                                      : emf::common::Notification::EventType::REMOVE_MANY;
            std::any oldValue;
            if (collectionSize == 1) {
                oldValue = std::any{oldData[0]};
            } else {
                oldValue = std::any{std::move(oldData)};
            }
            auto n = createNotification(type, std::move(oldValue), std::any{}, NO_INDEX, oldIsSet);
            dispatchNotification(n);
        } else {
            BasicEList<E>::clear();
        }
    }

    E setUnique(int index, E object) override {
        if (isNotificationRequired()) {
            bool oldIsSet = isSet();
            E oldObject = BasicEList<E>::setUnique(index, object);
            auto n = createNotification(emf::common::Notification::EventType::SET,
                                        std::any{oldObject}, std::any{object}, index, oldIsSet);
            dispatchNotification(n);
            return oldObject;
        } else {
            return BasicEList<E>::setUnique(index, object);
        }
    }

    E move(int targetIndex, int sourceIndex) override {
        if (isNotificationRequired()) {
            bool oldIsSet = isSet();
            E object = BasicEList<E>::move(targetIndex, sourceIndex);
            std::any oldValue = std::any{sourceIndex};
            std::any newValue = std::any{object};
            auto n = createNotification(emf::common::Notification::EventType::MOVE,
                                        std::move(oldValue), std::move(newValue), targetIndex, oldIsSet);
            dispatchNotification(n);
            return object;
        } else {
            return BasicEList<E>::move(targetIndex, sourceIndex);
        }
    }
};

}  // namespace emf::common::util
