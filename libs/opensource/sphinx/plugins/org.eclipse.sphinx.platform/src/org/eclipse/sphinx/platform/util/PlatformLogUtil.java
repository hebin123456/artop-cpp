/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [458921] Newly introduced registries for metamodel serives, check validators and workflow contributors are not standalone-safe
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.util;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;

/**
 * This class is used to log messages to the platform error log.
 */
public final class PlatformLogUtil {

	private static ILog consoleLog = null;

	public static ILog getLog(Plugin plugin) {
		if (plugin != null) {
			return plugin.getLog();
		} else {
			if (consoleLog == null) {
				consoleLog = new ConsoleLog();
			}
			return consoleLog;
		}
	}

	public static void logAsError(Plugin plugin, Object object) {
		IStatus status = StatusUtil.createErrorStatus(plugin, object);
		getLog(plugin).log(status);
	}

	public static void logAsWarning(Plugin plugin, Object object) {
		IStatus status = StatusUtil.createWarningStatus(plugin, object);
		getLog(plugin).log(status);
	}

	public static void logAsInfo(Plugin plugin, Object object) {
		IStatus status = StatusUtil.createInfoStatus(plugin, object);
		getLog(plugin).log(status);
	}
}
