#!/bin/bash
# 批量运行 5 个 sample round-trip 测试，对比 Java 输出
set -u
ulimit -s unlimited 2>/dev/null
export ASAN_OPTIONS=detect_leaks=0
TEST=/tmp/roundtrip_test
SAMPLES=/workspace/output/samples
JAVA_OUT=/workspace/java/demo/output
RESULT_LOG=/tmp/sample_test_results.log
> "$RESULT_LOG"

if [ ! -f "$TEST" ]; then
  echo "ERROR: $TEST not found. Run build_roundtrip_test.sh first."
  exit 1
fi

SAMPLES_LIST=(
  "GeneralDefinitionReferenceBase.arxml"
  "AISpecification_PhysicalDimension_LifeCycle_Standard.arxml"
  "GeneralDefinitionEnumerationTables.arxml"
  "AISpecificationKeywordSetBlueprint.arxml"
  "AISpecificationCollectionBodyBlueprint.arxml"
)

PASS=0
FAIL=0
SKIP=0

for sample in "${SAMPLES_LIST[@]}"; do
  input="$SAMPLES/$sample"
  java_ref="$JAVA_OUT/$sample"
  output="/tmp/roundtrip_out_${sample}"

  echo "===== Testing: $sample =====" | tee -a "$RESULT_LOG"

  if [ ! -s "$input" ]; then
    echo "SKIP (empty input): $sample" | tee -a "$RESULT_LOG"
    SKIP=$((SKIP + 1))
    continue
  fi

  # 运行测试（超时 120s，防止 segfault 卡死）
  timeout 120 "$TEST" "$input" "$output" "$java_ref" 2>/tmp/roundtrip_stderr_${sample}.log
  rc=$?

  if [ $rc -eq 0 ]; then
    echo "PASS: $sample" | tee -a "$RESULT_LOG"
    PASS=$((PASS + 1))
  elif [ $rc -eq 7 ]; then
    # DIFF（rc=7 表示 C++ 输出与 Java 不同）
    echo "DIFF: $sample (rc=$rc)" | tee -a "$RESULT_LOG"
    # 显示 roundtrip_test 的 stdout（含 diff 信息）
    cat /tmp/roundtrip_stderr_${sample}.log | tail -5 >> "$RESULT_LOG"
    FAIL=$((FAIL + 1))
  elif [ $rc -eq 124 ]; then
    echo "TIMEOUT: $sample (rc=$rc)" | tee -a "$RESULT_LOG"
    FAIL=$((FAIL + 1))
  else
    echo "FAIL: $sample (rc=$rc)" | tee -a "$RESULT_LOG"
    tail -10 /tmp/roundtrip_stderr_${sample}.log >> "$RESULT_LOG"
    FAIL=$((FAIL + 1))
  fi
  echo "" | tee -a "$RESULT_LOG"
done

echo "===== Summary =====" | tee -a "$RESULT_LOG"
echo "PASS: $PASS" | tee -a "$RESULT_LOG"
echo "FAIL: $FAIL" | tee -a "$RESULT_LOG"
echo "SKIP: $SKIP" | tee -a "$RESULT_LOG"
