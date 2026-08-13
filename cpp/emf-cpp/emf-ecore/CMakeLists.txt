# emf::ecore —— EMF Ecore 元模型（EClass/EAttribute/EReference/... + EcorePackage/EcoreFactory）
# 对齐 Java: org.eclipse.emf.ecore
#
# 依赖方向（单向）：
#   emf-ecore → emf-common
#   emf-ecore-util → emf-ecore（反向，emf-ecore 不依赖 emf-ecore-util）
# BasicEObject.h 在本模块内自定义 NotificationChain 别名，避免对 emf-ecore-util 的循环依赖。

# Helper: 仅收集磁盘上实际存在的 .cpp，避免个别文件临时缺失导致整体构建失败。
function(emf_collect_cpps out_var)
    set(_list)
    foreach(_src IN LISTS ARGN)
        if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/${_src})
            list(APPEND _list ${_src})
        else()
            message(STATUS "Skipping missing source: ${_src}")
        endif()
    endforeach()
    set(${out_var} "${_list}" PARENT_SCOPE)
endfunction()

emf_collect_cpps(_ECORE_SRCS
    src/EModelElementImpl.cpp
    src/ENamedElementImpl.cpp
    src/ETypedElementImpl.cpp
    src/EClassifierImpl.cpp
    src/EClassImpl.cpp
    src/EDataTypeImpl.cpp
    src/EEnumImpl.cpp
    src/EEnumLiteralImpl.cpp
    src/EStructuralFeatureImpl.cpp
    src/EAttributeImpl.cpp
    src/EReferenceImpl.cpp
    src/EOperationImpl.cpp
    src/EParameterImpl.cpp
    src/ETypeParameterImpl.cpp
    src/EGenericTypeImpl.cpp
    src/EAnnotationImpl.cpp
    src/EFactoryImpl.cpp
    src/EPackageImpl.cpp
    src/EcorePackage.cpp
    src/EcoreMetadata.cpp
    src/BasicEObject.cpp
    src/DynamicEObject.cpp
    src/util/ConversionDelegate.cpp
)

add_library(emf_ecore ${_ECORE_SRCS})

target_include_directories(emf_ecore
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
        ${CMAKE_SOURCE_DIR}/emf-common/include
)

# emf-ecore 仅依赖 emf-common（线程由 emf-common 传递）
target_link_libraries(emf_ecore PUBLIC emf_common)

# 单元测试
if(EMF_BUILD_TESTS)
    emf_collect_cpps(_ECORE_TEST_SRCS
        tests/test_main.cpp
        tests/EcorePackageTests.cpp
        tests/EClassImplTests.cpp
        tests/EPackageImplTests.cpp
        tests/DataTypeUtilTests.cpp
        tests/BasicEObjectTests.cpp
        tests/ChangeNotificationTests.cpp
        tests/DynamicEObjectImplTests.cpp
        tests/EObjectEInvokeTests.cpp
        tests/ETypedElementImplTests.cpp
    )
    add_executable(emf_ecore_tests ${_ECORE_TEST_SRCS})
    # 测试可能用到 emf-ecore-util 的 EList 实现（如 EObjectWithInverseEList），
    # 暴露其 include 目录（若存在）以便编译；不强制链接。
    if(EXISTS ${CMAKE_SOURCE_DIR}/emf-ecore-util/include)
        target_include_directories(emf_ecore_tests PRIVATE
            ${CMAKE_SOURCE_DIR}/emf-ecore-util/include)
    endif()
    target_link_libraries(emf_ecore_tests PRIVATE emf_ecore emf_common)
    add_test(NAME emf_ecore_tests COMMAND emf_ecore_tests)
endif()
