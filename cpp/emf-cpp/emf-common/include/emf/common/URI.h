// EMF Common: URI
// 对齐 org.eclipse.emf.common.util.URI (Java)
#pragma once

#include <string>
#include <cctype>
#include <vector>
#include <algorithm>

namespace emf::common {

class URI {
public:
    URI() = default;
    explicit URI(std::string s) { parse(std::move(s)); }

    bool isEmpty() const { return scheme_.empty() && path_.empty() && opaque_.empty(); }
    bool isFile() const { return scheme_ == "file" || scheme_.empty(); }
    bool isPlatform() const { return scheme_ == "platform"; }
    bool isArchive() const { return isArchive_; }
    
    // 相对 URI：没有 scheme 且路径不是以 / 开头
    bool isRelative() const { return !hasScheme() && path_.empty() ? true : !path_.empty() && path_[0] != '/'; }
    
    // 是否有相对路径（包含 ./ 或 ../）
    bool hasRelativePath() const;
    
    // 是否是层级 URI（非 opaque）
    bool isHierarchical() const { return !schemeOpaque_; }
    
    // 是否有绝对路径（路径以 / 开头）
    bool hasAbsolutePath() const { return !path_.empty() && path_[0] == '/'; }
    
    // 是否有 scheme
    bool hasScheme() const { return !scheme_.empty(); }
    
    const std::string& scheme() const { return scheme_; }
    const std::string& opaquePart() const { return opaque_; }
    const std::string& device() const { return device_; }
    const std::string& path() const { return path_; }
    const std::string& fragment() const { return fragment_; }
    const std::string& query() const { return query_; }
    bool isSchemeOpaque() const { return schemeOpaque_; }

    // 返回 path 的 segments（按 / 分割）
    std::vector<std::string> segments() const;
    
    // 获取 segment 数量
    int segmentCount() const { return (int)segments().size(); }
    
    // 获取指定 segment
    std::string segment(int index) const;

    std::string toFilePath() const {
        if (scheme_ == "file" || scheme_.empty()) {
            std::string p = path_;
            if (device_.size() == 2 && device_[0] == '/') p = device_ + p;
            return p;
        }
        return toString();
    }

    std::string toString() const {
        std::string out;
        if (!scheme_.empty()) { out += scheme_; out += ':'; }
        if (schemeOpaque_) {
            out += opaque_;
        } else {
            // 还原 "scheme://..." 形式：对齐 Java URI.toString()
            // 注：Java URI 在 authority 存在时输出 "//"（即便 authority 为空）
            if (hasAuthority_) out += "//";
            if (!authority_.empty()) out += authority_;
            if (!path_.empty()) out += path_;
        }
        if (!query_.empty()) { out += '?'; out += query_; }
        if (!fragment_.empty()) { out += '#'; out += fragment_; }
        return out;
    }

    // 追加 fragment
    URI appendFragment(const std::string& f) const {
        URI r = *this;
        r.fragment_ = f;
        return r;
    }
    
    // 追加 path segment
    URI appendSegment(const std::string& seg) const {
        URI r = *this;
        if (!r.path_.empty() && r.path_.back() != '/') r.path_ += '/';
        r.path_ += seg;
        return r;
    }
    
    // 去掉 fragment
    URI trimFragment() const {
        URI r = *this;
        r.fragment_.clear();
        return r;
    }
    
    // 去掉最后 n 个 path segments
    // 对齐 Java: trimSegments(int i)
    // - i < 1 返回 *this
    // - 把 segments 数减 i；如果结果 <= 0，path 变空字符串（NO_SEGMENTS 语义）
    URI trimSegments(int count) const {
        if (count < 1) return *this;
        URI r = *this;
        // 1. 拆 segments（按 / 拆，丢掉开头的空 segment）
        std::vector<std::string> segs;
        std::string p = r.path_;
        if (!p.empty() && p[0] == '/') p = p.substr(1);
        size_t start = 0;
        for (size_t i = 0; i < p.size(); ++i) {
            if (p[i] == '/') {
                if (i > start) segs.emplace_back(p.substr(start, i - start));
                start = i + 1;
            }
        }
        if (start < p.size()) segs.emplace_back(p.substr(start));
        // 2. 截掉最后 count 个
        if (count >= (int)segs.size()) {
            r.path_.clear();
        } else {
            std::string np;
            for (int i = 0; i < (int)segs.size() - count; ++i) {
                np += "/" + segs[i];
            }
            r.path_ = np;
        }
        return r;
    }
    
    // 解析相对 URI 为绝对 URI
    // 对齐 Java: URI.resolve(URI base)
    URI resolve(const URI& base) const;
    
    // 反解析绝对 URI 为相对 URI（相对于 base）
    // 对齐 Java: URI.deresolve(URI base)
    URI deresolve(const URI& base) const;

    // 判断是否为 base URI
    bool isBase() const { return isHierarchical() && !isRelative(); }

    static URI createFileURI(const std::string& path);
    static URI createURI(const std::string& s) { return URI(s); }
    static URI createPlatformURI(const std::string& path);
    
    // 简化版 createFileURI（path 可以是相对或绝对路径）
    static URI createFileURI(const char* path) { return createFileURI(std::string(path)); }

    // 判断是否相等
    bool operator==(const URI& other) const {
        return scheme_ == other.scheme_ && path_ == other.path_ && 
               fragment_ == other.fragment_ && device_ == other.device_;
    }
    bool operator!=(const URI& other) const { return !(*this == other); }

private:
    void parse(std::string s);
    
    // 内部：从 path 中提取 segments
    std::vector<std::string> parseSegments(const std::string& p) const;

    std::string scheme_;
    std::string opaque_;
    std::string device_;
    std::string authority_;  // //host 部分（不含 // 前缀）
    std::string path_;
    std::string fragment_;
    std::string query_;
    bool schemeOpaque_ = false;
    bool isArchive_ = false;
    bool hasAuthority_ = false;  // 是否有 "//authority" 段（对 toString 还原 scheme:// 形式至关重要）
};

}  // namespace emf::common
