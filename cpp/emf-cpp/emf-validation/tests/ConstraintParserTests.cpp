// ConstraintParser 单元测试
// 覆盖 OCL 子集：implies, forAll, exists, not/or/and, = / <>, if-then-else,
//               ->size/isEmpty/notEmpty, 路径导航, 字面量, 容错。
// 对齐 Eclipse OCL / EMF Validation OCL 求值语义。
#include "test_main.h"
#include "emf/validation/ConstraintParser.h"
#include "emf/validation/Constraint.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EObject.h"

#include <any>
#include <string>
#include <vector>

using emf::xmi::XMIResource;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EFactory;
using emf::ecore::EStructuralFeature;
using emf::common::EObject;

namespace {

// 测试元模型：Container -> [Element(shortName, count, ref:Element)]
//   elements: Element[]（多值 containment，用于集合迭代测试）
//   single:   Element  （单值 containment，用于单值引用 -> 迭代测试）
const char* kEcore =
    "<?xml version=\"1.0\"?>\n"
    "<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
    "xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
    "xmi:version=\"2.0\" name=\"oclp\" nsURI=\"http://example.com/oclp/1.0\" nsPrefix=\"oclp\">"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Container\">"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"elements\" upperBound=\"-1\" "
    "eType=\"#//Element\" containment=\"true\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"single\" "
    "eType=\"#//Element\" containment=\"true\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Element\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"shortName\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"count\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"ref\" eType=\"#//Element\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"children\" upperBound=\"-1\" "
    "eType=\"#//Element\" containment=\"true\"/>"
    "</eClassifiers>"
    "</ecore:EPackage>";

struct Meta {
    EPackage* pkg = nullptr;
    EClass* containerCls = nullptr;
    EClass* elementCls = nullptr;
    EFactory* factory = nullptr;
    EStructuralFeature* fElements = nullptr;
    EStructuralFeature* fSingle = nullptr;
    EStructuralFeature* fShortName = nullptr;
    EStructuralFeature* fCount = nullptr;
    EStructuralFeature* fRef = nullptr;
    EStructuralFeature* fChildren = nullptr;
};

Meta loadMeta() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();
    XMIResource res;
    res.loadFromString(std::string(kEcore));
    Meta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();
    m.containerCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Container"));
    m.elementCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Element"));
    m.factory = m.pkg->getEFactoryInstance();
    m.fElements = m.containerCls->getEStructuralFeature("elements");
    m.fSingle = m.containerCls->getEStructuralFeature("single");
    m.fShortName = m.elementCls->getEStructuralFeature("shortName");
    m.fCount = m.elementCls->getEStructuralFeature("count");
    m.fRef = m.elementCls->getEStructuralFeature("ref");
    m.fChildren = m.elementCls->getEStructuralFeature("children");
    return m;
}

EObject* makeElement(const Meta& m, const std::string& sn, int count = 0) {
    auto* e = m.factory->create(m.elementCls);
    e->eSet(m.fShortName, std::any(sn));
    e->eSet(m.fCount, std::any(count));
    return e;
}

void setElements(EObject* container, EStructuralFeature* f, const std::vector<EObject*>& kids) {
    container->eSet(f, std::any(std::vector<EObject*>(kids)));
}

// 为 element 设置 children（嵌套集合测试用）
void setChildren(EObject* element, EStructuralFeature* f, const std::vector<EObject*>& kids) {
    element->eSet(f, std::any(std::vector<EObject*>(kids)));
}

// 便捷：编译并求值（target 非 null）
bool eval(const std::string& expr, EObject* target) {
    return emf::validation::ConstraintParser::compile(expr)(target, std::any{});
}

}  // namespace

// ===== 向后兼容：原有测试 =====

EMF_TEST(ConstraintParser_Compile_ReturnsEvaluator) {
    auto eval = emf::validation::ConstraintParser::compile("attr != null");
    EXPECT_TRUE(eval(nullptr, std::any{}));  // null target → true
}

EMF_TEST(ConstraintParser_Parse_ReturnsConstraint) {
    auto* c = emf::validation::ConstraintParser::parse("src", "MyConstraint", "x > 0",
                                                       emf::validation::Severity::WARNING);
    EXPECT_NOT_NULL(c);
    EXPECT_EQ(c->getName(), "MyConstraint");
    delete c;
}

// ===== implies（逻辑蕴含）=====

EMF_TEST(OCL_Implies_TrueImpliesFalse_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("true implies false", c));
}

EMF_TEST(OCL_Implies_FalseImpliesFalse_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("false implies false", c));
}

EMF_TEST(OCL_Implies_TrueImpliesTrue_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("true implies true", c));
}

EMF_TEST(OCL_Implies_RightAssociative) {
    // A implies B implies C = A implies (B implies C)
    // false implies false implies false = false implies (false implies false)
    //   = false implies true = true
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("false implies false implies false", c));
}

EMF_TEST(OCL_Implies_WithComparison) {
    // shortName 非空 implies ref 非空（target 是 Element，属性真实存在）
    Meta m = loadMeta();
    auto* e = makeElement(m, "x", 5);  // shortName="x" 非空, ref 未设置 → null
    // shortName <> '' → true, ref <> null → false → true implies false → false
    EXPECT_FALSE(eval("self.shortName <> '' implies self.ref <> null", e));

    auto* target = makeElement(m, "t");
    e->eSet(m.fRef, std::any(target));  // ref 已设置
    // shortName <> '' → true, ref <> null → true → true implies true → true
    EXPECT_TRUE(eval("self.shortName <> '' implies self.ref <> null", e));

    auto* e2 = makeElement(m, "", 0);  // shortName="" → 前提假
    // shortName <> '' → false → false implies (whatever) → true
    EXPECT_TRUE(eval("self.shortName <> '' implies self.ref <> null", e2));
}

// ===== forAll（全称量词）=====

EMF_TEST(OCL_ForAll_AllSatisfy_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a");
    auto* e1 = makeElement(m, "b");
    auto* e2 = makeElement(m, "c");
    setElements(c, m.fElements, {e0, e1, e2});
    // 所有元素的 shortName 非空 → true
    EXPECT_TRUE(eval("self.elements->forAll(x | x.shortName <> '')", c));
}

EMF_TEST(OCL_ForAll_OneFails_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a");
    auto* e1 = makeElement(m, "");  // 空 shortName
    auto* e2 = makeElement(m, "c");
    setElements(c, m.fElements, {e0, e1, e2});
    EXPECT_FALSE(eval("self.elements->forAll(x | x.shortName <> '')", c));
}

EMF_TEST(OCL_ForAll_EmptyCollection_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    // 空集合 → forAll 返回 true（OCL 语义）
    EXPECT_TRUE(eval("self.elements->forAll(x | x.shortName <> '')", c));
}

EMF_TEST(OCL_ForAll_NumericCondition) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a", 3);
    auto* e1 = makeElement(m, "b", 5);
    setElements(c, m.fElements, {e0, e1});
    EXPECT_TRUE(eval("self.elements->forAll(x | x.count > 0)", c));
    EXPECT_FALSE(eval("self.elements->forAll(x | x.count > 4)", c));
}

EMF_TEST(OCL_ForAll_WithImpliesInBody) {
    // 常见 AUTOSAR 模式：count > 0 的元素必须有 shortName
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a", 3);
    auto* e1 = makeElement(m, "", 0);  // count=0 → implies 前提假 → 通过
    setElements(c, m.fElements, {e0, e1});
    EXPECT_TRUE(eval("self.elements->forAll(x | x.count > 0 implies x.shortName <> '')", c));
}

// ===== exists（存在量词）=====

EMF_TEST(OCL_Exists_OneSatisfies_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a");
    auto* e1 = makeElement(m, "target");
    auto* e2 = makeElement(m, "c");
    setElements(c, m.fElements, {e0, e1, e2});
    EXPECT_TRUE(eval("self.elements->exists(x | x.shortName = 'target')", c));
}

EMF_TEST(OCL_Exists_NoneSatisfy_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a");
    auto* e1 = makeElement(m, "b");
    setElements(c, m.fElements, {e0, e1});
    EXPECT_FALSE(eval("self.elements->exists(x | x.shortName = 'target')", c));
}

EMF_TEST(OCL_Exists_EmptyCollection_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("self.elements->exists(x | x.shortName = 'target')", c));
}

// ===== 单值引用上的 -> 迭代（OCL Collection(source) 语义）=====

EMF_TEST(OCL_SingleRef_ForAll_TreatedAsSingleton) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e = makeElement(m, "only");
    c->eSet(m.fSingle, std::any(e));
    // single 是单值 containment → ->forAll 视为单元素集合
    EXPECT_TRUE(eval("self.single->forAll(x | x.shortName = 'only')", c));
}

EMF_TEST(OCL_SingleRef_Null_ForAll_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    // single 未设置 → null → 空集合 → forAll true
    EXPECT_TRUE(eval("self.single->forAll(x | x.shortName = 'only')", c));
}

EMF_TEST(OCL_SingleRef_Null_Exists_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("self.single->exists(x | x.shortName = 'only')", c));
}

// ===== 逻辑运算符 =====

EMF_TEST(OCL_And_BothTrue_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("true and true", c));
}

EMF_TEST(OCL_And_OneFalse_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("true and false", c));
}

EMF_TEST(OCL_Or_BothFalse_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("false or false", c));
}

EMF_TEST(OCL_Or_OneTrue_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("false or true", c));
}

EMF_TEST(OCL_Not_True_IsFalse) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("not true", c));
}

EMF_TEST(OCL_Not_NotFalse_IsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("not not true", c));
}

EMF_TEST(OCL_AndOrPrecedence) {
    // and 优先于 or：false or true and false = false or (true and false) = false or false = false
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("false or true and false", c));
}

EMF_TEST(OCL_ParenGrouping) {
    // (false or true) and false = true and false = false
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("(false or true) and false", c));
}

// ===== OCL 等值运算 = / <> =====

EMF_TEST(OCL_OclEquality_OpEquals) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e = makeElement(m, "foo");
    setElements(c, m.fElements, {e});
    // OCL = 等价于 ==
    EXPECT_TRUE(eval("self.elements->forAll(x | x.shortName = 'foo')", c));
}

EMF_TEST(OCL_OclInequality_OpDiamond) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e = makeElement(m, "foo");
    setElements(c, m.fElements, {e});
    // OCL <> 等价于 !=
    EXPECT_FALSE(eval("self.elements->exists(x | x.shortName <> 'foo')", c));
}

// ===== 集合操作 ->size / isEmpty / notEmpty =====

EMF_TEST(OCL_Collection_Size) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->size() = 3", c));
    EXPECT_TRUE(eval("self.elements->size() > 2", c));
    EXPECT_FALSE(eval("self.elements->size() = 2", c));
}

EMF_TEST(OCL_Collection_IsEmpty) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("self.elements->isEmpty()", c));
    setElements(c, m.fElements, {makeElement(m, "a")});
    EXPECT_FALSE(eval("self.elements->isEmpty()", c));
}

EMF_TEST(OCL_Collection_NotEmpty) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("self.elements->notEmpty()", c));
    setElements(c, m.fElements, {makeElement(m, "a")});
    EXPECT_TRUE(eval("self.elements->notEmpty()", c));
}

// ===== 路径导航 =====

EMF_TEST(OCL_PathNavigation_SelfAttr) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e = makeElement(m, "hello");
    setElements(c, m.fElements, {e});
    // self.elements->forAll(x | x.shortName <> '') — 路径 self.elements
    EXPECT_TRUE(eval("self.elements->forAll(x | x.shortName <> '')", c));
}

EMF_TEST(OCL_PathNavigation_ImplicitSelf) {
    // 裸 attr 隐式 self.attr
    Meta m = loadMeta();
    auto* e = makeElement(m, "world");
    // target 是 Element，直接引用 shortName（隐式 self.shortName）
    EXPECT_TRUE(eval("shortName = 'world'", e));
    EXPECT_FALSE(eval("shortName = 'other'", e));
}

EMF_TEST(OCL_PathNavigation_DeepPath) {
    // self.ref.shortName — 二级导航
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "first");
    auto* e1 = makeElement(m, "second");
    e0->eSet(m.fRef, std::any(e1));  // e0.ref → e1
    setElements(c, m.fElements, {e0});
    // self.elements->exists(x | x.ref.shortName = 'second')
    EXPECT_TRUE(eval("self.elements->exists(x | x.ref.shortName = 'second')", c));
    EXPECT_FALSE(eval("self.elements->exists(x | x.ref.shortName = 'first')", c));
}

EMF_TEST(OCL_DotSize_OnCollection) {
    // .size() 语法（dot 而非 arrow）也应工作
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b")});
    EXPECT_TRUE(eval("self.elements.size() = 2", c));
}

// ===== if-then-else =====

EMF_TEST(OCL_IfThenElse_ThenBranch) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("if true then true else false endif", c));
}

EMF_TEST(OCL_IfThenElse_ElseBranch) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("if false then true else false endif", c));
}

EMF_TEST(OCL_IfThenElse_WithCondition) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a")});
    // 集合非空 → then 分支
    EXPECT_TRUE(eval("if self.elements->notEmpty() then true else false endif", c));
}

// ===== null 检查 =====

EMF_TEST(OCL_NullCheck_RefNotNull) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e = makeElement(m, "a");
    auto* target = makeElement(m, "b");
    e->eSet(m.fRef, std::any(target));
    setElements(c, m.fElements, {e});
    // e.ref 已设置 → not null
    EXPECT_TRUE(eval("self.elements->forAll(x | x.ref <> null)", c));
}

EMF_TEST(OCL_NullCheck_RefIsNull) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e = makeElement(m, "a");  // ref 未设置 → null
    setElements(c, m.fElements, {e});
    EXPECT_TRUE(eval("self.elements->forAll(x | x.ref = null)", c));
    EXPECT_FALSE(eval("self.elements->forAll(x | x.ref <> null)", c));
}

// ===== 容错：解析失败返回恒 true =====

EMF_TEST(OCL_Fallback_InvalidExpression_ReturnsTrue) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    // 不支持的语法 → 容错 true
    EXPECT_TRUE(eval("this is not valid ocl @#$", c));
}

EMF_TEST(OCL_Fallback_EmptyExpression_ReturnsTrue) {
    auto eval = emf::validation::ConstraintParser::compile("");
    EXPECT_TRUE(eval(nullptr, std::any{}));
}

// ===== value 约束入参 =====

EMF_TEST(OCL_ValueParam_NumericCompare) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto eval = emf::validation::ConstraintParser::compile("value > 5");
    EXPECT_TRUE(eval(c, std::any(10)));
    EXPECT_FALSE(eval(c, std::any(3)));
}

// ===== 组合复杂表达式 =====

EMF_TEST(OCL_Complex_ForAllWithOrAndImplies) {
    // 所有元素：count > 0 或 shortName = 'zero'
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a", 3);
    auto* e1 = makeElement(m, "zero", 0);
    setElements(c, m.fElements, {e0, e1});
    EXPECT_TRUE(eval("self.elements->forAll(x | x.count > 0 or x.shortName = 'zero')", c));
}

EMF_TEST(OCL_Complex_ExistsWithNot) {
    // 存在元素：shortName 非空 且 count > 2
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "a", 1);
    auto* e1 = makeElement(m, "b", 5);
    setElements(c, m.fElements, {e0, e1});
    EXPECT_TRUE(eval("self.elements->exists(x | not x.shortName = '' and x.count > 2)", c));
    EXPECT_FALSE(eval("self.elements->exists(x | x.shortName = '' and x.count > 2)", c));
}

// ===== let / def 表达式 =====

EMF_TEST(OCL_Let_SimpleBinding_IsTrue) {
    // let x = 5 in x > 3 → true
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("let x = 5 in x > 3", c));
    EXPECT_FALSE(eval("let x = 5 in x > 10", c));
}

EMF_TEST(OCL_Let_WithOptionalTypeAnnotation) {
    // let x : Integer = 10 in x > 5 → true（类型注解解析但忽略）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("let x : Integer = 10 in x > 5", c));
}

EMF_TEST(OCL_Let_NestedBindings) {
    // let a = 1 in let b = 2 in a + b > 2 → true
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("let a = 1 in let b = 2 in a + b > 2", c));
    EXPECT_FALSE(eval("let a = 1 in let b = 2 in a + b > 5", c));
}

EMF_TEST(OCL_Let_BindsAttribute) {
    // let n = self.shortName in n.size() > 0（用测试模型 Element.shortName）
    Meta m = loadMeta();
    auto* e = makeElement(m, "hello");
    EXPECT_TRUE(eval("let n = self.shortName in n.size() > 0", e));
    EXPECT_TRUE(eval("let n = self.shortName in n = 'hello'", e));
}

EMF_TEST(OCL_Let_ScopeIsRemovedAfterBody) {
    // let 变量在 body 退出后不可见：外层 x 仍为 5
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("let x = 5 in (let y = 10 in y > 5) and x = 5", c));
}

// ===== 集合推导式（collect / select / reject / any / iterate）=====

EMF_TEST(OCL_Collect_ReturnsElementCollection) {
    // self.elements->collect(c | c)->size() > 0
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->collect(c | c)->size() > 0", c));
    EXPECT_FALSE(eval("self.elements->collect(c | c)->isEmpty()", c));
}

EMF_TEST(OCL_Collect_PullsAttribute) {
    // self.elements->collect(c | c.shortName)->size() = 3
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->collect(c | c.shortName)->size() = 3", c));
}

EMF_TEST(OCL_Select_FiltersSubset) {
    // self.elements->select(c | c.shortName = 'a')->size() = 1
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->select(c | c.shortName = 'a')->size() = 1", c));
    EXPECT_TRUE(eval("self.elements->select(c | c.shortName = 'z')->isEmpty()", c));
}

EMF_TEST(OCL_Reject_ExcludesMatching) {
    // self.elements->reject(c | c.shortName = 'a')->size() = 2
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->reject(c | c.shortName = 'a')->size() = 2", c));
}

EMF_TEST(OCL_Any_ReturnsFirstMatching) {
    // self.elements->any(c | c.shortName = 'a').shortName = 'a'
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->any(c | c.shortName = 'a').shortName = 'a'", c));
    // 无匹配 → null → oclIsUndefined
    EXPECT_TRUE(eval("self.elements->any(c | c.shortName = 'z').oclIsUndefined()", c));
}

EMF_TEST(OCL_Iterate_CountsElements) {
    // self.elements->iterate(c; sum : Integer = 0 | sum + 1) > 0
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->iterate(c; sum : Integer = 0 | sum + 1) > 0", c));
    EXPECT_TRUE(eval("self.elements->iterate(c; sum : Integer = 0 | sum + 1) = 3", c));
}

EMF_TEST(OCL_Iterate_SumsCounts) {
    // self.elements->iterate(c; sum : Integer = 0 | sum + c.count) = 8（3 + 5）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a", 3), makeElement(m, "b", 5)});
    EXPECT_TRUE(eval("self.elements->iterate(c; sum : Integer = 0 | sum + c.count) = 8", c));
}

// ===== String 操作库 =====

EMF_TEST(OCL_String_ToUpper) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'hello'.toUpper() = 'HELLO'", c));
}

EMF_TEST(OCL_String_ToLower) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'HELLO'.toLower() = 'hello'", c));
}

EMF_TEST(OCL_String_Concat) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'ab'.concat('cd') = 'abcd'", c));
}

EMF_TEST(OCL_String_Substring) {
    // OCL substring(start, end)：1-based，闭区间
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'hello'.substring(2, 4) = 'ell'", c));
    EXPECT_TRUE(eval("'hello'.substring(1, 1) = 'h'", c));
}

EMF_TEST(OCL_String_StartsWith) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'hello'.startsWith('he')", c));
    EXPECT_FALSE(eval("'hello'.startsWith('lo')", c));
}

EMF_TEST(OCL_String_EndsWith) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'hello'.endsWith('lo')", c));
    EXPECT_FALSE(eval("'hello'.endsWith('he')", c));
}

EMF_TEST(OCL_String_IndexOf) {
    // OCL indexOf：1-based，未找到返回 0
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'hello'.indexOf('l') = 3", c));
    EXPECT_TRUE(eval("'hello'.indexOf('z') = 0", c));
}

EMF_TEST(OCL_String_Trim) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'  hi  '.trim() = 'hi'", c));
}

EMF_TEST(OCL_String_Length) {
    // length 是 size 的别名
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'hello'.length() = 5", c));
}

EMF_TEST(OCL_String_PlusOperator) {
    // str + other：字符串拼接
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("'ab' + 'cd' = 'abcd'", c));
}

// ===== Integer / Real 操作库 =====

EMF_TEST(OCL_Integer_Abs) {
    // (-5).abs() = 5（注意负数字面量解析）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("(-5).abs() = 5", c));
    EXPECT_TRUE(eval("5.abs() = 5", c));
}

EMF_TEST(OCL_Integer_Max) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("3.max(5) = 5", c));
    EXPECT_TRUE(eval("3.max(2) = 3", c));
}

EMF_TEST(OCL_Integer_Min) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("3.min(5) = 3", c));
    EXPECT_TRUE(eval("3.min(2) = 2", c));
}

EMF_TEST(OCL_Integer_Mod) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("7.mod(3) = 1", c));
}

EMF_TEST(OCL_Integer_Div) {
    // OCL div：整除（向下取整）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("7.div(3) = 2", c));
}

EMF_TEST(OCL_Integer_Floor) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("(3.7).floor() = 3", c));
}

EMF_TEST(OCL_Integer_ToString) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("(42).toString().size() > 0", c));
}

// ===== 通用对象操作 =====

EMF_TEST(OCL_OclIsUndefined_MissingAttr_IsTrue) {
    // self.missing.oclIsUndefined() → true（missing 属性不存在）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("self.missing.oclIsUndefined()", c));
    // 已存在属性 → false
    auto* e = makeElement(m, "x");
    EXPECT_FALSE(eval("self.shortName.oclIsUndefined()", e));
}

EMF_TEST(OCL_OclIsInvalid_AlwaysFalse) {
    // 简化语义：始终 false
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_FALSE(eval("self.oclIsInvalid()", c));
    EXPECT_TRUE(eval("not self.oclIsInvalid()", c));
}

EMF_TEST(OCL_OclIsKindOf_SelfClass) {
    // self.oclIsKindOf(Container) → true（target 是 Container）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("self.oclIsKindOf(Container)", c));
    EXPECT_FALSE(eval("self.oclIsKindOf(Element)", c));
    // target 是 Element
    auto* e = makeElement(m, "x");
    EXPECT_TRUE(eval("self.oclIsKindOf(Element)", e));
    EXPECT_FALSE(eval("self.oclIsKindOf(Container)", e));
}

EMF_TEST(OCL_OclIsTypeOf_ExactClass) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("self.oclIsTypeOf(Container)", c));
    EXPECT_FALSE(eval("self.oclIsTypeOf(Element)", c));
}

EMF_TEST(OCL_AsSequence_OnSingleRef) {
    // self.single.asSequence()->size() = 1（单值引用 → 单元素序列）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e = makeElement(m, "only");
    c->eSet(m.fSingle, std::any(e));
    EXPECT_TRUE(eval("self.single.asSequence()->size() = 1", c));
}

// ===== 算术运算（+ - * /）=====

EMF_TEST(OCL_Arithmetic_Precedence) {
    // 2 + 3 * 4 = 14（* 优先于 +）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("2 + 3 * 4 = 14", c));
    EXPECT_TRUE(eval("(2 + 3) * 4 = 20", c));
}

EMF_TEST(OCL_Arithmetic_Subtraction) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("10 - 3 - 2 = 5", c));
}

EMF_TEST(OCL_Arithmetic_Division) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("8 / 2 = 4", c));
}

// ===== 嵌套集合推导式（Nested Collection Comprehensions）=====
//
// 测试树结构：Container.elements = [e0, e1]
//   e0.children = [a, b]   e1.children = [d]

EMF_TEST(OCL_NestedCollect_FlattensInnerCollect) {
    // collect 内嵌 collect：内层返回 vector<any> 被外层扁平化 → 3 个孙节点名
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "e0");
    auto* e1 = makeElement(m, "e1");
    setChildren(e0, m.fChildren, {makeElement(m, "a"), makeElement(m, "b")});
    setChildren(e1, m.fChildren, {makeElement(m, "d")});
    setElements(c, m.fElements, {e0, e1});
    EXPECT_TRUE(eval("self.elements->collect(x | x.children->collect(y | y.shortName))->size() = 3", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.children->collect(y | y.shortName))->includes('a')", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.children->collect(y | y.shortName))->excludes('z')", c));
}

EMF_TEST(OCL_NestedSelect_WithExistsInBody) {
    // select 条件中嵌套 exists：仅 e0 含名为 'a' 的子节点
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "e0");
    auto* e1 = makeElement(m, "e1");
    setChildren(e0, m.fChildren, {makeElement(m, "a"), makeElement(m, "b")});
    setChildren(e1, m.fChildren, {makeElement(m, "d")});
    setElements(c, m.fElements, {e0, e1});
    EXPECT_TRUE(eval("self.elements->select(x | x.children->exists(y | y.shortName = 'a'))->size() = 1", c));
    EXPECT_TRUE(eval("self.elements->select(x | x.children->exists(y | y.shortName = 'z'))->isEmpty()", c));
}

EMF_TEST(OCL_NestedForAll_DeepQuantifier) {
    // 嵌套 forAll：所有孙节点 shortName 非空
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "e0");
    auto* e1 = makeElement(m, "e1");
    setChildren(e0, m.fChildren, {makeElement(m, "a"), makeElement(m, "b")});
    setChildren(e1, m.fChildren, {makeElement(m, "d")});
    setElements(c, m.fElements, {e0, e1});
    EXPECT_TRUE(eval("self.elements->forAll(x | x.children->forAll(y | y.shortName.size() > 0))", c));

    // 出现空名孙节点 → false
    auto* c2 = m.factory->create(m.containerCls);
    auto* p = makeElement(m, "p");
    setChildren(p, m.fChildren, {makeElement(m, ""), makeElement(m, "ok")});
    setElements(c2, m.fElements, {p});
    EXPECT_FALSE(eval("self.elements->forAll(x | x.children->forAll(y | y.shortName.size() > 0))", c2));
}

// ===== SortedSet / OrderedSet 标准库操作 =====

EMF_TEST(OCL_SortedBy_StringKey_FirstLast) {
    // sortedBy shortName 升序 → 首元素 'a'，末元素 'c'
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "c"), makeElement(m, "a"), makeElement(m, "b")});
    EXPECT_TRUE(eval("self.elements->sortedBy(x | x.shortName)->first().shortName = 'a'", c));
    EXPECT_TRUE(eval("self.elements->sortedBy(x | x.shortName)->last().shortName = 'c'", c));
}

EMF_TEST(OCL_SortedBy_NumericKey) {
    // sortedBy count 升序 → 首元素 count = 1
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "x", 3), makeElement(m, "y", 1), makeElement(m, "z", 2)});
    EXPECT_TRUE(eval("self.elements->sortedBy(x | x.count)->first().count = 1", c));
    EXPECT_TRUE(eval("self.elements->sortedBy(x | x.count)->last().count = 3", c));
}

EMF_TEST(OCL_FirstLast_InsertionOrder) {
    // first/last 按插入顺序（非排序）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "first"), makeElement(m, "mid"), makeElement(m, "last")});
    EXPECT_TRUE(eval("self.elements->first().shortName = 'first'", c));
    EXPECT_TRUE(eval("self.elements->last().shortName = 'last'", c));
    // 空集合 first/last → null
    auto* empty = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("self.elements->first().oclIsUndefined()", empty));
    EXPECT_TRUE(eval("self.elements->last().oclIsUndefined()", empty));
}

EMF_TEST(OCL_At_OneBasedIndex) {
    // at(i)：1-based 索引取元素
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "first"), makeElement(m, "second"), makeElement(m, "third")});
    EXPECT_TRUE(eval("self.elements->at(1).shortName = 'first'", c));
    EXPECT_TRUE(eval("self.elements->at(2).shortName = 'second'", c));
    EXPECT_TRUE(eval("self.elements->at(3).shortName = 'third'", c));
    // 越界 / 0 → null
    EXPECT_TRUE(eval("self.elements->at(0).oclIsUndefined()", c));
    EXPECT_TRUE(eval("self.elements->at(99).oclIsUndefined()", c));
}

EMF_TEST(OCL_IndexOf_OneBased) {
    // indexOf：返回元素首次出现的 1-based 索引
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->indexOf(self.elements->first()) = 1", c));
    EXPECT_TRUE(eval("self.elements->indexOf(self.elements->last()) = 3", c));
    EXPECT_TRUE(eval("self.elements->indexOf(self.elements->at(2)) = 2", c));
    // 未找到 → 0
    EXPECT_TRUE(eval("self.elements->indexOf(null) = 0", c));
}

EMF_TEST(OCL_Count_Occurrences) {
    // count：元素出现次数（按值）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "a")});
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->count('a') = 2", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->count('b') = 1", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->count('z') = 0", c));
    // 对象按指针计数：首元素仅出现一次
    EXPECT_TRUE(eval("self.elements->count(self.elements->first()) = 1", c));
}

EMF_TEST(OCL_Includes_Excludes) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->includes('a')", c));
    EXPECT_FALSE(eval("self.elements->collect(x | x.shortName)->includes('z')", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->excludes('z')", c));
    EXPECT_FALSE(eval("self.elements->collect(x | x.shortName)->excludes('a')", c));
}

EMF_TEST(OCL_IncludesAll_ExcludesAll) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    // ['a','b','c'] includesAll ['a'] → true
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->includesAll(self.elements->select(x | x.shortName = 'a')->collect(y | y.shortName))", c));
    // ['a','b'] includesAll ['a','b','c'] → false（缺 'c'）
    EXPECT_FALSE(eval("self.elements->select(x | x.shortName = 'a' or x.shortName = 'b')->collect(y | y.shortName)->includesAll(self.elements->collect(x | x.shortName))", c));
    // ['a'] excludesAll ['b'] → true（不相交）
    EXPECT_TRUE(eval("self.elements->select(x | x.shortName = 'a')->collect(y | y.shortName)->excludesAll(self.elements->select(x | x.shortName = 'b')->collect(y | y.shortName))", c));
    // ['a'] excludesAll ['a'] → false
    EXPECT_FALSE(eval("self.elements->select(x | x.shortName = 'a')->collect(y | y.shortName)->excludesAll(self.elements->select(x | x.shortName = 'a')->collect(y | y.shortName))", c));
}

EMF_TEST(OCL_Union_Intersection_Difference) {
    // A = ['a','b'], B = ['b','c']
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    // union → ['a','b','b','c'] size 4（Bag 语义保留重复）
    EXPECT_TRUE(eval("self.elements->select(x | x.shortName = 'a' or x.shortName = 'b')->collect(y | y.shortName)->union(self.elements->select(x | x.shortName = 'b' or x.shortName = 'c')->collect(y | y.shortName))->size() = 4", c));
    // intersection → ['b'] size 1
    EXPECT_TRUE(eval("self.elements->select(x | x.shortName = 'a' or x.shortName = 'b')->collect(y | y.shortName)->intersection(self.elements->select(x | x.shortName = 'b' or x.shortName = 'c')->collect(y | y.shortName))->size() = 1", c));
    // difference A - B → ['a'] size 1
    EXPECT_TRUE(eval("self.elements->select(x | x.shortName = 'a' or x.shortName = 'b')->collect(y | y.shortName)->difference(self.elements->select(x | x.shortName = 'b' or x.shortName = 'c')->collect(y | y.shortName))->size() = 1", c));
    // difference B - A → ['c'] size 1
    EXPECT_TRUE(eval("self.elements->select(x | x.shortName = 'b' or x.shortName = 'c')->collect(y | y.shortName)->difference(self.elements->select(x | x.shortName = 'a' or x.shortName = 'b')->collect(y | y.shortName))->includes('c')", c));
}

EMF_TEST(OCL_Flatten_NestedELists) {
    // collect(x | x.children) 返回嵌套 EList（每个父节点的子集合），
    // flatten() 递归扁平化为所有孙节点。
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    auto* e0 = makeElement(m, "e0");
    auto* e1 = makeElement(m, "e1");
    setChildren(e0, m.fChildren, {makeElement(m, "a"), makeElement(m, "b")});
    setChildren(e1, m.fChildren, {makeElement(m, "d")});
    setElements(c, m.fElements, {e0, e1});
    // 扁平化前：2 个父节点的子集合
    EXPECT_TRUE(eval("self.elements->collect(x | x.children)->size() = 2", c));
    // 扁平化后：3 个孙节点
    EXPECT_TRUE(eval("self.elements->collect(x | x.children)->flatten()->size() = 3", c));
    // 已扁平化的集合再 flatten 幂等
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->flatten()->size() = 2", c));
}

EMF_TEST(OCL_Sum_NumericAndString) {
    // 数值求和
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a", 1), makeElement(m, "b", 2), makeElement(m, "c", 3)});
    EXPECT_TRUE(eval("self.elements->collect(x | x.count)->sum() = 6", c));
    // 字符串拼接求和
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->sum() = 'abc'", c));
    // 空集合 sum = 0
    auto* empty = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("self.elements->collect(x | x.count)->sum() = 0", empty));
}

EMF_TEST(OCL_AsSet_Deduplicates) {
    // asSet 去重
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "a"), makeElement(m, "b")});
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->size() = 4", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->asSet()->size() = 2", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->asSet()->includes('a')", c));
}

EMF_TEST(OCL_AsSequence_AsBag_PreserveElements) {
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b")});
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->asSequence()->size() = 2", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->asBag()->size() = 2", c));
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->asOrderedSet()->size() = 2", c));
}

// ===== 集合比较（= / <>）=====

EMF_TEST(OCL_CollectionEquality_SameElements_IsEqual) {
    // 相同集合 = → true
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName) = self.elements->collect(x | x.shortName)", c));
}

EMF_TEST(OCL_CollectionEquality_OrderIndependent) {
    // Bag/Set 语义：顺序无关。collect（插入序）与 sortedBy（排序序）元素相同 → 相等
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "c"), makeElement(m, "a"), makeElement(m, "b")});
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName) = self.elements->sortedBy(x | x.shortName)->collect(y | y.shortName)", c));
}

EMF_TEST(OCL_CollectionInequality_DifferentElements) {
    // 不同元素 <> → true
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "c")});
    // ['a','b','c'] <> ['a'] → true
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName) <> self.elements->select(x | x.shortName = 'a')->collect(y | y.shortName)", c));
    // ['a','b','c'] <> [1,2,3]（不同类型值）→ true
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName) <> self.elements->collect(x | x.count)", c));
    // 相同集合 <> → false
    EXPECT_FALSE(eval("self.elements->collect(x | x.shortName) <> self.elements->collect(x | x.shortName)", c));
}

EMF_TEST(OCL_CollectionEquality_DuplicateSensitive) {
    // 多集语义：重复计数。['a','b','a'] ≠ ['a','b']
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a"), makeElement(m, "b"), makeElement(m, "a")});
    // 全集（含重复）≠ 去重后
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName) <> self.elements->collect(x | x.shortName)->asSet()", c));
    // asSet 后两者都是 {'a','b'} → 相等
    EXPECT_TRUE(eval("self.elements->collect(x | x.shortName)->asSet() = self.elements->collect(x | x.shortName)->asSet()", c));
}

// ===== OCL Tuple 类型测试（对齐 Eclipse OCL TupleLiteralExp）=====

EMF_TEST(OCL_Tuple_LiteralAndFieldAccess) {
    // Tuple { a = 1, b = 'x' } .a = 1
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' }.a = 1", c));
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' }.b = 'x'", c));
}

EMF_TEST(OCL_TLiteral_WithTypeAnnotation) {
    // 带 ': Type' 注解（类型忽略，值仍正确）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { a : Integer = 42, b : String = 'hi' }.a = 42", c));
    EXPECT_TRUE(eval("Tuple { a : Integer = 42, b : String = 'hi' }.b = 'hi'", c));
}

EMF_TEST(OCL_Tuple_AccessMissingPart_IsNull) {
    // 访问不存在的 part → null（与 null 比较 = true）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { a = 1 }.missing = null", c));
    // 已有 part 不等于 null
    EXPECT_TRUE(eval("Tuple { a = 1 }.a <> null", c));
}

EMF_TEST(OCL_Tuple_Equality_SameParts_IsEqual) {
    // 相同部分相同值 → =
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' } = Tuple { a = 1, b = 'x' }", c));
}

EMF_TEST(OCL_Tuple_Equality_OrderIndependent) {
    // 部分顺序不同但同名同值 → =（按名匹配，顺序无关）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' } = Tuple { b = 'x', a = 1 }", c));
}

EMF_TEST(OCL_Tuple_Inequality_DifferentValues) {
    // 同名不同值 → <>
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' } <> Tuple { a = 2, b = 'x' }", c));
    // 部分 b 值不同 → <>
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' } <> Tuple { a = 1, b = 'y' }", c));
}

EMF_TEST(OCL_Tuple_Inequality_DifferentParts) {
    // 部分集合不同 → <>
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' } <> Tuple { a = 1, c = 'x' }", c));
    // 部分数不同 → <>
    EXPECT_TRUE(eval("Tuple { a = 1, b = 'x' } <> Tuple { a = 1 }", c));
}

EMF_TEST(OCL_Tuple_WithLetBinding) {
    // let t = Tuple {...} in t.a
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("let t = Tuple { a = 5, b = 10 } in t.a + t.b = 15", c));
}

EMF_TEST(OCL_Tuple_InCollect_Grouping) {
    // collect 中构造 Tuple，再访问 part（典型分组聚合用法）
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    setElements(c, m.fElements, {makeElement(m, "a", 1), makeElement(m, "b", 2)});
    // 每个元素映射成 Tuple { name, cnt }，collect 后取 size = 2
    EXPECT_TRUE(eval("self.elements->collect(x | Tuple { name = x.shortName, cnt = x.count })->size() = 2", c));
    // 第一个 Tuple 的 name = 'a'
    EXPECT_TRUE(eval("self.elements->collect(x | Tuple { name = x.shortName, cnt = x.count })->first().name = 'a'", c));
}

EMF_TEST(OCL_Tuple_NestedTuple) {
    // 嵌套 Tuple：外层 part 值为内层 Tuple，链式 . 访问
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple { outer = Tuple { inner = 7 } }.outer.inner = 7", c));
}

EMF_TEST(OCL_Tuple_EmptyLiteral) {
    // 空字面量 Tuple {} 合法，与自身相等
    Meta m = loadMeta();
    auto* c = m.factory->create(m.containerCls);
    EXPECT_TRUE(eval("Tuple {} = Tuple {}", c));
}
