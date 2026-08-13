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
package org.eclipse.sphinx.emf.workspace.ui.internal;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.ui.EclipseUIPlugin;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sphinx.emf.workspace.ui.internal.saving.CloseWorkbenchListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

/**
 * This is the central singleton for this plug-in.
 */
public final class Activator extends EMFPlugin {

	public static final IPath FULL_PATH = new Path("/full"); //$NON-NLS-1$

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
	 * The actual implementation of the Eclipse <b>Plugin</b>.
	 */
	public static class Implementation extends EclipseUIPlugin {

		/**
		 * {@linkplain IWorkbenchListener Workbench listener} that is notified during workbench closing; asks
		 * confirmation for dirty resources saving.
		 */
		private CloseWorkbenchListener closeWorkbenchListener = new CloseWorkbenchListener();

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
			Job.getJobManager().addJobChangeListener(ModelLoadingProgressIndicator.INSTANCE);
			PlatformUI.getWorkbench().addWorkbenchListener(closeWorkbenchListener);
		}

		@Override
		public void stop(BundleContext context) throws Exception {
			super.stop(context);
			Job.getJobManager().removeJobChangeListener(ModelLoadingProgressIndicator.INSTANCE);
			PlatformUI.getWorkbench().removeWorkbenchListener(closeWorkbenchListener);
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

		/**
		 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions are retrieved
		 * from the *lcl16 folders.
		 *
		 * @param action
		 *            the action
		 * @param iconName
		 *            the icon name
		 */
		public void setLocalImageDescriptors(IAction action, String iconName) {
			setImageDescriptors(action, "lcl16", iconName); //$NON-NLS-1$
		}

		private void setImageDescriptors(IAction action, String type, String relPath) {
			IPath dPath = FULL_PATH.append("d" + type).append(relPath); //$NON-NLS-1$
			ImageDescriptor imageDescriptor = getImageDescriptor(dPath.toString());
			if (imageDescriptor != null) {
				action.setDisabledImageDescriptor(imageDescriptor);
			}

			IPath ePath = FULL_PATH.append("e" + type).append(relPath); //$NON-NLS-1$
			ImageDescriptor descriptor = getImageDescriptor(ePath.toString());
			if (descriptor != null) {
				action.setHoverImageDescriptor(descriptor);
				action.setImageDescriptor(descriptor);
			}
		}
	}
}
