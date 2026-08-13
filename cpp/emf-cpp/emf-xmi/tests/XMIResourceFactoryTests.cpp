// XMIResourceFactory 单元测试
#include "test_main.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/URI.h"

using emf::xmi::XMIResourceFactory;
using emf::xmi::XMIResource;
using emf::common::URI;

EMF_TEST(XMIResourceFactory_RegisterDefaults) {
    XMIResourceFactory::registerDefaults();
    auto r1 = XMIResourceFactory::createResourceFor(URI::createURI("file:///tmp/foo.xmi"));
    EXPECT_NOT_NULL(r1);
    auto r2 = XMIResourceFactory::createResourceFor(URI::createURI("file:///tmp/bar.ecore"));
    EXPECT_NOT_NULL(r2);
}

EMF_TEST(XMIResourceFactory_UnknownExtensionFallback) {
    auto r = XMIResourceFactory::createResourceFor(URI::createURI("file:///tmp/foo.unknown"));
    EXPECT_NOT_NULL(r);  // 不抛异常，回退到 XMIResource
}

EMF_TEST(XMIResourceFactory_CustomExtension) {
    bool called = false;
    XMIResourceFactory::registerFactory("myext",
        [&called](const URI& u) -> std::unique_ptr<XMIResource> {
            called = true;
            return std::unique_ptr<XMIResource>(new XMIResource(u));
        });
    auto r = XMIResourceFactory::createResourceFor(URI::createURI("file:///tmp/foo.myext"));
    EXPECT_NOT_NULL(r);
    EXPECT_TRUE(called);
    auto r2 = XMIResourceFactory::createResourceFor(URI::createURI("file:///tmp/foo.MYEXT"));
    EXPECT_NOT_NULL(r2);  // 大小写不敏感
}

EMF_TEST(XMIResourceFactory_DirectCreate) {
    XMIResourceFactory f;
    auto r = f.createResource(URI::createURI("inmemory://x"));
    EXPECT_NOT_NULL(r);
}
