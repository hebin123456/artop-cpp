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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.emf.mwe2.runtime.workflow.Workflow;

public class WorkspaceWorkflow extends Workflow {

	/*
	 * @see
	 * org.eclipse.emf.mwe2.runtime.workflow.AbstractCompositeWorkflowComponent#invoke(org.eclipse.emf.mwe2.runtime.
	 * workflow.IWorkflowContext)
	 */
	@Override
	public void invoke(IWorkflowContext ctx) {
		Object monitorAsObject = ctx.get(IModelWorkflowSlots.PROGRESS_MONTIOR_SLOT_NAME);
		IProgressMonitor monitor = monitorAsObject instanceof IProgressMonitor ? (IProgressMonitor) monitorAsObject : new NullProgressMonitor();
		SubMonitor progress = SubMonitor.convert(monitor, getChildren().size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		for (IWorkflowComponent component : getChildren()) {
			ctx.put(IModelWorkflowSlots.PROGRESS_MONTIOR_SLOT_NAME, progress.newChild(1));

			component.invoke(ctx);

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	/*
	 * Overridden to make this method public.
	 * @see org.eclipse.emf.mwe2.runtime.workflow.AbstractCompositeWorkflowComponent#getChildren()
	 */
	@Override
	public List<IWorkflowComponent> getChildren() {
		return super.getChildren();
	}
}
