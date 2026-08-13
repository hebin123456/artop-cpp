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
package org.eclipse.sphinx.emf.edit;

import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider;
import org.eclipse.emf.edit.provider.IItemFontProvider;
import org.eclipse.emf.edit.provider.IItemStyledLabelProvider;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.emf.edit.provider.StyledString;

public class ExtendedDelegatingWrapperItemProvider extends DelegatingWrapperItemProvider implements ITreeItemAncestorProvider, IItemFontProvider,
		IItemStyledLabelProvider {

	private ITreeItemAncestorProvider treeItemContentProviderHelper = null;

	public ExtendedDelegatingWrapperItemProvider(Object value, Object owner, EStructuralFeature feature, int index, AdapterFactory adapterFactory) {
		super(value, owner, feature, index, adapterFactory);
	}

	protected ITreeItemAncestorProvider getTreeItemContentProviderHelper() {
		if (treeItemContentProviderHelper == null) {
			treeItemContentProviderHelper = createTreeItemContentProviderHelper();
		}
		return treeItemContentProviderHelper;
	}

	protected ITreeItemAncestorProvider createTreeItemContentProviderHelper() {
		return new TreeItemAncestorProvider(this, adapterFactory);
	}

	/*
	 * @see org.eclipse.sphinx.emf.edit.ITreeItemAncestorProvider#getAncestorPath(java.lang.Object, boolean)
	 */
	@Override
	public List<Object> getAncestorPath(Object object, boolean unwrap) {
		return getTreeItemContentProviderHelper().getAncestorPath(object, unwrap);
	}

	/*
	 * @see org.eclipse.sphinx.emf.edit.ITreeItemAncestorProvider#getAncestorPath(java.lang.Object, java.lang.Class,
	 * boolean)
	 */
	@Override
	public List<Object> getAncestorPath(Object beginObject, Class<?> endType, boolean unwrap) {
		return getTreeItemContentProviderHelper().getAncestorPath(beginObject, endType, unwrap);
	}

	/*
	 * @see org.eclipse.sphinx.emf.edit.ITreeItemAncestorProvider#findAncestor(java.lang.Object, java.lang.Class,
	 * boolean)
	 */
	@Override
	public Object findAncestor(Object object, Class<?> ancestorType, boolean unwrap) {
		return getTreeItemContentProviderHelper().findAncestor(object, ancestorType, unwrap);
	}

	/*
	 * @see org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider#createWrapper(java.lang.Object,
	 * java.lang.Object, org.eclipse.emf.common.notify.AdapterFactory)
	 */
	@Override
	protected IWrapperItemProvider createWrapper(Object value, Object owner, AdapterFactory adapterFactory) {
		return new ExtendedDelegatingWrapperItemProvider(value, owner, null, CommandParameter.NO_INDEX, adapterFactory);
	}

	/*
	 * @see org.eclipse.emf.edit.provider.IItemStyledLabelProvider#getStyledText(java.lang.Object)
	 */
	@Override
	public Object getStyledText(Object object) {
		return delegateItemProvider instanceof IItemStyledLabelProvider ? ((IItemStyledLabelProvider) delegateItemProvider)
				.getStyledText(getDelegateValue()) : new StyledString(getText(object));
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [getDelegateValue()=" + getDelegateValue() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
