// TransactionalEditingDomain.cpp
// 对齐 Java: org.eclipse.emf.transaction.TransactionalEditingDomain
#include "emf/edit/domain/TransactionalEditingDomain.h"
#include "emf/common/EObject.h"
#include "emf/common/Notification.h"
#include "emf/common/ENotifier.h"
#include "emf/common/Resource.h"

#include <stdexcept>

namespace emf::edit {

// thread_local 通知延迟状态（对齐 Java TransactionalEditingDomain 的通知延迟机制）
// 事务期间 setDeliverNotifications(false)，eNotify 累积通知，事务结束后批量投递。
// 跨对象去重（对齐 Java EMF Transaction NotificationManager）：flushPendingNotifications
// 对整个累积列表做全局 SET 合并 + ADD/REMOVE 抵消（非相邻也合并），NotificationChain 仅
// 合并相邻通知，这里覆盖整个 pending 列表。headless 场景足够，无需 OSGi/Eclipse 插件。
namespace {
thread_local bool tlsDeliverNotifications = true;
thread_local std::vector<emf::common::Notification> tlsPendingNotifications;
thread_local int tlsTransactionDepth = 0;

// 通知拦截器：eNotify 在事务期间调用此函数。
// 对齐 Java EMF Transaction 的通知延迟机制：
//   - 事务期间（tlsDeliverNotifications == false）累积通知到 tlsPendingNotifications，返回 true（已处理）
//   - 非事务期间返回 false（不拦截，正常投递）
// 注册到 emf-common 的 setNotificationInterceptor()，避免 emf-common 反向依赖 emf-edit。
bool transactionInterceptor(const emf::common::Notifier* /*notifier*/,
                            const emf::common::Notification& n) {
    if (!tlsDeliverNotifications) {
        tlsPendingNotifications.push_back(n);
        return true;  // 已累积处理
    }
    return false;  // 未拦截，正常投递
}

// 静态初始化器：进程启动时注册拦截器到 emf-common。
// 利用静态变量的初始化副作用，确保拦截器在 main 之前注册（线程安全）。
struct InterceptorRegistrar {
    InterceptorRegistrar() {
        emf::common::setNotificationInterceptor(&transactionInterceptor);
    }
} gInterceptorRegistrar;
}  // namespace

TransactionalEditingDomain::TransactionalEditingDomain() = default;

TransactionalEditingDomain::TransactionalEditingDomain(emf::common::AdapterFactory* factory,
                                                       emf::common::command::CommandStack* stack)
    : AdapterFactoryEditingDomain(factory, stack) {}

TransactionalEditingDomain::~TransactionalEditingDomain() = default;

// ===== 通知延迟静态接口 =====

bool TransactionalEditingDomain::isDeliverNotifications() {
    return tlsDeliverNotifications;
}

void TransactionalEditingDomain::setDeliverNotifications(bool deliver) {
    tlsDeliverNotifications = deliver;
}

std::vector<emf::common::Notification>& TransactionalEditingDomain::getPendingNotifications() {
    return tlsPendingNotifications;
}

void TransactionalEditingDomain::addPendingNotification(const emf::common::Notification& n) {
    tlsPendingNotifications.push_back(n);
}

void TransactionalEditingDomain::flushPendingNotifications() {
    // 批量投递累积的通知（对齐 Java 事务提交时 NotificationManager 投递）。
    //
    // 跨对象去重（对齐 Java EMF Transaction NotificationManager）：对整个 pending
    // 列表做全局合并，不仅限于相邻通知（NotificationChain 只合并相邻）。
    //   - SET + SET（同 notifier/feature，非相邻也合并）→ 保留最早 oldValue + 最新 newValue
    //   - ADD + REMOVE（同 notifier/feature/position/对象）→ 抵消为 no-op
    //   - 其余事件原样保留
    // 这使事务内对同一属性的多次 SET 只投递一条通知（oldValue=最早值，newValue=最新值），
    // 对齐 Java EMF Transaction NotificationManager 的合并语义。
    auto pending = std::move(tlsPendingNotifications);
    tlsPendingNotifications.clear();

    std::vector<emf::common::Notification> coalesced;
    coalesced.reserve(pending.size());

    for (auto& n : pending) {
        using ET = emf::common::Notification::EventType;
        if (n.eventType() == ET::SET) {
            // 查找 coalesced 中是否已有同 (notifier, feature) 的 SET，有则合并
            bool merged = false;
            for (size_t i = 0; i < coalesced.size(); ++i) {
                auto& existing = coalesced[i];
                if (existing.eventType() == ET::SET &&
                    existing.notifier() == n.notifier() &&
                    existing.feature() == n.feature()) {
                    // 合并：保留 existing.oldValue（最早），更新为 n.newValue（最新）
                    existing = emf::common::Notification(
                        ET::SET, n.notifier(), n.feature(), n.featureID(),
                        std::move(const_cast<std::any&>(existing.oldValue())),
                        std::any(n.newValue()), n.position());
                    merged = true;
                    break;
                }
            }
            if (merged) continue;
            coalesced.push_back(std::move(n));
        } else if (n.eventType() == ET::REMOVE) {
            // 查找 coalesced 中是否有可抵消的 ADD（同 notifier/feature/position/对象）
            bool cancelled = false;
            for (size_t i = 0; i < coalesced.size(); ++i) {
                auto& existing = coalesced[i];
                if (existing.eventType() == ET::ADD &&
                    existing.notifier() == n.notifier() &&
                    existing.feature() == n.feature() &&
                    existing.position() == n.position()) {
                    const std::any& addNew = existing.newValue();
                    const std::any& removeOld = n.oldValue();
                    if (addNew.type() == removeOld.type() &&
                        addNew.type() == typeid(emf::common::EObject*) &&
                        std::any_cast<emf::common::EObject*>(addNew) ==
                            std::any_cast<emf::common::EObject*>(removeOld)) {
                        // 抵消：用末尾元素覆盖并移除尾部（O(1) 移除）
                        coalesced[i] = std::move(coalesced.back());
                        coalesced.pop_back();
                        cancelled = true;
                        break;
                    }
                }
            }
            if (cancelled) continue;
            coalesced.push_back(std::move(n));
        } else {
            coalesced.push_back(std::move(n));
        }
    }

    // 投递去重后的通知。先恢复 deliver=true 以避免递归累积
    // （投递期间 adapter 可能再触发 eNotify，应直接投递而非再次累积）。
    bool wasDeliver = tlsDeliverNotifications;
    tlsDeliverNotifications = true;
    for (const auto& n : coalesced) {
        auto* notifier = const_cast<emf::common::Notifier*>(n.notifier());
        if (notifier && notifier->eDeliver()) {
            notifier->eNotify(n);
        }
    }
    tlsDeliverNotifications = wasDeliver;
}

// ===== 读写锁事务 =====

void TransactionalEditingDomain::runExclusive(emf::common::ResourceSet* /*rs*/,
                                              std::function<void()> body) {
    // 读事务：共享读锁，只读 body（对齐 Java runExclusive）
    // 重入处理：若当前线程已持有写锁（tlsTransactionDepth > 0），跳过加锁
    // （写锁隐含读权限，对齐 Java ReentrantReadWriteLock 的可重入语义）
    bool needsLock = (tlsTransactionDepth == 0);
    std::shared_lock<std::shared_mutex> readLock(rwLock_, std::defer_lock);
    if (needsLock) {
        readLock.lock();
    }
    body();
}

void TransactionalEditingDomain::runWrite(emf::common::ResourceSet* /*rs*/,
                                         std::function<void()> body) {
    // 写事务：独占写锁 + 通知延迟（对齐 Java runExclusive with isWrite=true）
    // 重入处理：std::shared_mutex 不可重入，嵌套 runWrite 仅最外层加锁
    // （tlsTransactionDepth 由 runWrite 递增，>0 表示当前线程已持有写锁）
    bool needsLock = (tlsTransactionDepth == 0);
    std::unique_lock<std::shared_mutex> writeLock(rwLock_, std::defer_lock);
    if (needsLock) {
        writeLock.lock();
    }

    // 嵌套事务计数：最外层事务负责开启/提交通知延迟
    bool isOutermost = (tlsTransactionDepth == 0);
    if (isOutermost) {
        // 开启通知延迟：事务期间 eNotify 累积，不直接投递
        tlsDeliverNotifications = false;
    }
    ++tlsTransactionDepth;

    try {
        body();
    } catch (...) {
        // 异常回滚：恢复通知状态，重新投递已累积通知
        --tlsTransactionDepth;
        if (tlsTransactionDepth == 0) {
            tlsDeliverNotifications = true;
            // 异常时仍投递已发生的变更通知（对齐 Java 事务异常时的通知处理）
            flushPendingNotifications();
        }
        throw;
    }

    // 正常提交
    --tlsTransactionDepth;
    if (tlsTransactionDepth == 0) {
        // 最外层事务提交：恢复通知投递 + 批量投递累积通知
        tlsDeliverNotifications = true;
        flushPendingNotifications();
    }
}

}  // namespace emf::edit
