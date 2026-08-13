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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.domain.factory.EditingDomainFactoryListenerRegistry;
import org.eclipse.sphinx.emf.domain.factory.ITransactionalEditingDomainFactoryListener;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.saving.IResourceSaveIndicator;
import org.eclipse.sphinx.emf.util.WorkspaceTransactionUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.domain.WorkspaceEditingDomainManager;
import org.eclipse.sphinx.emf.workspace.domain.factory.IExtendedTransactionalEditingDomainFactory;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.internal.saving.ResourceSaveIndicator;
import org.eclipse.sphinx.platform.resources.AbstractResourceVisitor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Abstract implementation of the {@link IWorkspaceEditingDomainMapping} which provides default implementations for some
 * of the methods. The class implements the {@link ITransactionalEditingDomainFactoryListener} interface in order to
 * stay up-to-date with the current EditingDomains. GlobalListeners are handled this way.
 */
public abstract class AbstractWorkspaceEditingDomainMapping implements IWorkspaceEditingDomainMapping, ITransactionalEditingDomainFactoryListener {

	protected static final String EDITING_DOMAIN_ID_PREFIX = "editingDomainFor"; //$NON-NLS-1$

	/**
	 * Returns a default identifier for the specified {@link TransactionalEditingDomain editingDomain}. It is composed
	 * of the prefix {@value #EDITING_DOMAIN_ID_PREFIX} followed by the identifiers of the specified
	 * {@link IMetaModelDescriptor}s. Implemented as static method in order to be accessible from test cases.
	 * 
	 * @param mmDescriptors
	 *            A list of {@linkplain IMetaModelDescriptor}s the given editing domain should be associated to.
	 * @return An unique identifier for the specified {@link TransactionalEditingDomain editingDomain}.
	 */
	public static String getDefaultEditingDomainId(Collection<IMetaModelDescriptor> mmDescriptors) {
		StringBuilder id = new StringBuilder(EDITING_DOMAIN_ID_PREFIX);
		for (IMetaModelDescriptor descriptor : mmDescriptors) {
			id.append("_"); //$NON-NLS-1$
			id.append(descriptor.getIdentifier());
		}
		return id.toString();
	}

	protected Map<TransactionalEditingDomain, IResourceSaveIndicator> resourceSaveIndicators = new WeakHashMap<TransactionalEditingDomain, IResourceSaveIndicator>();
	protected Map<TransactionalEditingDomain, WorkspaceSynchronizer> modelWorkspaceSynchronizers = new WeakHashMap<TransactionalEditingDomain, WorkspaceSynchronizer>();

	protected ListenerList globalResourceSetListeners = new ListenerList();
	protected ListenerList globalOperationHistoryListeners = new ListenerList();

	protected AbstractWorkspaceEditingDomainMapping() {
		// Register this EditingDomainMapping as listener of EditingDomainFactory
		EditingDomainFactoryListenerRegistry.INSTANCE.addListener(MetaModelDescriptorRegistry.ANY_MM, null, this, null);
	}

	protected TransactionalEditingDomain createEditingDomain(IExtendedTransactionalEditingDomainFactory factory,
			Collection<IMetaModelDescriptor> mmDescriptors) {
		Assert.isNotNull(factory);

		// Create new EditingDomain using appropriate EditingDomainFactory
		TransactionalEditingDomain editingDomain = factory.createEditingDomain(mmDescriptors);

		String editingDomainId = getEditingDomainId(editingDomain, mmDescriptors);

		// Register newly created EdtingDomain in order to enable ResourceSetListeners to be statically registered
		// upon it (see
		// http://help.eclipse.org/ganymede/index.jsp?topic=/org.eclipse.emf.transaction.doc/references/overview/
		// listeners.html
		// for details)
		TransactionalEditingDomain.Registry.INSTANCE.add(editingDomainId, editingDomain);

		return editingDomain;
	}

	protected IResourceSaveIndicator createResourceSaveIndicator(TransactionalEditingDomain editingDomain) {
		return new ResourceSaveIndicator(editingDomain);
	}

	protected IExtendedTransactionalEditingDomainFactory getEditingDomainFactory(IMetaModelDescriptor mmDescriptor) {
		IExtendedTransactionalEditingDomainFactory factory = WorkspaceEditingDomainManager.INSTANCE.getEditingDomainFactory(mmDescriptor);
		if (factory == null) {
			throw new NullPointerException(NLS.bind(Messages.error_notFound_editingDomainFactory, mmDescriptor.getName()));
		}
		return factory;
	}

	/**
	 * Returns a unique identifier for the specified {@link TransactionalEditingDomain editingDomain}, considering the
	 * given <code>mmDescriptors</code>parameter. This returned string identifier is used as key in the map of
	 * {@linkplain TransactionalEditingDomain editingDomain}s owned by this {@linkplain IWorkspaceEditingDomainMapping
	 * mapping}.
	 * 
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} for which an identifier must be returned.
	 * @param mmDescriptors
	 *            A list of {@linkplain IMetaModelDescriptor}s the given editing domain should be associated to.
	 * @return An unique identifier for the specified {@link TransactionalEditingDomain editingDomain}.
	 */
	protected String getEditingDomainId(TransactionalEditingDomain editingDomain, Collection<IMetaModelDescriptor> mmDescriptors) {
		return getDefaultEditingDomainId(mmDescriptors);
	}

	@Override
	public TransactionalEditingDomain getEditingDomain(IFile file) {
		IMetaModelDescriptor descriptor = MetaModelDescriptorRegistry.INSTANCE.getEffectiveDescriptor(file);
		if (descriptor != null) {
			return getEditingDomain(file.getParent(), descriptor);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to the right protected method according to the type of the specified {@link IContainer container}.
	 */
	@Override
	public final List<TransactionalEditingDomain> getEditingDomains(IContainer container) {
		if (container instanceof IFolder) {
			return getEditingDomains((IFolder) container);
		} else if (container instanceof IProject) {
			return getEditingDomains((IProject) container);
		} else if (container instanceof IWorkspaceRoot) {
			return getEditingDomains((IWorkspaceRoot) container);
		}
		return Collections.emptyList();
	}

	protected List<TransactionalEditingDomain> getEditingDomains(IFolder folder) {
		if (folder != null) {
			final List<TransactionalEditingDomain> editingDomains = new ArrayList<TransactionalEditingDomain>();
			try {
				folder.accept(new AbstractResourceVisitor() {
					@Override
					public boolean doVisit(IResource resource) throws CoreException {
						if (resource instanceof IFile) {
							IFile file = (IFile) resource;
							if (file.isAccessible()) {
								TransactionalEditingDomain editingDomain = getEditingDomain(file);
								if (editingDomain != null && !editingDomains.contains(editingDomain)) {
									editingDomains.add(editingDomain);
								}
							}
						}
						return true;
					}
				});
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
			return editingDomains;
		}
		return Collections.emptyList();
	}

	protected List<TransactionalEditingDomain> getEditingDomains(IProject project) {
		List<TransactionalEditingDomain> editingDomains = new ArrayList<TransactionalEditingDomain>();
		for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(project)) {
			editingDomains.add(modelDescriptor.getEditingDomain());
		}
		return editingDomains;
	}

	protected List<TransactionalEditingDomain> getEditingDomains(IWorkspaceRoot workspaceRoot) {
		return getEditingDomains();
	}

	@Override
	public void addGlobalResourceSetListener(ResourceSetListener listener) {
		for (TransactionalEditingDomain editingDomain : getEditingDomains()) {
			editingDomain.addResourceSetListener(listener);
		}
		globalResourceSetListeners.add(listener);
	}

	@Override
	public void removeGlobalResourceSetListener(ResourceSetListener listener) {
		for (TransactionalEditingDomain editingDomain : getEditingDomains()) {
			editingDomain.removeResourceSetListener(listener);
		}
		globalResourceSetListeners.remove(listener);
	}

	@Override
	public void addGlobalOperationHistoryListener(IOperationHistoryListener listener) {
		for (TransactionalEditingDomain editingDomain : getEditingDomains()) {
			WorkspaceTransactionUtil.getOperationHistory(editingDomain).addOperationHistoryListener(listener);
		}
		globalOperationHistoryListeners.add(listener);
	}

	@Override
	public void removeGlobalOperationHistoryListener(IOperationHistoryListener listener) {
		for (TransactionalEditingDomain editingDomain : getEditingDomains()) {
			WorkspaceTransactionUtil.getOperationHistory(editingDomain).removeOperationHistoryListener(listener);
		}
		globalOperationHistoryListeners.remove(listener);
	}

	@Override
	public IResourceSaveIndicator getResourceSaveIndicator(TransactionalEditingDomain editingDomain) {
		return resourceSaveIndicators.get(editingDomain);
	}

	@Override
	public void postCreateEditingDomain(TransactionalEditingDomain editingDomain) {
		// Install a WorkspaceSynchronizer with an IResourceSaveIndicator delegate on newly created EditingDomain
		IResourceSaveIndicator resourceSaveIndicator = createResourceSaveIndicator(editingDomain);
		resourceSaveIndicators.put(editingDomain, resourceSaveIndicator);
		WorkspaceSynchronizer workspaceSynchronizer = new WorkspaceSynchronizer(editingDomain, resourceSaveIndicator);
		modelWorkspaceSynchronizers.put(editingDomain, workspaceSynchronizer);

		// Register global ResourceSetListeners and IOperationHistoryListeners on newly created EditingDomain
		for (Object listener : globalResourceSetListeners.getListeners()) {
			editingDomain.addResourceSetListener((ResourceSetListener) listener);
		}
		for (Object listener : globalOperationHistoryListeners.getListeners()) {
			WorkspaceTransactionUtil.getOperationHistory(editingDomain).addOperationHistoryListener((IOperationHistoryListener) listener);
		}
	}

	@Override
	public void preDisposeEditingDomain(TransactionalEditingDomain editingDomain) {
		// Discard WorkspaceSynchronizer and IResourceSaveIndicator associated with EditingDomain to be disposed
		resourceSaveIndicators.remove(editingDomain);
		WorkspaceSynchronizer workspaceSynchronizer = modelWorkspaceSynchronizers.remove(editingDomain);
		if (workspaceSynchronizer != null) {
			workspaceSynchronizer.dispose();
		}

		// Unregister global ResourceSetListeners and IOperationHistoryListeners on EditingDomain to be disposed
		for (Object listener : globalResourceSetListeners.getListeners()) {
			editingDomain.removeResourceSetListener((ResourceSetListener) listener);
		}
		for (Object listener : globalOperationHistoryListeners.getListeners()) {
			WorkspaceTransactionUtil.getOperationHistory(editingDomain).removeOperationHistoryListener((IOperationHistoryListener) listener);
		}
	}

	@Override
	public void dispose() {
		// Unregister this EditingDomainMapping as listener of all encountered EditingDomainFactories
		EditingDomainFactoryListenerRegistry.INSTANCE.removeListener(this);
	}
}
