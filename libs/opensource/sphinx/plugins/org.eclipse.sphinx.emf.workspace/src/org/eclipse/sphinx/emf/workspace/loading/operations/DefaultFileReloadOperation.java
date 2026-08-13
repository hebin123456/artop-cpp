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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;

public class DefaultFileReloadOperation extends AbstractFileLoadOperation implements IFileReloadOperation {

	private boolean memoryOptimized;

	/**
	 * Constructor.
	 *
	 * @param files
	 *            The list of files this reloading operation is supposed to cover.
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} considered for reloading.
	 * @param memoryOptimized
	 *            Will activate the memory optimization option for unloading the resource. This is only available if the
	 *            resource is an XMLResource.
	 */
	public DefaultFileReloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
		super(Messages.job_reloadingModelResources, files, mmDescriptor);
		this.memoryOptimized = memoryOptimized;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		runDetectAndReloadModelFiles(getFiles(), getMetaModelDescriptor(), memoryOptimized, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.IFileReloadOperation#isMemoryOptimized()
	 */
	@Override
	public boolean isMemoryOptimized() {
		return memoryOptimized;
	}
}
