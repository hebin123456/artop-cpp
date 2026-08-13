/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Enhancements and maintenance
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.sphinx.emf.validation.evalidator.adapter.EValidatorRegistering;
import org.osgi.framework.BundleContext;

/**
 * This is the central singleton for the Hummingbird20 model plugin.
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
	 * Returns the singleton instance of the Eclipse plugin.
	 *
	 * @return the singleton instance.
	 */
	@Override
	public ResourceLocator getPluginResourceLocator() {
		return plugin;
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
	 * The actual implementation of the Eclipse <b>Plugin</b>.
	 */
	public static class Implementation extends EclipsePlugin {
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

			// Register EValidator for each contribution to org.eclipse.sphinx.emf.validation.registration
			EValidatorRegistering.getSingleton().eValidatorSetAllContributions(getSymbolicName());
		}
	}
}
