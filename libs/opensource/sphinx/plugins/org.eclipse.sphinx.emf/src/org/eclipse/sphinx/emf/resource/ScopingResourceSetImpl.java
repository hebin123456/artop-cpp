/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [420792] Sphinx is not able to load resources that are registered in the EMF package registry
 *     itemis - [421205] Model descriptor registry does not return correct model descriptor for (shared) plugin resources
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.scoping.DefaultResourceScope;
import org.eclipse.sphinx.emf.scoping.IResourceScope;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

/**
 * A default implementation of the {@link ScopingResourceSet} interface.
 */
public class ScopingResourceSetImpl extends ExtendedResourceSetImpl implements ScopingResourceSet {

	private IResourceScope outsideWorkspaceScope;

	/**
	 * Default constructor.
	 */
	public ScopingResourceSetImpl() {
	}

	/**
	 * Returns the {@link IResourceScope resource scope} used to encompass all resources that are located outside of the
	 * workspace.
	 *
	 * @return The scope used to encompass all resources that are located outside of the workspace.
	 */
	protected IResourceScope getOutsideWorkspaceScope() {
		if (outsideWorkspaceScope == null) {
			outsideWorkspaceScope = createOutsideWorkspaceScope();
		}
		return outsideWorkspaceScope;
	}

	/**
	 * Creates the {@link IResourceScope resource scope} used to encompass all resources that are located outside of the
	 * workspace.
	 *
	 * @return The scope used to encompass all resources that are located outside of the workspace.
	 */
	protected IResourceScope createOutsideWorkspaceScope() {
		return new DefaultResourceScope() {
			@Override
			public boolean belongsTo(Resource resource, boolean includeReferencedScopes) {
				Assert.isNotNull(resource);
				return !resource.getURI().isPlatform() || isShared(resource);
			}

			@Override
			public boolean isShared(Resource resource) {
				Assert.isNotNull(resource);
				return resource.getURI().isPlatformPlugin();
			}
		};
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ScopingResourceSet#getResourcesInModel(java.lang.Object)
	 */
	@Override
	public List<Resource> getResourcesInModel(Object contextObject) {
		return getResourcesInScope(contextObject, true, false);
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ScopingResourceSet#getResourcesInModel(java.lang.Object, boolean)
	 */
	@Override
	public List<Resource> getResourcesInModel(Object contextObject, boolean includeReferencedScopes) {
		return getResourcesInScope(contextObject, includeReferencedScopes, false);
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ScopingResourceSet#getResourcesInScope(java.lang.Object)
	 */
	@Override
	public List<Resource> getResourcesInScope(Object contextObject) {
		return getResourcesInScope(contextObject, true, true);
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ScopingResourceSet#getResourcesInScope(java.lang.Object, boolean)
	 */
	@Override
	public List<Resource> getResourcesInScope(Object contextObject, boolean includeReferencedScopes) {
		return getResourcesInScope(contextObject, includeReferencedScopes, true);
	}

	/**
	 * Retrieves the {@link Resource resource}s contained by this {@link ResourceSet resource set} that belong to the
	 * {@link IResourceScope resource scope}(s) behind provided <code>contextObject</code>.
	 *
	 * @param contextObject
	 *            The context object the resource scope to refer to.
	 * @param includeReferencedScopes
	 *            <code>true</code> if the resources of resource scopes that are referenced by the resource scope behind
	 *            the context object are to be retrieved as well, <code>false</code> if only the resources of the
	 *            resource scope behind the context object are to be retrieved.
	 * @param ignoreMetaModel
	 *            <code>true</code> if the resources should be retrieved regardless whether their metamodel descriptor
	 *            matches that behind the context object, <code>false</code> if the metamodel descriptors of the
	 *            resources and the context object are required to match.
	 * @return The resources contained by this resource set that belong to the resource scope(s) behind provided
	 *         <code>contextObject</code>.
	 */
	protected List<Resource> getResourcesInScope(Object contextObject, boolean includeReferencedScopes, boolean ignoreMetaModel) {
		// Retrieve resource scope(s) along with the descriptor(s) of the metamodel(s) using it(them) behind given
		// context object
		Map<IResourceScope, Set<IMetaModelDescriptor>> contextResourceScopes = getContextResourceScopes(contextObject);

		// Collect resources which belong to the same resource scope(s) and metamodel(s) (if required) as the context
		// object does
		/*
		 * !! Important Note !! A LinkedHashSet is used to preserve the ordering of the Resources. Using a simple
		 * HashSet will not preserve the ordering which leads to inconsistent results when implementing Resource merging
		 * based on the getResourcesInScope() method.
		 */
		Set<Resource> resourcesInScope = new LinkedHashSet<Resource>();
		List<Resource> safeResources = new ArrayList<Resource>(getResources());
		for (Resource resource : safeResources) {
			for (IResourceScope contextResourceScope : contextResourceScopes.keySet()) {
				if (contextResourceScope.belongsTo(resource, includeReferencedScopes)) {
					if (ignoreMetaModel || hasMatchingMetaModel(contextResourceScopes.get(contextResourceScope), resource)) {
						resourcesInScope.add(resource);
						break;
					}
				}
			}
		}
		return Collections.unmodifiableList(new ArrayList<Resource>(resourcesInScope));
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.resource.ScopingResourceSet#isResourceInScope(org.eclipse.emf.ecore.resource.Resource,
	 * java.lang.Object)
	 */
	@Override
	public boolean isResourceInScope(Resource resource, Object contextObject) {
		return isResourceInScope(resource, contextObject, true, true);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.resource.ScopingResourceSet#isResourceInScope(org.eclipse.emf.ecore.resource.Resource,
	 * java.lang.Object, boolean)
	 */
	@Override
	public boolean isResourceInScope(Resource resource, Object contextObject, boolean includeReferencedScopes) {
		return isResourceInScope(resource, contextObject, includeReferencedScopes, true);
	}

	/**
	 * Tests if given {@link Resource resource} belongs to the {@link IResourceScope resource scope}(s) behind provided
	 * <code>contextObject</code>.
	 *
	 * @param resource
	 *            The resource to be investigated.
	 * @param contextObject
	 *            The context object identifying the resource scope to refer to.
	 * @param includeReferencedScopes
	 *            <code>true</code> if the resource scopes that are referenced by the resource scope behind the context
	 *            object are to be considered as well, <code>false</code> if only the resource scope behind the context
	 *            object is to be considered.
	 * @param ignoreMetaModel
	 *            <code>true</code> if the given resource should be considered to belong to the resource scope behind
	 *            the context object regardless whether its metamodel descriptor matches that of the context object,
	 *            <code>false</code> if the metamodel descriptors of the given resource and the context object are
	 *            required to match.
	 * @return <code>true</code> if given resource belongs to the resource scope behind provided
	 *         <code>contextObject</code>, or <code>false</code> otherwise.
	 */
	protected boolean isResourceInScope(Resource resource, Object contextObject, boolean includeReferencedScopes, boolean ignoreMetaModel) {
		// Retrieve resource scope(s) along with the descriptor(s) of the metamodel(s) using it(them) behind given
		// context object
		Map<IResourceScope, Set<IMetaModelDescriptor>> contextResourceScopes = getContextResourceScopes(contextObject);

		// Check if resource belongs to the same resource scope(s) and metamodel(s) (if required) as the context
		// object does
		for (IResourceScope contextResourceScope : contextResourceScopes.keySet()) {
			if (contextResourceScope.belongsTo(resource, includeReferencedScopes)) {
				if (ignoreMetaModel || hasMatchingMetaModel(contextResourceScopes.get(contextResourceScope), resource)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Retrieves a map that is keyed by the {@link IResourceScope resource scopes} behind given context object and
	 * yields the {@link IMetaModelDescriptor descriptors of the metamodel}s using them.
	 *
	 * @param contextObject
	 *            The context object to retrieve the resource scopes for.
	 * @return A map containing the resource scopes behind given context object along with the descriptor of the
	 *         metamodels using them.
	 */
	protected Map<IResourceScope, Set<IMetaModelDescriptor>> getContextResourceScopes(Object contextObject) {
		Map<IResourceScope, Set<IMetaModelDescriptor>> resourceScopes = new HashMap<IResourceScope, Set<IMetaModelDescriptor>>(1);

		// Try to retrieve resource behind given context object
		Resource resource = EcoreResourceUtil.getResource(contextObject);
		if (resource != null) {
			contextObject = resource;
		}

		if (contextObject instanceof IFile) {
			IFile contextFile = (IFile) contextObject;

			// Retrieve URI behind context file
			contextObject = EcorePlatformUtil.createURI(contextFile.getFullPath());
		}

		if (contextObject instanceof URI) {
			URI contextURI = (URI) contextObject;

			// Try to resolve context URI to resource contained by this resource set
			resource = getResource(contextURI, false);
			if (resource != null) {
				contextObject = resource;
			} else {
				// Try to resolve context URI to workspace root, project or folder
				if (contextURI.isPlatformResource()) {
					IPath contextPath = new Path(contextURI.toPlatformString(true));
					IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(contextPath);
					if (member instanceof IContainer) {
						contextObject = member;
					}
				}
			}
		}

		if (contextObject instanceof Resource) {
			Resource contextResource = (Resource) contextObject;

			// Try to find descriptor of model that context resource belongs to
			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(contextResource);
			if (modelDescriptor != null) {
				// Return the context model's scope along with the descriptor of its metamodel
				resourceScopes.put(modelDescriptor.getScope(), Collections.singleton(modelDescriptor.getMetaModelDescriptor()));
			} else {
				// Context resource located outside of the workspace?
				if (getOutsideWorkspaceScope().belongsTo(contextResource, false)) {
					// Return common scope for resources located outside of the workspace along with the descriptor of
					// the metamodel behind the context resource
					IMetaModelDescriptor contextMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(contextResource);
					resourceScopes.put(getOutsideWorkspaceScope(), Collections.singleton(contextMMDescriptor));
				}
			}
		}

		if (contextObject instanceof IContainer) {
			IContainer contextContainer = (IContainer) contextObject;
			for (IModelDescriptor modelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(contextContainer)) {
				IResourceScope resourceScope = modelDescriptor.getScope();
				Set<IMetaModelDescriptor> mmDescriptors = resourceScopes.get(resourceScope);
				if (mmDescriptors == null) {
					mmDescriptors = new HashSet<IMetaModelDescriptor>(1);
					resourceScopes.put(resourceScope, mmDescriptors);
				}
				mmDescriptors.add(modelDescriptor.getMetaModelDescriptor());
			}
		}

		if (contextObject instanceof IModelDescriptor) {
			IModelDescriptor modelDescriptor = (IModelDescriptor) contextObject;
			resourceScopes.put(modelDescriptor.getScope(), Collections.singleton(modelDescriptor.getMetaModelDescriptor()));
		}

		if (contextObject instanceof IResourceScope) {
			resourceScopes.put((IResourceScope) contextObject, Collections.singleton(MetaModelDescriptorRegistry.ANY_MM));
		}
		return resourceScopes;
	}

	protected boolean hasMatchingMetaModel(Set<IMetaModelDescriptor> mmDescriptors, Resource resource) {
		Assert.isNotNull(mmDescriptors);

		IMetaModelDescriptor resourceMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
		if (resourceMMDescriptor != null) {
			for (IMetaModelDescriptor mmDescriptor : mmDescriptors) {
				if (resourceMMDescriptor.equals(mmDescriptor) || MetaModelDescriptorRegistry.ANY_MM == mmDescriptor) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl#getEObject(org.eclipse.emf.common.util.URI,
	 * org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor, java.lang.Object, boolean)
	 */
	@Override
	protected EObject getEObject(URI uri, IMetaModelDescriptor targetMetaModelDescriptor, Object contextObject, boolean loadOnDemand) {
		Assert.isNotNull(uri);

		// Fragment-based URI not knowing its target resource?
		if (uri.segmentCount() == 0) {
			// Search for object behind given URI within relevant set of potential target resources in scope
			List<Resource> resources = getResourcesToSearchIn(getResourcesInScope(contextObject), uri, targetMetaModelDescriptor);
			return safeFindEObjectInResources(resources, uri, loadOnDemand);
		} else {
			// Target resource is known, so search for object behind given URI only in that resource
			Resource resource = safeGetResource(uri, loadOnDemand);
			if (resource != null) {
				// Do we have any context information?
				if (contextObject != null) {
					// Retrieve EObject behind URI fragment only if target resource is in scope
					if (isResourceInScope(resource, contextObject, true, true)) {
						return safeGetEObjectFromResource(resource, uri.fragment());
					}
				} else {
					// Apply default behavior and retrieve EObject behind URI fragment regardless of the
					// resource's scope
					return safeGetEObjectFromResource(resource, uri.fragment());
				}
			}
			return null;
		}
	}

	/**
	 * Determines the set of {@link Resource resource}s to be considered for resolving given {@link URI}. Only called
	 * when given URI is fragment-based (i.e., has no segments and doesn't reference any explicit target resource).
	 * <p>
	 * This implementation uses the provided {@link EObject context object}, if not <code>null</code>, to retrieve the
	 * {@link #getResourcesInScope(Object) resources in scope}, and returns {@link #getResources() all resources} in
	 * this {@link ScopingResourceSetImpl resource set} otherwise. Clients may override this method in order to tweak
	 * the set of {@link Resource resource}s used for resolving fragment-based {@link URI}s.
	 * </p>
	 *
	 * @param uri
	 *            The fragment-based {@link URI} to resolve.
	 * @param loadOnDemand
	 *            Whether to create and load the {@link Resource target resource}, if it isn't already present in this
	 *            {@link ScopingResourceSetImpl resource set}.
	 * @param contextObject
	 *            The {@link EObject context object} used to determine the set of {@link Resource resource}s to be
	 *            considered for resolving given fragment-based {@link URI}.
	 * @return The set of {@link Resource resource}s to be considered for resolving given fragment-based {@link URI}.
	 * @see #getResourcesInScope(Object)
	 * @see #getResources()
	 * @deprecated Use {@link ExtendedResourceSetImpl#getResourcesToSearchIn(List, URI, IMetaModelDescriptor)} instead.
	 */
	@Deprecated
	protected List<Resource> getResourcesToSearchIn(URI uri, boolean loadOnDemand, EObject contextObject) {
		// Do we have any context information?
		if (contextObject != null) {
			// Only resources in scope are relevant
			return getResourcesInScope(contextObject);
		} else {
			// All resources regardless of their scope need to be considered
			return Collections.unmodifiableList(getResources());
		}
	}
}
