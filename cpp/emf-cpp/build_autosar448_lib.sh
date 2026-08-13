#!/bin/bash
# 编译 /workspace/.build_cache/autosar448_combined 的所有 .cpp 文件成 .o，打包成 libautosar448.a
# 输出到 /tmp/autosar448_native_build/libautosar448.a（与 build_roundtrip_test.sh 一致）
set -u
SRC=/workspace/.build_cache/autosar448_combined
OUT=/tmp/autosar448_lib_build
LIB=/tmp/autosar448_native_build/libautosar448.a
LOG=/tmp/autosar448_build.log
mkdir -p "$OUT" "$(dirname "$LIB")"

# 找所有 .cpp 文件（含 init_all_packages.cpp）
mapfile -t CPPS < <(find "$SRC" -name "*.cpp" | sort)
N=$(nproc)
echo "Compiling ${#CPPS[@]} cpp files with $N threads..."

INCLUDES="-I/workspace/cpp/emf-cpp/emf-common/include -I/workspace/cpp/emf-cpp/emf-ecore/include -I/workspace/cpp/emf-cpp/emf-ecore-util/include -I/workspace/cpp/emf-cpp/emf-xmi/include -I/workspace/cpp/emf-cpp/emf-artop/emf-artop-runtime/include -I/workspace/cpp/emf-cpp/emf-ecore-codegen/include -I$SRC"
# 与 build_roundtrip_test.sh 一致：启用 ASan（emf 库带 ASan，必须匹配，否则 undefined symbol）
CFLAGS="-std=c++17 -O0 -w -c -fsanitize=address -fno-omit-frame-pointer -g"

cat > "$OUT/compile_one.sh" <<EOF
#!/bin/bash
cpp="\$1"
rel="\${cpp#$SRC/}"
outname=\$(echo "\$rel" | tr '/' '_' | sed 's/\.cpp\$/.o/')
out="$OUT/\$outname"
# 增量编译：.o 存在且比 .cpp 新则跳过
if [ -f "\$out" ] && [ "\$out" -nt "\$cpp" ]; then
  echo "SKIP \$rel"
  exit 0
fi
if c++ $CFLAGS $INCLUDES "\$cpp" -o "\$out" 2>"$OUT/\${outname}.err"; then
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
skip=$(grep -c "^SKIP " "$LOG" || true)
bad=$(grep -c "^BAD " "$LOG" || true)
echo "===== Compile Result ====="
echo "OK: $ok / ${#CPPS[@]}"
echo "SKIP: $skip / ${#CPPS[@]}"
echo "BAD: $bad / ${#CPPS[@]}"

if [ "$bad" -gt 0 ]; then
  echo "===== First 30 BAD files ====="
  grep "^BAD " "$LOG" | head -30
  exit 1
fi

echo "===== Archiving into $LIB ====="
find "$OUT" -name "*.o" -printf "%p\n" | sort > "$OUT/obj_list.txt"
echo "Total .o files: $(wc -l < "$OUT/obj_list.txt")"

# 使用 lld 链接器避免 bfd ar 在 1.7GB archive 上 OOM
export PATH="/root/.swiftly/bin:$PATH"
ar rcs "$LIB" $(cat "$OUT/obj_list.txt")
echo "===== Done: $LIB ====="
ls -lh "$LIB"
