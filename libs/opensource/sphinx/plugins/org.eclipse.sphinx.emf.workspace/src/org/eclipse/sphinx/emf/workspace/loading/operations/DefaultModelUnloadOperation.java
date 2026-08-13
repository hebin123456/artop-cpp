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

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.loading.SchedulingRuleFactory;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

public class DefaultModelUnloadOperation extends AbstractLoadOperation implements IModelUnloadOperation {

	private Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload;
	private boolean memoryOptimized;

	public DefaultModelUnloadOperation(Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload, boolean memoryOptimized) {
		super(Messages.job_unloadingModelResources, null);
		this.resourcesToUnload = resourcesToUnload;
		this.memoryOptimized = memoryOptimized;
	}

	@Override
	public ISchedulingRule getRule() {
		return new SchedulingRuleFactory().createLoadSchedulingRule(getResourcesToUnload());
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IModelUnloadOperation#getResourcesToUnload()
	 */
	@Override
	public Map<TransactionalEditingDomain, Collection<Resource>> getResourcesToUnload() {
		return resourcesToUnload;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IModelUnloadOperation#isMemoryOptimized()
	 */
	@Override
	public boolean isMemoryOptimized() {
		return memoryOptimized;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		runUnloadModelResources(resourcesToUnload, memoryOptimized, monitor);
	}

	protected void runUnloadModelResources(Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload, boolean memoryOptimized,
			IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(resourcesToUnload);
		SubMonitor progress = SubMonitor.convert(monitor, getResourcesToUnloadCount(resourcesToUnload));
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		for (TransactionalEditingDomain editingDomain : resourcesToUnload.keySet()) {
			Collection<Resource> resourcesToUnloadInEditingDomain = resourcesToUnload.get(editingDomain);
			EcorePlatformUtil.unloadResources(editingDomain, resourcesToUnloadInEditingDomain, memoryOptimized,
					progress.newChild(resourcesToUnloadInEditingDomain.size()));

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		// Perform a full garbage collection
		ExtendedPlatform.performGarbageCollection();
	}

	@Override
	public boolean covers(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor) {
		return false;
	}

	@Override
	public boolean covers(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		return false;
	}

	protected int getResourcesToUnloadCount(Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload) {
		Assert.isNotNull(resourcesToUnload);

		int count = 0;
		for (TransactionalEditingDomain editingDomain : resourcesToUnload.keySet()) {
			count += resourcesToUnload.get(editingDomain).size();
		}
		return count;
	}
}
