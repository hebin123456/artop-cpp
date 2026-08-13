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
package org.eclipse.sphinx.platform.ui.internal;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.sphinx.platform.ui"; //$NON-NLS-1$

	private static final String JQUERY_DIR = "jquery"; //$NON-NLS-1$
	private static final String BOOTSTRAP_DIR = "dist"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/** The bundle context */
	private static BundleContext context;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Activator.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		Activator.context = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
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

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static String getBootstrapDir() {
		return getDirectoryPath(new Path(BOOTSTRAP_DIR));
	}

	public static String getJQueryDir() {
		return getDirectoryPath(new Path(JQUERY_DIR));
	}

	private static String getDirectoryPath(Path path) {
		URL url = FileLocator.findEntries(context.getBundle(), path)[0];
		URI uri = null;
		try {
			uri = FileLocator.toFileURL(url).toURI();
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}

		File file = new File(uri);
		return file.getAbsolutePath();
	}
}
