/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.platform.ui.preferences.messages;

import org.eclipse.osgi.util.NLS;

public class AbstractPreferenceAndPropertyMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.platform.ui.preferences.messages.AbstractPreferenceAndPropertyMessages"; //$NON-NLS-1$

	public static String AbstractPreferenceAndPropertyPage_enableProjectSpecificSettings;
	public static String AbstractPreferenceAndPropertyPage_configureWorkspaceSettings;
	public static String AbstractPreferenceAndPropertyPage_configureProjectSpecificSettings;

	static {
		/* Load message values from bundle file. */
		NLS.initializeMessages(BUNDLE_NAME, AbstractPreferenceAndPropertyMessages.class);
	}
}
