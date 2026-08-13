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
package org.eclipse.sphinx.platform.operations;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * An {@link IWorkspaceOperation} adapter that can be used to wrap existing {@link IWorkspaceRunnable}s and use them as
 * {@link IWorkspaceOperation}s.
 *
 * @see IWorkspaceOperation
 * @see ILabeledWorkspaceRunnable
 * @see IWorkspaceRunnable
 */
public class WorkspaceOperationAdapter extends AbstractWorkspaceOperation {

	private ISchedulingRule rule;
	private IWorkspaceRunnable runnable;

	public WorkspaceOperationAdapter(String label, ISchedulingRule rule, IWorkspaceRunnable runnable) {
		super(label);
		Assert.isNotNull(runnable);
		this.rule = rule;
		this.runnable = runnable;
	}

	/*
	 * @see org.eclipse.sphinx.platform.operations.IWorkspaceOperation#getRule()
	 */
	@Override
	public ISchedulingRule getRule() {
		return rule;
	}

	/*
	 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		runnable.run(monitor);
	}
}
