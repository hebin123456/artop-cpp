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
package org.eclipse.sphinx.examples.hummingbird.ide.internal.scoping;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.sphinx.emf.scoping.IResourceScopeMarkerSyncRequest;
import org.eclipse.sphinx.emf.scoping.ResourceScopeMarkerSynchronizer;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizerDelegate;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * Resource Synchronizer Delegate that is in charge of triggering updates of the Resource Scope Markers. see also
 * {@link AbstractResourceSynchronizerDelegate} and {@link ResourceScopeMarkerSynchronizer}
 */
public class HummingbirdResourceScopeMarkerSynchronizerDelegate extends AbstractResourceSynchronizerDelegate<IResourceScopeMarkerSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final HummingbirdResourceScopeMarkerSynchronizerDelegate INSTANCE = new HummingbirdResourceScopeMarkerSynchronizerDelegate();

	/*
	 * @see org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler#handleProjectDescriptionChanged(int,
	 * org.eclipse.core.resources.IProject)
	 */
	@Override
	public void handleProjectDescriptionChanged(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			Collection<IFile> allFiles = ExtendedPlatform.getAllFiles(project, false);
			for (IFile file : allFiles) {
				syncRequest.addFileToValidate(file);
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler#handleProjectSettingsChanged(int,
	 * org.eclipse.core.resources.IProject, java.util.Collection)
	 */
	@Override
	public void handleProjectSettingsChanged(int eventType, IProject project, Collection<String> preferenceFileNames) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			Collection<IFile> allFiles = ExtendedPlatform.getAllFiles(project, false);
			for (IFile file : allFiles) {
				syncRequest.addFileToValidate(file);
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler#handleFileAdded(int,
	 * org.eclipse.core.resources.IFile)
	 */
	@Override
	public void handleFileAdded(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addFileToValidate(file);
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler#handleFileChanged(int,
	 * org.eclipse.core.resources.IFile)
	 */
	@Override
	public void handleFileChanged(int eventType, IFile file) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addFileToValidate(file);
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler#handleFileMoved(int,
	 * org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile)
	 */
	@Override
	public void handleFileMoved(int eventType, IFile oldFile, IFile newFile) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			syncRequest.addFileToValidate(newFile);
		}
	}
}
