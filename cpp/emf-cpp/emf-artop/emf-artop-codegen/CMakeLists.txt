# emf::artop::codegen —— ARTOP 静态模型代码生成器
# 对齐 Java: org.artop.aal.gautosar  +  org.artop.aal.autosar40/448
#             + Acceleo 模板
#
# 作用：扩展 emf-ecore-codegen，把 autosar448.ecore / gautosar.ecore
#       生成 C++ 静态模型库（取代 Java codegen 输出的 .class）。
#
# 设计要点（逆向 .class 后总结）：
#   1. Mixin 注入：每个根 EObject 注入 g* accessor（gGetArPackages/gGetAdminData
#      等），把 ARPackage 转 GARPackage，把 AdminData 转 GAdminData。
#      Java 里这是 gautosar/ 子包的"g*" 接口，autosar40 的 Impl 继承
#      autosar40 类型同时实现 gautosar 类型。
#      C++ 里通过 mixin / 多重继承 emf_artop::gautosar::G<...> 表达。
#
#   2. Mixed / MixedOuterContent 字段：根对象（如 AUTOSARImpl）有
#      - FeatureMap mixed;
#      - FeatureMap mixedOuterContent;
#      - EMap<String, String> xSISchemaLocation;
#      - EList<EObject> containedExtensions;
#      - Map<String, Object> wrappedExtensions;
#      - EMap<String, EObject> allExtensions;
#      - Map<String, Object> getExtensions();
#      codegen 模板里这些字段对 root EClass 自动注入。
#
#   3. Resource + ResourceFactory：每个 generated model 都生成
#      Autosar448ResourceImpl    extends  AutosarXMLResource
#      Autosar448ResourceFactoryImpl extends AutosarXMLResourceFactory
#      内部维护多版本 AUTOSAR_4_x_y_RESOURCE_DESCRIPTOR（4.0.1 ~ 4.4.8 共 15 个）
#      通过 createSchemaLocationCatalog() 注册 xsi:schemaLocation namespace。
#
#   4. Util：生成 Autosar448Switch / Autosar448AdapterFactory / Autosar448Package /
#      Autosar448Factory / Autosar448ResourceImpl。Switch 的 doSwitch 包含
#      数百个 case（与 gautosar Switch 合并实现 740+ subpackages）。
#
#   5. Activator：生成 Activator extends EMFPlugin（Java 必需，C++ 跳过）。
#
#   6. Identifier 工具：Identifiable 接口的 eGet/eSet 走 sfIdentifier 通道。
#
# 输入（codegen 输入，逆向到 /workspace/decompiler/）：
#   - /workspace/decompiler/autosar448/model/gautosar.ecore       公共基类
#   - /workspace/decompiler/autosar448/model/autosar448.ecore     448 增量
#   - /workspace/decompiler/autosar448/model/autosar448.genmodel  生成配置
#   - /workspace/decompiler/autosar448/model/library/*.arxml      110+ 样例（用于 roundtrip 测试）
#
# 继承 emf-ecore-codegen 的 C++ Templates：
#   /workspace/emf-cpp/emf-ecore-codegen/src/CppTemplates.cpp
#   改写为 ARTOP 模板（追加 mixed/extensions/ResourceImpl 等）即可。

cmake_minimum_required(VERSION 3.15)
project(emf_artop_codegen LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# codegen 工具：独立可执行
# 输入：ecore/genmodel 路径
# 输出：emf-artop-codegen-output/<package>/（include/ + src/ + CMakeLists.txt）
# 当前：ArtopCppGenerator 继承 CppGenerator，添加 ARTOP 特有输出
add_executable(emf_artop_codegen
    src/main.cpp
    src/ArtopCppGenerator.cpp
)
target_link_libraries(emf_artop_codegen
    PRIVATE emf_common emf_ecore emf_xmi emf_ecore_codegen emf_artop_runtime
)
target_include_directories(emf_artop_codegen
    PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include
)

if(EMF_BUILD_TESTS)
    enable_testing()
    add_executable(emf_artop_codegen_tests
        tests/test_main.cpp
        src/ArtopCppGenerator.cpp
    )
    target_link_libraries(emf_artop_codegen_tests
        PRIVATE emf_common emf_ecore emf_xmi emf_ecore_codegen emf_artop_runtime
    )
    target_include_directories(emf_artop_codegen_tests
        PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include
                ${CMAKE_CURRENT_SOURCE_DIR}/tests
    )
    add_test(NAME emf_artop_codegen_tests COMMAND emf_artop_codegen_tests)
endif()
