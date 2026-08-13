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

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class ModelSearchLabelProvider implements ILabelProvider {

	public static final String PROPERTY_MATCH_COUNT = "org.eclipse.sphinx.emf.search.ui.matchCount"; //$NON-NLS-1$

	protected Map<TransactionalEditingDomain, AdapterFactoryLabelProvider> modelLabelProviders = new WeakHashMap<TransactionalEditingDomain, AdapterFactoryLabelProvider>();

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return PROPERTY_MATCH_COUNT.equals(property);
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		AdapterFactoryLabelProvider labelProvider = getModelLabelProvider(element);
		if (labelProvider != null) {
			return labelProvider.getImage(element);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		AdapterFactoryLabelProvider labelProvider = getModelLabelProvider(element);
		if (labelProvider != null) {
			return labelProvider.getText(element);
		}
		return element.toString();
	}

	protected AdapterFactoryLabelProvider getModelLabelProvider(Object element) {
		// Retrieve editing domain behind specified object
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(element);
		if (editingDomain != null) {
			// Retrieve model label provider for given editing domain; create new one if not existing yet
			AdapterFactoryLabelProvider modelLabelProvider = modelLabelProviders.get(editingDomain);
			if (modelLabelProvider == null) {
				modelLabelProvider = createModelLabelProvider(editingDomain);
				modelLabelProviders.put(editingDomain, modelLabelProvider);
			}
			return modelLabelProvider;
		}
		return null;
	}

	protected AdapterFactoryLabelProvider createModelLabelProvider(final TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);
		AdapterFactory adapterFactory = ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(adapterFactory);

		// Set default font to enable the customized getFont()
		Font defaultFont = JFaceResources.getDefaultFont();
		labelProvider.setDefaultFont(defaultFont);

		return labelProvider;
	}
}
