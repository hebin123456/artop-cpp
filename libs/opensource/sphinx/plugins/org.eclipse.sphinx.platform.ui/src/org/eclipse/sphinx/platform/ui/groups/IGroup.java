/**
 * <copyright>
 *
 * Copyright (c) 2008-2016 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.ui.groups;

import org.eclipse.swt.widgets.Composite;

public interface IGroup {

	/**
	 * Creates the content i.e., all necessary fields of the group.
	 */
	void createContent(Composite parent, int numColumns);

	/**
	 * Creates the content i.e., all necessary fields of the group.
	 */
	void createContent(Composite parent, int numColumns, boolean grabExcessVerticalSpace);

	/**
	 * Creates the content i.e., all necessary fields of the group.
	 */
	void createContent(Composite parent, int numColumns, boolean grabExcessVerticalSpace, boolean suppressGroupFrame);

	/**
	 * Returns true or false if the group is complete or not.
	 */
	boolean isGroupComplete();

	/**
	 * Adds the given group listener in the listeners list of this group.
	 *
	 * @param listener
	 *            The group listener to add.
	 */
	void addGroupListener(IGroupListener listener);

	/**
	 * Removes the given group listener from the listeners list of this group.
	 *
	 * @param listener
	 *            The group listener to remove.
	 */
	void removeGroupListener(IGroupListener listener);
}
