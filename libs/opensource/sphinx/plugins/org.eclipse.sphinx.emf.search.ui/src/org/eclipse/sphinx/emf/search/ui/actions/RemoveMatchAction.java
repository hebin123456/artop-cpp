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
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.sphinx.emf.search.ui.ModelSearchMatch;
import org.eclipse.sphinx.emf.search.ui.ModelSearchResult;
import org.eclipse.sphinx.emf.search.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.search.ui.pages.ModelSearchResultViewPage;

@SuppressWarnings("restriction")
public class RemoveMatchAction extends Action {

	private ModelSearchResultViewPage searchResultPage;

	public RemoveMatchAction(ModelSearchResultViewPage page) {
		searchResultPage = page;
		setText(Messages.RemoveMatchAction_label);
		setToolTipText(Messages.RemoveMatchAction_tooltip);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM);
	}

	@Override
	public void run() {
		ModelSearchMatch match = searchResultPage.getCurrentMatch();
		ModelSearchResult result = searchResultPage.getInput();
		if (match != null && result != null) {
			result.removeMatch(match);
		}
	}
}
