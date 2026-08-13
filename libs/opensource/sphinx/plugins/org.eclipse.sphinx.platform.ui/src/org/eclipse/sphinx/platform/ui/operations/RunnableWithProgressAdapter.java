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
 *     itemis - [468171] Model element splitting service
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.ui.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.operations.ILabeledWorkspaceRunnable;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;
import org.eclipse.sphinx.platform.operations.WorkspaceOperationAdapter;
import org.eclipse.sphinx.platform.ui.internal.messages.Messages;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * An {@link IRunnableWithProgress} adapter that can be used to wrap {@link IWorkspaceOperation}s,
 * {@link ILabeledWorkspaceRunnable}s, or {@link IWorkspaceRunnable}s and run them as {@link IRunnableWithProgress} in
 * an {@link IRunnableContext} that provides the UI for the progress monitor and cancel button.
 * <p>
 * The {@link RunnableWithProgressAdapter} is in principal very similar to the
 * {@link WorkspaceModifyDelegatingOperation}. The main differences are that the enclosed operation can be one of
 * {@link IWorkspaceOperation}, {@link ILabeledWorkspaceRunnable}, or {@link IWorkspaceRunnable} instead of having to be
 * an {@link IRunnableWithProgress} itself, and that the {@link ISchedulingRule} is retrieved from the enclosed
 * operation, in case it is an {@link IWorkspaceOperation}, instead of providing it on the {@link IRunnableWithProgress}
 * adapter itself.
 * </p>
 *
 * @see IRunnableWithProgress
 * @see IRunnableContext
 * @see IWorkspaceOperation
 * @see IWorkspaceRunnable
 * @see WorkspaceModifyOperation
 */
public class RunnableWithProgressAdapter implements IRunnableWithProgress {

	private IWorkspaceRunnable operation;

	public RunnableWithProgressAdapter(IWorkspaceRunnable operation) {
		Assert.isNotNull(operation);
		this.operation = operation;
	}

	public RunnableWithProgressAdapter(ILabeledWorkspaceRunnable operation) {
		Assert.isNotNull(operation);
		this.operation = operation;
	}

	public RunnableWithProgressAdapter(IWorkspaceOperation operation) {
		Assert.isNotNull(operation);
		this.operation = operation;
	}

	public RunnableWithProgressAdapter(String label, ISchedulingRule rule, IWorkspaceRunnable operation) {
		this(new WorkspaceOperationAdapter(label, rule, operation));
	}

	/*
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			ISchedulingRule rule = operation instanceof IWorkspaceOperation ? ((IWorkspaceOperation) operation).getRule() : null;
			ResourcesPlugin.getWorkspace().run(operation, rule, IWorkspace.AVOID_UPDATE, monitor);
		} catch (CoreException ex) {
			String msg;
			if (operation instanceof ILabeledWorkspaceRunnable) {
				msg = NLS.bind(Messages.error_whileRunningOperation, ((ILabeledWorkspaceRunnable) operation).getLabel());
			} else {
				msg = NLS.bind(Messages.error_whileRunningOperation, operation.getClass().getSimpleName());
			}
			throw new InvocationTargetException(ex, msg);
		} catch (OperationCanceledException ex) {
			throw new InterruptedException(ex.getMessage());
		}
	}
}
