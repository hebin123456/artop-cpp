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
package org.eclipse.sphinx.emf.validation.ui.util;

import org.eclipse.osgi.util.NLS;

//TODO Move content of the class to internal Messages class.
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.validation.ui.util.messages"; //$NON-NLS-1$

	public static String _UI_automaticValidation_groupText;
	public static String _UI_enableDisableAutomaticValidationPreferencesMsg;

	public static String _UI_EMFConstraintsGroupText;
	public static String _UI_EMFConstraintsEnabledPreferencesMsg;

	public static String _UI_ProblemIndicationGroupText;
	public static String _UI_ProblemIndicationFieldLabelText;

	public static String _UI_subValidationMonitorIntro;

	public static String _UI_Workbench_showIn;

	public static String _UI_progressBar_InitialMsg;
	public static String _UI_progressBarMulti_ErrWarnInfo;

	public static String _Job_HandleDiagnostic;
	public static String _Job_Clean_Markers;

	public static String _UI_Clean_menu_item;
	public static String _UI_Clean_simple_description;

	public static String _UI_Validate_menu_item;
	public static String _UI_Validate_simple_description;

	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
