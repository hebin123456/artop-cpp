#include <cstdio>
#include <cstdint>

// 模拟实际继承链
namespace emf::common {
class Notifier {};
class EObject : public Notifier { public: virtual ~EObject() {} };
class EObjectImpl : public virtual EObject {};
}

class GRunnableEntity : public emf::common::EObjectImpl {};

int main() {
    GRunnableEntity* d = new GRunnableEntity();
    emf::common::EObject* s = static_cast<emf::common::EObject*>(d);
    printf("derived=%p eobject_static=%p eobject_reinterpret=%p same=%d\n",
        (void*)d, (void*)s, (void*)reinterpret_cast<emf::common::EObject*>(d),
        (void*)d == (void*)s);
    // 测试数组 reinterpret_cast
    GRunnableEntity* arr[2] = {d, d};
    emf::common::EObject* const* earr = reinterpret_cast<emf::common::EObject* const*>(arr);
    printf("arr[0]=%p earr[0]=%p same=%d\n", (void*)arr[0], (void*)earr[0],
        (void*)arr[0] == (void*)earr[0]);
    delete d;
    return 0;
}
