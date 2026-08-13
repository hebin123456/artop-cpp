// TransactionalEditingDomain.h
// 对齐 Java: org.eclipse.emf.transaction.TransactionalEditingDomain
//
// 在 AdapterFactoryEditingDomain 基础上增加：
//   1. 读写锁（shared_mutex）：runExclusive 读锁共享，runWrite 写锁独占
//   2. 通知延迟：写事务期间 eSetDeliver(false)，事务结束后批量投递累积的 Notification
//   3. 事务嵌套计数（引用计数），最外层事务结束时才提交
//
// 简化点（对齐 headless 场景，不依赖 Eclipse Job/WorkspaceRunnable）：
//   - 用 std::shared_mutex 实现读写锁（C++17）
//   - 通知延迟用 thread_local 标志 + 累积 vector（非 EMF Transaction 的完整 NotificationManager）
//   - 不实现 EMF Transaction 的 validate/privilege 约束集成（headless 无需）
#pragma once

#include "emf/edit/domain/AdapterFactoryEditingDomain.h"
#include "emf/common/Notification.h"
#include "emf/common/ENotifier.h"

#include <atomic>
#include <functional>
#include <memory>
#include <mutex>
#include <shared_mutex>
#include <vector>

// 前向声明（EditingDomain.h 用 elaborated-type-specifier 在 emf::edit 命名空间声明了
// emf::edit::ResourceSet，需显式引用 emf::common::ResourceSet）
namespace emf::common { class ResourceSet; }

namespace emf::edit {

// TransactionalEditingDomain：事务型编辑域（对齐 org.eclipse.emf.transaction.TransactionalEditingDomain）
class TransactionalEditingDomain : public AdapterFactoryEditingDomain {
public:
    TransactionalEditingDomain();
    TransactionalEditingDomain(emf::common::AdapterFactory* factory,
                               emf::common::command::CommandStack* stack = nullptr);
    ~TransactionalEditingDomain() override;

    // ===== 读写锁事务（对齐 Java TransactionalEditingDomain.runExclusive / runWritable）=====
    // runExclusive：读事务，共享读锁，只读 body，不可修改模型
    // 对齐 Java: domain.runExclusive(new Runnable() {...})
    void runExclusive(emf::common::ResourceSet* rs, std::function<void()> body);

    // runWrite：写事务，独占写锁，body 可修改模型；事务期间通知延迟，结束后批量投递
    // 对齐 Java: domain.runExclusive(new Runnable() {...}, /* isWrite */ true)
    void runWrite(emf::common::ResourceSet* rs, std::function<void()> body);

    // ===== 通知延迟（对齐 Java TransactionalEditingDomain 的通知延迟机制）=====
    // 通知累积开关：事务期间 setDeliverNotifications(false)，
    // eNotify 调用方检查此标志决定是累积还是直接投递。
    static bool isDeliverNotifications();
    static void setDeliverNotifications(bool deliver);

    // 累积通知队列（thread_local，事务期间收集）
    static std::vector<emf::common::Notification>& getPendingNotifications();
    static void addPendingNotification(const emf::common::Notification& n);
    static void flushPendingNotifications();

    // ===== 事务状态查询 =====
    bool isInTransaction() const { return transactionDepth_ > 0; }
    int getTransactionDepth() const { return transactionDepth_; }

private:
    // 读写锁（对齐 Java TransactionalEditingDomain 的读写锁）
    // shared_mutex：多读单写，对齐 Java ReentrantReadWriteLock
    std::shared_mutex rwLock_;
    int transactionDepth_ = 0;  // 嵌套事务计数（最外层提交）
};

}  // namespace emf::edit
