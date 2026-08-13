# examples —— 对齐 Java 的 emf-demo
add_executable(emf_demo
    src/EcoreModelBuilder.cpp
    src/GenModelBuilder.cpp
    src/DynamicInstanceFactory.cpp
    src/XmiSerializerDemo.cpp
    src/ChangeNotificationDemo.cpp
    src/CompareDemo.cpp
    src/ValidationDemo.cpp
    src/SphinxServerDemo.cpp
    src/Main.cpp
)

target_include_directories(emf_demo PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/src)
target_link_libraries(emf_demo PRIVATE
    emf_common emf_ecore emf_xmi emf_xsd emf_sphinx emf_compare emf_validation
)
