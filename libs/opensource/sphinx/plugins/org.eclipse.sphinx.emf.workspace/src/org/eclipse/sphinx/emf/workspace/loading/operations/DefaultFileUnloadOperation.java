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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;

public class DefaultFileUnloadOperation extends AbstractFileLoadOperation implements IFileUnloadOperation {

	private boolean memoryOptimized;

	public DefaultFileUnloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
		super(Messages.job_unloadingModelResources, files, mmDescriptor);
		this.memoryOptimized = memoryOptimized;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		runDetectAndUnloadModelFiles(getFiles(), getMetaModelDescriptor(), memoryOptimized, monitor);
	}

	protected void runDetectAndUnloadModelFiles(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized,
			IProgressMonitor monitor) throws OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_unloadingModelFiles, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		Map<TransactionalEditingDomain, Collection<IFile>> filesToUnload = detectFilesToUnload(files, mmDescriptor, progress.newChild(10));

		// Nothing to unload?
		if (filesToUnload.size() == 0) {
			progress.done();

			// TODO Surround with appropriate tracing option
			// System.out.println("[ModelLoadManager#runDetectAndUnloadModelFiles()] No model files to be unloaded");
			return;
		}

		runUnloadModelFiles(filesToUnload, memoryOptimized, progress.newChild(90));

		// TODO Surround with appropriate tracing option
		// System.out.println("[ModelLoadManager#runDetectAndUnloadModelFiles()] Unloaded " +
		// getFilesToUnloadCount(filesToUnload) + " model file(s)");
	}
}
