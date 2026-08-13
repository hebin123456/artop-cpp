/**
 * <copyright>
 *
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.examples.transform.xtend.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.examples.transform.xtend.ui.ISphinxTransformExampleMenuConstants;
import org.eclipse.sphinx.examples.transform.xtend.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.sphinx.xtendxpand.ui.actions.BasicM2MAction;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class SphinxTransformExampleActionProvider extends BasicActionProvider {

	protected BasicM2MAction launchTransformAction;

	@Override
	public void doInit() {
		launchTransformAction = createLaunchTransformAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(launchTransformAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);

			launchTransformAction.selectionChanged(structuredSelection);
		}
	}

	protected BasicM2MAction createLaunchTransformAction() {
		return new BasicM2MAction(Messages.menuItem_transform_modelUsingXtend);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#addSubMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager subMenuManager = contextMenuManager.findMenuUsingPath(ISphinxTransformExampleMenuConstants.MENU_TRANSFORM_ID);
		if (subMenuManager == null) {
			subMenuManager = new MenuManager(ISphinxTransformExampleMenuConstants.MENU_TRANSFORM_LABEL,
					ISphinxTransformExampleMenuConstants.MENU_TRANSFORM_ID);
			contextMenuManager.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, subMenuManager);
		}
		return subMenuManager;
	}

	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		if (launchTransformAction != null) {
			subMenuManager.add(launchTransformAction);
		}
	}

	@Override
	public void dispose() {
		super.dispose();

		if (selectionProvider != null) {
			if (launchTransformAction != null) {
				selectionProvider.removeSelectionChangedListener(launchTransformAction);
			}
		}
	}
}
