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
package org.eclipse.sphinx.emf.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.ui.internal.messages.Messages"; //$NON-NLS-1$

	public static String action_openInEditor_label;
	public static String action_rename_label;
	public static String action_move_label;

	public static String action_resetValue_label;
	public static String action_resetValue_toolTip;

	public static String action_locateValue_label;
	public static String action_locateValue_toolTip;

	public static String dialog_rename_title;
	public static String dialog_rename_message;

	public static String label_selectedNothing;
	public static String label_multipleItemsSelected;

	public static String menu_openWith_label;

	public static String label_editProxyURI;
	public static String message_proxyURIMustNotReferenceAnAlreadyExistingListElement;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
