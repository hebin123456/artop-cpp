# emf::xcore —— Ecore 文本建模 DSL（Xcore 子集）
# 对齐 Java: org.eclipse.emf.ecore.xcore
#
# 实现：
#   - .xcore 解析器（手写递归下降）
#   - XPackage AST
#   - XcoreGenerator（AST → EPackage/EClass/EAttribute/EReference/EOperation/EEnum）
#   - XcoreResource / XcoreResourceFactory / XcoreStandaloneSetup
add_library(emf_xcore
    src/XcoreParser.cpp
    src/XcoreGenerator.cpp
    src/XcoreResource.cpp
)
target_include_directories(emf_xcore
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_xcore PUBLIC emf_common emf_ecore)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_xcore_tests
        tests/test_main.cpp
        tests/XcoreTests.cpp
    )
    target_link_libraries(emf_xcore_tests PRIVATE emf_xcore)
    add_test(NAME emf_xcore_tests COMMAND emf_xcore_tests)
endif()
