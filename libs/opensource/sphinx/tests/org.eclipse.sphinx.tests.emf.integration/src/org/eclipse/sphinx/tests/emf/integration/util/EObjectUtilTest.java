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
 *     itemis - [400897] ExtendedResourceAdapter's approach of reflectively clearing all EObject fields when performing memory-optimized unloads bears the risk of leaving some EObjects leaked
 *     itemis - Fixed EObjectUtilTest that was failing since server infrastructure upgrade at Eclipse Foundation
 *     itemis - [410825] Make sure that EcorePlatformUtil#getResourcesInModel(contextResource, includeReferencedModels) method return resources of the context resource in the same resource set
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapter;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.ComponentTypeImpl;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.impl.PortImpl;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;

@SuppressWarnings("nls")
public class EObjectUtilTest extends DefaultIntegrationTestCase {

	private int hbProject10_A_HummingbirdObjectCount = 0;
	private int hbProject20_B_Uml2ObjectCount = 0;

	private List<String> hbProject10AResources10;
	private int resources10FromHbProject10_A;

	private List<String> hbProject20AResources20;

	private List<String> hbProject20BResourcesUml2;
	private int resourcesUml2FromHbProject20_B;

	private List<String> hbProject20CResources20;

	public EObjectUtilTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// Remove all project references except:
		// HB_PROJECT_NAME_20_E -> HB_PROJECT_NAME_20_D
		Map<String, Set<String>> projectReferences = getProjectReferences();
		projectReferences.remove(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectReferences.remove(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		hbProject10AResources10 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A,
				Hummingbird10MMDescriptor.INSTANCE);
		resources10FromHbProject10_A = hbProject10AResources10.size();

		hbProject20AResources20 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A,
				Hummingbird20MMDescriptor.INSTANCE);

		hbProject20BResourcesUml2 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B, UML2MMDescriptor.INSTANCE);
		resourcesUml2FromHbProject20_B = hbProject20BResourcesUml2.size();

		hbProject20CResources20 = refWks.getReferenceFileNames(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C,
				Hummingbird20MMDescriptor.INSTANCE);

		hbProject10_A_HummingbirdObjectCount = resources10FromHbProject10_A;
		hbProject20_B_Uml2ObjectCount = resourcesUml2FromHbProject20_B;
	}

	private int getNumberOfHB20fApplicationInstances(Collection<String> resourceNames) {
		int res = 0;
		for (Resource resource : refWks.editingDomain20.getResourceSet().getResources()) {
			if (resourceNames.contains(resource.getURI().lastSegment())) {
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				if (modelRoot instanceof Application) {
					res++;
				}
			}
		}
		return res;
	}

	private int getNumberOfHB20ComponentInstances(Collection<String> resourceNames) {
		int res = 0;
		for (Resource resource : refWks.editingDomain20.getResourceSet().getResources()) {
			if (resourceNames.contains(resource.getURI().lastSegment())) {
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				if (modelRoot instanceof Application) {
					Application app = (Application) modelRoot;
					res += app.getComponents().size();
				}
			}
		}
		return res;
	}

	private int getNumberOfHB20InterfaceInstances(Collection<String> resourceNames) {
		int res = 0;
		for (Resource resource : refWks.editingDomain20.getResourceSet().getResources()) {
			if (resourceNames.contains(resource.getURI().lastSegment())) {
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				if (modelRoot instanceof Platform) {
					Platform platform = (Platform) modelRoot;
					res += platform.getInterfaces().size();
				}
			}
		}
		return res;
	}

	private int getNumberOfHB20PlatfromInstances(Collection<String> resourceNames) {
		int res = 0;
		for (Resource resource : refWks.editingDomain20.getResourceSet().getResources()) {
			if (resourceNames.contains(resource.getURI().lastSegment())) {
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				if (modelRoot instanceof Platform) {
					res++;
				}
			}
		}
		return res;
	}

	private int getNumberOfHB10ComponentInstances(Collection<String> resourceNames) {
		int res = 0;
		for (Resource resource : refWks.editingDomain10.getResourceSet().getResources()) {
			if (resourceNames.contains(resource.getURI().lastSegment())) {
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				if (modelRoot instanceof org.eclipse.sphinx.examples.hummingbird10.Application) {
					org.eclipse.sphinx.examples.hummingbird10.Application app = (org.eclipse.sphinx.examples.hummingbird10.Application) modelRoot;
					res += app.getComponents().size();
				}
			}
		}
		return res;
	}

	private int getNumberOfHB10ApplicationiInstances(Collection<String> resourceNames) {
		int res = 0;
		for (Resource resource : refWks.editingDomain10.getResourceSet().getResources()) {
			if (resourceNames.contains(resource.getURI().lastSegment())) {
				assertFalse(resource.getContents().isEmpty());
				EObject modelRoot = resource.getContents().get(0);
				if (modelRoot instanceof org.eclipse.sphinx.examples.hummingbird10.Application) {
					res++;
				}
			}
		}
		return res;
	}

	/**
	 * Test for method {@link EObjectUtil#getAllInstancesOf(EObject, EReference, boolean)}
	 */
	public void testGetAllInstancesOf_EObject_EReference() {
		// HB20 Object
		Resource resource20_A_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);

		assertNotNull(resource20_A_1);
		assertFalse(resource20_A_1.getContents().isEmpty());

		EObject hb20ModelRoot_20A_1 = resource20_A_1.getContents().get(0);

		assertEquals(getNumberOfHB20ComponentInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20A_1, InstanceModel20Package.eINSTANCE.getApplication_Components(), true).size());

		assertEquals(getNumberOfHB20fApplicationInstances(hbProject20AResources20), EObjectUtil
				.getAllInstancesOf(hb20ModelRoot_20A_1, InstanceModel20Package.eINSTANCE.getApplication().getInstanceClass(), false).size());

		assertEquals(getNumberOfHB20InterfaceInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20A_1, TypeModel20Package.eINSTANCE.getPlatform_Interfaces(), true).size());

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20A_1, TypeModel20Package.eINSTANCE.getPlatform().getInstanceClass(), true).size());

		assertEquals(0,
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20A_1, InstanceModel20Package.eINSTANCE.getComponent_IncomingConnections(), true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(hb20ModelRoot_20A_1, Hummingbird10Package.eINSTANCE.getApplication_Components(), true).size());
		// --------------------------------
		Resource resource20_C_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, true), false);

		assertNotNull(resource20_C_1);
		assertFalse(resource20_C_1.getContents().isEmpty());
		EObject hb20ModelRoot_20_C_1 = resource20_C_1.getContents().get(0);

		assertEquals(getNumberOfHB20ComponentInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20_C_1, InstanceModel20Package.eINSTANCE.getApplication_Components(), true).size());

		assertEquals(getNumberOfHB20fApplicationInstances(hbProject20CResources20), EObjectUtil
				.getAllInstancesOf(hb20ModelRoot_20_C_1, InstanceModel20Package.eINSTANCE.getApplication().getInstanceClass(), true).size());

		assertEquals(getNumberOfHB20InterfaceInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20_C_1, TypeModel20Package.eINSTANCE.getPlatform_Interfaces(), true).size());

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20_C_1, TypeModel20Package.eINSTANCE.getPlatform().getInstanceClass(), true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(hb20ModelRoot_20_C_1, TypeModel20Package.eINSTANCE.getComponentType_Parameters(), true).size());

		assertEquals(0,
				EObjectUtil.getAllInstancesOf(hb20ModelRoot_20_C_1, Hummingbird10Package.eINSTANCE.getApplication().getInstanceClass(), true).size());
		// =================================================
		// HB10 Object
		Resource resource10_A = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);

		assertNotNull(resource10_A);
		assertFalse(resource10_A.getContents().isEmpty());

		EObject hb10ModelRoot_10A_1 = resource10_A.getContents().get(0);

		assertEquals(getNumberOfHB10ComponentInstances(hbProject10AResources10),
				EObjectUtil.getAllInstancesOf(hb10ModelRoot_10A_1, Hummingbird10Package.eINSTANCE.getApplication_Components(), true).size());

		assertEquals(getNumberOfHB10ApplicationiInstances(hbProject10AResources10),
				EObjectUtil.getAllInstancesOf(hb10ModelRoot_10A_1, Hummingbird10Package.eINSTANCE.getApplication().getInstanceClass(), true).size());

		assertEquals(0,
				EObjectUtil.getAllInstancesOf(hb10ModelRoot_10A_1, TypeModel20Package.eINSTANCE.getComponentType().getInstanceClass(), true).size());

	}

	/**
	 * Test method for {@link EObjectUtil#getAllInstancesOf(EObject, Class, boolean)}
	 */
	public void testGetAllInstancesOf_EObject_Class() {

		Resource resource20 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);

		assertNotNull(resource20);
		assertFalse(resource20.getContents().isEmpty());
		EObject hb20ModelRoot = resource20.getContents().get(0);
		assertTrue(hb20ModelRoot instanceof Application);
		Application contextApplication = (Application) hb20ModelRoot;
		assertFalse(contextApplication.getComponents().isEmpty());
		Component contextComponent = contextApplication.getComponents().get(0);

		assertEquals(getNumberOfHB20fApplicationInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot, Application.class, false).size());

		assertEquals(getNumberOfHB20ComponentInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot, Component.class, true).size());
		assertEquals(getNumberOfHB20ComponentInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(contextComponent, Component.class, true).size());

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot, Platform.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(hb20ModelRoot, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		// ------------------------------------------------------------
		resource20 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, true), false);

		assertNotNull(resource20);
		assertFalse(resource20.getContents().isEmpty());
		hb20ModelRoot = resource20.getContents().get(0);

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(hb20ModelRoot, Platform.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(hb20ModelRoot, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		// =========================================================
		Resource resource10_A = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		assertNotNull(resource10_A);
		assertFalse(resource10_A.getContents().isEmpty());

		EObject hb10ModelRoot = resource10_A.getContents().get(0);
		assertTrue(hb10ModelRoot instanceof org.eclipse.sphinx.examples.hummingbird10.Application);

		assertEquals(hbProject10_A_HummingbirdObjectCount,
				EObjectUtil.getAllInstancesOf(hb10ModelRoot, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(hb10ModelRoot, Application.class, true).size());
		// ===========================================================
		// UML2 resource
		Resource resourceUml2_20C = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, true), false);

		assertNotNull(resourceUml2_20C);
		assertFalse(resourceUml2_20C.getContents().isEmpty());
		EObject uml2ModelRoot = resourceUml2_20C.getContents().get(0);

		assertEquals(hbProject20_B_Uml2ObjectCount, EObjectUtil.getAllInstancesOf(uml2ModelRoot, Model.class, true).size());

		assertEquals(hbProject20_B_Uml2ObjectCount * 2, EObjectUtil.getAllInstancesOf(uml2ModelRoot, Package.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(uml2ModelRoot, Application.class, true).size());

		// ==============================================================
		// Context Object doesn't belong to any resource
		Application hb20Application = createHb20Application();

		assertEquals(2, EObjectUtil.getAllInstancesOf(hb20Application, Component.class, true).size());

		contextComponent = hb20Application.getComponents().get(0);
		assertEquals(2, EObjectUtil.getAllInstancesOf(contextComponent, Component.class, true).size());
		// ==============================================================
		// Not exact match
		assertEquals(3, EObjectUtil.getAllInstancesOf(hb20Application, Identifiable.class, false).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(hb20Application.getComponents().get(0), Identifiable.class, false).size());
		// ==============================================================
		// Context Object belongs to resource without resourceSet
		Resource contextResource = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(contextResource);
		assertFalse(contextResource.getContents().isEmpty());
		EObject contextObject = contextResource.getContents().get(0);
		assertNotNull(contextObject);
		// Remove contextResource from ResourceSet
		refWks.editingDomain20.getResourceSet().getResources().remove(contextResource);
		assertNull(contextResource.getResourceSet());
		hbProject20AResources20.remove(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertFalse(contextResource.getContents().isEmpty());
		hb20ModelRoot = contextResource.getContents().get(0);
		assertNotNull(hb20ModelRoot);

		assertEquals(1, EObjectUtil.getAllInstancesOf(hb20ModelRoot, Component.class, true).size());
		// ==============================================================
		// Context Object belong to resource in ResourceSet without EditingDomain
		ResourceSetImpl testResourceSet = new ScopingResourceSetImpl();

		URI newResourceUri1 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/1.instancemodel", true);
		URI newResourceUri2 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/2.instancemodel", true);
		URI newResourceUri3 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/3.instancemodel", true);
		Resource newRes1 = testResourceSet.createResource(newResourceUri1);
		Resource newRes2 = testResourceSet.createResource(newResourceUri2);
		Resource newRes3 = testResourceSet.createResource(newResourceUri3);

		newRes1.getContents().add(createHb20Application());
		newRes2.getContents().add(createHb20Application());
		newRes3.getContents().add(createHb20Application());

		assertFalse(newRes1.getContents().isEmpty());
		EObject contextEObject = newRes1.getContents().get(0);
		assertNotNull(contextEObject);

		assertEquals(6, EObjectUtil.getAllInstancesOf(contextEObject, Component.class, true).size());
		// ==============================================================
		// Input context Object is NULL
		try {
			EObject nullObject = null;
			assertEquals(0, EObjectUtil.getAllInstancesOf(nullObject, Component.class, true).size());
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when contextObject is NULL" + ex.getClass().getSimpleName() + " " + ex.getMessage());
			}
		}
		// ==============================================================
		// Given class is null
		try {
			Class<?> nullClass = null;
			assertEquals(0, EObjectUtil.getAllInstancesOf(hb20ModelRoot, nullClass, true).size());
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when given type is NULL" + ex.getClass().getSimpleName() + " " + ex.getMessage());
			}
		}

	}

	/**
	 * Test method for {@link EObjectUtil#getAllInstancesOf(Resource, Class, boolean)}
	 */
	public void testGetAllInstancesOf_Resource() {

		Resource resource20_A = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		assertNotNull(resource20_A);

		assertEquals(getNumberOfHB20fApplicationInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(resource20_A, Application.class, false).size());

		assertEquals(getNumberOfHB20ComponentInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(resource20_A, Component.class, true).size());

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(resource20_A, Platform.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(resource20_A, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		// ------------------------------------------------------------
		Resource resource20_C = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20C_1, true), false);

		assertNotNull(resource20_C);

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(resource20_C, Platform.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(resource20_C, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		// =========================================================
		Resource resource10_A = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		assertNotNull(resource10_A);

		assertEquals(getNumberOfHB10ComponentInstances(hbProject10AResources10),
				EObjectUtil.getAllInstancesOf(resource10_A, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(resource10_A, Application.class, true).size());
		// ===========================================================
		// UML2 resource
		Resource resourceUml2_20C = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, true), false);

		assertNotNull(resourceUml2_20C);

		assertEquals(hbProject20_B_Uml2ObjectCount, EObjectUtil.getAllInstancesOf(resourceUml2_20C, Model.class, true).size());

		assertEquals(hbProject20_B_Uml2ObjectCount * 2, EObjectUtil.getAllInstancesOf(resourceUml2_20C, Package.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(resourceUml2_20C, Application.class, true).size());
		// ==============================================================
		// Resource without ResourceSet
		ResourceSet resourceSet = resource20_A.getResourceSet();
		resourceSet.getResources().remove(resource20_A);
		hbProject20AResources20.remove(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);

		assertEquals(1, EObjectUtil.getAllInstancesOf(resource20_A, Application.class, true).size());

		assertEquals(1, EObjectUtil.getAllInstancesOf(resource20_A, Component.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(resource20_A, Platform.class, true).size());
		// ==============================================================
		// Resource in ResourceSet without EditingDomain

		ResourceSetImpl testResourceSet = new ScopingResourceSetImpl();

		URI newResourceUri1 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/1.instancemodel", true);
		URI newResourceUri2 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/2.instancemodel", true);
		URI newResourceUri3 = URI.createURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C + "/3.instancemodel", true);
		Resource newRes1 = testResourceSet.createResource(newResourceUri1);
		Resource newRes2 = testResourceSet.createResource(newResourceUri2);
		Resource newRes3 = testResourceSet.createResource(newResourceUri3);

		newRes1.getContents().add(createHb20Application());
		newRes2.getContents().add(createHb20Application());
		newRes3.getContents().add(createHb20Application());

		assertEquals(6, EObjectUtil.getAllInstancesOf(newRes1, Component.class, true).size());

		// ==============================================================
		// Not exact match
		assertEquals(9, EObjectUtil.getAllInstancesOf(newRes1, Identifiable.class, false).size());
		// ==============================================================
		try {
			Resource nullResource = null;
			assertEquals(0, EObjectUtil.getAllInstancesOf(nullResource, Component.class, true).size());
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when contextResource is NULL" + ex.getClass().getSimpleName() + " " + ex.getMessage());
			}
		}
		// ==============================================================
		// Given class is null
		try {
			Class<?> nullClass = null;
			assertEquals(0, EObjectUtil.getAllInstancesOf(resource20_A, nullClass, true).size());
		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				fail("Exception when given type is NULL" + ex.getClass().getSimpleName() + " " + ex.getMessage());
			}
		}

	}

	private Application createHb20Application() {
		Application hb20Application = InstanceModel20Factory.eINSTANCE.createApplication();
		Component component1 = InstanceModel20Factory.eINSTANCE.createComponent();
		component1.setName("Component1");
		Component component2 = InstanceModel20Factory.eINSTANCE.createComponent();
		component2.setName("Component2");

		hb20Application.getComponents().add(component1);
		hb20Application.getComponents().add(component2);
		return hb20Application;
	}

	/**
	 * Test method for {@link EObjectUtil#getAllInstancesOf(IModelDescriptor, Class, boolean)}
	 */
	public void testGetAllInstancesOf_IModelDescriptor() {
		// ModelDescriptor of hbProject20_A
		Collection<IModelDescriptor> hbProjectModels = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A);
		assertNotNull(hbProjectModels);
		assertEquals(1, hbProjectModels.size());

		IModelDescriptor modelDescriptor = hbProjectModels.iterator().next();

		assertEquals(getNumberOfHB20fApplicationInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(modelDescriptor, Application.class, false).size());

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(modelDescriptor, Platform.class, true).size());

		assertEquals(getNumberOfHB20ComponentInstances(hbProject20AResources20),
				EObjectUtil.getAllInstancesOf(modelDescriptor, Component.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(modelDescriptor, Identifiable.class, true).size());
		// Not exact match
		assertTrue(EObjectUtil.getAllInstancesOf(modelDescriptor, Identifiable.class, false).size() > 0);
		// ---------------------------------------
		// ModelDescriptor of hbProject20C
		hbProjectModels = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_C);
		assertNotNull(hbProjectModels);
		assertEquals(1, hbProjectModels.size());
		modelDescriptor = hbProjectModels.iterator().next();

		assertEquals(getNumberOfHB20fApplicationInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(modelDescriptor, Application.class, true).size());

		assertEquals(getNumberOfHB20PlatfromInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(modelDescriptor, Platform.class, true).size());

		assertEquals(getNumberOfHB20ComponentInstances(hbProject20CResources20),
				EObjectUtil.getAllInstancesOf(modelDescriptor, Component.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(modelDescriptor, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(modelDescriptor, Identifiable.class, true).size());
		// Not exact match
		assertTrue(EObjectUtil.getAllInstancesOf(modelDescriptor, Identifiable.class, false).size() > 0);
		// ==================================================
		// Model Descriptor of HB10
		// Model of hbProject10_A
		hbProjectModels = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_A);
		assertNotNull(hbProjectModels);
		assertEquals(1, hbProjectModels.size());
		modelDescriptor = hbProjectModels.iterator().next();

		assertEquals(hbProject10_A_HummingbirdObjectCount,
				EObjectUtil.getAllInstancesOf(modelDescriptor, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());

		assertEquals(getNumberOfHB10ComponentInstances(hbProject10AResources10),
				EObjectUtil.getAllInstancesOf(modelDescriptor, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(modelDescriptor, Application.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(modelDescriptor, EObject.class, true).size());
		// Not exact match
		assertTrue(EObjectUtil.getAllInstancesOf(modelDescriptor, EObject.class, false).size() > 0);
		// ==============================================================
		// Uml2 model
		Collection<IModelDescriptor> uml2ProjectModels = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_B, UML2MMDescriptor.INSTANCE);
		assertNotNull(hbProjectModels);
		assertEquals(1, hbProjectModels.size());

		modelDescriptor = uml2ProjectModels.iterator().next();

		assertEquals(hbProject20_B_Uml2ObjectCount, EObjectUtil.getAllInstancesOf(modelDescriptor, Model.class, true).size());

		assertEquals(hbProject20_B_Uml2ObjectCount * 2, EObjectUtil.getAllInstancesOf(modelDescriptor, Package.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(modelDescriptor, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());

		assertEquals(0, EObjectUtil.getAllInstancesOf(modelDescriptor, EObject.class, true).size());
		// Not exact match
		assertTrue(EObjectUtil.getAllInstancesOf(modelDescriptor, EObject.class, false).size() > 0);
		// ==============================================================
		// Input model is NULL
		ModelDescriptor nullModel = null;
		assertEquals(0, EObjectUtil.getAllInstancesOf(nullModel, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
	}

	/**
	 * Test method for {@link EObjectUtil#getAllInstancesOf(List, Class, boolean)}
	 */
	public void testGetAllInstancesOf_Resources() {
		/* Test variable settings */
		Resource resource20_A_1 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1, true), false);
		Resource resource20_A_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		Resource resource20_A_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);

		assertNotNull(resource20_A_1);
		assertFalse(resource20_A_1.getContents().isEmpty());
		assertNotNull(resource20_A_2);
		assertFalse(resource20_A_2.getContents().isEmpty());
		assertNotNull(resource20_A_3);
		assertFalse(resource20_A_3.getContents().isEmpty());
		// --------------
		Resource resource10_1 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1, true), false);
		Resource resource10_2 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_2, true), false);
		Resource resource10_3 = refWks.editingDomain10.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_3, true), false);

		assertNotNull(resource10_1);
		assertFalse(resource10_1.getContents().isEmpty());
		assertNotNull(resource10_2);
		assertFalse(resource10_2.getContents().isEmpty());
		assertNotNull(resource10_3);
		assertFalse(resource10_3.getContents().isEmpty());
		// --------------------
		Resource resourceUml2_1 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_1, true), false);
		Resource resourceUml2_2 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_2, true), false);
		Resource resourceUml2_3 = refWks.editingDomainUml2.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B + "/" + DefaultTestReferenceWorkspace.UML2_FILE_NAME_20B_3, true), false);

		assertNotNull(resourceUml2_1);
		assertFalse(resourceUml2_1.getContents().isEmpty());
		assertNotNull(resourceUml2_2);
		assertFalse(resourceUml2_2.getContents().isEmpty());
		assertNotNull(resourceUml2_3);
		assertFalse(resourceUml2_3.getContents().isEmpty());
		/* Tests cases execution */
		LinkedList<Resource> resources = new LinkedList<Resource>();
		// Add resource20_A_1
		resources.add(resource20_A_1);
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		// Add resource20_A_2
		resources.add(resource20_A_2);
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		// Add resource20_A_3
		resources.add(resource20_A_3);
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());

		// Add resource10_A_1
		resources.add(resource10_1);
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());

		// Add resource10_A_2
		resources.add(resource10_2);
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());

		// Add resource10_A_3
		resources.add(resource10_3);
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, Model.class, true).size());
		assertEquals(0, EObjectUtil.getAllInstancesOf(resources, Package.class, true).size());

		// Add resourceUml2_1
		resources.add(resourceUml2_1);
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Model.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Package.class, true).size());

		// Add resourceUml2_2
		resources.add(resourceUml2_2);
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Model.class, true).size());
		assertEquals(4, EObjectUtil.getAllInstancesOf(resources, Package.class, true).size());
		// Add resourceUml2_3
		resources.add(resourceUml2_3);
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Component.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, ComponentType.class, true).size());
		assertEquals(1, EObjectUtil.getAllInstancesOf(resources, Platform.class, true).size());
		assertEquals(2, EObjectUtil.getAllInstancesOf(resources, Interface.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Application.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, org.eclipse.sphinx.examples.hummingbird10.Component.class, true).size());
		assertEquals(3, EObjectUtil.getAllInstancesOf(resources, Model.class, true).size());
		assertEquals(6, EObjectUtil.getAllInstancesOf(resources, Package.class, true).size());

	}

	/**
	 * Test method for {@link EObjectUtil#createProxyFrom(EObject, Resource)}
	 */
	public void testCreateProxyFromEObject() {
		Collection<IModelDescriptor> hbProjectModels = ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_A);
		assertNotNull(hbProjectModels);
		assertEquals(1, hbProjectModels.size());

		// Context: EObject is Hummingbird Object
		Resource resource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);

		Resource resource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);

		assertNotNull(resource20_2);
		assertFalse(resource20_2.getContents().isEmpty());
		assertEquals(1, resource20_2.getContents().size());
		Platform hb20Platform_A_2 = (Platform) resource20_2.getContents().get(0);
		assertNotNull(hb20Platform_A_2);
		assertEquals(2, hb20Platform_A_2.getComponentTypes().size());
		assertEquals(2, hb20Platform_A_2.getInterfaces().size());

		ComponentType componentType1 = hb20Platform_A_2.getComponentTypes().get(0);
		ComponentType componentType2 = hb20Platform_A_2.getComponentTypes().get(1);

		Interface interface1 = hb20Platform_A_2.getInterfaces().get(0);
		Interface interface2 = hb20Platform_A_2.getInterfaces().get(1);

		assertFalse(interface1.getProvidingComponentTypes().isEmpty());
		for (ComponentType componentType : interface1.getProvidingComponentTypes()) {
			assertFalse(componentType.eIsProxy());
			assertTrue(hb20Platform_A_2.getComponentTypes().contains(componentType));
		}

		assertFalse(interface2.getRequiringPorts().isEmpty());
		for (Port port : interface2.getRequiringPorts()) {
			assertFalse(port.eIsProxy());
			assertTrue(componentType1.getPorts().contains(port));
		}

		assertFalse(resource20_3.getContents().isEmpty());
		Application hb20Application = (Application) resource20_3.getContents().get(0);
		assertNotNull(hb20Application);
		assertEquals(2, hb20Application.getComponents().size());
		Component referringComponent_1 = hb20Application.getComponents().get(0);
		assertNotNull(referringComponent_1.getType());
		assertFalse(referringComponent_1.getType().eIsProxy());
		assertTrue(hb20Platform_A_2.getComponentTypes().contains(referringComponent_1.getType()));

		Component referringComponent_2 = hb20Application.getComponents().get(1);
		assertNotNull(referringComponent_2.getType());
		assertFalse(referringComponent_2.getType().eIsProxy());
		assertTrue(hb20Platform_A_2.getComponentTypes().contains(referringComponent_2.getType()));

		// Proxify referred objects
		EObject proxy_1 = EObjectUtil.createProxyFrom(componentType1, componentType1.eResource());
		assertNotNull(proxy_1);
		assertTrue(proxy_1.eIsProxy());
		assertTrue(proxy_1 instanceof InternalEObject);

		EObject proxy_2 = EObjectUtil.createProxyFrom(componentType2, componentType2.eResource());
		assertNotNull(proxy_2);
		assertTrue(proxy_2.eIsProxy());
		assertTrue(proxy_2 instanceof InternalEObject);
		assertEquals(URI.createURI("hb:/#//ComponentType2"), ((InternalEObject) proxy_2).eProxyURI());

		assertNotNull(interface1);
		assertFalse(interface1.getProvidingComponentTypes().isEmpty());
		for (ComponentType componentType : interface1.getProvidingComponentTypes()) {
			assertFalse(componentType.eIsProxy());
		}

		assertNotNull(interface2);
		assertFalse(interface2.getRequiringPorts().isEmpty());
		for (Port port : interface2.getRequiringPorts()) {
			assertFalse(port.eIsProxy());
		}

		assertNotNull(referringComponent_1);
		assertNotNull(referringComponent_1.getType());
		assertFalse(referringComponent_1.getType().eIsProxy());

		assertNotNull(referringComponent_2);
		assertNotNull(referringComponent_2.getType());
		assertFalse(referringComponent_2.getType().eIsProxy());

		// ==================================================================
		// Context: EObject is Uml2 object
		for (String fileName : hbProject20BResourcesUml2) {
			IFile file = refWks.hbProject20_B.getFile(fileName);
			Resource resource = EcorePlatformUtil.getResource(file);
			assertNotNull(resource);
			ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
			assertTrue(extendedResource instanceof ExtendedResourceAdapter);
			assertNotNull(resource.getContents());
			assertEquals(1, resource.getContents().size());

			Model uml2Model = (Model) resource.getContents().get(0);
			assertNotNull(uml2Model);

			for (PackageableElement element : uml2Model.getPackagedElements()) {

				EObject proxy = EObjectUtil.createProxyFrom(element, element.eResource());
				assertNotNull(proxy);
				assertTrue(proxy.eIsProxy());

				Element expectedProxy = (Element) UMLFactory.eINSTANCE.create(element.eClass());
				((InternalEObject) expectedProxy).eSetProxyURI(EcoreUtil.getURI(element));
				extendedResource.augmentToContextAwareProxy(expectedProxy);

				assertEquals(((InternalEObject) expectedProxy).eProxyURI(), ((InternalEObject) proxy).eProxyURI());
			}
		}

		// Other context
		// Context: EObject is NULL-> It should have a null assertion
		boolean flag = false;
		try {
			EObjectUtil.createProxyFrom(null, null);
		} catch (AssertionFailedException ex) {
			flag = true;
		}
		assertTrue("No assertion when given Object is NULL", flag);
	}

	/**
	 * Test method for
	 * {@link EObjectUtil#getReferencedInstancesOf(EObject, org.eclipse.emf.ecore.EReference, Class, boolean)}
	 */
	public void testGetReferencedInstancesOf() {
		// Test data
		Resource resource20_A_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		assertNotNull(resource20_A_2);
		assertFalse(resource20_A_2.getContents().isEmpty());
		Platform hb20ModelRoot = (Platform) resource20_A_2.getContents().get(0);
		assertNotNull(hb20ModelRoot);
		assertEquals(2, hb20ModelRoot.getComponentTypes().size());
		assertEquals(2, hb20ModelRoot.getInterfaces().size());
		ComponentType hb20Component = hb20ModelRoot.getComponentTypes().get(0);

		Interface hb20Interface = hb20ModelRoot.getInterfaces().get(0);
		assertFalse(hb20Interface.getRequiringPorts().isEmpty());
		// ------------
		Resource resource20_A_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);
		assertNotNull(resource20_A_3);
		assertFalse(resource20_A_3.getContents().isEmpty());
		Application hb20Application = (Application) resource20_A_3.getContents().get(0);
		assertNotNull(hb20Application);
		assertFalse(hb20Application.getComponents().isEmpty());
		Component component = hb20Application.getComponents().get(0);
		// *****************************************************
		// Context: EReference is Many
		EReference testEReferenceMany_components = (EReference) hb20ModelRoot.eClass().getEStructuralFeature("componentTypes");
		EReference testEReferenceMany_requiringports = (EReference) hb20Interface.eClass().getEStructuralFeature("requiringPorts");

		// --EReference is aggregate
		assertNotNull(testEReferenceMany_components);
		assertTrue(testEReferenceMany_components.isMany());
		List<ComponentType> components = EObjectUtil.getReferencedInstancesOf(hb20ModelRoot, testEReferenceMany_components, ComponentType.class,
				true);
		assertEquals(2, components.size());
		assertEquals(components, hb20ModelRoot.getComponentTypes());
		// --EReference is reference
		assertNotNull(testEReferenceMany_requiringports);
		assertTrue(testEReferenceMany_requiringports.isMany());
		List<Port> rerquiringports = EObjectUtil.getReferencedInstancesOf(hb20Interface, testEReferenceMany_requiringports, Port.class, true);
		assertEquals(1, rerquiringports.size());
		assertTrue(hb20Component.getPorts().containsAll(rerquiringports));

		assertEquals(2, EObjectUtil.getReferencedInstancesOf(hb20ModelRoot, testEReferenceMany_components, ComponentTypeImpl.class, true).size());
		assertEquals(1, EObjectUtil.getReferencedInstancesOf(hb20Interface, testEReferenceMany_requiringports, PortImpl.class, true).size());

		// --Get from accurate class
		assertEquals(0, EObjectUtil.getReferencedInstancesOf(hb20Interface, testEReferenceMany_requiringports, Identifiable.class, true).size());
		assertEquals(0, EObjectUtil.getReferencedInstancesOf(hb20ModelRoot, testEReferenceMany_components, Identifiable.class, true).size());

		// *****************************************************
		// Context: EReference is Unique
		EReference refUnique_Component_Type = InstanceModel20Package.eINSTANCE.getComponent_Type();
		assertFalse(refUnique_Component_Type.isMany());
		assertTrue(EObjectUtil.getReferencedInstancesOf(component, refUnique_Component_Type, ComponentType.class, true).isEmpty());

		// *****************************************************
		// Context: exactMatch is FALSE
		assertEquals(1, EObjectUtil.getReferencedInstancesOf(hb20Interface, testEReferenceMany_requiringports, Identifiable.class, false).size());
		assertEquals(2, EObjectUtil.getReferencedInstancesOf(hb20ModelRoot, testEReferenceMany_components, Identifiable.class, false).size());

		// *****************************************************
		// Context: Given object is NULL
		boolean flag = true;
		String message = "";
		try {
			EObjectUtil.getReferencedInstancesOf(null, testEReferenceMany_requiringports, Port.class, true);

		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				flag = false;
				message = ex.getMessage();
			}
		}
		assertTrue("Exception while owner EObject is Null:" + message, flag);

		flag = true;
		try {
			EObjectUtil.getReferencedInstancesOf(hb20Interface, null, Port.class, true);

		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				flag = false;
				message = ex.getMessage();
			}
		}
		assertTrue("Exception while EReference is Null:" + message, flag);

		flag = true;
		try {
			EObjectUtil.getReferencedInstancesOf(hb20Interface, testEReferenceMany_requiringports, null, true);

		} catch (Exception ex) {
			if (!(ex instanceof AssertionFailedException)) {
				flag = false;
				message = ex.getMessage();
			}
		}
		assertTrue("Exception while specified return Class is Null:" + message, flag);

	}

	/**
	 * Test method for {@link EObjectUtil#proxify(EObject, EStructuralFeature, EObject)}
	 */
	public void testProxify() {
		Resource resource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);

		Resource resource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);

		assertNotNull(resource20_2);
		assertFalse(resource20_2.getContents().isEmpty());
		assertEquals(1, resource20_2.getContents().size());
		Platform hb20Platform_A_2 = (Platform) resource20_2.getContents().get(0);
		assertNotNull(hb20Platform_A_2);
		assertEquals(2, hb20Platform_A_2.getComponentTypes().size());
		assertEquals(2, hb20Platform_A_2.getInterfaces().size());

		ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource20_2);

		ComponentType componentType1 = hb20Platform_A_2.getComponentTypes().get(0);
		URI compTypeUri1 = extendedResource.getURI(componentType1);
		Port port1 = componentType1.getPorts().get(0);
		URI portUri1 = extendedResource.getURI(port1);
		Port port2 = componentType1.getPorts().get(1);
		URI portUri2 = extendedResource.getURI(port2);

		ComponentType componentType2 = hb20Platform_A_2.getComponentTypes().get(1);
		URI compTypeUri2 = extendedResource.getURI(componentType2);

		Interface interface1 = hb20Platform_A_2.getInterfaces().get(0);
		Interface interface2 = hb20Platform_A_2.getInterfaces().get(1);

		assertFalse(interface1.getProvidingComponentTypes().isEmpty());
		for (ComponentType componentType : interface1.getProvidingComponentTypes()) {
			assertFalse(componentType.eIsProxy());
			assertTrue(hb20Platform_A_2.getComponentTypes().contains(componentType));
		}

		assertFalse(interface2.getRequiringPorts().isEmpty());
		for (Port port : interface2.getRequiringPorts()) {
			assertFalse(port.eIsProxy());
			assertTrue(componentType1.getPorts().contains(port));
		}

		assertFalse(resource20_3.getContents().isEmpty());
		Application hb20Application = (Application) resource20_3.getContents().get(0);
		assertNotNull(hb20Application);
		assertEquals(2, hb20Application.getComponents().size());
		Component referringComponent_1 = hb20Application.getComponents().get(0);
		assertNotNull(referringComponent_1.getType());
		assertFalse(referringComponent_1.getType().eIsProxy());
		assertTrue(hb20Platform_A_2.getComponentTypes().contains(referringComponent_1.getType()));

		Component referringComponent_2 = hb20Application.getComponents().get(1);
		assertNotNull(referringComponent_2.getType());
		assertFalse(referringComponent_2.getType().eIsProxy());
		assertTrue(hb20Platform_A_2.getComponentTypes().contains(referringComponent_2.getType()));

		// Proxify ComponentType
		EObject proxy = EObjectUtil.proxify(componentType1.eContainer(), componentType1.eContainmentFeature(), componentType1);
		assertNotNull(proxy);
		assertTrue(proxy.eIsProxy());
		proxy = EObjectUtil.proxify(componentType2.eContainer(), componentType2.eContainmentFeature(), componentType2);
		assertNotNull(proxy);
		assertTrue(proxy.eIsProxy());

		for (ComponentType componentType : interface1.getProvidingComponentTypes()) {
			assertTrue(componentType.eIsProxy());
			assertTrue(componentType instanceof InternalEObject);
			URI proxyUri = ((InternalEObject) componentType).eProxyURI();
			assertTrue(proxyUri.toString(), compTypeUri1.equals(proxyUri) || compTypeUri2.equals(proxyUri));
		}

		assertFalse(interface2.getRequiringPorts().isEmpty());
		for (Port port : interface2.getRequiringPorts()) {
			assertTrue(port.eIsProxy());
			assertTrue(port instanceof InternalEObject);
			URI proxyUri = ((InternalEObject) port).eProxyURI();
			assertTrue(portUri1.equals(proxyUri) || portUri2.equals(proxyUri));
		}
		// Verify resource20_3 contents
		assertNotNull(referringComponent_1.getType());
		assertTrue(referringComponent_1.getType().eIsProxy());
		assertTrue(referringComponent_1.getType() instanceof InternalEObject);
		URI proxyUri = ((InternalEObject) referringComponent_1.getType()).eProxyURI();
		assertTrue(proxyUri.toString(), compTypeUri1.equals(proxyUri) || compTypeUri2.equals(proxyUri));

		assertNotNull(referringComponent_2.getType());
		assertTrue(referringComponent_2.getType().eIsProxy());
		assertTrue(referringComponent_2.getType() instanceof InternalEObject);
		proxyUri = ((InternalEObject) referringComponent_2.getType()).eProxyURI();
		assertTrue(proxyUri.toString(), compTypeUri1.equals(proxyUri) || compTypeUri2.equals(proxyUri));

		// Context: container is Null
		proxy = EObjectUtil.proxify(null, componentType1.eContainingFeature(), componentType1);
		assertNotNull(proxy);
		assertTrue(proxy.eIsProxy());
		// Context: feature is NULL
		proxy = EObjectUtil.proxify(hb20Platform_A_2, null, componentType2);
		assertNotNull(proxy);
		assertTrue(proxy.eIsProxy());

		// Proxify object with valid arguments
		URI expectedUri = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource20_3).getURI(referringComponent_1);
		proxy = EObjectUtil.proxify(referringComponent_1.eContainer(), referringComponent_1.eContainingFeature(), referringComponent_1);
		assertNotNull(proxy);
		assertTrue(proxy.eIsProxy());
		assertTrue(referringComponent_1.eIsProxy());
		proxyUri = ((InternalEObject) referringComponent_1).eProxyURI();
		assertEquals(expectedUri, proxyUri);

		// Context: eobject is null
		proxy = EObjectUtil.proxify(componentType2.eContainer(), componentType2.eContainmentFeature(), null);
		assertNull(proxy);

		// Unload test resources to clean up partially proxified test models
		resource20_2.unload();
		resource20_3.unload();
	}

	/**
	 * Test method for {@link EObjectUtil#deproxify(EObject)}
	 *
	 * @throws InterruptedException
	 * @throws OperationCanceledException
	 */
	public void testDeproxify() throws OperationCanceledException, InterruptedException {
		Resource resource20_2 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);

		Resource resource20_3 = refWks.editingDomain20.getResourceSet().getResource(URI.createPlatformResourceURI(
				DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/" + DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);

		assertNotNull(resource20_2);
		assertFalse(resource20_2.getContents().isEmpty());
		assertEquals(1, resource20_2.getContents().size());
		Platform hb20Platform_A_2 = (Platform) resource20_2.getContents().get(0);
		assertNotNull(hb20Platform_A_2);
		assertEquals(2, hb20Platform_A_2.getComponentTypes().size());
		assertEquals(2, hb20Platform_A_2.getInterfaces().size());

		ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource20_2);

		ComponentType componentType1 = hb20Platform_A_2.getComponentTypes().get(0);
		URI compTypeUri1 = extendedResource.getURI(componentType1);
		Port port1 = componentType1.getPorts().get(0);
		URI portUri1 = extendedResource.getURI(port1);
		Port port2 = componentType1.getPorts().get(1);
		URI portUri2 = extendedResource.getURI(port2);

		ComponentType componentType2 = hb20Platform_A_2.getComponentTypes().get(1);
		URI compTypeUri2 = extendedResource.getURI(componentType2);

		Interface interface1 = hb20Platform_A_2.getInterfaces().get(0);
		Interface interface2 = hb20Platform_A_2.getInterfaces().get(1);

		assertFalse(resource20_3.getContents().isEmpty());
		Application hb20Application = (Application) resource20_3.getContents().get(0);
		assertNotNull(hb20Application);
		assertEquals(2, hb20Application.getComponents().size());
		Component referringComponent_1 = hb20Application.getComponents().get(0);
		assertNotNull(referringComponent_1.getType());

		Component referringComponent_2 = hb20Application.getComponents().get(1);
		assertNotNull(referringComponent_2.getType());

		// Proxify ComponentType
		EObject proxy = EObjectUtil.proxify(componentType1.eContainer(), componentType1.eContainmentFeature(), componentType1);
		assertNotNull(proxy);
		assertTrue(proxy.eIsProxy());
		proxy = EObjectUtil.proxify(componentType2.eContainer(), componentType2.eContainmentFeature(), componentType2);
		assertNotNull(proxy);
		assertTrue(proxy.eIsProxy());

		for (ComponentType componentType : interface1.getProvidingComponentTypes()) {
			assertTrue(componentType.eIsProxy());
			assertTrue(componentType instanceof InternalEObject);
			URI proxyUri = ((InternalEObject) componentType).eProxyURI();
			assertTrue(proxyUri.toString(), compTypeUri1.equals(proxyUri) || compTypeUri2.equals(proxyUri));
		}

		assertFalse(interface2.getRequiringPorts().isEmpty());
		for (Port port : interface2.getRequiringPorts()) {
			assertTrue(port.eIsProxy());
			assertTrue(port instanceof InternalEObject);
			URI proxyUri = ((InternalEObject) port).eProxyURI();
			assertTrue(portUri1.equals(proxyUri) || portUri2.equals(proxyUri));
		}
		// Verify resource20_3 contents
		assertNotNull(referringComponent_1.getType());
		assertTrue(referringComponent_1.getType().eIsProxy());
		assertTrue(referringComponent_1.getType() instanceof InternalEObject);
		URI proxyUri = ((InternalEObject) referringComponent_1.getType()).eProxyURI();
		assertTrue(proxyUri.toString(), compTypeUri1.equals(proxyUri) || compTypeUri2.equals(proxyUri));

		assertNotNull(referringComponent_2.getType());
		assertTrue(referringComponent_2.getType().eIsProxy());
		assertTrue(referringComponent_2.getType() instanceof InternalEObject);
		proxyUri = ((InternalEObject) referringComponent_2.getType()).eProxyURI();
		assertTrue(proxyUri.toString(), compTypeUri1.equals(proxyUri) || compTypeUri2.equals(proxyUri));

		// Deproxify
		assertNotNull(EObjectUtil.deproxify(componentType1));
		assertNotNull(EObjectUtil.deproxify(componentType2));

		assertFalse(interface1.getProvidingComponentTypes().isEmpty());
		for (ComponentType componentType : interface1.getProvidingComponentTypes()) {
			assertFalse(componentType.eIsProxy());
			assertTrue(hb20Platform_A_2.getComponentTypes().contains(componentType));
		}
		assertFalse(interface2.getRequiringPorts().isEmpty());
		for (Port port : interface2.getRequiringPorts()) {
			assertFalse(port.eIsProxy());
			assertTrue(componentType1.getPorts().contains(port));
		}

		assertNotNull(referringComponent_2.getType());
		assertFalse(referringComponent_2.getType().eIsProxy());
		assertTrue(hb20Platform_A_2.getComponentTypes().contains(referringComponent_2.getType()));

		assertNotNull(referringComponent_1.getType());
		assertFalse(referringComponent_1.getType().eIsProxy());
		assertTrue(hb20Platform_A_2.getComponentTypes().contains(referringComponent_1.getType()));
		waitForModelLoading();
	}
}
