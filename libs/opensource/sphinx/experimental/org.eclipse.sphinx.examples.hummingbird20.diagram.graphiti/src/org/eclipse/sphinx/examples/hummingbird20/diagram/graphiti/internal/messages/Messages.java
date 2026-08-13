/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [392424] Migrate Sphinx integration of Graphiti to Graphiti 0.9.x
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.internal.messages;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.internal.messages.messages"; //$NON-NLS-1$

	public static String Hummingbird20DiagramContainerWizardPage_PageDescription;
	public static String Hummingbird20DiagramRootWizardPage_PageDescription;
	public static String Hummingbird20DiagramRootWizardPage_NoRootSelected;
	public static String Hummingbird20DiagramRootWizardPage_PlatformIsExpected;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
