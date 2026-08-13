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
package org.eclipse.sphinx.emf.ui.actions.providers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.sphinx.emf.ui.actions.BasicOpenInEditorAction;
import org.eclipse.sphinx.emf.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * Implementation of basic {@linkplain BasicActionProvider action provider} providing a basic
 * {@linkplain BasicOpenInEditorAction open in editor} action. Such action is directly added as a global action in the
 * contextual menu so that default behavior on double-click could be overridden (opening selected element in editor
 * instead of expand/collapse it).
 */
public class BasicEditorActionProvider extends BasicActionProvider {

	/**
	 * This is the action used to open current selection in an editor.
	 */
	protected BasicOpenInEditorAction openInEditorAction;

	protected IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
		@Override
		public void doubleClick(DoubleClickEvent evt) {
			ISelection selection = evt.getSelection();
			if (selection instanceof IStructuredSelection) {
				if (openInEditorAction.isEnabled()) {
					openInEditorAction.run();
				}
			}
		}
	};

	@Override
	public void doInit() {
		openInEditorAction = new BasicOpenInEditorAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(openInEditorAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
			openInEditorAction.updateSelection(structuredSelection);
		}

		if (viewer instanceof StructuredViewer) {
			((StructuredViewer) viewer).addDoubleClickListener(doubleClickListener);
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menuManager) {

		// Contribute Open In Editor action
		menuManager.appendToGroup(ICommonMenuConstants.GROUP_OPEN, new ActionContributionItem(openInEditorAction));

		// Contribute Open With sub-menu
		IStructuredSelection selection = SelectionUtil.getStructuredSelection(getContext().getSelection());
		if (selection.size() == 1) {
			Object selected = selection.getFirstElement();
			if (selected instanceof EObject) {
				if (workbenchPart != null) {
					IMenuManager submenu = new MenuManager(Messages.menu_openWith_label);
					submenu.add(new OpenWithMenu(workbenchPart.getSite().getPage(), (EObject) selected));
					menuManager.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
				}
			}
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);

		// Set Open in Editor action as global open action in order to override default double click behavior which is
		// to expand/collapse selected element upon double click (see
		// org.eclipse.ui.navigator.CommonNavigator#handleDoubleClick() for details)
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openInEditorAction);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (viewer instanceof StructuredViewer) {
			((StructuredViewer) viewer).removeDoubleClickListener(doubleClickListener);
		}
		if (selectionProvider != null && openInEditorAction != null) {
			selectionProvider.removeSelectionChangedListener(openInEditorAction);
		}
	}
}