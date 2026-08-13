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
package org.eclipse.sphinx.emf.search.ui.providers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.sphinx.emf.search.ui.ModelSearchResult;
import org.eclipse.sphinx.emf.search.ui.pages.ModelSearchResultViewPage;
import org.eclipse.swt.widgets.Table;

public class ModelSeachTableContentProvider extends AbstractModelSearchContentProvider {

	public ModelSeachTableContentProvider(ModelSearchResultViewPage page) {
		super(page);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ModelSearchResult) {
			Set<Object> filteredElements = new HashSet<Object>();
			Object[] rawElements = ((ModelSearchResult) inputElement).getElements();
			int limit = getPage().getElementLimit().intValue();
			for (Object rawElement : rawElements) {
				if (getPage().getDisplayedMatchCount(rawElement) > 0) {
					filteredElements.add(rawElement);
					if (limit != -1 && limit < filteredElements.size()) {
						break;
					}
				}
			}
			return filteredElements.toArray();
		}
		return EMPTY_ARR;
	}

	@Override
	public void elementsChanged(Object[] updatedElements) {
		if (getSearchResult() == null) {
			return;
		}

		int addLimit = getAddLimit();

		TableViewer viewer = (TableViewer) getPage().getViewer();
		Set<Object> updated = new HashSet<Object>();
		Set<Object> added = new HashSet<Object>();
		Set<Object> removed = new HashSet<Object>();
		for (Object updatedElement : updatedElements) {
			if (getPage().getDisplayedMatchCount(updatedElement) > 0) {
				if (viewer.testFindItem(updatedElement) != null) {
					updated.add(updatedElement);
				} else {
					if (addLimit > 0) {
						added.add(updatedElement);
						addLimit--;
					}
				}
			} else {
				removed.add(updatedElement);
			}
		}

		viewer.add(added.toArray());
		viewer.update(updated.toArray(), new String[] { ModelSearchLabelProvider.PROPERTY_MATCH_COUNT });
		viewer.remove(removed.toArray());
	}

	private int getAddLimit() {
		int limit = getPage().getElementLimit().intValue();
		if (limit != -1) {
			Table table = (Table) getPage().getViewer().getControl();
			int itemCount = table.getItemCount();
			if (itemCount >= limit) {
				return 0;
			}
			return limit - itemCount;
		}
		return Integer.MAX_VALUE;
	}

	@Override
	public void clear() {
		getPage().getViewer().refresh();
	}
}
