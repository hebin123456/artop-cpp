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
package org.eclipse.sphinx.examples.common.ui;

import org.eclipse.sphinx.examples.common.ui.internal.messages.Messages;

/**
 * Defines constants for Sphinx example menus and groups.
 * 
 * @since 0.7.0
 */
public interface ISphinxExampleMenuConstants {

	/**
	 * Identifier of the Sphinx Examples sub menu.
	 */
	String MENU_SPHINX_EXAMPLES_ID = "sphinx.examples.menu";//$NON-NLS-1$

	/**
	 * Label of the Sphinx Examples sub menu.
	 */
	String MENU_SPHINX_EXAMPLES_LABEL = Messages.menu_sphinxExamples_label;

	/**
	 * Identifier of the Sphinx Examples menu item group.
	 */
	String GROUP_SPHINX_EXAMPLES = "sphinx.examples.group";//$NON-NLS-1$
}
