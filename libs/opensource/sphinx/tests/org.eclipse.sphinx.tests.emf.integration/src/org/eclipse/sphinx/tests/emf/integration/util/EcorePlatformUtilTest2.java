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
 *     itemis - [410825] Make sure that EcorePlatformUtil#getResourcesInModel(contextResource, includeReferencedModels) method return resources of the context resource in the same resource set
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.WrapperItemProvider;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.examples.hummingbird10.Application;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Factory;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterExpression;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * JUnit Test for class {@link EcorePlatformUtil}
 */
@SuppressWarnings("nls")
public class EcorePlatformUtilTest2 extends DefaultIntegrationTestCase {
	List<String> hbProject10AResource10;
	int resources10FromHbProject10_A;

	List<String> hbProject10DResource10;
	int resources10FromHbProject10_D;

	List<String> hbProject10EResource10;
	int resources10FromHbProject10_E;

	List<String> hbProject10FResource10;
	int resources10FromHbProject10_F;

	List<String> hbProject20AResources20;
	int resources20FromHbProject20_A;
	List<String> hbProject20AResourcesUml2;
	int ResourcesUml2FromHbProject20_A;

	List<String> hbProject20DResources20;
	int resource20FromHbProject20_D;
	List<String> hbProject20DResourcesUml2;
	int resourcesUml2FromHbProject20_D;

	List<String> hbProject20EResources20;
	int resource20FromHbProject20_E;
	List<String> hbProject20EResourcesUml2;
	int resourcesUml2FromHbProject20_E;

	public EcorePlatformUtilTest2() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		hbProject10EResource10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_E = hbProject10EResource10.size();

		hbProject10AResource10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_A = hbProject10AResource10.size();

		hbProject10DResource10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_D = hbProject10DResource10.size();

		hbProject10FResource10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_F, Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_F = hbProject10FResource10.size();

		hbProject20AResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, Hummingbird20MMDescriptor.INSTANCE);
		resources20FromHbProject20_A = hbProject20AResources20.size();
		hbProject20AResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A, UML2MMDescriptor.INSTANCE);
		ResourcesUml2FromHbProject20_A = hbProject20AResourcesUml2.size();

		hbProject20DResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, Hummingbird20MMDescriptor.INSTANCE);
		resource20FromHbProject20_D = hbProject20DResources20.size();
		hbProject20DResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_D = hbProject20DResourcesUml2.size();

		hbProject20EResources20 = refWks
				.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, Hummingbird20MMDescriptor.INSTANCE);
		resource20FromHbProject20_E = hbProject20EResources20.size();
		hbProject20EResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_E = hbProject20EResourcesUml2.size();

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(org.eclipse.emf.ecore.EObject)}
	 */
	public void testGetFilteredResourcesFromEObject() {
		Collection<Resource> resResources = new ArrayList<Resource>();
		EObject object = null; // Test Object

		// HB10 Object
		{
			Resource testResource10_A_1 = refWks.editingDomain10.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);

			Resource testResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
			Resource testResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
			assertNotNull(testResource10_A_1);
			assertNotNull(testResource10_D_1);
			assertNotNull(testResource10_E_1);

			assertFalse(testResource10_A_1.getContents().isEmpty());
			object = testResource10_A_1.getContents().get(0);
			assertTrue(object instanceof Application);
			Application applicationHb10_A_1 = (Application) object;

			assertFalse(testResource10_D_1.getContents().isEmpty());
			object = testResource10_D_1.getContents().get(0);
			assertTrue(object instanceof Application);
			Application applicationHb10_D_1 = (Application) object;

			assertFalse(testResource10_E_1.getContents().isEmpty());
			object = testResource10_E_1.getContents().get(0);
			assertTrue(object instanceof Application);
			Application applicationHb10_E_1 = (Application) object;

			resResources = EcorePlatformUtil.getResourcesInModel(applicationHb10_A_1, true);
			assertEquals(resources10FromHbProject10_A, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject10AResource10.contains(resource.getURI().lastSegment()));
			}

			resResources = EcorePlatformUtil.getResourcesInModel(applicationHb10_D_1, true);
			assertEquals(resources10FromHbProject10_D, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject10DResource10.contains(resource.getURI().lastSegment()));
			}

			resResources = EcorePlatformUtil.getResourcesInModel(applicationHb10_E_1, true);
			assertEquals(resources10FromHbProject10_D + resources10FromHbProject10_E, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject10EResource10.contains(resource.getURI().lastSegment())
						|| hbProject10DResource10.contains(resource.getURI().lastSegment()));
			}
		}
		// ---------------------------------------------------------
		// HB20 Object
		{
			Resource testResource20_A_1 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
			Resource testResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
			Resource testResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
							+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
			assertNotNull(testResource20_A_1);
			assertNotNull(testResource20_D_1);
			assertNotNull(testResource20_E_1);

			assertFalse(testResource20_A_1.getContents().isEmpty());
			object = testResource20_A_1.getContents().get(0);
			assertTrue(object instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
			org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application applicationHb20_A_1 = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) object;

			assertFalse(testResource20_D_1.getContents().isEmpty());
			object = testResource20_D_1.getContents().get(0);
			assertTrue(object instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
			org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application applicationHb20_D_1 = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) object;

			assertFalse(testResource20_E_1.getContents().isEmpty());
			object = testResource20_E_1.getContents().get(0);
			assertTrue(object instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
			org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application applicationHb20_E_1 = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) object;

			resResources = EcorePlatformUtil.getResourcesInModel(applicationHb20_A_1, true);
			assertEquals(resources20FromHbProject20_A, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject20AResources20.contains(resource.getURI().lastSegment()));
			}
			resResources = EcorePlatformUtil.getResourcesInModel(applicationHb20_D_1, true);
			assertEquals(resource20FromHbProject20_D, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
			resResources = EcorePlatformUtil.getResourcesInModel(applicationHb20_E_1, true);
			assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject20EResources20.contains(resource.getURI().lastSegment())
						|| hbProject20DResources20.contains(resource.getURI().lastSegment()));
			}
		}
		// ---------------------------------------------------------------------------------
		// Uml2 Resource
		{
			Resource testUml2Resource3x_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
							+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
			Resource testUml2Resource3x_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
					URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
							+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
			assertNotNull(testUml2Resource3x_D_1);
			assertNotNull(testUml2Resource3x_E_1);

			assertFalse(testUml2Resource3x_D_1.getContents().isEmpty());
			object = testUml2Resource3x_D_1.getContents().get(0);
			assertTrue(object instanceof Model);
			Model model_D_1 = (Model) object;

			assertFalse(testUml2Resource3x_E_1.getContents().isEmpty());
			object = testUml2Resource3x_E_1.getContents().get(0);
			assertTrue(object instanceof Model);
			Model model_E_1 = (Model) object;

			resResources = EcorePlatformUtil.getResourcesInModel(model_D_1, true);
			assertEquals(resourcesUml2FromHbProject20_D, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment()));
			}
			resResources = EcorePlatformUtil.getResourcesInModel(model_E_1, true);
			assertEquals(resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resResources.size());
			for (Resource resource : resResources) {
				assertTrue(hbProject20DResourcesUml2.contains(resource.getURI().lastSegment())
						|| hbProject20EResourcesUml2.contains(resource.getURI().lastSegment()));
			}
		}
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(org.eclipse.sphinx.emf.model.IModelDescriptor)}
	 */
	public void testGetFilteredResourcesFromIModelDescriptor() {

		IModelDescriptor model10_10A = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject10_A
				.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1));
		IModelDescriptor model10_10D = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject10_D
				.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		IModelDescriptor model10_10E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject10_E
				.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		IModelDescriptor model20_20A = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject20_A
				.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1));
		IModelDescriptor model20_20D = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject20_D
				.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		IModelDescriptor model20_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject20_E
				.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		IModelDescriptor modelUml2_20D = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject20_D
				.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		IModelDescriptor modelUml2_20E = ModelDescriptorRegistry.INSTANCE.getModel(refWks.hbProject20_E
				.getFile(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		assertNotNull(model10_10A);
		assertNotNull(model10_10D);
		assertNotNull(model10_10E);
		assertNotNull(model20_20A);
		assertNotNull(model20_20D);
		assertNotNull(model20_20E);

		// GetFilter Resource
		Collection<Resource> resResource = new ArrayList<Resource>();
		resResource = EcorePlatformUtil.getResourcesInModel(model10_10A, true);
		assertEquals(resources10FromHbProject10_A, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(model10_10D, true);
		assertEquals(resources10FromHbProject10_D, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(model10_10E, true);
		assertEquals(resources10FromHbProject10_D + resources10FromHbProject10_E, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(model20_20A, true);
		assertEquals(resources20FromHbProject20_A, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(model20_20D, true);
		assertEquals(resource20FromHbProject20_D, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(model20_20E, true);
		assertEquals(resource20FromHbProject20_E + resource20FromHbProject20_D, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(modelUml2_20D, true);
		assertEquals(resourcesUml2FromHbProject20_D, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(modelUml2_20E, true);
		assertEquals(resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resResource.size());
		// Get Filter Resource from null Model
		IModelDescriptor nullModel = null;
		resResource = EcorePlatformUtil.getResourcesInModel(nullModel, true);
		assertNotNull(resResource);
		assertEquals(0, resResource.size());

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(IFile)}
	 */
	public void testGetFilteredResourcesFromIFile() {
		IFile hbFile_10A = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		IFile hbFile_10D = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);
		IFile hbFile_10E = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);

		IFile hbFile_20A = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		IFile hbFile_20D = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		IFile hbFile_20E = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);

		IFile uml2File_20D = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		IFile uml2File_20E = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);

		assertTrue(hbFile_10A.isAccessible());
		assertTrue(hbFile_10D.isAccessible());
		assertTrue(hbFile_10E.isAccessible());

		assertTrue(hbFile_20A.isAccessible());
		assertTrue(hbFile_20D.isAccessible());
		assertTrue(hbFile_20E.isAccessible());

		assertTrue(uml2File_20D.isAccessible());
		assertTrue(uml2File_20E.isAccessible());

		Collection<Resource> resResource = new ArrayList<Resource>();
		resResource = EcorePlatformUtil.getResourcesInModel(hbFile_10A, true);
		assertEquals(resources10FromHbProject10_A, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(hbFile_10D, true);
		assertEquals(resources10FromHbProject10_D, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(hbFile_10E, true);
		assertEquals(resources10FromHbProject10_D + resources10FromHbProject10_E, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(hbFile_20A, true);
		assertEquals(resources20FromHbProject20_A, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(hbFile_20D, true);
		assertEquals(resource20FromHbProject20_D, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(hbFile_20E, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(uml2File_20D, true);
		assertEquals(resourcesUml2FromHbProject20_D, resResource.size());

		resResource = EcorePlatformUtil.getResourcesInModel(uml2File_20E, true);
		assertEquals(resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resResource.size());

		// Non Exit File
		IFile nonExistingFile = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		assertNotNull(nonExistingFile);
		assertFalse(nonExistingFile.isAccessible());
		resResource = EcorePlatformUtil.getResourcesInModel(nonExistingFile, true);
		assertEquals(0, resResource.size());

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(org.eclipse.emf.common.util.URI)}
	 */
	public void testGetFilteredResourcesFromURI() {
		Resource testResource10_A_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource testResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		Resource testResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(testResource10_A_1);
		assertNotNull(testResource10_D_1);
		assertNotNull(testResource10_E_1);

		Resource testResource20_A_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource testResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource testResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(testResource20_A_1);
		assertNotNull(testResource20_D_1);
		assertNotNull(testResource20_E_1);

		Resource testUml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource testUml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);

		assertNotNull(testUml2Resource20_D_1);
		assertNotNull(testUml2Resource20_E_1);
		// Resource URI
		Collection<Resource> resResources = new ArrayList<Resource>();
		resResources = EcorePlatformUtil.getResourcesInModel(testResource10_A_1.getURI(), true);
		assertEquals(resources10FromHbProject10_A, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource10_D_1.getURI(), true);
		assertEquals(resources10FromHbProject10_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource10_E_1.getURI(), true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource20_A_1.getURI(), true);
		assertEquals(resources20FromHbProject20_A, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource20_D_1.getURI(), true);
		assertEquals(resource20FromHbProject20_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource20_E_1.getURI(), true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testUml2Resource20_D_1.getURI(), true);
		assertEquals(resourcesUml2FromHbProject20_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testUml2Resource20_E_1.getURI(), true);
		assertEquals(resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resResources.size());

		// File URI
		IFile testFile = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(testFile);
		assertTrue(testFile.isAccessible());
		resResources = EcorePlatformUtil.getResourcesInModel(testFile.getLocationURI(), true);
		assertEquals(0, resResources.size());
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(Resource))}
	 */

	public void testGetFilteredResourcesFromResource() {
		Resource testResource10_A_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource testResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		Resource testResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(testResource10_A_1);
		assertNotNull(testResource10_D_1);
		assertNotNull(testResource10_E_1);

		Resource testResource20_A_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource testResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		Resource testResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(testResource20_A_1);
		assertNotNull(testResource20_D_1);
		assertNotNull(testResource20_E_1);

		Resource testUml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		Resource testUml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
		assertNotNull(testUml2Resource20_D_1);
		assertNotNull(testUml2Resource20_E_1);

		// GetFilteredResource from Resource
		Collection<Resource> resResources = new ArrayList<Resource>();
		resResources = EcorePlatformUtil.getResourcesInModel(testResource10_A_1, true);
		assertEquals(resources10FromHbProject10_A, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource10_D_1, true);
		assertEquals(resources10FromHbProject10_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource10_E_1, true);
		assertEquals(resources10FromHbProject10_D + resources10FromHbProject10_E, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource20_A_1, true);
		assertEquals(resources20FromHbProject20_A, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource20_D_1, true);
		assertEquals(resource20FromHbProject20_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testResource20_E_1, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testUml2Resource20_D_1, true);
		assertEquals(resourcesUml2FromHbProject20_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModel(testUml2Resource20_E_1, true);
		assertEquals(resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resResources.size());
		// Given resource is NULL
		Resource nullResource = null;
		resResources = EcorePlatformUtil.getResourcesInModel(nullResource, true);
		assertEquals(0, resResources.size());

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(org.eclipse.emf.edit.provider.IWrapperItemProvider)
	 * ()}
	 */

	public void testGetFilteredResourcesFromIWrapperItemProvider() {
		Collection<Resource> resResources = new ArrayList<Resource>();
		// HB20 contextObject
		{
			IFile file20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
			assertNotNull(file20_20A_1);
			assertTrue(file20_20A_1.isAccessible());
			Resource resource = EcorePlatformUtil.getResource(file20_20A_1);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
			assertTrue(modelRoot instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
			Component component = ((org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) modelRoot).getComponents().get(0);
			WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, modelRoot,
					InstanceModel20Package.eINSTANCE.getApplication_Components(), 1,
					((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(file20_20A_1)).getAdapterFactory());
			resResources = EcorePlatformUtil.getResourcesInModel(wrapperItemProvider, true);
			assertEquals(resources20FromHbProject20_A, resResources.size());
		}
		{
			IFile file20_20D_1 = refWks.hbProject20_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
			assertNotNull(file20_20D_1);
			assertTrue(file20_20D_1.isAccessible());
			Resource resource = EcorePlatformUtil.getResource(file20_20D_1);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
			assertTrue(modelRoot instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
			Component component = ((org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) modelRoot).getComponents().get(0);
			WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, modelRoot,
					InstanceModel20Package.eINSTANCE.getApplication_Components(), 1,
					((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(file20_20D_1)).getAdapterFactory());
			resResources = EcorePlatformUtil.getResourcesInModel(wrapperItemProvider, true);
			assertEquals(resource20FromHbProject20_D, resResources.size());
		}
		{
			IFile file20_20E_1 = refWks.hbProject20_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
			assertNotNull(file20_20E_1);
			assertTrue(file20_20E_1.isAccessible());
			Resource resource = EcorePlatformUtil.getResource(file20_20E_1);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
			assertTrue(modelRoot instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
			Component component = ((org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) modelRoot).getComponents().get(0);
			WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, modelRoot,
					InstanceModel20Package.eINSTANCE.getApplication_Components(), 1,
					((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(file20_20E_1)).getAdapterFactory());
			resResources = EcorePlatformUtil.getResourcesInModel(wrapperItemProvider, true);
			assertEquals(resource20FromHbProject20_E + resource20FromHbProject20_D, resResources.size());
		}
		// ----------------------------------------------------
		// HB10 context Object
		// HB10 Object
		{
			IFile file10_10A_1 = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
			assertNotNull(file10_10A_1);
			assertTrue(file10_10A_1.isAccessible());
			Resource resource = EcorePlatformUtil.getResource(file10_10A_1);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
			assertTrue(modelRoot instanceof Application);

			org.eclipse.sphinx.examples.hummingbird10.Component component = ((Application) modelRoot).getComponents().get(0);
			WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, modelRoot,
					Hummingbird10Package.eINSTANCE.getComponent_Parameters(), 1,
					((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(file10_10A_1)).getAdapterFactory());
			resResources = EcorePlatformUtil.getResourcesInModel(wrapperItemProvider, true);
			assertEquals(resources10FromHbProject10_A, resResources.size());
		}
		{
			IFile file10_10D_1 = refWks.hbProject10_D.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);
			assertNotNull(file10_10D_1);
			assertTrue(file10_10D_1.isAccessible());

			Resource resource = EcorePlatformUtil.getResource(file10_10D_1);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
			assertTrue(modelRoot instanceof Application);

			org.eclipse.sphinx.examples.hummingbird10.Component component = ((Application) modelRoot).getComponents().get(0);
			WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, modelRoot,
					Hummingbird10Package.eINSTANCE.getComponent_Parameters(), 1,
					((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(file10_10D_1)).getAdapterFactory());
			resResources = EcorePlatformUtil.getResourcesInModel(wrapperItemProvider, true);
			assertEquals(resources10FromHbProject10_D, resResources.size());
		}
		{
			IFile file10_10E_1 = refWks.hbProject10_E.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
			assertNotNull(file10_10E_1);
			assertTrue(file10_10E_1.isAccessible());

			Resource resource = EcorePlatformUtil.getResource(file10_10E_1);
			assertNotNull(resource);
			assertFalse(resource.getContents().isEmpty());
			EObject modelRoot = resource.getContents().get(0);
			assertNotNull(modelRoot);
			assertTrue(modelRoot instanceof Application);

			org.eclipse.sphinx.examples.hummingbird10.Component component = ((Application) modelRoot).getComponents().get(0);
			WrapperItemProvider wrapperItemProvider = new WrapperItemProvider(component, modelRoot,
					Hummingbird10Package.eINSTANCE.getComponent_Parameters(), 1,
					((AdapterFactoryEditingDomain) WorkspaceEditingDomainUtil.getEditingDomain(file10_10E_1)).getAdapterFactory());
			resResources = EcorePlatformUtil.getResourcesInModel(wrapperItemProvider, true);
			assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resResources.size());
		}
	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(org.eclipse.emf.ecore.util.FeatureMap.Entry))}
	 */
	public void testGetFilteredResourcesFromEntry() {
		Collection<Resource> resResources = new ArrayList<Resource>();
		IFile file20_20A_1 = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(file20_20A_1);
		assertTrue(file20_20A_1.isAccessible());
		Resource resource = EcorePlatformUtil.getResource(file20_20A_1);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertNotNull(modelRoot);
		assertTrue(modelRoot instanceof org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application);
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application modelRoot20A_1 = (org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application) modelRoot;

		assertFalse(modelRoot20A_1.getComponents().isEmpty());
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component component = modelRoot20A_1.getComponents().get(0);
		assertNotNull(component);

		assertFalse(component.getParameterExpressions().isEmpty());
		ParameterExpression parameterExpression = component.getParameterExpressions().get(0);
		assertNotNull(parameterExpression);
		assertFalse(parameterExpression.getMixed().isEmpty());
		Entry testEntry = parameterExpression.getMixed().get(0);

		assertNotNull(testEntry);
		assertEquals(InstanceModel20Package.Literals.PARAMETER_EXPRESSION__EXPRESSIONS, testEntry.getEStructuralFeature());

		resResources = EcorePlatformUtil.getResourcesInModel(testEntry, true);
		assertEquals(resources20FromHbProject20_A, resResources.size());
	}

	/**
	 * Test method for
	 * {@link EcorePlatformUtil#getResourcesInModel(org.eclipse.core.resources.IContainer, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)
	 * )}
	 */

	public void testGetFilteredResourcesFromIContainer() {
		IProject nullProject = null;
		IContainer workspaceRoot = refWks.hbProject10_A.getWorkspace().getRoot();

		Collection<Resource> resResources = new ArrayList<Resource>();
		// Get Filtered Resource from Folder
		IFolder testFolder = refWks.hbProject10_F.getFolder(DefaultTestReferenceWorkspace.HB_FOLDER_NAME_10_10F_1);
		assertNotNull(testFolder);
		resResources = EcorePlatformUtil.getResourcesInModels(testFolder, Hummingbird20MMDescriptor.INSTANCE, true);
		assertEquals(0, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(testFolder, Hummingbird10MMDescriptor.INSTANCE, true);
		assertEquals(resources10FromHbProject10_F, resResources.size());

		// Get Filtered Resource from Project
		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject10_A, Hummingbird10MMDescriptor.INSTANCE, true);
		assertEquals(resources10FromHbProject10_A, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject10_D, Hummingbird10MMDescriptor.INSTANCE, true);
		assertEquals(resources10FromHbProject10_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject10_E, Hummingbird10MMDescriptor.INSTANCE, true);
		assertEquals(resources10FromHbProject10_D + resources10FromHbProject10_E, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject20_A, Hummingbird20MMDescriptor.INSTANCE, true);
		assertEquals(resources20FromHbProject20_A, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject20_D, Hummingbird20MMDescriptor.INSTANCE, true);
		assertEquals(resource20FromHbProject20_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject20_E, Hummingbird20MMDescriptor.INSTANCE, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject20_D, UML2MMDescriptor.INSTANCE, true);
		assertEquals(resourcesUml2FromHbProject20_D, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(refWks.hbProject20_E, UML2MMDescriptor.INSTANCE, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resResources.size());

		// Get Filtered Resource from Workspace Root
		resResources = EcorePlatformUtil.getResourcesInModels(workspaceRoot, Hummingbird10MMDescriptor.INSTANCE, true);
		assertEquals(resources10FromHbProject10_A + resources10FromHbProject10_D + resources10FromHbProject10_E + resources10FromHbProject10_F,
				resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(workspaceRoot, Hummingbird20MMDescriptor.INSTANCE, true);
		assertEquals(resources20FromHbProject20_A + resource20FromHbProject20_D + resource20FromHbProject20_E, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(workspaceRoot, UML2MMDescriptor.INSTANCE, true);
		assertEquals(ResourcesUml2FromHbProject20_A + resourcesUml2FromHbProject20_D + resourcesUml2FromHbProject20_E, resResources.size());

		resResources = EcorePlatformUtil.getResourcesInModels(nullProject, UML2MMDescriptor.INSTANCE, true);
		assertEquals(0, resResources.size());

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInModel(Object, boolean)}
	 */

	public void testGetResourceInModel_File() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();

		// --Hummingbird20 Model
		// File in root project
		IFile hbFile20_D_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(hbFile20_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile20_D_1, false);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile20_D_1, true);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		IFile hbFile20_E_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertNotNull(hbFile20_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile20_E_1, false);
		assertEquals(resource20FromHbProject20_E, resourcesInModel.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile20_E_1, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// File in root project
		IFile hbFile10_D_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1);
		assertNotNull(hbFile10_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile10_D_1, false);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile10_D_1, true);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		IFile hbFile10_E_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1);
		assertNotNull(hbFile10_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile10_E_1, false);
		assertEquals(resources10FromHbProject10_E, resourcesInModel.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbFile10_E_1, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));
		// =====================================================
		// --Uml model
		// File in root project
		IFile uml2File20_D_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1);
		assertNotNull(uml2File20_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2File20_D_1, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2File20_D_1, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		IFile uml2File20_E_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		assertNotNull(uml2File20_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2File20_E_1, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2File20_E_1, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		// File is unexisting
		assertNotNull(refWks.hbProject10_A);
		IFile unexistingFile = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		assertNotNull(unexistingFile);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(unexistingFile, true);
		assertEquals(0, resourcesInModel.size());
		// Null File
		IFile nullFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		assertNull(nullFile);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(nullFile, true);
		assertEquals(0, resourcesInModel.size());
		// Non model file
		IFile projectFile = refWks.hbProject10_A.getFile(".project");
		assertNotNull(projectFile);
		assertTrue(projectFile.isAccessible());
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(projectFile, true);
		assertEquals(0, resourcesInModel.size());

	}

	public void testGetResourceInModel_Resource() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();

		// --Hummingbird20 Model
		// Resource in root project
		Resource hbResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		assertNotNull(hbResource20_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource20_D_1, false);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource20_D_1, true);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		Resource hbResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(hbResource20_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource20_E_1, false);
		assertEquals(resource20FromHbProject20_E, resourcesInModel.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource20_E_1, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		Resource hbResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		assertNotNull(hbResource10_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource10_D_1, false);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource10_D_1, true);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		Resource hbResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(hbResource10_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource10_E_1, false);
		assertEquals(resources10FromHbProject10_E, resourcesInModel.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource10_E_1, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));
		// =====================================================
		// --Uml model
		// File in root project
		Resource uml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(uml2Resource20_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Resource20_D_1, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Resource20_D_1, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		Resource uml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
		assertNotNull(uml2Resource20_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Resource20_E_1, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Resource20_E_1, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		// ==================================================
		// Create Hummingbird10 Resource in EditingDomain20
		int resourcesInEditingDomain20Count = refWks.editingDomain20.getResourceSet().getResources().size();

		Application hbApplication10 = Hummingbird10Factory.eINSTANCE.createApplication();
		IPath newResourcePath = refWks.hbProject20_E.getFullPath().append("1.hummingbird");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath, Hummingbird10Package.eCONTENT_TYPE, hbApplication10, false,
				null);
		resourcesInEditingDomain20Count++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count);

		Resource newHb10Resource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(newResourcePath.toString(), true), false);
		assertNotNull(newHb10Resource);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbResource20_E_1, false);
		// Hummingbird 10 Resources must be rejected. Because of
		// org.eclipse.sphinx.examples.hummingbird.ide.scoping.HummingbirdProjectResourceScope.isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(IFile,
		// IMetaModelDescriptor)
		assertEquals(resource20FromHbProject20_E, resourcesInModel.size());

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(newHb10Resource, false);
		// In case the resource does not belong to any model, all resources in underlying resource set must be returned,
		// i.e., all Hummingbird 2.0 resources in the workspace plus additional Hummingbird 1.0 resource
		assertEquals(resourcesInEditingDomain20Count, resourcesInModel.size());
		// --------------------------------------------------
		// Create UML2 resource in EditingDomain20
		Model model = UMLFactory.eINSTANCE.createModel();

		IPath newResourcePath2 = refWks.hbProject20_E.getFullPath().append("2.uml");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath2, UMLPackage.eCONTENT_TYPE, model, false, null);
		resourcesInEditingDomain20Count++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count);

		Resource newUml2Resource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(newResourcePath2.toString(), true), false);
		assertNotNull(newUml2Resource);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource20_E_1, false);
		// Only the Hummingbird 2.0 resources that match the Hummingbird metamodel version of the context project plus
		// the additional UML2 resource must be returned
		assertEquals(resource20FromHbProject20_E + 1, resourcesInScope.size());
		assertTrue(resourcesInScope.contains(newUml2Resource));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(newUml2Resource, false);
		// In case the resource does not belong to any model, all resources in underlying resource set must be returned,
		// i.e., all Hummingbird 2.0 resources in the workspace plus additional Hummingbird 1.0 resource and additional
		// UML2 resource
		assertEquals(resourcesInEditingDomain20Count, resourcesInScope.size());

		// ======================================
		// Null Resource
		Resource nullResource = null;
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(nullResource, true);
		assertEquals(0, resourcesInModel.size());
	}

	public void testGetResourceInModel_ModelDescriptor() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();

		// --Hummingbird20 Model
		// Resource in root project
		Resource hbResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		assertNotNull(hbResource20_D_1);
		IModelDescriptor hbModelDescriptor20D = ModelDescriptorRegistry.INSTANCE.getModel(hbResource20_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescriptor20D, false);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescriptor20D, true);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		Resource hbResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(hbResource20_E_1);

		IModelDescriptor hbModelDescriptor20E = ModelDescriptorRegistry.INSTANCE.getModel(hbResource20_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescriptor20E, false);
		assertEquals(resource20FromHbProject20_E, resourcesInModel.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescriptor20E, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		Resource hbResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		assertNotNull(hbResource10_D_1);

		IModelDescriptor hbModelDescripor10D = ModelDescriptorRegistry.INSTANCE.getModel(hbResource10_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescripor10D, false);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescripor10D, true);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		Resource hbResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(hbResource10_E_1);
		IModelDescriptor hbModelDescripor10E = ModelDescriptorRegistry.INSTANCE.getModel(hbResource10_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescripor10E, false);
		assertEquals(resources10FromHbProject10_E, resourcesInModel.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescripor10E, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));
		// =====================================================
		// --Uml model
		// File in root project
		Resource uml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(uml2Resource20_D_1);
		IModelDescriptor uml2ModelDescripor10D = ModelDescriptorRegistry.INSTANCE.getModel(uml2Resource20_D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2ModelDescripor10D, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2ModelDescripor10D, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		Resource uml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
		assertNotNull(uml2Resource20_E_1);
		IModelDescriptor uml2ModelDescripor10E = ModelDescriptorRegistry.INSTANCE.getModel(uml2Resource20_E_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2ModelDescripor10E, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2ModelDescripor10E, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		// ==================================================
		// Create Hummingbird10 Resource in EditingDomain20
		int resourcesInEditingDomain20Count = refWks.editingDomain20.getResourceSet().getResources().size();

		Application hbApplication10 = Hummingbird10Factory.eINSTANCE.createApplication();
		IPath newResourcePath = refWks.hbProject20_E.getFullPath().append("1.hummingbird");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath, Hummingbird10Package.eCONTENT_TYPE, hbApplication10, false,
				null);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count + 1);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbModelDescriptor20E, false);
		// Hummingbird 10 Resources must be rejected. Because of
		// org.eclipse.sphinx.examples.hummingbird.ide.scoping.HummingbirdProjectResourceScope.isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(IFile,
		// IMetaModelDescriptor)
		assertEquals(resource20FromHbProject20_E, resourcesInModel.size());

		// --------------------------------------------------
		// Create UML2 resource in EditingDomain20
		Model model = UMLFactory.eINSTANCE.createModel();

		IPath newResourcePath2 = refWks.hbProject20_E.getFullPath().append("2.uml");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath2, UMLPackage.eCONTENT_TYPE, model, false, null);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count + 2);

		Resource newUml2Resource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(newResourcePath2.toString(), true), false);
		assertNotNull(newUml2Resource);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescriptor20E, false);
		// Only resources in Model of the context resource will be returned
		assertEquals(resource20FromHbProject20_E + 1, resourcesInScope.size());
		assertTrue(resourcesInScope.contains(newUml2Resource));
	}

	public void testGetResourceInModel_URI() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();

		// --Hummingbird20 Model
		// Resource in root project
		URI hbUri20D_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri20D_1, false);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri20D_1, true);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		URI hbUri20E_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri20E_1, false);
		assertEquals(resource20FromHbProject20_E, resourcesInModel.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri20E_1, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		URI hbUri10D_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri10D_1, false);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri10D_1, true);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		URI hbUri10E_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri10E_1, false);
		assertEquals(resources10FromHbProject10_E, resourcesInModel.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(hbUri10E_1, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		// URI of EObject
		IFile hbFile20E_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertNotNull(hbFile20E_1);

		Resource resource = EcorePlatformUtil.getResource(hbFile20E_1);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject contextObject = resource.getContents().get(0);
		assertNotNull(contextObject);
		URI testUri = EcoreUtil.getURI(contextObject);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(testUri, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));

		// =====================================================
		// --Uml model
		// File in root project
		URI uml2Uri20D_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true);
		assertNotNull(uml2Uri20D_1);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Uri20D_1, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Uri20D_1, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		URI uml2Uri20E_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
				+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Uri20E_1, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(uml2Uri20E_1, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

	}

	public void testGetResourceInModel_EObject() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();
		EObject contextObject = null;
		// --Hummingbird20 Model
		// Resource in root project
		Resource hbResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		assertNotNull(hbResource20_D_1);

		assertFalse(hbResource20_D_1.getContents().isEmpty());
		contextObject = hbResource20_D_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, false);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, true);
		assertEquals(resource20FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		Resource hbResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(hbResource20_E_1);

		assertFalse(hbResource20_E_1.getContents().isEmpty());
		contextObject = hbResource20_E_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, false);
		assertEquals(resource20FromHbProject20_E, resourcesInModel.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		Resource hbResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		assertNotNull(hbResource10_D_1);
		assertFalse(hbResource10_D_1.getContents().isEmpty());
		contextObject = hbResource10_D_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, false);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, true);
		assertEquals(resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		Resource hbResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(hbResource10_E_1);

		assertFalse(hbResource10_E_1.getContents().isEmpty());
		contextObject = hbResource10_E_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, false);
		assertEquals(resources10FromHbProject10_E, resourcesInModel.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInModel.size());

		resourceNameInScope10E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));
		// =====================================================
		// --Uml model
		// File in root project
		Resource uml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(uml2Resource20_D_1);

		assertFalse(uml2Resource20_D_1.getContents().isEmpty());
		contextObject = uml2Resource20_D_1.getContents().get(0);
		assertNotNull(contextObject);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20D = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInModel.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		Resource uml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
		assertNotNull(uml2Resource20_E_1);

		assertFalse(uml2Resource20_E_1.getContents().isEmpty());
		contextObject = uml2Resource20_E_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextObject, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInModel.size());

		resourceNameInScope20E = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

	}

	public void testGetResourceInModel_EObject_WithoutUnderlyingResource() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();

		// Context Object without resource
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application testApplication = InstanceModel20Factory.eINSTANCE.createApplication();
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(testApplication, true);
		assertEquals(0, resourcesInModel.size());

		// Context Object without underlying resource
		// Create new resource
		String newResourceName = "ResourceInMemoryOnly.instancemodel";
		IPath newResourcePath = refWks.hbProject20_E.getFullPath().append(newResourceName);
		int resourceInEditingDomain20Count = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

		// Add new resource to EditingDomain
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath, Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(),
				testApplication, false, null);
		resourceInEditingDomain20Count++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20Count);
		// Verify that underlying file is not exist
		IFile file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, newResourceName);
		assertNull(file);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(testApplication, false);
		assertEquals(resource20FromHbProject20_E + 1, resourcesInModel.size());
		Collection<String> resourceNameInScope = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));
		assertTrue(resourceNameInScope.contains(newResourceName));

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(testApplication, true);
		assertEquals(resource20FromHbProject20_E + resource20FromHbProject20_D + 1, resourcesInModel.size());
		resourceNameInScope = getResourceNames(resourcesInModel);
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));
		assertTrue(resourceNameInScope.contains(newResourceName));
	}

	public void testGetResourceInModel_EObject_InResource_WithoutResourceSet() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();
		// Resource without resourceSet but belong to a Model
		int resourceInEditingDomain20Count = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);
		Resource testResource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(testResource);

		ResourceSet testResourceSet = testResource.getResourceSet();
		testResourceSet.getResources().remove(testResource);

		resourceInEditingDomain20Count--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20Count);

		assertNotNull(testResource);
		assertNull(testResource.getResourceSet());
		EObject testEObject = testResource.getContents().get(0);

		resourcesInModel = EcorePlatformUtil.getResourcesInModel(testEObject, true);
		assertNotNull(resourcesInModel);
		assertEquals(1, resourcesInModel.size());

		Collection<String> resourceNameInScope = getResourceNames(resourcesInModel);
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2));
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3));
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4));
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4));
	}

	public void testGetResourceInModel_EObject_InResourceSet_WithoutEditingDomain() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();

		// New resources in new ResourceSet
		{
			ResourceSetImpl testResourceSet = new ScopingResourceSetImpl();
			org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application testApplication20 = InstanceModel20Factory.eINSTANCE
					.createApplication();
			Application testApplication10 = Hummingbird10Factory.eINSTANCE.createApplication();
			Platform testPlatform = TypeModel20Factory.eINSTANCE.createPlatform();
			Model umlModel = UMLFactory.eINSTANCE.createModel();

			URI newResourceUri1 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/1.instancemodel", true);
			URI newResourceUri2 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/2.typemodel", true);
			URI newResourceUri3 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/3.hummingbird", true);
			URI newResourceUri4 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/4.uml", true);
			Resource newRes1 = testResourceSet.createResource(newResourceUri1);
			Resource newRes2 = testResourceSet.createResource(newResourceUri2);
			Resource newRes3 = testResourceSet.createResource(newResourceUri3);
			Resource newRes4 = testResourceSet.createResource(newResourceUri4);

			newRes1.getContents().add(testApplication20);
			newRes2.getContents().add(testPlatform);
			newRes3.getContents().add(testApplication10);
			newRes4.getContents().add(umlModel);

			EObject testEObject = newRes1.getContents().get(0);
			// Resources in the ResourceSet were returned regardless its MetaModelDescriptor
			resourcesInModel = EcorePlatformUtil.getResourcesInModel(testEObject, true);
			assertEquals(4, resourcesInModel.size());
			assertTrue(resourcesInModel.contains(newRes1));
			assertTrue(resourcesInModel.contains(newRes2));
			assertTrue(resourcesInModel.contains(newRes3));
			assertTrue(resourcesInModel.contains(newRes4));

			testResourceSet.getResources().remove(newRes1);

			resourcesInModel = EcorePlatformUtil.getResourcesInModel(testEObject, true);
			assertEquals(1, resourcesInModel.size());
			assertTrue(resourcesInModel.contains(newRes1));

		}
	}

	public void testGetResourceInModel_NullObject() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();
		Object nullObject = null;
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(nullObject, true);
		assertTrue(resourcesInModel.isEmpty());

	}

	public void testGetResourcesInModel_AnyObject() {
		Collection<Resource> resourcesInModel = new ArrayList<Resource>();

		IProject contextProject = refWks.hbProject10_A;
		assertNotNull(contextProject);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(contextProject, true);
		assertTrue(resourcesInModel.isEmpty());

		IWorkspaceRoot wsRoot = EcorePlugin.getWorkspaceRoot();
		assertNotNull(wsRoot);
		resourcesInModel = EcorePlatformUtil.getResourcesInModel(wsRoot, true);
		assertTrue(resourcesInModel.isEmpty());

	}

	/**
	 * Test method for {@link EcorePlatformUtil#getResourcesInScope(Object, boolean)}
	 */
	public void testGetResourceInScope_File() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();

		int resourcesInEditingDomain20 = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

		IFile hbFile20_D_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1);
		assertNotNull(hbFile20_D_1);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbFile20_D_1, true);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		IFile hbFile20_E_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertNotNull(hbFile20_E_1);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbFile20_E_1, false);
		assertEquals(resource20FromHbProject20_E, resourcesInScope.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbFile20_E_1, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		// ==================================================
		// Create Hummingbird10 Resource in EditingDomain20
		Application hbApplication10 = Hummingbird10Factory.eINSTANCE.createApplication();
		IPath newResourcePath = refWks.hbProject20_E.getFullPath().append("1.hummingbird");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath, Hummingbird10Package.eCONTENT_TYPE, hbApplication10, false,
				null);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20 + 1);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbFile20_E_1, false);
		// Hummingbird 10 Resources must be rejected. Because of
		// org.eclipse.sphinx.examples.hummingbird.ide.scoping.HummingbirdProjectResourceScope.isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(IFile,
		// IMetaModelDescriptor)
		assertEquals(resource20FromHbProject20_E, resourcesInScope.size());

		IFile testHb10File = refWks.hbProject20_E.getFile("1.hummingbird");
		assertNotNull(testHb10File);
		testHb10File.isAccessible();

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(testHb10File, false);
		assertEquals(0, resourcesInScope.size());
		// --------------------------------------------------
		// Create UML2 resource in EditingDomain20
		Model model = UMLFactory.eINSTANCE.createModel();

		IPath newResourcePath2 = refWks.hbProject20_E.getFullPath().append("2.uml");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath2, UMLPackage.eCONTENT_TYPE, model, false, null);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20 + 2);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbFile20_E_1, false);

		assertEquals(resource20FromHbProject20_E + 1, resourcesInScope.size());
		// ======================================

		// File is unexisting
		assertNotNull(refWks.hbProject10_A);
		IFile unexistingFile = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		assertNotNull(unexistingFile);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(unexistingFile, true);
		assertEquals(0, resourcesInScope.size());
		// Null File
		IFile nullFile = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_B,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10B_1);
		assertNull(nullFile);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(nullFile, true);
		assertEquals(0, resourcesInScope.size());
		// Non model file
		IFile projectFile = refWks.hbProject10_A.getFile(".project");
		assertNotNull(projectFile);
		assertTrue(projectFile.isAccessible());
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(projectFile, true);
		assertEquals(0, resourcesInScope.size());
	}

	public void testGetResourceInScope_Resource() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();

		// --Hummingbird20 Model
		// Resource in root project
		Resource hbResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		assertNotNull(hbResource20_D_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource20_D_1, false);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource20_D_1, true);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		// ---------------------------------------------
		// File in referencing project
		Resource hbResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(hbResource20_E_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource20_E_1, false);
		assertEquals(resource20FromHbProject20_E, resourcesInScope.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource20_E_1, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));

		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		Resource hbResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		assertNotNull(hbResource10_D_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource10_D_1, false);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource10_D_1, true);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// -----------------------------------------------
		// File in referencing project
		Resource hbResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(hbResource10_E_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource10_E_1, false);
		assertEquals(resources10FromHbProject10_E, resourcesInScope.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource10_E_1, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		// =====================================================
		// --Uml model

		// File in root project
		Resource uml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(uml2Resource20_D_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Resource20_D_1, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Resource20_D_1, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// ----------------------------------------
		// File in referencing project
		Resource uml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
		assertNotNull(uml2Resource20_E_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Resource20_E_1, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Resource20_E_1, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		// ==================================================
		// Create Hummingbird10 Resource in EditingDomain20
		int resourcesInEditingDomain20Count = refWks.editingDomain20.getResourceSet().getResources().size();

		Application hbApplication10 = Hummingbird10Factory.eINSTANCE.createApplication();
		IPath newResourcePath = refWks.hbProject20_E.getFullPath().append("1.hummingbird");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath, Hummingbird10Package.eCONTENT_TYPE, hbApplication10, false,
				null);
		resourcesInEditingDomain20Count++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count);

		Resource newHb10Resource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(newResourcePath.toString(), true), false);
		assertNotNull(newHb10Resource);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource20_E_1, false);
		// Hummingbird 10 Resources must be rejected. Because of
		// org.eclipse.sphinx.examples.hummingbird.ide.scoping.HummingbirdProjectResourceScope.isResourceVersionCorrespondingToMetaModelVersionOfEnclosingProject(IFile,
		// IMetaModelDescriptor)
		assertEquals(resource20FromHbProject20_E, resourcesInScope.size());

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(newHb10Resource, false);
		// In case the resource doesnot belong to any modeldescriptor, resources in resourceSet will be return
		assertEquals(resourcesInEditingDomain20Count, resourcesInScope.size());
		// --------------------------------------------------
		// Create UML2 resource in EditingDomain20
		Model model = UMLFactory.eINSTANCE.createModel();

		IPath newResourcePath2 = refWks.hbProject20_E.getFullPath().append("2.uml");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath2, UMLPackage.eCONTENT_TYPE, model, false, null);

		resourcesInEditingDomain20Count++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count);

		Resource newUml2Resource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(newResourcePath2.toString(), true), false);
		assertNotNull(newUml2Resource);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbResource20_E_1, false);
		// Only the Hummingbird 2.0 resources that match the Hummingbird metamodel version of the context project plus
		// the additional UML2 resource must be returned
		assertEquals(resource20FromHbProject20_E + 1, resourcesInScope.size());

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(newUml2Resource, false);
		// In case the resource does not belong to any model, all resources in underlying resource set must be returned,
		// i.e., all Hummingbird 2.0 resources in the workspace plus additional Hummingbird 1.0 resource and additional
		// UML2 resource
		assertEquals(resourcesInEditingDomain20Count, resourcesInScope.size());

		// ===================================================
		// Null Resource
		Resource nullResource = null;
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(nullResource, true);
		assertEquals(0, resourcesInScope.size());
	}

	public void testGetResourceInScope_ModelDescriptor() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();

		// --Hummingbird20 Model
		// Resource in root project
		Resource hbResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		assertNotNull(hbResource20_D_1);

		IModelDescriptor hbModelDescriptor20D = ModelDescriptorRegistry.INSTANCE.getModel(hbResource20_D_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescriptor20D, false);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescriptor20D, true);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		Resource hbResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(hbResource20_E_1);
		IModelDescriptor hbModelDescriptor20E = ModelDescriptorRegistry.INSTANCE.getModel(hbResource20_E_1);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescriptor20E, false);
		assertEquals(resource20FromHbProject20_E, resourcesInScope.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescriptor20E, true);
		assertEquals(resource20FromHbProject20_E + resource20FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		Resource hbResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		assertNotNull(hbResource10_D_1);

		IModelDescriptor hbModelDescripor10D = ModelDescriptorRegistry.INSTANCE.getModel(hbResource10_D_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescripor10D, false);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescripor10D, true);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		Resource hbResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(hbResource10_E_1);

		IModelDescriptor hbModelDescripor10E = ModelDescriptorRegistry.INSTANCE.getModel(hbResource10_E_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescripor10E, false);
		assertEquals(resources10FromHbProject10_E, resourcesInScope.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescripor10E, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));
		// =====================================================
		// --Uml model
		// File in root project
		Resource uml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(uml2Resource20_D_1);
		IModelDescriptor uml2ModelDescripor10D = ModelDescriptorRegistry.INSTANCE.getModel(uml2Resource20_D_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2ModelDescripor10D, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2ModelDescripor10D, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		Resource uml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
		assertNotNull(uml2Resource20_E_1);

		IModelDescriptor uml2ModelDescripor20E = ModelDescriptorRegistry.INSTANCE.getModel(uml2Resource20_E_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2ModelDescripor20E, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2ModelDescripor20E, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		// ==================================================
		// Create Hummingbird10 Resource in EditingDomain20
		int resourcesInEditingDomain20Count = refWks.editingDomain20.getResourceSet().getResources().size();

		Application hbApplication10 = Hummingbird10Factory.eINSTANCE.createApplication();
		IPath newResourcePath = refWks.hbProject20_E.getFullPath().append("1.hummingbird");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath, Hummingbird10Package.eCONTENT_TYPE, hbApplication10, false,
				null);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count + 1);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescriptor20E, true);
		assertEquals(resource20FromHbProject20_E + resource20FromHbProject20_D, resourcesInScope.size());

		// --------------------------------------------------
		// Create UML2 resource in EditingDomain20
		Model model = UMLFactory.eINSTANCE.createModel();

		IPath newResourcePath2 = refWks.hbProject20_E.getFullPath().append("2.uml");
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath2, UMLPackage.eCONTENT_TYPE, model, false, null);
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourcesInEditingDomain20Count + 2);

		Resource newUml2Resource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(newResourcePath2.toString(), true), false);
		assertNotNull(newUml2Resource);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbModelDescriptor20E, true);
		assertEquals(resource20FromHbProject20_E + 1 + resource20FromHbProject20_D, resourcesInScope.size());
		resourcesInScope.contains(newUml2Resource);

	}

	public void testGetResourceInScope_URI() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();

		// --Hummingbird20 Model
		// Resource in root project
		URI hbUri20D_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri20D_1, false);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri20D_1, true);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		URI hbUri20E_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri20E_1, false);
		assertEquals(resource20FromHbProject20_E, resourcesInScope.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri20E_1, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		URI hbUri10D_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri10D_1, false);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri10D_1, true);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		URI hbUri10E_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
				+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri10E_1, false);
		assertEquals(resources10FromHbProject10_E, resourcesInScope.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(hbUri10E_1, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		// URI of EObject
		IFile hbFile20E_1 = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E,
				DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1);
		assertNotNull(hbFile20E_1);

		Resource resource = EcorePlatformUtil.getResource(hbFile20E_1);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject contextObject = resource.getContents().get(0);
		assertNotNull(contextObject);
		URI testUri = EcoreUtil.getURI(contextObject);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(testUri, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));

		// =====================================================
		// --Uml model
		// File in root project
		URI uml2Uri20D_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
				+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true);
		assertNotNull(uml2Uri20D_1);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Uri20D_1, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Uri20D_1, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		URI uml2Uri20E_1 = URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
				+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Uri20E_1, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(uml2Uri20E_1, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

	}

	public void testGetResourceInScope_EObject() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();
		EObject contextObject = null;
		// --Hummingbird20 Model
		// Resource in root project
		Resource hbResource20_D_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1, true), false);
		assertNotNull(hbResource20_D_1);

		assertFalse(hbResource20_D_1.getContents().isEmpty());
		contextObject = hbResource20_D_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, false);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		Collection<String> resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, true);
		assertEquals(resource20FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		// File in referencing project
		Resource hbResource20_E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);
		assertNotNull(hbResource20_E_1);

		assertFalse(hbResource20_E_1.getContents().isEmpty());
		contextObject = hbResource20_E_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, false);
		assertEquals(resource20FromHbProject20_E, resourcesInScope.size());

		Collection<String> resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, true);
		assertEquals(resource20FromHbProject20_D + resource20FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		// ======================================
		// --Hummingbird10 Model
		// resource in root project
		Resource hbResource10_D_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1, true), false);
		assertNotNull(hbResource10_D_1);

		assertFalse(hbResource10_D_1.getContents().isEmpty());
		contextObject = hbResource10_D_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, false);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		Collection<String> resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, true);
		assertEquals(resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));

		assertFalse(resourceNameInScope10D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		// File in referencing project
		Resource hbResource10_E_1 = refWks.editingDomain10.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1, true), false);
		assertNotNull(hbResource10_E_1);

		assertFalse(hbResource10_E_1.getContents().isEmpty());
		contextObject = hbResource10_E_1.getContents().get(0);
		assertNotNull(contextObject);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, false);
		assertEquals(resources10FromHbProject10_E, resourcesInScope.size());

		Collection<String> resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertFalse(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, true);
		assertEquals(resources10FromHbProject10_E + resources10FromHbProject10_D, resourcesInScope.size());

		resourceNameInScope10E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10E_3));

		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_1));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_2));
		assertTrue(resourceNameInScope10E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10D_3));
		// =====================================================
		// --Uml model
		// File in root project
		Resource uml2Resource20_D_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1, true), false);
		assertNotNull(uml2Resource20_D_1);

		assertFalse(uml2Resource20_D_1.getContents().isEmpty());
		contextObject = uml2Resource20_D_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, false);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20D = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, true);
		assertEquals(resourcesUml2FromHbProject20_D, resourcesInScope.size());

		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertFalse(resourceNameInScope20D.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		// File in referencing project
		Resource uml2Resource20_E_1 = refWks.editingDomainUml2.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1, true), false);
		assertNotNull(uml2Resource20_E_1);

		assertFalse(uml2Resource20_E_1.getContents().isEmpty());
		contextObject = uml2Resource20_E_1.getContents().get(0);
		assertNotNull(contextObject);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, false);
		assertEquals(resourcesUml2FromHbProject20_E, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextObject, true);
		assertEquals(resourcesUml2FromHbProject20_E + resourcesUml2FromHbProject20_D, resourcesInScope.size());

		resourceNameInScope20E = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3));

		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_1));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_2));
		assertTrue(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.UML2_FILE_NAME_20D_3));

		assertFalse(resourceNameInScope20E.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));

	}

	public void testGetResourceInScope_EObject_WithoutUnderlyingResource() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();

		// Context Object without resource
		org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application testApplication = InstanceModel20Factory.eINSTANCE.createApplication();
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(testApplication, true);
		assertEquals(0, resourcesInScope.size());

		// Context Object without underlying resource
		// Create new resource
		String newResourceName = "ResourceInMemoryOnly.instancemodel";
		IPath newResourcePath = refWks.hbProject20_E.getFullPath().append(newResourceName);
		int resourceInEditingDomain20Count = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

		// Add new resource to EditingDomain
		EcorePlatformUtil.addNewModelResource(refWks.editingDomain20, newResourcePath, Hummingbird20MMDescriptor.INSTANCE.getDefaultContentTypeId(),
				testApplication, false, null);
		resourceInEditingDomain20Count++;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20Count);
		// Verify that underlying file is not exist
		IFile file = refWks.getReferenceFile(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E, newResourceName);
		assertNull(file);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(testApplication, false);
		assertEquals(resource20FromHbProject20_E + 1, resourcesInScope.size());
		Collection<String> resourceNameInScope = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));
		assertTrue(resourceNameInScope.contains(newResourceName));

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(testApplication, true);
		assertEquals(resource20FromHbProject20_E + resource20FromHbProject20_D + 1, resourcesInScope.size());
		resourceNameInScope = getResourceNames(resourcesInScope);
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_2));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_3));

		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_1));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2));
		assertTrue(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3));
		assertTrue(resourceNameInScope.contains(newResourceName));
	}

	public void testGetResourceInScope_EObject_InResource_WithoutResourceSet() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();
		// Resource without resourceSet but belong to a Model
		int resourceInEditingDomain20Count = refWks.getInitialResourcesInReferenceEditingDomainCount(Hummingbird20MMDescriptor.INSTANCE);

		Resource testResource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(testResource);

		ResourceSet testResourceSet = testResource.getResourceSet();
		testResourceSet.getResources().remove(testResource);

		resourceInEditingDomain20Count--;
		assertEditingDomainResourcesSizeEquals(refWks.editingDomain20, resourceInEditingDomain20Count);

		assertNotNull(testResource);
		assertNull(testResource.getResourceSet());
		EObject testEObject = testResource.getContents().get(0);

		resourcesInScope = EcorePlatformUtil.getResourcesInScope(testEObject, true);
		assertNotNull(resourcesInScope);
		assertEquals(1, resourcesInScope.size());

		Collection<String> resourceNameInScope = getResourceNames(resourcesInScope);
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2));
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3));
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_4));
		assertFalse(resourceNameInScope.contains(DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4));
	}

	public void testGetResourceInScope_EObject_InResourceSet_WithoutEditingDomain() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();

		// New resources in new ResourceSet
		{
			ResourceSetImpl testResourceSet = new ScopingResourceSetImpl();
			org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application testApplication20 = InstanceModel20Factory.eINSTANCE
					.createApplication();
			Application testApplication10 = Hummingbird10Factory.eINSTANCE.createApplication();
			Platform testPlatform = TypeModel20Factory.eINSTANCE.createPlatform();
			Model umlModel = UMLFactory.eINSTANCE.createModel();

			URI newResourceUri1 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/1.instancemodel", true);
			URI newResourceUri2 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/2.typemodel", true);
			URI newResourceUri3 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/3.hummingbird", true);
			URI newResourceUri4 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/4.uml", true);
			Resource newRes1 = testResourceSet.createResource(newResourceUri1);
			Resource newRes2 = testResourceSet.createResource(newResourceUri2);
			Resource newRes3 = testResourceSet.createResource(newResourceUri3);
			Resource newRes4 = testResourceSet.createResource(newResourceUri4);

			newRes1.getContents().add(testApplication20);
			newRes2.getContents().add(testPlatform);
			newRes3.getContents().add(testApplication10);
			newRes4.getContents().add(umlModel);

			EObject testEObject = newRes1.getContents().get(0);
			// Resources in the ResourceSet were returned regardless its MetaModelDescriptor
			resourcesInScope = EcorePlatformUtil.getResourcesInScope(testEObject, true);
			assertEquals(4, resourcesInScope.size());
			assertTrue(resourcesInScope.contains(newRes1));
			assertTrue(resourcesInScope.contains(newRes2));
			assertTrue(resourcesInScope.contains(newRes3));
			assertTrue(resourcesInScope.contains(newRes4));

			testResourceSet.getResources().remove(newRes1);

			resourcesInScope = EcorePlatformUtil.getResourcesInScope(testEObject, true);
			assertEquals(1, resourcesInScope.size());
			assertTrue(resourcesInScope.contains(newRes1));

		}
	}

	public void testGetResourceInScope_NullObject() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();
		Object nullObject = null;
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(nullObject, true);
		assertTrue(resourcesInScope.isEmpty());

	}

	public void testGetResourcesInScope_AnyObject() {
		Collection<Resource> resourcesInScope = new ArrayList<Resource>();

		IProject contextProject = refWks.hbProject10_A;
		assertNotNull(contextProject);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(contextProject, true);
		assertTrue(resourcesInScope.isEmpty());

		IWorkspaceRoot wsRoot = EcorePlugin.getWorkspaceRoot();
		assertNotNull(wsRoot);
		resourcesInScope = EcorePlatformUtil.getResourcesInScope(wsRoot, true);
		assertTrue(resourcesInScope.isEmpty());

	}

	protected Collection<String> getResourceNames(Collection<Resource> resources) {
		Collection<String> resourceNames = new ArrayList<String>();
		for (Resource res : resources) {
			resourceNames.add(res.getURI().lastSegment());
		}
		return resourceNames;
	}
}
