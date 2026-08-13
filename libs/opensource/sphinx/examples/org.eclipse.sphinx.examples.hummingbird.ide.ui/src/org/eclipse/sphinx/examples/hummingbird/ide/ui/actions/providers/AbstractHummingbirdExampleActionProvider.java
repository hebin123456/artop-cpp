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
package org.eclipse.sphinx.examples.hummingbird.ide.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.IHummingbirdExampleMenuConstants;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * A {@link BasicActionProvider action provider} implementation for Hummingbird examples actions.
 *
 * @since 0.8.0
 */
public abstract class AbstractHummingbirdExampleActionProvider extends BasicActionProvider {

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#addSubMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager examplesMenuManager = contextMenuManager.findMenuUsingPath(IHummingbirdExampleMenuConstants.MENU_HUMMINGBIRD_EXAMPLES_ID);
		if (examplesMenuManager == null) {
			examplesMenuManager = new MenuManager(IHummingbirdExampleMenuConstants.MENU_HUMMINGBIRD_EXAMPLES_LABEL,
					IHummingbirdExampleMenuConstants.MENU_HUMMINGBIRD_EXAMPLES_ID);
			contextMenuManager.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, examplesMenuManager);

			examplesMenuManager.add(new Separator(IHummingbirdExampleMenuConstants.GROUP_HUMMINGBIRD_EXAMPLES));
		}
		return examplesMenuManager;
	}
}