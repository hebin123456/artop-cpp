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
package org.eclipse.sphinx.emf.workspace.domain.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.domain.factory.IExtendedTransactionalEditingDomainFactory;
import org.eclipse.sphinx.platform.messages.PlatformMessages;

/**
 * Default editing domain mapping. This implementation uses one shared editing domain per type of meta-model on the
 * workspace.
 */
public class DefaultWorkspaceEditingDomainMapping extends AbstractWorkspaceEditingDomainMapping {

	/**
	 * Map associating one editing domain to each type of meta-model.
	 */
	protected Map<IMetaModelDescriptor, TransactionalEditingDomain> mappedEditingDomains = Collections
			.synchronizedMap(new HashMap<IMetaModelDescriptor, TransactionalEditingDomain>());

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.domain.mapping.AbstractWorkspaceEditingDomainMapping#createEditingDomain(org
	 * .eclipse .sphinx.emf.workspace.domain.factory.IExtendedTransactionalEditingDomainFactory, java.util.Collection)
	 */
	@Override
	protected TransactionalEditingDomain createEditingDomain(IExtendedTransactionalEditingDomainFactory factory,
			Collection<IMetaModelDescriptor> mmDescriptors) {
		if (mmDescriptors.size() != 1) {
			throw new IllegalArgumentException(NLS.bind(PlatformMessages.error_unexpectedArrayLength, mmDescriptors.size(), 1));
		}
		return super.createEditingDomain(factory, mmDescriptors);
	}

	/**
	 * <p>
	 * <table>
	 * <tr valign=top>
	 * <td><b>Note</b>&nbsp;&nbsp;</td>
	 * <td>In order to retrieve the right {@linkplain TransactionalEditingDomain editing domain} according to the
	 * specified {@linkplain IMetaModelDescriptor meta-model descriptor} in a thread safe manner, the access to
	 * {@link DefaultWorkspaceEditingDomainMapping#mappedEditingDomains mappedEditingDomains} is <b>synchronized</b>. In
	 * that way, if two concurrent threads need to get a non-already created editing domain:
	 * <ol>
	 * <li>Both threads won't find any editing domain matching the specified meta-model descriptor;</li>
	 * <li>Both will ask for the creation of a new editing domain;</li>
	 * <li>Both will try to register this new editing domain into the <code>mappedEditingDomains</code>:
	 * <ul>
	 * <li>One of these two threads first enters the synchronized section. The <code>mappedEditingDomains</code> still
	 * do not contain any editing domain for the specified meta-model descriptor; as a consequence it adds it in that
	 * map and exits the synchronized section;</li>
	 * <li>The second thread enters the synchronized section. As the first thread already registered an editing domain
	 * for the specified meta-model descriptor, that second thread just gets that same editing domain instance (in order
	 * to finally return it) and exits the synchronized section.</li>
	 * </ul>
	 * </li>
	 * <ol></td>
	 * </tr>
	 * </table>
	 * 
	 * @see {@linkplain IWorkspaceEditingDomainMapping#getEditingDomain(IContainer, IMetaModelDescriptor)}
	 */
	@Override
	public TransactionalEditingDomain getEditingDomain(IContainer container, IMetaModelDescriptor mmDescriptor) {
		if (mmDescriptor != null) {
			synchronized (mappedEditingDomains) {
				TransactionalEditingDomain editingDomain = mappedEditingDomains.get(mmDescriptor);
				if (editingDomain == null) {
					// Obtain the right editing domain factory
					IExtendedTransactionalEditingDomainFactory factory = getEditingDomainFactory(mmDescriptor);
					// Ask for the creation of a new editing domain
					editingDomain = createEditingDomain(factory, Collections.singleton(mmDescriptor));
					// Register the newly created editing domain
					mappedEditingDomains.put(mmDescriptor, editingDomain);
				}
				return editingDomain;
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping#getEditingDomains()
	 */
	@Override
	public List<TransactionalEditingDomain> getEditingDomains() {
		return Collections.unmodifiableList(new ArrayList<TransactionalEditingDomain>(mappedEditingDomains.values()));
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.domain.mapping.AbstractWorkspaceEditingDomainMapping#preDisposeEditingDomain
	 * (org. eclipse.emf.transaction.TransactionalEditingDomain)
	 */
	@Override
	public void preDisposeEditingDomain(TransactionalEditingDomain editingDomain) {
		// Remove EditingDomain to be disposed from mapping
		mappedEditingDomains.values().remove(editingDomain);
		super.preDisposeEditingDomain(editingDomain);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.domain.mapping.AbstractWorkspaceEditingDomainMapping#dispose()
	 */
	@Override
	public void dispose() {
		/*
		 * !! Important Note !! Perform iteration over unsynchronized copy of mapped editing domain set in order to
		 * avoid deadlocks between this thread intending to dispose all mapped editing domains and concurrent threads
		 * needing to access synchronized mapped editing domains meanwhile.
		 */
		List<TransactionalEditingDomain> unsynchronizedMappedEditingDomains = new ArrayList<TransactionalEditingDomain>(mappedEditingDomains.values());
		for (TransactionalEditingDomain editingDomain : unsynchronizedMappedEditingDomains) {
			editingDomain.dispose();
		}
		super.dispose();
	}
}
