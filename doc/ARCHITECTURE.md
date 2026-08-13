# emf-cpp 架构文档

C++ 重写 Eclipse EMF + Java ARTOP，定位为 **headless AUTOSAR 模型工具平台**。
本文描述整体分层、模块职责、与 Java 端的对齐关系，以及关键设计取舍。

---

## 1. 两层架构

```
┌─────────────────────────────────────────────────────────────────┐
│  应用层（autosar_server 等）                                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────┐
│  第 2 层：emf-artop/*   对齐 Java org.artop.aal.*               │
│  ┌──────────────────────┐  ┌──────────────────────┐             │
│  │ emf-artop-runtime    │  │ emf-artop-codegen    │             │
│  │ AUTOSAR 序列化/反序列化│  │ 从 .ecore 生成 C++  │             │
│  │ 资源/工厂/版本元数据  │  │ 静态模型             │             │
│  └──────────┬───────────┘  └──────────┬───────────┘             │
└─────────────┼─────────────────────────┼─────────────────────────┘
              │                         │
┌─────────────┴─────────────────────────┴─────────────────────────┐
│  第 1 层：emf-*   对齐 Eclipse EMF / Eclipse 项目                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐            │
│  │ common   │ │ ecore    │ │ ecore-   │ │ xmi      │            │
│  │ EObject  │ │ EClass/  │ │ util     │ │ XMI/XML  │            │
│  │ URI/Res  │ │ EPackage │ │ EcoreUtil│ │ 资源加载 │            │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐            │
│  │ xsd      │ │ edit     │ │validation│ │ compare  │            │
│  │ XSD 元模型│ │ Command  │ │EValidator│ │Match/Diff│            │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐            │
│  │ xcore    │ │ acceleo  │ │ sphinx   │ │ecore-    │            │
│  │ Xcore DSL│ │ MTL 模板 │ │ headless │ │codegen   │            │
│  │ 解析器   │ │ M2T 引擎 │ │ 核心子集 │ │ GenModel │            │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

- **第 1 层 `emf-*`（12 个模块）**：通用 EMF 模型基础设施，对齐
  `org.eclipse.emf.*` / `org.eclipse.acceleo` / `org.eclipse.sphinx.emf`，
  **不涉及 AUTOSAR 业务**。
- **第 2 层 `emf-artop/*`（2 个模块）**：AUTOSAR 特化层，对齐
  `org.artop.aal.*`，依赖第 1 层。Java ARTOP 本身也建在 Eclipse EMF 之上，
  C++ 端完全镜像了这个依赖关系。

---

## 2. 第 1 层：emf-* 模块（对齐 Eclipse EMF）

| 模块 | Java 包 | 职责 | 完整度 |
|---|---|---|---|
| **emf-common** | org.eclipse.emf.common | EObject/URI/Resource/Notification/EList/Command | 高 |
| **emf-ecore** | org.eclipse.emf.ecore(.impl) | EClass/EPackage/EFactory + 17 个 Impl + EcorePackage | 高 |
| **emf-ecore-util** | org.eclipse.emf.ecore.util | EcoreUtil（128+ 方法 1:1）/ Copier / 6 个 EList 变体 | 高 |
| **emf-ecore-codegen** | org.eclipse.emf.codegen.ecore | GenModel → C++17 代码生成（替代 JET） | 中高 |
| **emf-xmi** | org.eclipse.emf.ecore.xmi | XMIResource / SAXXMIHandler / UUID / 跨文件代理 | 中高 |
| **emf-xsd** | org.eclipse.xsd | XSD 元模型 + 19 个 Facet + Parser/Validator | 高 |
| **emf-edit** | org.eclipse.emf.edit | Command/EditingDomain 已实现 / Provider 体系骨架 | 中高 |
| **emf-validation** | org.eclipse.emf.validation | EValidator / ConstraintParser / LiveValidator | 中 |
| **emf-compare** | org.eclipse.emf.compare | Match/Diff/Merge 引擎 | 中 |
| **emf-xcore** | org.eclipse.emf.ecore.xcore | Xcore DSL 解析 → EPackage | 中 |
| **emf-acceleo** | org.eclipse.acceleo | MTL 模板引擎 + AQL 子集 | 中 |
| **emf-sphinx** | org.eclipse.sphinx.emf | headless 核心子集（Platform 部分 stub） | 中高 |

### 核心设计（emf-ecore）

C++ 用菱形 virtual 继承解决 Java 接口多继承：

```
EModelElementImpl : virtual EObjectImpl, virtual EModelElement
ENamedElementImpl : virtual EModelElementImpl, virtual ENamedElement
ETypedElementImpl : virtual ENamedElementImpl, virtual ETypedElement
EClassifierImpl   : virtual ETypedElementImpl,  virtual EClassifier
EClassImpl        : virtual EClassifierImpl,    virtual EClass
```

- 接口（`EObject`/`EClass`/...）virtual 继承，提供纯虚 API
- Impl 类单继承 state + virtual 继承接口，vtable 小且对称
- `eGet/eSet/eIsSet/eUnset` 按 featureID switch-case 派发，对齐 Java `EObjectImpl`

`std::any` 替代 Java 反射：`eGet` 返回 `std::any`，存储**精确类型**
（`EClass*` 而非 `EObject*`），消费端用 `any_cast` 提取。acceleo 引擎的
`asEObject()` helper 对所有 emf::ecore 派生类指针逐一尝试 `any_cast` 再
`dynamic_cast<EObject*>`，模拟 Java EObject 接口天然多态性。

### 命令体系（emf-edit）

emf-edit 的 Command 层已完整实现，对齐 Java `org.eclipse.emf.edit.command`：

| C++ 类 | 对齐 Java 类 | 状态 |
|---|---|---|
| `CommandHelper` | （Java 隐式类型擦除） | **已实现**：RTTI 分派 8 种 `EList<T>*` |
| `AddCommand` | `AddCommand` | **已实现**：多值 add + index 插入 + undo/redo |
| `RemoveCommand` | `RemoveCommand` | **已实现**：多值 remove + undo/redo |
| `SetCommand` | `SetCommand` | **已实现**：单值 eSet/eUnset + 多值 index 替换 |
| `ReplaceCommand` | `ReplaceCommand` | **已实现**：单值/多值替换 |
| `MoveCommand` | `MoveCommand` | **已实现**：多值列表内 move + 类型感知索引查找 |
| `AdapterFactoryEditingDomain` | `AdapterFactoryEditingDomain` | **已实现**：createCommand 分派 5 命令 + create/copy/clone（Copier） |
| `ComposedAdapterFactory` | `ComposedAdapterFactory` | **已实现**：adapt/isFactoryForType/createAdapter 遍历子 factory |
| `ChangeDescription` | `ChangeDescription` | **已实现**：简化版，记录 copy/clone 的 original→copy 映射 |

**CommandHelper 设计要点**：C++ 不像 Java 有类型擦除（`EList<?>` 可直接 add/remove）。
`EObject::eGet` 对多值 feature 返回 `std::any`，内含 `EList<T>*`，但 T 在编译期未知。
CommandHelper 通过 `std::any::type()` 与 `typeid(EList<T>*)` 比较，分派到正确的 T 进行操作。

关键语义（DynamicEObject）：
- EReference 多值：`eGet` 返回内部 list 指针（lazy-created），**直接修改即生效**，
  不能再调 `eSet` 回写（eSet 会 clear 再从自身 copy → 列表被清空）。
- EAttribute 多值：`eGet` 返回存储的指针，直接修改即生效。
- feature 未设置：创建新 list 并 `eSet` 存储一次（仅此场景需要 eSet）。
- 支持类型：`EObject*`/`string`/`int32_t`/`double`/`float`/`int64_t`/`int16_t`/`int8_t`
  （`bool` 因 `std::vector<bool>` 特化导致 `EList<bool>::get()` 返回引用失败，刻意排除）。

**SetCommand/ReplaceCommand 的 undo 正确性**：DynamicEObject 对未设置的 EAttribute，
`eGet` 返回默认值（如 EInt 的 0）而非空 any。因此命令记录 `wasSet_`（execute 前 `eIsSet` 状态），
undo 时若原未设置则 `eUnset`，否则恢复旧值，确保 undo 后 `eIsSet` 状态正确恢复。

**AdapterFactoryEditingDomain.createCommand**：Java 通过 adapterFactory.adapt(owner,
IEditingDomainItemProvider.class) 找到 ItemProvider 再委托。C++ 端 Provider 体系未落地，
改为基于 commandClass（string 命令名 "Add"/"Remove"/"Set"/"Replace"/"Move"）直接分派到对应命令类，
参数布局为 `[owner*, feature*, values..., index?]`。

**copy/clone**：均使用 `EcoreUtil::Copier`。copy 为浅拷贝（仅 containment），clone 为深拷贝
（containment + reference，调用 `Copier::copyReferences()`）。返回 `ChangeDescription` 记录
original→copy 映射与结果副本列表。

---

## 3. 第 2 层：emf-artop 模块（对齐 Java ARTOP）

Java ARTOP 有 6 个 bundle，C++ 端根据 headless 工具定位做了取舍：

| Java Bundle | C++ 处理 | 原因 |
|---|---|---|
| org.artop.aal.common | **emf-artop-runtime**（实质实现） | 核心运行时 |
| org.artop.aal.serialization | **合并进 runtime** | Loader/Saver 吸收 LoadImpl/SaveImpl；EAnnotation 元数据取代 RuleRegistry |
| org.artop.aal.*.codegen | **emf-artop-codegen**（骨架） | 离线代码生成工具 |
| org.artop.aal.extender | **不实现** | Eclipse extension point 机制不适用；SDG 按需在 runtime 补 |
| org.artop.aal.validation | **不实现** | 核心已在 emf-validation；autosar 约束直接 registerConstraint |
| org.artop.aal.workspace | **不实现** | 纯 Eclipse IDE 集成（IProject/IMarker/nature），headless 不需要 |

### emf-artop-runtime

AUTOSAR 模型的序列化/反序列化核心，对齐 `org.artop.aal.common` +
`org.artop.aal.serialization`：

| C++ 类 | 对齐 Java 类 | 功能 |
|---|---|---|
| AutosarXMLResource | AutosarXMLResourceImpl | ARXML 资源（mixed FeatureMap / eObjectToExtensionMap） |
| AutosarResourceFactory | AutosarXMLResourceFactoryImpl | 资源工厂 + schemaLocation 注入 |
| AutosarXMLLoader | AutosarXMLLoadImpl + AutosarSAXXMLHandler + ReferenceHelper | APRXML 0012-0016 规则 / 跨文件代理 / UnknownElement |
| AutosarXMLSaver | AutosarXMLSaveImpl + AtpSplitkeyAwareComparator | 序列化 + 引用 deresolve |
| AutosarReleaseDescriptor | AutosarReleaseDescriptor | 版本三元组 + canonical number |
| AutosarMetaModelVersionData | AutosarMetaModelVersionData | 4.0.1~4.4.8 版本元数据 |
| IdentifiableUtil | IdentifiableUtil | shortName/identifier 反射访问 |

**设计差异（序列化规则）**：
- Java 用"代码驱动"：`AutosarXMLRuleRegistry` + `AutosarTaggedValues` 常量表 + 生成的 `eFeatureXmlName`/`eFeatureAprxmlRule` 方法
- C++ 用"模型驱动"：所有 xmlName / APRXML 规则 / atp.Splitkey / roleWrapperElement 从 `EAnnotation` 经 `EAnnotationReader` 读取

所以 Java 的 `AutosarXMLRuleRegistry`/`AutosarPersistenceRules`/`AutosarTaggedValues` 在 C++ 没有对应物——**被 EAnnotation 元数据机制取代**，不是缺失。

### emf-artop-codegen

从 `.ecore` 生成 C++ 静态模型代码的离线工具，继承 `emf-ecore-codegen`：
- 生成 `<Pkg>ResourceImpl.h`（继承 AutosarXMLResource）
- 生成 `<Pkg>ResourceFactoryImpl.h`（继承 AutosarXMLResourceFactory，构造器组装 ReleaseDescriptor）
- 生成 `ARTOP_ROOT_EXTENSIONS.md` marker（描述根 EClass 要注入的 mixed/extensions 字段）

---

## 4. 模块依赖关系

```
emf-common ◄── emf-ecore ◄── emf-ecore-util
    ▲              ▲
    │              ├── emf-ecore-codegen ◄── emf-artop-codegen
    │              ├── emf-xsd
    │              ├── emf-xcore
    │              └── emf-acceleo
    │
    ├── emf-xmi ◄── emf-sphinx ◄── emf-artop-runtime ◄── emf-artop-codegen
    │     ▲              ▲
    │     │              ├── emf-validation
    │     │              └── emf-edit
    │     │
    │     └── emf-compare
    │
    └── emf-validation
```

关键依赖链：
- `emf-artop-runtime` → `emf-common`/`emf-ecore`/`emf-xmi`/`emf-sphinx`
- `emf-artop-codegen` → `emf-ecore-codegen` + `emf-artop-runtime`
- `emf-acceleo` → `emf-ecore` + `emf-xcore`（对齐测试用）
- `emf-xcore` → `emf-ecore`

---

## 5. 与 Java 对齐的设计取舍

### 5.1 类型系统差异

| Java | C++ | 补偿方案 |
|---|---|---|
| EObject 接口多态 | 菱形 virtual 继承 | 接口 virtual + Impl 单继承 state |
| 反射 Class<?> | std::any（存精确类型） | any_cast + asEObject() helper 枚举派生类 |
| EList<T> | std::vector<T> | 直接用 vector，EList 作适配器 |
| OSGi bundle | CMake target | 模块边界用 CMake 表达 |
| Eclipse extension point | ResourceHandlerRegistry 直接注册 | 无需 extension registry |

### 5.2 省略的 Java 模块及原因

| 省略部分 | 原因 |
|---|---|
| org.artop.aal.extender | Eclipse extension point 机制不适用；SDG 厂商扩展按需在 runtime 补 ResourceHandler |
| org.artop.aal.validation | 核心已在 emf-validation；autosar 约束直接 registerConstraint |
| org.artop.aal.workspace | 纯 Eclipse IDE 集成（IProject/IMarker/nature/preference），headless 按 URI 直接加载 |
| 所有 Activator 类 | Eclipse plugin 生命周期管理，C++ 无对应概念 |
| emf-edit 的 ItemProvider/UI 类 | Eclipse JFace/SWT UI 桥接，C++ 无 UI 框架（Command/EditingDomain 已实现，Provider 层仅留接口骨架） |

### 5.3 子集化（非省略）的模块

xcore / acceleo / compare 三个模块实现了 Java 对应功能的**子集**：
- **xcore**：Parser + Generator 完整，但缺 `import`/`@Extension`/op body 解释器
- **acceleo**：Parser + Engine 完整，但 AQL/MTL 语法是子集（缺 switch/case/Tuple/Set 等）
- **compare**：Match/Diff/Merge 齐全，但相似度算法简化（无 ProximityMatchEngine 打分）

这些是"够用即可"的工程取舍，注释中均明确标注了覆盖范围。

### 5.4 中完整度模块的补齐（对齐 Java 关键功能）

**emf-xcore**：
- **opposite 引用配对**（对齐 `XcoreEcoreBuilder`）：第三遍遍历，在目标 EClass 中按名查找对应
  EReference 并双向 `setEOpposite`。修复了 `oppositeName` 被解析但从未应用的缺陷。
- **unique 修饰符**：EReference 默认 `setUnique(true)`，对齐 Java EReference.unique 默认值。

**emf-compare**：
- **DifferenceState 枚举**（对齐 `DifferenceState.java`）：PENDING/MERGED/DISCARDED。
  MergeEngine 合并后标记 diff 为 MERGED，支持按状态统计/过滤。
- **Match::getDifferences()**（对齐 `Match.java#getDifferences`）：Comparison 提供
  按 Match 反查 diff 列表的方法（const/非 const 双版本）。
- **Conflict 结构**（对齐 `Conflict.java`/`ConflictKind.java`）：REAL/PSEUDO 冲突类型 +
  leftDifferences/rightDifferences 双侧 diff 列表 + Comparison 的冲突管理 API。

**emf-validation**：
- **ConstraintParser 裸表达式语义修正**：裸属性引用（`self.attr` 无比较符）从错误的 `> 0`
  改为 `CMP_IS_NOT_NULL`（属性必须已设置），对齐 Java OCL `not attr.oclIsUndefined()`。
  `.size()` 无比较符保留 `> 0`（非空检查，语义正确）。
- **ConstraintMode 传递**：ConstraintDescriptor::instantiate() 现在将 mode（LIVE/BATCH）
  传递到 Constraint，对齐 Java `EvaluationMode`。
- **死代码清理**：移除 EValidator.cpp 中未使用的 `isEmpty` 辅助函数。
- **OCL 编译缓存**（对齐 Java `OCLExpressionCache`）：`AnnotationConstraintLoader` 加
  per-EClass 编译缓存 `g_compileCache`，避免同一 EClass 的 OCL 表达式重复编译。
- **LiveValidator 增量化**：`notifyChanged` 只 `validateNow(target)` 校验变更对象本身，
  不递归校验子树，对齐 Java live validation 的增量语义。
- **核心 AUTOSAR 业务约束**（`AutosarConstraints.h`）：反射式实现 5 类 artop 常见约束，
  通过 `registerAutosarConstraints(validator)` opt-in 注册：
  - `short_name_non_empty` / `short_name_unique_in_parent`（shortName 非空 + 同父同类型唯一）
  - `uuid_non_empty`（Identifiable.uuid 非空）
  - `category_required`（lowerBound>=1 的 category 非空）
  - `no_unresolved_proxy`（跨 resource 引用必须可解析）
  支持 BATCH+LIVE 双模式。

---

### 5.5 Notification / Compare / Validation 性能对齐

本节总结 emf-common/emf-compare/emf-validation 三模块对齐 Java 性能的关键优化。

**emf-common（通知机制）**：
- **eNotify 去复制**（对齐 Java `NotifierImpl.eNotify`）：原 `Notifier::notify` 每次都
  做 `adapters.find(this)` 确认 adapter 归属，O(n²)。改为仅快照 copy 后遍历，
  0.03→0.02 us/notify（57153 对象场景）。
- **NotificationChain.merge()**（对齐 Java `NotificationChainImpl.add(NotificationChain)`）：
  新增 `merge(std::vector<Notification>)` 重载，逐条调 `add()` 复用 SET+SET 合并 /
  ADD+REMOVE 抵消语义。`EObjectWithInverseEList::dispatchChain` 改用
  `NotificationChain::merge + dispatch`，消除本类约 30 行重复合并逻辑。

**emf-compare（模型比较）**：
- **类型分桶 + 同位置优先 + perfect 早停**（对齐 Java `ProximityEObjectMatcher`）：
  right 端按 EClass name 分桶（`unordered_map<EClass name, vector<index>>`），
  proximity 只在同类型桶内匹配，O(L×R) → O(Σ L_k×R_k)。对 identical 模型先试同位置
  right[i]，sim=1.0 直接采纳（O(1)），避免桶内扫描。
- **RequirementEngine + 拓扑序 merge**（对齐 Java `DiffEngine.computeRequirements` +
  `MergeEngine`）：新增 `RequirementEngine` 计算 Diff 间依赖（ADD 子依赖 ADD 父、
  REFERENCE_CHANGE 依赖被引用对象 ADD、DELETE 父依赖 DELETE 子、MOVE 依赖 ADD），
  `MergeEngine` 按拓扑序逐 diff 应用（DFS 后序，环检测打破）。`srcToDst` 改为可变映射，
  ADD 克隆新对象后注册 `srcToDst[added]=cloned` 供后续 REFERENCE_CHANGE 映射引用。
- **LCS 最小 MOVE 集合**（对齐 Java `ReferenceChangeFinder`）：`detectMoves` 用最长公共
  子序列计算最小 MOVE 集合，LCS 中的元素锚定不产 MOVE，相比朴素法减少 MOVE 数量。
  内存保护 `kLcsMaxN=2048`（2048² int ≈ 16MB），超限退化为朴素 O(n) 检测。

**emf-validation（模型校验）**：
- **OCL 编译缓存**：见上 §5.4 emf-validation 条目。
- **LiveValidator 增量化**：见上 §5.4 emf-validation 条目。

---

## 6. 构建与测试

### 构建

```bash
# 原生构建
cd /workspace/cpp/emf-cpp/build
cmake .. -DEMF_BUILD_TESTS=ON
cmake --build . -j4

# Windows 交叉编译（llvm-mingw）
cd /workspace/cpp/emf-cpp/build-mingw
cmake .. -DCMAKE_SYSTEM_NAME=Windows \
         -DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc \
         -DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++ \
         -DEMF_BUILD_TESTS=ON
cmake --build . -j4
```

### 测试通过情况

| 模块 | 测试数 | 状态 |
|---|---|---|
| emf-common | ~195 | 通过 |
| emf-ecore | ~80 | 通过 |
| emf-ecore-util | ~277 | 通过 |
| emf-xsd | ~132 | 通过 |
| emf-validation | ~38 | 通过 |
| emf-compare | 22 | 通过（含 DifferenceState/Conflict/getDifferences） |
| emf-xcore | 10 | 通过（含 opposite 配对 + unique 修饰符） |
| emf-acceleo | 20（含 8 个对齐集成测试） | 通过 |
| emf-sphinx | ~69 | 通过 |
| emf-artop-runtime | 15 | 通过 |
| emf-artop-codegen | 3 | 通过 |
| emf-xmi | 78 | 通过（硬编码路径已改用 `EMFCPP_SOURCE_DIR`/`EMF_BUILD_DIR` 宏） |
| emf-ecore-codegen | 51 | 通过（单值 EAttribute getter 命名规范已修正） |
| emf-edit | 25 | 通过（CommandHelper + 5 命令 execute/undo/redo 全覆盖） |

---

## 7. 性能优化（XMI 反序列化热路径）

XMI/XML 反序列化是模型工具的核心热路径（加载 .ecore / .arxml）。本节记录
已落地的四项优化，以及与 Java EMF 的性能对比。

### 7.1 优化栈总览

| 层 | 优化 | 提升（100M 文件累计） | 风险 |
|---|---|---|---|
| 解析器 | 手写逐字符 → **pugixml**（MIT，SIMD + in-situ） | 解析非瓶颈，但代码大幅简化 | 无 |
| 中间层 | 删除 XmlNode，build*/apply* 直接消费 `pugi::xml_node` | ~10%（省一层字符串拷贝） | 无 |
| 编译 | **PGO**（profile-guided optimization） | ~27%（3256ms → 2389ms） | 无 |
| 分配器 | glibc malloc → **jemalloc**（thread cache + size class bin） | ~14%（2378ms → 2048ms） | 无（仅链接选项） |

四项优化互不冲突，叠加后 100M 文件加载从 5906ms 降到 2048ms（2.9x）。

### 7.2 pugixml 替换手写解析器

**动机**：原 `XMILoader.cpp` 内嵌一个逐字符 XML 解析器（`isNameChar` 查找表 +
`skipProlog`/`parseElement`/`readAttrValue`），代码复杂且维护成本高。

**方案**：引入 [pugixml 1.16](https://pugixml.org/)（MIT 许可证，3 个文件无依赖），
源码 vendored 在 `emf-xmi/third-party/pugixml/`。

```cmake
# emf-xmi/CMakeLists.txt
add_library(emf_xmi ... third-party/pugixml/pugixml.cpp)
target_include_directories(emf_xmi PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/third-party/pugixml)
```

**关键 flag**：`parse_default | parse_ws_pcdata` 保留所有文本节点，
对齐原手写解析器的行为（实体展开 / 注释丢弃 / CDATA 处理 / 行尾归一化）。

**生命周期约束**：`pugi::xml_document` 必须存活到所有 `pugi::xml_node` 引用使用
完毕——`XMILoader::load()` 中 `XmlParser` 作为局部变量持有 `doc_`，覆盖整个
EObject 构建过程。

### 7.3 跳过 XmlNode 中间层

**原架构**：`pugixml → XmlNode（字符串拷贝） → EObject`
**新架构**：`pugixml → EObject`（直通，零拷贝）

`pugi::xml_node::name()` / `value()` / `attribute().value()` 返回 `const char*`
直接指向文档缓冲区，无需拷贝到中间 `XmlNode` 结构。

改造点（均在 `XMILoader.cpp` 匿名 namespace）：
- `splitNodeName(node, prefix, local)` — 从 `node.name()` 拆分 `prefix:local`
- `localNameIs(node, expected)` — 用 `strcmp` 零分配比较 local 部分
- `getNodeText(node)` — 累积 pcdata/cdata 文本
- 所有 `build*`/`apply*` 方法签名从 `const XmlNode&` 改为 `const pugi::xml_node&`
- 字段访问：`node.attr("x")` → `node.attribute("x").value()`
  `node.hasAttr("x")` → `!node.attribute("x").empty()`
  `node.children` 遍历 → `node.children()` + `if (child.type() != pugi::node_element) continue;`

### 7.4 PGO（Profile-Guided Optimization）

**动机**：CMake 默认 `CMAKE_BUILD_TYPE` 未设置 = Debug（-g 无优化），是隐藏的
性能杀手。即使切到 Release（-O3），编译器对分支预测/内联的猜测仍不够准。

**PGO 流程**（两阶段编译）：

```bash
# 阶段 1：instrument 编译（插桩）
mkdir build-pgo && cd build-pgo
cmake .. -DCMAKE_BUILD_TYPE=Release \
         -DCMAKE_CXX_FLAGS="-fprofile-generate=/tmp/pgo-data" \
         -DCMAKE_C_FLAGS="-fprofile-generate=/tmp/pgo-data"
cmake --build . -j$(nproc)

# 阶段 2：用代表性 workload 跑 instrument 二进制，收集 profile
./emf_benchmark <代表性 ecore 文件> 5

# 阶段 3：recompile 用 profile 指导优化
cmake .. -DCMAKE_BUILD_TYPE=Release \
         -DCMAKE_CXX_FLAGS="-fprofile-use=/tmp/pgo-data -fprofile-correction" \
         -DCMAKE_C_FLAGS="-fprofile-use=/tmp/pgo-data -fprofile-correction"
cmake --build . -j$(nproc)
```

**效果**：100M 文件加载 3256ms → 2389ms（提速 27%），主要来自热点分支
（`localNameIs` 的 `strcmp`、`EClassImpl` 构造、feature 查找）的内联优化。

### 7.5 jemalloc 替换 glibc malloc

**动机**：Java GC 用 bump-pointer 分配（Eden 区指针碰撞 O(1)），C++ 用 glibc
malloc（空闲链表查找）。jemalloc 的 thread cache + size class bin 接近 bump-pointer
语义，是侵入性最小的对齐方案。

**部署方式**（无需改代码，仅链接选项）：

```bash
# Linux：LD_PRELOAD 注入
LD_PRELOAD=/path/to/libjemalloc.so.2 ./emf_benchmark <file> <iters>

# 或编译期链接（CMake）
target_link_libraries(emf_benchmark PRIVATE jemalloc)

# Windows：交叉编译 jemalloc.dll，部署到 exe 同目录
# ./configure --host=x86_64-w64-mingw32 --prefix=... && make && make install
# 链接 -ljemalloc，运行时 jemalloc.dll 与 exe 同目录
```

**效果**（100M 文件，3 轮中位数）：

| 配置 | load (ms) | save (ms) | vs Java load |
|---|---|---|---|
| Java EMF | 1647 | — | 1.0x |
| C++ PGO（glibc malloc） | 2378 | 2190 | 1.44x |
| **C++ PGO + jemalloc** | **2048** | **2048** | **1.24x** |

**为什么没追平 Java**：jemalloc 仍需 size class bin 查找 + thread cache 补充，
不如 Java Eden 的单条 ADD 指令。剩余 24% 差距主要来自 EObject 构建逻辑
（字符串拷贝、hash 查找、反射派发），malloc 仅占 14% 改进空间。

### 7.6 arena/对象池方案（暂缓）

理论上 arena 分配器（bump-pointer + 整块释放）能进一步对齐 Java GC，但需把
EObject 所有权从"独立 new + Resource 持有"改为"arena 分配 + arena 统一释放"，
破坏现有所有权模型：
- 单个 EObject 可能被多个 Resource / 跨资源引用
- 业务代码可能 `resource->getContents().erase(i)` 单独移除 EObject
  （arena 无法单独释放）

Java 用 GC 自动追踪可达性免费解决了这个问题，C++ 要复刻需重写所有权模型，
风险高于收益，暂缓。

### 7.7 瓶颈定位

用 pugixml 纯解析（不构建 EObject）测得 100M 文件仅 197ms，而完整加载
2048ms——**96.7% 时间花在 EObject 构建**，XML 解析非瓶颈。后续优化方向
应聚焦 EObject 构建逻辑（字符串 intern、feature 查找 hash 优化、反射派发
去虚化），而非继续优化解析器。

### 7.8 性能对比（小文件）

PGO + jemalloc 后，小文件 C++ 全面领先 Java（Java 有 JVM 启动 + JIT 预热开销）：

| 文件 | 大小 | C++ (ms) | Java (ms) | C++ 优势 |
|---|---|---|---|---|
| GenericsGoCrazy | 7.4K | 0.146 | 6.217 | 43x |
| ExtendedMetaData | 10K | 0.128 | 5.780 | 45x |
| XMLType | 27K | 0.223 | 6.557 | 29x |
| Ecore | 32K | 0.585 | 10.271 | 18x |
| XSD | 47K | 0.833 | 12.218 | 15x |
| GenModel | 65K | 0.768 | 15.090 | 20x |

**取舍总结**：Java 用 1.5-2x 内存 + GC 暂停 + JIT 预热，换来大文件 24% 加载
速度优势；C++ + jemalloc 用 24% 速度劣势，换来 1/3 内存 + 无暂停 + 无预热。
对 EMF 模型加载器场景（一次性加载、长期持有、嵌入到其他程序），C++ 取舍
通常更划算。

### 7.9 benchmark 工具

- `/tmp/emf-bench/emf_bench_pgo` — PGO + jemalloc 优化的 benchmark 二进制
- `/tmp/emf-bench/gen_large` — 大 XMI 文件生成器（用 ecore 元模型，两端无需额外注册 package）
- `/workspace/cpp/emf-cpp/emf_benchmark.cpp` — benchmark 源码（load + save 计时，CSV 输出）
- `/workspace/cpp/emf-cpp/build-pgo/` — PGO 构建目录

**生成大文件**：`./gen_large <output.xmi> <targetSizeMB>`（如 `./gen_large 100 file.xmi`）

### 7.10 统一 Benchmark 套件（notification / compare / validation / cpp-vs-java）

除上述 XMI 序列化 benchmark 外，`/workspace/benchmark/` 下还有一套统一组织的
benchmark 套件，覆盖 notification/compare/validation 三模块及 C++ vs Java 对比。
所有 benchmark 通过 `run_benchmark.sh` 统一入口运行，详见 `benchmark/README.md`。

**目录结构**：
```
benchmark/
├── README.md              使用说明
├── build_cpp_benchmark.sh C++ benchmark 构建脚本
├── run_benchmark.sh       统一运行入口
├── cpp/                   C++ benchmark 源码（5 个）
├── java/                  Java benchmark 源码（对比用）
├── data/                  测试数据（large_96m.arxml 96MB）
└── results/BASELINE.md    性能基线归档
```

**运行方式**：
```bash
cd /workspace/benchmark
bash build_cpp_benchmark.sh              # 构建（首次约 10 分钟）
./run_benchmark.sh all                   # C++ 全套（arxml+notification+compare+validation）
./run_benchmark.sh notification          # 单独运行通知 benchmark
./run_benchmark.sh cpp-vs-java           # C++ vs Java 对比
```

**当前基线**（2026-07-09，详见 `benchmark/results/BASELINE.md`）：

| Benchmark | 输入 | 关键指标 | 耗时 |
|-----------|------|----------|------|
| notification | 57153 对象 | eNotify 吞吐 | 0.02 us/notify |
| compare | 1494 对象 (identical) | match+diff | 152 ms |
| validation | 57153 对象 | batch validate | 4500 ms (8051 diags) |
| validation | 1494 对象 | batch validate | 35 ms (9 diags) |

**已知遗留**：大文件（57153 对象）compare 仍 >120s，根因 AUTOSAR ecore 无 iD=true 标记，
所有对象走 proximity 匹配。待 artop identifier 集成（手动注册 shortName 作 ID）解决。

---

## 8. 应用层

`/workspace/invoke/server/autosar_server.cpp` 是 HTTP 服务，提供：
- ARXML 文件加载/保存（经 emf-artop-runtime）
- 模型树浏览/编辑（经 emf-ecore 反射）
- 校验（经 emf-validation）
- 代码生成（经 emf-acceleo + emf-xcore）

构建产物 `/workspace/output/autosar_server.exe`（Windows PE32+，162MB，
静态链接，8MB 栈），配套 `frontend/index.html` 提供 Web 编辑器。
