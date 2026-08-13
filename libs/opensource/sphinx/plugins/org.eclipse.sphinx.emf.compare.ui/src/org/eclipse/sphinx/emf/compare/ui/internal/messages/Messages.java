/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [457704] Integrate EMF compare 3.x in Sphinx
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.compare.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.compare.ui.internal.messages.Messages"; //$NON-NLS-1$

	/*
	 * Compare Actions
	 */
	public static String action_compareWithEachOther;
	public static String action_compareWithEachOther_description;
	public static String action_mergeWithEachOther;
	public static String action_copyRightToLeft;
	public static String action_copyLeftToRight;

	/*
	 * Dialogs
	 */
	public static String dlg_mergeAuto_title;
	public static String dlg_mergeAuto_message;
	public static String dlg_mergeAuto_messageLeftProperties;
	public static String dlg_mergeAuto_messageRightProperties;
	public static String dlg_mergeAuto_buttonLabel_leftToRight;
	public static String dlg_mergeAuto_buttonLabel_rightToLeft;

	/*
	 * Errors
	 */
	public static String error_invalidEditorInput;
	public static String error_openEditorError;
	public static String error_noActiveWorkbenchPage;
	public static String error_notFound_editingDomain;
	public static String error_notFound_editingDomainFor;

	/*
	 * Warnings
	 */
	public static String warning_workbenchPartNull;
	public static String warning_workbenchPartInstanceofModelCompareEditor;
	public static String warning_inputModelComparisonInputNull;
	public static String warning_inputsNull;

	/*
	 * Compare With Each Other
	 */
	public static String twoWay_title;
	public static String twoWay_tooltip;
	public static String threeWay_title;
	public static String threeWay_tooltip;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
