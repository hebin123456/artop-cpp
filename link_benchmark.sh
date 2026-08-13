#!/bin/bash
# link_benchmark.sh — 仅链接 benchmark，跳过编译（复用已有 .o）
set -e

WORKSPACE=${WORKSPACE:-/workspace}
EMF_CPP=$WORKSPACE/cpp/emf-cpp
CXX=${CXX:-g++}
MODEL_BUILD=$WORKSPACE/.build_cache/autosar448_build
BENCH_DIR=$WORKSPACE/benchmark

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
          -I$WORKSPACE/.build_cache/autosar448_combined"

MODEL_OBJS=$(find "$MODEL_BUILD/obj" -name '*.o' | sort)
echo "==== Linking arxml_benchmark (model .o: $(echo "$MODEL_OBJS" | wc -l)) ===="

$CXX -std=c++17 -O2 -w -pthread \
    $INCLUDES \
    -o "$BENCH_DIR/cpp/arxml_benchmark" \
    "$BENCH_DIR/cpp/ArxmlBenchmark.cpp" \
    "$BENCH_DIR/cpp/init_all_packages.cpp" \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lpthread

echo "==== arxml_benchmark ready ===="
ls -la "$BENCH_DIR/cpp/arxml_benchmark"
