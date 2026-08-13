/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     itemis - [421205] Model descriptor registry does not return correct model descriptor for (shared) plugin resources
 *     itemis - [423662] Prevent creation of duplicated model descriptors by design
 *     itemis - [425854] The diagram created in the Artop is not saved after being updated to "sphinx-Update-0.8.0M4".
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.scoping.IResourceScope;
import org.eclipse.sphinx.emf.scoping.IResourceScopeProvider;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * The model descriptor registry owning descriptors of loaded models.
 * <p>
 * This registry provides methods allowing to add, remove or retrieve one (or several) {@linkplain IModelDescriptor
 * model descriptor}(s). Registry is kept up-to-date thanks to a resource set listener that is notified when any
 * resource is loaded or unloaded.
 *
 * @see org.eclipse.sphinx.emf.model.IModelDescriptor
 */
public class ModelDescriptorRegistry {

	private ListenerList modelDescriptorChangeListeners = new ListenerList();

	/**
	 * The singleton instance of this registry.
	 */
	public static ModelDescriptorRegistry INSTANCE = new ModelDescriptorRegistry();

	/**
	 * The described models.
	 * <p>
	 * This map - which represents an association between a root model project and the models it owns - contains an
	 * entry for each loaded model.
	 */

	protected Map<IMetaModelDescriptor, Set<IModelDescriptor>> modelDescriptors = Collections
			.synchronizedMap(new HashMap<IMetaModelDescriptor, Set<IModelDescriptor>>());

	/**
	 * Private constructor for the singleton pattern.
	 */
	private ModelDescriptorRegistry() {
		// Nothing to do.
	}

	/**
	 * Adds the {@linkplain IModelDescriptor model descriptor} for the specified {@link IFile file} to this registry.
	 * Usually called when the {@linkplain Resource resource} corresponding to the specified {@link IFile file} has just
	 * been loaded.
	 *
	 * @param file
	 *            The {@linkplain IFile file} for which corresponding {@linkplain IModelDescriptor model descriptor}
	 *            must be added/upgraded.
	 * @since 0.7.0
	 */
	public void addModel(IFile file) {
		/*
		 * Performance optimization: Check if given file is a potential model file inside an existing scope. This helps
		 * excluding obvious non-model files and model files that are out of scope right away and avoids potentially
		 * lengthy but useless processing of the same.
		 */
		if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
			/*
			 * Only work with synchronized files to avoid automatic synchronization that will be triggered if the file
			 * needs to be read for content type detection (see
			 * org.eclipse.sphinx.platform.util.ExtendedPlatform.getContentTypeId(IFile)). Automatic synchronization is
			 * triggered by EMF in org.eclipse.emf.ecore.resource.impl.PlatformResourceURIHandlerImpl.WorkbenchHelper.
			 * createPlatformResourceInputStream(String, Map<?, ?>).
			 */
			boolean isSynchronized = ExtendedPlatform.isSynchronized(file);
			if (isSynchronized) {
				IModelDescriptor modelDescriptor = internalGetModel(file);
				if (modelDescriptor == null) {
					internalAddModel(file);
				}
			}
		}
	}

	private void internalAddModel(IFile file) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(file);

		IMetaModelDescriptor targetMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getTargetDescriptor(file);
		IResourceScopeProvider resourceScopeProvider = ResourceScopeProviderRegistry.INSTANCE
				.getResourceScopeProvider(targetMMDescriptor != null ? targetMMDescriptor : mmDescriptor);
		IResourceScope resourceScope = null;
		if (resourceScopeProvider != null) {
			resourceScope = resourceScopeProvider.getScope(file);
		}

		internalAddModel(mmDescriptor, targetMMDescriptor, resourceScopeProvider, resourceScope);
	}

	/**
	 * Adds the {@linkplain IModelDescriptor model descriptor} for the specified {@link Resource resource} to this
	 * registry. Usually called when the {@linkplain Resource resource} has just been loaded.
	 *
	 * @param resource
	 *            The {@linkplain Resource resource} for which corresponding {@linkplain IModelDescriptor model
	 *            descriptor} must be added/upgraded.
	 * @since 0.7.0
	 */
	public void addModel(Resource resource) {
		IModelDescriptor modelDescriptor = internalGetModel(resource);
		if (modelDescriptor == null) {
			internalAddModel(resource);
		}
	}

	private void internalAddModel(Resource resource) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);

		IMetaModelDescriptor targetMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getTargetDescriptor(resource);
		IResourceScopeProvider resourceScopeProvider = ResourceScopeProviderRegistry.INSTANCE
				.getResourceScopeProvider(targetMMDescriptor != null ? targetMMDescriptor : mmDescriptor);
		IResourceScope resourceScope = null;
		if (resourceScopeProvider != null) {
			resourceScope = resourceScopeProvider.getScope(resource);
		}

		internalAddModel(mmDescriptor, targetMMDescriptor, resourceScopeProvider, resourceScope);
	}

	/**
	 * Adds the {@linkplain IModelDescriptor model descriptor} corresponding to the specified
	 * {@linkplain IMetaModelDescriptor meta-model descriptor}, {@linkplain IMetaModelDescriptor target meta-model
	 * descriptor} and {@linkplain IResourceScope resource scope} {@link IResource root} to this registry. The model
	 * descriptor is also added for the resource scopes referenced by the resource scope behind specified
	 * <tt>resourceScopeRoot</tt>.
	 *
	 * @param mmDescriptor
	 *            The meta-model descriptor of the model to add.
	 * @param targetMMDescriptor
	 *            The target meta-model descriptor of the model to add.
	 * @param resourceScopeRoot
	 *            The root of the resource scope of the model to add.
	 * @since 0.8.0
	 */
	public void addModel(IMetaModelDescriptor mmDescriptor, IMetaModelDescriptor targetMMDescriptor, IResource resourceScopeRoot) {
		IResourceScopeProvider resourceScopeProvider = ResourceScopeProviderRegistry.INSTANCE.getResourceScopeProvider(mmDescriptor);
		IResourceScope resourceScope = null;
		if (resourceScopeProvider != null) {
			resourceScope = resourceScopeProvider.getScope(resourceScopeRoot);
		}
		internalAddModel(mmDescriptor, targetMMDescriptor, resourceScopeProvider, resourceScope);
	}

	private void internalAddModel(IMetaModelDescriptor mmDescriptor, IMetaModelDescriptor targetMMDescriptor,
			IResourceScopeProvider resourceScopeProvider, IResourceScope resourceScope) {
		if (mmDescriptor != null && resourceScope != null) {
			// Create and add model descriptor for given root
			internalAddModel(mmDescriptor, targetMMDescriptor, resourceScope);

			// Create and add corresponding model descriptors that are implied on referencing roots if necessary
			for (IResource referencingRoot : resourceScope.getReferencingRoots()) {
				IModelDescriptor referencedModelDescriptor = internalGetModel(referencingRoot, mmDescriptor);
				if (referencedModelDescriptor == null) {
					IResourceScope referencingResourceScope = resourceScopeProvider.getScope(referencingRoot);
					if (referencingResourceScope != null) {
						internalAddModel(mmDescriptor, targetMMDescriptor, referencingResourceScope);
					}
				}
			}
		}
	}

	/**
	 * Adds the {@linkplain IModelDescriptor model descriptor} corresponding to the specified
	 * {@linkplain IMetaModelDescriptor meta-model descriptor}, {@linkplain IMetaModelDescriptor target meta-model
	 * descriptor} and {@link IResourceScope resource scope} to this registry.
	 *
	 * @param mmDescriptor
	 *            The meta-model descriptor of the model to add.
	 * @param targetMMDescriptor
	 *            The target meta-model descriptor of the model to add.
	 * @param resourceScope
	 *            The {@link IResourceScope resource scope} of the model to add.
	 */
	private void internalAddModel(IMetaModelDescriptor mmDescriptor, IMetaModelDescriptor targetMMDescriptor, IResourceScope resourceScope) {
		Assert.isNotNull(mmDescriptor);
		Assert.isNotNull(resourceScope);

		Set<IModelDescriptor> modelDescriptorsForMetaModelDescriptor = modelDescriptors.get(mmDescriptor);
		if (modelDescriptorsForMetaModelDescriptor == null) {
			modelDescriptorsForMetaModelDescriptor = Collections.synchronizedSet(new HashSet<IModelDescriptor>(2));
			modelDescriptors.put(mmDescriptor, modelDescriptorsForMetaModelDescriptor);
		}
		ModelDescriptor modelDescriptor = new ModelDescriptor(mmDescriptor, targetMMDescriptor, resourceScope);
		if (modelDescriptorsForMetaModelDescriptor.add(modelDescriptor)) {
			fireModelAdded(modelDescriptor);
			// TODO Surround with appropriate tracing option
			// System.out.println("[ModelDescriptorRegistry#internalAddModelFor()] Added " + modelDescriptor);
		}
	}

	public void addModels(IProject project) {
		if (project != null) {
			Collection<IFile> files = ExtendedPlatform.getAllFiles(project, false);
			for (IFile file : files) {
				addModel(file);
			}
		}
	}

	/**
	 * Returns the {@link IModelDescriptor descriptor} of the model the specified {@link IFile file} belongs to.
	 *
	 * @param file
	 *            The file whose {@link IModelDescriptor model descriptor} is to be retrieved.
	 * @return The {@link IModelDescriptor descriptor} of the model the specified {@link IFile file} belongs to, or
	 *         <code>null</code> if {@link IFile file} is not part of any model.
	 */
	public IModelDescriptor getModel(IFile file) {
		/*
		 * Performance optimization: Check if given file is a potential model file inside an existing scope. This helps
		 * excluding obvious non-model files and model files that are out of scope right away and avoids potentially
		 * lengthy but useless processing of the same.
		 */
		if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
			IModelDescriptor model = internalGetModel(file);
			if (model == null && getOldModel(file) == null) {
				internalAddModel(file);
				model = internalGetModel(file);
			}
			return model;
		}
		return null;
	}

	/**
	 * Check if specified file is a Sphinx model file
	 *
	 * @param file
	 *            the file be checked.
	 * @return <code>true</code> if specified file is a Sphinx model file. Otherwise <code>false</code>.
	 */
	public boolean isModelFile(IFile file) {
		return getModel(file) != null;
	}

	private IModelDescriptor internalGetModel(IFile file) {
		if (file != null) {
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(file);
			if (mmDescriptor != null) {
				Set<IModelDescriptor> modelDescriptorsForMMDescriptor = modelDescriptors.get(mmDescriptor);
				if (modelDescriptorsForMMDescriptor != null) {
					/*
					 * !! Important Note !! Perform iteration over unsynchronized copy of model descriptor set in order
					 * to avoid deadlocks between threads occupying workspace and waiting for access to synchronized
					 * original model descriptor set and threads being inside synchronized iteration over original model
					 * descriptor set but waiting for workspace access during IModelDescriptor#belongsTo() operation.
					 */
					Set<IModelDescriptor> unsynchronizedModelDescriptorsForMMDescriptor = new HashSet<IModelDescriptor>(
							modelDescriptorsForMMDescriptor);

					for (IModelDescriptor modelDescriptor : unsynchronizedModelDescriptorsForMMDescriptor) {
						// Given file shared among multiple models?
						if (modelDescriptor.isShared(file)) {
							// Return null to indicate that there is no unique model which given file belongs to
							return null;
						}

						if (modelDescriptor.belongsTo(file, false)) {
							return modelDescriptor;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link IModelDescriptor descriptor} of the model the specified {@link Resource resource} belongs to.
	 *
	 * @param resource
	 *            The resource whose {@link IModelDescriptor model descriptor} is to be retrieved.
	 * @return The {@link IModelDescriptor descriptor} of the model the specified {@linkplain Resource resource} belongs
	 *         to, or <code>null</code> if {@link IFile file} is not part of any model.
	 * @since 0.7.0
	 */
	public IModelDescriptor getModel(Resource resource) {
		IModelDescriptor model = internalGetModel(resource);
		if (model == null && getOldModel(resource) == null) {
			internalAddModel(resource);
			model = internalGetModel(resource);
		}
		return model;
	}

	private IModelDescriptor internalGetModel(Resource resource) {
		if (resource != null) {
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
			if (mmDescriptor != null) {
				Set<IModelDescriptor> modelDescriptorsForMMDescriptor = modelDescriptors.get(mmDescriptor);
				if (modelDescriptorsForMMDescriptor != null) {
					/*
					 * !! Important Note !! Perform iteration over unsynchronized copy of model descriptor set in order
					 * to avoid deadlocks between threads occupying workspace and waiting for access to synchronized
					 * original model descriptor set and threads being inside synchronized iteration over original model
					 * descriptor set but waiting for workspace access during IModelDescriptor#belongsTo() operation.
					 */
					Set<IModelDescriptor> unsynchronizedModelDescriptorsForMMDescriptor = new HashSet<IModelDescriptor>(
							modelDescriptorsForMMDescriptor);

					for (IModelDescriptor modelDescriptor : unsynchronizedModelDescriptorsForMMDescriptor) {
						// Given resource shared among multiple models?
						if (modelDescriptor.isShared(resource)) {
							// Return null to indicate that there is no unique model which given resource belongs to
							return null;
						}

						if (modelDescriptor.belongsTo(resource, false)) {
							return modelDescriptor;
						}
					}
				}
			}

		}
		return null;
	}

	/**
	 * Returns the old {@link IModelDescriptor descriptor} of the model the specified {@link IFile file} did belong to
	 * before it was changed or deleted.
	 *
	 * @param file
	 *            The file whose old {@link IModelDescriptor model descriptor} is to be retrieved.
	 * @return The {@link IModelDescriptor descriptor} of the model the specified {@link IFile file} did belong to, or
	 *         <code>null</code> if {@link IFile file} hadn't been part of any model before it was changed or deleted.
	 */
	public IModelDescriptor getOldModel(IFile file) {
		if (file != null) {
			IMetaModelDescriptor oldMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getOldDescriptor(file);
			if (oldMMDescriptor != null) {
				Set<IModelDescriptor> modelDescriptorsForOldMMDescriptor = modelDescriptors.get(oldMMDescriptor);
				if (modelDescriptorsForOldMMDescriptor != null) {
					/*
					 * !! Important Note !! Perform iteration over unsynchronized copy of model descriptor set in order
					 * to avoid deadlocks between threads occupying workspace and waiting for access to synchronized
					 * original model descriptor set and threads being inside synchronized iteration over original model
					 * descriptor set but waiting for workspace access during IModelDescriptor#belongsTo() operation.
					 */
					Set<IModelDescriptor> unsynchronizedModelDescriptorsForOldMMDescriptor = new HashSet<IModelDescriptor>(
							modelDescriptorsForOldMMDescriptor);

					for (IModelDescriptor modelDescriptor : unsynchronizedModelDescriptorsForOldMMDescriptor) {
						if (modelDescriptor.didBelongTo(file, false)) {
							return modelDescriptor;
						}
					}
				}
			}

		}
		return null;
	}

	/**
	 * Returns the old {@link IModelDescriptor descriptor} of the model the specified {@link Resource resource} did
	 * belong to before it was changed or unloaded.
	 *
	 * @param file
	 *            The file whose old {@link IModelDescriptor model descriptor} is to be retrieved.
	 * @return The {@link IModelDescriptor descriptor} of the model the specified {@link Resource resource} did belong
	 *         to, or <code>null</code> if {@link Resource resource} hadn't been part of any model before it was changed
	 *         or unloaded.
	 */
	public IModelDescriptor getOldModel(Resource resource) {
		if (resource != null) {
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getOldDescriptor(resource);
			if (mmDescriptor != null) {
				Set<IModelDescriptor> modelDescriptorsForMMDescriptor = modelDescriptors.get(mmDescriptor);
				if (modelDescriptorsForMMDescriptor != null) {
					/*
					 * !! Important Note !! Perform iteration over unsynchronized copy of model descriptor set in order
					 * to avoid deadlocks between threads occupying workspace and waiting for access to synchronized
					 * original model descriptor set and threads being inside synchronized iteration over original model
					 * descriptor set but waiting for workspace access during IModelDescriptor#belongsTo() operation.
					 */
					Set<IModelDescriptor> unsynchronizedModelDescriptorsForMMDescriptor = new HashSet<IModelDescriptor>(
							modelDescriptorsForMMDescriptor);

					for (IModelDescriptor modelDescriptor : unsynchronizedModelDescriptorsForMMDescriptor) {
						if (modelDescriptor.didBelongTo(resource, false)) {
							return modelDescriptor;
						}
					}
				}
			}

		}
		return null;
	}

	/**
	 * @param container
	 *            The {@linkplain IContainer container} for which {@linkplain IModelDescriptor model descriptor}s must
	 *            be returned; can be an instance of {@linkplain IWorkspaceRoot}, {@linkplain IProject} or
	 *            {@linkplain IFolder}.
	 * @return The {@linkplain IModelDescriptor descriptor}s of the models contained in the specified {@link IContainer
	 *         container}.
	 */
	public Collection<IModelDescriptor> getModels(IContainer container) {
		if (container instanceof IFolder) {
			return getModels((IFolder) container);
		} else if (container instanceof IProject) {
			return getModels((IProject) container);
		} else if (container instanceof IWorkspaceRoot) {
			return getModels((IWorkspaceRoot) container);
		}
		return Collections.emptyList();
	}

	/**
	 * @param container
	 *            The {@linkplain IContainer container} for which {@linkplain IModelDescriptor model descriptor}s must
	 *            be returned; can be an instance of {@linkplain IWorkspaceRoot}, {@linkplain IProject} or
	 *            {@linkplain IFolder}.
	 * @param mmFilter
	 *            meta-model desciptor used to filter model descriptors returned
	 * @return
	 */
	public Collection<IModelDescriptor> getModels(IContainer container, IMetaModelDescriptor mmFilter) {
		Collection<IModelDescriptor> filteredModels = new HashSet<IModelDescriptor>();
		for (IModelDescriptor model : getModels(container)) {
			IMetaModelDescriptor metaModelDescriptor = model.getMetaModelDescriptor();
			if (metaModelDescriptor != null && mmFilter.getClass().isInstance(metaModelDescriptor)) {
				filteredModels.add(model);
			}
		}
		return filteredModels;

	}

	/**
	 * Returns all registered {@linkplain IModelDescriptor model descriptors}.
	 *
	 * @return As specified above.
	 */
	public Collection<IModelDescriptor> getAllModels() {
		return getModels(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Returns all registered {@linkplain IModelDescriptor model descriptors} for the specified
	 * {@linkplain IMetaModelDescriptor meta-model descriptor}.
	 *
	 * @param mmDescriptor
	 *            The meta-model descriptor to use in order to filter the model descriptors in the registry.
	 * @return As specified above.
	 */
	public Collection<IModelDescriptor> getAllModels(IMetaModelDescriptor mmDescriptor) {
		Set<IModelDescriptor> result = modelDescriptors.get(mmDescriptor);
		if (result == null || result.isEmpty()) {
			return Collections.<IModelDescriptor> emptyList();
		}

		return Collections.unmodifiableCollection(result);
	}

	private IModelDescriptor internalGetModel(IResource root, IMetaModelDescriptor mmDescriptor) {
		if (root != null && mmDescriptor != null) {
			Set<IModelDescriptor> modelDescriptorsForMetaModelDescriptor = modelDescriptors.get(mmDescriptor);
			if (modelDescriptorsForMetaModelDescriptor == null) {
				synchronized (modelDescriptors) {
					modelDescriptorsForMetaModelDescriptor = new HashSet<IModelDescriptor>();
					for (IMetaModelDescriptor metaModelDescriptor : modelDescriptors.keySet()) {
						if (mmDescriptor.getClass().isInstance(metaModelDescriptor)) {
							modelDescriptorsForMetaModelDescriptor.addAll(modelDescriptors.get(metaModelDescriptor));
						}
					}

				}
			}

			if (modelDescriptorsForMetaModelDescriptor != null) {
				synchronized (modelDescriptors) {
					for (IModelDescriptor modelDescriptor : modelDescriptorsForMetaModelDescriptor) {
						if (modelDescriptor.getRoot().equals(root)) {
							return modelDescriptor;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param folder
	 *            A folder whose model descriptors must be returned.
	 * @return The list of descriptors for the models under the specified folder.
	 */
	private Collection<IModelDescriptor> getModels(IFolder folder) {
		if (folder != null) {
			Set<IModelDescriptor> modelDescriptors = new HashSet<IModelDescriptor>(1);
			collectModels(folder, modelDescriptors);
			return Collections.unmodifiableCollection(modelDescriptors);
		}
		return Collections.emptyList();
	}

	/**
	 * @param project
	 *            A project whose model descriptors must be returned.
	 * @return The list of descriptors for the models owned by the specified project.
	 */
	private Collection<IModelDescriptor> getModels(IProject project) {
		if (project != null) {
			Set<IModelDescriptor> modelDescriptorsForRoot = new HashSet<IModelDescriptor>();
			synchronized (modelDescriptors) {
				for (Set<IModelDescriptor> modelDescriptorsForMetaModel : modelDescriptors.values()) {
					for (IModelDescriptor modelDescriptor : modelDescriptorsForMetaModel) {
						if (modelDescriptor.getRoot().getProject().equals(project)) {
							modelDescriptorsForRoot.add(modelDescriptor);
						}
					}
				}
				return Collections.unmodifiableCollection(new HashSet<IModelDescriptor>(modelDescriptorsForRoot));
			}
		}
		return Collections.emptySet();
	}

	/**
	 * @param workspaceRoot
	 *            the workspace root whose model descriptors must be returned.
	 * @return The list of descriptors for all the models owned by projects under the specified workspace root.
	 */
	private Collection<IModelDescriptor> getModels(IWorkspaceRoot workspaceRoot) {
		Set<IModelDescriptor> allModels = new HashSet<IModelDescriptor>();
		synchronized (modelDescriptors) {
			for (Set<IModelDescriptor> modelDescriptorsForMetaModelDescriptor : modelDescriptors.values()) {
				allModels.addAll(modelDescriptorsForMetaModelDescriptor);
			}
		}
		return Collections.unmodifiableCollection(allModels);
	}

	/**
	 * Recursively collects {@linkplain IModelDescriptor model descriptor}s from the specified {@link IFolder folder}.
	 *
	 * @param folder
	 *            The folder containing {@link IFile file}s for which descriptors must be collected.
	 * @param modelDescriptors
	 *            The collected {@linkplain IModelDescriptor model descriptor}s.
	 */
	private void collectModels(IFolder folder, Set<IModelDescriptor> modelDescriptors) {
		Assert.isNotNull(modelDescriptors);

		for (IResource resource : ExtendedPlatform.getMembersSafely(folder)) {
			switch (resource.getType()) {
			case IResource.FILE:
				IFile file = (IFile) resource;
				IModelDescriptor modelDescriptor = internalGetModel(file);
				if (modelDescriptor != null) {
					modelDescriptors.add(modelDescriptor);
				}
				break;
			case IResource.FOLDER:
				collectModels((IFolder) resource, modelDescriptors);
				break;
			}
		}
	}

	/**
	 * Moves all {@link IModelDescriptor model descriptor}s from specified {@link IProject old project}, if any, to
	 * specified {@link IProject new project}.Usually called when specified {@link IProject old project} has been
	 * renamed.
	 *
	 * @param oldProject
	 *            The {@link IProject old project} whose {@link IMetaModelDescriptor meta-model descriptor}s are to be
	 *            moved.
	 * @param newProject
	 *            The {@link IProject new project} to which the {@link IProject old project}'s
	 *            {@link IMetaModelDescriptor meta-model descriptor}s are to be moved.
	 */
	public void moveModels(IProject oldProject, IProject newProject) {
		for (IModelDescriptor modelDescriptor : getModels(oldProject)) {
			internalRemoveModel(modelDescriptor);
			internalAddModel(modelDescriptor.getMetaModelDescriptor(), modelDescriptor.getTargetMetaModelDescriptor(), modelDescriptor.getScope());
		}
	}

	/**
	 * Removes the {@linkplain IModelDescriptor model descriptor} for the specified {@link IFile file} from this
	 * registry. Usually called when the {@linkplain Resource resource} corresponding to the specified {@link IFile
	 * file} has just been unloaded.
	 *
	 * @param file
	 *            The {@linkplain IFile file} for which corresponding {@linkplain IModelDescriptor model descriptor}
	 *            must be upgraded/removed.
	 * @since 0.7.0
	 */
	public void removeModel(IFile file) {
		/*
		 * Performance optimization: Check if current file is a potential model file by investigating it's extension.
		 * This helps excluding obvious non-model files right away and avoids potentially lengthy but useless processing
		 * of the same.
		 */
		if (ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file)) {
			IModelDescriptor modelDescriptor = internalGetModel(file);
			if (modelDescriptor == null) {
				modelDescriptor = getOldModel(file);
			}
			if (modelDescriptor != null) {
				// Remove model descriptor if it has no resources anymore
				removeModel(modelDescriptor);
			}
		}
	}

	/**
	 * Removes the {@linkplain IModelDescriptor model descriptor} for the specified {@link Resource resource} from this
	 * registry. Usually called when the {@linkplain Resource resource} has just been unloaded.
	 *
	 * @param resource
	 *            The {@linkplain Resource resource} for which corresponding {@linkplain IModelDescriptor model
	 *            descriptor} must be upgraded/removed.
	 * @since 0.7.0
	 */
	public void removeModel(Resource resource) {
		IModelDescriptor modelDescriptor = internalGetModel(resource);
		if (modelDescriptor == null) {
			modelDescriptor = getOldModel(resource);
		}
		if (modelDescriptor != null) {
			// Remove model descriptor if it has no resources anymore
			removeModel(modelDescriptor);
		}
	}

	/**
	 * Removes the specified {@linkplain IModelDescriptor model descriptor} this registry. Also removes as well all
	 * corresponding {@linkplain IModelDescriptor model descriptor}s from projects referencing the projects covered by
	 * the given {@link IModelDescriptor modelDescriptor}.
	 *
	 * @param modelDescriptor
	 *            The model descriptor to remove from this registry.
	 * @since 0.7.0
	 */
	public void removeModel(IModelDescriptor modelDescriptor) {
		if (modelDescriptor != null && (isModelEmpty(modelDescriptor) || !modelDescriptor.getScope().exists())) {
			internalRemoveModel(modelDescriptor);
			Collection<IResource> referencingRoots = modelDescriptor.getReferencingRoots();
			for (IResource referencingRoot : referencingRoots) {
				IModelDescriptor referencingModelDescriptor = internalGetModel(referencingRoot, modelDescriptor.getMetaModelDescriptor());
				if (referencingModelDescriptor != null) {
					removeModel(referencingModelDescriptor);
				}
			}
		}
	}

	public void removeModels(IContainer container) {
		// TODO Surround with appropriate tracing option
		// System.out.println("[ModelDescriptorRegistry#removeModels()] Removing models from " + project.getName());
		Collection<IModelDescriptor> modelDescriptorsForContainer = getModels(container);
		if (modelDescriptorsForContainer != null) {
			Set<IModelDescriptor> modelDescriptorsInProjectSnapshot = new HashSet<IModelDescriptor>(modelDescriptorsForContainer);
			for (IModelDescriptor modelDescriptor : modelDescriptorsInProjectSnapshot) {
				removeModel(modelDescriptor);
			}
		}
	}

	/**
	 * Removes the specified {@link IModelDescriptor modelDescriptor} from this registry.
	 *
	 * @param modelDescriptor
	 *            The {@linkplain IModelDescriptor model descriptor} to remove from this registry.
	 */
	private void internalRemoveModel(IModelDescriptor modelDescriptor) {
		Assert.isNotNull(modelDescriptor);

		Set<IModelDescriptor> modelDescriptorsForMetaModelDescriptor = modelDescriptors.get(modelDescriptor.getMetaModelDescriptor());
		if (modelDescriptorsForMetaModelDescriptor != null) {
			if (modelDescriptorsForMetaModelDescriptor.remove(modelDescriptor)) {
				fireModelRemoved(modelDescriptor);
				if (modelDescriptorsForMetaModelDescriptor.isEmpty()) {
					modelDescriptors.remove(modelDescriptor.getMetaModelDescriptor());
				}
				// TODO Surround with appropriate tracing option
				// System.out.println("[ModelDescriptorRegistry#internalRemoveModel()] Removed " + modelDescriptor);
			}
		}
	}

	private boolean isModelEmpty(IModelDescriptor modelDescriptor) {
		if (!modelDescriptor.getRoot().isAccessible()) {
			return true;
		}

		return modelDescriptor.getLoadedResources(true).isEmpty() && modelDescriptor.getPersistedFiles(true).isEmpty();
	}

	private void fireModelAdded(IModelDescriptor modelDescriptor) {
		for (Object listener : modelDescriptorChangeListeners.getListeners()) {
			((IModelDescriptorChangeListener) listener).handleModelAdded(modelDescriptor);
		}
	}

	private void fireModelRemoved(IModelDescriptor modelDescriptor) {
		for (Object listener : modelDescriptorChangeListeners.getListeners()) {
			((IModelDescriptorChangeListener) listener).handleModelRemoved(modelDescriptor);
		}
	}

	/**
	 * Adds the given listener for {@link IModelDescriptor model descriptor} change events to this
	 * modelDescriptorRegistry. Has no effect if an identical listener is already registered.
	 *
	 * @param listener
	 *            the listener
	 * @see IModelDescriptorChangeListener
	 */
	public void addModelDescriptorChangeListener(IModelDescriptorChangeListener listener) {
		modelDescriptorChangeListeners.add(listener);
	}

	/**
	 * remove the given listener for {@link IModelDescriptor model descriptor} change events from this
	 * modelDescriptorRegistry. Has no effect if such a listener is not registered.
	 *
	 * @param listener
	 *            the listener
	 * @see IModelDescriptorChangeListener
	 */
	public void removeModelDescriptorChangeListener(IModelDescriptorChangeListener listener) {
		modelDescriptorChangeListeners.remove(listener);
	}

	public void printAllModels() {
		System.out.println("The following models currently exist in this workspace:"); //$NON-NLS-1$
		for (IModelDescriptor modelDescriptor : getAllModels()) {
			System.out.println("  " + modelDescriptor + (isModelEmpty(modelDescriptor) ? "(empty)" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
}
