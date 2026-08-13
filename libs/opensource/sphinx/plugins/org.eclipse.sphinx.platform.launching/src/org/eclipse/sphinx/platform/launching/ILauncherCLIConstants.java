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

import org.eclipse.sphinx.platform.launching.internal.Activator;
import org.eclipse.sphinx.platform.launching.internal.messages.Messages;

public interface ILauncherCLIConstants {

	/*
	 * Application name.
	 */
	String APPLICATION_NAME = Activator.PLUGIN_ID + ".Launcher"; //$NON-NLS-1$

	/*
	 * Launch option.
	 */
	String OPTION_LAUNCH = "launch"; //$NON-NLS-1$
	String OPTION_LAUNCH_ARG_NAME = Messages.cliOption_launch_argName;
	String OPTION_LAUNCH_DESCRIPTION = Messages.cliOption_launch_description;
}
