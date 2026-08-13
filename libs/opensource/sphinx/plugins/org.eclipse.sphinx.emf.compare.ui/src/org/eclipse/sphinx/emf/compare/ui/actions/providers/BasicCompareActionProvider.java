/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [457704] Integrate EMF compare 3.x in Sphinx
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.compare.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sphinx.emf.compare.ui.ICompareMenuConstants;
import org.eclipse.sphinx.emf.compare.ui.actions.BasicCompareAction;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.IWorkbenchActionConstants;

public class BasicCompareActionProvider extends BasicActionProvider {

	/**
	 * The compare action to use.
	 */
	protected BasicCompareAction compareAction;

	@Override
	protected void doInit() {
		compareAction = new BasicCompareAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(compareAction);

			ISelection selection = selectionProvider.getSelection();
			compareAction.updateSelection(SelectionUtil.getStructuredSelection(selection));
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#addSubMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager subMenuManager = contextMenuManager.findMenuUsingPath(ICompareMenuConstants.MENU_COMPARE_WITH_ID);
		if (subMenuManager == null) {
			subMenuManager = new MenuManager(ICompareMenuConstants.MENU_COMPARE_WITH_LABEL, ICompareMenuConstants.MENU_COMPARE_WITH_ID);
			subMenuManager.add(new Separator(ICompareMenuConstants.MENU_COMPARE_WITH_GROUP));
			contextMenuManager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, subMenuManager);
		}
		return subMenuManager;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#fillSubMenu(org.eclipse.jface.action.IMenuManager
	 * )
	 */
	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		subMenuManager.add(compareAction);
	}

	@Override
	public void dispose() {
		super.dispose();

		if (selectionProvider != null) {
			if (compareAction != null) {
				selectionProvider.removeSelectionChangedListener(compareAction);
			}
		}
	}
}
