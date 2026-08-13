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
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.operations;

/**
 * Default implementation of {@link ILabeledRunnable}.
 *
 * @see ILabeledWorkspaceRunnable
 */
public abstract class AbstractLabeledRunnable implements ILabeledRunnable {

	private String label;

	public AbstractLabeledRunnable(String label) {
		this.label = label;
	}

	/*
	 * @see org.eclipse.sphinx.platform.operations.ILabeledRunnable#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}
}
