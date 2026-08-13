/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4Sys - Initial API and implementation
 *     itemis - Improved application of changed proxy URI to model so as to enable consistent update of the model's dirty state 
 *              without needing to invoke org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager#notifyDirtyChanged(Object) 
 *              and introducing dependency from EMF Runtime Extensions to Workspace Management
 *     itemis - [393869] Proxy objects should be removed from model when users delete proxy URI displayed as target for a model 
 *                       object references in advanced property sheet page
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.swt.widgets.Composite;

/**
 * This class extends TextCellEditor and is used in
 * {@link BasicTransactionalAdvancedPropertySection#createModelPropertySourceProvider(org.eclipse.emf.transaction.TransactionalEditingDomain)}
 * to enable the edition of the proxy URI of a proxy inside the property sheet page of a single feature's reference
 * element. This let to edit the proxy URI in text mode, validate and set this value to the concerned proxy object.
 */
public class ProxyURICellEditor extends TextCellEditor {

	/**
	 * A delegate for handling validation and conversion for proxy URIs.
	 */
	protected static class ProxyURIHandler implements ICellEditorValidator {

		protected EObject owner;
		protected EObject value;

		public ProxyURIHandler(EObject owner, EObject value) {
			Assert.isNotNull(owner);
			Assert.isNotNull(value);

			this.owner = owner;
			this.value = value;
		}

		@Override
		public String isValid(Object value) {
			ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(owner.eResource());
			if (extendedResource != null) {
				if (value instanceof String) {
					Diagnostic diagnostic = extendedResource.validateURI((String) value);
					return Diagnostic.OK_INSTANCE == diagnostic ? null : diagnostic.getMessage();
				}
			}
			return null;
		}

		public Object toObject(String valueAsString) {
			if (valueAsString.length() > 0) {
				// A new proxy URI has been entered; check if it is different from previous one and create a new
				// corresponding proxy object to get the old one replaced with if so
				URI proxyURI = URI.createURI(valueAsString);
				if (value == null || !proxyURI.equals(((InternalEObject) value).eProxyURI())) {
					EFactory factory = value.eClass().getEPackage().getEFactoryInstance();
					value = factory.create(value.eClass());
					((InternalEObject) value).eSetProxyURI(proxyURI);
				}
			} else {
				// Proxy URI has been deleted; check if a proxy URI still existed previously and set proxy object to
				// null to get the old one removed from model if so
				if (value != null) {
					value = null;
				}
			}
			return value;
		}

		public String toString(Object valueAsObject) {
			if (valueAsObject instanceof String) {
				return (String) valueAsObject;
			} else {
				if (valueAsObject instanceof EObject) {
					InternalEObject internalValue = (InternalEObject) valueAsObject;
					if (internalValue.eIsProxy()) {
						return internalValue.eProxyURI().toString();
					}
				}
			}
			return ""; //$NON-NLS-1$
		}
	}

	protected ProxyURIHandler valueHandler;

	public ProxyURICellEditor(Composite parent, EObject owner, final EStructuralFeature feature, EObject value) {
		super(parent);
		Assert.isNotNull(owner);
		Assert.isNotNull(feature);
		Assert.isNotNull(value);

		valueHandler = new ProxyURIHandler(owner, value);
		setValidator(valueHandler);
	}

	@Override
	public Object doGetValue() {
		String valueAsString = (String) super.doGetValue();
		return valueHandler.toObject(valueAsString);
	}

	@Override
	public void doSetValue(Object value) {
		String valueAsString = valueHandler.toString(value);
		super.doSetValue(valueAsString);
	}
}
