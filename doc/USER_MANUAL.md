# emf-cpp 用户手册

C++ 重写 Eclipse EMF + Java ARTOP 的用户指南。本文覆盖构建、EMF 基础（ecore/xmi）、
AUTOSAR 模型创建与操作、核心 API 使用（notification / compare / validation）、性能测试。

## 1. 构建

### 1.1 依赖

- C++17 编译器（g++ ≥ 7 或 clang ≥ 6）
- CMake ≥ 3.10
- pthread

### 1.2 原生构建

```bash
cd /workspace/cpp/emf-cpp
mkdir -p build && cd build
cmake .. -DEMF_BUILD_TESTS=ON
cmake --build . -j$(nproc)
```

构建产物：
- 库：`build/emf-*/libemf_*.a`（静态库）
- 测试：`build/bin/emf_*_tests`

### 1.3 运行测试

```bash
cd /workspace/cpp/emf-cpp/build
ctest --output-on-failure          # 全部测试套件
./bin/emf_common_tests             # 单独运行某套件
./bin/emf_compare_tests
./bin/emf_validation_tests
```

当前测试规模：13 个套件，~1500+ 测试用例。

### 1.4 代码生成（codegen）

AUTOSAR 模型类由 codegen 从 `.ecore` 生成 C++ 类。codegen 二进制位于
`cpp/emf-cpp/build/bin/emf_artop_codegen`。

```bash
# 用法
emf_artop_codegen <ecore-or-genmodel> <out-dir> [options]

# 选项
#   --version=X.Y.Z     AUTOSAR 版本（默认 4.4.8）
#   --release-id=ID     Release id（默认 org.artop.aal.autosar448）
#   --namespace=URI     基础命名空间（默认 http://autosar.org/schema/r4.0）
#   --no-resource       跳过 Resource/Factory 生成
#   --no-extensions     跳过根 extensions 注入

# 示例：从 autosar448.ecore 生成
emf_artop_codegen autosar448.ecore ./gen_out --version=4.4.8
```

每个 EClass 生成 `<ClassName>.h` / `<ClassName>.cpp`，包含：getter/setter、多值
reference 的 `EList<T*>&` getter、反射 override（eGet/eSet/eIsSet/eUnset）、类型化
eGet override（eGetString/eGetInt64/eGetBool/eGetEObject）、静态元数据方法、feature ID
常量。

---

## 2. EMF 基础（ecore / xmi）

本章覆盖 EMF 基础能力的复刻：动态元模型构建（Ecore）、动态对象实例化、通用
XMI 读写。这些 API 对齐 Java `org.eclipse.emf.ecore` / `org.eclipse.emf.ecore.xmi`，
不依赖 AUTOSAR 特定代码，适合处理任意 `.ecore` / `.xmi` 模型。

### 2.1 命名约定（C++ vs Java）

| Java EMF | C++ 本仓库 | 说明 |
|---|---|---|
| `EcoreFactory.eINSTANCE` | `EcoreFactory::instance()` | 单例方法名不同 |
| `EcorePackage.eINSTANCE` | `EcorePackage::instance()` | 同上 |
| `EPackage.getEClassifiers()` | 同名 | 一致 |
| `EPackage.addEClassifier()` | 同名 | 一致（不是 addEClass） |
| `DynamicEObjectImpl` | `DynamicEObject` | 简化命名，有别名 |

生成代码（codegen 产物）中保留 `eINSTANCE` 别名以对齐 Java 习惯，但手写动态
代码用 `instance()`。

### 2.2 初始化

```cpp
#include "emf/ecore/EcorePackage.h"
#include "emf/xmi/XMIResourceFactory.h"

// 1. 初始化 Ecore 元模型单例（必须最先调用）
emf::ecore::EcoreFactory::initialize();
emf::ecore::EcorePackage::initialize();

// 2. 注册 .xmi / .ecore 后缀到 XMIResource 工厂
emf::xmi::XMIResourceFactory::registerDefaults();
```

### 2.3 动态构建元模型

用 `EcoreFactory` 创建 EClass/EAttribute/EReference/EPackage，组装成元模型：

```cpp
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EPackage;
using emf::ecore::EEnum;
using emf::ecore::EEnumLiteral;

auto& f = EcoreFactory::instance();

// 创建 EPackage
auto* pkg = f.createEPackage();
pkg->setName("library");
pkg->setNsURI("http://example.com/library");
pkg->setNsPrefix("lib");

// 创建 EClass：Book
auto* bookCls = f.createEClass();
bookCls->setName("Book");
pkg->addEClassifier(bookCls);

// 创建 EAttribute：title（String 类型）
auto* titleAttr = f.createEAttribute();
titleAttr->setName("title");
titleAttr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
titleAttr->setLowerBound(1);   // 必填
titleAttr->setUpperBound(1);   // 单值
bookCls->addEStructuralFeature(titleAttr);

// 创建 EAttribute：pages（Int 类型）
auto* pagesAttr = f.createEAttribute();
pagesAttr->setName("pages");
pagesAttr->setEAttributeType(EcorePackage::instance().getEDataType_EInt());
pagesAttr->setLowerBound(0);
pagesAttr->setUpperBound(1);
bookCls->addEStructuralFeature(pagesAttr);

// 创建 EEnum：BookCategory
auto* categoryEnum = f.createEEnum();
categoryEnum->setName("BookCategory");
auto* lit1 = f.createEEnumLiteral();
lit1->setName("SCIENCE");
lit1->setValue(0);
categoryEnum->addELiteral(lit1);
auto* lit2 = f.createEEnumLiteral();
lit2->setName("FICTION");
lit2->setValue(1);
categoryEnum->addELiteral(lit2);
pkg->addEClassifier(categoryEnum);

// 创建 EClass：Library（含 containment reference 到 Book）
auto* libCls = f.createEClass();
libCls->setName("Library");
pkg->addEClassifier(libCls);

auto* booksRef = f.createEReference();
booksRef->setName("books");
booksRef->setEReferenceType(bookCls);
booksRef->setContainment(true);    // containment：Library 拥有 Book
booksRef->setLowerBound(0);
booksRef->setUpperBound(-1);       // 多值
libCls->addEStructuralFeature(booksRef);

// 继承：用 addESuperType
// derivedCls->addESuperType(baseCls);
```

#### 内建 EDataType

`EcorePackage::instance()` 提供全部内建类型：

```cpp
auto& p = EcorePackage::instance();
p.getEDataType_EString();      // java.lang.String
p.getEDataType_EBoolean();     // java.lang.Boolean
p.getEDataType_EInt();         // java.lang.Integer
p.getEDataType_ELong();        // java.lang.Long
p.getEDataType_EDouble();      // java.lang.Double
p.getEDataType_EFloat();       // java.lang.Float
p.getEDataType_EByteArray();   // byte[]
p.getEDataType_EDate();        // java.util.Date
// 共 33 个，含 EShort/EByte/EChar/EBigInteger/EBigDecimal 等
```

### 2.4 动态创建 EObject 实例

动态构建的 EClass 无 codegen 生成代码，用 `DynamicEObject` 实例化（对齐 Java
`DynamicEObjectImpl`）：

```cpp
#include "emf/ecore/DynamicEObject.h"

// 方式 A：直接 new DynamicEObject
auto* book = new emf::ecore::DynamicEObject(bookCls);

// 方式 B：通过 EFactory（内部回退到 DynamicEObject）
EObject* book2 = pkg->getEFactoryInstance()->create(bookCls);
```

#### 反射式读写

`DynamicEObject` 自动按 EClass 的 structural feature 反射存储值：

```cpp
auto* titleSf = bookCls->getEStructuralFeature("title");
book->eSet(titleSf, std::any(std::string("Effective C++")));

auto v = book->eGet(titleSf);
if (v.type() == typeid(std::string)) {
    std::cout << "title=" << std::any_cast<std::string>(v) << std::endl;
}

// isSet / unset
bool set = book->eIsSet(titleSf);  // true（已显式设置）
book->eUnset(titleSf);             // 恢复默认
```

#### 构建 containment 树

```cpp
auto* lib = new emf::ecore::DynamicEObject(libCls);
auto* booksRef = libCls->getEStructuralFeature("books");
// 多值 reference 通过 eGet 取出 EList<EObject*> 再 add
auto v = lib->eGet(booksRef);
// DynamicEObject 对多值 reference 返回 EList<EObject*>*
if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
    auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
    list->add(book);   // containment add 自动维护 eContainer
}
// book->eContainer() == lib
```

### 2.5 EList\<T\> 通用工具

多值 feature 返回 `EList<T*>`（对齐 Java `EList`）：

```cpp
emf::common::EList<emf::common::EObject*>& list = ...;

list.add(obj);              // 末尾添加
list.size();                // 元素数
list.empty();               // 是否为空
list[0];                    // 按索引访问
list.removeByIndex(0);      // 按索引删除（T=int 时用此方法）
list.remove(obj);           // 按值删除（T 非 int 时可用）
list.clear();               // 清空
list.contains(obj);         // 是否包含
list.indexOf(obj);          // 索引（不存在返回 -1）
list.move(targetIdx, srcIdx);  // 移动元素

// range-for 遍历
for (auto* e : list) {
    // ...
}
```

> 注意：当 `T=int` 时 `remove(int)` 有重载歧义，用 `removeByIndex(int)`。

### 2.6 通用 XMI 加载与保存

`XMIResource` 处理标准 XMI 格式（`.xmi` / `.ecore`），不依赖 AUTOSAR。

#### 注册 EPackage

加载前需把 EPackage 注册到 `EPackageRegistry`（按 nsURI 查找）：

```cpp
#include "emf/common/EPackageRegistry.h"

emf::common::EPackageRegistry::instance().put(
    pkg->getNsURI(),   // "http://example.com/library"
    pkg);
```

> `XMIResourceFactory::registerDefaults()` 只注册 `.xmi` / `.ecore` 后缀到
> XMIResource 工厂，**不**注册任何 EPackage。自定义 EPackage 必须显式 put。

#### 加载与保存

```cpp
#include "emf/xmi/XMIResource.h"
#include <fstream>

// 加载
emf::xmi::XMIResource res(emf::common::URI("file:/path/to/model.xmi"));
emf::xmi::XMIOptions opts;
std::ifstream ifs("/path/to/model.xmi");
res.load(ifs, opts);

// 获取根对象
auto& contents = res.getContents();
// contents[0] 是根 EObject

// 保存
std::ofstream ofs("/path/to/out.xmi");
res.save(ofs, opts);
```

#### 字符串加载/保存

```cpp
emf::xmi::XMIResource res;
emf::xmi::XMIOptions opts;
res.loadFromString("<library:Library xmi:version=\"2.0\" ...>", opts);
std::string xml = res.saveToString(opts);
```

#### 用 ResourceSet（跨文件 reference）

```cpp
#include "emf/xmi/XMIResourceSet.h"

emf::xmi::XMIResourceSet rs;
auto* r1 = rs.createResource(emf::common::URI("file:/path/to/main.xmi"));
auto* r2 = rs.createResource(emf::common::URI("file:/path/to/lib.xmi"));
r1->load();
r2->load();
// r1 中的 href 引用自动解析到 r2 的对象
```

#### XMIOptions

```cpp
emf::xmi::XMIOptions opts;
opts.declareXmi = true;              // 输出 xmi 声明
opts.xmiVersion = "2.0";             // xmi 版本
opts.declareXsiType = true;          // 输出 xsi:type
opts.assignIDs = true;               // 自动分配 xmi:id
opts.indent = "  ";                  // 缩进
opts.encoding = "UTF-8";             // 编码
opts.xmlDeclaration = true;          // XML 声明
opts.lineWidth = 80;                 // 换行宽度（0 不换行）
opts.useEncodedAttributeStyle = false;
opts.recordUnknownFeature = false;   // 未知 feature 处理
```

### 2.7 加载 .ecore 元模型文件

`.ecore` 文件本身是 XMI 格式的 Ecore 元模型，可加载为 EPackage：

```cpp
emf::common::EPackageRegistry::instance().put(
    emf::ecore::EcorePackage::eNS_URI,
    emf::ecore::EcorePackage::instance());  // 注册 Ecore 自身

emf::xmi::XMIResource res(emf::common::URI("file:model.ecore"));
res.load();

// 根对象是 EPackage
auto* loadedPkg = dynamic_cast<emf::ecore::EPackage*>(res.getContents()[0]);
// loadedPkg->getEClassifiers() 获取所有 EClass
```

---

## 3. 模型创建与操作

### 3.1 初始化

任何模型操作前，必须初始化所有生成的 EPackage（把 EClass 注册到 EPackageRegistry）。
对 AUTOSAR 模型，调用 `init_all_autosar40_packages()`（见
`benchmark/cpp/init_all_packages.cpp`，420+ 个子包的 `initialize()`）。

```cpp
#include "emf/common/EPackageRegistry.h"
#include "emf/artop/runtime/AutosarResource.h"
// 生成代码的头文件
#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"
// ...

// 1. 初始化所有生成的 autosar40 子包
extern void init_all_autosar40_packages();
init_all_autosar40_packages();

// 2. 把根包注册到 nsURI（Loader 按此 nsURI 查找 EPackage）
emf::common::EPackageRegistry::instance().put(
    "http://autosar.org/schema/r4.0",
    emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::eINSTANCE);
```

### 3.2 创建模型对象

#### 类型化创建（推荐）

每个子包生成带 `createXxx()` 的 Factory：

```cpp
#include "autosar40/autosartoplevelstructure/AutosartoplevelstructureFactory.h"
#include "autosar40/genericstructure/generaltemplateclasses/arpackage/ARPackage.h"

using namespace emf::artop::autosar40;

auto* autosar = autosartoplevelstructure::AutosartoplevelstructureFactory::eINSTANCE->createAUTOSAR();
auto* pkg = genericstructure::generaltemplateclasses::arpackage::ArpackageFactory::eINSTANCE->createARPackage();
```

#### 反射式创建（按 EClass*）

根包 Factory 提供 `create(EClass*)`：

```cpp
#include "autosar40/Autosar40Factory.h"

auto* cls = Autosar40Package::eINSTANCE->getEClass("ARPackage");
auto* obj = Autosar40Factory::eINSTANCE->create(cls);  // 返回 EObject*
```

### 3.3 读写属性（codegen 类型化 API）

每个 EAttribute 生成 getter/setter，直接操作字段，无 `std::any` 装箱：

```cpp
// 单值 attribute（值类型，by value）
pkg->setShortName("myPkg");
std::string name = pkg->shortName();

pkg->setUuid("12345678-1234-1234-1234-123456789abc");
std::string uuid = pkg->uuid();

// Int64 / Bool 同理
// pkg->setSomeInt(42);
// int64_t v = pkg->someInt();
```

### 3.4 读写属性（反射式 API）

通过 `eGet/eSet(EStructuralFeature*)` 或 `eGet/eSet(int featureID)`，用 `std::any`
传递值（类型擦除，适合通用工具代码）：

```cpp
auto* cls = obj->eClass();
auto* sf = cls->getEStructuralFeature("shortName");

// 读
std::any v = obj->eGet(sf);
if (v.type() == typeid(std::string)) {
    std::cout << std::any_cast<std::string>(v) << std::endl;
}

// 写
obj->eSet(sf, std::any(std::string("newName")));

// 按 featureID（更快，避免按名查找）
int fid = obj->eStaticFeatureID("shortName");  // 静态查找，O(1)
std::any v2 = obj->eGet(fid);
```

#### 类型化 eGet（避免 std::any 装箱，高性能路径）

对单值 attribute/reference，codegen 生成了类型化虚函数，返回 `bool` 表示命中：

```cpp
int fid = obj->eStaticFeatureID("shortName");
std::string s;
if (obj->eGetString(fid, s)) {       // 命中单值 String 字段
    // s 已赋值，无需 std::any
}

bool b;
if (obj->eGetBool(fid, b)) { /* ... */ }

int64_t i;
if (obj->eGetInt64(fid, i)) { /* ... */ }

emf::common::EObject* target;
if (obj->eGetEObject(fid, target)) { /* 单值 reference，target 已赋值 */ }
```

未命中（多值/类型不匹配/featureID 不存在）返回 `false`，需 fallback 到 `eGet(fid)`。

#### isSet / unset

codegen 不生成 per-feature 的 `isSetXxx()` / `unsetXxx()` 方法。isSet 状态通过
protected `xxx_isSet_` 成员内部跟踪，外部用反射式 `eIsSet/eUnset`：

```cpp
if (obj->eIsSet(sf)) { /* 该 feature 已被显式设置过 */ }
obj->eUnset(sf);  // 恢复默认值
```

### 3.5 构建关系

#### 单值 reference（containment 或非 containment）

```cpp
auto* longName = ...createMultilanguageLongName();
longName->setShortName("MyLongName");
pkg->setLongName(longName);  // containment setter 自动维护 eContainer/eContainingFeature
// pkg->getLongName() == longName
// longName->eContainer() == pkg
```

#### 多值 reference（返回 `EList<T*>&`，无 setter）

```cpp
emf::common::EList<ARPackage*>& subPkgs = pkg->getArPackages();
subPkgs.add(childPkg1);
subPkgs.add(childPkg2);
// containment add 自动维护 eContainer
// childPkg1->eContainer() == pkg

size_t n = subPkgs.size();
ARPackage* first = subPkgs[0];
// 遍历
for (auto* child : subPkgs) {
    std::cout << child->shortName() << std::endl;
}
```

#### 跨包 reference 与 proxy

非 containment reference 指向其他 Resource 的对象时，loader 会创建 proxy
（`eIsProxy() == true`）。用 `eResolveProxy` 解析：

```cpp
emf::common::EObject* target = obj->getSomeReference();
if (target && target->eIsProxy()) {
    target = obj->eResolveProxy(target);  // 跨 Resource 解析
}
```

### 3.6 遍历模型树

```cpp
// 直接子对象（containment 子节点）
for (auto* child : obj->eContents()) {
    // ...
}

// 深度优先遍历整棵树
auto* it = root->eAllContents();
while (it->hasNext()) {
    auto* e = it->next();
    // ...
}
delete it;  // 调用方负责释放 iterator
```

### 3.7 加载与保存（arxml）

#### 直接用 AutosarXMLResource

```cpp
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/xmi/XMIResource.h"
#include <fstream>

// 加载
emf::common::URI uri("file:/path/to/input.arxml");
auto resource = std::make_shared<emf::artop::runtime::AutosarXMLResource>(uri);
resource->setSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd");

emf::xmi::XMIOptions opts;
std::ifstream ifs("/path/to/input.arxml", std::ios::binary);
resource->load(ifs, opts);

// 获取根对象
auto& contents = resource->getContents();
// contents[0] 是 AUTOSAR 根元素

// 保存
std::ofstream ofs("/path/to/output.arxml");
resource->save(ofs);
```

#### 用 ResourceSet（支持跨文件 reference 解析）

```cpp
#include "emf/artop/runtime/AutosarResourceSet.h"

emf::artop::runtime::AutosarResourceSet rs;
emf::common::URI uri1("file:/path/to/main.arxml");
emf::common::URI uri2("file:/path/to/library.arxml");

emf::common::Resource* r1 = rs.createResource(uri1);
emf::common::Resource* r2 = rs.createResource(uri2);
r1->load();  // 按 uri 打开文件
r2->load();  // library 加载后 rs 自动 indexLibrary()
// main 中的跨文件 reference 解析到 library 的对象
```

#### 字符串加载/保存（XMIResource）

```cpp
#include "emf/xmi/XMIResource.h"

emf::xmi::XMIResource res;
emf::xmi::XMIOptions opts;
res.loadFromString("<AUTOSAR>...</AUTOSAR>", opts);
std::string xml = res.saveToString(opts);
```

### 3.8 一个完整示例：创建并保存 AUTOSAR 模型

```cpp
#include "emf/common/EPackageRegistry.h"
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/xmi/XMIResource.h"
#include "autosar40/autosartoplevelstructure/AutosartoplevelstructureFactory.h"
#include "autosar40/autosartoplevelstructure/AUTOSAR.h"
#include "autosar40/genericstructure/generaltemplateclasses/arpackage/ARPackage.h"
#include "autosar40/genericstructure/generaltemplateclasses/identifiable/Referrable.h"
#include <fstream>

namespace ats = emf::artop::autosar40;
namespace ggc = emf::artop::autosar40::genericstructure::generaltemplateclasses;

int main() {
    // 1. 初始化（生产代码应调用 init_all_autosar40_packages() 覆盖所有子包）
    extern void init_all_autosar40_packages();
    init_all_autosar40_packages();
    emf::common::EPackageRegistry::instance().put(
        "http://autosar.org/schema/r4.0",
        ats::autosartoplevelstructure::AutosartoplevelstructurePackage::eINSTANCE);

    // 2. 创建对象
    auto* autosar = ats::autosartoplevelstructure::AutosartoplevelstructureFactory::eINSTANCE->createAUTOSAR();
    auto* rootPkg = ggc::arpackage::ArpackageFactory::eINSTANCE->createARPackage();
    rootPkg->setShortName("Root");
    auto* childPkg = ggc::arpackage::ArpackageFactory::eINSTANCE->createARPackage();
    childPkg->setShortName("Child");

    // 3. 构建树（containment add 自动维护 eContainer）
    autosar->getArPackages().add(rootPkg);
    rootPkg->getArPackages().add(childPkg);

    // 4. 放入 Resource 并保存
    emf::common::URI uri("file:/tmp/out.arxml");
    auto res = std::make_shared<emf::artop::runtime::AutosarXMLResource>(uri);
    res->setSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd");
    res->getContents().push_back(autosar);

    std::ofstream ofs("/tmp/out.arxml");
    res->save(ofs);
    return 0;
}
```

---

## 4. Notification API

对齐 Java `org.eclipse.emf.common.notify`。

### 4.1 基本通知

```cpp
#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"
#include "emf/common/EObject.h"

// 自定义 adapter
class MyAdapter : public emf::common::EAdapter {
public:
    void notifyChanged(const emf::common::Notification& n) override {
        std::cout << "Event " << (int)n.eventType()
                  << " on feature " << n.featureID() << std::endl;
    }
};

// 挂载到 EObject
MyAdapter adapter;
obj->eAdapters().push_back(&adapter);
obj->eSetDeliver(true);  // 启用通知分发

// eSet 会自动 eNotify
obj->eSet(nameFeature, std::any(std::string("new value")));
// → MyAdapter::notifyChanged 被调用
```

### 4.2 EContentAdapter（递归子树监听）

```cpp
#include "emf/common/ENotifier.h"

// EContentAdapter 挂载到根对象后，自动递归监听所有 containment 子对象
auto* contentAdapter = new emf::common::EContentAdapter();
root->eAdapters().push_back(contentAdapter);
// 此后 root 子树内任何 eSet/eGet 变更都会通知 contentAdapter
```

### 4.3 NotificationChain（批量通知 + 合并）

```cpp
#include "emf/common/Notification.h"

emf::common::NotificationChain chain;

// 累积通知，自动合并：
//   - SET + SET（同 notifier/feature）→ 合并，保留最早 oldValue
//   - ADD + REMOVE（同 notifier/feature/position/对象）→ 抵消
chain.add(emf::common::Notification(
    emf::common::Notification::EventType::SET, obj, nullptr, featureID,
    std::any(oldValue), std::any(value1), -1));
chain.add(emf::common::Notification(
    emf::common::Notification::EventType::SET, obj, nullptr, featureID,
    std::any(value1), std::any(value2), -1));  // 合并：oldValue 保留，newValue=value2

// 合并另一条链（vector<Notification>）
std::vector<emf::common::Notification> other;
other.emplace_back(...);
chain.merge(std::move(other));  // 逐条 add，复用合并语义

chain.dispatch();  // 逐个调 notifier.eNotify，标记 dispatched
```

---

## 5. Compare API

对齐 Java `org.eclipse.emf.compare`。

### 5.1 基本比较

```cpp
#include "emf/compare/Comparison.h"
#include "emf/compare/MatchEngine.h"
#include "emf/compare/DiffEngine.h"

// 比较 left 和 right 两个 EObject 树
emf::compare::Comparison comp = emf::compare::compare(leftRoot, rightRoot);

// 遍历 match
for (auto& m : comp.getMatches()) {
    std::cout << "Match: left=" << m.getLeft() << " right=" << m.getRight()
              << " kind=" << (int)m.getKind() << " sim=" << m.getSimilarity() << std::endl;
}

// 遍历 diff
for (auto* d : comp.getDifferences()) {
    std::cout << "Diff: kind=" << (int)d->getKind()
              << " type=" << (int)d->getType()
              << " feature=" << d->getAttributeName() << std::endl;
}
```

### 5.2 ID 匹配（跨文件场景）

```cpp
emf::compare::MatchEngine me;
// 注册 xmi:id 作 identifier，对象按 ID 严格匹配（不回退 proximity）
me.registerIdentifier(obj1, "id-001");
me.registerIdentifier(obj2, "id-001");
me.match(leftRoot, rightRoot, comp);
// → obj1 与 obj2 按 ID 配对为 IDENTICAL
```

### 5.3 Merge（应用差异）

```cpp
#include "emf/compare/MergeEngine.h"

emf::compare::MergeEngine merger;
// 将 right 端差异合并到 left（默认 LEFT_TO_RIGHT 方向）
merger.merge(comp, leftRoot);

// RequirementEngine 自动计算 Diff 依赖，MergeEngine 按拓扑序应用：
//   - ADD 子对象 依赖 ADD 父对象（先创建父容器）
//   - REFERENCE_CHANGE 依赖被引用对象的 ADD（先创建被引用对象）
//   - DELETE 父对象 依赖 DELETE 子对象（先删子）
//   - MOVE 依赖 ADD
```

---

## 6. Validation API

对齐 Java `org.eclipse.emf.ecore.util` 和 `org.eclipse.emf.validation`。

### 6.1 批量校验

```cpp
#include "emf/validation/Diagnostician.h"

// 全树 DFS 批量校验（运行所有 BATCH 模式约束）
auto diags = emf::validation::Diagnostician::validate(root,
    emf::validation::ConstraintMode::BATCH);

for (auto& d : diags) {
    std::cout << "Diag: sev=" << (int)d.severity()
              << " source=" << d.source()
              << " msg=" << d.message() << std::endl;
}
```

### 6.2 Live Validation（实时校验）

```cpp
#include "emf/validation/EValidator.h"
#include "emf/validation/LiveValidator.h"

emf::validation::EValidator validator;
// 注册默认约束（no_empty_name, no_null_required_ref）
validator.registerDefaultConstraints();

emf::validation::ValidationLiveAdapter live(validator);
live.addListener([](emf::common::EObject* target,
                    const std::vector<emf::common::Diagnostic>& diags) {
    for (auto& d : diags) {
        std::cerr << "Live validation: " << d.message() << std::endl;
    }
});
live.attach(root);  // 递归挂载到 containment 树

// 此后任何 eSet 触发通知，LiveValidator 增量校验变更对象（不递归子树）
obj->eSet(nameFeature, std::any(std::string("")));  // 触发 no_empty_name
// → listener 被调用

live.detach();
```

### 6.3 OCL 约束（注解加载）

```cpp
#include "emf/validation/AnnotationConstraintLoader.h"

// 从 EClass 的 OCL 注解加载约束（per-EClass 编译缓存）
emf::validation::EValidator validator;
emf::validation::AnnotationConstraintLoader::loadAll(validator, eClass);
// eClass 需有 EAnnotation(source="http://www.eclipse.org/emf/2002/Ecore/OCL",
//   details={("name_nonempty", "name != ''")})
```

### 6.4 AUTOSAR 业务约束

```cpp
#include "emf/validation/AutosarConstraints.h"

emf::validation::EValidator validator;
// 注册 5 类核心 AUTOSAR 约束（反射式，对 Referrable/Identifiable 派生类生效）
emf::validation::registerAutosarConstraints(validator);
// 注册的约束：
//   - short_name_non_empty / short_name_unique_in_parent
//   - uuid_non_empty
//   - category_required
//   - no_unresolved_proxy
// 支持 BATCH + LIVE 双模式
```

### 6.5 自定义约束（lambda）

```cpp
emf::validation::EValidator validator;

// 最简方式：注册 lambda 约束（反射读取 feature）
validator.registerConstraint(
    [](emf::common::EObject* obj) -> bool {
        auto* sf = obj->eClass()->getEStructuralFeature("version");
        if (!sf) return true;  // 不适用
        auto v = obj->eGet(sf);
        if (v.type() == typeid(std::string)) {
            return !std::any_cast<std::string>(v).empty();
        }
        return true;
    },
    "my.version_non_empty", "VersionNonEmpty",
    "version must not be empty",
    emf::validation::Severity::ERROR,
    emf::validation::ConstraintMode::BATCH);
```

---

## 7. 性能测试

### 7.1 Benchmark 套件

`/workspace/benchmark/` 下提供统一组织的 benchmark 套件，覆盖 notification/compare/
validation 三模块及 C++ vs Java 对比。详见 `benchmark/README.md`。

### 7.2 构建 Benchmark

```bash
# 前置：emf-cpp 库已构建
cd /workspace/cpp/emf-cpp/build && cmake --build . -j$(nproc)

# 构建 benchmark（首次约 10 分钟，编译 4200+ AUTOSAR 模型文件）
cd /workspace/benchmark
bash build_cpp_benchmark.sh
```

产物：`benchmark/cpp/` 下 5 个二进制（arxml_benchmark, ecore_xmi_benchmark,
notificationbenchmark, comparebenchmark, validationbenchmark）。

### 7.3 运行 Benchmark

```bash
cd /workspace/benchmark

# C++ 全套（arxml + notification + compare + validation）
./run_benchmark.sh all

# 单独运行某类
./run_benchmark.sh notification [input.arxml] [iterations]
./run_benchmark.sh compare      [input.arxml] [iterations]
./run_benchmark.sh validation   [input.arxml] [iterations]
./run_benchmark.sh arxml        [input.arxml] [iterations]
./run_benchmark.sh ecore-xmi    [input.xmi]   [iterations]

# C++ vs Java 对比（需 Java ARTOP 环境）
./run_benchmark.sh cpp-vs-java
```

默认输入 `benchmark/data/large_96m.arxml`（96MB），3 轮取均值（排除首轮 warmup）。

### 7.4 当前性能基线

详见 `benchmark/results/BASELINE.md`。摘要：

| Benchmark | 输入规模 | 关键指标 | 耗时 |
|-----------|----------|----------|------|
| notification | 57153 对象 | eNotify 吞吐 | 0.02 us/notify |
| compare | 1494 对象 (identical) | match+diff | 152 ms |
| validation (中等) | 1494 对象 | batch validate | 35 ms (9 diags) |
| validation (大) | 57153 对象 | batch validate | 4500 ms (8051 diags) |

### 7.5 回归检查

修改 notification/compare/validation 代码后，运行对应 benchmark 确认无退化：

1. **Notification**：eNotify 0.02 us/notify 不退化，NotificationChain 3ms 不退化
2. **Compare**：中等文件 152ms 不退化，diffs=0（identical 模型）正确
3. **Validation**：大文件 batch 4500ms 不退化，diagnostics=8051 数量正确

若耗时增加 >20% 或 diagnostic/diff 数量变化，需排查回归原因。

---

## 8. 相关文档

- `doc/ARCHITECTURE.md` — 架构设计、模块分层、与 Java 对齐关系、性能优化栈
- `doc/WIKI.md` — 开发 wiki（设计决策、FAQ）
- `benchmark/README.md` — Benchmark 套件详细使用说明
- `benchmark/results/BASELINE.md` — 性能基线历史数据
