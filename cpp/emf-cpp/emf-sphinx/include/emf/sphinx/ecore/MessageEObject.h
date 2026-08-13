// MessageEObject.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.MessageEObjectImpl
//
// 占位 EObject：在模型尚未完全加载时作为真实对象的临时替代，
// 持有一条可向用户展示的提示消息。toString() 返回该消息。
#pragma once

#include "emf/common/EObject.h"
#include <string>

namespace emf::sphinx::ecore {

class MessageEObject : public emf::common::EObjectImpl {
public:
    explicit MessageEObject(std::string message);
    ~MessageEObject() override;

    const std::string& getMessage() const { return message_; }
    void setMessage(std::string message) { message_ = std::move(message); }

    // 占位对象无真实 EClass
    emf::ecore::EClass* eClass() const override { return nullptr; }

    // 对齐 Java toString()
    std::string toString() const { return message_; }

private:
    std::string message_;
};

}  // namespace emf::sphinx::ecore
