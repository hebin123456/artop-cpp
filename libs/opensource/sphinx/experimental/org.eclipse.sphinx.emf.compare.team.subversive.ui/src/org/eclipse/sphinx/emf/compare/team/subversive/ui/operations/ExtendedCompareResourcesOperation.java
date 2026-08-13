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
package org.eclipse.sphinx.emf.compare.team.subversive.ui.operations;

import org.eclipse.core.runtime.IProgressMonitor;

public class ExtendedCompareResourcesOperation extends DelegatingCompositeOperation {

	protected ILocalResource local;

	protected IRepositoryResource remote;

	protected CompareResourcesInternalOperation internalCompareOp;

	public ExtendedCompareResourcesOperation(ILocalResource local, IRepositoryResource remote) {
		this(local, remote, false, false);
	}

	public ExtendedCompareResourcesOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse) {
		this(local, remote, forceReuse, false);
	}

	public ExtendedCompareResourcesOperation(ILocalResource local, IRepositoryResource remote, boolean forceReuse, boolean showInDialog) {
		super("Operation_CompareLocal"); //$NON-NLS-1$
		this.local = local;
		this.remote = remote;

		final RunExternalCompareOperation externalCompareOp = new RunExternalCompareOperation(local, remote,
				SVNTeamDiffViewerPage.loadDiffViewerSettings());
		this.add(externalCompareOp);

		internalCompareOp = new ExtendedCompareResourcesInternalOperation(local, remote, forceReuse, showInDialog) {
			@Override
			protected void runImpl(IProgressMonitor monitor) throws Exception {
				if (!externalCompareOp.isExecuted()) {
					super.runImpl(monitor);
				}
			}
		};
		this.add(internalCompareOp, new IActionOperation[] { externalCompareOp });
	}

	public void setDiffFile(String diffFile) {
		if (diffFile != null) {
			this.add(new UDiffGenerateOperation(local, remote, diffFile), new IActionOperation[] { internalCompareOp });
		}
	}

	public void setForceId(String forceId) {
		internalCompareOp.setForceId(forceId);
	}
}
