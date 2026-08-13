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
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel impelmentations to subclass EObjectImpl
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.integration.syncing;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.examples.hummingbird.ide.natures.HummingbirdNature;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Port;

@SuppressWarnings("nls")
public class ModelSyncingTest extends DefaultIntegrationTestCase {

	public ModelSyncingTest() {
		// Start tests with projects in reference workspace being closed
		setProjectsClosedOnStartup(true);
	}

	// + testProjectOpened
	// o right click on project + Open Project
	// -> all model resources in project must be loaded
	// -> corresponding model descriptor must be added
	// -> all proxies in project (and referencing projects) must be resolved or
	// blacklisted
	public void testProjectOpened() throws Exception {
		int resourcesInEditingDomain20 = 0;
		int resourcesInEditingDomain10 = 0;
		int resourcesInEditingDomainUml2 = 0;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// ########### HUMMINGBIRD 10 project #######################
		// open project hbProject10_A
		assertFalse(refWks.hbProject10_A.isOpen());
		synchronizedOpenProject(refWks.hbProject10_A);

		assertTrue(refWks.hbProject10_A.isOpen());
		resourcesInEditingDomain10 += refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).size();
		// check if resources have been created in editing domain
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		Resource project10_A_Resource10_1 = getProjectResource(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(project10_A_Resource10_1);
		Resource project10_A_Resource10_2 = getProjectResource(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertNotNull(project10_A_Resource10_2);
		Resource project10_A_Resource10_3 = getProjectResource(refWks.hbProject10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertNotNull(project10_A_Resource10_3);

		// check if modelDescriptors have been put in place correctly
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);
		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_A)) {
			assertSame(refWks.editingDomain10, modelDescriptor.getEditingDomain());
			assertSame(refWks.hbProject10_A, modelDescriptor.getRoot());
		}

		// check if all proxies have been resolved

		assertProxiesResolved(project10_A_Resource10_1);
		assertProxiesResolved(project10_A_Resource10_2);
		assertProxiesResolved(project10_A_Resource10_3);

		// open project hbProject10_B
		assertFalse(refWks.hbProject10_B.isOpen());

		synchronizedOpenProject(refWks.hbProject10_B);

		assertTrue(refWks.hbProject10_B.isOpen());

		assertTrue(refWks.hbProject10_A.isOpen());
		resourcesInEditingDomain10 += refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B).size();
		// check if resources have been created in editing domain
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		Resource project10_B_Resource10_1 = getProjectResource(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		assertNotNull(project10_B_Resource10_1);
		Resource project10_B_Resource10_2 = getProjectResource(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_2);
		assertNotNull(project10_B_Resource10_2);
		Resource project10_B_Resource10_3 = getProjectResource(refWks.hbProject10_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_3);
		assertNotNull(project10_B_Resource10_3);
		// check if modelDescriptors have been put in place correctly
		assertProjectModelsSizeEquals(refWks.hbProject10_B, 1);
		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_B)) {
			assertSame(refWks.editingDomain10, modelDescriptor.getEditingDomain());
			assertSame(refWks.hbProject10_B, modelDescriptor.getRoot());

		}

		// check if all proxies have been resolved
		assertProxiesResolved(project10_B_Resource10_1);
		assertProxiesResolved(project10_B_Resource10_2);
		assertProxiesResolved(project10_B_Resource10_3);

		// ########### HB 20 projects #######################
		assertFalse(refWks.hbProject20_A.isOpen());

		synchronizedOpenProject(refWks.hbProject20_A);

		assertTrue(refWks.hbProject20_A.isOpen());
		resourcesInEditingDomain20 += refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE)
				.size();
		resourcesInEditingDomainUml2 = resourcesInEditingDomainUml2
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, UML2MMDescriptor.INSTANCE).size();

		// check if resources have been created in editing domain
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		Resource project20_A_Resource20_1 = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(project20_A_Resource20_1);
		Resource project20_A_Resource20_2 = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		assertNotNull(project20_A_Resource20_2);
		Resource project20_A_Resource20_3 = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		assertNotNull(project20_A_Resource20_3);
		// check if modelDescriptors have been put in place correctly
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);
		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A)) {
			assertSame(refWks.editingDomain20, modelDescriptor.getEditingDomain());
			assertSame(refWks.hbProject20_A, modelDescriptor.getRoot());
		}

		// check if all proxies have been resolved
		assertProxiesResolved(project20_A_Resource20_1);
		assertProxiesResolved(project20_A_Resource20_2);
		assertProxiesResolved(project20_A_Resource20_3);

		assertFalse(refWks.hbProject20_D.isOpen());
		assertFalse(refWks.hbProject20_E.isOpen());

		synchronizedOpenProject(refWks.hbProject20_E);
		synchronizedOpenProject(refWks.hbProject20_D);

		assertTrue(refWks.hbProject20_D.isOpen());
		assertTrue(refWks.hbProject20_E.isOpen());
		resourcesInEditingDomain20 = resourcesInEditingDomain20
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size()
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE).size();
		resourcesInEditingDomainUml2 = resourcesInEditingDomainUml2
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size()
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE).size();

		// check if resources have been created in editing domain
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		Resource project20_D_Resource20_1 = getProjectResource(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(project20_D_Resource20_1);
		Resource project20_D_Resource20_2 = getProjectResource(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertNotNull(project20_D_Resource20_2);
		Resource project20_D_Resource20_3 = getProjectResource(refWks.hbProject20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertNotNull(project20_D_Resource20_3);

		Resource project20_E_Resource20_1 = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertNotNull(project20_E_Resource20_1);
		Resource project20_E_Resource20_2 = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertNotNull(project20_E_Resource20_2);
		Resource project20_E_Resource20_3 = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertNotNull(project20_E_Resource20_3);
		assertProjectModelsSizeEquals(refWks.hbProject20_D, 2);

		int checkCount = 0;
		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_D)) {
			assertSame(refWks.hbProject20_D, modelDescriptor.getRoot());
			if (modelDescriptor.getEditingDomain().equals(refWks.editingDomain20)) {
				checkCount++;
			} else if (modelDescriptor.getEditingDomain().equals(refWks.editingDomainUml2)) {
				checkCount++;
			} else if (modelDescriptor.getEditingDomain().equals(refWks.editingDomain10)) {
				checkCount++;
			}
		}
		assertEquals(2, checkCount);
		checkCount = 0;
		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_E)) {
			assertSame(refWks.hbProject20_E, modelDescriptor.getRoot());
			if (modelDescriptor.getEditingDomain().equals(refWks.editingDomain20)) {
				checkCount++;
			} else if (modelDescriptor.getEditingDomain().equals(refWks.editingDomainUml2)) {
				checkCount++;
			} else if (modelDescriptor.getEditingDomain().equals(refWks.editingDomain10)) {
				checkCount++;
			}
		}
		assertEquals(2, checkCount);

		// check if all proxies have been resolved
		assertProxiesResolved(project20_D_Resource20_1);
		assertProxiesResolved(project20_D_Resource20_2);
		assertProxiesResolved(project20_D_Resource20_3);
		assertProxiesResolved(project20_E_Resource20_1);
		assertProxiesResolved(project20_E_Resource20_2);
		assertProxiesResolved(project20_E_Resource20_3);
	}

	// + testProjectRenamed
	// o right click on project + Rename
	// -> the project's name must change
	// -> all model resources in project must be remain loaded
	// -> corresponding model descriptor must stay in place
	// -> all proxies in project (and referencing projects) must remain resolved
	// or blacklisted
	public void testProjectRenamed() throws Exception {

		// test rename HB 20 project
		synchronizedOpenProject(refWks.hbProject20_D);

		int modelCount = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_D).size();
		// check initial project name
		assertEquals(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, refWks.hbProject20_D.getName());

		// Check if resources under the project are correctly loaded.
		Set<IFile> hbProject20_DFiles = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				Hummingbird20MMDescriptor.INSTANCE);
		Set<IFile> uml2Project20_DFiles = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, hbProject20_DFiles.size());
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, uml2Project20_DFiles.size());
		String projectName = DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "_NewName"; //$NON-NLS-1$

		synchronizedRenameProject(refWks.hbProject20_D, projectName);

		// Check if project name has been correctly changed
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		assertTrue(newProject.exists());

		// Check if resources under the project are still loaded.
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, hbProject20_DFiles.size());
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, uml2Project20_DFiles.size());

		// check if resources are correctly loaded
		Resource project20_D_Resource20_1 = getProjectResource(newProject, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(project20_D_Resource20_1);
		Resource project20_D_Resource20_2 = getProjectResource(newProject, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertNotNull(project20_D_Resource20_2);
		Resource project20_D_Resource20_3 = getProjectResource(newProject, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertNotNull(project20_D_Resource20_3);

		// check if proxies remain resolved
		assertProxiesResolved(project20_D_Resource20_1);
		assertProxiesResolved(project20_D_Resource20_2);
		assertProxiesResolved(project20_D_Resource20_3);

		// check if model descriptors are still in place
		assertProjectModelsSizeEquals(newProject, modelCount);
	}

	// + testProjectDescriptionChanged (nature)
	// o remove HB nature from HB .project file and save it with
	// Eclipse text editor
	// -> all model resources in project must be unloaded
	// -> corresponding model descriptor must be removed
	// o add HB nature to non HB .project file and save it with
	// Eclipse text editor
	// -> all model resources in project must be loaded
	// -> corresponding model descriptor must be added
	// -> all proxies in project (and referencing projects) must be resolved or
	// blacklisted
	public void testProjectDescriptionChangedNature() throws Exception {
		// Open hbProject20_A
		synchronizedOpenProject(refWks.hbProject20_A);

		assertTrue(refWks.hbProject20_A.hasNature(HummingbirdNature.ID));

		int hbProject20_A_Files20Count = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE).size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, hbProject20_A_Files20Count);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
		assertProjectHasNoModels(refWks.hbProject20_A, Hummingbird10MMDescriptor.INSTANCE);
		assertProjectHasModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject20_A, UML2MMDescriptor.INSTANCE);

		// Remove HB nature from hbProject20_A
		HummingbirdNature.removeFrom(refWks.hbProject20_A, null);
		waitForModelLoading();

		assertFalse(refWks.hbProject20_A.hasNature(HummingbirdNature.ID));

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
		assertProjectHasNoModels(refWks.hbProject20_A, Hummingbird10MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject20_A, UML2MMDescriptor.INSTANCE);

		assertProjectModelsSizeEquals(refWks.hbProject20_A, 0);

		// Add HB nature to hbProject20_A
		HummingbirdNature.addTo(refWks.hbProject20_A, null);
		waitForModelLoading();

		assertTrue(refWks.hbProject20_A.hasNature(HummingbirdNature.ID));
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, hbProject20_A_Files20Count);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
		assertProjectHasNoModels(refWks.hbProject20_A, Hummingbird10MMDescriptor.INSTANCE);
		assertProjectHasModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject20_A, UML2MMDescriptor.INSTANCE);

		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);

		Resource hbProject20_A_Resource20_1 = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(hbProject20_A_Resource20_1);
		Resource hbProject20_A_Resource20_2 = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		assertNotNull(hbProject20_A_Resource20_2);
		Resource hbProject20_A_Resource20_3 = getProjectResource(refWks.hbProject20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		assertNotNull(hbProject20_A_Resource20_3);

		assertProxiesResolved(hbProject20_A_Resource20_1);
		assertProxiesResolved(hbProject20_A_Resource20_2);
		assertProxiesResolved(hbProject20_A_Resource20_3);

		// Open hbProject10_A

		synchronizedOpenProject(refWks.hbProject10_A);

		assertTrue(refWks.hbProject10_A.hasNature(HummingbirdNature.ID));

		int hbProject10_A_Files10Count = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE).size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, hbProject10_A_Files10Count);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, hbProject20_A_Files20Count);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
		assertProjectHasModels(refWks.hbProject10_A, Hummingbird10MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject10_A, Hummingbird20MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject10_A, UML2MMDescriptor.INSTANCE);

		// Remove HB nature from hbProject10_A
		HummingbirdNature.removeFrom(refWks.hbProject10_A, null);
		waitForModelLoading();

		assertFalse(refWks.hbProject10_A.hasNature(HummingbirdNature.ID));
		// assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, hbProject20_A_Files20Count);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
		assertProjectHasNoModels(refWks.hbProject10_A, Hummingbird10MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject10_A, Hummingbird20MMDescriptor.INSTANCE);
		assertProjectHasNoModels(refWks.hbProject10_A, UML2MMDescriptor.INSTANCE);
	}

	// + testProjectDescriptionChanged (project references)
	// -> check for useles loading events.
	// -> resolve proxies in given projects and all referencing projects.
	public void testProjectDescriptionChangedNatureProjectReference() throws Exception {
		// open projects
		synchronizedOpenProject(refWks.hbProject20_A);

		synchronizedOpenProject(refWks.hbProject20_B);

		synchronizedOpenProject(refWks.hbProject20_C);

		synchronizedOpenProject(refWks.hbProject20_D);

		synchronizedOpenProject(refWks.hbProject20_E);

		synchronizedOpenProject(refWks.hbProject10_E);

		IProjectDescription hbProject3X_D_Desc = refWks.hbProject20_D.getDescription();

		assertNotNull(hbProject3X_D_Desc);

		IProjectDescription hbProject3X_E_Desc = refWks.hbProject20_E.getDescription();

		assertNotNull(hbProject3X_E_Desc);

		IProject[] referencedProjectsProject3X_E = hbProject3X_E_Desc.getReferencedProjects();

		assertNotNull(referencedProjectsProject3X_E);

		assertEquals(1, referencedProjectsProject3X_E.length);

		IProject[] referencedProjectsProject3X_D = hbProject3X_D_Desc.getReferencedProjects();

		assertNotNull(referencedProjectsProject3X_D);
		assertEquals(1, referencedProjectsProject3X_E.length);
		assertEquals(1, referencedProjectsProject3X_D.length);
		// check if project references are correctly set
		// references before removal.
		// ( <-- dependency ):
		// refWks.hbProject10_E <-- refWks.hbProject20_D <--
		// refWks.hbProject20_E
		assertTrue(referencedProjectsProject3X_E[0].equals(refWks.hbProject20_D));
		assertTrue(referencedProjectsProject3X_D[0].equals(refWks.hbProject10_E));

		// check if modelDescriptors have been put in place correctly
		assertProjectModelsSizeEquals(refWks.hbProject20_E, 3);
		assertProjectModelsSizeEquals(refWks.hbProject20_D, 3);

		// change references on project 3X E
		hbProject3X_E_Desc.setReferencedProjects(new IProject[] {});
		refWks.hbProject20_E.setDescription(hbProject3X_E_Desc, new NullProgressMonitor());
		waitForModelLoading();

		referencedProjectsProject3X_E = hbProject3X_E_Desc.getReferencedProjects();

		assertNotNull(referencedProjectsProject3X_E);

		assertEquals(0, referencedProjectsProject3X_E.length);

		referencedProjectsProject3X_D = hbProject3X_D_Desc.getReferencedProjects();

		assertNotNull(referencedProjectsProject3X_D);

		assertEquals(1, referencedProjectsProject3X_D.length);
		// references after removal.
		// ( X-- dependency removed)
		// ( <-- dependency )
		// refWks.hbProject10_E X- refWks.hbProject20_D X- refWks.hbProject20_E
		assertNotNull(referencedProjectsProject3X_E);
		assertEquals(0, referencedProjectsProject3X_E.length);

		assertProjectModelsSizeEquals(refWks.hbProject20_E, 2);
		assertProjectModelsSizeEquals(refWks.hbProject20_D, 3);

		// change references on project 3X D
		hbProject3X_D_Desc.setReferencedProjects(new IProject[] {});
		refWks.hbProject20_D.setDescription(hbProject3X_D_Desc, new NullProgressMonitor());
		waitForModelLoading();

		assertProjectModelsSizeEquals(refWks.hbProject20_D, 2);
	}

	// + testProjectClosed
	// o right click on project + Close Project
	// -> unsaved changes must be saved
	// -> all model resources in project must be unloaded
	// -> corresponding model descriptor must be removed
	public void testProjectClosed() throws Exception {
		// test project closed on HB 20 project

		synchronizedOpenProject(refWks.hbProject20_A);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20,
				refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE).size());
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);

		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);
		// close HB 20 project A
		synchronizedCloseProject(refWks.hbProject20_A);

		// check that resources have been unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);

		// check that model descriptors are removed
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 0);

		// test project closed on HUMMINGBIRD 10 project
		synchronizedOpenProject(refWks.hbProject10_A);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10,
				refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE).size());
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);

		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);

		synchronizedCloseProject(refWks.hbProject10_A);

		// check that resources have been unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);

		// check that model descriptors are removed
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 0);

	}

	// + testProjectDeleted
	// o right click on project + Delete
	// -> all model resources in project must be unloaded
	// -> corresponding model descriptor must be removed
	public void testProjectDeleted() throws Exception {

		int expectedHb20ResourcesForArProject20A = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE).size();
		int expectedHb20ResourcesForArProject20B = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size();
		int expectedResourcesInEditingDomain20 = expectedHb20ResourcesForArProject20A + expectedHb20ResourcesForArProject20B;
		int expectedUml2ResourcesForArProject20B = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE).size();

		int expectedUml2ResourcesForArProject20A = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, UML2MMDescriptor.INSTANCE).size();
		int expectedResourcesInEditingDomain3Uml2 = expectedUml2ResourcesForArProject20A + expectedUml2ResourcesForArProject20B;

		int expectedModelNumberForArProject20A = refWks.getReferenceProjectDescriptor(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A)
				.getReferenceModelDescriptors().size();
		int expectedModelNumberForArProject20B = refWks.getReferenceProjectDescriptor(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B)
				.getReferenceModelDescriptors().size();

		synchronizedOpenProject(refWks.hbProject20_A);
		synchronizedOpenProject(refWks.hbProject20_B);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, expectedResourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, expectedResourcesInEditingDomain3Uml2);

		assertProjectModelsSizeEquals(refWks.hbProject20_A, expectedModelNumberForArProject20A);
		assertProjectModelsSizeEquals(refWks.hbProject20_B, expectedModelNumberForArProject20B);

		synchronizedDeleteProject(refWks.hbProject20_A);
		expectedResourcesInEditingDomain20 = expectedResourcesInEditingDomain20 - expectedHb20ResourcesForArProject20A;
		expectedResourcesInEditingDomain3Uml2 = expectedResourcesInEditingDomain3Uml2 - expectedUml2ResourcesForArProject20A;
		// check that resources have been unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, expectedResourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, expectedResourcesInEditingDomain3Uml2);

		// check that model descriptors are removed
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 0);
		assertProjectModelsSizeEquals(refWks.hbProject20_B, expectedModelNumberForArProject20B);

		synchronizedDeleteProject(refWks.hbProject20_B);
		expectedResourcesInEditingDomain20 = expectedResourcesInEditingDomain20 - expectedHb20ResourcesForArProject20B;
		expectedResourcesInEditingDomain3Uml2 = expectedResourcesInEditingDomain3Uml2 - expectedUml2ResourcesForArProject20B;
		// check that resources have been unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, expectedResourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, expectedResourcesInEditingDomain3Uml2);

		// check that model descriptors are removed
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 0);
		assertProjectModelsSizeEquals(refWks.hbProject20_B, 0);

		synchronizedOpenProject(refWks.hbProject10_A);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10,
				refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE).size());
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);

		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);

		synchronizedDeleteProject(refWks.hbProject10_A);

		// check that resources have been unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
		// check that model descriptors are removed
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 0);
	}

	// + testNewFileAdded
	// o right click on project + Import... > General/Existing Projects into
	// Workspace
	// o drag & drop file to project in workspace
	// -> model resource(s) must be loaded
	// -> corresponding model descriptor(s) must be added or stay in place
	// -> all proxies in enclosing project (and referencing projects) must be
	// resolved or blacklisted
	public void testNewFileAdded() throws Exception {
		int resourcesInEditingDomain20 = 0;
		int resourcesInEditingDomain10 = 0;
		int resourcesInEditingDomainUml2 = 0;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		synchronizedOpenProject(refWks.hbProject20_A);
		synchronizedOpenProject(refWks.hbProject20_B);
		synchronizedOpenProject(refWks.hbProject10_A);

		// test add a new file in an HB 20 project
		// check if model descriptor in project hbProject20_A has been put in place
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);

		resourcesInEditingDomain10 += refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE)
				.size();
		resourcesInEditingDomain20 += refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE)
				.size() + refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size();
		resourcesInEditingDomainUml2 += refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE)
				.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// creation of a new file by copying UML2_FILE_10C_1 into hbProject20_A
		String newFileName = "Copy_Of_" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1; //$NON-NLS-1$
		IFile uml2FileProject20B_1 = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		uml2FileProject20B_1.copy(refWks.hbProject20_A.getFullPath().append(newFileName), true, null);
		waitForModelLoading();

		// check if Uml2 model descriptor has been correctly added
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 2);

		// check if resources in project hbProject20_A has been reloaded
		// A new Uml2 resource must be loaded
		resourcesInEditingDomainUml2++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		// -------------------
		// test add a new file HUMMINGBIRD 10

		// check if model descriptor in project hbProject20_A has been put in place
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);

		// creation of a new file by copying HB20 file into hbProject10_A
		String newFileName2 = "Copy_Of_" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1;
		IFile hbFileProject10A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		hbFileProject10A_1.copy(refWks.hbProject10_A.getFullPath().append(newFileName2), true, null);
		waitForModelLoading();

		// check if Uml2 model descriptor has been correctly added
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);

		// New file was not loaded in target project
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

	}

	// + testNotYetLoadedFileChanged
	// o change invalid to valid model resource namespace and save it with
	// Eclipse text editor
	// -> model resource must be loaded
	// -> corresponding model descriptor must be added or stay in place
	// -> all proxies in enclosing project (and referencing projects) must be
	// resolved or blacklisted
	public void testNotYetLoadedFileChanged() throws Exception {
		int resourcesInEditingDomain20 = 0;
		int resourcesInEditingDomain10 = 0;
		int resourcesInEditingDomainUml2 = 0;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		synchronizedOpenProject(refWks.hbProject20_B);
		synchronizedOpenProject(refWks.hbProject10_A);

		// test add a new file in an HB 20 project

		// check if model descriptor in project hbProject20_B has been put in
		// place
		assertProjectModelsSizeEquals(refWks.hbProject20_B, 2);

		// check if resources in project hbProject20_A has been loaded
		resourcesInEditingDomain20 += refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE)
				.size();
		resourcesInEditingDomain10 += refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE)
				.size();
		resourcesInEditingDomainUml2 += refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE)
				.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// creation of a new empty Uml2 file
		String newFileName = "newFile.uml"; //$NON-NLS-1$
		IFile uml2FileProject20B_1 = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile newFile = refWks.hbProject10_A.getFile(newFileName);

		// fill in the file with empty content
		newFile.create(new ByteArrayInputStream("".getBytes()), true, null); //$NON-NLS-1$
		waitForModelLoading();
		// Verify that ModelDescriptor for UML was created
		if (hasContentTypeOf(newFile, UML2MMDescriptor.INSTANCE)) {
			assertProjectModelsSizeEquals(refWks.hbProject10_A, 2);
		} else {
			assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);
		}

		// Verify that newly created file was not loaded because its contents is empty
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, newFileName);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);

		// add Uml2 resource content to the new created file
		synchronizedSetFileContents(newFile, uml2FileProject20B_1.getContents());

		// check if new model descriptor have been put in place
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 2);

		// check if resources in project hbProject20_A has been reloaded
		// A new Uml2 resource must be loaded
		resourcesInEditingDomainUml2++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);
	}

	// + testAlreadyLoadedFileChanged
	// o modify model resource content such that it remains valid and save it
	// with Eclipse text editor
	// -> model resource must be loaded
	// -> corresponding model descriptor must stay in place
	// -> all proxies in enclosing project (and referencing projects) must be
	// resolved or blacklisted
	// o modify model resource content such that it becomes invalid and save it
	// with Eclipse text editor
	// -> model resource must be unloaded
	// -> corresponding model descriptor must stay in place unless changed file
	// was the last model resource
	public void testAlreadyLoadedFileChanged() throws Exception {
		synchronizedOpenProject(refWks.hbProject20_B);
		synchronizedOpenProject(refWks.hbProject10_A);
		int resourcesInEditingDomain20 = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size();
		int resourceUml2InArProject20_B = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE)
				.size();
		int resourcesInEditingDomain10 = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE).size();
		int resourceUml2InArProject10_A = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, UML2MMDescriptor.INSTANCE)
				.size();

		int resourceUml2InEditingDomain = resourceUml2InArProject20_B + resourceUml2InArProject10_A;
		// test add a new file and changes its content in an HB 20 project

		// check if model descriptor in project hbProject20_B has been put in place
		assertProjectModelsSizeEquals(refWks.hbProject20_B, 2);

		// check if resources in project hbProject20_A has been loaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceUml2InEditingDomain);

		// creation of a new file by copying UML2_FILE_NAME_20B_1 into hbProject10_A
		String newFileName = "Copy_Of_" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1;
		IFile uml2FileProject10A_1 = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		// create an new file copy of Uml2 file from project 20 B
		uml2FileProject10A_1.copy(refWks.hbProject10_A.getFullPath().append(newFileName), true, null);
		waitForModelLoading();

		assertProjectModelsSizeEquals(refWks.hbProject10_A, 2);
		resourceUml2InEditingDomain++;
		// check if resources in project hbProject20_A has been reloaded
		// A new Uml2 resource must be loaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceUml2InEditingDomain);

		// =======================================================
		// set empty content to the new created file
		IFile newFile = refWks.hbProject10_A.getFile(newFileName);
		synchronizedSetFileContents(newFile, new ByteArrayInputStream("".getBytes())); //$NON-NLS-1$

		// check if Uml2 model descriptor still put in place.
		if (hasContentTypeOf(newFile, UML2MMDescriptor.INSTANCE)) {
			assertProjectModelsSizeEquals(refWks.hbProject10_A, 2);
		} else {
			assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);
		}
		// Verify that the changed resource will be unloaded from editingDomain because its empty content
		resourceUml2InEditingDomain--;
		// check if resources in project hbProject20_A has been reloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceUml2InEditingDomain);

		final IFile hbFileProject20B_1 = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertNotNull(hbFileProject20B_1);
		final IFile hbFileProject20B_2 = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertNotNull(hbFileProject20B_2);
		final IFile hbFileProject20B_3 = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertNotNull(hbFileProject20B_3);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					// set empty content to hbFileProject20B_1 HB20 file
					hbFileProject20B_1.setContents(new ByteArrayInputStream("".getBytes()), true, false, null); //$NON-NLS-1$
					// set empty content to hbFileProject20B_2 HB20 file
					hbFileProject20B_2.setContents(new ByteArrayInputStream("".getBytes()), true, false, null); //$NON-NLS-1$
					// set empty content to hbFileProject20B_3 HB20 file
					hbFileProject20B_3.setContents(new ByteArrayInputStream("".getBytes()), true, false, null); //$NON-NLS-1$

				} catch (CoreException ex) {
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		waitForModelLoading();

		// check if resources in project hbProject20_B has been unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceUml2InEditingDomain);

		// check if HB model descriptor has been removed if cannot retrieve contentypId of HB files
		if (hasContentTypeOf(hbFileProject20B_1, Hummingbird20MMDescriptor.INSTANCE)
				|| hasContentTypeOf(hbFileProject20B_2, Hummingbird20MMDescriptor.INSTANCE)
				|| hasContentTypeOf(hbFileProject20B_3, Hummingbird20MMDescriptor.INSTANCE)) {
			assertProjectHasModels(refWks.hbProject20_B, Hummingbird20MMDescriptor.INSTANCE);
		} else {
			assertProjectHasNoModels(refWks.hbProject20_B, Hummingbird20MMDescriptor.INSTANCE);
		}
	}

	// + testFileMoved
	// o drag HB20 file from an HB20 project and drop it into an HB 10 project
	// -> the HB20 file must be present in the HB 10 project
	// -> the corresponding model resource must be unloaded
	// -> corresponding model descriptor must be removed if the moved HB20 file was the last one in the original
	// HB20project
	public void testFileMoved() throws Exception {
		// File removed in HB 20 project
		synchronizedOpenProject(refWks.hbProject20_A);
		synchronizedOpenProject(refWks.hbProject20_D);
		synchronizedOpenProject(refWks.hbProject10_A);
		int resourceAr20InProject_20_A = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE).size();
		int resourceAr20InProject_20_D = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size();
		int resourceAr10InProject_10_A = refWks
				.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE).size();
		int resourceUml2InProject_20_D = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE)
				.size();

		int resourceInEditingDomain10 = resourceAr10InProject_10_A;
		int resourceInEditingDomain20 = resourceAr20InProject_20_A + resourceAr20InProject_20_D;
		int resourceInEditingDomainUml2 = resourceUml2InProject_20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);
		Collection<IModelDescriptor> models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		IModelDescriptor hbmodelDescriptor_20_A = models.iterator().next();

		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_D, Hummingbird20MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		IModelDescriptor hbmodelDescriptor_20_D = models.iterator().next();

		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_A, Hummingbird10MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		IModelDescriptor hbmodelDescriptor_10_A = models.iterator().next();
		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_D, UML2MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		IModelDescriptor uml2modelDescriptor_20_D = models.iterator().next();

		assertNotNull(hbmodelDescriptor_20_A);
		assertNotNull(hbmodelDescriptor_20_D);
		assertNotNull(hbmodelDescriptor_10_A);
		assertNotNull(uml2modelDescriptor_20_D);

		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_A, hbmodelDescriptor_20_A.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());
		// ==================================================================
		// Move file hbFile20_20A_1 to hbProject10_A
		IFile file20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(file20_20A_1);
		assertTrue(file20_20A_1.isAccessible());

		IPath targetOfFile20_20A_1 = refWks.hbProject10_A.getFullPath().append(file20_20A_1.getName());
		synchronizedMoveFile(file20_20A_1, targetOfFile20_20A_1);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, --resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFile20_20A_1 = refWks.hbProject10_A.getFile(file20_20A_1.getName());
		assertNotNull(movedFile20_20A_1);
		assertTrue(movedFile20_20A_1.isAccessible());
		assertFalse(EcorePlatformUtil.isFileLoaded(movedFile20_20A_1));
		// Verify that move filed is removed from the oldModelDescriptor
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(--resourceAr20InProject_20_A, hbmodelDescriptor_20_A.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());
		// ---------------
		// Move file hbFile20_20A_2 to hbProject10_A
		IFile file20_20A_2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		assertNotNull(file20_20A_2);
		assertTrue(file20_20A_2.isAccessible());

		IPath targetOfFile20_20A_2 = refWks.hbProject10_A.getFullPath().append(file20_20A_2.getName());
		synchronizedMoveFile(file20_20A_2, targetOfFile20_20A_2);
		// Verify that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, --resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFile20_20A_2 = refWks.hbProject10_A.getFile(file20_20A_2.getName());
		assertNotNull(movedFile20_20A_2);
		assertTrue(movedFile20_20A_2.isAccessible());
		assertFalse(EcorePlatformUtil.isFileLoaded(movedFile20_20A_2));
		// Verify that move filed is removed from the oldModelDescriptor
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(--resourceAr20InProject_20_A, hbmodelDescriptor_20_A.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());
		// ---------------
		// Move file hbFile20_20A_3 to hbProject10_A
		IFile file20_20A_3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		assertNotNull(file20_20A_3);
		assertTrue(file20_20A_3.isAccessible());

		IPath targetOfFile20_20A_3 = refWks.hbProject10_A.getFullPath().append(file20_20A_3.getName());
		synchronizedMoveFile(file20_20A_3, targetOfFile20_20A_3);
		// Veriy that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, --resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFile20_20A_3 = refWks.hbProject10_A.getFile(file20_20A_3.getName());
		assertNotNull(movedFile20_20A_3);
		assertTrue(movedFile20_20A_3.isAccessible());
		assertFalse(EcorePlatformUtil.isFileLoaded(movedFile20_20A_3));
		// Verify that move filed is removed from the oldModelDescriptor
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(--resourceAr20InProject_20_A, hbmodelDescriptor_20_A.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());
		// ---------------
		// Move file hbFile20_20A_4 to hbProject10_A
		IFile file20_20A_4 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4);
		assertNotNull(file20_20A_4);
		assertTrue(file20_20A_4.isAccessible());

		IPath targetOfFile20_20A_4 = refWks.hbProject10_A.getFullPath().append(file20_20A_4.getName());
		synchronizedMoveFile(file20_20A_4, targetOfFile20_20A_4);
		// Verify that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, --resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFile20_20A_4 = refWks.hbProject10_A.getFile(file20_20A_4.getName());
		assertNotNull(movedFile20_20A_4);
		assertTrue(movedFile20_20A_4.isAccessible());
		assertFalse(EcorePlatformUtil.isFileLoaded(movedFile20_20A_4));
		// Verify that move filed is removed from the oldModelDescriptor
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(--resourceAr20InProject_20_A, hbmodelDescriptor_20_A.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());

		// ---------------
		// Move file hbFile20_20A_5 to hbProject10_A
		IFile file20_20A_5 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_5);
		assertNotNull(file20_20A_5);
		assertTrue(file20_20A_5.isAccessible());

		IPath targetOfFile20_20A_5 = refWks.hbProject10_A.getFullPath().append(file20_20A_5.getName());
		synchronizedMoveFile(file20_20A_5, targetOfFile20_20A_5);
		// Verify that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, --resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFile20_20A_5 = refWks.hbProject10_A.getFile(file20_20A_5.getName());
		assertNotNull(movedFile20_20A_5);
		assertTrue(movedFile20_20A_5.isAccessible());
		assertFalse(EcorePlatformUtil.isFileLoaded(movedFile20_20A_5));
		// Verify that move filed is removed from the oldModelDescriptor
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(--resourceAr20InProject_20_A, hbmodelDescriptor_20_A.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());

		// -----------------------
		// Move file hbFile21_20A_4 to hbProject20_D
		IFile file21_20A_4 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4);
		assertNotNull(file21_20A_4);
		assertTrue(file21_20A_4.isAccessible());

		IPath targetOfFile21_20A_4 = refWks.hbProject20_D.getFullPath().append(file21_20A_4.getName());
		synchronizedMoveFile(file21_20A_4, targetOfFile21_20A_4);
		// Verify that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		// Verify that the ModelDescriptor of hbProject20_A was deleted when the moved File is the last one of this
		// project
		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertTrue(models.isEmpty());
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(++resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());

		// Reload project 20_D
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_D, false, false, new NullProgressMonitor());
		waitForModelLoading();
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		// ==================================================================
		// Move file hbFilet20_20D_1 to hbProject20_A
		IFile file20_20D_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(file20_20D_1);
		assertTrue(file20_20D_1.isAccessible());

		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertTrue(models.isEmpty());

		IPath targetOfFile20_20D_1 = refWks.hbProject20_A.getFullPath().append(file20_20D_1.getName());
		synchronizedMoveFile(file20_20D_1, targetOfFile20_20D_1);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFile20_20D_1 = refWks.hbProject20_A.getFile(file20_20D_1.getName());
		assertNotNull(movedFile20_20D_1);
		assertTrue(movedFile20_20D_1.isAccessible());
		assertTrue(EcorePlatformUtil.isFileLoaded(movedFile20_20D_1));

		// Reload project
		ModelLoadManager.INSTANCE.reloadProject(refWks.hbProject20_A, false, false, new NullProgressMonitor());
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);

		// Verify that the ModelDescriptor of hbProject20_A was created to load moved File
		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		hbmodelDescriptor_20_A = models.iterator().next();
		assertNotNull(hbmodelDescriptor_20_A);
		assertEquals(1, hbmodelDescriptor_20_A.getLoadedResources(true).size());

		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(--resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(resourceInEditingDomainUml2, uml2modelDescriptor_20_D.getLoadedResources(true).size());

		// ==================================================================
		// resources
		// Move file uml2File20_D_1 to hbProject20_A
		IFile fileUml2_20D_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertNotNull(fileUml2_20D_1);
		assertTrue(fileUml2_20D_1.isAccessible());

		IPath targetOfFileUml2_20D_1 = refWks.hbProject20_A.getFullPath().append(fileUml2_20D_1.getName());
		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, UML2MMDescriptor.INSTANCE);
		assertTrue(models.isEmpty());
		synchronizedMoveFile(fileUml2_20D_1, targetOfFileUml2_20D_1);
		// Verify that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFileUml2_20D_1 = refWks.hbProject20_A.getFile(fileUml2_20D_1.getName());
		assertNotNull(movedFileUml2_20D_1);
		assertTrue(movedFileUml2_20D_1.isAccessible());
		assertTrue(EcorePlatformUtil.isFileLoaded(movedFileUml2_20D_1));
		// Verify that move filed is removed from the oldModelDescriptor
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(--resourceUml2InProject_20_D, uml2modelDescriptor_20_D.getLoadedResources(true).size());
		// Verify a new uml model in hbProject20_A was automatically generated
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);
		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A, UML2MMDescriptor.INSTANCE);
		assertEquals(1, models.size());
		IModelDescriptor uml2ModelDescriptor_20_A = models.iterator().next();
		assertNotNull(uml2ModelDescriptor_20_A);
		assertEquals(1, uml2ModelDescriptor_20_A.getLoadedResources(true).size());
		// ------------------
		// // Move file uml2File20_D_2 to hbProject20_A
		IFile fileUml2_20D_2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertNotNull(fileUml2_20D_2);
		assertTrue(fileUml2_20D_2.isAccessible());

		IPath targetOfFileUml2_20D_2 = refWks.hbProject20_A.getFullPath().append(fileUml2_20D_2.getName());

		synchronizedMoveFile(fileUml2_20D_2, targetOfFileUml2_20D_2);
		// Verify that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFileUml2_20D_2 = refWks.hbProject20_A.getFile(fileUml2_20D_2.getName());
		assertNotNull(movedFileUml2_20D_2);
		assertTrue(movedFileUml2_20D_2.isAccessible());
		assertTrue(EcorePlatformUtil.isFileLoaded(movedFileUml2_20D_2));
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(--resourceUml2InProject_20_D, uml2modelDescriptor_20_D.getLoadedResources(true).size());

		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);
		assertEquals(2, uml2ModelDescriptor_20_A.getLoadedResources(true).size());

		// ------------------
		// Move file uml2File20_D_2 to hbProject20_A
		IFile fileUml2_20D_3 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		assertNotNull(fileUml2_20D_3);
		assertTrue(fileUml2_20D_3.isAccessible());

		IPath targetOfFileUml2_20D_3 = refWks.hbProject20_A.getFullPath().append(fileUml2_20D_3.getName());
		synchronizedMoveFile(fileUml2_20D_3, targetOfFileUml2_20D_3);
		// Verify that moved File is Unloaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourceInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);

		IFile movedFileUml2_20D_3 = refWks.hbProject20_A.getFile(fileUml2_20D_3.getName());
		assertNotNull(movedFileUml2_20D_3);
		assertTrue(movedFileUml2_20D_3.isAccessible());
		assertTrue(EcorePlatformUtil.isFileLoaded(movedFileUml2_20D_3));
		// Verify that move filed is removed from the oldModelDescriptor
		assertEquals(resourceAr10InProject_10_A, hbmodelDescriptor_10_A.getLoadedResources(true).size());
		assertEquals(resourceAr20InProject_20_D, hbmodelDescriptor_20_D.getLoadedResources(true).size());
		assertEquals(3, uml2ModelDescriptor_20_A.getLoadedResources(true).size());
		models = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_D, UML2MMDescriptor.INSTANCE);
		assertTrue(models.isEmpty());

		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourceInEditingDomainUml2);
		assertEquals(3, uml2ModelDescriptor_20_A.getLoadedResources(true).size());

	}

	// +testFileRenamed]
	// o rename a file in an HB project
	// -> The renamed file is loaded in editingDomain and put it the ModelDescriptor in correct place

	public void testFileRenamed() throws Exception {
		int resourcesInEditingDomain20 = 0;
		int resourcesInEditingDomain10 = 0;
		int resourcesInEditingDomainUml2 = 0;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		synchronizedOpenProject(refWks.hbProject20_B);
		synchronizedOpenProject(refWks.hbProject10_C);

		int resource20InProject20_B = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE)
				.size();
		int resourceUml2InProject20_B = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE)
				.size();
		int resource10InProject10_C = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C, Hummingbird10MMDescriptor.INSTANCE)
				.size();
		int resourceUml2InProject10_C = refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C, UML2MMDescriptor.INSTANCE)
				.size();

		resourcesInEditingDomain20 += resource20InProject20_B;
		resourcesInEditingDomain10 += resource10InProject10_C;
		resourcesInEditingDomainUml2 += resourceUml2InProject10_C + resourceUml2InProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// ============================================================
		// test rename a file in an HB 20 project
		IFile hbFile20B_1 = refWks.hbProject20_B.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);

		// check if model descriptor in project hbProject20_B has been put in
		// place
		assertProjectModelsSizeEquals(refWks.hbProject20_B, 2);
		IModelDescriptor modelDescriptor20_B = ModelDescriptorRegistry.INSTANCE.getModel(hbFile20B_1);
		assertEquals(resource20InProject20_B, modelDescriptor20_B.getLoadedResources(false).size());

		// Rename an HbFile in hbProject20A
		String newName20 = "hbFile20.typemodel";
		synchronizedRenameFile(hbFile20B_1, newName20);
		waitForModelLoading();

		// check if Hb model descriptor has been updated
		assertProjectModelsSizeEquals(refWks.hbProject20_B, 2);
		assertEquals(resource20InProject20_B, modelDescriptor20_B.getLoadedResources(false).size());

		// check if resources in project hbProject20_B has been reloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		assertNull(refWks.editingDomain20.getResourceSet()
				.getResource(URI.createPlatformResourceURI(
						"/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1, true),
						false));

		assertNotNull(refWks.editingDomain20.getResourceSet()
				.getResource(URI.createPlatformResourceURI("/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/" + newName20, true), false));

		IFile newFile20 = refWks.hbProject20_B.getFile(newName20);
		assertNotNull(newFile20);
		assertTrue(newFile20.exists());
		assertTrue(EcorePlatformUtil.isFileLoaded(newFile20));

		// ============================================================
		// test rename a file in an HUMMINGBIRD 10 project
		IFile hbFile10C_1 = refWks.hbProject10_C.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1);
		// check if model descriptor in project hbProject20_A has been put in
		// place
		assertProjectModelsSizeEquals(refWks.hbProject10_C, 2);
		IModelDescriptor modelDescriptor10_C = ModelDescriptorRegistry.INSTANCE.getModel(hbFile10C_1);
		assertEquals(resourceUml2InProject10_C, modelDescriptor10_C.getLoadedResources(false).size());

		// Rename an HbFile in hbProject10C
		String newName10 = "HbFile10.hummingbird";
		synchronizedRenameFile(hbFile10C_1, newName10);
		waitForModelLoading();

		// check if Hb model descriptor has been updated
		assertProjectModelsSizeEquals(refWks.hbProject10_C, 2);
		assertEquals(resourceUml2InProject10_C, modelDescriptor10_C.getLoadedResources(false).size());

		// check if resources in project hbProject20_A has been reloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		assertNull(refWks.editingDomain10.getResourceSet()
				.getResource(URI.createPlatformResourceURI(
						"/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10C_1, true),
						false));

		assertNotNull(refWks.editingDomain10.getResourceSet()
				.getResource(URI.createPlatformResourceURI("/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + newName10, true), false));

		IFile newFile10 = refWks.hbProject10_C.getFile(newName10);
		assertNotNull(newFile10);
		assertTrue(newFile10.exists());
		assertTrue(EcorePlatformUtil.isFileLoaded(newFile10));
		// ============================================================
		// test rename an UML2 file in a project
		IFile uml2File10C_1 = refWks.hbProject10_C.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1);
		// check if model descriptor in project hbProject20_A has been put in
		// place
		assertProjectModelsSizeEquals(refWks.hbProject10_C, 2);
		IModelDescriptor uml2ModelDescriptor10_C = ModelDescriptorRegistry.INSTANCE.getModel(uml2File10C_1);
		assertEquals(resourceUml2InProject10_C, uml2ModelDescriptor10_C.getLoadedResources(false).size());
		// check if resources in project hbProject20_A has been loaded

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		// Rename an HbFile in hbProject10C

		String newNameUml2 = "uml2File.uml";
		synchronizedRenameFile(uml2File10C_1, newNameUml2);
		waitForModelLoading();

		// check if Hb model descriptor has been updated
		assertProjectModelsSizeEquals(refWks.hbProject10_C, 2);
		assertEquals(resourceUml2InProject10_C, uml2ModelDescriptor10_C.getLoadedResources(false).size());
		// check if resources in project hbProject20_A has been reloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, resourcesInEditingDomain10);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, resourcesInEditingDomainUml2);

		assertNull(refWks.editingDomainUml2.getResourceSet()
				.getResource(URI.createPlatformResourceURI(
						"/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, true),
						false));

		assertNotNull(refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI("/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + newNameUml2, true), false));

		IFile newFileUml2 = refWks.hbProject10_C.getFile(newNameUml2);
		assertNotNull(newFileUml2);
		assertTrue(newFileUml2.exists());
		assertTrue(EcorePlatformUtil.isFileLoaded(newFileUml2));
	}

	// + testFileRemoved
	// o right click on file + Delete
	// o right click on folder + Delete
	// -> model resource(s) must be unloaded
	// -> corresponding model descriptor(s) must stay in place unless removed
	// file(s) was(were) the last model resource(s)
	public void testFileRemoved() throws Exception {

		// File removed in HB 20 project
		synchronizedOpenProject(refWks.hbProject20_A);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20,
				refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE).size());
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
		IFile file1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		synchronizedDeleteFile(file1);

		assertFalse(EcorePlatformUtil.isFileLoaded(file1));
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);

		IFile file2 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2);
		synchronizedDeleteFile(file2);

		assertFalse(EcorePlatformUtil.isFileLoaded(file2));
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);

		IFile file3 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3);
		synchronizedDeleteFile(file3);

		assertFalse(EcorePlatformUtil.isFileLoaded(file3));
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);

		IFile file4 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4);
		synchronizedDeleteFile(file4);

		assertFalse(EcorePlatformUtil.isFileLoaded(file4));
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);

		IFile file5 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4);
		synchronizedDeleteFile(file5);
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 1);

		IFile file6 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_5);
		synchronizedDeleteFile(file6);
		assertProjectModelsSizeEquals(refWks.hbProject20_A, 0);

		// File removed in HUMMINGBIRD 10 project
		synchronizedOpenProject(refWks.hbProject10_A);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10,
				refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE).size());
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);

		file4 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		synchronizedDeleteFile(file4);

		assertFalse(EcorePlatformUtil.isFileLoaded(file4));
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);

		file6 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		synchronizedDeleteFile(file6);

		assertFalse(EcorePlatformUtil.isFileLoaded(file6));
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);

		IFile file7 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		synchronizedDeleteFile(file7);

		assertFalse(EcorePlatformUtil.isFileLoaded(file7));
		assertProjectModelsSizeEquals(refWks.hbProject10_A, 1);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10,
				refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE).size() - 3);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);
	}

	// test Proxy Resolution is redone after adding references btw projects
	// A refers to B and so all references in objects from A pointing to reachable
	// elements of B are resolved
	public void testProxyResolution_AddReferencedProject() throws Exception {

		synchronizedOpenProject(refWks.hbProject20_A);
		synchronizedOpenProject(refWks.hbProject20_C);

		List<org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component> objects20 = new ArrayList<org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component>();
		Resource resource20_C_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, true), false);
		assertNotNull(resource20_C_1);
		assertFalse(resource20_C_1.getContents().isEmpty());
		Application application20 = (Application) resource20_C_1.getContents().get(0);
		assertNotNull(application20);
		assertFalse(application20.getComponents().isEmpty());
		objects20 = application20.getComponents();
		// Test DataElementPrototype have unresolved reference
		for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : objects20) {
			assertNotNull(component.getType());
			assertTrue(component.getType().eIsProxy());
		}
		// Add reference from hbProject20_C to hbProject20_A
		IProjectDescription hbProject20_C_prjDesc = refWks.hbProject20_C.getDescription();
		hbProject20_C_prjDesc.setReferencedProjects(new IProject[] { refWks.hbProject20_A });
		refWks.hbProject20_C.setDescription(hbProject20_C_prjDesc, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the references are resolved
		List<org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component> testObjects20 = new ArrayList<org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component>();
		Resource testResource20_C_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, true), false);
		assertNotNull(testResource20_C_1);
		assertFalse(testResource20_C_1.getContents().isEmpty());
		Application testApplication20 = (Application) resource20_C_1.getContents().get(0);
		assertNotNull(testApplication20);
		assertFalse(testApplication20.getComponents().isEmpty());
		testObjects20 = testApplication20.getComponents();
		// Test ComponentType have unresolved reference
		for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : testObjects20) {
			assertNotNull(component.getType());
			assertFalse(component.getType().eIsProxy());
			IFile referenceFile = EcorePlatformUtil.getFile(component.getType());
			assertNotNull(referenceFile);
			assertEquals(refWks.hbProject20_A, referenceFile.getProject());
		}

	}

	// Open referring project hbProject10_C, verify that references are proxies.
	// Test proxy is unresolved when open referred project hbProject20_B, since no dependencies between the projects.
	// Add reference from hbProject10_C to hbProject20_B, verify again that proxy resolution is done.
	public void testProxyResolution_UMLReference1() throws Exception {

		synchronizedOpenProject(refWks.hbProject10_C);

		// verify that no project references exist in hbProject10_C
		assertEquals(0, refWks.hbProject10_C.getDescription().getReferencedProjects().length);

		Resource resourceUml2_C_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, true), false);
		assertNotNull(resourceUml2_C_1);
		assertFalse(resourceUml2_C_1.getContents().isEmpty());

		Model model = (Model) resourceUml2_C_1.getContents().get(0);
		assertNotNull(model);
		assertEquals(2, model.getPackagedElements().size());
		org.eclipse.uml2.uml.PackageableElement uml2Package = model.getPackagedElements().get(1);

		List<Port> objectUml2 = new ArrayList<Port>();
		for (Element element : uml2Package.getOwnedElements()) {
			if (element instanceof Component) {
				Component comp = (Component) element;
				assertFalse(comp.getOwnedPorts().isEmpty());
				Port port = comp.getOwnedPorts().get(0);
				objectUml2.add(port);
			}
		}
		assertFalse(objectUml2.isEmpty());
		// Verify that port of component have unresolved reference.
		// The referred resources in hbProject20_B are not existing, so the references will be resolved as proxies
		for (Port port : objectUml2) {
			EObject refObject = port.getType();
			assertNotNull(refObject);
			assertTrue(refObject.eIsProxy());
		}

		// ---------------------------------------------
		/*
		 * Open referred project hbProject20_B, references are asked to resolved. Now referred resources is existing,
		 * however there is no project reference dependency from hbProject10_C to hbProject20_B, the proxies are still
		 * unresolved.
		 */
		synchronizedOpenProject(refWks.hbProject20_B);

		// Verify the references are unresolved
		List<Port> testObjectUml2 = new ArrayList<Port>();
		Resource testresourceUml2_C_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, true), false);
		assertNotNull(testresourceUml2_C_1);
		assertTrue(testresourceUml2_C_1.getContents().size() > 0);
		Model testModel = (Model) testresourceUml2_C_1.getContents().get(0);
		assertNotNull(testModel);
		assertEquals(2, testModel.getPackagedElements().size());
		org.eclipse.uml2.uml.PackageableElement testUml2Package = testModel.getPackagedElements().get(1);

		for (Element element : testUml2Package.getOwnedElements()) {
			if (element instanceof Component) {
				Component comp = (Component) element;
				assertTrue(comp.getOwnedPorts().size() > 0);
				Port port = comp.getOwnedPorts().get(0);
				testObjectUml2.add(port);
			}
		}
		assertFalse(testObjectUml2.isEmpty());
		// Verify that port of component have unresolved reference
		// The referred resources in hbProject20_B are not in the project scope, so the references will be resolved as
		// proxies
		for (Port port : testObjectUml2) {
			EObject refObject = port.getType();
			assertNotNull(refObject);
			assertTrue(refObject.eIsProxy());
		}

		// ---------------------------------------------
		/*
		 * Add reference from hbProject10_C to hbProject20_B. Now the referred resource is in the same project scope,
		 * the proxy resolution is done.
		 */
		// add reference from hbProject10_C to hbProject20_B
		IProjectDescription hbProject10_C_prjDesc = refWks.hbProject10_C.getDescription();
		hbProject10_C_prjDesc.setReferencedProjects(new IProject[] { refWks.hbProject20_B });
		refWks.hbProject10_C.setDescription(hbProject10_C_prjDesc, new NullProgressMonitor());
		waitForModelLoading();

		// Verify the references are resolved
		List<Port> testObjectUml2B = new ArrayList<Port>();
		Resource testresourceUml2_C_1B = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, true), false);
		assertNotNull(testresourceUml2_C_1B);
		assertTrue(testresourceUml2_C_1B.getContents().size() > 0);
		Model testModelB = (Model) testresourceUml2_C_1B.getContents().get(0);
		assertNotNull(testModelB);
		assertEquals(2, testModelB.getPackagedElements().size());
		org.eclipse.uml2.uml.PackageableElement testUml2PackageB = testModel.getPackagedElements().get(1);

		for (Element element : testUml2PackageB.getOwnedElements()) {
			if (element instanceof Component) {
				Component comp = (Component) element;
				assertTrue(comp.getOwnedPorts().size() > 0);
				Port port = comp.getOwnedPorts().get(0);
				testObjectUml2B.add(port);
			}
		}
		assertFalse(testObjectUml2B.isEmpty());
		// Verify that port of component have resolved reference
		for (Port port : testObjectUml2B) {
			EObject refObject = port.getType();
			assertNotNull(refObject);
			assertFalse(refObject.eIsProxy());
			IFile refFile = EcorePlatformUtil.getFile(refObject);
			assertEquals(refWks.hbProject20_B, refFile.getProject());
		}
	}

	// Open referred project hbProject20_B, then open referring project hbProject10_C.
	// Test that proxy resolution is done only when the project reference (from hbProject10_C to hbProject20_B) is
	// added.
	public void testProxyResolution_UMLReference2() throws Exception {
		// Open referred project hbProject20_B
		synchronizedOpenProject(refWks.hbProject20_B);
		// Open referring project hbProject10_C
		synchronizedOpenProject(refWks.hbProject10_C);
		// Verify that there is no link btw 2 projects
		assertEquals(0, refWks.hbProject10_C.getDescription().getReferencedProjects().length);

		// ------------------------------------------------
		// Verify the references are unresolved
		List<Port> testObjectUml2 = new ArrayList<Port>();
		Resource testresourceUml2_C_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, true), false);
		assertNotNull(testresourceUml2_C_1);
		assertTrue(testresourceUml2_C_1.getContents().size() > 0);
		Model testModel = (Model) testresourceUml2_C_1.getContents().get(0);
		assertNotNull(testModel);
		assertEquals(2, testModel.getPackagedElements().size());
		org.eclipse.uml2.uml.PackageableElement testUml2Package = testModel.getPackagedElements().get(1);

		for (Element element : testUml2Package.getOwnedElements()) {
			if (element instanceof Component) {
				Component comp = (Component) element;
				assertTrue(comp.getOwnedPorts().size() > 0);
				Port port = comp.getOwnedPorts().get(0);
				testObjectUml2.add(port);
			}
		}
		assertFalse(testObjectUml2.isEmpty());
		// Verify that port of component have unresolved reference.
		// The referred resources in hbProject20_B are not in the project scope, so the references will be resolved as
		// proxies
		for (Port port : testObjectUml2) {
			EObject refObject = port.getType();
			assertNotNull(refObject);
			assertTrue(refObject.eIsProxy());
			IFile refFile = EcorePlatformUtil.getFile(refObject);
			assertNull(refFile);
		}

		// ------------------------------------------------
		/*
		 * Add reference from hbProject10_C to hbProject20_B. Now the referred resource is in the same project scope,
		 * the proxy resolution is done.
		 */
		// add reference from hbProject10_C to hbProject20_B
		IProjectDescription hbProject10_C_prjDesc = refWks.hbProject10_C.getDescription();
		hbProject10_C_prjDesc.setReferencedProjects(new IProject[] { refWks.hbProject20_B });
		refWks.hbProject10_C.setDescription(hbProject10_C_prjDesc, new NullProgressMonitor());
		waitForModelLoading();
		assertEquals(1, refWks.hbProject10_C.getDescription().getReferencedProjects().length);

		// Verify the references are resolved
		List<Port> objectUml2 = new ArrayList<Port>();
		testresourceUml2_C_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_C + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_10C_1, true), false);
		assertNotNull(testresourceUml2_C_1);
		assertTrue(testresourceUml2_C_1.getContents().size() > 0);
		testModel = (Model) testresourceUml2_C_1.getContents().get(0);
		assertNotNull(testModel);
		assertEquals(2, testModel.getPackagedElements().size());
		testUml2Package = testModel.getPackagedElements().get(1);

		for (Element element : testUml2Package.getOwnedElements()) {
			if (element instanceof Component) {
				Component comp = (Component) element;
				assertTrue(comp.getOwnedPorts().size() > 0);
				Port port = comp.getOwnedPorts().get(0);
				objectUml2.add(port);
			}
		}
		assertFalse(objectUml2.isEmpty());
		// Verify that port of component have resolved reference
		for (Port port : objectUml2) {
			EObject refObject = port.getType();
			assertNotNull(refObject);
			assertFalse(refObject.eIsProxy());
			IFile refFile = EcorePlatformUtil.getFile(refObject);
			assertEquals(refWks.hbProject20_B, refFile.getProject());
		}
	}

	// A refers to B and so all references in objects from A pointing to elements of B are resolved
	// removed the dependency btw projects => references are unresolved
	public void testProxyResolution_RemoveReferencedProject() throws Exception {
		// ---------------------------------------------------------------------------------------------------
		// Context: HB10 objects
		{
			synchronizedOpenProject(refWks.hbProject10_D);
			synchronizedOpenProject(refWks.hbProject10_E);

			Resource resource10_E_1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true),
							false);
			assertNotNull(resource10_E_1);
			assertFalse(resource10_E_1.getContents().isEmpty());
			org.eclipse.sphinx.examples.hummingbird10.Application application10 = (org.eclipse.sphinx.examples.hummingbird10.Application) resource10_E_1
					.getContents().get(0);
			assertNotNull(application10);
			assertTrue(application10.getComponents().size() > 0);
			for (org.eclipse.sphinx.examples.hummingbird10.Component component : application10.getComponents()) {
				assertNotNull(component.getProvidedInterfaces());
				for (org.eclipse.sphinx.examples.hummingbird10.Interface proInterface : component.getProvidedInterfaces()) {
					assertFalse(proInterface.eIsProxy());
					IFile refFile = EcorePlatformUtil.getFile(proInterface);
					assertNotNull(refFile);
					assertEquals(refWks.hbProject10_D, refFile.getProject());
				}
			}

			//
			// *** Remove reference from hbProject10_E to hbProject10_D
			IProjectDescription hbProject10_E_prjDesc = refWks.hbProject10_E.getDescription();
			hbProject10_E_prjDesc.setReferencedProjects(new IProject[] {});
			refWks.hbProject10_E.setDescription(hbProject10_E_prjDesc, new NullProgressMonitor());
			waitForModelLoading();

			//
			// Verify the references are unresolved
			Resource testResource10E_1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true),
							false);
			assertNotNull(testResource10E_1);
			assertFalse(testResource10E_1.getContents().isEmpty());
			org.eclipse.sphinx.examples.hummingbird10.Application testApplication10 = (org.eclipse.sphinx.examples.hummingbird10.Application) testResource10E_1
					.getContents().get(0);
			assertNotNull(testApplication10);
			assertTrue(testApplication10.getComponents().size() > 0);
			for (org.eclipse.sphinx.examples.hummingbird10.Component component : testApplication10.getComponents()) {
				assertNotNull(component.getProvidedInterfaces());
				for (org.eclipse.sphinx.examples.hummingbird10.Interface proInterface : component.getProvidedInterfaces()) {
					assertTrue(proInterface.eIsProxy());
				}
			}
		}
		// ---------------------------------------------------------------------------------------------------
		// Context: HB 20 Objects and Uml Objecst

		{
			synchronizedOpenProject(refWks.hbProject20_D);
			synchronizedOpenProject(refWks.hbProject20_E);

			// ************************************
			// HB20 Objects
			Resource resource20_E_1 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true),
							false);

			assertNotNull(resource20_E_1);
			assertFalse(resource20_E_1.getContents().isEmpty());
			Application application20 = (Application) resource20_E_1.getContents().get(0);
			assertNotNull(application20);
			assertFalse(application20.getComponents().isEmpty());
			for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : application20.getComponents()) {
				assertNotNull(component.getType());
				assertFalse(component.getType().eIsProxy());
				IFile refFile = EcorePlatformUtil.getFile(component.getType());
				assertNotNull(refFile);
				assertEquals(refWks.hbProject20_D, refFile.getProject());
			}

			// *********************************
			// Uml2 Objects
			List<Operation> testObjectUml2 = new ArrayList<Operation>();
			Resource resourceUml2_E_1 = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true),
							false);
			assertNotNull(resourceUml2_E_1);
			assertFalse(resourceUml2_E_1.getContents().isEmpty());
			Model model = (Model) resourceUml2_E_1.getContents().get(0);
			assertNotNull(model);
			assertEquals(2, model.getPackagedElements().size());
			PackageableElement uml2Package = model.getPackagedElements().get(1);
			assertFalse(uml2Package.getOwnedElements().isEmpty());
			Interface uml2Interface = (Interface) uml2Package.getOwnedElements().get(0);
			assertNotNull(uml2Interface);
			testObjectUml2.addAll(uml2Interface.getOperations());

			// Test references
			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertFalse(operation.getMethods().get(0).eIsProxy());
				IFile referencedFile = EcorePlatformUtil.getFile(operation.getMethods().get(0));
				assertNotNull(referencedFile);
				assertEquals(refWks.hbProject20_D, referencedFile.getProject());
			}
			//
			//
			// *****Remove reference from hbProject20_D to hbProject20_E
			IProjectDescription hbProject20_E_prjDesc = refWks.hbProject20_E.getDescription();
			hbProject20_E_prjDesc.setReferencedProjects(new IProject[] {});
			refWks.hbProject20_E.setDescription(hbProject20_E_prjDesc, new NullProgressMonitor());
			waitForModelLoading();

			//
			//
			// ********************************
			// Verify the reference of HB20 objects are unresolved
			Resource testResource20_E_1 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true),
							false);
			assertNotNull(testResource20_E_1);
			assertFalse(testResource20_E_1.getContents().isEmpty());
			Application testApplication20 = (Application) resource20_E_1.getContents().get(0);
			assertNotNull(testApplication20);
			assertFalse(testApplication20.getComponents().isEmpty());
			for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : testApplication20.getComponents()) {
				assertNotNull(component.getType());
				assertTrue(component.getType().eIsProxy());

			}
			//
			// *************************************
			// Verify the references of Uml2 Objects are unresolved
			testObjectUml2.clear();
			Resource testresourceUml2_E_1 = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true),
							false);
			assertNotNull(testresourceUml2_E_1);
			assertTrue(testresourceUml2_E_1.getContents().size() > 0);
			Model testModel = (Model) testresourceUml2_E_1.getContents().get(0);
			assertNotNull(testModel);
			assertEquals(2, testModel.getPackagedElements().size());
			org.eclipse.uml2.uml.Package testUml2Package = (Package) testModel.getPackagedElements().get(1);
			assertNotNull(testUml2Package);
			assertFalse(testUml2Package.getOwnedElements().isEmpty());
			Interface testInterface = (Interface) testUml2Package.getOwnedElements().get(0);
			assertNotNull(testInterface);
			assertFalse(testInterface.getOperations().isEmpty());

			testObjectUml2.addAll(testInterface.getOperations());

			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertNotNull(operation.getMethods().get(0));
				// assertTrue(operation.getMethods().get(0).eIsProxy());

			}
		}
	}

	// Close referenced project and verify that all references to objects in closed project are unresolved
	public void testProxyResolution_CloseReferencedProject() throws Exception {

		// Context: HB10 objects
		{
			synchronizedOpenProject(refWks.hbProject10_D);
			synchronizedOpenProject(refWks.hbProject10_E);

			Resource resource10_E_1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true),
							false);
			assertNotNull(resource10_E_1);
			assertFalse(resource10_E_1.getContents().isEmpty());
			org.eclipse.sphinx.examples.hummingbird10.Application application10 = (org.eclipse.sphinx.examples.hummingbird10.Application) resource10_E_1
					.getContents().get(0);
			assertNotNull(application10);
			assertTrue(application10.getComponents().size() > 0);
			for (org.eclipse.sphinx.examples.hummingbird10.Component component : application10.getComponents()) {
				assertNotNull(component.getProvidedInterfaces());
				for (org.eclipse.sphinx.examples.hummingbird10.Interface proInterface : component.getProvidedInterfaces()) {
					assertFalse(proInterface.eIsProxy());
					IFile refFile = EcorePlatformUtil.getFile(proInterface);
					assertNotNull(refFile);
					assertEquals(refWks.hbProject10_D, refFile.getProject());
				}
			}
			//
			// ****Close referenced project
			synchronizedCloseProject(refWks.hbProject10_D);
			//
			//
			// Verify the references are unresolved
			Resource testResource10E_1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true),
							false);
			assertNotNull(testResource10E_1);
			assertFalse(testResource10E_1.getContents().isEmpty());
			org.eclipse.sphinx.examples.hummingbird10.Application testApplication10 = (org.eclipse.sphinx.examples.hummingbird10.Application) testResource10E_1
					.getContents().get(0);
			assertNotNull(testApplication10);
			assertTrue(testApplication10.getComponents().size() > 0);
			for (org.eclipse.sphinx.examples.hummingbird10.Component component : testApplication10.getComponents()) {
				assertNotNull(component.getProvidedInterfaces());
				for (org.eclipse.sphinx.examples.hummingbird10.Interface proInterface : component.getProvidedInterfaces()) {
					assertTrue(proInterface.eIsProxy());
				}
			}
		}

		// ---------------------------------------------------------------------------------------------------
		// Context: HB20 Objects and Non UML Object

		{
			synchronizedOpenProject(refWks.hbProject20_D);
			synchronizedOpenProject(refWks.hbProject20_E);

			List<Operation> testObjectUml2 = new ArrayList<Operation>();

			Resource resource20_E_1 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true),
							false);
			// ************************************
			// HB20 Objects
			assertNotNull(resource20_E_1);
			assertFalse(resource20_E_1.getContents().isEmpty());
			Application application20 = (Application) resource20_E_1.getContents().get(0);
			assertNotNull(application20);
			assertFalse(application20.getComponents().isEmpty());
			for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : application20.getComponents()) {
				assertNotNull(component.getType());
				assertFalse(component.getType().eIsProxy());
				IFile refFile = EcorePlatformUtil.getFile(component.getType());
				assertNotNull(refFile);
				assertEquals(refWks.hbProject20_D, refFile.getProject());
			}

			// *********************************
			// Uml2 Objects
			Resource resourceUml2_E_1 = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true),
							false);
			assertNotNull(resourceUml2_E_1);
			assertFalse(resourceUml2_E_1.getContents().isEmpty());
			Model model = (Model) resourceUml2_E_1.getContents().get(0);
			assertNotNull(model);
			assertEquals(2, model.getPackagedElements().size());
			PackageableElement uml2Package = model.getPackagedElements().get(1);
			assertFalse(uml2Package.getOwnedElements().isEmpty());
			Interface uml2Interface = (Interface) uml2Package.getOwnedElements().get(0);
			assertNotNull(uml2Interface);
			testObjectUml2.addAll(uml2Interface.getOperations());

			// Test references
			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertFalse(operation.getMethods().get(0).eIsProxy());
				IFile referencedFile = EcorePlatformUtil.getFile(operation.getMethods().get(0));
				assertNotNull(referencedFile);
				assertEquals(refWks.hbProject20_D, referencedFile.getProject());
			}
			//
			//
			// *****Close referenced project hbProject20_D
			synchronizedCloseProject(refWks.hbProject20_D);

			// Verify the references of Hummingbird objects are unresolved
			Resource testResource20_E_1 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true),
							false);

			assertNotNull(testResource20_E_1);
			assertFalse(testResource20_E_1.getContents().isEmpty());
			Application testApplication20 = (Application) testResource20_E_1.getContents().get(0);
			assertNotNull(testApplication20);
			assertFalse(testApplication20.getComponents().isEmpty());
			for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : testApplication20.getComponents()) {
				assertNotNull(component.getType());
				assertTrue(component.getType().eIsProxy());
			}

			// Verify the references of Uml2 Objects are unresolved
			testObjectUml2.clear();
			Resource testresourceUml2_E_1 = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true),
							false);
			assertNotNull(testresourceUml2_E_1);
			assertFalse(testresourceUml2_E_1.getContents().isEmpty());
			Model testModel = (Model) testresourceUml2_E_1.getContents().get(0);
			assertNotNull(testModel);
			assertEquals(2, testModel.getPackagedElements().size());
			org.eclipse.uml2.uml.Package testUml2Package = (Package) testModel.getPackagedElements().get(1);
			assertNotNull(testUml2Package);
			assertFalse(testUml2Package.getOwnedElements().isEmpty());
			Interface testInterface = (Interface) testUml2Package.getOwnedElements().get(0);
			assertNotNull(testInterface);
			assertFalse(testInterface.getOperations().isEmpty());

			testObjectUml2.addAll(testInterface.getOperations());

			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertNotNull(operation.getMethods().get(0));
				assertTrue(operation.getMethods().get(0).eIsProxy());

			}
		}
	}

	// Delete referenced project then verify that all references to objects in deleted objects are unresolved
	public void testProxyResolution_DeleteReferencedProject() throws Exception {
		// Context: HB10 objects
		{
			synchronizedOpenProject(refWks.hbProject10_D);
			synchronizedOpenProject(refWks.hbProject10_E);
			Resource resource10_E_1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true),
							false);
			assertNotNull(resource10_E_1);
			assertFalse(resource10_E_1.getContents().isEmpty());
			org.eclipse.sphinx.examples.hummingbird10.Application application10 = (org.eclipse.sphinx.examples.hummingbird10.Application) resource10_E_1
					.getContents().get(0);
			assertNotNull(application10);
			assertTrue(application10.getComponents().size() > 0);
			for (org.eclipse.sphinx.examples.hummingbird10.Component component : application10.getComponents()) {
				assertNotNull(component.getProvidedInterfaces());
				for (org.eclipse.sphinx.examples.hummingbird10.Interface proInterface : component.getProvidedInterfaces()) {
					assertFalse(proInterface.eIsProxy());
					IFile refFile = EcorePlatformUtil.getFile(proInterface);
					assertNotNull(refFile);
					assertEquals(refWks.hbProject10_D, refFile.getProject());
				}
			}
			// /
			// Close referenced project
			synchronizedDeleteProject(refWks.hbProject10_D);
			// /
			// Verify the references are unresolved
			Resource testResource10E_1 = refWks.editingDomain10.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true),
							false);
			assertNotNull(testResource10E_1);
			assertFalse(testResource10E_1.getContents().isEmpty());
			org.eclipse.sphinx.examples.hummingbird10.Application testApplication10 = (org.eclipse.sphinx.examples.hummingbird10.Application) testResource10E_1
					.getContents().get(0);
			assertNotNull(testApplication10);
			assertTrue(testApplication10.getComponents().size() > 0);
			for (org.eclipse.sphinx.examples.hummingbird10.Component component : testApplication10.getComponents()) {
				assertNotNull(component.getProvidedInterfaces());
				for (org.eclipse.sphinx.examples.hummingbird10.Interface proInterface : component.getProvidedInterfaces()) {
					assertTrue(proInterface.eIsProxy());
				}
			}
		}
		// ---------------------------------------------------------------------------------------------------
		// Context: HB 20 Objects and UML Objecst

		{
			synchronizedOpenProject(refWks.hbProject20_D);
			synchronizedOpenProject(refWks.hbProject20_E);

			// HB20 Objects
			Resource resource20_E_1 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true),
							false);

			assertNotNull(resource20_E_1);
			assertFalse(resource20_E_1.getContents().isEmpty());
			Application application20 = (Application) resource20_E_1.getContents().get(0);
			assertNotNull(application20);
			assertFalse(application20.getComponents().isEmpty());
			for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : application20.getComponents()) {
				assertNotNull(component.getType());
				assertFalse(component.getType().eIsProxy());
				IFile refFile = EcorePlatformUtil.getFile(component.getType());
				assertNotNull(refFile);
				assertEquals(refWks.hbProject20_D, refFile.getProject());
			}

			// *********************************
			// Uml2 Objects
			List<Operation> testObjectUml2 = new ArrayList<Operation>();
			Resource resourceUml2_E_1 = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true),
							false);
			assertNotNull(resourceUml2_E_1);
			assertFalse(resourceUml2_E_1.getContents().isEmpty());
			Model model = (Model) resourceUml2_E_1.getContents().get(0);
			assertNotNull(model);
			assertEquals(2, model.getPackagedElements().size());
			PackageableElement uml2Package = model.getPackagedElements().get(1);
			assertFalse(uml2Package.getOwnedElements().isEmpty());
			Interface uml2Interface = (Interface) uml2Package.getOwnedElements().get(0);
			assertNotNull(uml2Interface);
			testObjectUml2.addAll(uml2Interface.getOperations());

			// Test references
			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertFalse(operation.getMethods().get(0).eIsProxy());
				IFile referencedFile = EcorePlatformUtil.getFile(operation.getMethods().get(0));
				assertNotNull(referencedFile);
				assertEquals(refWks.hbProject20_D, referencedFile.getProject());
			}
			//
			//
			// *****Delete referenced project hbProject20_D
			synchronizedDeleteProject(refWks.hbProject20_D);
			//
			// Verify the references of Hummingbird objects are unresolved
			Resource testResource20_E_1 = refWks.editingDomain20.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true),
							false);

			assertNotNull(testResource20_E_1);
			assertFalse(testResource20_E_1.getContents().isEmpty());
			Application testApplication20 = (Application) testResource20_E_1.getContents().get(0);
			assertNotNull(testApplication20);
			assertFalse(testApplication20.getComponents().isEmpty());
			for (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component : testApplication20.getComponents()) {
				assertNotNull(component.getType());
				assertTrue(component.getType().eIsProxy());
			}
			// Verify the references of Uml2 Objects are unresolved
			testObjectUml2.clear();
			Resource testresourceUml2_E_1 = refWks.editingDomainUml2.getResourceSet()
					.getResource(URI.createPlatformResourceURI(
							DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true),
							false);
			assertNotNull(testresourceUml2_E_1);
			assertTrue(testresourceUml2_E_1.getContents().size() > 0);
			Model testModel = (Model) testresourceUml2_E_1.getContents().get(0);
			assertNotNull(testModel);
			assertEquals(2, testModel.getPackagedElements().size());
			org.eclipse.uml2.uml.Package testUml2Package = (Package) testModel.getPackagedElements().get(1);
			assertNotNull(testUml2Package);
			assertFalse(testUml2Package.getOwnedElements().isEmpty());
			Interface testInterface = (Interface) testUml2Package.getOwnedElements().get(0);
			assertNotNull(testInterface);
			assertFalse(testInterface.getOperations().isEmpty());

			testObjectUml2.addAll(testInterface.getOperations());

			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertNotNull(operation.getMethods().get(0));
				assertTrue(operation.getMethods().get(0).eIsProxy());

			}
		}
	}

	// Add reference from hbProject10_A to hbProject10_B verify that all proxies are resolved
	// Then remove the reference and verify that all reference from A to B are unresolved
	// Re- add reference and verify again that references from A to B are re- resolved
	public void testProxyResolution_IntegrateAddingRemovingReferencedProject() throws Exception {
		// Context: UML Objects
		{
			synchronizedOpenProject(refWks.hbProject20_D);
			synchronizedOpenProject(refWks.hbProject20_E);

			List<Operation> testObjectUml2 = new ArrayList<Operation>();
			Resource resourceUml2_E_1 = null;
			for (Resource res : refWks.editingDomainUml2.getResourceSet().getResources()) {
				if (DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1.equals(res.getURI().lastSegment())) {
					resourceUml2_E_1 = res;
					break;
				}
			}
			assertNotNull(resourceUml2_E_1);
			assertTrue(resourceUml2_E_1.getContents().size() > 0);
			Model model = (Model) resourceUml2_E_1.getContents().get(0);
			assertNotNull(model);
			assertEquals(2, model.getPackagedElements().size());
			PackageableElement uml2Package = model.getPackagedElements().get(1);
			assertFalse(uml2Package.getOwnedElements().isEmpty());
			Interface uml2Interface = (Interface) uml2Package.getOwnedElements().get(0);
			assertNotNull(uml2Interface);
			testObjectUml2.addAll(uml2Interface.getOperations());

			// Test GatewayInstance have resolved references
			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertFalse(operation.getMethods().get(0).eIsProxy());
				IFile referencedFile = EcorePlatformUtil.getFile(operation.getMethods().get(0));
				assertNotNull(referencedFile);
				assertEquals(refWks.hbProject20_D, referencedFile.getProject());
			}
			//
			// ****Remove reference from hbProject20_E to hbProject20_D
			IProjectDescription hbProject20_E_prjDesc = refWks.hbProject20_E.getDescription();
			hbProject20_E_prjDesc.setReferencedProjects(new IProject[] {});
			refWks.hbProject20_E.setDescription(hbProject20_E_prjDesc, new NullProgressMonitor());
			waitForModelLoading();

			//
			// Verify the references are unresolved
			testObjectUml2.clear();
			Resource testresourceUml2_E_1 = null;
			for (Resource res : refWks.editingDomainUml2.getResourceSet().getResources()) {
				if (DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1.equals(res.getURI().lastSegment())) {
					testresourceUml2_E_1 = res;
					break;
				}
			}
			assertNotNull(testresourceUml2_E_1);
			assertTrue(testresourceUml2_E_1.getContents().size() > 0);
			Model testModel = (Model) testresourceUml2_E_1.getContents().get(0);
			assertNotNull(testModel);
			assertEquals(2, testModel.getPackagedElements().size());
			org.eclipse.uml2.uml.Package testUml2Package = (Package) testModel.getPackagedElements().get(1);
			assertNotNull(testUml2Package);
			assertFalse(testUml2Package.getOwnedElements().isEmpty());
			Interface testInterface = (Interface) testUml2Package.getOwnedElements().get(0);
			assertNotNull(testInterface);
			assertFalse(testInterface.getOperations().isEmpty());

			testObjectUml2.addAll(testInterface.getOperations());

			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertNotNull(operation.getMethods().get(0));
				assertTrue(operation.getMethods().get(0).eIsProxy());
				IFile referencedFile = EcorePlatformUtil.getFile(operation.getMethods().get(0));
				assertNull(referencedFile);
			}
			// Re- add reference from hbProject20_D to hbProject20_E

			hbProject20_E_prjDesc.setReferencedProjects(new IProject[] { refWks.hbProject20_D });
			refWks.hbProject20_E.setDescription(hbProject20_E_prjDesc, new NullProgressMonitor());
			waitForModelLoading();

			// Verify the references are resolved
			testObjectUml2.clear();
			testresourceUml2_E_1 = null;
			for (Resource res : refWks.editingDomainUml2.getResourceSet().getResources()) {
				if (DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1.equals(res.getURI().lastSegment())) {
					testresourceUml2_E_1 = res;
					break;
				}
			}
			assertNotNull(testresourceUml2_E_1);
			assertTrue(testresourceUml2_E_1.getContents().size() > 0);
			testModel = (Model) testresourceUml2_E_1.getContents().get(0);
			assertNotNull(testModel);
			assertEquals(2, testModel.getPackagedElements().size());
			testUml2Package = (Package) testModel.getPackagedElements().get(1);
			assertNotNull(testUml2Package);
			assertFalse(testUml2Package.getOwnedElements().isEmpty());
			testInterface = (Interface) testUml2Package.getOwnedElements().get(0);
			assertNotNull(testInterface);
			assertFalse(testInterface.getOperations().isEmpty());

			testObjectUml2.addAll(testInterface.getOperations());

			for (Operation operation : testObjectUml2) {
				assertFalse(operation.getMethods().isEmpty());
				assertFalse(operation.getMethods().get(0).eIsProxy());
				IFile referencedFile = EcorePlatformUtil.getFile(operation.getMethods().get(0));
				assertNotNull(referencedFile);
				assertEquals(refWks.hbProject20_D, referencedFile.getProject());
			}
		}
	}

	private boolean hasContentTypeOf(IFile file, AbstractMetaModelDescriptor modelDescriptor) throws CoreException {
		String contentTypeId = ExtendedPlatform.getContentTypeId(file);
		if (contentTypeId == null) {
			return false;
		}
		if (modelDescriptor.getContentTypeIds().contains(contentTypeId)) {
			return true;
		}
		if (modelDescriptor.getCompatibleContentTypeIds().contains(contentTypeId)) {
			return true;

		}
		return false;
	}
}