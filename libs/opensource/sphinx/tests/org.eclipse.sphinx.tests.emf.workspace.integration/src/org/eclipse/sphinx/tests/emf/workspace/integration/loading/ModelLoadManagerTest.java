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
package org.eclipse.sphinx.tests.emf.workspace.integration.loading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

public class ModelLoadManagerTest extends DefaultIntegrationTestCase {
	private int uml2ReferencedFiles_Of_HbProject20_E = 2;

	List<String> hbProject10AResources10;
	int resources10FromHbProject10_A;

	List<String> hbProject20BResources20;
	int resources20FromHbProject20_B;
	List<String> hbProject20BResourcesUml2;
	int resourcesUml2FromHbProject20_B;

	List<String> hbProject20DResources20;
	int resources20FromHbProject20_D;
	List<String> hbProject20DResourcesUml2;
	int resourcesUml2FromHbProject20_D;

	List<String> hbProject20EResources20;
	int resources20FromHbProject20_E;
	List<String> hbProject20EResourcesUml2;
	int resourcesUml2FromHbProject20_E;

	public ModelLoadManagerTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		hbProject10AResources10 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_A = hbProject10AResources10.size();

		hbProject20BResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_B = hbProject20BResources20.size();
		hbProject20BResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_B = hbProject20BResourcesUml2.size();

		hbProject20DResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_D = hbProject20DResources20.size();
		hbProject20DResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_D = hbProject20DResourcesUml2.size();

		hbProject20EResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_E = hbProject20EResources20.size();
		hbProject20EResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_E = hbProject20EResourcesUml2.size();

	}

	// =================================================================================
	// ========================== UNLOAD =============================================
	// =================================================================================
/**
	 * Test method for {@link ModelLoadManager#unloadProjects(Collection, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 *
	 * @throws Exception
	 */
	public void testUnloadProjects() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		Collection<IProject> projectList20 = new ArrayList<IProject>();
		projectList20.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));

		Collection<IProject> projectListMixed1020 = new ArrayList<IProject>();
		projectListMixed1020.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectListMixed1020.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		/* Test cases */
		// Unload projects without its referenced
		ModelLoadManager.INSTANCE.unloadProjects(projectList20, false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		for (IProject project : projectList20) {
			assertReferenceProjectAllResourcesNotLoaded(project.getName());
		}

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);

		// Unload projects including its referenced
		ModelLoadManager.INSTANCE.unloadProjects(projectList20, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_D;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);
		for (IProject project : projectList20) {
			assertReferenceProjectAllResourcesNotLoaded(project.getName());
		}
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);

		// Unload project collection: mixed HB10 and HB20 Projects
		ModelLoadManager.INSTANCE.unloadProjects(projectListMixed1020, true, false, null);

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B;
		editingDomain10ResourceCount = editingDomain10ResourceCount - resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);
		for (IProject project : projectListMixed1020) {
			assertReferenceProjectAllResourcesNotLoaded(project.getName());
		}
		projectListMixed1020.clear();
		projectList20.clear();

	}

	/**
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 *             Test method for
	 *             {@link ModelLoadManager#unloadProjects(Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)

	 */
	public void testUnloadProjectsWithMMDescriptor() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		Collection<IProject> projectList20 = new ArrayList<IProject>();
		projectList20.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));

		Collection<IProject> projectListMixed1020 = new ArrayList<IProject>();
		projectListMixed1020.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectListMixed1020.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		/* Test cases */
		// ==================================================================
		// Unload projects without its referenced
		// Unload Projects 20 with Hummingbird10MM
		ModelLoadManager.INSTANCE.unloadProjects(projectList20, false, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		// Verify that resources were not unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		// Unload Projects 20 with Hummingbird20MM
		ModelLoadManager.INSTANCE.unloadProjects(projectList20, false, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		// Verify that resourcex20 in projects unloaded
		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		// ==================================================================
		// Unload projects including its referenced
		// Unload Projects 20 with Hummingbird10MM
		ModelLoadManager.INSTANCE.unloadProjects(projectList20, true, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		// Verify that resources in projects were not unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);

		// Unload Projects 20 with Hummingbird20MM
		ModelLoadManager.INSTANCE.unloadProjects(projectList20, true, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		// Verify that resources in projects were unloaded
		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// Unload Projects 20 with UML2MM
		ModelLoadManager.INSTANCE.unloadProjects(projectList20, true, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		// Verify that resources in projects were unloaded
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E - resourcesUml2FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		// ==================================================================
		// Unload project collection: mixed HB10 and HB20 Projects
		// Unload with Hummingbird10MM
		ModelLoadManager.INSTANCE.unloadProjects(projectListMixed1020, true, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		editingDomain10ResourceCount = editingDomain10ResourceCount - resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// Unload with Hummingbird20MM
		ModelLoadManager.INSTANCE.unloadProjects(projectListMixed1020, true, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		// Unload with UML2MM
		ModelLoadManager.INSTANCE.unloadProjects(projectListMixed1020, true, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// Clear project lists
		projectListMixed1020.clear();
		projectList20.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#unloadProject(IProject, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testUnloadProject() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* Test cases */
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);
		// ===============================================================
		// Unload of a simple project
		// HB20Project
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

		// HB10Project
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), true, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		// ===============================================================
		// unload of a project and its referenced projects.
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E - resources20FromHbProject20_D;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E - resourcesUml2FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

	}

	/**
	 * Test method for
	 * {@link ModelLoadManager#unloadProject(IProject, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)

	 */
	public void testUnloadProjectWithMMDescriptor() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* Test cases */
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);
		// ===============================================================
		// Unload of a simple project

		// HB10Project with its MMDescriptor
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), true,
				Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		// HB20Project
		// -- with Hummingbird10MM
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		// -- with Hummingbird20MM
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);

		// --with UML2MM
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		// ===============================================================
		// unload of a project and its referenced projects.
		// -- with HB20MM
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E - resources20FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// ---with UML2MM
		ModelLoadManager.INSTANCE.unloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E - resourcesUml2FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
	}

	/**
	 * Test method for
	 * {@link ModelLoadManager#unloadModels(Collection, boolean, org.eclipse.core.runtime.IProgressMonitor)

	 */
	public void testUnloadModels() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* Test cases */
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		List<IModelDescriptor> mixedModelList1020Uml2 = new ArrayList<IModelDescriptor>();
		mixedModelList1020Uml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		mixedModelList1020Uml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B)));

		List<IModelDescriptor> modelList20 = new ArrayList<IModelDescriptor>();
		modelList20.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E),
				Hummingbird20MMDescriptor.INSTANCE));

		List<IModelDescriptor> modelListUml2 = new ArrayList<IModelDescriptor>();
		modelListUml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), UML2MMDescriptor.INSTANCE));

		// ===============================================================
		// Unload mixedModelList1020Uml2
		ModelLoadManager.INSTANCE.unloadModels(mixedModelList1020Uml2, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B;
		editingDomain10ResourceCount = editingDomain10ResourceCount - resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

		// ===============================================================
		ModelLoadManager.INSTANCE.unloadModels(modelList20, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E - resources20FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		// ===============================================================
		// unload of a project and its referenced projects.
		ModelLoadManager.INSTANCE.unloadModels(modelListUml2, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E - resourcesUml2FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

	}

	/**
	 * Test method for
	 * {@link ModelLoadManager#unloadModels(Collection, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 * with 2nd argument is FALSE
	 */
	public void testUnloadModelsWithoutReferencedRoots() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* Test cases */
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		List<IModelDescriptor> modelList20Uml2 = new ArrayList<IModelDescriptor>();
		modelList20Uml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E)));

		// ===============================================================
		ModelLoadManager.INSTANCE.unloadModels(modelList20Uml2, false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

	}

	/**
	 * Test method for
	 * {@link ModelLoadManager#unloadModel(org.eclipse.sphinx.emf.model.IModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)

	 */
	public void testUnloadModel() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* Test cases */
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		IModelDescriptor hb10Model_10A = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));

		IModelDescriptor hb20Model_20B = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1));

		IModelDescriptor uml2Model_20B = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1));

		IModelDescriptor hb20Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		IModelDescriptor uml2Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		// ===============================================================
		// Unload model 10 in hbProject10A
		ModelLoadManager.INSTANCE.unloadModel(hb10Model_10A, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		// ===============================================================
		// Unload model 20 of hbProject20B
		ModelLoadManager.INSTANCE.unloadModel(hb20Model_20B, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);

		// Unload model Uml2 of hbProject20B
		ModelLoadManager.INSTANCE.unloadModel(uml2Model_20B, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		// ===============================================================
		ModelLoadManager.INSTANCE.unloadModel(hb20Model_20E, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E - resources20FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);

		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		// ===============================================================
		// unload of a project and its referenced projects.
		ModelLoadManager.INSTANCE.unloadModel(uml2Model_20E, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E - resourcesUml2FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
	}

	/**
	 * Test method for
	 * {@link ModelLoadManager#unloadModel(org.eclipse.sphinx.emf.model.IModelDescriptor, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 * with 3rd argument is FALSE
	 */
	public void testUnloadModelWithoutReferencedRoots() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* Test cases */
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		IModelDescriptor hb20Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		IModelDescriptor uml2Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		// ===============================================================
		ModelLoadManager.INSTANCE.unloadModel(hb20Model_20E, false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

		assertReferenceProjectResourcesLoaded(UML2MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		assertReferenceProjectResourcesNotLoaded(Hummingbird20MMDescriptor.INSTANCE, DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
		// ===============================================================
		// unload of a project and its referenced projects.
		ModelLoadManager.INSTANCE.unloadModel(uml2Model_20E, false, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		assertReferenceProjectAllResourcesLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		assertReferenceProjectAllResourcesNotLoaded(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	/**
	 * Test method for
	 * {@link ModelLoadManager#unloadFiles(Collection, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)

	 */

	public void testUnloadFiles() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		Resource resource20_B_1 = null;
		Resource resource20_B_2 = null;
		Resource resource20_B_3 = null;
		Resource resource10_A_1 = null;
		Resource resource10_A_2 = null;
		Resource resource10_A_3 = null;
		Resource resource20_D_1 = null;
		Resource resource20_D_2 = null;
		Resource resource20_D_3 = null;
		for (Resource res : refWks.editingDomain20.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1.equals(res.getURI().lastSegment())) {
				resource20_B_1 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2.equals(res.getURI().lastSegment())) {
				resource20_B_2 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3.equals(res.getURI().lastSegment())) {
				resource20_B_3 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1.equals(res.getURI().lastSegment())) {
				resource20_D_1 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2.equals(res.getURI().lastSegment())) {
				resource20_D_2 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3.equals(res.getURI().lastSegment())) {
				resource20_D_3 = res;

			}

		}
		for (Resource res : refWks.editingDomain10.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1.equals(res.getURI().lastSegment())) {
				resource10_A_1 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2.equals(res.getURI().lastSegment())) {
				resource10_A_2 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3.equals(res.getURI().lastSegment())) {
				resource10_A_3 = res;

			}
		}

		assertNotNull(resource20_B_1);
		assertNotNull(resource20_B_2);
		assertNotNull(resource20_B_3);
		assertNotNull(resource10_A_1);
		assertNotNull(resource10_A_2);
		assertNotNull(resource10_A_3);
		assertNotNull(resource20_D_1);
		assertNotNull(resource20_D_2);
		assertNotNull(resource20_D_3);

		IFile file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		IFile file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(file_Project20_B_1);
		assertNotNull(file_Project20_B_2);
		assertNotNull(file_Project20_B_3);
		assertNotNull(file_Project20_D_1);
		assertNotNull(file_Project20_D_2);
		assertNotNull(file_Project20_D_3);
		assertNotNull(file_Project10_A_1);
		assertNotNull(file_Project10_A_2);
		assertNotNull(file_Project10_A_3);

		Collection<IFile> filesList_20_Only = new ArrayList<IFile>();
		filesList_20_Only.add(file_Project20_B_1);
		filesList_20_Only.add(file_Project20_B_2);

		Collection<IFile> filesList_10_Only = new ArrayList<IFile>();
		filesList_10_Only.add(file_Project10_A_1);

		Collection<IFile> filesList_10_20_Mixed_1 = new ArrayList<IFile>();
		filesList_10_20_Mixed_1.add(file_Project20_B_3);
		filesList_10_20_Mixed_1.add(file_Project10_A_2);

		Collection<IFile> filesList_10_20_Mixed_2 = new ArrayList<IFile>();
		filesList_10_20_Mixed_2.add(file_Project20_D_1);
		filesList_10_20_Mixed_2.add(file_Project20_D_2);
		filesList_10_20_Mixed_2.add(file_Project20_D_3);
		filesList_10_20_Mixed_2.add(file_Project10_A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_3);

		ModelLoadManager.INSTANCE.unloadFiles(filesList_20_Only, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - filesList_20_Only.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_3);

		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_Only, true, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - filesList_10_Only.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, resource10_A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_3);

		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_20_Mixed_1, true, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - 1;
		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, resource10_A_2);

	}

	/**
	 * Test method for
	 * {@link ModelLoadManager#unloadFiles(Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)

	 */
	public void testUnloadFilesWithMMDescriptor() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		Resource resource20_B_1 = null;
		Resource resource20_B_2 = null;
		Resource resource20_B_3 = null;
		Resource resource10_A_1 = null;
		Resource resource10_A_2 = null;
		Resource resource10_A_3 = null;
		Resource resource20_D_1 = null;
		Resource resource20_D_2 = null;
		Resource resource20_D_3 = null;
		for (Resource res : refWks.editingDomain20.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1.equals(res.getURI().lastSegment())) {
				resource20_B_1 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2.equals(res.getURI().lastSegment())) {
				resource20_B_2 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3.equals(res.getURI().lastSegment())) {
				resource20_B_3 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1.equals(res.getURI().lastSegment())) {
				resource20_D_1 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2.equals(res.getURI().lastSegment())) {
				resource20_D_2 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3.equals(res.getURI().lastSegment())) {
				resource20_D_3 = res;

			}

		}
		for (Resource res : refWks.editingDomain10.getResourceSet().getResources()) {
			if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1.equals(res.getURI().lastSegment())) {
				resource10_A_1 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2.equals(res.getURI().lastSegment())) {
				resource10_A_2 = res;

			} else if (DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3.equals(res.getURI().lastSegment())) {
				resource10_A_3 = res;

			}
		}

		assertNotNull(resource20_B_1);
		assertNotNull(resource20_B_2);
		assertNotNull(resource20_B_3);
		assertNotNull(resource10_A_1);
		assertNotNull(resource10_A_2);
		assertNotNull(resource10_A_3);
		assertNotNull(resource20_D_1);
		assertNotNull(resource20_D_2);
		assertNotNull(resource20_D_3);

		IFile file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		IFile file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertNotNull(file_Project20_B_1);
		assertNotNull(file_Project20_B_2);
		assertNotNull(file_Project20_B_3);
		assertNotNull(file_Project20_D_1);
		assertNotNull(file_Project20_D_2);
		assertNotNull(file_Project20_D_3);
		assertNotNull(file_Project10_A_1);
		assertNotNull(file_Project10_A_2);
		assertNotNull(file_Project10_A_3);

		Collection<IFile> filesList_20_Only = new ArrayList<IFile>();
		filesList_20_Only.add(file_Project20_B_1);
		filesList_20_Only.add(file_Project20_B_2);

		Collection<IFile> filesList_10_Only = new ArrayList<IFile>();
		filesList_10_Only.add(file_Project10_A_1);

		Collection<IFile> filesList_10_20_Mixed_1 = new ArrayList<IFile>();
		filesList_10_20_Mixed_1.add(file_Project20_B_3);
		filesList_10_20_Mixed_1.add(file_Project10_A_2);

		Collection<IFile> filesList_10_20_Mixed_2 = new ArrayList<IFile>();
		filesList_10_20_Mixed_2.add(file_Project20_D_1);
		filesList_10_20_Mixed_2.add(file_Project20_D_2);
		filesList_10_20_Mixed_2.add(file_Project20_D_3);
		filesList_10_20_Mixed_2.add(file_Project10_A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_3);

		// ==============================================
		// Unload hb20 files with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.unloadFiles(filesList_20_Only, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		// Verify that no resource was unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		// Unload hb20 files with Hummingbird20MMDescriptor
		ModelLoadManager.INSTANCE.unloadFiles(filesList_20_Only, Hummingbird20MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - filesList_20_Only.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_3);

		// ==============================================
		// Unload hb10 files with Uml2MM
		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_Only, UML2MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		// verify that no resource was unloaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		// Unload hb10 files with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_Only, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - filesList_10_Only.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, resource10_A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, resource10_A_3);

		// ==============================================
		// Unload mixed models with various releases with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_20_Mixed_1, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, resource10_A_2);

		// Unload mixed models with various release with Hummingbird20MMDescriptor
		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_20_Mixed_1, Hummingbird20MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, resource10_A_2);

		// ==============================================
		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_20_Mixed_2, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, resource20_D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, resource10_A_3);

		ModelLoadManager.INSTANCE.unloadFiles(filesList_10_20_Mixed_2, Hummingbird20MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 3;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, resource20_D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, resource10_A_3);
		filesList_10_20_Mixed_2.clear();
		filesList_10_20_Mixed_1.clear();
		filesList_10_Only.clear();
		filesList_20_Only.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#unloadFile(IFile, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testUnloadFile() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hb_file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		IFile uml2_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);
		assertNotNull(uml2_file_Project20_B_3);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project20_D_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(hb_file_Project10_A_2);
		assertNotNull(hb_file_Project10_A_3);

		/* test unload file with file only */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project20_B_1, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_B_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_B_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_B_3.getName());

		/* test unload file with file only */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project20_B_2, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_B_1.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_B_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_B_3.getName());

		/* test unload file with file only */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project20_B_3, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_B_1.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_B_2.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_B_3.getName());

		/* test unload file with file only */
		ModelLoadManager.INSTANCE.unloadFile(uml2_file_Project20_B_1, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, uml2_file_Project20_B_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_3.getName());

		/* same tests for Hummingbird10 project */

		/* test unload file with file only */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project10_A_1, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, hb_file_Project10_A_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_3.getName());
		/* test unload file with file only */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project10_A_2, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, hb_file_Project10_A_1.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, hb_file_Project10_A_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_3.getName());

		/* test unload file with file only */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project10_A_3, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, hb_file_Project10_A_1.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, hb_file_Project10_A_2.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, hb_file_Project10_A_3.getName());

	}

/**
	 * Test method for {@link ModelLoadManager#unloadFile(IFile, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testUnloadFileWithMMDescriptor() throws Exception {
		/* Creation of test variables */
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hb_file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		IFile uml2_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);
		assertNotNull(uml2_file_Project20_B_3);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project20_D_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(hb_file_Project10_A_2);
		assertNotNull(hb_file_Project10_A_3);

		/* test unload file withfile and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(uml2_file_Project20_B_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_3.getName());

		/* test unload file withfile and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(uml2_file_Project20_B_1, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_3.getName());

		/* test unload file withfile and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(uml2_file_Project20_B_1, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, uml2_file_Project20_B_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomainUml2, uml2_file_Project20_B_3.getName());

		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project20_D_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_D_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_D_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_D_3.getName());

		/* test unload file with file and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project20_D_1, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_D_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_D_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_D_3.getName());

		/* test unload file with file and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project20_D_2, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_D_1.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_D_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain20, hb_file_Project20_D_3.getName());
		/* test unload file with file and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project20_D_3, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_D_1.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_D_2.getName());
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, hb_file_Project20_D_3.getName());

		/* same tests for Hummingbird10 project */

		/* test unload file with file and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project10_A_1, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_3.getName());

		/* test unload file with file and metamodel descriptor */
		ModelLoadManager.INSTANCE.unloadFile(hb_file_Project10_A_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount - 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, hb_file_Project10_A_1.getName());
		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_2.getName());
		assertEditingDomainContainsResource(refWks.editingDomain10, hb_file_Project10_A_3.getName());

	}

	// =================================================================================
	// ========================== LOAD =================================================
	// =================================================================================
/**
	 * Test method for {@link ModelLoadManager#loadFile(IFile, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */

	public void testLoadFile() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		/* Test cases */
		// =======================================================
		/* Load file HB 20 */
		ModelLoadManager.INSTANCE.loadFile(hb_file_Project20_B_1, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		// --------------------
		ModelLoadManager.INSTANCE.loadFile(hb_file_Project20_B_2, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		// --------------------
		ModelLoadManager.INSTANCE.loadFile(hb_file_Project20_B_3, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		// =======================================================
		/* Load file Uml2 file */
		ModelLoadManager.INSTANCE.loadFile(uml2_file_Project20_B_1, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		// --------------------
		ModelLoadManager.INSTANCE.loadFile(uml2_file_Project20_B_2, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		// =======================================================
		/* Load file HB 10 file */
		ModelLoadManager.INSTANCE.loadFile(hb_file_Project10_A_1, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);

	}

/**
	 * Test method for {@link ModelLoadManager#loadFile(IFile, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadFileWithMMDescriptor() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);

		IFile uml2_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);

		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(uml2_file_Project20_D_1);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		/* Test cases */

		// ===========================================
		/* Load HB 20 file with metamodelDescriptor */

		ModelLoadManager.INSTANCE.loadFile(hb_file_Project20_D_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		// -----------------
		ModelLoadManager.INSTANCE.loadFile(hb_file_Project20_D_1, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		// ----------------

		ModelLoadManager.INSTANCE.loadFile(hb_file_Project20_D_1, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		// ====================================================
		/* Load Uml2 file with metamodelDescriptor */

		ModelLoadManager.INSTANCE.loadFile(uml2_file_Project20_D_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		// -------------------------
		ModelLoadManager.INSTANCE.loadFile(uml2_file_Project20_D_1, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		// -------------------------
		ModelLoadManager.INSTANCE.loadFile(uml2_file_Project20_D_1, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		// ===================================
		/* Load HB 10 file with metamodelDescriptor */

		ModelLoadManager.INSTANCE.loadFile(hb_file_Project10_A_1, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		// --------------------------
		ModelLoadManager.INSTANCE.loadFile(hb_file_Project10_A_1, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		// ------------------------------
		ModelLoadManager.INSTANCE.loadFile(hb_file_Project10_A_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
	}

/**
	 * Test method for {@link ModelLoadManager#loadFiles(Collection, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadFiles() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hb_file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		IFile uml2_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project20_D_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(hb_file_Project10_A_2);
		assertNotNull(hb_file_Project10_A_3);
		assertNotNull(uml2_file_Project20_D_1);
		assertNotNull(uml2_file_Project20_D_2);
		assertNotNull(uml2_file_Project20_D_3);
		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);
		assertNotNull(uml2_file_Project20_B_3);

		Collection<IFile> hb_20_files = new ArrayList<IFile>();
		hb_20_files.add(hb_file_Project20_B_1);
		hb_20_files.add(hb_file_Project20_D_1);

		Collection<IFile> hb_10_files = new ArrayList<IFile>();
		hb_10_files.add(hb_file_Project10_A_1);

		Collection<IFile> hb_10_20_files = new ArrayList<IFile>();
		hb_10_20_files.add(hb_file_Project10_A_2);
		hb_10_20_files.add(hb_file_Project20_B_2);

		Collection<IFile> uml2_files = new ArrayList<IFile>();
		uml2_files.add(uml2_file_Project20_B_1);
		uml2_files.add(uml2_file_Project20_D_1);

		Collection<IFile> uml2_hb_10_20_files = new ArrayList<IFile>();
		uml2_hb_10_20_files.add(hb_file_Project10_A_3);
		uml2_hb_10_20_files.add(hb_file_Project20_B_3);
		uml2_hb_10_20_files.add(hb_file_Project20_D_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_B_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_D_2);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		/* load file with file only */
		ModelLoadManager.INSTANCE.loadFiles(hb_20_files, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + hb_20_files.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

		ModelLoadManager.INSTANCE.loadFiles(hb_10_files, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + hb_10_files.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);

		ModelLoadManager.INSTANCE.loadFiles(uml2_files, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2_files.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);

		ModelLoadManager.INSTANCE.loadFiles(hb_10_20_files, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + 1;
		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);

		ModelLoadManager.INSTANCE.loadFiles(uml2_hb_10_20_files, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + 1;
		editingDomain20ResourceCount = editingDomain20ResourceCount + 2;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 2;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);

	}

/**
	 * Test method for {@link ModelLoadManager#loadFiles(Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadFilesWithMMDescriptor() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);

		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project10_A_3);
		assertNotNull(uml2_file_Project20_D_2);
		assertNotNull(uml2_file_Project20_B_2);

		Collection<IFile> uml2_hb_10_20_files = new ArrayList<IFile>();
		uml2_hb_10_20_files.add(hb_file_Project10_A_3);
		uml2_hb_10_20_files.add(hb_file_Project20_B_3);
		uml2_hb_10_20_files.add(hb_file_Project20_D_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_B_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_D_2);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		/* load file with file and metamodel descriptor */

		ModelLoadManager.INSTANCE.loadFiles(uml2_hb_10_20_files, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);

		ModelLoadManager.INSTANCE.loadFiles(uml2_hb_10_20_files, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 2;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);

		ModelLoadManager.INSTANCE.loadFiles(uml2_hb_10_20_files, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 2;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
	}

/**
	 * Test method for {@link ModelLoadManager#loadProject(IProject, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadProject() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);
		/* Since all resources previously in the workspace are unloaded we can start testing loadProject method */
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_B;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), false, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_E;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_E + uml2ReferencedFiles_Of_HbProject20_E;
		// referenced files of HB_PROJECT_20_D were loaded also :UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_D;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E + resourcesUml2FromHbProject20_D;

		// Remove duplicated files in HB_PROJECT_20_D
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		projectsTestToUnload.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#loadProject(IProject, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadProjectWithMMDescriptor() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);
		/* Since all resources previously in the workspace are unloaded we can start testing loadProject method */
		// =======================================================================
		// Load Project 20 with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		// No resource were loaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		// Load project 20B with Hummingbird20MMDescriptor
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		// Load project 20B with uml2MM

		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		// =======================================================================
		// Load hb10 project with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), false,
				Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		// =======================================================================
		// Case of references
		// Load project with hb20 release
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), false,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_E;
		// referenced files of HB_PROJECT_20_D were loaded also :UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// -----------------
		// Load project with Uml2 release
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), false,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_E + uml2ReferencedFiles_Of_HbProject20_E;
		// referenced files of HB_PROJECT_20_D were loaded also :UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// ---------------------
		// Load project include referenced project
		// --with hb20 release
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_D;
		// Remove duplicated files in HB_PROJECT_20_D
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// --with UML2MM
		ModelLoadManager.INSTANCE.loadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E + resourcesUml2FromHbProject20_D;

		// Remove duplicated files in HB_PROJECT_20_D
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		projectsTestToUnload.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#loadProjects(Collection, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadProjects() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		Collection<IProject> project20List = new ArrayList<IProject>();
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));

		Collection<IProject> project10List = new ArrayList<IProject>();
		project10List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));

		ModelLoadManager.INSTANCE.loadProjects(project20List, false, false, null);
		waitForModelLoading();

		for (IProject project : project20List) {
			editingDomain10ResourceCount = editingDomain10ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird10MMDescriptor.INSTANCE).size();

			editingDomain20ResourceCount = editingDomain20ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird20MMDescriptor.INSTANCE).size();

			editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
					+ refWks.getReferenceFiles(project.getName(), UML2MMDescriptor.INSTANCE).size();
		}

		// Uml2 Resource should add 2 referenced files of HB_PROJECT_20_E in HB_PROJECT_20_D(
		// UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2)
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2ReferencedFiles_Of_HbProject20_E;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// ===========================================
		// Load projects include references
		ModelLoadManager.INSTANCE.loadProjects(project20List, true, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird10MMDescriptor.INSTANCE).size();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size();
		// Uml2 resource need to remove duplicated files
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// ====================================
		// Load hb10 projects
		ModelLoadManager.INSTANCE.loadProjects(project10List, false, false, null);
		waitForModelLoading();

		for (IProject project : project10List) {
			editingDomain10ResourceCount = editingDomain10ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird10MMDescriptor.INSTANCE).size();

			editingDomain20ResourceCount = editingDomain20ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird20MMDescriptor.INSTANCE).size();

			editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
					+ refWks.getReferenceFiles(project.getName(), UML2MMDescriptor.INSTANCE).size();
		}
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		project20List.clear();
		project10List.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#loadProjects(Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadProjectsWithMMDescriptor() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, 0);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, 0);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		Collection<IProject> project20List = new ArrayList<IProject>();
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));

		Collection<IProject> project10List = new ArrayList<IProject>();
		project10List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		// ==================================
		// Load 20 projects with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.loadProjects(project20List, false, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();
		for (IProject project : project20List) {

			editingDomain10ResourceCount = editingDomain10ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird10MMDescriptor.INSTANCE).size();

		}
		// No resource was loaded
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		// -----------------
		// Load hb20 projects with Hummingbird20MMDescriptor
		ModelLoadManager.INSTANCE.loadProjects(project20List, false, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		for (IProject project : project20List) {

			editingDomain20ResourceCount = editingDomain20ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird20MMDescriptor.INSTANCE).size();

		}
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// ----------------------------
		// Load hb20 Projects with Uml2MM
		ModelLoadManager.INSTANCE.loadProjects(project20List, false, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		for (IProject project : project20List) {
			editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
					+ refWks.getReferenceFiles(project.getName(), UML2MMDescriptor.INSTANCE).size();
		}

		// Uml2 Resource should add 2 referenced files of HB_PROJECT_20_E in HB_PROJECT_20_D(
		// UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2)
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2ReferencedFiles_Of_HbProject20_E;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// ===========================================
		// Load projects include references
		// --load with hb20MM
		ModelLoadManager.INSTANCE.loadProjects(project20List, true, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// --load with Uml2MM
		ModelLoadManager.INSTANCE.loadProjects(project20List, true, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		// Uml2 resource need to remove duplicated files
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E + resourcesUml2FromHbProject20_D;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// ====================================
		// Load hb10 projecs
		ModelLoadManager.INSTANCE.loadProjects(project10List, false, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		for (IProject project : project10List) {
			editingDomain10ResourceCount = editingDomain10ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird10MMDescriptor.INSTANCE).size();

		}
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		project20List.clear();
		project10List.clear();
	}

/**
	 * Test method for
	 * {@link ModelLoadManager#loadModel(org.eclipse.sphinx.emf.model.IModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadModel() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		IModelDescriptor hb10Model_10A = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));

		IModelDescriptor hb20Model_20B = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1));

		IModelDescriptor uml2Model_20B = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1));

		IModelDescriptor hb20Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		IModelDescriptor uml2Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		assertNotNull(hb10Model_10A);
		assertNotNull(hb20Model_20B);
		assertNotNull(uml2Model_20B);
		assertNotNull(hb20Model_20E);
		assertNotNull(uml2Model_20E);
		// =======================================
		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */
		// --load hb20 model
		ModelLoadManager.INSTANCE.loadModel(hb20Model_20B, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		// --load uml2 model
		ModelLoadManager.INSTANCE.loadModel(uml2Model_20B, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		// =======================================
		// load hb10 model
		ModelLoadManager.INSTANCE.loadModel(hb10Model_10A, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount + resources10FromHbProject10_A;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		// =======================================
		// --load hb20Model in hbProject20E

		ModelLoadManager.INSTANCE.loadModel(hb20Model_20E, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_E + resources20FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		// -- load uml2Model in hbProject20E
		ModelLoadManager.INSTANCE.loadModel(uml2Model_20E, false, null);
		waitForModelLoading();

		// Uml2 Resource of referenced files in HB_PROJECT_20_D were loaded also (UML2_FILE_NAME_20D_1,
		// UML2_FILE_NAME_20D_2)
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		projectsTestToUnload.clear();
	}

/**
	 * Test method for
	 * {@link ModelLoadManager#loadModel(org.eclipse.sphinx.emf.model.IModelDescriptor, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadModelWithoutReferencedRoots() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		IModelDescriptor hb20Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		IModelDescriptor uml2Model_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.getReferenceFile(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		assertNotNull(hb20Model_20E);
		assertNotNull(uml2Model_20E);
		// =================================
		// Load hb20Model including referenced roots
		ModelLoadManager.INSTANCE.loadModel(hb20Model_20E, false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// Load Uml2Model including referenced roots
		ModelLoadManager.INSTANCE.loadModel(uml2Model_20E, false, false, null);
		waitForModelLoading();

		// Removed duplicated files of HB_PROJECR_20_D
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_E + uml2ReferencedFiles_Of_HbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		projectsTestToUnload.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#loadModels(Collection, boolean, org.eclipse.core.runtime.IProgressMonitor).
	 *
	 * @throws Exception
	 */
	public void testLoadModels() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		List<IModelDescriptor> mixedModelList1020Uml2 = new ArrayList<IModelDescriptor>();
		mixedModelList1020Uml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		mixedModelList1020Uml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B)));

		List<IModelDescriptor> modelList20 = new ArrayList<IModelDescriptor>();
		modelList20.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E),
				Hummingbird20MMDescriptor.INSTANCE));

		List<IModelDescriptor> modelListUml2 = new ArrayList<IModelDescriptor>();
		modelListUml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), UML2MMDescriptor.INSTANCE));
		// =====================================
		// Load mixedModelList1020Uml2
		ModelLoadManager.INSTANCE.loadModels(mixedModelList1020Uml2, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_B;
		editingDomain10ResourceCount = editingDomain10ResourceCount + resources10FromHbProject10_A;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		// ======================================
		// Load modelist20 without referenced roots
		ModelLoadManager.INSTANCE.loadModels(modelList20, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_E + resources20FromHbProject20_D;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// =====================================
		// Load modeListUml2 without referenced roots
		ModelLoadManager.INSTANCE.loadModels(modelListUml2, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E;
		// Referenced files in HbProject20D were also loaded: UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		mixedModelList1020Uml2.clear();
		modelList20.clear();
		modelListUml2.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#loadModels(Collection, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 * with 2nd argument is FALSE
	 * @throws Exception
	 */
	public void testLoadModelsWithoutReferencedRoots() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		List<IModelDescriptor> mixedModelList1020Uml2 = new ArrayList<IModelDescriptor>();
		mixedModelList1020Uml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A)));
		mixedModelList1020Uml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks
				.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B)));

		List<IModelDescriptor> modelList20 = new ArrayList<IModelDescriptor>();
		modelList20.addAll(ModelDescriptorRegistry.INSTANCE.getModels(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E),
				Hummingbird20MMDescriptor.INSTANCE));

		List<IModelDescriptor> modelListUml2 = new ArrayList<IModelDescriptor>();
		modelListUml2.addAll(ModelDescriptorRegistry.INSTANCE.getModels(
				refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), UML2MMDescriptor.INSTANCE));
		// =====================================
		// Load mixedModelList1020Uml2
		ModelLoadManager.INSTANCE.loadModels(mixedModelList1020Uml2, false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_B;
		editingDomain10ResourceCount = editingDomain10ResourceCount + resources10FromHbProject10_A;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_B;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		// ======================================
		// Load modelist20 without referenced roots
		ModelLoadManager.INSTANCE.loadModels(modelList20, false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + resources20FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// =====================================
		// Load modeListUml2 without referenced roots
		ModelLoadManager.INSTANCE.loadModels(modelListUml2, false, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + resourcesUml2FromHbProject20_E + uml2ReferencedFiles_Of_HbProject20_E;
		// Referenced files in HbProject20D were also loaded: UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		mixedModelList1020Uml2.clear();
		modelList20.clear();
		modelListUml2.clear();
	}

/**
	 * Test method for {@link ModelLoadManager#loadAllProjects(org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor).
	 *
	 * @throws Exception
	 */
	public void testLoadAllProjects() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		ModelLoadManager.INSTANCE.loadAllProjects(Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.loadAllProjects(UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
				+ refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.loadAllProjects(Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = editingDomain10ResourceCount
				+ refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

	}

/**
	 * Test method for {@link ModelLoadManager#loadWorkspace(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testLoadWorkspace() throws Exception {
		int editingDomain10ResourceCount = 0;
		int editingDomain20ResourceCount = 0;
		int editingDomainUml2ResourceCount = 0;
		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);
		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		ModelLoadManager.INSTANCE.loadWorkspace(false, null);
		waitForModelLoading();

		editingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		editingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		editingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		projectsTestToUnload.clear();

	}

	// =================================================================================
	// ========================== RELOAD ============================================
	// =================================================================================
/**
	 * Test method for {@link ModelLoadManager#reloadFile(IFile, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testReloadFile() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hb_file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		IFile uml2_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);
		assertNotNull(uml2_file_Project20_B_3);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project20_D_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(hb_file_Project10_A_2);
		assertNotNull(hb_file_Project10_A_3);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B - resources20FromHbProject20_D
				- resources20FromHbProject20_E;

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B - resourcesUml2FromHbProject20_D
				- resourcesUml2FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		ModelLoadManager.INSTANCE.reloadFile(hb_file_Project20_B_1, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFile(hb_file_Project20_B_2, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload loaded file
		ModelLoadManager.INSTANCE.reloadFile(hb_file_Project10_A_2, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload Uml2 file
		ModelLoadManager.INSTANCE.reloadFile(uml2_file_Project20_B_2, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
	}

/**
	 * Test method for {@link ModelLoadManager#reloadFile(IFile, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testReloadFileWithMMDescriptor() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hb_file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		IFile uml2_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);
		assertNotNull(uml2_file_Project20_B_3);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project20_D_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(hb_file_Project10_A_2);
		assertNotNull(hb_file_Project10_A_3);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B - resources20FromHbProject20_D
				- resources20FromHbProject20_E;

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B - resourcesUml2FromHbProject20_D
				- resourcesUml2FromHbProject20_E;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */
		// Reload hb20 file with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.reloadFile(hb_file_Project20_B_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload hb20File with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.reloadFile(hb_file_Project20_B_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload hb20File with Hummingbird20MMDescriptor
		ModelLoadManager.INSTANCE.reloadFile(hb_file_Project20_B_1, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload Uml2 file with UML2MM
		ModelLoadManager.INSTANCE.reloadFile(uml2_file_Project20_B_1, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload loaded file
		ModelLoadManager.INSTANCE.reloadFile(uml2_file_Project20_B_1, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload loaded Uml2 file with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.reloadFile(uml2_file_Project20_B_1, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
	}

/**
	 * Test method for {@link ModelLoadManager#reloadFiles(Collection, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testReloadFiles() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hb_file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		IFile uml2_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project20_D_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(hb_file_Project10_A_2);
		assertNotNull(hb_file_Project10_A_3);
		assertNotNull(uml2_file_Project20_D_1);
		assertNotNull(uml2_file_Project20_D_2);
		assertNotNull(uml2_file_Project20_D_3);
		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);
		assertNotNull(uml2_file_Project20_B_3);

		Collection<IFile> hb_20_files = new ArrayList<IFile>();
		hb_20_files.add(hb_file_Project20_B_1);
		hb_20_files.add(hb_file_Project20_D_1);

		Collection<IFile> hb_10_files = new ArrayList<IFile>();
		hb_10_files.add(hb_file_Project10_A_1);

		Collection<IFile> hb_10_20_files = new ArrayList<IFile>();
		hb_10_20_files.add(hb_file_Project10_A_2);
		hb_10_20_files.add(hb_file_Project20_B_2);

		Collection<IFile> uml2_files = new ArrayList<IFile>();
		uml2_files.add(uml2_file_Project20_B_1);
		uml2_files.add(uml2_file_Project20_D_1);

		Collection<IFile> uml2_hb_10_20_files = new ArrayList<IFile>();
		uml2_hb_10_20_files.add(hb_file_Project10_A_3);
		uml2_hb_10_20_files.add(hb_file_Project20_B_3);
		uml2_hb_10_20_files.add(hb_file_Project20_D_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_B_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_D_2);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));

		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B - resources20FromHbProject20_D
				- resources20FromHbProject20_E;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B - resourcesUml2FromHbProject20_D
				- resourcesUml2FromHbProject20_E;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		ModelLoadManager.INSTANCE.reloadFiles(hb_20_files, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + hb_20_files.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload loaded file
		ModelLoadManager.INSTANCE.reloadFiles(hb_10_files, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(uml2_files, true, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2_files.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(hb_10_20_files, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(uml2_hb_10_20_files, true, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 2;
		editingDomain20ResourceCount = editingDomain20ResourceCount + 2;

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);

		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);

		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

	}

/**
	 * Test method for {@link ModelLoadManager#reloadFiles(Collection, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testReloadFilesWithMMDescriptor() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		IFile hb_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		IFile hb_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		IFile hb_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		IFile hb_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hb_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		IFile hb_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		IFile hb_file_Project10_A_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hb_file_Project10_A_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		IFile hb_file_Project10_A_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A).getFile(
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		IFile uml2_file_Project20_D_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2_file_Project20_D_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		IFile uml2_file_Project20_D_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		IFile uml2_file_Project20_B_1 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		IFile uml2_file_Project20_B_2 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		IFile uml2_file_Project20_B_3 = refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B).getFile(
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		assertNotNull(hb_file_Project20_B_1);
		assertNotNull(hb_file_Project20_B_2);
		assertNotNull(hb_file_Project20_B_3);
		assertNotNull(hb_file_Project20_D_1);
		assertNotNull(hb_file_Project20_D_2);
		assertNotNull(hb_file_Project20_D_3);
		assertNotNull(hb_file_Project10_A_1);
		assertNotNull(hb_file_Project10_A_2);
		assertNotNull(hb_file_Project10_A_3);
		assertNotNull(uml2_file_Project20_D_1);
		assertNotNull(uml2_file_Project20_D_2);
		assertNotNull(uml2_file_Project20_D_3);
		assertNotNull(uml2_file_Project20_B_1);
		assertNotNull(uml2_file_Project20_B_2);
		assertNotNull(uml2_file_Project20_B_3);

		Collection<IFile> hb_20_files = new ArrayList<IFile>();
		hb_20_files.add(hb_file_Project20_B_1);
		hb_20_files.add(hb_file_Project20_D_1);

		Collection<IFile> hb_10_files = new ArrayList<IFile>();
		hb_10_files.add(hb_file_Project10_A_1);

		Collection<IFile> hb_10_20_files = new ArrayList<IFile>();
		hb_10_20_files.add(hb_file_Project10_A_2);
		hb_10_20_files.add(hb_file_Project20_B_2);

		Collection<IFile> uml2_files = new ArrayList<IFile>();
		uml2_files.add(uml2_file_Project20_B_1);
		uml2_files.add(uml2_file_Project20_D_1);

		Collection<IFile> uml2_hb_10_20_files = new ArrayList<IFile>();
		uml2_hb_10_20_files.add(hb_file_Project10_A_3);
		uml2_hb_10_20_files.add(hb_file_Project20_B_3);
		uml2_hb_10_20_files.add(hb_file_Project20_D_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_B_2);
		uml2_hb_10_20_files.add(uml2_file_Project20_D_2);

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));

		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount - resources20FromHbProject20_B - resources20FromHbProject20_D
				- resources20FromHbProject20_E;
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - resourcesUml2FromHbProject20_B - resourcesUml2FromHbProject20_D
				- resourcesUml2FromHbProject20_E;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */
		// Reload hb20File with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.reloadFiles(hb_20_files, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);
		// Reload hb20Files with Uml2MMDescriptor
		ModelLoadManager.INSTANCE.reloadFiles(hb_20_files, UML2MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		// Reload hb20Files with Hummingbird20MMDescriptor
		ModelLoadManager.INSTANCE.reloadFiles(hb_20_files, Hummingbird20MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();
		editingDomain20ResourceCount = editingDomain20ResourceCount + hb_20_files.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);
		// Reload loaded files
		ModelLoadManager.INSTANCE.reloadFiles(hb_10_files, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		// Reload UML2 files with Hummingbird10MMDescriptor
		ModelLoadManager.INSTANCE.reloadFiles(uml2_files, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(uml2_files, UML2MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2_files.size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(hb_10_20_files, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(hb_10_20_files, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(hb_10_20_files, Hummingbird20MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 1;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(uml2_hb_10_20_files, Hummingbird10MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(uml2_hb_10_20_files, Hummingbird20MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount + 2;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadFiles(uml2_hb_10_20_files, UML2MMDescriptor.INSTANCE, true, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + 2;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

	}

/**
	 * Test method for
	 * {@link ModelLoadManager#reloadModel(IProject, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)(IProject, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)(IProject, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testReloadModel() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size()

				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size()

				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE).size();
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE).size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */
		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), false,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), false,
				Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), false,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), false,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		// Referenced files in HB_PROJECT_20_D are aslo loaded:UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2ReferencedFiles_Of_HbProject20_E
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true,
				Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true,
				UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		// Remove duplicate files in HB_PROJECT_20_D
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		projectsTestToUnload.clear();

	}

/**
	 * Test method for {@link ModelLoadManager#reloadProject(IProject, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testReloadProject() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE).size();
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE).size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */
		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B), false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size();
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A), false, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), false, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE).size();
		// Referenced files in HB_PROJECT_20_D were also loaded
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2ReferencedFiles_Of_HbProject20_E
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);

		ModelLoadManager.INSTANCE.reloadProject(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E), true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size();
		// Remove duplicate files in HB_PROJECT_20_D
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		projectsTestToUnload.clear();
	}

/**
	 * Test method for
	 * {@link ModelLoadManager#reloadProjects(Collection, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */

	public void testReloadProjects() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE).size();
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE).size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		Collection<IProject> project20List = new ArrayList<IProject>();
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		Collection<IProject> project10List = new ArrayList<IProject>();
		project10List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));

		ModelLoadManager.INSTANCE.reloadProjects(project20List, false, false, null);
		waitForModelLoading();

		for (IProject project : project20List) {
			editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
					+ refWks.getReferenceFiles(project.getName(), UML2MMDescriptor.INSTANCE).size();
			editingDomain20ResourceCount = editingDomain20ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird20MMDescriptor.INSTANCE).size();

			editingDomain10ResourceCount = editingDomain10ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird10MMDescriptor.INSTANCE).size();

		}
		// Referenced files of HB_PROJECT_20_D were also loaded
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2ReferencedFiles_Of_HbProject20_E;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadProjects(project20List, true, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size();
		// Remove duplicate files in HB_PROJECT_20_D
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadProjects(project10List, false, false, null);
		waitForModelLoading();

		for (IProject project : project10List) {
			editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
					+ refWks.getReferenceFiles(project.getName(), UML2MMDescriptor.INSTANCE).size();
			editingDomain20ResourceCount = editingDomain20ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird20MMDescriptor.INSTANCE).size();
		}
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		project20List.clear();
		project10List.clear();
	}

/**
	 * Test method for
	 * {@link ModelLoadManager#reloadProjects(Collection, boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 *
	 * @throws Exception
	 */
	public void testReloadProjectsWithMMDescriptor() throws Exception {
		final int initialEditingDomain10ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird10MMDescriptor.INSTANCE);
		final int initialEditingDomain20ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		final int initialEditingDomainUml2ResourceCount = refWks.getInitialResourcesInReferenceEditingDomainCount(UML2MMDescriptor.INSTANCE);
		int editingDomain10ResourceCount = initialEditingDomain10ResourceCount;
		int editingDomain20ResourceCount = initialEditingDomain20ResourceCount;
		int editingDomainUml2ResourceCount = initialEditingDomainUml2ResourceCount;

		/* We use the previously validated method unloadProjects to unload all test projects in workspace. */
		/* The result of this test method depends on the result of the test method 'testUnloadProjects()' */
		Collection<IProject> projectsTestToUnload = new ArrayList<IProject>();
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		projectsTestToUnload.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		synchronizedUnloadProjects(projectsTestToUnload, true);

		editingDomain20ResourceCount = editingDomain20ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, Hummingbird20MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE).size();
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size()
				- refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE).size();
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		/* Since all resources previously in the workspace are unloaded we can start testing loadFile method */

		Collection<IProject> project20List = new ArrayList<IProject>();
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B));
		project20List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E));
		Collection<IProject> project10List = new ArrayList<IProject>();
		project10List.add(refWks.getReferenceProject(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A));

		ModelLoadManager.INSTANCE.reloadProjects(project20List, false, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		ModelLoadManager.INSTANCE.reloadProjects(project20List, false, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		for (IProject project : project20List) {
			editingDomain20ResourceCount = editingDomain20ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird20MMDescriptor.INSTANCE).size();

		}
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadProjects(project20List, false, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		for (IProject project : project20List) {
			editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
					+ refWks.getReferenceFiles(project.getName(), UML2MMDescriptor.INSTANCE).size();

		}
		// Referenced files in HB_PROJECT_20_D were also loaded:UML2_FILE_NAME_20D_1,UML2_FILE_NAME_20D_2
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount + uml2ReferencedFiles_Of_HbProject20_E;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadProjects(project20List, true, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		editingDomain20ResourceCount = editingDomain20ResourceCount
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainDoesNotContainResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadProjects(project20List, true, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		// Removed duplicate files in HB_PROJECT_20_D
		editingDomainUml2ResourceCount = editingDomainUml2ResourceCount - uml2ReferencedFiles_Of_HbProject20_E
				+ refWks.getReferenceFiles(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE).size();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		ModelLoadManager.INSTANCE.reloadProjects(project10List, false, UML2MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		for (IProject project : project10List) {
			editingDomainUml2ResourceCount = editingDomainUml2ResourceCount
					+ refWks.getReferenceFiles(project.getName(), UML2MMDescriptor.INSTANCE).size();

		}
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		ModelLoadManager.INSTANCE.reloadProjects(project10List, false, Hummingbird20MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		ModelLoadManager.INSTANCE.reloadProjects(project10List, false, Hummingbird10MMDescriptor.INSTANCE, false, null);
		waitForModelLoading();

		for (IProject project : project20List) {
			editingDomain10ResourceCount = editingDomain10ResourceCount
					+ refWks.getReferenceFiles(project.getName(), Hummingbird10MMDescriptor.INSTANCE).size();

		}
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain10, editingDomain10ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, editingDomain20ResourceCount);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomainUml2, editingDomainUml2ResourceCount);

		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2);
		assertEditingDomainContainsResource(refWks.editingDomain10, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomain20, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2);
		assertEditingDomainContainsResource(refWks.editingDomainUml2, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3);

		project20List.clear();
		project10List.clear();
	}

}
