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
package org.eclipse.sphinx.tests.emf.serialization.generators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData;
import org.eclipse.sphinx.emf.serialization.generators.persistencemapping.CreateDefaultXSDExtendedMetaData;
import org.eclipse.sphinx.tests.emf.serialization.generators.util.LoadSaveUtil;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class CreateDefaultXMLPersistenceMappingExtendedMetadataTests {
	static EPackage annotatedEPackage;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		EPackage ePackage = LoadSaveUtil.loadEcorePackage("resources/input/model/Nodes.ecore");
		CreateDefaultXSDExtendedMetaData defaultXmlPersistenceExtendedMetadataGenerator = new CreateDefaultXSDExtendedMetaData(ePackage, "Nodes");
		annotatedEPackage = defaultXmlPersistenceExtendedMetadataGenerator.execute(new NullProgressMonitor());
	}

	@Test
	public void testNotNull() {
		assertNotNull(annotatedEPackage);
	}

	@Test
	public void testNodeEclassAnnotations() {
		EClass nodeEClass = getNodeEClass();
		assertEquals("NODE", EcoreUtil.getAnnotation(nodeEClass, ExtendedMetaData.ANNOTATION_URI, XMLPersistenceMappingExtendedMetaData.NAME));
		assertEquals("NODES", EcoreUtil.getAnnotation(nodeEClass,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				XMLPersistenceMappingExtendedMetaData.WRAPPER_NAME));
	}

	@Test
	public void testSubNodeEclassAnnotations() {
		EClass subNodeEClass = getSubNodeEClass();
		assertEquals("SUB-NODE", EcoreUtil.getAnnotation(subNodeEClass, ExtendedMetaData.ANNOTATION_URI, XMLPersistenceMappingExtendedMetaData.NAME));
		assertEquals("SUB-NODES", EcoreUtil.getAnnotation(subNodeEClass,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				XMLPersistenceMappingExtendedMetaData.WRAPPER_NAME));
	}

	@Test
	public void testChildEReferenceAnnotations() {
		EReference childEReference = getNodeChildEReference();
		assertEquals("CHILD", EcoreUtil.getAnnotation(childEReference, ExtendedMetaData.ANNOTATION_URI, XMLPersistenceMappingExtendedMetaData.NAME));

		// assertEquals("##targetNamespace", EcoreUtil.getAnnotation(childEReference, ExtendedMetaData.ANNOTATION_URI,
		// "namespace"));

		assertEquals("CHILDS", EcoreUtil.getAnnotation(childEReference,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				XMLPersistenceMappingExtendedMetaData.WRAPPER_NAME));
		assertEquals("true", EcoreUtil.getAnnotation(childEReference,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				XMLPersistenceMappingExtendedMetaData.FEATURE_WRAPPER_ELEMENT));
		assertEquals("false", EcoreUtil.getAnnotation(childEReference,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				XMLPersistenceMappingExtendedMetaData.FEATURE_ELEMENT));
		assertEquals("false", EcoreUtil.getAnnotation(childEReference,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				XMLPersistenceMappingExtendedMetaData.CLASSIFIER_WRAPPER_ELEMENT));
		assertEquals("true", EcoreUtil.getAnnotation(childEReference,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_ANNOTATION_SOURCE_URI,
				XMLPersistenceMappingExtendedMetaData.CLASSIFIER_ELEMENT));

	}

	public EClass getNodeEClass() {
		return getEClass("Node");
	}

	public EClass getSubNodeEClass() {
		return getEClass("SubNode");
	}

	public EReference getNodeChildEReference() {
		EClass nodeEClass = getNodeEClass();
		EStructuralFeature childEStructuralFeature = nodeEClass.getEStructuralFeature("child");
		assertNotNull(childEStructuralFeature);
		assertTrue(childEStructuralFeature instanceof EReference);
		return (EReference) childEStructuralFeature;
	}

	public EClass getEClass(String name) {
		EClassifier eClassifier = annotatedEPackage.getEClassifier(name);
		assertNotNull(eClassifier);
		assertTrue(eClassifier instanceof EClass);
		return (EClass) eClassifier;
	}

}
