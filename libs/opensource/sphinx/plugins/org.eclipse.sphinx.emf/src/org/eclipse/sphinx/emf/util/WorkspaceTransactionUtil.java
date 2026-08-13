/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added/Updated javadoc
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.TransactionImpl;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.emf.workspace.IWorkspaceCommandStack;
import org.eclipse.emf.workspace.impl.WorkspaceCommandStackImpl;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.platform.operations.ILabeledRunnable;
import org.eclipse.sphinx.platform.operations.ILabeledWorkspaceRunnable;

/**
 * An utility class providing helper methods for handling and executing transactions on an EMF model.
 */
public final class WorkspaceTransactionUtil {

	// Prevent from instantiation
	private WorkspaceTransactionUtil() {
	}

	/**
	 * Returns the IOperationHistory for the given EditingDomain.
	 *
	 * @param editingDomain
	 *            The EditingDomain for which the IOperationHistory is to be retrieved.
	 * @return The IOperationHistory of the given <code>edingDomain</code>.
	 */
	public static IOperationHistory getOperationHistory(TransactionalEditingDomain editingDomain) {
		if (editingDomain != null) {
			CommandStack commandStack = editingDomain.getCommandStack();
			if (commandStack instanceof IWorkspaceCommandStack) {
				return ((IWorkspaceCommandStack) commandStack).getOperationHistory();
			}
		}
		return OperationHistoryFactory.getOperationHistory();
	}

	/**
	 * Returns the IUndoContext for the given EditingDomain.
	 *
	 * @param editingDomain
	 *            The EditingDomain for which the IUndoContext is to be retrieved.
	 * @return The IUndoContext of the given <code>edingDomain</code>.
	 */
	public static IUndoContext getUndoContext(TransactionalEditingDomain editingDomain) {
		if (editingDomain != null) {
			CommandStack commandStack = editingDomain.getCommandStack();
			if (commandStack instanceof WorkspaceCommandStackImpl) {
				return IOperationHistory.GLOBAL_UNDO_CONTEXT;
			}
		}
		return (IUndoContext) ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
	}

	/**
	 * Returns a default set of options which can be used for executing an operation within a transaction.
	 *
	 * @return The default options for executing an operation in a transaction.
	 */
	public static Map<String, Object> getDefaultTransactionOptions() {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(TransactionImpl.BLOCK_CHANGE_PROPAGATION, Boolean.TRUE);
		options.put(Transaction.OPTION_NO_VALIDATION, Boolean.TRUE);
		return options;
	}

	/**
	 * Returns a default set of options which can be used for executing an operation saving a new model within a write
	 * transaction.
	 *
	 * @return The default options for executing a save new operation in a transaction.
	 */
	public static Map<String, Object> getDefaultSaveNewTransactionOptions() {
		Map<String, Object> options = WorkspaceTransactionUtil.getDefaultTransactionOptions();
		options.put(Transaction.OPTION_NO_UNDO, Boolean.TRUE);
		return options;
	}

	/**
	 * Returns a default set of options which can be used for executing an operation saving an existing model within a
	 * write transaction.
	 *
	 * @return The default options for executing a save operation in a transaction.
	 */
	public static Map<String, Object> getDefaultSaveTransactionOptions() {
		Map<String, Object> options = WorkspaceTransactionUtil.getDefaultTransactionOptions();
		options.put(Transaction.OPTION_NO_UNDO, Boolean.TRUE);
		options.put(Transaction.OPTION_NO_NOTIFICATIONS, Boolean.TRUE);
		options.put(Transaction.OPTION_NO_TRIGGERS, Boolean.TRUE);
		return options;
	}

	/**
	 * Executes given {@linkplain Runnable runnable} in a write transaction.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} receiving the transaction.
	 * @param runnable
	 *            The {@linkplain Runnable runnable} to be executed.
	 * @param operationLabel
	 *            The label of the operation to be executed.
	 * @throws OperationCanceledException
	 *             Thrown when the transaction is cancelled by the user.
	 * @throws ExecutionException
	 *             Thrown when the transaction could not be completed to an Exception.
	 **/
	public static void executeInWriteTransaction(TransactionalEditingDomain editingDomain, Runnable runnable, String operationLabel)
			throws OperationCanceledException, ExecutionException {
		executeInWriteTransaction(editingDomain, runnable, operationLabel, getOperationHistory(editingDomain), getDefaultTransactionOptions());
	}

	/**
	 * Execute given {@linkplain Runnable runnable} in a write transaction.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} receiving the transaction.
	 * @param runnable
	 *            The {@linkplain Runnable runnable} to be executed.
	 * @param operationLabel
	 *            The label of the operation to be executed.
	 * @param operationHistory
	 *            The {@linkplain IOperationHistory operation history} to store the executed operation.
	 * @param transactionOptions
	 *            The options to set the transaction.
	 * @throws OperationCanceledException
	 *             Thrown when the transaction is canceled by the user.
	 * @throws ExecutionException
	 *             Thrown when the transaction could not be completed to an Exception.
	 **/
	public static void executeInWriteTransaction(TransactionalEditingDomain editingDomain, final Runnable runnable, final String operationLabel,
			IOperationHistory operationHistory, final Map<String, Object> transactionOptions) throws OperationCanceledException, ExecutionException {
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(runnable);
		Assert.isNotNull(operationHistory);

		final String safeLabel = operationLabel == null ? "Unnamed operation" : operationLabel; //$NON-NLS-1$
		IUndoableOperation operation = new AbstractEMFOperation(editingDomain, safeLabel, transactionOptions) {
			@Override
			protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				try {
					runnable.run();
					return Status.OK_STATUS;
				} catch (Exception ex) {
					if (ex instanceof OperationCanceledException) {
						throw (OperationCanceledException) ex;
					} else {
						throw new ExecutionException(NLS.bind(Messages.problem_transactionFailed, safeLabel), ex);
					}
				}
			}

			@Override
			public boolean canUndo() {
				return transactionOptions.get(Transaction.OPTION_NO_UNDO) != Boolean.TRUE;
			}
		};
		// Perform the execution of the transaction.
		IStatus status = operationHistory.execute(operation, null, null);

		if (status.getSeverity() == IStatus.CANCEL) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Execute given {@linkplain Runnable runnable} in a write transaction.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} receiving the transaction.
	 * @param runnable
	 *            The {@linkplain Runnable runnable} to be executed.
	 * @param operationLabel
	 *            The label of the operation to be executed.
	 * @param operationHistory
	 *            The {@linkplain IOperationHistory operation history} to store the executed operation.
	 * @param transactionOptions
	 *            The options to set the transaction.
	 * @param monitor
	 *            The {@linkplain IProgressMonitor progress monitor} to use during operation execution.
	 * @throws OperationCanceledException
	 *             Thrown when the transaction is canceled by the user.
	 * @throws ExecutionException
	 *             Thrown when the transaction could not be completed to an Exception.
	 * @since 0.7.0
	 * @deprecated Use
	 *             {@link #executeInWriteTransaction(TransactionalEditingDomain, Runnable, String, IOperationHistory, Map)}
	 *             or
	 *             {@link #executeInWriteTransaction(TransactionalEditingDomain, ILabeledWorkspaceRunnable, IOperationHistory, Map, IProgressMonitor)}
	 *             instead.
	 **/
	@Deprecated
	public static void executeInWriteTransaction(TransactionalEditingDomain editingDomain, final Runnable runnable, final String operationLabel,
			IOperationHistory operationHistory, final Map<String, Object> transactionOptions, IProgressMonitor monitor)
					throws OperationCanceledException, ExecutionException {
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(runnable);
		Assert.isNotNull(operationHistory);

		final String safeLabel = operationLabel == null ? "Unnamed operation" : operationLabel; //$NON-NLS-1$
		IUndoableOperation operation = new AbstractEMFOperation(editingDomain, safeLabel, transactionOptions) {
			@Override
			protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				try {
					runnable.run();
					return Status.OK_STATUS;
				} catch (Exception ex) {
					if (ex instanceof OperationCanceledException) {
						throw (OperationCanceledException) ex;
					} else {
						throw new ExecutionException(NLS.bind(Messages.problem_transactionFailed, safeLabel), ex);
					}
				}
			}

			@Override
			public boolean canUndo() {
				return transactionOptions.get(Transaction.OPTION_NO_UNDO) != Boolean.TRUE;
			}
		};
		// Perform the execution of the transaction.
		IStatus status = operationHistory.execute(operation, monitor, null);

		if (status.getSeverity() == IStatus.CANCEL) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Executes given {@linkplain ILabeledRunnable runnable} in a write transaction.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} receiving the transaction.
	 * @param runnable
	 *            The {@linkplain ILabeledRunnable runnable} to be executed.
	 * @throws OperationCanceledException
	 *             Thrown when the transaction is cancelled by the user.
	 * @throws ExecutionException
	 *             Thrown when the transaction could not be completed to an Exception.
	 **/
	public static void executeInWriteTransaction(TransactionalEditingDomain editingDomain, ILabeledRunnable runnable)
			throws OperationCanceledException, ExecutionException {
		executeInWriteTransaction(editingDomain, runnable, runnable.getLabel(), getOperationHistory(editingDomain), getDefaultTransactionOptions());
	}

	/**
	 * Execute given {@linkplain ILabeledRunnable runnable} in a write transaction.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} receiving the transaction.
	 * @param runnable
	 *            The {@linkplain ILabeledRunnable runnable} to be executed.
	 * @param operationHistory
	 *            The {@linkplain IOperationHistory operation history} to store the executed operation.
	 * @param transactionOptions
	 *            The options to set the transaction.
	 * @throws OperationCanceledException
	 *             Thrown when the transaction is canceled by the user.
	 * @throws ExecutionException
	 *             Thrown when the transaction could not be completed to an Exception.
	 **/
	public static void executeInWriteTransaction(TransactionalEditingDomain editingDomain, ILabeledRunnable runnable,
			IOperationHistory operationHistory, Map<String, Object> transactionOptions) throws OperationCanceledException, ExecutionException {
		executeInWriteTransaction(editingDomain, runnable, runnable.getLabel(), operationHistory, transactionOptions);
	}

	/**
	 * Executes given {@linkplain ILabeledWorkspaceRunnable runnable} in a write transaction.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} receiving the transaction.
	 * @param runnable
	 *            The {@linkplain ILabeledWorkspaceRunnable runnable} to be executed.
	 * @param monitor
	 *            The {@linkplain IProgressMonitor progress monitor} to use during operation execution.
	 * @throws OperationCanceledException
	 *             Thrown when the transaction is cancelled by the user.
	 * @throws ExecutionException
	 *             Thrown when the transaction could not be completed to an Exception.
	 **/
	public static void executeInWriteTransaction(TransactionalEditingDomain editingDomain, ILabeledWorkspaceRunnable runnable,
			IProgressMonitor monitor) throws OperationCanceledException, ExecutionException {
		executeInWriteTransaction(editingDomain, runnable, getOperationHistory(editingDomain), getDefaultTransactionOptions(), monitor);
	}

	/**
	 * Execute given {@linkplain ILabeledWorkspaceRunnable runnable} in a write transaction.
	 *
	 * @param editingDomain
	 *            The {@linkplain TransactionalEditingDomain editing domain} receiving the transaction.
	 * @param runnable
	 *            The {@linkplain ILabeledWorkspaceRunnable runnable} to be executed.
	 * @param operationHistory
	 *            The {@linkplain IOperationHistory operation history} to store the executed operation.
	 * @param transactionOptions
	 *            The options to set the transaction.
	 * @param monitor
	 *            The {@linkplain IProgressMonitor progress monitor} to use during operation execution.
	 * @throws OperationCanceledException
	 *             Thrown when the transaction is canceled by the user.
	 * @throws ExecutionException
	 *             Thrown when the transaction could not be completed to an Exception.
	 **/
	public static void executeInWriteTransaction(TransactionalEditingDomain editingDomain, final ILabeledWorkspaceRunnable runnable,
			IOperationHistory operationHistory, final Map<String, Object> transactionOptions, IProgressMonitor monitor)
					throws OperationCanceledException, ExecutionException {
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(runnable);
		Assert.isNotNull(operationHistory);

		String label = runnable.getLabel();
		final String safeLabel = label == null ? "Unnamed operation" : label; //$NON-NLS-1$
		IUndoableOperation operation = new AbstractEMFOperation(editingDomain, safeLabel, transactionOptions) {
			@Override
			protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				try {
					runnable.run(monitor);
					return Status.OK_STATUS;
				} catch (Exception ex) {
					if (ex instanceof OperationCanceledException) {
						throw (OperationCanceledException) ex;
					} else {
						throw new ExecutionException(NLS.bind(Messages.problem_transactionFailed, safeLabel), ex);
					}
				}
			}

			@Override
			public boolean canUndo() {
				return transactionOptions.get(Transaction.OPTION_NO_UNDO) != Boolean.TRUE;
			}
		};
		// Perform the execution of the transaction.
		IStatus status = operationHistory.execute(operation, monitor, null);

		if (status.getSeverity() == IStatus.CANCEL) {
			throw new OperationCanceledException();
		}
	}
}
