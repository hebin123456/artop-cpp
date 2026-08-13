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
package org.eclipse.sphinx.platform.internal;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.internal.util.ContentTypeIdCachePurger;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.sphinx.platform"; //$NON-NLS-1$

	/** The shared instance */
	private static Activator plugin;

	/** The bundle context */
	private static BundleContext context;

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns the shared bundle context
	 *
	 * @return the shared bundle context
	 */
	public static BundleContext getContext() {
		return context;
	}

	private IResourceChangeListener contentTypeIdPropertyInvalidator;

	/*
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
		Activator.context = bundleContext;

		System.setProperty(IExtendedPlatformConstants.SYSTEM_PROPERTY_PLATFORM_FEATURE_VERSION, ExtendedPlatform.getFeatureVersion());

		contentTypeIdPropertyInvalidator = new ContentTypeIdCachePurger();
		ServiceCaller.callOnce(getClass(), IWorkspace.class, workspace -> {
			workspace.addResourceChangeListener(contentTypeIdPropertyInvalidator);
		});
	}

	/*
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, workspace -> {
			workspace.removeResourceChangeListener(contentTypeIdPropertyInvalidator);
		});

		plugin = null;
		Activator.context = null;
		super.stop(bundleContext);
	}
}
