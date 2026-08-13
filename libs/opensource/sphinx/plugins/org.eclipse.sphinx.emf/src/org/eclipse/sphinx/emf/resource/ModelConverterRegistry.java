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
 *     BMW Car IT - Added mechanism for converter description management
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.resource.ModelConverterDescription;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Registry managing contributed {@link IModelConverter}s and their {@link IModelConverterDescription}s.
 */
public class ModelConverterRegistry {

	private static final String EXTP_MODEL_CONVERTERS = "modelConverters"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ELEM_DESC = "behaviorDetails"; //$NON-NLS-1$
	private static final String ELEM_WARNING = "warning"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	public static final ModelConverterRegistry INSTANCE = new ModelConverterRegistry();

	/**
	 * The registered model converters.
	 */
	protected List<IModelConverter> fModelConverters = null;

	private Map<IModelConverter, IModelConverterDescription> fConverterDescriptions = new HashMap<IModelConverter, IModelConverterDescription>();

	/**
	 * Reads the platform extension registry to initialize the model converter registry.
	 */
	protected void init() {
		fModelConverters = new ArrayList<IModelConverter>();
		try {
			if (ExtendedPlatform.IS_PLATFORM_RUNNING) {
				if (Activator.getPlugin() != null) {
					String symbolicName = Activator.getPlugin().getSymbolicName();
					IExtensionRegistry registry = Platform.getExtensionRegistry();
					if (registry != null) {
						IConfigurationElement[] configurationElements = registry.getConfigurationElementsFor(symbolicName, EXTP_MODEL_CONVERTERS);
						for (IConfigurationElement element : configurationElements) {
							try {
								IModelConverter converter = (IModelConverter) element.createExecutableExtension(ATTR_CLASS);
								IModelConverterDescription convDesc = new ModelConverterDescription(getDescription(element), getWarning(element));
								fConverterDescriptions.put(converter, convDesc);
								fModelConverters.add(converter);
							} catch (CoreException ex) {
								PlatformLogUtil.logAsError(Activator.getDefault(), ex);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	private String getWarning(IConfigurationElement element) {
		return getUniqueChildValue(element, ELEM_WARNING);
	}

	private String getDescription(IConfigurationElement element) {
		return getUniqueChildValue(element, ELEM_DESC);
	}

	private String getUniqueChildValue(IConfigurationElement configElem, String childName) {
		IConfigurationElement[] childElems = configElem.getChildren(childName);
		if (childElems.length == 1) {
			return childElems[0].getValue();
		}
		if (childElems.length > 1) {
			// TODO: Error Logging! Only one element was expected!
		}
		return null;
	}

	/**
	 * Resets the converters to null, so that the registry will be read again next time the converters will be accessed.
	 */
	public void reset() {
		fModelConverters = null;
	}

	/**
	 * Gets the registered converters.
	 *
	 * @return the converters
	 */
	public List<IModelConverter> getConverters() {
		if (fModelConverters == null) {
			init();
		}
		return fModelConverters;
	}

	public IModelConverter getLoadConverter(XMLResource xmlResource, Map<?, ?> options) {
		for (IModelConverter converter : getConverters()) {
			if (converter.isLoadConverterFor(xmlResource, options)) {
				return converter;
			}
		}
		return null;
	}

	public IModelConverter getSaveConverter(XMLResource xmlResource, Map<?, ?> options) {
		for (IModelConverter converter : getConverters()) {
			if (converter.isSaveConverterFor(xmlResource, options)) {
				return converter;
			}
		}
		return null;
	}

	/**
	 * Returns the IModelConverterDescription for the specified <code>converter</code>
	 *
	 * @param converter
	 *            The IModelConverter for which the IModelConverterDescription is to be retrieved.
	 * @return The IModelConverterDescription of the <code>converter</code>.
	 * @since 0.7.0
	 */
	public IModelConverterDescription getConverterDescription(IModelConverter converter) {
		return fConverterDescriptions.get(converter);
	}

	/**
	 * Returns the descriptions of all the IModelConverters which target at <code>targetResourceVersion</code>.
	 *
	 * @param targetResourceVersion
	 *            The target resource version of the IModelConverters whom's descriptions are to be returned.
	 * @return The IModelConverterDescriptions of all IModelConverters which target at the specified
	 *         <code>targetResourceVersion</code>.
	 * @since 0.7.0
	 */
	public Collection<IModelConverterDescription> getConverterDescriptions(IMetaModelDescriptor targetResourceVersion) {
		Collection<IModelConverterDescription> converters = new ArrayList<IModelConverterDescription>();
		for (IModelConverter converter : getConverters()) {
			if (converter.getResourceVersionDescriptor() == targetResourceVersion) {
				converters.add(getConverterDescription(converter));
			}
		}
		return converters;
	}

}
