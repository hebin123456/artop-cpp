/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - [477076] Access to the "ordered" attributes in metamodels that are optimized for deterministic code generation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.sphinx.emf.resource.ExtendedBasicExtendedMetaData;
import org.junit.Test;

@SuppressWarnings("nls")
public class ExtendedBasicExtendedMetaDataTest {
	ExtendedBasicExtendedMetaData metadata = ExtendedBasicExtendedMetaData.INSTANCE;

	@Test
	public void testAnnotationOrderedTrue() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		eStructuralFeature.setOrdered(false);
		EcoreUtil.setAnnotation(eStructuralFeature, ExtendedMetaData.ANNOTATION_URI, "ordered", "true");
		assertTrue(metadata.isOrdered(eStructuralFeature));
		// test cache
		assertTrue(metadata.isOrdered(eStructuralFeature));
	}

	@Test
	public void testAnnotationOrderedFalse() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		eStructuralFeature.setOrdered(true);
		EcoreUtil.setAnnotation(eStructuralFeature, ExtendedMetaData.ANNOTATION_URI, "ordered", "false");
		assertFalse(metadata.isOrdered(eStructuralFeature));
		// test cache
		assertFalse(metadata.isOrdered(eStructuralFeature));
	}

	@Test
	public void testAnnotationOrderedXyz() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		eStructuralFeature.setOrdered(true);
		EcoreUtil.setAnnotation(eStructuralFeature, ExtendedMetaData.ANNOTATION_URI, "ordered", "Xyz");
		assertFalse(metadata.isOrdered(eStructuralFeature));
		// test cache
		assertFalse(metadata.isOrdered(eStructuralFeature));
	}

	@Test
	public void testNoAnnotationOrderedTrue() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		eStructuralFeature.setOrdered(true);
		assertTrue(metadata.isOrdered(eStructuralFeature));
		// test cache
		assertTrue(metadata.isOrdered(eStructuralFeature));
	}

	@Test
	public void testNoAnnotationOrderedFalse() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		eStructuralFeature.setOrdered(false);
		assertFalse(metadata.isOrdered(eStructuralFeature));
		// test cache
		assertFalse(metadata.isOrdered(eStructuralFeature));
	}

	@Test
	public void testAnnotationSetOrderedTrue() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		metadata.setOrdered(eStructuralFeature, true);
		assertEquals("true", EcoreUtil.getAnnotation(eStructuralFeature, ExtendedMetaData.ANNOTATION_URI, "ordered"));
		// test cache
		assertTrue(metadata.isOrdered(eStructuralFeature));
	}

	@Test
	public void testAnnotationSetOrderedFalse() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		metadata.setOrdered(eStructuralFeature, false);
		assertEquals("false", EcoreUtil.getAnnotation(eStructuralFeature, ExtendedMetaData.ANNOTATION_URI, "ordered"));
		// test cache
		assertFalse(metadata.isOrdered(eStructuralFeature));
	}

	@Test
	public void testSingleton() {
		EStructuralFeature eStructuralFeature = EcoreFactory.eINSTANCE.createEReference();
		String name = "TheName";
		String name2 = "TheName2";

		metadata.setName(eStructuralFeature, name);

		// check data via ExtendedBasicExtendedMetaData
		assertSame(name, metadata.getName(eStructuralFeature));
		assertSame(name, EcoreUtil.getAnnotation(eStructuralFeature, ExtendedMetaData.ANNOTATION_URI, "name"));

		// check holder cache by retrieving name from BasicExtendedMetaData
		ExtendedMetaData extendedMetaData = ExtendedMetaData.INSTANCE;
		assertSame(name, extendedMetaData.getName(eStructuralFeature));

		// modify data via BasicExtendedMetaData
		extendedMetaData.setName(eStructuralFeature, name2);

		// check holder cache by retrieving name from ExtendedBasicExtendedMetaData
		assertSame(name2, metadata.getName(eStructuralFeature));

	}

}
