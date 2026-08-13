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
package org.eclipse.sphinx.platform.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.platform.ui.internal.messages.messages"; //$NON-NLS-1$

	public static String error_whileRunningOperation;
	public static String title_section_error;
	public static String title_documentation;
	public static String desc_model_object_selection;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
