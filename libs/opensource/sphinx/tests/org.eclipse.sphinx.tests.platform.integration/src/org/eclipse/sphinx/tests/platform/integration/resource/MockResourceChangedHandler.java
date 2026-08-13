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
package org.eclipse.sphinx.tests.platform.integration.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

@SuppressWarnings("nls")
public class MockResourceChangedHandler implements IResourceChangeListener {
	public static final String ADD_PROJECT = "Add Project";
	public static final String ADD_FOLDER = "Add Folder";
	public static final String ADD_FILE = "Add File";

	public static final String CHANGED_PROJECT_DESCIPTION = "Change ProjectDescription File";
	public static final String CHANGED_PROJECT_SETTING = "Change Project Setting Files";
	public static final String CHANGED_PROJECT_OPENED = "Open Project";
	public static final String CHANGED_PROJECT_CLOSED = "Close Project";

	public static final String CHANGED_FOLDER = "Change Folder";
	public static final String CHANGED_FILE = "Change File";

	public static final String REMOVED_PROJECT = "Delete Project";
	public static final String REMOVED_FOLDER = "Delete folder";
	public static final String REMOVED_FILE = "Delete file";

	public static final String CHANGED_PROJECT_RENAMED = "Rename Project";
	public static final String MOVED_FOLDER = "Move Folder or Rename";
	public static final String MOVED_FILE = "Move File or Rename";

	static List<ResourceHandled> resourcesHandled = new ArrayList<ResourceHandled>();

	// public static MockResourceSetChangedHandler INTANCE = new MockResourceSetChangedHandler();

	public MockResourceChangedHandler() {

	}

	@Override
	protected void finalize() throws Throwable {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		resourcesHandled.clear();
		super.finalize();
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {

				// Investigate resource delta on saved files
				IResourceDeltaVisitor visitor = new ResourceDeltaVisitor(event.getType(), new DefaultResourceChangeHandler() {

					@Override
					public void handleProjectCreated(int eventType, IProject project) {
						resourcesHandled.add(new ResourceHandled(project.getFullPath(), ADD_PROJECT));
					}

					@Override
					public void handleFolderAdded(int eventType, IFolder folder) {
						resourcesHandled.add(new ResourceHandled(folder.getFullPath(), ADD_FOLDER));
					}

					@Override
					public void handleFileAdded(int eventType, IFile file) {
						resourcesHandled.add(new ResourceHandled(file.getFullPath(), ADD_FILE));
					}

					// CHANGED
					@Override
					public void handleProjectOpened(int eventType, IProject project) {
						resourcesHandled.add(new ResourceHandled(project.getFullPath(), CHANGED_PROJECT_OPENED));
					}

					@Override
					public void handleProjectDescriptionChanged(int eventType, IProject project) {
						resourcesHandled.add(new ResourceHandled(project.getFullPath(), CHANGED_PROJECT_DESCIPTION));
					}

					@Override
					public void handleProjectSettingsChanged(int eventType, IProject project, Collection<String> preferenceFileNames) {
						resourcesHandled.add(new ResourceHandled(project.getFullPath(), CHANGED_PROJECT_SETTING));
					}

					@Override
					public void handleProjectClosed(int eventType, IProject project) {
						resourcesHandled.add(new ResourceHandled(project.getFullPath(), CHANGED_PROJECT_CLOSED));
					}

					@Override
					public void handleFileChanged(int eventType, IFile file) {
						resourcesHandled.add(new ResourceHandled(file.getFullPath(), CHANGED_FILE));
					}

					// REMOVED
					@Override
					public void handleProjectRemoved(int eventType, IProject project) {
						resourcesHandled.add(new ResourceHandled(project.getFullPath(), REMOVED_PROJECT));
					}

					@Override
					public void handleProjectRenamed(int eventType, IProject oldProject, IProject newProject) {
						resourcesHandled.add(new ResourceHandled(oldProject.getFullPath(), CHANGED_PROJECT_RENAMED));
					}

					@Override
					// Handle file removed or renamed
					public void handleFileRemoved(int eventType, IFile file) {
						resourcesHandled.add(new ResourceHandled(file.getFullPath(), REMOVED_FILE));
					}

					@Override
					public void handleFileMoved(int eventType, IFile oldFile, IFile newFile) {
						resourcesHandled.add(new ResourceHandled(oldFile.getFullPath(), MOVED_FILE));
					}

					@Override
					// Handle folder removed or renamed
					public void handleFolderRemoved(int eventType, IFolder folder) {
						resourcesHandled.add(new ResourceHandled(folder.getFullPath(), REMOVED_FOLDER));
					}

					@Override
					public void handleFolderMoved(int eventType, IFolder oldFolder, IFolder newFolder) {
						resourcesHandled.add(new ResourceHandled(oldFolder.getFullPath(), MOVED_FOLDER));
					}

				});
				delta.accept(visitor);

			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	public List<ResourceHandled> getResourcesHandled() {
		return resourcesHandled;
	}

	public void clearResourcesHandledMap() {
		resourcesHandled.clear();
	}

}
