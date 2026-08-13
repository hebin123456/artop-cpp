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
package org.eclipse.sphinx.emf.ui.properties.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.messages.EMFMessages;
import org.eclipse.sphinx.emf.ui.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * 
 */
public final class PropertySourceFilterRegistry {

	public static final PropertySourceFilterRegistry INSTANCE = new PropertySourceFilterRegistry();

	private static final String EXTP_PROPERTY_SOURCE_FILTERS = "propertySourceFilters"; //$NON-NLS-1$
	private static final String NODE_FILTER = "filter"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private List<IPropertySourceFilter> propertySourceFilterList = new ArrayList<IPropertySourceFilter>();
	private Map<Class<?>, IPropertySourceFilter> propertySourceFilterMap = new HashMap<Class<?>, IPropertySourceFilter>();

	// Prevent from instantiation by clients
	private PropertySourceFilterRegistry() {
		readContributedPropertySourceFilters();
	}

	public IPropertySourceFilter getPropertySourceFilter(Object owner) {
		if (owner != null) {
			Class<?> ownerType = owner.getClass();
			if (propertySourceFilterMap.containsKey(ownerType)) {
				return propertySourceFilterMap.get(ownerType);
			}
			IPropertySourceFilter filter = findPropertySourceFilter(owner);
			if (filter != null) {
				propertySourceFilterMap.put(ownerType, filter);
				return filter;
			}
		}
		return null;
	}

	private IPropertySourceFilter findPropertySourceFilter(Object owner) {
		for (IPropertySourceFilter filter : propertySourceFilterList) {
			if (filter.isFilterForObject(owner)) {
				return filter;
			}
		}
		return null;
	}

	private void readContributedPropertySourceFilters() {
		String symbolicName = Activator.getPlugin().getSymbolicName();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] filters = registry.getConfigurationElementsFor(symbolicName, EXTP_PROPERTY_SOURCE_FILTERS);
		for (IConfigurationElement filter : filters) {
			try {
				Object object = filter.createExecutableExtension(ATTR_CLASS);
				if (object instanceof IPropertySourceFilter) {
					propertySourceFilterList.add((IPropertySourceFilter) object);
				} else {
					String[] args = new String[] { ATTR_CLASS, NODE_FILTER, filter.getDeclaringExtension().getExtensionPointUniqueIdentifier(),
							filter.getContributor().getName(), IPropertySourceFilter.class.getName() };
					String msg = NLS.bind(EMFMessages.error_unexpectedImplementationOfElementAttributeInContribution, args);
					IStatus status = StatusUtil.createErrorStatus(Activator.getPlugin(), new RuntimeException(msg));
					throw new CoreException(status);
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}
}
