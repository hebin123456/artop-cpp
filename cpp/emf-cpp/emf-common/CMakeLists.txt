# emf::common —— EMF Common (EList, URI, Notifier/Adapter, EObject, Resource, Registry)
# 对齐 Java: org.eclipse.emf.common
#
# 注：历史遗留的 emf::common::notify:: 体系（对齐 Java BasicNotifierImpl 的完整接口层次）
# 已删除。生产代码统一使用 emf::common::Notifier / EAdapter（ENotifier.h），EObject 直接
# 继承 Notifier 基类，无 Impl 层。

# Helper: collect .cpp sources that actually exist on disk, to gracefully
# handle cases where a few files in this directory are temporarily missing.
function(emf_collect_cpps out_var)
    set(_list)
    foreach(_src IN LISTS ARGN)
        if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/${_src})
            list(APPEND _list ${_src})
        else()
            message(STATUS "Skipping missing source: ${_src}")
        endif()
    endforeach()
    set(${out_var} "${_list}" PARENT_SCOPE)
endfunction()

emf_collect_cpps(_COMMON_SRCS
    src/URI.cpp
    src/Resource.cpp
    src/EPackageRegistry.cpp
    src/Diagnostic.cpp
    src/ENotifier.cpp
    src/EObject.cpp
    src/FeatureMap.cpp
    src/URIConverter.cpp
    src/util/SegmentSequence.cpp
    src/util/BasicEList.cpp
    src/util/BasicEMap.cpp
    src/util/UniqueEList.cpp
    src/util/ArrayDelegatingEList.cpp
    src/util/DelegatingEList.cpp
    src/util/NotifyingList.cpp
    src/util/NotifyingListImpl.cpp
    src/command/AbstractCommand.cpp
    src/command/CompoundCommand.cpp
    src/command/UnexecutableCommand.cpp
    src/command/IdentityCommand.cpp
    src/command/CommandWrapper.cpp
    src/command/StrictCompoundCommand.cpp
    src/command/BasicCommandStack.cpp
)

add_library(emf_common ${_COMMON_SRCS})

target_include_directories(emf_common
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)

find_package(Threads REQUIRED)
target_link_libraries(emf_common PUBLIC Threads::Threads)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_common_tests
        tests/test_main.cpp
        tests/EListTests.cpp
        tests/URITests.cpp
        tests/ENotifierTests.cpp
        tests/ResourceTests.cpp
        tests/EPackageRegistryTests.cpp
        tests/CommandTests.cpp
        tests/BasicEMapTests.cpp
        tests/SegmentSequenceTests.cpp
        tests/NotifyingListTests.cpp
        tests/UniqueEListTests.cpp
    )
    target_include_directories(emf_common_tests PRIVATE ${CMAKE_SOURCE_DIR}/emf-ecore/include)
    target_include_directories(emf_common_tests PRIVATE ${CMAKE_SOURCE_DIR}/emf-ecore/include)
    target_include_directories(emf_common_tests PRIVATE ${CMAKE_SOURCE_DIR}/emf-ecore-util/include)
    target_link_libraries(emf_common_tests PRIVATE emf_common emf_ecore)
    add_test(NAME emf_common_tests COMMAND emf_common_tests)
endif()
