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
package org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.workspace.loading.DefaultModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUpdateResourceURIOperation;

public class CustomModelLoadManager extends DefaultModelLoadManager {

	@Override
	public IFileLoadOperation createFileLoadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		return new CustomFileLoadOperation(files, mmDescriptor);
	}

	@Override
	public IFileReloadOperation createFileReloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
		return new CustomFileReloadOperation(files, mmDescriptor, memoryOptimized);
	}

	@Override
	public IFileUnloadOperation createFileUnloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
		return new CustomFileUnloadOperation(files, mmDescriptor, memoryOptimized);
	}

	@Override
	public IModelLoadOperation createModelLoadOperation(IModelDescriptor modelDescriptor, boolean includeReferencedScopes) {
		return new CustomModelLoadOperation(modelDescriptor, includeReferencedScopes);
	}

	@Override
	public IModelUnloadOperation createModelUnloadOperation(Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload,
			boolean memoryOptimized) {
		return new CustomModelUnloadOperation(resourcesToUnload, memoryOptimized);
	}

	@Override
	public IProjectLoadOperation createProjectLoadOperation(Collection<IProject> projects, boolean includeReferencedProjects,
			IMetaModelDescriptor mmDescriptor) {
		return new CustomProjectLoadOperation(projects, includeReferencedProjects, mmDescriptor);
	}

	@Override
	public IProjectReloadOperation createProjectReloadOperation(Collection<IProject> projects, boolean includeReferencedProjects,
			IMetaModelDescriptor mmDescriptor) {
		return new CustomProjectReloadOperation(projects, includeReferencedProjects, mmDescriptor);
	}

	@Override
	public IUnresolveUnreachableCrossProjectReferencesOperation createUnresolveUnreachableCrossProjectReferencesOperation(
			Collection<IProject> projectsWithUnreachableCrossRefrencesToUnresolve) {
		return new CustomUnresolveUnreachableCrossProjectReferencesOperation(projectsWithUnreachableCrossRefrencesToUnresolve);
	}

	@Override
	public IUpdateResourceURIOperation createUpdateResourceURIOperation(Map<IFile, IPath> filesToUpdate) {
		return new CustomUpdateResourceURIOperation(filesToUpdate);
	}
}
