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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;

/**
 * Interface enabling the retrieving and setting of one Eclipse workspace preference.
 * <p>
 * Extends the {@link IEclipsePreference} to define {@link #get(IProject) get} and {@link #set(IProject, Object) set}
 * methods to retrieve and set the value of the preference.
 */
public interface IWorkspacePreference<T> extends IEclipsePreference<T> {

	/**
	 * Returns the value of the preference. If the value does not exist yet in the Eclipse preferences mechanism,
	 * returns the default value.
	 * 
	 * @return the value of the preference
	 * @see IEclipsePreference#getDefaultValueAsString()
	 */
	T get();

	/**
	 * Set the value of the preference. The value is provided as an object.
	 * 
	 * @param valueAsObject
	 *            the new value of the preference
	 */
	void set(T valueAsObject);

	T getDefaultValueAsObject();

	void setToDefault();

	/**
	 * Registers the given listener for notification of changes of this preference. Calling this method multiple times
	 * with the same listener has no effect. The given listener argument must not be <code>null</code>.
	 * 
	 * @param listener
	 *            the preference change listener to register
	 * @see #removePreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void addPreferenceChangeListener(IPreferenceChangeListener listener);

	/**
	 * Unregisters the given listener from receiving notification of changes of this preference. Calling this method
	 * multiple times with the same listener has no effect. The given listener argument must not be <code>null</code>.
	 * 
	 * @param listener
	 *            the preference change listener to remove
	 * @see #addPreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void removePreferenceChangeListener(IPreferenceChangeListener listener);
}