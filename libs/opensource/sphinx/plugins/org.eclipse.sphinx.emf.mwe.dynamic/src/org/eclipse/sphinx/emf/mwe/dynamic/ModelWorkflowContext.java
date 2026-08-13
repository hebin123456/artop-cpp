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
package org.eclipse.sphinx.emf.mwe.dynamic;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.mwe2.runtime.workflow.WorkflowContextImpl;

// TODO Provide "model" specialization of org.eclipse.emf.mwe.core.WorkflowContext and use it in AbstractWorkspaceWorkflowComponent.WorkspaceMwe2Bridge#invoke(IWorkflowContext)
public class ModelWorkflowContext extends WorkflowContextImpl {

	public ModelWorkflowContext(Object model, Map<String, Object> arguments, IProgressMonitor monitor) {
		put(IModelWorkflowSlots.MODEL_SLOT_NAME, model);
		put(IModelWorkflowSlots.ARGUMENTS_SLOT_NAME, arguments);
		put(IModelWorkflowSlots.PROGRESS_MONTIOR_SLOT_NAME, monitor);
	}

	@SuppressWarnings("unchecked")
	public List<EObject> getModel() {
		return (List<EObject>) get(IModelWorkflowSlots.MODEL_SLOT_NAME);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getArguments() {
		return (Map<String, Object>) get(IModelWorkflowSlots.ARGUMENTS_SLOT_NAME);
	}

	public IProgressMonitor getProgressMonitor() {
		return (IProgressMonitor) get(IModelWorkflowSlots.PROGRESS_MONTIOR_SLOT_NAME);
	}
}