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
package org.eclipse.sphinx.emf.util;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.DelegatingEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

public abstract class AbstractFilteringEList<E> extends DelegatingEList<E> {

	private static final long serialVersionUID = 1L;

	private EObject targetOwner;
	private EStructuralFeature targetFeature;

	public AbstractFilteringEList(EObject targetOwner, EStructuralFeature targetFeature) {
		Assert.isNotNull(targetOwner);
		Assert.isLegal(targetFeature instanceof EReference);

		this.targetOwner = targetOwner;
		this.targetFeature = targetFeature;
	}

	/*
	 * @see org.eclipse.emf.common.util.DelegatingEList#delegateList()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected List<E> delegateList() {
		return (List<E>) targetOwner.eGet(targetFeature);
	}

	protected List<E> getFilteredEList() {
		EList<E> filteredEList = new BasicEList<E>();
		for (E item : delegateList()) {
			if (accept(item)) {
				filteredEList.add(item);
			}
		}
		return filteredEList;
	}

	/*
	 * @see org.eclipse.emf.common.util.DelegatingEList#delegateSize()
	 */
	@Override
	protected int delegateSize() {
		return getFilteredEList().size();
	}

	/*
	 * @see org.eclipse.emf.common.util.DelegatingEList#delegateGet(int)
	 */
	@Override
	protected E delegateGet(int index) {
		return getFilteredEList().get(index);
	}

	@Override
	protected E delegateSet(int index, E object) {
		return getFilteredEList().set(index, object);
	}

	@Override
	protected void delegateAdd(int index, E object) {
		// Add the new object in the delegate list and also in the filtered list
		getFilteredEList().add(index, object);
		delegateList().add(index, object);
	}

	@Override
	protected E delegateRemove(int index) {
		// Retrieve the index of this object in delegateList() and remove it. Then remove the object also in the
		// filtering list.
		E object = delegateGet(index);
		delegateList().remove(object);
		return delegateList().remove(index);
	}

	@Override
	protected Object[] delegateToArray() {
		return getFilteredEList().toArray();
	}

	@Override
	protected <T> T[] delegateToArray(T[] array) {
		return getFilteredEList().toArray(array);
	}

	protected abstract boolean accept(E item);
}
