# emf::compare —— 模型差异/匹配/合并
# 对齐 Java: org.eclipse.emf.compare

add_library(emf_compare
    src/Comparison.cpp
    src/MatchEngine.cpp
    src/DiffEngine.cpp
    src/MergeEngine.cpp
    src/RequirementEngine.cpp
    src/EquivalenceEngine.cpp
    src/ConflictDetector.cpp
    src/DiffFilter.cpp
)

target_include_directories(emf_compare
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_compare PUBLIC emf_common emf_ecore emf_ecore_util)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_compare_tests
        tests/test_main.cpp
        tests/MatchEngineTests.cpp
        tests/DiffEngineTests.cpp
        tests/MergeEngineTests.cpp
        tests/ComparisonTests.cpp
        tests/CompareE2ETests.cpp
        tests/CompareP0RegressionTests.cpp
    )
    target_link_libraries(emf_compare_tests PRIVATE emf_compare emf_xmi)
    add_test(NAME emf_compare_tests COMMAND emf_compare_tests)
endif()
