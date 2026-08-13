// EMF Compare: Comparison / Match / Conflict
// 对齐 org.eclipse.emf.compare.Match, Comparison, Conflict (Java)
#include "emf/compare/Comparison.h"
#include "emf/compare/Diff.h"
#include "emf/compare/MatchEngine.h"
#include "emf/compare/DiffEngine.h"
#include "emf/compare/MergeEngine.h"
#include "emf/compare/EquivalenceEngine.h"  // Equivalence 计算（对齐 Java EquivalenceEngine）
#include "emf/compare/ConflictDetector.h"   // 3-way 冲突检测（对齐 Java ConflictDetector）

#include "emf/common/EObject.h"
#include "emf/common/URI.h"
#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/util/EcoreUtil.h"  // EcoreUtil::equalsValue（统一值比较）

#include <any>
#include <unordered_map>
#include <unordered_set>

namespace emf::compare {

// eObjectEquals 实现：复用 EcoreUtil::equalsValue 的 EObject* 比较逻辑。
// 语义：proxy 按 eProxyURI 比较，非 proxy 按指针身份比较
// （对齐 Java DefaultEqualityHelper：proxy 用 EcoreUtil.getURI(proxy) 比较）。
// 注：equalsValue 内部对 EObject* 已实现相同语义，这里通过 std::any 包装委托，
// 消除 compare 模块与 ecore-util 模块间的重复比较逻辑。
bool eObjectEquals(const emf::common::EObject* a, const emf::common::EObject* b) {
    return emf::ecore::util::EcoreUtil::equalsValue(
        std::any(const_cast<emf::common::EObject*>(a)),
        std::any(const_cast<emf::common::EObject*>(b)));
}

// 值比较统一复用 EcoreUtil::equalsValue（对齐 Java EqualityHelper.haveEqualValue 语义）。
// 旧 Comparison 内的本地 anyEquals 已删除以消除三处重复实现。
//
// 注：Equivalence 计算与 3-way 冲突检测已分别抽取为独立 Engine 类：
//   - EquivalenceEngine（见 emf/compare/EquivalenceEngine.h）
//   - ConflictDetector  （见 emf/compare/ConflictDetector.h）
// 原匿名命名空间内的 getEObjectList / computeSetDelta 辅助函数已随之上移至各 .cpp
// 编译单元本地副本，避免跨翻译单元链接问题。

// 顶层便利函数：2-way match + diff
Comparison compare(emf::common::EObject* left, emf::common::EObject* right) {
    Comparison comp;
    if (!left && !right) return comp;

    // 1) match
    MatchEngine me;
    me.match(left, right, comp);

    // 2) diff
    DiffEngine de;
    de.diff(comp);

    // 3) P0-8：计算 Equivalence
    EquivalenceEngine ee;
    ee.computeEquivalences(comp);

    return comp;
}

// 顶层便利函数：2-way match + diff，带外部 IdentifierProvider（artop 层注入）
Comparison compare(emf::common::EObject* left, emf::common::EObject* right,
                   const IdentifierProvider& idProvider) {
    Comparison comp;
    if (!left && !right) return comp;

    // 1) match（注入 ID provider，对齐 artop IdentifiableUtil）
    MatchEngine me;
    me.setIdentifierProvider(idProvider);
    me.match(left, right, comp);

    // 2) diff
    DiffEngine de;
    de.diff(comp);

    // 3) P0-8：计算 Equivalence
    EquivalenceEngine ee;
    ee.computeEquivalences(comp);

    return comp;
}

// 顶层便利函数：3-way match + diff + conflict（对齐 Java EMFCompare 3-way 流程）
Comparison compare(emf::common::EObject* left,
                   emf::common::EObject* right,
                   emf::common::EObject* origin) {
    Comparison comp;
    if (!left && !right && !origin) return comp;

    // 1) 3-way match（设置 origin）
    MatchEngine me;
    me.match(left, right, origin, comp);

    // 2) diff
    DiffEngine de;
    de.diff(comp);

    // 3) 冲突检测
    ConflictDetector cd;
    cd.detectConflicts(comp);

    // 4) P0-8：计算 Equivalence
    EquivalenceEngine ee;
    ee.computeEquivalences(comp);

    return comp;
}

}  // namespace emf::compare
