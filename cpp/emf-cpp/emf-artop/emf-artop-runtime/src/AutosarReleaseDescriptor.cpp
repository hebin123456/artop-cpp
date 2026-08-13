// emf::artop::runtime —— AutosarReleaseDescriptor 实现
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"

#include <sstream>
#include <stdexcept>

namespace emf::artop::runtime {

AutosarReleaseDescriptor& AutosarReleaseDescriptor::getInstance() {
    static AutosarReleaseDescriptor inst{std::string(ID), AutosarMetaModelVersionData(4, 4, 0)};
    inst.setName("AUTOSAR 4.4.0 (default)");
    return inst;
}

AutosarReleaseDescriptor::AutosarReleaseDescriptor(std::string id,
                                                   AutosarMetaModelVersionData version)
    : id_(std::move(id)), versionData_(version) {}

std::string AutosarReleaseDescriptor::getSchemaLocationBase() const {
    // "http://autosar.org/schema/r4.0"
    return baseNamespace_;
}

std::string AutosarReleaseDescriptor::getSchemaLocation() const {
    // "http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd" —— 旧 4.x 风格
    // 新 4.x 风格: "http://autosar.org/schema/r4.0 AUTOSAR_4-4-8.xsd"
    std::ostringstream oss;
    oss << getSchemaLocationBase() << " ";
    oss << AUTOSAR_SCHEMA_FILE_NAME_PREFIX;
    if (versionData_.isNewVersion()) {
        oss << versionData_.getSchemaVersionNumberString(AUTOSAR_SCHEMA_VERSION_NUMBER_SEPARATOR);
    } else {
        oss << versionData_.getSchemaVersionNumberString("");
    }
    oss << "." << XSD_FILE_EXTENSION;
    return oss.str();
}

bool AutosarReleaseDescriptor::matchesSchemaLocation(const std::string& sl) const {
    if (sl.empty()) return false;
    // sl 形如 "http://autosar.org/schema/r4.0 AUTOSAR_4-4-8.xsd"
    // 也可能多个空格分隔的多组：取第一组
    std::string first = sl;
    auto sp = first.find(' ');
    if (sp != std::string::npos) {
        first = first.substr(0, sp);
    }
    return first == baseNamespace_;
}

std::string AutosarReleaseDescriptor::getDefaultContentTypeId() const {
    if (defaultContentTypeId_ == ARXML_BASE_CONTENT_TYPE_ID && !id_.empty()) {
        // 与 Java 一致：拼成具体 id "org.artop.aal.autosar40.contenttype" 等
        return id_ + ".contenttype";
    }
    return defaultContentTypeId_;
}

std::shared_ptr<AutosarReleaseDescriptor> AutosarReleaseDescriptor::create(
    const std::string& id, const AutosarMetaModelVersionData& v) {
    return std::make_shared<AutosarReleaseDescriptor>(id, v);
}

}  // namespace emf::artop::runtime
