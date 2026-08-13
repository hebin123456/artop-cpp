/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.messages.messages"; //$NON-NLS-1$

	public static String menu_runWorkflow_label;
	public static String operation_runWorkflow_label;
	public static String action_runWorkflow_label;

	public static String dialog_workflowFileSelection_title;

	public static String dialog_workflowTypeSelection_title;
	public static String dialog_workflowTypeSelection_message;

	public static String workflowSelectionWizard_Title;

	public static String workflowSelectionWizardPage_description;
	public static String workflowSelectionWizardPage_workflowGroupTitle;
	public static String workflowSelectionWizardPage_title;
	public static String workflowSelectionWizardPage_browseWorkspaceButtonLabel;
	public static String workflowSelectionWizardPage_workflowClassErrorMessage;
	public static String workflowSelectionWizardPage_workflowClassLabel;
	public static String workflowSelectionWizardPage_workflowFileErrorMessage;
	public static String workflowSelectionWizardPage_workflowPathLabel;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
