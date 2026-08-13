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
package org.eclipse.sphinx.platform.ui.fields.messages;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class FieldsMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.platform.ui.fields.messages.FieldsMessages"; //$NON-NLS-1$

	// Dialogs messages

	public static String field_EmptyListItem;

	public static String field_ButtonLabel_Add;
	public static String field_ButtonLabel_AddWithDots;
	public static String field_ButtonLabel_DeselectAll;
	public static String field_ButtonLabel_Down;
	public static String field_ButtonLabel_Edit;
	public static String field_ButtonLabel_MoveDown;
	public static String field_ButtonLabel_MoveUp;
	public static String field_ButtonLabel_Properties;
	public static String field_ButtonLabel_Remove;
	public static String field_ButtonLabel_SelectAll;
	public static String field_ButtonLabel_Up;

	// Problems and errors

	public static String problem_Field_CannotBeCreated_UnexpectedLayout;

	public static String error_assert_LayoutNumberOfColumnsIsTooSmall;

	static {
		/* Load message values from bundle file. */
		NLS.initializeMessages(BUNDLE_NAME, FieldsMessages.class);
	}
}
