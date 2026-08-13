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
package org.eclipse.sphinx.emf.check.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.mwe.core.monitor.NullProgressMonitor;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;

/**
 * An adapter between MWE's {@link ProgressMonitor} interface and Eclipse's {@link IProgressMonitor} interface. The
 * adapter delegates all invocations to the wrapped <tt>ProgressMonitor</tt> instance.
 */
public class EclipseProgressMonitorAdapter implements IProgressMonitor {

	private ProgressMonitor monitor;

	/**
	 * Constructor.
	 *
	 * @param monitor
	 *            The MWE {@link ProgressMonitor} implementation to which the invocations of the Eclipse
	 *            {@link IProgressMonitor} interface should be delegated.
	 */
	public EclipseProgressMonitorAdapter(ProgressMonitor monitor) {
		this.monitor = monitor != null ? monitor : new NullProgressMonitor();
	}

	/*
	 * @see org.eclipse.emf.mwe.core.monitor.ProgressMonitor#beginTask(java.lang.String, int)
	 */
	@Override
	public void beginTask(String name, int totalWork) {
		monitor.beginTask(name, totalWork);
	}

	/*
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	@Override
	public void done() {
		monitor.done();
	}

	/*
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	@Override
	public void internalWorked(double work) {
		monitor.internalWorked(work);
	}

	/*
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	/*
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean value) {
		monitor.setCanceled(value);
	}

	/*
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	@Override
	public void setTaskName(String name) {
		monitor.setTaskName(name);
	}

	/*
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	@Override
	public void subTask(String name) {
		monitor.subTask(name);
	}

	/*
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	@Override
	public void worked(int work) {
		monitor.worked(work);
	}
}
