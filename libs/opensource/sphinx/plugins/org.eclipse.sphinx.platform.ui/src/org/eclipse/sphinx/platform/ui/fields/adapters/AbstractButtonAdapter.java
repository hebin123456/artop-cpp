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
package org.eclipse.sphinx.platform.ui.fields.adapters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.eclipse.sphinx.platform.ui.fields.IField;

/**
 * 
 */
public abstract class AbstractButtonAdapter implements IButtonAdapter {

	/**
	 * Protected constructor.
	 */
	protected AbstractButtonAdapter() {
		// Nothing to do.
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.ui.fields.adapters.IButtonAdapter#changeControlPressed(org.eclipse.sphinx.platform
	 * .ui.fields .BasicField)
	 */
	@Override
	public void changeControlPressed(IField field) {
		Assert.isNotNull(field);
		Dialog dialog = createDialog();
		int result = dialog.open();
		if (result == Window.OK) {
			performOk(field, dialog);
		} else {
			performCancel(field, dialog);
		}
	}

	protected final Dialog createDialog() {
		Dialog dialog = doCreateDialog();
		if (dialog == null) {
			throw new RuntimeException(NLS.bind(PlatformMessages.error_mustNotBeNull, "dialog")); //$NON-NLS-1$
		}
		return dialog;
	}

	protected abstract Dialog doCreateDialog();

	protected void performOk(IField field, Dialog dialog) {
		// Default implementation does nothing.
	}

	protected void performCancel(IField field, Dialog dialog) {
		// Default implementation does nothing.
	}
}
