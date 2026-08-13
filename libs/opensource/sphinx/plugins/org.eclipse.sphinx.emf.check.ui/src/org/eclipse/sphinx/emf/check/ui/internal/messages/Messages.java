/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.check.ui.internal.messages.Messages"; //$NON-NLS-1$

	public static String menu_validation_label;
	public static String operation_validate_label;

	public static String text_enableIntrinsicModelIntegrityConstraints;

	public static String other_category_label;
	public static String other_category_desc;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
