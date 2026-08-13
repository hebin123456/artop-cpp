// EMF Common: EPackage / EFactory 接口
// 对齐 org.eclipse.emf.ecore.EPackage, EFactory (Java)
#pragma once

#include "EObject.h"
#include <string>
#include <vector>

namespace emf::ecore {
class EClass;
class EClassifier;
class EPackage;
class EFactory;
}

namespace emf::common {

class EFactory : virtual public EObject {
public:
    virtual emf::ecore::EPackage* getEPackage() const = 0;
    virtual EObject* create(const emf::ecore::EClass* eClass) const = 0;
    virtual std::any createFromString(const emf::ecore::EClassifier* classifier, const std::string& literal) const = 0;
    virtual std::string convertToString(const emf::ecore::EClassifier* classifier, const std::any& value) const = 0;
};

class EPackage : virtual public EObject {
public:
    virtual const std::string& getName() const = 0;
    virtual const std::string& getNsURI() const = 0;
    virtual const std::string& getNsPrefix() const = 0;
    virtual emf::ecore::EFactory* getEFactoryInstance() const = 0;
    virtual emf::ecore::EClassifier* getEClassifier(const std::string& name) const = 0;
    virtual const std::vector<emf::ecore::EClassifier*>& getEClassifiers() const = 0;
};

}  // namespace emf::common
