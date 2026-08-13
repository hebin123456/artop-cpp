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
package org.eclipse.sphinx.testutils.messages;

import org.eclipse.osgi.util.NLS;

public class AbstractTestCaseMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.sphinx.testutils.messages.PerformanceStatsTestsMessages"; //$NON-NLS-1$

	/* --------------------------------------------------------------------- */
	/*
	 * Messages category for arguments in methods.
	 */

	public static String assert_RunningTimeUnderLowerBound;
	public static String assert_EventExceedTimeOut;
	public static String assert_EventInContextExceedTimeOut;
	static {
		// Load message values from bundle file
		NLS.initializeMessages(AbstractTestCaseMessages.BUNDLE_NAME, AbstractTestCaseMessages.class);
	}
}
