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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sphinx.emf.serialization.XMLPersistenceMappingExtendedMetaData;
import org.eclipse.sphinx.tests.emf.serialization.generators.model.ModelBuilder;
import org.eclipse.sphinx.tests.emf.serialization.generators.model.MultiPackageNodeModelBuilder;
import org.eclipse.sphinx.tests.emf.serialization.generators.model.SinglePackageNodelModelBuilder;
import org.junit.Test;

@SuppressWarnings("nls")
public class SchemaDeclarationTests extends AbstractTestCase {

	@Test
	public void testSinglePackageModel1() throws Exception {
		String name = "SinglePackageModel";
		SinglePackageNodelModelBuilder modelBuilder = new SinglePackageNodelModelBuilder(name,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT);
		modelBuilder.setXMLExternalSchemaLocations(getXMLExternalSchemaLocations());
		List<EPackage> metamodel = modelBuilder.getSingleMetaModel();
		EObject model = modelBuilder.getSingleModel(1);

		validate(metamodel, model, name, !SKIP_SCHEMA_VALIDATION);
	}

	@Test
	public void testMultiPackageModel1() throws Exception {
		String name = "MultiPackageModel";
		MultiPackageNodeModelBuilder modelBuilder = new MultiPackageNodeModelBuilder(name,
				XMLPersistenceMappingExtendedMetaData.XML_PERSISTENCE_MAPPING_STRATEGY__0100__FEATURE_ELEMENT);
		modelBuilder.setXMLExternalSchemaLocations(getXMLExternalSchemaLocations());
		List<EPackage> metamodel = modelBuilder.getSingleMetaModel();
		EObject model = modelBuilder.getSingleModel(1);

		validate(metamodel, model, name, !SKIP_SCHEMA_VALIDATION);
	}

	protected Map<String, String> getXMLExternalSchemaLocations() {
		Map<String, String> externalSchemaLocations = new HashMap<String, String>();
		externalSchemaLocations.put("http://mylang", "dummy.xsd");
		return externalSchemaLocations;
	}

	@Override
	protected ModelBuilder getModelBuilder(String name, int persistenceMappingStrategy) {
		// intentionally return null, since this method should not be called in this class
		return null;
	}

}
