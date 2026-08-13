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
 *     itemis - [410825] Make sure that EcorePlatformUtil#getResourcesInModel(contextResource, includeReferencedModels) method return resources of the context resource in the same resource set
 *     itemis - [421205] Model descriptor registry does not return correct model descriptor for (shared) plugin resources
 *     itemis - [423662] Prevent creation of duplicated model descriptors by design
 *     itemis - [425854] The diagram created in the Artop is not saved after being updated to "sphinx-Update-0.8.0M4".
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ScopingResourceSet;
import org.eclipse.sphinx.emf.scoping.IResourceScope;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * A default implementation of model descriptor.
 * 
 * @see IModelDescriptor
 */
public class ModelDescriptor extends PlatformObject implements IModelDescriptor {

	/**
	 * The meta-model descriptor.
	 */
	protected IMetaModelDescriptor mmDescriptor;

	/**
	 * The target meta-model descriptor.
	 */
	protected IMetaModelDescriptor targetMMDescriptor;

	/**
	 * The {@link IResourceScope resource scope} of the model described here.
	 */
	protected IResourceScope resourceScope;

	/**
	 * The editing domain of the described model.
	 */
	private TransactionalEditingDomain editingDomain = null;

	/**
	 * Constructor.
	 * 
	 * @param mmDescriptor
	 *            The meta-model descriptor.
	 * @param rootProject
	 *            The root project of the described model.
	 */
	public ModelDescriptor(IMetaModelDescriptor mmDescriptor, IMetaModelDescriptor targetMMDescriptor, IResourceScope resourceScope) {
		Assert.isNotNull(mmDescriptor);
		Assert.isNotNull(resourceScope);
		this.mmDescriptor = mmDescriptor;
		this.targetMMDescriptor = targetMMDescriptor;
		this.resourceScope = resourceScope;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getMetaModelDescriptor()
	 */
	@Override
	public IMetaModelDescriptor getMetaModelDescriptor() {
		return mmDescriptor;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getTargetMetaModelDescriptor()
	 */
	@Override
	public IMetaModelDescriptor getTargetMetaModelDescriptor() {
		return targetMMDescriptor;
	}

	protected IMetaModelDescriptor getEffectiveMetaModelDescriptor() {
		if (targetMMDescriptor != null) {
			return targetMMDescriptor;
		}
		return mmDescriptor;
	}

	@Override
	public IResourceScope getScope() {
		return resourceScope;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getEditingDomain()
	 */
	@Override
	public TransactionalEditingDomain getEditingDomain() {
		if (editingDomain == null) {
			IResource resourceScopeRoot = getRoot();
			if (resourceScopeRoot instanceof IContainer) {
				editingDomain = WorkspaceEditingDomainUtil.getEditingDomain((IContainer) resourceScopeRoot, getEffectiveMetaModelDescriptor());
			} else if (resourceScopeRoot instanceof IFile) {
				editingDomain = WorkspaceEditingDomainUtil.getEditingDomain((IFile) resourceScopeRoot);
			} else {
				throw new RuntimeException("Unable to retrieve editing domain for resource scope root being an instance of '" //$NON-NLS-1$
						+ resourceScopeRoot.getClass().getSimpleName() + "'"); //$NON-NLS-1$
			}
		}
		return editingDomain;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getRoot()
	 */
	@Override
	public IResource getRoot() {
		return resourceScope.getRoot();
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getProjects()
	 */
	@Override
	public Collection<IResource> getReferencedRoots() {
		return resourceScope.getReferencedRoots();
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getReferencingRoots()
	 */
	@Override
	public Collection<IResource> getReferencingRoots() {
		return resourceScope.getReferencingRoots();
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getPersistedFiles(boolean)
	 */
	@Override
	public Collection<IFile> getPersistedFiles(boolean includeReferencedScopes) {
		Collection<IFile> persistedFiles = new HashSet<IFile>();
		for (IFile file : resourceScope.getPersistedFiles(includeReferencedScopes)) {
			/*
			 * Performance optimization: Check if given file is a potential model file inside an existing scope. This
			 * helps excluding obvious non-model files and model files that are out of scope right away and avoids
			 * potentially lengthy but useless processing of the same.
			 */
			if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
				/*
				 * Only work with synchronized files to avoid automatic synchronization that will be triggered if the
				 * file needs to be read for content type detection (see
				 * org.eclipse.sphinx.platform.util.ExtendedPlatform.getContentTypeId(IFile)). Automatic synchronization
				 * is triggered by EMF in
				 * org.eclipse.emf.ecore.resource.impl.PlatformResourceURIHandlerImpl.WorkbenchHelper.
				 * createPlatformResourceInputStream(String, Map<?, ?>).
				 */
				boolean isSynchronized = ExtendedPlatform.isSynchronized(file);
				if (isSynchronized) {
					IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(file);
					if (this.mmDescriptor.equals(mmDescriptor)) {
						if (targetMMDescriptor != null) {
							IMetaModelDescriptor targetMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getTargetDescriptor(file);
							if (this.targetMMDescriptor.equals(targetMMDescriptor)) {
								persistedFiles.add(file);
							}
						} else {
							persistedFiles.add(file);
						}
					}
				}
			}
		}

		return Collections.unmodifiableCollection(persistedFiles);
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#getLoadedResources()
	 */
	@Override
	public Collection<Resource> getLoadedResources(final boolean includeReferencedScopes) {
		try {
			return TransactionUtil.runExclusive(getEditingDomain(), new RunnableWithResult.Impl<List<Resource>>() {
				@Override
				public void run() {
					ResourceSet resourceSet = getEditingDomain().getResourceSet();
					if (resourceSet instanceof ScopingResourceSet) {
						setResult(((ScopingResourceSet) resourceSet).getResourcesInModel(ModelDescriptor.this, includeReferencedScopes));
					} else {
						setResult(resourceSet.getResources());
					}
				}
			});
		} catch (InterruptedException ex) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
		}
		return Collections.emptyList();
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#belongsTo(org.eclipse.core.resources.IFile, boolean)
	 */
	@Override
	public boolean belongsTo(IFile file, boolean includeReferencedScopes) {
		if (file != null) {
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(file);
			if (this.mmDescriptor.equals(mmDescriptor)) {
				if (targetMMDescriptor != null) {
					IMetaModelDescriptor targetMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getTargetDescriptor(file);
					if (this.targetMMDescriptor.equals(targetMMDescriptor)) {
						return resourceScope.belongsTo(file, includeReferencedScopes);
					}
				} else {
					return resourceScope.belongsTo(file, includeReferencedScopes);
				}
			}
		}

		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#belongsTo(org.eclipse.emf.ecore.resource.Resource, boolean)
	 */
	@Override
	public boolean belongsTo(final Resource resource, boolean includeReferencedScopes) {
		if (resource != null) {
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
			if (this.mmDescriptor.equals(mmDescriptor)) {
				if (resourceScope.belongsTo(resource, includeReferencedScopes)) {
					// Make sure that given resource is actually contained in the resource set of this model
					// descriptor's editing domain
					/*
					 * !! Important Note !! The provided resource could be a resource in a resource set that is not
					 * managed by Sphinx (but privately owned by a traditional EMF model editor instead). In this case
					 * we must make sure that the resource is NOT deemed a part of the Sphinx model represented by this
					 * model descriptor. Not doing so would let Sphinx assume that the resource is present in the
					 * resource set of this model descriptor's editing domain and most likely cause pretty unpredictable
					 * side effects.
					 */
					try {
						return TransactionUtil.runExclusive(getEditingDomain(), new RunnableWithResult.Impl<Boolean>() {
							@Override
							public void run() {
								setResult(getEditingDomain().getResourceSet().getResources().contains(resource));
							}
						});
					} catch (InterruptedException ex) {
						PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
					}
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#belongsTo(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public boolean belongsTo(URI uri, boolean includeReferencedScopes) {
		if (uri != null) {
			Resource resource = EcorePlatformUtil.getResource(uri);
			if (resource != null) {
				return belongsTo(resource, includeReferencedScopes);
			} else {
				IFile file = EcorePlatformUtil.getFile(uri);
				return belongsTo(file, includeReferencedScopes);
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#didBelongTo(org.eclipse.core.resources.IFile)
	 */
	@Override
	public boolean didBelongTo(IFile file, boolean includeReferencedScopes) {
		if (file != null) {
			IMetaModelDescriptor oldMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getOldDescriptor(file);
			if (mmDescriptor.equals(oldMMDescriptor)) {
				return resourceScope.didBelongTo(file, includeReferencedScopes);
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#didBelongTo(org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	public boolean didBelongTo(Resource resource, boolean includeReferencedScopes) {
		if (resource != null) {
			IMetaModelDescriptor oldMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getOldDescriptor(resource);
			if (mmDescriptor.equals(oldMMDescriptor)) {
				return resourceScope.didBelongTo(resource, includeReferencedScopes);
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#didBelongTo(org.eclipse.core.resources.IFile)
	 */
	@Override
	public boolean didBelongTo(URI uri, boolean includeReferencedScopes) {
		if (uri != null) {
			Resource resource = EcorePlatformUtil.getResource(uri);
			if (resource != null) {
				return didBelongTo(resource, includeReferencedScopes);
			} else {
				IFile file = EcorePlatformUtil.getFile(uri);
				return didBelongTo(file, includeReferencedScopes);
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#isShared(org.eclipse.core.resources.IFile)
	 */
	@Override
	public boolean isShared(IFile file) {
		return resourceScope.isShared(file);
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#isShared(org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	public boolean isShared(Resource resource) {
		return resourceScope.isShared(resource);
	}

	/*
	 * @see org.eclipse.sphinx.emf.model.IModelDescriptor#isShared(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public boolean isShared(URI uri) {
		return resourceScope.isShared(uri);
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof ModelDescriptor) {
			ModelDescriptor otherModelDescriptor = (ModelDescriptor) object;
			return mmDescriptor.equals(otherModelDescriptor.mmDescriptor)
					&& (targetMMDescriptor == null && otherModelDescriptor.targetMMDescriptor == null || targetMMDescriptor
							.equals(otherModelDescriptor.targetMMDescriptor)) && resourceScope.equals(otherModelDescriptor.resourceScope);
		}
		return false;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return mmDescriptor.hashCode() + (targetMMDescriptor != null ? targetMMDescriptor.hashCode() : 0) + resourceScope.getRoot().hashCode();
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return mmDescriptor + (targetMMDescriptor != null ? "->" + targetMMDescriptor : "") + "@" + resourceScope.getRoot().getName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
