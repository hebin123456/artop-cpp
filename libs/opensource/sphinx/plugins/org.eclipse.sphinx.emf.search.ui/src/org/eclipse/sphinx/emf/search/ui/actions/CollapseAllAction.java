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
package org.eclipse.sphinx.emf.search.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search2.internal.ui.SearchMessages;

@SuppressWarnings("restriction")
public class CollapseAllAction extends Action {

	private TreeViewer treeViewer;

	public CollapseAllAction() {
		super(SearchMessages.CollapseAllAction_0);
		setToolTipText(SearchMessages.CollapseAllAction_1);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_COLLAPSE_ALL);
	}

	public void setViewer(TreeViewer viewer) {
		treeViewer = viewer;
	}

	@Override
	public void run() {
		if (treeViewer != null) {
			treeViewer.collapseAll();
		}
	}
}
