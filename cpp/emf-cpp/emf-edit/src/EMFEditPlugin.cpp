// EMFEditPlugin.cpp
// 对齐 Java: org.eclipse.emf.edit.EMFEditPlugin
// 状态: 框架骨架（仅占位实现）
#include "emf/edit/EMFEditPlugin.h"

namespace emf::edit {

EMFEditPlugin* EMFEditPlugin::s_instance_ = nullptr;

EMFEditPlugin* EMFEditPlugin::instance() {
    if (!s_instance_) s_instance_ = new EMFEditPlugin();
    return s_instance_;
}

void EMFEditPlugin::initialize() {
    if (!s_instance_) s_instance_ = new EMFEditPlugin();
    // TODO: 加载 properties 资源束
}

void EMFEditPlugin::shutdown() {
    delete s_instance_;
    s_instance_ = nullptr;
}

std::string EMFEditPlugin::getString(const std::string& key, bool /*translate*/) const {
    // TODO: 翻译表
    return key;
}

std::string EMFEditPlugin::getString(const std::string& key, const std::string& fallback) const {
    // TODO: 翻译表 + fallback
    (void)key;
    return fallback;
}

void* EMFEditPlugin::getImage(const std::string& /*key*/) {
    return nullptr;  // Java 端返 Image；C++ 占位 null
}

}  // namespace emf::edit
