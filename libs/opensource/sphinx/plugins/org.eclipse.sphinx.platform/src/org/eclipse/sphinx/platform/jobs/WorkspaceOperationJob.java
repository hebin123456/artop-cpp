/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.jobs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.operations.ILabeledWorkspaceRunnable;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * A {@link Job} that can be used to run {@link ILabeledWorkspaceRunnable}s as a background task.
 */
public class WorkspaceOperationJob extends Job {

	protected ILabeledWorkspaceRunnable operation;

	public WorkspaceOperationJob(ILabeledWorkspaceRunnable operation) {
		super(operation.getLabel());

		Assert.isNotNull(operation);
		this.operation = operation;

		setPriority(Job.BUILD);
	}

	/*
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			operation.run(monitor);
		} catch (OperationCanceledException exception) {
			return Status.CANCEL_STATUS;
		} catch (CoreException ex) {
			return ex.getStatus();
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getDefault(), ex);
		}
		return Status.OK_STATUS;
	}
}