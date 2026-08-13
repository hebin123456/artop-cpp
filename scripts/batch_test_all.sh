#!/bin/bash
# 批量测试所有 input → Java output 对比
# 输入: /workspace/java/demo/output_test/*.arxml
# 期望: /workspace/java/demo/output/*.arxml (Java 输出)
set -u
ulimit -s unlimited 2>/dev/null
export ASAN_OPTIONS=detect_leaks=0
TEST=/tmp/roundtrip_test
INPUT_DIR=/workspace/java/demo/output_test
JAVA_DIR=/workspace/java/demo/output
TMP_OUT=/tmp/batch_test_out.xml

PASS=0
FAIL=0
SKIP=0
FAIL_LIST=()

for input in "$INPUT_DIR"/*.arxml; do
  fname=$(basename "$input")
  java_out="$JAVA_DIR/$fname"
  if [ ! -f "$java_out" ]; then
    echo "SKIP (no java output): $fname"
    SKIP=$((SKIP+1))
    continue
  fi
  # 跳过空文件
  if [ ! -s "$input" ]; then
    echo "SKIP (empty input): $fname"
    SKIP=$((SKIP+1))
    continue
  fi
  rm -f "$TMP_OUT"
  timeout 60 "$TEST" "$input" "$TMP_OUT" >/dev/null 2>/tmp/batch_err.txt
  rc=$?
  if [ "$rc" -eq 124 ]; then
    echo "TIMEOUT: $fname"
    FAIL=$((FAIL+1))
    FAIL_LIST+=("$fname (TIMEOUT)")
    continue
  fi
  if [ "$rc" -ne 0 ]; then
    echo "CRASH (rc=$rc): $fname"
    FAIL=$((FAIL+1))
    FAIL_LIST+=("$fname (CRASH rc=$rc)")
    continue
  fi
  if [ ! -f "$TMP_OUT" ]; then
    echo "NO_OUTPUT: $fname"
    FAIL=$((FAIL+1))
    FAIL_LIST+=("$fname (NO_OUTPUT)")
    continue
  fi
  if diff -q "$java_out" "$TMP_OUT" >/dev/null 2>&1; then
    echo "PASS: $fname"
    PASS=$((PASS+1))
  else
    echo "DIFF: $fname"
    FAIL=$((FAIL+1))
    FAIL_LIST+=("$fname (DIFF)")
  fi
done

echo "===== Summary ====="
echo "PASS: $PASS"
echo "FAIL: $FAIL"
echo "SKIP: $SKIP"
if [ "${#FAIL_LIST[@]}" -gt 0 ]; then
  echo "===== Failed files ====="
  for f in "${FAIL_LIST[@]}"; do
    echo "  $f"
  done
fi
