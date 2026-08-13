# emf::artop::runtime —— AUTOSAR 运行时核心
# 对齐 Java: org.artop.aal.common (serialization 合并进 runtime)

# pugixml 头文件路径（复用 emf-xmi 内置的 third-party 副本）
# pugixml.cpp 已编译进 emf_xmi，emf_artop_runtime 通过 PUBLIC 链接 emf_xmi 获取符号，
# 此处仅需头文件路径供 AutosarXMLLoader.cpp / AutosarXMLSaver.cpp 编译，避免重复编译产生符号冲突
set(EMF_ARTOP_PUGIXML_DIR ${CMAKE_SOURCE_DIR}/emf-xmi/third-party/pugixml)

add_library(emf_artop_runtime
    src/AutosarMetaModelVersionData.cpp
    src/AutosarReleaseDescriptor.cpp
    src/AutosarResource.cpp
    src/AutosarResourceFactory.cpp
    src/AutosarXMLLoader.cpp
    src/AutosarXMLSaver.cpp
    src/IdentifiableUtil.cpp
    src/UnknownElement.cpp
    src/AutosarLibraryIndex.cpp
)

target_include_directories(emf_artop_runtime
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
    PRIVATE ${EMF_ARTOP_PUGIXML_DIR}
)
# emf_ecore_codegen：EAnnotationReader（模型驱动读取 xmlName / APRXML 规则元数据）
target_link_libraries(emf_artop_runtime PUBLIC emf_common emf_ecore emf_xmi emf_ecore_codegen)

# 注入内置 autosar40.ecore 绝对路径（供 registerDefaultAutosar40Metamodel 使用）
target_compile_definitions(emf_artop_runtime PRIVATE
    EMF_ARTOP_AUTOSAR40_ECORE="${CMAKE_CURRENT_SOURCE_DIR}/model/autosar40.ecore"
)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_artop_runtime_tests
        tests/test_main.cpp
    )
    target_link_libraries(emf_artop_runtime_tests PRIVATE emf_artop_runtime)
    add_test(NAME emf_artop_runtime_tests COMMAND emf_artop_runtime_tests)
endif()
