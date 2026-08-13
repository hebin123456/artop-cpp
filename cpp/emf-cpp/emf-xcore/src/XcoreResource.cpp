// XcoreResource.cpp — XcoreResource 实现
// 对齐 Java: org.eclipse.emf.ecore.xcore.resource.XcoreResource
//
// load 行为对齐 Java：
//   1. 读取输入流文本
//   2. XcoreParser 解析为 XPackage AST
//   3. XcoreGenerator 派生为 EPackage
//   4. 派生 EPackage 加入 contents()（contents()[0]）
//   5. 注册到 EPackageRegistry（按 nsURI）
#include "emf/ecore/xcore/XcoreResource.h"
#include "emf/ecore/xcore/XcoreParser.h"
#include "emf/ecore/xcore/XcoreGenerator.h"
#include "emf/common/EPackageRegistry.h"
#include <sstream>
#include <fstream>

namespace emf::ecore::xcore {

void XcoreResource::load(std::istream& is) {
    std::ostringstream oss;
    oss << is.rdbuf();
    std::string source = oss.str();

    // 解析
    xpackage_ = XcoreParser::parse(source);

    // 派生 EPackage
    XcoreGenerator gen;
    derivedPackage_ = gen.generate(xpackage_);

    // 派生 GenModel XML（对齐 Java XcoreResource 加载时同时派生 .genmodel）
    genModel_ = gen.generateGenModel(xpackage_);

    // 加入 contents（对齐 Java XcoreResource：EPackage 作为 contents()[0]）
    addToContents(derivedPackage_);

    // 注册到全局 EPackageRegistry（按 nsURI）
    if (derivedPackage_) {
        ::emf::common::EPackageRegistry::instance().put(derivedPackage_->getNsURI(), derivedPackage_);
    }

    setLoaded(true);
}

void XcoreResource::load() {
    // 复用基类的按 URI 加载文件逻辑
    ::emf::common::Resource::load();
}

void XcoreResource::save(std::ostream& os) {
    // Xcore 是源码格式，不支持回写（对齐 Java：XcoreResource 只读）
    (void)os;
}

// ===== XcoreResourceFactory =====

// 静态注册：把 .xcore 后缀映射到本 factory。
// 这里用独立注册表（对齐 XMIResourceFactory 的风格）。
namespace {
std::unordered_map<std::string, std::function<XcoreResource*(const ::emf::common::URI&)>>&
factoryRegistry() {
    static std::unordered_map<std::string, std::function<XcoreResource*(const ::emf::common::URI&)>> r;
    return r;
}
}  // namespace

void XcoreResourceFactory::registerFactory() {
    static bool registered = false;
    if (registered) return;
    registered = true;
    factoryRegistry()["xcore"] = [](const ::emf::common::URI& uri) {
        return new XcoreResource(uri);
    };
}

XcoreResource* XcoreResourceFactory::createResourceFor(const ::emf::common::URI& uri) {
    // 取后缀
    auto str = uri.toString();
    auto dot = str.find_last_of('.');
    if (dot == std::string::npos) return nullptr;
    std::string ext = str.substr(dot + 1);
    auto& r = factoryRegistry();
    auto it = r.find(ext);
    if (it == r.end()) return nullptr;
    return it->second(uri);
}

// ===== XcoreStandaloneSetup =====

void XcoreStandaloneSetup::setup() {
    // 初始化 EcorePackage（Xcore 依赖 Ecore 元模型）
    ::emf::ecore::EcorePackage::initialize();
    // 注册 .xcore ResourceFactory
    XcoreResourceFactory::registerFactory();
}

}  // namespace emf::ecore::xcore
