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
package org.eclipse.sphinx.testutils.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.resource.ResourceProblemMarkerService;
import org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Listens for {@link Resource resource}s that have been loaded or saved and requests the problem markers of underlying
 * {@link IFile file}s to be updated according to the {@link Resource#getErrors() errors} and
 * {@link Resource#getWarnings() warnings} of each loaded or saved {@link Resource} resource.
 * 
 * @see ResourceProblemMarkerService#updateProblemMarkers(Collection, boolean,
 *      org.eclipse.core.runtime.IProgressMonitor)
 */
@SuppressWarnings("restriction")
public class ReferenceWorkspaceChangeListener implements IResourceChangeListener {

	protected Set<IFile> changedFiles = new HashSet<IFile>();
	protected Set<IFile> addedFiles = new HashSet<IFile>();
	protected Set<IFolder> addedFolders = new HashSet<IFolder>();
	protected Set<IProject> renamedProjects = new HashSet<IProject>();
	protected Set<IProject> projectsWithChangedDescription = new HashSet<IProject>();
	protected Map<IProject, Collection<String>> projectsWithChangedSettings = new HashMap<IProject, Collection<String>>();

	/*
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
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
					public void handleProjectDescriptionChanged(int eventType, IProject project) {
						projectsWithChangedDescription.add(project);
					}

					@Override
					public void handleProjectSettingsChanged(int eventType, IProject project, Collection<String> preferenceFileNames) {
						if (projectsWithChangedSettings.containsKey(project)) {
							projectsWithChangedSettings.get(project).addAll(preferenceFileNames);

						} else {
							projectsWithChangedSettings.put(project, preferenceFileNames);
						}
					}

					@Override
					public void handleFileAdded(int eventType, IFile file) {
						addedFiles.add(file);
					}

					@Override
					public void handleFolderAdded(int eventType, IFolder folder) {
						addedFolders.add(folder);
					}

					@Override
					public void handleProjectOpened(int eventType, IProject project) {
					}

					@Override
					public void handleFileChanged(int eventType, IFile file) {
						changedFiles.add(file);
					}

					@Override
					public void handleProjectRenamed(int eventType, IProject oldProject, IProject newProject) {
						renamedProjects.add(newProject);
					}

					@Override
					public void handleFolderMoved(int eventType, IFolder oldFolder, IFolder newFolder) {
						addedFolders.add(newFolder);
					}

					@Override
					public void handleFileMoved(int eventType, IFile oldFile, IFile newFile) {
						addedFiles.add(newFile);
					}
				});
				delta.accept(visitor);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	public Collection<IFile> getChangedFiles() {
		return changedFiles;
	}

	public Collection<IFile> getAddedFiles() {
		return addedFiles;
	}

	public Collection<IFolder> getAddedFolders() {
		return addedFolders;
	}

	public Collection<IProject> getRenamedProjects() {
		return renamedProjects;
	}

	public Collection<IProject> getProjectsWithChangedDescription() {
		return projectsWithChangedDescription;
	}

	public Map<IProject, Collection<String>> getProjectsWithChangedSettings() {
		return projectsWithChangedSettings;
	}

	public void clearHistory() {
		changedFiles.clear();
		addedFiles.clear();
		addedFolders.clear();
		renamedProjects.clear();
		projectsWithChangedDescription.clear();
		projectsWithChangedSettings.clear();
	}
}