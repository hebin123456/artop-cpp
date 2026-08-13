/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20DiagramEditorPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @generated
 */
public class Hummingbird20NavigatorLinkHelper implements ILinkHelper {

	/**
	 * @generated
	 */
	private static IEditorInput getEditorInput(Diagram diagram) {
		Resource diagramResource = diagram.eResource();
		for (EObject nextEObject : diagramResource.getContents()) {
			if (nextEObject == diagram) {
				return new FileEditorInput(WorkspaceSynchronizer.getFile(diagramResource));
			}
			if (nextEObject instanceof Diagram) {
				break;
			}
		}
		URI uri = EcoreUtil.getURI(diagram);
		String editorName = uri.lastSegment() + '#' + diagram.eResource().getContents().indexOf(diagram);
		IEditorInput editorInput = new URIEditorInput(uri, editorName);
		return editorInput;
	}

	/**
	 * @generated
	 */
	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		IDiagramDocument document = Hummingbird20DiagramEditorPlugin.getInstance().getDocumentProvider().getDiagramDocument(anInput);
		if (document == null) {
			return StructuredSelection.EMPTY;
		}
		Diagram diagram = document.getDiagram();
		if (diagram == null || diagram.eResource() == null) {
			return StructuredSelection.EMPTY;
		}
		IFile file = WorkspaceSynchronizer.getFile(diagram.eResource());
		if (file != null) {
			Hummingbird20NavigatorItem item = new Hummingbird20NavigatorItem(diagram, file, false);
			return new StructuredSelection(item);
		}
		return StructuredSelection.EMPTY;
	}

	/**
	 * @generated
	 */
	@Override
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		if (aSelection == null || aSelection.isEmpty()) {
			return;
		}
		if (false == aSelection.getFirstElement() instanceof Hummingbird20AbstractNavigatorItem) {
			return;
		}

		Hummingbird20AbstractNavigatorItem abstractNavigatorItem = (Hummingbird20AbstractNavigatorItem) aSelection.getFirstElement();
		View navigatorView = null;
		if (abstractNavigatorItem instanceof Hummingbird20NavigatorItem) {
			navigatorView = ((Hummingbird20NavigatorItem) abstractNavigatorItem).getView();
		} else if (abstractNavigatorItem instanceof Hummingbird20NavigatorGroup) {
			Hummingbird20NavigatorGroup navigatorGroup = (Hummingbird20NavigatorGroup) abstractNavigatorItem;
			if (navigatorGroup.getParent() instanceof Hummingbird20NavigatorItem) {
				navigatorView = ((Hummingbird20NavigatorItem) navigatorGroup.getParent()).getView();
			}
		}
		if (navigatorView == null) {
			return;
		}
		IEditorInput editorInput = getEditorInput(navigatorView.getDiagram());
		IEditorPart editor = aPage.findEditor(editorInput);
		if (editor == null) {
			return;
		}
		aPage.bringToTop(editor);
		if (editor instanceof DiagramEditor) {
			DiagramEditor diagramEditor = (DiagramEditor) editor;
			ResourceSet diagramEditorResourceSet = diagramEditor.getEditingDomain().getResourceSet();
			EObject selectedView = diagramEditorResourceSet.getEObject(EcoreUtil.getURI(navigatorView), true);
			if (selectedView == null) {
				return;
			}
			GraphicalViewer graphicalViewer = (GraphicalViewer) diagramEditor.getAdapter(GraphicalViewer.class);
			EditPart selectedEditPart = (EditPart) graphicalViewer.getEditPartRegistry().get(selectedView);
			if (selectedEditPart != null) {
				graphicalViewer.select(selectedEditPart);
			}
		}
	}

}
