// EcoreSwitch 测试
#include "test_main.h"
#include "emf/ecore/util/EcoreUtil.h"
#include "emf/ecore/util/EcoreSwitch.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::util;
using emf::common::EObject;

class CountingSwitch : public EcoreSwitch {
public:
    int eClassCount = 0;
    int eAttrCount = 0;
    int eRefCount = 0;
    int defaultCount = 0;
    EObject* caseEClass(EClass* obj) override { eClassCount++; return obj; }
    EObject* caseEAttribute(EAttribute* obj) override { eAttrCount++; return obj; }
    EObject* caseEReference(EReference* obj) override { eRefCount++; return obj; }
    EObject* caseEObject(EObject* obj) override { defaultCount++; return obj; }
};

EMF_TEST(EcoreSwitch_Dispatch) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    // 实际 EClass / EAttribute / EReference 实例
    EObject* cls = EcoreFactory::instance().createEClass();
    EObject* attr = EcoreFactory::instance().createEAttribute();
    EObject* ref = EcoreFactory::instance().createEReference();
    EXPECT_NOT_NULL(cls);
    EXPECT_NOT_NULL(attr);
    EXPECT_NOT_NULL(ref);

    CountingSwitch sw;
    sw.doSwitch(cls);
    sw.doSwitch(attr);
    sw.doSwitch(ref);
    EXPECT_EQ(sw.eClassCount, 1);
    EXPECT_EQ(sw.eAttrCount, 1);
    EXPECT_EQ(sw.eRefCount, 1);
}
