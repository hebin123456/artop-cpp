# EMF/ARTOP C++ 实现设计文档

本项目的将 Eclipse EMF + artop 的 compare / validation / notification 能力用 C++ 重新实现，
并按 Java artop 工具（`/workspace/artop/`，490 plugins）的实际行为分层。本文档记录核心设计决策、
性能优化与对齐情况。

---

## 1. emf / artop 分层架构

对齐 Java artop 的三层结构（artop = Eclipse EMF + sphinx + artop 业务层），C++ 侧按目录分层：

| 层 | C++ 目录 | 对齐的 Java bundle | 职责 |
|---|---|---|---|
| 标准 EMF 层 | `emf-common` / `emf-ecore` / `emf-xmi` / `emf-validation` / `emf-compare` | `org.eclipse.emf.*` | EObject / Ecore / XMI 序列化 / Validation 框架 / Compare 框架 |
| sphinx 中间层 | `emf-sphinx` | `org.eclipse.sphinx.*` | 资源加载、元模型描述符、validation bridge |
| artop 业务层 | `emf-artop` | `org.artop.aal.*` | AUTOSAR 元模型、IdentifiableUtil、AutosarIdentifierProvider |

### 关键对齐结论（基于 artop jar 反编译）

1. **artop 不含 EMF Compare**：artop 只有 `org.eclipse.compare`（文本比较），不含
   `org.eclipse.emf.compare`（模型比较）。因此 C++ 的 `emf-compare` 是独立实现，不对应 artop 任何 bundle。
2. **artop Validation 用 Eclipse EMF Validation**：`org.eclipse.emf.validation_1.8.0` +
   `org.eclipse.emf.validation.ocl`，OCL 求值委托给 `org.eclipse.ocl.Query`（完整 OCL 2.x 引擎）。
3. **artop 约束注册走 Sphinx**：`org.artop.aal.autosar40.validation` 的 plugin.xml 使用
   `org.eclipse.sphinx.emf.validation.registration` 扩展点，通过
   `validatorAdapterClass="org.artop.aal.validation.adapter.EAutosarValidatorAdapter"` **程序化**注册约束，
   并用 `org.eclipse.emf.validation.constraintBindings` 绑定 clientContext
   `autosar40.ARObject.context`（enablement: `instanceof ARObject`）。约束不是声明式 XML。
4. **artop 内置约束为 Java 编码**：autosar40.validation jar 仅 4 个 class（Activator + Listener），
   真正的约束逻辑在 `EAutosarValidatorAdapter`（程序化注册到 EMF Validation）。OCL 注解是标准 EMF
   用户自定义约束机制（EAnnotation），artop 自身约束不依赖 OCL 表达式文件。

---

## 2. IdentifierProvider 钩子集成

### 问题
AUTOSAR 元模型的 EClass（如 `ARObject` 子类）大多没有 `isID()==true` 的 EAttribute，
Java EMF Compare 默认 proximity 匹配对大文件 O(n²) 且易误配。artop 实际用 shortName / uuid 作业务标识。

### 设计
在 **emf-compare 层**定义 provider 钩子（不耦合 artop），在 **artop 层**注入实现：

- [MatchEngine.h](file:///workspace/cpp/emf-cpp/emf-compare/include/emf/compare/MatchEngine.h) —
  `setIdentifierProvider(std::function<std::string(EObject*)>)`，match 时优先取 provider 返回的 ID。
- artop 层 `IdentifiableUtil::asIdentifierProvider()` 返回一个 provider，内部调
  `GIdentifiable.gGetShortName()` / `gGetUuid()`。

### ID 优先级链（对齐 Java DefaultMatchEngine）
```
手动 registerIdentifier  >  IdentifierProvider  >  自动 ID 属性(isID)  >  proximity 相似度
```

---

## 3. DiffEngine O(n²) 性能修复

### 问题
`DiffEngine::diff()` 中 `diffSingleValueReferences` 和 `detectMoves` 对每个 match 都重建
`leftToRight` 映射，导致 57153 对象的 compare 耗时 >60s（实际 >120s 超时）。

### 修复
在 [DiffEngine.cpp](file:///workspace/cpp/emf-cpp/emf-compare/src/DiffEngine.cpp) 的 `diff()` 入口
一次性构建 `leftToRight_` 成员映射，下游方法接收已构建的 map 引用，避免 per-match 重建。

### 效果
```
修复前: 57153 obj, compare >60s (超时)
修复后: 57153 obj, compare ~1s
```

---

## 4. UUID 全局唯一性约束

对齐 artop `FixUuidConflictsAction.getUuidConflicts()` 的行为，实现在
[AutosarConstraints.cpp](file:///workspace/cpp/emf-cpp/emf-validation/src/AutosarConstraints.cpp) 的
`validateUuidUniqueness(EObject* root)`：

- DFS 遍历整棵模型树
- `HashMap<uuid, EObject*>` 去重
- **空 uuid 和重复 uuid 都报告**，首个重复不报告（与 artop 一致）
- 反射读取 `uuid` feature，不依赖具体生成的 EClass

由 `ValidationService::validateAll` 末尾集成调用。

---

## 5. OCL 子集解析器

### 背景
artop 的 EMF Validation OCL bridge (`AbstractOCLModelConstraint`) 将 OCL 求值委托给
`org.eclipse.ocl.Query`（完整 OCL 2.x 引擎）。C++ 侧实现对齐该语义子集。

### 实现
[ConstraintParser.cpp](file:///workspace/cpp/emf-cpp/emf-validation/src/ConstraintParser.cpp) —
644 行递归下降解析器，[ConstraintParser.h](file:///workspace/cpp/emf-cpp/emf-validation/include/emf/validation/ConstraintParser.h)
声明公共 API。

### 支持的语法（OCL 子集）
```
逻辑运算（优先级低→高）：implies, or, xor, and, not/!
比较运算：= / ==, <> / !=, >, <, >=, <=
集合迭代：source->forAll(v | boolExpr), source->exists(v | boolExpr)
集合操作：source->size(), source->isEmpty(), source->notEmpty()
路径导航：self, 迭代变量v, self.attr.subattr, attr（隐式 self）
对象操作：obj.attr.size()（集合大小或字符串长度）
字面量：null, '', 'str', true, false, 数字（含负数）
分组：(expr)
条件：if expr then expr else expr endif
```

### OCL 语义要点
- 空集合 `forAll → true`, `exists → false`
- `implies: A implies B = (not A) or B`（右结合）
- `= / <>` 为 OCL 标准等值运算（同时兼容 `== / !=`）
- 单值引用上 `->` 迭代视为单元素集合
- 解析失败返回恒 `true`（对齐 Java constraint 语法容错）

### 文法（优先级低→高）
```
expr        := implies
implies     := or ('implies' implies)?          // 右结合
or          := xor (('or'|'||') xor)*
xor         := and ('xor' and)*
and         := unary (('and'|'&&') unary)*
unary       := ('not'|'!') unary | '-' unary | relational
relational  := primary (relop primary)?
primary     := '(' expr ')'
             | 'if' expr 'then' expr 'else' expr 'endif'
             | pathOrLiteral ('->' iterOp)*
iterOp      := forAll '(' ident '|' expr ')'
             | exists '(' ident '|' expr ')'
             | size '(' ')' | isEmpty '(' ')' | notEmpty '(' ')'
```

### 测试
[ConstraintParserTests.cpp](file:///workspace/cpp/emf-cpp/emf-validation/tests/ConstraintParserTests.cpp) —
~35 个测试覆盖 implies 右结合、forAll/exists 空集合语义、单值引用迭代、深层路径导航、
if-then-else、null 检查、容错等。共 81/81 validation 测试通过。

---

## 6. 性能基线

测试文件：`java/demo/output/ECUConfigurationParameters.arxml`（57153 对象）

### Compare（emf-compare）
| 指标 | 值 |
|---|---|
| 模型加载（2 次，左+右） | avg 605 ms |
| Compare（含 match + diff） | avg 1010 ms |
| Matches | 57153 |
| Diffs | 0（同模型对比） |

修复前同模型 compare >60s 超时。

### Validation（emf-validation）
| 指标 | 值 |
|---|---|
| 模型加载 | avg 301 ms |
| Batch validate（全量约束） | avg 4510 ms |
| 诊断数 | 8051 |
| Live attach（注册监听器） | avg 390 ms |

### 测试覆盖
- `emf_compare_tests`: 40/40 通过
- `emf_validation_tests`: 81/81 通过

---

## 7. artop headless validation 性能对比（已完成）

在 linux 命令行用 artop 的 jar 跑 AUTOSAR arxml validation，与 C++ 实现做性能对比。

### 搭建方案
artop 是 Windows RCP 发行版（`artop.exe`），但 jar 跨平台。linux 下用
`java -jar org.eclipse.equinox.launcher_*.jar` 启动 Equinox OSGi（Java 8 / temurin-8），绕过 native launcher。

- 自定义 headless IApplication bundle：[/workspace/artop-headless/](file:///workspace/artop-headless/) —
  `ArxmlValidationApplication` 加载 arxml → `IBatchValidator.validate` → 计时
- 自定义 OSGi 配置：[/workspace/artop/headless-config/](file:///workspace/artop/headless-config/) —
  `config.ini` + `bundles.info`（79 个 bundle，排除 GUI/p2/win32 fragment）
- 完整结果：[RESULT.txt](file:///workspace/artop-headless/RESULT.txt)

### 约束机制（jar 内部复核）
- 49 个 `lang="JAVA"` 约束在 `autosar40.constraints.ecuc` jar，通过标准
  `org.eclipse.emf.validation.constraintProviders` + `constraintBindings` 扩展点声明
- 绑定到 clientContext `autosar40.ARObject.context`（enablement: `instanceof ARObject`）
- mode="Batch"，severity ERROR/WARNING
- 元模型在 `autosar448` jar（`generated_package` 扩展点，nsURI `http://autosar.org/schema/r4.0/*`）
- `autosar40.validation` 仅 4 个 class（Activator + Listener），约束逻辑在 constraints.ecuc

### 对比结果（ECUConfigurationParameters.arxml，57154 对象）

| 指标 | C++ 实现 | artop Java | 说明 |
|---|---|---|---|
| 模型加载 | 301 ms | 1118 ms | **C++ 快 3.7x** |
| Validation | 4510 ms | 405 ms | 见下方公平性说明 |
| 诊断数 | 8051 | 88 | 约束集不同 |
| 约束数 | 通用约束集 | 49 ECUC 约束（47 触发） | 见下方 |

### 公平性说明
验证性能对比需注意**约束集不同**，不能直接等价：
- **artop**：49 个 ECUC 专用约束（针对 EcucParameterValue / ContainerDef 等），由 clientContext
  按 EClass 过滤，只对匹配对象执行，产生 88 个诊断（多为 code=10003 引用解析失败）
- **C++**：通用约束集（shortName 兄弟唯一性、uuid 全局唯一性、proxy 检查等），对全树 57153 对象
  逐个执行，产生 8051 个诊断

因此 **加载性能对比是公平的**（C++ 快 3.7x）；验证性能反映的是"不同约束集的开销"，artop 的
clientContext 按 EClass 过滤机制使其只对少数对象跑约束，开销自然低。若 C++ 也实现 ECUC 约束并
按 EClass 过滤，验证耗时预计大幅下降。

### Java 版本
artop 4.13 构建 target 为 JavaSE-1.7/1.8。用 temurin-8.0.482 运行，避免 Java 25 反射兼容问题。
