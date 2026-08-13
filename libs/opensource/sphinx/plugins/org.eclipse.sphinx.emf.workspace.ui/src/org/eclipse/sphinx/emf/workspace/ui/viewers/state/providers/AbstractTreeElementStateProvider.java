/**
 * <copyright>
 *
 * Copyright (c) 2015-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [480105] Occasional ConcurrentModificationException when re-launching Sphinx on previously used workspace
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers;

import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.navigator.CommonViewer;

public abstract class AbstractTreeElementStateProvider implements ITreeElementStateProvider {

	protected TreeViewer viewer;

	public AbstractTreeElementStateProvider(TreeViewer viewer) {
		Assert.isNotNull(viewer);

		this.viewer = viewer;
	}

	protected boolean isTreeContentProviderAvailable(Object element) {
		if (element == null) {
			return false;
		}

		if (viewer instanceof CommonViewer) {
			Set<?> contentExtensions = ((CommonViewer) viewer).getNavigatorContentService().findContentExtensionsByTriggerPoint(element);
			return !contentExtensions.isEmpty();
		}

		return true;
	}

	@Override
	public boolean isResolved() {
		// Providers with underlying models cannot be resolved as long as the latter have not been loaded
		if (hasUnderlyingModel() && !isUnderlyingModelLoaded()) {
			return false;
		} else {
			// Provided tree element doesn't exist?
			if (isStale()) {
				// Provider is fully resolved
				return true;
			} else {
				// Provider is fully resolved as soon as navigator content associated with tree element becomes
				// available
				return isTreeContentProviderAvailable(getTreeElement());
			}
		}
	}

	@Override
	public boolean canBeExpanded() {
		if (isStale()) {
			return false;
		} else {
			Object element = getTreeElement();
			return element != null && viewer.isExpandable(element);
		}
	}

	@Override
	public boolean isExpanded() {
		if (isStale()) {
			return false;
		} else {
			Object element = getTreeElement();
			return element != null && viewer.getExpandedState(element);
		}
	}
}