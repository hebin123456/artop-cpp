/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer.actions.providers;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.ui.actions.BasicMoveAction;
import org.eclipse.sphinx.emf.ui.actions.BasicRenameAction;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class BasicModelRefactorActionProvider extends BasicActionProvider {

	/**
	 * This is the action used to implement renaming EObjects.
	 */
	protected BasicRenameAction renameAction;

	/**
	 * This is the action used to implement moving EObjects.
	 */
	protected BasicMoveAction moveAction;

	@Override
	public void doInit() {
		renameAction = createRenameAction();
		moveAction = createMoveAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(renameAction);
			selectionProvider.addSelectionChangedListener(moveAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);

			renameAction.selectionChanged(structuredSelection);
			moveAction.selectionChanged(structuredSelection);
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menuManager) {
		super.fillContextMenu(menuManager);

		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(renameAction));
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(moveAction));
	}

	@Override
	public void dispose() {
		super.dispose();

		if (selectionProvider != null) {
			if (renameAction != null) {
				selectionProvider.removeSelectionChangedListener(renameAction);
			}
			if (moveAction != null) {
				selectionProvider.removeSelectionChangedListener(moveAction);
			}
		}
	}

	protected BasicRenameAction createRenameAction() {
		if (viewer instanceof TreeViewer) {
			return new BasicRenameAction((TreeViewer) viewer);
		} else {
			return new BasicRenameAction();
		}
	}

	protected BasicMoveAction createMoveAction() {
		return new BasicMoveAction();
	}
}