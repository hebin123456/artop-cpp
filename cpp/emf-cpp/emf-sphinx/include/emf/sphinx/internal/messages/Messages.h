// Messages.h
// 对齐 Java org.eclipse.sphinx.emf.internal.messages.Messages
// 国际化字符串（headless 版直接返回常量）
#pragma once

#include <string>

namespace emf::sphinx::internal::messages {

class Messages {
public:
    static std::string msg_xmlWellformednessProblemFormatString() { return "Malformed XML: {0}"; }
    static std::string msg_xmlValidityProblemFormatString() { return "Invalid XML: {0}"; }

    static std::string error_problemOccurredWhenSavingResource() { return "Problem occurred when saving resource: {0}"; }
    static std::string error_problemOccurredWhenLoadingResource() { return "Problem occurred when loading resource: {0}"; }
};

}  // namespace emf::sphinx::internal::messages
