/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sphinx.emf.internal.metamodel.IFileMetaModelDescriptorCache;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.ReflectUtil;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings({ "nls", "restriction" })
public class ModelManagementTest extends DefaultIntegrationTestCase {

	public void testCreateWorkspaceLoadModelsAndUnloadModelsDeleteResources() throws Exception {

		int modelsCount = ModelDescriptorRegistry.INSTANCE.getModels(ResourcesPlugin.getWorkspace().getRoot()).size();

		// All non dot files must be present in file meta-model descriptor cache
		assertInFileMetaModelDescriptorCache(getAllModelFiles());
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Unload all models from one project
		synchronizedUnloadProject(refWks.hbProject20_A, false);
		// All models of unloaded project must stay in place
		assertWorkspaceModelsSizeEquals(modelsCount);
		// Underlying files must still be present in file meta-model descriptor cache
		assertInFileMetaModelDescriptorCache(getAllModelFiles());
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Delete one file from one project
		IFile hbProject20_AFile = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		synchronizedDeleteFile(hbProject20_AFile);
		// Deleted file must no longer exist
		assertFalse(hbProject20_AFile.exists());
		// No more models must have gone yet
		assertWorkspaceModelsSizeEquals(modelsCount);
		// Deleted file must have been removed from file meta-model descriptor cache
		assertNotInFileMetaModelDescriptorCache(hbProject20_AFile);
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Delete one project
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A).size();
		synchronizedDeleteProject(refWks.hbProject20_A);
		// Deleted project must no longer exist
		assertFalse(refWks.hbProject20_A.exists());

		// No more models must have gone yet
		assertWorkspaceModelsSizeEquals(modelsCount);
		// All files of deleted project must have been removed from file meta-model descriptor cache
		assertNotInFileMetaModelDescriptorCache(ExtendedPlatform.getAllFiles(refWks.hbProject20_A, false));
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Unload all models from all other projects
		synchronizedUnloadAllProjects();
		// All models should stay in place
		assertWorkspaceModelsSizeEquals(modelsCount);
		// Underlying files must still be present in file meta-model descriptor cache
		assertInFileMetaModelDescriptorCache(getAllModelFiles());
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Delete all other projects
		synchronizedDeleteWorkspace();
		// All models must have gone
		assertWorkspaceModelsSizeEquals(0);
		// All files must be removed from file meta-model descriptor cache
		assertFileMetaModelDescriptorCacheSizeEquals(0);
	}

	public void testCreateWorkspaceLoadModelsAndDeleteResources() throws Exception {

		int modelsCount = ModelDescriptorRegistry.INSTANCE.getModels(ResourcesPlugin.getWorkspace().getRoot()).size();

		// All non dot files must be present in file meta-model descriptor cache
		assertInFileMetaModelDescriptorCache(getAllModelFiles());
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Delete one file from one project
		IFile hbProject20_AFile = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		synchronizedDeleteFile(hbProject20_AFile);
		// Deleted file must no longer exist
		assertFalse(hbProject20_AFile.exists());
		// No models must have gone yet
		assertWorkspaceModelsSizeEquals(modelsCount);
		// Deleted file must have been removed from file meta-model descriptor cache
		assertNotInFileMetaModelDescriptorCache(hbProject20_AFile);
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Delete one project
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_A).size();
		synchronizedDeleteProject(refWks.hbProject20_A);
		waitForModelLoading();
		// Deleted project must no longer exist
		assertFalse(refWks.hbProject20_A.exists());
		// All models of deleted project must have gone
		assertWorkspaceModelsSizeEquals(modelsCount);
		// All files of deleted project must have been removed from file meta-model descriptor cache
		assertNotInFileMetaModelDescriptorCache(ExtendedPlatform.getAllFiles(refWks.hbProject20_A, false));
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);

		// Delete all other projects
		synchronizedDeleteWorkspace();
		// All models must have gone
		assertWorkspaceModelsSizeEquals(0);
		// All files must be removed from file meta-model descriptor cache
		assertFileMetaModelDescriptorCacheSizeEquals(0);
	}

	public void testCreateWorkspaceLoadModelsAndCloseProjects() throws Exception {

		int modelsCount = ModelDescriptorRegistry.INSTANCE.getModels(ResourcesPlugin.getWorkspace().getRoot()).size();

		// Close projects referencing each other starting with outer most referencing project and going to inner most
		// referenced project
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_E).size();
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_D).size();
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_E).size();
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_D).size();
		synchronizedCloseProject(refWks.hbProject20_E);
		synchronizedCloseProject(refWks.hbProject20_D);
		synchronizedCloseProject(refWks.hbProject10_E);
		synchronizedCloseProject(refWks.hbProject10_D);

		Thread.sleep(1000);

		// All models of closed projects must have gone
		assertWorkspaceModelsSizeEquals(modelsCount);
	}

	public void testCreateWorkspaceLoadModelsAndDeleteProjects() throws Exception {

		int modelsCount = ModelDescriptorRegistry.INSTANCE.getModels(ResourcesPlugin.getWorkspace().getRoot()).size();

		// Close projects referencing each other starting with outer most referencing project and going to inner most
		// referenced project
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_E).size();
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject20_D).size();
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_E).size();
		modelsCount -= ModelDescriptorRegistry.INSTANCE.getModels(refWks.hbProject10_D).size();
		synchronizedDeleteProject(refWks.hbProject20_E);
		synchronizedDeleteProject(refWks.hbProject20_D);
		synchronizedDeleteProject(refWks.hbProject10_E);
		synchronizedDeleteProject(refWks.hbProject10_D);

		Thread.sleep(1000);

		// All models of closed projects must have gone
		assertWorkspaceModelsSizeEquals(modelsCount);
	}

	public void testCreateWorkspaceLoadModels() throws Exception {

		// All non dot files must be present in file meta-model descriptor cache
		assertInFileMetaModelDescriptorCache(getAllModelFiles());
		assertOldFileMetaModelDescriptorCacheSizeEquals(0);
	}

	private static final String FIELD_NAME_META_MODEL_DESCRIPTOR_CACHE = "fFileMetaModelDescriptors";

	private static final String FIELD_NAME_OLD_META_MODEL_DESCRIPTOR_CACHE = "fOldFileMetaModelDescriptors";

	private void assertInFileMetaModelDescriptorCache(Collection<IFile> files) throws Exception {
		for (IFile file : files) {
			assertInFileMetaModelDescriptorCache(file);
		}
	}

	private void assertInFileMetaModelDescriptorCache(IFile file) throws Exception {
		IFileMetaModelDescriptorCache cache = (IFileMetaModelDescriptorCache) MetaModelDescriptorRegistry.INSTANCE
				.getAdapter(IFileMetaModelDescriptorCache.class);
		@SuppressWarnings("unchecked")
		Map<IFile, IMetaModelDescriptor> fileMetaModelDescriptorCache = (Map<IFile, IMetaModelDescriptor>) ReflectUtil.getInvisibleFieldValue(cache,
				FIELD_NAME_META_MODEL_DESCRIPTOR_CACHE);
		assertTrue(file.getFullPath() + " not in file meta-model descriptor cache", fileMetaModelDescriptorCache.containsKey(file));
	}

	private void assertNotInFileMetaModelDescriptorCache(Collection<IFile> files) throws Exception {
		for (IFile file : files) {
			assertNotInFileMetaModelDescriptorCache(file);
		}
	}

	private void assertNotInFileMetaModelDescriptorCache(IFile file) throws Exception {
		IFileMetaModelDescriptorCache cache = (IFileMetaModelDescriptorCache) MetaModelDescriptorRegistry.INSTANCE
				.getAdapter(IFileMetaModelDescriptorCache.class);
		@SuppressWarnings("unchecked")
		Map<IFile, IMetaModelDescriptor> fileMetaModelDescriptorCache = (Map<IFile, IMetaModelDescriptor>) ReflectUtil.getInvisibleFieldValue(cache,
				FIELD_NAME_META_MODEL_DESCRIPTOR_CACHE);
		assertFalse(file.getFullPath() + " in file meta-model descriptor cache", fileMetaModelDescriptorCache.containsKey(file));
	}

	private void assertFileMetaModelDescriptorCacheSizeEquals(int expected) throws Exception {
		IFileMetaModelDescriptorCache cache = (IFileMetaModelDescriptorCache) MetaModelDescriptorRegistry.INSTANCE
				.getAdapter(IFileMetaModelDescriptorCache.class);
		@SuppressWarnings("unchecked")
		Map<IFile, IMetaModelDescriptor> fileMetaModelDescriptorCache = (Map<IFile, IMetaModelDescriptor>) ReflectUtil.getInvisibleFieldValue(cache,
				FIELD_NAME_META_MODEL_DESCRIPTOR_CACHE);

		if (expected == 0 && fileMetaModelDescriptorCache.size() > 0) {
			System.err.println("Unexpected cached file meta-model descriptors:");
			synchronized (fileMetaModelDescriptorCache) {
				for (IFile file : fileMetaModelDescriptorCache.keySet()) {
					System.err.println("  " + file.getFullPath() + ": " + fileMetaModelDescriptorCache.get(file));
				}
			}
		}

		assertEquals(expected, fileMetaModelDescriptorCache.size());
	}

	private void assertOldFileMetaModelDescriptorCacheSizeEquals(int expected) throws Exception {
		IFileMetaModelDescriptorCache cache = (IFileMetaModelDescriptorCache) MetaModelDescriptorRegistry.INSTANCE
				.getAdapter(IFileMetaModelDescriptorCache.class);
		@SuppressWarnings("unchecked")
		Map<IFile, IMetaModelDescriptor> oldFileMetaModelDescriptorCache = (Map<IFile, IMetaModelDescriptor>) ReflectUtil.getInvisibleFieldValue(
				cache, FIELD_NAME_OLD_META_MODEL_DESCRIPTOR_CACHE);

		if (expected == 0 && oldFileMetaModelDescriptorCache.size() > 0) {
			System.err.println("Unexpected cached old file meta-model descriptors:");
			synchronized (oldFileMetaModelDescriptorCache) {
				for (IFile file : oldFileMetaModelDescriptorCache.keySet()) {
					System.err.println("  " + file.getFullPath() + ": " + oldFileMetaModelDescriptorCache.get(file));
				}
			}
		}

		assertEquals(expected, oldFileMetaModelDescriptorCache.size());
	}
}
