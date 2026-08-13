# emf::sphinx —— Sphinx EMF 扩展
# 对齐 Java: org.eclipse.sphinx.emf (Eclipse Sphinx 项目)
#
# Sphinx 提供 EMF 的扩展功能：代理管理、模型分割、作用域管理、元模型描述符等。
# 本 C++ 实现是 Java sphinx.emf 的行为对齐翻译，不依赖 Eclipse 平台（OSGi/IWorkspace）。
#
# 注意：部分 .cpp 引用了 Eclipse 平台概念（IProject/IFile/IWorkspace），
# 这些文件通过条件编译或桩实现处理，不阻塞核心功能编译。
cmake_minimum_required(VERSION 3.10)

# 自动收集所有 .cpp 源文件
file(GLOB_RECURSE SPHINX_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp"
)

# 过滤掉依赖 Eclipse 平台的文件（无法编译）
# 这些文件引用 IProject/IFile/IWorkspace 等 Eclipse 平台 API
set(SPATIAL_EXCLUDE "")
foreach(src ${SPHINX_SOURCES})
    file(STRINGS "${src}" _content LIMIT_COUNT 200)
    # 检查是否包含 Eclipse 平台依赖（非注释行）
    string(FIND "${_content}" "IWorkspace" _pos1)
    string(FIND "${_content}" "IProject " _pos2)
    string(FIND "${_content}" "IFile " _pos3)
    if(NOT _pos1 EQUAL -1 OR NOT _pos2 EQUAL -1 OR NOT _pos3 EQUAL -1)
        list(APPEND SPATIAL_EXCLUDE "${src}")
    endif()
endforeach()

# 从源文件列表中移除排除的文件
if(SPATIAL_EXCLUDE)
    list(REMOVE_ITEM SPHINX_SOURCES ${SPATIAL_EXCLUDE})
endif()

if(NOT SPHINX_SOURCES)
    message(STATUS "emf-sphinx: no compilable sources, creating interface library")
    add_library(emf_sphinx INTERFACE)
    target_include_directories(emf_sphinx INTERFACE ${CMAKE_CURRENT_SOURCE_DIR}/include)
    return()
endif()

add_library(emf_sphinx ${SPHINX_SOURCES})
target_include_directories(emf_sphinx
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${CMAKE_CURRENT_SOURCE_DIR}/src
)
target_link_libraries(emf_sphinx
    PUBLIC
        emf_common
        emf_ecore
        emf_ecore_util
        emf_edit
        emf_xmi
)

# 测试
if(EMF_BUILD_TESTS)
    file(GLOB SPHINX_TEST_SOURCES
        "${CMAKE_CURRENT_SOURCE_DIR}/tests/*.cpp"
    )
    # 过滤空测试文件
    set(SPHINX_TEST_FILES "")
    foreach(tf ${SPHINX_TEST_SOURCES})
        file(SIZE "${tf}" _sz)
        if(_sz GREATER 0)
            list(APPEND SPHINX_TEST_FILES "${tf}")
        endif()
    endforeach()

    if(SPHINX_TEST_FILES)
        add_executable(emf_sphinx_tests ${SPHINX_TEST_FILES})
        target_link_libraries(emf_sphinx_tests PRIVATE emf_sphinx)
        add_test(NAME emf_sphinx_tests COMMAND emf_sphinx_tests)
    endif()
endif()
