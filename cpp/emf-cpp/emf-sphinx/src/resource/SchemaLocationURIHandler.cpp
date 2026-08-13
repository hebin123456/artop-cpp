// SchemaLocationURIHandler.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.SchemaLocationURIHandler
// 简化版：只实现 schema location 解析相关核心逻辑（不依赖 Eclipse Platform）
#include "emf/sphinx/resource/SchemaLocationURIHandler.h"
#include "emf/xmi/XMIResource.h"
#include <sstream>

namespace emf::sphinx::resource {

// 解析 "ns1 uri1 ns2 uri2" 形式的 xsi:schemaLocation 字符串
// 对齐 Java: xsi:schemaLocation 内容是 "namespaceUri systemId" 对组成
std::map<std::string, std::string> SchemaLocationURIHandler::parseSchemaLocation(const std::string& schemaLoc) const {
    std::map<std::string, std::string> result;
    if (schemaLoc.empty()) return result;
    std::istringstream iss(schemaLoc);
    std::vector<std::string> tokens;
    std::string tok;
    while (iss >> tok) tokens.push_back(tok);
    // 必须成对出现
    if (tokens.size() % 2 != 0) return result;
    for (size_t i = 0; i + 1 < tokens.size(); i += 2) {
        result[tokens[i]] = tokens[i + 1];
    }
    return result;
}

// 从 XMIResource 中提取 schemaLocation 内容
// 对齐 Java: getSchemaLocation(XMIResource)
// XMIResource 在 load 时将 xsi:schemaLocation 属性存为字符串
std::string SchemaLocationURIHandler::getSchemaLocation(const emf::xmi::XMIResource* res) const {
    if (!res) return "";
    return res->getXSISchemaLocation();
}

}  // namespace emf::sphinx::resource
