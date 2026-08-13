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
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.workflows.model

import org.eclipse.emf.mwe.core.WorkflowContext
import org.eclipse.emf.mwe.core.issues.Issues
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor
import org.eclipse.sphinx.emf.mwe.dynamic.WorkspaceWorkflow
import org.eclipse.sphinx.emf.mwe.dynamic.components.AbstractModelWorkflowComponent
import org.eclipse.sphinx.emf.util.EcoreResourceUtil

import static extension org.eclipse.sphinx.examples.workflows.lib.ModelWorkflowExtensions.*

class PrintModelContentWorkflow extends WorkspaceWorkflow {

	new(){
		children += new PrintModelContentWorkflowComponent
	}
}

class PrintModelContentWorkflowComponent extends AbstractModelWorkflowComponent {

	override protected invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		println("Executing Print Model Content workflow component")
		val modelObjects = ctx.modelSlot

		for (modelObject : modelObjects) {
			println("=> " + modelObject.label + " ["+ EcoreResourceUtil.getURI(modelObject) + "]")

			val eAllContents = modelObject.eAllContents
			while (eAllContents.hasNext) {
				val element = eAllContents.next
				println("=> " + element.label + " ["+ EcoreResourceUtil.getURI(element) + "]")
			}
		}

		println("Done!")
	}
}
