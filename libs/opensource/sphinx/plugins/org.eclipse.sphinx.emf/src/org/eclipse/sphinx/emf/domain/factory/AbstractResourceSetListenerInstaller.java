/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.emf.domain.factory;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * An abstract {@link ResourceSetListenerImpl resource set listener} installer that can be used to automatically create
 * and install resource set listeners with the type specified by <T> on newly created {@link TransactionalEditingDomain
 * transactional editing domain}s.
 * <p>
 * Recommended usage:
 * <ul>
 * <li>Create a subclass of AbstractResourceSetListenerInstaller<T></li>
 * <li>Bind type parameter <T> to type of resource set listeners that should be automatically created and installed on
 * newly created transactional editing domains</li>
 * <li>Override default constructor, invoke AbstractResourceSetListenerInstaller(Class<T>) constructor on super class,
 * and pass along the type of resource set listeners that should be automatically created and installed on newly created
 * transactional editing domains as argument</li>
 * <li>Contribute the subclass of AbstractResourceSetListenerInstaller<T> to the
 * <code>org.eclipse.sphinx.emf.editingDomainFactoryListeners</code> extension point</li>
 * </ul>
 * </p>
 * 
 * @param <T>
 *            The type of {@link ResourceSetListenerImpl resource set listener}s that this installer automatically
 *            creates and installs on newly created {@link TransactionalEditingDomain transactional editing domain}s
 */
public abstract class AbstractResourceSetListenerInstaller<T extends ResourceSetListenerImpl> implements ITransactionalEditingDomainFactoryListener {

	private Class<T> resourceSetListenerType;
	private Map<TransactionalEditingDomain, T> resourceSetListeners = new HashMap<TransactionalEditingDomain, T>();

	public AbstractResourceSetListenerInstaller(Class<T> resourceSetListenerType) {
		Assert.isNotNull(resourceSetListenerType);

		this.resourceSetListenerType = resourceSetListenerType;
	}

	/**
	 * Creates a new resource set listener instance with the type specified by <T>.
	 * 
	 * @return The newly created resource set listener instance.
	 */
	protected T createResourceSetListener() {
		try {
			return resourceSetListenerType.newInstance();
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener#postCreateEditingDomain(org.
	 * eclipse.emf.transaction.TransactionalEditingDomain)
	 */
	@Override
	public void postCreateEditingDomain(TransactionalEditingDomain editingDomain) {
		// Do nothing if a resource set listener from this installer is already present on given editing domain
		// (should normally not happen)
		if (resourceSetListeners.containsKey(editingDomain)) {
			return;
		}

		// Create new resource set listener
		T listener = createResourceSetListener();
		if (listener != null) {
			// Install it on given editing domain ...
			editingDomain.addResourceSetListener(listener);

			// ... and remember it
			resourceSetListeners.put(editingDomain, listener);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener#preDisposeEditingDomain(org.
	 * eclipse.emf.transaction.TransactionalEditingDomain)
	 */
	@Override
	public void preDisposeEditingDomain(TransactionalEditingDomain editingDomain) {
		// Retrieve resource set listener that has been installed by this installer on given editing domain
		T listener = resourceSetListeners.get(editingDomain);
		if (listener != null) {
			// Remove it from given editing domain ...
			editingDomain.removeResourceSetListener(listener);

			// ... and forget it
			resourceSetListeners.remove(editingDomain);
		}
	}
}
