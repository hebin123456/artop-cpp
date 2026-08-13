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
package org.eclipse.sphinx.tests.emf.workspace.extension.loading;

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
import org.eclipse.sphinx.emf.model.ModelDescriptor;
import org.eclipse.sphinx.emf.scoping.DefaultResourceScope;
import org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUpdateResourceURIOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomFileLoadOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomFileReloadOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomFileUnloadOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomModelLoadManager;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomModelLoadOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomModelUnloadOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomProjectLoadOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomProjectReloadOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.tests.emf.workspace.extension.loading.custom.CustomUpdateResourceURIOperation;
import org.junit.Test;

public class ModelLoadManagerExtensionTest {

	@Test
	public void testCustomModelLoadManager() {
		final IModelLoadManager instance = ModelLoadManager.INSTANCE;
		assertNotNull(instance);
		assertEquals(instance.getClass(), CustomModelLoadManager.class);
	}

	@Test
	public void testDefaultOpreations() {
		final CustomModelLoadManager instance = new CustomModelLoadManager();

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

		assertEquals(fileLoadOperation.getClass(), CustomFileLoadOperation.class);
		assertEquals(fileReloadOperation.getClass(), CustomFileReloadOperation.class);
		assertEquals(fileUnloadOperation.getClass(), CustomFileUnloadOperation.class);
		assertEquals(modelLoadOperation.getClass(), CustomModelLoadOperation.class);
		assertEquals(modelUnloadOperation.getClass(), CustomModelUnloadOperation.class);
		assertEquals(projectLoadOperation.getClass(), CustomProjectLoadOperation.class);
		assertEquals(projectReloadOperation.getClass(), CustomProjectReloadOperation.class);
		assertEquals(unresolveUnreachableCrossProjectReferencesOperation.getClass(), CustomUnresolveUnreachableCrossProjectReferencesOperation.class);
		assertEquals(updateResourceURIOperation.getClass(), CustomUpdateResourceURIOperation.class);
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
}
