/**
 * <copyright>
 *
 * Copyright (c) 2008-2021 See4sys, BMW Car IT, Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Avoid usage of Object.finalize
 *     Siemens - [577073] URI change detector delegate extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.referentialintegrity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 *
 */
public class URIChangeListenerRegistry {
	public static URIChangeListenerRegistry INSTANCE = new URIChangeListenerRegistry();

	private Set<IURIChangeListener> fURIChangeListeners = ConcurrentHashMap.newKeySet();

	private static final String EXTP_URI_CHANGE_LISTENERS = "org.eclipse.sphinx.emf.workspace.uriChangeListeners"; //$NON-NLS-1$

	private static final String NODE_LISTENER = "listener"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_OVERRIDE = "override"; //$NON-NLS-1$

	/**
	 * Private default constructor for singleton pattern
	 */
	private URIChangeListenerRegistry() {
		readContributedURIChangeListeners();
	}

	/**
	 * @return
	 */
	public Set<IURIChangeListener> getListeners() {
		return fURIChangeListeners;
	}

	private void readContributedURIChangeListeners() {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(EXTP_URI_CHANGE_LISTENERS).getExtensions();
		Set<String> overriddenIds = new HashSet<String>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			overriddenIds.addAll(getOverriddenURIChangeListenerIds(configElements));
		}
		for (IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			readContributedURIChangeListeners(configElements, overriddenIds);
		}
	}

	/**
	 * Reads contributions to <em>Meta-Model Descriptor</em> extension point.
	 * <p>
	 * <table>
	 * <tr valign=top>
	 * <td><b>Note</b>&nbsp;&nbsp;</td>
	 * <td>It is recommended to call this method inside a block <tt><b>synchronized</b></tt> on the encapsulated
	 * <code>fMetaModelDescriptors</code> field in order to avoid inconsistencies in registered meta-model
	 * {@linkplain IMetaModelDescriptor descriptor}s in case of concurrent read/adds.</td>
	 * </tr>
	 * </table>
	 */
	private void readContributedURIChangeListeners(IConfigurationElement[] configElements, Set<String> overriddenIds) {
		for (IConfigurationElement configElement : configElements) {
			try {
				String id = configElement.getAttribute(ATTR_ID);
				if (!overriddenIds.contains(id)) {
					IURIChangeListener listener = (IURIChangeListener) configElement.createExecutableExtension(ATTR_CLASS);
					addListener(listener);
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}

	/**
	 * @param listener
	 */
	public void addListener(IURIChangeListener listener) {
		if (listener != null) {
			fURIChangeListeners.add(listener);
		}
	}

	/**
	 * @param listener
	 */
	public void removeListener(IURIChangeListener listener) {
		if (listener != null) {
			fURIChangeListeners.remove(listener);
		}
	}

	private Set<String> getOverriddenURIChangeListenerIds(IConfigurationElement[] configElements) {
		Assert.isNotNull(configElements);
		Set<String> overriddenIds = new HashSet<String>();
		for (IConfigurationElement configElement : configElements) {
			if (NODE_LISTENER.equals(configElement.getName())) {
				String overriddenURIChangeDetectorDelegateId = configElement.getAttribute(ATTR_OVERRIDE);
				if (overriddenURIChangeDetectorDelegateId != null) {
					if (!overriddenIds.contains(overriddenURIChangeDetectorDelegateId)) {
						overriddenIds.add(overriddenURIChangeDetectorDelegateId);
					} else {
						PlatformLogUtil.logAsWarning(Activator.getPlugin(), new RuntimeException(
								NLS.bind(Messages.warning_multipleOverridesForSameURIChangeListener, overriddenURIChangeDetectorDelegateId)));
					}
				}
			}
		}
		return overriddenIds;
	}

}
