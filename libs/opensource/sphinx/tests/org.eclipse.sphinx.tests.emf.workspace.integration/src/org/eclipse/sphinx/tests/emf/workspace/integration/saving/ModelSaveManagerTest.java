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
package org.eclipse.sphinx.tests.emf.workspace.integration.saving;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.saving.IResourceSaveIndicator;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;

@SuppressWarnings("nls")
public class ModelSaveManagerTest extends DefaultIntegrationTestCase {

	public ModelSaveManagerTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	/**
	 * Test method for {@link ModelSaveManager#saveModel(Resource, boolean)}
	 * 
	 * @throws Exception
	 */
	public void testSaveModel_Resource() throws Exception {

		{
			int resourceInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

			final Resource resource20 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
			assertNotNull(resource20);
			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20));

			assertFalse(resource20.getContents().isEmpty());

			final Application previousApplication = (Application) resource20.getContents().get(0);
			assertNotNull(previousApplication);

			int previousNumberOfComponent = previousApplication.getComponents().size();

			// Modify Resource
			refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
				@Override
				protected void doExecute() {
					String newComponentName = "newComponent";
					Component newComponent = InstanceModel20Factory.eINSTANCE.createComponent();
					previousApplication.getComponents().add(newComponent);
					newComponent.setName(newComponentName);
				}
			});

			assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20));

			// Save resource
			ModelSaveManager.INSTANCE.saveModel(resource20, false, null);
			waitForModelLoading();

			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20));

			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, false, null);
			waitForModelLoading();

			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);

			Resource savedResource = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);

			assertNotNull(savedResource);
			assertFalse(savedResource.getContents().isEmpty());

			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20));

			Application savedApplication = (Application) savedResource.getContents().get(0);
			assertNotNull(savedApplication);
			assertEquals(previousNumberOfComponent + 1, savedApplication.getComponents().size());
		}

		{
			int resourceInEditingDomain10 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);

			final Resource resource10 = refWks.editingDomain10.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);

			assertNotNull(resource10);
			assertFalse(resource10.getContents().isEmpty());

			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource10));

			final org.eclipse.sphinx.examples.hummingbird10.Application previousApplication = (org.eclipse.sphinx.examples.hummingbird10.Application) resource10
					.getContents().get(0);
			assertNotNull(previousApplication);

			int previousNumberOfComponent = previousApplication.getComponents().size();

			// Modify Resource
			refWks.editingDomain10.getCommandStack().execute(new RecordingCommand(refWks.editingDomain10) {
				@Override
				protected void doExecute() {
					String newComponentName = "newComponent";
					org.eclipse.sphinx.examples.hummingbird10.Component newComponent = Hummingbird10Factory.eINSTANCE.createComponent();
					previousApplication.getComponents().add(newComponent);
					newComponent.setName(newComponentName);
				}
			});

			waitForModelLoading();
			assertTrue(ModelSaveManager.INSTANCE.isDirty(resource10));

			// Save resource
			ModelSaveManager.INSTANCE.saveModel(resource10, false, null);
			waitForModelLoading();

			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource10));

			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject10_A, false, false, null);
			waitForModelLoading();

			assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);

			Resource savedResource = refWks.editingDomain10.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);

			assertNotNull(savedResource);
			assertFalse(savedResource.getContents().isEmpty());

			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource10));

			org.eclipse.sphinx.examples.hummingbird10.Application savedApplication = (org.eclipse.sphinx.examples.hummingbird10.Application) savedResource
					.getContents().get(0);
			assertNotNull(savedApplication);
			assertEquals(previousNumberOfComponent + 1, savedApplication.getComponents().size());
		}
	}

	/**
	 * Test method for {@link ModelSaveManager#saveModel(Resource, boolean, IProgressMonitor)}
	 * 
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testSaveModel_ModelDescriptor() throws OperationCanceledException, InterruptedException {
		// Context Resources in the same model
		// verify the dirty state of the context resource
		IWorkspaceEditingDomainMapping editingDomainMapping = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping();
		int resourceInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		int resourceInEditingDomainUml2 = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		{
			final Resource resource20_1 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
			final Resource resource20_2 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);

			final IResourceSaveIndicator resourceSaveIndicator = editingDomainMapping.getResourceSaveIndicator(refWks.editingDomain20);
			IModelDescriptor modelDescriptor20 = ModelDescriptorRegistry.INSTANCE.getModel(resource20_1);
			assertNotNull(modelDescriptor20);
			assertFalse(resourceSaveIndicator.isDirty(resource20_1));
			assertFalse(resourceSaveIndicator.isDirty(resource20_2));

			assertNotNull(resource20_1);
			assertFalse(resource20_1.getContents().isEmpty());
			assertEquals(1, resource20_1.getContents().size());

			assertNotNull(resource20_2);
			assertFalse(resource20_2.getContents().isEmpty());
			assertEquals(1, resource20_2.getContents().size());
			// verify the Dirty Change
			// Test for method ModelSaveManage@isDirty(Resource)
			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20_1));
			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20_2));

			final Application application = (Application) resource20_1.getContents().get(0);
			assertNotNull(application);
			int previousNumberOfComponent_20_1 = application.getComponents().size();

			// Modify Resource20_1
			refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
				@Override
				protected void doExecute() {
					String newComponentName = "newComponent";
					Component newComponent = InstanceModel20Factory.eINSTANCE.createComponent();
					newComponent.setName(newComponentName);
					application.getComponents().add(newComponent);
				}
			});

			// Modify Resource20_2
			final Platform platform = (Platform) resource20_2.getContents().get(0);
			assertNotNull(platform);
			int previousNumbreOfComponentType = platform.getComponentTypes().size();
			refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
				@Override
				protected void doExecute() {
					String newComponentTypeName = "newComponentType";
					ComponentType newcomType = TypeModel20Factory.eINSTANCE.createComponentType();
					platform.getComponentTypes().add(newcomType);
					newcomType.setName(newComponentTypeName);
				}
			});

			assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20_1));
			assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20_2));
			// Save resource
			ModelSaveManager.INSTANCE.saveModel(modelDescriptor20, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20_1));
			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20_2));

			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, false, null);
			waitForModelLoading();

			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
			Resource savedResource20_1 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);

			Resource savedResource20_2 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);

			assertNotNull(savedResource20_1);
			assertNotNull(savedResource20_2);

			// Verify saved package
			assertEquals(1, savedResource20_1.getContents().size());
			Application savedApplication = (Application) savedResource20_1.getContents().get(0);
			assertNotNull(savedApplication);
			assertEquals(previousNumberOfComponent_20_1 + 1, savedApplication.getComponents().size());

			assertEquals(1, savedResource20_2.getContents().size());
			Platform savedPlatform = (Platform) savedResource20_2.getContents().get(0);
			assertNotNull(savedPlatform);
			assertEquals(previousNumbreOfComponentType + 1, savedPlatform.getComponentTypes().size());
		}
		// Context resource in 2 models

		{
			final Resource resource20_1 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
			final Resource resourceUml2_1 = refWks.editingDomainUml2.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
							+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
			assertNotNull(resource20_1);
			assertNotNull(resourceUml2_1);

			final IResourceSaveIndicator resourceSaveIndicator20 = editingDomainMapping.getResourceSaveIndicator(refWks.editingDomain20);
			final IResourceSaveIndicator resourceSaveIndicatorUml2 = editingDomainMapping.getResourceSaveIndicator(refWks.editingDomainUml2);

			IModelDescriptor modelDescriptor20 = ModelDescriptorRegistry.INSTANCE.getModel(resource20_1);
			assertNotNull(modelDescriptor20);
			assertFalse(resourceSaveIndicator20.isDirty(resource20_1));
			assertFalse(resourceSaveIndicatorUml2.isDirty(resourceUml2_1));

			assertFalse(resource20_1.getContents().isEmpty());
			assertEquals(1, resource20_1.getContents().size());

			assertFalse(resourceUml2_1.getContents().isEmpty());
			assertEquals(1, resourceUml2_1.getContents().size());
			// verify the Dirty Change
			// Test for method ModelSaveManage@isDirty(Resource)
			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20_1));
			assertFalse(ModelSaveManager.INSTANCE.isDirty(resourceUml2_1));

			final Application application = (Application) resource20_1.getContents().get(0);
			assertNotNull(application);
			int previousNumbreOfComponent_20_1 = application.getComponents().size();

			// Modify Resource20_1

			refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
				@Override
				protected void doExecute() {
					String newComponentName = "newComponent";
					Component newComponent = InstanceModel20Factory.eINSTANCE.createComponent();
					application.getComponents().add(newComponent);
					newComponent.setName(newComponentName);
				}
			});

			// Modify Resource20_2
			final Model modelUml2 = (Model) resourceUml2_1.getContents().get(0);
			assertNotNull(modelUml2);
			int previousNumbreOfPackageUml2 = 2;
			assertEquals(previousNumbreOfPackageUml2, modelUml2.getPackagedElements().size());
			refWks.editingDomainUml2.getCommandStack().execute(new RecordingCommand(refWks.editingDomainUml2) {
				@Override
				protected void doExecute() {
					String newPackageName = "newPacakge";
					Package newPackage = UMLFactory.eINSTANCE.createPackage();
					newPackage.setName(newPackageName);
					modelUml2.getPackagedElements().add(newPackage);
				}
			});

			assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20_1));
			assertTrue(ModelSaveManager.INSTANCE.isDirty(resourceUml2_1));
			// Save resource
			ModelSaveManager.INSTANCE.saveModel(modelDescriptor20, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20_1));
			assertTrue(ModelSaveManager.INSTANCE.isDirty(resourceUml2_1));

			ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_D, false, false, null);
			waitForModelLoading();

			assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
			assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);
			Resource savedResource20_1 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);

			Resource savedResource20_2 = refWks.editingDomainUml2.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
							+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);

			assertNotNull(savedResource20_1);
			assertNotNull(savedResource20_2);

			// Verify saved package
			assertEquals(1, savedResource20_1.getContents().size());
			Application savedApplication = (Application) savedResource20_1.getContents().get(0);
			assertNotNull(savedApplication);
			assertEquals(previousNumbreOfComponent_20_1 + 1, savedApplication.getComponents().size());
			// Verify unsaved resource
			assertEquals(1, savedResource20_2.getContents().size());
			Model savedModelUml2 = (Model) savedResource20_2.getContents().get(0);
			assertNotNull(savedModelUml2);
			assertEquals(previousNumbreOfPackageUml2, savedModelUml2.getPackagedElements().size());
		}

	}

	/**
	 * Test method for
	 * {@link ModelSaveManager#saveModel(org.eclipse.sphinx.emf.model.IModelDescriptor, boolean, IProgressMonitor)} with
	 * context the{@link ModelDescriptor} has referenced roots
	 * 
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testSaveModel_ModelDescriptor_WithRefererencedRoots() throws OperationCanceledException, InterruptedException {
		// verify the dirty state of the context resource
		int resourceInEditingDomain20 = refWks.editingDomain20.getResourceSet().getResources().size();
		IWorkspaceEditingDomainMapping editingDomainMapping = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping();
		final Resource resource20D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		final Resource resource20E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);

		IModelDescriptor modelDescriptor20 = ModelDescriptorRegistry.INSTANCE.getModel(resource20E_1);
		assertNotNull(modelDescriptor20);
		modelDescriptor20.belongsTo(resource20D_1, true);
		modelDescriptor20.belongsTo(resource20E_1, true);

		final IResourceSaveIndicator resourceSaveIndicator = editingDomainMapping.getResourceSaveIndicator(refWks.editingDomain20);
		assertFalse(resourceSaveIndicator.isDirty(resource20D_1));
		assertFalse(resourceSaveIndicator.isDirty(resource20E_1));

		assertNotNull(resource20D_1);
		assertFalse(resource20D_1.getContents().isEmpty());
		assertEquals(1, resource20D_1.getContents().size());

		assertNotNull(resource20E_1);
		assertFalse(resource20E_1.getContents().isEmpty());
		assertEquals(1, resource20E_1.getContents().size());
		// verify the Dirty Change
		// Test for method ModelSaveManage@isDirty(Resource)
		assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20D_1));
		assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20E_1));

		final Application application20D_1 = (Application) resource20D_1.getContents().get(0);
		assertNotNull(application20D_1);
		int previousNumbreOfComponent20D_1 = application20D_1.getComponents().size();

		// Modify Resource20_1
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				String newComponentName = "newComponent";
				Component newComponent = InstanceModel20Factory.eINSTANCE.createComponent();
				application20D_1.getComponents().add(newComponent);
				newComponent.setName(newComponentName);
			}
		});

		// Modify Resource20_2
		final Application application20E_1 = (Application) resource20E_1.getContents().get(0);
		assertNotNull(application20E_1);
		int previousNumbreOfComponent20E_1 = application20E_1.getComponents().size();
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				String newComponentName = "newComponent";
				Component newComponent = InstanceModel20Factory.eINSTANCE.createComponent();
				application20E_1.getComponents().add(newComponent);
				newComponent.setName(newComponentName);
			}
		});

		assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20D_1));
		assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20E_1));
		// Save resource
		ModelSaveManager.INSTANCE.saveModel(modelDescriptor20, false, new NullProgressMonitor());
		waitForModelLoading();

		assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20D_1));
		assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20E_1));

		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		Resource savedResource20D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);

		Resource savedResource20E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);

		assertNotNull(savedResource20D_1);
		assertNotNull(savedResource20E_1);

		// Verify saved package
		assertEquals(1, savedResource20D_1.getContents().size());
		Application savedApplication20D_1 = (Application) savedResource20D_1.getContents().get(0);
		assertNotNull(savedApplication20D_1);
		assertEquals(previousNumbreOfComponent20D_1 + 1, savedApplication20D_1.getComponents().size());

		assertEquals(1, savedResource20E_1.getContents().size());
		Application savedApplication20E_1 = (Application) savedResource20E_1.getContents().get(0);
		assertNotNull(savedApplication20E_1);
		assertEquals(previousNumbreOfComponent20E_1 + 1, savedApplication20E_1.getComponents().size());
	}

	// TODO add test for async saving
	/**
	 * /** Test method for ({@link ModelSaveManager#saveProject(IProject, boolean, IProgressMonitor))}
	 */
	public void testSaveProject() throws Exception {
		// verify the dirty state of the context resource
		int resourceInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		IWorkspaceEditingDomainMapping editingDomainMapping = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping();
		final Resource resource20_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		final Resource resource20_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		final IResourceSaveIndicator resourceSaveIndicator = editingDomainMapping.getResourceSaveIndicator(refWks.editingDomain20);

		assertFalse(resourceSaveIndicator.isDirty(resource20_1));
		assertFalse(resourceSaveIndicator.isDirty(resource20_2));
		assertNotNull(resource20_1);
		assertFalse(resource20_1.getContents().isEmpty());

		assertEquals(1, resource20_1.getContents().size());
		// verify the Dirty Change
		// Verify saved package
		assertFalse(ModelSaveManager.INSTANCE.isDirty(resource20_1));

		assertEquals(1, resource20_1.getContents().size());

		final Application application_1 = (Application) resource20_1.getContents().get(0);
		assertNotNull(application_1);
		int previousNumberOfComponent = application_1.getComponents().size();

		// Modify Resource
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				String newComponentName = "newComponent";
				Component newComponent = InstanceModel20Factory.eINSTANCE.createComponent();
				application_1.getComponents().add(newComponent);
				newComponent.setName(newComponentName);
			}
		});

		waitForModelLoading();
		assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20_1));

		assertNotNull(resource20_2);
		assertFalse(resource20_2.getContents().isEmpty());

		assertEquals(1, resource20_2.getContents().size());
		// verify the Dirty Change
		// Verify saved package
		assertTrue(ModelSaveManager.INSTANCE.isDirty(refWks.hbProject20_A));

		assertEquals(1, resource20_2.getContents().size());

		final Platform platform = (Platform) resource20_2.getContents().get(0);
		assertNotNull(platform);
		int previousNumbreOfComponentType = platform.getComponentTypes().size();
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				String newComponentTypeName = "newComponentType";
				ComponentType newcomType = TypeModel20Factory.eINSTANCE.createComponentType();
				platform.getComponentTypes().add(newcomType);
				newcomType.setName(newComponentTypeName);
			}
		});

		waitForModelLoading();
		assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20_2));
		assertNotNull(refWks
				.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3));

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				try {

					refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3).delete(true, new NullProgressMonitor());
				} catch (Exception ex) {
					fail(ex.toString());
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		waitForModelLoading();
		assertTrue(ModelSaveManager.INSTANCE.isDirty(refWks.hbProject20_A));
		// Save Models
		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject20_A, false, null);
		waitForModelLoading();

		assertFalse(ModelSaveManager.INSTANCE.isDirty(refWks.hbProject20_A));

		ModelLoadManager.INSTANCE.unloadProject(refWks.hbProject20_A, false, false, new NullProgressMonitor());
		waitForModelLoading();

		// Verify saved resource
		ModelLoadManager.INSTANCE.loadProject(refWks.hbProject20_A, false, false, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20 - 1);
		Resource savedResource_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource savedResource_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);

		assertNotNull(savedResource_1);
		assertFalse(savedResource_1.getContents().isEmpty());
		assertNotNull(savedResource_2);
		assertFalse(savedResource_2.getContents().isEmpty());

		assertEquals(1, savedResource_1.getContents().size());
		assertEquals(1, savedResource_2.getContents().size());
		// verify the Dirty Change
		// Verify saved package

		assertEquals(1, savedResource_1.getContents().size());

		Application savedApplication = (Application) savedResource_1.getContents().get(0);
		assertNotNull(savedApplication);
		assertEquals(previousNumberOfComponent + 1, savedApplication.getComponents().size());

		Platform savedPlatform = (Platform) savedResource_2.getContents().get(0);
		assertNotNull(savedPlatform);
		assertEquals(previousNumbreOfComponentType + 1, savedPlatform.getComponentTypes().size());

		Resource savedResource_3 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		assertNull(savedResource_3);
	}

	/**
	 * Test method for ({@link ModelSaveManager#saveProject(IProject, boolean, IProgressMonitor)} with contextProject (
	 * {@link IProject}) has referenced projects
	 */
	public void testSaveProject_WithReferenceProject() throws Exception {
		// Load models
		// verify the dirty state of the context resource
		IWorkspaceEditingDomainMapping editingDomainMapping = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping();
		final Resource resource20D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		final Resource resource20D_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);
		final IResourceSaveIndicator resourceSaveIndicator = editingDomainMapping.getResourceSaveIndicator(refWks.editingDomain20);

		assertFalse(resourceSaveIndicator.isDirty(resource20D_1));
		assertFalse(resourceSaveIndicator.isDirty(resource20D_2));

		assertNotNull(resource20D_1);
		assertEquals(1, resource20D_1.getContents().size());

		assertEquals(1, resource20D_1.getContents().size());

		final Application application20D_1 = (Application) resource20D_1.getContents().get(0);
		assertNotNull(application20D_1);
		int previousNumbreOfComponent = application20D_1.getComponents().size();

		// Modify Resource20_1
		try {
			refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
				@Override
				protected void doExecute() {
					String newComponentName = "newComponent";
					Component newComponent = InstanceModel20Factory.eINSTANCE.createComponent();
					application20D_1.getComponents().add(newComponent);
					newComponent.setName(newComponentName);
				}
			});
		} catch (Exception e) {
			fail("Transaction  errors.");
		}
		waitForModelLoading();
		assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20D_1));

		// Modify Resource20D_2
		assertNotNull(resource20D_2);
		assertEquals(1, resource20D_2.getContents().size());

		final Platform platform = (Platform) resource20D_2.getContents().get(0);
		assertNotNull(platform);
		int previousNumbreOfComponentType = platform.getComponentTypes().size();

		try {
			refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
				@Override
				protected void doExecute() {
					String newComponentTypeName = "newComponentType";
					ComponentType newcomType = TypeModel20Factory.eINSTANCE.createComponentType();
					platform.getComponentTypes().add(newcomType);
					newcomType.setName(newComponentTypeName);
				}
			});

		} catch (Exception e) {
			fail("Transaction  errors.");
		}

		waitForModelLoading();
		assertTrue(ModelSaveManager.INSTANCE.isDirty(resource20D_2));

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				try {

					refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3).delete(true, new NullProgressMonitor());
				} catch (Exception ex) {
					fail(ex.toString());
				}
			}
		};

		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		waitForModelLoading();
		assertTrue(ModelSaveManager.INSTANCE.isDirty(refWks.hbProject20_E));
		assertTrue(ModelSaveManager.INSTANCE.isDirty(refWks.hbProject20_D));
		// Save Models
		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject20_E, false, null);
		waitForModelLoading();

		assertFalse(ModelSaveManager.INSTANCE.isDirty(refWks.hbProject20_E));
		assertFalse(ModelSaveManager.INSTANCE.isDirty(refWks.hbProject20_D));

		ModelLoadManager.INSTANCE.unloadProject(refWks.hbProject20_E, false, false, new NullProgressMonitor());
		waitForModelLoading();

		// Verify saved resource
		ModelLoadManager.INSTANCE.loadProject(refWks.hbProject20_E, false, false, new NullProgressMonitor());
		waitForModelLoading();

		assertEquals(refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE) - 1, refWks.editingDomain20
				.getResourceSet().getResources().size());
		Resource savedResource_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource savedResource_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);

		assertNotNull(savedResource_1);
		assertNotNull(savedResource_2);

		assertEquals(1, savedResource_1.getContents().size());
		assertEquals(1, savedResource_2.getContents().size());

		// Verify saved resource 1

		Application savedApplication = (Application) savedResource_1.getContents().get(0);
		assertNotNull(savedApplication);
		assertEquals(previousNumbreOfComponent + 1, savedApplication.getComponents().size());

		Platform savedPlatform = (Platform) savedResource_2.getContents().get(0);
		assertNotNull(savedPlatform);
		assertEquals(previousNumbreOfComponentType + 1, savedPlatform.getComponentTypes().size());

		Resource savedResource_3 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);
		assertNull(savedResource_3);
	}

}
