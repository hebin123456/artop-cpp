/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.emf.search.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.sphinx.emf.search.ui.pages.ModelSearchResultViewPage;

public class SetLayoutAction extends Action {

	private ModelSearchResultViewPage searchResultPage;
	private int fLayout;

	public SetLayoutAction(ModelSearchResultViewPage page, String label, String tooltip, int layout) {
		super(label, IAction.AS_RADIO_BUTTON);
		searchResultPage = page;
		setToolTipText(tooltip);
		fLayout = layout;
	}

	@Override
	public void run() {
		searchResultPage.setLayout(fLayout);
	}

	public int getLayout() {
		return fLayout;
	}
}
