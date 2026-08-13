/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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

import org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState;
import org.eclipse.ui.navigator.ICommonContentProvider;

/**
 * Extended version of {@link ICommonContentProvider} interface that includes general extra services that are not (yet)
 * related to providing content of EMF models.
 */
// TODO Move to org.eclipse.sphinx.platform.ui.viewers
public interface IExtendedCommonContentProvider extends ICommonContentProvider {

	/**
	 * Determines if this {@link IExtendedCommonContentProvider content provider} is enabled for the given element.
	 *
	 * @param element
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the content provider is enabled for the element.
	 */
	boolean isTriggerPoint(Object element);

	/**
	 * Determines if this {@link IExtendedCommonContentProvider content provider} could provide the given element as a
	 * child.
	 *
	 * @param element
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the content provider might provide an object of this type as a child.
	 */
	boolean isPossibleChild(Object element);

	ITreeViewerState recordViewerState();

	void applyViewerState(ITreeViewerState state);
}
