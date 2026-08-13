/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.ui.actions.contributor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sphinx.emf.validation.preferences.IValidationPreferences;
import org.eclipse.sphinx.emf.validation.ui.SphinxValidationUiActivator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class AutomaticValidationActionContributor implements IWorkbenchWindowActionDelegate, IPropertyChangeListener {

	private IAction me = null;

	private IPreferenceStore preferenceStore = SphinxValidationUiActivator.getDefault().getPreferenceStore();

	@Override
	public void init(IWorkbenchWindow window) {
		preferenceStore.addPropertyChangeListener(this);
	}

	@Override
	public void dispose() {
		preferenceStore.removePropertyChangeListener(this);
	}

	@Override
	public void run(IAction action) {
		preferenceStore.setValue(IValidationPreferences.PREF_ENABLE_AUTOMATIC_VALIDATION, action.isChecked());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Init action part
		if (me == null) {
			me = action;
			me.setChecked(preferenceStore.getBoolean(IValidationPreferences.PREF_ENABLE_AUTOMATIC_VALIDATION));
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (me != null) {
			if (event.getProperty().equals(IValidationPreferences.PREF_ENABLE_AUTOMATIC_VALIDATION)) {
				me.setChecked(preferenceStore.getBoolean(IValidationPreferences.PREF_ENABLE_AUTOMATIC_VALIDATION));
			}
		}
	}
}
