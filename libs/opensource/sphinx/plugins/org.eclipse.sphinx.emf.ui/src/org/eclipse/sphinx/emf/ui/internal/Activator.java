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
package org.eclipse.sphinx.emf.ui.internal;

import java.net.URL;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.ui.EclipseUIPlugin;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

/**
 * This is the central singleton for this plug-in.
 */
public final class Activator extends EMFPlugin {
	/**
	 * Keep track of the singleton.
	 */
	public static final Activator INSTANCE = new Activator();

	/**
	 * Keep track of the singleton.
	 */
	private static Implementation plugin;

	private ResourceManager resourceManager;

	/**
	 * Create the instance.
	 */
	public Activator() {
		super(new ResourceLocator[] {});
	}

	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 *
	 * @return the singleton instance.
	 */
	@Override
	public ResourceLocator getPluginResourceLocator() {
		return plugin;
	}

	/**
	 * Return the resourceManager used by this plug-in.
	 *
	 * @return
	 */
	public ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return resourceManager;
	}

	/**
	 * Returns the singleton instance of the Eclipse plugin.
	 *
	 * @return the singleton instance.
	 */
	public static Implementation getPlugin() {
		return plugin;
	}

	/**
	 * Returns the singleton instance of the Eclipse plug-in. This method does actually the same thing as getPlugin()
	 * and has been put in place for compatibility reasons with Activator classes which are not EMF-based but generated
	 * by PDE.
	 *
	 * @return the singleton instance.
	 */
	public static Implementation getDefault() {
		return plugin;
	}

	/**
	 * The actual implementation of the Eclipse <b>Plugin</b>.
	 */
	public static class Implementation extends EclipseUIPlugin {

		private IWorkbenchListener workbenchListener = new IWorkbenchListener() {

			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				// Shut down long running system jobs
				Job.getJobManager().cancel(IExtendedPlatformConstants.FAMILY_LONG_RUNNING);
				return true;
			}

			@Override
			public void postShutdown(IWorkbench workbench) {
			}
		};

		/**
		 * Creates an instance.
		 */
		public Implementation() {
			super();

			// Remember the static instance.
			//
			plugin = this;
		}

		@Override
		public void start(BundleContext context) throws Exception {
			super.start(context);
			PlatformUI.getWorkbench().addWorkbenchListener(workbenchListener);
		}

		@Override
		public void stop(BundleContext context) throws Exception {
			PlatformUI.getWorkbench().removeWorkbenchListener(workbenchListener);
			super.stop(context);
		}

		public ImageDescriptor getImageDescriptor(String key) {
			Object imageURL = getImage(key);
			if (imageURL instanceof URL) {
				return getImageDescriptor((URL) imageURL);
			}
			return null;
		}

		public ImageDescriptor getImageDescriptor(URL url) {
			// FIXME File bug to EMF: Impossible to use ExtendedImageRegistry.INSTANCE when Display.getCurrent() returns
			// null
			if (Display.getCurrent() != null) {
				return ExtendedImageRegistry.INSTANCE.getImageDescriptor(url);
			}
			return ImageDescriptor.createFromURL(url);
		}
	}
}
