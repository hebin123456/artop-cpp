#!/bin/bash
# build_cpp_benchmark.sh — 编译 C++ arxml benchmark（并行版）
# 编译 4206 个模型 .cpp → .o，然后链接 benchmark
set -e

WORKSPACE=${WORKSPACE:-/workspace}
EMF_CPP=$WORKSPACE/cpp/emf-cpp
CXX=${CXX:-g++}
MODEL_GEN=$WORKSPACE/.build_cache/autosar448_combined
MODEL_BUILD=$WORKSPACE/.build_cache/autosar448_build
BENCH_DIR=$WORKSPACE/benchmark
JOBS=${JOBS:-3}

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

# ===== Step 1: 并行编译模型 .o =====
mkdir -p "$MODEL_BUILD/obj"

# 生成需要编译的任务列表（跳过已编译且最新的）
TASK_FILE=$(mktemp)
ALL_CPP=$(find "$MODEL_GEN" -name '*.cpp' | sort)
TOTAL=$(echo "$ALL_CPP" | wc -l)
echo "==== Step 1: Compiling $TOTAL model .cpp files ($JOBS parallel) ===="

NEED_COMPILE=0
for f in $ALL_CPP; do
    rel=${f#$MODEL_GEN/}
    obj="$MODEL_BUILD/obj/${rel%.cpp}.o"
    if [ -f "$obj" ] && [ "$obj" -nt "$f" ]; then
        continue  # 已编译且最新
    fi
    echo "$f|$obj" >> "$TASK_FILE"
    NEED_COMPILE=$((NEED_COMPILE + 1))
done
echo "  Already compiled: $((TOTAL - NEED_COMPILE)), need compile: $NEED_COMPILE"

# 并行编译函数
compile_one() {
    local line="$1"
    local src="${line%%|*}"
    local obj="${line##*|}"
    mkdir -p "$(dirname "$obj")"
    $CXX -std=c++17 -O1 -w -c $INCLUDES -o "$obj" "$src" 2>/dev/null || {
        echo "  WARN: compile failed: ${src#$MODEL_GEN/}"
    }
}
export -f compile_one
export CXX MODEL_GEN EMF_CPP
export INCLUDES="$INCLUDES"

# 用 xargs 并行
cat "$TASK_FILE" | xargs -P "$JOBS" -I {} bash -c 'compile_one "$@"' _ {}
rm -f "$TASK_FILE"

OBJ_COUNT=$(find "$MODEL_BUILD/obj" -name '*.o' | wc -l)
echo "==== Model .o compilation done: $OBJ_COUNT files ===="

# ===== Step 2: 链接 benchmark =====
# 注意：enum_literals 已由 Step 1 编译为 autosar448_enum_literals.gen.o，
# 无需单独编译（否则与 Step 1 产物重复定义）
echo "==== Step 2: Linking benchmark ===="
MODEL_OBJS=$(find "$MODEL_BUILD/obj" -name '*.o' | sort)
echo "  Model .o count: $(echo "$MODEL_OBJS" | wc -l)"

$CXX -std=c++17 -O2 -w -pthread \
    $INCLUDES \
    -o "$BENCH_DIR/cpp/arxml_benchmark" \
    "$BENCH_DIR/cpp/ArxmlBenchmark.cpp" \
    "$BENCH_DIR/cpp/init_all_packages.cpp" \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lpthread

echo "==== Benchmark binary: $BENCH_DIR/cpp/arxml_benchmark ===="
echo "Run: $BENCH_DIR/cpp/arxml_benchmark [input.arxml] [output.arxml] [iterations]"

# ===== Step 3: 链接其余 4 个 benchmark（notification/compare/validation/ecore-xmi）=====
# 复用 Step 1 编译的 MODEL_OBJS，避免重复编译 4200+ 模型文件。
# notification/compare/validation 依赖 init_all_packages.cpp（AUTOSAR 模型初始化）。
# ecore-xmi 只依赖 emf-xmi + emf-ecore（纯 ecore，无需 AUTOSAR 模型对象）。
echo "==== Step 3: Linking notification/compare/validation benchmarks ===="
for BENCH in NotificationBenchmark CompareBenchmark ValidationBenchmark; do
    echo "  Linking $BENCH..."
    $CXX -std=c++17 -O2 -w -pthread \
        $INCLUDES \
        -o "$BENCH_DIR/cpp/$(echo $BENCH | tr 'A-Z' 'a-z')" \
        "$BENCH_DIR/cpp/${BENCH}.cpp" \
        "$BENCH_DIR/cpp/init_all_packages.cpp" \
        $MODEL_OBJS \
        -Wl,--start-group $EMF_LIBS -Wl,--end-group \
        -lpthread
done

echo "  Linking EcoreXmiBenchmark (no artop model deps)..."
$CXX -std=c++17 -O2 -w -pthread \
    $INCLUDES \
    -o "$BENCH_DIR/cpp/ecore_xmi_benchmark" \
    "$BENCH_DIR/cpp/EcoreXmiBenchmark.cpp" \
    -Wl,--start-group \
        $EMF_CPP/build/emf-common/libemf_common.a \
        $EMF_CPP/build/emf-ecore/libemf_ecore.a \
        $EMF_CPP/build/emf-ecore-util/libemf_ecore_util.a \
        $EMF_CPP/build/emf-xmi/libemf_xmi.a \
    -Wl,--end-group \
    -lpthread

echo "==== Benchmark binaries ready ===="
echo "  $BENCH_DIR/cpp/notificationbenchmark [input.arxml] [iterations]"
echo "  $BENCH_DIR/cpp/comparebenchmark    [input.arxml] [iterations]"
echo "  $BENCH_DIR/cpp/validationbenchmark [input.arxml] [iterations]"
echo "  $BENCH_DIR/cpp/ecore_xmi_benchmark [input.xmi] [output.xmi] [iterations]"
