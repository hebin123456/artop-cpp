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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sphinx.emf.validation.ui.views.MarkerAdapter.MarkerCategory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The ActionSelectAll is the action for selecting all of the entries.
 */
public class ActionSelectAll extends MarkerSelectionProviderAction {

	private MarkerView view;

	/**
	 * Create a new instance of the receiver with the supplied
	 * 
	 * @param markerView
	 */
	public ActionSelectAll(MarkerView markerView) {
		super(markerView.getViewer(), MarkerMessages.selectAllAction_title);
		setEnabled(true);
		view = markerView;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		if (view.getMarkerAdapter().isBuilding()) {
			return;
		}

		IRunnableWithProgress selectionRunnableWithProgress = new IRunnableWithProgress() {
			/*
			 * (non-Javadoc)
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			public void run(IProgressMonitor monitor) {

				try {
					monitor.beginTask(MarkerMessages.selectAllAction_title, 100);
					monitor.subTask(MarkerMessages.selectAllAction_calculating);
					if (view.getMarkerAdapter().isShowingHierarchy()) {

						if (monitor.isCanceled()) {
							return;
						}

						monitor.worked(10);
						PlatformUI.getWorkbench().getDisplay().readAndDispatch();
						MarkerCategory[] categories = view.getMarkerAdapter().getCategories();
						int totalSize = 0;
						for (MarkerCategory markerCategory : categories) {
							totalSize += markerCategory.getDisplayedSize();
						}
						monitor.worked(10);
						PlatformUI.getWorkbench().getDisplay().readAndDispatch();
						Object[] selection = new Object[totalSize];
						int index = 0;
						for (MarkerCategory categorie : categories) {
							MarkerNode[] children = categorie.getChildren();
							System.arraycopy(children, 0, selection, index, children.length);
							index += children.length;
						}
						monitor.worked(10);
						if (monitor.isCanceled()) {
							return;
						}
						PlatformUI.getWorkbench().getDisplay().readAndDispatch();
						monitor.subTask(MarkerMessages.selectAllAction_applying);
						getSelectionProvider().setSelection(new StructuredSelection(selection));
					} else {
						if (monitor.isCanceled()) {
							return;
						}
						monitor.worked(10);
						List selection = view.getMarkerAdapter().getCurrentMarkers().asList();
						monitor.worked(10);
						monitor.subTask(MarkerMessages.selectAllAction_applying);
						PlatformUI.getWorkbench().getDisplay().readAndDispatch();
						getSelectionProvider().setSelection(new StructuredSelection(selection));
					}
				} finally {
					monitor.done();
				}

			}
		};

		try {
			PlatformUI.getWorkbench().getProgressService().run(false, false, selectionRunnableWithProgress);
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(StatusUtil.newStatus(IStatus.ERROR, e.getLocalizedMessage(), e), StatusManager.LOG);
		} catch (InterruptedException e) {
			StatusManager.getManager().handle(StatusUtil.newStatus(IStatus.ERROR, e.getLocalizedMessage(), e), StatusManager.LOG);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(!selection.isEmpty());
	}
}
