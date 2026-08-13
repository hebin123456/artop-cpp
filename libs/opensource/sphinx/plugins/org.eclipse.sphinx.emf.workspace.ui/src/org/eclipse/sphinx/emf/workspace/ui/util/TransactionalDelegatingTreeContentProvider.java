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
package org.eclipse.sphinx.emf.workspace.ui.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class TransactionalDelegatingTreeContentProvider implements ITreeContentProvider {

	private final TransactionalEditingDomain domain;

	private final ITreeContentProvider treeContentProvider;

	/**
	 * Initializes me with the editing domain in which I create read transactions and with the content provider to which
	 * I delegate.
	 *
	 * @param domain
	 *            my editing domain
	 * @param treeContentProvider
	 *            the content provider to delegate to.
	 */
	public TransactionalDelegatingTreeContentProvider(ITreeContentProvider treeContentProvider, TransactionalEditingDomain domain) {
		Assert.isNotNull(treeContentProvider);
		Assert.isNotNull(domain);

		this.treeContentProvider = treeContentProvider;
		this.domain = domain;
	}

	/**
	 * Runs the specified runnable in the editing domain, with interrupt handling.
	 *
	 * @param <T>
	 *            the result type of the runnable
	 * @param run
	 *            the runnable to run
	 * @return its result, or <code>null</code> on interrupt
	 */
	protected <T> T run(RunnableWithResult<? extends T> run) {
		try {
			return TransactionUtil.runExclusive(domain, run);
		} catch (InterruptedException e) {
			// Propagate interrupt status because we are not throwing
			Thread.currentThread().interrupt();
			PlatformLogUtil.getLog(Activator.getPlugin()).log(
					new Status(IStatus.ERROR, Activator.getPlugin().getSymbolicName(), Messages.contentProviderInterrupted, e));

			return null;
		}
	}

	/**
	 * Wrap the delegation in a read-only transaction.
	 */
	@Override
	public Object[] getChildren(final Object object) {
		return run(new RunnableWithResult.Impl<Object[]>() {
			@Override
			public void run() {
				setResult(treeContentProvider.getChildren(object));
			}
		});
	}

	/**
	 * Wrap the delegation in a read-only transaction.
	 */
	@Override
	public Object[] getElements(final Object object) {
		return run(new RunnableWithResult.Impl<Object[]>() {
			@Override
			public void run() {
				setResult(treeContentProvider.getElements(object));
			}
		});
	}

	/**
	 * Wrap the delegation in a read-only transaction.
	 */
	@Override
	public Object getParent(final Object object) {
		return run(new RunnableWithResult.Impl<Object>() {
			@Override
			public void run() {
				setResult(treeContentProvider.getParent(object));
			}
		});
	}

	/**
	 * Wrap the delegation in a read-only transaction.
	 */
	@Override
	public boolean hasChildren(final Object object) {
		return run(new RunnableWithResult.Impl<Boolean>() {
			@Override
			public void run() {
				setResult(treeContentProvider.hasChildren(object));
			}
		});
	}

	/**
	 * Wrap the delegation in a read-only transaction.
	 */
	@Override
	public void inputChanged(final Viewer vwr, final Object oldInput, final Object newInput) {
		run(new RunnableWithResult.Impl<Object>() {
			@Override
			public void run() {
				treeContentProvider.inputChanged(vwr, oldInput, newInput);
			}
		});
	}

	/**
	 * Wrap the delegation in a read-only transaction.
	 */
	@Override
	public void dispose() {
		run(new RunnableWithResult.Impl<Object>() {
			@Override
			public void run() {
				treeContentProvider.dispose();
			}
		});
	}
}
