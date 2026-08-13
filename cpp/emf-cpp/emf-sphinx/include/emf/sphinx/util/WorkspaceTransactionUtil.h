// WorkspaceTransactionUtil.h
// 对齐 Java org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil
// headless C++ 版本：轻量级事务包装（不依赖 Eclipse IWorkspaceRunnable）
#pragma once

#include <functional>
#include <string>
#include <any>
#include <vector>

namespace emf::common {
class Resource;
class ResourceSet;
}

namespace emf::sphinx::util {

class WorkspaceTransactionUtil {
public:
    WorkspaceTransactionUtil() = delete;

    // 读取事务
    static void runExclusive(emf::common::ResourceSet* rs, std::function<void()> body);
    // 写入事务
    static void runWrite(emf::common::ResourceSet* rs, std::function<void()> body);
};

}  // namespace emf::sphinx::util
