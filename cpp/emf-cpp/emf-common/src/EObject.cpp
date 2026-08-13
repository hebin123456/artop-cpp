// EObject.cpp —— EObject / EObjectImpl 实现
// 对齐 org.eclipse.emf.ecore.impl.BasicEObjectImpl
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"

#include <stdexcept>

namespace emf::common {

// EObject 析构（虚，需 out-of-line 以保证 vtable 唯一）
EObject::~EObject() = default;

// ===== EObject 默认实现（featureID 版本抛异常，子类 override）=====

std::any EObject::eGet(int /*featureID*/) const {
    throw std::runtime_error("EObject::eGet(featureID) not implemented");
}

void EObject::eSet(int /*featureID*/, std::any /*value*/) {
    throw std::runtime_error("EObject::eSet(featureID) not implemented");
}

bool EObject::eIsSet(int /*featureID*/) const {
    return false;
}

void EObject::eUnset(int /*featureID*/) {
    // 默认空操作
}

// 类型化 eGet 默认实现：未命中（生成类 override 才会返回 true）
bool EObject::eGetString(int /*featureID*/, std::string& /*out*/) const { return false; }
bool EObject::eGetInt64(int /*featureID*/, int64_t& /*out*/) const { return false; }
bool EObject::eGetBool(int /*featureID*/, bool& /*out*/) const { return false; }
bool EObject::eGetEObject(int /*featureID*/, EObject*& /*out*/) const { return false; }
bool EObject::eGetDouble(int /*featureID*/, double& /*out*/) const { return false; }

std::any EObject::eInvoke(emf::ecore::EOperation* /*operation*/,
                           const std::vector<std::any>& /*args*/) {
    throw std::runtime_error("EObject::eInvoke not implemented");
}

int EObject::eDerivedOperationID(emf::ecore::EOperation* /*operation*/) const {
    return -1;
}

const EObject* EObject::eContainerConst() const {
    return nullptr;
}

// ===== ARTOP 扩展方法默认实现（返回空/默认值，子类 override）=====

const std::string& EObject::eXmlName() const {
    static const std::string e;
    return e;
}

const std::string& EObject::eXmlNamePlural() const {
    static const std::string e;
    return e;
}

const std::string& EObject::eNsPrefix() const {
    static const std::string e;
    return e;
}

const std::string& EObject::eContentKind() const {
    static const std::string e;
    return e;
}

const std::string& EObject::eFeatureXmlName(int /*featureID*/) const {
    static const std::string e;
    return e;
}

int EObject::eFeatureAprxmlRule(int /*featureID*/) const {
    return 0;
}

int EObject::eFeatureSequenceOffset(int /*featureID*/) const {
    return 0;
}

bool EObject::eFeatureIsXmlAttribute(int /*featureID*/) const {
    return false;
}

bool EObject::eFeatureIsRoleElement(int /*featureID*/) const {
    return false;
}

bool EObject::eFeatureIsRoleWrapperElement(int /*featureID*/) const {
    return false;
}

bool EObject::eFeatureIsTypeElement(int /*featureID*/) const {
    return false;
}

bool EObject::eFeatureIsTypeWrapperElement(int /*featureID*/) const {
    return false;
}

int EObject::eFeatureCount() const {
    // 默认返回 0，由生成的 Impl override 返回 eClass()->getFeatureCount()
    // （emf-common 不能依赖 emf-ecore 完整定义）
    return 0;
}

EObject* EObject::eResolveProxy(EObject* proxy) const {
    // 对齐 Java BasicEObjectImpl.eResolveProxy：
    //   Resource.Internal eResource = eInternalResource();
    //   return eResource != null ? eResource.getEObject(proxy.eProxyURI()) : proxy;
    if (!proxy || !proxy->eIsProxy()) return proxy;
    // 获取 this 的 resource（调用方所在资源）
    Resource* res = eResource();
    if (!res) return proxy;
    // 优先通过 ResourceSet 解析（支持跨资源 proxy，对齐 Java URI.deresolve）
    ResourceSet* rs = res->getResourceSet();
    if (rs) {
        auto* impl = dynamic_cast<EObjectImpl*>(proxy);
        if (impl) {
            EObject* resolved = rs->getEObject(impl->eProxyURI(), true);
            if (resolved) return resolved;
        }
    }
    // 退化：在同一 resource 内按 fragment 解析
    auto* impl = dynamic_cast<EObjectImpl*>(proxy);
    if (impl) {
        const URI& uri = impl->eProxyURI();
        std::string s = uri.toString();
        auto hashPos = s.find('#');
        if (hashPos != std::string::npos) {
            std::string fragment = s.substr(hashPos + 1);
            EObject* resolved = res->getEObject(fragment);
            if (resolved) return resolved;
        }
    }
    return proxy;
}

bool EObject::eNotificationRequired() const {
    return eDeliver() && !eAdapters().empty();
}

std::string EObject::eGetTextContent() const {
    static const std::string e;
    return e;
}

// ===== EObject 基类 eInverseAdd/eInverseRemove 默认实现 =====
// 非纯虚虚方法必须有 out-of-line 定义以保证 vtable 符号唯一。
// 默认行为：不做反向引用维护，直接返回 notifications（对齐 Java BasicEObjectImpl 默认）。
EObject::EObjectNotificationChain EObject::eInverseAdd(
    EObject* /*otherEnd*/, int /*featureID*/,
    emf::ecore::EClass* /*inverseFeatureClass*/,
    EObject::EObjectNotificationChain notifications) {
    return notifications;
}

EObject::EObjectNotificationChain EObject::eInverseRemove(
    EObject* /*otherEnd*/, int /*featureID*/,
    emf::ecore::EClass* /*inverseFeatureClass*/,
    EObject::EObjectNotificationChain notifications) {
    return notifications;
}

// ===== EObjectImpl 实现 =====

const std::vector<EObject*> EObjectImpl::kEmptyContents_;

EObjectImpl::~EObjectImpl() = default;

void EObjectImpl::setEContainer(EObject* container) {
    if (eContainer_ == container) return;
    // 对齐 Java BasicEObjectImpl.eBasicSetContainer：发反向 ADD/REMOVE 通知。
    // notifier = this（child），feature = nullptr（标识 containment 反向通知，
    //   区别于正向 containment ADD 通知——正向通知 feature 非 null），
    // oldValue = oldContainer, newValue = newContainer。
    // EContentAdapter::selfAdapt 跳过 feature==nullptr 的通知，避免对父对象误
    //   触发 removeAdapter（旧 container 是父，不应被 detach）。
    // 修复（原 gap：setEContainer 静默赋值，child 侧 adapter 收不到 container 变化）。
    bool notify = eNotificationRequired();
    EObject* oldContainer = eContainer_;
    eContainer_ = container;
    if (notify) {
        if (oldContainer) {
            Notification n(Notification::EventType::REMOVE, this, nullptr, -1,
                           std::any(static_cast<EObject*>(oldContainer)), std::any(), -1);
            eNotify(n);
        }
        if (container) {
            Notification n(Notification::EventType::ADD, this, nullptr, -1,
                           std::any(), std::any(static_cast<EObject*>(container)), -1);
            eNotify(n);
        }
    }
}

void EObjectImpl::setEContainingFeature(const emf::ecore::EStructuralFeature* feature) {
    eContainingFeature_ = feature;
}

std::vector<EObject*> EObjectImpl::eContents() const {
    return kEmptyContents_;
}

std::vector<EObject*> EObjectImpl::eCrossReferences() const {
    return kEmptyContents_;
}

TreeIterator<EObject*>* EObjectImpl::eAllContents() const {
    // 对齐 Java BasicEObjectImpl.eAllContents：基于 eContents() 的 DFS 树迭代器。
    // 用栈保存每层的 (vector, index)，避免递归；prune() 弹出当前层。
    // 修复（原 gap：返回空迭代器，EContentAdapter 等消费者失效）。
    class ContentsTreeIterator : public TreeIterator<EObject*> {
    public:
        // 初始把根对象的 eContents 入栈
        explicit ContentsTreeIterator(const EObject* root) {
            if (root) push(root->eContents());
        }
        bool hasNext() override {
            advance();
            return !stack_.empty() && stack_.back().idx < (int)stack_.back().children.size();
        }
        EObject* next() override {
            advance();
            if (stack_.empty()) return nullptr;
            auto& frame = stack_.back();
            EObject* result = frame.children[frame.idx++];
            // 下钻到子对象（惰性：仅当用户真正 next 到该对象时才展开其子树，
            // 但为简化实现，next 后立即把子对象的 eContents 入栈，对齐 Java 行为）
            if (result) push(result->eContents());
            return result;
        }
    private:
        struct Frame {
            std::vector<EObject*> children;
            int idx = 0;
        };
        std::vector<Frame> stack_;
        void push(std::vector<EObject*> children) {
            if (!children.empty()) stack_.push_back(Frame{std::move(children), 0});
        }
        // 弹出已耗尽的顶层帧
        void advance() {
            while (!stack_.empty()) {
                auto& frame = stack_.back();
                if (frame.idx < (int)frame.children.size()) return;
                stack_.pop_back();
            }
        }
    };
    return new ContentsTreeIterator(this);
}

Resource* EObjectImpl::eResource() const {
    // 对齐 Java BasicEObjectImpl.eResource：
    //   return eDirectResource != null ? eDirectResource : eInternalContainer() != null ? ... : null;
    // 优先返回直接关联的 Resource（由 addToContents 设置）
    if (eDirectResource_) return eDirectResource_;
    // 否则沿 eContainer 链向上查找
    const EObject* current = eContainer_;
    while (current) {
        auto* impl = dynamic_cast<const EObjectImpl*>(current);
        if (impl && impl->eDirectResource_) return impl->eDirectResource_;
        current = current->eContainer();
    }
    return nullptr;
}

bool EObjectImpl::eIsProxy() const {
    // 用 isEmpty() 避免构造临时 string（toString 会触发 string 分配，
    // 若对象内存已损坏可能导致 bad_alloc）
    return !eProxyURI_.isEmpty();
}

EObject* EObjectImpl::eResolveProxy(EObject* proxy) const {
    // 对齐 Java BasicEObjectImpl.eResolveProxy：委托基类实现（通过 eResource 解析）
    return EObject::eResolveProxy(proxy);
}

std::string EObjectImpl::eGetTextContent() const {
    static const std::string e;
    return e;
}

// eInverseAdd/eInverseRemove 默认实现：不做反向维护，直接返回 notifications。
// BasicEObject override 查 eInverseELists_ 注册表并调用 EInverseList::basicAdd/basicRemove。
EObject::EObjectNotificationChain EObjectImpl::eInverseAdd(
    EObject* /*otherEnd*/, int /*featureID*/,
    emf::ecore::EClass* /*inverseFeatureClass*/,
    EObject::EObjectNotificationChain notifications) {
    return notifications;
}

EObject::EObjectNotificationChain EObjectImpl::eInverseRemove(
    EObject* /*otherEnd*/, int /*featureID*/,
    emf::ecore::EClass* /*inverseFeatureClass*/,
    EObject::EObjectNotificationChain notifications) {
    return notifications;
}

// ===== EObjectImpl 的 eGet/eSet/eIsSet/eUnset 默认实现 =====
// 子类（EModelElementImpl 等）override 这些方法处理特定 FeatureID，
// 未匹配的 featureID 分发到父类（最终到这里抛异常）。

std::any EObjectImpl::eGet(const emf::ecore::EStructuralFeature* /*feature*/) const {
    // 对齐 Java BasicEObjectImpl.eDynamicGet：未知 feature 返回默认值（容错）
    return std::any{};
}

std::any EObjectImpl::eGet(const emf::ecore::EStructuralFeature* feature, bool /*resolve*/) const {
    return eGet(feature);
}

void EObjectImpl::eSet(const emf::ecore::EStructuralFeature* /*feature*/, std::any /*value*/) {
    // 对齐 Java BasicEObjectImpl.eDynamicSet：未知 feature 静默忽略（容错）
    // 子类（生成的 Impl）override 此方法处理特定 featureID，未匹配的走到这里。
}

bool EObjectImpl::eIsSet(const emf::ecore::EStructuralFeature* /*feature*/) const {
    return false;
}

void EObjectImpl::eUnset(const emf::ecore::EStructuralFeature* /*feature*/) {
    // 默认空操作
}

}  // namespace emf::common
