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
package org.eclipse.sphinx.emf.compare.team.subversive.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.sphinx.emf.compare.team.subversive.ui.operations.DelegatingCompositeOperation;
import org.eclipse.sphinx.emf.compare.team.subversive.ui.operations.ExtendedCompareResourcesOperation;

public class CompareWithLatestFromRepositoryAction extends CompareWithLatestRevisionAction {

	@Override
	public void runImpl(IAction action) {

		IResource resource = this.getSelectedResources()[0];

		ILocalResource local = SVNRemoteStorage.instance().asLocalResourceAccessible(resource);
		IRepositoryResource remote = local.isCopied() ? SVNUtility.getCopiedFrom(resource) : SVNRemoteStorage.instance().asRepositoryResource(
				resource);
		remote.setSelectedRevision(SVNRevision.HEAD);

		ExtendedCompareResourcesOperation mainOp = new ExtendedCompareResourcesOperation(local, remote);
		DelegatingCompositeOperation op = new DelegatingCompositeOperation(mainOp.getId());
		op.add(new CorrectRevisionOperation(null, remote, local.getRevision(), resource));
		op.add(mainOp);
		if (SVNTeamPreferences.getHistoryBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(),
				SVNTeamPreferences.HISTORY_CONNECT_TO_COMPARE_WITH_NAME)) {
			op.add(new ShowHistoryViewOperation(resource, remote, ISVNHistoryView.COMPARE_MODE, ISVNHistoryView.COMPARE_MODE),
					new IActionOperation[] { mainOp });
		}
		runScheduled(op);
	}
}
