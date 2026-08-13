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
package org.eclipse.sphinx.tests.emf.serialization.generators.model;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaDataImpl;

@SuppressWarnings("nls")
public abstract class AbstractNodeModelBuilder implements ModelBuilder {
	protected XMLPersistenceMappingExtendedMetaData metadata = new XMLPersistenceMappingExtendedMetaDataImpl();
	final protected List<EPackage> singlePackages;
	final protected List<EPackage> manyPackages;

	public AbstractNodeModelBuilder(String name, int persistenceMappingStrategy) {
		singlePackages = createMetaModel(name, false, persistenceMappingStrategy);
		manyPackages = createMetaModel(name, true, persistenceMappingStrategy);
	}

	@Override
	public List<EPackage> getSingleMetaModel() {
		return singlePackages;
	}

	@Override
	public List<EPackage> getManyMetaModel() {
		return manyPackages;
	}

	@Override
	public EObject getManyModel(int numberOfChildren) {
		return createModel(manyPackages.get(0), manyPackages.get(0), numberOfChildren);
	}

	@Override
	public EObject getSingleModel(int numberOfChildren) {
		return createModel(singlePackages.get(0), singlePackages.get(0), numberOfChildren);
	}

	abstract protected List<EPackage> createMetaModel(String name, boolean isMany, int persistenceMappingStrategy);

	abstract protected EObject createModel(EPackage rootEPackage, EPackage childEPackage, int numberOfChildren);

	public void setXMLExternalSchemaLocations(Map<String, String> externalSchemaLocations) {
		metadata.setXMLExternalSchemaLocations(singlePackages.get(0), externalSchemaLocations);
		metadata.setXMLExternalSchemaLocations(manyPackages.get(0), externalSchemaLocations);
	}

	protected EPackage createEPackage(String name, String nsUri, String nsPrefix) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(name);
		ePackage.setNsURI(nsUri);
		ePackage.setNsPrefix(nsPrefix);
		metadata.setXMLSchemaLocation(ePackage, name + ".xsd");
		metadata.setXMLDefaultEReferenceReferencedTypeAttributeName(ePackage, "TYPE");
		metadata.setXMLDefaultEReferenceReferencedTypeDeclarationStrategy(ePackage, XMLPersistenceMappingExtendedMetaData.TYPE_DECLARATION_ALWAYS);

		metadata.setXMLDefaultEReferenceContainedTypeAttributeName(ePackage, "type");
		metadata.setXMLDefaultEReferenceContainedTypeDeclarationStrategy(ePackage, XMLPersistenceMappingExtendedMetaData.TYPE_DECLARATION_IF_NEEDED);

		// metadata.setQualified(ePackage, true); // not necessary since default is true
		return ePackage;
	}

	protected EClass createEClass(EPackage ePackage, String name, String xmlName, String xmlWrapperName, boolean isGlobal) {
		EClass nodeEClass = EcoreFactory.eINSTANCE.createEClass();
		nodeEClass.setName(name);
		ePackage.getEClassifiers().add(nodeEClass);
		metadata.setXMLName(nodeEClass, xmlName);
		metadata.setXMLWrapperName(nodeEClass, xmlWrapperName);
		metadata.setXMLGlobalElement(nodeEClass, isGlobal);

		return nodeEClass;
	}

	protected EDataType createEDataType(EPackage ePackage, String name, String xmlName, String xmlWrapperName, boolean isGlobal) {
		EDataType eDataType = EcoreFactory.eINSTANCE.createEDataType();
		eDataType.setName(name);
		eDataType.setInstanceClassName("java.lang.String");
		ePackage.getEClassifiers().add(eDataType);
		metadata.setXMLName(eDataType, xmlName);
		metadata.setXMLWrapperName(eDataType, xmlWrapperName);
		metadata.setXMLGlobalElement(eDataType, isGlobal);

		return eDataType;
	}

	protected EObject createNodeEObject(EPackage ePackage, String className) {
		EObject nodeEClass = ePackage.getEFactoryInstance().create((EClass) ePackage.getEClassifier(className));
		return nodeEClass;
	}

	protected EReference createEReference(EClass eClass, String name, boolean isContainment, boolean isMany, EClass type, String xmlName,
			String xmlWrapperName, int xmlPersistenceMapping) {
		return createEReference(eClass, name, isContainment, isMany, type, xmlName, xmlWrapperName, null, xmlPersistenceMapping);
	}

	protected EReference createEReference(EClass eClass, String name, boolean isContainment, boolean isMany, EClass type, String xmlName,
			String xmlWrapperName, String classifierSuffix, int xmlPersistenceMapping) {
		EReference eReference = EcoreFactory.eINSTANCE.createEReference();
		eReference.setName(name);
		eReference.setContainment(isContainment);
		eReference.setLowerBound(0);
		eReference.setUpperBound(isMany ? -1 : 1);
		eReference.setEType(type);
		eClass.getEStructuralFeatures().add(eReference);

		metadata.setXMLName(eReference, xmlName);
		metadata.setXMLWrapperName(eReference, xmlWrapperName);
		metadata.setXMLPersistenceMappingStrategy(eReference, xmlPersistenceMapping);
		metadata.setNamespace(eReference, eReference.getEContainingClass().getEPackage().getNsURI());
		metadata.setFeatureKind(eReference, ExtendedMetaData.ELEMENT_FEATURE);
		if (null != classifierSuffix) {
			metadata.setXMLClassifierNameSuffix(eReference, classifierSuffix);
		}

		return eReference;

	}

	protected EAttribute createEAttribute(EClass eClass, String name, boolean isMany, EDataType type, String xmlName, String xmlWrapperName,
			int xmlPersistenceMapping) {
		EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
		eAttribute.setName(name);
		eAttribute.setLowerBound(0);
		eAttribute.setUpperBound(isMany ? -1 : 1);
		eAttribute.setEType(type);
		eClass.getEStructuralFeatures().add(eAttribute);

		metadata.setXMLName(eAttribute, xmlName);
		metadata.setXMLWrapperName(eAttribute, xmlWrapperName);
		metadata.setXMLPersistenceMappingStrategy(eAttribute, xmlPersistenceMapping);
		metadata.setNamespace(eAttribute, eAttribute.getEContainingClass().getEPackage().getNsURI());
		metadata.setFeatureKind(eAttribute, ExtendedMetaData.ELEMENT_FEATURE);

		return eAttribute;

	}

}
