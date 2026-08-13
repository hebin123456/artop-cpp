/**
 * <copyright>
 *
 * Copyright (c) 2014-2016 itemis and others.
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
package org.eclipse.sphinx.emf.mwe.dynamic;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe.core.lib.WorkflowComponentWithModelSlot;
import org.eclipse.emf.mwe2.runtime.workflow.Workflow;

public interface IModelWorkflowSlots {

	/**
	 * Name of slot for passing a {@link EObject model element} to {@link Workflow workflow}s and
	 * {@link WorkflowComponentWithModelSlot workflow component}s.
	 */
	String MODEL_SLOT_NAME = "model"; //$NON-NLS-1$

	/**
	 * Name of slot for passing arguments to {@link Workflow workflow}s and {@link WorkflowComponentWithModelSlot
	 * workflow component}s.
	 */
	String ARGUMENTS_SLOT_NAME = "arguments"; //$NON-NLS-1$

	/**
	 * Name of slot for passing a {@link IProgressMonitor progress monitor} to {@link Workflow workflow}s and
	 * {@link WorkflowComponentWithModelSlot workflow component}s.
	 */
	String PROGRESS_MONTIOR_SLOT_NAME = "progressMonitor"; //$NON-NLS-1$
}