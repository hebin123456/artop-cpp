/**
 * <copyright>
 * 
 * Copyright (c) 2011 itemis and others.
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
package org.eclipse.sphinx.xtendxpand.jobs;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * Abstract base class for implementing handlers that can be registered as {@link IJobChangeListener} on an
 * {@link XpandJob}, {@link CheckJob} or {@link XtendJob} instance or a {@link M2TJob} or {@link M2MJob} instance
 * enclosing the latter and process the result produced by the same.
 * <p>
 * Clients are expected to subclass this class and override the {@link #handleResult(Job)} method for implementing the
 * required result handling behavior.
 * </p>
 * 
 * @see XpandJob
 * @see XtendJob
 * @see CheckJob
 * @see M2TJob
 * @see M2MJob
 */
public abstract class AbstractM2xResultHandler extends JobChangeAdapter {

	/**
	 * The last job that has completed execution and is subject to this handler.
	 */
	private Job m2xJob;

	/**
	 * Returns the last job that has completed execution and is subject to this handler.
	 */
	protected Job getM2xJob() {
		return m2xJob;
	}

	/**
	 * Returns the {@link XpandJob} behind last job that has completed execution.
	 * 
	 * @return The {@link XpandJob} behind last job or <code>null</code> if last job was no or {@link XpandJob} or
	 *         didn't enclose any such.
	 */
	protected XpandJob getXpandJob() {
		if (m2xJob instanceof XpandJob) {
			return (XpandJob) m2xJob;
		}
		if (m2xJob instanceof M2TJob) {
			return ((M2TJob) m2xJob).getXpandJob();
		}
		return null;
	}

	/**
	 * Returns the {@link XtendJob} behind last job that has completed execution.
	 * 
	 * @return The {@link XtendJob} behind last job or <code>null</code> if last job was no or {@link XtendJob} or
	 *         didn't enclose any such.
	 */
	protected XtendJob getXtendJob() {
		if (m2xJob instanceof XtendJob) {
			return (XtendJob) m2xJob;
		}
		if (m2xJob instanceof M2MJob) {
			return ((M2MJob) m2xJob).getXtendJob();
		}
		return null;
	}

	/**
	 * Returns the {@link CheckJob} behind last job that has completed execution.
	 * 
	 * @return The {@link CheckJob} behind last job or <code>null</code> if last job was no or {@link CheckJob} or
	 *         didn't enclose any such.
	 */
	protected CheckJob getCheckJob() {
		if (m2xJob != null) {
			if (m2xJob instanceof CheckJob) {
				return (CheckJob) m2xJob;
			}
			if (m2xJob instanceof M2TJob) {
				return ((M2TJob) m2xJob).getCheckJob();
			}
			if (m2xJob instanceof M2MJob) {
				return ((M2MJob) m2xJob).getCheckJob();
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public final void done(IJobChangeEvent event) {
		// Update job to be handled
		if (event != null) {
			m2xJob = event.getJob();
		}

		// Handle job result if any
		if (m2xJob != null) {
			handleResult(m2xJob);
		}
	}

	/**
	 * Invoked for handling the result of last job that has completed execution.
	 * <p>
	 * Clients are expected to override this method and for implementing the required result handling behavior.
	 * </p>
	 * 
	 * @param m2xJob
	 *            The last job that has completed execution - same job instance as that returned by {@link #getM2xJob()}
	 *            .
	 * @see #getM2xJob()
	 */
	protected abstract void handleResult(Job m2xJob);
}
