// AbstractMetaModelDescriptor.cpp
// 对齐 Java org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor
#include "emf/sphinx/metamodel/AbstractMetaModelDescriptor.h"
#include "emf/common/URI.h"

namespace emf::sphinx::metamodel {

void AbstractMetaModelDescriptor::initNamespace() {
    std::string ns = baseNamespaceURI_;
    if (versionData_ && !versionData_->getNsPostfix().empty()) {
        ns += "/";
        ns += versionData_->getNsPostfix();
    }
    namespaceURI_ = emf::common::URI(ns);
}

IMetaModelDescriptor* AbstractMetaModelDescriptor::getBaseDescriptor() const {
    // Java 行为：先看 versionData 的 baseDescriptor；如果没有再看用户 setBaseDescriptor
    if (versionData_ && versionData_->getBaseDescriptor()) {
        return versionData_->getBaseDescriptor();
    }
    return nullptr;
}

int AbstractMetaModelDescriptor::getOrdinal() const {
    if (versionData_) return versionData_->getOrdinal();
    return -1;
}

void AbstractMetaModelDescriptor::initEPackageNsURIPattern() {
    ePackageNsURIPatternStr_.clear();
    ePackageNsURIPatternStr_ += baseNamespaceURI_;
    if (!ePackageNsURIPostfixPattern_.empty()) {
        ePackageNsURIPatternStr_ += "/";
        ePackageNsURIPatternStr_ += ePackageNsURIPostfixPattern_;
    } else if (versionData_ && !versionData_->getEPackageNsURIPostfixPattern().empty()) {
        ePackageNsURIPatternStr_ += "/";
        ePackageNsURIPatternStr_ += versionData_->getEPackageNsURIPostfixPattern();
    }
    ePackageNsURIPattern_ = std::regex(ePackageNsURIPatternStr_);
    ePackageNsURIPatternInit_ = true;
}

std::string AbstractMetaModelDescriptor::getEPackageNsURIPattern() const {
    if (!ePackageNsURIPatternInit_) {
        const_cast<AbstractMetaModelDescriptor*>(this)->initEPackageNsURIPattern();
    }
    return ePackageNsURIPatternStr_;
}

bool AbstractMetaModelDescriptor::matchesEPackageNsURIPattern(const std::string& ns) const {
    if (!ePackageNsURIPatternInit_) {
        const_cast<AbstractMetaModelDescriptor*>(this)->initEPackageNsURIPattern();
    }
    if (ePackageNsURIPatternStr_.empty()) return false;
    try {
        return std::regex_match(ns, ePackageNsURIPattern_);
    } catch (const std::regex_error&) {
        return false;
    }
}

bool AbstractMetaModelDescriptor::matchesNamespace(const std::string& ns) const {
    return getNamespace() == ns;
}

bool AbstractMetaModelDescriptor::equals(const IMetaModelDescriptor* other) const {
    if (!other) return false;
    // Java 行为：按 identifier 比较
    return identifier_ == other->getIdentifier();
}

int AbstractMetaModelDescriptor::hashCode() const {
    // Java String.hashCode() 简化：用 std::hash
    return static_cast<int>(std::hash<std::string>{}(identifier_));
}

}  // namespace emf::sphinx::metamodel
