// CommandStackListener.h
// 对齐 Java org.eclipse.emf.common.command.CommandStackListener
// Listener 已内嵌在 CommandStack 中作为 Listener 类型；
// 这里仅给出类型别名以保持与 Java 命名一致。
#pragma once

namespace emf::common::command {

class CommandStack;
using CommandStackListener = CommandStack::Listener;

}  // namespace emf::common::command
