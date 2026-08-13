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
package org.eclipse.sphinx.examples.hummingbird20.splitting.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.IHummingbirdExampleMenuConstants;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.actions.providers.AbstractHummingbirdExampleActionProvider;
import org.eclipse.sphinx.examples.hummingbird20.splitting.ui.IHummingbirdSplittingExampleMenuConstants;
import org.eclipse.sphinx.examples.hummingbird20.splitting.ui.actions.Hummingbirg20ModelSplitAction;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;

public class Hummingbirg20ModelSplitActionProvider extends AbstractHummingbirdExampleActionProvider {

	protected Hummingbirg20ModelSplitAction modelSplitAction;

	@Override
	protected void doInit() {
		modelSplitAction = new Hummingbirg20ModelSplitAction();
		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(modelSplitAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
			modelSplitAction.selectionChanged(structuredSelection);
		}
	}

	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager examplesMenuManager = super.addSubMenu(contextMenuManager);

		IMenuManager splittingMenuManager = examplesMenuManager.findMenuUsingPath(IHummingbirdSplittingExampleMenuConstants.MENU_MODEL_SPLITTING_ID);
		if (splittingMenuManager == null) {
			splittingMenuManager = new MenuManager(IHummingbirdSplittingExampleMenuConstants.MENU_MODEL_SPLITTING_LABEL,
					IHummingbirdSplittingExampleMenuConstants.MENU_MODEL_SPLITTING_ID);
			examplesMenuManager.appendToGroup(IHummingbirdExampleMenuConstants.GROUP_HUMMINGBIRD_EXAMPLES, splittingMenuManager);
		}
		return splittingMenuManager;
	}

	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		subMenuManager.add(modelSplitAction);
	}

	@Override
	public void dispose() {
		if (selectionProvider != null) {
			if (modelSplitAction != null) {
				selectionProvider.removeSelectionChangedListener(modelSplitAction);
			}
		}

		super.dispose();
	}
}
