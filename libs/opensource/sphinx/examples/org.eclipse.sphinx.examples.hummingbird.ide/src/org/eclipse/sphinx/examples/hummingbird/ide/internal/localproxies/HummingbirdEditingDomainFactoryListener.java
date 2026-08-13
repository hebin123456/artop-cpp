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
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird.ide.internal.localproxies;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener;
import org.eclipse.sphinx.emf.edit.LocalProxyChangeListener;

/**
 * 
 */
public class HummingbirdEditingDomainFactoryListener implements ITransactionalEditingDomainFactoryListener {
	private LocalProxyChangeListener localProxyChangeListener = new LocalProxyChangeListener();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postCreateEditingDomain(TransactionalEditingDomain editingDomain) {
		// Install local proxy management
		editingDomain.addResourceSetListener(localProxyChangeListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preDisposeEditingDomain(TransactionalEditingDomain editingDomain) {
		// Uninstall local proxy management
		editingDomain.removeResourceSetListener(localProxyChangeListener);
	}
}
