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
package org.eclipse.sphinx.emf.workspace.domain.factory;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

/**
 * 
 */
public class DelegatingTransactionalEditingDomainFactory implements IExtendedTransactionalEditingDomainFactory {

	/**
	 * The transactional editing domain factory to use as delegate for factory handling.
	 */
	protected IExtendedTransactionalEditingDomainFactory factoryDelegate;

	/**
	 * Constructor.
	 * 
	 * @param factoryDelegate
	 *            The transactional editing domain factory to use as delegate.
	 */
	public DelegatingTransactionalEditingDomainFactory(IExtendedTransactionalEditingDomainFactory factoryDelegate) {
		Assert.isNotNull(factoryDelegate);
		this.factoryDelegate = factoryDelegate;
	}

	protected ResourceSet createResourceSet() {
		return null;
	}

	@Override
	public TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors) {
		ResourceSet resourceSet = createResourceSet();
		if (resourceSet == null) {
			return factoryDelegate.createEditingDomain(metaModelDescriptors);
		} else {
			return factoryDelegate.createEditingDomain(metaModelDescriptors, resourceSet);
		}
	}

	@Override
	public TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors, ResourceSet resourceSet) {
		return factoryDelegate.createEditingDomain(metaModelDescriptors, resourceSet);
	}
}
