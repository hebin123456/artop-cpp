/**
 * <copyright>
 *
 * Copyright (c) 2008-2017 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [511105] Ensure Mars compatibility & fix compilation errors under Oxygen due to internal API usage
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.validation.util.messages"; //$NON-NLS-1$

	public static String __EValidatorRegstering_NoSuchPackage;
	public static String warningNoSuchMarker;
	public static String warningProblemWithMarkerOperationOnResource;
	public static String noMessageAvailableForThisMarker;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
