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
 *     itemis - [506671] Add support for specifying and injecting user-defined arguments for workflows through workflow launch configurations
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.launching;

import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_ARGUMENTS;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_ARGUMENTS_DEFAULT;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_AUTO_SAVE;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_AUTO_SAVE_DEFAULT;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_MODEL;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_MODEL_DEFAULT;
import static org.eclipse.sphinx.emf.mwe.dynamic.launching.IWorkflowLaunchConfigurationConstants.ATTR_WORKFLOW;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.emf.mwe.dynamic.launching.internal.Activator;
import org.eclipse.sphinx.emf.mwe.dynamic.launching.internal.messages.Messages;
import org.eclipse.sphinx.emf.mwe.dynamic.operations.BasicWorkflowRunnerOperation;
import org.eclipse.sphinx.emf.mwe.dynamic.operations.IWorkflowRunnerOperation;
import org.eclipse.sphinx.emf.mwe.dynamic.util.WorkflowRunnerHelper;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class WorkflowLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	protected WorkflowRunnerHelper helper = new WorkflowRunnerHelper();

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			// Retrieve attributes
			String workflowAttrValue = configuration.getAttribute(ATTR_WORKFLOW, ATTR_WORKFLOW);
			String modelAttrValue = configuration.getAttribute(ATTR_MODEL, ATTR_MODEL_DEFAULT);
			boolean autoSaveAttrValue = configuration.getAttribute(ATTR_AUTO_SAVE, ATTR_AUTO_SAVE_DEFAULT);
			Map<String, String> argumentsAttrValue = configuration.getAttribute(ATTR_ARGUMENTS, ATTR_ARGUMENTS_DEFAULT);

			// Retrieve workflow class or file and create the workflow operation
			Object workflow = helper.toWorkflowObject(workflowAttrValue);
			IWorkflowRunnerOperation operation = createWorkflowRunnerOperation(workflow);

			// Retrieve model URI and add it to workflow operation
			URI modelURI = helper.toModelURIObject(modelAttrValue);
			if (modelURI != null) {
				operation.getModelURIs().add(modelURI);
			}

			// Retrieve auto save option and set it on workflow operation
			operation.setAutoSave(autoSaveAttrValue);

			// Retrieve arguments and add them to workflow operation
			operation.getArguments().putAll(argumentsAttrValue);

			// Run workflow operation
			operation.run(monitor);
		} catch (OperationCanceledException ex) {
			return;
		} catch (Throwable th) {
			System.err.println(th.getMessage());
			PlatformLogUtil.logAsError(Activator.getPlugin(), th);
		}
	}

	protected BasicWorkflowRunnerOperation createWorkflowRunnerOperation(Object workflow) {
		return new BasicWorkflowRunnerOperation(Messages.operation_runWorkflow_label, workflow);
	}
}
