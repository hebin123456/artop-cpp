// SegmentSequence.h
// 对齐 org.eclipse.emf.common.util.SegmentSequence (Java)
//
// 内存高效地存储一个以分隔符分割的字符串段序列。
// 同一 (delimiter, segments) 组合只会存在一个实例（pool 内部）。
// 哈希码与该序列的字符串表示的哈希码相同。
//
// 设计要点（C++ 与 Java 的差异）：
//   - Java 的 SegmentSequence 是 final + CharSequence，且所有段是不可变的
//     String[]；C++ 端使用 std::shared_ptr<const SegmentSequence>，并在 POOL
//     中以 std::unordered_map 做内部化（hash-consing）来保留 Java 的
//     "== 可用于等价判断" 的语义（通过同一 shared_ptr 复用实现）。
//   - Java 复杂的 AccessUnit/Pool 池化机制在 C++ 端简化为单个静态 map
//     （POOL）+ 互斥锁，但对外行为保持一致。
//   - Java 端的 WeakReference<String> toString 缓存在 C++ 端用 mutable
//     std::shared_ptr<const std::string> 替代。
#pragma once

#include <cstddef>
#include <cstdint>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

namespace emf::common::util {

class SegmentSequence;
using SegmentSequencePtr = std::shared_ptr<const SegmentSequence>;

/**
 * A memory efficient structure to store a sequence of delimited string segments.
 * 对齐 Java: org.eclipse.emf.common.util.SegmentSequence
 *
 * SegmentSequence 实现 java.lang.CharSequence；
 * C++ 端通过 segment_count()/segment()/toString()/length()/charAt()/subSequence() 体现。
 */
class SegmentSequence {
public:
    /**
     * 静态工厂：返回按给定 delimiter 拆分 value 得到的 SegmentSequence。
     * 等价 Java SegmentSequence.create(String delimiter, String value)。
     */
    static SegmentSequencePtr create(const std::string& delimiter, const std::string& value);

    /**
     * 静态工厂：返回对应 delimiter 的空 SegmentSequence。
     * 等价 Java SegmentSequence.create(String delimiter)。
     */
    static SegmentSequencePtr create(const std::string& delimiter);

    /**
     * 静态工厂：把若干段构造成一个 SegmentSequence；如果某段包含 delimiter，
     * 会被按 delimiter 进一步拆分；如果 delimiter 为空，会丢弃空段。
     * 等价 Java SegmentSequence.create(String delimiter, String... segments)。
     */
    static SegmentSequencePtr create(const std::string& delimiter, const std::vector<std::string>& segments);

    /**
     * Builder 工厂，对应 Java newBuilder(String delimiter) / newBuilder(String, int)。
     */
    class Builder;
    static std::shared_ptr<Builder> newBuilder(const std::string& delimiter);
    static std::shared_ptr<Builder> newBuilder(const std::string& delimiter, int capacity);

    /**
     * 不可变 list 视图（read-only），对齐 Java UnmodifiableArrayList。
     */
    template <typename E>
    class UnmodifiableArrayList;

    /**
     * 不可变 sublist 视图（read-only），对齐 Java UnmodifiableArraySubList。
     */
    template <typename E>
    class UnmodifiableArraySubList;

    /**
     * 默认构造得到一个空 sequence（delimiter 为 "/"）。
     * 注意：Java 端没有公开的无参构造，但为了 C++ 端使用方便提供一个。
     * 等价于 create("/")。
     */
    SegmentSequence() = default;
    SegmentSequence(const SegmentSequence&) = default;
    // 注：mutex 成员使隐式 move 构造被删除，故显式删除之；SegmentSequence
    // 仅通过 shared_ptr<const SegmentSequence> 共享，依赖拷贝语义即可。
    SegmentSequence(SegmentSequence&&) noexcept = delete;
    SegmentSequence& operator=(const SegmentSequence&) = default;
    SegmentSequence& operator=(SegmentSequence&&) noexcept = delete;

    // === 公共查询（对齐 Java SegmentSequence 公共 API） ===

    const std::string& delimiter() const noexcept { return delimiter_; }

    /** 返回 segments 的拷贝（对齐 Java segments()）。 */
    std::vector<std::string> segments() const;

    /** 返回 [start, end) 范围的 segments 拷贝（对齐 Java subSegments(int, int)）。 */
    std::vector<std::string> subSegments(int start, int end) const;

    /** 返回 [start, end) 范围内的 segmentsList 视图。 */
    std::shared_ptr<UnmodifiableArrayList<std::string>> subSegmentsList(int start, int end) const;

    /** 返回整个 segments 的 list 视图。 */
    std::shared_ptr<UnmodifiableArrayList<std::string>> segmentsList() const;

    int segmentCount() const noexcept { return static_cast<int>(segments_.size()); }

    const std::string& segment(int index) const;

    /** 最后一段；如果没有则返回空串。 */
    const std::string& lastSegment() const noexcept;

    /** 第一段；如果没有则返回空串。 */
    const std::string& firstSegment() const noexcept;

    int hashCode() const noexcept { return hashCode_; }

    // === CharSequence 接口（Java implements CharSequence） ===

    /** 对应 Java length()：返回该序列字符串表示的字符数。 */
    std::size_t length() const;

    char charAt(std::size_t index) const;

    std::string subSequence(std::size_t start, std::size_t end) const;

    /** Java toString()：返回分隔符连接的字符串表示。 */
    std::string toString() const;

    // === 追加（返回新实例，不修改自身） ===

    SegmentSequencePtr append(const SegmentSequence& other) const;
    SegmentSequencePtr append(const std::string& segment) const;
    SegmentSequencePtr append(const std::vector<std::string>& newSegments) const;

    // === 友元：Builder / Pool 内部使用 ===

    // 构造时给定 delimiter / segments / hashCode。
    SegmentSequence(std::string delimiter,
                    std::vector<std::string> segments,
                    int hashCode)
        : delimiter_(std::move(delimiter)),
          segments_(std::move(segments)),
          hashCode_(hashCode) {}

private:
    std::string delimiter_ = "/";
    std::vector<std::string> segments_;
    int hashCode_ = 0;
    // 缓存 toString 结果（mutable，便于 const 风格 toString() 复用）。
    mutable std::shared_ptr<const std::string> cachedToString_;
    // mutex 用 shared_ptr 包装，使得 SegmentSequence 可移动
    mutable std::shared_ptr<std::mutex> toStringMutex_ = std::make_shared<std::mutex>();
};

// ============================================================
//  UnmodifiableArrayList / UnmodifiableArraySubList
//  对齐 Java SegmentSequence.UnmodifiableArrayList / UnmodifiableArraySubList
// ============================================================

template <typename E>
class SegmentSequence::UnmodifiableArrayList {
public:
    explicit UnmodifiableArrayList(const std::vector<E>& array) : array_(array) {}
    std::size_t size() const noexcept { return array_.size(); }
    const E& get(std::size_t index) const {
        if (index >= array_.size()) {
            throw std::out_of_range("UnmodifiableArrayList::get index out of range");
        }
        return array_[index];
    }
    typename std::vector<E>::const_iterator begin() const noexcept { return array_.begin(); }
    typename std::vector<E>::const_iterator end() const noexcept { return array_.end(); }
private:
    std::vector<E> array_;
};

template <typename E>
class SegmentSequence::UnmodifiableArraySubList {
public:
    UnmodifiableArraySubList(const std::vector<E>& array, std::size_t start, std::size_t end)
        : array_(array), start_(start), end_(end) {
        if (start > array.size() || end > array.size() || start > end) {
            throw std::out_of_range("UnmodifiableArraySubList ctor: bad range");
        }
    }
    std::size_t size() const noexcept { return end_ - start_; }
    const E& get(std::size_t index) const {
        if (index > end_ - start_) {
            throw std::out_of_range("UnmodifiableArraySubList::get index out of range");
        }
        return array_[start_ + index];
    }
    typename std::vector<E>::const_iterator begin() const noexcept { return array_.begin() + start_; }
    typename std::vector<E>::const_iterator end() const noexcept { return array_.begin() + end_; }
private:
    std::vector<E> array_;
    std::size_t start_;
    std::size_t end_;
};

// ============================================================
//  Builder —— 对齐 Java SegmentSequence.Builder
// ============================================================

class SegmentSequence::Builder {
public:
    Builder(const std::string& delimiter, int capacity)
        : delimiter_(delimiter), strings_(static_cast<std::size_t>(std::max(0, capacity))) {
        if (delimiter_.empty() && strings_.empty()) {
            // 与 Java 端语义保持一致：非空 delimiter 或 0 长度都允许
        }
    }

    /** 添加一个字符串。null 在 Java 端会被替换为 "null"，此处按相同语义处理。 */
    Builder& append(const std::string& s) {
        if (size_ == strings_.size()) {
            std::size_t newCap = strings_.empty() ? 4 : 2 * strings_.size();
            strings_.resize(newCap);
        }
        strings_[size_++] = s;
        return *this;
    }

    /** 添加一个字符（按 1 字符串写入）。 */
    Builder& append(char c) {
        std::string s(1, c);
        return append(s);
    }

    /** 翻转已添加的字符串顺序。 */
    Builder& reverse() {
        for (std::size_t i = 0, j = size_ - 1; i < j; ++i, --j) {
            std::swap(strings_[i], strings_[j]);
        }
        return *this;
    }

    int size() const noexcept { return static_cast<int>(size_); }

    /** 转换为对应的 SegmentSequence（内部化）。 */
    SegmentSequencePtr toSegmentSequence() const {
        std::vector<std::string> sub(strings_.begin(), strings_.begin() + size_);
        return SegmentSequence::create(delimiter_, sub);
    }

    /** toString：返回 delimiter 连接的字符串。 */
    std::string toString() const {
        if (size_ == 0) return std::string();
        if (size_ == 1) return strings_[0];
        std::size_t total = 0;
        std::size_t delimLen = delimiter_.size();
        total += strings_[0].size();
        for (std::size_t i = 1; i < size_; ++i) {
            total += delimLen + strings_[i].size();
        }
        std::string out;
        out.reserve(total);
        out.append(strings_[0]);
        for (std::size_t i = 1; i < size_; ++i) {
            out.append(delimiter_);
            out.append(strings_[i]);
        }
        return out;
    }

private:
    std::string delimiter_;
    std::vector<std::string> strings_;
    std::size_t size_ = 0;
};

}  // namespace emf::common::util
