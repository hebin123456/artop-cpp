// EditUtil.cpp
// 对齐 Java: org.eclipse.emf.edit.util.EditUtil
// 状态: 框架骨架（仅占位实现）
#include "emf/edit/util/EditUtil.h"

namespace emf::edit::util {

std::string EditUtil::getText(std::any value) {
    // TODO: 委托给 IItemLabelProvider
    return value.has_value() ? "<not implemented>" : "";
}

std::string EditUtil::getString(std::any value) {
    return getText(std::move(value));
}

void* EditUtil::findElementByName(const std::vector<std::any>& /*elements*/, const std::string& /*name*/) {
    return nullptr;  // TODO
}

bool EditUtil::isEditable(emf::common::EObject* /*object*/) {
    return false;  // TODO
}

bool EditUtil::isReadOnly(emf::common::EObject* /*object*/) {
    return false;  // TODO
}

bool EditUtil::isSet(emf::common::EObject* /*object*/) {
    return false;  // TODO
}

}  // namespace emf::edit::util
