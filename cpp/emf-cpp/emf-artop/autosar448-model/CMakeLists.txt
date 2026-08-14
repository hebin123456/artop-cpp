# autosar448-model —— 由 emf_artop_codegen 从 autosar448.ecore 生成的 ARTOP 静态模型代码工程
#
# 设计：
#   - 本目录由 CI 在构建期运行 emf_artop_codegen 生成（autosar40/ 子树）。
#   - 与底座 (emf-cpp) 放在一起：源码目录位于 cpp/emf-cpp/emf-artop/autosar448-model/，
#     通过相对路径引用底座各模块的 include 目录。
#   - 编译所有生成的 .cpp 成静态库 autosar448_model，链接底座库。
#     若编译通过即表示“工程无报错”。
#
# 目录约定：
#   <this dir>/autosar40/...        生成的包/类源码（include 根为本目录）
#   <this dir>/../../<module>/include  底座各模块头文件
cmake_minimum_required(VERSION 3.16)
project(autosar448_model LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

# 底座 (emf-cpp) 源码根：本目录为 <emf-cpp>/emf-artop/autosar448-model
get_filename_component(EMF_CPP_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../.." ABSOLUTE)

# 收集所有生成的源文件（代码gen 输出 autosar40/ 子树）
file(GLOB_RECURSE MODEL_SOURCES CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_SOURCE_DIR}/autosar40/*.cpp")
file(GLOB_RECURSE MODEL_HEADERS CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_SOURCE_DIR}/autosar40/*.h")

if(NOT MODEL_SOURCES)
    message(WARNING
        "autosar448-model: no generated sources found under autosar40/. "
        "Run emf_artop_codegen <ecore> ${CMAKE_CURRENT_SOURCE_DIR} first.")
    return()
endif()

list(LENGTH MODEL_SOURCES _n)
message(STATUS "autosar448-model: compiling ${_n} generated source files")

add_library(autosar448_model STATIC ${MODEL_SOURCES} ${MODEL_HEADERS})
target_include_directories(autosar448_model PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}                                      # 解析 autosar40/... 跨包 include
    ${EMF_CPP_SOURCE_DIR}/emf-common/include
    ${EMF_CPP_SOURCE_DIR}/emf-ecore/include
    ${EMF_CPP_SOURCE_DIR}/emf-ecore-util/include
    ${EMF_CPP_SOURCE_DIR}/emf-xmi/include
    ${EMF_CPP_SOURCE_DIR}/emf-xsd/include
    ${EMF_CPP_SOURCE_DIR}/emf-compare/include
    ${EMF_CPP_SOURCE_DIR}/emf-validation/include
    ${EMF_CPP_SOURCE_DIR}/emf-sphinx/include
    ${EMF_CPP_SOURCE_DIR}/emf-ecore-codegen/include
    ${EMF_CPP_SOURCE_DIR}/emf-artop/emf-artop-runtime/include
)

# 链接底座库（若在同一 CMake 构建树中被 add_subdirectory 引入，则目标已存在；
# 否则作为独立工程构建时，通过 EMF_CPP_BUILD_DIR 指向已构建的底座 build 目录）
if(TARGET emf_common)
    target_link_libraries(autosar448_model PUBLIC
        emf_common emf_ecore emf_ecore_util emf_xmi emf_xsd
        emf_compare emf_validation emf_sphinx emf_ecore_codegen
        emf_artop_runtime)
else()
    if(EMF_CPP_BUILD_DIR)
        target_link_directories(autosar448_model PRIVATE
            ${EMF_CPP_BUILD_DIR}/emf-common
            ${EMF_CPP_BUILD_DIR}/emf-ecore
            ${EMF_CPP_BUILD_DIR}/emf-ecore-util
            ${EMF_CPP_BUILD_DIR}/emf-xmi
            ${EMF_CPP_BUILD_DIR}/emf-xsd
            ${EMF_CPP_BUILD_DIR}/emf-compare
            ${EMF_CPP_BUILD_DIR}/emf-validation
            ${EMF_CPP_BUILD_DIR}/emf-sphinx
            ${EMF_CPP_BUILD_DIR}/emf-ecore-codegen
            ${EMF_CPP_BUILD_DIR}/emf-artop/emf-artop-runtime)
        target_link_libraries(autosar448_model PUBLIC
            emf_common emf_ecore emf_ecore_util emf_xmi emf_xsd
            emf_compare emf_validation emf_sphinx emf_ecore_codegen
            emf_artop_runtime)
    endif()
endif()
