// emf::xmi —— XMIHelper（loader/saver 共用的工具函数集合）
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl（子集）
//
// 这些函数由 XMILoader.cpp / XMISaver.cpp 共享，集中放置以避免重复实现。
// 保留为 inline 以减小调用开销并避免文件间循环依赖。
#pragma once

#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EObject.h"
#include <string>
#include <vector>

namespace emf::xmi {

// Ecore 命名空间 URI（对齐 Java EcorePackage.eNS_URI）
inline constexpr const char* kEcoreNsURI   = "http://www.eclipse.org/emf/2002/Ecore";
inline constexpr const char* kXmiNsURI     = "http://www.omg.org/XMI";
inline constexpr const char* kXmiNsURI2    = "http://schema.omg.org/spec/XMI/2.0";
inline constexpr const char* kXsiNsURI     = "http://www.w3.org/2001/XMLSchema-instance";

// 把 "prefix:local" 切成 {prefix, local}；无冒号时 prefix 为空。
inline std::pair<std::string, std::string> splitQName(const std::string& q) {
    auto p = q.find(':');
    if (p == std::string::npos) return {"", q};
    return {q.substr(0, p), q.substr(p + 1)};
}

// 解析 href 字符串：
//   "path#fragment"     -> {path, fragment}
//   "path"              -> {path, ""}
//   "#fragment"         -> {"", fragment}
//   "#//Class"          -> {"", "//Class"}
//   "ecore:EDataType http://...#//EString"  -> 取空格后部分再切
struct HrefRef { std::string path; std::string fragment; };
inline HrefRef splitHref(const std::string& s) {
    // 形如 "ecore:EAttribute http://.../Ecore#//EString"：先取空格后部分
    std::string rest = s;
    auto sp = rest.find(' ');
    if (sp != std::string::npos) rest = rest.substr(sp + 1);
    HrefRef r;
    auto hash = rest.find('#');
    if (hash == std::string::npos) { r.path = rest; r.fragment = ""; }
    else { r.path = rest.substr(0, hash); r.fragment = rest.substr(hash + 1); }
    return r;
}

// 把 fragment "//Class" 或 "//Container/feature.name" 中的前导 "//" 或 "/" 去掉
inline std::string stripFragmentSlash(const std::string& frag) {
    std::string s = frag;
    while (!s.empty() && s[0] == '/') s = s.substr(1);
    return s;
}

// XML 转义（attribute value 形式）—— 对齐 Java XMLSaveImpl.Escape.convert()
// 规则（mappableLimit 默认 0x7F，对应 Java 默认 encoding=ASCII）：
//   & → &amp;   < → &lt;   " → &quot;
//   \n → &#xA;  \r → &#xD;  \t → &#x9;
//   不转义 > 和 '（Java 属性值不转义这两个）
//   非 ASCII（code point > mappableLimit）→ &#x<hex>;（小写 hex，对齐 Java Integer.toHexString）
//   控制字符 0x01-0x1F（除 \t \n \r）→ &#xN;
//   mappableLimit: ASCII=0x7F, Latin1=0xFF, UTF=0x10FFFF
std::string escapeXmlAttr(const std::string& s, int mappableLimit = 0x7F);

// XML 转义（text content 形式）—— 对齐 Java XMLSaveImpl.Escape.convertText()
// 规则：
//   & → &amp;   < → &lt;   " → &quot;   \r → &#xD;
//   不转义 > ' \n \t（Java convertText 保留这些）
//   非 ASCII → &#x<hex>;
std::string escapeXmlText(const std::string& s, int mappableLimit = 0x7F);

// 根据 encoding 名字计算 mappableLimit（对齐 Java XMLSaveImpl 第 458-473 行）
//   ASCII/US-ASCII → 0x7F
//   ISO-8859-1/Latin1 → 0xFF
//   其他（UTF-8 等）→ 0x10FFFF
int mappableLimitForEncoding(const std::string& encoding);

}  // namespace emf::xmi
