// emf::artop::runtime —— AutosarMetaModelVersionData
// 对齐 Java: org.artop.aal.common.metamodel.AutosarMetaModelVersionData
//
// 描述 AUTOSAR 模型的版本号 (major.minor.revision)，
// 支持：
//   - 直接构造 (major, minor, revision)
//   - canonical 版本字符串 "4.4.8" <-> 整数三元组
//   - 决定 schema file naming（4.x 旧版/新版 schema 文件名格式不同）
#pragma once

#include <string>
#include <cstdint>

namespace emf::artop::runtime {

class AutosarMetaModelVersionData {
public:
    // 旧/新 4.x 格式分界点
    // 对齐 Java: LAST_OLD_4X_MINOR_VERSION = 3; FIRST_NEW_4X_MINOR_VERSION = 4
    static constexpr int LAST_OLD_4X_MINOR_VERSION   = 3;
    static constexpr int FIRST_NEW_4X_MINOR_VERSION  = 4;

    AutosarMetaModelVersionData() = default;
    AutosarMetaModelVersionData(int major, int minor, int revision);

    // 从 canonical 版本字符串构造，例如 "4.4.8"
    // 对齐 Java: AutosarMetaModelVersionData.createFromCanonicalVersionNumberString(String)
    static AutosarMetaModelVersionData createFromCanonicalVersionNumberString(const std::string& s);

    int getMajor()    const { return major_; }
    int getMinor()    const { return minor_; }
    int getRevision() const { return revision_; }

    // 是否是新版 4.x schema (4.4+)
    // 对齐 Java: AutosarMetaModelVersionData.isNewVersion(int major, int minor)
    static bool isNewVersion(int major, int minor) {
        return major == 4 && minor >= FIRST_NEW_4X_MINOR_VERSION;
    }
    bool isNewVersion() const { return isNewVersion(major_, minor_); }

    // 把版本号打成单个 32 位整数：major << 24 | minor << 16 | revision
    // 对齐 Java: AutosarMetaModelVersionData.getCanonicalVersionNumber()
    int getCanonicalVersionNumber() const;

    // schema 文件名版本号段
    // 旧版本（4.0~4.3）: "00046"
    // 新版本（4.4+）:    "4.4.8"
    // 对齐 Java: getSchemaVersionNumberString()
    std::string getSchemaVersionNumberString(const std::string& separator) const;

    // 完整显示版本号
    std::string toString() const;

    bool operator==(const AutosarMetaModelVersionData& other) const {
        return major_ == other.major_ && minor_ == other.minor_ && revision_ == other.revision_;
    }
    bool operator!=(const AutosarMetaModelVersionData& other) const { return !(*this == other); }
    bool operator<(const AutosarMetaModelVersionData& other) const;

private:
    int major_    = 0;
    int minor_    = 0;
    int revision_ = 0;
};

}  // namespace emf::artop::runtime
