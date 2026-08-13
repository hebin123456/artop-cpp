// XMLLoadImpl.cpp —— XMLLoad 抽象类的默认实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl
// 委托给 free function loadInto()（XMILoader.cpp 提供）
#include "emf/xmi/XMLLoad.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"

namespace emf::xmi {

// free function 在 XMILoader.cpp 中定义
void loadInto(std::istream& is, XMIResource& res, const XMIOptions& opts);

void XMLLoadImpl::load(XMIResource* resource, std::istream& input, const XMIOptions& options) {
    if (!resource) return;
    loadInto(input, *resource, options);
}

}  // namespace emf::xmi
