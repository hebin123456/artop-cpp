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
package org.eclipse.sphinx.emf.internal.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.sphinx.emf.internal.metamodel.InternalMetaModelDescriptorRegistry;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizerDelegate;

public class BasicModelDescriptorSynchronizerDelegate extends AbstractResourceSynchronizerDelegate<IModelDescriptorSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final BasicModelDescriptorSynchronizerDelegate INSTANCE = new BasicModelDescriptorSynchronizerDelegate();

	@Override
	public void handleProjectRenamed(int eventType, IProject oldProject, IProject newProject) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addProjectToMoveModelDescriptorsFor(oldProject, newProject);
		}
	}

	@Override
	public void handleFileAdded(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addFileToAddModelDescriptorFor(file);
		}
	}

	@Override
	public void handleProjectClosed(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addProjectToRemoveModelDescriptorsFor(project);
		}
	}

	@Override
	public void handleProjectRemoved(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addProjectToRemoveModelDescriptorsFor(project);
		}
	}

	@Override
	public void handleFileChanged(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			// Remove entry for changed file from meta-model descriptor cache
			/*
			 * !! Important Note !! This should normally be the business of MetaModelDescriptorCacheUpdater. However, we
			 * have to do so here as well because we depend on that cached metamodel descriptors are up to date but
			 * cannot know which of both BasicModelDescriptorSynchronizerDelegate or MetaModelDescriptorCacheUpdater
			 * gets called first.
			 */
			InternalMetaModelDescriptorRegistry.INSTANCE.removeCachedDescriptor(file);

			syncRequest.addFileToUpdateModelDescriptorFor(file);
		}
	}

	@Override
	public void handleFileMoved(int eventType, IFile oldFile, IFile newFile) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			// Remove entry for old file from meta-model descriptor cache and add an equivalent entry
			// for new file
			/*
			 * !! Important Note !! This should normally be the business of MetaModelDescriptorCacheUpdater. However, we
			 * have to do so here as well because we depend on that cached metamodel descriptors are up to date but
			 * cannot know which of both BasicModelDescriptorSynchronizerDelegate or MetaModelDescriptorCacheUpdater
			 * gets called first.
			 */
			InternalMetaModelDescriptorRegistry.INSTANCE.moveCachedDescriptor(oldFile, newFile);

			// Remove descriptor for model behind old file from ModelDescriptorRegistry if it is the
			// last file of the that model
			syncRequest.addFileToRemoveModelDescriptorFor(oldFile);
			syncRequest.addFileToAddModelDescriptorFor(newFile);
		}
	}

	@Override
	public void handleFileRemoved(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			// Remove entry for removed file from meta-model descriptor cache
			/*
			 * !! Important Note !! This should normally be the business of MetaModelDescriptorCacheUpdater. However, we
			 * have to do so here as well because we depend on that cached metamodel descriptors are up to date but
			 * cannot know which of both BasicModelDescriptorSynchronizerDelegate or MetaModelDescriptorCacheUpdater
			 * gets called first.
			 */
			InternalMetaModelDescriptorRegistry.INSTANCE.removeCachedDescriptor(file);

			// Remove descriptor for model behind removed file from ModelDescriptorRegistry if it is the
			// last file of the that model
			syncRequest.addFileToRemoveModelDescriptorFor(file);
		}
	}
}
