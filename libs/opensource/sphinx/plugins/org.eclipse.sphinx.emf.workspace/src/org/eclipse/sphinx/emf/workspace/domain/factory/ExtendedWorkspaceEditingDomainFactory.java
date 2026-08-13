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
import java.util.HashSet;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.AbstractTransactionalCommandStack;
import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
import org.eclipse.emf.workspace.WorkspaceEditingDomainFactory;
import org.eclipse.emf.workspace.impl.WorkspaceCommandStackImpl;
import org.eclipse.sphinx.emf.domain.factory.EditingDomainFactoryListenerRegistry;
import org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;

/**
 * Enables adaptable {@linkplain TransactionalEditingDomain Workspace Editing Domain}s to be created which can be
 * extended with additional behaviors by clients. Adds an {@linkplain IEditingDomainProvider} adapter on the
 * WorkspaceEditingDomain's {@linkplain ResourceSet resource set} so that it can be accessed from {@linkplain EObject}s
 * or {@linkplain Resource}s by using {@link AdapterFactoryEditingDomain#getEditingDomainFor(Object)}.<br>
 * Supports association and creation of model ResourceFactories based on namespaces of model files being loaded/saved.
 * <p>
 * <table>
 * <tr valign=top>
 * <td><b>Note</b>&nbsp;&nbsp;</td>
 * <td>This factory implements {@linkplain INotifyingTransactionalEditingDomainFactory} in order to allow performing
 * additional treatments when editing domain is going to be disposed.</td>
 * </tr>
 * </table>
 */
public class ExtendedWorkspaceEditingDomainFactory extends WorkspaceEditingDomainFactory implements IExtendedTransactionalEditingDomainFactory {

	/**
	 * <p align=center>
	 * <b><em> Model Extended Workspace Editing Domain </em></b>
	 * </p>
	 */
	protected class ExtendedWorkspaceEditingDomain extends TransactionalEditingDomainImpl implements IAdaptable {

		private Collection<IMetaModelDescriptor> fMetaModelDescriptors = new HashSet<IMetaModelDescriptor>();

		public ExtendedWorkspaceEditingDomain(AdapterFactory adapterFactory) {
			super(adapterFactory);
		}

		public ExtendedWorkspaceEditingDomain(AdapterFactory adapterFactory, ResourceSet resourceSet) {
			super(adapterFactory, resourceSet);
		}

		public ExtendedWorkspaceEditingDomain(AdapterFactory adapterFactory, TransactionalCommandStack commandStack) {
			super(adapterFactory, commandStack);
		}

		public ExtendedWorkspaceEditingDomain(AdapterFactory adapterFactory, TransactionalCommandStack commandStack, ResourceSet resourceSet) {
			super(adapterFactory, commandStack, resourceSet);
		}

		/**
		 * Returns an object which is an instance of the given class associated with this object. Returns
		 * <code>null</code> if no such object can be found.
		 * <p>
		 * This implementation of the method declared by <code>IAdaptable</code> passes the request along to the
		 * platform's adapter manager; roughly <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
		 * Subclasses may override this method (however, if they do so, they should invoke the method on their
		 * superclass to ensure that the Platform's adapter manager is consulted).
		 * </p>
		 * 
		 * @param adapter
		 *            the class to adapt to
		 * @return the adapted object or <code>null</code>
		 * @see IAdaptable#getAdapter(Class)
		 * @see Platform#getAdapterManager()
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object getAdapter(Class adapterType) {
			Object adapter = super.getAdapter(adapterType);
			if (adapter != null) {
				return adapter;
			}
			return Platform.getAdapterManager().getAdapter(this, adapterType);
		}

		public Collection<IMetaModelDescriptor> getMetaModelDescriptors() {
			return fMetaModelDescriptors;
		}

		@Override
		public boolean isReadOnly(Resource resource) {
			// FIXME File bug to EMF: NPE is raised when opening a platform:/plugin resource and resolving its URL in an
			// editor while workspace is closing
			try {
				return super.isReadOnly(resource);
			} catch (NullPointerException ex) {
				return true;
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		protected boolean isReadOnlyURI(URI uri) {
			// FIXME File bug to EMF: NPE is raised when calling this method for an URI that doesn't exist
			if (!EcoreResourceUtil.exists(uri)) {
				return true;
			}
			return super.isReadOnlyURI(uri);
		}

		@Override
		public void dispose() {
			/*
			 * !! Important Note !! First unload all resources in order to give a chance to registered
			 * ResourceSetListeners to act upon as needed. Wait until all of them are done and only then proceed with
			 * unregistering and disposing editing domain itself.
			 */
			EcorePlatformUtil.unloadAllResources(this, null);
			try {
				Job.getJobManager().join(IExtendedPlatformConstants.FAMILY_MODEL_LOADING, null);
			} catch (Exception ex) {
				// Ignore exception
			}
			firePreDisposeEditingDomain(fMetaModelDescriptors, this);
			super.dispose();
		}

		@Override
		public String toString() {
			return getID();
		}
	}

	protected ResourceSet createResourceSet() {
		return new ScopingResourceSetImpl();
	}

	protected IOperationHistory createOperationHistory() {
		return OperationHistoryFactory.getOperationHistory();
	}

	@Override
	public TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors) {
		return createEditingDomain(metaModelDescriptors, createResourceSet(), createOperationHistory());
	}

	public TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors, IOperationHistory history) {
		return createEditingDomain(metaModelDescriptors, createResourceSet(), history);
	}

	@Override
	public TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors, ResourceSet resourceSet) {
		return createEditingDomain(metaModelDescriptors, resourceSet, createOperationHistory());
	}

	public TransactionalEditingDomain createEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors, ResourceSet resourceSet,
			IOperationHistory history) {

		// Create new WorkspaceCommandStack and TransactionalEditingDomain using given IOperationHistory and ResourceSet
		WorkspaceCommandStackImpl stack = new WorkspaceCommandStackImpl(history) {
			/*
			 * Overridden for passing {@link WorkspaceTransactionUtil#getDefaultTransactionOptions()} rather than
			 * <code>null</code> to {@link WorkspaceCommandStackImpl#execute(Command, Map<?, ?>)}.
			 */
			@SuppressWarnings("restriction")
			@Override
			public void execute(Command command) {
				try {
					execute(command, WorkspaceTransactionUtil.getDefaultTransactionOptions());
				} catch (InterruptedException e) {
					// just log it. Note that the transaction is already rolled back,
					// so handleError() will not find an active transaction
					org.eclipse.emf.transaction.internal.Tracing.catching(AbstractTransactionalCommandStack.class, "execute", e); //$NON-NLS-1$
					handleError(e);
				} catch (RollbackException e) {
					// just log it. Note that the transaction is already rolled back,
					// so handleError() will not find an active transaction
					org.eclipse.emf.transaction.internal.Tracing.catching(AbstractTransactionalCommandStack.class, "execute", e); //$NON-NLS-1$
					handleError(e);
				}
			}
		};
		TransactionalEditingDomain result = new ExtendedWorkspaceEditingDomain(new ComposedAdapterFactory(
				ComposedAdapterFactory.Descriptor.Registry.INSTANCE), stack, resourceSet);

		// Do default initialization
		mapResourceSet(result);

		// Add IEditingDomainProvider adapter which EMF.Edit needs for retrieving EditingDomain from ResourceSet
		resourceSet.eAdapters().add(new AdapterFactoryEditingDomain.EditingDomainProvider(result));

		// Give the specified meta-model descriptors to the newly created editing domain
		((ExtendedWorkspaceEditingDomain) result).getMetaModelDescriptors().addAll(metaModelDescriptors);

		firePostCreateEditingDomain(metaModelDescriptors, result);
		return result;
	}

	protected void firePostCreateEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors, TransactionalEditingDomain editingDomain) {
		for (IMetaModelDescriptor mmd : metaModelDescriptors) {
			// Retrieve and notify TEDFactoryListeners contributed to
			// 'org.eclipse.sphinx.emf.editingDomainFactoryListeners'
			for (ITransactionalEditingDomainFactoryListener listener : EditingDomainFactoryListenerRegistry.INSTANCE.getListeners(mmd)) {
				listener.postCreateEditingDomain(editingDomain);
			}
		}
	}

	protected void firePreDisposeEditingDomain(Collection<IMetaModelDescriptor> metaModelDescriptors, TransactionalEditingDomain editingDomain) {
		for (IMetaModelDescriptor mmd : metaModelDescriptors) {
			// Retrieve and notify TEDFactoryListeners contributed to
			// 'org.eclipse.sphinx.emf.editingDomainFactoryListeners'
			for (ITransactionalEditingDomainFactoryListener listener : EditingDomainFactoryListenerRegistry.INSTANCE.getListeners(mmd)) {
				listener.preDisposeEditingDomain(editingDomain);
			}
		}
	}
}
