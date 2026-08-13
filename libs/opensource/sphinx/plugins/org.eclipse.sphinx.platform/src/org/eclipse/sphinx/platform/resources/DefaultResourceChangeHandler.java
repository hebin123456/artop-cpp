/**
 * <copyright>
 * 
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [414125] Enhance ResourceDeltaVisitor to enable the analysis of IFolder added/moved/removed
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.resources;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

public class DefaultResourceChangeHandler implements IResourceChangeHandler {

	@Override
	public void handleProjectCreated(int eventType, IProject project) {
		// Do nothing by default
	}

	@Override
	public void handleProjectOpened(int eventType, IProject project) {
		// Do nothing by default
	}

	@Override
	public void handleProjectRenamed(int eventType, IProject oldProject, IProject newProject) {
		// Do nothing by default
	}

	@Override
	public void handleProjectDescriptionChanged(int eventType, IProject project) {
		// Do nothing by default
	}

	@Override
	public void handleProjectSettingsChanged(int eventType, IProject project, Collection<String> preferenceFileNames) {
		// Do nothing by default
	}

	@Override
	public void handleProjectAboutToBeClosed(IProject project) {
		// Do nothing by default
	}

	@Override
	public void handleProjectAboutToBeDeleted(IProject project) {
		// Do nothing by default
	}

	@Override
	public void handleProjectClosed(int eventType, IProject project) {
		// Do nothing by default
	}

	@Override
	public void handleProjectRemoved(int eventType, IProject project) {
		// Do nothing by default
	}

	@Override
	public void handleFolderAdded(int eventType, IFolder folder) {
		// Do nothing by default
	}

	@Override
	public void handleFolderMoved(int eventType, IFolder oldFolder, IFolder newFolder) {
		// Do nothing by default
	}

	@Override
	public void handleFolderRemoved(int eventType, IFolder folder) {
		// Do nothing by default
	}

	@Override
	public void handleFileAdded(int eventType, IFile file) {
		// Do nothing by default
	}

	@Override
	public void handleFileChanged(int eventType, IFile file) {
		// Do nothing by default
	}

	@Override
	public void handleFileMoved(int eventType, IFile oldFile, IFile newFile) {
		// Do nothing by default
	}

	@Override
	public void handleFileRemoved(int eventType, IFile file) {
		// Do nothing by default
	}
}
