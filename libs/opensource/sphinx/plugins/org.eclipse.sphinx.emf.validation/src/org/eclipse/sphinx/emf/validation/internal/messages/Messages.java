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
package org.eclipse.sphinx.emf.validation.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String job_HandlingDiagnostics;
	public static String task_subtask_validatingFile;
	public static String task_subtask_validatingObject;
	public static String task_progressBar_InitialMsg;
	public static String task_progressBar_ErrWarnInfo;

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.validation.internal.messages.Messages"; //$NON-NLS-1$

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
