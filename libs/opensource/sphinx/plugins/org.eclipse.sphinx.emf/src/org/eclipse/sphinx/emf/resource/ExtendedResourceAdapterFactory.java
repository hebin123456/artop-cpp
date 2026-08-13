/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * An {@link AdapterFactory adapter factory} for creating {@link ExtendedResource} adapters.
 */
public class ExtendedResourceAdapterFactory extends AdapterFactoryImpl {

	/**
	 * The singleton instance of this {@link AdapterFactory adapter factory}.
	 */
	public static ExtendedResourceAdapterFactory INSTANCE = new ExtendedResourceAdapterFactory();

	// Protected default constructor for singleton pattern
	protected ExtendedResourceAdapterFactory() {
		// Nothing to do
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterFactoryImpl#isFactoryForType(java.lang.Object)
	 */
	@Override
	public boolean isFactoryForType(Object type) {
		return type == ExtendedResource.class;
	}

	/**
	 * Retrieves the {@link Resource resource} behind given {@link EObject} and returns either a previously associated
	 * {@link ExtendedResource} adapter or a newly associated one, as appropriate.
	 *
	 * @param eObject
	 *            The {@link EObject} the {@link Resource resource} of which is to be adapted.
	 * @return A previously existing associated {@link ExtendedResource} adapter of the {@link EObject}'s
	 *         {@link Resource resource}, a new associated {@link ExtendedResource} adapter if possible, or
	 *         <code>null</code> otherwise.
	 * @see AdapterFactory#adapt(Notifier, Object)
	 * @see Adapter#isAdapterForType(Object)
	 * @see AdapterFactory#adaptNew(Notifier, Object)
	 */
	public ExtendedResource getExtendedResource(EObject eObject) {
		if (eObject != null) {
			if (!eObject.eIsProxy()) {
				return adapt(eObject.eResource());
			} else {
				OldResourceProvider provider = (OldResourceProvider) EcoreUtil.getExistingAdapter(eObject, OldResourceProvider.class);
				if (provider != null) {
					return adapt(provider.getOldResource());
				}
			}
		}
		return null;
	}

	/**
	 * Returns either a previously associated {@link ExtendedResource} adapter or a newly associated one, as
	 * appropriate. {@link Adapter#isAdapterForType(Object) Checks} if an adapter for {@link ExtendedResource} is
	 * already associated with the target and returns it in that case; {@link AdapterFactory#adaptNew(Notifier, Object)
	 * creates} a new {@link ExtendedResource} adapter if possible otherwise.
	 *
	 * @param target
	 *            The notifier to adapt.
	 * @return A previously existing associated {@link ExtendedResource} adapter, a new associated
	 *         {@link ExtendedResource} adapter if possible, or <code>null</code> otherwise.
	 * @see AdapterFactory#adapt(Notifier, Object)
	 * @see Adapter#isAdapterForType(Object)
	 * @see AdapterFactory#adaptNew(Notifier, Object)
	 */
	public ExtendedResource adapt(Resource target) {
		if (target != null) {
			return (ExtendedResource) adapt(target, ExtendedResource.class);
		}
		return null;
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterFactoryImpl#createAdapter(org.eclipse.emf.common.notify.Notifier)
	 */
	@Override
	protected Adapter createAdapter(Notifier target) {
		if (target instanceof Resource) {
			return new ExtendedResourceAdapter();
		}
		return null;
	}
}
