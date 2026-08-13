#!/bin/bash
# 编译 roundtrip_test，链接 libautosar448.a + emf 库
set -e
EMF=/workspace/cpp/emf-cpp
GEN=/workspace/.build_cache/autosar448_combined
AUTOSAR_LIB=/tmp/autosar448_native_build/libautosar448.a
OUT=/tmp/roundtrip_test

if [ ! -f "$AUTOSAR_LIB" ]; then
  echo "ERROR: $AUTOSAR_LIB not found. Wait for autosar448 compilation."
  exit 1
fi

INCLUDES="-I$EMF/emf-common/include -I$EMF/emf-ecore/include -I$EMF/emf-ecore-util/include -I$EMF/emf-xmi/include -I$EMF/emf-artop/emf-artop-runtime/include -I$EMF/emf-ecore-codegen/include -I$GEN"

LIBS="$AUTOSAR_LIB $EMF/build/emf-artop/emf-artop-runtime/libemf_artop_runtime.a $EMF/build/emf-xmi/libemf_xmi.a $EMF/build/emf-ecore-codegen/libemf_ecore_codegen.a $EMF/build/emf-ecore-util/libemf_ecore_util.a $EMF/build/emf-ecore/libemf_ecore.a $EMF/build/emf-common/libemf_common.a -lstdc++fs -pthread"

ASAN=-fsanitize=address
# Ensure lld is on PATH (bfd ld OOM-killed by 1.7GB libautosar448.a)
export PATH="/root/.swiftly/bin:$PATH"
echo "Using linker: $(which ld.lld 2>/dev/null || echo 'NOT FOUND')"
echo "Compiling roundtrip_test..."
# Pre-compile init_all_packages.cpp to .o (avoid OOM: 420 heavy headers + 1.7GB archive)
c++ -std=c++17 -O0 -w $ASAN -fno-omit-frame-pointer -g -c $INCLUDES $GEN/init_all_packages.cpp -o /tmp/init_all_packages.o
c++ -std=c++17 -O0 -w $ASAN -fno-omit-frame-pointer -g -c $INCLUDES $EMF/emf-xmi/tests/roundtrip_test.cpp -o /tmp/roundtrip_test.o
# Link with lld (bfd ld OOM-killed by 1.7GB libautosar448.a)
# --start-group/--end-group: resolve circular deps between autosar448 sub-packages
c++ -std=c++17 -O0 -w $ASAN -fno-omit-frame-pointer -g -fuse-ld=lld -B/root/.swiftly/bin /tmp/roundtrip_test.o /tmp/init_all_packages.o \
    -Wl,--start-group $LIBS -Wl,--end-group -o $OUT
echo "OK: $OUT"
