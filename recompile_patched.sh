#!/bin/bash
# recompile_patched.sh — 仅重新编译含 null-check 的 patched .cpp，然后链接 benchmark
set -e

WORKSPACE=${WORKSPACE:-/workspace}
EMF_CPP=$WORKSPACE/cpp/emf-cpp
CXX=${CXX:-g++}
MODEL_GEN=$WORKSPACE/.build_cache/autosar448_combined
MODEL_BUILD=$WORKSPACE/.build_cache/autosar448_build
BENCH_DIR=$WORKSPACE/benchmark
JOBS=${JOBS:-4}

INCLUDES="-I$EMF_CPP/emf-common/include \
          -I$EMF_CPP/emf-ecore/include \
          -I$EMF_CPP/emf-ecore-util/include \
          -I$EMF_CPP/emf-xmi/include \
          -I$EMF_CPP/emf-sphinx/include \
          -I$EMF_CPP/emf-validation/include \
          -I$EMF_CPP/emf-compare/include \
          -I$EMF_CPP/emf-artop/emf-artop-runtime/include \
          -I$EMF_CPP/emf-ecore-codegen/include \
          -I$MODEL_GEN"

# Step 1: 找出所有含 null-check pattern 的 patched .cpp（即刚被 fix_eget_null.py 修改的文件）
echo "==== Step 1: Find patched .cpp files ===="
PATCHED=$(grep -rl ' ? std::any(static_cast<emf::common::EList<emf::common::EObject\*>' "$MODEL_GEN" --include='*.cpp' | sort)
COUNT=$(echo "$PATCHED" | wc -l)
echo "  Patched files to recompile: $COUNT"

# Step 2: 并行编译 patched .cpp
echo "==== Step 2: Compiling $COUNT patched files ($JOBS parallel) ===="
TASK_FILE=$(mktemp)
for f in $PATCHED; do
    echo "$f" >> "$TASK_FILE"
done

compile_one() {
    local f="$1"
    local rel=${f#$MODEL_GEN/}
    local obj="$MODEL_BUILD/obj/${rel%.cpp}.o"
    mkdir -p "$(dirname "$obj")"
    $CXX -std=c++17 -O2 -w -c $INCLUDES -o "$obj" "$f" 2>/dev/null || {
        echo "  WARN: compile failed: ${rel}"
    }
}
export -f compile_one
export CXX MODEL_GEN MODEL_BUILD EMF_CPP
export INCLUDES="$INCLUDES"

cat "$TASK_FILE" | xargs -P "$JOBS" -I {} bash -c 'compile_one "$@"' _ {}
rm -f "$TASK_FILE"
echo "  Compilation done"

# Step 3: 链接 benchmark
EMF_LIBS="$EMF_CPP/build/emf-common/libemf_common.a \
          $EMF_CPP/build/emf-ecore/libemf_ecore.a \
          $EMF_CPP/build/emf-ecore-util/libemf_ecore_util.a \
          $EMF_CPP/build/emf-xmi/libemf_xmi.a \
          $EMF_CPP/build/emf-sphinx/libemf_sphinx.a \
          $EMF_CPP/build/emf-compare/libemf_compare.a \
          $EMF_CPP/build/emf-validation/libemf_validation.a \
          $EMF_CPP/build/emf-ecore-codegen/libemf_ecore_codegen.a \
          $EMF_CPP/build/emf-edit/libemf_edit.a \
          $EMF_CPP/build/emf-artop/emf-artop-runtime/libemf_artop_runtime.a"

MODEL_OBJS=$(find "$MODEL_BUILD/obj" -name '*.o' | sort)
echo "==== Step 3: Linking benchmark (model .o: $(echo "$MODEL_OBJS" | wc -l)) ===="
$CXX -std=c++17 -O2 -w -pthread \
    $INCLUDES \
    -o "$BENCH_DIR/cpp/arxml_benchmark" \
    "$BENCH_DIR/cpp/ArxmlBenchmark.cpp" \
    "$BENCH_DIR/cpp/init_all_packages.cpp" \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lpthread
echo "==== Benchmark ready ===="
ls -la "$BENCH_DIR/cpp/arxml_benchmark"
