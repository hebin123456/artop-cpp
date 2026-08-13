/**
 * <copyright>
 *
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.xtendxpand.internal.Activator;

/**
 * Initializes the Outlets preference with its default value.
 */
public class OutletsPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * The qualifier for the Outlets preference.
	 */
	public static final String QUALIFIER = Activator.getPlugin().getSymbolicName();

	/**
	 * The key for the Outlets preference.
	 */
	public static final String PREF_OUTLETS = "xpand.outlets"; //$NON-NLS-1$

	/**
	 * The key for the "prDefaultExcludes" preference used for file names which are excluded by default.
	 */
	public static final String PREF_PR_DEFAULT_EXCLUDES = "xpand.prDefaultExcludes"; //$NON-NLS-1$

	/**
	 * The key for the "prExcludes" preference used for additional excludes.
	 */
	public static final String PREF_PR_EXCLUDES = "xpand.prExcludes"; //$NON-NLS-1$

	/**
	 * The default value for the Outlets preference.
	 */
	public static final String PREF_OUTLETS_DEFAULT = "@${project_loc}/gen"; //$NON-NLS-1$

	/**
	 * The default value for the prDefaultExcludes preference.
	 */
	public static final boolean PREF_PR_DEFAULT_EXCLUDES_DEFAULT = true;

	/**
	 * The default value for the prExcludes preference.
	 */
	public static final String PREF_PR_EXCLUDES_DEFAULT = ""; //$NON-NLS-1$

	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(QUALIFIER);
		if (defaultPreferences == null) {
			RuntimeException ex = new RuntimeException("Failed to retrieve default preferences for '" + QUALIFIER + "'."); //$NON-NLS-1$ //$NON-NLS-2$
			PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
		}

		defaultPreferences.put(PREF_OUTLETS, PREF_OUTLETS_DEFAULT);
		defaultPreferences.put(PREF_PR_EXCLUDES, PREF_PR_EXCLUDES_DEFAULT);
		defaultPreferences.putBoolean(PREF_PR_DEFAULT_EXCLUDES, PREF_PR_DEFAULT_EXCLUDES_DEFAULT);
	}
}
