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
package org.eclipse.sphinx.emf.mwe.dynamic.ui;

import org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.Activator;
import org.eclipse.sphinx.emf.mwe.dynamic.ui.internal.messages.Messages;

public interface IWorkflowRunnerMenuConstants {

	/**
	 * Identifier of the Run sub menu.
	 */
	public static final String MENU_RUN_WORKFLOW_ID = Activator.getPlugin().getSymbolicName() + ".menus.run"; //$NON-NLS-1$

	/**
	 * Label of the Run sub menu.
	 */
	public static final String MENU_RUN_WORKFLOW_LABEL = Messages.menu_runWorkflow_label;
}
