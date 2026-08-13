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
package org.eclipse.sphinx.emf.workspace.internal.syncing;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.internal.metamodel.InternalMetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.syncing.IModelSyncRequest;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizerDelegate;

public class BasicModelSynchronizerDelegate extends AbstractResourceSynchronizerDelegate<IModelSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final BasicModelSynchronizerDelegate INSTANCE = new BasicModelSynchronizerDelegate();

	@Override
	public void handleProjectCreated(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			// When a project is created only the project description file (.project) is added to it and signaled by
			// this IResourceChangeEvent.POST_CHANGE event; so it's too early for requesting a project load
		}
	}

	@Override
	public void handleProjectOpened(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addProjectToLoad(project);
		}
	}

	@Override
	public void handleProjectDescriptionChanged(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addProjectToUnresolveUnreachableCrossReferencesFor(project);
		}
	}

	@Override
	public void handleProjectClosed(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addProjectToUnload(project);
		}
	}

	@Override
	public void handleProjectRemoved(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addProjectToUnload(project);
		}
	}

	@Override
	public void handleFileAdded(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addFileToLoad(file);
		}
	}

	@Override
	public void handleFileChanged(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			/**
			 * !! Important Note !! We must not try to obtain the model resource behind the changed file in the present
			 * execution context. This would require requesting exclusive access to underlying editing domain by
			 * creating a read transaction. However, the workspace is locked during resource change event processing.
			 * Any attempt of obtaining exclusive editing domain access while this is the case would therefore introduce
			 * a major risk of deadlocks. Some other thread might be waiting for exclusive workspace access but already
			 * have exclusive editing domain access.
			 */
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getMappedEditingDomain(file);
			URI uri = EcorePlatformUtil.createURI(file.getFullPath());
			if (!SaveIndicatorUtil.isSaved(editingDomain, uri)) {
				syncRequest.addFileToReload(file);
			}
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

			// Retrieve the file's old and new model descriptors
			IModelDescriptor oldModelDescriptor = ModelDescriptorRegistry.INSTANCE.getOldModel(oldFile);
			IModelDescriptor newModelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(newFile);

			// Unload moved file if it is no longer a model file (e.g., because its extension has been changed)
			if (oldModelDescriptor != null && oldModelDescriptor.getScope() != null
					&& (newModelDescriptor == null || newModelDescriptor.getScope() == null)) {
				syncRequest.addFileToUnload(oldFile);
			}
			// Load moved file if it was no model file before (e.g., because its extension has been changed)
			else if ((oldModelDescriptor == null || oldModelDescriptor.getScope() == null) && newModelDescriptor != null
					&& newModelDescriptor.getScope() != null) {
				syncRequest.addFileToLoad(newFile);
			}
			// Was moved file a model file before and still is?
			else if (oldModelDescriptor != null && oldModelDescriptor.getScope() != null && newModelDescriptor != null
					&& newModelDescriptor.getScope() != null) {
				// Reload file if it now belongs to a different scope than before
				if (!newModelDescriptor.getScope().belongsTo(oldFile, true) || !oldModelDescriptor.getScope().belongsTo(newFile, true)) {
					syncRequest.addFileToUnload(oldFile);
					syncRequest.addFileToLoad(newFile);
				}

				// Just update URI of resource behind moved file otherwise
				else {
					syncRequest.addFileToUpdateResourceURIFor(oldFile, newFile.getFullPath());
				}
			}
		}
	}

	@Override
	public void handleFileRemoved(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addFileToUnload(file);
		}
	}
}
