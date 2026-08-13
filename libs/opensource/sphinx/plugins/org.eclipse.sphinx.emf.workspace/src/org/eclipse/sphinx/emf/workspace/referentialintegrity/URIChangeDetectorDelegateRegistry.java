/**
 * <copyright>
 *
 * Copyright (c) 2008-2021 See4sys, Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     Siemens - [577073] URI change detector delegate extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.referentialintegrity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Registers all the contributed {@link IURIChangeDetectorDelegate}.
 */
public class URIChangeDetectorDelegateRegistry {
	private static final String EXTP_URI_CHANGE_DETECTOR_DELEGATE = "org.eclipse.sphinx.emf.workspace.uriChangeDetectorDelegates";//$NON-NLS-1$
	private static final String NODE_APPLICABLEFOR = "applicableFor";//$NON-NLS-1$
	private static final String NODE_DELEGATE = "delegate";//$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_OVERRIDE = "override"; //$NON-NLS-1$
	private static final String ATTR_RESOURCE_TYPE = "resourceType";//$NON-NLS-1$

	public static final URIChangeDetectorDelegateRegistry INSTANCE = new URIChangeDetectorDelegateRegistry();

	private final Map<Class<? extends Resource>, IURIChangeDetectorDelegate> fContributedURIChangeDetectorDelegates = new ConcurrentHashMap<Class<? extends Resource>, IURIChangeDetectorDelegate>();

	private URIChangeDetectorDelegateRegistry() {
		readContributedURIChangeDetectors();
	}

	/**
	 * Returns the contributed {@link IURIChangeDetectorDelegate URIChangeDetectorDelegate} for the provided
	 * {@link Resource resource} if such one exists.
	 *
	 * @param resource
	 *            the {@link Resource resource} to retrieve {@link IURIChangeDetectorDelegate URIChangeDetectorDelegate}
	 *            for.
	 * @return the {@link IURIChangeDetectorDelegate URIChangeDetectorDelegate} contributed for the provided resource's
	 *         {@link Class type}.
	 */
	public IURIChangeDetectorDelegate getDetectorDelegate(Resource resource) {
		if (resource != null) {
			// We retrieve delegate contributed for the given resource type
			Class<? extends Resource> resourceClass = resource.getClass();
			return getDetectorDelegate(resourceClass);
		}
		return null;
	}

	public void removeDelegate(IURIChangeDetectorDelegate delegate) {
		if (delegate != null) {
			for (Class<? extends Resource> resourceType : fContributedURIChangeDetectorDelegates.keySet()) {
				if (fContributedURIChangeDetectorDelegates.get(resourceType).equals(delegate)) {
					fContributedURIChangeDetectorDelegates.remove(resourceType);
				}
			}
		}

	}

	/**
	 * Returns the contributed {@link IURIChangeDetectorDelegate URIChangeDetectorDelegate} for the provided
	 * {@link Class resource Type} if such one exists.
	 *
	 * @param resourceType
	 *            the {@link Class resource Type} to retrieve {@link IURIChangeDetectorDelegate
	 *            URIChangeDetectorDelegate} for.
	 * @return the {@link IURIChangeDetectorDelegate URIChangeDetectorDelegate} contributed for the provided
	 *         {@link Class resource Type}.
	 */
	public IURIChangeDetectorDelegate getDetectorDelegate(Class<? extends Resource> resourceType) {
		IURIChangeDetectorDelegate uriChangeDetectorDelegate = fContributedURIChangeDetectorDelegates.get(resourceType);

		// If no such exists we try to match the resource type with one of its super class and return the
		// contributed delegate
		if (uriChangeDetectorDelegate == null) {
			uriChangeDetectorDelegate = getDetectorDelegateOfSuperType(resourceType);
			if (uriChangeDetectorDelegate != null) {
				// We register this delegate with the resource type to improve future request on such resource type
				fContributedURIChangeDetectorDelegates.put(resourceType, uriChangeDetectorDelegate);
			}
		}

		return uriChangeDetectorDelegate;
	}

	@SuppressWarnings("unchecked")
	// TODO refactorize see ReflectUtil , rename method findDetectorDelegate optimize call by including first call
	private IURIChangeDetectorDelegate getDetectorDelegateOfSuperType(Class<? extends Resource> resourceClass) {
		Class<? extends Resource> superclass = (Class<? extends Resource>) resourceClass.getSuperclass();
		if (Resource.class.equals(superclass) || ResourceImpl.class.equals(superclass)) {
			return fContributedURIChangeDetectorDelegates.get(ResourceImpl.class);
		} else {
			IURIChangeDetectorDelegate uriChangeDetectorDelegate = fContributedURIChangeDetectorDelegates.get(superclass);
			if (uriChangeDetectorDelegate == null) {
				return getDetectorDelegateOfSuperType(superclass);
			} else {
				return uriChangeDetectorDelegate;
			}
		}
	}

	private void readContributedURIChangeDetectors() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EXTP_URI_CHANGE_DETECTOR_DELEGATE);
		IExtension[] extensions = extensionPoint.getExtensions();
		Set<String> overriddenIds = new HashSet<String>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			overriddenIds.addAll(getOverriddenURIChangeDetectorsIds(configElements));
		}
		for (IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			readContributedURIChangeDetectors(configElements, overriddenIds);
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
	@SuppressWarnings("unchecked")
	private void readContributedURIChangeDetectors(IConfigurationElement[] configElements, Set<String> overriddenIds) {
		for (IConfigurationElement configElement : configElements) {
			try {
				String id = configElement.getAttribute(ATTR_ID);
				IURIChangeDetectorDelegate uriChangeDetectorDelegate = null;
				if (!overriddenIds.contains(id)) {
					uriChangeDetectorDelegate = (IURIChangeDetectorDelegate) configElement.createExecutableExtension(ATTR_CLASS);
					IConfigurationElement[] childrenConfigElements = configElement.getChildren();
					for (IConfigurationElement childConfigElement : childrenConfigElements) {
						if (NODE_APPLICABLEFOR.equals(childConfigElement.getName())) {
							String className = childConfigElement.getAttribute(ATTR_RESOURCE_TYPE);
							Class<?> clazz = Platform.getBundle(configElement.getContributor().getName()).loadClass(className);
							if (Resource.class.isAssignableFrom(clazz)) {
								Class<? extends Resource> resourceType = (Class<? extends Resource>) clazz;
								addDelegate(resourceType, uriChangeDetectorDelegate);
							}
						}
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}

	private Set<String> getOverriddenURIChangeDetectorsIds(IConfigurationElement[] configElements) {
		Assert.isNotNull(configElements);
		Set<String> overriddenIds = new HashSet<String>();
		for (IConfigurationElement configElement : configElements) {
			if (NODE_DELEGATE.equals(configElement.getName())) {
				String overriddenURIChangeDetectorDelegateId = configElement.getAttribute(ATTR_OVERRIDE);
				if (overriddenURIChangeDetectorDelegateId != null) {
					if (!overriddenIds.contains(overriddenURIChangeDetectorDelegateId)) {
						overriddenIds.add(overriddenURIChangeDetectorDelegateId);
					} else {
						PlatformLogUtil.logAsWarning(Activator.getPlugin(), new RuntimeException(
								NLS.bind(Messages.warning_multipleOverridesForSameURIChangeDetectorDelegate, overriddenURIChangeDetectorDelegateId)));
					}
				}
			}
		}
		return overriddenIds;
	}

	public void addDelegate(Class<? extends Resource> resourceType, IURIChangeDetectorDelegate uriChangeDetectorDelegate) {
		if (fContributedURIChangeDetectorDelegates.get(resourceType) == null) {
			fContributedURIChangeDetectorDelegates.put(resourceType, uriChangeDetectorDelegate);
		} else {
			PlatformLogUtil.logAsWarning(Activator.getPlugin(),
					new RuntimeException(NLS.bind(Messages.warning_multipleURIChangeDetectorDelegatesContributedForSameResourceType, resourceType)));
		}

	}
}
