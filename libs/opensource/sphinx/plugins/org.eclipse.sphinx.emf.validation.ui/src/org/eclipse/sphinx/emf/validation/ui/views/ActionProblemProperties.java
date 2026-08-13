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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * ActionProblemProperties is the action for opening the properties in the problems view.
 */
public class ActionProblemProperties extends MarkerSelectionProviderAction {

	private IWorkbenchPart part;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param part
	 * @param provider
	 */
	public ActionProblemProperties(IWorkbenchPart part, ISelectionProvider provider) {
		super(provider, MarkerMessages.propertiesAction_title);
		setEnabled(false);
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {

		IMarker marker = getSelectedMarker();
		DialogMarkerProperties dialog = new DialogProblemProperties(part.getSite().getShell());
		dialog.setMarker(marker);
		dialog.open();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(Util.isSingleConcreteSelection(selection));
	}
}
