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
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Abstract implementation of {@link IProjectPreference}. Provides almost all logic required for being used with a
 * concrete preference.
 * <p>
 * Defines two methods, {@link #toObject(String)} and {@link #toString(Object)}, to convert the preference values. These
 * methods should be overwritten by the subclasses.
 */
public abstract class AbstractProjectPreference<T> extends AbstractEclipsePreference<T> implements IProjectPreference<T> {

	/**
	 * Project nature that is required by the preference.
	 */
	protected String requiredProjectNatureId;

	/**
	 * Constructor of the preference.
	 * 
	 * @param requiredProjectNatureId
	 *            the project nature that is required by the preference
	 * @param qualifier
	 *            the qualifier of the preference
	 * @param key
	 *            the key of the preference
	 * @param defaultValueAsString
	 *            the default value of the preference
	 * @see AbstractEclipsePreference#AbstractEclipsePreference(String, String, String)
	 */
	public AbstractProjectPreference(String requiredProjectNatureId, String qualifier, String key, String defaultValueAsString) {
		super(qualifier, key, defaultValueAsString);
		this.requiredProjectNatureId = requiredProjectNatureId;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectPreference#getRequiredProjectNature()
	 */
	@Override
	public String getRequiredProjectNatureId() {
		return requiredProjectNatureId;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectPreference#get(org.eclipse.core.resources.IProject)
	 */
	@Override
	public T get(IProject project) {
		Assert.isNotNull(project);

		try {
			if (project.isAccessible()) {
				if (requiredProjectNatureId == null || project.hasNature(requiredProjectNatureId)) {
					IEclipsePreferences prefs = getProjectPreferences(project);
					if (prefs != null) {
						String valueAsString = prefs.get(key, defaultValueAsString);
						return toObject(project, valueAsString);
					} else {
						return toObject(project, defaultValueAsString);
					}
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectPreference#set(org.eclipse.core.resources.IProject,
	 * java.lang.Object)
	 */
	@Override
	public void set(IProject project, T valueAsObject) {
		Assert.isNotNull(project);

		try {
			if (project.isAccessible()) {
				if (requiredProjectNatureId == null || project.hasNature(requiredProjectNatureId)) {
					String valueAsString = toString(project, valueAsObject);
					IEclipsePreferences prefs = getProjectPreferences(project);
					if (prefs != null) {
						if (valueAsString != null && !valueAsString.equals(defaultValueAsString)) {
							prefs.put(key, valueAsString);
						} else {
							prefs.remove(key);
						}
						try {
							prefs.flush();
						} catch (BackingStoreException ex) {
							// Ignore exception
						}
					}
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectPreference#getDefaultValueAsObject(org.eclipse.core.resources
	 * .IProject)
	 */
	@Override
	public T getDefaultValueAsObject(IProject project) {
		return toObject(project, defaultValueAsString);
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IProjectPreference#setToDefault(org.eclipse.core.resources.IProject)
	 */
	@Override
	public void setToDefault(IProject project) {
		IEclipsePreferences prefs = getProjectPreferences(project);
		if (prefs != null) {
			prefs.remove(key);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectPreference#addPreferenceChangeListener(org.eclipse.core.resources
	 * .IProject, org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void addPreferenceChangeListener(IProject project, IPreferenceChangeListener listener) {
		IEclipsePreferences prefs = getProjectPreferences(project);
		if (prefs != null) {
			prefs.addPreferenceChangeListener(listener);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IProjectPreference#removePreferenceChangeListener(org.eclipse.core.resources
	 * .IProject, org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void removePreferenceChangeListener(IProject project, IPreferenceChangeListener listener) {
		IEclipsePreferences prefs = getProjectPreferences(project);
		if (prefs != null) {
			prefs.removePreferenceChangeListener(listener);
		}
	}

	/**
	 * Get the Eclipse preferences associated with the {@link IProject project}. <code>project</code> should not be
	 * <code>null</code>.
	 * 
	 * @param project
	 *            the project from which the preferences are retrieved
	 * @return the node containing the preferences
	 * @see org.eclipse.core.resources.ProjectScope#getNode(String)
	 */
	protected IEclipsePreferences getProjectPreferences(IProject project) {
		Assert.isNotNull(project);

		ProjectScope projectScope = new ProjectScope(project);
		return projectScope.getNode(qualifier);
	}

	/**
	 * Converts the <code>valueAsString</code>, that is a possible string value for the preference, into an object
	 * value. The conversion may depend on the nature of the {@link IProject project} that is provided.
	 * <p>
	 * By default this method returns <code>null</code>. It has to be overwritten by the subclasses.
	 * 
	 * @param project
	 *            the project the preference depends on
	 * @param valueAsString
	 *            string value to be converted
	 * @return converted object value
	 */
	protected T toObject(IProject project, String valueAsString) {
		return null;
	}

	/**
	 * Converts the <code>valueAsObject</code>, that is a possible object value for the preference, into a string value.
	 * The conversion may depend on the nature of the {@link IProject project} that is provided.
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
	protected String toString(IProject project, T valueAsObject) {
		return valueAsObject != null ? valueAsObject.toString() : null;
	}
}
