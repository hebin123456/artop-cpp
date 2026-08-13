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
package org.eclipse.sphinx.emf.workspace.domain.mapping;

import java.util.List;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.saving.IResourceSaveIndicator;

/**
 * Interface representing the mapping between EditingDomain and IResources. There is only one
 * IWorkspaceEditingDomainMapping for the entire platform. It determines which EditingDomain is to be used to access
 * which IResource.
 */
public interface IWorkspaceEditingDomainMapping {

	/**
	 * Returns the {@link TransactionalEditingDomain editing domain} which corresponds to the given {@link IFile file}.
	 * 
	 * @param file
	 *            The {@link IFile file} whose {@link TransactionalEditingDomain editing domain} is to be returned.
	 * @return The {@link TransactionalEditingDomain editing domain} for the specified {@link IFile file}, or
	 *         <code>null</code> if the {@link IFile file} doesn't belong to any {@link TransactionalEditingDomain
	 *         editing domain}.
	 */
	TransactionalEditingDomain getEditingDomain(IFile file);

	/**
	 * Returns the {@link TransactionalEditingDomain editing domain} corresponding to the meta-model descriptor in
	 * parameter according to the given context container.
	 * 
	 * @param container
	 *            The container to use as context object.
	 * @param mmDescriptor
	 *            The meta-model descriptor for which {@link TransactionalEditingDomain editing domain} must be
	 *            returned.
	 * @return The {@link TransactionalEditingDomain editing domain} associated to the specified meta-model descriptor
	 *         according to the given context container.
	 */
	TransactionalEditingDomain getEditingDomain(IContainer container, IMetaModelDescriptor mmDescriptor);

	/**
	 * Returns the {@linkplain TransactionalEditingDomain editing domain}s associated with the {@linkplain IResource
	 * resource}s contained in the given {@linkplain IContainer container}. The method will recursively find all
	 * resources in the given {@link IContainer container} and will determine the {@link TransactionalEditingDomain
	 * editing domain}s with which the resources can be handled.
	 * 
	 * @param container
	 *            The {@linkplain IContainer container} containing the {@linkplain IResource resource}s for which the
	 *            {@linkplain TransactionalEditingDomain editing domain}s are to be returned.
	 * @return The {@linkplain TransactionalEditingDomain editing domain}s associated with the {@linkplain IResource
	 *         resource}s in the specified {@link IContainer container}.
	 */
	List<TransactionalEditingDomain> getEditingDomains(IContainer container);

	/**
	 * Returns all EditingDomains in the Workspace.
	 * 
	 * @return The list of TransactionalEditingDomains in the Workspace.
	 */
	List<TransactionalEditingDomain> getEditingDomains();

	/**
	 * Registers a listener for all model changes in the workspace. The registered ResourceSetListener's
	 * {@link org.eclipse.emf.transaction.ResourceSetListener#resourceSetChanged} will be called every time a loaded
	 * IResource in the workspace is changed.
	 * 
	 * @param listener
	 *            The listener to be notified of the model changes.
	 */
	void addGlobalResourceSetListener(ResourceSetListener listener);

	/**
	 * Removes a ResourceSetListener listening to all model changes in the workspace. If the listener has not been added
	 * previously with
	 * {@link org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping#addGlobalResourceSetListener(ResourceSetListener)}
	 * the method will do nothing.
	 * 
	 * @param listener
	 *            The listener to be removed.
	 */
	void removeGlobalResourceSetListener(ResourceSetListener listener);

	/**
	 * Registers a listener for all OperationHistories in the workspace. The registered IOperationHistoryListener's
	 * {@linkplain org.eclipse.core.commands.operations.IOperationHistoryListener#historyNotification(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 * IOperationHistoryListener.historyNotification(OperationHistoryEvent)} will be called every time a something of
	 * note has happened in any OperationHistory in the workspace.
	 * 
	 * @param listener
	 *            The listener to be notified of the events in the Workspace's OperationHistories.
	 */
	void addGlobalOperationHistoryListener(IOperationHistoryListener listener);

	/**
	 * Removes a registered IOperationHistoryListener which is listening to all OperationHistories in the workspace. If
	 * the listener has not been added previously with
	 * {@link org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping#addGlobalOperationHistoryListener(IOperationHistoryListener)}
	 * this method will do nothing.
	 * 
	 * @param listener
	 *            The listener to be removed.
	 */
	void removeGlobalOperationHistoryListener(IOperationHistoryListener listener);

	/**
	 * Returns an IResourceSaveIndicator for the provided EditingDomain.
	 * 
	 * @param editingDomain
	 *            The EditingDomain for which the IResourceSaveIndicator is to be returned.
	 * @return An IResourceSaveIndicator for the given EditingDoamin.
	 */
	IResourceSaveIndicator getResourceSaveIndicator(TransactionalEditingDomain editingDomain);

	/**
	 * Disposes of this {@link TransactionalEditingDomain editing domain} mapping and any resources that it has
	 * allocated. Editing domain mappings must be disposed when they are no longer in use, but only by the client that
	 * created them (in case of sharing of editing domain mappings).
	 * <p>
	 * <b>Note</b> that {@link TransactionalEditingDomain editing domain} mappings registered on the extension point may
	 * not be disposed.
	 * </p>
	 */
	void dispose();
}
