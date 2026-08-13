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
package org.eclipse.sphinx.examples.hummingbird20.ide.ui.actions.providers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.actions.BasicWalkUpAncestorsAction;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.actions.providers.AbstractHummingbirdExampleActionProvider;
import org.eclipse.sphinx.examples.hummingbird20.ide.ui.actions.Hummingbird20WalkUpAncestorsAction;
import org.eclipse.sphinx.platform.ui.util.SelectionUtil;

/**
 * {@link AbstractHummingbirdExampleActionProvider Provider} for Hummingbird 2.0 example actions.
 *
 * @since 0.9.0
 */
public class Hummingbird20ActionProvider extends AbstractHummingbirdExampleActionProvider {

	private BasicWalkUpAncestorsAction walkUpAncestorsAction;

	/*
	 * @see org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#doInit()
	 */
	@Override
	public void doInit() {
		walkUpAncestorsAction = new Hummingbird20WalkUpAncestorsAction(viewer);

		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(walkUpAncestorsAction);

			ISelection selection = selectionProvider.getSelection();
			IStructuredSelection structuredSelection = SelectionUtil.getStructuredSelection(selection);
			walkUpAncestorsAction.updateSelection(structuredSelection);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.ui.actions.providers.BasicActionProvider#fillSubMenu(org.eclipse.jface.action.IMenuManager
	 * )
	 */
	@Override
	protected void fillSubMenu(IMenuManager subMenuManager) {
		subMenuManager.add(walkUpAncestorsAction);
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		if (selectionProvider != null) {
			if (walkUpAncestorsAction != null) {
				selectionProvider.removeSelectionChangedListener(walkUpAncestorsAction);
			}
		}

		super.dispose();
	}
}