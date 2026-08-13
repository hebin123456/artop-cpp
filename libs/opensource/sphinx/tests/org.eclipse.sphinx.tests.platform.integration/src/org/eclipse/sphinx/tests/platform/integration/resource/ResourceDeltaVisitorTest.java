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
 *     itemis - [414125] Enhance ResourceDeltaVisitor to enable the analysis of IFolder added/moved/removed
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.platform.integration.resource;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.internal.saving.ResourceSaveIndicator;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

@SuppressWarnings({ "nls", "restriction" })
public class ResourceDeltaVisitorTest extends DefaultIntegrationTestCase {
	MockResourceChangedHandler handler = new MockResourceChangedHandler();

	public ResourceDeltaVisitorTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// Remove all project references except:
		// HB_PROJECT_NAME_20_E -> HB_PROJECT_NAME_20_D
		Map<String, Set<String>> projectReferences = getProjectReferences();
		projectReferences.remove(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectReferences.remove(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
	}

	@Override
	public void setUp() throws Exception {

		ResourcesPlugin.getWorkspace().addResourceChangeListener(handler);
		super.setUp();
		assertNotNull(refWks.hbProject10_A);
		assertNotNull(refWks.hbProject10_F);
		assertNotNull(refWks.hbProject20_E);
		assertNotNull(refWks.hbProject20_D);
	}

	@Override
	public void tearDown() throws Exception {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(handler);
		super.tearDown();
	}

	// ##############################################
	// ############# ADD ############################
	// ##############################################

	// Create new project
	public void testProjectCreated() throws Exception {
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		resourcesHandled.clear();
		// Create new project
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IProject project = EcorePlugin.getWorkspaceRoot().getProject("NewProject");
				if (!project.exists()) {
					project.create(new NullProgressMonitor());
					project.open(new NullProgressMonitor());
				} else if (!project.isAccessible()) {
					project.open(new NullProgressMonitor());
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, null);
		waitForModelLoading();

		assertTrue(resourcesHandled.contains(new ResourceHandled(new Path("/NewProject"), MockResourceChangedHandler.ADD_PROJECT)));
	}

	// Create new Folder
	public void testFolderCreated() throws CoreException {
		handler.clearResourcesHandledMap();
		IFolder newFolder = refWks.hbProject10_A.getFolder("New Folder");
		assertFalse(newFolder.exists());
		newFolder.create(true, true, null);
		// Check that creating new Folder was handled
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(newFolder.getFullPath(), MockResourceChangedHandler.ADD_FOLDER)));
	}

	// Create new files
	public void testFileCreated() throws CoreException {
		// Create new Hummingbird file
		handler.clearResourcesHandledMap();
		IPath newHbFilePath = refWks.hbProject10_A.getFullPath().append("newHbFile.hummingbird");
		Application hb10Application = Hummingbird10Factory.eINSTANCE.createApplication();
		EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newHbFilePath, Hummingbird10Package.eCONTENT_TYPE, hb10Application, false,
				null);
		// Check that event creating new file was handled
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled.contains(new ResourceHandled(newHbFilePath, MockResourceChangedHandler.ADD_FILE)));

		// Create new UML2 File
		handler.clearResourcesHandledMap();
		IPath newUml2FilePath = refWks.hbProject10_A.getFullPath().append("newUml2File.uml");
		Model uml2Model = UMLFactory.eINSTANCE.createModel();
		EcorePlatformUtil.saveNewModelResource(refWks.editingDomainUml2, newUml2FilePath, UMLPackage.eCONTENT_TYPE, uml2Model, false, null);
		// Check that event creating new file was handled
		resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled.contains(new ResourceHandled(newUml2FilePath, MockResourceChangedHandler.ADD_FILE)));

	}

	// ##############################################
	// ############# CHANGE #########################
	// ##############################################
	// Project opened

	public void testProjectOpened() throws Exception {
		// Close all projects
		synchronizedCloseAllProjects();
		assertFalse(refWks.hbProject10_A.isOpen());
		// Cleare the tracing list
		handler.clearResourcesHandledMap();
		// Open Project
		synchronizedOpenProject(refWks.hbProject10_A);
		// Check that project opened and included files added were handled.
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_OPENED)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1),
						MockResourceChangedHandler.ADD_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2),
						MockResourceChangedHandler.ADD_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3),
						MockResourceChangedHandler.ADD_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4),
						MockResourceChangedHandler.ADD_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5),
						MockResourceChangedHandler.ADD_FILE)));

	}

	// Project closed
	public void testProjectClosed() throws Exception {
		// Verify that context Project is open
		assertTrue(refWks.hbProject10_A.isOpen());

		handler.clearResourcesHandledMap();
		// Close context project
		synchronizedCloseProject(refWks.hbProject10_A);

		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_CLOSED)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5),
						MockResourceChangedHandler.REMOVED_FILE)));
	}

	// Project Description Change
	public void testProjectDescriptonChanged() throws CoreException {
		// Update Project dependencies
		handler.clearResourcesHandledMap();
		IProjectDescription projectDesc = refWks.hbProject20_E.getDescription();
		// Remove dependency from hbProject20E to 20D
		projectDesc.setReferencedProjects(new IProject[] {});
		refWks.hbProject20_E.setDescription(projectDesc, null);

		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject20_E.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_DESCIPTION)));

		// Add nature
		handler.clearResourcesHandledMap();
		String natureId = "org.eclipse.jdt.core.javanature";
		ExtendedPlatform.addNature(refWks.hbProject10_A, natureId, null);
		resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_DESCIPTION)));

		// Remove nature
		handler.clearResourcesHandledMap();
		ExtendedPlatform.removeNature(refWks.hbProject10_A, natureId, null);
		resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_DESCIPTION)));
	}

	// Project Setting change
	public void testProjectSettingsChanged() throws CoreException {
		// Change the metamodel version of project
		handler.clearResourcesHandledMap();
		IFile settingFile_hb10 = refWks.hbProject10_A.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");
		IFile settingFile_hb20 = refWks.hbProject20_E.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");

		assertTrue(settingFile_hb10.isAccessible());
		assertTrue(settingFile_hb20.isAccessible());

		// Set content in settingFile_hb10 to settingFile_hb20 to change metamodel vesion of hbProject10_A
		synchronizedSetFileContents(settingFile_hb10, settingFile_hb20.getContents());

		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_SETTING)));
	}

	// Folder changed
	// ----Settings folder: covered by testProjectSettingsChanged

	public void testSettingsFolderChanged() throws CoreException {
		// Create new file
		handler.clearResourcesHandledMap();
		IFile settingFile_hb10 = refWks.hbProject10_A.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");
		IFile settingFile_hb20 = refWks.hbProject20_E.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");

		assertTrue(settingFile_hb10.isAccessible());
		assertTrue(settingFile_hb20.isAccessible());
		// Set content in settingFile_hb10 to settingFile_hb20 to change metamodel version of hbProject10_A
		settingFile_hb20.copy(refWks.hbProject10_A.getFullPath().append(".settings/newSettingfile.prefs"), true, null);

		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_SETTING)));

		// -----------------------------
		// Remove file
		handler.clearResourcesHandledMap();
		IFile copiedSettingFile = refWks.hbProject10_A.getFile(".settings/newSettingfile.prefs");
		copiedSettingFile.delete(true, null);
		resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_SETTING)));
	}

	// ----Private platform resource change
	public void testPrivatePlatformResourceChanged() throws Exception {
		IFolder svnFolder = refWks.hbProject20_D.getFolder(".svn");
		svnFolder.create(true, true, new NullProgressMonitor());
		IFile svnFile = refWks.hbProject20_D.getFile(".svn/entries");
		svnFile.create(new ByteArrayInputStream("entry".getBytes()), false, new NullProgressMonitor());

		handler.clearResourcesHandledMap();
		assertNotNull(svnFolder);
		assertTrue(svnFolder.exists());

		// delete file
		assertNotNull(svnFile);
		assertTrue(svnFile.isAccessible());

		synchronizedDeleteFile(svnFile);

		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertEquals(0, resourcesHandled.size());

	}

	// File changed
	public void testFileChanged() throws CoreException, OperationCanceledException, InterruptedException {
		// Hummingbird File
		// Change contents
		IFile hbFile_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);

		handler.clearResourcesHandledMap();
		synchronizedSetFileContents(hbFile_1, hbFile_2.getContents());
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled.contains(new ResourceHandled(hbFile_1.getFullPath(), MockResourceChangedHandler.CHANGED_FILE)));
		// ---------------------------------------
		// Modify resource
		handler.clearResourcesHandledMap();

		Resource hbResource10 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);
		assertNotNull(hbResource10);
		assertFalse(hbResource10.getContents().isEmpty());
		assertTrue(hbResource10.getContents().get(0) instanceof Application);

		final Application testApplicationHB10 = (Application) hbResource10.getContents().get(0);
		assertEquals(1, testApplicationHB10.getComponents().size());
		assertEquals(1, testApplicationHB10.getInterfaces().size());
		final Component testComponent = testApplicationHB10.getComponents().get(0);
		final Interface testInterface = testApplicationHB10.getInterfaces().get(0);
		ResourceSaveIndicator resourceSaveIndicator = new ResourceSaveIndicator(refWks.editingDomain10);
		assertFalse(resourceSaveIndicator.isDirty(hbResource10));
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, new Runnable() {
				@Override
				public void run() {
					testComponent.setName("newName1");
					testInterface.setName("newName2");
					Component newComponent = Hummingbird10Factory.eINSTANCE.createComponent();
					newComponent.setName("newComponent");
					testApplicationHB10.getComponents().add(newComponent);

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		assertEquals("newName1", testComponent.getName());
		assertEquals("newName2", testInterface.getName());

		assertTrue(resourceSaveIndicator.isDirty(hbResource10));
		ModelSaveManager.INSTANCE.saveModel(hbResource10, false, null);
		resourcesHandled = handler.getResourcesHandled();
		assertEquals(1, resourcesHandled.size());
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3),
						MockResourceChangedHandler.CHANGED_FILE)));
		// =======================================================
		// Hummingbird 20 resource
		handler.clearResourcesHandledMap();
		Resource hbResource20 = getProjectResource(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(hbResource20);

		assertFalse(hbResource20.getContents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application modelRoot20 = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) hbResource20
				.getContents().get(0);
		assertNotNull(modelRoot20);
		assertFalse(modelRoot20.getComponents().isEmpty());
		final org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component20 = modelRoot20.getComponents().get(0);

		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, new Runnable() {
				@Override
				public void run() {
					component20.setName("newName3");

				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue(ModelSaveManager.INSTANCE.isDirty(hbResource20));
		ModelSaveManager.INSTANCE.saveModel(hbResource20, false, null);
		resourcesHandled = handler.getResourcesHandled();

		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject20_D.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1),
						MockResourceChangedHandler.CHANGED_FILE)));
		// =======================================================
		// UML2
		// Change contents
		IFile uml2File_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		IFile uml2File_2 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		IFile uml2File_3 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		handler.clearResourcesHandledMap();
		synchronizedSetFileContents(uml2File_1, uml2File_2.getContents());
		synchronizedSetFileContents(uml2File_3, uml2File_2.getContents());
		resourcesHandled = handler.getResourcesHandled();
		assertEquals(2, resourcesHandled.size());
		assertTrue(resourcesHandled.contains(new ResourceHandled(uml2File_1.getFullPath(), MockResourceChangedHandler.CHANGED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(uml2File_3.getFullPath(), MockResourceChangedHandler.CHANGED_FILE)));

		// Modify resource
		handler.clearResourcesHandledMap();

		Resource uml2Resource_1 = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		Resource uml2Resource_2 = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);

		assertFalse(uml2Resource_1.getContents().isEmpty());
		Model modelRoot1 = (Model) uml2Resource_1.getContents().get(0);
		assertNotNull(modelRoot1);
		assertFalse(modelRoot1.getPackagedElements().isEmpty());
		final PackageableElement element1 = modelRoot1.getPackagedElements().get(0);

		assertFalse(uml2Resource_2.getContents().isEmpty());
		Model modelRoot2 = (Model) uml2Resource_2.getContents().get(0);
		assertNotNull(modelRoot2);
		assertFalse(modelRoot2.getPackagedElements().isEmpty());
		final PackageableElement element2 = modelRoot2.getPackagedElements().get(0);
		final String newName1 = "newName1";
		final String newName2 = "newName2";
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomainUml2, new Runnable() {
				@Override
				public void run() {
					// Rename objects in first resource
					element1.setName(newName1);
					element2.setName(newName2);
				}
			}, "Modify model");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
		assertEquals(newName1, element1.getName());
		assertEquals(newName2, element2.getName());
		assertTrue(ModelSaveManager.INSTANCE.isDirty(uml2Resource_1));
		assertTrue(ModelSaveManager.INSTANCE.isDirty(uml2Resource_2));

		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject20_E, false, null);

		resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(uml2File_1.getFullPath(), MockResourceChangedHandler.CHANGED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(uml2File_2.getFullPath(), MockResourceChangedHandler.CHANGED_FILE)));

	}

	// ##############################################
	// ############# REMOVE #########################
	// ##############################################
	// Project removed
	public void testProjectRemoved() throws Exception {
		handler.clearResourcesHandledMap();
		synchronizedDeleteProject(refWks.hbProject10_A);

		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();

		assertTrue(resourcesHandled.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.REMOVED_PROJECT)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4),
						MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5),
						MockResourceChangedHandler.REMOVED_FILE)));

	}

	// Folder removed
	public void testFolderRemoved() throws CoreException {
		handler.clearResourcesHandledMap();

		IFolder folder = refWks.hbProject10_F.getFolder(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		folder.delete(true, null);
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(
				resourcesHandled
						.contains(
								new ResourceHandled(
										refWks.hbProject10_F.getFullPath()
												.append(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1 + "/"
														+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10F_1),
										MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(
				resourcesHandled
						.contains(
								new ResourceHandled(
										refWks.hbProject10_F.getFullPath()
												.append(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1 + "/"
														+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10F_2),
										MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(
				resourcesHandled
						.contains(
								new ResourceHandled(
										refWks.hbProject10_F.getFullPath()
												.append(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1 + "/"
														+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10F_3),
										MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(folder.getFullPath(), MockResourceChangedHandler.REMOVED_FOLDER)));
	}

	// file removed
	public void testFileRemoved() throws Exception {
		// Hummingbird files
		handler.clearResourcesHandledMap();
		IFile hbFile_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);

		// Delete files
		synchronizedDeleteFile(hbFile_1);
		synchronizedDeleteFile(hbFile_2);
		// Verify the visitor
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(hbFile_1.getFullPath(), MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(hbFile_2.getFullPath(), MockResourceChangedHandler.REMOVED_FILE)));

		// ====================================
		// UML2 files
		handler.clearResourcesHandledMap();
		IFile uml2File_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		IFile uml2File_2 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		IFile uml2File_3 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		synchronizedDeleteFile(uml2File_1);
		synchronizedDeleteFile(uml2File_2);
		synchronizedDeleteFile(uml2File_3);
		// Delete files
		// Verify the visitor
		resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(uml2File_1.getFullPath(), MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(uml2File_2.getFullPath(), MockResourceChangedHandler.REMOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(uml2File_3.getFullPath(), MockResourceChangedHandler.REMOVED_FILE)));

	}

	// File moved
	public void testFileMoved() throws CoreException {
		// Hummingbird files
		handler.clearResourcesHandledMap();
		IFile hbFile_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);

		IPath targetLocation1 = refWks.hbProject10_F.getFullPath();
		// Delete filesMOVED_FILE
		synchronizedMoveFile(hbFile_1, targetLocation1.append(hbFile_1.getName()));
		synchronizedMoveFile(hbFile_2, targetLocation1.append(hbFile_2.getName()));

		// Verify the visitor
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(hbFile_1.getFullPath(), MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(hbFile_2.getFullPath(), MockResourceChangedHandler.MOVED_FILE)));

		// ====================================
		// UML2 files
		handler.clearResourcesHandledMap();
		IFile uml2File_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		IFile uml2File_2 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		IFile uml2File_3 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		IPath targetLocation2 = refWks.hbProject20_D.getFullPath();
		synchronizedMoveFile(uml2File_1, targetLocation2.append(uml2File_1.getName()));
		synchronizedMoveFile(uml2File_2, targetLocation2.append(uml2File_2.getName()));
		synchronizedMoveFile(uml2File_3, targetLocation2.append(uml2File_3.getName()));
		// Delete files
		// Verify the visitor
		resourcesHandled = handler.getResourcesHandled();
		resourcesHandled.remove(new ResourceHandled(uml2File_1.getFullPath(), MockResourceChangedHandler.MOVED_FILE));
		resourcesHandled.remove(new ResourceHandled(uml2File_2.getFullPath(), MockResourceChangedHandler.MOVED_FILE));
		resourcesHandled.remove(new ResourceHandled(uml2File_3.getFullPath(), MockResourceChangedHandler.MOVED_FILE));
		assertEquals("Events which are unexpected: " + resourcesHandled.toString(), 0, resourcesHandled.size());
	}

	// Folder moved
	public void testFolderMoved() throws CoreException {

		// create source folder
		handler.clearResourcesHandledMap();
		IFolder sourceFolder = refWks.hbProject10_A.getFolder("New Folder");
		assertFalse(sourceFolder.exists());
		sourceFolder.create(true, true, null);

		// target location
		IPath targetFolderLocation = refWks.hbProject10_F.getFullPath();

		// move folder
		synchronizedMoveFolder(sourceFolder, targetFolderLocation.append(sourceFolder.getName()));

		// Verify the visitor
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(sourceFolder.getFullPath(), MockResourceChangedHandler.ADD_FOLDER)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(sourceFolder.getFullPath(), MockResourceChangedHandler.MOVED_FOLDER)));
	}

	// ##############################################
	// ############# RENAME #########################
	// ##############################################

	// Project renamed
	public void testProjectRenamed() throws Exception {
		handler.clearResourcesHandledMap();
		synchronizedRenameProject(refWks.hbProject10_A, "NewProject");

		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath(), MockResourceChangedHandler.CHANGED_PROJECT_RENAMED)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1),
						MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2),
						MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3),
						MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_4),
						MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled
				.contains(new ResourceHandled(refWks.hbProject10_A.getFullPath().append(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_5),
						MockResourceChangedHandler.MOVED_FILE)));

	}

	// file renamed
	public void testFileRenamed() throws CoreException {
		// Hummingbird files
		handler.clearResourcesHandledMap();
		IFile hbFile_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IPath oldFilePath1 = hbFile_1.getFullPath();
		IPath oldFilePath2 = hbFile_2.getFullPath();

		// Rename files
		synchronizedRenameFile(hbFile_1, hbFile_1.getName() + "_renamed");
		synchronizedRenameFile(hbFile_2, hbFile_2.getName() + "_renamed");

		// Verify the visitor
		List<ResourceHandled> resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(oldFilePath1, MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(oldFilePath2, MockResourceChangedHandler.MOVED_FILE)));

		// ====================================
		// UML2 files
		handler.clearResourcesHandledMap();
		IFile uml2File_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		IFile uml2File_2 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		IFile uml2File_3 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		IPath oldUml2FilePath1 = uml2File_1.getFullPath();
		IPath oldUml2FilePath2 = uml2File_2.getFullPath();
		IPath oldUml2FilePath3 = uml2File_3.getFullPath();
		// Rename files
		synchronizedRenameFile(uml2File_1, uml2File_1.getName() + "_renamed");
		synchronizedRenameFile(uml2File_2, uml2File_2.getName() + "_renamed");
		synchronizedRenameFile(uml2File_3, uml2File_3.getName() + "_renamed");
		// Verify the visitor
		resourcesHandled = handler.getResourcesHandled();
		assertTrue(resourcesHandled.contains(new ResourceHandled(oldUml2FilePath1, MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(oldUml2FilePath2, MockResourceChangedHandler.MOVED_FILE)));
		assertTrue(resourcesHandled.contains(new ResourceHandled(oldUml2FilePath3, MockResourceChangedHandler.MOVED_FILE)));
	}

}
