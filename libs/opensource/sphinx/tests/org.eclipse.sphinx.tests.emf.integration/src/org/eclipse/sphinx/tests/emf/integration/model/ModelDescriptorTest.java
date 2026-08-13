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
 *     itemis - [410825] Make sure that EcorePlatformUtil#getResourcesInModel(contextResource, includeReferencedModels) method return resources of the context resource in the same resource set
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.UMLPackage;

@SuppressWarnings("nls")
public class ModelDescriptorTest extends DefaultIntegrationTestCase {
	List<String> hbProject10AResources10;
	int resources10InProject10A;

	List<String> hbProject10DResources10;
	int resources10InProject10D;

	List<String> hbProject10EResources10;
	int resources10InProject10E;

	List<String> hbProject20AResources20;
	int resources20InProject20A;

	List<String> hbProject20DResources20;
	int resources20InProject20D;

	List<String> hbProject20EResources20;
	int resources20InProject20E;

	List<String> hbProject20DResourcesUml2;
	int resourcesUml2InProject20D;

	List<String> hbProject20EResourcesUml2;
	int resourcesUml2InProject20E;

	public ModelDescriptorTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		hbProject10AResources10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				Hummingbird10MMDescriptor.INSTANCE);
		resources10InProject10A = hbProject10AResources10.size();

		hbProject10DResources10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D,
				Hummingbird10MMDescriptor.INSTANCE);
		resources10InProject10D = hbProject10DResources10.size();

		hbProject10EResources10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
				Hummingbird10MMDescriptor.INSTANCE);
		resources10InProject10E = hbProject10EResources10.size();

		hbProject20AResources20 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				Hummingbird20MMDescriptor.INSTANCE);
		resources20InProject20A = hbProject20AResources20.size();

		hbProject20DResources20 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				Hummingbird20MMDescriptor.INSTANCE);
		resources20InProject20D = hbProject20DResources20.size();

		hbProject20EResources20 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				Hummingbird20MMDescriptor.INSTANCE);
		resources20InProject20E = hbProject20EResources20.size();

		hbProject20DResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE);
		resourcesUml2InProject20D = hbProject20DResourcesUml2.size();

		hbProject20EResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE);
		resourcesUml2InProject20E = hbProject20EResourcesUml2.size();
	}

	/**
	 * Test method for {@link ModelDescriptor#getLoadedResources(boolean)}
	 */
	public void testGetLoadedResources() {

		// Models of Hummingbird10Resource
		IModelDescriptor hbModel10A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor hbModel10D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor hbModel10E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertNotNull(hbModel10A);
		assertNotNull(hbModel10D);
		assertNotNull(hbModel10E);

		Collection<Resource> loadedResources = hbModel10A.getLoadedResources(true);

		assertEquals(resources10InProject10A, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject10AResources10.contains(res.getURI().lastSegment()));
		}

		loadedResources = hbModel10D.getLoadedResources(true);
		assertEquals(resources10InProject10D, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject10DResources10.contains(res.getURI().lastSegment()));
		}

		loadedResources = hbModel10D.getLoadedResources(false);
		assertEquals(resources10InProject10D, loadedResources.size());

		loadedResources = hbModel10E.getLoadedResources(true);
		assertEquals(resources10InProject10D + resources10InProject10E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject10DResources10.contains(res.getURI().lastSegment()) || hbProject10EResources10.contains(res.getURI().lastSegment()));
		}

		loadedResources = hbModel10E.getLoadedResources(false);
		assertEquals(resources10InProject10E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject10EResources10.contains(res.getURI().lastSegment()));
		}
		// Unload file
		// File in hbProject10A
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1),
				false, new NullProgressMonitor());
		loadedResources = hbModel10A.getLoadedResources(true);

		assertEquals(--resources10InProject10A, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1.equals(res.getURI().lastSegment()));

		}
		loadedResources = hbModel10D.getLoadedResources(true);
		assertEquals(resources10InProject10D, loadedResources.size());

		loadedResources = hbModel10E.getLoadedResources(true);
		assertEquals(resources10InProject10E + resources10InProject10D, loadedResources.size());

		// File in hbProject10D
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1),
				false, new NullProgressMonitor());
		loadedResources = hbModel10A.getLoadedResources(true);
		assertEquals(resources10InProject10A, loadedResources.size());

		loadedResources = hbModel10D.getLoadedResources(true);
		assertEquals(--resources10InProject10D, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1.equals(res.getURI().lastSegment()));

		}
		loadedResources = hbModel10D.getLoadedResources(false);
		assertEquals(resources10InProject10D, loadedResources.size());

		loadedResources = hbModel10E.getLoadedResources(true);
		assertEquals(resources10InProject10D + resources10InProject10E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1.equals(res.getURI().lastSegment()));

		}
		loadedResources = hbModel10E.getLoadedResources(false);
		assertEquals(resources10InProject10E, loadedResources.size());

		// File in hbProject10E

		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1),
				false, new NullProgressMonitor());
		loadedResources = hbModel10A.getLoadedResources(true);
		assertEquals(resources10InProject10A, loadedResources.size());

		loadedResources = hbModel10D.getLoadedResources(true);
		assertEquals(resources10InProject10D, loadedResources.size());

		loadedResources = hbModel10D.getLoadedResources(false);
		assertEquals(resources10InProject10D, loadedResources.size());

		loadedResources = hbModel10E.getLoadedResources(true);
		assertEquals(resources10InProject10D + --resources10InProject10E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1.equals(res.getURI().lastSegment()));

		}

		loadedResources = hbModel10E.getLoadedResources(false);
		assertEquals(resources10InProject10E, loadedResources.size());
		// ================================================
		// Models of Hummingbird20Resource
		IModelDescriptor hbModel20A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor hbModel20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor hbModel20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertNotNull(hbModel20A);
		assertNotNull(hbModel20D);
		assertNotNull(hbModel20E);

		loadedResources = hbModel20A.getLoadedResources(true);
		assertEquals(resources20InProject20A, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject20AResources20.contains(res.getURI().lastSegment()));
		}

		loadedResources = hbModel20D.getLoadedResources(true);
		assertEquals(resources20InProject20D, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject20DResources20.contains(res.getURI().lastSegment()));
		}
		loadedResources = hbModel20E.getLoadedResources(true);
		assertEquals(resources20InProject20D + resources20InProject20E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject20DResources20.contains(res.getURI().lastSegment()) || hbProject20EResources20.contains(res.getURI().lastSegment()));
		}

		loadedResources = hbModel20E.getLoadedResources(false);
		assertEquals(resources20InProject20E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject20EResources20.contains(res.getURI().lastSegment()));
		}
		// Unload file
		// File in hbProject20A
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1),
				false, new NullProgressMonitor());
		loadedResources = hbModel20A.getLoadedResources(true);

		assertEquals(--resources20InProject20A, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1.equals(res.getURI().lastSegment()));

		}
		loadedResources = hbModel20D.getLoadedResources(true);
		assertEquals(resources20InProject20D, loadedResources.size());

		loadedResources = hbModel20D.getLoadedResources(false);
		assertEquals(resources20InProject20D, loadedResources.size());

		loadedResources = hbModel20E.getLoadedResources(true);
		assertEquals(resources20InProject20E + resources20InProject20D, loadedResources.size());

		loadedResources = hbModel20E.getLoadedResources(false);
		assertEquals(resources20InProject20E, loadedResources.size());

		// File in hbProject20D
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1),
				false, new NullProgressMonitor());
		loadedResources = hbModel20A.getLoadedResources(true);
		assertEquals(resources20InProject20A, loadedResources.size());

		loadedResources = hbModel20D.getLoadedResources(true);
		assertEquals(--resources20InProject20D, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1.equals(res.getURI().lastSegment()));

		}
		loadedResources = hbModel20D.getLoadedResources(false);
		assertEquals(resources20InProject20D, loadedResources.size());

		loadedResources = hbModel20E.getLoadedResources(true);
		assertEquals(resources20InProject20D + resources20InProject20E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1.equals(res.getURI().lastSegment()));

		}

		loadedResources = hbModel20E.getLoadedResources(false);
		assertEquals(resources20InProject20E, loadedResources.size());
		// File in hbProject20E

		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1),
				false, new NullProgressMonitor());
		loadedResources = hbModel20A.getLoadedResources(true);
		assertEquals(resources20InProject20A, loadedResources.size());

		loadedResources = hbModel20D.getLoadedResources(true);
		assertEquals(resources20InProject20D, loadedResources.size());

		loadedResources = hbModel20D.getLoadedResources(false);
		assertEquals(resources20InProject20D, loadedResources.size());

		loadedResources = hbModel20E.getLoadedResources(true);
		assertEquals(resources20InProject20D + --resources20InProject20E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1.equals(res.getURI().lastSegment()));

		}

		loadedResources = hbModel20E.getLoadedResources(false);
		assertEquals(resources20InProject20E, loadedResources.size());
		// ======================================================================
		// Models of Hummingbird20Resource
		IModelDescriptor uml2Model20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor uml2Model20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertNotNull(uml2Model20D);
		assertNotNull(uml2Model20E);

		loadedResources = uml2Model20D.getLoadedResources(true);
		assertEquals(resourcesUml2InProject20D, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(hbProject20DResourcesUml2.contains(res.getURI().lastSegment()));
		}

		loadedResources = uml2Model20D.getLoadedResources(false);
		assertEquals(resourcesUml2InProject20D, loadedResources.size());

		loadedResources = uml2Model20E.getLoadedResources(true);
		assertEquals(resourcesUml2InProject20D + resourcesUml2InProject20E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertTrue(
					hbProject20DResourcesUml2.contains(res.getURI().lastSegment()) || hbProject20EResourcesUml2.contains(res.getURI().lastSegment()));
		}

		loadedResources = uml2Model20E.getLoadedResources(false);
		assertEquals(resourcesUml2InProject20E, loadedResources.size());
		// Unload file
		// File in hbProject20D
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1),
				false, new NullProgressMonitor());

		loadedResources = uml2Model20D.getLoadedResources(true);
		assertEquals(--resourcesUml2InProject20D, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1.equals(res.getURI().lastSegment()));

		}
		loadedResources = uml2Model20D.getLoadedResources(false);
		assertEquals(resourcesUml2InProject20D, loadedResources.size());

		loadedResources = uml2Model20E.getLoadedResources(true);
		assertEquals(resourcesUml2InProject20D + resourcesUml2InProject20E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1.equals(res.getURI().lastSegment()));

		}
		loadedResources = uml2Model20E.getLoadedResources(false);
		assertEquals(resourcesUml2InProject20E, loadedResources.size());
		// File in hbProject20E
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1),
				false, new NullProgressMonitor());

		loadedResources = uml2Model20D.getLoadedResources(true);
		assertEquals(resourcesUml2InProject20D, loadedResources.size());

		loadedResources = uml2Model20D.getLoadedResources(false);
		assertEquals(resourcesUml2InProject20D, loadedResources.size());

		loadedResources = uml2Model20E.getLoadedResources(true);
		assertEquals(resourcesUml2InProject20D + --resourcesUml2InProject20E, loadedResources.size());
		for (Resource res : loadedResources) {
			assertFalse(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1.equals(res.getURI().lastSegment()));

		}

		loadedResources = uml2Model20E.getLoadedResources(false);
		assertEquals(resourcesUml2InProject20E, loadedResources.size());
	}

	/**
	 * Test method for {@link ModelDescriptor#getPersistedFiles(boolean)}
	 */
	public void testGetPersistedFiles() {

		IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		Resource resource = EcorePlatformUtil.getResource(referenceFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertNotNull(modelRoot);
		// Models of Hummingbird10Resource
		IModelDescriptor hbModel10A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor hbModel10D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor hbModel10E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertNotNull(hbModel10A);
		assertNotNull(hbModel10D);
		assertNotNull(hbModel10E);

		Collection<IFile> persistedFiles = hbModel10A.getPersistedFiles(true);
		assertEquals(resources10InProject10A, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10AResources10.contains(file.getName()));
		}

		persistedFiles = hbModel10D.getPersistedFiles(true);
		assertEquals(resources10InProject10D, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10DResources10.contains(file.getName()));
		}

		persistedFiles = hbModel10D.getPersistedFiles(false);
		assertEquals(resources10InProject10D, persistedFiles.size());

		persistedFiles = hbModel10E.getPersistedFiles(true);
		assertEquals(resources10InProject10D + resources10InProject10E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10DResources10.contains(file.getName()) || hbProject10EResources10.contains(file.getName()));
		}

		persistedFiles = hbModel10E.getPersistedFiles(false);
		assertEquals(resources10InProject10E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10EResources10.contains(file.getName()));
		}
		// Unload file
		// File in hbProject10A
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1),
				false, new NullProgressMonitor());
		persistedFiles = hbModel10A.getPersistedFiles(true);

		assertEquals(resources10InProject10A, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10AResources10.contains(file.getName()));

		}
		persistedFiles = hbModel10D.getPersistedFiles(true);
		assertEquals(resources10InProject10D, persistedFiles.size());

		persistedFiles = hbModel10E.getPersistedFiles(true);
		assertEquals(resources10InProject10E + resources10InProject10D, persistedFiles.size());

		// File in hbProject10D
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1),
				false, new NullProgressMonitor());

		persistedFiles = hbModel10A.getPersistedFiles(true);
		assertEquals(resources10InProject10A, persistedFiles.size());

		persistedFiles = hbModel10D.getPersistedFiles(true);
		assertEquals(resources10InProject10D, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10DResources10.contains(file.getName()));

		}
		persistedFiles = hbModel10D.getPersistedFiles(false);
		assertEquals(resources10InProject10D, persistedFiles.size());

		persistedFiles = hbModel10E.getPersistedFiles(true);
		assertEquals(resources10InProject10D + resources10InProject10E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10DResources10.contains(file.getName()) || hbProject10EResources10.contains(file.getName()));

		}
		persistedFiles = hbModel10E.getPersistedFiles(false);
		assertEquals(resources10InProject10E, persistedFiles.size());

		// File in hbProject10E

		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1),
				false, new NullProgressMonitor());
		persistedFiles = hbModel10A.getPersistedFiles(true);
		assertEquals(resources10InProject10A, persistedFiles.size());

		persistedFiles = hbModel10D.getPersistedFiles(true);
		assertEquals(resources10InProject10D, persistedFiles.size());

		persistedFiles = hbModel10D.getPersistedFiles(false);
		assertEquals(resources10InProject10D, persistedFiles.size());

		persistedFiles = hbModel10E.getPersistedFiles(true);
		assertEquals(resources10InProject10D + resources10InProject10E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject10DResources10.contains(file.getName()) || hbProject10EResources10.contains(file.getName()));

		}

		persistedFiles = hbModel10E.getPersistedFiles(false);
		assertEquals(resources10InProject10E, persistedFiles.size());
		// ================================================
		// Models of Hummingbird20Resource
		IModelDescriptor hbModel20A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor hbModel20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor hbModel20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertNotNull(hbModel20A);
		assertNotNull(hbModel20D);
		assertNotNull(hbModel20E);

		persistedFiles = hbModel20A.getPersistedFiles(true);
		assertEquals(resources20InProject20A, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20AResources20.contains(file.getName()));
		}

		persistedFiles = hbModel20D.getPersistedFiles(true);
		assertEquals(resources20InProject20D, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResources20.contains(file.getName()));
		}
		persistedFiles = hbModel20E.getPersistedFiles(true);
		assertEquals(resources20InProject20D + resources20InProject20E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResources20.contains(file.getName()) || hbProject20EResources20.contains(file.getName()));
		}

		persistedFiles = hbModel20E.getPersistedFiles(false);
		assertEquals(resources20InProject20E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20EResources20.contains(file.getName()));
		}
		// Unload file
		// File in hbProject20A
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1),
				false, new NullProgressMonitor());
		persistedFiles = hbModel20A.getPersistedFiles(true);

		assertEquals(resources20InProject20A, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20AResources20.contains(file.getName()));

		}
		persistedFiles = hbModel20D.getPersistedFiles(true);
		assertEquals(resources20InProject20D, persistedFiles.size());

		persistedFiles = hbModel20D.getPersistedFiles(false);
		assertEquals(resources20InProject20D, persistedFiles.size());

		persistedFiles = hbModel20E.getPersistedFiles(true);
		assertEquals(resources20InProject20E + resources20InProject20D, persistedFiles.size());

		persistedFiles = hbModel20E.getPersistedFiles(false);
		assertEquals(resources20InProject20E, persistedFiles.size());

		// File in hbProject20D
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1),
				false, new NullProgressMonitor());
		persistedFiles = hbModel20A.getPersistedFiles(true);
		assertEquals(resources20InProject20A, persistedFiles.size());

		persistedFiles = hbModel20D.getPersistedFiles(true);
		assertEquals(resources20InProject20D, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResources20.contains(file.getName()));

		}
		persistedFiles = hbModel20D.getPersistedFiles(false);
		assertEquals(resources20InProject20D, persistedFiles.size());

		persistedFiles = hbModel20E.getPersistedFiles(true);
		assertEquals(resources20InProject20D + resources20InProject20E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20EResources20.contains(file.getName()) || hbProject20DResources20.contains(file.getName()));

		}

		persistedFiles = hbModel20E.getPersistedFiles(false);
		assertEquals(resources20InProject20E, persistedFiles.size());
		// File in hbProject20E

		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1),
				false, new NullProgressMonitor());
		persistedFiles = hbModel20A.getPersistedFiles(true);
		assertEquals(resources20InProject20A, persistedFiles.size());

		persistedFiles = hbModel20D.getPersistedFiles(true);
		assertEquals(resources20InProject20D, persistedFiles.size());

		persistedFiles = hbModel20D.getPersistedFiles(false);
		assertEquals(resources20InProject20D, persistedFiles.size());

		persistedFiles = hbModel20E.getPersistedFiles(true);
		assertEquals(resources20InProject20D + resources20InProject20E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20EResources20.contains(file.getName()) || hbProject20DResources20.contains(file.getName()));

		}

		persistedFiles = hbModel20E.getPersistedFiles(false);
		assertEquals(resources20InProject20E, persistedFiles.size());
		// ======================================================================
		// Models of Hummingbird20Resource
		IModelDescriptor uml2Model20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor uml2Model20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertNotNull(uml2Model20D);
		assertNotNull(uml2Model20E);

		persistedFiles = uml2Model20D.getPersistedFiles(true);
		assertEquals(resourcesUml2InProject20D, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResourcesUml2.contains(file.getName()));
		}

		persistedFiles = uml2Model20D.getPersistedFiles(false);
		assertEquals(resourcesUml2InProject20D, persistedFiles.size());

		persistedFiles = uml2Model20E.getPersistedFiles(true);
		assertEquals(resourcesUml2InProject20D + resourcesUml2InProject20E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResourcesUml2.contains(file.getName()) || hbProject20EResourcesUml2.contains(file.getName()));
		}

		persistedFiles = uml2Model20E.getPersistedFiles(false);
		assertEquals(resourcesUml2InProject20E, persistedFiles.size());
		// Unload file
		// File in hbProject20D
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1),
				false, new NullProgressMonitor());

		persistedFiles = uml2Model20D.getPersistedFiles(true);
		assertEquals(resourcesUml2InProject20D, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResourcesUml2.contains(file.getName()));

		}
		persistedFiles = uml2Model20D.getPersistedFiles(false);
		assertEquals(resourcesUml2InProject20D, persistedFiles.size());

		persistedFiles = uml2Model20E.getPersistedFiles(true);
		assertEquals(resourcesUml2InProject20D + resourcesUml2InProject20E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResourcesUml2.contains(file.getName()) || hbProject20EResourcesUml2.contains(file.getName()));

		}
		persistedFiles = uml2Model20E.getPersistedFiles(false);
		assertEquals(resourcesUml2InProject20E, persistedFiles.size());
		// File in hbProject20E
		ModelLoadManager.INSTANCE.unloadFile(
				refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1),
				false, new NullProgressMonitor());

		persistedFiles = uml2Model20D.getPersistedFiles(true);
		assertEquals(resourcesUml2InProject20D, persistedFiles.size());

		persistedFiles = uml2Model20D.getPersistedFiles(false);
		assertEquals(resourcesUml2InProject20D, persistedFiles.size());

		persistedFiles = uml2Model20E.getPersistedFiles(true);
		assertEquals(resourcesUml2InProject20D + resourcesUml2InProject20E, persistedFiles.size());
		for (IFile file : persistedFiles) {
			assertTrue(hbProject20DResourcesUml2.contains(file.getName()) || hbProject20EResourcesUml2.contains(file.getName()));

		}
		persistedFiles = uml2Model20E.getPersistedFiles(false);
		assertEquals(resourcesUml2InProject20E, persistedFiles.size());
	}

	/**
	 * Test method for {@link ModelDescriptor#getReferencedRoots()}
	 */
	public void testGetReferencedRoots() {
		IModelDescriptor hbModel10A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor hbModel10D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor hbModel10E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertNotNull(hbModel10A);
		assertNotNull(hbModel10D);
		assertNotNull(hbModel10E);

		Collection<IResource> referencedRoots = hbModel10A.getReferencedRoots();
		assertEquals(0, referencedRoots.size());
		referencedRoots = hbModel10D.getReferencedRoots();
		assertEquals(0, referencedRoots.size());
		referencedRoots = hbModel10E.getReferencedRoots();
		assertEquals(1, referencedRoots.size());

		IModelDescriptor hbModel20A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor hbModel20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor hbModel20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertNotNull(hbModel20A);
		assertNotNull(hbModel20D);
		assertNotNull(hbModel20E);

		referencedRoots = hbModel20A.getReferencedRoots();
		assertEquals(0, referencedRoots.size());

		referencedRoots = hbModel20D.getReferencedRoots();
		assertEquals(2, referencedRoots.size());

		referencedRoots = hbModel20E.getReferencedRoots();
		assertEquals(3, referencedRoots.size());

		IModelDescriptor uml2Model20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor uml2Model20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertNotNull(uml2Model20D);
		assertNotNull(uml2Model20E);

		referencedRoots = uml2Model20D.getReferencedRoots();
		assertEquals(2, referencedRoots.size());

		referencedRoots = uml2Model20E.getReferencedRoots();
		assertEquals(3, referencedRoots.size());
	}

	/**
	 * Test method for {@link ModelDescriptor#getReferencingRoots()}
	 */
	public void testGetReferencingRoots() {
		IModelDescriptor hbModel10A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor hbModel10D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor hbModel10E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertNotNull(hbModel10A);
		assertNotNull(hbModel10D);
		assertNotNull(hbModel10E);

		Collection<IResource> referencedRoots = hbModel10A.getReferencingRoots();
		assertEquals(0, referencedRoots.size());
		referencedRoots = hbModel10D.getReferencingRoots();
		assertEquals(3, referencedRoots.size());
		referencedRoots = hbModel10E.getReferencingRoots();
		assertEquals(2, referencedRoots.size());

		IModelDescriptor hbModel20A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor hbModel20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor hbModel20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertNotNull(hbModel20A);
		assertNotNull(hbModel20D);
		assertNotNull(hbModel20E);

		referencedRoots = hbModel20A.getReferencingRoots();
		assertEquals(0, referencedRoots.size());

		referencedRoots = hbModel20D.getReferencingRoots();
		assertEquals(1, referencedRoots.size());

		referencedRoots = hbModel20E.getReferencingRoots();
		assertEquals(0, referencedRoots.size());

		IModelDescriptor uml2Model20D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor uml2Model20E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertNotNull(uml2Model20D);
		assertNotNull(uml2Model20E);

		referencedRoots = uml2Model20D.getReferencingRoots();
		assertEquals(1, referencedRoots.size());

		referencedRoots = uml2Model20E.getReferencingRoots();
		assertEquals(0, referencedRoots.size());
	}

	/**
	 * Test method for {@link ModelDescriptor#belongsTo(IFile, boolean)}
	 *
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testBelongsTo_IFile() throws OperationCanceledException, InterruptedException {

		assertNotNull(refWks.editingDomain10);
		assertNotNull(refWks.editingDomain20);
		assertNotNull(refWks.editingDomainUml2);

		// HBProjectModelScope
		IModelDescriptor modelDescriptor10_A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor modelDescriptor10_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor modelDescriptor10_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		IModelDescriptor modelDescriptor20_A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor modelDescriptor20_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor modelDescriptor20_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		// UML2MMDescriptor.INSTANCE);
		IModelDescriptor modelDescriptorUml2_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor modelDescriptorUml2_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		assertNotNull(modelDescriptor10_A);
		assertNotNull(modelDescriptor10_D);
		assertNotNull(modelDescriptor10_E);

		assertNotNull(modelDescriptor20_A);
		assertNotNull(modelDescriptor20_D);
		assertNotNull(modelDescriptor20_E);

		assertNotNull(modelDescriptorUml2_E);
		assertNotNull(modelDescriptorUml2_D);
		{
			IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
			Resource resource = EcorePlatformUtil.getResource(referenceFile);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
		}
		// // --------------------------------------------------------------
		// // Context: ModelDescriptor10
		//
		{
			//
			// // - Given file has Persisted: TRUE, Loaded: TRUE
			for (String file10AName : hbProject10AResources10) {
				IFile file10 = refWks.hbProject10_A.getFile(file10AName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				assertTrue(modelDescriptor10_A.belongsTo(file10, true));
			}
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				assertTrue(modelDescriptor10_D.belongsTo(file10, true));
				assertTrue(modelDescriptor10_E.belongsTo(file10, true));
				assertFalse(modelDescriptor10_E.belongsTo(file10, false));
			}
			for (String file10EName : hbProject10EResources10) {
				IFile file10 = refWks.hbProject10_E.getFile(file10EName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				assertFalse(modelDescriptor10_D.belongsTo(file10, true));
				assertTrue(modelDescriptor10_E.belongsTo(file10, true));
				assertTrue(modelDescriptor10_E.belongsTo(file10, false));
			}
			{
				IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
						DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
				Resource resource = EcorePlatformUtil.getResource(referenceFile);
				assertNotNull(resource);
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				assertNotNull(modelRoot);
			}
			// // - Given file has Persisted: FALSE, Loaded: TRUE
			// // - Given file is not part of the modelDescriptor

			for (String file10AName : hbProject10AResources10) {
				IFile file10 = refWks.hbProject10_A.getFile(file10AName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				assertFalse(modelDescriptor10_D.belongsTo(file10, true));
				assertFalse(modelDescriptor20_A.belongsTo(file10, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(file10, true));
			}
			// // - Given file has Loaded: FALSE
			{
				IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
						DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
				Resource resource = EcorePlatformUtil.getResource(referenceFile);
				assertNotNull(resource);
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				assertNotNull(modelRoot);
			}
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				// Unload file
				ModelLoadManager.INSTANCE.unloadFile(file10, false, new NullProgressMonitor());
				waitForModelLoading();

				assertTrue(file10.isAccessible());
				assertFalse(EcorePlatformUtil.isFileLoaded(file10));

				assertFalse(EcorePlatformUtil.isFileLoaded(file10));
				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertTrue(modelDescriptor10_D.belongsTo(file10, true));
				assertTrue(modelDescriptor10_D.belongsTo(file10, false));
				assertTrue(modelDescriptor10_E.belongsTo(file10, true));
				assertFalse(modelDescriptor10_E.belongsTo(file10, false));

				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor10_A.belongsTo(file10, true));
				assertFalse(modelDescriptor20_A.belongsTo(file10, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(file10, true));
			}
			{
				IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
						DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
				Resource resource = EcorePlatformUtil.getResource(referenceFile);
				assertNotNull(resource);
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				assertNotNull(modelRoot);
			}
			//
			// // - Given file in memory only
			String onlyInMemoryResourceName = "newResource.hummingbird";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);
			//
			// We retrieve model root from file HB_FILE_NAME_10_10A_1
			EObject modelRoot = createHumingbird10ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain10, onlyInMemoryResourceIPath, Hummingbird10Package.eCONTENT_TYPE, modelRoot,
					false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource10 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource10);
			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertFalse(modelDescriptor10_A.belongsTo(fileInMemory, true));
			assertTrue(modelDescriptor10_D.belongsTo(fileInMemory, true));
			assertTrue(modelDescriptor10_E.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptor10_E.belongsTo(fileInMemory, false));

			assertFalse(modelDescriptor20_A.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptorUml2_E.belongsTo(fileInMemory, true));

			// Unload fileInMemory
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptor10_D.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptor10_E.belongsTo(fileInMemory, true));
		}
		// ----------------------------------------------------------
		// Context object is HB 20Files
		{

			// - Given file has Persisted: TRUE, Loaded: TRUE

			for (String file20AName : hbProject20AResources20) {
				IFile file20 = refWks.hbProject20_A.getFile(file20AName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertTrue(modelDescriptor20_A.belongsTo(file20, true));

			}
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertTrue(modelDescriptor20_D.belongsTo(file20, true));
				assertTrue(modelDescriptor20_D.belongsTo(file20, false));
				assertTrue(modelDescriptor20_E.belongsTo(file20, true));
				assertFalse(modelDescriptor20_E.belongsTo(file20, false));

			}
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				assertFalse(modelDescriptor20_D.belongsTo(file20, true));
				assertFalse(modelDescriptor20_D.belongsTo(file20, false));

				assertTrue(modelDescriptor20_E.belongsTo(file20, true));
				assertTrue(modelDescriptor20_E.belongsTo(file20, false));

			}
			// - Given file has Persisted: FALSE, Loaded: TRUE
			// - Given file is existed

			for (String file20AName : hbProject20AResources20) {
				IFile file20 = refWks.hbProject20_A.getFile(file20AName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertFalse(modelDescriptor20_D.belongsTo(file20, true));
				assertFalse(modelDescriptor20_E.belongsTo(file20, true));

				assertFalse(modelDescriptor10_A.belongsTo(file20, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(file20, true));

			}
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertFalse(modelDescriptor20_A.belongsTo(file20, true));

				assertFalse(modelDescriptor10_A.belongsTo(file20, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(file20, true));
			}
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				assertFalse(modelDescriptor20_A.belongsTo(file20, true));
				assertFalse(modelDescriptor20_D.belongsTo(file20, true));

				assertFalse(modelDescriptor10_A.belongsTo(file20, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(file20, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(file20, true));

			}

			// - Given file has Loaded: FALSE
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);
				// Unload file
				ModelLoadManager.INSTANCE.unloadFile(file20, false, new NullProgressMonitor());
				waitForModelLoading();

				assertTrue(file20.isAccessible());
				assertFalse(EcorePlatformUtil.isFileLoaded(file20));

				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertTrue(modelDescriptor20_D.belongsTo(file20, true));
				assertTrue(modelDescriptor20_E.belongsTo(file20, true));
				assertFalse(modelDescriptor20_E.belongsTo(file20, false));

				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor20_A.belongsTo(file20, true));
				assertFalse(modelDescriptor10_A.belongsTo(file20, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(file20, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(file20, true));

			}
			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.instancemodel";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file HB_FILE_NAME_20_20A_1
			EObject modelRoot = createHummingbird20InstanceModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource20);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertFalse(modelDescriptor20_A.belongsTo(fileInMemory, true));
			assertTrue(modelDescriptor20_D.belongsTo(fileInMemory, true));
			assertTrue(modelDescriptor20_E.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptor20_E.belongsTo(fileInMemory, false));

			assertFalse(modelDescriptor10_A.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptorUml2_D.belongsTo(fileInMemory, true));

			// Unload fileInMemoryOnly
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptor20_D.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptor20_E.belongsTo(fileInMemory, true));
		}
		{
			IFile referenceFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
					DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
			Resource resource = EcorePlatformUtil.getResource(referenceFile);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
		}
		// --------------------------------------------------------
		// UML2 files
		{
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertTrue(modelDescriptorUml2_E.belongsTo(fileUml2, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(fileUml2, false));
				assertTrue(modelDescriptorUml2_D.belongsTo(fileUml2, true));
				assertTrue(modelDescriptorUml2_D.belongsTo(fileUml2, false));

			}
			for (String fileUml2_20EName : hbProject20EResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_E.getFile(fileUml2_20EName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertFalse(modelDescriptorUml2_D.belongsTo(fileUml2, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(fileUml2, false));
				assertTrue(modelDescriptorUml2_E.belongsTo(fileUml2, true));
				assertTrue(modelDescriptorUml2_E.belongsTo(fileUml2, false));

			}
			//
			// - Given file has Persisted: FALSE, Loaded: TRUE
			// - Given file is existed
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertFalse(modelDescriptor10_A.belongsTo(fileUml2, true));
				assertFalse(modelDescriptor20_D.belongsTo(fileUml2, true));
				assertFalse(modelDescriptor20_E.belongsTo(fileUml2, true));
			}

			// - Given file has Loaded: FALSE
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				// unload file
				ModelLoadManager.INSTANCE.unloadFile(fileUml2, false, new NullProgressMonitor());
				waitForModelLoading();

				assertTrue(fileUml2.isAccessible());
				assertFalse(EcorePlatformUtil.isFileLoaded(fileUml2));
				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertTrue(modelDescriptorUml2_D.belongsTo(fileUml2, true));
				assertTrue(modelDescriptorUml2_E.belongsTo(fileUml2, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(fileUml2, false));
				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor10_A.belongsTo(fileUml2, true));
				assertFalse(modelDescriptor20_D.belongsTo(fileUml2, true));
				assertFalse(modelDescriptor20_E.belongsTo(fileUml2, true));

			}
			// - Given file in memory only
			String onlyInMemoryResourceName = "NewResource.uml";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file UML2_FILE_NAME_20E_1
			EObject modelRoot = createUML2ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomainUml2, onlyInMemoryResourceIPath, UMLPackage.eCONTENT_TYPE, modelRoot, false,
					null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResourceUml2 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResourceUml2);
			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(modelDescriptorUml2_E.belongsTo(fileInMemory, true));
			assertTrue(modelDescriptorUml2_D.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptorUml2_E.belongsTo(fileInMemory, false));

			assertFalse(modelDescriptor10_A.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptor20_D.belongsTo(fileInMemory, true));

			// Unload fileInMemoryOnly
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptorUml2_E.belongsTo(fileInMemory, true));
			assertFalse(modelDescriptorUml2_D.belongsTo(fileInMemory, true));
		}
		// --------------------------------------------------------------
		// Input is null
		IFile nullFile = null;
		assertFalse(modelDescriptor10_A.belongsTo(nullFile, true));

	}

	/**
	 * Test method for {@link ModelDescriptor#belongsTo(Resource, boolean))}
	 *
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testBelongsTo_IResource() throws OperationCanceledException, InterruptedException {

		assertNotNull(refWks.editingDomain10);
		assertNotNull(refWks.editingDomain20);
		assertNotNull(refWks.editingDomainUml2);

		// HBProjectModelScope
		// HBProjectModelScope
		IModelDescriptor modelDescriptor10_A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor modelDescriptor10_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor modelDescriptor10_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		IModelDescriptor modelDescriptor20_A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor modelDescriptor20_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor modelDescriptor20_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		// UML2MMDescriptor.INSTANCE);
		IModelDescriptor modelDescriptorUml2_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor modelDescriptorUml2_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		assertNotNull(modelDescriptor10_A);
		assertNotNull(modelDescriptor10_D);
		assertNotNull(modelDescriptor10_E);

		assertNotNull(modelDescriptor20_A);
		assertNotNull(modelDescriptor20_D);
		assertNotNull(modelDescriptor20_E);

		assertNotNull(modelDescriptorUml2_E);
		assertNotNull(modelDescriptorUml2_D);

		// // --------------------------------------------------------------
		// // Context: ModelDescriptor10
		//
		{
			//
			// // - Given file has Persisted: TRUE, Loaded: TRUE
			for (String file10AName : hbProject10AResources10) {
				IFile file10 = refWks.hbProject10_A.getFile(file10AName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertTrue(modelDescriptor10_A.belongsTo(resource10, true));

			}
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				assertTrue(modelDescriptor10_D.belongsTo(EcorePlatformUtil.getResource(file10), true));
				assertTrue(modelDescriptor10_E.belongsTo(EcorePlatformUtil.getResource(file10), true));
				assertFalse(modelDescriptor10_E.belongsTo(EcorePlatformUtil.getResource(file10), false));
			}
			for (String file10EName : hbProject10EResources10) {
				IFile file10 = refWks.hbProject10_E.getFile(file10EName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				assertFalse(modelDescriptor10_D.belongsTo(EcorePlatformUtil.getResource(file10), true));
				assertTrue(modelDescriptor10_E.belongsTo(EcorePlatformUtil.getResource(file10), true));
				assertTrue(modelDescriptor10_E.belongsTo(EcorePlatformUtil.getResource(file10), false));
			}

			// // - Given file has Persisted: FALSE, Loaded: TRUE
			// // - Given file is not part of the modelDescriptor
			for (String file10AName : hbProject10AResources10) {
				IFile file10 = refWks.hbProject10_A.getFile(file10AName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				assertFalse(modelDescriptor10_D.belongsTo(EcorePlatformUtil.getResource(file10), true));
				assertFalse(modelDescriptor20_A.belongsTo(EcorePlatformUtil.getResource(file10), true));
				assertFalse(modelDescriptorUml2_E.belongsTo(EcorePlatformUtil.getResource(file10), true));
			}
			// // - Given file has Loaded: FALSE
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				// Unload file
				ModelLoadManager.INSTANCE.unloadFile(file10, false, new NullProgressMonitor());
				waitForModelLoading();
				assertFalse(EcorePlatformUtil.isFileLoaded(file10));
				assertNotNull(resource10);

				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertFalse(modelDescriptor10_D.belongsTo(resource10, true));
				assertFalse(modelDescriptor10_E.belongsTo(resource10, true));
				assertFalse(modelDescriptor10_E.belongsTo(resource10, false));
				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor10_A.belongsTo(resource10, true));
				assertFalse(modelDescriptor20_A.belongsTo(resource10, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(resource10, true));

			}
			//
			// // - Given file in memory only
			String onlyInMemoryResourceName = "newResource.hummingbird";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);
			//
			EObject modelRoot = createHumingbird10ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain10, onlyInMemoryResourceIPath, Hummingbird10Package.eCONTENT_TYPE, modelRoot,
					true, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource10 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource10);
			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(modelDescriptor10_D.belongsTo(onlyInMemoryResource10, true));
			assertTrue(modelDescriptor10_E.belongsTo(onlyInMemoryResource10, true));
			assertFalse(modelDescriptor10_E.belongsTo(onlyInMemoryResource10, false));
			assertFalse(modelDescriptor10_A.belongsTo(onlyInMemoryResource10, true));
			assertFalse(modelDescriptor20_A.belongsTo(onlyInMemoryResource10, true));
			assertFalse(modelDescriptorUml2_E.belongsTo(onlyInMemoryResource10, true));

			// Unload FileInMemory
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptor10_D.belongsTo(onlyInMemoryResource10, true));
			assertFalse(modelDescriptor10_E.belongsTo(onlyInMemoryResource10, true));
		}
		// ----------------------------------------------------------
		// Context object is HB 20Files
		{

			// - Given file has Persisted: TRUE, Loaded: TRUE

			for (String file20AName : hbProject20AResources20) {
				IFile file20 = refWks.hbProject20_A.getFile(file20AName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				assertTrue(modelDescriptor20_A.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertTrue(modelDescriptor20_A.belongsTo(EcorePlatformUtil.getResource(file20), false));

			}
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertTrue(modelDescriptor20_D.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertTrue(modelDescriptor20_D.belongsTo(EcorePlatformUtil.getResource(file20), false));
				assertTrue(modelDescriptor20_E.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptor20_E.belongsTo(EcorePlatformUtil.getResource(file20), false));

			}
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				assertFalse(modelDescriptor20_D.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptor20_D.belongsTo(EcorePlatformUtil.getResource(file20), false));

				assertTrue(modelDescriptor20_E.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertTrue(modelDescriptor20_E.belongsTo(EcorePlatformUtil.getResource(file20), false));

			}
			// - Given file has Persisted: FALSE, Loaded: TRUE
			// - Given file is existed

			for (String file20AName : hbProject20AResources20) {
				IFile file20 = refWks.hbProject20_A.getFile(file20AName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertFalse(modelDescriptor20_D.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptor20_E.belongsTo(EcorePlatformUtil.getResource(file20), true));

				assertFalse(modelDescriptor10_A.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptorUml2_D.belongsTo(EcorePlatformUtil.getResource(file20), true));

			}
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertFalse(modelDescriptor20_A.belongsTo(EcorePlatformUtil.getResource(file20), true));

				assertFalse(modelDescriptor10_A.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptorUml2_E.belongsTo(EcorePlatformUtil.getResource(file20), true));
			}
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				assertFalse(modelDescriptor20_A.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptor20_D.belongsTo(EcorePlatformUtil.getResource(file20), true));

				assertFalse(modelDescriptor10_A.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptorUml2_E.belongsTo(EcorePlatformUtil.getResource(file20), true));
				assertFalse(modelDescriptorUml2_D.belongsTo(EcorePlatformUtil.getResource(file20), true));

			}

			// - Given file has Loaded: FALSE
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);
				// Unload File
				Resource resource20 = EcorePlatformUtil.getResource(file20);
				ModelLoadManager.INSTANCE.unloadFile(file20, false, new NullProgressMonitor());
				waitForModelLoading();

				assertTrue(file20.isAccessible());
				assertFalse(EcorePlatformUtil.isFileLoaded(file20));
				assertNotNull(resource20);
				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertFalse(modelDescriptor20_D.belongsTo(resource20, true));
				assertFalse(modelDescriptor20_E.belongsTo(resource20, true));
				assertFalse(modelDescriptor20_E.belongsTo(resource20, false));
				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor20_A.belongsTo(resource20, true));
				assertFalse(modelDescriptor10_A.belongsTo(resource20, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(resource20, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(resource20, true));

			}
			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.instancemodel";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file HB_FILE_NAME_20_20A_1
			EObject modelRoot = createHummingbird20InstanceModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource20);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(modelDescriptor20_D.belongsTo(onlyInMemoryResource20, true));
			assertTrue(modelDescriptor20_E.belongsTo(onlyInMemoryResource20, true));
			assertFalse(modelDescriptor20_E.belongsTo(onlyInMemoryResource20, false));

			assertFalse(modelDescriptor10_A.belongsTo(onlyInMemoryResource20, true));
			assertFalse(modelDescriptor20_A.belongsTo(onlyInMemoryResource20, true));
			assertFalse(modelDescriptorUml2_D.belongsTo(onlyInMemoryResource20, true));

			// Unload fileInMemoryOnly
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptor20_D.belongsTo(onlyInMemoryResource20, true));
			assertFalse(modelDescriptor20_E.belongsTo(onlyInMemoryResource20, true));
		}

		// --------------------------------------------------------
		// UML2 files
		{
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertTrue(modelDescriptorUml2_E.belongsTo(EcorePlatformUtil.getResource(fileUml2), true));
				assertFalse(modelDescriptorUml2_E.belongsTo(EcorePlatformUtil.getResource(fileUml2), false));
				assertTrue(modelDescriptorUml2_D.belongsTo(EcorePlatformUtil.getResource(fileUml2), true));
				assertTrue(modelDescriptorUml2_D.belongsTo(EcorePlatformUtil.getResource(fileUml2), false));

			}
			for (String fileUml2_20EName : hbProject20EResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_E.getFile(fileUml2_20EName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertFalse(modelDescriptorUml2_D.belongsTo(EcorePlatformUtil.getResource(fileUml2), true));
				assertFalse(modelDescriptorUml2_D.belongsTo(EcorePlatformUtil.getResource(fileUml2), false));
				assertTrue(modelDescriptorUml2_E.belongsTo(EcorePlatformUtil.getResource(fileUml2), true));
				assertTrue(modelDescriptorUml2_E.belongsTo(EcorePlatformUtil.getResource(fileUml2), false));

			}
			//
			// - Given file has Persisted: FALSE, Loaded: TRUE
			// - Given file is existed
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertFalse(modelDescriptor10_A.belongsTo(EcorePlatformUtil.getResource(fileUml2), true));
				assertFalse(modelDescriptor20_D.belongsTo(EcorePlatformUtil.getResource(fileUml2), true));
				assertFalse(modelDescriptor20_E.belongsTo(EcorePlatformUtil.getResource(fileUml2), true));
			}

			// - Given file has Loaded: FALSE
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);

				// Unload File
				ModelLoadManager.INSTANCE.unloadFile(fileUml2, false, new NullProgressMonitor());
				waitForModelLoading();

				assertTrue(fileUml2.isAccessible());
				assertFalse(EcorePlatformUtil.isFileLoaded(fileUml2));
				assertNotNull(resourceUml2);
				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertFalse(modelDescriptorUml2_D.belongsTo(resourceUml2, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(resourceUml2, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(resourceUml2, false));
				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor10_A.belongsTo(resourceUml2, true));
				assertFalse(modelDescriptor20_D.belongsTo(resourceUml2, true));
				assertFalse(modelDescriptor20_E.belongsTo(resourceUml2, true));

			}
			// - Given file in memory only
			String onlyInMemoryResourceName = "NewResource.uml";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file UML2_FILE_NAME_20E_1
			EObject modelRoot = createUML2ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomainUml2, onlyInMemoryResourceIPath, UMLPackage.eCONTENT_TYPE, modelRoot, false,
					null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResourceUml2 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResourceUml2);
			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(modelDescriptorUml2_E.belongsTo(onlyInMemoryResourceUml2, true));
			assertTrue(modelDescriptorUml2_D.belongsTo(onlyInMemoryResourceUml2, true));
			assertFalse(modelDescriptorUml2_E.belongsTo(onlyInMemoryResourceUml2, false));

			assertFalse(modelDescriptor10_A.belongsTo(onlyInMemoryResourceUml2, true));
			assertFalse(modelDescriptor20_D.belongsTo(onlyInMemoryResourceUml2, true));

			// Unload FileInMemoryOnly
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptorUml2_E.belongsTo(onlyInMemoryResourceUml2, true));
			assertFalse(modelDescriptorUml2_D.belongsTo(onlyInMemoryResourceUml2, true));
		}
		{
			Resource nullResource = null;
			assertFalse(modelDescriptor10_A.belongsTo(nullResource, true));
			assertFalse(modelDescriptor10_D.belongsTo(nullResource, true));
			assertFalse(modelDescriptor10_E.belongsTo(nullResource, true));
			assertFalse(modelDescriptor20_A.belongsTo(nullResource, true));
			assertFalse(modelDescriptor20_D.belongsTo(nullResource, true));
			assertFalse(modelDescriptor20_E.belongsTo(nullResource, true));
			assertFalse(modelDescriptorUml2_D.belongsTo(nullResource, true));
			assertFalse(modelDescriptorUml2_E.belongsTo(nullResource, true));
		}

	}

	/**
	 * Test method for {@link ModelDescriptor#belongsTo(URI)}
	 *
	 * @throws OperationCanceledException
	 * @throws InterruptedException
	 */
	public void testBelongsTo_URI() throws OperationCanceledException, InterruptedException {

		assertNotNull(refWks.editingDomain10);
		assertNotNull(refWks.editingDomain20);
		assertNotNull(refWks.editingDomainUml2);

		// HBProjectModelScope
		IModelDescriptor modelDescriptor10_A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor modelDescriptor10_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor modelDescriptor10_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		IModelDescriptor modelDescriptor20_A = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor modelDescriptor20_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor modelDescriptor20_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		// UML2ModelScope
		IModelDescriptor modelDescriptorUml2_D = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor modelDescriptorUml2_E = ModelDescriptorRegistry.INSTANCE
				.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		assertNotNull(modelDescriptor10_A);
		assertNotNull(modelDescriptor10_D);
		assertNotNull(modelDescriptor10_E);

		assertNotNull(modelDescriptor20_A);
		assertNotNull(modelDescriptor20_D);
		assertNotNull(modelDescriptor20_E);

		assertNotNull(modelDescriptorUml2_E);
		assertNotNull(modelDescriptorUml2_D);

		// // --------------------------------------------------------------
		// // Context: ModelDescriptor10
		//
		{
			//
			// // - Given file has Persisted: TRUE, Loaded: TRUE
			for (String file10AName : hbProject10AResources10) {
				IFile file10 = refWks.hbProject10_A.getFile(file10AName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				URI uri10 = resource10.getURI();
				assertTrue(modelDescriptor10_A.belongsTo(uri10, true));

			}
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				URI uri10 = resource10.getURI();
				assertTrue(modelDescriptor10_D.belongsTo(uri10, true));
				assertTrue(modelDescriptor10_E.belongsTo(uri10, true));
				assertFalse(modelDescriptor10_E.belongsTo(uri10, false));
			}
			for (String file10EName : hbProject10EResources10) {
				IFile file10 = refWks.hbProject10_E.getFile(file10EName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				URI uri10 = resource10.getURI();

				assertFalse(modelDescriptor10_D.belongsTo(uri10, true));
				assertTrue(modelDescriptor10_E.belongsTo(uri10, true));
				assertTrue(modelDescriptor10_E.belongsTo(uri10, false));
			}

			// // - Given file has Persisted: FALSE, Loaded: TRUE
			// // - Given file is not part of the modelDescriptor
			for (String file10AName : hbProject10AResources10) {
				IFile file10 = refWks.hbProject10_A.getFile(file10AName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				URI uri10 = resource10.getURI();

				assertFalse(modelDescriptor10_D.belongsTo(uri10, true));
				assertFalse(modelDescriptor20_A.belongsTo(uri10, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(uri10, true));
			}
			// // - Given file has Loaded: FALSE
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				URI uri10 = resource10.getURI();
				// Unload file
				ModelLoadManager.INSTANCE.unloadFile(file10, false, new NullProgressMonitor());
				waitForModelLoading();

				assertFalse(EcorePlatformUtil.isFileLoaded(file10));
				assertNotNull(resource10);
				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertTrue(modelDescriptor10_D.belongsTo(uri10, true));
				assertTrue(modelDescriptor10_E.belongsTo(uri10, true));
				assertFalse(modelDescriptor10_E.belongsTo(uri10, false));
				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor10_A.belongsTo(uri10, true));
				assertFalse(modelDescriptor20_A.belongsTo(uri10, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(uri10, true));

			}
			//
			// // - Given file in memory only
			String onlyInMemoryResourceName = "newResource.hummingbird";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);
			//
			EObject modelRoot = createHumingbird10ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain10, onlyInMemoryResourceIPath, Hummingbird10Package.eCONTENT_TYPE, modelRoot,
					true, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource10 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource10);
			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));
			assertTrue(modelDescriptor10_D.belongsTo(onlyInMemoryResourceUri, true));
			assertTrue(modelDescriptor10_E.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptor10_E.belongsTo(onlyInMemoryResourceUri, false));
			assertFalse(modelDescriptor10_A.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptor20_A.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptorUml2_E.belongsTo(onlyInMemoryResourceUri, true));

			// Unload fileInMemory
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptor10_D.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptor10_E.belongsTo(onlyInMemoryResourceUri, true));
		}
		// ----------------------------------------------------------
		// Context object is HB 20Files
		{

			// - Given file has Persisted: TRUE, Loaded: TRUE

			for (String file20AName : hbProject20AResources20) {
				IFile file20 = refWks.hbProject20_A.getFile(file20AName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				URI uri20 = resource20.getURI();

				assertTrue(modelDescriptor20_A.belongsTo(uri20, true));
				assertTrue(modelDescriptor20_A.belongsTo(uri20, false));

			}
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				URI uri20 = resource20.getURI();

				assertTrue(modelDescriptor20_D.belongsTo(uri20, true));
				assertTrue(modelDescriptor20_D.belongsTo(uri20, false));
				assertTrue(modelDescriptor20_E.belongsTo(uri20, true));
				assertFalse(modelDescriptor20_E.belongsTo(uri20, false));

			}
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				URI uri20 = resource20.getURI();

				assertFalse(modelDescriptor20_D.belongsTo(uri20, true));
				assertFalse(modelDescriptor20_D.belongsTo(uri20, false));

				assertTrue(modelDescriptor20_E.belongsTo(uri20, true));
				assertTrue(modelDescriptor20_E.belongsTo(uri20, false));

			}
			// - Given file has Persisted: FALSE, Loaded: TRUE
			// - Given file is existed

			for (String file20AName : hbProject20AResources20) {
				IFile file20 = refWks.hbProject20_A.getFile(file20AName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				URI uri20 = resource20.getURI();

				assertFalse(modelDescriptor20_D.belongsTo(uri20, true));
				assertFalse(modelDescriptor20_E.belongsTo(uri20, true));

				assertFalse(modelDescriptor10_A.belongsTo(uri20, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(uri20, true));

			}

			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				URI uri20 = resource20.getURI();

				assertFalse(modelDescriptor20_A.belongsTo(uri20, true));
				assertFalse(modelDescriptor20_D.belongsTo(uri20, true));

				assertFalse(modelDescriptor10_A.belongsTo(uri20, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(uri20, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(uri20, true));

			}

			// - Given file has Loaded: FALSE
			for (String file20DName : hbProject20DResources20) {
				IFile file20 = refWks.hbProject20_D.getFile(file20DName);
				assertNotNull(file20);

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				URI uri20 = resource20.getURI();
				// Unload file
				ModelLoadManager.INSTANCE.unloadFile(file20, false, new NullProgressMonitor());
				waitForModelLoading();

				assertTrue(file20.isAccessible());
				assertFalse(EcorePlatformUtil.isFileLoaded(file20));

				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertTrue(modelDescriptor20_D.belongsTo(uri20, true));
				assertTrue(modelDescriptor20_E.belongsTo(uri20, true));
				assertFalse(modelDescriptor20_E.belongsTo(uri20, false));
				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor20_A.belongsTo(uri20, true));
				assertFalse(modelDescriptor10_A.belongsTo(uri20, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(uri20, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(uri20, true));

			}
			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.instancemodel";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file HB_FILE_NAME_20_20A_1
			EObject modelRoot = createHummingbird20InstanceModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource20);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(modelDescriptor20_D.belongsTo(onlyInMemoryResourceUri, true));
			assertTrue(modelDescriptor20_E.belongsTo(onlyInMemoryResourceUri, true));

			assertFalse(modelDescriptor20_E.belongsTo(onlyInMemoryResourceUri, false));
			assertFalse(modelDescriptor20_A.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptor10_A.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptorUml2_D.belongsTo(onlyInMemoryResourceUri, true));
			// Unload fileInMemoryOnly

			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();

			assertFalse(modelDescriptor20_D.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptor20_E.belongsTo(onlyInMemoryResourceUri, true));
		}

		// --------------------------------------------------------
		// UML2 files
		{
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
				assertNotNull(resourceUml2);
				URI uriUml2 = resourceUml2.getURI();

				assertTrue(modelDescriptorUml2_E.belongsTo(uriUml2, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(uriUml2, false));
				assertTrue(modelDescriptorUml2_D.belongsTo(uriUml2, true));
				assertTrue(modelDescriptorUml2_D.belongsTo(uriUml2, false));

			}
			for (String fileUml2_20EName : hbProject20EResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_E.getFile(fileUml2_20EName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
				assertNotNull(resourceUml2);
				URI uriUml2 = resourceUml2.getURI();

				assertFalse(modelDescriptorUml2_D.belongsTo(uriUml2, true));
				assertFalse(modelDescriptorUml2_D.belongsTo(uriUml2, false));
				assertTrue(modelDescriptorUml2_E.belongsTo(uriUml2, true));
				assertTrue(modelDescriptorUml2_E.belongsTo(uriUml2, false));

			}
			//
			// - Given file has Persisted: FALSE, Loaded: TRUE
			// - Given file is existed
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
				assertNotNull(resourceUml2);
				URI uriUml2 = resourceUml2.getURI();

				assertFalse(modelDescriptor10_A.belongsTo(uriUml2, true));
				assertFalse(modelDescriptor20_D.belongsTo(uriUml2, true));
				assertFalse(modelDescriptor20_E.belongsTo(uriUml2, true));
			}

			// - Given file has Loaded: FALSE
			for (String fileUml2_20DName : hbProject20DResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2_20DName);
				assertNotNull(fileUml2);

				Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
				assertNotNull(resourceUml2);
				URI uriUml2 = resourceUml2.getURI();
				// Unload file
				ModelLoadManager.INSTANCE.unloadFile(fileUml2, false, new NullProgressMonitor());
				waitForModelLoading();

				assertTrue(fileUml2.isAccessible());
				assertFalse(EcorePlatformUtil.isFileLoaded(fileUml2));
				assertNotNull(resourceUml2);
				// - Given file has Persisted: TRUE, Loaded: FALSE
				assertTrue(modelDescriptorUml2_D.belongsTo(uriUml2, true));
				assertTrue(modelDescriptorUml2_E.belongsTo(uriUml2, true));
				assertFalse(modelDescriptorUml2_E.belongsTo(uriUml2, false));
				// - Given file has Persisted: FALSE, Loaded: FALSE
				assertFalse(modelDescriptor10_A.belongsTo(uriUml2, true));
				assertFalse(modelDescriptor20_D.belongsTo(uriUml2, true));
				assertFalse(modelDescriptor20_E.belongsTo(uriUml2, true));

			}
			// - Given file in memory only
			String onlyInMemoryResourceName = "NewResource.uml";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file UML2_FILE_NAME_20E_1
			EObject modelRoot = createUML2ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomainUml2, onlyInMemoryResourceIPath, UMLPackage.eCONTENT_TYPE, modelRoot, false,
					null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResourceUml2 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResourceUml2);
			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(modelDescriptorUml2_E.belongsTo(onlyInMemoryResourceUri, true));
			assertTrue(modelDescriptorUml2_D.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptorUml2_E.belongsTo(onlyInMemoryResourceUri, false));

			assertFalse(modelDescriptor10_A.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptor20_D.belongsTo(onlyInMemoryResourceUri, true));
			// Unload
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();
			assertFalse(modelDescriptorUml2_E.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(modelDescriptorUml2_D.belongsTo(onlyInMemoryResourceUri, true));

		}
		{
			// URI nullUri = null;

		}

	}

	/**
	 * Test method for {@link ModelDescriptor#didBelongsTo(IFile)}
	 *
	 * @throws Exception
	 */
	// TODO the behavior of didBelongto(...) will be considered and might be changed again. It will impact to tests on
	// case: Delete, Rename, Move file. The return should be TRUE
	public void testDidBelongsTo_IFile() throws Exception {

		assertNotNull(refWks.editingDomain10);
		assertNotNull(refWks.editingDomain20);
		assertNotNull(refWks.editingDomainUml2);

		// --------------------------------------------------------------
		// Context: ModelDescriptor10
		IFile testFile10 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile testFile20 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile testFileUml2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		{
			IModelDescriptor hbModel10A = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
			IModelDescriptor hbModel10D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
			IModelDescriptor hbModel10E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

			assertNotNull(hbModel10A);
			assertNotNull(hbModel10D);
			assertNotNull(hbModel10E);

			// Given file is persisted file
			// - Change contents the underlying file

			for (String file10AName : hbProject10AResources10) {
				if (!testFile10.getName().equals(file10AName)) {
					IFile file10 = refWks.hbProject10_A.getFile(file10AName);
					assertNotNull(file10);
					assertTrue(file10.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));
					assertTrue(hbModel10A.belongsTo(file10, true));

					synchronizedSetFileContents(file10, testFile10.getContents());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));
					assertFalse(hbModel10A.didBelongTo(file10, true));
					assertFalse(hbModel10D.didBelongTo(file10, true));
					assertFalse(hbModel10E.didBelongTo(file10, true));
				}
			}

			// Delete the underlying file
			List<String> testResource10E = hbProject10EResources10.subList(0, 2);
			for (String file10EName : testResource10E) {
				IFile file10 = refWks.hbProject10_E.getFile(file10EName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));
				assertTrue(hbModel10E.belongsTo(file10, true));

				synchronizedDeleteFile(file10);
				assertNotNull(file10);
				assertFalse(EcorePlatformUtil.isFileLoaded(file10));

				assertFalse(hbModel10A.belongsTo(file10, true));
				assertFalse(hbModel10D.belongsTo(file10, true));
				assertFalse(hbModel10E.belongsTo(file10, true));
			}

			// Rename the underlying file
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));
				assertTrue(hbModel10D.belongsTo(file10, true));
				assertTrue(hbModel10E.belongsTo(file10, true));
				// Rename = delete old file + copy to a new file with new name
				String newFileName = "renamed_" + file10.getName();
				synchronizedRenameFile(file10, newFileName);
				IFile ifile = refWks.hbProject10_D.getFile(newFileName);
				assertTrue(ifile.isAccessible());

				assertFalse(hbModel10D.didBelongTo(file10, true));
				assertFalse(hbModel10E.didBelongTo(file10, true));
				assertFalse(hbModel10A.didBelongTo(file10, true));

				assertTrue(hbModel10D.belongsTo(ifile, true));
				assertTrue(hbModel10E.belongsTo(ifile, true));
			}
			// Move file

			for (String file10AName : hbProject10AResources10) {
				if (!testFile10.getName().equals(file10AName)) {
					IFile file10 = refWks.hbProject10_A.getFile(file10AName);
					assertNotNull(file10);
					assertTrue(file10.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));
					assertTrue(hbModel10A.belongsTo(file10, true));

					IPath target = refWks.hbProject10_E.getFullPath().append(file10AName);

					synchronizedMoveFile(file10, target);

					assertFalse(hbModel10A.didBelongTo(file10, true));
					assertFalse(hbModel10D.didBelongTo(file10, true));
					assertFalse(hbModel10E.didBelongTo(file10, true));
				}
			}
			// - Given file in memory only- unload it
			String onlyInMemoryResourceName = "newResource.hummingbird";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			EObject modelRoot = createHumingbird10ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain10, onlyInMemoryResourceIPath, Hummingbird10Package.eCONTENT_TYPE, modelRoot,
					false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource10 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource10);

			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));
			assertTrue(hbModel10D.belongsTo(fileInMemory, true));
			assertTrue(hbModel10E.belongsTo(fileInMemory, true));

			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertFalse(hbModel10A.didBelongTo(fileInMemory, true));
			// assertTrue(hbModel10D.didBelongTo(fileInMemory, true));
			// assertTrue(hbModel10E.didBelongTo(fileInMemory, true));
			assertFalse(hbModel10E.didBelongTo(fileInMemory, false));

		}
		// ----------------------------------------------------------
		// Context object is HB 20Files
		{
			IModelDescriptor hbModel20A = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
			IModelDescriptor hbModel20D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
			IModelDescriptor hbModel20E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
			assertNotNull(hbModel20A);
			assertNotNull(hbModel20D);
			assertNotNull(hbModel20E);

			// - Given file has Persisted
			// Context: change file's contents
			for (String file20AName : hbProject20AResources20) {
				if (!testFile20.getName().equals(file20AName)) {
					IFile file20 = refWks.hbProject20_A.getFile(file20AName);
					assertNotNull(file20);
					assertTrue(file20.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file20));
					assertTrue(hbModel20A.belongsTo(file20, true));

					synchronizedSetFileContents(file20, testFile20.getContents());

					assertTrue(EcorePlatformUtil.isFileLoaded(file20));
					assertFalse(hbModel20A.didBelongTo(file20, true));
					assertFalse(hbModel20D.didBelongTo(file20, true));
					assertFalse(hbModel20E.didBelongTo(file20, true));
				}

			}

			// Context: Delete underlying file
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));
				assertTrue(hbModel20E.belongsTo(file20, true));

				synchronizedDeleteFile(file20);
				assertNotNull(file20);
				assertFalse(EcorePlatformUtil.isFileLoaded(file20));

				assertFalse(hbModel20A.didBelongTo(file20, true));
				assertFalse(hbModel20D.didBelongTo(file20, true));
				assertFalse(hbModel20E.didBelongTo(file20, true));

			}
			// Context: Rename underlying file
			// Context: Move file

			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.instancemodel";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file HB_FILE_NAME_20_20A_1
			EObject modelRoot = createHummingbird20InstanceModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource20);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(hbModel20A.belongsTo(fileInMemory, true));
			assertFalse(hbModel20D.belongsTo(fileInMemory, true));
			assertFalse(hbModel20E.belongsTo(fileInMemory, true));

			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));
			waitForModelLoading();
			// assertTrue(hbModel20A.didBelongTo(fileInMemory, true));
			assertFalse(hbModel20D.didBelongTo(fileInMemory, true));
			assertFalse(hbModel20E.didBelongTo(fileInMemory, true));

		}
		// ----------------------------------------------------------
		// Context object is Uml2 Files
		{
			IModelDescriptor uml2Model20D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
			IModelDescriptor uml2Model20E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
			assertNotNull(uml2Model20D);
			assertNotNull(uml2Model20E);

			// - Given file has Persisted
			// Context: change file's contents
			for (String fileUml2Name : hbProject20DResourcesUml2) {
				if (!testFileUml2.getName().equals(fileUml2Name)) {
					IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2Name);
					assertNotNull(fileUml2);
					assertTrue(fileUml2.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));
					assertTrue(uml2Model20D.belongsTo(fileUml2, true));
					assertTrue(uml2Model20E.belongsTo(fileUml2, true));

					synchronizedSetFileContents(fileUml2, testFileUml2.getContents());
					assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

					assertFalse(uml2Model20D.didBelongTo(fileUml2, true));
					assertFalse(uml2Model20E.didBelongTo(fileUml2, true));
				}

			}

			// Context: Delete underlying file
			for (String fileUml2EName : hbProject20EResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_E.getFile(fileUml2EName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));
				assertTrue(uml2Model20E.belongsTo(fileUml2, true));
				assertFalse(uml2Model20D.belongsTo(fileUml2, true));

				synchronizedDeleteFile(fileUml2);
				assertNotNull(fileUml2);
				assertFalse(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertFalse(uml2Model20E.didBelongTo(fileUml2, true));
				assertFalse(uml2Model20D.didBelongTo(fileUml2, true));

			}
			// Context: Rename underlying file
			// Context: Move file
			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.uml";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file testFileUml2
			EObject modelRoot = createUML2ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomainUml2, onlyInMemoryResourceIPath, UMLPackage.eCONTENT_TYPE, modelRoot, false,
					null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResourceUml2 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResourceUml2);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(uml2Model20D.belongsTo(fileInMemory, true));
			assertTrue(uml2Model20D.belongsTo(fileInMemory, true));
			// Unload resource in memory only
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));
			assertFalse(uml2Model20D.didBelongTo(fileInMemory, true));
			assertFalse(uml2Model20E.didBelongTo(fileInMemory, true));

		}

	}

	/**
	 * Test method for {@link ModelDescriptor#didBelongTo(Resource, boolean)}
	 *
	 * @throws Exception
	 */
	public void testDidBelongsTo_IResource() throws Exception {

		assertNotNull(refWks.editingDomain10);
		assertNotNull(refWks.editingDomain20);
		assertNotNull(refWks.editingDomainUml2);

		// --------------------------------------------------------------
		// Context: ModelDescriptor10
		IFile testFile10 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile testFile20 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile testFileUml2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		{
			IModelDescriptor hbModel10A = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
			IModelDescriptor hbModel10D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
			IModelDescriptor hbModel10E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
			assertNotNull(hbModel10A);
			assertNotNull(hbModel10D);
			assertNotNull(hbModel10E);

			// Given file is persisted file
			// - Change contents the underlying file

			for (String file10AName : hbProject10AResources10) {
				if (!testFile10.getName().equals(file10AName)) {
					IFile file10 = refWks.hbProject10_A.getFile(file10AName);
					assertNotNull(file10);
					assertTrue(file10.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));
					Resource resource10 = EcorePlatformUtil.getResource(file10);
					assertNotNull(resource10);
					assertTrue(hbModel10A.belongsTo(resource10, true));

					synchronizedSetFileContents(file10, testFile10.getContents());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));
					assertNotNull(resource10);
					assertFalse(hbModel10A.didBelongTo(resource10, true));
					assertFalse(hbModel10D.didBelongTo(resource10, true));
					assertFalse(hbModel10E.didBelongTo(resource10, true));
				}
			}

			// Delete the underlying file
			List<String> testResource10E = hbProject10EResources10.subList(0, 2);
			for (String file10EName : testResource10E) {
				IFile file10 = refWks.hbProject10_E.getFile(file10EName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				assertTrue(hbModel10E.belongsTo(resource10, true));

				synchronizedDeleteFile(file10);
				assertNotNull(resource10);
				assertFalse(hbModel10A.belongsTo(resource10, true));
				assertFalse(hbModel10D.belongsTo(resource10, true));
				assertFalse(hbModel10E.belongsTo(resource10, true));
			}

			// Rename the underlying file
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertTrue(hbModel10D.belongsTo(resource10, true));
				assertTrue(hbModel10E.belongsTo(resource10, true));
				// Rename = delete old file + copy to a new file with new name
				String newFileName = "renamed_" + file10.getName();
				synchronizedRenameFile(file10, newFileName);
				IFile ifile = refWks.hbProject10_D.getFile(newFileName);
				assertTrue(ifile.isAccessible());

				assertNotNull(resource10);
				assertFalse(hbModel10D.didBelongTo(resource10, true));
				assertFalse(hbModel10E.didBelongTo(resource10, true));
				assertFalse(hbModel10A.didBelongTo(resource10, true));

				assertTrue(hbModel10D.belongsTo(ifile, true));
				assertTrue(hbModel10E.belongsTo(ifile, true));
			}
			// Move file

			for (String file10AName : hbProject10AResources10) {
				if (!testFile10.getName().equals(file10AName)) {
					IFile file10 = refWks.hbProject10_A.getFile(file10AName);
					assertNotNull(file10);
					assertTrue(file10.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));

					Resource resource10 = EcorePlatformUtil.getResource(file10);
					assertNotNull(resource10);
					assertTrue(hbModel10A.belongsTo(resource10, true));

					IPath target = refWks.hbProject10_E.getFullPath().append(file10AName);

					synchronizedMoveFile(file10, target);
					assertNotNull(resource10);
					assertFalse(hbModel10A.didBelongTo(resource10, true));
					assertFalse(hbModel10D.didBelongTo(resource10, true));
					assertFalse(hbModel10E.didBelongTo(resource10, true));
				}
			}
			// - Given file in memory only- unload it
			String onlyInMemoryResourceName = "newResource.hummingbird";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			EObject modelRoot = createHumingbird10ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain10, onlyInMemoryResourceIPath, Hummingbird10Package.eCONTENT_TYPE, modelRoot,
					false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource10 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource10);

			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());

			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));
			assertTrue(hbModel10D.belongsTo(fileInMemory, true));
			assertTrue(hbModel10E.belongsTo(fileInMemory, true));
			// Unload resourceInMemoryOnly
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertFalse(hbModel10A.didBelongTo(onlyInMemoryResource10, true));
			// assertTrue(hbModel10D.didBelongTo(onlyInMemoryResource10, true));
			// assertTrue(hbModel10E.didBelongTo(onlyInMemoryResource10, true));
			assertFalse(hbModel10E.didBelongTo(onlyInMemoryResource10, false));

		}
		// ----------------------------------------------------------
		// Context object is HB 20Files
		{
			IModelDescriptor hbModel20A = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
			IModelDescriptor hbModel20D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
			IModelDescriptor hbModel20E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
			assertNotNull(hbModel20A);
			assertNotNull(hbModel20D);
			assertNotNull(hbModel20E);

			// - Given file has Persisted
			// Context: change file's contents
			for (String file20AName : hbProject20AResources20) {
				if (!testFile20.getName().equals(file20AName)) {
					IFile file20 = refWks.hbProject20_A.getFile(file20AName);
					assertNotNull(file20);
					assertTrue(file20.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file20));

					Resource resource20 = EcorePlatformUtil.getResource(file20);
					assertNotNull(resource20);
					assertTrue(hbModel20A.belongsTo(resource20, true));

					synchronizedSetFileContents(file20, testFile20.getContents());
					assertTrue(EcorePlatformUtil.isFileLoaded(file20));

					assertFalse(hbModel20A.didBelongTo(resource20, true));
					assertFalse(hbModel20D.didBelongTo(resource20, true));
					assertFalse(hbModel20E.didBelongTo(resource20, true));
				}

			}

			// Context: Delete underlying file
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				assertTrue(hbModel20E.belongsTo(resource20, true));

				synchronizedDeleteFile(file20);
				assertNotNull(file20);
				assertFalse(EcorePlatformUtil.isFileLoaded(file20));

				assertNotNull(resource20);
				assertFalse(hbModel20A.didBelongTo(resource20, true));
				assertFalse(hbModel20D.didBelongTo(resource20, true));
				assertFalse(hbModel20E.didBelongTo(resource20, true));

			}
			// Context: Rename underlying file
			// Context: Move file

			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.instancemodel";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			EObject modelRoot = createHummingbird20InstanceModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource20);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(hbModel20A.belongsTo(fileInMemory, true));
			assertFalse(hbModel20D.belongsTo(fileInMemory, true));
			assertFalse(hbModel20E.belongsTo(fileInMemory, true));

			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));

			// assertTrue(hbModel20A.didBelongTo(onlyInMemoryResource20, true));
			assertFalse(hbModel20D.didBelongTo(onlyInMemoryResource20, true));
			assertFalse(hbModel20E.didBelongTo(onlyInMemoryResource20, true));

		}
		// ----------------------------------------------------------
		// Context object is Uml2 Files
		{
			IModelDescriptor uml2Model20D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
			IModelDescriptor uml2Model20E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
			assertNotNull(uml2Model20D);
			assertNotNull(uml2Model20E);

			// - Given file has Persisted
			// Context: change file's contents
			for (String fileUml2Name : hbProject20DResourcesUml2) {
				if (!testFileUml2.getName().equals(fileUml2Name)) {
					IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2Name);
					assertNotNull(fileUml2);
					assertTrue(fileUml2.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

					Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
					assertNotNull(resourceUml2);
					assertTrue(uml2Model20D.belongsTo(resourceUml2, true));
					assertTrue(uml2Model20E.belongsTo(resourceUml2, true));

					synchronizedSetFileContents(fileUml2, testFileUml2.getContents());
					assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));
					assertNotNull(resourceUml2);
					assertFalse(uml2Model20D.didBelongTo(resourceUml2, true));
					assertFalse(uml2Model20E.didBelongTo(resourceUml2, true));
				}

			}

			// Context: Delete underlying file
			for (String fileUml2EName : hbProject20EResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_E.getFile(fileUml2EName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
				assertNotNull(resourceUml2);
				assertTrue(uml2Model20E.belongsTo(resourceUml2, true));
				assertFalse(uml2Model20D.belongsTo(resourceUml2, true));

				synchronizedDeleteFile(fileUml2);
				assertNotNull(fileUml2);
				assertNotNull(resourceUml2);

				assertFalse(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertFalse(uml2Model20E.didBelongTo(resourceUml2, true));
				assertFalse(uml2Model20D.didBelongTo(resourceUml2, true));

			}
			// Context: Rename underlying file
			// Context: Move file
			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.uml";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file testFileUml2
			EObject modelRoot = createUML2ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomainUml2, onlyInMemoryResourceIPath, UMLPackage.eCONTENT_TYPE, modelRoot, true,
					null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResourceUml2 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResourceUml2);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(uml2Model20D.belongsTo(onlyInMemoryResourceUml2, true));
			assertTrue(uml2Model20D.belongsTo(onlyInMemoryResourceUml2, true));

			// Unload resource in memory only
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));
			// assertTrue(uml2Model20D.didBelongTo(onlyInMemoryResourceUml2, true));
			// assertTrue(uml2Model20E.didBelongTo(onlyInMemoryResourceUml2, true));

		}

	}

	/**
	 * Test method for {@link ModelDescriptor#didBelongTo(URI, boolean)}
	 *
	 * @throws Exception
	 */
	public void testDidBelongsTo_URI() throws Exception {

		assertNotNull(refWks.editingDomain10);
		assertNotNull(refWks.editingDomain20);
		assertNotNull(refWks.editingDomainUml2);

		// --------------------------------------------------------------
		// Context: ModelDescriptor10
		IFile testFile10 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile testFile20 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile testFileUml2 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		{
			IModelDescriptor hbModel10A = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
			IModelDescriptor hbModel10D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
			IModelDescriptor hbModel10E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
			assertNotNull(hbModel10A);
			assertNotNull(hbModel10D);
			assertNotNull(hbModel10E);

			// Given file is persisted file
			// - Change contents the underlying file

			for (String file10AName : hbProject10AResources10) {
				if (!testFile10.getName().equals(file10AName)) {
					IFile file10 = refWks.hbProject10_A.getFile(file10AName);
					assertNotNull(file10);
					assertTrue(file10.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));
					Resource resource10 = EcorePlatformUtil.getResource(file10);
					assertNotNull(resource10);
					URI uri10 = resource10.getURI();
					assertTrue(hbModel10A.belongsTo(uri10, true));

					System.out.println(">>>>>>>>>>>> Setting contents");
					synchronizedSetFileContents(file10, testFile10.getContents());
					System.out.println("<<<<<<<<<<<< Done.");
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));

					assertFalse(hbModel10A.didBelongTo(uri10, true));
					assertFalse(hbModel10D.didBelongTo(uri10, true));
					assertFalse(hbModel10E.didBelongTo(uri10, true));
				}
			}

			// Delete the underlying file
			List<String> testResource10E = hbProject10EResources10.subList(0, 2);
			for (String file10EName : testResource10E) {
				IFile file10 = refWks.hbProject10_E.getFile(file10EName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				URI uri10 = resource10.getURI();
				assertTrue(hbModel10E.belongsTo(uri10, true));

				synchronizedDeleteFile(file10);
				assertFalse(hbModel10A.belongsTo(uri10, true));
				assertFalse(hbModel10D.belongsTo(uri10, true));
				assertFalse(hbModel10E.belongsTo(uri10, true));
			}

			// Rename the underlying file
			for (String file10DName : hbProject10DResources10) {
				IFile file10 = refWks.hbProject10_D.getFile(file10DName);
				assertNotNull(file10);
				assertTrue(file10.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file10));

				Resource resource10 = EcorePlatformUtil.getResource(file10);
				assertNotNull(resource10);
				URI uri10 = resource10.getURI();

				assertTrue(hbModel10D.belongsTo(uri10, true));
				assertTrue(hbModel10E.belongsTo(uri10, true));

				// Rename = delete old file + copy to a new file with new name
				String newFileName = "renamed_" + file10.getName();
				synchronizedRenameFile(file10, newFileName);
				IFile ifile = refWks.hbProject10_D.getFile(newFileName);
				assertTrue(ifile.isAccessible());

				assertNotNull(resource10);
				assertFalse(hbModel10D.didBelongTo(uri10, true));
				assertFalse(hbModel10E.didBelongTo(uri10, true));
				assertFalse(hbModel10A.didBelongTo(uri10, true));

				assertTrue(hbModel10D.belongsTo(URI.createPlatformResourceURI(ifile.getFullPath().toString(), true), true));
				assertTrue(hbModel10E.belongsTo(URI.createPlatformResourceURI(ifile.getFullPath().toString(), true), true));
			}
			// Move file

			for (String file10AName : hbProject10AResources10) {
				if (!testFile10.getName().equals(file10AName)) {
					IFile file10 = refWks.hbProject10_A.getFile(file10AName);
					assertNotNull(file10);
					assertTrue(file10.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file10));

					Resource resource10 = EcorePlatformUtil.getResource(file10);
					assertNotNull(resource10);
					URI uri10 = resource10.getURI();
					assertTrue(hbModel10A.belongsTo(uri10, true));

					IPath target = refWks.hbProject10_E.getFullPath().append(file10AName);

					synchronizedMoveFile(file10, target);

					assertFalse(hbModel10A.didBelongTo(uri10, true));
					assertFalse(hbModel10D.didBelongTo(uri10, true));
					assertFalse(hbModel10E.didBelongTo(uri10, true));
				}
			}
			// - Given file in memory only- unload it
			String onlyInMemoryResourceName = "newResource.hummingbird";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			EObject modelRoot = createHumingbird10ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain10, onlyInMemoryResourceIPath, Hummingbird10Package.eCONTENT_TYPE, modelRoot,
					false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource10 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource10);

			Path path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(hbModel10D.belongsTo(onlyInMemoryResourceUri, true));
			assertTrue(hbModel10E.belongsTo(onlyInMemoryResourceUri, true));

			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertFalse(hbModel10A.didBelongTo(onlyInMemoryResourceUri, true));
			// assertTrue(hbModel10D.didBelongTo(onlyInMemoryResourceUri, true));
			// assertTrue(hbModel10E.didBelongTo(onlyInMemoryResourceUri, true));
			assertFalse(hbModel10E.didBelongTo(onlyInMemoryResourceUri, false));

		}
		// ----------------------------------------------------------
		// Context object is HB 20Files
		{
			IModelDescriptor hbModel20A = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
			IModelDescriptor hbModel20D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
			IModelDescriptor hbModel20E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
			assertNotNull(hbModel20A);
			assertNotNull(hbModel20D);
			assertNotNull(hbModel20E);

			// - Given file has Persisted
			// Context: change file's contents
			for (String file20AName : hbProject20AResources20) {
				if (!testFile20.getName().equals(file20AName)) {
					IFile file20 = refWks.hbProject20_A.getFile(file20AName);
					assertNotNull(file20);
					assertTrue(file20.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(file20));

					Resource resource20 = EcorePlatformUtil.getResource(file20);
					assertNotNull(resource20);
					URI uri20 = resource20.getURI();
					assertTrue(hbModel20A.belongsTo(uri20, true));

					synchronizedSetFileContents(file20, testFile20.getContents());
					assertTrue(EcorePlatformUtil.isFileLoaded(file20));

					assertFalse(hbModel20A.didBelongTo(uri20, true));
					assertFalse(hbModel20D.didBelongTo(uri20, true));
					assertFalse(hbModel20E.didBelongTo(uri20, true));
				}

			}

			// Context: Delete underlying file
			for (String file20EName : hbProject20EResources20) {
				IFile file20 = refWks.hbProject20_E.getFile(file20EName);
				assertNotNull(file20);
				assertTrue(file20.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(file20));

				Resource resource20 = EcorePlatformUtil.getResource(file20);
				assertNotNull(resource20);
				URI uri20 = resource20.getURI();
				assertTrue(hbModel20E.belongsTo(uri20, true));

				synchronizedDeleteFile(file20);
				assertNotNull(file20);
				assertFalse(EcorePlatformUtil.isFileLoaded(file20));

				assertFalse(hbModel20A.didBelongTo(uri20, true));
				assertFalse(hbModel20D.didBelongTo(uri20, true));
				assertFalse(hbModel20E.didBelongTo(uri20, true));

			}
			// Context: Rename underlying file
			// Context: Move file

			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.instancemodel";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			EObject modelRoot = createHummingbird20InstanceModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, onlyInMemoryResourceIPath,
					Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(), modelRoot, false, null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResource20 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResource20);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(hbModel20A.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(hbModel20D.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(hbModel20E.belongsTo(onlyInMemoryResourceUri, true));

			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			waitForModelLoading();
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertFalse(hbModel20A.didBelongTo(onlyInMemoryResourceUri, true));
			assertFalse(hbModel20D.didBelongTo(onlyInMemoryResourceUri, true));
			assertFalse(hbModel20E.didBelongTo(onlyInMemoryResourceUri, true));

		}
		// ----------------------------------------------------------
		// Context object is Uml2 Files
		{
			IModelDescriptor uml2Model20D = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
			IModelDescriptor uml2Model20E = ModelDescriptorRegistry.INSTANCE
					.getModel(refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
			assertNotNull(uml2Model20D);
			assertNotNull(uml2Model20E);

			// - Given file has Persisted
			// Context: change file's contents
			for (String fileUml2Name : hbProject20DResourcesUml2) {
				if (!testFileUml2.getName().equals(fileUml2Name)) {
					IFile fileUml2 = refWks.hbProject20_D.getFile(fileUml2Name);
					assertNotNull(fileUml2);
					assertTrue(fileUml2.isAccessible());
					assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

					Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
					assertNotNull(resourceUml2);
					URI uriUml2 = resourceUml2.getURI();
					assertTrue(uml2Model20D.belongsTo(uriUml2, true));
					assertTrue(uml2Model20E.belongsTo(uriUml2, true));

					synchronizedSetFileContents(fileUml2, testFileUml2.getContents());
					assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

					assertFalse(uml2Model20D.didBelongTo(uriUml2, true));
					assertFalse(uml2Model20E.didBelongTo(uriUml2, true));
				}

			}

			// Context: Delete underlying file
			for (String fileUml2EName : hbProject20EResourcesUml2) {
				IFile fileUml2 = refWks.hbProject20_E.getFile(fileUml2EName);
				assertNotNull(fileUml2);
				assertTrue(fileUml2.isAccessible());
				assertTrue(EcorePlatformUtil.isFileLoaded(fileUml2));

				Resource resourceUml2 = EcorePlatformUtil.getResource(fileUml2);
				assertNotNull(resourceUml2);
				URI uriUml2 = resourceUml2.getURI();

				assertTrue(uml2Model20E.belongsTo(uriUml2, true));
				assertFalse(uml2Model20D.belongsTo(uriUml2, true));

				synchronizedDeleteFile(fileUml2);
				assertNotNull(fileUml2);
				assertFalse(EcorePlatformUtil.isFileLoaded(fileUml2));

				assertFalse(uml2Model20E.didBelongTo(uriUml2, true));
				assertFalse(uml2Model20D.didBelongTo(uriUml2, true));

			}
			// Context: Rename underlying file
			// Context: Move file
			// - Given file in memory only
			String onlyInMemoryResourceName = "newResource.uml";
			String onlyInMemoryResourcePath = "/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/" + onlyInMemoryResourceName;
			URI onlyInMemoryResourceUri = URI.createPlatformResourceURI(onlyInMemoryResourcePath, true);

			// We retrieve model root from file testFileUml2
			EObject modelRoot = createUML2ModelRoot();
			// we add the new resource.
			IPath onlyInMemoryResourceIPath = EcorePlatformUtil.createPath(onlyInMemoryResourceUri);
			EcorePlatformUtil.addNewModelResource(refWks.editingDomainUml2, onlyInMemoryResourceIPath, UMLPackage.eCONTENT_TYPE, modelRoot, false,
					null);
			waitForModelLoading();

			// We ensure that no underlying file exist on file system for our newly created resource.
			assertFalse(EcoreResourceUtil.exists(onlyInMemoryResourceUri));
			Resource onlyInMemoryResourceUml2 = EcorePlatformUtil.getResource(onlyInMemoryResourceUri);
			assertNotNull(onlyInMemoryResourceUml2);

			IPath path = new Path(onlyInMemoryResourceUri.toPlatformString(true));
			IFile fileInMemory = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			assertNotNull(fileInMemory);
			assertFalse(fileInMemory.isAccessible());
			assertTrue(EcorePlatformUtil.isFileLoaded(fileInMemory));

			assertTrue(uml2Model20D.belongsTo(onlyInMemoryResourceUri, true));
			assertTrue(uml2Model20E.belongsTo(onlyInMemoryResourceUri, true));
			assertFalse(uml2Model20E.belongsTo(onlyInMemoryResourceUri, false));

			// Unload resource in memory only
			ModelLoadManager.INSTANCE.unloadFile(fileInMemory, false, new NullProgressMonitor());
			assertFalse(EcorePlatformUtil.isFileLoaded(fileInMemory));
			// assertTrue(uml2Model20D.didBelongTo(onlyInMemoryResourceUri, true));
			// assertTrue(uml2Model20E.didBelongTo(onlyInMemoryResourceUri, true));
			assertFalse(uml2Model20E.didBelongTo(onlyInMemoryResourceUri, false));

		}

	}

	private EObject createHumingbird10ModelRoot() {
		Application hb10Application = Hummingbird10Factory.eINSTANCE.createApplication();
		return hb10Application;
	}

}
