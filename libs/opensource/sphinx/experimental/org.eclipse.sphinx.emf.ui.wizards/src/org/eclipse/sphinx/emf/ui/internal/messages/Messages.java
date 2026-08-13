/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     itemis - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.ui.internal.messages.messages"; //$NON-NLS-1$

	public static String _UI_ModelEditorFilenameDefaultBase;
	public static String _UI_DefaultModelEditorFilenameExtensions;
	public static String _UI_Wizard_label;
	public static String _UI_ModelObject;
	public static String _UI_XMLEncoding;
	public static String _UI_XMLEncodingChoices;
	public static String _UI_ModelWizard_label;
	public static String _UI_ModelWizard_description;
	public static String _UI_Wizard_initial_object_description;
	public static String _WARN_FilenameExtension;
	public static String _WARN_FilenameExtensions;
	public static String _UI_OpenEditorError_label;
	public static String _UI_ModelWizardName;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
