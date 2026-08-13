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

public interface IResourceChangeHandler {

	void handleProjectCreated(int eventType, IProject project);

	void handleProjectOpened(int eventType, IProject project);

	void handleProjectRenamed(int eventType, IProject oldProject, IProject newProject);

	void handleProjectDescriptionChanged(int eventType, IProject project);

	void handleProjectSettingsChanged(int eventType, IProject project, Collection<String> preferenceFileNames);

	void handleProjectAboutToBeClosed(IProject project);

	void handleProjectAboutToBeDeleted(IProject project);

	void handleProjectClosed(int eventType, IProject project);

	void handleProjectRemoved(int eventType, IProject project);

	void handleFolderAdded(int eventType, IFolder folder);

	void handleFolderMoved(int eventType, IFolder oldFolder, IFolder newFolder);

	void handleFolderRemoved(int eventType, IFolder folder);

	void handleFileAdded(int eventType, IFile file);

	void handleFileChanged(int eventType, IFile file);

	void handleFileMoved(int eventType, IFile oldFile, IFile newFile);

	void handleFileRemoved(int eventType, IFile file);
}
