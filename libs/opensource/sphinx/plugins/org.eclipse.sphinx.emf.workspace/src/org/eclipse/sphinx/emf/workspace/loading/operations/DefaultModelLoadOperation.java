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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class DefaultModelLoadOperation extends AbstractLoadOperation implements IModelLoadOperation {

	private IModelDescriptor modelDescriptor;
	private boolean includeReferencedScopes;
	private Collection<IFile> persistedFiles;

	public DefaultModelLoadOperation(IModelDescriptor modelDescriptor, boolean includeReferencedScopes) {
		super(Messages.job_loadingModelResources, modelDescriptor.getMetaModelDescriptor());
		this.modelDescriptor = modelDescriptor;
		this.includeReferencedScopes = includeReferencedScopes;
		persistedFiles = getModelDescriptor().getPersistedFiles(includeReferencedScopes);
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		if (!persistedFiles.isEmpty()) {
			runCollectAndLoadModelFiles(getModelDescriptor().getEditingDomain(), persistedFiles, monitor);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IModelLoadOperation#getModelDescriptor()
	 */
	@Override
	public IModelDescriptor getModelDescriptor() {
		return modelDescriptor;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IModelLoadOperation#isIncludeReferencedScopes()
	 */
	@Override
	public boolean isIncludeReferencedScopes() {
		return includeReferencedScopes;
	}

	protected void runCollectAndLoadModelFiles(TransactionalEditingDomain editingDomain, Collection<IFile> files, IProgressMonitor monitor)
			throws OperationCanceledException {
		Assert.isNotNull(files);

		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_loadingModelFiles, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Collect model files to load
		SubMonitor collectProgress = progress.newChild(1).setWorkRemaining(files.size());
		Map<TransactionalEditingDomain, Collection<IFile>> filesToLoad = new HashMap<TransactionalEditingDomain, Collection<IFile>>();
		for (IFile file : files) {
			try {
				// Exclude inaccessible files
				if (file.isAccessible()) {
					// Ignore files that already have been loaded
					if (!EcorePlatformUtil.isFileLoaded(file)) {
						// Retrieve already existing load file request for given editing domain
						Collection<IFile> filesToLoadInEditingDomain = filesToLoad.get(editingDomain);
						if (filesToLoadInEditingDomain == null) {
							filesToLoadInEditingDomain = new HashSet<IFile>();
							filesToLoad.put(editingDomain, filesToLoadInEditingDomain);
						}
						// Add current file to load file request
						filesToLoadInEditingDomain.add(file);
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}

			collectProgress.worked(1);
			if (collectProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		// Nothing to load?
		if (filesToLoad.size() == 0) {
			progress.done();
			// TODO Surround with appropriate tracing option
			// System.out.println("[ModelLoadManager#runCollectAndLoadModelFiles()] No model files to be loaded");
			return;
		}
		runLoadModelFiles(filesToLoad, progress.newChild(99));
		// TODO Surround with appropriate tracing option
		// System.out.println("[ModelLoadManager#runCollectAndLoadModelFiles()] Loaded " +
		// getFilesToLoadCount(filesToLoad) + " model file(s)");
	}

	@Override
	public ISchedulingRule getRule() {
		return getSchedulingRuleFactory().createLoadSchedulingRule(persistedFiles);
	}

	@Override
	public boolean covers(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor) {
		return false;
	}

	@Override
	public boolean covers(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		int filesComparison = compare(persistedFiles, files);
		int mmDescriptorsComparison = compare(getMetaModelDescriptor(), mmDescriptor);
		if (filesComparison == EQUAL) {
			if (mmDescriptorsComparison == EQUAL || mmDescriptorsComparison == GREATER_THAN) {
				return true;
			}
		} else if (filesComparison == GREATER_THAN) {
			if (mmDescriptorsComparison == EQUAL || mmDescriptorsComparison == GREATER_THAN) {
				return true;
			}
		}
		return false;
	}
}
