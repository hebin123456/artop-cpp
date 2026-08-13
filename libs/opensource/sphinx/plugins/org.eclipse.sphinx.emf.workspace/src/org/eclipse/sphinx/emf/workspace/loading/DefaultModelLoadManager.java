/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 BMW Car IT, itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     itemis - [393021] ClassCastExceptions raised during loading model resources with Sphinx are ignored
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [427461] Add progress monitor to resource load options (useful for loading large models)
 *     itemis - [454092] Loading model resources
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScope;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.internal.loading.UnresolveUnreachableCrossProjectReferencesJob;
import org.eclipse.sphinx.emf.workspace.internal.loading.UpdateResourceURIJob;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultFileUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultModelLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultModelUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultProjectLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultProjectReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.DefaultUpdateResourceURIOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadCommonOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.ILoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadCommonOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUnresolveUnreachableCrossProjectReferencesOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IUpdateResourceURIOperation;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * Default implementation for the {@link IModelLoadManager} interface. It is recommended to use this class as a base
 * class for ModelLoadManager extensions. This implementation also provides a possibility to override Default Model Load
 * Operations by overriding their create method.
 */
public class DefaultModelLoadManager implements IModelLoadManager {

	protected Map<TransactionalEditingDomain, Collection<Resource>> detectResourcesToUnload(final IProject project,
			final boolean includeReferencedScopes, final IMetaModelDescriptor mmFilter, IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(project);

		// Investigate all editing domains for loaded resources which belong to given project (or one of its referenced
		// projects if required)
		/*
		 * !! Important Note !! For the sake of robustness, it is necessary to consider all editing domains but not only
		 * the those which would be returned by WorkspaceEditingDomainUtil#getEditingDomains(IContainer). Although not
		 * really intended by Sphinx workspace management it might anyway happen that wired kind of applications manage
		 * to populate editing domains with resources which don't belong to any IModelDescriptor. In this case,
		 * WorkspaceEditingDomainUtil#getEditingDomains(IContainer) is likely to return an incomplete set of editing
		 * domains for given project and we would have no chance to make sure that actually all resources from given
		 * project (and its referenced projects if required) get unloaded.
		 */
		Collection<TransactionalEditingDomain> editingDomains = WorkspaceEditingDomainUtil.getAllEditingDomains();
		SubMonitor progress = SubMonitor.convert(monitor, editingDomains.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload = new HashMap<TransactionalEditingDomain, Collection<Resource>>();
		for (final TransactionalEditingDomain editingDomain : editingDomains) {
			try {
				List<Resource> resourcesToUnloadInEditingDomain = TransactionUtil.runExclusive(editingDomain,
						new RunnableWithResult.Impl<List<Resource>>() {
							@Override
							public void run() {
								List<Resource> resources = new ArrayList<Resource>();
								ProjectResourceScope projectResourceScope = new ProjectResourceScope(project);
								for (Resource resource : editingDomain.getResourceSet().getResources()) {
									if (projectResourceScope.belongsTo(resource, includeReferencedScopes)) {
										if (mmFilter != null) {
											IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
											if (mmDescriptor == null) {
												mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getOldDescriptor(resource);
											}
											if (mmFilter.equals(mmDescriptor)) {
												resources.add(resource);
											}
										} else {
											resources.add(resource);
										}
									}
								}
								setResult(resources);
							}
						});
				if (resourcesToUnloadInEditingDomain.size() > 0) {
					resourcesToUnload.put(editingDomain, resourcesToUnloadInEditingDomain);
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}

			progress.worked(1);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		return resourcesToUnload;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadWorkspace(boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadWorkspace(boolean async, IProgressMonitor monitor) {
		loadAllProjects(null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadAllProjects(org.eclipse.sphinx.emf.metamodel.
	 * IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadAllProjects(final IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		// Collect root projects in workspace
		Collection<IProject> projects = ExtendedPlatform.getRootProjects();

		// No projects found?
		if (projects.size() == 0) {
			return;
		}

		// Load models from root projects including referenced projects
		loadProjects(projects, true, mmDescriptor, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadProject(org.eclipse.core.resources.IProject,
	 * boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadProject(IProject project, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor) {
		loadProjects(Collections.singleton(project), includeReferencedProjects, null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadProject(org.eclipse.core.resources.IProject,
	 * boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadProject(IProject project, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor) {
		loadProjects(Collections.singleton(project), includeReferencedProjects, mmDescriptor, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadProjects(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadProjects(Collection<IProject> projects, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor) {
		loadProjects(projects, includeReferencedProjects, null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadProjects(java.util.Collection, boolean,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadProjects(final Collection<IProject> projects, final boolean includeReferencedProjects, final IMetaModelDescriptor mmDescriptor,
			boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(projects);

		if (!projects.isEmpty()) {
			IProjectLoadCommonOperation projectLoadOperation = createProjectLoadOperation(projects, includeReferencedProjects, mmDescriptor);
			LoadOperationRunnerHelper.run(projectLoadOperation, async, monitor);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadFile(org.eclipse.core.resources.IFile,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadFile(IFile file, boolean async, IProgressMonitor monitor) {
		loadFiles(Collections.singleton(file), null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadFile(org.eclipse.core.resources.IFile,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadFile(IFile file, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		loadFiles(Collections.singleton(file), mmDescriptor, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadFiles(java.util.Collection, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadFiles(Collection<IFile> files, boolean async, IProgressMonitor monitor) {
		loadFiles(files, null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadFiles(java.util.Collection,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadFiles(final Collection<IFile> files, final IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(files);

		if (!files.isEmpty()) {
			IFileLoadCommonOperation fileLoadOperation = createFileLoadOperation(files, mmDescriptor);
			LoadOperationRunnerHelper.run(fileLoadOperation, async, monitor);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadURI(org.eclipse.emf.common.util.URI, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadURI(URI uri, boolean async, IProgressMonitor monitor) {
		loadURIs(Collections.singleton(uri), null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadURI(org.eclipse.emf.common.util.URI,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadURI(URI uri, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		loadURIs(Collections.singleton(uri), mmDescriptor, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadURIs(java.util.Collection, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadURIs(Collection<URI> uris, boolean async, IProgressMonitor monitor) {
		loadURIs(uris, null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadURIs(java.util.Collection,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadURIs(final Collection<URI> uris, final IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(uris);
		loadFiles(getFiles(uris), mmDescriptor, async, monitor);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadProject(org.eclipse.core.resources.IProject,
	 * boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadProject(IProject project, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor) {
		unloadProjects(Collections.singleton(project), includeReferencedProjects, null, async, monitor);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadProject(org.eclipse.core.resources.IProject,
	 * boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadProject(IProject project, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor) {
		unloadProjects(Collections.singleton(project), includeReferencedProjects, mmDescriptor, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadProjects(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor) {
		unloadProjects(projects, includeReferencedProjects, null, async, monitor);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadAllProjects(org.eclipse.sphinx.emf.metamodel.
	 * IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadAllProjects(final IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		// Collect root projects in workspace
		Collection<IProject> projects = ExtendedPlatform.getRootProjects();

		// No projects found?
		if (projects.size() == 0) {
			return;
		}

		// Unload models from root projects including referenced projects
		unloadProjects(projects, true, mmDescriptor, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadWorkspace(boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadWorkspace(boolean async, IProgressMonitor monitor) {
		unloadAllProjects(null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadProjects(java.util.Collection, boolean,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor) {
		Assert.isNotNull(projects);
		try {
			for (IProject project : projects) {
				String taskName = mmDescriptor != null ? NLS.bind(Messages.task_unloadingModelInProject, mmDescriptor.getName(), project.getName())
						: NLS.bind(Messages.task_unloadingModelsInProject, project.getName());
				SubMonitor progress = SubMonitor.convert(monitor, taskName, 100);
				if (progress.isCanceled()) {
					throw new OperationCanceledException();
				}

				unloadProjectResources(project, includeReferencedProjects, mmDescriptor, async, progress);
			}
		} catch (OperationCanceledException ex) {
			// Ignore exception
		}
	}

	protected void unloadProjectResources(IProject project, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor progress) throws OperationCanceledException {
		// Detect files to unload in given project and its referenced projects
		/*
		 * !! Important Note !! Perform this part always synchronously. Otherwise - when being called right before the
		 * project is closed or deleted - the affected files might no longer exist and couldn't be retrieved anymore by
		 * the time where an asynchronous job would be running.
		 */
		// FIXME Include referenced projects only if only the given project or its child projects but no other
		// root projects reference them; add an appropriate method to ExtendedPlatform for that purpose
		Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload = detectResourcesToUnload(project, includeReferencedProjects,
				mmDescriptor, SubMonitor.convert(progress).newChild(1));

		// Unload resources; perform memory-optimized unloading if given project is a root project, i.e. is not
		// referenced by any other project
		unloadResources(resourcesToUnload, ExtendedPlatform.isRootProject(project), async, progress);
	}

	protected void unloadResources(final Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload, boolean memoryOptimized,
			boolean async, IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(resourcesToUnload);

		if (!resourcesToUnload.isEmpty()) {
			SubMonitor unloadProgress = SubMonitor.convert(monitor).newChild(99);
			ILoadOperation unloadModelResourceOperation = createModelUnloadOperation(resourcesToUnload, memoryOptimized);
			LoadOperationRunnerHelper.run(unloadModelResourceOperation, async, unloadProgress);
		} else {
			monitor.done();
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadFile(org.eclipse.core.resources.IFile,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadFile(IFile file, boolean async, IProgressMonitor monitor) {
		unloadFiles(Collections.singleton(file), null, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadFile(org.eclipse.core.resources.IFile,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadFile(IFile file, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		unloadFiles(Collections.singleton(file), mmDescriptor, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadFiles(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadFiles(Collection<IFile> files, boolean memoryOptimized, boolean async, IProgressMonitor monitor) {
		unloadFiles(files, null, memoryOptimized, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadFiles(java.util.Collection,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadFiles(final Collection<IFile> files, final IMetaModelDescriptor mmDescriptor, final boolean memoryOptimized, boolean async,
			IProgressMonitor monitor) {
		Assert.isNotNull(files);

		if (!files.isEmpty()) {
			IFileLoadCommonOperation fileUnloadOperation = createFileUnloadOperation(files, mmDescriptor, memoryOptimized);
			LoadOperationRunnerHelper.run(fileUnloadOperation, async, monitor);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadURI(org.eclipse.emf.common.util.URI,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadURI(URI uri, boolean async, IProgressMonitor monitor) {
		unloadURIs(Collections.singleton(uri), null, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadURI(org.eclipse.emf.common.util.URI,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadURI(URI uri, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		unloadURIs(Collections.singleton(uri), mmDescriptor, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadURIs(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadURIs(Collection<URI> uris, boolean memoryOptimized, boolean async, IProgressMonitor monitor) {
		unloadURIs(uris, null, memoryOptimized, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadURIs(java.util.Collection,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadURIs(final Collection<URI> uris, final IMetaModelDescriptor mmDescriptor, final boolean memoryOptimized, boolean async,
			IProgressMonitor monitor) {
		Assert.isNotNull(uris);
		unloadFiles(getFiles(uris), mmDescriptor, memoryOptimized, async, monitor);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadProject(org.eclipse.core.resources.IProject,
	 * boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadProject(IProject project, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor) {
		reloadProjects(Collections.singleton(project), includeReferencedProjects, null, async, monitor);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadProject(org.eclipse.core.resources.IProject,
	 * boolean, org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadProject(IProject project, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor, boolean async,
			IProgressMonitor monitor) {
		reloadProjects(Collections.singleton(project), includeReferencedProjects, mmDescriptor, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadProjects(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, boolean async, IProgressMonitor monitor) {
		reloadProjects(projects, includeReferencedProjects, null, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadProjects(java.util.Collection, boolean,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadProjects(final Collection<IProject> projects, final boolean includeReferencedProjects, final IMetaModelDescriptor mmDescriptor,
			boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(projects);

		if (!projects.isEmpty()) {
			IProjectLoadCommonOperation projectReloadOperation = createProjectReloadOperation(projects, includeReferencedProjects, mmDescriptor);
			LoadOperationRunnerHelper.run(projectReloadOperation, async, monitor);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadFile(org.eclipse.core.resources.IFile,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadFile(IFile file, boolean async, IProgressMonitor monitor) {
		reloadFiles(Collections.singleton(file), null, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadFile(org.eclipse.core.resources.IFile,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadFile(IFile file, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		reloadFiles(Collections.singleton(file), mmDescriptor, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadFiles(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadFiles(Collection<IFile> files, boolean memoryOptimized, boolean async, IProgressMonitor monitor) {
		reloadFiles(files, null, memoryOptimized, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadFiles(java.util.Collection,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadFiles(final Collection<IFile> files, final IMetaModelDescriptor mmDescriptor, final boolean memoryOptimized, boolean async,
			IProgressMonitor monitor) {
		Assert.isNotNull(files);

		if (!files.isEmpty()) {
			IFileLoadCommonOperation fileReloadOperation = createFileReloadOperation(files, mmDescriptor, memoryOptimized);
			LoadOperationRunnerHelper.run(fileReloadOperation, async, monitor);
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadURI(org.eclipse.emf.common.util.URI,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadURI(URI uri, boolean async, IProgressMonitor monitor) {
		reloadURIs(Collections.singleton(uri), null, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadURI(org.eclipse.emf.common.util.URI,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadURI(URI uri, IMetaModelDescriptor mmDescriptor, boolean async, IProgressMonitor monitor) {
		reloadURIs(Collections.singleton(uri), mmDescriptor, false, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadURIs(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadURIs(Collection<URI> uris, boolean memoryOptimized, boolean async, IProgressMonitor monitor) {
		reloadURIs(uris, null, memoryOptimized, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#reloadURIs(java.util.Collection,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, boolean, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void reloadURIs(final Collection<URI> uris, final IMetaModelDescriptor mmDescriptor, final boolean memoryOptimized, boolean async,
			IProgressMonitor monitor) {
		Assert.isNotNull(uris);
		reloadFiles(getFiles(uris), mmDescriptor, memoryOptimized, async, monitor);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unresolveUnreachableCrossProjectReferences(java.util.
	 * Collection, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unresolveUnreachableCrossProjectReferences(final Collection<IProject> projects, boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(projects);

		// Perform unresolving procedure in all given projects as well as in all projects that reference those
		final HashSet<IProject> projectsWithUnreachableCrossRefrencesToUnresolve = new HashSet<IProject>(projects);
		for (IProject project : projects) {
			Collection<IProject> referencingProjects = ExtendedPlatform.getAllReferencingProjects(project);
			projectsWithUnreachableCrossRefrencesToUnresolve.addAll(referencingProjects);
		}

		IUnresolveUnreachableCrossProjectReferencesOperation unresolveUnreachableCrossProjectReferencesOperation = createUnresolveUnreachableCrossProjectReferencesOperation(
				projectsWithUnreachableCrossRefrencesToUnresolve);
		if (async && projectsWithUnreachableCrossRefrencesToUnresolve.size() > 0) {
			Job job = new UnresolveUnreachableCrossProjectReferencesJob(unresolveUnreachableCrossProjectReferencesOperation);
			job.setPriority(Job.BUILD);
			job.setRule(unresolveUnreachableCrossProjectReferencesOperation.getRule());
			job.schedule();
		} else {
			try {
				unresolveUnreachableCrossProjectReferencesOperation.run(monitor);
			} catch (OperationCanceledException ex) {
				// Ignore exception
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadModel(org.eclipse.sphinx.emf.model.
	 * IModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadModel(IModelDescriptor modelDescriptor, boolean async, IProgressMonitor monitor) {
		loadModel(modelDescriptor, true, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadModel(org.eclipse.sphinx.emf.model.
	 * IModelDescriptor, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadModel(final IModelDescriptor modelDescriptor, boolean includeReferencedScopes, boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(modelDescriptor);

		ILoadOperation modelLoadOperation = createModelLoadOperation(modelDescriptor, includeReferencedScopes);
		LoadOperationRunnerHelper.run(modelLoadOperation, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadModels(java.util.Collection, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadModels(Collection<IModelDescriptor> modelDescriptors, boolean async, IProgressMonitor monitor) {
		loadModels(modelDescriptors, true, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#loadModels(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadModels(Collection<IModelDescriptor> modelDescriptors, boolean includeReferencedScopes, boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(modelDescriptors);
		SubMonitor progress = SubMonitor.convert(monitor, modelDescriptors.size());

		for (IModelDescriptor modelDescriptor : modelDescriptors) {
			loadModel(modelDescriptor, includeReferencedScopes, async, progress.newChild(1));

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadModel(org.eclipse.sphinx.emf.model.
	 * IModelDescriptor, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadModel(IModelDescriptor modelDescriptor, boolean async, IProgressMonitor monitor) {
		unloadModel(modelDescriptor, true, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadModel(org.eclipse.sphinx.emf.model.
	 * IModelDescriptor, boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadModel(final IModelDescriptor modelDescriptor, final boolean includeReferencedScopes, boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(modelDescriptor);

		// Collect resources to unload in given model
		Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload = new HashMap<TransactionalEditingDomain, Collection<Resource>>();
		resourcesToUnload.put(modelDescriptor.getEditingDomain(), modelDescriptor.getLoadedResources(includeReferencedScopes));

		// Unload resources; perform memory-optimized unloading
		unloadResources(resourcesToUnload, true, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadModels(java.util.Collection, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadModels(Collection<IModelDescriptor> modelDescriptors, boolean async, IProgressMonitor monitor) {
		unloadModels(modelDescriptors, true, async, monitor);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#unloadModels(java.util.Collection, boolean,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void unloadModels(Collection<IModelDescriptor> modelDescriptors, boolean includeReferencedScopes, boolean async,
			IProgressMonitor monitor) {
		Assert.isNotNull(modelDescriptors);
		SubMonitor progress = SubMonitor.convert(monitor, modelDescriptors.size());

		for (IModelDescriptor modelDescriptor : modelDescriptors) {
			unloadModel(modelDescriptor, includeReferencedScopes, async, progress.newChild(1));

			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.IModelLoadManager#updateResourceURIs(java.util.Map, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void updateResourceURIs(final Map<IFile, IPath> filesToUpdate, boolean async, IProgressMonitor monitor) {
		Assert.isNotNull(filesToUpdate);

		IUpdateResourceURIOperation updateResourceURIOperation = createUpdateResourceURIOperation(filesToUpdate);
		if (async && filesToUpdate.size() > 0) {
			Job job = new UpdateResourceURIJob(updateResourceURIOperation);
			job.setPriority(Job.BUILD);
			job.setRule(updateResourceURIOperation.getRule());
			job.schedule();
		} else {
			try {
				updateResourceURIOperation.run(monitor);
			} catch (OperationCanceledException ex) {
				// Ignore exception
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	/**
	 * Creates an {@link IFileLoadOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultFileLoadOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IFileLoadOperation createFileLoadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor) {
		return new DefaultFileLoadOperation(files, mmDescriptor);
	}

	/**
	 * Creates an {@link IFileReloadOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultFileReloadOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IFileReloadOperation createFileReloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
		return new DefaultFileReloadOperation(files, mmDescriptor, memoryOptimized);
	}

	/**
	 * Creates an {@link IFileUnloadOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultFileUnloadOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IFileUnloadOperation createFileUnloadOperation(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized) {
		return new DefaultFileUnloadOperation(files, mmDescriptor, memoryOptimized);
	}

	/**
	 * Creates an {@link IModelLoadOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultModelLoadOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IModelLoadOperation createModelLoadOperation(IModelDescriptor modelDescriptor, boolean includeReferencedScopes) {
		return new DefaultModelLoadOperation(modelDescriptor, includeReferencedScopes);
	}

	/**
	 * Creates an {@link IModelUnloadOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultModelUnloadOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IModelUnloadOperation createModelUnloadOperation(Map<TransactionalEditingDomain, Collection<Resource>> resourcesToUnload,
			boolean memoryOptimized) {
		return new DefaultModelUnloadOperation(resourcesToUnload, memoryOptimized);
	}

	/**
	 * Creates an {@link IProjectLoadOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultProjectLoadOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IProjectLoadOperation createProjectLoadOperation(Collection<IProject> projects, boolean includeReferencedProjects,
			IMetaModelDescriptor mmDescriptor) {
		return new DefaultProjectLoadOperation(projects, includeReferencedProjects, mmDescriptor);
	}

	/**
	 * Creates an {@link IProjectReloadOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultProjectReloadOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IProjectReloadOperation createProjectReloadOperation(Collection<IProject> projects, boolean includeReferencedProjects,
			IMetaModelDescriptor mmDescriptor) {
		return new DefaultProjectReloadOperation(projects, includeReferencedProjects, mmDescriptor);
	}

	/**
	 * Creates an {@link IUnresolveUnreachableCrossProjectReferencesOperation} instance. It is recommended to return a
	 * subclass of the {@link DefaultUnresolveUnreachableCrossProjectReferencesOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IUnresolveUnreachableCrossProjectReferencesOperation createUnresolveUnreachableCrossProjectReferencesOperation(
			Collection<IProject> projectsWithUnreachableCrossRefrencesToUnresolve) {
		return new DefaultUnresolveUnreachableCrossProjectReferencesOperation(projectsWithUnreachableCrossRefrencesToUnresolve);
	}

	/**
	 * Creates an {@link IUpdateResourceURIOperation} instance. It is recommended to return a subclass of the
	 * {@link DefaultUpdateResourceURIOperation} class.
	 *
	 * @return a new instance.
	 */
	protected IUpdateResourceURIOperation createUpdateResourceURIOperation(Map<IFile, IPath> filesToUpdate) {
		return new DefaultUpdateResourceURIOperation(filesToUpdate);
	}

	protected Collection<IFile> getFiles(Collection<URI> uris) {
		Set<IFile> files = new HashSet<IFile>();
		if (uris != null) {
			Set<URI> modelResourceURIs = new HashSet<URI>();
			for (URI uri : uris) {
				// Removing URI fragment
				modelResourceURIs.add(uri.trimFragment());
			}

			for (URI modelResourceURI : modelResourceURIs) {
				final IFile file = EcorePlatformUtil.getFile(modelResourceURI);
				if (file != null) {
					files.add(file);
				}
			}
		}
		return files;
	}
}