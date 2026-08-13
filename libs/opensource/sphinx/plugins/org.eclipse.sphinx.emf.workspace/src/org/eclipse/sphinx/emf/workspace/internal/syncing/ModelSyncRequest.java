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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.syncing.IModelSyncRequest;

public class ModelSyncRequest implements IModelSyncRequest {

	private Set<IProject> projectsToLoad = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IProject> loadedProjects = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IProject> projectsToUnload = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IProject> unloadedProjects = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IProject> projectsToReload = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IProject> reloadedProjects = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IProject> projectsToUnresolveUnreachableCrossReferencesFor = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IProject> projectsWithUnresolvedUnreachableCrossReferences = Collections.synchronizedSet(new HashSet<IProject>());
	private Set<IFile> filesToLoad = Collections.synchronizedSet(new HashSet<IFile>());
	private Set<IFile> loadedFiles = Collections.synchronizedSet(new HashSet<IFile>());
	private Set<IFile> filesToUnload = Collections.synchronizedSet(new HashSet<IFile>());
	private Set<IFile> unloadedFiles = Collections.synchronizedSet(new HashSet<IFile>());
	private Set<IFile> filesToReload = Collections.synchronizedSet(new HashSet<IFile>());
	private Set<IFile> reloadedFiles = Collections.synchronizedSet(new HashSet<IFile>());
	private Map<IFile, IPath> filesToUpdateResourceURIFor = Collections.synchronizedMap(new HashMap<IFile, IPath>());
	private Map<IFile, IPath> filesWithUpdatedResourceURI = Collections.synchronizedMap(new HashMap<IFile, IPath>());

	@Override
	public void init() {
		// Noting to do
	}

	@Override
	public void addProjectToLoad(IProject project) {
		if (project != null) {
			// Do not request loading of given project when the same project has already has been requested for
			// unloading
			if (projectsToUnload.contains(project)) {
				projectsToUnload.remove(project);
				return;
			}

			projectsToLoad.add(project);

			// Remove previously encountered load requests for files which belong to project which is requested for
			// loading now
			removeFilesToLoadFor(project);
		}
	}

	private boolean isProjectAboutToBeLoaded(IProject project) {
		return projectsToLoad.contains(project) || loadedProjects.contains(project);
	}

	@Override
	public void addProjectToUnload(IProject project) {
		if (project != null) {
			// Do not request unloading of given project when the same project has already has been requested for
			// loading
			if (projectsToLoad.contains(project)) {
				projectsToLoad.remove(project);
				return;
			}

			projectsToUnload.add(project);

			// Remove previously encountered unload requests for files which belong to project which is requested for
			// unloading now
			removeFilesToUnloadFor(project);
		}
	}

	private boolean isProjectAboutToBeUnloaded(IProject project) {
		return projectsToUnload.contains(project) || unloadedProjects.contains(project);
	}

	@Override
	public void addProjectToReload(IProject project) {
		if (project != null) {
			projectsToReload.add(project);

			// Remove previously encountered reload requests for files which belong to project which is requested for
			// reloading now
			removeFilesToReloadFor(project);
		}
	}

	private boolean isProjectAboutToBeReloaded(IProject project) {
		return projectsToReload.contains(project) || reloadedProjects.contains(project);
	}

	@Override
	public void addProjectToUnresolveUnreachableCrossReferencesFor(IProject project) {
		if (project != null && !isProjectWithUnreachableCrossReferencesAboutToBeUnresolved(project)) {
			projectsToUnresolveUnreachableCrossReferencesFor.add(project);
		}
	}

	private boolean isProjectWithUnreachableCrossReferencesAboutToBeUnresolved(IProject project) {
		return projectsToUnresolveUnreachableCrossReferencesFor.contains(project)
				|| projectsWithUnresolvedUnreachableCrossReferences.contains(project);
	}

	@Override
	public void addFileToLoad(IFile file) {
		if (file != null) {
			// Do not request loading of given file when it belongs to a project that has already has been requested for
			// loading or reloading
			if (!isProjectAboutToBeLoaded(file.getProject()) && !isProjectAboutToBeReloaded(file.getProject())) {
				// Exclude inaccessible files
				if (file.isAccessible()) {
					// Exclude obvious non-model files and model files which are out of scope
					if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
						filesToLoad.add(file);
					}
				}
			}
		}
	}

	private void removeFilesToLoadFor(IProject project) {
		if (project != null) {
			synchronized (filesToLoad) {
				Iterator<IFile> iter = filesToLoad.iterator();
				while (iter.hasNext()) {
					IFile file = iter.next();
					if (project.equals(file.getProject())) {
						iter.remove();
					}
				}
			}
		}
	}

	@Override
	public void addFileToUnload(IFile file) {
		if (file != null) {
			// Do not request unloading of given file when it belongs to a project that has already has been requested
			// for unloading or reloading
			if (!isProjectAboutToBeUnloaded(file.getProject()) && !isProjectAboutToBeReloaded(file.getProject())) {
				// Exclude obvious non-model files
				if (ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file)) {
					filesToUnload.add(file);
				}
			}
		}
	}

	private void removeFilesToUnloadFor(IProject project) {
		if (project != null) {
			synchronized (filesToUnload) {
				Iterator<IFile> iter = filesToUnload.iterator();
				while (iter.hasNext()) {
					IFile file = iter.next();
					if (project.equals(file.getProject())) {
						iter.remove();
					}
				}
			}
		}
	}

	@Override
	public void addFileToReload(IFile file) {
		if (file != null) {
			// Do not request reloading of given file when it belongs to a project that has already has been requested
			// for reloading
			if (!isProjectAboutToBeReloaded(file.getProject())) {
				// Exclude obvious non-model files and model files which are out of scope
				if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
					filesToReload.add(file);
				}
			}
		}
	}

	private void removeFilesToReloadFor(IProject project) {
		if (project != null) {
			synchronized (filesToReload) {
				Iterator<IFile> iter = filesToReload.iterator();
				while (iter.hasNext()) {
					IFile file = iter.next();
					if (project.equals(file.getProject())) {
						iter.remove();
					}
				}
			}
		}
	}

	@Override
	public void addFileToUpdateResourceURIFor(IFile oldFile, IPath newPath) {
		// Exclude obvious non-model files and model files which are out of scope
		if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(oldFile)) {
			filesToUpdateResourceURIFor.put(oldFile, newPath);
		}
	}

	@Override
	public boolean canPerform() {
		boolean canPerform = projectsToLoad.size() > 0 || projectsToUnload.size() > 0 || projectsToReload.size() > 0
				|| projectsToUnresolveUnreachableCrossReferencesFor.size() > 0 || filesToLoad.size() > 0 || filesToUnload.size() > 0
				|| filesToReload.size() > 0 || filesToUpdateResourceURIFor.size() > 0;
		// TODO Surround with appropriate tracing option
		// if (!canPerform) {
		// System.out.println("[WorkspaceSyncRequest#canPerform()] No sync requests to be scheduled");
		// }
		return canPerform;
	}

	@Override
	public void perform() {
		if (!canPerform()) {
			return;
		}

		if (projectsToLoad.size() > 0) {
			Set<IProject> projects = new HashSet<IProject>(projectsToLoad);
			// TODO Surround with appropriate tracing option
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling load projects request");
			ModelLoadManager.INSTANCE.loadProjects(projects, false, true, null);
			loadedProjects.addAll(projectsToLoad);
			projectsToLoad.clear();
		}
		if (projectsToUnload.size() > 0) {
			Set<IProject> projects = new HashSet<IProject>(projectsToUnload);
			// TODO Surround with appropriate tracing option
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling unload projects request");
			ModelLoadManager.INSTANCE.unloadProjects(projects, false, true, null);
			unloadedProjects.addAll(projectsToUnload);
			projectsToUnload.clear();
		}
		if (projectsToReload.size() > 0) {
			Set<IProject> projects = new HashSet<IProject>(projectsToReload);
			// TODO Surround with appropriate tracing option
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling reload projects request");
			ModelLoadManager.INSTANCE.reloadProjects(projects, false, true, null);
			reloadedProjects.addAll(projectsToReload);
			projectsToReload.clear();
		}
		if (projectsToUnresolveUnreachableCrossReferencesFor.size() > 0) {
			Set<IProject> projects = new HashSet<IProject>(projectsToUnresolveUnreachableCrossReferencesFor);
			// TODO Surround with appropriate tracing option
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling unresolve unreachable cross-project references request");
			ModelLoadManager.INSTANCE.unresolveUnreachableCrossProjectReferences(projects, true, null);
			projectsWithUnresolvedUnreachableCrossReferences.addAll(projectsToUnresolveUnreachableCrossReferencesFor);
			projectsToUnresolveUnreachableCrossReferencesFor.clear();
		}
		if (filesToLoad.size() > 0) {
			Set<IFile> files = new HashSet<IFile>(filesToLoad);
			// TODO Surround with appropriate tracing option
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling load files request");
			ModelLoadManager.INSTANCE.loadFiles(files, true, null);
			loadedFiles.addAll(filesToLoad);
			filesToLoad.clear();
		}
		if (filesToUnload.size() > 0) {
			Set<IFile> files = new HashSet<IFile>(filesToUnload);
			// TODO Surround with appropriate tracing option
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling unload files request");
			ModelLoadManager.INSTANCE.unloadFiles(files, false, true, null);
			unloadedFiles.addAll(filesToUnload);
			filesToUnload.clear();
		}
		if (filesToReload.size() > 0) {
			Set<IFile> files = new HashSet<IFile>(filesToReload);
			// TODO Surround with appropriate tracing option
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling reload files request");
			ModelLoadManager.INSTANCE.reloadFiles(files, false, true, null);
			reloadedFiles.addAll(filesToReload);
			filesToReload.clear();
		}
		if (filesToUpdateResourceURIFor.size() > 0) {
			Map<IFile, IPath> files = new HashMap<IFile, IPath>(filesToUpdateResourceURIFor);
			// TODO Surround with appropriate tracing option
			// FIXME Adjust message
			// System.out.println("[WorkspaceSyncRequest#perform()] Scheduling unload resource URIs request");
			ModelLoadManager.INSTANCE.updateResourceURIs(files, true, null);
			filesWithUpdatedResourceURI.putAll(filesToUpdateResourceURIFor);
			filesToUpdateResourceURIFor.clear();
		}
	}

	@Override
	public void dispose() {
		projectsToLoad.clear();
		loadedProjects.clear();
		projectsToUnload.clear();
		unloadedProjects.clear();
		projectsToReload.clear();
		reloadedProjects.clear();
		projectsToUnresolveUnreachableCrossReferencesFor.clear();
		projectsWithUnresolvedUnreachableCrossReferences.clear();
		filesToLoad.clear();
		loadedFiles.clear();
		filesToUnload.clear();
		unloadedFiles.clear();
		filesToReload.clear();
		reloadedFiles.clear();
		filesToUpdateResourceURIFor.clear();
		filesWithUpdatedResourceURI.clear();
	}
}
