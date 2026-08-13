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
package org.eclipse.sphinx.tests.emf.integration.resource;

import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Port;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Factory;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings("nls")
public class LocalProxifyMechanismTest extends DefaultIntegrationTestCase {

	public LocalProxifyMechanismTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	protected Platform platform;
	protected Resource contextResource;
	protected ComponentType componentType1;
	protected ComponentType componentType2;
	protected Port port1;
	protected Port port2;
	protected Interface interface1;
	protected Interface interface2;
	final String componentType1ProxyUri = "hb:/#//ComponentType1";
	final String port1ProxyUri = "hb:/#//ComponentType1/Port1";
	final String port2ProxyUri = "hb:/#//ComponentType1/Port2";
	final String componentType2ProxyUri = "hb:/#//ComponentType2";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		contextResource = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A + "/"
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_2, true), false);
		assertNotNull(contextResource);

		assertFalse(contextResource.getContents().isEmpty());
		EObject rootModel = contextResource.getContents().get(0);
		assertTrue(rootModel instanceof Platform);
		platform = (Platform) rootModel;
		assertEquals(2, platform.getComponentTypes().size());
		componentType1 = platform.getComponentTypes().get(0);
		assertEquals(2, componentType1.getPorts().size());
		port1 = componentType1.getPorts().get(0);
		port2 = componentType1.getPorts().get(1);

		componentType2 = platform.getComponentTypes().get(1);

		assertEquals(2, platform.getInterfaces().size());
		interface1 = platform.getInterfaces().get(0);
		interface2 = platform.getInterfaces().get(1);

	}

	// Delete an object which is a Container by using EObjectUtil.delete().
	// Object will be proxified before being removed
	// -> Object to delete and it children will be proxified
	// Index of object to delete is -1. Its children's proxy URI are calculated base on container proxy URI
	public void testDeleteContainer() throws ExecutionException {

		// delete object
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				EcoreUtil.remove(componentType1);
			}
		});
		assertEquals(1, platform.getComponentTypes().size());
		// Verify that deleted objects were proxified
		InternalEObject internalEObject;
		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertTrue(interface1.getRequiringPorts().get(0).eIsProxy());
		internalEObject = (InternalEObject) interface1.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port2", internalEObject.eProxyURI().toString());

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertTrue(interface2.getRequiringPorts().get(0).eIsProxy());
		internalEObject = (InternalEObject) interface2.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port1", internalEObject.eProxyURI().toString());

	}

	// Delete an object which is a Container by using EObjectUtil.delete()
	// -> Object to delete and it children will be proxified
	// Index of object to delete is -1. Its children's proxy URI are calculated base on container proxy URI
	// Undo deleting object -> proxies are resolved. References navigate to restored object again
	public void testDeleteContainer_Undo() throws ExecutionException {

		// delete object
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				EcoreUtil.remove(componentType1);
			}
		});
		assertEquals(1, platform.getComponentTypes().size());
		// Verify that deleted objects were proxified
		InternalEObject internalEObject;
		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertTrue(interface1.getRequiringPorts().get(0).eIsProxy());
		internalEObject = (InternalEObject) interface1.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port2", internalEObject.eProxyURI().toString());

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertTrue(interface2.getRequiringPorts().get(0).eIsProxy());
		internalEObject = (InternalEObject) interface2.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port1", internalEObject.eProxyURI().toString());

		// Undo deleting
		IUndoContext op = WorkspaceTransactionUtil.getUndoContext(refWks.editingDomain20);
		OperationHistoryFactory.getOperationHistory().undo(op, null, null);
		waitForModelLoading();

		// Verify that proxies are resolved
		assertEquals(2, platform.getComponentTypes().size());

		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertFalse(interface1.getRequiringPorts().get(0).eIsProxy());

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertFalse(interface2.getRequiringPorts().get(0).eIsProxy());

	}

	// Delete an object which is a not a container by using EObjectUtil.delete()
	// -> Object to delete and it children will be proxified
	// Index of object to delete is -1.
	// Undo deleting object -> proxies are resolved. References navigate to restored object again
	public void testDeleteSingleObject_Undo() throws ExecutionException {

		// delete object
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				EcoreUtil.remove(port1);
			}
		});
		// Verify that deleted objects were proxified
		InternalEObject internalEObject;

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertTrue(interface2.getRequiringPorts().get(0).eIsProxy());
		internalEObject = (InternalEObject) interface2.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port1", internalEObject.eProxyURI().toString());

		// Undo deleting
		IUndoContext op = WorkspaceTransactionUtil.getUndoContext(refWks.editingDomain20);
		OperationHistoryFactory.getOperationHistory().undo(op, null, null);
		waitForModelLoading();

		// Verify that proxies are resolved
		assertEquals(2, platform.getComponentTypes().size());

		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertFalse(interface1.getRequiringPorts().get(0).eIsProxy());

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertFalse(interface2.getRequiringPorts().get(0).eIsProxy());

	}

	// Delete an object which is a Container by using EcoreUtil.remove()-
	// ->object had been removed before being proxified
	// -> Object to delete and it children will be proxified
	// Index of object to delete is -1. Its children's proxy URI are calculated base on container proxy URI
	public void testRemoveContainer() throws OperationCanceledException, ExecutionException {

		ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(contextResource);
		assertEquals(componentType1ProxyUri, extendedResource.getURI(componentType1).toString());
		assertEquals(port1ProxyUri, extendedResource.getURI(port1).toString());
		assertEquals(port2ProxyUri, extendedResource.getURI(port2).toString());
		assertEquals(componentType2ProxyUri, extendedResource.getURI(componentType2).toString());

		// delete object
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				EcoreUtil.remove(componentType1);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable, "remove componentType");
		waitForModelLoading();

		assertTrue(ModelSaveManager.INSTANCE.isDirty(contextResource));
		assertEquals(1, platform.getComponentTypes().size());

		// Verify that removed object and its children were proxified
		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertTrue(interface1.getRequiringPorts().get(0).eIsProxy());
		InternalEObject internalEObject = (InternalEObject) interface1.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port2", internalEObject.eProxyURI().toString());

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertTrue(interface2.getRequiringPorts().get(0).eIsProxy());
		InternalEObject internalEObject2 = (InternalEObject) interface2.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port1", internalEObject2.eProxyURI().toString());

	}

	// Delete an object which is a Container by using EcoreUtil.remove()-
	// ->object had been removed before being proxified
	// -> Object to delete and it children will be proxified
	// Index of object to delete is -1. Its children's proxy URI are calculated base on container proxy URI
	// Undo deleting-> proxies are resolved. Reference navigate to restored object
	public void testRemoveContainer_Undo() throws OperationCanceledException, ExecutionException {

		ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(contextResource);
		assertEquals(componentType1ProxyUri, extendedResource.getURI(componentType1).toString());
		assertEquals(port1ProxyUri, extendedResource.getURI(port1).toString());
		assertEquals(port2ProxyUri, extendedResource.getURI(port2).toString());
		assertEquals(componentType2ProxyUri, extendedResource.getURI(componentType2).toString());

		// delete object
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				EcoreUtil.remove(componentType1);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable, "remove componentType");
		waitForModelLoading();

		assertTrue(ModelSaveManager.INSTANCE.isDirty(contextResource));
		assertEquals(1, platform.getComponentTypes().size());

		// Verify that removed object and its children were proxified
		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertTrue(interface1.getRequiringPorts().get(0).eIsProxy());
		InternalEObject internalEObject = (InternalEObject) interface1.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port2", internalEObject.eProxyURI().toString());

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertTrue(interface2.getRequiringPorts().get(0).eIsProxy());
		InternalEObject internalEObject2 = (InternalEObject) interface2.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port1", internalEObject2.eProxyURI().toString());

		// Undo
		IUndoContext op = WorkspaceTransactionUtil.getUndoContext(refWks.editingDomain20);
		OperationHistoryFactory.getOperationHistory().undo(op, null, null);
		waitForModelLoading();
		assertEquals(2, platform.getComponentTypes().size());
		// Verify that reference are resolved
		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertFalse(interface1.getRequiringPorts().get(0).eIsProxy());
		assertEquals(port2, interface1.getRequiringPorts().get(0));

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertFalse(interface2.getRequiringPorts().get(0).eIsProxy());
		assertEquals(port1, interface2.getRequiringPorts().get(0));

	}

	// Delete an object which is not a Container by using EcoreUtil.remove()
	// ->object had been removed before being proxified
	// -> Object to delete and it children will be proxified
	// Index of object to delete is -1.
	// Undo deleting-> proxies are resolved. Reference navigate to restored object
	public void testRemoveSingleObject_Undo() throws OperationCanceledException, ExecutionException {

		// delete object
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				EcoreUtil.remove(port1);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable, "remove the first port");
		waitForModelLoading();

		assertTrue(ModelSaveManager.INSTANCE.isDirty(contextResource));
		assertEquals(1, componentType1.getPorts().size());

		// Verify that removed object and its children were proxified

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertTrue(interface2.getRequiringPorts().get(0).eIsProxy());
		InternalEObject internalEObject = (InternalEObject) interface2.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port1", internalEObject.eProxyURI().toString());

		// Undo
		IUndoContext op = WorkspaceTransactionUtil.getUndoContext(refWks.editingDomain20);
		OperationHistoryFactory.getOperationHistory().undo(op, null, null);
		waitForModelLoading();
		assertEquals(2, componentType1.getPorts().size());
		// Verify that reference are resolved

		assertFalse(interface2.getRequiringPorts().isEmpty());
		assertNotNull(interface2.getRequiringPorts().get(0));
		assertFalse(interface2.getRequiringPorts().get(0).eIsProxy());
		assertEquals(port1, interface2.getRequiringPorts().get(0));

	}

	// Delete an object which is a Container by using EcoreUtil.remove()-
	// ->object had been removed before being proxified
	// -> Object to delete and it children will be proxified
	// Index of object to delete is -1. Its children's proxy URI are calculated base on container proxy URI
	// Create a new object with the same value with deleted object-> proxies are not resolved
	public void testRemoveObject_CreateNew() throws OperationCanceledException, ExecutionException {

		// delete object
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				EcoreUtil.remove(port2);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable, "remove the last port");
		waitForModelLoading();

		assertTrue(ModelSaveManager.INSTANCE.isDirty(contextResource));
		assertEquals(1, componentType1.getPorts().size());

		// Verify that removed object and its children were proxified

		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertTrue(interface1.getRequiringPorts().get(0).eIsProxy());
		InternalEObject internalEObject = (InternalEObject) interface1.getRequiringPorts().get(0);
		assertEquals("hb:/#//ComponentType1/Port2", internalEObject.eProxyURI().toString());

		// Create a new one
		Runnable runnable2 = new Runnable() {
			@Override
			public void run() {
				Port newPort = TypeModel20Factory.eINSTANCE.createPort();
				newPort.setName("newPort");
				componentType1.getPorts().add(newPort);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable2, "add new port");
		waitForModelLoading();
		assertEquals(2, componentType1.getPorts().size());
		// Verify that reference are resolved

		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertTrue(interface1.getRequiringPorts().get(0).eIsProxy());
		// assertEquals("newPort", interface2.getRequiringPorts().get(0).getName());

	}

	// Move an object to a new container.
	// References are re-navigated
	public void testMoveObject() {

		// delete object
		refWks.editingDomain20.getCommandStack().execute(new RecordingCommand(refWks.editingDomain20) {
			@Override
			protected void doExecute() {
				port2.setOwner(componentType2);
			}
		});
		assertTrue(ModelSaveManager.INSTANCE.isDirty(contextResource));

		assertEquals(1, componentType1.getPorts().size());
		assertEquals(1, componentType2.getPorts().size());

		assertFalse(interface1.getRequiringPorts().isEmpty());
		assertNotNull(interface1.getRequiringPorts().get(0));
		assertFalse(interface1.getRequiringPorts().get(0).eIsProxy());
		assertEquals(componentType2, interface1.getRequiringPorts().get(0).getOwner());
	}
}
