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
package org.eclipse.sphinx.examples.workflows.longrunning

import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.sphinx.emf.mwe.dynamic.WorkspaceWorkflow
import org.eclipse.sphinx.emf.mwe.dynamic.components.AbstractWorkspaceWorkflowComponent

class LongRunningWorkflow extends WorkspaceWorkflow {

	new(){
		children += new LongRunningWorkflowComponent
	}
}

class LongRunningWorkflowComponent extends AbstractWorkspaceWorkflowComponent {

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		println("Executing long running workflow component")

		try {
			monitor.beginTask("Some long running task", 10);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			for (i : 1 .. 10) {
				Thread.sleep(1000);
				println(i  + "/10 done")

				monitor.worked(1);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		} finally {
			monitor.done
		}
	}
}