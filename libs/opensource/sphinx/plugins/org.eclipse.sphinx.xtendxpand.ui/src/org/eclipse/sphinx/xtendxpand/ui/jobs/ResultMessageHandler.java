/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [358591] ResultObjectHandler and ResultMessageHandler used by M2xConfigurationWizards are difficult to customize and should be usable in BasicM2xActions too
 * 
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.jobs;

import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.xtendxpand.jobs.AbstractM2xResultHandler;
import org.eclipse.sphinx.xtendxpand.jobs.CheckJob;
import org.eclipse.sphinx.xtendxpand.jobs.M2MJob;
import org.eclipse.sphinx.xtendxpand.jobs.M2TJob;
import org.eclipse.sphinx.xtendxpand.jobs.XpandJob;
import org.eclipse.sphinx.xtendxpand.jobs.XtendJob;
import org.eclipse.sphinx.xtendxpand.ui.internal.messages.Messages;
import org.eclipse.swt.widgets.Display;

/**
 * An {@link IJobChangeListener} implementation that can be registered on an {@link XpandJob}, {@link CheckJob} or
 * {@link XtendJob} instance or a {@link M2TJob} or {@link M2MJob} instance enclosing the latter and opens a message
 * dialog indicating the result status of the same.
 * 
 * @see XpandJob
 * @see XtendJob
 * @see CheckJob
 * @see M2TJob
 * @see M2MJob
 */
public class ResultMessageHandler extends AbstractM2xResultHandler {

	/**
	 * Indicates when the result message dialog is to be displayed (on completion only, or on cancellation only or on
	 * completion and cancellation, etc.).
	 * 
	 * @see {@link IResultMessageConstants} interface.
	 */
	private int openDialogOn;

	/**
	 * Constructs a result message handler that opens a message dialog indicating the result status of underlying M2x
	 * job, if failed.
	 */
	public ResultMessageHandler() {
		this(IResultMessageConstants.OPEN_DIALOG_ON_FAILED);
	}

	/**
	 * Constructs a result message handler that opens a message dialog indicating the result status of underlying M2x
	 * job.
	 * 
	 * @param m2xJob
	 *            the M2TJob or M2MJob instance to be use.
	 * @param openDialogOn
	 *            a bit indicating when the message is to be displayed (on completion only, or on cancellation only or
	 *            on completion and cancellation, etc.).
	 */
	public ResultMessageHandler(int openDialogOn) {
		this.openDialogOn = openDialogOn;
	}

	@Override
	public void handleResult(Job m2xJob) {
		handleResultMessage();
	}

	/**
	 * Opens a message dialog that indicates the result status of underlying M2x job.
	 */
	protected void handleResultMessage() {
		final Display display = ExtendedPlatformUI.getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					XpandJob xpandJob = getXpandJob();
					String title = xpandJob != null ? Messages.title_codeGen : Messages.title_modelTransformation;
					String m2xOperationName = xpandJob != null ? Messages.msg_codeGen : Messages.msg_modelTransformation;

					// If check is enabled and check job ends with errors then open a message dialog indicating that
					CheckJob checkJob = getCheckJob();
					if (checkJob != null && checkJob.getResult() != null && checkJob.getResult().getSeverity() == IStatus.ERROR) {
						MessageDialog dialog = new MessageDialog(ExtendedPlatformUI.getActiveShell(), title, null, capMessage(NLS.bind(
								Messages.msg_M2x_Check_operation_failed, m2xOperationName)), MessageDialog.ERROR,
								new String[] { IDialogConstants.OK_LABEL }, 0);
						dialog.open();
						return;
					}

					// Handle result of M2x job
					String message = ""; //$NON-NLS-1$
					int imageType = -1;
					IStatus m2xResultStatus = getM2xJob().getResult();
					switch (openDialogOn) {

					// The case when the message dialog is to be displayed on failure or completion only
					case IResultMessageConstants.OPEN_DIALOG_ON_FAILED_OR_COMPLETION:
						if (m2xResultStatus.getSeverity() == IStatus.OK) {
							message = capMessage(NLS.bind(Messages.msg_M2x_operation_successful, m2xOperationName));
							imageType = MessageDialog.INFORMATION;
						}
						break;

					// The case when the message dialog is to be displayed on failure or completion or cancellation
					case IResultMessageConstants.OPEN_DIALOG_ON_FAILED_OR_COMPLETION_OR_CANCELLATION:
						if (m2xResultStatus != null && m2xResultStatus.getSeverity() == IStatus.OK) {
							message = capMessage(NLS.bind(Messages.msg_M2x_operation_successful, m2xOperationName));
							imageType = MessageDialog.INFORMATION;
						} else if (m2xResultStatus != null && m2xResultStatus.getSeverity() == IStatus.CANCEL) {
							message = capMessage(NLS.bind(Messages.msg_M2x_operation_canceled, m2xOperationName));
							imageType = Window.CANCEL;
						}
						break;

					// The default case: open message dialog if M2x job failed
					default:
						/*
						 * !! Important Note !! Note that Eclipse automatically opens a message dialog with a status
						 * message when a job returns with an error status. In this case, we must not open up another
						 * one by ourselves.
						 */
						break;
					}

					// Open message dialog if required
					if (message.length() > 0 && imageType != -1) {
						MessageDialog dialog = new MessageDialog(ExtendedPlatformUI.getActiveShell(), title, null, message, imageType,
								new String[] { IDialogConstants.OK_LABEL }, 0);
						dialog.open();
					}
				}
			});
		}
	}

	protected String capMessage(String message) {
		if (message.length() > 0) {
			return message.substring(0, 1).toUpperCase(Locale.getDefault()) + message.substring(1);
		}
		return message;
	}
}
