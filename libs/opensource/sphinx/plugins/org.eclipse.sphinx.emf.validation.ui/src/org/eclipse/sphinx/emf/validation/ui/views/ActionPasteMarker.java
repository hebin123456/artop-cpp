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
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Pastes one or more bookmark(s) from the clipboard into the bookmark navigator.
 */
public class ActionPasteMarker extends MarkerSelectionProviderAction {

	private IWorkbenchPart part;

	private Clipboard clipboard;

	private String[] pastableTypes;

	private String markerName;

	/**
	 * Creates the action.
	 * 
	 * @param part
	 * @param provider
	 * @param markerName
	 *            the name used to describe the specific kind of marker being pasted.
	 */
	public ActionPasteMarker(IWorkbenchPart part, ISelectionProvider provider, String markerName) {
		super(provider, MarkerMessages.pasteAction_title);
		this.part = part;
		pastableTypes = new String[0];
		this.markerName = markerName;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setEnabled(false);
	}

	void setClipboard(Clipboard clipboard) {
		this.clipboard = clipboard;
	}

	/**
	 * Copies the marker(s) from the clipboard to the bookmark navigator view.
	 */
	@Override
	public void run() {
		// Get the markers from the clipboard
		MarkerTransfer transfer = MarkerTransfer.getInstance();
		IMarker[] markerData = (IMarker[]) clipboard.getContents(transfer);
		paste(markerData);
	}

	void paste(final IMarker[] markers) {
		if (markers == null) {
			return;
		}

		final ArrayList<String> newMarkerTypes = new ArrayList<String>();
		final ArrayList<Map> newMarkerAttributes = new ArrayList<Map>();
		final ArrayList<IResource> newMarkerResources = new ArrayList<IResource>();

		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					for (IMarker marker : markers) {
						// Collect info about the markers to be pasted.
						newMarkerTypes.add(marker.getType());
						newMarkerResources.add(marker.getResource());
						newMarkerAttributes.add(marker.getAttributes());

					}
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(part.getSite().getShell(), MarkerMessages.PasteMarker_errorTitle, null, e.getStatus());
			return;
		}

		final String[] types = newMarkerTypes.toArray(new String[newMarkerTypes.size()]);
		final Map[] attrs = newMarkerAttributes.toArray(new Map[newMarkerAttributes.size()]);
		final IResource[] resources = newMarkerResources.toArray(new IResource[newMarkerResources.size()]);
		String operationTitle = NLS.bind(MarkerMessages.qualifiedMarkerCommand_title, MarkerMessages.pasteAction_title, markerName);
		final CreateMarkersOperation op = new CreateMarkersOperation(types, attrs, resources, operationTitle);
		execute(op, MarkerMessages.PasteMarker_errorTitle, null, WorkspaceUndoUtil.getUIInfoAdapter(part.getSite().getShell()));

		// Need to do this in an asyncExec, even though we're in the UI thread
		// here,
		// since the marker view updates itself with the addition in an
		// asyncExec,
		// which hasn't been processed yet.
		// Must be done outside the create marker operation above since
		// notification for add is
		// sent after the operation is executed.
		if (getSelectionProvider() != null && op.getMarkers() != null) {
			part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					getSelectionProvider().setSelection(new StructuredSelection(op.getMarkers()));
				}
			});
		}
	}

	void updateEnablement() {
		setEnabled(false);
		if (clipboard == null) {
			return;
		}

		// Paste if clipboard contains pastable markers
		MarkerTransfer transfer = MarkerTransfer.getInstance();
		IMarker[] markerData = (IMarker[]) clipboard.getContents(transfer);
		if (markerData == null || markerData.length < 1 || pastableTypes == null) {
			return;
		}
		for (IMarker marker : markerData) {
			try {
				if (!marker.exists()) {
					break;
				}
				boolean pastable = false;
				for (String pastableType : pastableTypes) {
					if (marker.isSubtypeOf(pastableType)) {
						pastable = true;
						break;
					}
				}
				if (!pastable) {
					return;
				}
				if (!Util.isEditable(marker)) {
					return;
				}
			} catch (CoreException e) {
				return;
			}
		}
		setEnabled(true);
	}

	/**
	 * @param strings
	 */
	void setPastableTypes(String[] strings) {
		pastableTypes = strings;
	}
}
