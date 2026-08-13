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
package org.eclipse.sphinx.emf.mwe.dynamic.launching.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.mwe.dynamic.launching.ui.internal.messages.messages"; //$NON-NLS-1$

	public static String tab_workflow_name;

	public static String group_workflow_label;
	public static String group_model_label;
	public static String group_arguments_label;

	public static String pushButton_browse_label;
	public static String checkButton_autoSave_label;

	public static String label_argName;
	public static String label_argValue;

	public static String error_workflowNotSpecified;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
