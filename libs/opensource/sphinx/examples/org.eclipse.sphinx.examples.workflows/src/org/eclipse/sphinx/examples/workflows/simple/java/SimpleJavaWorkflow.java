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
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.workflows.simple.java;

import org.eclipse.emf.mwe2.runtime.workflow.Workflow;
import org.eclipse.sphinx.examples.workflows.simple.xtend.SimpleXtendWorkflowComponent;

@SuppressWarnings("nls")
public class SimpleJavaWorkflow extends Workflow {

	public SimpleJavaWorkflow() {
		// Add workflow components to be executed
		getChildren().add(new SimpleJavaWorkflowComponent());
		getChildren().add(new SimpleXtendWorkflowComponent());
	}

	/*
	 * @see org.eclipse.emf.mwe2.runtime.workflow.AbstractCompositeWorkflowComponent#preInvoke()
	 */
	@Override
	public void preInvoke() {
		System.out.println("Running simple Java-based workflow");
		super.preInvoke();
	}
}
