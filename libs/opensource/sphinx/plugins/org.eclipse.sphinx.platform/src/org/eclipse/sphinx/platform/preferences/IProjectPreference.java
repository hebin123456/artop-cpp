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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;

/**
 * Interface enabling the retrieving and setting of one Eclipse project preference.
 * <p>
 * Extends the {@link IEclipsePreference} by adding the support for a required project nature identifier. This may be
 * used to make the property dependent of a specific {@link IProjectNature project nature}.
 * <p>
 * Defines {@link #get(IProject) get} and {@link #set(IProject, Object) set} methods to retrieve and set the value of
 * the preference.
 */
public interface IProjectPreference<T> extends IEclipsePreference<T> {

	/**
	 * Return the identifier of the project nature that is required by the preference.
	 * 
	 * @return the required project nature
	 */
	String getRequiredProjectNatureId();

	/**
	 * Returns the value of the preference. If the value does not exist yet in the Eclipse preferences mechanism,
	 * returns the default value. The returned value may depend on the nature of the project that is provided as a
	 * parameter.
	 * 
	 * @param project
	 *            the project for which the preference is retrieved
	 * @return the value of the preference
	 * @see IEclipsePreference#getDefaultValueAsString()
	 */
	T get(IProject project);

	/**
	 * Set the value of the preference. The behavior of this method may depend on the nature of the project that is
	 * provided as a parameter. The value is provided as an object.
	 * 
	 * @param project
	 *            the project for which the preference is retrieved
	 * @param valueAsObject
	 *            the new value of the preference
	 */
	void set(IProject project, T valueAsObject);

	T getDefaultValueAsObject(IProject project);

	void setToDefault(IProject project);

	/**
	 * Registers the given listener for notification of changes of this preference in specified project. Calling this
	 * method multiple times with the same listener has no effect. The given listener argument must not be
	 * <code>null</code>.
	 * 
	 * @param project
	 *            the project in which the changes of this preference are to be observed
	 * @param listener
	 *            the preference change listener to register
	 * @see #removePreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void addPreferenceChangeListener(IProject project, IPreferenceChangeListener listener);

	/**
	 * Unregisters the given listener from receiving notification of changes of this preference in specified project.
	 * Calling this method multiple times with the same listener has no effect. The given listener argument must not be
	 * <code>null</code>.
	 * 
	 * @param project
	 *            the project in which the changes of this preference have been observed
	 * @param listener
	 *            the preference change listener to remove
	 * @see #addPreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void removePreferenceChangeListener(IProject project, IPreferenceChangeListener listener);
}