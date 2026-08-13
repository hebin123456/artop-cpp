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

/**
 * Default implementation of {@link IWorkspaceOperation}.
 *
 * @see IWorkspaceOperation
 */
public abstract class AbstractWorkspaceOperation extends AbstractLabeledWorkspaceRunnable implements IWorkspaceOperation {

	public AbstractWorkspaceOperation(String label) {
		super(label);
	}
}
