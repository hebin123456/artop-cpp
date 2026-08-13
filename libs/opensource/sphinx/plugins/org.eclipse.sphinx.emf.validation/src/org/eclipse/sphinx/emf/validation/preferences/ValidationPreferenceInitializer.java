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
package org.eclipse.sphinx.emf.validation.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.sphinx.emf.validation.Activator;

public class ValidationPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultNode = DefaultScope.INSTANCE.getNode(Activator.getDefault().getBundle().getSymbolicName());
		defaultNode.putBoolean(IValidationPreferences.PREF_ENABLE_AUTOMATIC_VALIDATION,
				IValidationPreferences.PREF_ENABLE_AUTOMATIC_VALIDATION_DEFAULT);
		defaultNode.putBoolean(IValidationPreferences.PREF_ENABLE_EMF_DEFAULT_RULES, IValidationPreferences.PREF_ENABLE_EMF_DEFAULT_RULES_DEFAULT);
		defaultNode.putInt(IValidationPreferences.PREF_MAX_NUMBER_OF_ERRORS, IValidationPreferences.PREF_MAX_NUMBER_OF_ERRORS_DEFAULT);
	}
}
