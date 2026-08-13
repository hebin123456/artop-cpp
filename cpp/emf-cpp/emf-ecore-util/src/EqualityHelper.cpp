// EqualityHelper 实现
// 对齐 Java: org.eclipse.emf.ecore.util.EqualityHelper
//
// 设计（对齐 Java EqualityHelper）：
//   - equals(EObject*,EObject*)：入口，做 a==b/null/proxy 守卫后委托 haveEqualFeature
//   - haveEqualFeature：遍历 eAllStructuralFeatures，分发到 haveEqualReference/haveEqualAttribute
//   - haveEqualReference：containment 递归 haveEqualObject（带循环保护）；
//     非 containment 委托 equalsValue（指针/proxy 比较）
//   - haveEqualAttribute：委托 equalsValue
//   - haveEqualObject：循环保护（visited_ 集合）+ 递归 haveEqualFeature
//
// EcoreUtil::equals(EObject*,EObject*) 静态方法内部创建 EqualityHelper 实例并委托，
// 从而获得 containment 深度比较 + 循环保护能力。
#include "emf/ecore/util/EcoreUtil.h"
#include "emf/ecore/EcoreImpls.h"

#include <any>

namespace emf::ecore::util {

using emf::common::EObject;
using emf::common::EObjectImpl;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;

bool EqualityHelper::equals(const std::any& a, const std::any& b) {
    return EcoreUtil::equalsValue(a, b);
}

bool EqualityHelper::equals(EObject* a, EObject* b) {
    if (a == b) return true;
    if (!a || !b) return false;
    // 代理对象：对齐原 EcoreUtil::equals 语义——任一为 proxy 即视为不等
    // （proxy 应由 resolve 后再比较；同对象 proxy 已在上面 a==b 命中）
    if (a->eIsProxy() || b->eIsProxy()) return false;
    // 委托 haveEqualFeature：containment 深度比较，循环保护由 haveEqualObject 负责
    return haveEqualFeature(a, b);
}

bool EqualityHelper::haveEqualFeature(EObject* a, EObject* b) {
    if (!a || !b) return a == b;
    auto* clsA = a->eClass();
    auto* clsB = b->eClass();
    if (!clsA || !clsB) return clsA == clsB;
    // 类必须相同（简化：不做继承兼容判断，要求精确匹配，对齐原 EcoreUtil::equals）
    if (clsA != clsB) return false;
    for (auto* f : clsA->getEAllStructuralFeatures()) {
        if (!f) continue;
        if (auto* ref = dynamic_cast<EReference*>(f)) {
            if (!haveEqualReference(a, b, ref)) return false;
        } else if (auto* attr = dynamic_cast<EAttribute*>(f)) {
            if (!haveEqualAttribute(a, b, attr)) return false;
        } else {
            // 未知 feature 类型：回退到 equalsValue
            if (!EcoreUtil::equalsValue(a->eGet(f), b->eGet(f))) return false;
        }
    }
    return true;
}

bool EqualityHelper::haveEqualReference(EObject* a, EObject* b, EReference* ref) {
    if (!a || !b || !ref) return a == b;
    std::any va = a->eGet(ref);
    std::any vb = b->eGet(ref);
    if (ref->isContainment()) {
        // containment：递归深度比较子树（对齐 Java haveEqualReference 对 containment 的递归）
        if (ref->isMany()) {
            // 多值 containment：取出元素列表，逐元素 haveEqualObject
            // 兼容 EList<EObject*>*（DynamicEObject/codegen）与 vector<EObject*> 两种返回形态
            std::vector<EObject*> la, lb;
            if (va.type() == typeid(emf::common::EList<EObject*>*)) {
                auto* p = std::any_cast<emf::common::EList<EObject*>*>(va);
                if (p) for (size_t i = 0; i < p->size(); ++i) la.push_back((*p)[i]);
            } else if (va.type() == typeid(std::vector<EObject*>)) {
                la = std::any_cast<std::vector<EObject*>>(va);
            }
            if (vb.type() == typeid(emf::common::EList<EObject*>*)) {
                auto* p = std::any_cast<emf::common::EList<EObject*>*>(vb);
                if (p) for (size_t i = 0; i < p->size(); ++i) lb.push_back((*p)[i]);
            } else if (vb.type() == typeid(std::vector<EObject*>)) {
                lb = std::any_cast<std::vector<EObject*>>(vb);
            }
            if (la.size() != lb.size()) return false;
            for (size_t i = 0; i < la.size(); ++i) {
                if (!la[i] && !lb[i]) continue;
                if (!la[i] || !lb[i]) return false;
                if (!haveEqualObject(la[i], lb[i])) return false;
            }
            return true;
        }
        // 单值 containment：递归 haveEqualObject
        if (va.type() != typeid(EObject*) || vb.type() != typeid(EObject*)) {
            // 值类型异常，回退到 equalsValue
            return EcoreUtil::equalsValue(va, vb);
        }
        auto* ea = std::any_cast<EObject*>(va);
        auto* eb = std::any_cast<EObject*>(vb);
        if (!ea && !eb) return true;
        if (!ea || !eb) return false;
        return haveEqualObject(ea, eb);
    }
    // 非 containment：委托 equalsValue（指针身份/proxy URI 比较，对齐 Java）
    return EcoreUtil::equalsValue(va, vb);
}

bool EqualityHelper::haveEqualAttribute(EObject* a, EObject* b, EAttribute* attr) {
    if (!a || !b) return a == b;
    if (!attr) return true;
    // 委托 equalsValue：处理基础类型与多值 EAttribute（EList<T>*）
    // 注：attr 已由 haveEqualFeature 选定，此处直接用 feature 取值比较
    auto* sf = dynamic_cast<EStructuralFeature*>(attr);
    if (!sf) return true;
    return EcoreUtil::equalsValue(a->eGet(sf), b->eGet(sf));
}

bool EqualityHelper::haveEqualObject(EObject* a, EObject* b) {
    if (a == b) return true;
    if (!a || !b) return false;
    auto key = std::make_pair(a, b);
    // 循环保护：比较进行中再次遇到同一对象对 → 视为相等以中断递归
    // （对齐 Java EqualityHelper 用 Set 防止 containment 自环/互环导致无限递归）
    if (visited_.count(key)) return true;
    visited_.insert(key);
    bool result = haveEqualFeature(a, b);
    visited_.erase(key);  // 仅做环检测，不缓存结果（对齐 Java EqualityHelper 语义）
    return result;
}

bool EqualityHelper::equalsFeatureValue(EObject* a, EObject* b, EStructuralFeature* f) {
    if (!a || !b || !f) return a == b;
    return equals(a->eGet(f), b->eGet(f));
}

double EqualityHelper::hashCode(const std::any& v) {
    if (!v.has_value()) return 0;
    if (v.type() == typeid(std::string)) {
        double h = 0;
        for (char c : std::any_cast<std::string>(v)) h = h * 31 + c;
        return h;
    }
    if (v.type() == typeid(int)) return static_cast<double>(std::any_cast<int>(v));
    if (v.type() == typeid(double)) return std::any_cast<double>(v);
    if (v.type() == typeid(bool)) return std::any_cast<bool>(v) ? 1.0 : 0.0;
    return 0;
}

double EqualityHelper::hashCode(EObject* obj) {
    if (!obj) return 0;
    auto* cls = obj->eClass();
    if (!cls) return 0;
    double h = 0;
    for (auto* f : cls->getEStructuralFeatures()) {
        h += hashCode(obj->eGet(f));
    }
    return h;
}

}  // namespace emf::ecore::util
