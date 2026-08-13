/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [473262] Check Framework Robustness: Gracefully handle null feature
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.check.internal.messages.Messages"; //$NON-NLS-1$

	public static String operation_validate_label;

	public static String task_validating;

	public static String job_handlingDiagnostics_label;

	public static String markerLocation_unknownObject;
	public static String markerLocation_unknownFeature;
	public static String markerMessage_unknownProblem;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
