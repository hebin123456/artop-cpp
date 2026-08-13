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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.sphinx.emf.search.ui.ModelSearchResult;
import org.eclipse.sphinx.emf.search.ui.pages.ModelSearchResultViewPage;

public class ModelSearchTreeContentProvider extends AbstractModelSearchContentProvider implements ITreeContentProvider {

	private Map<Object, Set<Object>> childrenMap;

	public ModelSearchTreeContentProvider(ModelSearchResultViewPage page) {
		super(page);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ModelSearchResult) {
			return ((ModelSearchResult) parentElement).getElements();
		}
		return EMPTY_ARR;
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return true;
	}

	@Override
	protected void initialize(ModelSearchResult result) {
		super.initialize(result);
		childrenMap = new HashMap<Object, Set<Object>>();
		if (result != null) {
			Object[] elements = result.getElements();
			for (Object element : elements) {
				if (getPage().getDisplayedMatchCount(element) > 0) {
					insert(null, null, element);
				}
			}
		}
	}

	@Override
	public void elementsChanged(Object[] updatedElements) {
		if (getSearchResult() == null) {
			return;
		}

		AbstractTreeViewer viewer = (AbstractTreeViewer) getPage().getViewer();

		Set<Object> toRemove = new HashSet<Object>();
		Set<Object> toUpdate = new HashSet<Object>();
		Map<Object, Set<Object>> toAdd = new HashMap<Object, Set<Object>>();
		for (Object updatedElement : updatedElements) {
			if (getPage().getDisplayedMatchCount(updatedElement) > 0) {
				// TODO (aakar)
				insert(toAdd, toUpdate, updatedElement);
			} else {
				// remove(toRemove, toUpdate, updatedElement);
			}
		}

		viewer.remove(toRemove.toArray());
		for (Object parent : toAdd.keySet()) {
			HashSet<Object> children = (HashSet<Object>) toAdd.get(parent);
			viewer.add(parent, children.toArray());
		}
		for (Object object : toUpdate) {
			viewer.refresh(object);
		}
		// viewer.refresh();
	}

	protected void insert(Map<Object, Set<Object>> toAdd, Set<Object> toUpdate, Object child) {
		Object parent = getParent(child);
		while (parent != null) {
			if (insertChild(parent, child)) {
				if (toAdd != null) {
					insertInto(parent, child, toAdd);
				}
			} else {
				if (toUpdate != null) {
					toUpdate.add(parent);
				}
				return;
			}
			child = parent;
			parent = getParent(child);
		}
		if (insertChild(getSearchResult(), child)) {
			if (toAdd != null) {
				insertInto(getSearchResult(), child, toAdd);
			}
		}
	}

	private boolean insertChild(Object parent, Object child) {
		return insertInto(parent, child, childrenMap);
	}

	private boolean insertInto(Object parent, Object child, Map<Object, Set<Object>> map) {
		Set<Object> children = map.get(parent);
		if (children == null) {
			children = new HashSet<Object>();
			map.put(parent, children);
		}
		return children.add(child);
	}

	@Override
	public void clear() {
		initialize(getSearchResult());
		getPage().getViewer().refresh();
	}
}
