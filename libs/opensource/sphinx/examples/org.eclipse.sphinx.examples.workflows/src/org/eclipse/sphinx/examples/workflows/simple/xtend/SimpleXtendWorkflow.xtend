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
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.workflows.simple.xtend

import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent2
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.emf.mwe2.runtime.workflow.Workflow
import org.eclipse.sphinx.examples.workflows.lib.ExampleWorkflowHelper
import org.eclipse.sphinx.examples.workflows.simple.java.SimpleJavaWorkflowComponent

import static extension org.eclipse.sphinx.examples.workflows.lib.ModelWorkflowExtensions.*

class SimpleXtendWorkflow extends Workflow {

	new() {
		// Add workflow components to be executed
		children += new SimpleJavaWorkflowComponent
		children += new SimpleXtendWorkflowComponent
	}
	
	override preInvoke() {
		println("Running simple Xtend-based workflow")
		super.preInvoke()
	}	
}

class SimpleXtendWorkflowComponent extends AbstractWorkflowComponent2 {

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		println("Executing simple Xtend-based workflow component")

		println("Arguments: " + ctx.argumentsSlot)

		println("Using some class from another project: " + ExampleWorkflowHelper)
		val helper = new ExampleWorkflowHelper()
		helper.doSomething

		println("Done!")
	}
}