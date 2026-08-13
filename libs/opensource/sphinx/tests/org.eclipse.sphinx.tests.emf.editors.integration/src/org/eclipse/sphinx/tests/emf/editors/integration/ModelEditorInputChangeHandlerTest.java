/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.editors.integration;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.editors.IModelEditorInputChangeAnalyzer;
import org.eclipse.sphinx.emf.editors.IModelEditorInputChangeHandler;
import org.eclipse.sphinx.emf.editors.ModelEditorInputSynchronizer;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.sphinx.tests.emf.editors.integration.internal.Activator;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.ui.IEditorInput;

public class ModelEditorInputChangeHandlerTest extends DefaultIntegrationTestCase {

	private IModelEditorInputChangeHandler editorInputChangeHandler;
	private ModelEditorInputSynchronizer editorInputSynchronizer;

	public ModelEditorInputChangeHandlerTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_B);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_C);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E);

		// Remove all project references except:
		// HB_PROJECT_NAME_20_E -> HB_PROJECT_NAME_20_D
		Map<String, Set<String>> projectReferences = getProjectReferences();
		projectReferences.remove(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_D);

		// Set test plug-in for retrieving test input resources
		setTestPlugin(Activator.getPlugin());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		editorInputChangeHandler = new ModelEditorInputChangeHandler();
	}

	public void testHandleEditorInputObjectAdded() throws OperationCanceledException, ExecutionException {
		Set<EObject> addedObjects = new HashSet<EObject>();
		final ParameterValue parameterValue = InstanceModel20Factory.eINSTANCE.createParameterValue();
		addedObjects.add(parameterValue);

		IEditorInput editorInput = EasyMock.createNiceMock(IEditorInput.class);
		IModelEditorInputChangeAnalyzer editorInputChangeAnalyzer = EasyMock.createNiceMock(IModelEditorInputChangeAnalyzer.class);
		expect(editorInputChangeAnalyzer.containsEditorInputObject(editorInput, addedObjects)).andReturn(true);
		replay(editorInput, editorInputChangeAnalyzer);

		editorInputSynchronizer = new ModelEditorInputSynchronizer(editorInput, refWks.editingDomain20, editorInputChangeAnalyzer,
				editorInputChangeHandler);

		Resource resource20E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" //$NON-NLS-1$
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);

		assertNotNull(resource20E_1);
		assertFalse(resource20E_1.getContents().isEmpty());
		Application application_20E_1 = (Application) resource20E_1.getContents().get(0);
		assertNotNull(application_20E_1);
		assertFalse(application_20E_1.getComponents().isEmpty());
		final Component hb20Component = application_20E_1.getComponents().get(0);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				hb20Component.getParameterValues().add(parameterValue);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable, "adding parameter value"); //$NON-NLS-1$

		assertTrue(((ModelEditorInputChangeHandler) editorInputChangeHandler).isEditorInputAdded());

		editorInputSynchronizer.dispose();
	}

	public void testHandleEditorInputObjectRemoved() throws OperationCanceledException, ExecutionException {
		Resource resource20E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" //$NON-NLS-1$
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);

		assertNotNull(resource20E_1);
		assertFalse(resource20E_1.getContents().isEmpty());
		final Application application_20E_1 = (Application) resource20E_1.getContents().get(0);
		assertNotNull(application_20E_1);
		assertFalse(application_20E_1.getComponents().isEmpty());
		final Component hb20Component = application_20E_1.getComponents().get(0);

		IEditorInput editorInput = EasyMock.createNiceMock(IEditorInput.class);
		IModelEditorInputChangeAnalyzer editorInputChangeAnalyzer = EasyMock.createNiceMock(IModelEditorInputChangeAnalyzer.class);
		Set<EObject> removedObjects = new HashSet<EObject>();
		removedObjects.add(hb20Component);
		expect(editorInputChangeAnalyzer.containsEditorInputObject(editorInput, removedObjects)).andReturn(true);
		replay(editorInput, editorInputChangeAnalyzer);

		editorInputSynchronizer = new ModelEditorInputSynchronizer(editorInput, refWks.editingDomain20, editorInputChangeAnalyzer,
				editorInputChangeHandler);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				application_20E_1.getComponents().remove(hb20Component);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable, "removing parameter value"); //$NON-NLS-1$

		assertTrue(((ModelEditorInputChangeHandler) editorInputChangeHandler).isEditorInputRemoved());

		editorInputSynchronizer.dispose();
	}

	public void testHandleEditorInputObjectMoved() throws OperationCanceledException, ExecutionException {
		Resource resource20E_1 = refWks.editingDomain20.getResourceSet().getResource(
				URI.createPlatformResourceURI(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_E + "/" //$NON-NLS-1$
						+ DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20E_1, true), false);

		assertNotNull(resource20E_1);
		assertFalse(resource20E_1.getContents().isEmpty());
		final Application application_20E_1 = (Application) resource20E_1.getContents().get(0);
		assertNotNull(application_20E_1);
		assertFalse(application_20E_1.getComponents().isEmpty());
		final Component hb20Component = application_20E_1.getComponents().get(0);

		IEditorInput editorInput = EasyMock.createNiceMock(IEditorInput.class);
		IModelEditorInputChangeAnalyzer editorInputChangeAnalyzer = EasyMock.createNiceMock(IModelEditorInputChangeAnalyzer.class);
		Set<EObject> movedObjects = new HashSet<EObject>();
		movedObjects.add(hb20Component);
		expect(editorInputChangeAnalyzer.containsEditorInputObject(editorInput, movedObjects)).andReturn(true);
		replay(editorInput, editorInputChangeAnalyzer);

		editorInputSynchronizer = new ModelEditorInputSynchronizer(editorInput, refWks.editingDomain20, editorInputChangeAnalyzer,
				editorInputChangeHandler);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// TODO create EMF command to simulate move operation (delete + add)
				// Command command = DragAndDropCommand.create(domain, unwrappedTarget, 0.5f, event.operations,
				// DND.DROP_MOVE, selectedEObjects);

				// application_20E_1.getComponents().remove(hb20Component);
				// Application application = InstanceModel20Factory.eINSTANCE.createApplication();
				// application.getComponents().add(hb20Component);
			}
		};
		WorkspaceTransactionUtil.executeInWriteTransaction(refWks.editingDomain20, runnable, "Move objects"); //$NON-NLS-1$

		// assertTrue(((ModelEditorInputChangeHandler) editorInputChangeHandler).isEditorInputMoved());

		editorInputSynchronizer.dispose();
	}
}
