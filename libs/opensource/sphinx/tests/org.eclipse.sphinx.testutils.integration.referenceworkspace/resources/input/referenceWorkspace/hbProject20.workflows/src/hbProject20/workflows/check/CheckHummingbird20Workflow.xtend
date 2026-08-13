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
package hbProject20.workflows.check

import org.eclipse.sphinx.emf.check.workflows.AbstractCheckValidationWorkflowComponent
import org.eclipse.sphinx.emf.mwe.dynamic.WorkspaceWorkflow

class CheckHummingbird20Workflow extends WorkspaceWorkflow {
	  
	new(){
		children += new CheckHummingbird20WorkflowComponent
	} 
}

class CheckHummingbird20WorkflowComponent extends AbstractCheckValidationWorkflowComponent {

	new() { 
		categories+="Category1"
		categories+="Category2"
	}
}