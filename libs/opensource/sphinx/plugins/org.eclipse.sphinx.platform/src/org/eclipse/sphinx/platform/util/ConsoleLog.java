/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.platform.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.Bundle;

/**
 * A {@link ILog} implementation that prints logged status events on the console. Intended to be used in standalone
 * applications when the Platform's log infrastructure is not available.
 */
public class ConsoleLog implements ILog {

	/*
	 * @see org.eclipse.core.runtime.ILog#addLogListener(org.eclipse.core.runtime.ILogListener)
	 */
	@Override
	public void addLogListener(ILogListener listener) {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.core.runtime.ILog#getBundle()
	 */
	@Override
	public Bundle getBundle() {
		return null;
	}

	/*
	 * @see org.eclipse.core.runtime.ILog#log(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	public void log(IStatus status) {
		Assert.isNotNull(status);

		// Print status message to standard error or out as appropriate
		if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.WARNING) {
			System.err.println(status.getMessage());
		} else {
			System.out.println(status.getMessage());
		}

		// Print exception stack trace if any
		Throwable exception = status.getException();
		if (exception != null) {
			exception.printStackTrace();
		}
	}

	/*
	 * @see org.eclipse.core.runtime.ILog#removeLogListener(org.eclipse.core.runtime.ILogListener)
	 */
	@Override
	public void removeLogListener(ILogListener listener) {
		throw new UnsupportedOperationException();
	}
}
