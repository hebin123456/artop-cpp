/**
 * <copyright>
 *
 * Copyright (c) 2021 Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Siemens - [574930] Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading.operations;

import java.util.Collection;

import org.eclipse.core.resources.IFile;

/**
 * Common interface for ModelLoadManager file load operations.
 * <ul>
 * <li>{@link IFileLoadOperation}</li>
 * <li>{@link IFileReloadOperation}</li>
 * <li>{@link IFileUnloadOperation}</li>
 * </ul>
 */
public interface IFileLoadCommonOperation extends ILoadOperation {

	/**
	 * @return all files added to the operation.
	 */
	Collection<IFile> getFiles();

	/**
	 * Add all files to the operation. If a file is already added to the operation, it will be skipped.
	 *
	 * @param files
	 *            to add to the operation.
	 */
	void addFiles(Collection<IFile> files);

}