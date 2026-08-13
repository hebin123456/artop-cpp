/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.emf.workspace.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.splitting.IModelSplitOperation;
import org.eclipse.sphinx.emf.splitting.IModelSplitPolicy;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.operations.BasicModelSplitOperation;
import org.eclipse.sphinx.platform.jobs.WorkspaceOperationWorkspaceJob;
import org.eclipse.sphinx.platform.ui.operations.RunnableWithProgressAdapter;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.handlers.HandlerUtil;

public class BasicModelSplitHandler extends AbstractHandler {

	protected URI resourceURI;
	protected IModelSplitPolicy modelSplitPolicy;
	private boolean runInBackground;

	public BasicModelSplitHandler(IModelSplitPolicy modelSplitPolicy) {
		Assert.isNotNull(modelSplitPolicy);
		this.modelSplitPolicy = modelSplitPolicy;
		setRunInBackground(false);
	}

	public boolean isRunInBackground() {
		return runInBackground;
	}

	public void setRunInBackground(boolean runInBackground) {
		this.runInBackground = runInBackground;
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		resourceURI = null;
		// Check if selection contains precisely 1 model file
		if (selection.size() == 1) {
			Object selected = selection.getFirstElement();
			if (selected instanceof IFile) {
				resourceURI = EcorePlatformUtil.createURI(((IFile) selected).getFullPath());
			}
		}
		return resourceURI != null;
	}

	protected IModelSplitOperation createModelSplitOperation() {
		return new BasicModelSplitOperation(resourceURI, modelSplitPolicy);
	}

	protected WorkspaceOperationWorkspaceJob createWorkspaceOperationJob(IModelSplitOperation operation) {
		return new WorkspaceOperationWorkspaceJob(operation);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = SelectionUtil.getStructuredSelection(HandlerUtil.getActiveMenuSelection(event));
		if (!updateSelection(selection)) {
			return null;
		}

		// Create the model split operation
		IModelSplitOperation operation = createModelSplitOperation();

		if (isRunInBackground()) {
			// Run the workflow operation in a workspace job
			WorkspaceOperationWorkspaceJob job = createWorkspaceOperationJob(operation);
			job.schedule();
		} else {
			// Run the workflow operation in a progress monitor dialog
			try {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(ExtendedPlatformUI.getActiveShell());
				dialog.run(true, true, new RunnableWithProgressAdapter(operation));
			} catch (InterruptedException ex) {
				// Operation has been canceled by user, do nothing
			} catch (InvocationTargetException ex) {
				throw new ExecutionException(ex.getMessage(), ex);
			}
		}

		return null;
	}
}
