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
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.saving;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.saving.IModelSaveIndicator;
import org.eclipse.sphinx.emf.saving.IResourceSaveIndicator;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.internal.saving.ModelSaveIndicator;
import org.eclipse.sphinx.emf.workspace.internal.saving.ResourceSaveIndicator;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Parameter;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings({ "nls", "restriction" })
public class SaveIndicatorUtilTest extends DefaultIntegrationTestCase {

	private TestEditingDomainAdapterFactory testEditingDomainAdapterFactory = new TestEditingDomainAdapterFactory();

	private TransactionalEditingDomain testEditingDomain;

	public SaveIndicatorUtilTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		testEditingDomain = refWks.editingDomain20;
		Platform.getAdapterManager().registerAdapters(testEditingDomainAdapterFactory, refWks.editingDomain20.getClass());
		assertTrue(Platform.getAdapterManager().getAdapter(refWks.editingDomain20, IResourceSaveIndicator.class) instanceof TestResourceSaveIndicatorImpl);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		Platform.getAdapterManager().unregisterAdapters(testEditingDomainAdapterFactory, refWks.editingDomain20.getClass());
		Object adapter = Platform.getAdapterManager().getAdapter(refWks.editingDomain20, IResourceSaveIndicator.class);
		assertTrue(adapter instanceof ResourceSaveIndicator);
		// delete the HB_PROJECT_NAME_20_A project, it has been changed by a test
		synchronizedDeleteProject(refWks.hbProject20_A);
	}

	/**
	 *
	 */
	public class TestResourceSaveIndicatorImpl extends ResourceSaveIndicator implements IResourceSaveIndicator {

		public TestResourceSaveIndicatorImpl(TransactionalEditingDomain editingDomain) {
			super(editingDomain);
		}

		@Override
		public boolean isDirty(Resource resource) {
			return super.isDirty(resource);
		}

		@Override
		public void setDirty(Resource resource) {
			super.setDirty(resource);
		}

		@Override
		public void setSaved(Resource resource) {
			super.setSaved(resource);
		}

		@Override
		public void dispose() {
			super.dispose();

		}

		@Override
		public boolean handleResourceChanged(Resource resource) {
			return super.handleResourceChanged(resource);
		}

		@Override
		public boolean handleResourceDeleted(Resource resource) {
			return super.handleResourceDeleted(resource);
		}

		@Override
		public boolean handleResourceMoved(Resource resource, URI newURI) {
			return super.handleResourceMoved(resource, newURI);
		}

		@Override
		public Collection<Resource> getDirtyResources() {
			return super.getDirtyResources();
		}

		@Override
		public void setSaved(Collection<Resource> resources) {
			super.setSaved(resources);
		}
	}

	public class TestEditingDomainAdapterFactory implements IAdapterFactory {

		TestResourceSaveIndicatorImpl testResourceSaveIndicatorImpl = new TestResourceSaveIndicatorImpl(testEditingDomain);

		@Override
		public Object getAdapter(final Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {

			// IResourceSaveIndicator adapter for TransactionalEditingDomain?
			if (adapterType.equals(IResourceSaveIndicator.class) && adaptableObject instanceof TransactionalEditingDomain) {
				return testResourceSaveIndicatorImpl;
			}

			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public Class[] getAdapterList() {
			return new Class<?>[] { IEditingDomainProvider.class, IResourceSaveIndicator.class };
		}

	}

	/**
	 * Test method for {@link SaveIndicatorUtil#getResourceSaveIndicator(org.eclipse.emf.edit.domain.EditingDomain)}
	 * 
	 * @throws Exception
	 */
	public void testGetResourceSaveIndicator() throws Exception {
		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain20);
		assertNotNull(resourceSaveIndicator);
		assertTrue(resourceSaveIndicator instanceof TestResourceSaveIndicatorImpl);

	}

	/**
	 * Test method for {@link SaveIndicatorUtil#getModelSaveIndicator(IModelDescriptor)}
	 * 
	 * @throws Exception
	 */
	public void testGetModelSaveIndicator() throws Exception {

		EList<Resource> resources = refWks.editingDomain20.getResourceSet().getResources();
		assertNotNull(resources);
		assertFalse(resources.isEmpty());

		Resource resourceHb20 = resources.get(0);
		assertNotNull(resourceHb20);

		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(resourceHb20);
		assertNotNull(modelDescriptor);

		IModelSaveIndicator modelSaveIndicator = SaveIndicatorUtil.getModelSaveIndicator(modelDescriptor);
		assertNotNull(modelSaveIndicator);
		assertTrue(modelSaveIndicator instanceof ModelSaveIndicator);

	}

	/**
	 * Test method for {@link SaveIndicatorUtil#setSaved(org.eclipse.sphinx.emf.model.IModelDescriptor)}
	 * 
	 * @throws Exception
	 */
	public void testSetSavedModelDescriptor() throws Exception {
		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain20);
		assertNotNull(resourceSaveIndicator);
		assertTrue(resourceSaveIndicator instanceof TestResourceSaveIndicatorImpl);

		EList<Resource> resources = refWks.editingDomain20.getResourceSet().getResources();
		assertNotNull(resources);
		assertFalse(resources.isEmpty());

		Resource resourceHb20 = resources.get(0);
		assertNotNull(resourceHb20);

		resourceSaveIndicator.setDirty(resourceHb20);

		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(resourceHb20);
		assertNotNull(modelDescriptor);

		assertTrue(resourceSaveIndicator.isDirty(resourceHb20));

		SaveIndicatorUtil.setSaved(modelDescriptor);

		assertFalse(resourceSaveIndicator.isDirty(resourceHb20));

		try {
			SaveIndicatorUtil.setSaved(null);
		} catch (Exception ex) {
			fail("Exception while setSave Null input");
		}
	}

	/**
	 * Test method for {@link SaveIndicatorUtil#setSaved(org.eclipse.emf.edit.domain.EditingDomain, Resource)}
	 * 
	 * @throws Exception
	 */
	public void testSetSavedResourceWithEditingDomain() throws Exception {
		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain20);
		assertNotNull(resourceSaveIndicator);
		assertTrue(resourceSaveIndicator instanceof TestResourceSaveIndicatorImpl);

		EList<Resource> resources = refWks.editingDomain20.getResourceSet().getResources();
		assertNotNull(resources);
		assertFalse(resources.isEmpty());

		Resource resourceHb20 = resources.get(0);
		assertNotNull(resourceHb20);

		resourceSaveIndicator.setDirty(resourceHb20);

		assertTrue(resourceSaveIndicator.isDirty(resourceHb20));

		SaveIndicatorUtil.setSaved(refWks.editingDomain20, resourceHb20);

		assertFalse(resourceSaveIndicator.isDirty(resourceHb20));
	}

	/**
	 * Test method for {@link SaveIndicatorUtil#setDirty(org.eclipse.emf.edit.domain.EditingDomain, Resource)}
	 */

	public void testSetDirty() throws Exception {
		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain20);
		assertNotNull(resourceSaveIndicator);
		assertTrue(resourceSaveIndicator instanceof TestResourceSaveIndicatorImpl);

		EList<Resource> resources = refWks.editingDomain20.getResourceSet().getResources();
		assertNotNull(resources);
		assertFalse(resources.isEmpty());

		Resource resourceHb20 = resources.get(0);
		assertNotNull(resourceHb20);
		assertFalse(resourceSaveIndicator.isDirty(resourceHb20));

		SaveIndicatorUtil.setDirty(refWks.editingDomain20, resourceHb20);

		assertTrue(resourceSaveIndicator.isDirty(resourceHb20));

	}

	/**
	 * Test method for {@link SaveIndicatorUtil#isDirty(org.eclipse.emf.edit.domain.EditingDomain, Resource)}
	 * 
	 * @throws Exception
	 */
	public void testIsDirty_ResourceWithEditingDomain() throws Exception {

		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain20);
		assertNotNull(resourceSaveIndicator);
		assertTrue(resourceSaveIndicator instanceof TestResourceSaveIndicatorImpl);

		EList<Resource> resources = refWks.editingDomain20.getResourceSet().getResources();
		assertNotNull(resources);
		assertFalse(resources.isEmpty());

		Resource resourceHb20 = resources.get(0);
		assertNotNull(resourceHb20);
		resourceSaveIndicator.setSaved(resourceHb20);
		assertFalse(resourceSaveIndicator.isDirty(resourceHb20));

		resourceSaveIndicator.setDirty(resourceHb20);

		assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomain20, resourceHb20));
	}

	/**
	 * Test method for {@link SaveIndicatorUtil#isDirty(IModelDescriptor)}
	 * 
	 * @throws Exception
	 */
	public void testIsDirty_ModelDescriptor() throws Exception {

		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(refWks.editingDomain20);
		assertNotNull(resourceSaveIndicator);
		assertTrue(resourceSaveIndicator instanceof TestResourceSaveIndicatorImpl);

		EList<Resource> resources = refWks.editingDomain20.getResourceSet().getResources();
		assertNotNull(resources);
		assertFalse(resources.isEmpty());

		Resource resourceHb20 = resources.get(0);
		assertNotNull(resourceHb20);

		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(resourceHb20);
		assertNotNull(modelDescriptor);

		resourceSaveIndicator.setSaved(resourceHb20);
		assertFalse(resourceSaveIndicator.isDirty(resourceHb20));

		resourceSaveIndicator.setDirty(resourceHb20);
		assertTrue(SaveIndicatorUtil.isDirty(modelDescriptor));
	}

	/**
	 * Test that changed a referenced to a file does not mark the referenced file also dirty
	 * 
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=404616
	 */
	public void testIsDirtyReferences() {
		ResourceSaveIndicator resourceSaveIndicator = new ResourceSaveIndicator(refWks.editingDomain20);
		assertTrue(resourceSaveIndicator.getDirtyResources().isEmpty());

		Resource resourceApp = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		Resource resourcePlatform = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);

		final Application application = (Application) resourceApp.getContents().get(0);
		final org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform platform = (org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform) resourcePlatform
				.getContents().get(0);

		// create first the content into the platform
		final Parameter[] param = new Parameter[1];
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, new Runnable() {
				@Override
				public void run() {
					platform.setName("platform");
					ComponentType cType = TypeModel20Factory.eINSTANCE.createComponentType();
					cType.setName("componentType");
					platform.getComponentTypes().add(cType);

					param[0] = TypeModel20Factory.eINSTANCE.createParameter();
					param[0].setName("paramType");

					cType.getParameters().add(param[0]);
				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		// assertTrue(ModelSaveManager.INSTANCE.isDirty(resourcePlatform));
		assertEquals(1, resourceSaveIndicator.getDirtyResources().size());
		assertTrue(resourceSaveIndicator.isDirty(resourcePlatform));
		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject20_A, false, null);

		resourceSaveIndicator.setSaved(resourcePlatform);
		assertEquals(0, resourceSaveIndicator.getDirtyResources().size());

		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, new Runnable() {
				@Override
				public void run() {
					application.setName("application");
					Component component = InstanceModel20Factory.eINSTANCE.createComponent();
					component.setName("component");
					application.getComponents().add(component);

					ParameterValue pValue = InstanceModel20Factory.eINSTANCE.createParameterValue();
					pValue.setName("PValue");

					component.getParameterValues().add(pValue);
					pValue.setType(param[0]);
				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals(1, resourceSaveIndicator.getDirtyResources().size());
		assertTrue(resourceSaveIndicator.isDirty(resourceApp));

		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject20_A, false, null);

		resourceSaveIndicator.setSaved(resourceApp);
		assertEquals(0, resourceSaveIndicator.getDirtyResources().size());

	}

}
