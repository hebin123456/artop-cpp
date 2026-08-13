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
package org.eclipse.sphinx.testutils.integration.referenceworkspace.tests;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird10.Interface;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.testutils.integration.ReferenceWorkspaceChangeListener;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

@SuppressWarnings("nls")
public class ReferenceWorkspaceChangeListenerTest extends DefaultIntegrationTestCase {

	private ReferenceWorkspaceChangeListener referenceWorkspaceChangeListener = getReferenceWorkspaceChangeListener();

	public ReferenceWorkspaceChangeListenerTest() {
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

		super.setUp();
		assertNotNull(refWks.hbProject10_A);
		assertNotNull(refWks.hbProject10_F);
		assertNotNull(refWks.hbProject20_E);
		assertNotNull(refWks.hbProject20_D);
	}

	// ##############################################
	// ############# ADD ############################
	// ##############################################

	// Create new files
	public void testFileCreated() throws CoreException {
		// Create new Hummingbird file
		IPath newHbFilePath = refWks.hbProject10_A.getFullPath().append("newHbFile.hummingbird");
		Application hb10Application = Hummingbird10Factory.eINSTANCE.createApplication();
		EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, newHbFilePath, Hummingbird10Package.eCONTENT_TYPE, hb10Application, false,
				null);
		IFile addedHbFile = EcorePlugin.getWorkspaceRoot().getFile(newHbFilePath);
		assertNotNull(addedHbFile);
		assertTrue(addedHbFile.isAccessible());
		// Check that event creating new file was handled
		Collection<IFile> addedFiles = referenceWorkspaceChangeListener.getAddedFiles();
		assertEquals(1, addedFiles.size());
		assertTrue(addedFiles.contains(addedHbFile));

		// Create new UML2 File
		IPath newUml2FilePath = refWks.hbProject10_A.getFullPath().append("newUml2File.uml");
		Model uml2Model = UMLFactory.eINSTANCE.createModel();
		EcorePlatformUtil.saveNewModelResource(refWks.editingDomainUml2, newUml2FilePath, UMLPackage.eCONTENT_TYPE, uml2Model, false, null);

		IFile addedUml2File = EcorePlugin.getWorkspaceRoot().getFile(newUml2FilePath);
		assertNotNull(addedUml2File);
		assertTrue(addedUml2File.isAccessible());
		assertEquals(2, addedFiles.size());
		assertTrue(addedFiles.contains(addedUml2File));

	}

	// ##############################################
	// ############# CHANGE #########################
	// ##############################################
	// Project opened

	// Project Description Change
	public void testProjectDescriptonChanged() throws CoreException {
		// Update Project dependencies
		IProjectDescription projectDesc = refWks.hbProject20_E.getDescription();
		// Remove dependency from hbProject20E to 20D
		projectDesc.setReferencedProjects(new IProject[] {});
		refWks.hbProject20_E.setDescription(projectDesc, null);

		Collection<IProject> projectsWithChangedDescriptions = referenceWorkspaceChangeListener.getProjectsWithChangedDescription();
		assertEquals(1, projectsWithChangedDescriptions.size());
		assertTrue(projectsWithChangedDescriptions.contains(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));

		// Add nature
		String natureId = "org.eclipse.jdt.core.javanature";
		ExtendedPlatform.addNature(refWks.hbProject10_A, natureId, null);
		assertEquals(2, projectsWithChangedDescriptions.size());
		assertTrue(projectsWithChangedDescriptions.contains(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));

		// Remove nature
		ExtendedPlatform.removeNature(refWks.hbProject10_A, natureId, null);
		assertEquals(2, projectsWithChangedDescriptions.size());
		assertTrue(projectsWithChangedDescriptions.contains(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
	}

	// Project Setting change
	public void testProjectSettingsChanged() throws CoreException {
		// Change the metamodel version of project
		IFile settingFile_hb10 = refWks.hbProject10_A.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");
		IFile settingFile_hb20 = refWks.hbProject20_E.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");

		assertTrue(settingFile_hb10.isAccessible());
		assertTrue(settingFile_hb20.isAccessible());

		// Set content in settingFile_hb10 to settingFile_hb20 to change metamodel vesion of hbProject10_A
		synchronizedSetFileContents(settingFile_hb10, settingFile_hb20.getContents());

		Map<IProject, Collection<String>> projectsWithChangedSettings = referenceWorkspaceChangeListener.getProjectsWithChangedSettings();
		assertTrue(projectsWithChangedSettings.keySet().contains(refWks.hbProject10_A.getName()));
		assertTrue(projectsWithChangedSettings.get(refWks.hbProject10_A.getName()).contains("org.eclipse.sphinx.examples.hummingbird.ide.prefs"));

	}

	// Folder changed
	// ----Settings folder: covered by testProjectSettingsChanged

	public void testSettingsFolderChanged() throws CoreException {

		// Create new file
		IFile settingFile_hb10 = refWks.hbProject10_A.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");
		IFile settingFile_hb20 = refWks.hbProject20_E.getFile(".settings/org.eclipse.sphinx.examples.hummingbird.ide.prefs");

		assertTrue(settingFile_hb10.isAccessible());
		assertTrue(settingFile_hb20.isAccessible());

		synchronizedSetFileContents(settingFile_hb10, settingFile_hb20.getContents());

		settingFile_hb20.copy(refWks.hbProject10_A.getFullPath().append(".settings/newSettingfile.prefs"), true, null);

		Map<IProject, Collection<String>> projectsWithChangedSettings = referenceWorkspaceChangeListener.getProjectsWithChangedSettings();
		assertEquals(1, projectsWithChangedSettings.size());
		assertTrue(projectsWithChangedSettings.keySet().contains(refWks.hbProject10_A.getName()));
		assertTrue(projectsWithChangedSettings.get(refWks.hbProject10_A.getName()).contains("newSettingfile.prefs"));
		assertTrue(projectsWithChangedSettings.get(refWks.hbProject10_A.getName()).contains("org.eclipse.sphinx.examples.hummingbird.ide.prefs"));
		// -----------------------------
		// Remove file
		IFile copiedSettingFile = refWks.hbProject10_A.getFile(".settings/newSettingfile.prefs");
		copiedSettingFile.delete(true, null);
		assertTrue(projectsWithChangedSettings.keySet().contains(refWks.hbProject10_A.getName()));
		assertTrue(projectsWithChangedSettings.get(refWks.hbProject10_A.getName()).contains("newSettingfile.prefs"));

	}

	// File changed
	public void testFileChanged() throws CoreException, OperationCanceledException, InterruptedException {

		// Hummingbird File
		// Change contents
		IFile hbFile_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);

		synchronizedSetFileContents(hbFile_1, hbFile_2.getContents());
		Collection<IFile> changedFiles = referenceWorkspaceChangeListener.getChangedFiles();
		assertEquals(1, changedFiles.size());
		assertTrue(changedFiles.contains(hbFile_1));

		// Modify resource
		Resource hbResource10 = getProjectResource(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(hbResource10);
		assertFalse(hbResource10.getContents().isEmpty());
		assertTrue(hbResource10.getContents().get(0) instanceof Application);

		final Application testApplicationHB10 = (Application) hbResource10.getContents().get(0);
		assertEquals(1, testApplicationHB10.getComponents().size());
		assertEquals(1, testApplicationHB10.getInterfaces().size());
		final Component testComponent = testApplicationHB10.getComponents().get(0);
		final Interface testInterface = testApplicationHB10.getInterfaces().get(0);
		assertFalse(ModelSaveManager.INSTANCE.isDirty(hbResource10));
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

		assertTrue(ModelSaveManager.INSTANCE.isDirty(hbResource10));

		ModelSaveManager.INSTANCE.saveModel(hbResource10, false, null);

		assertEquals(1, changedFiles.size());
		assertTrue(changedFiles.contains(EcorePlatformUtil.getFile(hbResource10)));
		// =======================================================
		// Hummingbird 20 resource
		Resource hbResource20 = getProjectResource(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(hbResource20);

		assertFalse(SaveIndicatorUtil.isDirty(refWks.editingDomain20, hbResource20));

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
		assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomain20, hbResource20));

		ModelSaveManager.INSTANCE.saveModel(hbResource20, false, null);

		assertTrue(changedFiles.contains(EcorePlatformUtil.getFile(hbResource20)));
		// =======================================================
		// UML2
		// Change contents
		IFile uml2File_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		IFile uml2File_2 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		IFile uml2File_3 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		synchronizedSetFileContents(uml2File_1, uml2File_2.getContents());
		synchronizedSetFileContents(uml2File_3, uml2File_2.getContents());

		assertTrue(changedFiles.contains(EcorePlatformUtil.getFile(uml2File_1)));
		assertTrue(changedFiles.contains(EcorePlatformUtil.getFile(uml2File_3)));

		// Modify resource
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

		assertTrue(changedFiles.contains(EcorePlatformUtil.getFile(uml2File_1)));
		assertTrue(changedFiles.contains(EcorePlatformUtil.getFile(uml2File_3)));

	}

	// File moved
	public void testFileMoved() throws CoreException {
		// Hummingbird files
		IFile hbFile_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);

		IPath targetLocation = refWks.hbProject10_F.getFullPath();
		IPath targetFilePath1 = targetLocation.append(hbFile_1.getName());
		IPath targetFilePath2 = targetLocation.append(hbFile_2.getName());
		// Delete filesMOVED_FILE
		synchronizedMoveFile(hbFile_1, targetFilePath1);
		synchronizedMoveFile(hbFile_2, targetFilePath2);

		// Verify the visitor
		Collection<IFile> movedFiles = referenceWorkspaceChangeListener.getAddedFiles();
		IFile movedFile1 = EcorePlugin.getWorkspaceRoot().getFile(targetFilePath1);
		assertNotNull(movedFile1);
		assertTrue(movedFile1.isAccessible());
		IFile movedFile2 = EcorePlugin.getWorkspaceRoot().getFile(targetFilePath1);
		assertNotNull(movedFile2);
		assertTrue(movedFile2.isAccessible());

		assertTrue(movedFiles.contains(movedFile1));
		assertTrue(movedFiles.contains(movedFile2));

		// ====================================
		// UML2 files
		IFile uml2File_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		IFile uml2File_2 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		IFile uml2File_3 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		IPath targetLocation2 = refWks.hbProject20_D.getFullPath();
		IPath targetFilePath21 = targetLocation2.append(uml2File_1.getName());
		IPath targetFilePath22 = targetLocation2.append(uml2File_2.getName());
		IPath targetFilePath23 = targetLocation2.append(uml2File_3.getName());

		synchronizedMoveFile(uml2File_1, targetFilePath21);
		synchronizedMoveFile(uml2File_2, targetFilePath22);
		synchronizedMoveFile(uml2File_3, targetFilePath23);
		// Delete files
		// Verify the visitor

		IFile movedFile21 = EcorePlugin.getWorkspaceRoot().getFile(targetFilePath21);
		assertNotNull(movedFile21);
		assertTrue(movedFile21.isAccessible());
		IFile movedFile22 = EcorePlugin.getWorkspaceRoot().getFile(targetFilePath22);
		assertNotNull(movedFile22);
		assertTrue(movedFile22.isAccessible());
		IFile movedFile23 = EcorePlugin.getWorkspaceRoot().getFile(targetFilePath23);
		assertNotNull(movedFile23);
		assertTrue(movedFile23.isAccessible());

		assertTrue(movedFiles.contains(movedFile21));
		assertTrue(movedFiles.contains(movedFile22));
		assertTrue(movedFiles.contains(movedFile23));
	}

	// ##############################################
	// ############# RENAME #########################
	// ##############################################

	// Project renamed
	public void testProjectRenamed() throws Exception {
		synchronizedRenameProject(refWks.hbProject10_A, "NewProject");

		Collection<IProject> renamedProjects = referenceWorkspaceChangeListener.getRenamedProjects();
		assertTrue(renamedProjects.contains("NewProject"));

	}

	// file renamed
	public void testFileRenamed() throws CoreException {
		// Hummingbird files
		IFile hbFile_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_2 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		String newFileName1 = hbFile_1.getName() + "_renamed";
		String newFileName2 = hbFile_2.getName() + "_renamed";

		// Rename files
		synchronizedRenameFile(hbFile_1, newFileName1);
		synchronizedRenameFile(hbFile_2, newFileName2);

		IFile renamedFile1 = refWks.hbProject10_A.getFile(newFileName1);
		assertNotNull(renamedFile1);
		assertTrue(renamedFile1.isAccessible());

		IFile renamedFile2 = refWks.hbProject10_A.getFile(newFileName2);
		assertNotNull(renamedFile2);
		assertTrue(renamedFile2.isAccessible());

		// Verify the visitor
		Collection<IFile> addedFiles = referenceWorkspaceChangeListener.getAddedFiles();
		assertEquals(2, addedFiles.size());
		assertTrue(addedFiles.contains(renamedFile1));
		assertTrue(addedFiles.contains(renamedFile2));

		// ====================================
		// UML2 files
		IFile uml2File_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		IFile uml2File_2 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		IFile uml2File_3 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		String newUmlFileName1 = uml2File_1.getName() + "_renamed";
		String newUmlFileName2 = uml2File_2.getName() + "_renamed";
		String newUmlFileName3 = uml2File_3.getName() + "_renamed";

		// Rename files
		synchronizedRenameFile(uml2File_1, newUmlFileName1);
		synchronizedRenameFile(uml2File_2, newUmlFileName2);
		synchronizedRenameFile(uml2File_3, newUmlFileName3);
		// Verify the visitor

		IFile renamedUmlFile1 = refWks.hbProject20_E.getFile(newUmlFileName1);
		assertNotNull(renamedUmlFile1);
		assertTrue(renamedUmlFile1.isAccessible());

		IFile renamedUmlFile2 = refWks.hbProject20_E.getFile(newUmlFileName2);
		assertNotNull(renamedUmlFile2);
		assertTrue(renamedUmlFile2.isAccessible());

		IFile renamedUmlFile3 = refWks.hbProject20_E.getFile(newUmlFileName3);
		assertNotNull(renamedUmlFile3);
		assertTrue(renamedUmlFile3.isAccessible());

		assertTrue(addedFiles.contains(renamedUmlFile1));
		assertTrue(addedFiles.contains(renamedUmlFile2));
		assertTrue(addedFiles.contains(renamedUmlFile3));
	}
}
