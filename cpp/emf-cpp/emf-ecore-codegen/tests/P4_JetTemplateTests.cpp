// P4 测试：JET 模板子集（each / if / unless）
// 对齐 Java: org.eclipse.emf.codegen.ecore.templates.model.* (JET each/c-if/c-choose/c-iterate)
#include "test_main.h"
#include "emf/ecore/codegen/CppTemplates.h"

#include <map>
#include <string>
#include <vector>

using emf::ecore::codegen::renderTemplate;
using emf::ecore::codegen::renderJetTemplate;

// =====================================================================
// 1) 基础：renderJetTemplate 不带控制块的行为等价于 renderTemplate
// =====================================================================
EMF_TEST(JetTemplate_Plain_NoControlBlocks_SameAsRenderTemplate) {
    std::string tmpl = "Hello {{name}}, age {{age}}!";
    std::map<std::string, std::string> vars;
    vars["name"] = "alice";
    vars["age"] = "30";
    std::string a = renderTemplate(tmpl, vars);
    std::string b = renderJetTemplate(tmpl, vars, {});
    EXPECT_EQ(a, b);
    EXPECT_EQ(a, std::string{"Hello alice, age 30!"});
}

// =====================================================================
// 2) each 循环：展开列表的每一行
// =====================================================================
EMF_TEST(JetTemplate_Each_RendersAllRows) {
    std::string tmpl =
        "names:\n"
        "{{#each people}}"
        "  - {{name}} (age {{age}})\n"
        "{{/each}}"
        "end";
    std::map<std::string, std::string> vars;
    std::map<std::string, std::vector<std::map<std::string, std::string>>> lists;
    lists["people"] = {
        {{"name", "alice"}, {"age", "30"}},
        {{"name", "bob"},   {"age", "25"}},
        {{"name", "carol"}, {"age", "28"}},
    };
    std::string out = renderJetTemplate(tmpl, vars, lists);
    EXPECT_TRUE(out.find("- alice (age 30)") != std::string::npos);
    EXPECT_TRUE(out.find("- bob (age 25)") != std::string::npos);
    EXPECT_TRUE(out.find("- carol (age 28)") != std::string::npos);
}

// =====================================================================
// 3) each + 外部 var 混合：each 块内能引用外层 vars
// =====================================================================
EMF_TEST(JetTemplate_Each_InheritsOuterVars) {
    std::string tmpl =
        "package: {{pkg}}\n"
        "{{#each classes}}"
        "class: {{name}} in {{pkg}}\n"
        "{{/each}}";
    std::map<std::string, std::string> vars;
    vars["pkg"] = "com.example";
    std::map<std::string, std::vector<std::map<std::string, std::string>>> lists;
    lists["classes"] = {
        {{"name", "Foo"}}, {{"name", "Bar"}},
    };
    std::string out = renderJetTemplate(tmpl, vars, lists);
    EXPECT_TRUE(out.find("package: com.example") != std::string::npos);
    EXPECT_TRUE(out.find("class: Foo in com.example") != std::string::npos);
    EXPECT_TRUE(out.find("class: Bar in com.example") != std::string::npos);
}

// =====================================================================
// 4) each 行级字段覆盖外层 var
// =====================================================================
EMF_TEST(JetTemplate_Each_RowOverridesOuter) {
    std::string tmpl = "{{#each rows}}{{x}};{{/each}}";
    std::map<std::string, std::string> vars;
    vars["x"] = "OUTER";
    std::map<std::string, std::vector<std::map<std::string, std::string>>> lists;
    lists["rows"] = {{{"x", "A"}}, {{"x", "B"}}, {{"x", "C"}}};
    std::string out = renderJetTemplate(tmpl, vars, lists);
    EXPECT_EQ(out, std::string{"A;B;C;"});
}

// =====================================================================
// 5) each 列表为空时不输出
// =====================================================================
EMF_TEST(JetTemplate_Each_EmptyList_ProducesEmptyBlock) {
    std::string tmpl = "before{{#each items}}<{{name}}>{{/each}}after";
    std::map<std::string, std::string> vars;
    std::map<std::string, std::vector<std::map<std::string, std::string>>> lists;  // items 缺失
    std::string out = renderJetTemplate(tmpl, vars, lists);
    EXPECT_EQ(out, std::string{"beforeafter"});
}

// =====================================================================
// 6) if：cond 非空时渲染
// =====================================================================
EMF_TEST(JetTemplate_If_ConditionTrue_Renders) {
    std::string tmpl = "before{{#if flag}}ENABLED{{/if}}after";
    std::map<std::string, std::string> vars;
    vars["flag"] = "yes";
    std::string out = renderJetTemplate(tmpl, vars, {});
    EXPECT_EQ(out, std::string{"beforeENABLEDafter"});
}

// =====================================================================
// 7) if：cond 缺失时不渲染
// =====================================================================
EMF_TEST(JetTemplate_If_ConditionMissing_Skips) {
    std::string tmpl = "before{{#if flag}}ENABLED{{/if}}after";
    std::map<std::string, std::string> vars;  // flag 缺失
    std::string out = renderJetTemplate(tmpl, vars, {});
    EXPECT_EQ(out, std::string{"beforeafter"});
}

// =====================================================================
// 8) if：cond 是空字符串也不渲染
// =====================================================================
EMF_TEST(JetTemplate_If_EmptyString_Falsey) {
    std::string tmpl = "[{{#if flag}}X{{/if}}]";
    std::map<std::string, std::string> vars;
    vars["flag"] = "";  // 空字符串 = false
    std::string out = renderJetTemplate(tmpl, vars, {});
    EXPECT_EQ(out, std::string{"[]"});
}

// =====================================================================
// 9) unless：cond 非空时不渲染
// =====================================================================
EMF_TEST(JetTemplate_Unless_ConditionTrue_Skips) {
    std::string tmpl = "before{{#unless flag}}DISABLED{{/unless}}after";
    std::map<std::string, std::string> vars;
    vars["flag"] = "yes";
    std::string out = renderJetTemplate(tmpl, vars, {});
    EXPECT_EQ(out, std::string{"beforeafter"});
}

// =====================================================================
// 10) unless：cond 缺失时渲染
// =====================================================================
EMF_TEST(JetTemplate_Unless_ConditionMissing_Renders) {
    std::string tmpl = "before{{#unless flag}}DISABLED{{/unless}}after";
    std::map<std::string, std::string> vars;  // flag 缺失
    std::string out = renderJetTemplate(tmpl, vars, {});
    EXPECT_EQ(out, std::string{"beforeDISABLEDafter"});
}

// =====================================================================
// 11) 嵌套 each：外层每个都展开内层
// =====================================================================
EMF_TEST(JetTemplate_NestedEach) {
    std::string tmpl =
        "{{#each classes}}"
        "class {{name}}:\n"
        "{{#each features}}"
        "  - {{name}} ({{type}})\n"
        "{{/each}}"
        "{{/each}}";
    std::map<std::string, std::string> vars;
    std::map<std::string, std::vector<std::map<std::string, std::string>>> lists;
    lists["classes"] = {
        {{"name", "Foo"},
         {"features", ""}},  // 注：nested each 不通过 row.features 注入（用顶层 lists["features"]）
    };
    // 简化版：内层 each 引用顶层 lists["features"]
    std::string out = renderJetTemplate(tmpl, vars, lists);
    EXPECT_TRUE(out.find("class Foo:") != std::string::npos);
}

// =====================================================================
// 12) if + each 混合
// =====================================================================
EMF_TEST(JetTemplate_IfInsideEach) {
    std::string tmpl =
        "{{#each items}}"
        "{{#if active}}*{{name}};{{/if}}"
        "{{name}};"
        "{{/each}}";
    std::map<std::string, std::string> vars;
    std::map<std::string, std::vector<std::map<std::string, std::string>>> lists;
    lists["items"] = {
        {{"name", "A"}, {"active", "yes"}},
        {{"name", "B"}, {"active", ""}},     // inactive
        {{"name", "C"}, {"active", "true"}}, // active
    };
    std::string out = renderJetTemplate(tmpl, vars, lists);
    // 期望：*A;A;B;C;   （第二个 B 不会带 *）
    EXPECT_EQ(out, std::string{"*A;A;B;*C;C;"});
}
