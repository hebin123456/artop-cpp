# emf::xmi —— XMI 序列化/反序列化
# 对齐 Java: org.eclipse.emf.ecore.xmi

add_library(emf_xmi
    src/XMIResource.cpp
    src/XMIResourceFactory.cpp
    src/XMISaver.cpp
    src/XMILoader.cpp
    src/XMLHelper.cpp
    src/XMLHandler.cpp
    src/SAXXMLHandler.cpp
    src/XMIHelper.cpp
    src/XMIHandler.cpp
    src/SAXXMIHandler.cpp
    src/XMLSaveImpl.cpp
    src/XMLLoadImpl.cpp
    third-party/pugixml/pugixml.cpp
)

target_include_directories(emf_xmi
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
    PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/third-party/pugixml
)
target_link_libraries(emf_xmi PUBLIC emf_common emf_ecore emf_ecore_util)

# 单元测试
if(EMF_BUILD_TESTS)
    add_executable(emf_xmi_tests
        tests/test_main.cpp
        tests/XMISaverTests.cpp
        tests/XMILoaderTests.cpp
        tests/XMIResourceFactoryTests.cpp
        tests/RoundtripTests.cpp
        tests/JavaInteropTests.cpp
        tests/XMLHelperTests.cpp
        tests/P3_XMLSaveLoadUUIDTests.cpp
        tests/P3_5_GetEObjectByIDHrefTests.cpp
        tests/E2E_MultiFileEcoreTests.cpp
        tests/E2E_GenModelXmiTests.cpp
        tests/E2E_GenModelXmiTypedMultiFileTests.cpp
        tests/E2E_GenModelXmiCxxProducesTests.cpp
        tests/E2E_GenModelXmiMultiEcoreTypedTests.cpp
        tests/E2E_GenModelXmiEquivalentReplacementTests.cpp
        tests/E2E_ProxyModelTests.cpp
        tests/RuntimeBehaviorTests.cpp
        tests/StaticVsDynamicXmiTests.cpp
        tests/XmiInteropTests.cpp
)
    target_link_libraries(emf_xmi_tests PRIVATE emf_xmi emf_ecore_codegen)
    target_compile_definitions(emf_xmi_tests PRIVATE
        EMF_CODEGEN_TEST_OUTPUT_DIR="${CMAKE_BINARY_DIR}/emf-xmi-tests"
        EMF_BUILD_DIR="${CMAKE_BINARY_DIR}"
        EMFCPP_SOURCE_DIR="${CMAKE_SOURCE_DIR}"
        EMF_TEST_CXX_FLAGS="${CMAKE_CXX_FLAGS}"
    )
    add_test(NAME emf_xmi_tests COMMAND emf_xmi_tests)

    # 探针：arxml 反序列化能力验证
    add_executable(probe_arxml tests/probe_arxml.cpp)
    target_link_libraries(probe_arxml PRIVATE emf_xmi emf_common emf_ecore emf_artop_runtime)
    add_executable(probe_autosar448 tests/probe_autosar448.cpp)
    target_link_libraries(probe_autosar448 PRIVATE emf_xmi emf_common emf_ecore)
endif()