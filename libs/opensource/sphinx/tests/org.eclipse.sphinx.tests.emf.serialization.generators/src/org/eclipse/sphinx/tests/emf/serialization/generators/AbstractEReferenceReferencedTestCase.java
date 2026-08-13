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

import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData;
import org.junit.Test;

public abstract class AbstractEReferenceReferencedTestCase extends AbstractTestCase {

	@Test
	public void testEReferenceReferenced0100_Single() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT, false);
	}

	@Test
	public void testEReferenceReferenced0100_Many() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT, true);
	}

	@Test
	// Not yet implemented in Schema generator
	public void testEReferenceReferenced0101_Single() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT, false,
				SKIP_SCHEMA_VALIDATION);
	}

	@Test
	// Not yet implemented in Schema generator
	public void testEReferenceReferenced0101_Many() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0101__FEATURE_ELEMENT__CLASSIFIER_ELEMENT, true,
				SKIP_SCHEMA_VALIDATION);
	}

	@Test
	// Not yet implemented in Schema generator
	public void testEReferenceReferenced1001_Single() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT, false,
				SKIP_SCHEMA_VALIDATION);
	}

	@Test
	// Not yet implemented in Schema generator
	public void testEReferenceReferenced1001_Many() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1001__FEATURE_WRAPPER_ELEMENT__CLASSIFIER_ELEMENT, true,
				SKIP_SCHEMA_VALIDATION);
	}

	@Test
	// Not yet implemented in Schema generator
	public void testEReferenceReferenced1100_Single() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT, false,
				SKIP_SCHEMA_VALIDATION);
	}

	@Test
	public void testEReferenceReferenced1100_Many() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT, true);
	}
}