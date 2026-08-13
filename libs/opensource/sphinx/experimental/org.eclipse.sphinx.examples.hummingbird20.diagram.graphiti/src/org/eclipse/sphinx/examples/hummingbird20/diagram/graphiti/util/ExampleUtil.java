/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.util;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class ExampleUtil {

	/**
	 * Opens an simple input dialog with OK and Cancel buttons.
	 * <p>
	 * 
	 * @param dialogTitle
	 *            the dialog title, or <code>null</code> if none
	 * @param dialogMessage
	 *            the dialog message, or <code>null</code> if none
	 * @param initialValue
	 *            the initial input value, or <code>null</code> if none (equivalent to the empty string)
	 * @return the string
	 */
	public static String askString(String dialogTitle, String dialogMessage, String initialValue) {
		String ret = null;
		Shell shell = getShell();
		InputDialog inputDialog = new InputDialog(shell, dialogTitle, dialogMessage, initialValue, null);
		int retDialog = inputDialog.open();
		if (retDialog == Window.OK) {
			ret = inputDialog.getValue();
		}
		return ret;
	}

	/**
	 * Returns the currently active Shell.
	 * 
	 * @return The currently active Shell.
	 */
	private static Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

}
