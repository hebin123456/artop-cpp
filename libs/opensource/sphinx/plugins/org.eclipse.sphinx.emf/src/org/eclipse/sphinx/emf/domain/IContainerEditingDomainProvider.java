/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.domain;

import java.util.Collection;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

/**
 * Container Editing Domain provider.
 * <p>
 * Provides editing domains for a specified workspace root, project or folder.
 */
public interface IContainerEditingDomainProvider {

	/**
	 * Returns all editing domains in the IContainer (workspace root, project or folder).
	 * 
	 * @return A collection of Editing Domains.
	 */
	Collection<TransactionalEditingDomain> getEditingDomains();

	/**
	 * Retrieves the editing domain corresponding to the specified meta-model descriptor for the given context
	 * container.
	 * 
	 * @param descriptor
	 *            A meta-model descriptor.
	 * @return The editing domain for the specified meta-model descriptor and the specified context container or
	 *         <code>null</code> if it cannot be mapped to any editing domain.
	 */
	TransactionalEditingDomain getEditingDomain(IMetaModelDescriptor descriptor);
}
