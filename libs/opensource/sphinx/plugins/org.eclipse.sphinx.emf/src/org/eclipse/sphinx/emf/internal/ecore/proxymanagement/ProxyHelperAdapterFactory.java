/**
 * <copyright>
 * 
 * Copyright (c) 2008-2019 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Avoid usage of Object.finalize
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.ecore.proxymanagement;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.domain.factory.EditingDomainFactoryListenerRegistry;
import org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * An {@link AdapterFactory adapter factory} for creating {@link ProxyHelper} adapters on {@link ResourceSet}s.
 * 
 * @deprecated Will be removed as soon as a full-fledged model indexing service is in place and can be used to overcome
 *             performance bottlenecks due to proxy resolution.
 */
@Deprecated
public class ProxyHelperAdapterFactory extends AdapterFactoryImpl implements ITransactionalEditingDomainFactoryListener {

	/**
	 * The singleton instance of this {@link AdapterFactory adapter factory}.
	 */
	public static ProxyHelperAdapterFactory INSTANCE = new ProxyHelperAdapterFactory();

	// Protected default constructor for singleton pattern
	protected ProxyHelperAdapterFactory() {
	}

	public void start() {
		if (ExtendedPlatform.IS_PLATFORM_RUNNING) {
			EditingDomainFactoryListenerRegistry.INSTANCE.addListener(MetaModelDescriptorRegistry.ANY_MM, null, this, null);
		}
	}

	public void stop() {
		if (ExtendedPlatform.IS_PLATFORM_RUNNING) {
			EditingDomainFactoryListenerRegistry.INSTANCE.removeListener(this);
		}
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterFactoryImpl#isFactoryForType(java.lang.Object)
	 */
	@Override
	public boolean isFactoryForType(Object type) {
		return type == ProxyHelper.class;
	}

	/**
	 * Returns either a previously associated {@link ProxyHelper} adapter or a newly associated one, as appropriate.
	 * {@link Adapter#isAdapterForType(Object) Checks} if the an adapter for {@link ProxyHelper} is already associated
	 * with the target and returns it in that case; {@link AdapterFactory#adaptNew(Notifier, Object) creates} a new
	 * {@link ProxyHelper} adapter if possible otherwise.
	 * 
	 * @param target
	 *            The notifier to adapt.
	 * @return A previously existing associated {@link ProxyHelper} adapter, a new associated {@link ProxyHelper}
	 *         adapter if possible, or <code>null</code> otherwise.
	 * @see AdapterFactory#adapt(Notifier, Object)
	 * @see Adapter#isAdapterForType(Object)
	 * @see AdapterFactory#adaptNew(Notifier, Object)
	 */
	public ProxyHelper adapt(Notifier target) {
		if (target != null) {
			return (ProxyHelper) adapt(target, ProxyHelper.class);
		}
		return null;
	}

	/*
	 * @see org.eclipse.emf.common.notify.impl.AdapterFactoryImpl#createAdapter(org.eclipse.emf.common.notify.Notifier)
	 */
	@Override
	protected Adapter createAdapter(Notifier target) {
		if (target instanceof ResourceSet) {
			return new ProxyHelperAdapter();
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener#postCreateEditingDomain(org.
	 * eclipse .emf.transaction.TransactionalEditingDomain)
	 */
	@Override
	public void postCreateEditingDomain(TransactionalEditingDomain editingDomain) {
		// Nothing to do
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener#preDisposeEditingDomain(org.
	 * eclipse .emf.transaction.TransactionalEditingDomain)
	 */
	@Override
	public void preDisposeEditingDomain(TransactionalEditingDomain editingDomain) {
		Adapter adapter = EcoreUtil.getExistingAdapter(editingDomain.getResourceSet(), ProxyHelper.class);
		if (adapter instanceof ProxyHelperAdapter) {
			((ProxyHelperAdapter) adapter).dispose();
		}
	}
}
