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
package org.eclipse.sphinx.emf.workspace.ui.viewers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * An {@link ITreeContentIterator} implementation that iterates over an object and uses the provided
 * {@link ITreeContentProvider content provider} to retrieve it's children, their children, and so on. Can be configured
 * to use an {@link ITreeContentIterator.IItemFilter item filter} that excludes certain objects from the tree iteration.
 * Also supports the detection of recurrent tree items, i.e., tree items that refer to the same underlying objects as
 * one or several other tree items that have already been traversed, and makes sure that all child items of recurrent
 * tree items are skipped.
 */
public class TreeContentProviderIterator extends AbstractTreeIterator<Object> implements ITreeContentIterator {

	private static final long serialVersionUID = 1L;

	private ITreeContentProvider contentProvider;
	private IItemFilter itemFilter;

	private Object currentItem = null;

	private Set<Object> visitedUnderlyingObjects = new HashSet<Object>();
	private Set<Object> recurrentItems = new HashSet<Object>();

	public TreeContentProviderIterator(ITreeContentProvider contentProvider, Object item) {
		this(contentProvider, item, null);
	}

	public TreeContentProviderIterator(ITreeContentProvider contentProvider, Object item, IItemFilter itemFilter) {
		super(item);

		Assert.isNotNull(contentProvider);
		this.contentProvider = contentProvider;
		this.itemFilter = itemFilter;
	}

	/*
	 * @see org.eclipse.emf.common.util.AbstractTreeIterator#next()
	 */
	@Override
	public Object next() {
		currentItem = super.next();
		return currentItem;
	}

	/*
	 * @see org.eclipse.emf.common.util.AbstractTreeIterator#getChildren(java.lang.Object)
	 */
	@Override
	protected Iterator<Object> getChildren(Object parentItem) {
		// Discontinue tree iteration if parent item is recurrent, i.e., refers to an underlying object that has already
		// been visited
		if (recurrentItems.contains(parentItem)) {
			return Collections.emptyList().iterator();
		}

		// Be sure to have underlying object of given parent item recorded in visited underlying objects collection
		Object underlyingParentObject = AdapterFactoryEditingDomain.unwrap(parentItem);
		visitedUnderlyingObjects.add(underlyingParentObject);

		List<Object> childItems = new ArrayList<Object>();
		for (Object childItem : contentProvider.getChildren(parentItem)) {
			if (itemFilter == null || itemFilter.accept(childItem)) {
				// Record underlying object of current child item in visited underlying objects collection and mark
				// current child item as recurrent if underlying object has already been visited
				Object underlyingChildObject = AdapterFactoryEditingDomain.unwrap(childItem);
				if (!visitedUnderlyingObjects.add(underlyingChildObject)) {
					recurrentItems.add(childItem);
				}

				childItems.add(childItem);
			}
		}
		return childItems.iterator();
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.viewers.ITreeContentIterator#isRecurrent()
	 */
	@Override
	public boolean isRecurrent() {
		return recurrentItems.contains(currentItem);
	}
}