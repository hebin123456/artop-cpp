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
package org.eclipse.sphinx.platform.launching;

import java.io.FileNotFoundException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.cli.AbstractCLIApplication;
import org.eclipse.sphinx.platform.cli.ICommonCLIConstants;
import org.eclipse.sphinx.platform.launching.internal.messages.Messages;

/**
 * Enables shared launch configurations that are persisted as *.launch files in a Eclipse workspace
 * to be run from the command line (i.e., without having to start the Eclipse workbench).
 *
 * Usage examples:
 * <ul>
 * <li>eclipse<br/>
 * -noSplash<br/>
 * -data /my/workspace/location<br/>
 * -application org.eclipse.sphinx.emf.mwe.dynamic.launch.Launcher<br/>
 * -launch org.example/launch/MyLaunch.launch<br/></li>
 * </ul>
 */
public class BasicLauncherApplication extends AbstractCLIApplication {

	/*
	 * @see org.eclipse.sphinx.platform.cli.AbstractCLIApplication#getCommandLineSyntax()
	 */
	@Override
	protected String getCommandLineSyntax() {
		return String.format(ICommonCLIConstants.COMMAND_LINE_SYNTAX_FORMAT_WITH_WORKSPACE, ILauncherCLIConstants.APPLICATION_NAME);
	}

	/*
	 * @see org.eclipse.sphinx.platform.cli.AbstractCLIApplication#defineOptions()
	 */
	@Override
	protected void defineOptions() {
		super.defineOptions();

		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withArgName(ILauncherCLIConstants.OPTION_LAUNCH_ARG_NAME);
		OptionBuilder.withDescription(ILauncherCLIConstants.OPTION_LAUNCH_DESCRIPTION);
		addOption(OptionBuilder.create(ILauncherCLIConstants.OPTION_LAUNCH));
	}

	/*
	 * @see org.eclipse.sphinx.platform.cli.AbstractCLIApplication#interrogate()
	 */
	@Override
	protected Object interrogate() throws Throwable {
		super.interrogate();

		// Retrieve options
		CommandLine commandLine = getCommandLine();
		String launchOptionValue = commandLine.getOptionValue(ILauncherCLIConstants.OPTION_LAUNCH);

		// Retrieve and invoke specified launch
		IFile launchFile = getLaunchFile(launchOptionValue);
		ILaunchConfiguration launchConfiguration = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(launchFile);
		launchConfiguration.launch(ILaunchManager.RUN_MODE, createProgressMonitor());

		return ERROR_NO;
	}

	protected IFile getLaunchFile(String launchOptionValue) throws FileNotFoundException {
		// Launch is assumed to be a workspace-relative path
		Path launchPath = new Path(launchOptionValue);
		IFile launchFile = ResourcesPlugin.getWorkspace().getRoot().getFile(launchPath);
		if (!launchFile.exists()) {
			IPath launchLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(launchPath);
			throw new FileNotFoundException(NLS.bind(Messages.error_launchFileDoesNotExist, launchLocation.toOSString()));
		}
		return launchFile;
	}

	protected IProgressMonitor createProgressMonitor() {
		return new NullProgressMonitor();
	}
}