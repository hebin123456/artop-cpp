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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This action selects all entries currently showing in view.
 */
@SuppressWarnings("restriction")
public class SelectAllAction extends Action {

	private StructuredViewer viewer;

	/**
	 * Creates the action.
	 */
	public SelectAllAction() {
		super("selectAll"); //$NON-NLS-1$
		setText(SearchMessages.SelectAllAction_label);
		setToolTipText(SearchMessages.SelectAllAction_tooltip);
	}

	public void setViewer(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	private void collectExpandedAndVisible(TreeItem[] items, List<TreeItem> result) {
		for (TreeItem item : items) {
			result.add(item);
			if (item.getExpanded()) {
				collectExpandedAndVisible(item.getItems(), result);
			}
		}
	}

	/**
	 * Selects all resources in the view.
	 */
	@Override
	public void run() {
		if (viewer == null || viewer.getControl().isDisposed()) {
			return;
		}
		if (viewer instanceof TreeViewer) {
			List<TreeItem> allVisible = new ArrayList<TreeItem>();
			Tree tree = ((TreeViewer) viewer).getTree();
			collectExpandedAndVisible(tree.getItems(), allVisible);
			tree.setSelection(allVisible.toArray(new TreeItem[allVisible.size()]));
		} else if (viewer instanceof TableViewer) {
			((TableViewer) viewer).getTable().selectAll();
			// force viewer selection change
			viewer.setSelection(viewer.getSelection());
		}
	}
}
