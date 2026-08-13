/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 BMW Car IT, itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     itemis - [393021] ClassCastExceptions raised during loading model resources with Sphinx are ignored
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [427461] Add progress monitor to resource load options (useful for loading large models)
 *     itemis - [454092] Loading model resources
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Extensible ModelLoadManager. This class provides a public {@link IModelLoadManager} instance through the
 * {@link #INSTANCE} field. The instance is either a custom implementation registered through
 * <i>org.eclipse.sphinx.emf.workspace.modelLoadManager</i> extension point, or it is a {@link DefaultModelLoadManager}
 * if no extension is found. It is recommended to use the {@link DefaultModelLoadManager} as a base class for the
 * ModelLoadManager extensions.
 */
public final class ModelLoadManager {

	public static final String EXTPOINT_MODEL_LOAD_MANAGER = "org.eclipse.sphinx.emf.workspace.modelLoadManager"; //$NON-NLS-1$
	public static final String NODE_MODEL_LOAD_MANAGER = "modelLoadManager"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	/**
	 * This field holds an {@link IModelLoadManager} instance. It is initialized when the class is loaded. It is either
	 * a custom ModelLoadManger registered as an extension, or it is {@link DefaultModelLoadManager}.
	 */
	public static final IModelLoadManager INSTANCE;

	static {
		IModelLoadManager extendedInstance = null;
		for (IConfigurationElement cfgElement : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTPOINT_MODEL_LOAD_MANAGER)) {
			if (NODE_MODEL_LOAD_MANAGER.equals(cfgElement.getName())) {
				try {
					Object obj = cfgElement.createExecutableExtension(ATTR_CLASS);
					if (obj instanceof IModelLoadManager) {
						extendedInstance = (IModelLoadManager) obj;
						break;
					}
				} catch (CoreException ex) {
					String message = MessageFormat.format(Messages.error_createModelLoadManager, cfgElement.getAttribute(ATTR_CLASS));
					PlatformLogUtil.logAsWarning(Activator.getPlugin(), message);
				}
			}
		}
		INSTANCE = extendedInstance == null ? new DefaultModelLoadManager() : extendedInstance;
	}

	private ModelLoadManager() {

	}

}