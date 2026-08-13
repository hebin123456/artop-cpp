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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This action opens a Filters Dialog and notifies the Marker View if the user has modified the filter via the filters
 * dialog.
 */
class FiltersAction extends Action {

	private MarkerView view;

	/**
	 * Creates the action
	 */
	public FiltersAction(MarkerView view) {
		super(MarkerMessages.filtersAction_title);
		setImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/filter_ps.gif")); //$NON-NLS-1$
		setToolTipText(MarkerMessages.filtersAction_tooltip);
		this.view = view;
		setEnabled(true);
	}

	/**
	 * Opens the dialog. Notifies the view if the filter has been modified.
	 */
	@Override
	public void run() {
		view.openFiltersDialog();
	}
}
