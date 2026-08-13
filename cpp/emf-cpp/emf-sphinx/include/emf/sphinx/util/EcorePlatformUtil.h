// EcorePlatformUtil.h
// 对齐 Java org.eclipse.sphinx.emf.util.EcorePlatformUtil
// 在 headless C++ 环境下，把 IFile/IProject 等 Eclipse 平台类型替换为 URI/路径
#pragma once

#include <any>
#include <map>
#include <string>
#include <vector>

namespace emf::common {
class EObject;
class Resource;
class URI;
}

namespace emf::sphinx::model {
class IModelDescriptor;
}

namespace emf::sphinx::util {

class EcorePlatformUtil {
public:
    EcorePlatformUtil() = delete;

    // URI ↔ 路径
    static emf::common::URI resolveURI(const emf::common::URI& base, const std::string& relativePath);

    // 读 model namespace
    static std::string readModelNamespace(const emf::common::URI& uri);
    static std::string readTargetNamespace(const emf::common::URI& uri);

    // 加载模型根
    static emf::common::EObject* loadModelRoot(const emf::common::URI& uri);
    static emf::common::EObject* getEObject(const emf::common::URI& uri);
    static emf::common::EObject* loadEObject(const emf::common::URI& uri);

    // 资源加载
    static emf::common::Resource* loadResource(const emf::common::URI& uri);
    static bool isResourceLoaded(const emf::common::URI& uri);

    // 模型加载
    static void loadModel(emf::sphinx::model::IModelDescriptor* md);
    static bool isModelLoaded(emf::sphinx::model::IModelDescriptor* md);

    // 资源
    static emf::common::Resource* getResource(emf::common::EObject* obj);
    static emf::common::Resource* getResource(const emf::common::URI& uri);

    // 卸载
    static void unloadFile(const emf::common::URI& uri);
    static void unloadFiles(const std::vector<emf::common::URI>& uris, bool memoryOptimized);
    static void unloadAllResources();

    // 资源在 model / scope 内
    static std::vector<emf::common::Resource*> getResourcesInModel(emf::common::EObject* ctx, bool includeReferenced);
    static std::vector<emf::common::Resource*> getResourcesInModel(const emf::common::URI& ctx, bool includeReferenced);
    static std::vector<emf::common::Resource*> getResourcesInModel(emf::common::Resource* res, bool includeReferenced);
    static std::vector<emf::common::Resource*> getResourcesInScope(emf::common::EObject* ctx, bool includeReferencedScopes);
    static std::vector<emf::common::Resource*> getResourcesInScope(const emf::common::URI& uri, bool includeReferencedScopes);

    // 保存
    static void saveModel(emf::common::EObject* ctx, bool async = false);
    static void saveModel(const std::map<std::string, std::any>& options, emf::common::EObject* ctx);
};

}  // namespace emf::sphinx::util
