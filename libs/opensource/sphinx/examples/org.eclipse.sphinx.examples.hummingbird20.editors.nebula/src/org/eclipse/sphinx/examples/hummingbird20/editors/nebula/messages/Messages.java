/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.editors.nebula.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.examples.hummingbird20.editors.nebula.messages.messages"; //$NON-NLS-1$

	public static String title_GenericParameterValues_OverviewPage;
	public static String title_EditableParameterValues_OverviewPage;
	public static String title_ParameterValues_Section;
	public static String desc_ParameterValues_Section;

	public static String xcol_ColumnName_Extra_INFO;
	public static String xcol_ColumnDesc_Extra_INFO;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
