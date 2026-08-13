// SegmentSequence 单元测试
// 对应 Java: org.eclipse.emf.common.util.SegmentSequence
#include "test_main.h"
#include "emf/common/util/SegmentSequence.h"

#include <string>
#include <vector>

using emf::common::util::SegmentSequence;
using emf::common::util::SegmentSequencePtr;

// 1) 空序列
EMF_TEST(SegmentSequence_Empty) {
    auto seq = SegmentSequence::create("/");
    EXPECT_EQ(seq->segmentCount(), 0);
    EXPECT_EQ(seq->toString(), std::string(""));
    EXPECT_EQ(seq->length(), std::size_t(0));
    EXPECT_EQ(seq->delimiter(), std::string("/"));
    EXPECT_TRUE(seq->lastSegment().empty());
    EXPECT_TRUE(seq->firstSegment().empty());
}

// 2) 简单 toString
EMF_TEST(SegmentSequence_ToString_Simple) {
    auto seq = SegmentSequence::create("/", std::string("a/b/c"));
    EXPECT_EQ(seq->segmentCount(), 3);
    EXPECT_EQ(seq->toString(), std::string("a/b/c"));
    EXPECT_EQ(seq->length(), std::size_t(5));
}

// 3) 单段（无 delimiter）
EMF_TEST(SegmentSequence_NoDelimiter) {
    auto seq = SegmentSequence::create("", std::string("hello"));
    EXPECT_EQ(seq->segmentCount(), 1);
    EXPECT_EQ(seq->segment(0), std::string("hello"));
    EXPECT_EQ(seq->toString(), std::string("hello"));
}

// 4) segment(int) / firstSegment / lastSegment
EMF_TEST(SegmentSequence_SegmentAccess) {
    auto seq = SegmentSequence::create("/", std::string("a/b/c"));
    EXPECT_EQ(seq->segmentCount(), 3);
    EXPECT_EQ(seq->segment(0), std::string("a"));
    EXPECT_EQ(seq->segment(1), std::string("b"));
    EXPECT_EQ(seq->segment(2), std::string("c"));
    EXPECT_EQ(seq->firstSegment(), std::string("a"));
    EXPECT_EQ(seq->lastSegment(), std::string("c"));
}

// 5) segments() 返回拷贝
EMF_TEST(SegmentSequence_SegmentsCopy) {
    auto seq = SegmentSequence::create("/", std::string("x/y/z"));
    auto segs = seq->segments();
    EXPECT_EQ(segs.size(), std::size_t(3));
    EXPECT_EQ(segs[0], std::string("x"));
    EXPECT_EQ(segs[1], std::string("y"));
    EXPECT_EQ(segs[2], std::string("z"));
}

// 6) subSegments
EMF_TEST(SegmentSequence_SubSegments) {
    auto seq = SegmentSequence::create("/", std::string("a/b/c/d"));
    auto sub = seq->subSegments(1, 3);
    EXPECT_EQ(sub.size(), std::size_t(2));
    EXPECT_EQ(sub[0], std::string("b"));
    EXPECT_EQ(sub[1], std::string("c"));
}

// 7) CharSequence 接口
EMF_TEST(SegmentSequence_CharSequence) {
    auto seq = SegmentSequence::create("/", std::string("foo/bar"));
    EXPECT_EQ(seq->charAt(0), 'f');
    EXPECT_EQ(seq->charAt(3), '/');
    EXPECT_EQ(seq->charAt(4), 'b');
    EXPECT_EQ(seq->subSequence(0, 3), std::string("foo"));
    EXPECT_EQ(seq->subSequence(4, 7), std::string("bar"));
}

// 8) create(delimiter, segments...)
EMF_TEST(SegmentSequence_CreateVararg) {
    std::vector<std::string> segs{"alpha", "beta", "gamma"};
    auto seq = SegmentSequence::create("/", segs);
    EXPECT_EQ(seq->segmentCount(), 3);
    EXPECT_EQ(seq->segment(1), std::string("beta"));
    EXPECT_EQ(seq->toString(), std::string("alpha/beta/gamma"));
}

// 9) create 拆分含 delimiter 的段
EMF_TEST(SegmentSequence_CreateSplitSegment) {
    std::vector<std::string> segs{"foo/bar", "baz"};
    auto seq = SegmentSequence::create("/", segs);
    EXPECT_EQ(seq->segmentCount(), 3);
    EXPECT_EQ(seq->toString(), std::string("foo/bar/baz"));
}

// 10) append(string) 简单追加
EMF_TEST(SegmentSequence_AppendString) {
    auto seq = SegmentSequence::create("/", std::string("a/b"));
    auto seq2 = seq->append(std::string("c"));
    EXPECT_EQ(seq2->segmentCount(), 3);
    EXPECT_EQ(seq2->toString(), std::string("a/b/c"));
    // 原序列不变
    EXPECT_EQ(seq->segmentCount(), 2);
}

// 11) append(string) 自动拆分含 delimiter 的段
EMF_TEST(SegmentSequence_AppendStringWithDelimiter) {
    auto seq = SegmentSequence::create("/", std::string("a"));
    auto seq2 = seq->append(std::string("b/c"));
    EXPECT_EQ(seq2->segmentCount(), 3);
    EXPECT_EQ(seq2->toString(), std::string("a/b/c"));
}

// 12) append(SegmentSequence)
EMF_TEST(SegmentSequence_AppendSegmentSequence) {
    auto a = SegmentSequence::create("/", std::string("a/b"));
    auto b = SegmentSequence::create("/", std::string("c/d"));
    auto c = a->append(*b);
    EXPECT_EQ(c->segmentCount(), 4);
    EXPECT_EQ(c->toString(), std::string("a/b/c/d"));
}

// 13) append(SegmentSequence) delimiter 不一致时
EMF_TEST(SegmentSequence_AppendDifferentDelimiter) {
    auto a = SegmentSequence::create("/", std::string("a/b"));
    auto b = SegmentSequence::create(".", std::string("c.d"));
    auto c = a->append(*b);
    // a 的 delimiter 是 "/"，b 的 segments "c.d" 会被按 "/" 拆；
    // 但 "c.d" 不含 "/"，所以不会拆；最终合并为 ["a","b","c","d"] -> "a/b/c/d"
    EXPECT_EQ(c->delimiter(), std::string("/"));
    EXPECT_EQ(c->toString(), std::string("a/b/c/d"));
}

// 14) append(vector) 多段一次性追加
EMF_TEST(SegmentSequence_AppendVector) {
    auto seq = SegmentSequence::create("/", std::string("a"));
    std::vector<std::string> more{"b", "c", "d"};
    auto seq2 = seq->append(more);
    EXPECT_EQ(seq2->segmentCount(), 4);
    EXPECT_EQ(seq2->toString(), std::string("a/b/c/d"));
}

// 15) hashCode 一致性（同样内容应该有同样的 hashCode）
EMF_TEST(SegmentSequence_HashCodeConsistent) {
    auto a = SegmentSequence::create("/", std::string("x/y/z"));
    auto b = SegmentSequence::create("/", std::string("x/y/z"));
    EXPECT_EQ(a->hashCode(), b->hashCode());
}

// 16) POOL 内部化：同样内容应该返回同一实例（Java == 语义）
EMF_TEST(SegmentSequence_PoolIntern) {
    auto a = SegmentSequence::create("/", std::string("foo/bar"));
    auto b = SegmentSequence::create("/", std::string("foo/bar"));
    EXPECT_TRUE(a.get() == b.get());
}

// 17) segmentCount / length 一致
EMF_TEST(SegmentSequence_LengthAndCount) {
    auto seq = SegmentSequence::create("/", std::string("ab/cd/ef"));
    EXPECT_EQ(seq->segmentCount(), 3);
    EXPECT_EQ(seq->length(), std::size_t(8));
    EXPECT_EQ(seq->length(), seq->toString().size());
}

// 18) Builder: append + toSegmentSequence
EMF_TEST(SegmentSequence_Builder_Basic) {
    auto b = SegmentSequence::newBuilder("/", 4);
    b->append(std::string("a"));
    b->append(std::string("b"));
    b->append(std::string("c"));
    auto seq = b->toSegmentSequence();
    EXPECT_EQ(seq->segmentCount(), 3);
    EXPECT_EQ(seq->toString(), std::string("a/b/c"));
}

// 19) Builder: toString
EMF_TEST(SegmentSequence_Builder_ToString) {
    auto b = SegmentSequence::newBuilder("/");
    EXPECT_EQ(b->toString(), std::string(""));
    b->append(std::string("only"));
    EXPECT_EQ(b->toString(), std::string("only"));
    b->append(std::string("more"));
    EXPECT_EQ(b->toString(), std::string("only/more"));
}

// 20) Builder: append(char)
EMF_TEST(SegmentSequence_Builder_AppendChar) {
    auto b = SegmentSequence::newBuilder(".");
    b->append('a');
    b->append('b');
    b->append('c');
    EXPECT_EQ(b->toString(), std::string("a.b.c"));
}

// 21) Builder: reverse
EMF_TEST(SegmentSequence_Builder_Reverse) {
    auto b = SegmentSequence::newBuilder("/");
    b->append(std::string("a"));
    b->append(std::string("b"));
    b->append(std::string("c"));
    b->reverse();
    auto seq = b->toSegmentSequence();
    EXPECT_EQ(seq->toString(), std::string("c/b/a"));
}

// 22) segmentsList 视图
EMF_TEST(SegmentSequence_SegmentsListView) {
    auto seq = SegmentSequence::create("/", std::string("p/q/r"));
    auto list = seq->segmentsList();
    EXPECT_EQ(list->size(), std::size_t(3));
    EXPECT_EQ(list->get(0), std::string("p"));
    EXPECT_EQ(list->get(2), std::string("r"));
}

// 23) subSegmentsList 视图
EMF_TEST(SegmentSequence_SubSegmentsListView) {
    auto seq = SegmentSequence::create("/", std::string("a/b/c/d"));
    auto list = seq->subSegmentsList(1, 3);
    EXPECT_EQ(list->size(), std::size_t(2));
    EXPECT_EQ(list->get(0), std::string("b"));
    EXPECT_EQ(list->get(1), std::string("c"));
}

// 24) 多次 append 形成链
EMF_TEST(SegmentSequence_AppendChain) {
    auto seq = SegmentSequence::create("/", std::string(""));
    seq = seq->append(std::string("a"));
    seq = seq->append(std::string("b"));
    seq = seq->append(std::string("c"));
    EXPECT_EQ(seq->segmentCount(), 3);
    EXPECT_EQ(seq->toString(), std::string("a/b/c"));
}

// 25) 单段序列（无 delimiter）边界
EMF_TEST(SegmentSequence_SingleSegment) {
    auto seq = SegmentSequence::create("/", std::string("only"));
    EXPECT_EQ(seq->segmentCount(), 1);
    EXPECT_EQ(seq->length(), std::size_t(4));
    EXPECT_EQ(seq->toString(), std::string("only"));
}

// 26) empty delimiter 时 create(value) 不会拆
EMF_TEST(SegmentSequence_EmptyDelimiterSingle) {
    auto seq = SegmentSequence::create("", std::string("a/b/c"));
    EXPECT_EQ(seq->segmentCount(), 1);
    EXPECT_EQ(seq->segment(0), std::string("a/b/c"));
    EXPECT_EQ(seq->toString(), std::string("a/b/c"));
}

// 27) 创建空时 segmentCount 0
EMF_TEST(SegmentSequence_CreateEmptyHasZeroCount) {
    auto seq = SegmentSequence::create("/");
    EXPECT_EQ(seq->segmentCount(), 0);
    EXPECT_EQ(seq->length(), std::size_t(0));
}

// 28) 不同 delimiter 的 POOL key 互不影响
EMF_TEST(SegmentSequence_DifferentDelimitersDistinct) {
    auto a = SegmentSequence::create("/", std::string("x"));
    auto b = SegmentSequence::create(".", std::string("x"));
    EXPECT_TRUE(a.get() != b.get());
    EXPECT_EQ(a->delimiter(), std::string("/"));
    EXPECT_EQ(b->delimiter(), std::string("."));
}
