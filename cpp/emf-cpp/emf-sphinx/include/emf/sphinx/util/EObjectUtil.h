// EObjectUtil.h
// 对齐 Java org.eclipse.sphinx.emf.util.EObjectUtil
#pragma once

#include "emf/common/ENotifier.h"  // 这里已 using ::emf::ecore::EStructuralFeature
#include <any>
#include <list>
#include <map>
#include <set>
#include <string>
#include <unordered_set>
#include <utility>
#include <vector>

namespace emf::common {
class EObject;
class EClass;
class EClassifier;
class EDataType;
class EPackage;
class EReference;
class Resource;
}

namespace emf::sphinx::model {
class IModelDescriptor;
}

namespace emf::sphinx::util {

// Java EStructuralFeature.Setting -> C++ 中的简单 pair<EObject*, EReference*>
struct InverseRefSetting {
    emf::common::EObject* source = nullptr;
    emf::common::EReference* reference = nullptr;
    int position = -1;
};

class EObjectUtil {
public:
    EObjectUtil() = delete;

    static constexpr const char* DEFAULT_EMF_MODEL_IMPLEMENTATION_PACKAGE_SUFFIX = "impl";
    static constexpr int DEPTH_ZERO = 0;
    static constexpr int DEPTH_ONE = 1;
    static constexpr int DEPTH_INFINITE = 2;

    // 查找所有实例
    static std::vector<emf::common::EObject*> getAllInstancesOf(emf::common::EObject* ctx, emf::common::EReference* ref, bool exactMatch);
    static std::vector<emf::common::EObject*> getAllInstancesOf(emf::common::Resource* res, bool exactMatch);

    // 检查某 EObject 是否 set 了任何 feature
    static bool isSet(emf::common::EObject* obj, const std::vector<emf::common::EStructuralFeature*>& ignoredFeatures = {});

    // 取反向引用
    static std::vector<InverseRefSetting> getInverseReferences(emf::common::EObject* obj, bool resolve);

    // 查找 EPackage / EClassifier
    static emf::common::EPackage* findEPackage(const std::string& className);
    static std::string getEMFModelInterfacePackageName(const std::string& packageName);
    static emf::common::EClassifier* findEClassifier(emf::common::EPackage* root, const std::string& typeName);
    static std::vector<emf::common::EClass*> findESubTypesOf(emf::common::EClass* cls, bool concreteOnly);
    static bool isAssignableFrom(emf::common::EClass* cls, const std::string& typeName);
    static std::vector<emf::common::EClassifier*> getEContainerClassifiers(emf::common::EClass* cls);
    static std::vector<emf::common::EClassifier*> getAnnotatedEClassifiers(emf::common::EPackage* pkg,
                                                                            const std::string& src, const std::string& k, const std::string& v);

    // 解析 / 找 structural feature
    static emf::common::EStructuralFeature* getEStructuralFeature(emf::common::EObject* obj, const std::string& name);
    static emf::common::EStructuralFeature* getEStructuralFeature(emf::common::EObject* obj, int featureID);

    // Data type 工具
    static emf::common::EDataType* getEDataType(const std::string& instanceClassName);
    static std::any createFromString(const std::string& instanceClassName, const std::string& literal);

    // 找 orphan
    static std::vector<emf::common::EObject*> getOrphans(emf::common::EObject* owner, emf::common::EReference* ref);

    // proxy 化/反化
    static emf::common::EObject* proxify(emf::common::EObject* obj);
    static emf::common::EObject* deproxify(emf::common::EObject* obj);
    static emf::common::EObject* createProxyFrom(emf::common::EObject* obj, emf::common::Resource* contextRes);
    static emf::common::EObject* createProxyFrom(emf::common::EObject* obj);

    // 解析所有 proxy
    static void resolveAll(emf::common::Resource* res);
};

}  // namespace emf::sphinx::util
