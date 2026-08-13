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
package org.eclipse.sphinx.tests.emf.workspace.integration.referentialintegrity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.FunctionBehavior;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;

/**
 * Test class associated to Bug 882 :"URI representing cross-document reference is not updated if an Identifiable is
 * renamed"
 */
@SuppressWarnings({ "nls" })
public class ReferentialIntegrityTest extends DefaultIntegrationTestCase {

	public ReferentialIntegrityTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_E);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);
	}

	// ######################################################################
	// ######################## IDENTIFIER ##################################
	// ######################################################################
	// Change Identifier of referred object

	// UML2 resource
	// Test for cross reference in the same resource

	public void testUML2ReferenceInTheSameResource() throws Exception {
		// Verify the reference
		Resource contextResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertNotNull(contextResource);

		List<Operation> operations = getOperations(contextResource);
		assertFalse(operations.isEmpty());

		List<FunctionBehavior> functionBehaviors = getFunctionBehaviors(contextResource);
		assertFalse(functionBehaviors.isEmpty());

		// Verify that reference is belong to resource in arProjecr3xD
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior behavior : operation.getMethods()) {
				assertFalse(behavior.eIsProxy());
				assertTrue(functionBehaviors.contains(behavior));
			}

		}
		// Change the short name of referred object
		final List<FunctionBehavior> behaviorToChangeIdenfitier = getFunctionBehaviors(contextResource);
		assertFalse(behaviorToChangeIdenfitier.isEmpty());

		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomainUml2, new Runnable() {
				@Override
				public void run() {
					for (FunctionBehavior funcBehavior : behaviorToChangeIdenfitier) {
						String Name = funcBehavior.getName();
						funcBehavior.setName(Name + "_changed");
					}

				}
			}, "Modify model resource");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		// verify the impact in referring resource

		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior method : operation.getMethods()) {
				assertFalse(method.eIsProxy());
				assertTrue(method.getName().contains("_changed"));
				assertTrue(functionBehaviors.contains(method));

			}

		}

		// assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomainUml2, referringResource));
		EcorePlatformUtil.saveProject(refWks.hbProject20_E, false, null);

		// Reload all resources
		synchronizedUnloadProject(refWks.hbProject20_E, false);
		synchronizedLoadProject(refWks.hbProject20_E, false);

		contextResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		operations = getOperations(contextResource);
		functionBehaviors = getFunctionBehaviors(contextResource);
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior behavior : operation.getMethods()) {
				assertFalse(behavior.eIsProxy());
				assertTrue(behavior.getName().contains("_changed"));
				assertTrue(functionBehaviors.contains(behavior));
			}

		}
	}

	// Cross document reference bases on Id

	public void testUML2CrossReferenceInTheSameProject() throws Exception {
		// Verify the reference
		Resource referredResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertNotNull(referredResource);

		Resource referringResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertNotNull(referringResource);

		List<Operation> operations = getOperations(referringResource);
		assertFalse(operations.isEmpty());

		// Verify that reference is belong to resource in arProjecr3xD
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior behavior : operation.getMethods()) {
				assertFalse(behavior.eIsProxy());
				assertEquals(referredResource, behavior.eResource());
			}

		}
		// Change the short name of referred object
		final List<FunctionBehavior> behaviorToChangeIdenfitier = getFunctionBehaviors(referredResource);
		assertFalse(behaviorToChangeIdenfitier.isEmpty());
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomainUml2, new Runnable() {
				@Override
				public void run() {
					for (FunctionBehavior funcBehavior : behaviorToChangeIdenfitier) {
						String Name = funcBehavior.getName();
						funcBehavior.setName(Name + "_changed");
					}

				}
			}, "Modify model resource");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		// verify the impact in referring resource
		referringResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		operations = getOperations(referringResource);
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior method : operation.getMethods()) {
				assertFalse(method.eIsProxy());
				assertTrue(method.getName().contains("_changed"));
				assertEquals(referredResource, method.eResource());
			}

		}
		// UML reference via Object ID-> referring changed nothing
		// assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomainUml2, referringResource));
		EcorePlatformUtil.saveProject(refWks.hbProject20_E, false, null);

		// Reload all resources
		synchronizedUnloadAllProjects();
		synchronizedLoadAllProjects();
		referredResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		referringResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertNotNull(referredResource);
		assertNotNull(referringResource);
		operations = getOperations(referringResource);
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior method : operation.getMethods()) {
				assertFalse(method.eIsProxy());
				assertTrue(method.getName().contains("_changed"));
				assertEquals(referredResource, method.eResource());
			}

		}
	}

	// Test for cross reference in different resource
	// Cross document reference bases on URI
	// TODO re-active when bug 1084 was fixed
	public void testUML2CrossReferenceInDifferentProjects() throws Exception {
		// // Verify the reference
		// Resource referredResource1 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_1);
		// assertNotNull(referredResource1);
		//
		// Resource referredResource2 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// assertNotNull(referredResource2);
		//
		// Resource referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		// assertNotNull(referringResource);
		//
		// List<Operation> operations = getOperations(referringResource);
		// assertFalse(operations.isEmpty());
		//
		// // URI uriRef1 = URI.createURI("/hbProject20_D/uml2File_3xD_1.uml#//package2/FunctionBehavior3xD1_1");
		// // URI uriRef2 = URI.createURI("/hbProject20_D/uml2File_3xD_2.uml#//package2/FunctionBehavior3xD2_2");
		//
		// // HashSet<URI> expectedUriRefs = new HashSet<URI>();
		// // expectedUriRefs.add(uriRef1);
		// // expectedUriRefs.add(uriRef2);
		//
		// // Verify that reference is belong to resource in arProjecr3xD
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior method : operation.getMethods()) {
		// assertFalse(method.eIsProxy());
		// assertTrue(referredResource1.equals(method.eResource()) || referredResource2.equals(method.eResource()));
		// // assertTrue(EcoreUtil.getURI(method).toString(),
		// // expectedUriRefs.contains(EcoreUtil.getURI(method)));
		// }
		//
		// }
		// // Change the short name of referred object
		//
		// final List<FunctionBehavior> behaviorToChangeIdenfitier = getFunctionBehaviors(referredResource1);
		// behaviorToChangeIdenfitier.addAll(getFunctionBehaviors(referredResource2));
		// assertFalse(behaviorToChangeIdenfitier.isEmpty());
		// try {
		// WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomainUml2, new Runnable() {
		// public void run() {
		// for (FunctionBehavior funcBehavior : behaviorToChangeIdenfitier) {
		// String Name = funcBehavior.getName();
		// funcBehavior.setName(Name + "_changed");
		//
		// }
		//
		// }
		// }, "Modify model resource");
		// } catch (Exception e) {
		// fail(e.getLocalizedMessage());
		// }
		// // expectedUriRefs.clear();
		// // uriRef1 = URI.createURI("/hbProject20_D/uml2File_3xD_1.uml#//package2_changed/FunctionBehavior3xD1_1");
		// // uriRef2 = URI.createURI("/hbProject20_D/uml2File_3xD_2.uml#//package2_changed/FunctionBehavior3xD2_2");
		// // expectedUriRefs.add(uriRef1);
		// // expectedUriRefs.add(uriRef2);
		//
		// // verify the impact in referring resource
		// referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		// operations = getOperations(referringResource);
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior method : operation.getMethods()) {
		// assertFalse(method.eIsProxy());
		// assertTrue(method.getName().contains("_changed"));
		// assertTrue(referredResource1.equals(method.eResource()) || referredResource2.equals(method.eResource()));
		// // assertTrue(EcoreUtil.getURI(method).toString(), expectedUriRefs.contains(EcoreUtil.getURI(method)));
		//
		// }
		//
		// }
		//
		// // TODO refering resource is not set to Dirty to save
		// // assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomainUml2, referringResource));
		// EcorePlatformUtil.saveProject(refWks.hbProject20_D, false, null);
		// EcorePlatformUtil.saveProject(refWks.hbProject20_E, false, null);
		// // Reload all resources
		// synchronizedUnloadAllProjects();
		// synchronizedLoadAllProjects();
		// referredResource1 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// referredResource1 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		// assertNotNull(referredResource1);
		// assertNotNull(referredResource2);
		// assertNotNull(referringResource);
		// for (FunctionBehavior behavior : getFunctionBehaviors(referredResource1)) {
		// assertNotNull(behavior.getName());
		// assertTrue(behavior.getName().contains("_changed"));
		// }
		//
		// for (FunctionBehavior behavior : getFunctionBehaviors(referredResource2)) {
		// assertNotNull(behavior.getName());
		// assertTrue(behavior.getName().contains("_changed"));
		// }
		// operations = getOperations(referringResource);
		// // TODO The URI reference was not changed in underlying file. But the reference is not proxy???
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior method : operation.getMethods()) {
		// assertFalse(method.eIsProxy());
		// assertTrue(method.getName().contains("_changed"));
		// assertTrue(referredResource1.equals(method.eResource()) || referredResource2.equals(method.eResource()));
		// // assertTrue(EcoreUtil.getURI(method).toString(), expectedUriRefs.contains(EcoreUtil.getURI(method)));
		// }
		//
		// }
	}

	// ######################################################################
	// ######################## CONTAINER ##################################
	// ######################################################################

	// Change identifier of referred object's container

	public void testUML2CrossReferenceInTheSameProject_Container() throws Exception {
		// Verify the reference
		Resource referredResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		assertNotNull(referredResource);

		Resource referringResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertNotNull(referringResource);

		List<Operation> operations = getOperations(referringResource);
		assertFalse(operations.isEmpty());

		// Verify that reference is belong to resource in arProjecr3xD
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior behavior : operation.getMethods()) {
				assertFalse(behavior.eIsProxy());
				assertEquals(referredResource, behavior.eResource());
			}

		}
		// Change the short name of referred object
		final List<FunctionBehavior> behaviors = getFunctionBehaviors(referredResource);
		assertFalse(behaviors.isEmpty());
		try {
			WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomainUml2, new Runnable() {
				@Override
				public void run() {
					for (FunctionBehavior funcBehavior : behaviors) {
						Package container = funcBehavior.getPackage();
						String name = container.getName();
						if (!name.contains("_changed")) {
							container.setName(name + "_changed");
						}
					}
				}
			}, "Modify model resource");
		} catch (Exception e) {
			fail(e.getLocalizedMessage());
		}

		// verify the impact in referring resource
		referringResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		operations = getOperations(referringResource);
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior method : operation.getMethods()) {
				assertFalse(method.eIsProxy());
				assertNotNull(method.eContainer());
				Package container = (Package) method.eContainer();
				assertNotNull(container);
				assertTrue(container.getName().contains("_changed"));
				assertEquals(referredResource, method.eResource());
			}

		}
		// UML reference via Object ID-> referring changed nothing
		// assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomainUml2, referringResource));
		EcorePlatformUtil.saveProject(refWks.hbProject20_E, false, null);

		// Reload all resources
		synchronizedUnloadAllProjects();
		synchronizedLoadAllProjects();
		referredResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		referringResource = getProjectResource(refWks.hbProject20_E, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		assertNotNull(referredResource);
		assertNotNull(referringResource);
		operations = getOperations(referringResource);
		for (Operation operation : operations) {
			assertNotNull(operation.getMethods());
			assertFalse(operation.getMethods().isEmpty());
			for (Behavior method : operation.getMethods()) {
				assertFalse(method.eIsProxy());
				assertNotNull(method.eContainer());
				Package container = (Package) method.eContainer();
				assertNotNull(container);
				assertTrue(container.getName().contains("_changed"));
				assertEquals(referredResource, method.eResource());
			}

		}
	}

	// Test for cross reference in different resource
	// Cross document reference bases on URI
	// TODO re-active when bug 1084 was fixed
	public void testUML2CrossReferenceInDifferentProjects_Container() throws Exception {
		// // Verify the reference
		// Resource referredResource1 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_1);
		// assertNotNull(referredResource1);
		//
		// Resource referredResource2 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// assertNotNull(referredResource2);
		//
		// Resource referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		// assertNotNull(referringResource);
		//
		// List<Operation> operations = getOperations(referringResource);
		// assertFalse(operations.isEmpty());
		//
		// // URI uriRef1 = URI.createURI("/hbProject20_D/uml2File_3xD_1.uml#//package2/FunctionBehavior3xD1_1");
		// // URI uriRef2 = URI.createURI("/hbProject20_D/uml2File_3xD_2.uml#//package2/FunctionBehavior3xD2_2");
		//
		// // HashSet<URI> expectedUriRefs = new HashSet<URI>();
		// // expectedUriRefs.add(uriRef1);
		// // expectedUriRefs.add(uriRef2);
		//
		// // Verify that reference is belong to resource in arProjecr3xD
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior method : operation.getMethods()) {
		// assertFalse(method.eIsProxy());
		// assertTrue(referredResource1.equals(method.eResource()) || referredResource2.equals(method.eResource()));
		// // assertTrue(EcoreUtil.getURI(method).toString(),
		// // expectedUriRefs.contains(EcoreUtil.getURI(method)));
		// }
		//
		// }
		// // Change the short name of referred object
		//
		// final List<FunctionBehavior> behaviorToChangeIdenfitier = getFunctionBehaviors(referredResource1);
		// behaviorToChangeIdenfitier.addAll(getFunctionBehaviors(referredResource2));
		// assertFalse(behaviorToChangeIdenfitier.isEmpty());
		// try {
		// WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomainUml2, new Runnable() {
		// public void run() {
		// for (Behavior method : operation.getMethods()) {
		// assertFalse(method.eIsProxy());
		// assertTrue(method.getName().contains("_changed"));
		// assertEquals(referredResource, method.eResource());
		// }
		// }
		//
		// }
		// }, "Modify model resource");
		// } catch (Exception e) {
		// fail(e.getLocalizedMessage());
		// }
		// // expectedUriRefs.clear();
		// // uriRef1 = URI.createURI("/hbProject20_D/uml2File_3xD_1.uml#//package2_changed/FunctionBehavior3xD1_1");
		// // uriRef2 = URI.createURI("/hbProject20_D/uml2File_3xD_2.uml#//package2_changed/FunctionBehavior3xD2_2");
		// // expectedUriRefs.add(uriRef1);
		// // expectedUriRefs.add(uriRef2);
		//
		// // verify the impact in referring resource
		// referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		// operations = getOperations(referringResource);
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior method : operation.getMethods()) {
		// assertFalse(method.eIsProxy());
		// assertTrue(method.getName().contains("_changed"));
		// assertTrue(referredResource1.equals(method.eResource()) || referredResource2.equals(method.eResource()));
		// // assertTrue(EcoreUtil.getURI(method).toString(), expectedUriRefs.contains(EcoreUtil.getURI(method)));
		//
		// }
		//
		// }
		//
		// // TODO refering resource is not set to Dirty to save
		// // assertTrue(SaveIndicatorUtil.isDirty(refWks.editingDomainUml2, referringResource));
		// EcorePlatformUtil.saveProject(refWks.hbProject20_D, false, null);
		// EcorePlatformUtil.saveProject(refWks.hbProject20_E, false, null);
		// // Reload all resources
		// synchronizedUnloadAllProjects();
		// synchronizedLoadAllProjects();
		// referredResource1 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// referredResource1 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		// assertNotNull(referredResource1);
		// assertNotNull(referredResource2);
		// assertNotNull(referringResource);
		// for (FunctionBehavior behavior : getFunctionBehaviors(referredResource1)) {
		// assertNotNull(behavior.getName());
		// assertTrue(behavior.getName().contains("_changed"));
		// }
		//
		// for (FunctionBehavior behavior : getFunctionBehaviors(referredResource2)) {
		// assertNotNull(behavior.getName());
		// assertTrue(behavior.getName().contains("_changed"));
		// }
		// operations = getOperations(referringResource);
		// // TODO The URI reference was not changed in underlying file. But the reference is not proxy???
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior method : operation.getMethods()) {
		// assertFalse(method.eIsProxy());
		// assertTrue(method.getName().contains("_changed"));
		// assertTrue(referredResource1.equals(method.eResource()) || referredResource2.equals(method.eResource()));
		// // assertTrue(EcoreUtil.getURI(method).toString(), expectedUriRefs.contains(EcoreUtil.getURI(method)));
		// }
		//
		// }
	}

	// ######################################################################
	// ######################## PROJECT ##################################
	// ######################################################################

	// UML2 resource

	public void testUML2CrossReferenceInDifferentProject_Project() throws Exception {
		// // Verify the reference
		//
		// IProject contextProject = refWks.hbProject20_E;
		// assertNotNull(contextProject);
		//
		// Resource referredResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		// assertNotNull(referredResource);
		//
		// Resource referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		// assertNotNull(referringResource);
		//
		// List<Operation> operations = getOperations(referringResource);
		// assertFalse(operations.isEmpty());
		//
		// // Verify that reference is belong to resource in arProjecr3xD
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior behavior : operation.getMethods()) {
		// assertFalse(behavior.eIsProxy());
		// assertEquals(referredResource, behavior.eResource());
		// }
		//
		// }
		// // rename project
		// String projectNewName = contextProject.getName() + "_changed";
		// synchronizedRenameProject(contextProject, projectNewName);
		//
		// synchronizedUnloadProject(contextProject, true);
		// synchronizedLoadProject(contextProject, true);
		//
		// // Verify references again
		// contextProject = EcorePlugin.getWorkspaceRoot().getProject(projectNewName);
		// assertNotNull(contextProject);
		//
		// referredResource = getProjectResource(contextProject, DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_2);
		// referringResource = getProjectResource(contextProject,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		// assertNotNull(referredResource);
		// assertNotNull(referringResource);
		// operations = getOperations(referringResource);
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior behavior : operation.getMethods()) {
		// assertFalse(behavior.eIsProxy());
		// assertEquals(referredResource, behavior.eResource());
		// assertTrue(behavior.eContainer() instanceof Package);
		// assertTrue(behavior.eResource().getURI().toString().contains("_changed"));
		// }
		//
		// }
		// }
		//
		// // Test for cross reference in different resource
		// // Cross document reference bases on URI
		//
		// public void testUML2CrossReferenceInDifferentProjects_Container() throws Exception {
		// // Verify the reference
		// IProject referredProject = refWks.hbProject20_D;
		// assertNotNull(referredProject);
		//
		// Resource referredResource1 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_1);
		// assertNotNull(referredResource1);
		//
		// Resource referredResource2 = getProjectResource(refWks.hbProject20_D,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// assertNotNull(referredResource2);
		//
		// Resource referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_1);
		// assertNotNull(referringResource);
		//
		// List<Operation> operations = getOperations(referringResource);
		// assertFalse(operations.isEmpty());
		//
		// // Verify that reference is belong to resource in arProjecr3xD
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior behavior : operation.getMethods()) {
		// assertFalse(behavior.eIsProxy());
		// assertTrue(referredResource1.equals(behavior.eResource()) || referredResource2.equals(behavior.eResource()));
		// }
		//
		// }
		// // Change referred project name
		//
		// String projectNewName = referredProject.getName() + "_changed";
		// synchronizedRenameProject(referredProject, projectNewName);
		//
		// synchronizedUnloadProject(referredProject, true);
		// synchronizedLoadProject(referredProject, true);
		//
		// // Verify references again
		// referredProject = EcorePlugin.getWorkspaceRoot().getProject(projectNewName);
		// assertNotNull(referredProject);
		//
		// referredResource1 = getProjectResource(referredProject,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// referredResource1 = getProjectResource(referredProject,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_3xD_2);
		// referringResource = getProjectResource(refWks.hbProject20_E,
		// DefaultTestReferenceWorkspace.UML2_FILE_NAME_20E_3);
		// assertNotNull(referredResource1);
		// assertNotNull(referredResource2);
		// assertNotNull(referringResource);
		//
		// operations = getOperations(referringResource);
		// for (Operation operation : operations) {
		// assertNotNull(operation.getMethods());
		// assertFalse(operation.getMethods().isEmpty());
		// for (Behavior behavior : operation.getMethods()) {
		// assertFalse(behavior.eIsProxy());
		// assertTrue(referredResource1.equals(behavior.eResource()) || referredResource2.equals(behavior.eResource()));
		// assertTrue(behavior.eResource().getURI().toString().contains("_changed"));
		// }
		//
		// }
	}

	// ==========================

	protected List<Operation> getOperations(Resource uml2Resource) {
		List<Operation> operations = new ArrayList<Operation>();
		assertNotNull(uml2Resource);
		if (!uml2Resource.getContents().isEmpty()) {
			Model model = (Model) uml2Resource.getContents().get(0);
			if (model != null) {
				for (org.eclipse.uml2.uml.PackageableElement uml2Content : model.getPackagedElements()) {
					if (uml2Content instanceof Package) {
						Package uml2Package = (Package) uml2Content;
						for (org.eclipse.uml2.uml.PackageableElement element : uml2Package.getPackagedElements()) {
							if (element instanceof Interface) {
								Interface intf = (Interface) element;
								operations.addAll(intf.getOwnedOperations());
							}
						}
					}
				}
			}
		}
		return operations;
	}

	protected List<FunctionBehavior> getFunctionBehaviors(Resource uml2Resource) {
		List<FunctionBehavior> functionBehaviors = new ArrayList<FunctionBehavior>();
		assertNotNull(uml2Resource);
		if (!uml2Resource.getContents().isEmpty()) {
			Model model = (Model) uml2Resource.getContents().get(0);
			if (model != null) {
				for (org.eclipse.uml2.uml.PackageableElement uml2Content : model.getPackagedElements()) {
					if (uml2Content instanceof Package) {
						Package uml2Package = (Package) uml2Content;
						for (org.eclipse.uml2.uml.PackageableElement element : uml2Package.getPackagedElements()) {
							if (element instanceof FunctionBehavior) {
								FunctionBehavior functionBehavior = (FunctionBehavior) element;
								functionBehaviors.add(functionBehavior);
							}
						}
					}
				}
			}

		}
		return functionBehaviors;
	}

	@Override
	public void tearDown() throws Exception {

		for (int i = 0; i < EcorePlugin.getWorkspaceRoot().getProjects().length; i++) {
			synchronizedDeleteProject(EcorePlugin.getWorkspaceRoot().getProjects()[i]);
		}
		super.tearDown();
	}

}
