/**
 * <copyright>
 *
 * Copyright (c) 2010-2017 Continental Engineering Services, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Continental Engineering Services - Initial API and implementation
 *     itemis - Moved from Artop to Sphinx, adapted method and variable naming to Sphinx conventions
 *     itemis - [458921] Newly introduced registries for metamodel serives, check validators and workflow contributors are not standalone-safe
 *     itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Allows lazy loading of plug-in classes. Must be used when needed to initialize classes contributed through extension
 * points.
 */
public class ExtensionClassDescriptor<T> {

	private final static String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_OVERRIDE = "override"; //$NON-NLS-1$

	private String contributorPluginId;
	private String id;
	private String className = null;
	private String override;

	private T instance;

	/**
	 * Create an <code>ExtensionClassDescriptor</code> wrapped around a configuration element
	 *
	 * @param configurationElement
	 *            the configuration element
	 */
	public ExtensionClassDescriptor(IConfigurationElement configurationElement) {
		Assert.isNotNull(configurationElement);

		contributorPluginId = configurationElement.getContributor().getName();
		Assert.isNotNull(contributorPluginId);

		className = configurationElement.getAttribute(ATTR_CLASS);
		Assert.isNotNull(className);

		id = configurationElement.getAttribute(ATTR_ID);

		override = configurationElement.getAttribute(ATTR_OVERRIDE);
	}

	public String getContributorPluginId() {
		return contributorPluginId;
	}

	public String getId() {
		return id;
	}

	public String getClassName() {
		return className != null ? className : null;
	}

	public String getOverride() {
		return override;
	}

	public Class<T> getType() throws ClassNotFoundException {
		Bundle contributorBundle = Platform.getBundle(contributorPluginId);
		if (contributorBundle == null) {
			throw new IllegalStateException("Cannot locate contributor plug-in '" + contributorPluginId + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) contributorBundle.loadClass(className);
		return type;
	}

	/**
	 * Returns a cached instance of the class creating one if necessary
	 *
	 * @return The class instance
	 * @throws Exception
	 */
	public T getInstance() throws Exception {
		if (instance == null) {
			synchronized (this) {
				instance = newInstance();
			}
		}
		return instance;
	}

	/**
	 * Creates a new extension class instance.
	 *
	 * @return
	 */
	public T newInstance() throws Exception {
		Class<T> type = getType();
		return type.newInstance();
	}

	/**
	 * Determines if this service registration dominates another service registration. A service registration dominates
	 * another registration if it overrides the class of the other service registration.
	 *
	 * @param otherService
	 *            the other service registration for which the domination relation is to be determined
	 * @return <code>true</code> if the other service registration is dominated, otherwise <code>false</code>
	 */
	public boolean overrides(ExtensionClassDescriptor<?> otherDescriptor) {
		if (otherDescriptor == null) {
			return true;
		}
		return otherDescriptor.getId().equals(getOverride());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + contributorPluginId.hashCode();
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + className.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		ExtensionClassDescriptor other = (ExtensionClassDescriptor) obj;
		if (!contributorPluginId.equals(other.contributorPluginId)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (!className.equals(other.className)) {
			return false;
		}
		return true;
	}
}
