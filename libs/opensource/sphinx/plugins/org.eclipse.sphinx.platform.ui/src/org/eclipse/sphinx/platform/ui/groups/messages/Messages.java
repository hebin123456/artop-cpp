/**
 * <copyright>
 *
 * Copyright (c) 2011-2016 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.ui.groups.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.platform.ui.groups.messages.messages"; //$NON-NLS-1$

	public static String button_add_label;
	public static String button_remove_label;

	public static String column_name_label;
	public static String column_value_label;

	public static String cell_name_default;
	public static String cell_value_default;

	public static String msg_fileSelectionError;
	public static String desc_fileSelection;
	public static String title_fileSelection;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
