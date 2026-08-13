// SchemaLocationURIHandler.h
// 对齐 Java org.eclipse.sphinx.emf.resource.SchemaLocationURIHandler
// xsi:schemaLocation 解析（用于代理的 schema 定位）
#pragma once

#include "emf/common/URI.h"
#include "emf/xmi/XMIResource.h"
#include <string>
#include <vector>
#include <map>

namespace emf::sphinx::resource {

class SchemaLocationURIHandler {
public:
    SchemaLocationURIHandler() = default;
    virtual ~SchemaLocationURIHandler() = default;

    // 解析 xsi:schemaLocation 字符串
    virtual std::map<std::string, std::string> parseSchemaLocation(const std::string& schemaLoc) const;

    // 取得 schema location
    virtual std::string getSchemaLocation(const emf::xmi::XMIResource* res) const;
};

}  // namespace emf::sphinx::resource
