/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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
package org.eclipse.sphinx.emf.mwe.dynamic.launching;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sphinx.emf.mwe.dynamic.launching.internal.Activator;

public interface IWorkflowLaunchConfigurationConstants {

	String ATTR_WORKFLOW = Activator.getPlugin().getSymbolicName() + ".WORKFLOW"; //$NON-NLS-1$
	String ATTR_WORKFLOW_DEFAULT = ""; //$NON-NLS-1$

	String ATTR_MODEL = Activator.getPlugin().getSymbolicName() + ".MODEL"; //$NON-NLS-1$
	String ATTR_MODEL_DEFAULT = ""; //$NON-NLS-1$

	String ATTR_AUTO_SAVE = Activator.getPlugin().getSymbolicName() + ".AUTO_SAVE"; //$NON-NLS-1$
	boolean ATTR_AUTO_SAVE_DEFAULT = true;

	String ATTR_ARGUMENTS = Activator.getPlugin().getSymbolicName() + ".ARGUMENTS"; //$NON-NLS-1$
	Map<String, String> ATTR_ARGUMENTS_DEFAULT = new HashMap<String, String>();
}
