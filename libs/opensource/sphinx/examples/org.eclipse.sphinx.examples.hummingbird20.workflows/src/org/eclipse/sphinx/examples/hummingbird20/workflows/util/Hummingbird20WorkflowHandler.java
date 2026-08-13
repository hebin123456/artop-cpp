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
package org.eclipse.sphinx.examples.hummingbird20.workflows.util;

import org.eclipse.emf.mwe2.runtime.workflow.IWorkflow;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.sphinx.emf.mwe.dynamic.IWorkflowHandler;

public class Hummingbird20WorkflowHandler implements IWorkflowHandler {

	@Override
	public void preRun(IWorkflow workflow, IWorkflowContext context) {
		System.out.println("Pre-run handler for " + workflow.getClass().getSimpleName()); //$NON-NLS-1$
	}

	@Override
	public void postRun(IWorkflow workflow, IWorkflowContext context) {
		System.out.println("Post-run handler for " + workflow.getClass().getSimpleName()); //$NON-NLS-1$
	}
}
