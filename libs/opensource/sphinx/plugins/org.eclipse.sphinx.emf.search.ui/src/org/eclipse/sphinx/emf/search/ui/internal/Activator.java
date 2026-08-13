/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.search.ui.internal;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.ui.EclipseUIPlugin;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.jface.dialogs.IDialogSettings;

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
		/**
		 * Creates an instance.
		 */
		public Implementation() {
			super();

			// Remember the static instance.
			//
			plugin = this;
		}

		/**
		 * Returns a section in the Java plugin's dialog settings. If the section doesn't exist yet, it is created.
		 *
		 * @param name
		 *            the name of the section
		 * @return the section of the given name
		 */
		public IDialogSettings getDialogSettingsSection(String name) {
			IDialogSettings dialogSettings = getDialogSettings();
			IDialogSettings section = dialogSettings.getSection(name);
			if (section == null) {
				section = dialogSettings.addNewSection(name);
			}
			return section;
		}
	}
}
