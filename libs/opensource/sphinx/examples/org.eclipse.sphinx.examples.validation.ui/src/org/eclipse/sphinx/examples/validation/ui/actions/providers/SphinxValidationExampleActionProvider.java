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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.validation.ui.actions.BasicValidateSelectedCategoriesAction;

/**
 * {@link AbstractSphinxValidationExampleActionProvider Provider} for Sphinx validation example actions.
 *
 * @since 0.7.0
 */
public class SphinxValidationExampleActionProvider extends AbstractSphinxValidationExampleActionProvider {

	private BasicValidateSelectedCategoriesAction validateCategoriesAction;

	/*
	 * @see org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#doInit()
	 */
	@Override
	public void doInit() {
		validateCategoriesAction = createValidateCategoriesAction();
	}

	protected BasicValidateSelectedCategoriesAction createValidateCategoriesAction() {
		return new BasicValidateSelectedCategoriesAction();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.validation.ui.actions.providers.AbstractValidationActionProvider#populateActions(org.eclipse
	 * .jface.action.IMenuManager, org.eclipse.jface.viewers.IStructuredSelection, boolean)
	 */
	@Override
	protected void populateActions(IMenuManager menu, IStructuredSelection selection, boolean enabled) {
		validateCategoriesAction.selectionChanged(selection);
		validateCategoriesAction.setEnabled(enabled);
		menu.add(validateCategoriesAction);
	}
}
