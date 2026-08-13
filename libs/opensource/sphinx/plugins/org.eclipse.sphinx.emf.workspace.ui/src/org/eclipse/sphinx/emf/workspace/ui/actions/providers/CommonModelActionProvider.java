/**
 * <copyright>
 * 
 * Copyright (c) 2011 itemis and others.
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
package org.eclipse.sphinx.emf.workspace.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.emf.workspace.ui.actions.BasicOpenReferencesAction;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class CommonModelActionProvider extends BasicActionProvider {

	protected BasicOpenReferencesAction openReferencesAction;

	@Override
	public void doInit() {
		openReferencesAction = createOpenReferencesAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(openReferencesAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);

			openReferencesAction.selectionChanged(structuredSelection);
		}
	}

	protected BasicOpenReferencesAction createOpenReferencesAction() {
		return new BasicOpenReferencesAction();
	}

	@Override
	public void fillContextMenu(IMenuManager contextMenuManager) {
		if (openReferencesAction.isEnabled()) {
			contextMenuManager.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openReferencesAction);
		}
	}

	@Override
	public void dispose() {
		super.dispose();

		if (selectionProvider != null) {
			if (openReferencesAction != null) {
				selectionProvider.removeSelectionChangedListener(openReferencesAction);
			}
		}
	}
}
