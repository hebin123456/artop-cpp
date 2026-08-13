# emf::ecore_codegen —— Ecore → C++17 代码生成器
# 对齐 Java: org.eclipse.emf.codegen.ecore（JET 模板 + GenModel 二次模型）
#
# 走 GenModel 路线（与 Java 一致）：
#   1. GenModelLoader: 从 .genmodel XMI 加载 → GenModel 数据结构
#   2. CppTemplates:  JET 模板的 C++ 实现（字符串 + {{placeholder}} 替换）
#   3. GenModelGenerator: 顶层调度器（每个 GenPackage 生成 Package/Factory/Switch/...）
#   4. 旧 emitter（PackageEmitter 等）作为 "模板子例程" 仍保留，
#      可被 GenModelGenerator 通过 .h 文件引用，也可单独使用。

add_library(emf_ecore_codegen
    # 新增：EAnnotation 读取工具（模型驱动核心）
    src/EAnnotationReader.cpp
    # 新增：GenModel 路线
    src/GenModel.cpp
    src/GenModelLoader.cpp
    src/CppTemplates.cpp
    src/GenModelGenerator.cpp
    # 旧 emitter（保留）—— 使用新的单类单继承 EClassEmitter
    src/CppGenerator.cpp
    src/IndentedWriter.cpp
    src/TypeMapper.cpp
    src/PackageEmitter.cpp
    src/FactoryEmitter.cpp
    src/EClassEmitter.cpp
    src/SwitchEmitter.cpp
    src/AdapterFactoryEmitter.cpp
    src/ValidatorEmitter.cpp
)

target_include_directories(emf_ecore_codegen
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_ecore_codegen
    PUBLIC emf_common emf_ecore
    PRIVATE emf_xmi
)

# 单元测试
add_executable(emf_ecore_codegen_tests
    tests/test_main.cpp
    tests/EmitterTests.cpp
    tests/CppGeneratorTests.cpp
    # 新增：GenModel 路线测试
    tests/GenModelLoaderTests.cpp
    tests/CppTemplatesTests.cpp
    tests/GenModelGeneratorTests.cpp
    tests/P4_JetTemplateTests.cpp
)
target_include_directories(emf_ecore_codegen_tests
    PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/tests
)
target_link_libraries(emf_ecore_codegen_tests
    PRIVATE emf_ecore_codegen emf_xmi
)
# 把 build/source 根目录注入编译期，方便测试拼 g++ 命令
target_compile_definitions(emf_ecore_codegen_tests PRIVATE
    EMF_CODEGEN_TEST_OUTPUT_DIR="${CMAKE_BINARY_DIR}/emf-ecore-codegen-tests"
    EMF_BUILD_DIR="${CMAKE_BINARY_DIR}"
    EMFCPP_SOURCE_DIR="${CMAKE_SOURCE_DIR}"
)
set_target_properties(emf_ecore_codegen_tests PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
)
add_test(NAME emf_ecore_codegen_tests COMMAND emf_ecore_codegen_tests
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tests)
