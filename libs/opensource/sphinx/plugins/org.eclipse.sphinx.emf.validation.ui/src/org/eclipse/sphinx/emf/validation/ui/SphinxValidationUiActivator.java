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
package org.eclipse.sphinx.emf.validation.ui;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.ui.EclipseUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.ibm.icu.text.MessageFormat;

/**
 * The activator class controls the plug-in life cycle
 */
public class SphinxValidationUiActivator extends EclipseUIPlugin {

	public static final String VALIDATION_UI_PLUGIN_ID = "org.eclipse.sphinx.emf.validation.ui"; //$NON-NLS-1$

	/*** Constants for Images ***/

	// problems images
	public static final String IMG_OBJS_ERROR_PATH = "IMG_OBJS_ERROR_PATH"; //$NON-NLS-1$
	private static final String IMG_OBJS_ERROR_PATH_FILE = "full/obj16/error_obj.png"; //$NON-NLS-1$

	public static final String IMG_OBJS_WARNING_PATH = "IMG_OBJS_WARNING_PATH"; //$NON-NLS-1$
	private static final String IMG_OBJS_WARNING_PATH_FILE = "full/obj16/warning_obj.png"; //$NON-NLS-1$

	public static final String IMG_OBJS_INFO_PATH = "IMG_OBJS_INFO_PATH"; //$NON-NLS-1$
	private static final String IMG_OBJS_INFO_PATH_FILE = "full/obj16/info_obj.png"; //$NON-NLS-1$

	public final static String IMG_OBJS_HEADER_COMPLETE = "IMG_OBJS_HEADER_COMPLETE"; //$NON-NLS-1$
	private final static String IMG_OBJS_HEADER_COMPLETE_FILE = "full/obj16/header_complete.png"; //$NON-NLS-1$

	// toolbar buttons
	public final static String IMG_ETOOL_PROBLEM_CATEGORY = "IMG_ETOOL_PROBLEM_CATEGORY"; //$NON-NLS-1$
	private final static String IMG_ETOOL_PROBLEM_CATEGORY_FILE = "full/etool16/problem_category.png"; //$NON-NLS-1$

	// wizard images
	public final static String IMG_DLGBAN_QUICKFIX_DLG = "IMG_DLGBAN_QUICKFIX_DLG"; //$NON-NLS-1$
	private final static String IMG_DLGBAN_QUICKFIX_DLG_FILE = "full/wizban/quick_fix.png"; //$NON-NLS-1$

	// quick fix images
	public static final String IMG_DLCL_QUICK_FIX_DISABLED = "IMG_DLCL_QUICK_FIX_DISABLED";//$NON-NLS-1$
	public static final String IMG_DLCL_QUICK_FIX_DISABLED_FILE = "full/obj16/quickfix_warning_obj_disabled.png";//$NON-NLS-1$
	public static final String IMG_ELCL_QUICK_FIX_ENABLED = "IMG_ELCL_QUICK_FIX_ENABLED"; //$NON-NLS-1$
	public static final String IMG_ELCL_QUICK_FIX_ENABLED_FILE = "full/obj16/quickfix_warning_obj.png"; //$NON-NLS-1$

	/** The shared instance */
	private static SphinxValidationUiActivator plugin;

	/** The icon folder */
	public static final String ICON_PATH = "$nl$/icons/"; //$NON-NLS-1$

	private IPreferenceStore corePreferenceStore;

	/**
	 * The constructor
	 */
	public SphinxValidationUiActivator() {
	}

	/*
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SphinxValidationUiActivator getDefault() {
		return plugin;
	}

	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Logs the given message to the platform log. If you have an exception in hand, call log(String, Throwable)
	 * instead. If you have a status object in hand call log(String, IStatus) instead. This convenience method is for
	 * internal use by the IDE Workbench only and must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 */
	public static void log(String message) {
		getDefault().getLog().log(newStatus(IStatus.ERROR, message, null));
	}

	/**
	 * Logs the given message and throwable to the platform log. If you have a status object in hand call log(String,
	 * IStatus) instead. This convenience method is for internal use by the IDE Workbench only and must not be called
	 * outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 * @param t
	 *            The throwable from where the problem actually occurred.
	 */
	public static void log(String message, Throwable t) {
		IStatus status = newStatus(IStatus.ERROR, message, t);
		log(message, status);
	}

	/**
	 * Logs the given throwable to the platform log, indicating the class and method from where it is being logged (this
	 * is not necessarily where it occurred). This convenience method is for internal use by the IDE Workbench only and
	 * must not be called outside the IDE Workbench.
	 *
	 * @param clazz
	 *            The calling class.
	 * @param methodName
	 *            The calling method name.
	 * @param t
	 *            The throwable from where the problem actually occurred.
	 */
	public static void log(Class<?> clazz, String methodName, Throwable t) {
		String msg = MessageFormat.format("Exception in {0}.{1}: {2}", //$NON-NLS-1$
				clazz.getName(), methodName, t);
		log(msg, t);
	}

	/**
	 * Logs the given message and status to the platform log. This convenience method is for internal use by the IDE
	 * Workbench only and must not be called outside the IDE Workbench.
	 *
	 * @param message
	 *            A high level UI message describing when the problem happened. May be <code>null</code>.
	 * @param status
	 *            The status describing the problem. Must not be null.
	 */
	public static void log(String message, IStatus status) {

		// 1FTUHE0: ITPCORE:ALL - API - Status & logging - loss of semantic info

		if (message != null) {
			getDefault().getLog().log(newStatus(IStatus.ERROR, message, null));
		}

		getDefault().getLog().log(status);
	}

	/**
	 * This method must not be called outside the workbench. Utility method for creating status.
	 *
	 * @param severity
	 * @param message
	 * @param exception
	 * @return {@link IStatus}
	 */
	public static IStatus newStatus(int severity, String message, Throwable exception) {

		String statusMessage = message;
		if (message == null || message.trim().length() == 0) {
			if (exception == null) {
				throw new IllegalArgumentException();
			} else if (exception.getMessage() == null) {
				statusMessage = exception.toString();
			} else {
				statusMessage = exception.getMessage();
			}
		}

		return new Status(severity, getPluginId(), severity, statusMessage, exception);
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		if (corePreferenceStore == null) {
			org.eclipse.sphinx.emf.validation.Activator coreActivator = org.eclipse.sphinx.emf.validation.Activator.getDefault();
			corePreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, coreActivator.getBundle().getSymbolicName());
		}
		return corePreferenceStore;
	}

	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		ImageDescriptor imageDescriptor = loadImageDescriptor(IMG_ETOOL_PROBLEM_CATEGORY_FILE);
		registry.put(IMG_ETOOL_PROBLEM_CATEGORY, imageDescriptor);

		imageDescriptor = loadImageDescriptor(IMG_DLGBAN_QUICKFIX_DLG_FILE);
		registry.put(IMG_DLGBAN_QUICKFIX_DLG, imageDescriptor);

		imageDescriptor = loadImageDescriptor(IMG_OBJS_ERROR_PATH_FILE);
		registry.put(IMG_OBJS_ERROR_PATH, imageDescriptor);

		imageDescriptor = loadImageDescriptor(IMG_OBJS_WARNING_PATH_FILE);
		registry.put(IMG_OBJS_WARNING_PATH, imageDescriptor);

		imageDescriptor = loadImageDescriptor(IMG_OBJS_INFO_PATH_FILE);
		registry.put(IMG_OBJS_INFO_PATH, imageDescriptor);

		imageDescriptor = loadImageDescriptor(IMG_OBJS_HEADER_COMPLETE_FILE);
		registry.put(IMG_OBJS_HEADER_COMPLETE_FILE, imageDescriptor);

		imageDescriptor = loadImageDescriptor(IMG_DLCL_QUICK_FIX_DISABLED_FILE);
		registry.put(IMG_DLCL_QUICK_FIX_DISABLED, imageDescriptor);

		imageDescriptor = loadImageDescriptor(IMG_ELCL_QUICK_FIX_ENABLED_FILE);
		registry.put(IMG_ELCL_QUICK_FIX_ENABLED, imageDescriptor);
	}

	public ImageDescriptor loadImageDescriptor(String path) {
		Object url = getImage(path);
		if (url instanceof URL) {
			return loadImageDescriptor((URL) url);
		} else {
			return null;
		}
	}

	public ImageDescriptor loadImageDescriptor(URL url) {
		return ImageDescriptor.createFromURL(url);
	}

	public ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}

}
