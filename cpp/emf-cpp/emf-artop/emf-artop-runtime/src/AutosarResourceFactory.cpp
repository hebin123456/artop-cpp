// emf::artop::runtime —— AutosarResourceFactory / AutosarXMLResourceFactory 实现
#include "emf/artop/runtime/AutosarResourceFactory.h"

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResourceFactory.h"

#include <any>
#include <fstream>
#include <sstream>

namespace emf::artop::runtime {

void AutosarResourceFactory::initResource(emf::xmi::XMIResource* res) {
    if (!res) return;
    initDefaultOptions(res);
    if (autosarRelease_) {
        // 把 release descriptor 的 schema location 注入到 resource
        res->setXSISchemaLocation(autosarRelease_->getSchemaLocation());
    }
}

void AutosarResourceFactory::initDefaultOptions(emf::xmi::XMIResource* res) {
    if (!res) return;
    // 对齐 Java AutosarResourceFactoryImpl.initDefaultOptions:
    //   - XMLResource.OPTION_ENCODING = "UTF-8"
    //   - XMLResource.OPTION_SCHEMA_LOCATION = true
    //   - XMIResource.OPTION_USE_XMI_TYPE = false
    //   - XMIResource.OPTION_LAX_FEATURE_PROCESSING = true
    //   - XMLResource.OPTION_EXTENDED_META_DATA = true
    //   - XMLResource.OPTION_RECORD_UNKNOWN_FEATURE = true
    //   - XMLResource.OPTION_KEEP_DEFAULT_CONTENT = true
    //   - XMLResource.OPTION_XML_MAP = ... (factory 内的 schemaLocationCatalog)
    // C++ 端当前没有完整 XMIOptions 系统，这里先在 resource 上 set 一些最常用的
    res->setEncoding("UTF-8");
    res->setXmiVersion("2.0");
}

// ===== AutosarXMLResourceFactory =====

std::unique_ptr<emf::common::Resource> AutosarXMLResourceFactory::createResource(const emf::common::URI& uri) {
    if (resourceCreator_) {
        return resourceCreator_(uri);
    }
    // 默认行为：返回一个新的 AutosarXMLResource
    auto r = std::make_unique<AutosarXMLResource>(uri);
    if (autosarRelease_) {
        r->setAutosarRelease(autosarRelease_);
        r->setSchemaLocation(autosarRelease_->getSchemaLocation());
    }
    return r;
}

std::unordered_map<std::string, std::string> AutosarXMLResourceFactory::createSchemaLocationCatalog() {
    std::unordered_map<std::string, std::string> cat;
    if (!autosarRelease_) return cat;
    // 注册 r4.0 命名空间
    cat[autosarRelease_->getBaseNamespace()] = autosarRelease_->getSchemaLocation();
    // xsi namespace
    cat["http://www.w3.org/XML/1998/namespace"] = "xml.xsd";
    cat["http://www.w3.org/2001/XMLSchema-instance"] = "XML.xsd";
    return cat;
}

void AutosarXMLResourceFactory::initSchemaLocationBaseURIs() {
    // 对齐 Java: 注册 plugin 内置 schema 路径
    // org.eclipse.sphinx.emf.resource.SchemaLocationURIHandler.addSchemaLocationBaseURI(...)
    // C++ 端在 schemaLocationCatalog_ 内集中管理，这里不做具体 I/O
    // 用户调用 createSchemaLocationCatalog() 获取
}

// ===== 元模型注册 =====
// 对齐 Java: Autosar40Package.eINSTANCE 静态初始化块
// Java 端在插件启动时把 nsURI="http://autosar.org/schema/r4.0" 注册到 EPackage.Registry，
// C++ 端通过动态加载 .ecore 文件达到同等效果（避免生成 740+ 静态 subpackage）。

emf::ecore::EPackage* AutosarResourceFactory::registerMetamodelFromEcore(const std::string& ecorePath) {
    // 确保 EMF 核心已初始化
    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    // 读取 ecore 文件
    std::ifstream ifs(ecorePath);
    if (!ifs.is_open()) return nullptr;

    // 使用 static ResourceSet 保活加载的 EPackage（避免局部 ResourceSet 析构后
    // EPackage 被释放，导致 EPackageRegistry 持有悬空指针）
    static emf::xmi::XMIResourceSet s_rs;
    auto* res = s_rs.createResource(emf::common::URI::createFileURI(ecorePath));
    auto* xres = dynamic_cast<emf::xmi::XMIResource*>(res);
    if (!xres) return nullptr;
    xres->load(ifs);

    // ecore 文档加载后，XMILoader 已把 EPackage 注册到 EPackageRegistry
    if (xres->getContents().empty()) return nullptr;
    auto* root = xres->getContents()[0];
    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(root);
    if (!pkg) return nullptr;
    const std::string& nsURI = pkg->getNsURI();
    if (nsURI.empty()) return nullptr;
    // 为 EPackage 创建 EFactory（动态加载的 ecore 不会自动带 factory）
    // 对齐 Java: EPackageImpl.initEFactory() —— EFactoryImpl 能为任意 EClass 创建 DynamicEObject
    if (!pkg->getEFactoryInstance()) {
        auto* factory = emf::ecore::EcoreFactory::instance().createEFactory();
        factory->setEPackage(pkg);
        pkg->setEFactoryInstance(factory);
    }
    // 确保注册（幂等）
    emf::common::EPackageRegistry::instance().put(nsURI, pkg);
    return pkg;
}

emf::ecore::EPackage* AutosarResourceFactory::registerDefaultAutosar40Metamodel() {
    // 幂等：已注册则直接返回
    static const std::string kAutosarNsURI = "http://autosar.org/schema/r4.0";
    if (auto* existing = emf::common::EPackageRegistry::instance().get(kAutosarNsURI)) {
        return dynamic_cast<emf::ecore::EPackage*>(existing);
    }
    // 内置 ecore 路径：源码树 model/autosar40.ecore
    // 编译时通过 EMF_ARTOP_AUTOSAR40_ECORE 宏注入绝对路径（见 CMakeLists.txt）
#ifdef EMF_ARTOP_AUTOSAR40_ECORE
    return registerMetamodelFromEcore(EMF_ARTOP_AUTOSAR40_ECORE);
#else
    return nullptr;
#endif
}

}  // namespace emf::artop::runtime
