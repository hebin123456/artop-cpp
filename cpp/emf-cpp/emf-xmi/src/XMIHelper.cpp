// XMIHelper.cpp —— XMIHelper 工具函数实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl + XMLSaveImpl.Escape
#include "emf/xmi/XMIHelper.h"

#include <cstdio>
#include <cstdint>
#include <string>

namespace emf::xmi {

namespace {

// 解码 UTF-8 多字节序列为 Unicode code point。
// 返回消耗的字节数；cp 为解码出的 code point。非法序列按单字节处理。
int decodeUtf8(const std::string& s, size_t i, uint32_t& cp) {
    unsigned char c = (unsigned char)s[i];
    int len = 0;
    cp = 0;
    if ((c & 0x80) == 0) {           // 0xxxxxxx
        cp = c; len = 1;
    } else if ((c & 0xE0) == 0xC0) { // 110xxxxx 10xxxxxx
        len = 2; cp = c & 0x1F;
    } else if ((c & 0xF0) == 0xE0) { // 1110xxxx 10xxxxxx 10xxxxxx
        len = 3; cp = c & 0x0F;
    } else if ((c & 0xF8) == 0xF0) { // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        len = 4; cp = c & 0x07;
    } else {
        // 非法首字节（continuation byte 或 0xFE/0xFF），按单字节处理
        cp = c; return 1;
    }
    for (int j = 1; j < len && i + j < s.size(); ++j) {
        unsigned char b = (unsigned char)s[i + j];
        if ((b & 0xC0) != 0x80) { len = j; break; }
        cp = (cp << 6) | (b & 0x3F);
    }
    return len;
}

}  // namespace

std::string escapeXmlAttr(const std::string& s, int mappableLimit) {
    std::string out;
    out.reserve(s.size());
    size_t i = 0;
    while (i < s.size()) {
        unsigned char c = (unsigned char)s[i];
        if (c < 0x80) {
            // ASCII 快速路径
            switch (c) {
                case '&':  out += "&amp;";  break;
                case '<':  out += "&lt;";   break;
                case '"':  out += "&quot;"; break;
                case '\n': out += "&#xA;";  break;
                case '\r': out += "&#xD;";  break;
                case '\t': out += "&#x9;";  break;
                default:
                    if (c < 0x20) {
                        // 控制字符 0x01-0x1F（除 \t \n \r）→ &#xN;
                        char buf[8];
                        std::snprintf(buf, sizeof(buf), "&#x%x;", c);
                        out += buf;
                    } else {
                        // 不转义 > 和 '（对齐 Java convert()）
                        out += (char)c;
                    }
                    break;
            }
            ++i;
        } else {
            // 非 ASCII：解码 UTF-8 → code point → 按需转义
            uint32_t cp = 0;
            int len = decodeUtf8(s, i, cp);
            if (cp > (uint32_t)mappableLimit) {
                char buf[16];
                std::snprintf(buf, sizeof(buf), "&#x%x;", cp);
                out += buf;
            } else {
                out.append(s, i, len);
            }
            i += len;
        }
    }
    return out;
}

std::string escapeXmlText(const std::string& s, int mappableLimit) {
    std::string out;
    out.reserve(s.size());
    size_t i = 0;
    while (i < s.size()) {
        unsigned char c = (unsigned char)s[i];
        if (c < 0x80) {
            switch (c) {
                case '&':  out += "&amp;";  break;
                case '<':  out += "&lt;";   break;
                case '"':  out += "&quot;"; break;
                case '\r': out += "&#xD;";  break;
                default:
                    if (c < 0x20 && c != '\n' && c != '\t') {
                        // 控制字符（除 \n \t）→ &#xN;
                        char buf[8];
                        std::snprintf(buf, sizeof(buf), "&#x%x;", c);
                        out += buf;
                    } else {
                        // 不转义 > ' \n \t（对齐 Java convertText()）
                        out += (char)c;
                    }
                    break;
            }
            ++i;
        } else {
            uint32_t cp = 0;
            int len = decodeUtf8(s, i, cp);
            if (cp > (uint32_t)mappableLimit) {
                char buf[16];
                std::snprintf(buf, sizeof(buf), "&#x%x;", cp);
                out += buf;
            } else {
                out.append(s, i, len);
            }
            i += len;
        }
    }
    return out;
}

int mappableLimitForEncoding(const std::string& encoding) {
    if (encoding == "ASCII" || encoding == "US-ASCII") return 0x7F;
    if (encoding == "ISO-8859-1" || encoding == "Latin1" || encoding == "latin1") return 0xFF;
    return 0x10FFFF;  // UTF-8 等
}

}  // namespace emf::xmi
