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
package org.eclipse.sphinx.examples.validation.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.sphinx.emf.validation.ui.actions.providers.AbstractValidationActionProvider;
import org.eclipse.sphinx.examples.common.ui.ISphinxExampleMenuConstants;
import org.eclipse.sphinx.examples.validation.ui.ISphinxValidationExampleMenuConstants;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * Basic {@link AbstractValidationActionProvider action provider} implementation for Sphinx validation examples actions.
 * 
 * @since 0.7.0
 */
public abstract class AbstractSphinxValidationExampleActionProvider extends AbstractValidationActionProvider {

	/*
	 * @see
	 * org.eclipse.sphinx.emf.validation.ui.actions.providers.AbstractValidationActionProvider#addSubMenu(org.eclipse
	 * .jface .action.IMenuManager)
	 */
	@Override
	protected IMenuManager addSubMenu(IMenuManager contextMenuManager) {
		IMenuManager examplesMenuManager = contextMenuManager.findMenuUsingPath(ISphinxExampleMenuConstants.MENU_SPHINX_EXAMPLES_ID);
		if (examplesMenuManager == null) {
			examplesMenuManager = new MenuManager(ISphinxExampleMenuConstants.MENU_SPHINX_EXAMPLES_LABEL,
					ISphinxExampleMenuConstants.MENU_SPHINX_EXAMPLES_ID);
			contextMenuManager.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, examplesMenuManager);

			examplesMenuManager.add(new Separator(ISphinxExampleMenuConstants.GROUP_SPHINX_EXAMPLES));
		}

		IMenuManager validationMenuManager = examplesMenuManager.findMenuUsingPath(ISphinxValidationExampleMenuConstants.MENU_VALIDATION_ID);
		if (validationMenuManager == null) {
			validationMenuManager = new MenuManager(ISphinxValidationExampleMenuConstants.MENU_VALIDATION_LABEL,
					ISphinxValidationExampleMenuConstants.MENU_VALIDATION_ID);
			examplesMenuManager.appendToGroup(ISphinxExampleMenuConstants.GROUP_SPHINX_EXAMPLES, validationMenuManager);
		}
		return validationMenuManager;
	}
}
