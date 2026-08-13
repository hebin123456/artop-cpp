// XcoreResource.h
// 对齐 Java: org.eclipse.emf.ecore.xcore.resource.XcoreResource
//
// XcoreResource 加载 .xcore 文件，load 时即派生并挂载一个 EPackage
// （这是 Xcore 与普通 Xtext Resource 的关键差异）。
// 同时提供 XcoreResourceFactory / XcoreStandaloneSetup。
#pragma once

#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/xcore/XcoreAst.h"
#include <memory>
#include <string>
#include <istream>

namespace emf::ecore::xcore {

// 对齐 Java: org.eclipse.emf.ecore.xcore.resource.XcoreResource
//   - load(is) 解析 .xcore → AST → EPackage（派生）
//   - getEPackage() 返回派生出的 EPackage
class XcoreResource : public ::emf::common::Resource {
public:
    explicit XcoreResource(const ::emf::common::URI& uri)
        : ::emf::common::Resource(uri) {}

    // 解析输入流，构造 AST 并派生 EPackage。
    // 失败抛 XcoreParseException。
    void load(std::istream& is) override;

    // 重载：从内部 uri 读取文件并 load
    void load() override;

    // 派生出的 EPackage（load 后非空）
    ::emf::ecore::EPackage* getEPackage() const { return derivedPackage_; }

    // 原始 AST（load 后非空）
    const std::shared_ptr<XPackage>& getXPackage() const { return xpackage_; }

    // 派生出的 GenModel XML 文本（load 后非空）。
    // 对齐 Java XcoreResource 在加载时同时派生 .genmodel 的行为。
    const std::string& getGenModel() const { return genModel_; }

    // 派生 EPackage 会通过基类 addToContents() 注册为 contents()[0]，
    // 对齐 Java XcoreResource 把 EPackage 作为 contents()[0]。
    // 用基类 getContents() 访问即可。

    void save(std::ostream& os) override;

private:
    std::shared_ptr<XPackage> xpackage_;
    ::emf::ecore::EPackage* derivedPackage_ = nullptr;
    std::string genModel_;  // 派生出的 GenModel XML
};

// 对齐 Java: org.eclipse.emf.ecore.xcore.resource.XcoreResourceFactory
//   - createResource(URI) 返回 XcoreResource
// 注：与 emf::xmi::XMIResourceFactory 风格一致，独立类不继承公共基类。
class XcoreResourceFactory {
public:
    XcoreResourceFactory() = default;

    // 创建 XcoreResource（调用方持有所有权）
    XcoreResource* createResource(const ::emf::common::URI& uri) const {
        return new XcoreResource(uri);
    }

    // 静态：注册 .xcore 后缀到内部注册表
    static void registerFactory();
    // 静态：按 URI 后缀创建（仅 .xcore）；其它返回 nullptr
    static XcoreResource* createResourceFor(const ::emf::common::URI& uri);
};

// 对齐 Java: org.eclipse.emf.ecore.xcore.XcoreStandaloneSetup
//   - createInjectorAndDoEMFRegistration(): 注册 ResourceFactory
class XcoreStandaloneSetup {
public:
    // 注册 .xcore 扩展名到 XcoreResourceFactory，并初始化 EcorePackage。
    // 幂等，多次调用安全。
    static void setup();
};

}  // namespace emf::ecore::xcore
