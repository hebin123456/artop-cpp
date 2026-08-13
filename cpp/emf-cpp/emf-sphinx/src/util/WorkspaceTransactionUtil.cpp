// WorkspaceTransactionUtil.cpp - 对齐 Java org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil
// headless C++ 版本：委托给 TransactionalEditingDomain 实现读写锁事务
#include "emf/sphinx/util/WorkspaceTransactionUtil.h"
#include "emf/edit/domain/TransactionalEditingDomain.h"
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include <stdexcept>

namespace emf::sphinx::util {

namespace {
// headless 场景下用单例 TransactionalEditingDomain 承载事务（对齐 Java Sphinx 的全局 editing domain）
// 注意：Java Sphinx 通过 WorkspaceEditingDomainUtil.getEditingDomain(resource) 获取，
// C++ headless 简化为进程级单例（避免 Eclipse Workspace 依赖）。
emf::edit::TransactionalEditingDomain& sharedTransactionalDomain() {
    static emf::edit::TransactionalEditingDomain domain;
    return domain;
}
}  // namespace

// 读取事务：共享读锁，只读 body
void WorkspaceTransactionUtil::runExclusive(emf::common::ResourceSet* rs, std::function<void()> body) {
    sharedTransactionalDomain().runExclusive(rs, std::move(body));
}

// 写入事务：独占写锁 + 通知延迟
void WorkspaceTransactionUtil::runWrite(emf::common::ResourceSet* rs, std::function<void()> body) {
    sharedTransactionalDomain().runWrite(rs, std::move(body));
}

}  // namespace emf::sphinx::util
