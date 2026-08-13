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
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.stats;

import java.util.ArrayList;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;

/**
 * A log writer that writes log entries. See PlatformLogReader for reading logs back into memory.
 * <p>
 * Note that this class just provides a bridge from the old ILog interface to the OSGi FrameworkLog interface.
 */
public class PlatformLogWriter implements ILogListener {

	private FrameworkLog frameworkLog;

	public PlatformLogWriter(FrameworkLog frameworkLog) {
		this.frameworkLog = frameworkLog;
	}

	/**
	 * @see ILogListener#logging(IStatus, String)
	 */
	@Override
	public synchronized void logging(IStatus status, String plugin) {
		frameworkLog.log(getLog(status));
	}

	protected FrameworkLogEntry getLog(IStatus status) {

		ArrayList<FrameworkLogEntry> childlist = new ArrayList<FrameworkLogEntry>();

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (IStatus element : children) {
				childlist.add(getLog(element));
			}
		}

		FrameworkLogEntry[] children = childlist.size() == 0 ? null : childlist.toArray(new FrameworkLogEntry[childlist.size()]);

		return new FrameworkLogEntry(status.getPlugin(), status.getSeverity(), status.getCode(), status.getMessage(), 0, status.getException(),
				children);
	}
}
