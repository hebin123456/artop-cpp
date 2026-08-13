/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [402945] Add a new Hummingbird project creation wizard for Hummingbird examples
 *     itemis - [402951] Add a new Hummingbird file creation wizard for Hummingbird examples
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird.ide.internal.preferences;

import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.examples.hummingbird.ide.internal.Activator;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Initializes the Hummingbird preferences with default values.
 */
public class HummingbirdPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * The qualifier which is commonly used for all Hummingbird preferences.
	 */
	public static final String QUALIFIER = Activator.getPlugin().getSymbolicName();

	/**
	 * The key for Hummingbird metamodel version preference.
	 */
	public static final String PREF_METAMODEL_VERSION = "hummingbird.metamodel.version"; //$NON-NLS-1$

	/**
	 * The default value for {@link #PREF_METAMODEL_VERSION}.
	 */
	public static final String PREF_METAMODEL_VERSION_DEFAULT = getMetamodelVersionDefault();

	/**
	 * The minor release/revision used when saving Hummingbird XMI files
	 */
	public static final String PREF_RESOURCE_VERSION = "hummingbird.resource.version"; //$NON-NLS-1$

	/**
	 * The minor release/revision used when saving Hummingbird XMI files
	 */
	public static final String PREF_RESOURCE_VERSION_DEFAULT = "same.as.in.original.resource"; //$NON-NLS-1$

	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPreferences = getDefaultPreferences();
		if (defaultPreferences == null) {
			RuntimeException ex = new RuntimeException("Failed to retrieve default preferences for '" + QUALIFIER + "'."); //$NON-NLS-1$ //$NON-NLS-2$
			PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
		}

		defaultPreferences.put(PREF_METAMODEL_VERSION, PREF_METAMODEL_VERSION_DEFAULT);
		defaultPreferences.put(PREF_RESOURCE_VERSION, PREF_RESOURCE_VERSION_DEFAULT);
	}

	/**
	 * Returns the {@link IEclipsePreferences default preference} for {@link HummingbirdPreferenceInitializer#QUALIFIER}
	 * .
	 * 
	 * @return The {@link IEclipsePreferences default preferences} for
	 *         {@link HummingbirdPreferenceInitializer#QUALIFIER} or <code>null</code> if no such could be determined.
	 */
	private IEclipsePreferences getDefaultPreferences() {
		return DefaultScope.INSTANCE.getNode(QUALIFIER);
	}

	/**
	 * Returns the id of the most recent {@link HummingbirdMMDescriptor Hummingbird metamodel descriptor} as default.
	 * 
	 * @return The default value for {@link #PREF_METAMODEL_VERSION} or an empty string if no such could be determined.
	 */
	private static String getMetamodelVersionDefault() {
		List<HummingbirdMMDescriptor> descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(HummingbirdMMDescriptor.INSTANCE, true);
		if (descriptors.isEmpty()) {
			return ""; //$NON-NLS-1$
		}

		HummingbirdMMDescriptor mostRecentDescriptor = descriptors.get(descriptors.size() - 1);
		return mostRecentDescriptor.getIdentifier();
	}
}
