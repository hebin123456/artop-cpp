# EMF AUTOSAR Windows 构建指南

本指南介绍如何在 Windows 平台上构建 EMF AUTOSAR 项目的 C++ 库、Python 绑定以及 AUTOSAR 编辑器服务端。项目支持两种主要构建路径：

- **交叉编译**（推荐）：在 Linux 上使用 `llvm-mingw` 交叉编译生成 Windows `.exe` / `.pyd`，产物可直接拷贝到 Windows 运行，无需在目标机器安装工具链。
- **原生构建**：在 Windows 上使用 MSYS2/MinGW-w64 或 MSVC 直接编译。

---

## 目录

1. [概述](#1-概述)
2. [前置条件](#2-前置条件)
3. [在 Windows 上构建 C++](#3-在-windows-上构建-c)
4. [在 Windows 上构建 Python 绑定](#4-在-windows-上构建-python-绑定)
5. [在 Windows 上构建服务端](#5-在-windows-上构建服务端)
6. [故障排查](#6-故障排查)
7. [性能优化构建（PGO + jemalloc）](#7-性能优化构建pgo--jemalloc)

---

## 1. 概述

### 1.1 项目结构

| 路径 | 说明 |
| --- | --- |
| `/workspace/cpp/emf-cpp/` | EMF C++ 核心库（11 个静态库） |
| `/workspace/invoke/` | 绑定与构建脚本根目录 |
| `/workspace/invoke/common/emf_autosar_capi.{h,cpp}` | 通用 C API 封装（不透明句柄） |
| `/workspace/invoke/python/emf_autosar_pybind.cpp` | Python 绑定源码（基于 pybind11） |
| `/workspace/invoke/server/` | AUTOSAR 编辑器服务端（HTTP + 前端） |
| `/workspace/invoke/build.sh` | 通用构建脚本（含 `build_python` 函数） |
| `/workspace/invoke/build_mingw.sh` | C++ 交叉编译脚本（Linux → Windows） |
| `/workspace/invoke/server/build_server_win.sh` | 服务端交叉编译脚本 |
| `/workspace/invoke/server/CMakeLists.txt` | 服务端 CMake 构建（支持原生 / 交叉） |
| `/workspace/.build_cache/autosar448_combined/` | 生成的 AUTOSAR 4.4.8 模型源码（约 4206 个 `.cpp`） |
| `/workspace/.build_cache/autosar448_build/obj/` | 模型编译产物（`.o` 文件） |

### 1.2 构建依赖关系

```
EMF C++ 库 (11 个 .a)
        │
        ├──► C++ 示例 (.exe)
        ├──► Python 绑定 (.pyd)
        └──► AUTOSAR 服务端 (.exe)
```

> **重要**：EMF C++ 库必须先构建完成，才能构建任何绑定（C++ / Python / 服务端）。模型代码（约 4206 个 `.cpp`）也必须先编译为 `.o`。

### 1.3 EMF 静态库清单（共 11 个）

| 库名 | 源码目录 |
| --- | --- |
| `emf_common` | `emf-common` |
| `emf_ecore` | `emf-ecore` |
| `emf_ecore_util` | `emf-ecore-util` |
| `emf_xmi` | `emf-xmi` |
| `emf_xsd` | `emf-xsd` |
| `emf_sphinx` | `emf-sphinx` |
| `emf_compare` | `emf-compare` |
| `emf_validation` | `emf-validation` |
| `emf_ecore_codegen` | `emf-ecore-codegen` |
| `emf_edit` | `emf-edit` |
| `emf_artop_runtime` | `emf-artop/emf-artop-runtime` |

### 1.4 Windows 与 Linux 链接差异

| 项目 | Linux | Windows (MinGW) |
| --- | --- | --- |
| 线程库 | `-lpthread` | `-lwinpthread -lws2_32` |
| 静态运行时 | （可选） | `-static-libgcc -static-libstdc++` |
| 可执行后缀 | （无） | `.exe` |
| Python 模块后缀 | `.so` | `.pyd` |

---

## 2. 前置条件

### 2.1 通用要求

- **CMake** ≥ 3.16
- **Python** ≥ 3.8（用于构建 Python 绑定，且需与目标平台 ABI 一致）
- **pybind11**：`python3 -m pip install pybind11`
- **bash** 环境（Linux/macOS 原生支持；Windows 上可用 MSYS2 或 WSL）
- 已生成的模型代码位于 `/workspace/.build_cache/autosar448_combined/`（如缺失，先运行 `bash cpp/emf-cpp/regenerate_model.sh`）

### 2.2 安装 llvm-mingw（交叉编译用）

`llvm-mingw` 提供基于 LLVM 的 MinGW-w64 工具链，可在 Linux 上交叉编译 Windows 程序。推荐使用 UCRT 版本。

```bash
# 1. 下载 ubuntu 预编译包（以 20240619 ucrt 版为例）
wget https://github.com/mstorsjo/llvm-mingw/releases/download/20240619/llvm-mingw-20240619-ucrt-ubuntu-20.04-x86_64.tar.xz

# 2. 解压
tar xf llvm-mingw-20240619-ucrt-ubuntu-20.04-x86_64.tar.xz

# 3. 加入 PATH（建议写入 ~/.bashrc）
export PATH="$PWD/llvm-mingw-20240619-ucrt-ubuntu-20.04-x86_64/bin:$PATH"

# 4. 验证
x86_64-w64-mingw32-g++ --version
```

交叉编译前缀为 `x86_64-w64-mingw32`，因此：

- `g++` → `x86_64-w64-mingw32-g++`
- `gcc` → `x86_64-w64-mingw32-gcc`
- `ar` → `x86_64-w64-mingw32-ar`
- `ranlib` → `x86_64-w64-mingw32-ranlib`

> 如需 32 位目标，可改用 `i686-w64-mingw32` 前缀（对应 i686 版 llvm-mingw），并通过环境变量 `MINGW_PREFIX=i686-w64-mingw32` 覆盖。

### 2.3 服务端额外依赖

服务端依赖两个头文件库（header-only），需放在 `invoke/server/` 目录下：

```bash
cd /workspace/invoke/server

# cpp-httplib
curl -sL https://raw.githubusercontent.com/yhirose/cpp-httplib/master/httplib.h -o httplib.h

# nlohmann/json
mkdir -p nlohmann
curl -sL https://raw.githubusercontent.com/nlohmann/json/develop/single_include/nlohmann/json.hpp -o nlohmann/json.hpp
```

### 2.4 环境变量速查

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `WORKSPACE` | `/workspace` | 工作根目录 |
| `MINGW_PREFIX` | `x86_64-w64-mingw32` | 交叉编译前缀 |
| `JOBS` | `nproc` | 并行编译任务数 |
| `MODEL_GEN` | `$WORKSPACE/.build_cache/autosar448_combined` | 模型源码目录 |
| `MODEL_BUILD` | `$WORKSPACE/.build_cache/autosar448_build` | 模型构建目录（原生） |
| `CXX` | `g++` | C++ 编译器（可设为 mingw 前缀编译器） |

---

## 3. 在 Windows 上构建 C++

本节介绍两种方式：**Linux 交叉编译**（推荐）与 **Windows 原生 MSYS2**。

### 3.1 方式 A：Linux 交叉编译（推荐）

使用 `invoke/build_mingw.sh`，它自动生成 CMake 工具链文件并完成全部步骤。所有产物位于 `/workspace/.build_cache/mingw_build/`。

#### 3.1.1 构建命令

```bash
cd /workspace

# 一次性构建全部（libs + model + cpp）
bash invoke/build_mingw.sh all
```

也可分步执行：

```bash
# Step 1: CMake 交叉编译 EMF 库（自动生成 toolchain.cmake）
bash invoke/build_mingw.sh libs

# Step 2: 并行编译约 4206 个模型 .cpp -> .o
bash invoke/build_mingw.sh model

# Step 3: 链接 C++ 示例 -> .exe
bash invoke/build_mingw.sh cpp
```

#### 3.1.2 各步骤说明

**Step 1 `libs`** —— CMake 交叉编译 EMF 库

脚本会在 `.build_cache/mingw_build/toolchain.cmake` 自动生成如下工具链文件：

```cmake
set(CMAKE_SYSTEM_NAME Windows)
set(CMAKE_SYSTEM_PROCESSOR x86_64)
set(CMAKE_C_COMPILER x86_64-w64-mingw32-gcc)
set(CMAKE_CXX_COMPILER x86_64-w64-mingw32-g++)
set(CMAKE_AR x86_64-w64-mingw32-ar)
set(CMAKE_RANLIB x86_64-w64-mingw32-ranlib)
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
```

随后执行：

```bash
cmake -S cpp/emf-cpp -B .build_cache/mingw_build/cmake \
    -DCMAKE_TOOLCHAIN_FILE=.build_cache/mingw_build/toolchain.cmake \
    -DCMAKE_BUILD_TYPE=Release \
    -DEMF_BUILD_TESTS=OFF \
    -DEMF_BUILD_EXAMPLES=OFF \
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON
cmake --build .build_cache/mingw_build/cmake --parallel "$JOBS"
```

构建完成后，11 个 `.a` 文件会被收集到 `.build_cache/mingw_build/libs/`（平铺结构）。

**Step 2 `model`** —— 编译模型代码

将 `MODEL_GEN` 下约 4206 个 `.cpp` 并行编译为 `.o`，输出到 `.build_cache/mingw_build/model_obj/`。编译选项：

```bash
x86_64-w64-mingw32-g++ -std=c++17 -w -O0 -fPIC $INCLUDES -c <file>.cpp -o <file>.o
```

**Step 3 `cpp`** —— 链接 C++ 示例

```bash
x86_64-w64-mingw32-g++ -std=c++17 -O2 -w $COMMON_INCLUDES \
    -o invoke/cpp/build/cpp_consumer_example.exe \
    invoke/cpp/cpp_consumer_example.cpp \
    invoke/common/emf_autosar_capi.cpp \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lwinpthread -lws2_32 -static-libgcc -static-libstdc++
```

#### 3.1.3 产物与部署

| 产物 | 路径 |
| --- | --- |
| EMF 静态库 | `.build_cache/mingw_build/libs/*.a` |
| 模型对象文件 | `.build_cache/mingw_build/model_obj/**/*.o` |
| C++ 示例 | `invoke/cpp/build/cpp_consumer_example.exe` |

部署到 Windows：直接拷贝 `cpp_consumer_example.exe` 及所需 `.arxml` 文件即可运行，**无需安装任何运行时 DLL**（已静态链接 libgcc/libstdc++）。

### 3.2 方式 B：Windows 原生 MSYS2 构建

若希望在 Windows 上直接构建，可使用 MSYS2 提供的 MinGW-w64 工具链。

#### 3.2.1 安装 MSYS2

1. 从 https://www.msys2.org/ 下载并安装 MSYS2。
2. 打开 **MSYS2 UCRT64** 终端（推荐 UCRT 版本，与 llvm-mingw UCRT 版一致）。
3. 安装工具链：

```bash
pacman -S --noconfirm \
    mingw-w64-ucrt-x86_64-gcc \
    mingw-w64-ucrt-x86_64-cmake \
    make
```

#### 3.2.2 构建 EMF 库（CMake）

```bash
cd /c/path/to/workspace   # 替换为实际 workspace 路径

# 原生构建 EMF 库
cd cpp/emf-cpp
cmake -B build -DCMAKE_BUILD_TYPE=Release \
      -DEMF_BUILD_TESTS=OFF -DEMF_BUILD_EXAMPLES=OFF \
      -DCMAKE_POSITION_INDEPENDENT_CODE=ON
cmake --build build -j
cd ../..
```

#### 3.2.3 编译模型代码

```bash
MODEL_GEN=/workspace/.build_cache/autosar448_combined
MODEL_OBJ=/workspace/.build_cache/autosar448_build/obj
mkdir -p "$MODEL_OBJ"

# 并行编译（PowerShell 或 bash 均可，此处用 bash）
find "$MODEL_GEN" -name '*.cpp' | while read f; do
    rel=${f#$MODEL_GEN/}
    obj="$MODEL_OBJ/${rel%.cpp}.o"
    mkdir -p "$(dirname "$obj")"
    g++ -std=c++17 -w -O0 -fPIC \
        -Icpp/emf-cpp/emf-common/include \
        -Icpp/emf-cpp/emf-ecore/include \
        -Icpp/emf-cpp/emf-xmi/include \
        -Icpp/emf-cpp/emf-sphinx/include \
        -Icpp/emf-cpp/emf-artop/emf-artop-runtime/include \
        -Icpp/emf-cpp/emf-ecore-codegen/include \
        -I"$MODEL_GEN" \
        -c "$f" -o "$obj"
done
```

> 提示：可借助 `xargs -P` 或 GNU parallel 加速。

#### 3.2.4 链接 C++ 示例

```bash
g++ -std=c++17 -O2 -w \
    -Iinvoke/common \
    -Icpp/emf-cpp/emf-common/include \
    -Icpp/emf-cpp/emf-ecore/include \
    -Icpp/emf-cpp/emf-ecore-util/include \
    -Icpp/emf-cpp/emf-xmi/include \
    -Icpp/emf-cpp/emf-sphinx/include \
    -Icpp/emf-cpp/emf-validation/include \
    -Icpp/emf-cpp/emf-compare/include \
    -Icpp/emf-cpp/emf-artop/emf-artop-runtime/include \
    -Icpp/emf-cpp/emf-ecore-codegen/include \
    -Icpp/emf-cpp/emf-xsd/include \
    -Icpp/emf-cpp/emf-edit/include \
    -I"$MODEL_GEN" \
    -o invoke/cpp/build/cpp_consumer_example.exe \
    invoke/cpp/cpp_consumer_example.cpp \
    invoke/common/emf_autosar_capi.cpp \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lwinpthread -lws2_32 -static-libgcc -static-libstdc++
```

---

## 4. 在 Windows 上构建 Python 绑定

Python 绑定源码为 `invoke/python/emf_autosar_pybind.cpp`，基于 pybind11。Linux 上的构建逻辑见 `invoke/build.sh` 的 `build_python` 函数：

```bash
g++ -std=c++17 -O2 -w -shared -fPIC \
    $COMMON_INCLUDES $PY_INCLUDES \
    -o python/build/emf_autosar$PY_SUFFIX \
    python/emf_autosar_pybind.cpp \
    common/emf_autosar_capi.cpp \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lpthread
```

其中：

- `PY_INCLUDES` 来自 `python3 -m pybind11 --includes`
- `PY_SUFFIX` 来自 `python3-config --extension-suffix`
- `EMF_LIBS` 为 11 个静态库
- `MODEL_OBJS` 为约 4206 个 `.o` 文件

> **注意**：本项目**不使用** `setup.py` / `pyproject.toml`，而是直接用 `g++` 编译生成扩展模块。

Windows 上有三种可行方案：

### 4.1 方案对比

| 方案 | 编译器 | 难度 | 适用场景 |
| --- | --- | --- | --- |
| A. MSYS2 + MinGW-w64 | `g++` (UCRT64) | 中 | 与 C++ 构建统一工具链，推荐 |
| B. MSVC + pybind11 | `cl.exe` | 高 | 需匹配官方 CPython ABI |
| C. Linux 交叉编译 | `x86_64-w64-mingw32-g++` | 高 | 需 Windows Python 头文件，ABI 难匹配 |

### 4.2 方案 A：MSYS2 + MinGW-w64（推荐）

此方案与 [3.2](#32-方式-bwindows-原生-msys2-构建) 共用工具链与已构建的 EMF 库 / 模型对象。

#### 4.2.1 安装 Python 与 pybind11

在 **MSYS2 UCRT64** 终端中：

```bash
pacman -S --noconfirm \
    mingw-w64-ucrt-x86_64-python \
    mingw-w64-ucrt-x86_64-pybind11

# 验证
python -c "import pybind11; print(pybind11.get_include())"
```

#### 4.2.2 获取编译参数

```bash
PY_INCLUDES=$(python -m pybind11 --includes)
PY_SUFFIX=$(python-config --extension-suffix)
echo "PY_INCLUDES=$PY_INCLUDES"
echo "PY_SUFFIX=$PY_SUFFIX"
```

#### 4.2.3 构建扩展模块

```bash
cd /workspace

MODEL_OBJS=$(find .build_cache/autosar448_build/obj -name '*.o' | sort | tr '\n' ' ')
EMF_LIBS=$(find cpp/emf-cpp/build -name '*.a' | sort | tr '\n' ' ')

mkdir -p invoke/python/build

g++ -std=c++17 -O2 -w -shared -fPIC \
    -Iinvoke/common \
    -Icpp/emf-cpp/emf-common/include \
    -Icpp/emf-cpp/emf-ecore/include \
    -Icpp/emf-cpp/emf-ecore-util/include \
    -Icpp/emf-cpp/emf-xmi/include \
    -Icpp/emf-cpp/emf-sphinx/include \
    -Icpp/emf-cpp/emf-validation/include \
    -Icpp/emf-cpp/emf-compare/include \
    -Icpp/emf-cpp/emf-artop/emf-artop-runtime/include \
    -Icpp/emf-cpp/emf-ecore-codegen/include \
    -Icpp/emf-cpp/emf-xsd/include \
    -Icpp/emf-cpp/emf-edit/include \
    -I.build_cache/autosar448_combined \
    $PY_INCLUDES \
    -o "invoke/python/build/emf_autosar${PY_SUFFIX}" \
    invoke/python/emf_autosar_pybind.cpp \
    invoke/common/emf_autosar_capi.cpp \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lwinpthread -lws2_32
```

产物为 `.pyd` 文件，例如 `emf_autosar.cp312-win_amd64.pyd`。

#### 4.2.4 使用

```bash
# 设置 PYTHONPATH 后即可导入
export PYTHONPATH=/workspace/invoke/python/build
python invoke/python/example.py
```

或在 Python 中：

```python
import sys
sys.path.insert(0, r"C:\path\to\workspace\invoke\python\build")
import emf_autosar
```

### 4.3 方案 B：MSVC + pybind11

适用于必须使用官方 CPython（python.org 发行版）的场景。

#### 4.3.1 安装

1. 安装 **Visual Studio Build Tools**（含 C++ 桌面开发工作负载），或完整版 Visual Studio。
2. 从 https://www.python.org/ 安装 Windows 版 Python（注意位数需与目标一致，通常 64 位）。
3. 安装 pybind11：

```powershell
py -m pip install pybind11 cmake
```

#### 4.3.2 构建 EMF 库与模型代码（MSVC）

MSVC 无法直接使用 MinGW 编译的 `.a` / `.o`，需用 MSVC 重新编译全部 EMF 库与模型代码：

```powershell
cd C:\path\to\workspace\cpp\emf-cpp
cmake -B build -G "Visual Studio 17 2022" -A x64 ^
      -DEMF_BUILD_TESTS=OFF -DEMF_BUILD_EXAMPLES=OFF ^
      -DCMAKE_POSITION_INDEPENDENT_CODE=ON
cmake --build build --config Release -j
```

模型代码（约 4206 个 `.cpp`）也需用 `cl.exe` 编译为 `.obj`，建议编写脚本遍历编译。

#### 4.3.3 构建扩展模块

在 **Developer Command Prompt for VS** 中：

```powershell
cd C:\path\to\workspace

for /f "delims=" %i in ('py -m pybind11 --includes') do set PY_INCLUDES=%i
for /f "delims=" %i in ('py -c "import sysconfig; print(sysconfig.get_config_var('EXT_SUFFIX'))"') do set PY_SUFFIX=%i

mkdir invoke\python\build

cl /nologo /std:c++17 /O2 /EHsc /LD ^
   /I invoke\common ^
   /I cpp\emf-cpp\emf-common\include ^
   /I cpp\emf-cpp\emf-ecore\include ^
   /I cpp\emf-cpp\emf-ecore-util\include ^
   /I cpp\emf-cpp\emf-xmi\include ^
   /I cpp\emf-cpp\emf-sphinx\include ^
   /I cpp\emf-cpp\emf-validation\include ^
   /I cpp\emf-cpp\emf-compare\include ^
   /I cpp\emf-cpp\emf-artop\emf-artop-runtime\include ^
   /I cpp\emf-cpp\emf-ecore-codegen\include ^
   /I cpp\emf-cpp\emf-xsd\include ^
   /I cpp\emf-cpp\emf-edit\include ^
   /I .build_cache\autosar448_combined ^
   %PY_INCLUDES% ^
   invoke\python\emf_autosar_pybind.cpp ^
   invoke\common\emf_autosar_capi.cpp ^
   <所有模型 .obj 文件> ^
   /link /LIBPATH:cpp\emf-cpp\build\Release ^
   libemf_common.lib libemf_ecore.lib libemf_ecore_util.lib ^
   libemf_xmi.lib libemf_xsd.lib libemf_sphinx.lib ^
   libemf_compare.lib libemf_validation.lib libemf_ecore_codegen.lib ^
   libemf_edit.lib libemf_artop_runtime.lib ^
   /OUT:invoke\python\build\emf_autosar%PY_SUFFIX%
```

> 由于 MSVC 命令行长度限制，模型 `.obj` 文件较多时建议先打包成静态库再链接，或使用 CMake 管理。

### 4.4 方案 C：Linux 交叉编译（高级）

理论上可用 `x86_64-w64-mingw32-g++` 交叉编译 `.pyd`，但**必须保证生成的模块与目标 Windows Python 的 ABI 完全一致**（Python 版本、位数、调试/发布构建均需匹配）。

#### 4.4.1 准备 Windows Python 头文件

从目标 Windows 机器拷贝 Python 安装目录（含 `include/` 与 `libs/`），例如 `C:\Python312\`：

```bash
# 假设已拷贝到 /opt/win-python312/
WIN_PY=/opt/win-python312
```

#### 4.4.2 交叉编译

```bash
cd /workspace

MINGW_LIBS=/workspace/.build_cache/mingw_build/libs
MINGW_MODEL_OBJ=/workspace/.build_cache/mingw_build/model_obj

# Python 版本号需与目标 Windows Python 一致，例如 3.12
PY_VER=312
PY_SUFFIX=".cp${PY_VER}-win_amd64.pyd"

mkdir -p invoke/python/build

x86_64-w64-mingw32-g++ -std=c++17 -O2 -w -shared \
    -Iinvoke/common \
    -Icpp/emf-cpp/emf-common/include \
    -Icpp/emf-cpp/emf-ecore/include \
    -Icpp/emf-cpp/emf-ecore-util/include \
    -Icpp/emf-cpp/emf-xmi/include \
    -Icpp/emf-cpp/emf-sphinx/include \
    -Icpp/emf-cpp/emf-validation/include \
    -Icpp/emf-cpp/emf-compare/include \
    -Icpp/emf-cpp/emf-artop/emf-artop-runtime/include \
    -Icpp/emf-cpp/emf-ecore-codegen/include \
    -Icpp/emf-cpp/emf-xsd/include \
    -Icpp/emf-cpp/emf-edit/include \
    -I.build_cache/autosar448_combined \
    -I${WIN_PY}/include \
    -DPYBIND11_COMPILER_CPLUSPLUS=201703L \
    -o "invoke/python/build/emf_autosar${PY_SUFFIX}" \
    invoke/python/emf_autosar_pybind.cpp \
    invoke/common/emf_autosar_capi.cpp \
    $(find ${MINGW_MODEL_OBJ} -name '*.o' | sort | tr '\n' ' ') \
    -Wl,--start-group $(find ${MINGW_LIBS} -name '*.a' | sort | tr '\n' ' ') -Wl,--end-group \
    -L${WIN_PY}/libs -lpython${PY_VER} \
    -lwinpthread -lws2_32 -static-libgcc -static-libstdc++
```

> **风险提示**：交叉编译 Python 扩展极易因 ABI 不匹配导致 `ImportError: DLL load failed`。若非必要，优先使用方案 A 或 B。

---

## 5. 在 Windows 上构建服务端

AUTOSAR 编辑器服务端（`autosar_server`）是一个基于 `cpp-httplib` 的 HTTP 服务，附带前端静态文件。

### 5.1 方式 A：Linux 交叉编译（推荐）

使用 `invoke/server/build_server_win.sh`，与 `build_mingw.sh` 共用 `.build_cache/mingw_build/` 输出目录，避免重复编译 EMF 库与模型代码。

#### 5.1.1 前置准备

确保已下载 `httplib.h` 与 `nlohmann/json.hpp`（见 [2.3](#23-服务端额外依赖)）。

#### 5.1.2 构建命令

```bash
cd /workspace

# 一次性构建全部（libs + model + server）
bash invoke/server/build_server_win.sh all
```

分步执行：

```bash
bash invoke/server/build_server_win.sh libs     # 交叉编译 EMF 库
bash invoke/server/build_server_win.sh model    # 交叉编译模型代码
bash invoke/server/build_server_win.sh server   # 链接 autosar_server.exe
```

`server` 步骤会额外编译 `autosar448_enum_literals.gen.cpp`，并链接：

```bash
x86_64-w64-mingw32-g++ -std=c++17 -O2 -w $COMMON_INCLUDES \
    -o invoke/server/build/autosar_server.exe \
    invoke/server/autosar_server.cpp \
    invoke/common/emf_autosar_capi.cpp \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lwinpthread -lws2_32 -static-libgcc -static-libstdc++
```

同时将前端文件拷贝到 `invoke/server/build/frontend/`。

#### 5.1.3 产物

| 产物 | 路径 |
| --- | --- |
| 服务端可执行文件 | `invoke/server/build/autosar_server.exe` |
| 前端静态文件 | `invoke/server/build/frontend/` |

#### 5.1.4 部署与运行

1. 将整个 `invoke/server/build/` 目录拷贝到 Windows 机器。
2. 运行：

```cmd
autosar_server.exe 3000
```

3. 浏览器访问 `http://localhost:3000/index.html`。

### 5.2 方式 B：CMake 构建（原生 / 交叉）

服务端提供 `invoke/server/CMakeLists.txt`，支持原生 Linux 构建与交叉编译。

#### 5.2.1 原生 Linux 构建

```bash
cd /workspace/invoke/server
cmake -B build_cmake
cmake --build build_cmake -j
```

#### 5.2.2 交叉编译到 Windows

先确保 EMF 库已交叉编译完成（`bash invoke/build_mingw.sh libs`），然后：

```bash
cd /workspace/invoke/server

# 使用 build_mingw.sh 生成的 toolchain.cmake
cmake -B build_win \
      -DCMAKE_TOOLCHAIN_FILE=/workspace/.build_cache/mingw_build/toolchain.cmake \
      -DEMF_LIBS_DIR=/workspace/.build_cache/mingw_build/libs
cmake --build build_win -j
```

CMake 会自动处理 Windows 特定链接（`winpthread`、`ws2_32`、`-static-libgcc -static-libstdc++`、`.exe` 后缀），并复制前端文件到构建目录。

#### 5.2.3 CMake 关键变量

| 变量 | 说明 |
| --- | --- |
| `EMF_CPP_DIR` | EMF C++ 源码根目录（默认 `../../cpp/emf-cpp`） |
| `MODEL_GEN_DIR` | 模型源码目录（默认 `$WORKSPACE/.build_cache/autosar448_combined`） |
| `EMF_LIBS_DIR` | EMF 预构建库目录（原生：`emf-cpp/build`；交叉：`mingw_build/libs`） |
| `AUTOSAR_SERVER_BUILD_FRONTEND` | 是否复制前端文件（默认 `ON`） |

---

## 6. 故障排查

### 6.1 工具链问题

| 现象 | 原因与解决 |
| --- | --- |
| `x86_64-w64-mingw32-g++: command not found` | llvm-mingw 未加入 PATH。执行 `export PATH=$PWD/llvm-mingw-*/bin:$PATH` |
| `ERROR: $CXX 未找到`（脚本报错） | 同上，或通过 `MINGW_PREFIX` 指定正确前缀 |
| 链接报 `undefined reference to pthread_*` | 未链接 `winpthread`。确认链接选项含 `-lwinpthread -lws2_32` |
| 目标 Windows 机器提示缺少 `libgcc_s_seh-1.dll` / `libstdc++-6.dll` | 未静态链接运行时。添加 `-static-libgcc -static-libstdc++` |

### 6.2 CMake 交叉编译问题

| 现象 | 原因与解决 |
| --- | --- |
| `EMF libraries not found in ...` | `EMF_LIBS_DIR` 路径错误，或未先运行 `build_mingw.sh libs`。检查 `.build_cache/mingw_build/libs/` 下是否有 11 个 `.a` |
| CMake 仍使用系统 `g++` 而非 mingw | 未指定 `CMAKE_TOOLCHAIN_FILE`，或工具链文件中编译器路径不正确 |
| `httplib.h not found` / `nlohmann/json.hpp not found` | 未下载依赖头文件，见 [2.3](#23-服务端额外依赖) |

### 6.3 模型代码编译问题

| 现象 | 原因与解决 |
| --- | --- |
| `ERROR: 模型源码目录不存在` | 模型代码未生成。先运行 `bash cpp/emf-cpp/regenerate_model.sh` |
| `Compiled: X / N` 中 X < N | 部分文件编译失败。脚本以 `-w` 抑制警告、`2>/dev/null` 隐藏错误，可手动对失败文件去掉重定向查看详细错误 |
| 编译耗时过长 | 增大 `JOBS`，例如 `JOBS=$(nproc) bash invoke/build_mingw.sh model` |
| 命令行过长（链接阶段） | 模型 `.o` 文件过多。可先用 `ar` 打包成静态库再链接，或使用 CMake（其将模型代码编译为 `autosar_model` 静态库） |

### 6.4 Python 绑定问题

| 现象 | 原因与解决 |
| --- | --- |
| `ImportError: DLL load failed while importing emf_autosar` | ABI 不匹配。确保编译器（MinGW/MSVC）、Python 版本、位数均与运行时一致 |
| `ModuleNotFoundError: No module named 'emf_autosar'` | 未设置 `PYTHONPATH`。执行 `export PYTHONPATH=/path/to/python/build` |
| `undefined symbol: emf_resource_load_file` | 未链接 `emf_autosar_capi.cpp` 或 EMF 库缺失。检查链接命令 |
| MSYS2 中 `python-config` 不存在 | 改用 `python3-config`，或通过 `python -c "import sysconfig; print(sysconfig.get_config_var('EXT_SUFFIX'))"` 获取后缀 |
| 交叉编译的 `.pyd` 在 Windows 报 ABI 错误 | 目标 Python 版本/构建与编译时不一致。优先改用方案 A（MSYS2）或 B（MSVC） |

### 6.5 服务端运行问题

| 现象 | 原因与解决 |
| --- | --- |
| `autosar_server.exe` 启动后无响应 | 端口被占用，换一个端口：`autosar_server.exe 8080` |
| 浏览器访问返回 404 | 前端文件未拷贝。确保 `frontend/` 目录与 `.exe` 同级 |
| 启动报缺少 `ws2_32.dll` / `winpthread` | 极少见，通常 Windows 自带 `ws2_32.dll`；`winpthread` 已静态链接。检查是否漏掉 `-static-libgcc -static-libstdc++` |

### 6.6 通用调试技巧

1. **查看产物类型**：构建后用 `file` 命令确认目标格式：

   ```bash
   file invoke/cpp/build/cpp_consumer_example.exe
   # 期望输出含: PE32+ executable (console) x86-64, for MS Windows
   ```

2. **检查依赖**（在 Windows 上）：

   ```cmd
   dumpbin /dependents autosar_server.exe
   ```

   静态链接成功时，依赖列表中不应出现 `libgcc_s_*` / `libstdc++-*` / `libwinpthread-*`。

3. **增量构建**：`build_server_win.sh` 的 `model` 步骤支持增量编译（`.o` 比 `.cpp` 新则跳过），重复构建会更快。

4. **清理重建**：

   ```bash
   rm -rf /workspace/.build_cache/mingw_build
   bash invoke/build_mingw.sh all
   ```

---

## 7. 性能优化构建（PGO + jemalloc）

XMI 反序列化是核心热路径。默认 Release 构建已可用，但对加载大文件
（几十 MB 以上的 .ecore / .arxml）有显著优化空间。下面两项优化互不冲突，
可叠加使用，100M 文件加载从 5906ms 降到 2048ms（2.9x）。详见
[ARCHITECTURE.md 第 7 节](./ARCHITECTURE.md#7-性能优化xmi-反序列化热路径)。

### 7.1 PGO（Profile-Guided Optimization）编译

PGO 用运行时 profile 指导编译器优化热点分支与内联，对 EObject 构建、
feature 查找等反射密集代码特别有效。

```bash
cd /workspace/cpp/emf-cpp

# 阶段 1：instrument 编译（插桩）
mkdir -p build-pgo && cd build-pgo
cmake .. -DCMAKE_BUILD_TYPE=Release \
         -DCMAKE_CXX_FLAGS="-fprofile-generate=/tmp/pgo-data" \
         -DCMAKE_C_FLAGS="-fprofile-generate=/tmp/pgo-data"
cmake --build . -j$(nproc)

# 阶段 2：跑代表性 workload 收集 profile
#   用真实业务场景的 ecore/arxml 文件效果最好
./emf_xmi_tests                    # 回归测试（覆盖各种解析路径）
./emf_benchmark <代表文件> 5       # benchmark 跑 5 轮

# 阶段 3：recompile 用 profile 指导优化
cd /workspace/cpp/emf-cpp/build-pgo
cmake .. -DCMAKE_BUILD_TYPE=Release \
         -DCMAKE_CXX_FLAGS="-fprofile-use=/tmp/pgo-data -fprofile-correction" \
         -DCMAKE_C_FLAGS="-fprofile-use=/tmp/pgo-data -fprofile-correction"
cmake --build . -j$(nproc)

# 清理 profile 数据（可选）
rm -rf /tmp/pgo-data
```

**效果**：100M 文件加载 3256ms → 2389ms（提速 27%）。

**注意**：
- instrument 阶段的二进制比正常慢 2-3x，仅用于收集 profile
- profile 数据与代码路径绑定，代码改动后需重新收集
- Windows 交叉编译同样适用，工具链 flags 一致

### 7.2 jemalloc 替换 glibc malloc

jemalloc 的 thread cache + size class bin 接近 Java GC 的 bump-pointer 语义，
对大量小对象（EObject、字符串）分配场景有明显优势。

#### 7.2.1 Linux 部署（最简单）

无需改代码，运行时 `LD_PRELOAD` 注入即可：

```bash
# 方式 A：运行时注入（最快验证）
LD_PRELOAD=/path/to/libjemalloc.so.2 ./your_program

# 方式 B：编译期链接（推荐生产）
# CMake 中添加：
#   target_link_libraries(your_target PRIVATE jemalloc)
# 或直接 g++：
#   g++ ... -ljemalloc
```

#### 7.2.2 Windows 部署（交叉编译 jemalloc.dll）

```bash
# 在 Linux 上交叉编译 jemalloc for Windows
cd /tmp
wget https://github.com/jemalloc/jemalloc/releases/download/5.3.0/jemalloc-5.3.0.tar.bz2
tar xjf jemalloc-5.3.0.tar.bz2 && cd jemalloc-5.3.0
./autogen.sh
./configure --host=x86_64-w64-mingw32 \
            --prefix=/opt/jemalloc-win \
            --disable-doc --disable-stats
make -j$(nproc)
make install
# 产出：/opt/jemalloc-win/bin/jemalloc.dll + lib/libjemalloc.dll.a

# 链接到 Windows exe
# CMake:
#   target_link_libraries(your_target PRIVATE /opt/jemalloc-win/lib/libjemalloc.dll.a)
# 部署：把 jemalloc.dll 与 .exe 放同一目录
```

**注意**：
- Windows 路径必须用 MinGW 工具链（`--host=x86_64-w64-mingw32`），
  不能用 MSVC 的 `.sln`（那是给 cl.exe 用的）
- jemalloc 5.x 自 2012 年起官方支持 MinGW Windows，Mozilla 维护

#### 7.2.3 效果

100M 文件加载，3 轮中位数：

| 配置 | load (ms) | vs Java (1647ms) |
|---|---|---|
| C++ PGO（glibc malloc） | 2378 | 1.44x |
| **C++ PGO + jemalloc** | **2048** | **1.24x** |

jemalloc 带来约 14% 提速，把 C++ 与 Java 的差距从 44% 缩到 24%。

### 7.3 benchmark 工具

```bash
# 编译 benchmark
cd /workspace/cpp/emf-cpp/build-pgo
cmake --build . --target emf_benchmark

# 生成大测试文件（用 ecore 元模型，两端无需额外注册 package）
./emf_benchmark_genlarge <output.xmi> <targetSizeMB>
# 例：./emf_benchmark_genlarge large100.xmi 100

# 跑 benchmark（load + save 计时，CSV 输出）
./emf_benchmark large100.xmi 5
# 输出：cpp,large100.xmi,5,104857956,104857960,2048.123,2048.456

# 对比 Java（需先构建 emf-all.jar）
cd /tmp/emf-bench
java -cp emf-all.jar EmfBenchmark large100.xmi 5
```

### 7.4 何时启用

| 场景 | 建议 |
|---|---|
| 开发调试 | 默认 Release 即可，PGO 编译慢不值得 |
| 单元测试 | 默认 Release |
| **生产部署（加载大模型）** | **PGO + jemalloc 全开** |
| 加载小文件（<1MB） | 默认 Release 已比 Java 快 15-45x |

---

## 附录：快速构建命令速查

```bash
# ===== C++ 交叉编译（Linux -> Windows）=====
cd /workspace
bash invoke/build_mingw.sh all
# 产物: invoke/cpp/build/cpp_consumer_example.exe

# ===== 服务端交叉编译 =====
bash invoke/server/build_server_win.sh all
# 产物: invoke/server/build/autosar_server.exe + frontend/

# ===== 服务端 CMake 交叉编译 =====
cd invoke/server
cmake -B build_win \
      -DCMAKE_TOOLCHAIN_FILE=/workspace/.build_cache/mingw_build/toolchain.cmake \
      -DEMF_LIBS_DIR=/workspace/.build_cache/mingw_build/libs
cmake --build build_win -j

# ===== Python 绑定（MSYS2 原生）=====
# 在 MSYS2 UCRT64 终端中：
cd /workspace
g++ -std=c++17 -O2 -w -shared -fPIC \
    $(python -m pybind11 --includes) \
    <COMMON_INCLUDES> \
    -o invoke/python/build/emf_autosar$(python-config --extension-suffix) \
    invoke/python/emf_autosar_pybind.cpp \
    invoke/common/emf_autosar_capi.cpp \
    $MODEL_OBJS \
    -Wl,--start-group $EMF_LIBS -Wl,--end-group \
    -lwinpthread -lws2_32
# 使用: PYTHONPATH=invoke/python/build python invoke/python/example.py
```
