/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.resource;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.IModelConverter;
import org.eclipse.sphinx.emf.resource.ModelConverterRegistry;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.examples.hummingbird.ide.preferences.IHummingbirdPreferences;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMCompatibility;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.platform.util.ReflectUtil;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings("nls")
public class ModelConverterTest extends DefaultIntegrationTestCase {

	private IProject contextProject;

	public ModelConverterTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Remove all contributed model converters for HB resource version 2.0.0 but the MockModelConverter
		@SuppressWarnings("unchecked")
		List<IModelConverter> modelConverters = (List<IModelConverter>) ReflectUtil.getInvisibleFieldValue(ModelConverterRegistry.INSTANCE,
				"fModelConverters");
		for (Iterator<IModelConverter> iter = modelConverters.iterator(); iter.hasNext();) {
			IModelConverter converter = iter.next();
			if (!(converter instanceof MockModelConverter
					&& Hummingbird20MMCompatibility.HUMMINGBIRD_2_0_0_RESOURCE_DESCRIPTOR.equals(converter.getResourceVersionDescriptor()))) {
				iter.remove();
			}
		}

		contextProject = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	/**
	 * Test method for conversion GatewayInstance from AR204 to AR202
	 *
	 * @throws Exception
	 */
	public void testConverterCalledWhenSavingResource() throws Exception {
		// we check that resource release descriptor is HB 2.0.1 before conversion
		Resource hb201Resource = getProjectResource(contextProject, DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4);
		assertNotNull(hb201Resource);
		assertEquals(Hummingbird20MMDescriptor.INSTANCE.getNamespace() + "/instancemodel", EcoreResourceUtil.readModelNamespace(hb201Resource));

		IMetaModelDescriptor descriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(hb201Resource);
		assertNotNull(descriptor);
		assertEquals(descriptor, Hummingbird20MMDescriptor.INSTANCE);

		// set resource version to HB 2.0.0
		IHummingbirdPreferences.RESOURCE_VERSION.set(contextProject, Hummingbird20MMCompatibility.HUMMINGBIRD_2_0_0_RESOURCE_DESCRIPTOR);

		// we place the resource in dirty state before saving
		SaveIndicatorUtil.setDirty(refWks.editingDomain20, hb201Resource);
		MockModelConverter modelConveter = (MockModelConverter) ModelConverterRegistry.INSTANCE.getSaveConverter((XMLResource) hb201Resource,
				Collections.EMPTY_MAP);
		assertNotNull(modelConveter);

		// save the project to trigger model conversion
		ModelSaveManager.INSTANCE.saveProject(contextProject, true, new NullProgressMonitor());
		waitForModelSaving();
		ModelLoadManager.INSTANCE.reloadProject(contextProject, false, true, new NullProgressMonitor());
		waitForModelLoading();

		// Check that resource was converted
		Resource convertedResource = getProjectResource(contextProject, DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4);
		assertNotNull(convertedResource);
		String expectedNamespace = Hummingbird20MMCompatibility.HUMMINGBIRD_2_0_0_RESOURCE_DESCRIPTOR.getNamespace() + "/instancemodel";
		checkConverterIsCalled(convertedResource, MockModelConverter.CONVERTED_IN_SAVING, expectedNamespace);

	}

	public void testConverterCalledWhenLoadingResource() throws Exception {
		// Verify that test resource is HB 2.0.0
		Resource hb200Resource = getProjectResource(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A),
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		String expectedNamespace = Hummingbird20MMCompatibility.HUMMINGBIRD_2_0_0_RESOURCE_DESCRIPTOR.getNamespace() + "/instancemodel";
		assertEquals(expectedNamespace, EcoreResourceUtil.readModelNamespace(hb200Resource));

		// check load converter of resource
		MockModelConverter modelLoadConveter = (MockModelConverter) ModelConverterRegistry.INSTANCE.getLoadConverter((XMLResource) hb200Resource,
				Collections.emptyMap());
		assertNotNull(modelLoadConveter);

		checkConverterIsCalled(hb200Resource, MockModelConverter.CONVERTED_IN_LOADING, expectedNamespace);

	}

	private void checkConverterIsCalled(Resource convertedResource, String progressName, String expectedNamespace) throws Exception {
		assertEquals(expectedNamespace, EcoreResourceUtil.readModelNamespace(convertedResource));
		assertFalse(convertedResource.getContents().isEmpty());
		EObject convertedModelRoot = convertedResource.getContents().get(0);
		if (convertedModelRoot instanceof Application) {
			Application convertedApplication = (Application) convertedModelRoot;
			for (Component component : convertedApplication.getComponents()) {
				assertTrue(component.getName(), component.getName().contains(progressName));
			}
		}
	}

}
