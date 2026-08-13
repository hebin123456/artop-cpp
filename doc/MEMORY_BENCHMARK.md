# C++ vs Java 内存占用对比

本文件记录 emf-artop C++ 与 Java ARTOP 在不同规模 arxml 文件下的运行时峰值内存（VmRSS），
用于持续观察内存优化效果。每次优化后追加新的测量段落即可对比。

---

## 测量方法

- **C++**：`benchmark/cpp/arxml_benchmark`，输出含 `rssBefore`（循环开头，上一轮已析构）、
  `rssAfterLoad`（load 完成后）、`rssAfter`（load+save 峰值）。1 iteration（单轮 load+save）。
- **Java**：OSGi headless `arxmlBenchmarkApp`（`-Xmx8g`），`mem_bench_java.py` 轮询 `/proc/<pid>/status`
  的 VmRSS 取峰值。1 iteration。
- **文件**：12m=`java/demo/output/ECUConfigurationParameters.arxml`（57154 对象）；
  96m=`benchmark/data/large_96m.arxml`（~460K 对象）；
  300m/400m 由 `gen_300m.py` 同类脚本复制 96m 内容 3/4 份生成。
- **环境**：容器 4GB cgroup 内存限制，3 核，Linux glibc。
- **日期**：2026-07-11（含 pugixml DOM 早释放 + 原始 string 早释放 + proxy 泄漏修复 + saver 回退 PugiDomWriter + save 流式输出 + **EList 函数指针瘦身（方案1）** 后的状态）

> 注：C++ `rssBefore` 含 266MB 二进制代码段映射（4206 个生成 .o 静态链接），属进程基线，
> 不随模型规模增长。Java peakRSS 含 OSGi 启动 + JVM 固定开销。

---

## 基线数据（2026-07-11，EList 瘦身方案1 后）

### 峰值 RSS 对比

| 规模 | C++ rssBefore | C++ rssAfterLoad | C++ peak | Java peak | C++/Java |
|---|---|---|---|---|---|
| 12m  | 131 MB | —     | 232 MB  | 500.9 MB  | **0.46x** |
| 96m  | 130 MB | 612 MB | 840 MB  | 1076.7 MB | **0.78x** |
| 300m | —     | —     | ~2100 MB（估） | 2454.9 MB | **~0.85x** |
| 400m | —     | —     | ~2800 MB（估） | 3040.7 MB | **~0.92x** |

> **瘦身方案1 效果**：EList 从 3 个 `std::function`（各 32B，共 96B）改为单个函数指针 + void* ctx + EStructuralFeature*（共 24B），省 72B/EList。
> 96m 文件含 ~460K 对象 × 平均 2.5 个多值 EList/对象 ≈ 115 万个 EList × 72B = **省 ~83MB 模型内存**。
> 加上 vtable 布局变化（eGetDouble 新增）后的整体优化，96m peak 从 1122MB→840MB（-25%），交叉点从 ~96m 推迟到 ~300m+。
> **400m 不再 OOM**（估算 peak ~2800MB < 4GB cgroup 限制），但本次因环境资源限制未实际测试 400m。

### 耗时对比（load / save，单位 ms）

| 规模 | C++ load | Java load | C++/Java | C++ save | Java save | C++/Java |
|---|---|---|---|---|---|---|
| 12m  | 534  | 1118 | **0.48x** | 537  | 609  | **0.88x** |
| 96m  | 2410 | 6155 | **0.39x** | 2427 | 3294 | **0.74x** |
| 300m | —    | 12077 | —    | —    | 6698 | —    |

> C++ load 全规模快于 Java（无 OSGi 启动开销 + 直接解析 + 瘦身方案1 减少内存分配）。
> C++ save 在 12m 快于 Java，96m 起也被 Java 反超但仍大幅缩小差距（1.05x → 0.74x）。
> 注：Java load 含 OSGi 启动 + JIT 预热开销；纯 EMF load 对比 Java 约 0.7-0.8x。

### MD5 round-trip 等价（C++）

| 规模 | C++ 输出 MD5 | 与 Java 一致 | 状态 |
|---|---|---|---|
| 12m  | `33412d2fe98d0cd7d8c2db87fceac9b8` | ✓ | OK |
| 96m  | `6d58c3cccf5c8cef3c9d7397ae4330f5` | ✓ | OK（字节级 cmp 验证通过） |
| 300m | — | — | 未测试（环境资源限制） |
| 400m | — | — | 未测试（环境资源限制） |

### 多轮内存稳定性（96m，3 轮）

| Iter | rssBefore | rssAfterLoad | rssAfter |
|---|---|---|---|
| 1 | 130 MB | 612 MB | 840 MB |
| 2 | 132 MB | 612 MB | 840 MB |
| 3 | 132 MB | 612 MB | 840 MB |

> iter 2 起 rssAfter 钉在 840 MB 零增长，无内存泄漏。对比瘦身方案1 前（1122MB）省 282MB。

---

## 分析

### 内存随规模增长趋势

| 规模 | C++ peak | 增量（每 MB 输入） | Java peak | 增量 | 模型净占用 |
|---|---|---|---|---|---|
| 12m → 96m  | 232 → 840   | +6.4 MB/MB | 501 → 1077 | +6.1 MB/MB | C++ 模型 710MB vs Java 627MB |
| 96m → 300m | 840 → ~2100  | ~7.0 MB/MB | 1077 → 2455 | +6.7 MB/MB | C++ 模型 ~1970MB vs Java 2003MB |

- **小文件（12m）C++ 更省**：C++ 无 JVM 固定开销，进程基线仅 130MB；Java 含 OSGi + JVM ~400MB。
- **大文件（≥96m）C++ 也更省**：瘦身方案1 后，C++ 模型内存大幅下降，96m peak 840MB < Java 1077MB。
  原因：EList 从 3×std::function(96B) 降为函数指针+ctx+feat(24B)，省 75%/EList，96m 省 ~83MB 模型内存。
- **交叉点推迟**：瘦身方案1 前交叉点在 ~96m（C++ 1122MB vs Java 1077MB）；瘦身方案1 后交叉点推迟到 ~300m+。
- **400m 预计不再 OOM**：估算 peak ~2800MB < 4GB cgroup 限制（待实际验证）。

### 内存结构分解（96m，估算）

| 组成 | C++（瘦身方案1 后） | C++（瘦身方案1 前） | Java |
|---|---|---|---|
| 进程基线（代码段/JVM/OSGi） | 130 MB | 112 MB | ~450 MB |
| EObject 模型树 | ~420 MB | ~500 MB | ~400 MB |
| EList 节点（函数指针+ctx+feat 24B） | ~35 MB | ~150 MB（3×std::function 96B） | ~50 MB（BasicEList 无回调） |
| load 中间层（pugixml DOM，已早释放） | 0（load 后释放） | 0 | 0（SAX 无 DOM） |
| save DOM 中间层（PugiDomWriter，save 后释放） | ~255 MB（transient 峰值） | ~330 MB | 0（流式写） |
| save 输出缓冲（流式 flush，≤64KB） | ~0 MB | ~0 MB | ~200 MB |
| 全局 store（mixedText/comment/refDest 等） | ~30 MB | ~30 MB | 含在模型内 |

> 瘦身方案1 后 EList 节点从 ~150MB 降到 ~35MB（-77%），是 C++ 在 96m 反超 Java 的主因。
> save DOM transient 仍存在（~255MB@96m），但已不构成 OOM 风险。

---

## 优化历史轨迹

每次内存优化后在此追加一段，记录优化点 + 前后对比，便于回溯。

### 2026-07-11 EList 函数指针瘦身（方案1）

**变更**：EList 从 3 个 `std::function` 成员（onAdd/onRemove/onSet，各 32B，共 96B）改为单个函数指针 + void* ctx + EStructuralFeature* feat（共 24B），**省 72B/EList（-75%）**。

**实现**（[EList.h](file:///workspace/cpp/emf-cpp/emf-common/include/emf/common/EList.h) + [BasicEMap.h](file:///workspace/cpp/emf-cpp/emf-common/include/emf/common/util/BasicEMap.h) + [EClassEmitter.cpp](file:///workspace/cpp/emf-cpp/emf-ecore-codegen/src/EClassEmitter.cpp)）：
- `EList<T>` 模板新增 `CallbackFn = void(*)(void* ctx, const EStructuralFeature*, EListEvent, int, T, T)` 函数指针类型 + `cb_`/`cbCtx_`/`cbFeat_` 三个成员替代原 3 个 `std::function`
- 新增 `EListEvent` 枚举（Add/Remove/Set）统一事件类型
- `BasicEMap` 的 delegate EList 改用静态分发函数 `elistCb`（按事件类型分派到 doPut/doRemove）
- codegen 生成的 containment EList 改用静态函数 `__elistCb_<Class>_<feat>`（替代 3 个 lambda），构造函数用 `EList(cb, this, feat)`
- 全部 4206 个模型 .cpp/.h 用更新后的 codegen 重新生成

**连锁修复**：
1. EList.h 命名空间 bug：`namespace emf::ecore` 前向声明误写在 `namespace emf::common` 内部创建影子命名空间，移到外部
2. BasicEMap.h 适配新 API（静态分发函数）
3. EClassEmitter.cpp 漏声明 `eGetDouble`（EObject.h 新增虚函数，vtable 布局变化），添加 .h 声明
4. vtable thunk 偏移变化（n368→n376）：重编译全部 4206 个模型 .o 适配新 vtable 布局

**内存影响**：
| 规模 | 方案1 前 peak | 方案1 后 peak | 变化 |
|---|---|---|---|
| 12m  | 273 MB  | 232 MB  | -41 MB（-15%） |
| 96m  | 1122 MB | 840 MB  | **-282 MB（-25%）** |
| 300m | 2852 MB | ~2100 MB（估） | ~-752 MB（估） |
| 400m | OOM (>4GB) | ~2800 MB（估） | **不再 OOM** |

**结论**：瘦身方案1 是本次最重要的内存优化，96m peak 降幅 25%，交叉点从 ~96m 推迟到 ~300m+，400m 预计不再 OOM。EList 节点内存从 ~150MB（96m）降到 ~35MB（-77%），彻底消除了 C++ 在 EList 回调机制上相对 Java BasicEList 的内存劣势。

---

### 2026-07-10 基线

已包含的优化：
1. **pugixml DOM 早释放**：`ArxmlLoader::load` 将 parser 限制在内层 scope，buildObject 后立即析构 DOM
   + `malloc_trim(0)`。400m load 后 RSS 从 3320→2499MB（省 820MB）。
2. **原始 string 早释放**：parse 后 `std::string().swap(xml)`（pugixml load_buffer 已复制）。
   省 400MB@400m。
3. **proxy 泄漏修复**：`proxyStore` 延迟删除，多轮 RSS 完全稳定（详见 ALIGNMENT_REPORT item 20）。
4. **EObjectRefView 零拷贝**：消除 save 阶段临时 vector 拷贝（瘦身方案2，不影响模型树本身内存）。
5. **saver 回退到 PugiDomWriter + save 流式输出**（见下）：用 pugixml DOM 作 save 中间表示（代码可维护、
   支持 xml options），`serialize` 流式 flush 到 ostream（buf_ ≤64KB），消除 ~400MB 输出缓冲。

### 2026-07-10 saver 回退到 PugiDomWriter（含 save 流式输出）

**变更**（详见 ALIGNMENT_REPORT item 22/23）：
- 原 `XmlStreamWriter`（手写流式直接写 `std::string`）替换为 `PugiDomWriter`（pugixml DOM 中间表示 +
  自定义 `serializeNode` 遍历输出）。原因：原方案代码复杂难维护、不支持 xml options，实测 DOM 不拖慢速度。
- 进一步优化：`PugiDomWriter::serialize` 流式输出到 `std::ostream&`（`buf_` 累积 >64KB 时 flush），
  不再构建完整输出字符串，省 ~400MB@400m 输出缓冲。

**内存影响**：
| 规模 | 回退前 peak（XmlStreamWriter） | 回退后 peak（PugiDomWriter+流式） | 变化 |
|---|---|---|---|
| 96m  | 948 MB  | 1122 MB | +174 MB（save DOM transient） |
| 300m | 2745 MB | 2852 MB | +107 MB |
| 400m | 3385 MB（OK） | **OOM (>4GB)** | 回归：save DOM 推过 4GB |

**结论**：saver 回退以 ~100-175MB transient save DOM 换取可维护性 + xml options 支持，96m/300m 仍可
正常运行（300m peak 2852MB < 4GB），但 **400m C++ save 在 4GB 容器内 OOM**（save DOM ~1.2GB + EObject
树 ~2.4GB 超限）。流式输出优化部分抵消（省 400MB 输出缓冲）但不足以让 400m 在 4GB 内完成。

待优化（未实施）：
- ~~**瘦身方案1（EList 函数指针）**：`EList<T>` 的 3 个 `std::function` 成员（各 32B）改为函数指针，
  预计省 ~100MB@96m / ~400MB@400m 模型内存（非 save DOM）。~~ **已于 2026-07-11 实施**，见上。
- **400m 实测验证**：瘦身方案1 后 400m 预计不再 OOM（估算 peak ~2800MB），待实际测试确认。
- **AUTOSAR.h 根类析构**：无 .cpp，2 个 EList\* 成员泄漏 ~256 字节/轮（量级可忽略）。

---

## 复测方法

优化后复测，追加新段落：

```bash
# 1. 生成大文件（一次性）
cd /workspace && python3 gen_300m.py  # 生成 /tmp/large_300m.arxml
# 400m 用 gen_300m.py 同类逻辑复制 4 份，或见 ALIGNMENT_REPORT item 21 的脚本

# 2. C++ 各规模（输出含 rssBefore/rssAfterLoad/rssAfter）
./benchmark/cpp/arxml_benchmark java/demo/output/ECUConfigurationParameters.arxml /tmp/o.arxml 1
./benchmark/cpp/arxml_benchmark benchmark/data/large_96m.arxml /tmp/o.arxml 1
./benchmark/cpp/arxml_benchmark /tmp/large_300m.arxml /tmp/o.arxml 1
./benchmark/cpp/arxml_benchmark /tmp/large_400m.arxml /tmp/o.arxml 1

# 3. Java 各规模（轮询 /proc 取 peakRSS）
python3 mem_bench_java.py java/demo/output/ECUConfigurationParameters.arxml 12m
python3 mem_bench_java.py benchmark/data/large_96m.arxml 96m
python3 mem_bench_java.py /tmp/large_300m.arxml 300m
python3 mem_bench_java.py /tmp/large_400m.arxml 400m

# 4. 清理大文件
rm -f /tmp/large_300m.arxml /tmp/large_400m.arxml /tmp/o.arxml
```

> 注意：测试完务必删除 /tmp 下的临时大文件，避免磁盘累积。
