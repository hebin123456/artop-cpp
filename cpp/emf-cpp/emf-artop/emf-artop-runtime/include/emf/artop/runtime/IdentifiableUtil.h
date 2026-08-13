// emf::artop::runtime —— IdentifiableUtil
// 对齐 Java: org.artop.aal.common.util.IdentifiableUtil
//
// AUTOSAR 模型中几乎所有"有名字"的对象都实现 Identifiable 接口（gautosar 库）。
// 工具类提供：
//   - 读 / 写 Identifiable 的 shortName、longName、description
//   - 处理多版本下 Identifiable feature 名字差异
//
// 简化版：用 EClass 的 EAllAttributes 反射查找
//   - shortName  → IDENTIFIER 类型 EAttribute
//   - longName   → LONG_NAME_* EReference
//   - description → DESCRIPTION EReference
#pragma once

#include "emf/common/EObject.h"
#include <functional>
#include <string>

namespace emf::artop::runtime {

class IdentifiableUtil {
public:
    // 通用标识符 feature 名称
    static constexpr const char* SHORT_NAME_FEATURE    = "shortName";
    static constexpr const char* LONG_NAME_FEATURE     = "longName";
    static constexpr const char* DESCRIPTION_FEATURE   = "description";
    static constexpr const char* IDENTIFIER_FEATURE    = "identifier";
    static constexpr const char* UUID_FEATURE          = "uuid";

    // 读取 shortName
    static std::string getShortName(emf::common::EObject* obj);

    // 写入 shortName
    static void setShortName(emf::common::EObject* obj, const std::string& name);

    // 读 longName
    static std::string getLongName(emf::common::EObject* obj);
    static void setLongName(emf::common::EObject* obj, const std::string& name);

    // 读 description
    static std::string getDescription(emf::common::EObject* obj);
    static void setDescription(emf::common::EObject* obj, const std::string& desc);

    // 是否有 shortName 特征
    static bool hasShortName(emf::common::EObject* obj);

    // 是否有 identifier（sfIdentifier 通道）
    static std::string getIdentifier(emf::common::EObject* obj);
    static void setIdentifier(emf::common::EObject* obj, const std::string& id);

    // 读取 uuid（对齐 Java IdentifiableUtil.getUUID → GIdentifiable.gGetUuid）
    static std::string getUUID(emf::common::EObject* obj);

    // 构造一个 IdentifierProvider 回调，对齐 Java artop IdentifiableUtil 语义：
    // 优先 shortName（同父同类型下唯一），其次 uuid（全局唯一）。
    // 返回 std::function 以避免 artop 层依赖 emf-compare 头文件
    // （emf-compare 的 IdentifierProvider 即 std::function<std::string(EObject*)>，可直接隐式转换）。
    // 集成方式：
    //   MatchEngine me;
    //   me.setIdentifierProvider(IdentifiableUtil::asIdentifierProvider());
    static std::function<std::string(emf::common::EObject*)> asIdentifierProvider();
};

}  // namespace emf::artop::runtime
