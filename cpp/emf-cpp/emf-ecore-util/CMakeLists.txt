# emf::ecore_util —— Ecore 工具集
# 对齐 Java: org.eclipse.emf.ecore.util

add_library(emf_ecore_util
    src/EcoreUtil.cpp
    src/Copier.cpp
    src/EqualityHelper.cpp
    src/EcoreSwitch.cpp
    src/EcoreAdapterFactory.cpp
    src/EObjectValidator.cpp
    src/EcoreValidator.cpp
    src/ECrossReferenceAdapter.cpp
    src/EContentsEList.cpp
    src/BasicExtendedMetaData.cpp
    src/ConversionDelegate.cpp
    # ===== 6 个 EObject* EList 变体（模板显式实例化 .cpp）=====
    src/EObjectEList.cpp
    src/EObjectResolvingEList.cpp
    src/EObjectContainmentEList.cpp
    src/EObjectWithInverseEList.cpp
    src/EObjectWithInverseResolvingEList.cpp
    src/EObjectContainmentWithInverseEList.cpp
    # ===== FeatureMap / EMap 体系 =====
    src/BasicFeatureMap.cpp
    src/EcoreEMap.cpp
    src/FeatureMapUtil.cpp
    # ===== Validator Registry 体系（对齐 Java EValidatorRegistryImpl / ValidationDelegateRegistryImpl）=====
    src/EValidatorRegistryImpl.cpp
)
target_include_directories(emf_ecore_util
    PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include
)
target_link_libraries(emf_ecore_util PUBLIC emf_common emf_ecore)

add_executable(emf_ecore_util_tests
    tests/test_main.cpp
    tests/BasicFeatureMapTests.cpp
    tests/EcoreUtilTests.cpp
    tests/EcoreUtilExtendedTests.cpp

    tests/CopierTests.cpp
    tests/EcoreSwitchTests.cpp
    tests/EObjectValidatorTests.cpp
    tests/ValidatorComprehensiveTests.cpp
    tests/EObjectEListTests.cpp
)
target_link_libraries(emf_ecore_util_tests PRIVATE emf_ecore_util)
add_test(NAME emf_ecore_util_tests COMMAND emf_ecore_util_tests)
