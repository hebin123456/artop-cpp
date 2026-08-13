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
package org.eclipse.sphinx.examples.hummingbird.metamodelgen.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.IHummingbirdExampleMenuConstants;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.actions.providers.AbstractHummingbirdExampleActionProvider;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.ui.IHummingbirdMetaModelGenExampleMenuConstants;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.ui.actions.GenerateXMLPersistenceMappingsAction;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.ui.actions.GenerateXMLPersistenceMappingsAndXSDAction;
import org.eclipse.sphinx.examples.hummingbird.metamodelgen.ui.actions.GenerateXSDAction;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;

public class HummingbirdMetaModelGenActionProvider extends AbstractHummingbirdExampleActionProvider {

	private GenerateXMLPersistenceMappingsAction generateXMLPersistenceMappingsAction;
	private GenerateXSDAction generateXSDAction;
	private GenerateXMLPersistenceMappingsAndXSDAction generateXMLPersistenceMappingsAndXSDAction;

	/*
	 * @see org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#doInit()
	 */
	@Override
	public void doInit() {
		generateXMLPersistenceMappingsAction = new GenerateXMLPersistenceMappingsAction();
		generateXSDAction = new GenerateXSDAction();
		generateXMLPersistenceMappingsAndXSDAction = new GenerateXMLPersistenceMappingsAndXSDAction();

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(generateXMLPersistenceMappingsAction);
			selectionProvider.addSelectionChangedListener(generateXSDAction);
			selectionProvider.addSelectionChangedListener(generateXMLPersistenceMappingsAndXSDAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
			generateXMLPersistenceMappingsAction.updateSelection(structuredSelection);
			generateXSDAction.updateSelection(structuredSelection);
			generateXMLPersistenceMappingsAndXSDAction.updateSelection(structuredSelection);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.examples.common.ui.actions.providers.AbstractSphinxExampleActionProvider#addSubMenu(org.eclipse
	 * .jface.action.IMenuManager)
	 */
	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager examplesMenuManager = super.addSubMenu(contextMenuManager);

		IMenuManager metaModelGenMenuManager = examplesMenuManager
				.findMenuUsingPath(IHummingbirdMetaModelGenExampleMenuConstants.MENU_META_MODEL_GEN_ID);
		if (metaModelGenMenuManager == null) {
			metaModelGenMenuManager = new MenuManager(IHummingbirdMetaModelGenExampleMenuConstants.MENU_META_MODEL_GEN_LABEL,
					IHummingbirdMetaModelGenExampleMenuConstants.MENU_META_MODEL_GEN_ID);
			examplesMenuManager.appendToGroup(IHummingbirdExampleMenuConstants.GROUP_HUMMINGBIRD_EXAMPLES, metaModelGenMenuManager);
		}
		return metaModelGenMenuManager;

	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#fillSubMenu(org.eclipse.jface.action.IMenuManager
	 * )
	 */
	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		subMenuManager.add(generateXMLPersistenceMappingsAction);
		subMenuManager.add(generateXSDAction);
		subMenuManager.add(generateXMLPersistenceMappingsAndXSDAction);
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		if (selectionProvider != null) {
			if (generateXMLPersistenceMappingsAction != null) {
				selectionProvider.removeSelectionChangedListener(generateXMLPersistenceMappingsAction);
			}
			if (generateXSDAction != null) {
				selectionProvider.removeSelectionChangedListener(generateXSDAction);
			}
			if (generateXMLPersistenceMappingsAndXSDAction != null) {
				selectionProvider.removeSelectionChangedListener(generateXMLPersistenceMappingsAndXSDAction);
			}
		}

		super.dispose();
	}
}
