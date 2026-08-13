/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [446576] Add support for providing labels with different fonts and styles for model elements through BasicExplorerLabelProvider
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl.BasicFeatureMapEntry;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.FeatureMapEntryWrapperItemProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.ui.internal.Tracing;
import org.eclipse.emf.transaction.ui.provider.TransactionalAdapterFactoryLabelProvider;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.explorer.internal.messages.Messages;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

@SuppressWarnings("restriction")
public class BasicExplorerLabelProvider extends BaseLabelProvider implements ICommonLabelProvider, IFontProvider, IStyledLabelProvider {

	protected Map<TransactionalEditingDomain, AdapterFactoryLabelProvider> modelLabelProviders = new WeakHashMap<TransactionalEditingDomain, AdapterFactoryLabelProvider>();

	protected ILabelProviderListener modelLabelProviderListener;

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
	}

	@Override
	public void saveState(IMemento memento) {
		// Do nothing by default
	}

	@Override
	public void restoreState(IMemento memento) {
		// Do nothing by default
	}

	protected AdapterFactoryLabelProvider getModelLabelProvider(Object element) {
		// Retrieve editing domain behind specified object
		TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(element);
		if (editingDomain != null) {
			// Retrieve model label provider for given editing domain; create new one if not existing yet
			AdapterFactoryLabelProvider modelLabelProvider = modelLabelProviders.get(editingDomain);
			if (modelLabelProvider == null) {
				modelLabelProvider = createModelLabelProvider(editingDomain);
				modelLabelProvider.addListener(getModelLabelProviderListener());
				modelLabelProviders.put(editingDomain, modelLabelProvider);
			}
			return modelLabelProvider;
		} else if (element instanceof EObject && ((EObject) element).eIsProxy()) {
			// Use non-transactional adapter factory label provider to avoid that proxified EObjects end up being
			// represented as empty tree nodes
			AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
			if (adapterFactory != null) {
				return new AdapterFactoryLabelProvider(adapterFactory);
			}
		}
		return null;
	}

	protected AdapterFactoryLabelProvider createModelLabelProvider(final TransactionalEditingDomain editingDomain) {
		Assert.isNotNull(editingDomain);
		AdapterFactory adapterFactory = getAdapterFactory(editingDomain);
		AdapterFactoryLabelProvider labelProvider = new TransactionalAdapterFactoryLabelProvider(editingDomain, adapterFactory) {
			@Override
			// Overridden to avoid the somewhat annoying logging of Eclipse exceptions resulting from event queue
			// dispatching that is done before transaction is acquired and actually starts to run
			protected <T> T run(RunnableWithResult<? extends T> run) {
				try {
					return TransactionUtil.runExclusive(editingDomain, run);
				} catch (Exception e) {
					Tracing.catching(TransactionalAdapterFactoryLabelProvider.class, "run", e); //$NON-NLS-1$

					// propagate interrupt status because we are not throwing
					Thread.currentThread().interrupt();

					return null;
				}
			}
		};

		// Set default font to enable the customized getFont()
		Font defaultFont = JFaceResources.getDefaultFont();
		labelProvider.setDefaultFont(defaultFont);

		return labelProvider;
	}

	/**
	 * Returns the {@link AdapterFactory adapter factory} to be used by this {@link BasicExplorerLabelProvider label
	 * provider} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject model
	 * element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns the {@link AdapterFactory adapter factory} which is embedded in the given
	 * <code>editingDomain</code> by default. Clients which want to use an alternative {@link AdapterFactory adapter
	 * factory} (e.g., an {@link AdapterFactory adapter factory} that creates {@link ItemProviderAdapter item provider}s
	 * which are specifically designed for the {@link IEditorPart editor} in which this
	 * {@link BasicExplorerLabelProvider label provider} is used) may override {@link #getCustomAdapterFactory()} and
	 * return any {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter
	 * factory} will then be returned as result by this method.
	 * </p>
	 *
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} whose embedded {@link AdapterFactory adapter
	 *            factory} is to be returned as default. May be left <code>null</code> if
	 *            {@link #getCustomAdapterFactory()} has been overridden and returns a non-<code>null</code> result.
	 * @return The {@link AdapterFactory adapter factory} that will be used by this {@link BasicExplorerLabelProvider
	 *         label provider}. <code>null</code> if no custom {@link AdapterFactory adapter factory} is provided
	 *         through {@link #getCustomAdapterFactory()} and no <code>editingDomain</code> has been specified.
	 * @see #getCustomAdapterFactory()
	 */
	protected AdapterFactory getAdapterFactory(TransactionalEditingDomain editingDomain) {
		AdapterFactory customAdapterFactory = getCustomAdapterFactory();
		if (customAdapterFactory != null) {
			return customAdapterFactory;
		} else if (editingDomain != null) {
			return ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
		}
		return null;
	}

	/**
	 * Returns a custom {@link AdapterFactory adapter factory} to be used by this {@link BasicExplorerLabelProvider
	 * label provider} for creating {@link ItemProviderAdapter item provider}s which control the way how {@link EObject
	 * model element}s from given <code>editingDomain</code> are displayed and can be edited.
	 * <p>
	 * This implementation returns <code>null</code> as default. Clients which want to use their own
	 * {@link AdapterFactory adapter factory} (e.g., an {@link AdapterFactory adapter factory} that creates
	 * {@link ItemProviderAdapter item provider}s which are specifically designed for the {@link IEditorPart editor} in
	 * which this {@link BasicExplorerLabelProvider label provider} is used) may override this method and return any
	 * {@link AdapterFactory adapter factory} of their choice. This custom {@link AdapterFactory adapter factory} will
	 * then be returned as result by {@link #getAdapterFactory(TransactionalEditingDomain)}.
	 * </p>
	 *
	 * @return The custom {@link AdapterFactory adapter factory} that is to be used by this
	 *         {@link BasicExplorerLabelProvider label provider}. <code>null</code> the default {@link AdapterFactory
	 *         adapter factory} returned by {@link #getAdapterFactory(TransactionalEditingDomain)} should be used
	 *         instead.
	 * @see #getAdapterFactory(TransactionalEditingDomain)
	 */
	protected AdapterFactory getCustomAdapterFactory() {
		return null;
	}

	/*
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	@Override
	public String getDescription(Object anElement) {
		// Don't try to retrieve descriptions for files - leave this to other common label providers which are more
		// appropriate
		if (!(anElement instanceof IFile)) {
			AdapterFactoryLabelProvider labelProvider = getModelLabelProvider(anElement);
			if (labelProvider != null) {
				String text = labelProvider.getText(anElement);
				if (text != null && text.length() != 0) {
					return text;
				} else {
					if (anElement instanceof FeatureMapEntryWrapperItemProvider) {
						FeatureMapEntryWrapperItemProvider provider = (FeatureMapEntryWrapperItemProvider) anElement;
						Object value = provider.getValue();
						if (value instanceof BasicFeatureMapEntry) {
							BasicFeatureMapEntry featureMapEntry = (BasicFeatureMapEntry) value;
							String label = NLS.bind(Messages.label_EmptyFeatureMapEntryLabel, new String[] {
									featureMapEntry.getEStructuralFeature().getName(), provider.getOwner().getClass().getSimpleName() });
							return label.replaceFirst("Impl$", ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
			}
			// Don't return empty String because otherwise the tabbed property sheet's title bar looses its
			// background color and becomes entirely blank
			return " "; //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public String getText(Object element) {
		// Don't try to retrieve texts for files - leave this to other common label providers which are more
		// appropriate
		if (!(element instanceof IFile)) {
			AdapterFactoryLabelProvider labelProvider = getModelLabelProvider(element);
			if (labelProvider != null) {
				return labelProvider.getText(element);
			}
			return ""; //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		// Don't try to retrieve images for files - leave this to other common label providers which are more
		// appropriate
		if (!(element instanceof IFile)) {
			AdapterFactoryLabelProvider labelProvider = getModelLabelProvider(element);
			if (labelProvider != null) {
				return labelProvider.getImage(element);
			}
		}
		return null;
	}

	@Override
	public void dispose() {
		for (AdapterFactoryLabelProvider labelProvider : modelLabelProviders.values()) {
			labelProvider.removeListener(modelLabelProviderListener);
		}
		modelLabelProviders.clear();
		super.dispose();
	}

	protected ILabelProviderListener getModelLabelProviderListener() {
		if (modelLabelProviderListener == null) {
			modelLabelProviderListener = createModelLabelProviderListener();
			Assert.isNotNull(modelLabelProviderListener);
		}
		return modelLabelProviderListener;
	}

	protected ILabelProviderListener createModelLabelProviderListener() {
		return new ILabelProviderListener() {
			@Override
			public void labelProviderChanged(LabelProviderChangedEvent event) {
				// Route LabelProviderChangedEvent from embedded AdapterFactoryLabelProvider to enclosing
				// BaseLabelProvider
				fireLabelProviderChanged(event);
			}
		};
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
	 */
	@Override
	public StyledString getStyledText(Object element) {
		if (!(element instanceof IFile)) {
			AdapterFactoryLabelProvider labelProvider = getModelLabelProvider(element);
			// FIXME Remove try/catch once we don't need to support Eclipse 4.3 (and EMF 2.9) any longer
			try {
				if (labelProvider != null) {
					return labelProvider.getStyledText(element);
				}
			} catch (NoSuchMethodError ex) {
				String text = labelProvider.getText(element);
				if (text != null && text.length() > 0) {
					return new StyledString(text);
				}
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	@Override
	public Font getFont(Object element) {
		if (!(element instanceof IFile)) {
			AdapterFactoryLabelProvider labelProvider = getModelLabelProvider(element);
			if (labelProvider != null) {
				return labelProvider.getFont(element);
			}
		}
		return null;
	}
}
