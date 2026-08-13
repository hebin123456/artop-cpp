#!/bin/bash
# 批量运行 roundtrip_test 对所有 sample arxml 进行验证
set -e

TEST=/tmp/roundtrip_test
SAMPLES_DIR=/workspace/output/samples
JAVA_OUTPUT_DIR=/workspace/java/demo/output
CPP_OUTPUT_DIR=/tmp/roundtrip_output

mkdir -p "$CPP_OUTPUT_DIR"

if [ ! -f "$TEST" ]; then
  echo "ERROR: $TEST not found. Run build_roundtrip_test.sh first."
  exit 1
fi

echo "===== Round-trip Test ====="
PASS=0
FAIL=0
SKIP=0

for input in "$SAMPLES_DIR"/*.arxml; do
  [ -f "$input" ] || continue
  basename=$(basename "$input")
  cpp_output="$CPP_OUTPUT_DIR/$basename"
  
  # 查找对应的 Java 参考输出
  java_output=""
  # 尝试多种命名模式
  for candidate in "$JAVA_OUTPUT_DIR/$basename"; do
    if [ -f "$candidate" ]; then
      java_output="$candidate"
      break
    fi
  done
  
  echo "--- Testing: $basename ---"
  
  # 运行 round-trip
  if [ -n "$java_output" ]; then
    "$TEST" "$input" "$cpp_output" "$java_output" 2>/tmp/roundtrip_stderr.log
    result=$?
  else
    "$TEST" "$input" "$cpp_output" 2>/tmp/roundtrip_stderr.log
    result=$?
  fi
  
  # 显示 stderr（加载/保存信息）
  cat /tmp/roundtrip_stderr.log
  
  if [ $result -eq 0 ]; then
    echo "  RESULT: MATCH (identical to Java output)"
    PASS=$((PASS + 1))
  elif [ $result -eq 1 ]; then
    echo "  RESULT: DIFF (differs from Java output)"
    FAIL=$((FAIL + 1))
  else
    echo "  RESULT: ERROR (load/save failed)"
    FAIL=$((FAIL + 1))
  fi
  echo ""
done

echo "===== Summary ====="
echo "PASS: $PASS"
echo "FAIL: $FAIL"
