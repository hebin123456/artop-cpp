#!/bin/bash
# run_benchmark.sh —— 统一 benchmark 运行入口
#
# 用法：
#   ./run_benchmark.sh <type> [input.arxml] [iterations] [extra args...]
#
# type 取值：
#   arxml         C++ arxml load/save roundtrip（大文件序列化性能）
#   ecore-xmi     C++ 纯 ecore XMI load/save（不依赖 artop）
#   notification  C++ 通知机制性能（eNotify / EContentAdapter / NotificationChain）
#   compare       C++ 模型比较性能（match + diff）
#   validation    C++ 模型校验性能（batch + live）
#   cpp-vs-java   C++ emf-artop vs Java ARTOP arxml roundtrip 对比
#   all           依次运行 arxml/notification/compare/validation（C++ 全套）
#
# 默认：
#   input = $WORKSPACE/benchmark/data/large_96m.arxml
#   iterations = 3（排除首轮 warmup）
#
# 示例：
#   ./run_benchmark.sh all                          # C++ 全套，默认大文件
#   ./run_benchmark.sh notification small.arxml 5   # 通知 benchmark，小文件 5 轮
#   ./run_benchmark.sh cpp-vs-java                  # C++ vs Java 对比
set -e

WORKSPACE=${WORKSPACE:-/workspace}
BENCH_DIR=$WORKSPACE/benchmark
CPP_DIR=$BENCH_DIR/cpp

TYPE=${1:-all}
INPUT_FILE=${2:-$BENCH_DIR/data/large_96m.arxml}
ITERATIONS=${3:-3}

# Java classpath（仅 cpp-vs-java 用）
ARTOP_PLUGINS=/tmp/artop_plugins
JAVA_CP=""
if [ -d "$ARTOP_PLUGINS" ]; then
    JAVA_CP=$(ls $ARTOP_PLUGINS/*.jar 2>/dev/null | tr '\n' ':')
    JAVA_CP="$JAVA_CP$WORKSPACE/java/demo/lib/org.artop.aal.common_4.13.0.201912171516.jar"
    JAVA_CP="$JAVA_CP:$WORKSPACE/java/demo/lib/org.artop.aal.autosar448_4.13.0.201912171516.jar"
    JAVA_CP="$JAVA_CP:$WORKSPACE/java/demo/lib/org.artop.aal.serialization_4.13.0.201912171516.jar"
    JAVA_CP="$JAVA_CP:$WORKSPACE/java/demo/lib/org.artop.eel.common_1.1.0.201706291244.jar"
    JAVA_CP="$JAVA_CP:$WORKSPACE/java/demo/lib/org.artop.eel.serialization_1.1.0.201706291244.jar"
    JAVA_CP="$JAVA_CP:$BENCH_DIR/java/classes"
fi

FILE_SIZE=$(stat -c%s "$INPUT_FILE" 2>/dev/null || echo 0)
FILE_MB=$((FILE_SIZE / 1024 / 1024))

print_header() {
    echo "############################################################"
    echo "# $1"
    echo "# File: $INPUT_FILE  (${FILE_MB} MB)"
    echo "# Iterations: $ITERATIONS (excl. warmup)"
    echo "############################################################"
    echo ""
}

# 运行单个 C++ benchmark（自动清理临时输出）
run_cpp() {
    local name=$1
    local binary=$2
    local outfile=$3
    if [ ! -x "$binary" ]; then
        echo "ERROR: $binary not found or not executable."
        echo "       Run $BENCH_DIR/build_cpp_benchmark.sh first."
        return 1
    fi
    echo "=== C++ $name Benchmark ==="
    if [ -n "$outfile" ]; then
        "$binary" "$INPUT_FILE" "$outfile" "$ITERATIONS" 2>&1
        rm -f "$outfile"  # 清理，避免磁盘累积
    else
        "$binary" "$INPUT_FILE" "$ITERATIONS" 2>&1
    fi
    echo ""
}

run_arxml() {
    print_header "C++ arxml load/save roundtrip"
    run_cpp "arxml" "$CPP_DIR/arxml_benchmark" "/tmp/bench_cpp_out.arxml"
}

run_ecore_xmi() {
    print_header "C++ ecore XMI load/save"
    # ecore-xmi 用独立默认输入（小 xmi 文件，非 arxml）
    local xmifile=${2:-$BENCH_DIR/data/xmi/ecore_5m.xmi}
    INPUT_FILE=$xmifile run_cpp "ecore-xmi" "$CPP_DIR/ecore_xmi_benchmark" "/tmp/bench_ecore_out.xmi"
}

run_notification() {
    print_header "C++ notification (eNotify / EContentAdapter / NotificationChain)"
    run_cpp "notification" "$CPP_DIR/notificationbenchmark"
}

run_compare() {
    print_header "C++ compare (match + diff)"
    run_cpp "compare" "$CPP_DIR/comparebenchmark"
}

run_validation() {
    print_header "C++ validation (batch + live)"
    run_cpp "validation" "$CPP_DIR/validationbenchmark"
}

run_cpp_vs_java() {
    print_header "arxml Performance: C++ emf-artop vs Java ARTOP"
    if [ -z "$JAVA_CP" ] || [ ! -d "$ARTOP_PLUGINS" ]; then
        echo "ERROR: Java artop plugins not found at $ARTOP_PLUGINS."
        echo "       cpp-vs-java requires Java ARTOP environment."
        return 1
    fi
    echo "=== [1/2] C++ emf-artop ==="
    "$CPP_DIR/arxml_benchmark" "$INPUT_FILE" /tmp/bench_cpp_out.arxml "$ITERATIONS" 2>&1
    rm -f /tmp/bench_cpp_out.arxml
    echo ""
    echo "=== [2/2] Java ARTOP ==="
    timeout 600 java -Xmx8g -cp "$JAVA_CP" artop.demo.ArxmlBenchmark "$INPUT_FILE" /tmp/bench_java_out.arxml "$ITERATIONS" 2>&1
    rm -f /tmp/bench_java_out.arxml
    echo ""
    echo "############################################################"
    echo "# Benchmark complete."
    echo "############################################################"
}

case "$TYPE" in
    arxml)         run_arxml ;;
    ecore-xmi)     run_ecore_xmi "$@" ;;
    notification)  run_notification ;;
    compare)       run_compare ;;
    validation)    run_validation ;;
    cpp-vs-java)   run_cpp_vs_java ;;
    all)
        run_arxml
        run_notification
        run_compare
        run_validation
        echo "############################################################"
        echo "# All C++ benchmarks complete."
        echo "############################################################"
        ;;
    *)
        echo "Usage: $0 <arxml|ecore-xmi|notification|compare|validation|cpp-vs-java|all> [input] [iterations]"
        echo "  arxml         arxml load/save roundtrip"
        echo "  ecore-xmi     pure ecore XMI load/save"
        echo "  notification  eNotify / EContentAdapter / NotificationChain"
        echo "  compare       model compare (match + diff)"
        echo "  validation    model validation (batch + live)"
        echo "  cpp-vs-java   C++ vs Java arxml roundtrip comparison"
        echo "  all           run arxml+notification+compare+validation (C++ suite)"
        exit 1
        ;;
esac
