/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [436112] Rework XML Persistence Mapping & XSD generation menu items to make them less prominent in the Eclipse UI
 *     itemis - Renamed ProjectStatisticsAction to GenerateModelStatisticsReportAction
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.actions.GenerateModelStatisticsReportAction;
import org.eclipse.sphinx.examples.common.ui.actions.providers.AbstractSphinxExampleActionProvider;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;

/**
 * {@link AbstractSphinxExampleActionProvider Provider} for Sphinx example actions.
 *
 * @since 0.7.0
 */
public class SphinxExampleActionProvider extends AbstractSphinxExampleActionProvider {

	/**
	 * The action responsible for creating a report on project's content (number of files, number of objects per type,
	 * available types, etc.)
	 */
	private GenerateModelStatisticsReportAction generateModelStatisticsReportAction;

	/*
	 * @see org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#doInit()
	 */
	@Override
	public void doInit() {
		generateModelStatisticsReportAction = new GenerateModelStatisticsReportAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(generateModelStatisticsReportAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
			generateModelStatisticsReportAction.updateSelection(structuredSelection);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#fillSubMenu(org.eclipse.jface.action.IMenuManager
	 * )
	 */
	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		subMenuManager.add(generateModelStatisticsReportAction);
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		if (selectionProvider != null) {
			if (generateModelStatisticsReportAction != null) {
				selectionProvider.removeSelectionChangedListener(generateModelStatisticsReportAction);
			}
		}

		super.dispose();
	}
}