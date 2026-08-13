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

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Abstract base class for implementing handlers that can be registered as {@link IJobChangeListener} on an
 * {@link XtendJob} instance or a {@link M2MJob} instance that encloses the latter and process the
 * {@link XtendJob#getResultObjects() result objects} produced by the {@link XtendJob}.
 * 
 * @see XtendJob
 * @see M2MJob
 */
public abstract class AbstractResultObjectHandler extends AbstractM2xResultHandler {

	/*
	 * @see org.eclipse.sphinx.xtendxpand.jobs.AbstractM2xResultHandler#handleResult(org.eclipse.core.runtime.jobs.Job)
	 */
	@Override
	public void handleResult(Job m2xJob) {
		XtendJob xtendJob = getXtendJob();
		if (xtendJob != null) {
			handleResultObjects(xtendJob.getResultObjects());
		}
	}

	/**
	 * Invoked for handling the the {@link XtendJob#getResultObjects() result objects} produced by the {@link XtendJob}
	 * behind the last job that has completed execution.
	 * <p>
	 * Clients are expected to override this method and for implementing the required result object handling behavior.
	 * </p>
	 * 
	 * @param resultObjects
	 *            A map that is keyed by the input objects that had been passed to the {@link XtendJob} and yields the
	 *            collection of result objects that the {@link XtendJob} has produced for each of them as value.
	 * @see #getM2xJob()
	 */
	protected abstract void handleResultObjects(Map<Object, Collection<?>> resultObjects);
}
