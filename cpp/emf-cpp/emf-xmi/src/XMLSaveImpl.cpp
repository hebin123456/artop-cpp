// XMLSaveImpl.cpp —— XMLSave 抽象类的默认实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl
// 委托给 free function saveInto()（XMISaver.cpp 提供）
#include "emf/xmi/XMLLoad.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"

namespace emf::xmi {

// free function 在 XMISaver.cpp 中定义
void saveInto(std::ostream& os, const XMIResource& res, const XMIOptions& opts);

void XMLSaveImpl::save(const XMIResource* resource, std::ostream& output, const XMIOptions& options) {
    if (!resource) return;
    saveInto(output, *resource, options);
}

}  // namespace emf::xmi
