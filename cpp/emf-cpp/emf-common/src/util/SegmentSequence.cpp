// SegmentSequence.cpp
// 对齐 org.eclipse.emf.common.util.SegmentSequence (Java)
#include "emf/common/util/SegmentSequence.h"

#include <algorithm>
#include <functional>
#include <sstream>
#include <utility>

namespace emf::common::util {

// ============================================================
// 内部辅助：static POOL —— 对齐 Java SegmentSequence.POOL /
//  SegmentSequencePool + StringArrayPool 的简化版。
//
//  Java 端使用复杂的 AccessUnit/Pool 机制在多线程间复用缓冲，
//  C++ 端由于有 std::string/vector 自身就能管理内存，简化为
//  一个 std::unordered_map 实现的 hash-consing。
//  同一 (delimiter, segments) 组合在 POOL 中只持有一个
//  shared_ptr<const SegmentSequence>，从而保持 Java 端
//  "== 即可判定等价" 的语义。
// ============================================================

namespace {

struct PoolKey {
    std::string delimiter;
    std::vector<std::string> segments;

    bool operator==(const PoolKey& other) const {
        return delimiter == other.delimiter && segments == other.segments;
    }
};

struct PoolKeyHash {
    std::size_t operator()(const PoolKey& k) const noexcept {
        // 与 Java 端 hashCode 的语义保持一致：
        //   initialHashCode = 1
        //   hashCode = 31 * hashCode + segmentHashCode
        //   delim 算在前/后置
        std::size_t h = 1;
        for (const auto& s : k.segments) {
            h = h * 31 + std::hash<std::string>{}(s);
        }
        // 混入 delimiter 参与哈希分布
        h = h * 31 + std::hash<std::string>{}(k.delimiter);
        return h;
    }
};

struct PoolState {
    std::unordered_map<PoolKey, SegmentSequencePtr, PoolKeyHash> entries;
    std::mutex mutex;
};

PoolState& pool() {
    static PoolState p;
    return p;
}

// 计算 Java 风格的 hashCode：
//   0 segments    -> 0
//   1 segment     -> segment.hashCode()
//   > 1 segments  -> 拼接 (delimiter) 的 String 风格 hashCode
//                       hash = 31 * hash + segment.hashCode()
//  （与 Java String.hashCode() 一致：s[0]*31^(n-1) + s[1]*31^(n-2) + ...）
int computeHashCode(const std::string& delimiter, const std::vector<std::string>& segments) {
    if (segments.empty()) return 0;
    if (segments.size() == 1) {
        return static_cast<int>(std::hash<std::string>{}(segments[0]));
    }
    int h = 1;
    for (const auto& s : segments) {
        h = h * 31 + static_cast<int>(std::hash<std::string>{}(s));
    }
    // 把 delimiter 的影响混入 hashCode（与 Java String 一致），
    // 这里采用"插入 delimiter 一次"的简化方式。
    int dHash = static_cast<int>(std::hash<std::string>{}(delimiter));
    h = h * 31 + dHash;
    return h;
}

// 对应 Java protected static String[] split(delimiter, segments, length)：
//   - length == 0 -> EMPTY_ARRAY
//   - delimiter 为空 -> 丢弃空段
//   - 否则 -> 对包含 delimiter 的段递归拆分
std::vector<std::string> splitSegments(const std::string& delimiter,
                                       const std::vector<std::string>& segments,
                                       std::size_t length) {
    if (length == 0) return {};
    if (delimiter.empty()) {
        // 过滤空段
        std::vector<std::string> out;
        out.reserve(length);
        for (std::size_t i = 0; i < length; ++i) {
            if (!segments[i].empty()) out.push_back(segments[i]);
        }
        return out;
    }
    std::vector<std::string> out;
    out.reserve(length);
    for (std::size_t i = 0; i < length; ++i) {
        const std::string& seg = segments[i];
        if (seg.find(delimiter) == std::string::npos) {
            out.push_back(seg);
        } else {
            // 递归 create，再取出其 segments
            auto sub = SegmentSequence::create(delimiter, seg);
            for (const auto& s : sub->segments()) {
                out.push_back(s);
            }
        }
    }
    return out;
}

// intern：从 POOL 中获取或新建一个与 (delimiter, segments) 关联的实例。
SegmentSequencePtr internSegments(const std::string& delimiter,
                                  std::vector<std::string> segments) {
    int h = computeHashCode(delimiter, segments);
    PoolKey key{delimiter, segments};
    auto& p = pool();
    std::lock_guard<std::mutex> lock(p.mutex);
    auto it = p.entries.find(key);
    if (it != p.entries.end()) {
        return it->second;
    }
    auto ptr = std::shared_ptr<SegmentSequence>(new SegmentSequence(delimiter, std::move(segments), h));
    p.entries.emplace(std::move(key), ptr);
    return ptr;
}

// lookUp：在 POOL 中查找 (delimiter, segments) 已有的实例；若不存在返回 nullptr。
// 对应 Java POOL.intern 在已存在场景下"返回原 POOL entry"的行为。
SegmentSequencePtr lookUp(const std::string& delimiter,
                          const std::vector<std::string>& segments) {
    PoolKey key{delimiter, segments};
    auto& p = pool();
    std::lock_guard<std::mutex> lock(p.mutex);
    auto it = p.entries.find(key);
    if (it != p.entries.end()) return it->second;
    return nullptr;
}

}  // namespace

// ============================================================
// 公开静态工厂
// ============================================================

SegmentSequencePtr SegmentSequence::create(const std::string& delimiter,
                                            const std::string& value) {
    if (value.empty()) {
        return create(delimiter);
    }
    if (delimiter.empty()) {
        // 无分隔符：原样作为单段
        return internSegments(delimiter, std::vector<std::string>{value});
    }
    // 按 delimiter 切分
    std::vector<std::string> out;
    std::size_t start = 0;
    std::size_t pos;
    while ((pos = value.find(delimiter, start)) != std::string::npos) {
        out.emplace_back(value.substr(start, pos - start));
        start = pos + delimiter.size();
    }
    out.emplace_back(value.substr(start));
    return internSegments(delimiter, std::move(out));
}

SegmentSequencePtr SegmentSequence::create(const std::string& delimiter) {
    return internSegments(delimiter, std::vector<std::string>{});
}

SegmentSequencePtr SegmentSequence::create(const std::string& delimiter,
                                            const std::vector<std::string>& segments) {
    auto split = splitSegments(delimiter, segments, segments.size());
    return internSegments(delimiter, std::move(split));
}

std::shared_ptr<SegmentSequence::Builder>
SegmentSequence::newBuilder(const std::string& delimiter) {
    return std::make_shared<Builder>(delimiter, 10);
}

std::shared_ptr<SegmentSequence::Builder>
SegmentSequence::newBuilder(const std::string& delimiter, int capacity) {
    return std::make_shared<Builder>(delimiter, capacity);
}

// ============================================================
// 段查询
// ============================================================

std::vector<std::string> SegmentSequence::segments() const {
    return segments_;  // 返回拷贝
}

std::vector<std::string> SegmentSequence::subSegments(int start, int end) const {
    if (start < 0) throw std::out_of_range("start < 0");
    if (start > static_cast<int>(segments_.size()))
        throw std::out_of_range("start > size");
    if (start > end) throw std::out_of_range("start > end");
    std::vector<std::string> out;
    out.reserve(static_cast<std::size_t>(end - start));
    for (int i = start; i < end; ++i) out.push_back(segments_[i]);
    return out;
}

std::shared_ptr<SegmentSequence::UnmodifiableArrayList<std::string>>
SegmentSequence::segmentsList() const {
    return std::make_shared<UnmodifiableArrayList<std::string>>(segments_);
}

std::shared_ptr<SegmentSequence::UnmodifiableArrayList<std::string>>
SegmentSequence::subSegmentsList(int start, int end) const {
    if (start < 0) throw std::out_of_range("start < 0");
    if (start > static_cast<int>(segments_.size()))
        throw std::out_of_range("start > size");
    if (end < start) throw std::out_of_range("end < start");
    if (end > static_cast<int>(segments_.size())) end = static_cast<int>(segments_.size());
    return std::make_shared<UnmodifiableArrayList<std::string>>(
        std::vector<std::string>(segments_.begin() + start, segments_.begin() + end));
}

const std::string& SegmentSequence::segment(int index) const {
    return segments_.at(static_cast<std::size_t>(index));
}

const std::string& SegmentSequence::lastSegment() const noexcept {
    static const std::string kEmpty;
    return segments_.empty() ? kEmpty : segments_.back();
}

const std::string& SegmentSequence::firstSegment() const noexcept {
    static const std::string kEmpty;
    return segments_.empty() ? kEmpty : segments_.front();
}

// ============================================================
// CharSequence / toString
// ============================================================

std::size_t SegmentSequence::length() const {
    auto s = toString();
    return s.size();
}

char SegmentSequence::charAt(std::size_t index) const {
    return toString().at(index);
}

std::string SegmentSequence::subSequence(std::size_t start, std::size_t end) const {
    auto s = toString();
    return s.substr(start, end - start);
}

std::string SegmentSequence::toString() const {
    {
        std::lock_guard<std::mutex> lock(*toStringMutex_);
        if (cachedToString_) return *cachedToString_;
    }
    std::string out;
    if (segments_.empty()) {
        out.clear();
    } else if (segments_.size() == 1) {
        out = segments_[0];
    } else {
        std::size_t total = 0;
        for (const auto& s : segments_) total += s.size();
        total += delimiter_.size() * (segments_.size() - 1);
        out.reserve(total);
        out.append(segments_[0]);
        for (std::size_t i = 1; i < segments_.size(); ++i) {
            out.append(delimiter_);
            out.append(segments_[i]);
        }
    }
    {
        std::lock_guard<std::mutex> lock(*toStringMutex_);
        cachedToString_ = std::make_shared<const std::string>(out);
    }
    return out;
}

// ============================================================
// append
// ============================================================

SegmentSequencePtr SegmentSequence::append(const SegmentSequence& other) const {
    if (segments_.empty()) {
        // 本为空：以本 delimiter 为准
        if (delimiter_ != other.delimiter_) {
            auto split = splitSegments(delimiter_, other.segments_, other.segments_.size());
            return internSegments(delimiter_, std::move(split));
        }
        // 直接复用 other 的实例（POOL 中已存在）
        auto found = lookUp(other.delimiter_, other.segments_);
        if (found) return found;
        return internSegments(other.delimiter_, other.segments_);
    }
    if (delimiter_ != other.delimiter_) {
        auto split = splitSegments(delimiter_, other.segments_, other.segments_.size());
        std::vector<std::string> merged;
        merged.reserve(segments_.size() + split.size());
        for (const auto& s : segments_) merged.push_back(s);
        for (const auto& s : split) merged.push_back(s);
        return internSegments(delimiter_, std::move(merged));
    }
    std::vector<std::string> merged;
    merged.reserve(segments_.size() + other.segments_.size());
    for (const auto& s : segments_) merged.push_back(s);
    for (const auto& s : other.segments_) merged.push_back(s);
    return internSegments(delimiter_, std::move(merged));
}

SegmentSequencePtr SegmentSequence::append(const std::string& segment) const {
    if (segments_.empty()) {
        return create(delimiter_, segment);
    }
    if (segment.find(delimiter_) != std::string::npos) {
        auto sub = create(delimiter_, segment);
        return append(*sub);
    }
    std::vector<std::string> merged = segments_;
    merged.push_back(segment);
    return internSegments(delimiter_, std::move(merged));
}

SegmentSequencePtr SegmentSequence::append(const std::vector<std::string>& newSegments) const {
    if (segments_.empty()) {
        return create(delimiter_, newSegments);
    }
    auto split = splitSegments(delimiter_, newSegments, newSegments.size());
    std::vector<std::string> merged;
    merged.reserve(segments_.size() + split.size());
    for (const auto& s : segments_) merged.push_back(s);
    for (const auto& s : split) merged.push_back(s);
    return internSegments(delimiter_, std::move(merged));
}

}  // namespace emf::common::util
