/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading.operations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.loading.SchedulingRuleFactory;
import org.eclipse.sphinx.platform.operations.AbstractWorkspaceOperation;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class DefaultUpdateResourceURIOperation extends AbstractWorkspaceOperation implements IUpdateResourceURIOperation {

	private Map<IFile, IPath> filesToUpdate;

	public DefaultUpdateResourceURIOperation(Map<IFile, IPath> filesToUpdate) {
		super(Messages.job_updatingResourceURIs);
		this.filesToUpdate = filesToUpdate;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		runDetectAndUpdateResourceURIs(filesToUpdate, monitor);
	}

	@Override
	public ISchedulingRule getRule() {
		return new SchedulingRuleFactory().createLoadSchedulingRule(filesToUpdate.keySet());
	}

	protected void runDetectAndUpdateResourceURIs(Map<IFile, IPath> filesToUpdate, IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(filesToUpdate);
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_updatingResourceURIs, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		Map<TransactionalEditingDomain, Map<IFile, IPath>> filesToUpdateResourceURIFor = detectFilesToUpdateResourceURIFor(filesToUpdate,
				progress.newChild(10));

		// Nothing to update?
		if (filesToUpdateResourceURIFor.size() == 0) {
			progress.done();
			return;
		}

		runUpdateResourceURIs(filesToUpdateResourceURIFor, progress.newChild(90));
	}

	protected void runUpdateResourceURIs(Map<TransactionalEditingDomain, Map<IFile, IPath>> filesToUpdateResourceURIFor, IProgressMonitor monitor)
			throws OperationCanceledException {
		Assert.isNotNull(filesToUpdateResourceURIFor);
		SubMonitor progress = SubMonitor.convert(monitor, filesToUpdateResourceURIFor.keySet().size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		for (TransactionalEditingDomain editingDomain : filesToUpdateResourceURIFor.keySet()) {
			updateResourceURIsInEditingDomain(editingDomain, filesToUpdateResourceURIFor.get(editingDomain), progress.newChild(1));
		}
	}

	protected void updateResourceURIsInEditingDomain(final TransactionalEditingDomain editingDomain,
			final Map<IFile, IPath> filesToUpdateResourceURIForInEditingDomain, final IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(filesToUpdateResourceURIForInEditingDomain);

		try {
			editingDomain.runExclusive(new Runnable() {
				@Override
				public void run() {
					SubMonitor progress = SubMonitor.convert(monitor, filesToUpdateResourceURIForInEditingDomain.size());
					if (progress.isCanceled()) {
						throw new OperationCanceledException();
					}

					for (IFile oldFile : filesToUpdateResourceURIForInEditingDomain.keySet()) {
						progress.subTask(NLS.bind(Messages.subtask_updatingResourceURI, oldFile.getFullPath().toString()));

						URI oldURI = EcorePlatformUtil.createURI(oldFile.getFullPath());
						Resource resource = editingDomain.getResourceSet().getResource(oldURI, false);
						if (resource != null) {
							IPath newPath = filesToUpdateResourceURIForInEditingDomain.get(oldFile);
							URI newURI = EcorePlatformUtil.createURI(newPath);
							resource.setURI(newURI);
						}

						progress.worked(1);
						progress.subTask(""); //$NON-NLS-1$
						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}
						editingDomain.yield();
					}
				}
			});
		} catch (InterruptedException ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
	}

	protected Map<TransactionalEditingDomain, Map<IFile, IPath>> detectFilesToUpdateResourceURIFor(Map<IFile, IPath> files,
			IProgressMonitor monitor) {
		Assert.isNotNull(files);
		SubMonitor progress = SubMonitor.convert(monitor, files.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		Map<TransactionalEditingDomain, Map<IFile, IPath>> filesToUpdateResourceURIFor = new HashMap<TransactionalEditingDomain, Map<IFile, IPath>>();
		for (final IFile oldFile : files.keySet()) {
			try {
				/*
				 * Performance optimization: Check if current file is a potential model file inside an existing scope.
				 * This helps excluding obvious non-model files and model files that are out of scope right away and
				 * avoids potentially lengthy but useless processing of the same.
				 */
				if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(oldFile)) {
					progress.subTask(NLS.bind(Messages.subtask_analyzingFile, oldFile.getFullPath()));

					TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getCurrentEditingDomain(oldFile);
					if (editingDomain != null) {
						Map<IFile, IPath> filesToUpdateResourceURIForInEditingDomain = filesToUpdateResourceURIFor.get(editingDomain);
						if (filesToUpdateResourceURIForInEditingDomain == null) {
							filesToUpdateResourceURIForInEditingDomain = new HashMap<IFile, IPath>();
							filesToUpdateResourceURIFor.put(editingDomain, filesToUpdateResourceURIForInEditingDomain);
						}
						filesToUpdateResourceURIForInEditingDomain.put(oldFile, files.get(oldFile));
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}

			progress.worked(1);
			progress.subTask(""); //$NON-NLS-1$
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		return filesToUpdateResourceURIFor;
	}
}
