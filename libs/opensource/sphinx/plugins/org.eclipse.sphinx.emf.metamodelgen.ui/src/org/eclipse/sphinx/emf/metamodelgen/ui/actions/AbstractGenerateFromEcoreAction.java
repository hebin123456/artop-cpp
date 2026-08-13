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
package org.eclipse.sphinx.emf.metamodelgen.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.metamodelgen.ui.internal.Activator;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;
import org.eclipse.sphinx.platform.ui.operations.RunnableWithProgressAdapter;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public abstract class AbstractGenerateFromEcoreAction extends BaseSelectionListenerAction {

	protected IFile selectedEcoreFile;

	public AbstractGenerateFromEcoreAction(String text) {
		super(text);
	}

	/*
	 * @see
	 * org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection
	 * )
	 */
	@Override
	public boolean updateSelection(IStructuredSelection selection) {
		// Check if selection contains precisely 1 Ecore file
		selectedEcoreFile = null;
		if (selection.size() == 1) {
			Object selected = selection.getFirstElement();
			if (selected instanceof IFile) {
				IFile selectedFile = (IFile) selected;
				String fileExtension = selectedFile.getFileExtension();
				if (fileExtension != null && fileExtension.equals(EcorePackage.eNAME)) {
					selectedEcoreFile = selectedFile;
				}
			}
		}
		return selectedEcoreFile != null;
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		// Create appropriate generate from Ecore operation
		IWorkspaceOperation operation = createGenerateFromEcoreOperation(selectedEcoreFile);

		// Run generated from Ecore operation in a progress monitor dialog
		try {
			Shell shell = ExtendedPlatformUI.getActiveShell();
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			dialog.run(true, true, new RunnableWithProgressAdapter(operation));
		} catch (InterruptedException ex) {
			// Operation has been canceled by user, do nothing
		} catch (InvocationTargetException ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	protected abstract IWorkspaceOperation createGenerateFromEcoreOperation(IFile ecoreFile);
}
