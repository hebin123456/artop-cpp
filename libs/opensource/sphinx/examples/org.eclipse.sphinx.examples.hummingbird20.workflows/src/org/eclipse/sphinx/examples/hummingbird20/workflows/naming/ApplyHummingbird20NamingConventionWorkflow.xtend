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
package org.eclipse.sphinx.examples.hummingbird20.workflows.naming

import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.sphinx.emf.mwe.dynamic.WorkspaceWorkflow
import org.eclipse.sphinx.emf.mwe.dynamic.components.AbstractModelWorkflowComponent
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component
import org.eclipse.sphinx.examples.hummingbird20.typemodel.ComponentType
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface

import static extension org.eclipse.sphinx.examples.workflows.lib.ModelWorkflowExtensions.*

class ApplyHummingbird20NamingConventionWorkflow extends WorkspaceWorkflow {

	new(){
		children += new ApplyHummingbird20NamingConventionWorkflowComponent
	}
}

class ApplyHummingbird20NamingConventionWorkflowComponent extends AbstractModelWorkflowComponent {

	new() {
		// Will modify the model in the slot
		super(true)
	}

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		println("Executing Apply Hummingbird 2.0 Naming Convention workflow component")
		val modelObjects = ctx.modelSlot

		for (modelObject : modelObjects) {
			val eAllContents = modelObject.eAllContents
			while (eAllContents.hasNext) {
				val element = eAllContents.next
				if (element instanceof Interface) {
					if (!element.name.startsWith("I") || element.name.substring(1, 2).matches("[a-z]"))
						element.name = "I" + element.name
				}
				if (element instanceof ComponentType) {
					if (!element.name.endsWith("Impl")) {
						element.name = element.name + "Impl"
					}
				}
				if (element instanceof Component) {
					if (!element.name.endsWith("Inst")) {
						element.name = element.name + "Inst"
					}
				}
			}
		}

		println("Done!")
	}
}