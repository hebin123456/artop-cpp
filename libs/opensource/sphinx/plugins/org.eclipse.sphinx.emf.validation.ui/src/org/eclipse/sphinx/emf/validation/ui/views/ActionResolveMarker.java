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
 *		Elektrobit - [572591]  Make MarkerResolutionDialog extensible
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.validation.ui.SphinxValidationUiActivator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * This action displays a list of resolutions for the selected marker
 * 
 * @since 0.7.0
 */
public class ActionResolveMarker extends MarkerSelectionProviderAction {

	private MarkerView view;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param markerView
	 * @param provider
	 */
	public ActionResolveMarker(MarkerView markerView, ISelectionProvider provider) {
		super(provider, MarkerMessages.resolveMarkerAction_title);
		setEnabled(false);
		setImageDescriptor(IDEInternalWorkbenchImages.getImageDescriptor(IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED));
		setDisabledImageDescriptor(IDEInternalWorkbenchImages.getImageDescriptor(IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED));
		view = markerView;
	}

	/**
	 * Displays a list of resolutions and performs the selection.
	 */
	@Override
	public void run() {

		IRunnableContext context = new ProgressMonitorDialog(view.getSite().getShell());
		final Object[] resolutions = new Object[1];

		IRunnableWithProgress resolutionsRunnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(NLS.bind(MarkerMessages.resolveMarkerAction_computationAction, getMarkerDescription()), 100);
				monitor.worked(25);
				resolutions[0] = IDE.getMarkerHelpRegistry().getResolutions(getSelectedMarker());
				monitor.done();
			}
		};

		Object service = view.getSite().getAdapter(IWorkbenchSiteProgressService.class);

		try {
			if (service == null) {
				PlatformUI.getWorkbench().getProgressService().runInUI(context, resolutionsRunnable, null);
			} else {
				((IWorkbenchSiteProgressService) service).runInUI(context, resolutionsRunnable, null);
			}
		} catch (InvocationTargetException exception) {
			handleException(exception);
			return;
		} catch (InterruptedException exception) {
			handleException(exception);
			return;
		}

		IMarkerResolution[] foundResolutions = (IMarkerResolution[]) resolutions[0];
		if (foundResolutions.length == 0) {
			MessageDialog.openInformation(view.getSite().getShell(), MarkerMessages.MarkerResolutionDialog_CannotFixTitle,
					NLS.bind(MarkerMessages.MarkerResolutionDialog_CannotFixMessage, getMarkerDescription()));
		} else {
			Dialog dialog = createMarkerResolutionDialog(foundResolutions, getSelectedMarker());
			dialog.open();
		}

	}

	/**
	 * @param resolutions
	 * @param selectedMarker
	 * @return the marker resolution dialog
	 */
	protected Dialog createMarkerResolutionDialog(IMarkerResolution[] resolutions, IMarker selectedMarker) {
		return new MarkerResolutionDialog(view.getSite().getShell(), selectedMarker, resolutions, view);
	}

	/**
	 * @return the marker view
	 */
	protected MarkerView getMarkerView() {
		return view;
	}

	/**
	 * Handle the exception.
	 * 
	 * @param exception
	 */
	private void handleException(Exception exception) {
		PlatformLogUtil.logAsError(SphinxValidationUiActivator.getDefault(), exception);
		ErrorDialog.openError(view.getSite().getShell(), MarkerMessages.Error,
				NLS.bind(MarkerMessages.MarkerResolutionDialog_CannotFixMessage, getMarkerDescription()), Util.errorStatus(exception));
	}

	/**
	 * Return the description of the marker.
	 * 
	 * @return String
	 */
	private String getMarkerDescription() {
		return Util.getProperty(IMarker.MESSAGE, getSelectedMarker());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void selectionChanged(IStructuredSelection selection) {

		if (Util.isSingleConcreteSelection(selection)) {
			if (IDE.getMarkerHelpRegistry().hasResolutions(getSelectedMarker())) {
				setEnabled(true);
				return;
			}
		}

		setEnabled(false);
	}
}
