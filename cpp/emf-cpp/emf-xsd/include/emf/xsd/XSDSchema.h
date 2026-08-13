// EMF XSD: XSDSchema 实现
// 对齐 Java: org.eclipse.xsd.XSDSchema
#pragma once

#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EList.h"
#include <string>
#include <unordered_map>
#include <vector>

namespace emf::xsd {

class XSDSchemaCompositor;

class XSDSchema : public emf::ecore::EObjectImpl {
public:
    XSDSchema() = default;
    ~XSDSchema() override = default;

    // 对齐 Java: EObjectImpl.eInvoke 默认实现（XSDSchema 直接继承 EObjectImpl，
    // 不经过 XSDConcreteComponent，所以这里单独 override）。
    std::any eInvoke(emf::ecore::EOperation* operation,
                     const std::vector<std::any>& arguments) override {
        if (!operation) return std::any{};
        return operation->invoke(this, arguments);
    }

    // targetNamespace
    const std::string& getTargetNamespace() const { return targetNamespace_; }
    void setTargetNamespace(const std::string& ns) { targetNamespace_ = ns; }

    // element declarations (containment)
    emf::common::EList<emf::common::EObject*>& getElementDeclarations() { return elementDeclarations_; }
    const emf::common::EList<emf::common::EObject*>& getElementDeclarations() const { return elementDeclarations_; }
    void addElementDeclaration(emf::common::EObject* elem);

    // type definitions (containment)
    emf::common::EList<emf::common::EObject*>& getTypeDefinitions() { return typeDefinitions_; }
    const emf::common::EList<emf::common::EObject*>& getTypeDefinitions() const { return typeDefinitions_; }
    void addTypeDefinition(emf::common::EObject* type);

    // attribute declarations (containment)
    emf::common::EList<emf::common::EObject*>& getAttributeDeclarations() { return attributeDeclarations_; }
    const emf::common::EList<emf::common::EObject*>& getAttributeDeclarations() const { return attributeDeclarations_; }
    void addAttributeDeclaration(emf::common::EObject* attr);

    // attribute group definitions (containment)
    emf::common::EList<emf::common::EObject*>& getAttributeGroupDefinitions() { return attributeGroupDefinitions_; }
    const emf::common::EList<emf::common::EObject*>& getAttributeGroupDefinitions() const { return attributeGroupDefinitions_; }
    void addAttributeGroupDefinition(emf::common::EObject* group);

    // model group definitions (containment)
    emf::common::EList<emf::common::EObject*>& getModelGroupDefinitions() { return modelGroupDefinitions_; }
    const emf::common::EList<emf::common::EObject*>& getModelGroupDefinitions() const { return modelGroupDefinitions_; }
    void addModelGroupDefinition(emf::common::EObject* group);

    // imports
    emf::common::EList<emf::common::EObject*>& getImports() { return imports_; }
    const emf::common::EList<emf::common::EObject*>& getImports() const { return imports_; }
    void addImport(emf::common::EObject* imp);

    // includes
    emf::common::EList<emf::common::EObject*>& getIncludes() { return includes_; }
    const emf::common::EList<emf::common::EObject*>& getIncludes() const { return includes_; }
    void addInclude(emf::common::EObject* inc);

    // annotations (containment)
    emf::common::EList<emf::common::EObject*>& getAnnotations() { return annotations_; }
    const emf::common::EList<emf::common::EObject*>& getAnnotations() const { return annotations_; }
    void addAnnotation(emf::common::EObject* ann);

    // ===== P5: incorporate 协议（对齐 Java: XSDSchema.incorporate）=====
    // 哪些 XSDSchemaCompositor 引用了本 schema（反向注册）
    emf::common::EList<emf::common::EObject*>& getReferencingDirectives() { return referencingDirectives_; }
    const emf::common::EList<emf::common::EObject*>& getReferencingDirectives() const { return referencingDirectives_; }

    // 历史 incorporate 的 schema 版本（Java 端用于追溯原始版本）
    emf::common::EList<emf::common::EObject*>& getIncorporatedVersions() { return incorporatedVersions_; }
    const emf::common::EList<emf::common::EObject*>& getIncorporatedVersions() const { return incorporatedVersions_; }

    // pending schema location：用于循环依赖时的延迟解析
    emf::common::EList<emf::common::EObject*>& getPendingSchemaLocations() { return pendingSchemaLocations_; }
    const emf::common::EList<emf::common::EObject*>& getPendingSchemaLocations() const { return pendingSchemaLocations_; }

    // 对齐 Java: XSDSchemaImpl.incorporate(XSDSchemaCompositor) line 3284
    //  1. setIncorporatedSchema(this)  on compositor
    //  2. addCompositor to referencingDirectives_
    //  3. target namespace fallback (if composor's schema has empty targetNamespace, use this)
    //  4. resolve pending schema locations referencing this schema
    void incorporate(XSDSchemaCompositor* compositor);

    // 找到第一个 specific XSDSchema (i.e., not null incorporatedSchema_)
    XSDSchema* getOriginalVersion() const;

    // 跨文件加载（按 schemaLocation 字符串）。返回 null 表示无法解析。
    // 对齐 Java: XSDSchema.resolveSchema(String, XSDSchema)
    // 默认实现不实际加载文件 —— 派生类或外部 registry 负责把 XSDResource 注册进来
    // 后调用 incorporate() 完成。
    virtual XSDSchema* resolveSchema(const std::string& /*schemaLocation*/) { return nullptr; }

    // 跨文件加载：把外部 XSD 文件加载为 XSDSchema（不通过 compositor）
    // 静态助手：默认 throw；可由宿主环境 override
    // 这里用 free function XSDResource::loadSchema(uri) 提供，详见 XSDResource.h

    // schema for import/include
    const std::string& getSchemaLocation() const { return schemaLocation_; }
    void setSchemaLocation(const std::string& loc) { schemaLocation_ = loc; }

    // prefix -> namespace
    const std::unordered_map<std::string, std::string>& getQNamePrefixToNamespaceMap() const { return prefixToNs_; }
    std::unordered_map<std::string, std::string>& getQNamePrefixToNamespaceMap() { return prefixToNs_; }

    // EMF 接口
    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    std::string targetNamespace_;
    std::string schemaLocation_;
    emf::common::EList<emf::common::EObject*> elementDeclarations_;
    emf::common::EList<emf::common::EObject*> typeDefinitions_;
    emf::common::EList<emf::common::EObject*> attributeDeclarations_;
    emf::common::EList<emf::common::EObject*> attributeGroupDefinitions_;
    emf::common::EList<emf::common::EObject*> modelGroupDefinitions_;
    emf::common::EList<emf::common::EObject*> imports_;
    emf::common::EList<emf::common::EObject*> includes_;
    emf::common::EList<emf::common::EObject*> annotations_;
    emf::common::EList<emf::common::EObject*> referencingDirectives_;
    emf::common::EList<emf::common::EObject*> incorporatedVersions_;
    emf::common::EList<emf::common::EObject*> pendingSchemaLocations_;
    std::unordered_map<std::string, std::string> prefixToNs_;
};

}  // namespace emf::xsd
