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

public class DefaultFileLoadOperation extends AbstractFileLoadOperation implements IFileLoadOperation {

	/**
	 * Constructor.
	 *
	 * @param files
	 *            The list of files this loading operation is supposed to cover.
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} considered for reloading.
	 */
	public DefaultFileLoadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		super(Messages.job_loadingModelResources, files, mmDescriptor);
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		runDetectAndLoadModelFiles(getFiles(), getMetaModelDescriptor(), monitor);
	}
}
