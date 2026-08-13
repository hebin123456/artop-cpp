// EcoreResourceUtil.cpp
// 对齐 Java org.eclipse.sphinx.emf.util.EcoreResourceUtil
// 骨架：headless 模式，不依赖 Eclipse 平台。
// 等 emf-common/emf-ecore API 稳定后再补具体实现。

#include "emf/sphinx/util/EcoreResourceUtil.h"

#include "emf/common/Resource.h"
#include "emf/common/EObject.h"
#include "emf/common/URI.h"
#include "emf/common/URIConverter.h"
#include "emf/common/EPackage.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/sphinx/resource/ExtendedResource.h"
#include "emf/sphinx/resource/ExtendedResourceAdapterFactory.h"
#include "emf/ecore/EcorePackage.h"

#include <fstream>
#include <sstream>
#include <stdexcept>

namespace emf::sphinx::util {

using emf::common::URI;
using emf::common::URIConverter;
using emf::common::Resource;
using emf::common::ResourceSet;
using emf::common::EObject;

URIConverter* EcoreResourceUtil::getURIConverter(ResourceSet* rs) {
    static URIConverter defaultConverter;
    if (rs) return &rs->getURIConverter();
    return &defaultConverter;
}

URI EcoreResourceUtil::convertToAbsoluteFileURI(const URI& uri) {
    if (uri.isRelative()) {
        return getURIConverter()->normalize(uri);
    }
    return uri;
}

URI EcoreResourceUtil::convertToPlatformResourceURI(const URI& uri) {
    // headless: 无 platform: 映射
    return getURIConverter()->normalize(uri);
}

bool EcoreResourceUtil::exists(const URI& uri) {
    return getURIConverter()->exists(uri);
}

bool EcoreResourceUtil::isEMFModelURI(const URI& uri) {
    std::string ns = readModelNamespace(nullptr, uri);
    if (ns.empty()) return false;
    return emf::common::EPackageRegistry::instance().get(ns) != nullptr;
}

URI EcoreResourceUtil::getURI(EObject* obj, bool /*resolve*/) {
    if (!obj) return URI();
    // TODO: 走 emf::common::EcoreUtil::getURI(obj, resolve)
    return URI();
}

std::string EcoreResourceUtil::normalizeURIFragment(Resource* res, const std::string& fragment) {
    if (!res) return fragment;
    // TODO: 调 ExtendedResourceAdapterFactory::adapt(res)->nomalizeURIFragment(fragment)
    (void)res;
    return fragment;
}

std::string EcoreResourceUtil::readModelNamespace(Resource* res) {
    if (!res) return "";
    return readModelNamespace(getURIConverter(res ? res->getResourceSet() : nullptr), res->getURI());
}

std::string EcoreResourceUtil::readModelNamespace(URIConverter* /*uc*/, const URI& uri) {
    if (!exists(uri)) return "";
    // 简单扫第一行 xmlns
    std::ifstream f(uri.toString());
    if (!f) return "";
    std::string line;
    while (std::getline(f, line)) {
        if (line.find("xmlns=") == std::string::npos) continue;
        auto p = line.find("xmlns=\"");
        if (p == std::string::npos) continue;
        p += 7;
        auto e = line.find('"', p);
        if (e == std::string::npos) return "";
        return line.substr(p, e - p);
    }
    return "";
}

std::string EcoreResourceUtil::readTargetNamespace(Resource* res) {
    if (!res) return "";
    return readModelNamespace(getURIConverter(res->getResourceSet()), res->getURI());
}

std::vector<std::string> EcoreResourceUtil::readRootElementComments(Resource* /*res*/) {
    return {};
}

std::map<std::string, std::string> EcoreResourceUtil::readSchemaLocationEntries(Resource* /*res*/) {
    return {};
}

std::map<std::string, std::any> EcoreResourceUtil::getDefaultLoadOptions() {
    return {{"RECORD_UNKNOWN_FEATURE", std::any(true)}};
}

std::map<std::string, std::any> EcoreResourceUtil::getDefaultSaveOptions() {
    return {};
}

Resource* EcoreResourceUtil::loadResource(ResourceSet* rs, const URI& uri, const std::map<std::string, std::any>& /*options*/) {
    if (!rs) return nullptr;
    URI n = convertToPlatformResourceURI(uri);
    return rs->getResource(n, true);
}

EObject* EcoreResourceUtil::loadEObject(ResourceSet* rs, const URI& uri) {
    return getEObject(rs, uri);
}

EObject* EcoreResourceUtil::getEObject(ResourceSet* rs, const URI& uri) {
    if (!rs) return nullptr;
    if (uri.fragment().empty()) return nullptr;
    return rs->getEObject(uri, false);
}

EObject* EcoreResourceUtil::getModelRoot(Resource* res) {
    if (!res) return nullptr;
    if (res->getContents().empty()) return nullptr;
    return res->getContents().front();
}

bool EcoreResourceUtil::isResourceLoaded(ResourceSet* rs, const URI& uri) {
    if (!rs) return false;
    Resource* r = rs->getResource(uri, false);
    return r && r->isLoaded();
}

std::string EcoreResourceUtil::getModelName(EObject* obj) {
    if (!obj) return "";
    auto* cls = obj->eClass();
    if (!cls) return "";
    auto* pkg = cls->getEPackage();
    if (!pkg) return "";
    std::string n = pkg->getName();
    if (n.empty()) return "";
    n[0] = static_cast<char>(std::toupper(static_cast<unsigned char>(n[0])));
    return n;
}

Resource* EcoreResourceUtil::addNewModelResource(ResourceSet* rs, const URI& uri, const std::string& /*ct*/, EObject* content) {
    if (!rs || !content) return nullptr;
    Resource* existing = rs->getResource(uri, false);
    if (existing) {
        try { unloadResource(existing); } catch (...) {}
    }
    Resource* r = rs->createResource(uri);
    if (r) {
        r->addToContents(content);
    }
    return r;
}

void EcoreResourceUtil::addModelResource(ResourceSet* rs, Resource* res) {
    if (!rs || !res) return;
    if (rs->getResource(res->getURI(), false) == nullptr) {
        // ResourceSet 提供的 getResources() 是 const，
        // 当前未暴露非 const 入口，骨架阶段仅做检查。
        (void)res;
    }
}

void EcoreResourceUtil::saveNewModelResource(ResourceSet* rs, const URI& uri, const std::string& ct, EObject* content, const std::map<std::string, std::any>& options) {
    Resource* r = addNewModelResource(rs, uri, ct, content);
    saveModelResource(r, options);
}

void EcoreResourceUtil::saveModelResource(Resource* res, const std::map<std::string, std::any>& /*options*/) {
    if (!res) return;
    try {
        res->save();
    } catch (...) {
        throw;
    }
}

void EcoreResourceUtil::unloadResource(Resource* res, bool /*memOpt*/) {
    if (!res) return;
    // Resource 暂未提供 unload；此处仅做占位
    (void)res;
}

void EcoreResourceUtil::unloadResource(ResourceSet* rs, const URI& uri, bool memOpt) {
    if (!rs) return;
    Resource* r = rs->getResource(uri, false);
    unloadResource(r, memOpt);
}

}  // namespace emf::sphinx::util
