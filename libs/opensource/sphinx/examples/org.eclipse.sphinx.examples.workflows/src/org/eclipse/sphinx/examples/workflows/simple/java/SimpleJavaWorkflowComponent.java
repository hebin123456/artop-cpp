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
package org.eclipse.sphinx.examples.workflows.simple.java;

import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent2;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.sphinx.examples.workflows.lib.ExampleWorkflowHelper;
import org.eclipse.sphinx.examples.workflows.lib.ModelWorkflowExtensions;

@SuppressWarnings("nls")
public class SimpleJavaWorkflowComponent extends AbstractWorkflowComponent2 {

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		System.out.println("Executing simple Java-based workflow component");

		System.out.println("Arguments: " + ModelWorkflowExtensions.getArgumentsSlot(ctx));

		System.out.println("Using some class from another project: " + ExampleWorkflowHelper.class);
		ExampleWorkflowHelper helper = new ExampleWorkflowHelper();
		helper.doSomething();

		System.out.println("Done!");
	}
}
