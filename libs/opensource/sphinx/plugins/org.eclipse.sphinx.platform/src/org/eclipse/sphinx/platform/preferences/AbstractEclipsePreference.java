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

import org.eclipse.core.runtime.Assert;

/**
 * Abstract implementation of {@link IEclipsePreference}.
 */
public abstract class AbstractEclipsePreference<T> implements IEclipsePreference<T> {

	/**
	 * Qualifier of the preference.
	 */
	protected final String qualifier;

	/**
	 * Key of the preference.
	 */
	protected final String key;

	/**
	 * Default string value for the preference.
	 */
	protected final String defaultValueAsString;

	/**
	 * Constructor of the class. Records the qualifier and the key that identify the preference, and the default value.
	 * The qualifier and the key should not be null. The default value is provided as a string.
	 * 
	 * @param qualifier
	 *            the qualifier of the preference
	 * @param key
	 *            the key of the preference
	 * @param defaultValueAsString
	 *            the default value of the preference
	 */
	public AbstractEclipsePreference(String qualifier, String key, String defaultValueAsString) {
		Assert.isNotNull(qualifier);
		Assert.isNotNull(key);

		this.qualifier = qualifier;
		this.key = key;
		this.defaultValueAsString = defaultValueAsString;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IEclipsePreference#getQualifier()
	 */
	@Override
	public String getQualifier() {
		return qualifier;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IEclipsePreference#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}

	/*
	 * @see org.eclipse.sphinx.platform.preferences.IEclipsePreference#getDefaultValueAsString()
	 */
	@Override
	public String getDefaultValueAsString() {
		return defaultValueAsString;
	}
}
