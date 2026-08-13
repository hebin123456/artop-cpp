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
package org.eclipse.sphinx.emf.workspace.ui.viewers;

import org.eclipse.emf.common.util.TreeIterator;

/**
 * An enhanced {@link TreeIterator tree iterator} for tree content that provides the capability to detect cyclic tree
 * paths. Cyclic tree paths are sequences of repeated child items that refer to the same underlying objects. If such
 * repeated tree paths are encountered, the tree iteration is {@link #prune prune}ed at the first recurrent child item
 * to avoid that all remaining repeated child items are - potentially infinitely - re-traversed. Additionally, the
 * {@link #isRecurrent()} method enables the caller to get to know if the current tree item (i.e., the tree item
 * returned by the last call to {@link #next()}) is recurrent or not.
 *
 * @see #prune()
 * @see #isRecurrent()
 */
public interface ITreeContentIterator extends TreeIterator<Object> {

	/**
	 * An filter allowing the items being included in the tree iteration to be narrowed down to a subset of all tree
	 * items.
	 */
	public interface IItemFilter {

		boolean accept(Object item);
	}

	boolean isRecurrent();
}
