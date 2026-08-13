/**
 * <copyright>
 *
 * Copyright (c) 2008-2017 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [393477] Provider hook for unwrapping elements before letting BasicTabbedPropertySheetTitleProvider retrieve text or image for them
 *     itemis - [393479] Enable BasicTabbedPropertySheetTitleProvider to retrieve same AdapterFactory as underlying IWorkbenchPart is using
 *     itemis - [526269] Properties page cannot be opened from Example Form Editor
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.properties;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * Defines a {@link ILabelProvider label provider} and a {@link IDescriptionProvider description provider} for the title
 * bar in tabbed properties views.
 * <p>
 * In contrast to Eclipse's built-in
 * {@link org.eclipse.ui.internal.navigator.resources.workbench.TabbedPropertySheetTitleProvider} this implementation
 * doesn't initialize the {@link ILabelProvider label provider} and the {@link IDescriptionProvider description
 * provider} in its constructor but lazily when the {@link #getImage(Object)} and {@link #getText(Object)} methods are
 * called. This makes sure that {@link ILabelProvider label provider} and {@link IDescriptionProvider description
 * provider} have a chance to get properly initialized by the time where they are really needed and don't remain
 * <code>null</code> when their initialization fails by the time where this class is instantiated.
 *
 * @since 0.7.0
 */
public class BasicTabbedPropertySheetTitleProvider extends LabelProvider {

	protected class DelegatingDescriptionProvider implements IDescriptionProvider {

		private ILabelProvider labelProvider;

		public DelegatingDescriptionProvider(ILabelProvider labelProvider) {
			Assert.isNotNull(labelProvider);
			this.labelProvider = labelProvider;
		}

		@Override
		public String getDescription(Object anElement) {
			if (anElement instanceof IStructuredSelection) {
				Collection<?> collection = ((IStructuredSelection) anElement).toList();
				switch (collection.size()) {
				case 0: {
					return Messages.label_selectedNothing;
				}
				case 1: {
					Object object = collection.iterator().next();
					String text = labelProvider.getText(object);
					if (text != null && text.length() > 0) {
						return text;
					}
					break;
				}
				default: {
					return NLS.bind(Messages.label_multipleItemsSelected, Integer.toString(collection.size()));
				}
				}
			} else {
				String text = labelProvider.getText(anElement);
				if (text != null && text.length() > 0) {
					return text;
				}
			}

			// Don't return empty String because otherwise the tabbed property sheet's title bar
			// looses its background color and becomes entirely blank
			return " "; //$NON-NLS-1$
		}
	}

	private ILabelProvider labelProvider;

	private IDescriptionProvider descriptionProvider;

	protected ILabelProvider getLabelProvider() {
		if (labelProvider == null) {
			initProviders();
		}
		return labelProvider;
	}

	protected IDescriptionProvider getDescriptionProvider() {
		if (descriptionProvider == null) {
			initProviders();
		}
		return descriptionProvider;
	}

	protected void initProviders() {
		IWorkbenchPart part = ExtendedPlatformUI.getActivePart();
		if (part != null) {
			INavigatorContentService contentService = part.getAdapter(INavigatorContentService.class);
			if (contentService != null) {
				labelProvider = contentService.createCommonLabelProvider();
				descriptionProvider = contentService.createCommonDescriptionProvider();
			} else {
				IEditingDomainProvider editingDomainProvider = part.getAdapter(IEditingDomainProvider.class);
				if (editingDomainProvider != null) {
					EditingDomain editingDomain = editingDomainProvider.getEditingDomain();
					if (editingDomain instanceof TransactionalEditingDomain) {
						AdapterFactory adapterFactory = part.getAdapter(AdapterFactory.class);
						if (adapterFactory == null && editingDomain instanceof AdapterFactoryEditingDomain) {
							adapterFactory = ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
						}
						labelProvider = new TransactionalAdapterFactoryLabelProvider((TransactionalEditingDomain) editingDomain, adapterFactory);
						descriptionProvider = new DelegatingDescriptionProvider(labelProvider);
					}
				}
			}
		}
	}

	protected void disposeProviders() {
		// Dispose label provider (description provider just wraps and delegates to label provider and therefore does not need
		// to be disposed on its own)
		/*
		 * !! Important Note !! Only dispose label provider if it has been created by ourselves, but not if it is managed by
		 * another view (e.g., Common Navigator).
		 */
		if (labelProvider instanceof AdapterFactoryLabelProvider) {
			labelProvider.dispose();
		}
	}

	/**
	 * Extracts the actual element to rendered from given {@link Object element}.
	 * <p>
	 * This implementation calls {@link AdapterFactoryEditingDomain#unwrap()} for that purpose. Subclasses may override and
	 * extend as appropriate.
	 * </p>
	 *
	 * @param element
	 *            The element to be unwrapped.
	 * @return The extracted {@link Object element} if the original element could be successfully unwrapped or the original
	 *         element otherwise.
	 */
	protected Object unwrap(Object element) {
		return AdapterFactoryEditingDomain.unwrap(element);
	}

	/*
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		ILabelProvider labelProvider = getLabelProvider();
		if (labelProvider != null) {
			if (element instanceof IStructuredSelection) {
				// Display image only if exactly one element has been selected
				IStructuredSelection structuredSelection = (IStructuredSelection) element;
				if (structuredSelection.size() == 1) {
					element = unwrap(structuredSelection.getFirstElement());
					if (element != null) {
						return labelProvider.getImage(element);
					}
				}
			} else {
				Object unwrapped = unwrap(element);
				if (unwrapped != null) {
					return labelProvider.getImage(unwrapped);
				}
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		IDescriptionProvider descriptionProvider = getDescriptionProvider();
		if (descriptionProvider != null) {
			if (element instanceof IStructuredSelection) {
				// Unwrap selected element only if exactly one element has been selected
				IStructuredSelection structuredSelection = (IStructuredSelection) element;
				if (structuredSelection.size() == 1) {
					element = unwrap(structuredSelection.getFirstElement());
				}
				if (element != null) {
					return descriptionProvider.getDescription(element);
				}
			} else {
				Object unwrapped = unwrap(element);
				if (unwrapped != null) {
					return descriptionProvider.getDescription(unwrapped);
				}
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		disposeProviders();
		super.dispose();
	}
}
