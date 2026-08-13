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
package org.eclipse.sphinx.examples.validation.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.examples.validation.ui.internal.messages.messages"; //$NON-NLS-1$

	public static String action_validateSelectedCategories_label;

	public static String menu_validation_label;

	public static String dialog_SelectConstraintCategories_title;
	public static String dialog_SelectConstraintCategories_description;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
