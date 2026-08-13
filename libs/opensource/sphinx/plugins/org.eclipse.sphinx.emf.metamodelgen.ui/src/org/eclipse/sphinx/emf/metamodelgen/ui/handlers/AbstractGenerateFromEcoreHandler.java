/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.metamodelgen.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;
import org.eclipse.sphinx.platform.ui.operations.RunnableWithProgressAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractGenerateFromEcoreHandler extends AbstractHandler {

	/*
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Retrieve selected Ecore file
		ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		IFile ecoreFile = getEcoreFile(selection);
		if (ecoreFile == null) {
			throw new ExecutionException("No Ecore file selected"); //$NON-NLS-1$
		}

		// Create appropriate generate from Ecore operation
		IWorkspaceOperation operation = createGenerateFromEcoreOperation(ecoreFile);

		// Run generated from Ecore operation in a progress monitor dialog
		try {
			Shell shell = HandlerUtil.getActiveShell(event);
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			dialog.run(true, true, new RunnableWithProgressAdapter(operation));
		} catch (InterruptedException ex) {
			// Operation has been canceled by user, do nothing
		} catch (InvocationTargetException ex) {
			throw new ExecutionException(ex.getMessage(), ex);
		}

		return null;
	}

	protected IFile getEcoreFile(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() == 1) {
				Object selected = structuredSelection.getFirstElement();
				if (selected instanceof IFile) {
					IFile selectedFile = (IFile) selected;
					String fileExtension = selectedFile.getFileExtension();
					if (fileExtension != null && fileExtension.equals(EcorePackage.eNAME)) {
						return selectedFile;
					}
				}
			}
		}
		return null;
	}

	protected abstract IWorkspaceOperation createGenerateFromEcoreOperation(IFile ecoreFile);
}
