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
package org.eclipse.sphinx.platform.preferences;

/**
 * Interface enabling the retrieving and setting of one Eclipse preference. This preference may be a workspace
 * preference, a project preference, or both. <code>T</code> is the type of the preference.
 * <p>
 * Supports automatic conversion of string-based preference values returned by <code>IEclipsePreferences</code> API to
 * corresponding domain objects and vice versa.
 * <p>
 * Compared with the default property mechanism, this interface adds the support for a default value that is used when
 * the given property is not set.
 * <p>
 * A class implementing this interface gets and set the preference value by using the Eclipse preferences mechanism.
 * 
 * @see org.osgi.service.prefs.Preferences
 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences
 * @see org.eclipse.core.resources.ProjectScope
 * @see org.eclipse.core.runtime.preferences.InstanceScope
 */
public interface IEclipsePreference<T> {

	/**
	 * Returns the qualifier of the preference. This qualifier is used to identify the preference in the Eclipse
	 * preferences mechanism.
	 * 
	 * @return the qualifier of the preference
	 */
	String getQualifier();

	/**
	 * Returns the key of the preference. This key is used to identify the preference in the Eclipse preferences
	 * mechanism.
	 * 
	 * @return the key of the preference
	 */
	String getKey();

	/**
	 * Returns the default value for the preference. This value may be used when no actual value is registered for this
	 * preference. The value is returned as a <code>String</code>, whatever the type of the preference is.
	 * 
	 * @return the default value for the preference
	 */
	String getDefaultValueAsString();
}