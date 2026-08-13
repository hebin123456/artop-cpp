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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.search.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.search.ui.internal.messages.messages"; //$NON-NLS-1$

	public static String ModelSearchPage_expression_label;
	public static String ModelSearchPage_expression_caseSensitive;

	public static String ModelSearchQuery_status_ok_message;
	public static String ModelSearchQuery_label;
	public static String ModelSearchQuery_singularLabel;
	public static String ModelSearchQuery_pluralPattern;

	public static String ModelSearchResultViewPage_searching_label;
	public static String ModelSearchResultViewPage_update_job_name;
	public static String ModelSearchResultViewPage_flat_layout_label;
	public static String ModelSearchResultViewPage_flat_layout_tooltip;
	public static String ModelSearchResultViewPage_hierarchical_layout_label;
	public static String ModelSearchResultViewPage_hierarchical_layout_tooltip;
	public static String ModelSearchResultViewPage_show_match;
	public static String ModelSearchResultViewPage_error_no_editor;

	public static String RemoveMatchAction_label;
	public static String RemoveMatchAction_tooltip;

	public static String ScopePart_group_text;
	public static String ScopePart_workspaceScope_text;
	public static String ScopePart_selectedResourcesScope_text;
	public static String ScopePart_selectedModelsScope_text;
	public static String ScopePart_enclosingModelsScope_text;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
