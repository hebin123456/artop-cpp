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
package org.eclipse.sphinx.examples.workflows.simple.xtend

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.sphinx.emf.mwe.dynamic.WorkspaceWorkflow
import org.eclipse.sphinx.emf.mwe.dynamic.annotations.WorkflowParameter
import org.eclipse.sphinx.emf.mwe.dynamic.components.AbstractModelWorkflowComponent

import static extension org.eclipse.sphinx.examples.workflows.lib.ModelWorkflowExtensions.*

class SimpleXtendWorkflowWithParams extends WorkspaceWorkflow {

	@WorkflowParameter
	boolean boolWorkflowParam;

	@WorkflowParameter
	String strWorkflowParam;

	@WorkflowParameter
	URI uriWorkflowParam;

	@WorkflowParameter("workflowParamWithAlias")
	String otherWorkflowParam;

	new(){
		children += new SimpleXtendWorkflowComponentWithParams
	}

	override preInvoke() {
		println("Running simple Xtend-based workflow with parameters")
		
		// Print auto-initialized workflow parameters
		println("boolWorkflowParam = " + boolWorkflowParam)
		println("strWorkflowParam = " + strWorkflowParam)
		println("uriWorkflowParam = " + uriWorkflowParam)
		println("otherWorkflowParam (workflowParamWithAlias) = " + otherWorkflowParam)

		super.preInvoke()
	}
}

class SimpleXtendWorkflowComponentWithParams extends AbstractModelWorkflowComponent {

	@WorkflowParameter
	boolean boolComponentParam;

	@WorkflowParameter
	String strComponentParam;

	@WorkflowParameter
	URI uriComponentParam;

	@WorkflowParameter("componentParamWithAlias")
	String otherComponentParam;

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		println("Executing simple Xtend-based workflow component with parameters")

		// Print auto-initialized workflow component parameters
		println("boolComponentParam = " + boolComponentParam)
		println("strComponentParam = " + strComponentParam)
		println("uriComponentParam = " + uriComponentParam)
		println("otherComponentParam (componentParamWithAlias) = " + otherComponentParam)

		// Print workflow arguments passed in via IModelWorkflowSlots#ARGUMENTS_SLOT_NAME - those happen to 
		// include the arguments corresponding to workflow-level and component-level parameters and may also 
		// yield some informal extra arguments that do not relate to any formally declared workflow or 
		// component parameter
		println("Arguments: " + ctx.argumentsSlot)

		println("Done!")
	}
}
