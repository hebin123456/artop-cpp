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
import org.eclipse.sphinx.tests.emf.serialization.generators.model.EReferenceContainedWithSubtypesAndAbstractTypesModelBuilder;
import org.eclipse.sphinx.tests.emf.serialization.generators.model.ModelBuilder;
import org.junit.Test;

public class EReferenceContainedWithSubtypesAndAbstractTypesTests extends AbstractEReferenceContainedTestCase {

	@Override
	protected ModelBuilder getModelBuilder(String name, int persistenceMappingStrategy) {
		return new EReferenceContainedWithSubtypesAndAbstractTypesModelBuilder(name, persistenceMappingStrategy);
	}

	@Override
	@Test
	// Not yet fully implemented in Schema generator
	public void testEReferenceContained0100_Single() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT, false, SKIP_SCHEMA_VALIDATION);
	}

	@Override
	@Test
	// Not yet fully implemented in Schema generator
	public void testEReferenceContained0100_Many() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT, true, SKIP_SCHEMA_VALIDATION);
	}

	@Override
	@Test
	// Not yet fully implemented in Schema generator
	public void testEReferenceContained1100_Single() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT, false,
				SKIP_SCHEMA_VALIDATION);
	}

	@Override
	@Test
	// Not yet fully implemented in Schema generator
	public void testEReferenceContained1100_Many() throws Exception {
		runTest(XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__1100__FEATURE_WRAPPER_ELEMENT__FEATURE_ELEMENT, true,
				SKIP_SCHEMA_VALIDATION);
	}
}