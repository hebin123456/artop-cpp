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

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

/**
 * 
 */
public interface IExtendedTransactionalEditingDomainFactory {

	/**
	 * Creates an editing domain with a default resource set implementation.
	 * 
	 * @param metaModelDescriptors
	 *            The collection of {@linkplain IMetaModelDescriptor meta-model descriptor}s the editing domain to be
	 *            created could be associated to.
	 * @return The newly created {@linkplain TransactionalEditingDomain editing domain}.
	 */
	TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors);

	/**
	 * Creates a new transactional editing domain on the specified resource set. Although it is possible to create
	 * multiple editing domains on the same resource set, this would rarely be useful.
	 * 
	 * @param metaModelDescriptors
	 *            The collection of {@linkplain IMetaModelDescriptor meta-model descriptor}s the editing domain to be
	 *            created could be associated to.
	 * @param resourceSet
	 *            the resource set
	 * @return A new {@linkplain TransactionalEditingDomain editing domain} on the supplied {@linkplain ResourceSet
	 *         resource set}.
	 */
	TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors, ResourceSet resourceSet);
}
