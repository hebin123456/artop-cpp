/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.integration.inmemoryresources;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings("nls")
public class HandleResourcesOnlyInMemoryTest extends DefaultIntegrationTestCase {
	private static final String NEW_RESOURCE_NAME = "newResource.instancemodel";;
	private static final String WORKSPACE_RESOURCE_PATH = DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + NEW_RESOURCE_NAME;
	private static final String ANY_RESOURCE_PATH = "NOT_EXISTING_PROJECT" + "/" + NEW_RESOURCE_NAME;

	private static final URI WORKSPACE_RESOURCE_URI = URI.createPlatformResourceURI(WORKSPACE_RESOURCE_PATH, true);
	private static final URI ANY_RESOURCE_URI = URI.createPlatformResourceURI(ANY_RESOURCE_PATH, true);

	public HandleResourcesOnlyInMemoryTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	@Override
	protected void tearDown() throws Exception {
		// we manually unload the created resource with ANY_RESOURCE_URI because the resource is not declared in the
		// workspace and so
		// is not handle by modelLoadManager unloadMoadels when test projects are deleted
		IFile file = EcorePlatformUtil.getFile(ANY_RESOURCE_URI);
		ModelLoadManager.INSTANCE.unloadFile(file, false, null);
		waitForModelLoading();

		super.tearDown();
	}

	public void testCreateResourceWithValidURI() throws OperationCanceledException, InterruptedException {
		//$NON-NLS-1$
		// we create a valid resource path for the new resource
		IPath resourcePath = EcorePlatformUtil.createPath(WORKSPACE_RESOURCE_URI);

		// We retrieve model root from fileHB_FILE_NAME_20_20A_1
		EObject modelRoot = createHummingbird20InstanceModelRoot();

		// we create new resource (filled in with model root previously retrieved) with no underlying file on file
		// system and add it to editingDomain relative to HB20 release.
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, resourcePath, Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(),
				modelRoot, false, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(WORKSPACE_RESOURCE_URI));

		// we now check that newly created resource belong to the HB20 model descriptor for hbProject20_A.
		Collection<IModelDescriptor> models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		IModelDescriptor mDescriptor = models.iterator().next();
		assertTrue(mDescriptor.belongsTo(WORKSPACE_RESOURCE_URI, true));

		// we retrieve the newly created resource
		Resource newResource = EcorePlatformUtil.getResource(WORKSPACE_RESOURCE_URI);
		assertNotNull(newResource);

		// we check that the newly created resource is filtered as a resource owned by arProject21_A
		Collection<Resource> filteredResources = mDescriptor.getLoadedResources(true);
		assertTrue(filteredResources.contains(newResource));

	}

	public void testCreateResourceWithInvalidURI() throws OperationCanceledException, InterruptedException {
		// we create a valid resource path for the new resource
		IPath resourcePath = EcorePlatformUtil.createPath(ANY_RESOURCE_URI);

		// We retrieve model root from file HB_FILE_NAME_20_20A_1
		IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		Resource resource = EcorePlatformUtil.getResource(referenceFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertNotNull(modelRoot);

		// we create new resource (filled in with model root previously retrieved) with no underlying file on file
		// system and add it to editingDomain relative to HB20 release.
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, resourcePath, Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(),
				modelRoot, false, null);
		waitForModelLoading();

		// We ensure that no underlying file exist on file system for our newly created resource.
		assertFalse(EcoreResourceUtil.exists(ANY_RESOURCE_URI));

		// we now check that newly created resource does not belong to the HB20 model descriptor for
		// hbProject20_A
		Collection<IModelDescriptor> models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		IModelDescriptor mDescriptor = models.iterator().next();
		assertFalse(mDescriptor.belongsTo(ANY_RESOURCE_URI, true));

		// we retrieve the newly created resource
		Resource newResource = EcorePlatformUtil.getResource(ANY_RESOURCE_URI);
		assertNotNull(newResource);

		// we check that the newly created resource is not filtered as a resource owned by arProject21_A
		Collection<Resource> filteredResources = mDescriptor.getLoadedResources(true);
		assertFalse(filteredResources.contains(newResource));
	}

}
