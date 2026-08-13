# emf::validation —— 模型约束校验
# 对齐 Java: org.eclipse.emf.validation（并入 emfservices 后）

add_library(emf_validation
    src/EValidator.cpp
    src/ConstraintParser.cpp
    src/ConstraintDescriptor.cpp
    src/Diagnostician.cpp
    src/LiveValidator.cpp
    src/ValidationService.cpp
    src/AnnotationConstraintLoader.cpp
    src/AutosarConstraints.cpp
)

target_include_directories(emf_validation
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_validation PUBLIC emf_common emf_ecore emf_ecore_util)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_validation_tests
        tests/test_main.cpp
        tests/ConstraintParserTests.cpp
        tests/EValidatorTests.cpp
        tests/ValidationServiceTests.cpp
        tests/ConstraintDescriptorTests.cpp
        tests/LiveValidatorTests.cpp
        tests/ValidationE2ETests.cpp
        tests/AnnotationConstraintLoaderTests.cpp
        tests/AutosarConstraintsTests.cpp
    )
    target_link_libraries(emf_validation_tests PRIVATE emf_validation emf_xmi)
    add_test(NAME emf_validation_tests COMMAND emf_validation_tests)
endif()
