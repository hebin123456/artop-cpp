/**
 * <copyright>
 *
 * Copyright (c) 2014-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.mwe.dynamic.internal.messages.messages"; //$NON-NLS-1$

	public static String error_workflowClassNotFound;
	public static String error_workflowFileDoesNotExist;
	public static String error_modelResourceDoesNotExist;

	public static String error_modelResourceContainsNoMatchingElement;
	public static String error_modelResourceCouldNotBeLoaded;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
