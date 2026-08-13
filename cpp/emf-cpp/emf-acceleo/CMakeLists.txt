# emf::acceleo —— 模型到代码生成（M2T 模板引擎，Acceleo 子集）
# 对齐 Java: org.eclipse.acceleo
#
# 实现：
#   - .mtl 模板解析器（手写递归下降）
#   - Module/Template/Block AST
#   - AQL 子集求值器（基于 emf::ecore EObject 反射）
#   - AcceleoEngine / AcceleoService
#   - 支持 [template]/[for]/[if]/[let]/[file]/[protected] 块
#   - 支持 C++ 服务注册（对齐 Java services）
add_library(emf_acceleo
    src/AcceleoParser.cpp
    src/AcceleoEngine.cpp
)
target_include_directories(emf_acceleo
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_acceleo PUBLIC emf_common emf_ecore)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_acceleo_tests
        tests/test_main.cpp
        tests/AcceleoTests.cpp
        tests/AlignmentTests.cpp
    )
    target_link_libraries(emf_acceleo_tests PRIVATE emf_acceleo emf_xcore)
    add_test(NAME emf_acceleo_tests COMMAND emf_acceleo_tests)
endif()
