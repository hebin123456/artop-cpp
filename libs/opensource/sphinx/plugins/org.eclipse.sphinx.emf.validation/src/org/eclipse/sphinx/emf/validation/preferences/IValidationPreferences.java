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

/**
 * Validation framework preferences.
 */
public interface IValidationPreferences {

	public final String PREF_ENABLE_AUTOMATIC_VALIDATION = "automatic_validation_enablement"; //$NON-NLS-1$
	public final boolean PREF_ENABLE_AUTOMATIC_VALIDATION_DEFAULT = Boolean.FALSE;

	public final String PREF_ENABLE_EMF_DEFAULT_RULES = "emf_rule_enablement"; //$NON-NLS-1$
	public final boolean PREF_ENABLE_EMF_DEFAULT_RULES_DEFAULT = Boolean.FALSE;

	public final String PREF_MAX_NUMBER_OF_ERRORS = "pref_max_number_of_errors"; //$NON-NLS-1$
	public final int PREF_MAX_NUMBER_OF_ERRORS_DEFAULT = 10000;
}
