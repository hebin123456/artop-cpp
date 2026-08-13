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
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Abstract implementation of {@link IWorkspacePreference}. Provides almost all logic required for being used with a
 * concrete preference.
 * <p>
 * Defines two methods, {@link #toObject(String)} and {@link #toString(Object)}, to convert the preference values. These
 * methods should be overwritten by the subclasses.
 */
public abstract class AbstractWorkspacePreference<T> extends AbstractEclipsePreference<T> implements IWorkspacePreference<T> {

	/**
	 * Constructor of the preference.
	 * 
	 * @param qualifier
	 *            the qualifier of the preference
	 * @param key
	 *            the key of the preference
	 * @param defaultValueAsString
	 *            the default value of the preference
	 * @see AbstractEclipsePreference#AbstractEclipsePreference(String, String, String)
	 */
	public AbstractWorkspacePreference(String qualifier, String key, String defaultValueAsString) {
		super(qualifier, key, defaultValueAsString);
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IWorkspacePreference#get()
	 */
	@Override
	public T get() {
		try {
			IEclipsePreferences prefs = getWorkspacePreferences();
			if (prefs != null) {
				String valueAsString = prefs.get(key, defaultValueAsString);
				return toObject(valueAsString);
			} else {
				return toObject(defaultValueAsString);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IWorkspacePreference#set(java.lang.Object)
	 */
	@Override
	public void set(T valueAsObject) {
		try {
			String valueAsString = toString(valueAsObject);
			IEclipsePreferences prefs = getWorkspacePreferences();
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
		} catch (Exception ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IWorkspacePreference#getDefaultValueAsObject()
	 */
	@Override
	public T getDefaultValueAsObject() {
		return toObject(defaultValueAsString);
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IWorkspacePreference#setToDefault()
	 */
	@Override
	public void setToDefault() {
		IEclipsePreferences prefs = getWorkspacePreferences();
		if (prefs != null) {
			prefs.remove(key);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IWorkspacePreference#addPreferenceChangeListener(org.eclipse.core.runtime
	 * .preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
		IEclipsePreferences prefs = getWorkspacePreferences();
		if (prefs != null) {
			prefs.addPreferenceChangeListener(listener);
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.platform.preferences.IWorkspacePreference#removePreferenceChangeListener(org.eclipse.core.
	 * runtime.preferences.IEclipsePreferences.IPreferenceChangeListener)
	 */
	@Override
	public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
		IEclipsePreferences prefs = getWorkspacePreferences();
		if (prefs != null) {
			prefs.removePreferenceChangeListener(listener);
		}
	}

	/**
	 * Get the Eclipse preferences associated with the workspace.
	 * 
	 * @return the node containing the preferences
	 * @see org.eclipse.core.runtime.preferences.InstanceScope#getNode(String)
	 */
	protected IEclipsePreferences getWorkspacePreferences() {
		return InstanceScope.INSTANCE.getNode(qualifier);
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
	protected T toObject(String valueAsString) {
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
	protected String toString(T valueAsObject) {
		return valueAsObject != null ? valueAsObject.toString() : null;
	}
}
