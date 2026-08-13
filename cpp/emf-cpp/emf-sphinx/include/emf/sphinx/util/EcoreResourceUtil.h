// EcoreResourceUtil.h
// 对齐 Java org.eclipse.sphinx.emf.util.EcoreResourceUtil
// 负责 resource 加载、保存、URI 转换等工具方法
#pragma once

#include <any>
#include <map>
#include <string>
#include <vector>
#include <memory>
#include <unordered_map>

namespace emf::common {
class EObject;
class Resource;
class ResourceSet;
class URI;
class URIConverter;
}

namespace emf::sphinx::util {

class EcoreResourceUtil {
public:
    EcoreResourceUtil() = delete;

    // 取得 URIConverter（应用 workspace → platform:/resource 映射）
    static emf::common::URIConverter* getURIConverter(emf::common::ResourceSet* rs = nullptr);

    // 转绝对文件 URI
    static emf::common::URI convertToAbsoluteFileURI(const emf::common::URI& uri);

    // 转 platform resource URI
    static emf::common::URI convertToPlatformResourceURI(const emf::common::URI& uri);

    // URI 是否存在
    static bool exists(const emf::common::URI& uri);

    // URI 是否为 EMF 模型
    static bool isEMFModelURI(const emf::common::URI& uri);

    // 取 EObject 的 URI
    static emf::common::URI getURI(emf::common::EObject* obj, bool resolve = false);

    // 规范化 URI fragment
    static std::string normalizeURIFragment(emf::common::Resource* res, const std::string& fragment);

    // 读取模型 namespace / target namespace / schema location
    static std::string readModelNamespace(emf::common::Resource* res);
    static std::string readModelNamespace(emf::common::URIConverter* uc, const emf::common::URI& uri);
    static std::string readTargetNamespace(emf::common::Resource* res);
    static std::vector<std::string> readRootElementComments(emf::common::Resource* res);
    static std::map<std::string, std::string> readSchemaLocationEntries(emf::common::Resource* res);

    // 默认 load/save options
    static std::map<std::string, std::any> getDefaultLoadOptions();
    static std::map<std::string, std::any> getDefaultSaveOptions();

    // 资源加载 / 卸载
    static emf::common::Resource* loadResource(emf::common::ResourceSet* rs, const emf::common::URI& uri,
                                                const std::map<std::string, std::any>& options);
    static emf::common::EObject* loadEObject(emf::common::ResourceSet* rs, const emf::common::URI& uri);
    static emf::common::EObject* getEObject(emf::common::ResourceSet* rs, const emf::common::URI& uri);
    static emf::common::EObject* getModelRoot(emf::common::Resource* res);
    static bool isResourceLoaded(emf::common::ResourceSet* rs, const emf::common::URI& uri);
    static std::string getModelName(emf::common::EObject* obj);

    // 创建/添加/保存新模型
    static emf::common::Resource* addNewModelResource(emf::common::ResourceSet* rs, const emf::common::URI& uri,
                                                      const std::string& contentTypeId, emf::common::EObject* content);
    static void addModelResource(emf::common::ResourceSet* rs, emf::common::Resource* res);
    static void saveNewModelResource(emf::common::ResourceSet* rs, const emf::common::URI& uri,
                                      const std::string& contentTypeId, emf::common::EObject* content,
                                      const std::map<std::string, std::any>& options);
    static void saveModelResource(emf::common::Resource* res, const std::map<std::string, std::any>& options);

    // 卸载
    static void unloadResource(emf::common::Resource* res, bool memoryOptimized = false);
    static void unloadResource(emf::common::ResourceSet* rs, const emf::common::URI& uri, bool memoryOptimized = false);
};

}  // namespace emf::sphinx::util
