/**
 * <copyright>
 *
 * Copyright (c) 2014-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.mwe.dynamic.operations.BasicWorkflowRunnerOperation;
import org.eclipse.sphinx.emf.mwe.dynamic.operations.IWorkflowRunnerOperation;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.Activator;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.util.WorkflowRunnerUIHelper;
import org.eclipse.sphinx.platform.jobs.WorkspaceOperationWorkspaceJob;
import org.eclipse.sphinx.platform.ui.operations.RunnableWithProgressAdapter;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class BasicWorkflowRunnerAction extends BaseSelectionListenerAction {

	private boolean runInBackground;

	protected WorkflowRunnerUIHelper helper;

	public BasicWorkflowRunnerAction() {
		this(Messages.action_runWorkflow_label);
	}

	public BasicWorkflowRunnerAction(String text) {
		super(text);
		setRunInBackground(false);
		helper = new WorkflowRunnerUIHelper();
	}

	public boolean isRunInBackground() {
		return runInBackground;
	}

	public void setRunInBackground(boolean runInBackground) {
		this.runInBackground = runInBackground;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (helper.isWorkflow(getStructuredSelection())) {
			setText(Messages.action_runWorkflow_label);
			return true;
		}
		if (helper.isModel(getStructuredSelection())) {
			setText(Messages.action_runWorkflow_label + "..."); //$NON-NLS-1$
			return true;
		}
		return false;
	}

	public String getOperationName() {
		return Messages.operation_runWorkflow_label;
	}

	protected IWorkflowRunnerOperation createWorkflowRunnerOperation() {
		Object workflow = helper.getWorkflow(getStructuredSelection());
		if (workflow == null) {
			workflow = helper.promptForWorkflowType();
		}

		IWorkflowRunnerOperation operation = new BasicWorkflowRunnerOperation(getOperationName(), workflow);
		List<URI> modelURIs = helper.getModelURIs(getStructuredSelection());
		operation.getModelURIs().addAll(modelURIs);

		return operation;
	}

	protected WorkspaceJob createWorkspaceOperationJob(IWorkflowRunnerOperation operation) {
		return new WorkspaceOperationWorkspaceJob(operation);
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		ExtendedPlatformUI.showSystemConsole();

		// Create the workflow operation
		IWorkflowRunnerOperation operation = createWorkflowRunnerOperation();

		if (isRunInBackground()) {
			// Run the workflow operation in a workspace job
			WorkspaceJob job = createWorkspaceOperationJob(operation);
			job.schedule();
		} else {
			// Run the workflow operation in a progress monitor dialog
			try {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(ExtendedPlatformUI.getActiveShell());
				dialog.run(true, true, new RunnableWithProgressAdapter(operation));
			} catch (InterruptedException ex) {
				// Operation has been canceled by user, do nothing
			} catch (InvocationTargetException ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}
}