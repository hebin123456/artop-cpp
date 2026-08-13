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
package org.eclipse.sphinx.emf.mwe.dynamic.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.IWorkflowRunnerMenuConstants;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.actions.BasicWorkflowRunnerAction;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class BasicWorkflowActionProvider extends BasicActionProvider {

	protected BasicWorkflowRunnerAction runWorkflowAction;

	@Override
	protected void doInit() {
		runWorkflowAction = createWorkflowRunnerAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(runWorkflowAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
			runWorkflowAction.selectionChanged(structuredSelection);
		}
	}

	protected BasicWorkflowRunnerAction createWorkflowRunnerAction() {
		return new BasicWorkflowRunnerAction();
	}

	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager subMenuManager = contextMenuManager.findMenuUsingPath(IWorkflowRunnerMenuConstants.MENU_RUN_WORKFLOW_ID);
		if (subMenuManager == null) {
			subMenuManager = new MenuManager(IWorkflowRunnerMenuConstants.MENU_RUN_WORKFLOW_LABEL, IWorkflowRunnerMenuConstants.MENU_RUN_WORKFLOW_ID);
			contextMenuManager.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, subMenuManager);
		}
		return subMenuManager;
	}

	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		subMenuManager.add(runWorkflowAction);
	}

	@Override
	public void dispose() {
		if (selectionProvider != null) {
			if (runWorkflowAction != null) {
				selectionProvider.removeSelectionChangedListener(runWorkflowAction);
			}
		}
		super.dispose();
	}
}