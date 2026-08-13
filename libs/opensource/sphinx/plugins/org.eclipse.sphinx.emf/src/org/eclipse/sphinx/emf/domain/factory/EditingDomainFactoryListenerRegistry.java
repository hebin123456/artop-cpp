/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Lazy extension initialization
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.domain.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.osgi.framework.Bundle;

/**
 * The singleton instance that is responsible for managing {@linkplain ITransactionalEditingDomainFactoryListener
 * editing domain factory listener}s that have been contributed to the platform <em>via</em> extension point
 * <tt>org.eclipse.sphinx.emf.editingDomainFactoryListeners</tt>.
 * <p>
 * This registry mainly provides one method allowing to retrieve {@linkplain ITransactionalEditingDomainFactoryListener
 * listener}s corresponding to one specified {@linkplain IMetaModelDescriptor meta-model descriptor}.
 * 
 * @since 0.7.0
 */
public class EditingDomainFactoryListenerRegistry {

	/**
	 * The singleton instance of this registry.
	 */
	public static final EditingDomainFactoryListenerRegistry INSTANCE = new EditingDomainFactoryListenerRegistry();

	/**
	 * Identifier of Editing Domain Factory Listeners extension point.
	 */
	private static final String EXTP_EDITING_DOMAIN_FACTORY_LISTENERS = "org.eclipse.sphinx.emf.editingDomainFactoryListeners"; //$NON-NLS-1$

	private static final String NODE_LISTENER = "listener"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_OVERRIDE = "override"; //$NON-NLS-1$

	private static final String NODE_APPLICABLE_FOR = "applicableFor"; //$NON-NLS-1$
	private static final String ATTR_MMDESC_ID_PATTERN = "metaModelDescriptorIdPattern"; //$NON-NLS-1$

	/**
	 * Flag to track lazy initialization.
	 */
	private boolean isInitialized = false;

	/**
	 * The registered {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s or
	 * {@linkplain ListenerDescriptor listener descriptor}s
	 */
	private Map<IMetaModelDescriptor, Map<String, Object>> fEditingDomainFactoryListeners = new HashMap<IMetaModelDescriptor, Map<String, Object>>();

	/**
	 * The {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s which are overridden
	 * by other ones.
	 */
	private Map<String, Set<IMetaModelDescriptor>> fOutstandingOverrides = new HashMap<String, Set<IMetaModelDescriptor>>();

	/**
	 * The descriptor that is used to store the listener information prior to instantiation
	 */
	private class ListenerDescriptor {

		private String className;
		private String contributorPluginId;

		/**
		 * @param className
		 * @param contributorPluginId
		 */
		private ListenerDescriptor(String className, String contributorPluginId) {
			this.className = className;
			this.contributorPluginId = contributorPluginId;
		}

		/**
		 * Construct and return the listener. This method might trigger plugin loading.
		 * 
		 * @return the listener
		 * @throws ClassNotFoundException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 */
		private ITransactionalEditingDomainFactoryListener getListener() throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
			Bundle bundle = Platform.getBundle(contributorPluginId);
			Class<?> factoryClz = bundle.loadClass(className);
			return (ITransactionalEditingDomainFactoryListener) factoryClz.newInstance();
		}
	}

	/**
	 * Private constructor for singleton pattern.
	 */
	private EditingDomainFactoryListenerRegistry() {

	}

	/**
	 * Reads contributions to the <em>Editing Domain Factory Listeners</em> extension point.
	 */
	private void readContributedEditingDomainFactoryListeners() {
		for (IConfigurationElement cfgElement : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTP_EDITING_DOMAIN_FACTORY_LISTENERS)) {
			try {
				if (NODE_LISTENER.equals(cfgElement.getName())) {
					String listenerId = cfgElement.getAttribute(ATTR_ID);
					String overriddenListenerId = cfgElement.getAttribute(ATTR_OVERRIDE);

					// Create a descriptor for the listener
					ListenerDescriptor descriptor = new ListenerDescriptor(cfgElement.getAttribute(ATTR_CLASS), cfgElement.getContributor().getName());

					// Register it upon meta-model descriptor is must be available for
					for (IConfigurationElement applicableFor : cfgElement.getChildren(NODE_APPLICABLE_FOR)) {
						String mmDescIdPattern = applicableFor.getAttribute(ATTR_MMDESC_ID_PATTERN);
						addListenerDescriptor(mmDescIdPattern, listenerId, descriptor, overriddenListenerId);
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}

		// Remove overridden editing domain factory listeners that couldn't be removed earlier
		for (String overriddenListenerId : fOutstandingOverrides.keySet()) {
			for (IMetaModelDescriptor mmDescriptor : fOutstandingOverrides.get(overriddenListenerId)) {
				Map<String, Object> listenersForMetaModel = fEditingDomainFactoryListeners.get(mmDescriptor);
				if (listenersForMetaModel != null) {
					listenersForMetaModel.remove(overriddenListenerId);
				}
			}
		}
		fOutstandingOverrides.clear();
	}

	/**
	 * Adds the specified {@linkplain ListenerDescriptor listener descriptor} to the map of registered listeners and
	 * descriptors that have been contributed to the platform through
	 * <tt>org.eclipse.sphinx.emf.editingDomainFactoryListeners</tt> extension point.
	 * 
	 * @param mmDescIdPattern
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} identifier pattern allowing to retrieve
	 *            the meta-model descriptors the specified {@linkplain ITransactionalEditingDomainFactoryListener
	 *            factory listener} must be associated to. Should not be <code>null</code>.
	 * @param listenerId
	 *            The identifier of the {@linkplain ITransactionalEditingDomainFactoryListener transactional editing
	 *            domain factory listener} to be registered. Should not be null.
	 * @param descriptor
	 *            The {@linkplain ListenerDescriptor listener descriptor} that must be registered on the
	 *            {@linkplain EditingDomainFactoryListenerRegistry factory listener registry} for the
	 *            {@linkplain IMetaModelDescriptor meta-model descriptor}s that match the specified
	 *            <code>mmDescIdPattern</code>. Should not be <code>null</code>.
	 * @param overriddenListenerId
	 *            The identifier of some other {@linkplain ITransactionalEditingDomainFactoryListener transactional
	 *            editing domain factory listener} to be overridden or <code>null</code> when no overriding is required.
	 */
	private void addListenerDescriptor(String mmDescIdPattern, String listenerId, ListenerDescriptor descriptor, String overriddenListenerId) {
		if (".*".equals(mmDescIdPattern) || ".+".equals(mmDescIdPattern)) { //$NON-NLS-1$ //$NON-NLS-2$
			addListenerDescriptor(MetaModelDescriptorRegistry.ANY_MM, listenerId, descriptor, overriddenListenerId);
			return;
		}

		for (IMetaModelDescriptor mmDescriptor : MetaModelDescriptorRegistry.INSTANCE.getDescriptors(mmDescIdPattern)) {
			addListenerDescriptor(mmDescriptor, listenerId, descriptor, overriddenListenerId);
		}
	}

	/**
	 * Adds the specified {@linkplain ListenerDescriptor listener descriptor} to the map of registered listeners and
	 * descriptors that have been contributed to the platform through
	 * <tt>org.eclipse.sphinx.emf.editingDomainFactoryListeners</tt> extension point.
	 * 
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} the specified
	 *            {@linkplain ITransactionalEditingDomainFactoryListener factory listener} must be associated to. Should
	 *            not be <code>null</code>.
	 * @param listenerId
	 *            The identifier of the {@linkplain ITransactionalEditingDomainFactoryListener transactional editing
	 *            domain factory listener} to be registered. Shall not be null.
	 * @param descriptor
	 *            The {@linkplain ListenerDescriptor listener descriptor} that must be registered on the
	 *            {@linkplain EditingDomainFactoryListenerRegistry factory listener registry} for the
	 *            {@linkplain IMetaModelDescriptor meta-model descriptor}s that match the specified
	 *            <code>mmDescIdPattern</code>. Should not be <code>null</code>.
	 * @param overriddenListenerId
	 *            The identifier of some other {@linkplain ITransactionalEditingDomainFactoryListener transactional
	 *            editing domain factory listener} to be overridden or <code>null</code> when no overriding is required.
	 */
	private void addListenerDescriptor(IMetaModelDescriptor mmDescriptor, String listenerId, ListenerDescriptor descriptor,
			String overriddenListenerId) {
		if (mmDescriptor != null && descriptor != null) {
			// Register editing domain factory listener for given meta-model descriptor
			Map<String, Object> listenersForMetaModel = fEditingDomainFactoryListeners.get(mmDescriptor);
			if (listenersForMetaModel == null) {
				listenersForMetaModel = new HashMap<String, Object>();
				fEditingDomainFactoryListeners.put(mmDescriptor, listenersForMetaModel);
			}
			listenersForMetaModel.put(listenerId, descriptor);

			// Manage overridden editing domain factory listener, if any
			if (overriddenListenerId != null) {
				// Remove overridden editing domain factory listener; remember it for being removed later if it is not
				// present yet
				if (listenersForMetaModel.remove(overriddenListenerId) == null) {
					Set<IMetaModelDescriptor> overridesForListener = fOutstandingOverrides.get(overriddenListenerId);
					if (overridesForListener == null) {
						overridesForListener = new HashSet<IMetaModelDescriptor>();
						fOutstandingOverrides.put(overriddenListenerId, overridesForListener);
					}
					overridesForListener.add(mmDescriptor);
				}
			}
		}
	}

	/**
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} for which matching
	 *            {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s must be
	 *            returned.
	 * @return The {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s that have
	 *         been contributed to the platform <em>via</em> <tt>editingDomainFactoryListeners</tt> extension point and
	 *         that match the specified {@linkplain IMetaModelDescriptor meta-model descriptor}.
	 */
	public Collection<ITransactionalEditingDomainFactoryListener> getListeners(IMetaModelDescriptor mmDescriptor) {
		synchronized (this) {
			if (isInitialized == false) {
				readContributedEditingDomainFactoryListeners();
				isInitialized = true;
			}
		}

		// Retrieve editing domain factory listeners registered upon specified meta-model descriptor or one of its super
		// descriptors
		Set<ITransactionalEditingDomainFactoryListener> listeners = new HashSet<ITransactionalEditingDomainFactoryListener>();
		for (IMetaModelDescriptor registeredMMDescriptor : fEditingDomainFactoryListeners.keySet()) {
			if (registeredMMDescriptor.getClass().isInstance(mmDescriptor)) {
				Map<String, Object> listenersForMetaModel = fEditingDomainFactoryListeners.get(registeredMMDescriptor);
				listeners.addAll(getListeners(listenersForMetaModel));
			}
		}

		// Retrieve editing domain factory listeners registered upon static any meta-model descriptor
		Map<String, Object> listenersForAnyMetaModel = fEditingDomainFactoryListeners.get(MetaModelDescriptorRegistry.ANY_MM);
		if (listenersForAnyMetaModel != null) {
			listeners.addAll(getListeners(listenersForAnyMetaModel));
		}
		return listeners;
	}

	/**
	 * Returns a set of {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listeners} that
	 * are contained or described by the entries in given <code>listenersOrDescriptors</code> map.
	 * <p>
	 * Depending on the type of the listener object contained by each map entry one of the following actions is taken:
	 * <ul>
	 * <li>Instances of {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s are
	 * directly added to the resulting listener set.</li>
	 * <li>Instances of {@linkplain ListenerDescriptor editing domain factory listener descriptor}s are used to create
	 * the actual {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s and add those
	 * to the resulting listener set. In addition the formers are replaced by the latters in given
	 * <code>listenersOrDescriptors</code> map.</li>
	 * <li>Instances of anything else are logged as errors and removed from given <code>listenersOrDescriptors</code>
	 * map.
	 * </p>
	 * 
	 * @param listenersOrDescriptors
	 *            The map with {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s
	 *            or {@linkplain ListenerDescriptor editing domain factory listener descriptor}s and their respective
	 *            identifiers to be processed.
	 * @return A set of {@linkplain ITransactionalEditingDomainFactoryListener editing domain factory listener}s that
	 *         are contained or described by the entries in given <code>listenersOrDescriptors </code> map.
	 */
	private Set<ITransactionalEditingDomainFactoryListener> getListeners(Map<String, Object> listenersOrDescriptors) {
		Set<ITransactionalEditingDomainFactoryListener> listeners = new HashSet<ITransactionalEditingDomainFactoryListener>();
		for (Iterator<String> iter = listenersOrDescriptors.keySet().iterator(); iter.hasNext();) {
			String listenerId = iter.next();
			Object listenerObject = listenersOrDescriptors.get(listenerId);
			if (listenerObject instanceof ITransactionalEditingDomainFactoryListener) {
				// Add existing listener
				listeners.add((ITransactionalEditingDomainFactoryListener) listenerObject);
			} else if (listenerObject instanceof ListenerDescriptor) {
				// Create the listener
				try {
					ITransactionalEditingDomainFactoryListener listener = ((ListenerDescriptor) listenerObject).getListener();
					listenersOrDescriptors.put(listenerId, listener);
					listeners.add(listener);
				} catch (Exception ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);

					// Discard bad descriptor so as to avoid repeated runs into same problem
					iter.remove();
				}
			} else {
				// Should not happen...
				PlatformLogUtil.logAsError(Activator.getPlugin(),
						NLS.bind(Messages.error_invalidEditingDomainFactoryListenerObject, listenerObject.getClass().getName()));

				// Discard bad descriptor so as to avoid repeated runs into same problem
				iter.remove();
			}
		}
		return listeners;
	}

	/**
	 * Adds the specified {@linkplain ITransactionalEditingDomainFactoryListener factory listener} to the map of
	 * registered listeners that have been contributed to the platform through
	 * <tt>org.eclipse.sphinx.emf.editingDomainFactoryListeners</tt> extension point.
	 * 
	 * @param mmDescIdPattern
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} identifier pattern allowing to retrieve
	 *            the meta-model descriptors the specified {@linkplain ITransactionalEditingDomainFactoryListener
	 *            factory listener} must be associated to. Should not be <code>null</code>.
	 * @param listenerId
	 *            The identifier of the {@linkplain ITransactionalEditingDomainFactoryListener transactional editing
	 *            domain factory listener} to be registered. If <code>null</code> the
	 *            {@linkplain ITransactionalEditingDomainFactoryListener transactional editing domain factory listener}
	 *            's identifier will be automatically computed based the result of
	 *            {@linkplain ITransactionalEditingDomainFactoryListener#toString() toString()}.
	 * @param listener
	 *            The {@linkplain ITransactionalEditingDomainFactoryListener transactional editing domain factory
	 *            listener} that must be registered on the {@linkplain EditingDomainFactoryListenerRegistry factory
	 *            listener registry} for the {@linkplain IMetaModelDescriptor meta-model descriptor}s that match the
	 *            specified <code>mmDescIdPattern</code>. Should not be <code>null</code>.
	 * @param overriddenListenerId
	 *            The identifier of some other {@linkplain ITransactionalEditingDomainFactoryListener transactional
	 *            editing domain factory listener} to be overridden or <code>null</code> when no overriding is required.
	 */
	public void addListener(String mmDescIdPattern, String listenerId, ITransactionalEditingDomainFactoryListener listener,
			String overriddenListenerId) {
		/*
		 * !! Important Note !! If given meta-model descriptor id pattern matches anything register specified editing
		 * domain factory listener only with static any meta-model descriptor rather than with all meta-model matching
		 * descriptors. This makes sure that the listener will not only be invoked for all static (contributed)
		 * meta-model descriptors but also for all dynamic meta-model descriptors including those which are created
		 * subsequently, i.e. AFTER the installation of specified listener.
		 */
		if (".*".equals(mmDescIdPattern) || ".+".equals(mmDescIdPattern)) { //$NON-NLS-1$ //$NON-NLS-2$
			addListener(MetaModelDescriptorRegistry.ANY_MM, listenerId, listener, overriddenListenerId);
			return;
		}

		for (IMetaModelDescriptor mmDescriptor : MetaModelDescriptorRegistry.INSTANCE.getDescriptors(mmDescIdPattern)) {
			addListener(mmDescriptor, listenerId, listener, overriddenListenerId);
		}
	}

	/**
	 * Adds the specified {@linkplain ITransactionalEditingDomainFactoryListener factory listener} to the map of
	 * registered listeners that have been contributed to the platform through
	 * <tt>org.eclipse.sphinx.emf.editingDomainFactoryListeners</tt> extension point.
	 * 
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} the specified
	 *            {@linkplain ITransactionalEditingDomainFactoryListener factory listener} must be associated to. Should
	 *            not be <code>null</code>.
	 * @param listenerId
	 *            The identifier of the {@linkplain ITransactionalEditingDomainFactoryListener transactional editing
	 *            domain factory listener} to be registered. If <code>null</code> the
	 *            {@linkplain ITransactionalEditingDomainFactoryListener transactional editing domain factory listener}
	 *            's identifier will be automatically computed based the result of
	 *            {@linkplain ITransactionalEditingDomainFactoryListener#toString() toString()}.
	 * @param listener
	 *            The {@linkplain ITransactionalEditingDomainFactoryListener transactional editing domain factory
	 *            listener} that must be registered on the {@linkplain EditingDomainFactoryListenerRegistry factory
	 *            listener registry} for the {@linkplain IMetaModelDescriptor meta-model descriptor}s that match the
	 *            specified <code>mmDescIdPattern</code>. Should not be <code>null</code>.
	 * @param overriddenListenerId
	 *            The identifier of some other {@linkplain ITransactionalEditingDomainFactoryListener transactional
	 *            editing domain factory listener} to be overridden or <code>null</code> when no overriding is required.
	 */
	public void addListener(IMetaModelDescriptor mmDescriptor, String listenerId, ITransactionalEditingDomainFactoryListener listener,
			String overriddenListenerId) {
		if (mmDescriptor != null && listener != null) {
			// Register editing domain factory listener for given meta-model descriptor
			Map<String, Object> listenersForMetaModel = fEditingDomainFactoryListeners.get(mmDescriptor);
			if (listenersForMetaModel == null) {
				listenersForMetaModel = new HashMap<String, Object>();
				fEditingDomainFactoryListeners.put(mmDescriptor, listenersForMetaModel);
			}
			listenersForMetaModel.put(listenerId != null ? listenerId : listener.toString(), listener);

			// Manage overridden editing domain factory listener, if any
			if (overriddenListenerId != null) {
				// Remove overridden editing domain factory listener; remember it for being removed later if it is not
				// present yet
				if (listenersForMetaModel.remove(overriddenListenerId) == null) {
					Set<IMetaModelDescriptor> overridesForListener = fOutstandingOverrides.get(overriddenListenerId);
					if (overridesForListener == null) {
						overridesForListener = new HashSet<IMetaModelDescriptor>();
						fOutstandingOverrides.put(overriddenListenerId, overridesForListener);
					}
					overridesForListener.add(mmDescriptor);
				}
			}
		}
	}

	/**
	 * Removes the specified {@linkplain ITransactionalEditingDomainFactoryListener factory listener} from that
	 * registry.
	 * 
	 * @param listener
	 *            The {@linkplain ITransactionalEditingDomainFactoryListener factory listener} to remove from the list
	 *            of registered ones.
	 */
	// FIXME Should we think about a solution allowing ONLY the removal of dynamically registered listeners?
	public void removeListener(ITransactionalEditingDomainFactoryListener listener) {
		if (listener != null) {
			Iterator<Map<String, Object>> iter1;
			for (iter1 = fEditingDomainFactoryListeners.values().iterator(); iter1.hasNext();) {
				Map<String, Object> listenersForMetaModel = iter1.next();
				Iterator<Object> iter2;
				for (iter2 = listenersForMetaModel.values().iterator(); iter2.hasNext();) {
					Object listenerForMetaModel = iter2.next();
					if (listener.equals(listenerForMetaModel)) {
						iter2.remove();
					}
				}
				if (listenersForMetaModel.isEmpty()) {
					iter1.remove();
				}
			}
		}
	}
}
