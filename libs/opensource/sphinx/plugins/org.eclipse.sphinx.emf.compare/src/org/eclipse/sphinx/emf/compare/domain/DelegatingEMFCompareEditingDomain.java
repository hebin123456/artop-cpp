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
package org.eclipse.sphinx.emf.compare.domain;

import java.util.List;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.command.ICompareCommandStack;
import org.eclipse.emf.compare.command.ICompareCopyCommand;
import org.eclipse.emf.compare.command.impl.CompareCommandStack;
import org.eclipse.emf.compare.domain.ICompareEditingDomain;
import org.eclipse.emf.compare.domain.IMergeRunnable;
import org.eclipse.emf.compare.merge.IMerger.Registry;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;

public class DelegatingEMFCompareEditingDomain implements ICompareEditingDomain {

	private ICompareEditingDomain delegate;

	public DelegatingEMFCompareEditingDomain() {
	}

	public DelegatingEMFCompareEditingDomain(ICompareEditingDomain delegate) {
		this.delegate = delegate;
	}

	public ICompareEditingDomain getDelegate() {
		return delegate;
	}

	public void setDelegate(ICompareEditingDomain delegate) {
		this.delegate = delegate;
	}

	@Override
	public ICompareCommandStack getCommandStack() {
		if (delegate != null) {
			return delegate.getCommandStack();
		}
		return new CompareCommandStack(new BasicCommandStack());
	}

	@Override
	public ChangeRecorder getChangeRecorder() {
		if (delegate != null) {
			return delegate.getChangeRecorder();
		}
		return null;
	}

	@Override
	public Command createCopyCommand(List<? extends Diff> differences, boolean leftToRight, Registry mergerRegistry) {
		if (delegate != null) {
			return delegate.createCopyCommand(differences, leftToRight, mergerRegistry);
		}
		return null;
	}

	@Override
	public ICompareCopyCommand createCopyCommand(List<? extends Diff> differences, boolean leftToRight, Registry mergerRegistry,
			IMergeRunnable runnable) {
		if (delegate != null) {
			return delegate.createCopyCommand(differences, leftToRight, mergerRegistry, runnable);
		}
		return null;
	}
}
