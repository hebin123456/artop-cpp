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
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.internal.loading;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class UnresolveUnreachableCrossProjectReferencesJob extends Job {

	private IUnresolveUnreachableCrossProjectReferencesOperation operation;

	/**
	 * Constructor.
	 *
	 * @param operation
	 *            The operation which this job is supposed to run.
	 */
	public UnresolveUnreachableCrossProjectReferencesJob(IUnresolveUnreachableCrossProjectReferencesOperation operation) {
		super(operation.getLabel());
		this.operation = operation;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			operation.run(monitor);
			return Status.OK_STATUS;
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} catch (CoreException ex) {
			return ex.getStatus();
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		}
	}

	@Override
	public boolean belongsTo(Object family) {
		return IExtendedPlatformConstants.FAMILY_MODEL_LOADING.equals(family) || IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
	}
}
