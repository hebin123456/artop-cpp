#!/bin/bash
# recompile_all_patched.sh — 并行重编译所有 patched .cpp，链接 ASan benchmark
set -e
WORKSPACE=/workspace
EMF_CPP=$WORKSPACE/cpp/emf-cpp
MODEL_BUILD=$WORKSPACE/.build_cache/autosar448_build
MODEL_SRC=$WORKSPACE/.build_cache/autosar448_combined
export MODEL_BUILD MODEL_SRC EMF_CPP

INCLUDES="-I$EMF_CPP/emf-common/include -I$EMF_CPP/emf-ecore/include -I$EMF_CPP/emf-ecore-util/include \
-I$EMF_CPP/emf-xmi/include -I$EMF_CPP/emf-sphinx/include -I$EMF_CPP/emf-validation/include \
-I$EMF_CPP/emf-compare/include -I$EMF_CPP/emf-artop/emf-artop-runtime/include \
-I$EMF_CPP/emf-ecore-codegen/include -I$EMF_CPP/emf-xmi/third-party/pugixml \
-I$MODEL_SRC"
CXXFLAGS="-std=c++17 -O2 -w -pthread"

# 找所有需要重编译的 .cpp（时间戳比 .o 新，或 .o 不存在）
count=0
compile_one() {
    local src="$1"
    local rel="${src#$MODEL_SRC/}"
    local obj="$MODEL_BUILD/obj/${rel%.cpp}.o"
    if [ ! -f "$obj" ] || [ "$src" -nt "$obj" ]; then
        mkdir -p "$(dirname "$obj")"
        g++ $CXXFLAGS $INCLUDES -c "$src" -o "$obj" 2>/dev/null && echo "OK $rel" || echo "FAIL $rel"
    fi
}
export -f compile_one
export MODEL_BUILD MODEL_SRC EMF_CPP CXXFLAGS INCLUDES

echo "==== Finding patched .cpp ===="
PATCHED=$(find "$MODEL_SRC" -name '*.cpp' | while read src; do
    rel="${src#$MODEL_SRC/}"
    obj="$MODEL_BUILD/obj/${rel%.cpp}.o"
    if [ ! -f "$obj" ] || [ "$src" -nt "$obj" ]; then echo "$src"; fi
done)
NPATCHED=$(echo "$PATCHED" | grep -c .)
echo "Need recompile: $NPATCHED files"

echo "==== Parallel compile (8 jobs) ===="
echo "$PATCHED" | xargs -P8 -I{} bash -c 'compile_one "$@"' _ {} | grep -c "^OK" | xargs echo "Compiled OK:"
echo "==== Done ===="
