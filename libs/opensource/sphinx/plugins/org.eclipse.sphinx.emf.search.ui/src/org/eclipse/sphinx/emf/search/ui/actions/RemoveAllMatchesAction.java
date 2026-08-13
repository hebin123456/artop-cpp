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
import org.eclipse.search2.internal.ui.SearchMessages;
import org.eclipse.sphinx.emf.search.ui.ModelSearchResult;
import org.eclipse.sphinx.emf.search.ui.pages.ModelSearchResultViewPage;

@SuppressWarnings("restriction")
public class RemoveAllMatchesAction extends Action {

	private ModelSearchResultViewPage searchResultPage;

	public RemoveAllMatchesAction(ModelSearchResultViewPage page) {
		super(SearchMessages.RemoveAllMatchesAction_label);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_REM_ALL);
		setToolTipText(SearchMessages.RemoveAllMatchesAction_tooltip);
		searchResultPage = page;
	}

	@Override
	public void run() {
		ModelSearchResult search = searchResultPage.getInput();
		if (search != null) {
			search.removeAll();
		}
	}
}
