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
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Enhancement of {@link ILabeledWorkspaceRunnable} to provide support for simple (i.e., non-undoable) operations in the
 * workspace that need to report progress, want to expose an operation label and define their own
 * {@link ISchedulingRule scheduling rule}.
 *
 * @see ILabeledWorkspaceRunnable
 * @see IWorkspaceRunnable
 * @see ISchedulingRule
 */
public interface IWorkspaceOperation extends ILabeledWorkspaceRunnable {

	/**
	 * Returns the {@link ISchedulingRule scheduling rule} required by this operation.
	 *
	 * @return The scheduling rule or <code>null</code> if no such is required.
	 */
	ISchedulingRule getRule();
}
