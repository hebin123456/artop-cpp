/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.StatusUtil;
import org.eclipse.sphinx.xtendxpand.internal.Activator;

public class M2MJob extends Job {

	private CheckJob checkJob;

	private XtendJob xtendJob;

	public M2MJob(String name, XtendJob xtendJob) {
		this(name, xtendJob, null);
	}

	public M2MJob(String name, XtendJob xtendJob, CheckJob checkJob) {
		super(name);
		this.xtendJob = xtendJob;
		this.checkJob = checkJob;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		try {
			// Run check if required
			if (checkJob != null) {
				IStatus status = checkJob.run(progress.newChild(50));

				// Abort if check job ends with errors or is cancelled; continue when there are no errors or
				// only warnings
				if (status.getSeverity() == IStatus.ERROR || progress.isCanceled()) {
					throw new OperationCanceledException();
				}
			}

			// Run Xtend
			return xtendJob.run(progress.newChild(50));
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		}
	}

	@Override
	public boolean belongsTo(Object family) {
		return IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
	}

	public CheckJob getCheckJob() {
		return checkJob;
	}

	public XtendJob getXtendJob() {
		return xtendJob;
	}
}
