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
package org.eclipse.sphinx.emf.validation;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.sphinx.emf.validation.bridge.extensions.RulesExtCache;
import org.eclipse.sphinx.emf.validation.listeners.ResourceURIChangeListener;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.sphinx.emf.validation"; //$NON-NLS-1$

	/** The shared instance */
	private static Activator plugin;

	/** the resource listener on URI changes */
	private static IResourceChangeListener resourceChangeListener = null;

	/**
	 * The constructor
	 */
	public Activator() {
		resourceChangeListener = new ResourceURIChangeListener();
	}

	/*
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		try {
			super.start(context);
			plugin = this;

			/*
			 * Let's start the RulesExtCache.
			 */
			RulesExtCache.getSingleton().startup();

		} catch (Exception e) {
			PlatformLogUtil.logAsError(plugin, e);
		}

		// Add our listener on the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.POST_BUILD);

	}

	/*
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;

		/*
		 * Let's stop the RulesExtCache.
		 */
		RulesExtCache.getSingleton().shutdown();

		super.stop(context);

		// Remove our listener on the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace != null) {
			workspace.removeResourceChangeListener(resourceChangeListener);
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
