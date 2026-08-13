/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Avoid usage of Object.finalize
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.emf.workspace.syncing.ModelSynchronizer;
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

	/**
	 * Create the instance.
	 */
	public Activator() {
		super(new ResourceLocator[] {});
	}

	/**
	 * Returns the singleton instance of the Eclipse plug-in.
	 * 
	 * @return the singleton instance.
	 */
	@Override
	public ResourceLocator getPluginResourceLocator() {
		return plugin;
	}

	/**
	 * Returns the singleton instance of the Eclipse plug-in.
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
	 * The actual implementation of the Eclipse <b>Plug-in</b>.
	 */
	public static class Implementation extends EclipsePlugin {

		/**
		 * Creates an instance.
		 */
		public Implementation() {
			super();

			// Remember the static instance
			plugin = this;
		}

		@Override
		public void start(BundleContext context) throws Exception {
			super.start(context);
			startWorkspaceSynchronizing();
			ModelSaveManager.INSTANCE.start();
		}

		@Override
		public void stop(BundleContext context) throws Exception {
			super.stop(context);
			ModelSaveManager.INSTANCE.stop();
			stopWorkspaceSynchronizing();
		}

		/**
		 * @see ModelSynchronizer#start()
		 */
		public void startWorkspaceSynchronizing() {
			ModelSynchronizer.INSTANCE.start();
		}

		/**
		 * @see ModelSynchronizer#stop()
		 */
		public void stopWorkspaceSynchronizing() {
			ModelSynchronizer.INSTANCE.stop();
		}
	}
}
