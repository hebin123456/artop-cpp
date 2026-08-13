# emf-artop 性能基线

本文件归档 emf-artop 各 benchmark 的历史测量数据，用于回归对比。
测量环境：Linux sandbox，g++ -O2，单线程，3 轮取均值（排除首轮 warmup）。

## 测试数据

| 文件 | 大小 | 对象数 | 用途 |
|------|------|--------|------|
| ECUConfigurationParameters.arxml | 12 MB | 57153 | 大文件基线 |
| AISpecification_DataConstr_Blueprint.arxml | 160 KB | 1494 | 中等文件基线 |

---

## 基线数据（2026-07-09，gap 修复后）

### Notification Benchmark

输入：ECUConfigurationParameters.arxml（57153 对象）

| 指标 | 耗时 |
|------|------|
| load | 300 ms |
| collect (DFS) | 8 ms |
| EContentAdapter attach | 0 ms |
| eNotify × 57153 | 1 ms（0.02 us/notify） |
| NotificationChain 构造+dispatch | 3 ms |

**优化历史**：
- eNotify 去复制前：0.03 us/notify
- eNotify 去复制后（任务 4）：0.02 us/notify

### Compare Benchmark

输入：AISpecification_DataConstr_Blueprint.arxml（1494 对象，identical 模型）

| 指标 | 耗时 |
|------|------|
| load (2x) | 10 ms |
| compare (match + diff) | 152 ms |
| matches | 1494 |
| diffs | 0（identical） |

**优化历史**：
- 类型分桶 + 同位置优先前：未测量（O(n²) 爆炸）
- 类型分桶 + 同位置优先 + perfect 早停（任务 5）：152 ms

**已知遗留**：
- 大文件（57153 对象）compare 仍 >120s：根因 AUTOSAR ecore 无 iD=true 标记，
  所有无 ID 对象走 proximity 匹配，computeSimilarity 开销大。
  待 artop identifier 集成（手动注册 shortName 作 ID）解决。

### Validation Benchmark

#### 中等文件（1494 对象）

| 指标 | 耗时 |
|------|------|
| load | 5 ms |
| batch validate | 35 ms |
| diagnostics | 9 |
| live attach | 0 ms |
| live validateNow | 0 ms |

#### 大文件（57153 对象）

| 指标 | 耗时 |
|------|------|
| load | 310 ms |
| batch validate | 4500 ms |
| diagnostics | 8051 |
| live attach | 395 ms |
| live validateNow | 0 ms（单对象增量，已优化） |

**优化历史**：
- OCL 编译缓存前（任务 6）：batch 4448 ms
- OCL 编译缓存后（任务 6）：batch 4500 ms（缓存命中率取决于 EClass 重复度，
  AUTOSAR 模型类型多样，收益主要在重复校验同 EClass 时）
- LiveValidator 增量化（任务 6）：liveValidate 0 ms（notifyChanged 只校验目标对象，
  不递归）

---

## C++ vs Java 对比（arxml roundtrip）

需 Java ARTOP 环境支持，运行 `./run_benchmark.sh cpp-vs-java`。
历史对比数据见 `doc/ARCHITECTURE.md` §7.8。

---

## 回归检查清单

每次修改 notification/compare/validation 代码后，运行对应 benchmark 确认：

1. **Notification**：eNotify 0.02 us/notify 不退化，NotificationChain 3ms 不退化
2. **Compare**：中等文件 152ms 不退化，diffs=0（identical 模型）正确
3. **Validation**：大文件 batch 4500ms 不退化，diagnostics=8051 数量正确

若耗时增加 >20% 或 diagnostic/diff 数量变化，需排查回归原因。
