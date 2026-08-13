// ConstraintDescriptor.cpp
// 对齐 org.eclipse.emf.validation.service.IConstraintDescriptor (Java)
#include "emf/validation/ConstraintDescriptor.h"
#include "emf/validation/ConstraintParser.h"

#include <algorithm>
#include <cctype>

namespace emf::validation {

Constraint* ConstraintDescriptor::instantiate() const {
    auto eval = ConstraintParser::compile(body_);
    Constraint::Evaluator wrapped = [eval](emf::common::EObject* target) {
        return eval(target, std::any{});
    };
    return new Constraint(wrapped, id_, name_, message_, severity_, mode_);
}

namespace {

// 极简 XML 属性值提取：在 [start, end) 范围内查找 name="value" 并返回 value
std::string attrValue(const std::string& s, const std::string& name, size_t start, size_t end) {
    std::string key = name + "=\"";
    size_t pos = s.find(key, start);
    if (pos == std::string::npos || pos >= end) return "";
    pos += key.size();
    size_t quote = s.find('"', pos);
    if (quote == std::string::npos || quote >= end) return "";
    return s.substr(pos, quote - pos);
}

Severity parseSeverity(const std::string& s) {
    if (s == "error")   return Severity::ERROR;
    if (s == "warning") return Severity::WARNING;
    if (s == "info")    return Severity::INFO;
    if (s == "ok")      return Severity::OK;
    if (s == "cancel")  return Severity::CANCEL;
    return Severity::WARNING;
}

ConstraintMode parseMode(const std::string& s) {
    if (s == "live") return ConstraintMode::LIVE;
    return ConstraintMode::BATCH;
}

int parseInt(const std::string& s) {
    try { return std::stoi(s); } catch (...) { return 0; }
}

}  // namespace

std::vector<ConstraintDescriptor> ConstraintDescriptorParser::parseDescriptors(const std::string& xml) {
    std::vector<ConstraintDescriptor> out;
    const std::string tag = "<constraint";
    size_t pos = 0;
    while ((pos = xml.find(tag, pos)) != std::string::npos) {
        size_t end = xml.find('>', pos);
        if (end == std::string::npos) break;
        ConstraintDescriptor d;
        d.setId(attrValue(xml, "id", pos, end));
        d.setName(attrValue(xml, "name", pos, end));
        d.setMessage(attrValue(xml, "message", pos, end));
        d.setDescription(attrValue(xml, "description", pos, end));
        d.setSeverity(parseSeverity(attrValue(xml, "severity", pos, end)));
        d.setMode(parseMode(attrValue(xml, "mode", pos, end)));
        d.setLanguage(attrValue(xml, "language", pos, end));
        d.setBody(attrValue(xml, "body", pos, end));
        d.setCategoryPath(attrValue(xml, "categoryPath", pos, end));
        d.setCode(parseInt(attrValue(xml, "code", pos, end)));
        if (!d.getId().empty()) out.push_back(std::move(d));
        pos = end + 1;
    }
    return out;
}

int ConstraintDescriptorParser::parseAndRegister(const std::string& xml, EValidator& validator) {
    auto descs = parseDescriptors(xml);
    int n = 0;
    for (auto& d : descs) {
        Constraint* c = d.instantiate();
        if (c) {
            delete validator.registerConstraint(c);
            ++n;
        }
    }
    return n;
}

int ConstraintDescriptorParser::parseAndRegisterFiltered(const std::string& xml,
                                                         EValidator& validator,
                                                         const std::string& categoryPathPrefix) {
    auto descs = parseDescriptors(xml);
    int n = 0;
    for (auto& d : descs) {
        if (!categoryPathPrefix.empty() &&
            d.getCategoryPath().rfind(categoryPathPrefix, 0) != 0) {
            continue;
        }
        Constraint* c = d.instantiate();
        if (c) {
            delete validator.registerConstraint(c);
            ++n;
        }
    }
    return n;
}

}  // namespace emf::validation
