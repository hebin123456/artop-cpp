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
 * Interface enabling the retrieving and setting of one Eclipse preference that may be store both as a project
 * preference or as a workspace preference.
 * <p>
 * Extends the {@link IEclipsePreference} by adding the support for a required project nature identifier. This may be
 * used to make the property dependent of a specific {@link IProjectNature project nature}.
 * <p>
 * Defines several <code>get</code>s and <code>set</code>s methods to retrieve and set the value of the preference.
 */
public interface IProjectWorkspacePreference<T> extends IEclipsePreference<T> {

	/**
	 * Return the identifier of the project nature that is required by the preference.
	 * 
	 * @return the required project nature
	 */
	String getRequiredProjectNatureId();

	/**
	 * Returns the value of the preference. The preference is first searched in the project, then in the workspace, then
	 * the default value is used. The behavior of this method may depend on the nature of the project that is provided
	 * as a parameter.
	 * 
	 * @param project
	 *            the project for which the preference is to be retrieved
	 * @return the value of the preference
	 */
	T get(IProject project);

	T getDefaultValueAsObject();

	/**
	 * Returns the value of the preference from the project. If the value does not exist yet in the Eclipse project
	 * preferences mechanism <code>null</code> is returned. The behavior of this method may depend on the nature of the
	 * project that is provided as a parameter.
	 * 
	 * @param project
	 *            the project for which the preference is to be retrieved
	 * @return the value of the preference
	 * @see IProjectPreference#get(IProject)
	 */
	T getFromProject(IProject project);

	/**
	 * Set the value of the preference in the project. The behavior of this method may depend on the nature of the
	 * project that is provided as a parameter. The value is provided as an object.
	 * 
	 * @param project
	 *            the project for which the preference is to be retrieved
	 * @param valueAsObject
	 *            the new value of the preference
	 * @see IProjectPreference#set(IProject, Object)
	 */
	void setInProject(IProject project, T valueAsObject);

	void setToDefaultInProject(IProject project);

	/**
	 * Registers the given listener for notification of project-level changes of this preference in specified project.
	 * Calling this method multiple times with the same listener has no effect. The given listener argument must not be
	 * <code>null</code>.
	 * 
	 * @param project
	 *            the project in which the changes of this preference are to be observed
	 * @param listener
	 *            the preference change listener to register
	 * @see #removePreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void addPreferenceChangeListenerToProject(IProject project, IPreferenceChangeListener listener);

	/**
	 * Unregisters the given listener from receiving notification of project-level changes of this preference in
	 * specified project. Calling this method multiple times with the same listener has no effect. The given listener
	 * argument must not be <code>null</code>.
	 * 
	 * @param project
	 *            the project in which the changes of this preference have been observed
	 * @param listener
	 *            the preference change listener to remove
	 * @see #addPreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void removePreferenceChangeListenerFromProject(IProject project, IPreferenceChangeListener listener);

	/**
	 * Returns the value of the preference from the workspace. If the value does not exist yet in the Eclipse workspace
	 * preferences mechanism the default value is returned.
	 * 
	 * @return the value of the preference
	 * @see IWorkspacePreference#get()
	 */
	T getFromWorkspace();

	/**
	 * Returns the value of the preference from the workspace. Enables the given project to be taken into account during
	 * conversion of the preference's string value to the corresponding object value. If the value does not exist yet in
	 * the Eclipse workspace preferences mechanism the default value is returned. The behavior of this method may depend
	 * on the nature of the project that is provided as a parameter.
	 * 
	 * @param project
	 *            the project for which the preference is to be retrieved
	 * @return the value of the preference
	 * @see IWorkspacePreference#get()
	 */
	T getFromWorkspaceForProject(IProject project);

	/**
	 * Sets the value of the preference in the workspace. The value is provided as an object.
	 * 
	 * @param valueAsObject
	 *            the new value of the preference
	 * @see IWorkspacePreference#set(Object)
	 */
	void setInWorkspace(T valueAsObject);

	void setToDefaultInWorkspace();

	/**
	 * Registers the given listener for notification of workspace-level changes of this preference. Calling this method
	 * multiple times with the same listener has no effect. The given listener argument must not be <code>null</code>.
	 * 
	 * @param listener
	 *            the preference change listener to register
	 * @see #removePreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void addPreferenceChangeListenerToWorkspace(IPreferenceChangeListener listener);

	/**
	 * Unregisters the given listener from receiving notification of workspace-level changes of this preference. Calling
	 * this method multiple times with the same listener has no effect. The given listener argument must not be
	 * <code>null</code>.
	 * 
	 * @param listener
	 *            the preference change listener to remove
	 * @see #addPreferenceChangeListener(IEclipsePreferences.IPreferenceChangeListener)
	 * @see IEclipsePreferences.IPreferenceChangeListener
	 */
	void removePreferenceChangeListenerFromWorkspace(IPreferenceChangeListener listener);
}