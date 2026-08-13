// EMFEditPlugin.h
// 对齐 Java: org.eclipse.emf.edit.EMFEditPlugin
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include <string>

namespace emf::edit {

// EMF Edit 框架的全局 plugin 单例（对齐 Java EMFEditPlugin）
// Java 端用 Plugin 机制读 EMFEditPlugin.properties 资源束；C++ 端简化
// 为静态字符串表 + initialize()。
class EMFEditPlugin {
public:
    static EMFEditPlugin* instance();
    static void initialize();
    static void shutdown();

    // 资源束（Java 端用 NLS 翻译；C++ 端用静态字符串表 + override 接口）
    virtual std::string getString(const std::string& key, bool /*translate*/ = true) const;
    virtual std::string getString(const std::string& key, const std::string& fallback) const;

    // 图片描述符（对齐 Java getImage）
    virtual void* getImage(const std::string& key);  // Java 端返 Image；C++ 占位返 void*

private:
    static EMFEditPlugin* s_instance_;
};

}  // namespace emf::edit
