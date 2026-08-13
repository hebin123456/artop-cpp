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
package org.eclipse.sphinx.emf.workspace.ui.viewers.state;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers.ITreeElementStateProvider;

public class TreeViewerState implements ITreeViewerState {

	protected List<ITreeElementStateProvider> expandedElements = null;
	protected List<ITreeElementStateProvider> selectedElements = null;

	protected boolean applying = false;

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState#getExpandedElements()
	 */
	@Override
	public List<ITreeElementStateProvider> getExpandedElements() {
		if (expandedElements == null) {
			expandedElements = new ArrayList<ITreeElementStateProvider>();
		}
		return expandedElements;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState#getSelectedElements()
	 */
	@Override
	public List<ITreeElementStateProvider> getSelectedElements() {
		if (selectedElements == null) {
			selectedElements = new ArrayList<ITreeElementStateProvider>();
		}
		return selectedElements;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return getExpandedElements().isEmpty() && getSelectedElements().isEmpty();
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState#isApplicationInProcess()
	 */
	@Override
	public boolean isApplying() {
		return applying;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.viewers.state.ITreeViewerState#setApplying(boolean)
	 */
	@Override
	public void setApplying(boolean applying) {
		this.applying = applying;
	}
}
