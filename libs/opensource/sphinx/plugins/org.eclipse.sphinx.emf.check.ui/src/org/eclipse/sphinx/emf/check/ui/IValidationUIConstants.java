/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.check.ui;

import org.eclipse.sphinx.emf.check.ui.internal.Activator;

public interface IValidationUIConstants {

	/**
	 * Identifier of the Validate sub menu.
	 */
	public static final String MENU_VALIDATE_ID = Activator.getDefault().getBundle().getSymbolicName() + ".menus.validate"; //$NON-NLS-1$

	/**
	 * Label of the Validate sub menu.
	 */
	public static final String MENU_VALIDATE_LABEL = "Validate"; //$NON-NLS-1$

	/**
	 * Label of the Validate sub menu.
	 */
	public static final String SUBMENU_VALIDATE_LABEL = "Check-based Validation"; //$NON-NLS-1$

	/**
	 * Check Validation problem marker generator
	 */
	public static final String VALIDATION_CHECK_MARKER_GENERATOR = "org.eclipse.sphinx.emf.check.ui.markerGenerator"; //$NON-NLS-1$

	public static final String CONSTRAINT_CATEGORIES_SELECTION_TITLE = "Constraint categories selection"; //$NON-NLS-1$

	public static final String CONSTRAINT_CATEGORIES_SELECTION_MESSAGE = "Select constraint categories"; //$NON-NLS-1$

}
