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
package org.eclipse.sphinx.tests.emf.workspace.loading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptor;
import org.eclipse.sphinx.emf.scoping.DefaultResourceScope;
import org.eclipse.sphinx.emf.workspace.loading.DefaultModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultModelLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultModelUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultProjectLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultProjectReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultUpdateResourceURIOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUpdateResourceURIOperation;
import org.junit.Test;

/**
 * In order to successfully run this test make sure that <i>org.eclipse.sphinx.tests.emf.workspace.extension</i> plug-in
 * is not present in the same runtime, because that will register the <i>CustomModelLoadManager</i> which will override
 * the {@link DefaultModelLoadManager}, as a result this test will fail.
 */
public class DefaultModelLoadManagerTest {

	@Test
	public void testDefaultInstance() {
		final IModelLoadManager instance = ModelLoadManager.INSTANCE;
		assertNotNull(instance);
		assertEquals(instance.getClass(), DefaultModelLoadManager.class);
	}

	@Test
	public void testDefaultOpreations() {
		final ModelLoadManagerOperationAccessor instance = new ModelLoadManagerOperationAccessor();

		DefaultMetaModelDescriptor defaultMetaModelDescriptor = new DefaultMetaModelDescriptor("id", "ns", "name"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ModelDescriptor defaultModelDescriptor = new ModelDescriptor(defaultMetaModelDescriptor, null, new DefaultResourceScope());
		Collection<IFile> files = Collections.emptyList();
		Collection<IProject> projects = Collections.emptyList();
		Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload = Collections.emptyMap();
		Map<IFile, IPath> filesToUpdate = Collections.emptyMap();

		IFileLoadOperation fileLoadOperation = instance.createFileLoadOperation(files, defaultMetaModelDescriptor);
		IFileReloadOperation fileReloadOperation = instance.createFileReloadOperation(files, defaultMetaModelDescriptor, false);
		IFileUnloadOperation fileUnloadOperation = instance.createFileUnloadOperation(files, defaultMetaModelDescriptor, false);
		IModelLoadOperation modelLoadOperation = instance.createModelLoadOperation(defaultModelDescriptor, false);
		IModelUnloadOperation modelUnloadOperation = instance.createModelUnloadOperation(resourcesToUnload, false);
		IProjectLoadOperation projectLoadOperation = instance.createProjectLoadOperation(projects, false, defaultMetaModelDescriptor);
		IProjectReloadOperation projectReloadOperation = instance.createProjectReloadOperation(projects, false, defaultMetaModelDescriptor);
		IUnresolveUnreachableCrossProjectReferencesOperation unresolveUnreachableCrossProjectReferencesOperation = instance
				.createUnresolveUnreachableCrossProjectReferencesOperation(projects);
		IUpdateResourceURIOperation updateResourceURIOperation = instance.createUpdateResourceURIOperation(filesToUpdate);

		assertNotNull(fileLoadOperation);
		assertNotNull(fileReloadOperation);
		assertNotNull(fileUnloadOperation);
		assertNotNull(modelLoadOperation);
		assertNotNull(modelUnloadOperation);
		assertNotNull(projectLoadOperation);
		assertNotNull(projectReloadOperation);
		assertNotNull(unresolveUnreachableCrossProjectReferencesOperation);
		assertNotNull(updateResourceURIOperation);

		assertEquals(fileLoadOperation.getClass(), DefaultFileLoadOperation.class);
		assertEquals(fileReloadOperation.getClass(), DefaultFileReloadOperation.class);
		assertEquals(fileUnloadOperation.getClass(), DefaultFileUnloadOperation.class);
		assertEquals(modelLoadOperation.getClass(), DefaultModelLoadOperation.class);
		assertEquals(modelUnloadOperation.getClass(), DefaultModelUnloadOperation.class);
		assertEquals(projectLoadOperation.getClass(), DefaultProjectLoadOperation.class);
		assertEquals(projectReloadOperation.getClass(), DefaultProjectReloadOperation.class);
		assertEquals(unresolveUnreachableCrossProjectReferencesOperation.getClass(),
				DefaultUnresolveUnreachableCrossProjectReferencesOperation.class);
		assertEquals(updateResourceURIOperation.getClass(), DefaultUpdateResourceURIOperation.class);
	}

	private static class DefaultMetaModelDescriptor extends AbstractMetaModelDescriptor {

		protected DefaultMetaModelDescriptor(String identifier, String namespace, String name) {
			super(identifier, namespace, name, null);
		}

		/*
		 * @see org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor#getDefaultContentTypeId()
		 */
		@Override
		public String getDefaultContentTypeId() {
			return ""; //$NON-NLS-1$
		}
	}

	private static class ModelLoadManagerOperationAccessor extends DefaultModelLoadManager {
		@Override
		public IFileLoadOperation createFileLoadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
			return super.createFileLoadOperation(files, mmDescriptor);
		}

		@Override
		public IFileReloadOperation createFileReloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
			return super.createFileReloadOperation(files, mmDescriptor, memoryOptimized);
		}

		@Override
		public IFileUnloadOperation createFileUnloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
			return super.createFileUnloadOperation(files, mmDescriptor, memoryOptimized);
		}

		@Override
		public IModelLoadOperation createModelLoadOperation(IModelDescriptor modelDescriptor, boolean includeReferencedScopes) {
			return super.createModelLoadOperation(modelDescriptor, includeReferencedScopes);
		}

		@Override
		public IModelUnloadOperation createModelUnloadOperation(Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload,
				boolean memoryOptimized) {
			return super.createModelUnloadOperation(resourcesToUnload, memoryOptimized);
		}

		@Override
		public IProjectLoadOperation createProjectLoadOperation(Collection<IProject> projects, boolean includeReferencedProjects,
				IMetaModelDescriptor mmDescriptor) {
			return super.createProjectLoadOperation(projects, includeReferencedProjects, mmDescriptor);
		}

		@Override
		public IProjectReloadOperation createProjectReloadOperation(Collection<IProject> projects, boolean includeReferencedProjects,
				IMetaModelDescriptor mmDescriptor) {
			return super.createProjectReloadOperation(projects, includeReferencedProjects, mmDescriptor);
		}

		@Override
		public IUnresolveUnreachableCrossProjectReferencesOperation createUnresolveUnreachableCrossProjectReferencesOperation(
				Collection<IProject> projectsWithUnreachableCrossRefrencesToUnresolve) {
			return super.createUnresolveUnreachableCrossProjectReferencesOperation(projectsWithUnreachableCrossRefrencesToUnresolve);
		}

		@Override
		public IUpdateResourceURIOperation createUpdateResourceURIOperation(Map<IFile, IPath> filesToUpdate) {
			return super.createUpdateResourceURIOperation(filesToUpdate);
		}
	}
}
