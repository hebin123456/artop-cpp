/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [457704] Integrate EMF compare 3.x in Sphinx
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.compare.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.compare.internal.messages.Messages"; //$NON-NLS-1$

	/*
	 * Error messages
	 */
	public static String error_editingDomainNotFound;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
