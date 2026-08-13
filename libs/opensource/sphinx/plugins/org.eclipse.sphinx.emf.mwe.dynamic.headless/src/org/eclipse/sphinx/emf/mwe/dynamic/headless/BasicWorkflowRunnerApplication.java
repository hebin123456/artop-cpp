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
 *     itemis - [454532] NPE in BasicWorkflowRunnerApplication
 *     itemis - [463978] org.eclipse.sphinx.emf.mwe.dynamic.headless.BasicWorkflowRunnerApplication.interrogate() handling of null URI
 *     itemis - [472576] Headless workflow runner application unable to resolve model element URIs
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.mwe.dynamic.headless;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.emf.mwe.dynamic.operations.BasicWorkflowRunnerOperation;
import org.eclipse.sphinx.emf.mwe.dynamic.operations.IWorkflowRunnerOperation;
import org.eclipse.sphinx.emf.mwe.dynamic.util.WorkflowRunnerHelper;
import org.eclipse.sphinx.platform.cli.AbstractCLIApplication;
import org.eclipse.sphinx.platform.cli.ICommonCLIConstants;

/**
 * Usage examples:
 * <ul>
 * <li>eclipse<br/>
 * -noSplash<br/>
 * -data /my/workspace/location<br/>
 * -application org.eclipse.sphinx.emf.mwe.dynamic.headless.WorkflowRunner<br/>
 * -workflow org.example/src/org/example/MyWorkflowFile.java<br/>
 * -model org.example/model/MyModelFile.hummingbird</li>
 * <li>eclipse<br/>
 * -noSplash<br/>
 * -data /my/workspace/location<br/>
 * -application org.eclipse.sphinx.emf.mwe.dynamic.headless.WorkflowRunner<br/>
 * -workflow org.example/src/org/example/MyWorkflowFile.xtend<br/>
 * -model org.example/model/MyModelFile.hummingbird</li>
 * <li>eclipse<br/>
 * -noSplash<br/>
 * -data /my/workspace/location<br/>
 * -application org.eclipse.sphinx.emf.mwe.dynamic.headless.WorkflowRunner<br/>
 * -workflow org.example/src/org/example/MyWorkflowFile.xtend<br/>
 * -model platform:/resource/org.example/model/MyModelFile.hummingbird</li>
 * <li>eclipse<br/>
 * -noSplash<br/>
 * -data /my/workspace/location<br/>
 * -application org.eclipse.sphinx.emf.mwe.dynamic.headless.WorkflowRunner<br/>
 * -workflow org.example/src/org/example/MyWorkflowFile.xtend<br/>
 * -model file:/my/workspace/location/org.example/model/MyModelFile.hummingbird</li>
 * <li>eclipse<br/>
 * -noSplash<br/>
 * -data /my/workspace/location<br/>
 * -application org.eclipse.sphinx.emf.mwe.dynamic.headless.WorkflowRunner<br/>
 * -workflow org.example/src/org/example/MyWorkflowFile.xtend<br/>
 * -model platform:/resource/org.example/model/MyModelFile.hummingbird#//@components.0</li>
 * <li>eclipse<br/>
 * -noSplash<br/>
 * -data /my/workspace/location<br/>
 * -application org.eclipse.sphinx.emf.mwe.dynamic.headless.WorkflowRunner<br/>
 * -workflow org.example/src/org/example/MyWorkflowFile.xtend</li>
 * </ul>
 */
// TODO Add support for passing in workflow arguments (see
// org.eclipse.sphinx.emf.mwe.dynamic.launching.WorkflowLaunchConfigurationDelegate for details)
// TODO Externalize strings in related extention point contributions (see plugin.xml for details)
public class BasicWorkflowRunnerApplication extends AbstractCLIApplication {

	protected WorkflowRunnerHelper helper = new WorkflowRunnerHelper();

	/*
	 * @see org.eclipse.sphinx.platform.cli.AbstractCLIApplication#getCommandLineSyntax()
	 */
	@Override
	protected String getCommandLineSyntax() {
		return String.format(ICommonCLIConstants.COMMAND_LINE_SYNTAX_FORMAT_WITH_WORKSPACE, IWorkflowRunnerCLIConstants.APPLICATION_NAME);
	}

	/*
	 * @see org.eclipse.sphinx.platform.cli.AbstractCLIApplication#defineOptions()
	 */
	@Override
	protected void defineOptions() {
		super.defineOptions();

		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withArgName(IWorkflowRunnerCLIConstants.OPTION_WORKFLOW_ARG_NAME);
		OptionBuilder.withDescription(IWorkflowRunnerCLIConstants.OPTION_WORKFLOW_DESCRIPTION);
		addOption(OptionBuilder.create(IWorkflowRunnerCLIConstants.OPTION_WORKFLOW));

		OptionBuilder.hasArg();
		OptionBuilder.withArgName(IWorkflowRunnerCLIConstants.OPTION_MODEL_ARG_NAME);
		OptionBuilder.withDescription(IWorkflowRunnerCLIConstants.OPTION_MODEL_DESCRIPTION);
		addOption(OptionBuilder.create(IWorkflowRunnerCLIConstants.OPTION_MODEL));

		addOption(new Option(IWorkflowRunnerCLIConstants.OPTION_SKIP_SAVE, IWorkflowRunnerCLIConstants.OPTION_SKIP_SAVE_DESCRIPTION));
	}

	/*
	 * @see org.eclipse.sphinx.platform.cli.AbstractCLIApplication#interrogate()
	 */
	@Override
	protected Object interrogate() throws Throwable {
		super.interrogate();

		// Retrieve options
		CommandLine commandLine = getCommandLine();
		String workflowOptionValue = commandLine.getOptionValue(IWorkflowRunnerCLIConstants.OPTION_WORKFLOW);
		String modelOptionValue = commandLine.getOptionValue(IWorkflowRunnerCLIConstants.OPTION_MODEL);
		boolean skipSaveOption = commandLine.hasOption(IWorkflowRunnerCLIConstants.OPTION_SKIP_SAVE);

		// Retrieve workflow class or file and create the workflow operation
		Object workflow = helper.toWorkflowObject(workflowOptionValue);
		IWorkflowRunnerOperation operation = createWorkflowRunnerOperation(workflow);

		// Retrieve model URI and add it to workflow operation
		URI modelURI = helper.toModelURIObject(modelOptionValue);
		if (modelURI != null) {
			operation.getModelURIs().add(modelURI);
		}

		// Retrieve auto save option and set it on workflow operation
		operation.setAutoSave(!skipSaveOption);

		// Run workflow operation
		operation.run(createProgressMonitor());

		return ERROR_NO;
	}

	protected BasicWorkflowRunnerOperation createWorkflowRunnerOperation(Object workflow) {
		return new BasicWorkflowRunnerOperation(IWorkflowRunnerCLIConstants.APPLICATION_NAME, workflow);
	}

	protected IProgressMonitor createProgressMonitor() {
		return new NullProgressMonitor();
	}
}