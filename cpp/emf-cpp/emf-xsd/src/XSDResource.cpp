// EMF XSD: XSDResource + XSDSchemaRegistry 实现
#include "emf/xsd/XSDResource.h"
#include "emf/xsd/XSDParser.h"
#include "emf/xsd/XSDSchemaCompositor.h"
#include "emf/xsd/XSDInclude.h"
#include "emf/xsd/XSDImport.h"
#include <sstream>
#include <stdexcept>

namespace emf::xsd {

// ================================================================
// XSDSchemaRegistry (singleton)
// ================================================================
XSDSchemaRegistry& XSDSchemaRegistry::instance() {
    static XSDSchemaRegistry r;
    return r;
}

void XSDSchemaRegistry::registerSchema(const std::string& schemaLocation, XSDSchema* schema) {
    if (schemaLocation.empty() || !schema) return;
    registry_[schemaLocation] = schema;
}

XSDSchema* XSDSchemaRegistry::findByLocation(const std::string& schemaLocation) const {
    auto it = registry_.find(schemaLocation);
    return (it != registry_.end()) ? it->second : nullptr;
}

void XSDSchemaRegistry::clear() {
    registry_.clear();
    loader_ = nullptr;
}

void XSDSchemaRegistry::setLoader(Loader loader) {
    loader_ = std::move(loader);
}

XSDSchema* XSDSchemaRegistry::load(const std::string& schemaLocation) {
    if (loader_) return loader_(schemaLocation);
    return nullptr;
}

// ================================================================
// XSDResource
// ================================================================
XSDSchema* XSDResource::resolveSchema(const std::string& schemaLocation) {
    auto& reg = XSDSchemaRegistry::instance();
    if (XSDSchema* cached = reg.findByLocation(schemaLocation)) {
        return cached;
    }
    return reg.load(schemaLocation);
}

XSDSchema* XSDResource::resolveAndIncorporate(XSDSchemaCompositor* compositor) {
    if (!compositor) return nullptr;

    // XSDImport 优先用 namespace 查（namespace 解析是 EMF XSD 的标准行为），
    // XSDInclude 只能按 schemaLocation 查。
    // 这里统一尝试 schemaLocation（因为 namespace 到 schemaLocation 的映射由外部处理）。
    std::string loc;
    if (auto* inc = dynamic_cast<XSDInclude*>(compositor)) {
        loc = inc->getSchemaLocation();
    } else if (auto* imp = dynamic_cast<XSDImport*>(compositor)) {
        loc = imp->getSchemaLocation();
        if (loc.empty()) {
            // 无 schemaLocation 的 import（仅 namespace 引用）—— 视为已解析
            compositor->setIncorporatedSchema(nullptr);
            return nullptr;
        }
    }
    if (loc.empty()) return nullptr;

    XSDSchema* schema = resolveSchema(loc);
    if (schema) {
        // 标记 composor 的 owning schema（composor 是 main schema 的子）
        compositor->setSchema(dynamic_cast<XSDSchema*>(
            static_cast<emf::common::EObject*>(compositor)->eContainer()));
        schema->incorporate(compositor);
    }
    return schema;
}

XSDSchema* XSDResource::parseSchemaFromString(const std::string& xml) {
    XSDParser p;
    return p.parseString(xml);
}

}  // namespace emf::xsd
