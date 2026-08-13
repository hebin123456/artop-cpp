// EMF Compare: Diff
// 对齐 org.eclipse.emf.compare.Diff / DifferenceKind / DifferenceSource (Java)
#pragma once

#include <any>
#include <string>

namespace emf::common {
class EObject;
}

namespace emf::compare {

// 差异类型（对齐 Java DifferenceKind）
enum class DiffKind {
    ADD,
    DELETE,
    CHANGE,
    MOVE
};

// Diff 子类型（对齐 Java Diff 子类层次：AttributeChange / ReferenceChange / ElementChange）
// ELEMENT_CHANGE：整对象的 ADD/DELETE（无 feature）
// ATTRIBUTE_CHANGE：EAttribute 的 CHANGE
// REFERENCE_CHANGE：EReference 的 CHANGE/ADD/DELETE/MOVE
enum class DiffType {
    ELEMENT_CHANGE,
    ATTRIBUTE_CHANGE,
    REFERENCE_CHANGE
};

// eObjectEquals：比较两个 EObject* 是否语义相等（对齐 Java DefaultEqualityHelper）。
// 对 proxy 对象按 eProxyURI 比较（跨 resource 的 proxy 即使指针不同，
// 只要指向同一 target URI 即视为相等），避免跨文档引用产生 false diff。
// 对非 proxy 对象用指针身份比较（对齐 Java 默认 IdentityEqualityHelper）。
bool eObjectEquals(const emf::common::EObject* a, const emf::common::EObject* b);

// 差异来源（对齐 Java DifferenceSource）
enum class DifferenceSource {
    LEFT,
    RIGHT
};

// 差异状态（对齐 Java Diff.getState / DifferenceState）
// PENDING：待处理（默认）；MERGED：已合并；DISCARDED：已丢弃
// MergeEngine 合并完成后将 Diff 标记为 MERGED，供调用方跟踪合并进度。
enum class DifferenceState {
    PENDING,
    MERGED,
    DISCARDED
};

// Diff：两个 EObject 之间某 feature 的差异（对齐 Java Diff）
class Diff {
public:
    Diff() = default;
    Diff(DiffKind kind, const std::string& attr)
        : kind_(kind), attributeName_(attr) {}

    DiffKind getKind() const { return kind_; }
    void setKind(DiffKind k) { kind_ = k; }

    // 子类型（对齐 Java Diff 子类层次）
    DiffType getType() const { return type_; }
    void setType(DiffType t) { type_ = t; }

    const std::string& getAttributeName() const { return attributeName_; }
    void setAttributeName(const std::string& s) { attributeName_ = s; }

    emf::common::EObject* getLeft() const { return left_; }
    void setLeft(emf::common::EObject* o) { left_ = o; }

    emf::common::EObject* getRight() const { return right_; }
    void setRight(emf::common::EObject* o) { right_ = o; }

    DifferenceSource getSource() const { return source_; }
    void setSource(DifferenceSource s) { source_ = s; }

    // 差异状态（对齐 Java Diff.getState/setState）
    DifferenceState state() const { return state_; }
    void setState(DifferenceState s) { state_ = s; }

    // MOVE 索引（对齐 Java ReferenceChange 的 index 信息）
    // oldIndex = 元素在源端的位置，newIndex = 元素在目标端的位置
    int getOldIndex() const { return oldIndex_; }
    void setOldIndex(int i) { oldIndex_ = i; }
    int getNewIndex() const { return newIndex_; }
    void setNewIndex(int i) { newIndex_ = i; }

    // old/new value（对齐 Java AttributeChange/ReferenceChange 的 oldValue/newValue）
    // 对 ADD：newValue = 新增值；对 DELETE：oldValue = 删除值
    // 对 CHANGE：oldValue = 左端值，newValue = 右端值
    // 对 MOVE：oldValue = 移动元素指针（std::any 包装 EObject*）
    const std::any& getOldValue() const { return oldValue_; }
    void setOldValue(std::any v) { oldValue_ = std::move(v); }
    const std::any& getNewValue() const { return newValue_; }
    void setNewValue(std::any v) { newValue_ = std::move(v); }

    // 所属 Match 的反向引用（对齐 Java Diff.getMatch()）
    class Match* getMatch() const { return match_; }
    void setMatch(class Match* m) { match_ = m; }

private:
    DiffKind kind_ = DiffKind::CHANGE;
    DiffType type_ = DiffType::ELEMENT_CHANGE;
    std::string attributeName_;
    emf::common::EObject* left_ = nullptr;
    emf::common::EObject* right_ = nullptr;
    DifferenceSource source_ = DifferenceSource::RIGHT;
    int oldIndex_ = -1;   // MOVE: 源端位置
    int newIndex_ = -1;   // MOVE: 目标端位置
    DifferenceState state_ = DifferenceState::PENDING;  // 差异状态（默认 PENDING）
    std::any oldValue_;   // 旧值
    std::any newValue_;   // 新值
    class Match* match_ = nullptr;  // 反向引用
};

}  // namespace emf::compare
