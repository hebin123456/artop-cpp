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
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Abstract implementation of {@link IProjectWorkspacePreference}. Provides almost all logic required for being used
 * with a concrete preference
 * <p>
 * Defines two methods, {@link #toObject(String)} and {@link #toString(Object)}, to convert the preference values. These
 * methods should be overwritten by the subclasses.
 */
public abstract class AbstractProjectWorkspacePreference<T> implements IProjectWorkspacePreference<T> {

	/**
	 * Project preference corresponding to the current preference.
	 */
	protected AbstractProjectPreference<T> projectPreference;

	/**
	 * Workspace preference corresponding to the current preference.
	 */
	protected AbstractWorkspacePreference<T> workspacePreference;

	/**
	 * Constructor of the preference using the same qualifier is used at project level and at workspace level.
	 * 
	 * @param requiredProjectNatureId
	 *            the project nature that is required by the preference
	 * @param qualifier
	 *            the qualifier of the preference
	 * @param key
	 *            the key of the preference
	 * @param defaultValueAsString
	 *            the default value of the preference
	 * @see #AbstractProjectWorkspacePreference(String, String, String, String, String)
	 */
	public AbstractProjectWorkspacePreference(String requiredProjectNatureId, String qualifier, String key, String defaultValueAsString) {
		this(requiredProjectNatureId, qualifier, qualifier, key, defaultValueAsString);
	}

	/**
	 * Constructor of the preference.
	 * 
	 * @param requiredProjectNatureId
	 *            the project nature that is required by the preference
	 * @param projectLevelQualifier
	 *            the qualifier of the preference at project level
	 * @param workspaceLevelQualifier
	 *            the qualifier of the preference at workspace level
	 * @param key
	 *            the key of the preference
	 * @param defaultValueAsString
	 *            the default value of the preference
	 */
	public AbstractProjectWorkspacePreference(String requiredProjectNatureId, String projectLevelQualifier, String workspaceLevelQualifier,
			String key, String defaultValueAsString) {

		// Creates the corresponding project preference. The default value is null to ensure that it will not be used
		// instead of the workspace value when the project value does not exist.
		projectPreference = new AbstractProjectPreference<T>(requiredProjectNatureId, projectLevelQualifier, key, null) {
			@Override
			protected T toObject(IProject project, String valueAsString) {
				return AbstractProjectWorkspacePreference.this.toObject(project, valueAsString);
			}

			@Override
			protected String toString(IProject project, T valueAsObject) {
				return AbstractProjectWorkspacePreference.this.toString(project, valueAsObject);
			};
		};

		// Creates the corresponding workspace preference. The default value is stored in this instance.
		workspacePreference = new AbstractWorkspacePreference<T>(workspaceLevelQualifier, key, defaultValueAsString) {
			@Override
			protected T toObject(String valueAsString) {
				return AbstractProjectWorkspacePreference.this.toObject(null, valueAsString);
			}

			@Override
			protected String toString(T valueAsObject) {
				return AbstractProjectWorkspacePreference.this.toString(null, valueAsObject);
			};
		};
	}

	/**
	 * Returns the qualifier of the preference.
	 * <p>
	 * In the case where the project level qualifier and the workspace level qualifiers are different, this
	 * implementation returns the latter. This method may be overwritten by subclasses if necessary.
	 * 
	 * @see org.eclipse.sphinx.platform.preferences.IEclipsePreference#getQualifier()
	 */
	@Override
	public String getQualifier() {
		return workspacePreference.getQualifier();
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IEclipsePreference#getKey()
	 */
	@Override
	public String getKey() {
		return workspacePreference.getKey();
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IEclipsePreference#getDefaultValueAsString()
	 */
	@Override
	public String getDefaultValueAsString() {
		return workspacePreference.getDefaultValueAsString();
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectPreference#getRequiredProjectNatureId()
	 */
	@Override
	public String getRequiredProjectNatureId() {
		return projectPreference.getRequiredProjectNatureId();
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#get(org.eclipse.core.resources.IProject)
	 */
	@Override
	public T get(IProject project) {
		if (project != null) {
			try {
				if (project.isAccessible()) {
					String natureId = projectPreference.getRequiredProjectNatureId();
					if (natureId == null || project.hasNature(natureId)) {
						T valueAsObject = getFromProject(project);
						if (valueAsObject != null) {
							return valueAsObject;
						}
						return getFromWorkspaceForProject(project);
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
			}
			return null;
		} else {
			return getFromWorkspace();
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#getDefaultValueAsObject()
	 */
	@Override
	public T getDefaultValueAsObject() {
		return workspacePreference.getDefaultValueAsObject();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#getFromProject(org.eclipse.core.resources
	 * .IProject)
	 */
	@Override
	public T getFromProject(IProject project) {
		return projectPreference.get(project);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#setInProject(org.eclipse.core.resources.IProject
	 * , T)
	 */
	@Override
	public void setInProject(IProject project, T valueAsObject) {
		projectPreference.set(project, valueAsObject);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#setToDefaultInProject(org.eclipse.core.resources
	 * .IProject)
	 */
	@Override
	public void setToDefaultInProject(IProject project) {
		projectPreference.setToDefault(project);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#addPreferenceChangeListenerToProject(org.
	 * eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void addPreferenceChangeListenerToProject(IProject project, IPreferenceChangeListener listener) {
		projectPreference.addPreferenceChangeListener(project, listener);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#removePreferenceChangeListenerFromProject
	 * (org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void removePreferenceChangeListenerFromProject(IProject project, IPreferenceChangeListener listener) {
		projectPreference.removePreferenceChangeListener(project, listener);
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#getFromWorkspace()
	 */
	@Override
	public T getFromWorkspace() {
		return workspacePreference.get();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#getFromWorkspaceForProject(org.eclipse.core
	 * .resources.IProject)
	 */
	@Override
	public T getFromWorkspaceForProject(IProject project) {
		try {
			if (project.isAccessible()) {
				String natureId = projectPreference.getRequiredProjectNatureId();
				if (natureId == null || project.hasNature(natureId)) {
					IEclipsePreferences prefs = workspacePreference.getWorkspacePreferences();
					if (prefs != null) {
						String valueAsString = prefs.get(workspacePreference.getKey(), workspacePreference.getDefaultValueAsString());
						return toObject(project, valueAsString);
					} else {
						return toObject(project, workspacePreference.getDefaultValueAsString());
					}
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#setInWorkspace(T)
	 */
	@Override
	public void setInWorkspace(T valueAsObject) {
		workspacePreference.set(valueAsObject);
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#setToDefaultInWorkspace()
	 */
	@Override
	public void setToDefaultInWorkspace() {
		workspacePreference.setToDefault();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#addPreferenceChangeListenerToWorkspace(org
	 * .eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void addPreferenceChangeListenerToWorkspace(IPreferenceChangeListener listener) {
		workspacePreference.addPreferenceChangeListener(listener);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference#removePreferenceChangeListenerFromWorkspace
	 * (org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void removePreferenceChangeListenerFromWorkspace(IPreferenceChangeListener listener) {
		workspacePreference.removePreferenceChangeListener(listener);
	}

	/**
	 * Converts the <code>valueAsString</code>, that is a possible string value for the preference, into an object
	 * value. The conversion may depend on the nature of the {@link IProject project} that is provided.
	 * <p>
	 * By default this method returns the result of {@link #toObject(String)}. This method may be overwritten by the
	 * subclasses.
	 * 
	 * @param project
	 *            the project the preference depends on
	 * @param valueAsString
	 *            string value to be converted
	 * @return converted object value
	 */
	protected T toObject(IProject project, String valueAsString) {
		return toObject(valueAsString);
	}

	/**
	 * Converts the <code>valueAsObject</code>, that is a possible object value for the preference, into a string value.
	 * The conversion may depend on the nature of the {@link IProject project} that is provided.
	 * <p>
	 * By default this method returns the result of {@link #toString(Object)}. This method may be overwritten by the
	 * subclasses.
	 * 
	 * @param project
	 *            the project the preference depends on
	 * @param valueAsObject
	 *            object value to be converted
	 * @return converted string value
	 */
	protected String toString(IProject project, T valueAsObject) {
		return toString(valueAsObject);
	}

	/**
	 * Converts the <code>valueAsString</code>, that is a possible string value for the preference, into an object
	 * value. This conversion do not depend on the nature of the project.
	 * <p>
	 * By default this method returns <code>null</code>. It has to be overwritten by the subclasses.
	 * 
	 * @param valueAsString
	 *            string value to be converted
	 * @return converted object value
	 */
	protected T toObject(String valueAsString) {
		return null;
	}

	/**
	 * Converts the <code>valueAsObject</code>, that is a possible object value for the preference, into a string value.
	 * This conversion do not depend on the nature of the project.
	 * <p>
	 * By default, calls the {@link Object#toString()} method of the object value to perform the conversion. It returns
	 * <code>null</code> if <code>valueAsObject</code> is <code>null</code>. This method may be overwritten by the
	 * subclasses.
	 * 
	 * @param project
	 *            the project the preference depends on
	 * @param valueAsObject
	 *            object value to be converted
	 * @return converted string value
	 */
	protected String toString(T valueAsObject) {
		return valueAsObject != null ? valueAsObject.toString() : null;
	}
}
