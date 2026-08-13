/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *     itemis - Added #resetEditingDomainMapping() method for safely getting rid of editing domain mapping in integration test tearDown() methods
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.domain;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.domain.factory.ExtendedWorkspaceEditingDomainFactory;
import org.eclipse.sphinx.emf.workspace.domain.factory.IExtendedTransactionalEditingDomainFactory;
import org.eclipse.sphinx.emf.workspace.domain.mapping.DefaultWorkspaceEditingDomainMapping;
import org.eclipse.sphinx.emf.workspace.domain.mapping.IWorkspaceEditingDomainMapping;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * This class provides access to the essential workspace management implementations. The implementations for the active
 * {@link IWorkspaceEditingDomainMapping} and {@link org.eclipse.emf.ecore.resource.Resource.Factory Resource Factory}
 * are provided by the extension point <tt>org.eclipse.sphinx.emf.workspace.editingDomains</tt>. If not configured the
 * default implementations {@link DefaultWorkspaceEditingDomainMapping} and/or
 * {@link ExtendedWorkspaceEditingDomainFactory} will be used.
 */
public final class WorkspaceEditingDomainManager {

	private static final String EXTPOINT_EDITINGDOMAINS = "org.eclipse.sphinx.emf.workspace.editingDomains"; //$NON-NLS-1$
	private static final String NODE_MAPPING = "mapping"; //$NON-NLS-1$
	private static final String NODE_FACTORY = "factory"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String NODE_REQUIRED_FOR = "requiredFor"; //$NON-NLS-1$
	private static final String ATTR_MMDESC_ID_PATTERN = "metaModelDescriptorIdPattern"; //$NON-NLS-1$

	/** The singleton instance of the WorkspaceEditingDomainManager. */
	public static final WorkspaceEditingDomainManager INSTANCE = new WorkspaceEditingDomainManager();

	protected IWorkspaceEditingDomainMapping fEditingDomainMapping;
	protected Map<IMetaModelDescriptor, IExtendedTransactionalEditingDomainFactory> fEditingDomainFactories = new HashMap<IMetaModelDescriptor, IExtendedTransactionalEditingDomainFactory>();

	/**
	 * Private constructor for singleton pattern.
	 */
	private WorkspaceEditingDomainManager() {
		// Nothing to do
	}

	/**
	 * Returns the {@link IWorkspaceEditingDomainMapping} used to associate workspace resources with the
	 * {@link TransactionalEditingDomain}s they belong to. If a user-defined mapping has been contributed via the
	 * <tt>org.eclipse.sphinx.emf.workspace.editingDomains</tt> extension point it will be returned right here.
	 * Otherwise, it defaults to {@link DefaultWorkspaceEditingDomainMapping}.
	 * 
	 * @return The {@link IWorkspaceEditingDomainMapping} currently used by the platform.
	 * @see #setEditingDomainMapping
	 */
	public synchronized IWorkspaceEditingDomainMapping getEditingDomainMapping() {
		if (fEditingDomainMapping == null) {
			for (IConfigurationElement cfgElement : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTPOINT_EDITINGDOMAINS)) {
				if (NODE_MAPPING.equals(cfgElement.getName())) {
					Assert.isTrue(fEditingDomainMapping == null, Messages.error_multipleMappingsConfigured);
					try {
						fEditingDomainMapping = (IWorkspaceEditingDomainMapping) cfgElement.createExecutableExtension(ATTR_CLASS);
					} catch (CoreException ex) {
						String message = MessageFormat.format(Messages.error_createMapping, cfgElement.getAttribute(ATTR_CLASS));
						PlatformLogUtil.logAsWarning(Activator.getPlugin(), message);
					}
				}
			}

			// If not configured or instantiation failed then use default mapping
			if (fEditingDomainMapping == null) {
				fEditingDomainMapping = new DefaultWorkspaceEditingDomainMapping();
			}
		}
		return fEditingDomainMapping;
	}

	/**
	 * Enables clients to dynamically register a user-defined {@link IWorkspaceEditingDomainMapping}.
	 * <p>
	 * The user-defined {@link IWorkspaceEditingDomainMapping} overrides the mapping that might have been contributed
	 * via the <tt>org.eclipse.sphinx.emf.workspace.editingDomains</tt> extension point.
	 * </p>
	 * 
	 * @param editingDomainMapping
	 *            The {@link IWorkspaceEditingDomainMapping} to be used by the platform.
	 * @see #getEditingDomainMapping
	 */
	public void setEditingDomainMapping(IWorkspaceEditingDomainMapping editingDomainMapping) {
		if (fEditingDomainMapping != null) {
			fEditingDomainMapping.dispose();
		}
		fEditingDomainMapping = editingDomainMapping;
	}

	/**
	 * Enables clients to dispose current {@link IWorkspaceEditingDomainMapping} and get a new one setup upon next
	 * access.
	 * <p>
	 * The new {@link IWorkspaceEditingDomainMapping} will be either an instance of the
	 * {@link IWorkspaceEditingDomainMapping} that has been contributed via the
	 * <tt>org.eclipse.sphinx.emf.workspace.editingDomains</tt> extension point or the
	 * {@link DefaultWorkspaceEditingDomainMapping}.
	 * </p>
	 * 
	 * @see #getEditingDomainMapping
	 * @see #setEditingDomainMapping(IWorkspaceEditingDomainMapping)
	 */
	public void resetEditingDomainMapping() {
		if (fEditingDomainMapping != null) {
			fEditingDomainMapping.dispose();
		}
		fEditingDomainMapping = null;
	}

	/**
	 * Returns the EditingDomainFactory associated with a meta-model. EditingDomainFactories for meta-models can be
	 * contributed via the <tt>org.eclipse.sphinx.emf.workspace.editingDomains</tt> extension point
	 * 
	 * @param mmDescriptor
	 *            The meta-model for which the EditingDomainFactory is to be returned.
	 * @return The right editing domain factory.
	 */
	public synchronized IExtendedTransactionalEditingDomainFactory getEditingDomainFactory(IMetaModelDescriptor mmDescriptor) {
		if (fEditingDomainFactories.isEmpty()) {
			fEditingDomainFactories.put(MetaModelDescriptorRegistry.ANY_MM, new ExtendedWorkspaceEditingDomainFactory());
			readContributedEditingDomainFactories();
		}
		// FIXME Implement matching of EditingDomainFactories more intelligently by leveraging inheritance hierarchy of
		// MetaModelDescriptors
		IExtendedTransactionalEditingDomainFactory factory = fEditingDomainFactories.get(mmDescriptor);
		if (factory == null) {
			factory = fEditingDomainFactories.get(MetaModelDescriptorRegistry.ANY_MM);
		}
		return factory;
	}

	private void readContributedEditingDomainFactories() {
		for (IConfigurationElement cfgElement : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTPOINT_EDITINGDOMAINS)) {
			if (NODE_FACTORY.equals(cfgElement.getName())) {
				// Get instance of contributed editing domain factory
				IExtendedTransactionalEditingDomainFactory editingDomainFactory = getEditingDomainFactory(cfgElement);
				// Obtain meta-models this factory must be associated with
				for (IConfigurationElement requiredFor : cfgElement.getChildren(NODE_REQUIRED_FOR)) {
					String mmDescriptorIdPattern = requiredFor.getAttribute(ATTR_MMDESC_ID_PATTERN);
					// Retrieves meta-model descriptors from its identifier or from the regex
					Collection<IMetaModelDescriptor> descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(mmDescriptorIdPattern);
					for (IMetaModelDescriptor descriptor : descriptors) {
						// Register editing domain factory for the current meta-model descriptor
						fEditingDomainFactories.put(descriptor, editingDomainFactory);
					}
				}
			}
		}
	}

	/**
	 * @param configElement
	 *            The <em>factory</em> configuration element.
	 * @return The contributed editing domain factory.
	 */
	private IExtendedTransactionalEditingDomainFactory getEditingDomainFactory(IConfigurationElement configElement) {
		IExtendedTransactionalEditingDomainFactory editingDomainFactory = null;
		try {
			editingDomainFactory = (IExtendedTransactionalEditingDomainFactory) configElement.createExecutableExtension(ATTR_CLASS);
		} catch (CoreException ex) {
			String message = MessageFormat.format(Messages.error_createEditingDomainFactory, configElement.getAttribute(ATTR_CLASS));
			PlatformLogUtil.logAsWarning(Activator.getPlugin(), new WrappedException(message, ex));
		}
		// If not configured or instantiation failed then use default editing domain factory
		if (editingDomainFactory == null) {
			editingDomainFactory = new ExtendedWorkspaceEditingDomainFactory();
		}
		return editingDomainFactory;
	}
}
