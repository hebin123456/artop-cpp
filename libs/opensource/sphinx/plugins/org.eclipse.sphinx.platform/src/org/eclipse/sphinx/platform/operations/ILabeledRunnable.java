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
package org.eclipse.sphinx.platform.operations;

/**
 * Enhancement of {@link Runnable} to provide support for simple (i.e., non-undoable) operations in the workspace that
 * don't need to report progress but want to expose an operation label.
 *
 * @see Runnable
 */
public interface ILabeledRunnable extends Runnable {

	/**
	 * Return the label that should be used to show the name of the operation to the user (e.g., in error messages).
	 *
	 * @return The operation label. Should never be <code>null</code>.
	 */
	String getLabel();
}