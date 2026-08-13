# emf-artop Benchmark 套件

C++ emf-artop 与 Java ARTOP 的性能基准测试套件。所有 benchmark 统一通过
`run_benchmark.sh` 入口运行，避免重复编写测试代码。

## 目录结构

```
benchmark/
├── README.md                    本文档
├── build_cpp_benchmark.sh       C++ benchmark 构建脚本
├── run_benchmark.sh             统一运行入口（所有 benchmark 类型）
├── cpp/                         C++ benchmark 源码
│   ├── ArxmlBenchmark.cpp       arxml load/save roundtrip（大文件序列化）
│   ├── EcoreXmiBenchmark.cpp    纯 ecore XMI load/save（不依赖 artop）
│   ├── NotificationBenchmark.cpp  通知机制（eNotify / EContentAdapter / NotificationChain）
│   ├── CompareBenchmark.cpp     模型比较（match + diff）
│   ├── ValidationBenchmark.cpp  模型校验（batch + live）
│   └── init_all_packages.cpp    AUTOSAR 420+ 子包初始化
├── java/                        Java benchmark 源码（对比用）
│   ├── ArxmlBenchmark.java      artop 完整版（依赖 sphinx/OSGi）
│   ├── ArxmlBenchmarkSimple.java  纯 EMF 版（无 OSGi 依赖）
│   ├── EcoreXmiBenchmark.java   纯 ecore XMI
│   └── classes/                 编译产物
├── data/                        测试数据
│   └── large_96m.arxml          96MB AUTOSAR 模型（57153 对象）
└── results/                     性能基线归档
    └── BASELINE.md              历次测量基线数据
```

## 快速开始

### 1. 构建 C++ benchmark

```bash
# 前置：emf-cpp 库已构建（cd /workspace/cpp/emf-cpp/build && cmake --build . -j4）
cd /workspace/benchmark
bash build_cpp_benchmark.sh
```

构建脚本会：
1. 并行编译 4206 个 AUTOSAR 模型 .cpp → .o（首次约 10 分钟，增量缓存）
2. 链接 5 个 benchmark 二进制到 `cpp/`：
   - `arxml_benchmark` / `ecore_xmi_benchmark`
   - `notificationbenchmark` / `comparebenchmark` / `validationbenchmark`

> 注：每个 artop benchmark 二进制约 250MB（静态链接 4200+ 模型 .o）。这些是构建产物，
> 可随时删除重建，不应提交到版本库。

### 2. 运行 benchmark

```bash
cd /workspace/benchmark

# 运行 C++ 全套（arxml + notification + compare + validation），默认大文件 3 轮
./run_benchmark.sh all

# 单独运行某类 benchmark
./run_benchmark.sh arxml                        # arxml roundtrip
./run_benchmark.sh notification                 # 通知机制
./run_benchmark.sh compare                      # 模型比较
./run_benchmark.sh validation                # 模型校验
./run_benchmark.sh ecore-xmi                 # 纯 ecore XMI

# 指定输入文件和迭代次数
./run_benchmark.sh compare small.arxml 5

# C++ vs Java 对比（需 Java ARTOP 环境在 /tmp/artop_plugins）
./run_benchmark.sh cpp-vs-java
```

### 3. 构建 Java benchmark（对比用）

```bash
# 需 Java artop 环境
ARTOP_PLUGINS=/tmp/artop_plugins
JAVA_CP=$(ls $ARTOP_PLUGINS/*.jar | tr '\n' ':'):$WORKSPACE/java/demo/lib/*.jar
javac -cp "$JAVA_CP" -d benchmark/java/classes benchmark/java/*.java
```

## Benchmark 说明

### arxml（ArxmlBenchmark）

测量 arxml 文件反序列化（load）和序列化（save）耗时。对齐 Java `ArxmlBenchmark.java`。

- 输入：arxml 文件（默认 96MB large_96m.arxml）
- 输出：临时 arxml 文件（测完自动删除，避免磁盘累积）
- 指标：load 时间、save 时间、对象数

### ecore-xmi（EcoreXmiBenchmark）

纯 EMF ecore 元模型的 XMI load/save，不依赖 artop。对齐 Java `EcoreXmiBenchmark.java`。
用于评估 XMI 引擎本身性能，排除 AUTOSAR 模型开销。

### notification（NotificationBenchmark）

测量 EMF 通知机制性能，对齐 Java `EContentAdapter` / `eNotify` 特性：

1. **load**：arxml 加载耗时
2. **collect**：DFS 收集所有 EObject 耗时
3. **EContentAdapter attach**：递归挂载内容适配器耗时
4. **eNotify × N**：对每个 EObject 发一条 SET 通知，测 eNotify 吞吐（us/notify）
5. **NotificationChain**：批量构造 + dispatch（含 SET+SET 合并）

### compare（CompareBenchmark）

测量模型比较性能（match + diff），对齐 Java `DefaultMatchEngine` / `DefaultDiffEngine`。
加载同一文件两次作 left/right，比较 identical 模型测 match 性能基线。

- 指标：load 时间（2x）、compare 时间、matches 数、diffs 数

### validation（ValidationBenchmark）

测量模型校验性能，对齐 Java `IBatchValidator` / live validation：

1. **batch validate**：全树 DFS 批量校验
2. **live attach**：LiveValidator 递归挂载
3. **live validateNow**：单对象增量校验

## 设计原则

1. **复用优先**：所有 benchmark 统一入口 `run_benchmark.sh`，新增 benchmark 类型只需
   加一个 `run_xxx()` 函数，不重复编写构建/运行逻辑。
2. **磁盘友好**：输出文件测完即删（atexit + rm），不累积大文件。二进制产物不提交版本库。
3. **warmup 隔离**：默认 3 轮，首轮作 warmup 不计入统计（排除 JIT/缓存预热）。
4. **对齐 Java**：每个 C++ benchmark 对应一个 Java 版本，指标定义一致，便于横向对比。

## 性能基线

历史测量数据归档在 `results/BASELINE.md`，包含各 benchmark 在不同优化阶段的耗时，
用于回归对比。
