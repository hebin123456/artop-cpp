/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [346829] Enhance DiagnosticUI API
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.ui.util;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.ui.dialogs.DiagnosticDialog;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for displaying {@link Diagnostic}s resulting from a model validation in a dialog.
 */
public class DiagnosticUI {

	/**
	 * Displays a dialog for given list of {@link Diagnostic diagnostic}s.
	 * 
	 * @param diagnostics
	 *            The list of {@link Diagnostic diagnostic}s to be displayed.
	 * @return The code of the button that was pressed to close the dialog. This will be {@link Window#OK} if the OK
	 *         button was pressed, or {@link Window#CANCEL} if this dialog's close window decoration or the ESC key was
	 *         used.
	 */
	public static int showDiagnostic(List<Diagnostic> diagnostics) {
		return showDiagnostic(diagnostics, null, null);
	}

	/**
	 * Displays a dialog with specified title and message for given list of {@link Diagnostic diagnostic}s.
	 * 
	 * @param diagnostics
	 *            The list of {@link Diagnostic diagnostic}s to be displayed.
	 * @param title
	 *            The dialog title to be used.
	 * @param message
	 *            The dialog message to be displayed above the diagnostics.
	 * @return The code of the button that was pressed to close the dialog. This will be {@link Window#OK} if the OK
	 *         button was pressed, or {@link Window#CANCEL} if this dialog's close window decoration or the ESC key was
	 *         used.
	 */
	public static int showDiagnostic(List<Diagnostic> diagnostics, String title, String message) {
		Assert.isNotNull(diagnostics);

		return diagnostics.size() == 1 ? showDiagnosticSingle(diagnostics.get(0), title, message) : showDiagnosticMulti(diagnostics, title, message);
	}

	/**
	 * Displays a dialog for given {@link Diagnostic diagnostic}.
	 * 
	 * @param diagnostic
	 *            The {@link Diagnostic diagnostic} to be displayed.
	 * @return The code of the button that was pressed to close the dialog. This will be {@link Window#OK} if the OK
	 *         button was pressed, or {@link Window#CANCEL} if this dialog's close window decoration or the ESC key was
	 *         used.
	 */
	public static int showDiagnosticSingle(Diagnostic diagnostic) {
		return showDiagnosticSingle(diagnostic, null, null);
	}

	/**
	 * Displays a dialog with specified title and message for given {@link Diagnostic diagnostic}.
	 * 
	 * @param diagnostic
	 *            The {@link Diagnostic diagnostic} to be displayed.
	 * @param title
	 *            The dialog title to be used.
	 * @param message
	 *            The dialog message to be displayed above the diagnostic.
	 * @return The code of the button that was pressed to close the dialog. This will be {@link Window#OK} if the OK
	 *         button was pressed, or {@link Window#CANCEL} if this dialog's close window decoration or the ESC key was
	 *         used.
	 */
	public static int showDiagnosticSingle(Diagnostic diagnostic, String title, String message) {
		int severity = diagnostic.getSeverity();

		if (severity == Diagnostic.ERROR || severity == Diagnostic.WARNING) {
			if (title == null) {
				title = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationProblems_title"); //$NON-NLS-1$
			}
			if (message == null) {
				message = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationProblems_message"); //$NON-NLS-1$
			}
		} else {
			if (title == null) {
				title = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationResults_title"); //$NON-NLS-1$
			}
			if (message == null) {
				message = EMFEditUIPlugin.INSTANCE
						.getString(severity == Diagnostic.OK ? "_UI_ValidationOK_message" : "_UI_ValidationResults_message"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		int result = Window.CANCEL;
		if (severity == Diagnostic.OK) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message);
		} else {
			result = DiagnosticDialog.open(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message, diagnostic);
		}
		return result;
	}

	/**
	 * Displays a dialog for given list of {@link Diagnostic diagnostic}s.
	 * 
	 * @param diagnostics
	 *            The list of {@link Diagnostic diagnostic}s to be displayed.
	 * @return The code of the button that was pressed to close the dialog. This will be {@link Window#OK} if the OK
	 *         button was pressed, or {@link Window#CANCEL} if this dialog's close window decoration or the ESC key was
	 *         used.
	 */
	public static int showDiagnosticMulti(List<Diagnostic> diagnostics) {
		return showDiagnosticMulti(diagnostics, null, null);
	}

	/**
	 * Displays a dialog with specified title and message for given list of {@link Diagnostic diagnostic}s.
	 * 
	 * @param diagnostics
	 *            The list of {@link Diagnostic diagnostic}s to be displayed.
	 * @param title
	 *            The dialog title to be used.
	 * @param message
	 *            The dialog message to be displayed above the diagnostics.
	 * @return The code of the button that was pressed to close the dialog. This will be {@link Window#OK} if the OK
	 *         button was pressed, or {@link Window#CANCEL} if this dialog's close window decoration or the ESC key was
	 *         used.
	 */
	public static int showDiagnosticMulti(List<Diagnostic> diagnostics, String title, String message) {
		Assert.isNotNull(diagnostics);

		boolean isOk = true;
		boolean isInfo = false;
		for (Diagnostic diagnostic : diagnostics) {
			int severity = diagnostic.getSeverity();
			if (severity == Diagnostic.ERROR || severity == Diagnostic.WARNING) {
				isOk = false;
				break;
			} else if (severity == Diagnostic.INFO) {
				isInfo = true;
			}
		}

		if (!isOk) {
			if (title == null) {
				title = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationProblems_title"); //$NON-NLS-1$
			}
			if (message == null) {
				message = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationProblems_message"); //$NON-NLS-1$
			}
		} else {
			if (title == null) {
				title = EMFEditUIPlugin.INSTANCE.getString("_UI_ValidationResults_title"); //$NON-NLS-1$
			}
			if (message == null) {
				message = EMFEditUIPlugin.INSTANCE.getString(!isInfo ? "_UI_ValidationOK_message" : "_UI_ValidationResults_message"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		int result = Window.CANCEL;
		if (isOk) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message);
		} else {
			result = ExtendedDiagnosticDialog.open(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, message, diagnostics);
		}
		return result;
	}
}
