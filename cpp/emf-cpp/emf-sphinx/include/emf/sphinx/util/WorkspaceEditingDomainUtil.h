// WorkspaceEditingDomainUtil.h
// 对齐 Java org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil
// 负责编辑域（EditingDomain）的获取与生命周期管理
#pragma once

#include <string>
#include <vector>
#include <memory>

namespace emf::common {
class EObject;
class Resource;
class ResourceSet;
class URI;
}

namespace emf::sphinx::metamodel {
class IMetaModelDescriptor;
}

namespace emf::sphinx::util {

class WorkspaceEditingDomainUtil {
public:
    WorkspaceEditingDomainUtil() = delete;

    // 取得某个 scope 根的 editing domain（uri 作为 key，headless 实现用 uri 替代 IContainer）
    static emf::common::ResourceSet* getEditingDomain(const emf::common::URI& rootUri,
                                                      emf::sphinx::metamodel::IMetaModelDescriptor* mm = nullptr);
    static emf::common::ResourceSet* getEditingDomain(const std::string& filePath);

    // 注销 editing domain
    static void disposeEditingDomain(const emf::common::URI& rootUri);
    static void disposeAll();
};

}  // namespace emf::sphinx::util
