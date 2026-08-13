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
package org.eclipse.sphinx.emf.workspace.internal;

import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.domain.IContainerEditingDomainProvider;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.saving.IResourceSaveIndicator;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping;

/**
 * Adapter Factory for Editing Domain that supports the following adapter types:
 * <ul>
 * <li>{@link IEditingDomainProvider} that can adapt a meta-model version descriptor or a file;</li>
 * <li>{@link IResourceSaveIndicator} that can adapt a transactional editing domain.</li>
 * </ul>
 * 
 * @see IAdapterFactory
 */
public class EditingDomainAdapterFactory implements IAdapterFactory {

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(final Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		// The workspace editing domain mapping
		final IWorkspaceEditingDomainMapping mapping = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainMapping();

		if (adapterType.equals(IEditingDomainProvider.class)) {
			// IEditingDomainProvider adapter for IFile?
			if (adaptableObject instanceof IFile) {
				return new IEditingDomainProvider() {
					@Override
					public EditingDomain getEditingDomain() {
						return mapping.getEditingDomain((IFile) adaptableObject);
					}
				};
			}
		} else if (adapterType.equals(IContainerEditingDomainProvider.class)) {
			// IContainerEditingDomainProvider adapter for IContainer?
			if (adaptableObject instanceof IContainer) {
				return new IContainerEditingDomainProvider() {
					@Override
					public Collection<TransactionalEditingDomain> getEditingDomains() {
						return mapping.getEditingDomains((IContainer) adaptableObject);
					}

					@Override
					public TransactionalEditingDomain getEditingDomain(IMetaModelDescriptor mmDescriptor) {
						return mapping.getEditingDomain((IContainer) adaptableObject, mmDescriptor);
					}
				};
			}
		} else if (adapterType.equals(IResourceSaveIndicator.class)) {
			// IResourceSaveIndicator adapter for TransactionalEditingDomain?
			if (adaptableObject instanceof TransactionalEditingDomain) {
				return mapping.getResourceSaveIndicator((TransactionalEditingDomain) adaptableObject);
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class<?>[] { IEditingDomainProvider.class, IContainerEditingDomainProvider.class, IResourceSaveIndicator.class };
	}
}
