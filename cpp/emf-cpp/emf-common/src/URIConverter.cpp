// URIConverter.cpp —— URIConverter 实现
// 对齐 Java: org.eclipse.emf.ecore.resource.impl.URIConverterImpl
#include "emf/common/URIConverter.h"
#include "emf/common/URI.h"

#include <sys/stat.h>
#include <fstream>
#include <sstream>

namespace emf::common {

// normalize：应用 URIMap，处理相对路径
URI URIConverter::normalize(const URI& uri) const {
    // 1. 应用 URIMap
    std::string key = uri.toString();
    auto it = uriMap_.find(key);
    if (it != uriMap_.end()) {
        return URI(it->second);
    }
    // 2. 相对路径处理（对齐 Java：相对 URI 加 file: 前缀）
    if (uri.isRelative() && uri.hasRelativePath()) {
        std::string s = uri.toString();
        if (!s.empty() && s[0] != '/') {
            return URI("file:./" + s);
        }
    }
    return uri;
}

// resolve：相对 URI 基于 base 解析为绝对 URI
URI URIConverter::resolve(const URI& uri, const URI& base) const {
    if (uri.isRelative() && uri.hasRelativePath() && !base.isEmpty()) {
        return uri.resolve(base);
    }
    return uri;
}

// deresolve：绝对 URI 转为相对 URI
URI URIConverter::deresolve(const URI& uri, const URI& base) const {
    if (!base.isEmpty() && !base.isRelative() && !uri.isRelative()) {
        URI deresolved = uri.deresolve(base);
        if (deresolved.hasRelativePath()) {
            return deresolved;
        }
    }
    return uri;
}

// createInputStream：打开输入流
std::unique_ptr<std::istream> URIConverter::createInputStream(const URI& uri) const {
    std::string path;
    if (uri.isFile()) {
        path = uri.toFilePath();
        if (path.empty()) path = uri.toString();
    } else {
        path = uri.toString();
    }
    auto stream = std::make_unique<std::ifstream>(path, std::ios::binary);
    if (!stream->is_open()) {
        return nullptr;
    }
    return stream;
}

// createOutputStream：打开输出流
std::unique_ptr<std::ostream> URIConverter::createOutputStream(const URI& uri) const {
    std::string path;
    if (uri.isFile()) {
        path = uri.toFilePath();
        if (path.empty()) path = uri.toString();
    } else {
        path = uri.toString();
    }
    auto stream = std::make_unique<std::ofstream>(path, std::ios::binary);
    if (!stream->is_open()) {
        return nullptr;
    }
    return stream;
}

// exists：检查 URI 是否存在
bool URIConverter::exists(const URI& uri) const {
    std::string path;
    if (uri.isFile()) {
        path = uri.toFilePath();
        if (path.empty()) path = uri.toString();
    } else {
        path = uri.toString();
    }
    struct stat st;
    return stat(path.c_str(), &st) == 0;
}

// getTimeStamp：获取 URI 的修改时间戳
long long URIConverter::getTimeStamp(const URI& uri) const {
    std::string path;
    if (uri.isFile()) {
        path = uri.toFilePath();
        if (path.empty()) path = uri.toString();
    } else {
        path = uri.toString();
    }
    struct stat st;
    if (stat(path.c_str(), &st) != 0) return 0;
    return static_cast<long long>(st.st_mtime);
}

}  // namespace emf::common
