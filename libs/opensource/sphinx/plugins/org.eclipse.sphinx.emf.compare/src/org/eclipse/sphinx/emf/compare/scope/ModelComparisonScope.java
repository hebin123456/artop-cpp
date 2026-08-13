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
package org.eclipse.sphinx.emf.compare.scope;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;

public class ModelComparisonScope extends DefaultComparisonScope implements IModelComparisonScope {

	private IFile leftFile;
	private IFile rightFile;

	private IComparisonScope delegate;

	public ModelComparisonScope(Notifier left, Notifier right, Notifier origin) {
		super(left, right, origin);
	}

	public ModelComparisonScope(IFile leftFile, IFile rightFile) {
		this(leftFile, rightFile, null);
	}

	public ModelComparisonScope(IFile leftFile, IFile rightFile, IComparisonScope delegate) {
		super(null, null, null);

		this.leftFile = leftFile;
		this.rightFile = rightFile;
		this.delegate = delegate;
	}

	@Override
	public IComparisonScope getDelegate() {
		return delegate;
	}

	@Override
	public void setDelegate(IComparisonScope delegate) {
		this.delegate = delegate;
	}

	@Override
	public IFile getLeftFile() {
		return leftFile;
	}

	public void setLeftFile(IFile leftFile) {
		this.leftFile = leftFile;
	}

	@Override
	public IFile getRightFile() {
		return rightFile;
	}

	public void setRightFile(IFile rightFile) {
		this.rightFile = rightFile;
	}

	@Override
	public boolean isFileBasedComparison() {
		return leftFile != null && rightFile != null;
	}

	@Override
	public Notifier getLeft() {
		if (delegate != null) {
			return delegate.getLeft();
		}
		return super.getLeft();
	}

	@Override
	public Notifier getRight() {
		if (delegate != null) {
			return delegate.getRight();
		}
		return super.getRight();
	}

	@Override
	public Notifier getOrigin() {
		if (delegate != null) {
			return delegate.getOrigin();
		}
		return super.getOrigin();
	}
}
