/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [346945] Label decoration for proxyfied EObjects does not work correctly for multiplicity-many features
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [434239] Sphinx-enhanced property descriptors prevent values of unset derived EAttributes to be shown in properties view
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.edit;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.ecore.MessageEObjectImpl;
import org.eclipse.sphinx.emf.internal.EcorePerformanceStats;
import org.eclipse.sphinx.emf.internal.messages.Messages;

public class ExtendedItemPropertyDescriptor extends ItemPropertyDescriptor {

	/**
	 * This creates an instance that uses a resource locator and determines the cell editor from the type of the
	 * structural feature.
	 */
	public ExtendedItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
			EStructuralFeature feature, boolean isSettable) {
		super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, false, false, null, null, null);
	}

	/**
	 * This creates an instance that uses a resource locator, specifies a static image, and determines the cell editor
	 * from the type of the structural feature.
	 */
	public ExtendedItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
			EStructuralFeature feature, boolean isSettable, Object staticImage) {
		super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, false, false, staticImage, null, null);
	}

	/**
	 * This creates an instance that uses a resource locator, specifies a category and filter flags, and determines the
	 * cell editor from the type of the structural feature.
	 */
	public ExtendedItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
			EStructuralFeature feature, boolean isSettable, String category, String[] filterFlags) {
		super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, false, false, null, category, filterFlags);
	}

	/**
	 * This creates an instance that uses a resource locator; specifies a static image, a category, and filter flags;
	 * and determines the cell editor from the type of the structural feature.
	 */
	public ExtendedItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
			EStructuralFeature feature, boolean isSettable, Object staticImage, String category, String[] filterFlags) {
		super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, false, false, staticImage, category, filterFlags);

	}

	/**
	 * This creates an instance that uses a resource locator; indicates whether to be multi-line and to sort choices;
	 * specifies a static image, a category, and filter flags; and determines the cell editor from the type of the
	 * structural feature.
	 */
	public ExtendedItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
			EStructuralFeature feature, boolean isSettable, boolean multiLine, boolean sortChoices, Object staticImage, String category,
			String[] filterFlags) {
		super(adapterFactory, resourceLocator, displayName, description, feature, isSettable, multiLine, sortChoices, staticImage, category,
				filterFlags);
	}

	/**
	 * This creates an instance that uses a resource locator and determines the cell editor from the parent references.
	 */
	public ExtendedItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
			EReference[] parentReferences, boolean isSettable) {
		this(adapterFactory, resourceLocator, displayName, description, parentReferences, isSettable, null, null);
	}

	/**
	 * This creates an instance that uses a resource locator, specifies a category and filter flags, and determines the
	 * cell editor from the parent references.
	 */
	public ExtendedItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator, String displayName, String description,
			EReference[] parentReferences, boolean isSettable, String category, String[] filterFlags) {
		super(adapterFactory, resourceLocator, displayName, description, parentReferences, isSettable, category, filterFlags);
	}

	/*
	 * Overridden to avoid that the label used for displaying EObject proxies defaults to the the name of their type or,
	 * if set, the value of their label feature. Instead the proxy URI is used as label and an exclamation mark is added
	 * as overlay image so as to facilitate their identification in the UI.
	 * @see org.eclipse.emf.edit.provider.ItemPropertyDescriptor#getLabelProvider(java.lang.Object)
	 */
	@Override
	public IItemLabelProvider getLabelProvider(Object object) {
		final IItemLabelProvider delegate = super.getLabelProvider(object);
		return new IItemLabelProvider() {
			@Override
			public String getText(Object object) {
				if (object instanceof EList<?>) {
					StringBuffer text = new StringBuffer();
					for (Object child : (List<?>) object) {
						if (text.length() != 0) {
							text.append(", "); //$NON-NLS-1$
						}
						text.append(getText(child));
					}
					return text.toString();
				}
				if (object instanceof EObject && ((EObject) object).eIsProxy()) {
					URI proxyURI = EcoreUtil.getURI((EObject) object);
					return proxyURI != null ? proxyURI.toString() : Messages.label_unknownProxyURI;
				}

				// If object is a message EObject then display its message
				if (object instanceof MessageEObjectImpl) {
					return ((MessageEObjectImpl) object).getMessage();
				}

				return delegate.getText(object);
			}

			@Override
			public Object getImage(Object object) {
				if (object instanceof EList<?>) {
					for (Object item : (EList<?>) (EList<?>) object) {
						if (item instanceof EObject && ((EObject) item).eIsProxy()) {
							return getImage(item);
						}
					}
					for (Object item : (List<?>) object) {
						return getImage(item);
					}
					return null;
				}

				return delegate.getImage(object);
			}
		};
	}

	/*
	 * Overridden to avoid that default values for unsettable single-valued non-derived attributes are displayed unless
	 * they have been explicitly set.
	 * @see org.eclipse.emf.edit.provider.ItemPropertyDescriptor#getValue(org.eclipse.emf.ecore.EObject,
	 * org.eclipse.emf.ecore.EStructuralFeature)
	 */
	@Override
	protected Object getValue(EObject object, EStructuralFeature feature) {
		if (feature instanceof EAttribute && !feature.isMany() && !feature.isDerived() && feature.isUnsettable() && !object.eIsSet(feature)) {
			return null;
		}
		return super.getValue(object, feature);
	}

	/*
	 * Overridden for adding a postfix to the property description which indicates the property's data type and default
	 * value in case that the property represents a non string attribute.
	 * @see org.eclipse.emf.edit.provider.ItemPropertyDescriptor#getDescription(java.lang.Object)
	 */
	@Override
	public String getDescription(Object object) {
		StringBuilder description = new StringBuilder(super.getDescription(object));
		Object feature = getFeature(object);
		if (feature instanceof EAttribute) {
			EAttribute attribute = (EAttribute) feature;
			Class<?> instanceClass = attribute.getEType().getInstanceClass();
			if (String.class != instanceClass) {
				description.append(" "); //$NON-NLS-1$
				if (attribute.getDefaultValueLiteral() != null && attribute.getDefaultValueLiteral().length() > 0) {
					description.append(NLS.bind(Messages.propertyDescriptionPostfix_mustBeADataTypeEgDefaultValue, instanceClass.getSimpleName(),
							attribute.getDefaultValueLiteral()));
				} else {
					description.append(NLS.bind(Messages.propertyDescriptionPostfix_mustBeADataType, instanceClass.getSimpleName()));
				}
			}
		}
		return description.toString();
	}

	/*
	 * @see org.eclipse.emf.edit.provider.ItemPropertyDescriptor#getChoiceOfValues(java.lang.Object)
	 */
	@Override
	public Collection<?> getChoiceOfValues(Object object) {
		Collection<?> result;

		// Start profiling
		EcorePerformanceStats.INSTANCE.startEvent(EcorePerformanceStats.EcoreEvent.EVENT_CHOICES_OF_VALUES, object);

		// Collect the choices of values for the specified feature of the given object
		ExtendedItemProviderAdapter adapt = (ExtendedItemProviderAdapter) adapterFactory.adapt(object, IEditingDomainItemProvider.class);
		result = adapt.getChoiceOfValues(object, parentReferences, feature);

		// End profiling
		EcorePerformanceStats.INSTANCE.endEvent(EcorePerformanceStats.EcoreEvent.EVENT_CHOICES_OF_VALUES, object);
		return result;
	}
}
