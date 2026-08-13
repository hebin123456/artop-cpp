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
package org.eclipse.sphinx.tests.emf.workspace.integration.saving;

import java.io.ByteArrayInputStream;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Component;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

@SuppressWarnings("nls")
public class ModelSavingTest extends DefaultIntegrationTestCase {

	public ModelSavingTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	public void testSaveNewModelAndSaveProjects() throws Exception {
		Resource hbProject10_A_Hb10Resource_1 = getProjectResource(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		Resource hbProject10_B_Hb10Resource_1 = getProjectResource(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		Resource hbProject20_A_Hb20Resource_1 = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(hbProject10_A_Hb10Resource_1);
		assertNotNull(hbProject10_B_Hb10Resource_1);
		assertNotNull(hbProject20_A_Hb20Resource_1);

		assertFalse(hbProject10_A_Hb10Resource_1.getContents().isEmpty());
		EObject hbProject10_A_Hb10Resource_1_modelRoot = hbProject10_A_Hb10Resource_1.getContents().get(0);
		assertNotNull(hbProject10_A_Hb10Resource_1_modelRoot);
		Application hbProject10_A_Hb10Resource_1_ModelRoot = (Application) hbProject10_A_Hb10Resource_1_modelRoot;
		assertNotNull(hbProject10_A_Hb10Resource_1_ModelRoot);
		assertFalse(hbProject10_A_Hb10Resource_1_ModelRoot.getComponents().isEmpty());
		final Component hbProject10_A_Hb10Resource_1_Component = hbProject10_A_Hb10Resource_1_ModelRoot.getComponents().get(0);

		Runnable runnable1 = new Runnable() {
			@Override
			public void run() {
				hbProject10_A_Hb10Resource_1_Component.setName("NewName");
			}
		};

		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, runnable1, "Integration Test Save");

		assertFalse(hbProject20_A_Hb20Resource_1.getContents().isEmpty());
		EObject hbProject20_A_Hb20Resource_1_modelRoot = hbProject20_A_Hb20Resource_1.getContents().get(0);
		assertNotNull(hbProject20_A_Hb20Resource_1_modelRoot);

		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application hbProject20_A_Hb20Resource_1_ModelRoot = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) hbProject20_A_Hb20Resource_1_modelRoot;
		assertNotNull(hbProject20_A_Hb20Resource_1_ModelRoot);

		assertFalse(hbProject20_A_Hb20Resource_1_ModelRoot.getComponents().isEmpty());
		final org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component hbProject20_A_Hb20Resource_1_Component = hbProject20_A_Hb20Resource_1_ModelRoot
				.getComponents().get(0);

		Runnable runnable2 = new Runnable() {
			@Override
			public void run() {
				hbProject20_A_Hb20Resource_1_Component.setName("NewName");
			}
		};

		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable2, "Integration Test Save");

		assertFalse(hbProject10_B_Hb10Resource_1.getContents().isEmpty());
		EObject hbProject10_B_Hb10Resource_1_modelRoot = hbProject10_B_Hb10Resource_1.getContents().get(0);
		assertNotNull(hbProject10_B_Hb10Resource_1_modelRoot);

		Application hbProject10_B_Hb10Resource_1_ModelRoot = (Application) hbProject10_B_Hb10Resource_1_modelRoot;
		assertNotNull(hbProject10_B_Hb10Resource_1_ModelRoot);
		assertFalse(hbProject10_B_Hb10Resource_1_ModelRoot.getComponents().isEmpty());
		final Component hbProject10_B_Hb10Resource_1_Component = hbProject10_B_Hb10Resource_1_ModelRoot.getComponents().get(0);

		Runnable runnable3 = new Runnable() {
			@Override
			public void run() {
				hbProject10_B_Hb10Resource_1_Component.setName("NewName");
			}
		};

		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, runnable3, "Integration Test Save");

		assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomain10, hbProject10_A_Hb10Resource_1));
		assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomain10, hbProject10_B_Hb10Resource_1));
		assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomain20, hbProject20_A_Hb20Resource_1));

		String resourceName = "NewUMLResource.uml";
		IPath resourcePath = refWks.hbProject10_A.getFullPath().append("/" + resourceName);
		Model uml2ModelRoot = UMLFactory.eINSTANCE.createModel();
		EcorePlatformUtil.saveNewModelResource(refWks.editingDomain10, resourcePath, UMLPackage.eCONTENT_TYPE, uml2ModelRoot, false, null);
		waitForModelLoading();

		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject10_A, false, null);
		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject10_B, false, null);
		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject20_A, false, null);
	}

	public void testSaveProjectAndNewResourceCreation() throws Exception {
		Resource hbProject10_A_Hb10Resource_1 = getProjectResource(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertFalse(hbProject10_A_Hb10Resource_1.getContents().isEmpty());
		EObject hbProject10_A_Hb10Resource_1_modelRoot = hbProject10_A_Hb10Resource_1.getContents().get(0);
		assertNotNull(hbProject10_A_Hb10Resource_1_modelRoot);

		assertTrue(hbProject10_A_Hb10Resource_1_modelRoot instanceof Application);
		Application hbProject10_A_Hb10Resource_1_HbModelRoot = (Application) hbProject10_A_Hb10Resource_1_modelRoot;
		assertFalse(hbProject10_A_Hb10Resource_1_HbModelRoot.getComponents().isEmpty());
		final Component hbProject10_A_Hb10Resource_1_Component = hbProject10_A_Hb10Resource_1_HbModelRoot.getComponents().get(0);

		Runnable runnable1 = new Runnable() {
			@Override
			public void run() {
				hbProject10_A_Hb10Resource_1_Component.setName("NewName");
			}
		};

		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, runnable1, "Integration Test Save");

		assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomain10, hbProject10_A_Hb10Resource_1));

		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject10_A, false, null);

		// creation of a new file in hbProject20_A
		IFile newFile = refWks.hbProject20_A.getFile("anyFile1");
		newFile.create(new ByteArrayInputStream("Just anything".getBytes()), true, null);
	}

	public void testNewResourceCreationAndSaveProject() throws Exception {
		Resource hbProject10_A_Hb10Resource_1 = getProjectResource(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertFalse(hbProject10_A_Hb10Resource_1.getContents().isEmpty());
		EObject hbProject10_A_Hb10Resource_1_modelRoot = hbProject10_A_Hb10Resource_1.getContents().get(0);
		assertNotNull(hbProject10_A_Hb10Resource_1_modelRoot);

		assertTrue(hbProject10_A_Hb10Resource_1_modelRoot instanceof Application);
		Application hbProject10_A_Hb10Resource_1_HbModelRoot = (Application) hbProject10_A_Hb10Resource_1_modelRoot;
		assertFalse(hbProject10_A_Hb10Resource_1_HbModelRoot.getComponents().isEmpty());
		final Component hbProject10_A_Hb10Resource_1_Component = hbProject10_A_Hb10Resource_1_HbModelRoot.getComponents().get(0);

		Runnable runnable1 = new Runnable() {
			@Override
			public void run() {
				hbProject10_A_Hb10Resource_1_Component.setName("NewName");
			}
		};

		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain10, runnable1, "Integration Test Save");

		assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomain10, hbProject10_A_Hb10Resource_1));

		// creation of a new file in hbProject20_A
		IFile newFile = refWks.hbProject20_A.getFile("anyFile2");
		newFile.create(new ByteArrayInputStream("Just anything".getBytes()), true, null);
		// saving of hbProject
		ModelSaveManager.INSTANCE.saveProject(refWks.hbProject10_A, false, null);

	}
}
