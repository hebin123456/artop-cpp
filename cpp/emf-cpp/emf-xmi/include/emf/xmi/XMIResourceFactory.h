// emf::xmi —— XMIResource 工厂
// 对齐 Java: org.eclipse.emf.ecore.xmi.XMIResourceFactory
#pragma once

#include "XMIResource.h"
#include "emf/common/URI.h"
#include <functional>
#include <string>
#include <unordered_map>
#include <memory>

namespace emf::xmi {

// 工厂函数签名：给定 URI，返回一个 XMIResource*
using XMIResourceCreator = std::function<std::unique_ptr<XMIResource>(const emf::common::URI&)>;

// XMIResourceFactory
// 负责按 URI 后缀（.xmi / .ecore）创建合适的 XMIResource
class XMIResourceFactory {
public:
    XMIResourceFactory() = default;
    ~XMIResourceFactory() = default;

    // 用默认构造器创建 XMIResource
    std::unique_ptr<XMIResource> createResource(const emf::common::URI& uri) const;

    // 静态：注册后缀 -> factory。extension 不含点，例如 "xmi", "ecore"
    static void registerFactory(const std::string& extension, XMIResourceCreator creator);

    // 静态：注册默认后缀 (.xmi, .ecore) 到 XMIResourceFactory
    static void registerDefaults();

    // 静态：根据 uri 后缀查找并创建资源；若未注册则 fallback 到 XMIResource
    static std::unique_ptr<XMIResource> createResourceFor(const emf::common::URI& uri);

private:
    static std::unordered_map<std::string, XMIResourceCreator>& registry();
};

}  // namespace emf::xmi
