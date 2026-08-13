/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     itemis - [436112] Rework XML Persistence Mapping & XSD generation menu items to make them less prominent in the Eclipse UI
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.platform.internal.messages.messages"; //$NON-NLS-1$

	public static String job_persistingContentTypeIdProperties;
	public static String task_persistingContentTypeIdPropertiesFor;

	public static String job_performingGarbageCollection;
	public static String perfLog_$0$1runningTimeAndUserRunningTime;
	public static String perfLog_$0runningTime;
	public static String perfLog_$0$1$2contextInfos;
	public static String perfLog_$0runningTimeExceedTimeout;
	public static String perfLog_$0runningTimeZero;
	public static String perfLog_performanceStatsNotActivated;
	public static String perfLog_$0$1$2$3statEventToStringSimple;
	public static String perfLog_$0$1$2$3statEventToStringWithRunCount;

	public static String warning_resourceIsOutOfSync;

	public static String operation_unnamed_label;

	public static String cliHelp_useHelpOptionForMoreInformation;
	public static String cliHelp_usagePrefix;
	public static String cliHelp_optionsHeader;
	public static String cliHelp_options;

	public static String cliOption_help;
	public static String cliOption_workspaceLocation_argName;

	public static String error_NoSuchVertex;
	public static String error_LoopsNotAllowed;

	public static String toString_Edge;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
