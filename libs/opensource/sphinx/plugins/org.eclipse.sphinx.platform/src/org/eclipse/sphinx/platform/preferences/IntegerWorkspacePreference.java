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
 * {@link IWorkspacePreference} implementation for Integer-typed preferences.
 */
public class IntegerWorkspacePreference extends AbstractWorkspacePreference<Integer> {

	public IntegerWorkspacePreference(String qualifier, String key, Integer defaultValue) {
		super(qualifier, key, Integer.toString(defaultValue));
	}

	@Override
	protected Integer toObject(String valueAsString) {
		return Integer.valueOf(valueAsString);
	}
}
