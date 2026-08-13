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

import org.eclipse.core.resources.IProject;

/**
 * Common interface for ModelLoadManager project load operations.
 * <ul>
 * <li>{@link IProjectLoadOperation}</li>
 * <li>{@link IProjectReloadOperation}</li>
 * </ul>
 */
public interface IProjectLoadCommonOperation extends ILoadOperation {

	/**
	 * @return all projects added to the operation.
	 */
	Collection<IProject> getProjects();

	/**
	 * Flag to also include the referenced projects in the operation. It should be set in the constructor of the
	 * operation.
	 *
	 * @return true if the referenced projects are also included, otherwise false.
	 */
	boolean isIncludeReferencedProjects();

	/**
	 * Add all projects to the operation. If a project is already added to the operation, it will be skipped.
	 *
	 * @param projects
	 *            to add to the operation.
	 */
	void addProjects(Collection<IProject> projects);

}