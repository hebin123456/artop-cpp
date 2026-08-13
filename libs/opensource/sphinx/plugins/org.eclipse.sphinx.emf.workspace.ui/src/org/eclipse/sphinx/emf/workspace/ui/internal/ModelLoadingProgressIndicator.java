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
package org.eclipse.sphinx.emf.workspace.ui.internal;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * {@linkplain IJobChangeListener Job change listener} that is responsible for asking the opening of a
 * {@linkplain org.eclipse.ui.internal.progress.ProgressMonitorFocusJobDialog dialog} in order to show the progress of
 * loading jobs. It asks the opening of such a dialog when it is notified that a job belonging to the
 * {@linkplain IExtendedPlatformConstants#FAMILY_MODEL_LOADING model loading} family is about to run.
 * <p>
 * As the dialog is internal to Eclipse, one must retrieve the {@linkplain IProgressService progress service} owned by
 * the {@linkplain IWorkbench workbench}. It is then possible to ask a job to be shown in a dialog with
 * {@linkplain IProgressService#showInDialog(Shell, Job)}.
 */
public class ModelLoadingProgressIndicator extends JobChangeAdapter {

	/**
	 * The singleton instance.
	 */
	public static ModelLoadingProgressIndicator INSTANCE = new ModelLoadingProgressIndicator();

	/**
	 * Constructor for singleton pattern.
	 */
	private ModelLoadingProgressIndicator() {
		// Nothing to do
	}

	/**
	 * @param event
	 *            The job change event owning the job that may be a model loader job.
	 * @return
	 * 		<ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if the specified {@linkplain IJobChangeEvent event} refers to a
	 *         model loader job (<em>i.e.</em> to a job that belongs to the
	 *         {@linkplain IExtendedPlatformConstants#FAMILY_MODEL_LOADING model loading} family);</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	private boolean isModelLoadingJob(IJobChangeEvent event) {
		if (event != null) {
			Job job = event.getJob();
			if (job != null) {
				return job.belongsTo(IExtendedPlatformConstants.FAMILY_MODEL_LOADING);
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void aboutToRun(final IJobChangeEvent event) {
		if (isModelLoadingJob(event)) {
			// Open progress dialog for model loading job in UI thread
			/*
			 * !! Important Note !! The progress dialog applies a "short operation time" delay and actually opens up
			 * only if the model loading job is still running after this delay. This avoids the progress dialog from
			 * flickering up for short running model loading jobs. In order to make sure that all this can take place in
			 * the intended way, we must invoke the progress dialog asynchronously and let this listener terminate in
			 * parallel. Otherwise, the model loading job would be prevented from running and consequently short running
			 * model loading jobs would never be complete when the progress dialog's short operation time delay is over.
			 */
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (display != null && !display.isDisposed()) {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						SafeRunner.run(new SafeRunnable("Creating dialog showing model loading progress...") { //$NON-NLS-1$
							@Override
							public void run() {
								PlatformUI.getWorkbench().getProgressService().showInDialog(null, event.getJob());
							}
						});
					}
				});
			}
		}
	}
}
