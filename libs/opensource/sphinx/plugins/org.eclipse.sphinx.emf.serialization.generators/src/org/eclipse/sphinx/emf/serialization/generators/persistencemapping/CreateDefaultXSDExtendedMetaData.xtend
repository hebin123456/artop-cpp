/**
 * <copyright>
 *  
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *  
 * Contributors: 
 *     itemis - Initial API and implementation
 *  
 * </copyright>
 */
 package org.eclipse.sphinx.emf.serialization.generators.persistencemapping

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EEnumLiteral
import org.eclipse.emf.ecore.ENamedElement
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.util.ExtendedMetaData
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData
import org.eclipse.sphinx.emf.serialization.generators.util.Ecore2XSDUtil
import org.eclipse.emf.ecore.util.EcoreUtil

class CreateDefaultXSDExtendedMetaData {
	protected EPackage rootModel;
	protected String globalEClassName;	
	
	new(EPackage rootModel, String globalEClassName) {
		this.rootModel = rootModel;
		this.globalEClassName = globalEClassName;
	} 
	   
	def EPackage execute(IProgressMonitor monitor) {
		monitor.subTask("Create Default XSD Extended MetaData");
		// configure default tagged values
		configure(rootModel);
		
		// set the namespace for all features with feature kind = element
		rootModel.eAllContents
		.filter[eObject | eObject instanceof EStructuralFeature && ExtendedMetaData.ELEMENT_FEATURE == ExtendedMetaData.INSTANCE.getFeatureKind((eObject as EStructuralFeature))]
		.forEach[feature | configureNamespace(feature as EStructuralFeature)]
			
		return rootModel;
	}
	
	def protected EObject doSwitch(EObject object){
	 	return switch object {	 		
	 		
	 		case object instanceof EPackage : configure(object as EPackage)
	 		case object instanceof EClass : configure(object as EClass)
	 		case object instanceof EAttribute : configure(object as EAttribute)
	 		case object instanceof EEnum : configure(object as EEnum)
	 		case object instanceof EEnumLiteral : configure(object as EEnumLiteral)
	 		case object instanceof EDataType : configure(object as EDataType)
	 		case object instanceof EReference : configure(object as EReference)
	 		case object instanceof EStructuralFeature : configure(object as EStructuralFeature)
	 	}
	 }	
	
	/**
	 * Configures tagged values of a package.
	 * 
	 * @param ePackage
	 * @return ePackage
	 */
	def protected EObject configure(EPackage ePackage) {
		XMLPersistenceMappingExtendedMetaData.INSTANCE.setQualified(ePackage, true);
		ePackage.getEClassifiers().forEach[doSwitch(it)]		
		ePackage.getESubpackages().forEach[doSwitch(it)]
	
		return ePackage;
	}
	
	/**
	 * Configures tagged values of a EClass
	 * To be override
	 * 
	 * @param eClass
	 * @return eClass
	 */
	def protected EObject configure(EClass eClass) {
		// configure default name
		configureClassifierNames(eClass)
		
		// configure namespace
		configureNamespace(eClass);
		
		// configure global element
		if(eClass.getName().equals(globalEClassName)){
			XMLPersistenceMappingExtendedMetaData.INSTANCE.setXMLGlobalElement(eClass, true);
		}
		
		// configure kind
		if (XMLPersistenceMappingExtendedMetaData.INSTANCE.getContentKind(eClass) != ExtendedMetaData.UNSPECIFIED_CONTENT) {
			// content kind is already set, so do not overwrite
		}
		else{ 			
			var int kind = ExtendedMetaData.ELEMENT_ONLY_CONTENT;
			XMLPersistenceMappingExtendedMetaData.INSTANCE.setContentKind(eClass, kind);
		}
		
		eClass.getEStructuralFeatures().forEach[doSwitch(it)]	
		return eClass;
	}
	
	/**
	 * Creates ExtendedMetaData for an EAttribute
	 * 
	 * @param eAttribute
	 * @return eAttribute
	 */
	def protected EObject configure(EAttribute eAttribute) {

		// cofigure as EStructuralFeature
		configure(eAttribute as EStructuralFeature);
		XMLPersistenceMappingExtendedMetaData.INSTANCE.setXMLName(eAttribute, XMLPersistenceMappingExtendedMetaData.INSTANCE.getXMLName(eAttribute));
			
		val	boolean isAttribute = XMLPersistenceMappingExtendedMetaData.INSTANCE.isXMLAttribute(eAttribute); 
		
		var int kind = ExtendedMetaData.ELEMENT_FEATURE;
		if(isAttribute){
			kind = ExtendedMetaData.ATTRIBUTE_FEATURE;
		}
		ExtendedMetaData.INSTANCE.setFeatureKind(eAttribute, kind);		
	
		if (!isAttribute) {
			//ExtendedMetaData.INSTANCE.setNamespace(eAttribute, getXMLNamespace());
		}
	
		return eAttribute;
	}
	
	/**
	 * Configures tagged values of a EStructuralFeature
	 * 
	 * @param feature
	 * @return feature
	 */
	def protected EObject configure(EStructuralFeature feature) {
		// configure default Name
		configureFeatureNames(feature);
		
		// configure namespace
		configureNamespace(feature);
		
		// configure isAttribute
		configureXMLAttribute(feature);	

        var int xmlPersistenceStrategy;
        val boolean isContainment = feature instanceof EAttribute || (feature as EReference).isContainment();
        if (feature instanceof EAttribute) {
        	//// EAttribute
        	// as xml element
        		// no need for a classifier element since the eType of EAttributes is an EDataType which doesn't support inheritance
        		if (feature.many) {
        			//1000?
        			//xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT
        			if (isContainment && feature.getEType() instanceof EClass){
							xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT
        			}
        			else{
        				xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1000__FEATURE_WRAPPER_ELEMENT       				
        			}
        		} else {
        			     
        			if (isContainment && feature.getEType() instanceof EClass){
        				if (Ecore2XSDUtil::hasConcreteSubclasses( feature.getEType() as EClass, rootModel)) {
							xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT
						} 
						else{
							xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT
						}
        			} 
        			else{
        				xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT
        			}  			
        		}
//        	
        } else {
        	//EReference
        	if ((feature as EReference).containment) {
        		if (feature.many) {
        			//xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT
        			xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1000__FEATURE_WRAPPER_ELEMENT
        			if (feature.getEType() instanceof EClass){
							xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT
        			}
        		} else {
        			if(feature.getEType() instanceof EClass){
        				if (Ecore2XSDUtil::hasConcreteSubclasses( feature.getEType() as EClass, rootModel)) {
        					xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT       			
 						}
 						else {
 							//MBR i do not think we should use this pattern as default since it will produce backwards compatibility issues in case the referenced class gets a subclass
        					xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT     			 						
 						}
        			}      			
        			 else {
 						//MBR i do not think we should use this pattern as default since it will produce backwards compatibility issues in case the referenced class gets a subclass
        				xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT     			 						
 					}
        		}
        	} else {
        		if (feature.many) {
        			xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT
        		} else {
        			xmlPersistenceStrategy = XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT      			
        		}
        	}
        }

        XMLPersistenceMappingExtendedMetaData::INSTANCE.setXMLPersistenceMappingStrategy(feature, xmlPersistenceStrategy);
		return feature;
	}
	
	/**
	 * to be override
	 */
	def protected void configureFeatureNames(EStructuralFeature feature) {
		// configure default Name
		XMLPersistenceMappingExtendedMetaData::INSTANCE.setXMLName(feature, Ecore2XSDUtil::getSingularName(feature));
		XMLPersistenceMappingExtendedMetaData::INSTANCE.setXMLWrapperName(feature, Ecore2XSDUtil::getPluralName(feature));		
	}
	
	/**
	 * to be override
	 */
	def protected void configureClassifierNames(EClassifier classifier) {
		// configure default Name
		XMLPersistenceMappingExtendedMetaData::INSTANCE.setXMLName(classifier, Ecore2XSDUtil::getSingularName(classifier));
		XMLPersistenceMappingExtendedMetaData::INSTANCE.setXMLWrapperName(classifier, Ecore2XSDUtil::getPluralName(classifier));		
	}
	
	/**
	 * Configures tagged values of a primitive Datatype
	 * To be override
	 * 
	 * @param dataType
	 * @return
	 */
	def protected EObject configure(EDataType dataType) {
		// configure default Name
		configureClassifierNames(dataType)
		return dataType;
	}
	
	/**
	 * Configures tagged values of a EEnumLiteral
	 * 
	 * @param literal
	 * @return literal
	 */
	def protected EObject configure(EEnumLiteral literal) {
		// configure default Name
//		XMLPersistenceMappingExtendedMetaData::INSTANCE.setXMLName(literal, XSDExtendedMetaDataUtil::getSingularName(literal));
//		XMLPersistenceMappingExtendedMetaData::INSTANCE.setXMLNamePlural(literal, XSDExtendedMetaDataUtil::getPluralName(literal));
		
		return literal;
	}
	
	/**
	 * Configures default tagged values of a EEnum
	 * 
	 * @param eEnum
	 * @return eEnum
	 */
	def protected EObject configure(EEnum eEnum) {
		// configure default Name
		configureClassifierNames(eEnum)
		eEnum.getELiterals().forEach[doSwitch(it)]
		
		return eEnum;
	}
	
	/**
	 * Creates ExtendedMetaData for an EReference
	 * 
	 * @param eReference
	 * @return eReference
	 */
	def protected EObject configure(EReference eReference) {

		// default values
		configure(eReference as EStructuralFeature);		
		ExtendedMetaData.INSTANCE.setFeatureKind(eReference, ExtendedMetaData.ELEMENT_FEATURE);
		//ExtendedMetaData.INSTANCE.setNamespace(eRef, getXMLNamespace());
	
		return eReference;
	}
	
	// features with explicitly set namespace are serialized qualified.
	def protected void configureNamespace(EStructuralFeature feature) {
		val String namespace = XMLPersistenceMappingExtendedMetaData.INSTANCE.getNamespace(feature);
		if (null == namespace) {
			//Set the "namespace" annotation directly, to avoid setting namespace as "##targetNamespace", since the value equals to the ns of the package
			EcoreUtil.setAnnotation(feature, ExtendedMetaData.ANNOTATION_URI, "namespace", feature.EContainingClass.EPackage.nsURI);
		}
	}
    
    /**
	 * Sets the following tagged values of an ENamedElement
	 * 
	 * <ul>
	 * <li>xml.name</li>
	 * <li>xml.namePlural</li>
	 * <li>xml.nsPrefix</li>
	 * <li>xml.nsUri</li>
	 * 
	 * @param element
	 */
	def protected void configureNamespace(ENamedElement element) {
		
//		ModelUtil.addDefaultTaggedValue(element, ModelConstants.TAGGED_VALUE_XML_NS_PREFIX, 
//				containerNsPrefix);
//						
//		if(metaModelType.equals(ModelConstants.METAMODEL_TYPE_SAFE)){
//			this.defaultNsUri = ModelConstants.DEFAULT_SAFE_NSURI;
//			if (version != null && version.length() > 0) {
//				this.defaultNsUri = this.defaultNsUri + "/" + version;
//			}
//			this.containerNsUri = this.defaultNsUri;
//		}
//				
//		ModelUtil.addDefaultTaggedValue(element, ModelConstants.TAGGED_VALUE_XML_NS_URI, containerNsUri);
		
	}
	
	// Configure xml.attribute tag
	// to be overridden
	def protected void configureXMLAttribute(EStructuralFeature eStructuralFeature){
		//MBR all attributes and references should be mapped to xml elements
		var boolean isXMLAttribute = false;
		// the tag xml.attribute is only allowed if:
		// the upper bound is 1, lower bound is 0, and the type is an enumeration or a primitive data type
		if(1.equals(eStructuralFeature.getUpperBound()) && 0.equals(eStructuralFeature.getLowerBound())){
			val EClassifier type = eStructuralFeature.getEType();
		 	// if type is instance of Enum or primitive data type
		 	if(type instanceof EEnum || type instanceof EDataType){
		 		isXMLAttribute = true;			
		 	}
		 }
		 
		XMLPersistenceMappingExtendedMetaData.INSTANCE.setXMLAttribute(eStructuralFeature, isXMLAttribute);
	}
}