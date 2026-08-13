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
package org.eclipse.sphinx.examples.transform.xtend.ui;

import org.eclipse.sphinx.examples.transform.xtend.ui.internal.Activator;
import org.eclipse.sphinx.examples.transform.xtend.ui.internal.messages.Messages;

public interface ISphinxTransformExampleMenuConstants {

	// TODO Add this as sub menu to Sphinx examples menu or move it to a non example plug-in

	/**
	 * Identifier of the Generate sub menu.
	 */
	public static final String MENU_TRANSFORM_ID = Activator.getPlugin().getSymbolicName() + ".menus.tranform"; //$NON-NLS-1$

	/**
	 * Label of the Generate sub menu.
	 */
	public static final String MENU_TRANSFORM_LABEL = Messages.menu_transform;
}
