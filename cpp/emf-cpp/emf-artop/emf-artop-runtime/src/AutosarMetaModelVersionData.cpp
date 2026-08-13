// emf::artop::runtime —— AutosarMetaModelVersionData 实现
#include "emf/artop/runtime/AutosarMetaModelVersionData.h"

#include <sstream>
#include <stdexcept>
#include <iomanip>

namespace emf::artop::runtime {

AutosarMetaModelVersionData::AutosarMetaModelVersionData(int major, int minor, int revision)
    : major_(major), minor_(minor), revision_(revision) {}

AutosarMetaModelVersionData AutosarMetaModelVersionData::createFromCanonicalVersionNumberString(const std::string& s) {
    if (s.empty()) {
        throw std::invalid_argument("AutosarMetaModelVersionData: empty version string");
    }
    // 解析 "major.minor.revision" 或 "major.minor"
    int major = 0, minor = 0, revision = 0;
    int parts[3] = {0, 0, 0};
    int partCount = 0;
    std::string cur;
    for (char c : s) {
        if (c == '.') {
            if (partCount >= 3) throw std::invalid_argument("AutosarMetaModelVersionData: too many parts in " + s);
            try { parts[partCount++] = std::stoi(cur); }
            catch (...) { throw std::invalid_argument("AutosarMetaModelVersionData: bad number in " + s); }
            cur.clear();
        } else {
            cur.push_back(c);
        }
    }
    if (partCount >= 3) throw std::invalid_argument("AutosarMetaModelVersionData: too many parts in " + s);
    if (!cur.empty()) {
        try { parts[partCount++] = std::stoi(cur); }
        catch (...) { throw std::invalid_argument("AutosarMetaModelVersionData: bad number in " + s); }
    }
    if (partCount == 0) throw std::invalid_argument("AutosarMetaModelVersionData: empty version " + s);
    major    = parts[0];
    minor    = partCount > 1 ? parts[1] : 0;
    revision = partCount > 2 ? parts[2] : 0;
    return AutosarMetaModelVersionData(major, minor, revision);
}

int AutosarMetaModelVersionData::getCanonicalVersionNumber() const {
    return (major_ << 24) | (minor_ << 16) | (revision_ & 0xFFFF);
}

std::string AutosarMetaModelVersionData::getSchemaVersionNumberString(const std::string& separator) const {
    std::ostringstream oss;
    if (isNewVersion()) {
        // 新版: "4.4.8" 用 separator 连接
        oss << major_ << separator << minor_ << separator << revision_;
    } else {
        // 旧版 5位: "00" + zero-padded(40+minor, 3)
        // 例如 4.2.1 -> "00042", 4.4.0 (旧) -> "00044", 4.7.0 -> "00047"
        // 4.0~4.1 和 4.4.8+ 都走 isNewVersion 分支
        int code = 40 + minor_;  // 4.0=40, 4.2=42, 4.4=44, 4.7=47, 4.8=48
        oss << "00" << std::setw(3) << std::setfill('0') << code;
    }
    return oss.str();
}

std::string AutosarMetaModelVersionData::toString() const {
    std::ostringstream oss;
    oss << major_ << "." << minor_ << "." << revision_;
    return oss.str();
}

bool AutosarMetaModelVersionData::operator<(const AutosarMetaModelVersionData& other) const {
    if (major_ != other.major_) return major_ < other.major_;
    if (minor_ != other.minor_) return minor_ < other.minor_;
    return revision_ < other.revision_;
}

}  // namespace emf::artop::runtime
