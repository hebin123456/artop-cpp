/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.check.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.check.ui.IValidationUIConstants;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class BasicCheckValidationActionProvider extends BasicActionProvider {

	protected BasicCheckValidationAction validateAction;

	@Override
	protected void doInit() {
		validateAction = createValidateAction();
		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(validateAction);
			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
			validateAction.selectionChanged(structuredSelection);
		}
	}

	protected BasicCheckValidationAction createValidateAction() {
		return new BasicCheckValidationAction();
	}

	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager subMenuManager = contextMenuManager.findMenuUsingPath(IValidationUIConstants.MENU_VALIDATE_ID);
		if (subMenuManager == null) {
			subMenuManager = new MenuManager(IValidationUIConstants.MENU_VALIDATE_LABEL, IValidationUIConstants.MENU_VALIDATE_ID);
			contextMenuManager.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, subMenuManager);
		}
		return subMenuManager;
	}

	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		subMenuManager.add(validateAction);
	}

	@Override
	public void dispose() {
		if (selectionProvider != null) {
			if (validateAction != null) {
				selectionProvider.removeSelectionChangedListener(validateAction);
			}
		}
		super.dispose();
	}
}
