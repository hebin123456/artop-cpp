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
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings("nls")
public class EObjectUtilTest2 extends DefaultIntegrationTestCase {

	private ComponentType componentType20A_2_1;
	private ComponentType componentType20A_2_2;
	private Port port20A_2_1;
	private Port port20A_2_2;

	private Component component20A_3_1;
	private Component component20A_3_2;
	private Component component21A_4_1;

	private ComponentType componentType20D_2_1;
	private ComponentType componentType20D_2_2;
	private Port port20D_2_1;
	private Port port20D_2_2;
	private Component component20D_3_1;
	private Component component20E_1_1;

	public EObjectUtilTest2() {
		setRecycleReferenceWorkspaceOfPreviousTestRun(false);

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

		Resource resource20A_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);

		assertNotNull(resource20A_2);
		assertFalse(resource20A_2.getContents().isEmpty());
		assertEquals(1, resource20A_2.getContents().size());
		Platform platform20A_2 = (Platform) resource20A_2.getContents().get(0);
		assertNotNull(platform20A_2);
		assertEquals(2, platform20A_2.getComponentTypes().size());
		assertEquals(2, platform20A_2.getInterfaces().size());

		componentType20A_2_1 = platform20A_2.getComponentTypes().get(0);
		assertFalse(componentType20A_2_1.getPorts().isEmpty());
		port20A_2_1 = componentType20A_2_1.getPorts().get(0);
		port20A_2_2 = componentType20A_2_1.getPorts().get(1);

		componentType20A_2_2 = platform20A_2.getComponentTypes().get(1);
		// ---------------------------------
		Resource resource20_3 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_3, true), false);

		assertNotNull(resource20_3);
		assertFalse(resource20_3.getContents().isEmpty());
		assertEquals(1, resource20_3.getContents().size());
		Application application_20A_3 = (Application) resource20_3.getContents().get(0);
		assertNotNull(application_20A_3);
		assertEquals(2, application_20A_3.getComponents().size());

		component20A_3_1 = application_20A_3.getComponents().get(0);
		component20A_3_2 = application_20A_3.getComponents().get(1);
		// ---------------------------------
		Resource resource21A_4 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_21_20A_4, true), false);

		assertNotNull(resource21A_4);
		assertFalse(resource21A_4.getContents().isEmpty());
		assertEquals(1, resource21A_4.getContents().size());
		Application application_21A_4 = (Application) resource21A_4.getContents().get(0);
		assertNotNull(application_21A_4);
		assertEquals(2, application_21A_4.getComponents().size());

		component21A_4_1 = application_21A_4.getComponents().get(0);
		// ---------------------------------
		// hbfile20D_2
		Resource resource20D_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);

		assertNotNull(resource20D_2);
		assertFalse(resource20D_2.getContents().isEmpty());
		assertEquals(1, resource20D_2.getContents().size());
		Platform platform20D_2 = (Platform) resource20D_2.getContents().get(0);
		assertNotNull(platform20D_2);
		assertEquals(2, platform20D_2.getComponentTypes().size());

		componentType20D_2_1 = platform20D_2.getComponentTypes().get(0);
		assertFalse(componentType20D_2_1.getPorts().isEmpty());
		port20D_2_1 = componentType20D_2_1.getPorts().get(0);
		port20D_2_2 = componentType20D_2_1.getPorts().get(1);

		componentType20D_2_2 = platform20D_2.getComponentTypes().get(1);
		// -----------------
		// hbfile20D_3
		Resource resource20D_3 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_3, true), false);

		assertNotNull(resource20D_3);
		assertFalse(resource20D_3.getContents().isEmpty());
		assertEquals(1, resource20D_3.getContents().size());
		Application application_20D_3 = (Application) resource20D_3.getContents().get(0);
		assertNotNull(application_20D_3);
		assertEquals(2, application_20D_3.getComponents().size());

		component20D_3_1 = application_20D_3.getComponents().get(0);
		// ----------------------
		// hbfile20E_1
		Resource resource20E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);

		assertNotNull(resource20E_1);
		assertFalse(resource20E_1.getContents().isEmpty());
		assertEquals(1, resource20E_1.getContents().size());
		Application application_20E_1 = (Application) resource20E_1.getContents().get(0);
		assertNotNull(application_20E_1);
		assertEquals(2, application_20E_1.getComponents().size());

		component20E_1_1 = application_20E_1.getComponents().get(0);
	}

	public void testGetInverseReferences_StandAloneProject() throws OperationCanceledException, ExecutionException {
		Collection<EStructuralFeature.Setting> references;

		references = EObjectUtil.getInverseReferences(componentType20A_2_1, true);
		assertFalse(references.isEmpty());
		List<EObject> objects = getEObjects(references);
		assertEquals(5, objects.size());
		assertTrue(objects.contains(componentType20A_2_1.eContainer()));
		assertTrue(objects.contains(component21A_4_1));
		assertTrue(objects.contains(component20A_3_1));
		assertFalse(objects.contains(component20A_3_2));
		assertTrue(objects.contains(port20A_2_1));
		assertTrue(objects.contains(port20A_2_2));

		assertFalse(objects.contains(component20D_3_1));
		assertFalse(objects.contains(componentType20D_2_1));
		assertFalse(objects.contains(componentType20D_2_2));
		assertFalse(objects.contains(component20E_1_1));
		assertFalse(objects.contains(port20D_2_2));
		assertFalse(objects.contains(port20D_2_1));

		assertFalse(objects.contains(componentType20A_2_2));
		// ---------------------
		references.clear();
		references = EObjectUtil.getInverseReferences(port20A_2_1, true);
		assertFalse(references.isEmpty());
		objects = getEObjects(references);
		assertTrue(objects.contains(componentType20A_2_1));

		assertFalse(objects.contains(componentType20A_2_1.eContainer()));
		assertFalse(objects.contains(componentType20A_2_2));
		assertFalse(objects.contains(component21A_4_1));
		assertFalse(objects.contains(component20A_3_1));
		assertFalse(objects.contains(component20A_3_2));
		assertFalse(objects.contains(port20A_2_1));
		assertFalse(objects.contains(port20A_2_2));
	}

	public void testGetInverseReferences_ProjectWithReferences() throws OperationCanceledException, ExecutionException {
		Collection<EStructuralFeature.Setting> references;

		assertNotNull(EcoreUtil.getRootContainer(componentType20D_2_1));
		assertNotNull(EcoreUtil.getRootContainer(componentType20D_2_1).eResource());
		assertNotNull(EcoreUtil.getRootContainer(componentType20D_2_1).eResource().getResourceSet());

		references = EObjectUtil.getInverseReferences(componentType20D_2_1, true);
		assertFalse(references.isEmpty());
		List<EObject> objects = getEObjects(references);

		assertFalse(objects.contains(componentType20D_2_2));

		assertFalse(objects.contains(component21A_4_1));
		assertFalse(objects.contains(component20A_3_1));
		assertFalse(objects.contains(port20A_2_1));
		assertFalse(objects.contains(port20A_2_2));

		EObject container = componentType20D_2_1.eContainer();
		assertNotNull(container);
		assertTrue(objects.contains(container));
		assertTrue(objects.contains(component20D_3_1));
		assertTrue(objects.contains(component20E_1_1));
		assertTrue(objects.contains(port20D_2_2));
		assertTrue(objects.contains(port20D_2_1));
		assertEquals(5, objects.size());
	}

	public void testGetInverseReferences_Proxy() throws OperationCanceledException, ExecutionException {
		Collection<EStructuralFeature.Setting> references;

		// ==============================================
		// Containment
		Runnable runnable1 = new Runnable() {
			@Override
			public void run() {
				EcoreUtil.delete(port20D_2_1);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable1, "remove componentType");
		waitForModelLoading();

		assertTrue(port20D_2_1.eIsProxy());
		// deleted element was removed

		// with resolved = false
		references = EObjectUtil.getInverseReferences(componentType20D_2_1, true);
		List<EObject> objects = getEObjects(references);
		assertEquals(4, objects.size());
		EObject container = componentType20D_2_1.eContainer();
		assertNotNull(container);
		assertTrue(objects.contains(container));
		assertTrue(objects.contains(component20D_3_1));
		assertTrue(objects.contains(component20E_1_1));
		assertFalse(objects.contains(port20D_2_1));
		assertTrue(objects.contains(port20D_2_2));

		// with resolved = false
		references = EObjectUtil.getInverseReferences(componentType20D_2_1, false);
		objects = getEObjects(references);
		assertTrue(objects.contains(component20D_3_1));
		assertTrue(objects.contains(component20E_1_1));
		assertFalse(objects.contains(port20D_2_1));
		assertTrue(objects.contains(port20D_2_2));

		// ==============================================
		// References
		Runnable runnable2 = new Runnable() {
			@Override
			public void run() {
				EcoreUtil.delete(component20D_3_1);
				EcoreUtil.delete(component20E_1_1);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable2, "remove component referecing to context object");
		waitForModelLoading();

		assertTrue(component20D_3_1.eIsProxy());
		assertTrue(component20E_1_1.eIsProxy());

		// with resolved = true
		references.clear();
		references = EObjectUtil.getInverseReferences(componentType20D_2_1, true);
		objects = getEObjects(references);
		assertEquals(4, objects.size());
		assertTrue(objects.contains(container));
		assertTrue(objects.contains(component20D_3_1));
		assertTrue(objects.contains(component20E_1_1));
		assertFalse(objects.contains(port20D_2_1));
		assertTrue(objects.contains(port20D_2_2));

		// with resolved = false
		references.clear();
		references = EObjectUtil.getInverseReferences(componentType20D_2_1, false);
		objects = getEObjects(references);
		assertEquals(4, objects.size());
		assertTrue(objects.contains(container));
		assertTrue(objects.contains(component20D_3_1));
		assertTrue(objects.contains(component20E_1_1));
		assertFalse(objects.contains(port20D_2_1));
		assertTrue(objects.contains(port20D_2_2));
	}

	public void testGetInverseReferences_ResourceSetIsNull() throws OperationCanceledException, ExecutionException {
		// -------------------------------------------
		// Context Object is NULL
		Collection<EStructuralFeature.Setting> references = EObjectUtil.getInverseReferences(null, true);
		assertNotNull(references);
		assertTrue(references.isEmpty());

		// -------------------------------------------
		// Resource is NOT NULL, but it does not belong to any ResourceSet
		final Resource resource20D_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);

		assertNotNull(resource20D_2);
		assertFalse(resource20D_2.getContents().isEmpty());

		final ResourceSet resourceSet = refWks.editingDomain20.getResourceSet();
		final EObject modelRoot = componentType20D_2_1.eContainer();
		assertNotNull(modelRoot);

		Runnable runnable0 = new Runnable() {
			@Override
			public void run() {
				resourceSet.getResources().remove(resource20D_2);
			}
		};

		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable0, "remove componentType");
		waitForModelLoading();

		assertNotNull(componentType20D_2_1.eResource());
		assertNull(componentType20D_2_1.eResource().getResourceSet());

		references = EObjectUtil.getInverseReferences(componentType20D_2_1, true);
		List<EObject> objects = getEObjects(references);
		assertEquals(3, objects.size());

		assertFalse(objects.contains(componentType20D_2_2));
		assertTrue(objects.contains(modelRoot));
		assertTrue(objects.contains(port20D_2_2));
		assertTrue(objects.contains(port20D_2_1));

		assertFalse(objects.contains(component21A_4_1));
		assertFalse(objects.contains(component20A_3_1));
		assertFalse(objects.contains(port20A_2_1));
		assertFalse(objects.contains(port20A_2_2));

		assertFalse(objects.contains(component20D_3_1));
		assertFalse(objects.contains(component20E_1_1));
		assertFalse(objects.contains(componentType20D_2_2));
	}

	public void testGetInverseReferences_ModelRootIsNull() throws OperationCanceledException, ExecutionException {
		Collection<EStructuralFeature.Setting> references;
		Resource resource20D_2 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20D_2, true), false);

		assertNotNull(resource20D_2);

		// Context Object is NOT NULL. But Model Root is NULL;
		final EObject modelRoot = componentType20D_2_1.eContainer();
		assertNotNull(modelRoot);

		Runnable runnable2 = new Runnable() {
			@Override
			public void run() {
				EcoreUtil.delete(componentType20D_2_1);
			}
		};

		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable2, "remove componentType");
		waitForModelLoading();

		references = EObjectUtil.getInverseReferences(componentType20D_2_1, true);
		assertFalse(references.isEmpty());
		List<EObject> objects = getEObjects(references);
		assertEquals(2, objects.size());
		assertFalse(objects.contains(componentType20D_2_2));
		assertFalse(objects.contains(modelRoot));
		assertTrue(objects.contains(port20D_2_2));
		assertTrue(objects.contains(port20D_2_1));

		assertFalse(objects.contains(component21A_4_1));
		assertFalse(objects.contains(component20A_3_1));
		assertFalse(objects.contains(port20A_2_1));
		assertFalse(objects.contains(port20A_2_2));

		assertFalse(objects.contains(component20D_3_1));
		assertFalse(objects.contains(componentType20D_2_2));
		assertFalse(objects.contains(component20E_1_1));

		EcoreResourceUtil.unloadResource(resource20D_2);
	}

	public List<EObject> getEObjects(Collection<EStructuralFeature.Setting> settings) {
		List<EObject> objects = new ArrayList<EObject>();
		if (settings != null) {
			for (Setting setting : settings) {
				if (setting.getEObject() != null) {
					objects.add(setting.getEObject());
				}
			}
		}
		return objects;
	}
}
