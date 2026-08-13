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
package org.eclipse.sphinx.platform.messages;

import org.eclipse.osgi.util.NLS;

public class PlatformMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.platform.messages.PlatformMessages"; //$NON-NLS-1$

	/* --------------------------------------------------------------------- */
	/*
	 * Message category for arguments in methods.
	 */

	public static String arg_mustBeInstanceOf;
	public static String arg_mustNotBeBlank;

	/**
	 * Example of use:
	 * 
	 * <pre>
	 * if (arg == null) {
	 * 	String m = NLS.bind(PlatformMessages.arg_mustNotBeNull, &quot;arg&quot;); //$NON-NLS-1$
	 * 	throw new IllegalArgumentException(m);
	 * }
	 * </pre>
	 */
	public static String arg_mustNotBeNull;

	/* --------------------------------------------------------------------- */
	/*
	 * Message category for 'ERROR' severity.
	 */

	/**
	 * Example of message that has a severity <em>ERROR</em> in a status.
	 */
	public static String error_example;

	public static String error_caseNotYetSupported;

	public static String error_exceptionWhenInvokingPlugin;
	public static String error_exceptionWhenInvokingUnknownPlugin;

	public static String error_messageDialogTitle;
	public static String error_methodResultMustNotBeNull;
	public static String error_mustBeInstanceOf;
	public static String error_mustNotBeEmpty;
	public static String error_mustNotBeInstanceOf;
	public static String error_mustNotBeNull;

	public static String error_openingStream;
	public static String error_projectMustExist;
	public static String error_unexpectedArrayLength;
	public static String error_unexpectedAttributeImplementationInContribution;
	public static String error_unexpectedInstanceOf;
	public static String error_unexpectedListSize;
	public static String error_unknown;
	public static String error_cantBeParsedToInt;
	public static String error_cantBeParsedToFloat;
	public static String error_cantBeConvertedToBinary;
	public static String error_cantBeConvertedToOctal;
	public static String error_cantBeConvertedToDecimal;
	public static String error_cantBeConvertedToHex;

	/* --------------------------------------------------------------------- */
	/*
	 * Message category for 'INFO' severity.
	 */

	/**
	 * Example of message that has a severity <em>INFO</em> in a status.
	 */
	public static String info_example;

	public static String info_messageDialogTitle;
	public static String info_readExtensionPointContributions;
	public static String info_unknown;
	public static String infos_whenInvokingPlugin;

	/* --------------------------------------------------------------------- */
	/*
	 * Message category for 'OK' severity.
	 */

	/**
	 * Example of message that has a severity <em>OK</em> in a status.
	 */
	public static String ok_example;

	/* --------------------------------------------------------------------- */
	/*
	 * Category for problems whose severity is not yet known.
	 */

	/**
	 * Example of message that whose severity is not yet known.
	 */
	public static String problem_example;

	public static String problem_unknown;
	public static String problem_messageDialogTitle;

	public static String problem_whenInvokingPlugin;
	public static String problem_whenInvokingUnknownPlugin;

	/* --------------------------------------------------------------------- */
	/*
	 * Message category for 'WARNING' severity.
	 */

	/**
	 * Example of message that has a severity <em>WARNING</em> in a status.
	 */
	public static String warning_example;

	public static String warning_modelNotLoaded;
	public static String warning_messageDialogTitle;
	public static String warning_unexpectedArrayLength;
	public static String warning_unknown;
	public static String warning_unresolvedProxyObject;

	/* --------------------------------------------------------------------- */
	/*
	 * Message category for 'CANCEL' severity.
	 */

	public static String cancel_couldNotPerformOperation;

	/* --------------------------------------------------------------------- */
	/*
	 * Message category for jobs.
	 */

	/**
	 * Example of message that represents a job name.
	 */
	public static String job_example;

	public static String job_creatingLinkFile;
	public static String job_inputChanged;
	public static String job_loadingResources;
	public static String job_savingModel;
	public static String job_updatingProblemMarkers;
	public static String job_updatingLabelDecoration;

	/* --------------------------------------------------------------------- */
	/*
	 * Miscellaneous messages.
	 */

	public static String pluginId_unknown;

	public static String message_none;

	/* --------------------------------------------------------------------- */

	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, PlatformMessages.class);
	}
}
