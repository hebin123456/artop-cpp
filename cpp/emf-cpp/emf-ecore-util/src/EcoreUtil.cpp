// EcoreUtil 实现
// 对齐 Java: org.eclipse.emf.ecore.util.EcoreUtil
#include "emf/ecore/util/EcoreUtil.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/Resource.h"

#include <algorithm>
#include <sstream>
#include <stack>
#include <optional>

namespace emf::ecore::util {

using emf::common::EObject;
using emf::common::Resource;
using emf::common::ResourceSet;
using emf::common::URI;
using emf::ecore::EClass;
using emf::ecore::EStructuralFeature;
using emf::ecore::EReference;
using emf::ecore::EAttribute;
using emf::ecore::EFactory;
using emf::ecore::EPackage;
using emf::ecore::EDataType;
using emf::ecore::EClassifier;

namespace {

// 多值 EAttribute 比较辅助：若 a/b 均为 EList<T>*，逐元素值比较。
// 对齐 Java EqualityHelper 对多值 EAttribute 的逐元素比较。
// 返回 optional<bool>：
//   nullopt —— 类型不匹配，交由下一个分支尝试
//   true    —— 类型匹配且相等
//   false   —— 类型匹配但不等
// 注：通过 data() 取 const vector<T>& 访问元素，避免 EList<bool>::operator[]
// 返回 bool& 与 std::vector<bool> 代理引用（_Bit_reference）无法绑定的问题。
template <typename T>
std::optional<bool> tryEListEquals(const std::any& a, const std::any& b) {
    if (a.type() != typeid(emf::common::EList<T>*) ||
        b.type() != typeid(emf::common::EList<T>*)) {
        return std::nullopt;  // 类型不匹配，交由下一个分支尝试
    }
    auto* la = std::any_cast<emf::common::EList<T>*>(a);
    auto* lb = std::any_cast<emf::common::EList<T>*>(b);
    if (!la || !lb) return la == lb;
    if (la->size() != lb->size()) return false;
    // 用 data() 取底层 vector 引用，元素比较走 vector 的 operator==
    const auto& da = la->data();
    const auto& db = lb->data();
    for (size_t i = 0; i < da.size(); ++i) {
        if (!(da[i] == db[i])) return false;
    }
    return true;
}

}  // namespace

// ===== AllContentsIterator =====

AllContentsIterator::AllContentsIterator(EObject* root) {
    if (root) {
        pushChildren(root);
    }
}

void AllContentsIterator::pushChildren(EObject* obj) {
    // 深度优先：将子对象按逆序压栈，保证正序出栈
    const auto& contents = obj->eContents();
    for (auto it = contents.rbegin(); it != contents.rend(); ++it) {
        stack_.push_back(*it);
    }
}

bool AllContentsIterator::hasNext() {
    return index_ < stack_.size();
}

EObject* AllContentsIterator::next() {
    if (index_ >= stack_.size()) return nullptr;
    EObject* result = stack_[index_++];
    // 深度优先：将其子对象插入当前位置之后
    // 注意：为保持深度优先顺序，将其子对象插入到 index_ 位置
    const auto& contents = result->eContents();
    if (!contents.empty()) {
        stack_.insert(stack_.begin() + index_, contents.begin(), contents.end());
    }
    return result;
}

// ===== EcoreUtil 静态方法 =====

bool EcoreUtil::equals(EObject* a, EObject* b) {
    if (a == b) return true;
    if (!a || !b) return false;
    // 委托 EqualityHelper 实现 containment 深度比较 + 循环保护
    // 对齐 Java EcoreUtil.equals 内部使用 EqualityHelper
    EqualityHelper helper;
    return helper.equals(a, b);
}

bool EcoreUtil::equalsValue(const std::any& a, const std::any& b) {
    if (!a.has_value() && !b.has_value()) return true;
    if (!a.has_value() || !b.has_value()) return false;
    if (a.type() != b.type()) return false;

    // 基础类型（对齐 Java EqualityHelper 逐类型比较）
    if (a.type() == typeid(std::string)) {
        return std::any_cast<std::string>(a) == std::any_cast<std::string>(b);
    }
    if (a.type() == typeid(int)) {
        return std::any_cast<int>(a) == std::any_cast<int>(b);
    }
    if (a.type() == typeid(long)) {
        return std::any_cast<long>(a) == std::any_cast<long>(b);
    }
    if (a.type() == typeid(double)) {
        return std::any_cast<double>(a) == std::any_cast<double>(b);
    }
    if (a.type() == typeid(float)) {
        return std::any_cast<float>(a) == std::any_cast<float>(b);
    }
    if (a.type() == typeid(bool)) {
        return std::any_cast<bool>(a) == std::any_cast<bool>(b);
    }
    if (a.type() == typeid(EObject*)) {
        auto* ea = std::any_cast<EObject*>(a);
        auto* eb = std::any_cast<EObject*>(b);
        // 非包含引用：比较代理 URI 或对象身份
        if (!ea || !eb) return ea == eb;
        if (ea->eIsProxy() && eb->eIsProxy()) {
            // eProxyURI 在 EObjectImpl 中，通过 dynamic_cast 访问
            auto* ia = dynamic_cast<emf::common::EObjectImpl*>(ea);
            auto* ib = dynamic_cast<emf::common::EObjectImpl*>(eb);
            if (ia && ib) {
                return ia->eProxyURI().toString() == ib->eProxyURI().toString();
            }
        }
        return ea == eb;
    }
    // 多值 EReference：vector<EObject*>（元素按指针/proxy 身份比较）
    if (a.type() == typeid(std::vector<EObject*>)) {
        const auto& va = std::any_cast<std::vector<EObject*>>(a);
        const auto& vb = std::any_cast<std::vector<EObject*>>(b);
        if (va.size() != vb.size()) return false;
        for (size_t i = 0; i < va.size(); ++i) {
            if (!equalsValue(std::any(va[i]), std::any(vb[i]))) return false;
        }
        return true;
    }
    // 多值 EReference：EList<EObject*>*（codegen/DynamicEObject 的多值引用返回类型）
    if (a.type() == typeid(emf::common::EList<EObject*>*)) {
        auto* la = std::any_cast<emf::common::EList<EObject*>*>(a);
        auto* lb = std::any_cast<emf::common::EList<EObject*>*>(b);
        if (!la || !lb) return la == lb;
        if (la->size() != lb->size()) return false;
        for (size_t i = 0; i < la->size(); ++i) {
            if (!equalsValue(std::any((*la)[i]), std::any((*lb)[i]))) return false;
        }
        return true;
    }
    // 多值 EAttribute：EList<T>*（逐元素值比较，对齐 Java 多值 attribute 比较）
    // 由于 std::any 类型擦除，需逐个 try any_cast 各基础类型
    if (auto r = tryEListEquals<std::string>(a, b)) return *r;
    if (auto r = tryEListEquals<int>(a, b)) return *r;
    if (auto r = tryEListEquals<long>(a, b)) return *r;
    if (auto r = tryEListEquals<double>(a, b)) return *r;
    if (auto r = tryEListEquals<float>(a, b)) return *r;
    if (auto r = tryEListEquals<bool>(a, b)) return *r;
    // 其他类型：无法识别，返回 false（兜底）
    return false;
}

// ===== 复制 =====

EObject* EcoreUtil::copy(EObject* eObject) {
    if (!eObject) return nullptr;
    Copier copier;
    EObject* result = copier.copy(eObject);
    copier.copyReferences();
    return result;
}

std::vector<EObject*> EcoreUtil::copyAll(const std::vector<EObject*>& objects) {
    if (objects.empty()) return {};
    Copier copier;
    auto result = copier.copyAll(objects);
    copier.copyReferences();
    return result;
}

void EcoreUtil::remove(EObject* eObject) {
    if (!eObject) return;
    auto* container = eObject->eContainer();
    if (!container) {
        // 根对象：从 Resource 中移除
        auto* res = eObject->eResource();
        if (res) {
            // Resource 的 contents 移除由 Resource 处理
            // 这里不直接操作 Resource 内部列表
        }
        return;
    }
    // 从 container 的 containment feature 中移除
    auto* feature = eObject->eContainmentFeature();
    if (!feature) return;
    auto value = container->eGet(feature);
    if (feature->isMany()) {
        auto& list = std::any_cast<std::vector<EObject*>&>(value);
        list.erase(std::remove(list.begin(), list.end(), eObject), list.end());
    } else {
        EObject* nullObj = nullptr;
        container->eSet(feature, std::any(nullObj));
    }
}

// ===== 遍历 =====

std::unique_ptr<emf::common::TreeIterator<EObject*>>
EcoreUtil::getAllContents(EObject* eObject) {
    return std::make_unique<AllContentsIterator>(eObject);
}

// ===== 代理解析 =====

EObject* EcoreUtil::resolve(EObject* proxy, ResourceSet* resourceSet) {
    if (!proxy || !proxy->eIsProxy()) return proxy;
    if (!resourceSet) return proxy;
    // eProxyURI 在 EObjectImpl 中，通过 dynamic_cast 访问
    auto* impl = dynamic_cast<emf::common::EObjectImpl*>(proxy);
    if (!impl) return proxy;
    const URI& uri = impl->eProxyURI();
    // 分离 resource URI 和 fragment
    std::string s = uri.toString();
    auto hashPos = s.find('#');
    if (hashPos == std::string::npos) return proxy;
    URI resourceURI(s.substr(0, hashPos));
    std::string fragment = s.substr(hashPos + 1);
    Resource* res = resourceSet->getResource(resourceURI, true);
    if (!res) return proxy;
    EObject* resolved = res->getEObject(fragment);
    return resolved ? resolved : proxy;
}

void EcoreUtil::resolveAll(EObject* eObject) {
    if (!eObject || !eObject->eClass()) return;
    auto* cls = eObject->eClass();
    for (auto* f : cls->getEAllStructuralFeatures()) {
        if (!f) continue;
        auto value = eObject->eGet(f);
        if (!value.has_value()) continue;

        if (f->isMany()) {
            // 多值引用：遍历列表中的代理
            if (value.type() == typeid(std::vector<EObject*>)) {
                auto& list = const_cast<std::vector<EObject*>&>(
                    std::any_cast<const std::vector<EObject*>&>(value));
                for (auto& item : list) {
                    if (item && item->eIsProxy()) {
                        auto* res = eObject->eResource();
                        auto* rs = res ? res->getResourceSet() : nullptr;
                        EObject* resolved = resolve(item, rs);
                        if (resolved != item) item = resolved;
                    }
                }
            }
        } else {
            // 单值引用
            if (value.type() == typeid(EObject*)) {
                auto* ref = std::any_cast<EObject*>(value);
                if (ref && ref->eIsProxy()) {
                    auto* res = eObject->eResource();
                    auto* rs = res ? res->getResourceSet() : nullptr;
                    EObject* resolved = resolve(ref, rs);
                    if (resolved != ref) {
                        eObject->eSet(f, std::any(resolved));
                    }
                }
            }
        }
    }
    // 递归处理 containment 子对象
    for (auto* child : eObject->eContents()) {
        resolveAll(child);
    }
}

// ===== URI / ID =====

URI EcoreUtil::getURI(EObject* eObject) {
    if (!eObject) return URI{};
    auto* res = eObject->eResource();
    if (res) {
        std::string fragment = res->getURIFragment(eObject);
        return URI(res->getURI().toString() + "#" + fragment);
    }
    // 无 Resource：使用 eContainer 路径
    std::vector<std::string> segments;
    EObject* current = eObject;
    while (current) {
        auto* container = current->eContainer();
        if (!container) break;
        auto* feature = current->eContainmentFeature();
        if (feature) {
            auto value = container->eGet(feature);
            if (feature->isMany() && value.type() == typeid(std::vector<EObject*>)) {
                const auto& list = std::any_cast<const std::vector<EObject*>&>(value);
                auto it = std::find(list.begin(), list.end(), current);
                if (it != list.end()) {
                    segments.push_back("/@" + std::to_string(it - list.begin()));
                }
            } else {
                segments.push_back("/0");
            }
        } else {
            segments.push_back("/0");
        }
        current = container;
    }
    std::reverse(segments.begin(), segments.end());
    std::string path;
    for (const auto& s : segments) path += s;
    return URI{"urn:emf" + path};
}

std::string EcoreUtil::getID(EObject* eObject) {
    if (!eObject || !eObject->eClass()) return "";
    auto* cls = eObject->eClass();
    // 查找 id feature（标记为 id 的 EAttribute）
    for (auto* f : cls->getEAllStructuralFeatures()) {
        auto* attr = dynamic_cast<EAttribute*>(f);
        if (attr && attr->isID()) {
            auto v = eObject->eGet(f);
            if (v.has_value() && v.type() == typeid(std::string)) {
                return std::any_cast<std::string>(v);
            }
        }
    }
    return "";
}

void EcoreUtil::setID(EObject* eObject, const std::string& id) {
    if (!eObject || !eObject->eClass()) return;
    auto* cls = eObject->eClass();
    for (auto* f : cls->getEAllStructuralFeatures()) {
        auto* attr = dynamic_cast<EAttribute*>(f);
        if (attr && attr->isID()) {
            eObject->eSet(f, std::any(id));
            return;
        }
    }
}

// ===== 类型工具 =====

bool EcoreUtil::isAncestor(EClass* ancestorEClass, EObject* eObject) {
    if (!ancestorEClass || !eObject) return false;
    auto* cls = eObject->eClass();
    if (!cls) return false;
    if (cls == ancestorEClass) return true;
    // 检查继承链
    for (auto* super : cls->getEAllSuperTypes()) {
        if (super == ancestorEClass) return true;
    }
    return false;
}

EDataType* EcoreUtil::getEClassifier(EPackage* ePackage, const std::string& name) {
    if (!ePackage) return nullptr;
    auto* cls = ePackage->getEClassifier(name);
    return dynamic_cast<EDataType*>(cls);
}

// ===== 字符串/值转换 =====

std::any EcoreUtil::createFromString(EDataType* eDataType, const std::string& literal) {
    if (!eDataType) return {};
    const std::string& typeName = eDataType->getName();
    if (typeName == "EString" || typeName == "EChar" || typeName == "ECharacter") {
        return std::any(literal);
    }
    if (typeName == "EInt" || typeName == "EInteger" || typeName == "EBigInteger") {
        try { return std::any(std::stoi(literal)); } catch (...) { return {}; }
    }
    if (typeName == "ELong") {
        try { return std::any(std::stol(literal)); } catch (...) { return {}; }
    }
    if (typeName == "EDouble" || typeName == "EDoubleObject") {
        try { return std::any(std::stod(literal)); } catch (...) { return {}; }
    }
    if (typeName == "EFloat" || typeName == "EFloatObject") {
        try { return std::any(std::stof(literal)); } catch (...) { return {}; }
    }
    if (typeName == "EBoolean" || typeName == "EBooleanObject") {
        return std::any(literal == "true" || literal == "1");
    }
    // 默认返回字符串
    return std::any(literal);
}

std::string EcoreUtil::convertToString(EDataType* eDataType, const std::any& value) {
    if (!eDataType || !value.has_value()) return "";
    const std::string& typeName = eDataType->getName();
    if (value.type() == typeid(std::string)) {
        return std::any_cast<std::string>(value);
    }
    if (value.type() == typeid(int)) {
        return std::to_string(std::any_cast<int>(value));
    }
    if (value.type() == typeid(long)) {
        return std::to_string(std::any_cast<long>(value));
    }
    if (value.type() == typeid(double)) {
        return std::to_string(std::any_cast<double>(value));
    }
    if (value.type() == typeid(bool)) {
        return std::any_cast<bool>(value) ? "true" : "false";
    }
    return "";
}

// ===== 补齐的 EcoreUtil API =====

EObject* EcoreUtil::getRootContainer(EObject* eObject) {
    if (!eObject) return nullptr;
    EObject* root = eObject;
    while (auto* parent = root->eContainer()) {
        root = parent;
    }
    return root;
}

bool EcoreUtil::isAncestor(EObject* ancestorEObject, EObject* eObject) {
    if (!ancestorEObject || !eObject) return false;
    EObject* cur = eObject;
    while (cur) {
        if (cur == ancestorEObject) return true;
        cur = cur->eContainer();
    }
    return false;
}

EObject* EcoreUtil::create(EClass* eClass) {
    if (!eClass) return nullptr;
    if (eClass->isAbstract()) return nullptr;
    auto* pkg = eClass->getEPackage();
    if (!pkg) return nullptr;
    auto* factory = pkg->getEFactoryInstance();
    if (!factory) return nullptr;
    return factory->create(eClass);
}

void EcoreUtil::set(EObject* eObject, EStructuralFeature* feature, const std::any& value) {
    if (!eObject || !feature) return;
    eObject->eSet(feature, value);
}

void EcoreUtil::deleteObject(EObject* eObject, bool recursive) {
    if (!eObject) return;
    if (recursive) {
        // 递归删除子对象
        auto children = eObject->eContents();
        for (auto* child : children) {
            deleteObject(child, true);
        }
    }
    // 从 eContainer 的 containment feature 移除
    remove(eObject);
}

bool EcoreUtil::remove(EObject* eObject, EStructuralFeature* feature, const std::any& value) {
    if (!eObject || !feature) return false;
    std::any v = eObject->eGet(feature);
    // 单值：eSet 为空
    if (v.type() == typeid(EObject*)) {
        EObject* current = std::any_cast<EObject*>(v);
        EObject* target = std::any_cast<EObject*>(value);
        if (current == target) {
            EObject* nullObj = nullptr;
            eObject->eSet(feature, nullObj);
            return true;
        }
        return false;
    }
    // 多值：从列表移除
    if (v.type() == typeid(std::vector<EObject*>*)) {
        auto* list = std::any_cast<std::vector<EObject*>*>(v);
        if (!list) return false;
        auto* target = std::any_cast<EObject*>(value);
        auto it = std::find(list->begin(), list->end(), target);
        if (it != list->end()) {
            list->erase(it);
            return true;
        }
    }
    return false;
}

bool EcoreUtil::replace(EObject* eObject, EStructuralFeature* feature,
                         const std::any& oldValue, const std::any& newValue) {
    if (!eObject || !feature) return false;
    std::any v = eObject->eGet(feature);
    // 单值：直接 eSet 新值
    if (v.type() == typeid(EObject*)) {
        if (std::any_cast<EObject*>(v) == std::any_cast<EObject*>(oldValue)) {
            eObject->eSet(feature, newValue);
            return true;
        }
        return false;
    }
    // 多值：在列表中替换
    if (v.type() == typeid(std::vector<EObject*>*)) {
        auto* list = std::any_cast<std::vector<EObject*>*>(v);
        if (!list) return false;
        auto* oldObj = std::any_cast<EObject*>(oldValue);
        auto* newObj = std::any_cast<EObject*>(newValue);
        auto it = std::find(list->begin(), list->end(), oldObj);
        if (it != list->end()) {
            *it = newObj;
            return true;
        }
    }
    return false;
}

EAnnotation* EcoreUtil::getEAnnotation(EObject* eObject, const std::string& source) {
    if (!eObject) return nullptr;
    // EModelElement 接口提供 getEAnnotation(source)
    // 通过 eGet 反射访问 eAnnotations feature
    auto* cls = eObject->eClass();
    if (!cls) return nullptr;
    auto* sf = cls->getEStructuralFeature("eAnnotations");
    if (!sf) {
        // 按 xml.name 找
        for (auto* f : cls->getEAllStructuralFeatures()) {
            if (!f) continue;
            if (f->getName() == "eAnnotations") { sf = f; break; }
        }
    }
    if (!sf) return nullptr;
    std::any v = eObject->eGet(sf);
    // 提取 EAnnotation 列表（单值 EObject* 或多值 vector<EObject*>*）
    std::vector<EObject*> anns;
    if (v.type() == typeid(EObject*)) {
        if (auto* o = std::any_cast<EObject*>(v)) anns.push_back(o);
    } else if (v.type() == typeid(std::vector<EObject*>*)) {
        auto* p = std::any_cast<std::vector<EObject*>*>(v);
        if (p) for (auto* o : *p) if (o) anns.push_back(o);
    }
    for (auto* a : anns) {
        if (!a) continue;
        auto* aCls = a->eClass();
        if (!aCls) continue;
        auto* srcFeat = aCls->getEStructuralFeature("source");
        if (!srcFeat) continue;
        std::any sv = a->eGet(srcFeat);
        if (sv.type() == typeid(std::string)) {
            if (std::any_cast<std::string>(sv) == source) {
                return dynamic_cast<EAnnotation*>(a);
            }
        }
    }
    return nullptr;
}

Resource* EcoreUtil::getContainingResource(EObject* eObject) {
    if (!eObject) return nullptr;
    return eObject->eResource();
}

std::vector<EObject*> EcoreUtil::getObjectsByType(
    const std::vector<EObject*>& objects, EClassifier* type) {
    std::vector<EObject*> result;
    if (!type) return result;
    auto* targetCls = dynamic_cast<EClass*>(type);
    if (!targetCls) return result;
    for (auto* obj : objects) {
        if (!obj) continue;
        auto* cls = obj->eClass();
        if (!cls) continue;
        // 检查 cls 是否是 targetCls 或其子类
        if (cls == targetCls) {
            result.push_back(obj);
            continue;
        }
        for (auto* st : cls->getEAllSuperTypes()) {
            if (st == targetCls) {
                result.push_back(obj);
                break;
            }
        }
    }
    return result;
}

std::string EcoreUtil::getIdentification(EObject* eObject) {
    if (!eObject) return "";
    // 优先用 ID
    std::string id = getID(eObject);
    if (!id.empty()) return id;
    // 其次用 URI fragment
    URI uri = getURI(eObject);
    return uri.fragment();
}

}  // namespace emf::ecore::util
