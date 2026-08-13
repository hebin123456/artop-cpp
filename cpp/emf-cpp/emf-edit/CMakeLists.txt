# emf::edit —— EMF 编辑框架
# 对齐 Java: org.eclipse.emf.edit
#
# 提供 EditingDomain、Command 体系、ItemProvider 等编辑支持。
# 注意：部分 Command 实现文件为空，仅编译非空文件。
cmake_minimum_required(VERSION 3.10)

# 自动收集所有 .cpp 源文件
file(GLOB EDIT_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp"
)

# 过滤空文件（size == 0）
set(EDIT_NONEMPTY_SOURCES "")
foreach(src ${EDIT_SOURCES})
    file(SIZE "${src}" _sz)
    if(_sz GREATER 0)
        list(APPEND EDIT_NONEMPTY_SOURCES "${src}")
    endif()
endforeach()

if(NOT EDIT_NONEMPTY_SOURCES)
    message(STATUS "emf-edit: no non-empty sources, creating interface library")
    add_library(emf_edit INTERFACE)
    target_include_directories(emf_edit INTERFACE ${CMAKE_CURRENT_SOURCE_DIR}/include)
    return()
endif()

add_library(emf_edit ${EDIT_NONEMPTY_SOURCES})
target_include_directories(emf_edit
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_edit
    PUBLIC
        emf_common
        emf_ecore
        emf_ecore_util
)

# 测试
if(EMF_BUILD_TESTS)
    file(GLOB EDIT_TEST_SOURCES
        "${CMAKE_CURRENT_SOURCE_DIR}/tests/*.cpp"
    )
    set(EDIT_TEST_FILES "")
    foreach(tf ${EDIT_TEST_SOURCES})
        file(SIZE "${tf}" _sz)
        if(_sz GREATER 0)
            list(APPEND EDIT_TEST_FILES "${tf}")
        endif()
    endforeach()

    if(EDIT_TEST_FILES)
        add_executable(emf_edit_tests ${EDIT_TEST_FILES})
        target_link_libraries(emf_edit_tests PRIVATE emf_edit)
        add_test(NAME emf_edit_tests COMMAND emf_edit_tests)
    endif()
endif()
