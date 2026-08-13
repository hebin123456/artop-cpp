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

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.edit.ui.action.RedoAction;
import org.eclipse.emf.edit.ui.action.UndoAction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.ui.actions.RedoActionWrapper;
import org.eclipse.emf.workspace.ui.actions.UndoActionWrapper;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class BasicModelUndoRedoActionProvider extends BasicActionProvider {

	/**
	 * This is the action used to implement Undo.
	 */
	protected UndoAction undoAction;

	/**
	 * This is the action used to implement Redo.
	 */
	protected RedoAction redoAction;

	@Override
	public void doInit() {
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

		undoAction = createUndoAction();
		Assert.isNotNull(undoAction);
		undoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		undoAction.setActiveWorkbenchPart(workbenchPart);
		undoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);

		redoAction = createRedoAction();
		Assert.isNotNull(redoAction);
		redoAction.setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		redoAction.setActiveWorkbenchPart(workbenchPart);
		redoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);

		updateActionBars();
	}

	@Override
	public void fillContextMenu(IMenuManager menuManager) {
		super.fillContextMenu(menuManager);

		// Add the edit menu actions
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(undoAction));
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_EDIT, new ActionContributionItem(redoAction));
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		updateActions(getContext().getSelection());

		// Redirect retargetable actions
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
	}

	@Override
	public void updateActionBars() {
		super.updateActionBars();

		undoAction.update();
		redoAction.update();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (undoAction != null) {
			undoAction.setEditingDomain(null);
		}
		if (undoAction != null) {
			redoAction.setEditingDomain(null);
		}
	}

	protected void updateActions(ISelection selection) {
		TransactionalEditingDomain editingDomain = getEditingDomainFromSelection(selection);

		undoAction.setEditingDomain(editingDomain);
		redoAction.setEditingDomain(editingDomain);
	}

	protected RedoAction createRedoAction() {
		return new RedoActionWrapper();
	}

	protected UndoAction createUndoAction() {
		return new UndoActionWrapper();
	}
}