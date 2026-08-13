// URI 实现
// 对齐 org.eclipse.emf.common.util.URI (Java)
#include "emf/common/URI.h"
#include <algorithm>
#include <filesystem>
#include <fstream>
#include <sstream>

namespace emf::common {

bool URI::hasRelativePath() const {
    // 相对路径包含 "./" 或 "../" 或以 "." 开头
    if (path_.empty()) return false;
    if (path_ == "." || path_ == "..") return true;
    // 检查是否包含 /./ 或 /. 或 /.. 或 ../
    for (size_t i = 0; i < path_.size(); ++i) {
        if (path_[i] == '.' && (i == 0 || path_[i-1] == '/')) {
            if (i + 1 < path_.size()) {
                char next = path_[i+1];
                if (next == '/' || next == '.') return true;
            } else {
                return true;  // 以 . 结尾
            }
        }
    }
    return false;
}

std::vector<std::string> URI::parseSegments(const std::string& p) const {
    std::vector<std::string> result;
    std::stringstream ss(p);
    std::string seg;
    while (std::getline(ss, seg, '/')) {
        if (!seg.empty()) result.push_back(seg);
    }
    return result;
}

std::vector<std::string> URI::segments() const {
    if (schemeOpaque_) return {};
    return parseSegments(path_);
}

std::string URI::segment(int index) const {
    auto segs = segments();
    if (index < 0 || index >= (int)segs.size()) return "";
    return segs[index];
}

URI URI::resolve(const URI& base) const {
    // 对齐 Java: URI.resolve(URI base)
    // 如果自己已经是绝对 URI，直接返回
    if (!isRelative()) return *this;
    
    // 如果 base 不是层级 URI，抛出异常（简化处理：直接返回 this）
    if (!base.isHierarchical() || base.isRelative()) return *this;
    
    URI result;
    result.scheme_ = base.scheme_;
    result.device_ = base.device_;
    result.hasAuthority_ = base.hasAuthority_;
    result.authority_ = base.authority_;
    
    // 解析路径
    if (!path_.empty()) {
        std::vector<std::string> baseSegs = base.parseSegments(base.path_);
        
        // 去掉 base 的最后一个 segment（文件名）
        if (!baseSegs.empty() && !base.hasAbsolutePath()) {
            // base 最后一个 segment 可能是文件名
            // 对于 resolve，base 路径保持不变，只替换最后的 segment
        }
        
        // 处理 . 和 ..
        std::vector<std::string> newSegs;
        bool first = true;
        for (auto& seg : parseSegments(path_)) {
            if (seg == ".") {
                // 当前目录，跳过
                continue;
            } else if (seg == "..") {
                // 父目录
                if (!newSegs.empty() && newSegs.back() != "..") {
                    newSegs.pop_back();
                } else if (!first || !base.hasAbsolutePath()) {
                    newSegs.push_back(seg);
                }
            } else {
                newSegs.push_back(seg);
            }
            first = false;
        }
        
        // 构建新路径
        std::ostringstream oss;
        if (base.hasAbsolutePath() && newSegs.empty()) {
            oss << "/";
        } else if (base.hasAbsolutePath() && !baseSegs.empty()) {
            // 从 base 的目录开始
            for (size_t i = 0; i < baseSegs.size() - 1; ++i) {
                oss << "/" << baseSegs[i];
            }
        }
        for (auto& seg : newSegs) {
            oss << "/" << seg;
        }
        result.path_ = oss.str();
    } else {
        result.path_ = base.path_;
    }
    
    result.query_ = query_;
    result.fragment_ = fragment_;
    
    return result;
}

URI URI::deresolve(const URI& base) const {
    // 对齐 Java: URI.deresolve(URI base)
    // 如果是相对 URI 或 base 不是有效的 base，直接返回
    if (isRelative() || !base.isHierarchical() || base.isRelative()) return *this;
    
    // 比较 scheme
    if (scheme_ != base.scheme_) return *this;
    
    // 比较 device
    if (device_ != base.device_) return *this;
    
    // 比较路径前缀
    auto baseSegs = parseSegments(base.path_);
    auto mySegs = parseSegments(path_);
    
    // 找到共同前缀
    size_t commonCount = 0;
    size_t minCount = std::min(baseSegs.size(), mySegs.size());
    for (size_t i = 0; i < minCount; ++i) {
        if (baseSegs[i] == mySegs[i]) {
            commonCount++;
        } else {
            break;
        }
    }
    
    // 构建相对路径
    std::ostringstream oss;
    for (size_t i = commonCount; i < baseSegs.size(); ++i) {
        oss << "../";
    }
    for (size_t i = commonCount; i < mySegs.size(); ++i) {
        oss << mySegs[i];
        if (i + 1 < mySegs.size()) oss << "/";
    }
    
    URI result;
    result.path_ = oss.str();
    result.query_ = query_;
    result.fragment_ = fragment_;
    
    return result;
}

URI URI::createFileURI(const std::string& path) {
    URI u;
    std::string p = path;

    // 处理绝对路径和相对路径
    if (p.empty()) {
        return u;
    }

    // 对齐 Java EMF: file URI 形如 "file:///absolute/path"（含空 authority "//"）
    // 必须设置 hasAuthority_=true，否则 toString() 输出 "file:/path"（单斜杠），
    // 重解析时会被当作 opaque URI（因为构造器要求 scheme 后跟 "//" 才算层级），
    // 导致 toFilePath() 返回空、跨文件 demand-load 失败。

    // Windows 路径处理
    if (p.size() >= 2 && p[1] == ':') {
        // Windows 绝对路径，如 C:/path
        u.scheme_ = "file";
        u.hasAuthority_ = true;
        u.device_ = p.substr(0, 2);
        u.path_ = p.substr(2);
        if (!u.path_.empty() && u.path_[0] != '/') u.path_ = "/" + u.path_;
        return u;
    }

    // Unix 绝对路径
    if (p[0] == '/') {
        u.scheme_ = "file";
        u.hasAuthority_ = true;
        u.path_ = p;
        return u;
    }

    // 相对路径：解析为绝对路径（对齐 Java java.io.File.getAbsolutePath()）
    // 必须解析为绝对 file: URI，否则 URI.resolve(base) 在 base 为相对 URI 时
    // 会直接返回原值（对齐 Java URI.resolve 要求 base 绝对），导致跨文件
    // href 的相对路径无法解析。
    try {
        std::filesystem::path cwd = std::filesystem::current_path();
        std::filesystem::path abs = cwd / p;
        u.scheme_ = "file";
        u.hasAuthority_ = true;
        u.path_ = abs.string();
        return u;
    } catch (...) {
        // 取不到 cwd 时退化为相对 URI（无 scheme）
        u.scheme_.clear();
        u.path_ = p;
        return u;
    }
}

URI URI::createPlatformURI(const std::string& path) {
    URI u;
    u.scheme_ = "platform";
    u.path_ = path.empty() ? "/" : (path[0] == '/' ? path : "/" + path);
    return u;
}

void URI::parse(std::string s) {
    if (s.empty()) return;
    auto fp = s.find('#');
    if (fp != std::string::npos) { fragment_ = s.substr(fp + 1); s = s.substr(0, fp); }
    auto qp = s.find('?');
    if (qp != std::string::npos) { query_ = s.substr(qp + 1); s = s.substr(0, qp); }
    auto cp = s.find(':');
    if (cp != std::string::npos) {
        bool isScheme = cp > 0 && std::isalpha(static_cast<unsigned char>(s[0]));
        for (size_t i = 1; isScheme && i < cp; ++i) {
            char c = s[i];
            if (!std::isalnum(static_cast<unsigned char>(c)) && c != '+' && c != '-' && c != '.') {
                isScheme = false;
            }
        }
        if (isScheme) {
            scheme_ = s.substr(0, cp);
            s = s.substr(cp + 1);
            if (!s.empty() && s[0] == '/' && s.size() > 1 && s[1] == '/') {
                hasAuthority_ = true;
                auto ap = s.find('/', 2);
                std::string authority = (ap == std::string::npos) ? s.substr(2) : s.substr(2, ap - 2);
                auto at = authority.find('@');
                std::string hostInfo = (at == std::string::npos) ? authority : authority.substr(at + 1);
                auto bang = hostInfo.find("!/");
                if (bang != std::string::npos) {
                    isArchive_ = true;
                    std::string first = hostInfo.substr(0, bang);
                    std::string rest  = hostInfo.substr(bang + 1);
                    if (first.size() >= 2 && first[0] == '/' && first[1] == '/') first = first.substr(2);
                    if (first.size() >= 2 && first[0] == '/' && first[1] == '/') first = first.substr(2);
                    if (first.size() >= 2 && std::isalpha(static_cast<unsigned char>(first[0])) && first[1] == ':') {
                        device_ = first.substr(0, 2);
                        first = first.substr(2);
                    }
                    // authority 段只到 hostInfo 前缀，archive 部分是 path
                    authority_ = hostInfo.substr(0, bang);
                    if (!first.empty() && first[0] != '/') first = "/" + first;
                    path_ = first + rest;
                } else {
                    if (hostInfo.size() >= 2 && hostInfo[0] == '/' && hostInfo[1] == '/') hostInfo = hostInfo.substr(2);
                    if (hostInfo.size() >= 2 && std::isalpha(static_cast<unsigned char>(hostInfo[0])) && hostInfo[1] == ':') {
                        device_ = hostInfo.substr(0, 2);
                        hostInfo = hostInfo.substr(2);
                    }
                    // 关键：把 authority 单独存到 authority_，path_ 不包含 authority
                    // 也就是 path_ 才是 Java URI 意义上的 path（以 / 开头或不以 / 开头）
                    authority_ = hostInfo;
                    path_ = "";  // path 暂存，剩下的 '/foo' 会在下面拼接
                }
                if (ap != std::string::npos) path_ += s.substr(ap);
            } else {
                schemeOpaque_ = true;
                opaque_ = s;
            }
        } else {
            path_ = s;
        }
    } else {
        path_ = s;
    }
}

}  // namespace emf::common
