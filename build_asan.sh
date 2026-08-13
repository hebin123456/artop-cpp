#!/bin/bash
# build_asan.sh — ASan 版 benchmark：只重编译 runtime+main 带 ASan，复用 4206 生成 .o + emf 静态库
set -e

WORKSPACE=/workspace
EMF_CPP=$WORKSPACE/cpp/emf-cpp
CXX=g++
MODEL_BUILD=$WORKSPACE/.build_cache/autosar448_build
BENCH_DIR=$WORKSPACE/benchmark
ASAN_OBJ=$WORKSPACE/.build_cache/asan_obj
mkdir -p "$ASAN_OBJ"

INCLUDES="-I$EMF_CPP/emf-common/include \
          -I$EMF_CPP/emf-ecore/include \
          -I$EMF_CPP/emf-ecore-util/include \
          -I$EMF_CPP/emf-xmi/include \
          -I$EMF_CPP/emf-sphinx/include \
          -I$EMF_CPP/emf-validation/include \
          -I$EMF_CPP/emf-compare/include \
          -I$EMF_CPP/emf-artop/emf-artop-runtime/include \
          -I$EMF_CPP/emf-ecore-codegen/include \
          -I$EMF_CPP/emf-xmi/third-party/pugixml \
          -I$WORKSPACE/.build_cache/autosar448_combined"

CXXFLAGS="-std=c++17 -O1 -g -fsanitize=address -fno-omit-frame-pointer -w -pthread"

EMF_LIBS="$EMF_CPP/build/emf-common/libemf_common.a \
          $EMF_CPP/build/emf-ecore/libemf_ecore.a \
          $EMF_CPP/build/emf-ecore-util/libemf_ecore_util.a \
          $EMF_CPP/build/emf-xmi/libemf_xmi.a \
          $EMF_CPP/build/emf-sphinx/libemf_sphinx.a \
          $EMF_CPP/build/emf-compare/libemf_compare.a \
          $EMF_CPP/build/emf-validation/libemf_validation.a \
          $EMF_CPP/build/emf-ecore-codegen/libemf_ecore_codegen.a \
          $EMF_CPP/build/emf-edit/libemf_edit.a"

echo "==== Compiling runtime with ASan ===="
RT_SRC="$EMF_CPP/emf-artop/emf-artop-runtime/src"
for f in AutosarXMLLoader AutosarXMLSaver AutosarResource AutosarResourceFactory \
         AutosarReleaseDescriptor AutosarLibraryIndex AutosarMetaModelVersionData \
         IdentifiableUtil UnknownElement; do
  echo "  $f.cpp"
  $CXX $CXXFLAGS $INCLUDES -c "$RT_SRC/$f.cpp" -o "$ASAN_OBJ/$f.o"
done

echo "==== Compiling benchmark main with ASan ===="
$CXX $CXXFLAGS $INCLUDES -c "$BENCH_DIR/cpp/ArxmlBenchmark.cpp" -o "$ASAN_OBJ/ArxmlBenchmark.o"
$CXX $CXXFLAGS $INCLUDES -c "$BENCH_DIR/cpp/init_all_packages.cpp" -o "$ASAN_OBJ/init_all_packages.o"

MODEL_OBJS=$(find "$MODEL_BUILD/obj" -name '*.o' | sort)
echo "==== Linking (model .o: $(echo "$MODEL_OBJS" | wc -l)) ===="

ASAN_RT_OBJS=$($CXX $CXXFLAGS -print-file-name=libasan.a)

$CXX $CXXFLAGS \
    $INCLUDES \
    -o "$BENCH_DIR/cpp/arxml_benchmark_asan" \
    "$ASAN_OBJ"/*.o \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lpthread -ldl

echo "==== arxml_benchmark_asan ready ===="
ls -la "$BENCH_DIR/cpp/arxml_benchmark_asan"
