# emf::artop::validation —— AUTOSAR 业务约束（ARTOP 特化层）
# 对齐 Java: org.artop.aal.validation（约束实现，运行于 emf-validation 底座之上）
#
# 解耦说明：AUTOSAR 约束从通用底座 emf-validation 迁出到此。
# emf-validation 保持与 AUTOSAR 无关；本模块反向依赖 emf-validation 的
# EValidator/Constraint 基础设施，由 artop 调用方显式注册约束。

add_library(emf_artop_validation
    src/AutosarConstraints.cpp
)

target_include_directories(emf_artop_validation
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_artop_validation PUBLIC emf_validation emf_common emf_ecore)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_artop_validation_tests
        tests/test_main.cpp
        tests/AutosarConstraintsTests.cpp
    )
    target_link_libraries(emf_artop_validation_tests PRIVATE emf_artop_validation emf_xmi)
    add_test(NAME emf_artop_validation_tests COMMAND emf_artop_validation_tests)
endif()