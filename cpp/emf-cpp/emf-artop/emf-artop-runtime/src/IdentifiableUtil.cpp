// emf::artop::runtime —— IdentifiableUtil 实现
#include "emf/artop/runtime/IdentifiableUtil.h"

#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

#include <any>
#include <mutex>
#include <unordered_map>
#include <vector>

namespace emf::artop::runtime {

namespace {

// per-EClass feature 查找缓存（对齐 Java 反射缓存）
// 大文件 compare 时 IdentifiableUtil 被频繁调用（每对象一次），缓存避免重复遍历 EAllStructuralFeatures。
// key: EClass* -> (featureName -> EStructuralFeature*)
using FeatureCache = std::unordered_map<std::string, emf::ecore::EStructuralFeature*>;
std::unordered_map<emf::ecore::EClass*, FeatureCache> g_featureCache;
std::mutex g_featureCacheMutex;

// 反射查 EClass 的 feature，带 per-EClass 缓存
emf::ecore::EStructuralFeature* findFeature(emf::common::EObject* obj, const std::string& name) {
    if (!obj) return nullptr;
    auto* cls = obj->eClass();
    if (!cls) return nullptr;
    {
        std::lock_guard<std::mutex> lk(g_featureCacheMutex);
        auto cit = g_featureCache.find(cls);
        if (cit != g_featureCache.end()) {
            auto fit = cit->second.find(name);
            if (fit != cit->second.end()) return fit->second;
            // 未找到：记录 nullptr（负缓存）
            return nullptr;
        }
    }
    // 首次访问该 EClass：遍历所有 feature 并缓存
    FeatureCache fc;
    for (auto* f : cls->getEAllStructuralFeatures()) {
        if (f) fc[f->getName()] = f;
    }
    std::lock_guard<std::mutex> lk(g_featureCacheMutex);
    g_featureCache[cls] = std::move(fc);
    auto cit = g_featureCache.find(cls);
    if (cit == g_featureCache.end()) return nullptr;
    auto fit = cit->second.find(name);
    return fit != cit->second.end() ? fit->second : nullptr;
}

}  // namespace

bool IdentifiableUtil::hasShortName(emf::common::EObject* obj) {
    return findFeature(obj, SHORT_NAME_FEATURE) != nullptr;
}

std::string IdentifiableUtil::getShortName(emf::common::EObject* obj) {
    auto* f = findFeature(obj, SHORT_NAME_FEATURE);
    if (!f || !obj) return {};
    std::any v = obj->eGet(f);
    if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    return {};
}

void IdentifiableUtil::setShortName(emf::common::EObject* obj, const std::string& name) {
    auto* f = findFeature(obj, SHORT_NAME_FEATURE);
    if (!f || !obj) return;
    obj->eSet(f, name);
}

std::string IdentifiableUtil::getLongName(emf::common::EObject* obj) {
    auto* f = findFeature(obj, LONG_NAME_FEATURE);
    if (!f || !obj) return {};
    // longName 是 multi-valued EList<LONG-NAME>，简化：取第一个 entry 的 l4/l5/t 等
    std::any v = obj->eGet(f);
    // 暂返回空：multi-valued 复杂结构留给 codegen 处理
    (void)v;
    return {};
}

void IdentifiableUtil::setLongName(emf::common::EObject* /*obj*/, const std::string& /*name*/) {
    // 占位
}

std::string IdentifiableUtil::getDescription(emf::common::EObject* obj) {
    auto* f = findFeature(obj, DESCRIPTION_FEATURE);
    if (!f || !obj) return {};
    // description 是 multi-valued 列表
    (void)f;
    return {};
}

void IdentifiableUtil::setDescription(emf::common::EObject* /*obj*/, const std::string& /*desc*/) {
    // 占位
}

std::string IdentifiableUtil::getIdentifier(emf::common::EObject* obj) {
    auto* f = findFeature(obj, IDENTIFIER_FEATURE);
    if (!f || !obj) return {};
    std::any v = obj->eGet(f);
    if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    return {};
}

void IdentifiableUtil::setIdentifier(emf::common::EObject* obj, const std::string& id) {
    auto* f = findFeature(obj, IDENTIFIER_FEATURE);
    if (!f || !obj) return;
    obj->eSet(f, id);
}

std::string IdentifiableUtil::getUUID(emf::common::EObject* obj) {
    auto* f = findFeature(obj, UUID_FEATURE);
    if (!f || !obj) return {};
    std::any v = obj->eGet(f);
    if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    return {};
}

std::function<std::string(emf::common::EObject*)> IdentifiableUtil::asIdentifierProvider() {
    // 对齐 Java artop IdentifiableUtil：优先 shortName（同父同类型下唯一），
    // 其次 uuid（全局唯一）。emf-compare 的 match 会用 EClass name + ID 作 key，
    // 故 provider 只需返回标识值本身。
    return [](emf::common::EObject* obj) -> std::string {
        if (!obj) return {};
        std::string sn = getShortName(obj);
        if (!sn.empty()) return sn;
        std::string uid = getUUID(obj);
        if (!uid.empty()) return uid;
        return {};
    };
}

}  // namespace emf::artop::runtime
