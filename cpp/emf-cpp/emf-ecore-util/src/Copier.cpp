// Copier 实现
// 对齐 Java: org.eclipse.emf.ecore.util.EcoreUtil.Copier
//
// 两阶段复制：
//   1. copy/copyAll: 通过 EFactory.create(EClass) 创建副本，复制所有 attribute 值，
//      递归复制 containment 子对象，建立源->副本映射
//   2. copyReferences: 遍历所有已复制对象，更新非 containment 引用，
//      使其指向副本而非源对象（保持引用拓扑）
#include "emf/ecore/util/EcoreUtil.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EObject.h"

#include <algorithm>
#include <unordered_map>

namespace emf::ecore::util {

using emf::common::EObject;
using emf::ecore::EClass;
using emf::ecore::EStructuralFeature;
using emf::ecore::EReference;
using emf::ecore::EAttribute;
using emf::ecore::EFactory;

EObject* Copier::copy(EObject* eObject) {
    if (!eObject) return nullptr;
    // 已复制过：返回缓存
    auto it = sourceToCopy_.find(eObject);
    if (it != sourceToCopy_.end()) return it->second;

    auto* cls = eObject->eClass();
    if (!cls) return nullptr;
    auto* pkg = cls->getEPackage();
    if (!pkg) return nullptr;
    EFactory* factory = pkg->getEFactoryInstance();
    if (!factory) return nullptr;

    // 通过 EFactory.create(EClass) 创建新对象
    EObject* copyObj = factory->create(cls);
    if (!copyObj) return nullptr;

    sourceToCopy_[eObject] = copyObj;
    copiedSources_.push_back(eObject);

    // 复制所有 structural features
    for (auto* f : cls->getEAllStructuralFeatures()) {
        if (!f) continue;
        auto value = eObject->eGet(f);
        if (!value.has_value()) continue;

        if (f->isMany()) {
            // 多值 feature
            if (value.type() == typeid(std::vector<EObject*>)) {
                const auto& srcList = std::any_cast<const std::vector<EObject*>&>(value);
                std::vector<EObject*> dstList;
                dstList.reserve(srcList.size());
                auto* ref = dynamic_cast<EReference*>(f);
                if (ref && ref->isContainment()) {
                    // containment: 递归复制
                    for (auto* item : srcList) {
                        auto* copied = copy(item);
                        if (copied) {
                            // setEContainer/setEContainingFeature 在 EObjectImpl 中
                            if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(copied)) {
                                impl->setEContainer(copyObj);
                                impl->setEContainingFeature(f);
                            }
                            dstList.push_back(copied);
                        }
                    }
                } else {
                    // 非 containment: 延迟到 copyReferences 阶段
                    // 暂存源列表，后面再解析
                    dstList = srcList;  // 临时指向源，后面替换
                }
                copyObj->eSet(f, std::any(std::move(dstList)));
            } else {
                // 多值 attribute（非 EObject 列表）
                copyObj->eSet(f, value);
            }
        } else {
            // 单值 feature
            if (value.type() == typeid(EObject*)) {
                auto* ref = dynamic_cast<EReference*>(f);
                auto* target = std::any_cast<EObject*>(value);
                if (ref && ref->isContainment() && target) {
                    // containment: 递归复制
                    auto* copied = copy(target);
                    if (copied) {
                        if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(copied)) {
                            impl->setEContainer(copyObj);
                            impl->setEContainingFeature(f);
                        }
                        copyObj->eSet(f, std::any(copied));
                    }
                } else {
                    // 非 containment: 暂存源引用，延迟解析
                    copyObj->eSet(f, value);
                }
            } else {
                // 单值 attribute: 直接复制值
                copyObj->eSet(f, value);
            }
        }
    }
    return copyObj;
}

std::vector<EObject*> Copier::copyAll(const std::vector<EObject*>& objects) {
    std::vector<EObject*> result;
    result.reserve(objects.size());
    for (auto* obj : objects) {
        result.push_back(copy(obj));
    }
    return result;
}

void Copier::copyReferences() {
    // 遍历所有已复制的源对象，更新非 containment 引用
    for (auto* src : copiedSources_) {
        auto* dst = sourceToCopy_[src];
        if (!dst) continue;
        auto* cls = src->eClass();
        if (!cls) continue;
        for (auto* f : cls->getEAllStructuralFeatures()) {
            if (!f) continue;
            auto* ref = dynamic_cast<EReference*>(f);
            if (!ref || ref->isContainment()) continue;  // 只处理非 containment 引用

            auto value = src->eGet(f);
            if (!value.has_value()) continue;

            // eOpposite 维护：对带 eOpposite 的非 containment 引用（且非 container 反向端），
            // 在副本上调用 eInverseAdd 维护反向端。
            // 对齐 Java Copier.copyReferences：bidirectional 引用复制后需在对端建立反向链。
            // 注：codegen 生成的 setter 不调 eInverseAdd（由另一任务修复），故这里显式维护。
            // 跳过 container 引用（其 opposite 是 containment，已由 containment 复制阶段
            // 通过 setEContainer 建立反向端，避免重复维护）。
            EReference* opp = ref->getEOpposite();
            bool maintainInverse = (opp != nullptr && !ref->isContainer());

            if (f->isMany()) {
                if (value.type() == typeid(std::vector<EObject*>)) {
                    const auto& srcList = std::any_cast<const std::vector<EObject*>&>(value);
                    std::vector<EObject*> dstList;
                    dstList.reserve(srcList.size());
                    for (auto* item : srcList) {
                        auto it = sourceToCopy_.find(item);
                        EObject* copyTarget = it != sourceToCopy_.end() ? it->second : item;
                        dstList.push_back(copyTarget);
                        // 维护 eOpposite 双向引用：把 dst 加入 copyTarget 的 opposite 反向端
                        if (maintainInverse && it != sourceToCopy_.end()) {
                            copyTarget->eInverseAdd(dst, opp->getFeatureID(),
                                                    opp->getEContainingClass(), {});
                        }
                    }
                    dst->eSet(f, std::any(std::move(dstList)));
                }
            } else {
                if (value.type() == typeid(EObject*)) {
                    auto* target = std::any_cast<EObject*>(value);
                    if (target) {
                        auto it = sourceToCopy_.find(target);
                        if (it != sourceToCopy_.end()) {
                            EObject* copyTarget = it->second;
                            dst->eSet(f, std::any(copyTarget));
                            // 维护 eOpposite 双向引用：把 dst 加入 copyTarget 的 opposite 反向端
                            if (maintainInverse) {
                                copyTarget->eInverseAdd(dst, opp->getFeatureID(),
                                                        opp->getEContainingClass(), {});
                            }
                        }
                    }
                }
            }
        }
    }
}

EObject* Copier::get(EObject* source) const {
    auto it = sourceToCopy_.find(source);
    return it != sourceToCopy_.end() ? it->second : nullptr;
}

}  // namespace emf::ecore::util
