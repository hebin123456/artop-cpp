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
import org.eclipse.core.resources.IProject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.platform.operations.IWorkspaceOperation;

/**
 * Common interface for ModelLoadManager load operations.
 */
public interface ILoadOperation extends IWorkspaceOperation {

	/**
	 * @param projects
	 *            The {@linkplain IProject project}s that this model load job may cover.
	 * @param includeReferencedProjects
	 *            If <b><code>true</code></b>, consider referenced projects.
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} of the model which has been asked for
	 *            loading.
	 * @return
	 *         <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if this job covers the loading of the specified projects with the
	 *         specified meta-model descriptor;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean covers(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor);

	/**
	 * @param files
	 *            The list of files this loading job is supposed to cover.
	 * @param mmDescriptor
	 *            The {@linkplain IMetaModelDescriptor meta-model descriptor} considered for loading.
	 * @return
	 *         <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if this job covers the loading of the specified files with the
	 *         specified meta-model descriptor;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean covers(Collection<IFile> files, IMetaModelDescriptor mmDescriptor);

	/**
	 * @return the MetaModelDescriptor used in the operation. It should be set in the constructor of the operation.
	 */
	IMetaModelDescriptor getMetaModelDescriptor();

}