/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - added support for problem markers on model objects (rather than 
 *               only on workspace resources). Unfortunately, there was no other 
 *               choice than copying the whole code from 
 *               org.eclipse.ui.views.markers.internal for that purpose because 
 *               many of the relevant classes, methods, and fields are private or
 *               package private.
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.validation.ui.SphinxValidationUiActivator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

/**
 * MarkerSelectionProviderAction is the abstract super class of the selection provider actions used by marker views.
 */
public abstract class MarkerSelectionProviderAction extends SelectionProviderAction {

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param provider
	 * @param text
	 */
	public MarkerSelectionProviderAction(ISelectionProvider provider, String text) {
		super(provider, text);

	}

	/**
	 * Get the selected markers in the receiver.
	 * 
	 * @return IMarker[]
	 */
	IMarker[] getSelectedMarkers() {

		return getSelectedMarkers(getStructuredSelection());
	}

	/**
	 * Return the selected markers for the structured selection.
	 * 
	 * @param structured
	 *            IStructuredSelection
	 * @return IMarker[]
	 */
	IMarker[] getSelectedMarkers(IStructuredSelection structured) {
		Object[] selection = structured.toArray();
		ArrayList markers = new ArrayList();
		for (Object element : selection) {
			Object object = element;
			if (!(object instanceof MarkerNode)) {
				return new IMarker[0];// still pending
			}
			MarkerNode marker = (MarkerNode) object;
			if (marker.isConcrete()) {
				markers.add(((ConcreteMarker) object).getMarker());
			}
		}

		return (IMarker[]) markers.toArray(new IMarker[markers.size()]);
	}

	/**
	 * Get the selected marker in the receiver.
	 * 
	 * @return IMarker
	 */
	IMarker getSelectedMarker() {

		ConcreteMarker selection = (ConcreteMarker) getStructuredSelection().getFirstElement();
		return selection.getMarker();
	}

	/**
	 * Execute the specified undoable operation
	 */
	void execute(IUndoableOperation operation, String title, IProgressMonitor monitor, IAdaptable uiInfo) {
		try {
			PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(operation, monitor, uiInfo);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof CoreException) {
				ErrorDialog.openError(WorkspaceUndoUtil.getShell(uiInfo), title, null, ((CoreException) e.getCause()).getStatus());
			} else {
				PlatformLogUtil.logAsError(SphinxValidationUiActivator.getDefault(), title + e);
			}
		}
	}
}
