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
package org.eclipse.sphinx.emf.validation.bridge.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.validation.bridge.util.messages"; //$NON-NLS-1$

	public static String errMissingAttributeOnExtensionPoint;
	public static String errOnExtensionIntro;
	public static String errNsURIRootPackageObject;
	public static String errOnExtensionModelNotRegistered;
	public static String errWrongClassifier;
	public static String errWrongValidatorAdapter;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
