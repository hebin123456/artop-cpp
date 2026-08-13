// EObject.eInvoke 单元测试
// 对齐 org.eclipse.emf.ecore.EObject.eInvoke / EOperation.Internal.InvocationDelegate
// 覆盖：默认 eInvoke 抛异常、自定义 EObject 覆盖 eInvoke 委托 EInvocationDelegate、
//       dynamicInvoke 接收 target/args 并返回结果、void 操作返回空 any、
//       eDerivedOperationID 默认 -1、按 EOperation 与按 operationID 调用
#include "test_main.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/impl/BasicEObject.h"
#include "emf/ecore/EInvocationDelegate.h"
#include "emf/common/EObject.h"
#include <any>
#include <memory>
#include <string>
#include <unordered_map>
#include <vector>

using emf::common::EObject;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EOperation;
using emf::ecore::EInvocationDelegate;
using emf::ecore::impl::BasicEObject;

namespace {

// ===== 自定义 EInvocationDelegate 实现 =====

// 加法 delegate：接收两个 int 参数，返回和
class AddDelegate : public EInvocationDelegate {
public:
    std::any dynamicInvoke(EObject* target, const std::vector<std::any>& args) override {
        lastTarget_ = target;
        lastArgCount_ = (int)args.size();
        int a = args.size() > 0 ? std::any_cast<int>(args[0]) : 0;
        int b = args.size() > 1 ? std::any_cast<int>(args[1]) : 0;
        return std::any{a + b};
    }
    EObject* lastTarget() const { return lastTarget_; }
    int lastArgCount() const { return lastArgCount_; }
private:
    EObject* lastTarget_ = nullptr;
    int lastArgCount_ = 0;
};

// 问候 delegate：返回字符串（无参）
class GreetDelegate : public EInvocationDelegate {
public:
    std::any dynamicInvoke(EObject* /*target*/, const std::vector<std::any>& /*args*/) override {
        return std::any{std::string{"hello"}};
    }
};

// void delegate：返回空 any 表示无返回值
class VoidDelegate : public EInvocationDelegate {
public:
    std::any dynamicInvoke(EObject* /*target*/, const std::vector<std::any>& /*args*/) override {
        called_ = true;
        return std::any{};  // 空 any = void
    }
    bool wasCalled() const { return called_; }
private:
    bool called_ = false;
};

// 记录参数的 delegate
class RecordingDelegate : public EInvocationDelegate {
public:
    std::any dynamicInvoke(EObject* target, const std::vector<std::any>& args) override {
        target_ = target;
        args_ = args;
        return std::any{};
    }
    EObject* target() const { return target_; }
    const std::vector<std::any>& args() const { return args_; }
private:
    EObject* target_ = nullptr;
    std::vector<std::any> args_;
};

// ===== 可挂载 InvocationDelegate 的 EObject 子类 =====
class InvokableObject : public BasicEObject {
public:
    void setEClass(EClass* cls) { eClass_ = cls; }
    EClass* eClass() const override { return eClass_; }

    // 注册 delegate：按 EOperation 指针注册
    void setEInvocationDelegate(EOperation* op, std::shared_ptr<EInvocationDelegate> delegate) {
        delegates_[op] = std::move(delegate);
    }

    // 覆盖 eInvoke：查找注册的 delegate 并委托
    std::any eInvoke(EOperation* operation, const std::vector<std::any>& args) override {
        if (operation) {
            auto it = delegates_.find(operation);
            if (it != delegates_.end() && it->second) {
                return it->second->dynamicInvoke(this, args);
            }
        }
        // 无 delegate：回退到基类默认（抛异常）
        return EObject::eInvoke(operation, args);
    }

private:
    EClass* eClass_ = nullptr;
    std::unordered_map<EOperation*, std::shared_ptr<EInvocationDelegate>> delegates_;
};

// 构建带若干 operation 的 EClass
struct OpModel {
    EClass* cls;
    EOperation* addOp;
    EOperation* greetOp;
    EOperation* voidOp;
};

OpModel makeOpModel() {
    OpModel m;
    m.cls = EcoreFactory::instance().createEClass();
    m.cls->setName("Calculator");

    m.addOp = EcoreFactory::instance().createEOperation();
    m.addOp->setName("add");
    m.addOp->setOperationID(0);
    m.cls->addEOperation(m.addOp);

    m.greetOp = EcoreFactory::instance().createEOperation();
    m.greetOp->setName("greet");
    m.greetOp->setOperationID(1);
    m.cls->addEOperation(m.greetOp);

    m.voidOp = EcoreFactory::instance().createEOperation();
    m.voidOp->setName("reset");
    m.voidOp->setOperationID(2);
    m.cls->addEOperation(m.voidOp);

    return m;
}

}  // namespace

// ===== 默认 EObject::eInvoke 抛异常 =====

EMF_TEST(EInvoke_Default_Throws) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    // 无 delegate 注册：回退到 EObject::eInvoke 抛异常
    EXPECT_THROWS(obj.eInvoke(m.addOp, {std::any{1}, std::any{2}}));
}

EMF_TEST(EInvoke_NullOperation_Throws) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    // null operation：不走 delegate，直接回退基类抛异常
    EXPECT_THROWS(obj.eInvoke(nullptr, {}));
}

// ===== 委托 EInvocationDelegate =====

EMF_TEST(EInvoke_DelegatesToAddDelegate) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    auto delegate = std::make_shared<AddDelegate>();
    obj.setEInvocationDelegate(m.addOp, delegate);

    std::vector<std::any> args{std::any{3}, std::any{4}};
    auto result = obj.eInvoke(m.addOp, args);
    EXPECT_TRUE(result.has_value());
    EXPECT_EQ(std::any_cast<int>(result), 7);
}

EMF_TEST(EInvoke_DelegateReceivesTargetAndArgs) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    auto delegate = std::make_shared<RecordingDelegate>();
    obj.setEInvocationDelegate(m.addOp, delegate);

    std::vector<std::any> args{std::any{10}, std::any{20}, std::any{30}};
    obj.eInvoke(m.addOp, args);
    EXPECT_EQ(delegate->target(), &obj);
    EXPECT_EQ(delegate->args().size(), (size_t)3);
    EXPECT_EQ(std::any_cast<int>(delegate->args()[0]), 10);
    EXPECT_EQ(std::any_cast<int>(delegate->args()[1]), 20);
    EXPECT_EQ(std::any_cast<int>(delegate->args()[2]), 30);
}

EMF_TEST(EInvoke_DelegateReceivesEmptyArgs) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    auto delegate = std::make_shared<RecordingDelegate>();
    obj.setEInvocationDelegate(m.greetOp, delegate);

    obj.eInvoke(m.greetOp, {});
    EXPECT_EQ(delegate->args().size(), (size_t)0);
    EXPECT_EQ(delegate->target(), &obj);
}

EMF_TEST(EInvoke_GreetDelegate_ReturnsString) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    auto delegate = std::make_shared<GreetDelegate>();
    obj.setEInvocationDelegate(m.greetOp, delegate);

    auto result = obj.eInvoke(m.greetOp, {});
    EXPECT_TRUE(result.has_value());
    EXPECT_EQ(std::any_cast<std::string>(result), std::string("hello"));
}

EMF_TEST(EInvoke_VoidDelegate_ReturnsEmptyAny) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    auto delegate = std::make_shared<VoidDelegate>();
    obj.setEInvocationDelegate(m.voidOp, delegate);

    auto result = obj.eInvoke(m.voidOp, {});
    EXPECT_FALSE(result.has_value());  // 空 any 表示 void
    EXPECT_TRUE(delegate->wasCalled());
}

// ===== 同一对象多 operation 委托 =====

EMF_TEST(EInvoke_MultipleOperations_DistinctDelegates) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    obj.setEInvocationDelegate(m.addOp, std::make_shared<AddDelegate>());
    obj.setEInvocationDelegate(m.greetOp, std::make_shared<GreetDelegate>());
    obj.setEInvocationDelegate(m.voidOp, std::make_shared<VoidDelegate>());

    EXPECT_EQ(std::any_cast<int>(obj.eInvoke(m.addOp, {std::any{5}, std::any{6}})), 11);
    EXPECT_EQ(std::any_cast<std::string>(obj.eInvoke(m.greetOp, {})), std::string("hello"));
    EXPECT_FALSE(obj.eInvoke(m.voidOp, {}).has_value());
}

EMF_TEST(EInvoke_UnregisteredOperation_Throws) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    // 只注册 addOp，调用 greetOp（未注册）应抛异常
    obj.setEInvocationDelegate(m.addOp, std::make_shared<AddDelegate>());
    EXPECT_THROWS(obj.eInvoke(m.greetOp, {}));
}

// ===== EInvocationDelegate 接口直接测试 =====

EMF_TEST(EInvocationDelegate_DynamicInvoke_Direct) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    AddDelegate delegate;
    std::vector<std::any> args{std::any{100}, std::any{200}};
    auto result = delegate.dynamicInvoke(&obj, args);
    EXPECT_EQ(std::any_cast<int>(result), 300);
    EXPECT_EQ(delegate.lastTarget(), &obj);
    EXPECT_EQ(delegate.lastArgCount(), 2);
}

EMF_TEST(EInvocationDelegate_DynamicInvoke_SingleArg) {
    AddDelegate delegate;
    std::vector<std::any> args{std::any{42}};
    auto result = delegate.dynamicInvoke(nullptr, args);
    EXPECT_EQ(std::any_cast<int>(result), 42);  // 缺省第二参数为 0
}

EMF_TEST(EInvocationDelegate_DynamicInvoke_NoArgs) {
    AddDelegate delegate;
    auto result = delegate.dynamicInvoke(nullptr, {});
    EXPECT_EQ(std::any_cast<int>(result), 0);  // 两参数均缺省为 0
}

// ===== eDerivedOperationID =====

EMF_TEST(EDerivedOperationID_DefaultReturnsMinusOne) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    // 默认实现返回 -1
    EXPECT_EQ(obj.eDerivedOperationID(m.addOp), -1);
    EXPECT_EQ(obj.eDerivedOperationID(nullptr), -1);
}

// ===== EOperation 元数据 =====

EMF_TEST(EOperation_OperationID_RoundTrip) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    EXPECT_EQ(m.addOp->getOperationID(), 0);
    EXPECT_EQ(m.greetOp->getOperationID(), 1);
    EXPECT_EQ(m.voidOp->getOperationID(), 2);

    // 修改 operationID
    m.addOp->setOperationID(99);
    EXPECT_EQ(m.addOp->getOperationID(), 99);
}

EMF_TEST(EOperation_GetByName) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    EXPECT_EQ(m.cls->getEOperation("add"), m.addOp);
    EXPECT_EQ(m.cls->getEOperation("greet"), m.greetOp);
    EXPECT_EQ(m.cls->getEOperation("reset"), m.voidOp);
    EXPECT_NULL(m.cls->getEOperation("nonexistent"));
}

EMF_TEST(EOperation_EAllOperations_ContainsAll) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    const auto& ops = m.cls->getEAllOperations();
    EXPECT_EQ(ops.size(), (size_t)3);
    EXPECT_EQ(m.cls->getOperationCount(), 3);
}

// ===== 替换已注册的 delegate =====

EMF_TEST(EInvoke_ReplaceDelegate) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeOpModel();
    InvokableObject obj;
    obj.setEClass(m.cls);
    obj.setEInvocationDelegate(m.addOp, std::make_shared<AddDelegate>());
    EXPECT_EQ(std::any_cast<int>(obj.eInvoke(m.addOp, {std::any{1}, std::any{2}})), 3);

    // 替换为新的 delegate（返回固定值）
    class FixedDelegate : public EInvocationDelegate {
    public:
        std::any dynamicInvoke(EObject*, const std::vector<std::any>&) override {
            return std::any{999};
        }
    };
    obj.setEInvocationDelegate(m.addOp, std::make_shared<FixedDelegate>());
    EXPECT_EQ(std::any_cast<int>(obj.eInvoke(m.addOp, {std::any{1}, std::any{2}})), 999);
}
