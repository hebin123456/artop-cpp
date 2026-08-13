// EMF Compare: Comparison / Match / Conflict
// 对齐 org.eclipse.emf.compare.Match, Comparison, Conflict (Java)
#pragma once

#include <functional>
#include <list>
#include <string>
#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::compare {

// IdentifierProvider 前置声明（定义在 MatchEngine.h，此处声明以供 compare() 重载使用）
using IdentifierProvider = std::function<std::string(emf::common::EObject*)>;

// 匹配类型（对齐 Java MatchKind）
enum class MatchKind {
    IDENTICAL,
    DIFFERENT,
    ABSENT_LEFT,
    ABSENT_RIGHT
};

class Diff;
class Match;

// 冲突类型（对齐 Java ConflictKind）
enum class ConflictKind {
    REAL,    // 真冲突：left 和 right 相对 origin 都改了同一处且语义互斥
    PSEUDO   // 伪冲突：表面冲突但实际改成了相同值
};

// Conflict：3-way 比较中的冲突（对齐 Java org.eclipse.emf.compare.Conflict）
class Conflict {
public:
    Conflict() = default;
    Conflict(ConflictKind kind) : kind_(kind) {}

    ConflictKind getKind() const { return kind_; }
    void setKind(ConflictKind k) { kind_ = k; }

    std::vector<Diff*>& getDifferences() { return diffs_; }
    const std::vector<Diff*>& getDifferences() const { return diffs_; }

private:
    ConflictKind kind_ = ConflictKind::REAL;
    std::vector<Diff*> diffs_;
};

// Equivalence：跨 containment 边界的等价关系（对齐 Java org.eclipse.emf.compare.Equivalence）
// 当两个 Match 的对象通过非 containment EReference 相互引用（或引用同一目标）时，
// 一个 Match 的 ADD/DELETE 隐含另一个 Match 的引用变更。
// 这里记录基本的等价元数据，供调用方据此推导隐含 diff（对齐 Java Equivalence.getMatchedDifferences）。
class Equivalence {
public:
    Equivalence() = default;

    std::vector<Match*>& getMatches() { return matches_; }
    const std::vector<Match*>& getMatches() const { return matches_; }

    void addMatch(Match* m) { if (m) matches_.push_back(m); }

private:
    std::vector<Match*> matches_;
};

// Dependency：差异间的依赖关系（对齐 Java org.eclipse.emf.compare.Diff.getRequires/getImplicatedBy）
// 当一个 Diff 依赖另一个 Diff 时（如 reference change 依赖 element ADD），
// 合并需按依赖顺序进行。此处用简单的有向边记录（requires_：A 需要 B 先合并）。
class Dependency {
public:
    Dependency() = default;
    Dependency(Diff* source, Diff* target) : source_(source), target_(target) {}

    Diff* getSource() const { return source_; }
    Diff* getTarget() const { return target_; }

private:
    Diff* source_ = nullptr;  // 依赖方
    Diff* target_ = nullptr;  // 被依赖方
};

// Match：左右（及 origin）EObject 的对应关系（对齐 Java org.eclipse.emf.compare.Match）
class Match {
public:
    Match() = default;
    Match(emf::common::EObject* left,
          emf::common::EObject* right,
          MatchKind kind,
          double similarity)
        : left_(left), right_(right), kind_(kind), similarity_(similarity) {}

    emf::common::EObject* getLeft() const { return left_; }
    void setLeft(emf::common::EObject* o) { left_ = o; }

    emf::common::EObject* getRight() const { return right_; }
    void setRight(emf::common::EObject* o) { right_ = o; }

    // 3-way：origin（公共祖先）
    emf::common::EObject* getOrigin() const { return origin_; }
    void setOrigin(emf::common::EObject* o) { origin_ = o; }

    MatchKind getKind() const { return kind_; }
    void setKind(MatchKind k) { kind_ = k; }

    double getSimilarity() const { return similarity_; }
    void setSimilarity(double s) { similarity_ = s; }

    std::vector<Diff*>& getDiffs() { return diffs_; }
    const std::vector<Diff*>& getDiffs() const { return diffs_; }

private:
    emf::common::EObject* left_ = nullptr;
    emf::common::EObject* right_ = nullptr;
    emf::common::EObject* origin_ = nullptr;  // 3-way origin
    MatchKind kind_ = MatchKind::IDENTICAL;
    double similarity_ = 0.0;
    std::vector<Diff*> diffs_;
};

// Comparison：一次比对的结果集（对齐 Java org.eclipse.emf.compare.Comparison）
class Comparison {
public:
    // 添加一个 Match，返回其引用（用 std::list 保持引用/指针稳定）
    Match& addMatch(emf::common::EObject* left,
                    emf::common::EObject* right,
                    MatchKind kind,
                    double similarity) {
        matches_.emplace_back(left, right, kind, similarity);
        return matches_.back();
    }

    std::list<Match>& getMatches() { return matches_; }
    const std::list<Match>& getMatches() const { return matches_; }

    std::vector<Diff*>& getDifferences() { return differences_; }
    const std::vector<Diff*>& getDifferences() const { return differences_; }

    // 3-way 冲突
    std::vector<Conflict>& getConflicts() { return conflicts_; }
    const std::vector<Conflict>& getConflicts() const { return conflicts_; }

    // Equivalence / Dependency（对齐 Java Equivalence / Diff.getRequires）
    std::vector<Equivalence>& getEquivalences() { return equivalences_; }
    const std::vector<Equivalence>& getEquivalences() const { return equivalences_; }

    std::vector<Dependency>& getDependencies() { return dependencies_; }
    const std::vector<Dependency>& getDependencies() const { return dependencies_; }

    bool isThreeWay() const { return threeWay_; }
    void setThreeWay(bool b) { threeWay_ = b; }

    void clear() {
        matches_.clear();
        differences_.clear();
        conflicts_.clear();
        equivalences_.clear();
        dependencies_.clear();
    }

private:
    std::list<Match> matches_;
    std::vector<Diff*> differences_;
    std::vector<Conflict> conflicts_;
    std::vector<Equivalence> equivalences_;
    std::vector<Dependency> dependencies_;
    bool threeWay_ = false;
};

// 顶层便利函数：2-way match + diff
Comparison compare(emf::common::EObject* left, emf::common::EObject* right);

// 顶层便利函数：2-way match + diff，带外部 IdentifierProvider（artop 层注入）
// 对齐 artop IdentifiableUtil：provider 返回 shortName/uuid 作 ID，避免大文件 proximity 爆炸
Comparison compare(emf::common::EObject* left, emf::common::EObject* right,
                   const IdentifierProvider& idProvider);

// 顶层便利函数：3-way match + diff + conflict
Comparison compare(emf::common::EObject* left,
                   emf::common::EObject* right,
                   emf::common::EObject* origin);

}  // namespace emf::compare
