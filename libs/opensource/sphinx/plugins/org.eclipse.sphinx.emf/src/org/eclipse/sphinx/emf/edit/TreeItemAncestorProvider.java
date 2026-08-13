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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ReflectiveItemProvider;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;

public class TreeItemAncestorProvider implements ITreeItemAncestorProvider {

	protected ITreeItemContentProvider contentProvider;
	protected AdapterFactory adapterFactory;

	protected ResourceItemProviderAdapterFactory resourceItemProviderAdapterFactory = null;

	public TreeItemAncestorProvider(Object contentProvider, AdapterFactory adapterFactory) {
		Assert.isNotNull(adapterFactory);

		if (contentProvider instanceof ITreeItemContentProvider) {
			this.contentProvider = (ITreeItemContentProvider) contentProvider;
		}
		this.adapterFactory = adapterFactory;
	}

	/*
	 * @see org.eclipse.sphinx.emf.edit.ITreeItemAncestorProvider#getAncestorPath(java.lang.Object, boolean)
	 */
	@Override
	public List<Object> getAncestorPath(Object object, boolean unwrap) {
		return getAncestorPath(object, null, unwrap);
	}

	/*
	 * @see org.eclipse.sphinx.emf.edit.ITreeItemAncestorProvider#getAncestorPath(java.lang.Object, java.lang.Class,
	 * boolean)
	 */
	@Override
	public List<Object> getAncestorPath(Object beginObject, Class<?> endType, boolean unwrap) {
		if (beginObject != null) {
			List<Object> ancestorPath = new ArrayList<Object>();

			// Add begin object as first element to ancestor path
			ancestorPath.add(unwrap ? unwrap(beginObject) : beginObject);

			// Get parent of given object and add it to ancestor path
			Object parent = getContentProvider().getParent(beginObject);
			if (parent != null) {
				ancestorPath.add(unwrap ? unwrap(parent) : parent);
			}

			// Parent instance of given ancestor type or no more parent?
			while ((endType == null || !endType.isInstance(unwrap(parent))) && parent != null) {
				// Retrieve content provider for current parent
				ITreeItemContentProvider parentContentProvider;
				if ((parent instanceof Resource || parent instanceof ResourceSet) && !(adapterFactory instanceof ResourceItemProviderAdapterFactory)) {
					parentContentProvider = (ITreeItemContentProvider) getResourceItemProviderAdapterFactory().adapt(parent,
							ITreeItemContentProvider.class);
				} else {
					parentContentProvider = (ITreeItemContentProvider) adapterFactory.adapt(parent, ITreeItemContentProvider.class);
				}

				// Get parent of current parent, make it the current parent and add it to ancestor path
				parent = parentContentProvider.getParent(parent);
				if (parent != null) {
					ancestorPath.add(unwrap ? unwrap(parent) : parent);
				}
			}
			return ancestorPath;
		}
		return Collections.emptyList();
	}

	/*
	 * @see org.eclipse.sphinx.emf.edit.ITreeItemAncestorProvider#findAncestor(java.lang.Object, java.lang.Class,
	 * boolean)
	 */
	@Override
	public Object findAncestor(Object object, Class<?> ancestorType, boolean unwrap) {
		List<Object> ancestorPath = getAncestorPath(object, ancestorType, unwrap);
		if (!ancestorPath.isEmpty()) {
			Object ancestorObject = ancestorPath.get(ancestorPath.size() - 1);
			if (ancestorType == null || ancestorType.isInstance(unwrap(ancestorObject))) {
				return ancestorObject;
			}
		}
		return null;
	}

	protected Object unwrap(Object object) {
		return AdapterFactoryEditingDomain.unwrap(object);
	}

	protected ITreeItemContentProvider getContentProvider() {
		if (contentProvider == null) {
			contentProvider = new ReflectiveItemProvider(adapterFactory);
		}
		return contentProvider;
	}

	protected ResourceItemProviderAdapterFactory getResourceItemProviderAdapterFactory() {
		if (resourceItemProviderAdapterFactory == null) {
			resourceItemProviderAdapterFactory = new ResourceItemProviderAdapterFactory();
		}
		return resourceItemProviderAdapterFactory;
	}
}
