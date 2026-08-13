/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Renamed ProjectStatisticsAction to GenerateModelStatisticsReportAction
 *     itemis - [450882] Enable navigation to ancestor tree items in Model Explorer kind of model views
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.actions.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String act_GenerateModelStatisticsReport_label;
	public static String act_GenerateModelStatisticsReport_result_ProjectName;
	public static String act_GenerateModelStatisticsReport_result_Summary;
	public static String act_GenerateModelStatisticsReport_result_columLabel_Quantity;
	public static String act_GenerateModelStatisticsReport_result_columLabel_Type;
	public static String act_GenerateModelStatisticsReport_result_eof;
	public static String dlg_GenerateModelStatisticsReport_fileAlreadyExists_title;
	public static String dlg_GenerateModelStatisticsReport_fileAlreadyExists_desc;

	public static String act_WalkUpAncestors_label;

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.examples.actions.internal.messages.messages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
