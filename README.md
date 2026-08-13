# emf-cpp

**C++17 重写 Eclipse EMF + Java ARTOP，定位为 headless AUTOSAR 模型工具平台。**

用纯 C++ 重新实现 Eclipse 建模框架（EMF）与 ARTOP（AUTOSAR 工具平台）的核心能力，
提供 AUTOSAR（ARXML）模型的加载、创建、序列化、比较、校验与代码生成，
目标是在脱离 Eclipse/OSGi 运行时的情况下，以更高的性能与更低的内存占用完成大规模 AUTOSAR 模型的处理。

---

## 核心特性

- **完整对齐 Eclipse EMF**：EObject / EClass / EPackage / Resource / URI / Notification / EList / Command 等核心抽象。
- **XMI / ARXML 序列化**：支持 XMI 加载与保存、AUTOSAR ARXML 的往返（round-trip）读写、跨文件代理与 UUID 解析。
- **模型比较与校验**：`emf-compare`（match/diff）、`emf-validation`（batch / live 校验）。
- **代码生成（codegen）**：从 `.ecore` / `.genmodel` 生成 C++17 静态模型，替代 JET 模板。
- **AUTOSAR 特化层**：对齐 `org.artop.aal.*`，覆盖 AUTOSAR 4.0 / 4.4.8 / gautosar 元模型。
- **性能基准**：内置 C++ 与 Java ARTOP 的对比基准套件（详见 `benchmark/`）。
- **无 OSGi 依赖**：headless 运行，适合 CI 与批处理。

---

## 目录结构

```
emf-cpp/
├── cpp/
│   ├── emf-cpp/                 # ★ 核心 C++ 实现（主交付物）
│   │   ├── CMakeLists.txt
│   │   ├── emf-common/          # EObject / URI / Resource / Notification / EList / Command
│   │   ├── emf-ecore/           # EClass / EPackage / EFactory + Impl + EcorePackage
│   │   ├── emf-ecore-util/      # EcoreUtil（128+ 方法）/ Copier / EList 变体
│   │   ├── emf-ecore-codegen/   # GenModel → C++17 代码生成（替代 JET）
│   │   ├── emf-xmi/             # XMIResource / SAXXMIHandler / UUID / 跨文件代理
│   │   ├── emf-xsd/             # XSD 元模型
│   │   ├── emf-edit/            # Command 框架
│   │   ├── emf-compare/         # 模型比较（match + diff）
│   │   ├── emf-validation/      # 模型校验（batch + live）
│   │   ├── emf-xcore/           # Xcore DSL 解析器
│   │   ├── emf-acceleo/         # Acceleo MTL 模板 / M2T 引擎
│   │   ├── emf-sphinx/          # Sphinx headless 核心子集
│   │   ├── emf-artop/           # ★ AUTOSAR 特化层（对齐 org.artop.aal.*）
│   │   │   ├── emf-artop-runtime/   # AUTOSAR 序列化/反序列化、资源/工厂/版本元数据
│   │   │   └── emf-artop-codegen/   # 从 .ecore 生成 C++ 静态模型
│   │   └── examples/            # arxml_roundtrip / arxml_validate 示例
│   └── demo/                    # library 模型 demo
├── java/                        # Java 参考实现（用于行为对齐与对比）
│   ├── codegen/                 # 代码生成 Java 端
│   ├── demo/                    # EMF demo
│   ├── emf-demo/                # EMF 示例
│   └── tests/                   # 行为对齐测试
├── benchmark/                   # C++ emf-artop vs Java ARTOP 性能基准
│   ├── cpp/                     # C++ benchmark 源码
│   ├── java/                    # Java benchmark 源码
│   ├── data/                    # 测试数据
│   └── results/                 # 性能基线
├── models/                      # AUTOSAR 元模型定义
│   ├── autosar448/              # AUTOSAR 4.4.8 .ecore / .genmodel
│   └── gautosar/                # gautosar .ecore / .genmodel
├── artop-headless/              # headless 基准运行器（Java）
├── decompiler/                  # ARTOP 插件反编译产物（C++ 端对齐参考）
│   ├── autosar40/ autosar448/ common/ gautosar/
│   ├── eel.common/ eel.serialization/ emf_xmi/ extender/
│   ├── serialization/ validation/ workspace/
├── libs/                        # 第三方运行时与开源参考源
│   ├── artop-runtime/           # ARTOP 运行时资源（plugin/feature 描述）
│   └── opensource/              # Eclipse EMF / Sphinx 等开源参考源
├── scripts/                     # 辅助脚本（Python / shell）
├── doc/                         # 项目文档
│   ├── ARCHITECTURE.md          # 架构文档（分层、模块职责、对齐关系）
│   ├── DESIGN.md                # 设计文档
│   ├── USER_MANUAL.md           # 用户手册（构建、API、用法）
│   ├── ALIGNMENT_REPORT.md      # Java 端对齐报告
│   ├── MEMORY_BENCHMARK.md      # 内存基准报告
│   └── WIKI.md                  # Wiki
├── *.py / *.sh                  # 顶层工具脚本（patch / bench / recompile）
└── LICENSE                      # MIT
```

---

## 架构概览

项目采用两层架构，完全镜像 Java 端（ARTOP 建立在 Eclipse EMF 之上）的依赖关系：

- **第 1 层 `emf-*`（12 个模块）**：通用 EMF 模型基础设施，对齐
  `org.eclipse.emf.*` / `org.eclipse.acceleo` / `org.eclipse.sphinx.emf`，**不涉及 AUTOSAR 业务**。
- **第 2 层 `emf-artop/*`（2 个模块）**：AUTOSAR 特化层，对齐
  `org.artop.aal.*`，依赖第 1 层。

详细分层图、模块职责表与对齐关系见 [doc/ARCHITECTURE.md](doc/ARCHITECTURE.md)。

---

## 构建

### 依赖

- C++17 编译器（g++ ≥ 7 或 clang ≥ 6）
- CMake ≥ 3.16
- pthread

### 原生构建

```bash
cd cpp/emf-cpp
mkdir -p build && cd build
cmake .. -DEMF_BUILD_TESTS=ON
cmake --build . -j$(nproc)
```

构建产物：

- 库：`build/emf-*/libemf_*.a`（静态库）
- 测试：`build/bin/emf_*_tests`

### 运行测试

```bash
cd cpp/emf-cpp/build
ctest --output-on-failure          # 全部测试套件
./bin/emf_common_tests             # 单独运行某套件
./bin/emf_compare_tests
./bin/emf_validation_tests
```

当前测试规模：13 个套件，~1500+ 测试用例。

更多用法（codegen、ARXML 往返、通知/比较/校验 API）见 [doc/USER_MANUAL.md](doc/USER_MANUAL.md)。

---

## 基准测试

C++ emf-artop 与 Java ARTOP 的对比基准，统一入口：

```bash
cd benchmark
bash run_benchmark.sh
```

覆盖：arxml load/save 往返、纯 ecore XMI、通知机制、模型比较、模型校验、AUTOSAR 420+ 子包初始化。
基线数据见 `benchmark/results/` 与 [doc/MEMORY_BENCHMARK.md](doc/MEMORY_BENCHMARK.md)。

---

## 关于本仓库内容

为保证仓库精简，以下内容**未纳入版本库**（可通过构建或运行重新生成）：

- 全部编译产物（`.o` / `.a` / `.so` / `.exe` / ELF 可执行文件、`.class` / `.jar`）
- 构建目录（`build/`、`.build_cache/`）
- 大体积生成数据（`core`、`baseline_96m_out.arxml`、`benchmark/data/large_96m.arxml` 等）
- 第三方 Eclipse/ARTOP 完整安装（`artop/`）

参见 `.gitignore`。

---

## License

MIT，详见 [LICENSE](LICENSE)。
