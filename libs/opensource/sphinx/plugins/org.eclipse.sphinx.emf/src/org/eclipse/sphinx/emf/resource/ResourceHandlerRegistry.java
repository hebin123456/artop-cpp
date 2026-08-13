/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *     Siemens - [577068] Call all registered resource handlers
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLResource.ResourceHandler;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * A registry which creates ResourceHandlers. By calling {@link #addHandlerType(String, Class)} the registry can be
 * configured which type of ResourceHandle is to be created for which namespace URI.
 */
public class ResourceHandlerRegistry {

	private static final String EXTPOINT_RESOURCE_HANDLERS = "resourceHandlers"; //$NON-NLS-1$

	private static final String ELEMENT_HANDLER = "handler"; //$NON-NLS-1$

	private static final String ATTR_NSURI_PATTERN = "nsURIPattern"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	public static final ResourceHandlerRegistry INSTANCE = new ResourceHandlerRegistry();

	/**
	 * The registered resource handler descriptors.
	 */
	private List<ResourceHandlerDescriptor> resourceHandlerDescriptors;

	private void init() {
		resourceHandlerDescriptors = new ArrayList<ResourceHandlerDescriptor>();
		readRegistry();
	}

	private void readRegistry() {
		try {
			if (ExtendedPlatform.IS_PLATFORM_RUNNING) {
				if (Activator.getPlugin() != null) {
					String symbolicName = Activator.getPlugin().getSymbolicName();
					IExtensionRegistry registry = Platform.getExtensionRegistry();
					if (registry != null) {
						IConfigurationElement[] contributions = registry.getConfigurationElementsFor(symbolicName, EXTPOINT_RESOURCE_HANDLERS);
						for (IConfigurationElement contribution : contributions) {
							try {
								readContribution(contribution);
							} catch (Exception ex) {
								PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
	}

	private void readContribution(IConfigurationElement contribution) {
		if (ELEMENT_HANDLER.equals(contribution.getName())) {
			resourceHandlerDescriptors.add(new ResourceHandlerDescriptor(contribution));
		}
	}

	protected List<ResourceHandlerDescriptor> getDescriptors() {
		if (resourceHandlerDescriptors == null) {
			init();
		}
		return resourceHandlerDescriptors;
	}

	/**
	 * Creates a ResourceHandler of the type registered for the given namespace URI. If there is more than one
	 * applicable, then a {@link DelegatingResourceHandler} will be created and returned which will delegate all calls
	 * to each handler.
	 *
	 * @param nsURI
	 *            The URI of the namespace for which a ResourceHandler is to be retrieved.
	 * @return The ResourceHandler associated with the given namespace URI.
	 * @see #addHandlerType(String, Class)
	 */
	public ResourceHandler getHandler(String nsURI) {
		List<ResourceHandler> resourceHandlers = new ArrayList<>();
		for (ResourceHandlerDescriptor descriptor : getDescriptors()) {
			if (descriptor.isHandlerFor(nsURI)) {
				ResourceHandler resourceHandlerInstance = descriptor.createResourceHandler();
				if (resourceHandlerInstance != null) {
					resourceHandlers.add(resourceHandlerInstance);
				}
			}
		}
		if (resourceHandlers.isEmpty()) {
			return null;
		}
		if (resourceHandlers.size() == 1) {
			return resourceHandlers.get(0);
		}
		return new DelegatingResourceHandler(resourceHandlers);
	}

	/**
	 * Registers a ResourceHandler type with namespaces. A ResourceHandle of the specified type will be created if
	 * {@link #getHandler(String)} is called with a namespace URI matching the namespace URI pattern passed to this
	 * method. Note that, if there is more than one handler for the same namespace URI, then all of them will be used.
	 *
	 * @param nsURIPattern
	 *            A namespace URI pattern describing for which namespaces a ResourceHandler of the given type is to be
	 *            returned.
	 * @param handlerType
	 *            The type of ResourceHandler which is to be created for the specified namespaces.
	 * @see #getHandler(String)
	 */
	public void addHandlerType(String nsURIPattern, Class<? extends ResourceHandler> handlerType) {
		List<ResourceHandlerDescriptor> descriptors = getDescriptors();
		descriptors.add(new ResourceHandlerDescriptor(nsURIPattern, handlerType));
	}

	protected class ResourceHandlerDescriptor {

		private IConfigurationElement configElement = null;
		private Class<? extends ResourceHandler> resourceHandlerType = null;
		private String nsURIPattern;

		protected ResourceHandlerDescriptor(IConfigurationElement aConfigElement) {
			Assert.isNotNull(aConfigElement, "ResourceHandlerRegistry.ResourceHandlerDescriptor objects cannot be null."); //$NON-NLS-1$
			Assert.isLegal(ELEMENT_HANDLER.equals(aConfigElement.getName()),
					"ResourceHandlerRegistry.ResourceHandlerDescriptor objects must have the name \"" + ELEMENT_HANDLER + "\"."); //$NON-NLS-1$ //$NON-NLS-2$
			configElement = aConfigElement;
			nsURIPattern = configElement.getAttribute(ATTR_NSURI_PATTERN);
		}

		public ResourceHandlerDescriptor(String nsURIPattern, Class<? extends ResourceHandler> handlerType) {
			Assert.isNotNull(nsURIPattern);
			Assert.isNotNull(handlerType);

			this.nsURIPattern = nsURIPattern;
			resourceHandlerType = handlerType;
		}

		public ResourceHandler createResourceHandler() {
			try {
				if (configElement != null) {
					return (ResourceHandler) configElement.createExecutableExtension(ATTR_CLASS);
				} else if (resourceHandlerType != null) {
					return resourceHandlerType.newInstance();
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
			return null;
		}

		public boolean isHandlerFor(String nsURI) {
			Assert.isNotNull(nsURI);
			return nsURI.matches(nsURIPattern);
		}
	}

	public static class DelegatingResourceHandler implements ResourceHandler {

		private final List<ResourceHandler> resourceHandlers;

		public DelegatingResourceHandler(List<ResourceHandler> resourceHandlers) {
			this.resourceHandlers = Collections.unmodifiableList(resourceHandlers);
		}

		public List<ResourceHandler> getResourceHandlers() {
			return resourceHandlers;
		}

		@Override
		public void preLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) {
			for (ResourceHandler handler : resourceHandlers) {
				handler.preLoad(resource, inputStream, options);
			}
		}

		@Override
		public void postLoad(XMLResource resource, InputStream inputStream, Map<?, ?> options) {
			for (ResourceHandler handler : resourceHandlers) {
				handler.postLoad(resource, inputStream, options);
			}
		}

		@Override
		public void preSave(XMLResource resource, OutputStream outputStream, Map<?, ?> options) {
			for (ResourceHandler handler : resourceHandlers) {
				handler.preSave(resource, outputStream, options);
			}
		}

		@Override
		public void postSave(XMLResource resource, OutputStream outputStream, Map<?, ?> options) {
			for (ResourceHandler handler : resourceHandlers) {
				handler.postSave(resource, outputStream, options);
			}
		}

	}

}
