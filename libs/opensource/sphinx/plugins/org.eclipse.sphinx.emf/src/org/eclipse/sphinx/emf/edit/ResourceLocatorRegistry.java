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
package org.eclipse.sphinx.emf.edit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class ResourceLocatorRegistry {

	/**
	 * Singleton instance.
	 */
	public static final ResourceLocatorRegistry INSTANCE = new ResourceLocatorRegistry();

	private static final String EXTPOINT_RESOURCE_LOCATOR = "resourceLocators"; //$NON-NLS-1$

	private static final String ELEMENT_LOCATOR = "locator"; //$NON-NLS-1$

	private static final String ATT_MM_PACKAGES_NSURI_PATTERN = "mmEPackagesNsURIPattern"; //$NON-NLS-1$ targetNamespace
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	/**
	 * The registered resource handler descriptors.
	 */
	private Map<String, ResourceLocator> contributedResourcesLocators = new HashMap<String, ResourceLocator>();

	private ResourceLocatorRegistry() {
		readRegistry();
	}

	public ResourceLocator getResourceLocator(String metaModelTargetNsURI) {
		Assert.isNotNull(metaModelTargetNsURI);
		for (String key : contributedResourcesLocators.keySet()) {
			if (metaModelTargetNsURI.matches(key)) {
				return contributedResourcesLocators.get(key);
			}
		}
		return null;
	}

	private void readRegistry() {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			if (registry != null) {
				String symbolicName = Activator.getPlugin().getSymbolicName();
				IConfigurationElement[] contributions = registry.getConfigurationElementsFor(symbolicName, EXTPOINT_RESOURCE_LOCATOR);
				for (IConfigurationElement contribution : contributions) {
					try {
						readContribution(contribution);
					} catch (Exception ex) {
						PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
					}
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	private void readContribution(IConfigurationElement element) {
		if (ELEMENT_LOCATOR.equals(element.getName())) {
			Object instance = null;
			String mmNsURIPattern = element.getAttribute(ATT_MM_PACKAGES_NSURI_PATTERN);
			if (contributedResourcesLocators.containsKey(mmNsURIPattern)) {
				String message = NLS.bind(Messages.warning_mmNsURIPatternNotUnique, mmNsURIPattern);
				PlatformLogUtil.logAsWarning(Activator.getDefault(), new RuntimeException(message));
			}
			try {
				Class<?> javaClass = Platform.getBundle(element.getDeclaringExtension().getContributor().getName()).loadClass(
						element.getAttribute(ATT_CLASS));
				Field field = javaClass.getField("INSTANCE"); //$NON-NLS-1$
				instance = field.get(null);
				contributedResourcesLocators.put(mmNsURIPattern, (ResourceLocator) instance);
			} catch (ClassNotFoundException e) {
				throw new WrappedException(e);
			} catch (IllegalAccessException e) {
				throw new WrappedException(e);
			} catch (NoSuchFieldException e) {
				try {
					instance = element.createExecutableExtension(ATT_CLASS);
					contributedResourcesLocators.put(mmNsURIPattern, (ResourceLocator) instance);
				} catch (CoreException ex) {
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
				PlatformLogUtil.logAsInfo(Activator.getPlugin(), e);
			}
		}
	}
}
