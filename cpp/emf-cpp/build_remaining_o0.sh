#!/bin/bash
# 用 -O0 编译剩余的 .cpp 文件（大文件 -O0 快很多）
set -u
SRC=/tmp/autosar448_gen
OUT=/tmp/autosar448_lib_build
LOG=/tmp/autosar448_build_o0.log
N=$(nproc)

# 找未编译的文件
> /tmp/remaining_cpps.txt
while IFS= read -r cpp; do
  rel="${cpp#$SRC/}"
  outname=$(echo "$rel" | tr '/' '_' | sed 's/\.cpp$/.o/')
  if [ ! -f "$OUT/$outname" ]; then
    echo "$cpp" >> /tmp/remaining_cpps.txt
  fi
done < <(find "$SRC" -name "*.cpp" | sort)

total=$(wc -l < /tmp/remaining_cpps.txt)
echo "Compiling $total remaining files with -O0 using $N threads..."

INCLUDES="-I/workspace/cpp/emf-cpp/emf-common/include -I/workspace/cpp/emf-cpp/emf-ecore/include -I/workspace/cpp/emf-cpp/emf-ecore-util/include -I/workspace/cpp/emf-cpp/emf-xmi/include -I/workspace/cpp/emf-cpp/emf-artop/emf-artop-runtime/include -I/workspace/cpp/emf-cpp/emf-ecore-codegen/include -I/tmp/autosar448_gen"
CFLAGS="-std=c++17 -O0 -w -c"

cat > "$OUT/compile_one_o0.sh" <<EOF
#!/bin/bash
cpp="\$1"
rel="\${cpp#$SRC/}"
outname=\$(echo "\$rel" | tr '/' '_' | sed 's/\.cpp\$/.o/')
out="$OUT/\$outname"
if c++ $CFLAGS $INCLUDES "\$cpp" -o "\$out" 2>"$OUT/\${outname}.err"; then
  echo "OK \$rel"
else
  echo "BAD \$rel"
fi
EOF
chmod +x "$OUT/compile_one_o0.sh"

xargs -P"$N" -I{} bash "$OUT/compile_one_o0.sh" {} < /tmp/remaining_cpps.txt > "$LOG" 2>&1

ok=$(grep -c "^OK " "$LOG" || true)
bad=$(grep -c "^BAD " "$LOG" || true)
echo "===== Compile Result ====="
echo "OK: $ok / $total"
echo "BAD: $bad / $total"

if [ "$bad" -gt 0 ]; then
  echo "===== First 30 BAD files ====="
  grep "^BAD " "$LOG" | head -30
  exit 1
fi

echo "===== Done. Total .o: $(ls $OUT/*.o 2>/dev/null | wc -l) ====="
