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
 *     BMW Car IT - [373481] Performance optimizations for model loading
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class ResourceScopeProviderRegistry {

	/**
	 * The singleton instance of this registry.
	 */
	public static ResourceScopeProviderRegistry INSTANCE = new ResourceScopeProviderRegistry();

	private static final String EXTP_MODEL_SCOPE_PROVIDER = "org.eclipse.sphinx.emf.resourceScopeProviders";//$NON-NLS-1$
	private static final String NODE_APPLICABLEFOR = "applicableFor";//$NON-NLS-1$
	private static final String NODE_PROVIDER = "provider";//$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_OVERRIDE = "override"; //$NON-NLS-1$
	private static final String ATTR_META_MODEL_DESCRIPTOR_ID_PATTERN = "metaModelDescriptorIdPattern";//$NON-NLS-1$

	private Map<IMetaModelDescriptor, IResourceScopeProvider> fContributedResourceScopeProviders = new HashMap<IMetaModelDescriptor, IResourceScopeProvider>();

	private Map<IResourceScopeProvider, Collection<IMetaModelDescriptor>> fMetaModelDescriptorsForResourceScopeProviders = new HashMap<IResourceScopeProvider, Collection<IMetaModelDescriptor>>();

	private IResourceScopeProvider defaultResourceScopeProvider = null;

	/**
	 * Private constructor for the singleton pattern that prevents from instantiation by clients.
	 */
	private ResourceScopeProviderRegistry() {
		readContributedResourceScopeProviders();
		contributeDefaultResourceScopeProvider();

	}

	private void readContributedResourceScopeProviders() {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(EXTP_MODEL_SCOPE_PROVIDER).getExtensions();
		Set<String> overriddenIds = new HashSet<String>();
		// We retrieve overriden ids
		for (IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			overriddenIds.addAll(getOverriddenResourceScopeProviderIds(configElements));
		}
		// we contribute extension points excluding overriden ids
		for (IExtension extension : extensions) {
			IConfigurationElement[] configElements = extension.getConfigurationElements();
			readContributedResourceScopeProviders(configElements, overriddenIds);
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
	private void readContributedResourceScopeProviders(IConfigurationElement[] configElements, Set<String> overriddenIds) {
		for (IConfigurationElement configElement : configElements) {
			try {
				String id = configElement.getAttribute(ATTR_ID);
				IResourceScopeProvider resourceScopeProvider = null;
				if (!overriddenIds.contains(id)) {
					resourceScopeProvider = (IResourceScopeProvider) configElement.createExecutableExtension(ATTR_CLASS);
					IConfigurationElement[] childrenConfigElements = configElement.getChildren();
					for (IConfigurationElement childConfigElement : childrenConfigElements) {
						if (NODE_APPLICABLEFOR.equals(childConfigElement.getName())) {
							String metaModelDescriptorIdPattern = childConfigElement.getAttribute(ATTR_META_MODEL_DESCRIPTOR_ID_PATTERN);
							addResourceScopeProvider(metaModelDescriptorIdPattern, resourceScopeProvider);
						}
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}
	}

	private void contributeDefaultResourceScopeProvider() {
		List<IMetaModelDescriptor> orphanMMDescriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(MetaModelDescriptorRegistry.ANY_MM);
		Set<IMetaModelDescriptor> alreadyRegisteredMMdescriptors = fContributedResourceScopeProviders.keySet();
		orphanMMDescriptors.removeAll(alreadyRegisteredMMdescriptors);
		addResourceScopeProvider(orphanMMDescriptors, getDefaultResourceScopeProvider());
	}

	private Set<String> getOverriddenResourceScopeProviderIds(IConfigurationElement[] configElements) {
		Assert.isNotNull(configElements);
		Set<String> overriddenIds = new HashSet<String>();
		for (IConfigurationElement configElement : configElements) {
			if (NODE_PROVIDER.equals(configElement.getName())) {
				String overriddenResourceScopePolicyId = configElement.getAttribute(ATTR_OVERRIDE);
				if (overriddenResourceScopePolicyId != null) {
					if (!overriddenIds.contains(overriddenResourceScopePolicyId)) {
						overriddenIds.add(overriddenResourceScopePolicyId);
					} else {
						PlatformLogUtil.logAsWarning(
								Activator.getPlugin(),
								new RuntimeException(NLS.bind(Messages.warning_multipleOverridesForSameResourceScopeProvider,
										overriddenResourceScopePolicyId)));
					}
				}
			}
		}
		return overriddenIds;
	}

	/**
	 * Register a new {@link IResourceScopeProvider resource scope provider} for the provided
	 * {@link IMetaModelDescriptor meta model descriptor}.
	 * 
	 * @param mmDescIdPattern
	 *            The {@link IMetaModelDescriptor meta model descriptor provider} to register a
	 *            {@link IResourceScopeProvider resource scope provider} for.
	 * @param contributedProvider
	 *            The instance of {@link IResourceScopeProvider resource scope provider} to register.
	 */
	private void addResourceScopeProvider(String mmDescIdPattern, IResourceScopeProvider resourceScopeProvider) {
		Collection<IMetaModelDescriptor> matchingMMDescriptors = getMetaModelDescriptors(mmDescIdPattern);
		addResourceScopeProvider(matchingMMDescriptors, resourceScopeProvider);
	}

	private void addResourceScopeProvider(Collection<IMetaModelDescriptor> mmDescriptors, IResourceScopeProvider resourceScopeProvider) {
		for (IMetaModelDescriptor mmDescriptor : mmDescriptors) {
			if (fContributedResourceScopeProviders.get(mmDescriptor) == null) {
				fContributedResourceScopeProviders.put(mmDescriptor, resourceScopeProvider);

				Collection<IMetaModelDescriptor> mmDescriptorsOfResourceScopeProvider = fMetaModelDescriptorsForResourceScopeProviders
						.get(resourceScopeProvider);
				if (mmDescriptorsOfResourceScopeProvider == null) {
					mmDescriptorsOfResourceScopeProvider = new HashSet<IMetaModelDescriptor>();
					fMetaModelDescriptorsForResourceScopeProviders.put(resourceScopeProvider, mmDescriptorsOfResourceScopeProvider);
				}
				mmDescriptorsOfResourceScopeProvider.add(mmDescriptor);
			} else {
				PlatformLogUtil.logAsWarning(
						Activator.getPlugin(),
						new RuntimeException(NLS.bind(Messages.warning_multipleResourceScopeProvidersContributedForSameMetaModelDescriptor,
								mmDescriptor.getIdentifier())));
			}
		}
	}

	private Collection<IMetaModelDescriptor> getMetaModelDescriptors(String mmDescIdPattern) {
		HashSet<IMetaModelDescriptor> metaModelDescriptors = new HashSet<IMetaModelDescriptor>();
		if (".*".equals(mmDescIdPattern) || ".+".equals(mmDescIdPattern)) { //$NON-NLS-1$ //$NON-NLS-2$
			metaModelDescriptors.add(MetaModelDescriptorRegistry.ANY_MM);
		}
		metaModelDescriptors.addAll(MetaModelDescriptorRegistry.INSTANCE.getDescriptors(mmDescIdPattern));
		return Collections.unmodifiableCollection(metaModelDescriptors);
	}

	/**
	 * Retrieves all the {@link IMetaModelDescriptor meta model descriptors} registering the provided instance of
	 * {@link IResourceScopeProvider resource scope provider}.
	 * 
	 * @param resourceScopeProvider
	 *            The {@link IResourceScopeProvider resource scope provider} used to investigate.
	 * @return the {@link IMetaModelDescriptor meta model descriptors} the {@link IResourceScopeProvider resource scope
	 *         provider} is registered for.
	 */
	public Collection<IMetaModelDescriptor> getMetaModelDescriptorsFor(IResourceScopeProvider resourceScopeProvider) {
		Collection<IMetaModelDescriptor> metaModelDescriptorsForProvider = fMetaModelDescriptorsForResourceScopeProviders.get(resourceScopeProvider);
		if (metaModelDescriptorsForProvider != null) {
			return Collections.unmodifiableCollection(metaModelDescriptorsForProvider);
		}
		return Collections.emptySet();
	}

	/**
	 * Returns the default instance of {@link IResourceScopeProvider resource scope provider}.
	 * 
	 * @return IResourceScopeProvider the default instance of {@link IResourceScopeProvider resource scope provider}.
	 */
	public IResourceScopeProvider getDefaultResourceScopeProvider() {
		if (defaultResourceScopeProvider == null) {
			defaultResourceScopeProvider = new ProjectResourceScopeProvider();
		}
		return defaultResourceScopeProvider;
	}

	/**
	 * Tests if given {@link IFile file} does not belong to any {@link IResourceScope resource scope}. This can be the
	 * case if given {@link IFile file} is an obvious non-model file (e.g., a *.txt file), a model file based on a
	 * metamodel that is not described by a {@link IMetaModelDescriptor metamodel descriptor}, or a model file which is
	 * out of scope.
	 * <p>
	 * This method is guaranteed to have a very little performance overhead. It can be used by clients to optimize the
	 * retrieval of the {@link IResourceScope resource scope}s for large amounts of model and/or non-model {@link IFile
	 * file}s (e.g., after new {@link IFile file}s have been imported into the workspace). Clients therefore should call
	 * this method of {@link #hasApplicableFileExtension(IFile)} prior to actually retrieving the {@link IResourceScope
	 * resource scope} for a {@link IFile file} using {@link #getResourceScopeProvider(IMetaModelDescriptor)} and
	 * {@link IResourceScopeProvider#getScope(org.eclipse.core.resources.IResource)}. This makes sure that irrelevant
	 * {@link IFile file}s get filtered out in an efficient way and useless but potentially costly attempts of
	 * retrieving the {@link IResourceScope resource scope} for such {@link IFile file}s are avoided.
	 * </p>
	 * 
	 * @param file
	 *            The {@link IFile file} to be investigated.
	 * @return <code>true</code> if given {@link IFile file} does not belong to any {@link IResourceScope resource
	 *         scope}, <code>false</code> otherwise.
	 * @see #hasApplicableFileExtension(IFile)
	 */
	public boolean isNotInAnyScope(IFile file) {
		if (file != null) {
			/*
			 * Performance optimization: Create a separate HashSet for all contributed ResourceScopeProviders instead of
			 * directly iterating over fContributedResourceScopeProviders.values() in order to avoid repeated processing
			 * of same ResourceScopeProvider.
			 */
			Set<IResourceScopeProvider> allResourceScopeProviders = new HashSet<IResourceScopeProvider>(fContributedResourceScopeProviders.values());
			for (IResourceScopeProvider provider : allResourceScopeProviders) {
				if (provider.isApplicableTo(file)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Tests if the given {@link IFile file}'s extension corresponds to a file type which is subject to some
	 * {@link IResourceScopeProvider resource scope provider}. This is automatically the case if given {@link IFile
	 * file} is a model file based on a metamodel that is described by a {@link IMetaModelDescriptor metamodel
	 * descriptor}. The applicable {@link IResourceScopeProvider resource scope provider} for such model files is either
	 * one that has been specifically contributed to the underlying {@link IMetaModelDescriptor metamodel descriptor}
	 * via the org.eclipse.sphinx.emf.resourceScopeProviders extension point or a
	 * {@link #getDefaultResourceScopeProvider() default resource scope provider}.
	 * <p>
	 * This method is guaranteed to have a very little performance overhead. It can be used by clients to optimize the
	 * retrieval of the {@link IResourceScope resource scope}s for large amounts of model and/or non-model {@link IFile
	 * file}s (e.g., after new {@link IFile file}s have been imported into the workspace). Clients therefore should call
	 * this method or {@link #isNotInAnyScope(IFile)} prior to actually retrieving the {@link IResourceScope resource
	 * scope} for a {@link IFile file} using {@link #getResourceScopeProvider(IMetaModelDescriptor)} and
	 * {@link IResourceScopeProvider#getScope(org.eclipse.core.resources.IResource)}. This makes sure that irrelevant
	 * {@link IFile file}s get filtered out in an efficient way and useless but potentially costly attempts of
	 * retrieving the {@link IResourceScope resource scope} for such {@link IFile file}s are avoided.
	 * </p>
	 * 
	 * @param file
	 *            The {@link IFile file} to be investigated.
	 * @return <code>true</code> if some {@link IResourceScopeProvider resource scope provider} is applicable to
	 *         {@link IFile file}s having the extension of the given {@link IFile file}, <code>false</code> otherwise.
	 * @see #isNotInAnyScope(IFile)
	 */
	public boolean hasApplicableFileExtension(IFile file) {
		if (file != null) {
			for (IResourceScopeProvider provider : fMetaModelDescriptorsForResourceScopeProviders.keySet()) {
				if (provider.hasApplicableFileExtension(file)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the {@link IResourceScopeProvider resource scope provider} registered for the provided
	 * {@link IMetaModelDescriptor meta model descriptor}.
	 * 
	 * @param mmDescriptor
	 *            The {@link IMetaModelDescriptor meta model descriptor} use to investigate.
	 * @return the {@link IResourceScopeProvider resource scope provider} registered for the provided
	 *         {@link IMetaModelDescriptor meta model descriptor} or <code>null</code> if none has been registered.
	 */
	public IResourceScopeProvider getResourceScopeProvider(IMetaModelDescriptor mmDescriptor) {
		if (mmDescriptor != null) {
			return fContributedResourceScopeProviders.get(mmDescriptor);
		}
		return null;
	}
}
