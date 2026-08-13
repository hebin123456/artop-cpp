/**
 * <copyright>
 *
 * Copyright (c) 2014-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *     itemis - [485407] Enable eager post-load proxy resolution to support manifold URI fragments referring to the same object
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading.operations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelper;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelperAdapterFactory;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist.ModelIndex;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.lookupresolver.EcoreIndex;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.scoping.IResourceScope;
import org.eclipse.sphinx.emf.scoping.IResourceScopeProvider;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.loading.SchedulingRuleFactory;
import org.eclipse.sphinx.platform.operations.AbstractWorkspaceOperation;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public abstract class AbstractLoadOperation extends AbstractWorkspaceOperation implements ILoadOperation {

	protected static int DIFFERENT = 0x00;
	protected static int EQUAL = 0x01;
	protected static int GREATER_THAN = 0x02;
	protected static int SMALLER_THAN = 0x03;

	private IMetaModelDescriptor mmDescriptor;
	private SchedulingRuleFactory schedulingRuleFactory;

	public AbstractLoadOperation(String label, IMetaModelDescriptor mmDescriptor) {
		super(label);
		this.mmDescriptor = mmDescriptor;
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.ILoadOperation#covers(java.util.Collection, boolean,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)
	 */
	@Override
	public abstract boolean covers(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor);

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.ILoadOperation#covers(java.util.Collection,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor)
	 */
	@Override
	public abstract boolean covers(Collection<IFile> files, IMetaModelDescriptor mmDescriptor);

	/*
	 * @see org.eclipse.sphinx.emf.workspace.loading.operations.ILoadOperation#getMetaModelDescriptor()
	 */
	@Override
	public IMetaModelDescriptor getMetaModelDescriptor() {
		return mmDescriptor;
	}

	/**
	 * Computes a group of projects from the given context object. Referencing projects are also taken into account if
	 * flag <code>includeReferencingProjects</code> is set to <code>true</code>. The supported object types are:
	 * <ul>
	 * <li>{@linkplain org.eclipse.core.resources.IResource}</li>
	 * <li>{@linkplain org.eclipse.emf.ecore.EObject}</li>
	 * <li>{@linkplain org.eclipse.emf.ecore.resource.Resource}</li>
	 * </ul>
	 * First, this method tries to retrieve the direct parent project of that context object accordingly to its type.
	 * Then, it delegates the project group computing to
	 * {@linkplain ExtendedPlatform#getProjectGroup(IProject, boolean)}.
	 * <p>
	 *
	 * @param contextObject
	 *            A context object whose scope must be computed.
	 * @param includeReferencingProjects
	 *            If <code>true</code> also includes referencing projects.
	 * @return The list projects that constitute the project group inside which the context object exists.
	 */
	protected Collection<IProject> getProjectGroup(Object contextObject, boolean includeReferencingProjects) {
		if (contextObject instanceof IResource) {
			IResource contextResource = (IResource) contextObject;
			return ExtendedPlatform.getProjectGroup(contextResource.getProject(), includeReferencingProjects);
		} else {
			IFile contextFile = EcorePlatformUtil.getFile(contextObject);
			if (contextFile != null) {
				return ExtendedPlatform.getProjectGroup(contextFile.getProject(), includeReferencingProjects);
			}
		}
		return Collections.emptySet();
	}

	protected int getFilesToLoadCount(Map<TransactionalEditingDomain, Collection<IFile>> filesToLoad) {
		Assert.isNotNull(filesToLoad);

		int count = 0;
		for (TransactionalEditingDomain editingDomain : filesToLoad.keySet()) {
			Collection<IFile> filesToLoadInEditingDomain = filesToLoad.get(editingDomain);
			count += filesToLoadInEditingDomain.size();
		}
		return count;
	}

	protected int getFilesToUnloadCount(Map<TransactionalEditingDomain, Collection<IFile>> filesToUnload) {
		Assert.isNotNull(filesToUnload);

		int count = 0;
		for (TransactionalEditingDomain editingDomain : filesToUnload.keySet()) {
			count += filesToUnload.get(editingDomain).size();
		}
		return count;
	}

	protected void runDetectAndLoadModelFiles(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, IProgressMonitor monitor)
			throws OperationCanceledException {
		Assert.isNotNull(files);
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_loadingModelFiles, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Detect model resources among given files
		Map<TransactionalEditingDomain, Collection<IFile>> filesToLoad = detectFilesToLoad(files, mmDescriptor, true, progress.newChild(10));

		// Nothing to load?
		if (filesToLoad.isEmpty()) {
			progress.done();
			// TODO Surround with appropriate tracing option
			// System.out.println("[ModelLoadManager#runDetectAndLoadModelFiles()] No model files to be loaded");
			return;
		}
		runLoadModelFiles(filesToLoad, progress.newChild(90));
		// TODO Surround with appropriate tracing option
		// System.out.println("[ModelLoadManager#runDetectAndLoadModelFiles()] Loaded " +
		// getFilesToLoadCount(filesToLoad) + " model file(s)");
	}

	protected Map<TransactionalEditingDomain, Collection<IFile>> detectFilesToLoad(Collection<IFile> files, IMetaModelDescriptor mmFilter,
			boolean ignoreIfAlreadyLoaded, IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(files);
		SubMonitor progress = SubMonitor.convert(monitor, files.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		Map<TransactionalEditingDomain, Collection<IFile>> filesToLoad = new HashMap<TransactionalEditingDomain, Collection<IFile>>();
		for (IFile file : files) {
			try {
				// Exclude inaccessible files
				if (file.isAccessible()) {
					/*
					 * Performance optimization: Check if current file is a potential model file inside an existing
					 * scope. This helps excluding obvious non-model files and model files that are out of scope right
					 * away and avoids potentially lengthy but useless processing of the same.
					 */
					if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
						progress.subTask(NLS.bind(Messages.subtask_analyzingFile, file.getFullPath()));

						// Skip files which don't make it through specified meta model filter
						IMetaModelDescriptor effectiveMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getEffectiveDescriptor(file);
						if (mmFilter == null || mmFilter.getClass().isInstance(effectiveMMDescriptor)) {
							// Ignore files that already have been loaded unless specified otherwise
							if (!ignoreIfAlreadyLoaded || !EcorePlatformUtil.isFileLoaded(file)) {
								// Retrieve resource scope which current file belongs to
								IResourceScopeProvider resourceScopeProvider = ResourceScopeProviderRegistry.INSTANCE
										.getResourceScopeProvider(effectiveMMDescriptor);
								if (resourceScopeProvider != null) {
									IResourceScope resourceScope = resourceScopeProvider.getScope(file);
									if (resourceScope != null) {
										// Retrieve editing domain which current file is mapped to
										TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getMappedEditingDomain(file);
										if (editingDomain != null) {
											// Retrieve already existing load file request for current editing domain
											Collection<IFile> filesToLoadInEditingDomain = filesToLoad.get(editingDomain);
											if (filesToLoadInEditingDomain == null) {
												filesToLoadInEditingDomain = new HashSet<IFile>();
												filesToLoad.put(editingDomain, filesToLoadInEditingDomain);
											}
											// Add current file current load file request
											filesToLoadInEditingDomain.add(file);
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}

			progress.worked(1);
			progress.subTask(""); //$NON-NLS-1$
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		return filesToLoad;
	}

	protected void runLoadModelFiles(Map<TransactionalEditingDomain, Collection<IFile>> filesToLoad, IProgressMonitor monitor)
			throws OperationCanceledException {
		Assert.isNotNull(filesToLoad);
		SubMonitor progress = SubMonitor.convert(monitor, filesToLoad.keySet().size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Iterate over editing domains of files to load
		for (TransactionalEditingDomain editingDomain : filesToLoad.keySet()) {
			loadModelFilesInEditingDomain(editingDomain, filesToLoad.get(editingDomain), progress.newChild(1));
		}

		// Perform a full garbage collection
		ExtendedPlatform.performGarbageCollection();
	}

	protected void loadModelFilesInEditingDomain(final TransactionalEditingDomain editingDomain, final Collection<IFile> filesToLoadInEditingDomain,
			final IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(filesToLoadInEditingDomain);

		try {
			editingDomain.runExclusive(new Runnable() {
				@Override
				public void run() {
					loadModelFiles(editingDomain, filesToLoadInEditingDomain, monitor);
				}
			});
		} catch (InterruptedException ex) {
			PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
		}
		monitor.done();
	}

	protected void loadModelFiles(final TransactionalEditingDomain editingDomain, final Collection<IFile> filesToLoadInEditingDomain,
			final IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Disable resolution of fragment-based proxies while model loading is ongoing
		ProxyHelper proxyHelper = ProxyHelperAdapterFactory.INSTANCE.adapt(editingDomain.getResourceSet());
		if (proxyHelper != null) {
			proxyHelper.setIgnoreFragmentBasedProxies(true);
		}

		// Load files into editing domain
		SubMonitor loadProgress = progress.newChild(80).setWorkRemaining(filesToLoadInEditingDomain.size());
		Set<IFile> loadedFiles = new HashSet<IFile>();
		for (IFile file : filesToLoadInEditingDomain) {
			loadModelFile(editingDomain, file, loadedFiles, loadProgress);
		}

		if (proxyHelper != null) {
			// Update unresolved proxy blacklist according to newly loaded files
			updateUnresolvedProxyBlackList(loadedFiles, proxyHelper.getBlackList());

			// Perform a performance-optimized resolution of fragment-based proxies
			forceProxyResolution(loadedFiles, proxyHelper.getLookupResolver(), progress.newChild(20));

			// Re-enable resolution of fragment-based proxies
			proxyHelper.setIgnoreFragmentBasedProxies(false);
		} else {
			progress.worked(20);
		}
	}

	protected void loadModelFile(final TransactionalEditingDomain editingDomain, final IFile fileToLoad, Set<IFile> loadedFiles,
			SubMonitor loadProgress) {
		loadProgress.subTask(NLS.bind(Messages.subtask_loadingFile, fileToLoad.getFullPath().toString()));
		try {
			Map<?, ?> loadOptions = Collections.singletonMap(ExtendedResource.OPTION_PROGRESS_MONITOR, loadProgress.newChild(1));
			EcorePlatformUtil.loadResource(editingDomain, fileToLoad, loadOptions);
			loadedFiles.add(fileToLoad);
		} catch (Exception ex) {
			// Ignore exception
			/*
			 * !! Important Note !! The exception has already been recorded as error on resource and will be converted
			 * to a problem marker by the resource problem handler later on (see
			 * org.eclipse.sphinx.emf.util.EcoreResourceUtil#loadModelResource(ResourceSet, URI, Map<?, ?>, boolean) and
			 * org.eclipse.sphinx.emf.internal.resource.ResourceProblemHandler#resourceSetChanged(
			 * ResourceSetChangeEvent)) for details).
			 */
		}

		loadProgress.subTask(""); //$NON-NLS-1$
		if (loadProgress.isCanceled()) {
			throw new OperationCanceledException();
		}
		editingDomain.yield();
	}

	protected void updateUnresolvedProxyBlackList(Collection<IFile> files, ModelIndex blackList) {
		Assert.isNotNull(files);
		Assert.isNotNull(blackList);

		for (IFile file : files) {
			Resource resource = EcorePlatformUtil.getResource(file);
			blackList.updateIndexOnResourceLoaded(resource);
		}
	}

	/**
	 * This is a fast implementation of proxy resolution. The idea is to use a map of URI -> EObject to quickly get
	 * object.
	 */
	protected void forceProxyResolution(Collection<IFile> files, EcoreIndex lookupResolver, IProgressMonitor monitor) {
		Assert.isNotNull(files);
		Assert.isNotNull(lookupResolver);

		// Get models behind given set of files
		Set<IModelDescriptor> modelDescriptors = new HashSet<IModelDescriptor>();
		for (IFile file : files) {
			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(file);
			if (modelDescriptor != null) {
				modelDescriptors.add(modelDescriptor);
			}
		}

		SubMonitor progress = SubMonitor.convert(monitor, modelDescriptors.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Force proxies within each model to be resolved
		for (IModelDescriptor modelDescriptor : modelDescriptors) {
			synchronized (lookupResolver) {
				SubMonitor resolveProxiesInModelProgress = progress.newChild(1).setWorkRemaining(100);

				// Initialize lookup-based proxy resolver
				resolveProxiesInModelProgress.subTask(NLS.bind(Messages.subtask_initializingProxyResolutionForModelInRoot,
						modelDescriptor.getMetaModelDescriptor().getName(), modelDescriptor.getRoot().getFullPath()));

				Collection<Resource> resources = modelDescriptor.getLoadedResources(true);
				lookupResolver.init(resources);

				resolveProxiesInModelProgress.worked(50);
				if (resolveProxiesInModelProgress.isCanceled()) {
					throw new OperationCanceledException();
				}

				resolveProxiesInResources(resources, resolveProxiesInModelProgress);

				// Clear lookup-based proxy resolver
				lookupResolver.clear();
			}
		}
		progress.subTask(""); //$NON-NLS-1$

		// Perform a full garbage collection
		ExtendedPlatform.performGarbageCollection();
	}

	protected void resolveProxiesInResources(Collection<Resource> resources, SubMonitor resolveProxiesInModelProgress) {
		// Try to resolve all proxies in given model
		SubMonitor resolveProxiesInResourcesProgress = resolveProxiesInModelProgress.newChild(50).setWorkRemaining(resources.size());
		for (Resource resource : resources) {
			resolveProxiesInResourcesProgress
					.subTask(NLS.bind(Messages.subtask_resolvingProxiesInResource, resource.getURI().toPlatformString(true)));

			EObjectUtil.resolveAll(resource);

			resolveProxiesInResourcesProgress.worked(1);
			if (resolveProxiesInResourcesProgress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
	}

	protected void runDetectAndReloadModelFiles(Collection<IFile> files, IMetaModelDescriptor mmDescriptor, boolean memoryOptimized,
			IProgressMonitor monitor) throws OperationCanceledException {
		SubMonitor progress = SubMonitor.convert(monitor, Messages.task_reloadingModelFiles, 100);
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		Map<TransactionalEditingDomain, Collection<IFile>> filesToUnload = detectFilesToUnload(files, mmDescriptor, progress.newChild(5));
		Map<TransactionalEditingDomain, Collection<IFile>> filesToLoad = detectFilesToLoad(files, mmDescriptor, false, progress.newChild(10));

		// Nothing to reload?
		if (filesToUnload.size() == 0 && filesToLoad.size() == 0) {
			progress.done();

			// TODO Surround with appropriate tracing option
			// System.out.println("[ModelLoadManager#runDetectAndReloadModelFiles()] No model files to be reloaded");
			return;
		}

		runReloadModelFiles(filesToUnload, filesToLoad, memoryOptimized, progress.newChild(85));

		// TODO Surround with appropriate tracing option
		// System.out.println("[ModelLoadManager#runDetectAndReloadModelFiles()] Loaded " +
		// getFilesToLoadCount(filesToLoad) + " and unloaded "
		// + getFilesToUnloadCount(filesToUnload) + " model file(s)");
	}

	protected void runReloadModelFiles(final Map<TransactionalEditingDomain, Collection<IFile>> filesToUnload,
			final Map<TransactionalEditingDomain, Collection<IFile>> filesToLoad, final boolean memoryOptimized, IProgressMonitor monitor)
			throws OperationCanceledException {
		Assert.isNotNull(filesToUnload);
		Assert.isNotNull(filesToLoad);
		final SubMonitor progress = SubMonitor.convert(monitor, getFilesToUnloadCount(filesToUnload) + getFilesToLoadCount(filesToLoad));
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		// Separate out editing domains with files to be unloaded only
		Map<TransactionalEditingDomain, Collection<IFile>> filesToUnloadOnly = new HashMap<TransactionalEditingDomain, Collection<IFile>>();
		for (TransactionalEditingDomain editingDomain : new HashSet<TransactionalEditingDomain>(filesToUnload.keySet())) {
			if (!filesToLoad.containsKey(editingDomain)) {
				filesToUnloadOnly.put(editingDomain, filesToUnload.get(editingDomain));
				filesToUnload.remove(editingDomain);
			}
		}

		// Separate out editing domains with files to be loaded only
		Map<TransactionalEditingDomain, Collection<IFile>> filesToLoadOnly = new HashMap<TransactionalEditingDomain, Collection<IFile>>();
		for (TransactionalEditingDomain editingDomain : new HashSet<TransactionalEditingDomain>(filesToLoad.keySet())) {
			if (!filesToUnload.containsKey(editingDomain)) {
				filesToLoadOnly.put(editingDomain, filesToLoad.get(editingDomain));
				filesToLoad.remove(editingDomain);
			}
		}

		// Process editing domains with files to be unloaded only
		if (filesToUnloadOnly.size() > 0) {
			runUnloadModelFiles(filesToUnloadOnly, memoryOptimized, progress.newChild(getFilesToUnloadCount(filesToUnloadOnly)));
		}

		// Process editing domains with files to be actually reloaded
		for (final TransactionalEditingDomain editingDomain : filesToUnload.keySet()) {
			try {
				// Create read transaction to ensure that reload procedure is atomic
				editingDomain.runExclusive(new Runnable() {
					@Override
					public void run() {
						// Unload files to be unloaded from current editing domain
						Collection<IFile> filesToUnloadInEditingDomain = filesToUnload.get(editingDomain);
						int totalWork = filesToUnloadInEditingDomain.size();
						EcorePlatformUtil.unloadFiles(editingDomain, filesToUnloadInEditingDomain, memoryOptimized, progress.newChild(totalWork));

						// Load files to be loaded into current editing domain
						Collection<IFile> filesToLoadInEditingDomain = filesToLoad.get(editingDomain);
						totalWork = filesToLoadInEditingDomain.size();
						loadModelFilesInEditingDomain(editingDomain, filesToLoadInEditingDomain, progress.newChild(totalWork));
					}
				});
			} catch (InterruptedException ex) {
				PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
			}
		}

		// Process editing domains with files to be loaded only
		if (filesToLoadOnly.size() > 0) {
			runLoadModelFiles(filesToLoadOnly, progress.newChild(getFilesToLoadCount(filesToLoadOnly)));
		}

		// Perform a full garbage collection
		ExtendedPlatform.performGarbageCollection();
	}

	protected void runUnloadModelFiles(Map<TransactionalEditingDomain, Collection<IFile>> filesToUnload, boolean memoryOptimized,
			IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(filesToUnload);
		SubMonitor progress = SubMonitor.convert(monitor, getFilesToUnloadCount(filesToUnload));
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		for (TransactionalEditingDomain editingDomain : filesToUnload.keySet()) {
			Collection<IFile> filesToUnloadInEditingDomain = filesToUnload.get(editingDomain);
			int totalWork = filesToUnloadInEditingDomain.size();
			EcorePlatformUtil.unloadFiles(editingDomain, filesToUnloadInEditingDomain, memoryOptimized, progress.newChild(totalWork));
		}

		// Perform a full garbage collection
		ExtendedPlatform.performGarbageCollection();
	}

	protected Map<TransactionalEditingDomain, Collection<IFile>> detectFilesToUnload(Collection<IFile> files, IMetaModelDescriptor mmFilter,
			IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(files);
		SubMonitor progress = SubMonitor.convert(monitor, files.size());
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}

		Map<TransactionalEditingDomain, Collection<IFile>> filesToUnload = new HashMap<TransactionalEditingDomain, Collection<IFile>>();
		for (IFile file : files) {
			try {
				/*
				 * Performance optimization: Check if current file is a potential model file by investigating it's
				 * extension. This helps excluding obvious non-model files right away and avoids potentially lengthy but
				 * useless processing of the same.
				 */
				if (ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file)) {
					progress.subTask(NLS.bind(Messages.subtask_analyzingFile, file.getFullPath()));

					// Skip files which don't make it through specified meta model filter
					IMetaModelDescriptor effectiveMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getEffectiveDescriptor(file);
					if (mmFilter == null || mmFilter.getClass().isInstance(effectiveMMDescriptor)) {
						// Retrieve editing domains in which current file is currently loaded
						/*
						 * !! Important Note !! For the sake of robustness, it is necessary to consider all editing
						 * domains but not only the one which would be returned by
						 * WorkspaceEditingDomainUtil#getCurrentEditingDomain(IFile). Although not really intended by
						 * Sphinx workspace management it might anyway happen that the same file gets loaded into
						 * multiple editing domains. Typical reasons for this are e.g. lazy loading of one file from
						 * multiple other files which are in different editing domains or programatic action by some
						 * application. We then have to make sure that the given file gets unloaded from all editing
						 * domains it is in.
						 */
						for (TransactionalEditingDomain editingDomain : WorkspaceEditingDomainUtil.getAllEditingDomains()) {
							if (EcorePlatformUtil.isFileLoaded(editingDomain, file)) {
								Collection<IFile> filesToUnloadInEditingDomain = filesToUnload.get(editingDomain);
								if (filesToUnloadInEditingDomain == null) {
									filesToUnloadInEditingDomain = new HashSet<IFile>();
									filesToUnload.put(editingDomain, filesToUnloadInEditingDomain);
								}
								filesToUnloadInEditingDomain.add(file);
							}
						}
					}
				}
			} catch (Exception ex) {
				PlatformLogUtil.logAsWarning(Activator.getPlugin(), ex);
			}

			progress.worked(1);
			progress.subTask(""); //$NON-NLS-1$
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		return filesToUnload;
	}

	protected SchedulingRuleFactory getSchedulingRuleFactory() {
		if (schedulingRuleFactory == null) {
			schedulingRuleFactory = createSchedulingRuleFactory();
		}
		return schedulingRuleFactory;
	}

	protected SchedulingRuleFactory createSchedulingRuleFactory() {
		return new SchedulingRuleFactory();
	}

	protected int compare(IMetaModelDescriptor mmd1, IMetaModelDescriptor mmd2) {
		if (mmd1 == null || MetaModelDescriptorRegistry.ANY_MM.equals(mmd1)) {
			if (mmd2 == null || MetaModelDescriptorRegistry.ANY_MM.equals(mmd2)) {
				return EQUAL;
			} else {
				return GREATER_THAN;
			}
		} else if (mmd2 == null || MetaModelDescriptorRegistry.ANY_MM.equals(mmd2)) {
			return SMALLER_THAN;
		} else if (mmd1.getClass().isAssignableFrom(mmd2.getClass())) {
			return GREATER_THAN;
		} else if (mmd2.getClass().isAssignableFrom(mmd1.getClass())) {
			return SMALLER_THAN;
		} else {
			return DIFFERENT;
		}
	}

	/**
	 * @param list1
	 * @param list2
	 * @return
	 */
	protected <T> int compare(Collection<T> list1, Collection<T> list2) {
		int from1ContainedIn2 = 0;
		int from2ContainedIn1 = 0;
		for (T o : list1) {
			if (list2.contains(o)) {
				from1ContainedIn2++;
			}
		}
		for (T o : list2) {
			if (list1.contains(o)) {
				from2ContainedIn1++;
			}
		}
		if (from1ContainedIn2 == from2ContainedIn1 && from1ContainedIn2 == list1.size() && from2ContainedIn1 == list2.size()) {
			return EQUAL;
		} else if (from1ContainedIn2 == list1.size()) {
			return SMALLER_THAN;
		} else if (from2ContainedIn1 == list2.size()) {
			return GREATER_THAN;
		} else {
			return DIFFERENT;
		}
	}
}
