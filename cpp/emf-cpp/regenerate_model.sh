#!/bin/bash
# 并行编译 /tmp/autosar448_gen 的所有 .cpp 文件，输出 OK/BAD 统计
set -u
SRC=/tmp/autosar448_gen
OUT=/tmp/autosar448_obj
LOG=/tmp/autosar448_compile.log
mkdir -p "$OUT"

# 找所有 .cpp 文件
mapfile -t CPPS < <(find "$SRC" -name "*.cpp" | sort)

N=$(nproc)
echo "Compiling ${#CPPS[@]} cpp files with $N threads..."

INCLUDES_STR="-I/workspace/cpp/emf-cpp/emf-common/include -I/workspace/cpp/emf-cpp/emf-ecore/include -I/workspace/cpp/emf-cpp/emf-ecore-util/include -I/workspace/cpp/emf-cpp/emf-xmi/include -I/workspace/cpp/emf-cpp/emf-artop-runtime/include -I/workspace/cpp/emf-cpp/emf-ecore-codegen/include -I/tmp/autosar448_gen -std=c++17 -fsyntax-only -Wno-all -Wno-error"

# 写一个子脚本，避免数组 export 问题
cat > "$OUT/compile_one.sh" <<EOF
#!/bin/bash
cpp="\$1"
rel="\${cpp#$SRC/}"
outname=\$(echo "\$rel" | tr '/' '_')
if c++ $INCLUDES_STR "\$cpp" 2>"$OUT/\${outname}.err"; then
  echo "OK \$rel"
else
  echo "BAD \$rel"
fi
EOF
chmod +x "$OUT/compile_one.sh"

{
  for cpp in "${CPPS[@]}"; do
    echo "$cpp"
  done
} | xargs -P"$N" -I{} bash "$OUT/compile_one.sh" {} > "$LOG" 2>&1

ok=$(grep -c "^OK " "$LOG" || true)
bad=$(grep -c "^BAD " "$LOG" || true)
echo "===== Result ====="
echo "OK: $ok / ${#CPPS[@]}"
echo "BAD: $bad / ${#CPPS[@]}"

if [ "$bad" -gt 0 ]; then
  echo "===== First 30 BAD files ====="
  grep "^BAD " "$LOG" | head -30
fi
