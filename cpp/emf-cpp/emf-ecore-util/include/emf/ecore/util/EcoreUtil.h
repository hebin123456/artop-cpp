// EcoreUtil.h —— Ecore 工具集核心
// 对齐 Java: org.eclipse.emf.ecore.util.EcoreUtil
//
// 提供 EMF 最常用的工具方法：
//   - equals: 深度比较两个 EObject
//   - copy / copyAll: 深拷贝 EObject 树（通过内部 Copier）
//   - delete: 删除 EObject（从 eContainer 中移除）
//   - getAllContents: 深度遍历所有内容
//   - resolve / resolveAll: 解析代理对象
//   - getURI / getID / setID: 对象 URI / ID 管理
//   - setEList: 模板化列表设置（在 EcoreUtil.tcc）
#pragma once

#include "emf/common/EObject.h"
#include "emf/common/URI.h"
#include "emf/common/EList.h"

#include <any>
#include <memory>
#include <string>
#include <vector>
#include <functional>
#include <unordered_map>
#include <unordered_set>
#include <utility>

namespace emf::ecore {
class EClass;
class EStructuralFeature;
class EReference;
class EAttribute;
class EFactory;
class EPackage;
class EDataType;
class EClassifier;
class EAnnotation;
}  // namespace emf::ecore

namespace emf::common {
class Resource;
class ResourceSet;
}

namespace emf::ecore::util {

// TreeIterator 包装：深度优先遍历 EObject 的所有内容
// 对齐 Java EcoreUtil.getAllContents(EObject)
class AllContentsIterator : public emf::common::TreeIterator<emf::common::EObject*> {
public:
    // 从 root 开始深度优先遍历 eContents()
    explicit AllContentsIterator(emf::common::EObject* root);
    bool hasNext() override;
    emf::common::EObject* next() override;

private:
    void pushChildren(emf::common::EObject* obj);
    std::vector<emf::common::EObject*> stack_;
    size_t index_ = 0;
};

// Copier：深度复制 EObject 树
// 对齐 Java EcoreUtil.Copier（内部类）
class Copier {
public:
    Copier() = default;
    ~Copier() = default;

    // 复制单个 EObject（含所有 containment 子对象）
    // 对齐 Java EcoreUtil.copier.copy(EObject)
    emf::common::EObject* copy(emf::common::EObject* eObject);

    // 批量复制（对齐 Java Copier.copyAll(EList)）
    std::vector<emf::common::EObject*> copyAll(const std::vector<emf::common::EObject*>& objects);

    // 复制完成后，解析引用（对齐 Java Copier.copyReferences()）
    // 在所有对象复制完成后调用，以更新非 containment 引用，使其指向复制后的对象
    void copyReferences();

    // 获取源 -> 副本 映射
    emf::common::EObject* get(emf::common::EObject* source) const;

private:
    // 源对象 -> 副本对象 映射
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> sourceToCopy_;
    // 待解析引用的源对象列表（用于 copyReferences 阶段）
    std::vector<emf::common::EObject*> copiedSources_;
};

// EcoreUtil：静态工具方法集合
// 对齐 org.eclipse.emf.ecore.util.EcoreUtil
class EcoreUtil {
public:
    // ===== 对象比较 =====
    // 深度比较两个 EObject：对 containment EReference 递归比较子树（带循环保护），
    // 对非 containment EReference 做指针/proxy 比较，对 EAttribute 调 equalsValue。
    // 对齐 Java EcoreUtil.equals(EObject, EObject)（内部委托 EqualityHelper 实现）
    static bool equals(emf::common::EObject* a, emf::common::EObject* b);

    // 比较两个 any 值：基础类型（string/int/long/double/float/bool）、
    // EObject*（proxy 按 eProxyURI 比较，非 proxy 指针身份）、多值 EAttribute（EList<T>*）、
    // 多值 EReference（vector<EObject*>*/EList<EObject*>*，元素指针/proxy 比较）。
    // 对齐 Java EqualityHelper.haveEqualValue 的值比较语义
    static bool equalsValue(const std::any& a, const std::any& b);

    // ===== 复制 / 删除 =====
    // 深拷贝单个对象（使用默认 Copier）
    // 对齐 Java EcoreUtil.copy(EObject)
    static emf::common::EObject* copy(emf::common::EObject* eObject);

    // 批量深拷贝
    // 对齐 Java EcoreUtil.copyAll(EList)
    static std::vector<emf::common::EObject*> copyAll(
        const std::vector<emf::common::EObject*>& objects);

    // 删除对象：从其 eContainer 的 containment feature 中移除
    // 对齐 Java EcoreUtil.delete(EObject)
    static void remove(emf::common::EObject* eObject);

    // ===== 遍历 =====
    // 深度遍历所有内容（对齐 Java EcoreUtil.getAllContents(EObject)）
    static std::unique_ptr<emf::common::TreeIterator<emf::common::EObject*>>
    getAllContents(emf::common::EObject* eObject);

    // ===== 代理解析 =====
    // 解析单个代理对象（对齐 Java EcoreUtil.resolve(EObject, ResourceSet)）
    static emf::common::EObject* resolve(
        emf::common::EObject* proxy,
        emf::common::ResourceSet* resourceSet);

    // 解析对象树中所有代理（对齐 Java EcoreUtil.resolveAll(EObject)）
    static void resolveAll(emf::common::EObject* eObject);

    // ===== URI / ID =====
    // 获取对象的 URI（对齐 Java EcoreUtil.getURI(EObject)）
    // 优先使用 resource 内的 ID/fragment，否则用 eContainer 路径
    static emf::common::URI getURI(emf::common::EObject* eObject);

    // 获取对象的 ID（对齐 Java EcoreUtil.getID(EObject)）
    static std::string getID(emf::common::EObject* eObject);

    // 设置对象的 ID（对齐 Java EcoreUtil.setID(EObject, String)）
    static void setID(emf::common::EObject* eObject, const std::string& id);

    // ===== 类型工具 =====
    // 检查对象是否为指定 EClass 的实例（含继承）
    // 对齐 Java EcoreUtil.isAncestor(EClass, EObject)
    static bool isAncestor(emf::ecore::EClass* ancestorEClass,
                           emf::common::EObject* eObject);

    // 查找 EClass 在 EPackage 中的实例
    // 对齐 Java EcoreUtil.getEClassifier(EPackage, String)
    static emf::ecore::EDataType* getEClassifier(
        emf::ecore::EPackage* ePackage, const std::string& name);

    // ===== 字符串/值转换 =====
    // 将字符串转为指定 EDataType 的值
    // 对齐 Java EcoreUtil.createFromString(EDataType, String)
    static std::any createFromString(emf::ecore::EDataType* eDataType,
                                     const std::string& literal);

    // 将值转为字符串表示
    // 对齐 Java EcoreUtil.convertToString(EDataType, Object)
    static std::string convertToString(emf::ecore::EDataType* eDataType,
                                       const std::any& value);

    // ===== 模板工具（在 EcoreUtil.tcc 中实现）=====
    template <typename T>
    static void setEList(emf::common::EList<T>* eList,
                         const std::vector<T>& prototypeCollection);

    // ===== 补齐对齐 Java EcoreUtil 的常用 API =====

    // 获取根容器（沿 eContainer 链向上直到无容器）
    // 对齐 Java EcoreUtil.getRootContainer(EObject)
    static emf::common::EObject* getRootContainer(emf::common::EObject* eObject);

    // 检查 ancestorEObject 是否为 eObject 的祖先（EObject 重载）
    // 对齐 Java EcoreUtil.isAncestor(EObject, EObject)
    static bool isAncestor(emf::common::EObject* ancestorEObject,
                           emf::common::EObject* eObject);

    // 创建指定 EClass 的实例（通过 EPackage 的 EFactory）
    // 对齐 Java EcoreUtil.create(EClass)
    static emf::common::EObject* create(emf::ecore::EClass* eClass);

    // 设置对象的 feature 值（反射 eSet 包装）
    // 对齐 Java EcoreUtil.set(EObject, EStructuralFeature, Object)
    static void set(emf::common::EObject* eObject,
                    emf::ecore::EStructuralFeature* feature,
                    const std::any& value);

    // 删除对象（递归从 containment 树移除）
    // 对齐 Java EcoreUtil.delete(EObject, boolean recursive)
    static void deleteObject(emf::common::EObject* eObject, bool recursive = true);

    // 从对象的指定 feature 移除值
    // 对齐 Java EcoreUtil.remove(EObject, EStructuralFeature, Object)
    static bool remove(emf::common::EObject* eObject,
                       emf::ecore::EStructuralFeature* feature,
                       const std::any& value);

    // 替换对象 feature 中的旧值为新值
    // 对齐 Java EcoreUtil.replace(EObject, EStructuralFeature, Object, Object)
    static bool replace(emf::common::EObject* eObject,
                        emf::ecore::EStructuralFeature* feature,
                        const std::any& oldValue,
                        const std::any& newValue);

    // 获取对象的 EAnnotation（按 source）
    // 对齐 Java EcoreUtil.getEAnnotation(EObject, String)
    static emf::ecore::EAnnotation* getEAnnotation(emf::common::EObject* eObject,
                                                     const std::string& source);

    // 获取包含对象的 Resource
    // 对齐 Java EcoreUtil.getContainingResource(EObject)（等价 eObject.eResource()）
    static emf::common::Resource* getContainingResource(emf::common::EObject* eObject);

    // 按 EClass 过滤集合中的对象
    // 对齐 Java EcoreUtil.getObjectsByType(Collection, EClassifier)
    static std::vector<emf::common::EObject*> getObjectsByType(
        const std::vector<emf::common::EObject*>& objects,
        emf::ecore::EClassifier* type);

    // 获取对象在容器中的 identification（URI fragment 或 ID）
    // 对齐 Java EcoreUtil.getIdentification(EObject)
    static std::string getIdentification(emf::common::EObject* eObject);
};

// EqualityHelper：深度比较 + 哈希
// 对齐 Java org.eclipse.emf.ecore.util.EqualityHelper
//
// 与 EcoreUtil::equals 的关系：EcoreUtil::equals(EObject*,EObject*) 静态方法
// 内部创建一个 EqualityHelper 实例并委托，从而获得 containment 深度比较 + 循环保护。
//
// virtual hook（对齐 Java EqualityHelper 的可扩展设计）：
//   - equals(EObject*,EObject*)：入口，委托 haveEqualFeature
//   - haveEqualFeature：遍历 eAllStructuralFeatures 分发到 haveEqualReference/haveEqualAttribute
//   - haveEqualReference：containment 递归 haveEqualObject（带循环保护）；
//     非 containment 做指针/proxy 比较
//   - haveEqualAttribute：委托 equalsValue
//   - haveEqualObject：循环保护 + 递归 haveEqualFeature
class EqualityHelper {
public:
    EqualityHelper() = default;
    virtual ~EqualityHelper() = default;

    // 比较 std::any 包装的值（委托 EcoreUtil::equalsValue，处理基础类型/多值 EAttribute/EObject*）
    bool equals(const std::any& a, const std::any& b);
    // 深度比较两个 EObject（containment 递归，带循环保护）
    // 对齐 Java EqualityHelper.equals(EObject,EObject)
    virtual bool equals(emf::common::EObject* a, emf::common::EObject* b);

    // 遍历 eAllStructuralFeatures 比较 a/b 的所有 feature（对齐 Java haveEqualFeature）
    virtual bool haveEqualFeature(emf::common::EObject* a, emf::common::EObject* b);
    // 比较 EReference：containment 递归 haveEqualObject；非 containment 指针/proxy 比较
    virtual bool haveEqualReference(emf::common::EObject* a, emf::common::EObject* b,
                                     emf::ecore::EReference* ref);
    // 比较 EAttribute：委托 equalsValue（对齐 Java haveEqualAttribute）
    virtual bool haveEqualAttribute(emf::common::EObject* a, emf::common::EObject* b,
                                     emf::ecore::EAttribute* attr);
    // 递归比较两个 EObject（带循环保护），对齐 Java EqualityHelper.haveEqualObject
    virtual bool haveEqualObject(emf::common::EObject* a, emf::common::EObject* b);

    bool equalsFeatureValue(emf::common::EObject* a,
                            emf::common::EObject* b,
                            emf::ecore::EStructuralFeature* f);

    double hashCode(const std::any& v);
    double hashCode(emf::common::EObject* obj);

private:
    // 循环保护：记录已在比较中的对象对，避免 containment 自环/互环导致无限递归
    // 对齐 Java EqualityHelper 内部用 Set<EObject> 判重（这里用 pair 处理 a/b 配对）
    struct ObjPairHash {
        std::size_t operator()(const std::pair<emf::common::EObject*, emf::common::EObject*>& p) const noexcept {
            // 简单组合 hash：对齐 Java Objects.hash 风格
            return std::hash<emf::common::EObject*>{}(p.first) ^
                   (std::hash<emf::common::EObject*>{}(p.second) << 1);
        }
    };
    std::unordered_set<std::pair<emf::common::EObject*, emf::common::EObject*>, ObjPairHash> visited_;
};

}  // namespace emf::ecore::util

#include "EcoreUtil.tcc"
