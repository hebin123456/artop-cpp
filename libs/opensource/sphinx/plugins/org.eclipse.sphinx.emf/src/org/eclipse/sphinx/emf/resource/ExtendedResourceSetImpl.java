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
 *     BMW Car IT - Added/Updated javadoc
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [425379] ExtendedResourceSet may contain a resource multiple times (with normalized and non-normalized URI)
 *	   itemis - [425175] Resources that produce exceptions while being loaded should not get unloaded by Sphinx thereafter
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIException;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelper;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelperAdapterFactory;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.internal.resource.ResourceProblemMarkerService;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.metamodel.services.DefaultMetaModelServiceProvider;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.scoping.IResourceScope;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * An enhanced {@link ResourceSet} implementation.
 */
@SuppressWarnings("deprecation")
public class ExtendedResourceSetImpl extends ResourceSetImpl implements ExtendedResourceSet {

	/**
	 * A {@link Resource resource} filter for determining the subsets of resources that are to be considered for
	 * resolving fragment-based {@link URI}s.
	 */
	protected interface ResourceFilter {

		boolean accept(Resource resource);
	}

	/**
	 * An enhanced {@link ResourcesEList} implementation that exposes a {@link #getModCount()} method through which
	 * clients figure out the number of times this list has been <i>structurally modified</i> and updates
	 * {@link ResourceSetImpl#getURIResourceMap()} when {@link Resource}s are added of removed.
	 *
	 * @see AbstractList#modCount
	 */
	protected class ExtendedResourcesEList<E extends Object & Resource> extends ResourcesEList<E> {

		private static final long serialVersionUID = 1L;

		protected int getModCount() {
			return modCount;
		}

		/*
		 * Overridden to force eager initialization of URI to resource cache as soon as new resources get created and
		 * added to the resource set.
		 * @see org.eclipse.emf.common.util.AbstractEList#didAdd(int, java.lang.Object)
		 */
		@Override
		protected void didAdd(int index, E newObject) {
			super.didAdd(index, newObject);

			Map<URI, Resource> map = getURIResourceMap();
			if (map != null) {
				URI uri = newObject.getURI();
				map.put(uri, newObject);

				URIConverter uriConverter = getURIConverter();
				URI normalizedURI = uriConverter.normalize(uri);
				map.put(normalizedURI, newObject);
			}
		};

		/*
		 * Overridden to force update of URI to resource cache as soon as resources get removed from the resource set.
		 * @see org.eclipse.emf.common.util.AbstractEList#didRemove(int, java.lang.Object)
		 */
		@Override
		protected void didRemove(int index, E oldObject) {
			Map<URI, Resource> map = getURIResourceMap();
			if (map != null) {
				URI uri = oldObject.getURI();
				map.remove(uri);

				URIConverter uriConverter = getURIConverter();
				URI normalizedURI = uriConverter.normalize(uri);
				map.remove(normalizedURI);
			}

			super.didRemove(index, oldObject);
		};

		/*
		 * @see org.eclipse.emf.common.util.AbstractEList#didSet(int, java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void didSet(int index, E newObject, E oldObject) {
			Map<URI, Resource> map = getURIResourceMap();
			if (newObject != null) {
				super.didSet(index, newObject, oldObject);

				if (map != null) {
					URI uri = newObject.getURI();
					map.put(uri, newObject);

					URIConverter uriConverter = getURIConverter();
					URI normalizedURI = uriConverter.normalize(uri);
					map.put(normalizedURI, newObject);
				}
			}
			if (oldObject != null) {
				if (map != null) {
					URI uri = oldObject.getURI();
					map.remove(uri);

					URIConverter uriConverter = getURIConverter();
					URI normalizedURI = uriConverter.normalize(uri);
					map.remove(normalizedURI);
				}

				super.didSet(index, newObject, oldObject);
			}
		};

		/*
		 * @see org.eclipse.emf.common.util.AbstractEList#didClear(int, java.lang.Object[])
		 */
		@Override
		protected void didClear(int size, Object[] oldObjects) {
			Map<URI, Resource> map = getURIResourceMap();
			if (map != null) {
				map.clear();
			}

			super.didClear(size, oldObjects);
		}
	}

	final protected ContextAwareProxyURIHelper contextAwareProxyURIHelper;

	final protected ProxyHelper proxyHelper;

	public ExtendedResourceSetImpl() {
		uriResourceMap = new WeakHashMap<URI, Resource>();

		// Initialize helper for creating context-aware URIs
		contextAwareProxyURIHelper = createContextAwareURIHelper();

		// Initialize proxy helper for resolving proxies in a performance-optimized way
		proxyHelper = createProxyHelper();
	}

	protected ContextAwareProxyURIHelper createContextAwareURIHelper() {
		return new ContextAwareProxyURIHelper();
	}

	protected ProxyHelper createProxyHelper() {
		return ProxyHelperAdapterFactory.INSTANCE.adapt(this);
	}

	/*
	 * Overridden to remove attempt to find uncached resources by normalizing their URIs and comparing them to the URIs
	 * of all existing resources in the ResourceSet. As we always have the URI to resource cache in place this is no
	 * longer necessary but would decrease runtime performance when new resources are loaded into resource sets which
	 * already contain a big number of files.
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#getResource(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public Resource getResource(URI uri, boolean loadOnDemand) {
		Assert.isNotNull(uri);

		// Be sure that given URI is a pure resource URI but not a object or proxy URI
		/*
		 * !! Important Note !! This is required to avoid the same resource to be loaded multiple times with different
		 * URIs.
		 */
		uri = trimProxyContextInfo(uri.trimFragment());

		if (resourceLocator != null) {
			return resourceLocator.getResource(uri, loadOnDemand);
		}

		Map<URI, Resource> map = getURIResourceMap();
		if (map != null) {
			Resource resource = map.get(uri);
			if (resource != null) {
				if (loadOnDemand && !resource.isLoaded()) {
					demandLoadHelper(resource);
				}
				return resource;
			}

			URIConverter uriConverter = getURIConverter();
			URI normalizedURI = uriConverter.normalize(uri);
			resource = map.get(normalizedURI);
			if (resource != null) {
				map.put(uri, resource);
				if (loadOnDemand && !resource.isLoaded()) {
					demandLoadHelper(resource);
				}
				return resource;
			}
		}

		Resource delegatedResource = delegatedGetResource(uri, loadOnDemand);
		if (delegatedResource != null) {
			if (map != null) {
				map.put(uri, delegatedResource);

				URIConverter uriConverter = getURIConverter();
				URI normalizedURI = uriConverter.normalize(uri);
				map.put(normalizedURI, delegatedResource);
			}
			return delegatedResource;
		}

		if (loadOnDemand) {
			Resource resource = demandCreateResource(uri);
			if (resource == null) {
				throw new RuntimeException("Cannot create a resource for '" + uri + "'; a registered resource factory is needed"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			demandLoadHelper(resource);

			/*
			 * !! Important note !! No need to add loaded resource to the URI to resource cache here - thanks to the
			 * ExtendedResourcesEList implementation, this has already been achieved under the hoods while the resource
			 * has been demand created and added to the resource set (see ResourceSetImpl.createResource(URI, String)
			 * and ExtendedResourceSetImpl.ExtendedResourcesEList.didAdd(int, E) for details).
			 */
			return resource;
		}

		return null;
	}

	/*
	 * Overridden for retrieving content type id behind given URI and making sure that the resource factory associated
	 * with that content type id is used for resource creation
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#demandCreateResource(org.eclipse.emf.common.util.URI)
	 */
	@Override
	protected Resource demandCreateResource(URI uri) {
		String contentTypeId = EcoreResourceUtil.getContentTypeId(uri);
		return createResource(uri, contentTypeId);
	}

	/*
	 * Overridden to make sure that errors and warnings encountered during resource creation remain available after the
	 * resource has been loaded. They are normally automatically cleared when loading begins (see
	 * org.eclipse.emf.ecore.resource.impl.ResourceImpl.load(InputStream, Map<?, ?>) for details)
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#demandLoad(org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	protected void demandLoad(Resource resource) throws IOException {
		// Capture errors and warnings encountered during resource creation
		List<Diagnostic> creationErrors = new ArrayList<Diagnostic>(resource.getErrors());
		List<Diagnostic> creationWarnings = new ArrayList<Diagnostic>(resource.getWarnings());

		// Load resource
		super.demandLoad(resource);

		// Restore creation time errors and warnings
		resource.getErrors().addAll(creationErrors);
		resource.getWarnings().addAll(creationWarnings);
	}

	/*
	 * Overridden to install ExtendedResourcesEList
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#getResources()
	 */
	@Override
	public EList<Resource> getResources() {
		if (resources == null) {
			resources = new ExtendedResourcesEList<Resource>();
		}
		return resources;
	}

	/**
	 * @return The number of times this {@link ResourceSet} has been <i>structurally modified</i>.
	 * @see AbstractList#modCount
	 */
	public int getModCount() {
		return ((ExtendedResourcesEList<Resource>) getResources()).getModCount();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.resource.ExtendedResourceSet#augmentToContextAwareProxy(org.eclipse.emf.ecore.EObject,
	 * org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	public void augmentToContextAwareProxy(EObject proxy, Resource contextResource) {
		contextAwareProxyURIHelper.augmentToContextAwareProxy(proxy, contextResource);
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ExtendedResourceSet#trimProxyContextInfo(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public URI trimProxyContextInfo(URI proxyURI) {
		return contextAwareProxyURIHelper.trimProxyContextInfo(proxyURI);
	}

	/*
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#getEObject(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public EObject getEObject(URI uri, boolean loadOnDemand) {
		if (uri == null) {
			return null;
		}

		// Retrieve context information from given URI
		String targetMMDescriptorId = contextAwareProxyURIHelper.getTargetMetaModelDescriptorId(uri);
		IMetaModelDescriptor targetMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(targetMMDescriptorId);
		URI contextURI = contextAwareProxyURIHelper.getContextURI(uri);

		// Try to retrieve an IProxyResolverService for the given meta-model descriptor
		IProxyResolverService proxyResolverService = getProxyResolverService(targetMMDescriptor);
		if (proxyResolverService != null) {
			// FIXME Make sure that cross-resource set proxy resolving capability doesn't get lost when using the proxy
			// resolver service
			return proxyResolverService.getEObject(uri, this, contextURI, loadOnDemand);
		}

		if (proxyHelper != null) {
			// If proxy URI references a known unresolved proxy then don't try to resolve it again
			if (proxyHelper.getBlackList().existsProxyURI(uri)) {
				return null;
			}

			// Fragment-based proxy?
			if (uri.segmentCount() == 0) {
				// If lookup-based proxy resolution is possible then go ahead and try to do so
				if (proxyHelper.getLookupResolver().isAvailable()) {
					EObject resolvedEObject = proxyHelper.getLookupResolver().get(uri);
					if (resolvedEObject != null) {
						return resolvedEObject;
					}
				}

				// If resolution of fragment-based proxies is currently disabled then don't resolve proxy just leave it
				// as is
				if (proxyHelper.isIgnoreFragmentBasedProxies()) {
					return null;
				}
			}
		}

		// Try to resolve proxy URI in this resource set
		EObject resolvedEObject = getEObject(uri, targetMMDescriptor, contextURI, loadOnDemand);
		if (resolvedEObject != null) {
			return resolvedEObject;
		}

		// Retrieve resource set for metamodel of object being referenced by given proxy URI
		ResourceSet otherResourceSet = getDelegateResourceSet(targetMMDescriptor, contextURI);
		if (otherResourceSet != null && otherResourceSet != this) {
			// Load target model(s) on demand if required
			if (loadOnDemand) {
				loadModels(targetMMDescriptor, contextURI);
			}

			// Try to resolve proxy URI in other resource set
			return otherResourceSet.getEObject(uri, true);
		}

		if (proxyHelper != null) {
			// Remember proxy as known unresolved proxy
			proxyHelper.getBlackList().addProxyURI(uri);
		}

		return null;
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ExtendedResourceSet#getEObject(org.eclipse.emf.ecore.EObject,
	 * org.eclipse.emf.ecore.EObject, boolean)
	 */
	@Override
	public EObject getEObject(EObject proxy, EObject contextObject, boolean loadOnDemand) {
		if (proxy == null) {
			return null;
		}

		// Retrieve context information from provided arguments
		IMetaModelDescriptor targetMMDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(proxy);

		// Try to retrieve an IProxyResolverService for the given meta-model descriptor
		IProxyResolverService proxyResolverService = getProxyResolverService(targetMMDescriptor);
		if (proxyResolverService != null) {
			// FIXME Make sure that cross-resource set proxy resolving capability doesn't get lost when using the proxy
			// resolver service
			return proxyResolverService.getEObject(proxy, contextObject, loadOnDemand);
		}

		URI uri = ((InternalEObject) proxy).eProxyURI();
		if (proxyHelper != null) {
			// If proxy URI references a known unresolved proxy then don't try to resolve it again
			if (proxyHelper.getBlackList().existsProxyURI(uri)) {
				return null;
			}

			// Fragment-based proxy?
			if (uri.segmentCount() == 0) {
				// If lookup-based proxy resolution is possible then go ahead and try to do so
				if (proxyHelper.getLookupResolver().isAvailable()) {
					EObject resolvedEObject = proxyHelper.getLookupResolver().get(uri);
					if (resolvedEObject != null) {
						return resolvedEObject;
					}
				}

				// If resolution of fragment-based proxies is currently disabled then don't resolve proxy just leave it
				// as is
				if (proxyHelper.isIgnoreFragmentBasedProxies()) {
					return null;
				}
			}
		}

		// Try to resolve proxy URI in this resource set
		EObject resolvedEObject = getEObject(uri, targetMMDescriptor, contextObject, loadOnDemand);
		if (resolvedEObject != null) {
			return resolvedEObject;
		}

		// Retrieve resource set for metamodel of object being referenced by given proxy URI
		ResourceSet otherResourceSet = getDelegateResourceSet(targetMMDescriptor, contextObject);
		if (otherResourceSet != null && otherResourceSet != this) {
			// Load target model(s) on demand if required
			if (loadOnDemand) {
				loadModels(targetMMDescriptor, contextObject);
			}

			// Try to resolve proxy URI in other resource set
			if (otherResourceSet instanceof ExtendedResourceSet) {
				ExtendedResourceSet extendedOtherResourceSet = (ExtendedResourceSet) otherResourceSet;
				return extendedOtherResourceSet.getEObject(proxy, contextObject, true);
			} else {
				return otherResourceSet.getEObject(((InternalEObject) proxy).eProxyURI(), true);
			}
		}

		if (proxyHelper != null) {
			// Remember proxy as known unresolved proxy
			proxyHelper.getBlackList().addProxyURI(uri);
		}

		return null;
	}

	/**
	 * Retrieves the {@linkplain EObject object} from specified target {@link IMetaModelDescriptor metamodel} that
	 * corresponds to given {@linkplain URI}. Uses provided {@linkResource context resource} to limit the search scope
	 * to the subset of {@link Resource resource}s that are in the same {@link IResourceScope scope} as the resource.
	 *
	 * @param uri
	 *            The {@linkplain URI} to be resolved.
	 * @param targetMetaModelDescriptor
	 *            The {@link IMetaModelDescriptor meta model descriptor} of the object that is referenced by given URI.
	 * @param contextObject
	 *            The context resource that is used to limit the search scope.
	 * @param loadOnDemand
	 *            Whether to load the resource or model containing the object that is referenced by given URI if it is
	 *            not already loaded.
	 * @return The object that corresponds to given URI or <code>null</code> if given URI cannot be resolved.
	 */
	protected EObject getEObject(URI uri, IMetaModelDescriptor targetMetaModelDescriptor, Object contextObject, boolean loadOnDemand) {
		Assert.isNotNull(uri);

		// Fragment-based URI not knowing its target resource?
		if (uri.segmentCount() == 0) {
			// Search for object behind given URI within relevant set of potential target resources
			List<Resource> resources = getResourcesToSearchIn(getResources(), uri, targetMetaModelDescriptor);
			return safeFindEObjectInResources(resources, uri, loadOnDemand);
		} else {
			// Target resource is known, so search for object behind given URI only in that resource
			Resource resource = safeGetResource(uri, loadOnDemand);
			if (resource != null) {
				return safeGetEObjectFromResource(resource, uri.fragment());
			}
			return null;
		}
	}

	/**
	 * Determines the subset of given set of {@link Resource resource}s that are to be considered for resolving given
	 * {@link URI}. Only called when given URI is fragment-based (i.e., has no segments and doesn't reference any
	 * explicit target resource).
	 * <p>
	 * This implementation applies a {@link ResourceFilter resource filter} to the provided set of {@link Resource
	 * resource}s retaining only those {@link Resource resource}s that match specified target
	 * {@link IMetaModelDescriptor metamodel descriptor} behind given {@link URI}.
	 *
	 * @param allResources
	 *            The set of {@link Resource resource}s from which the resources to be considered for resolving given
	 *            fragment-based {@link URI} are to be extracted.
	 * @param uri
	 *            The fragment-based {@link URI} to resolve.
	 * @param targetMetaModelDescriptor
	 *            The {@link IMetaModelDescriptor metamodel descriptor} of the object that given fragment-based
	 *            {@link URI} is supposed to resolve to.
	 * @return The set of {@link Resource resource}s to be considered for resolving given fragment-based {@link URI}.
	 * @see #getResources()
	 */
	protected List<Resource> getResourcesToSearchIn(List<Resource> allResources, URI uri, final IMetaModelDescriptor targetMetaModelDescriptor) {
		Assert.isNotNull(allResources);

		if (targetMetaModelDescriptor != null) {
			return getFilteredResources(allResources, new ResourceFilter() {
				@Override
				public boolean accept(Resource resource) {
					// Accept only resources that match provided metamodel descriptor
					if (targetMetaModelDescriptor.equals(MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource))) {
						return true;
					}
					return false;
				}
			});
		}

		return allResources;
	}

	/**
	 * Returns the subset of given set of {@link Resource resource}s that make through provided {@link ResourceFilter
	 * resource filter}.
	 *
	 * @param allResources
	 *            The set of {@link Resource resource}s to be filtered.
	 * @param filter
	 *            The {@IResourceFilter filter} to be applied.
	 * @return The subset of given set of {@link Resource resource}s that make through provided {@link ResourceFilter
	 *         resource filter} or an empty list of no matching resources are found.
	 */
	protected List<Resource> getFilteredResources(List<Resource> allResources, ResourceFilter resourceFilter) {
		Assert.isNotNull(allResources);
		Assert.isNotNull(resourceFilter);

		List<Resource> filteredResources = new ArrayList<Resource>();
		for (Resource resource : allResources) {
			if (resourceFilter.accept(resource)) {
				filteredResources.add(resource);
			}
		}
		return filteredResources;
	}

	/**
	 * Resolves given {@link URI} against given set of {@link Resource resource}s.Only called when given URI is
	 * fragment-based (i.e., has no segments and doesn't reference any explicit target resource).
	 *
	 * @param resources
	 *            The set of {@link Resource resource}s to resolve given fragment-based {@link URI} against.
	 * @param uri
	 *            The fragment-based {@link URI} to resolve.
	 * @param loadOnDemand
	 *            Whether to create and load the {@link Resource target resource}, if it isn't already present in this
	 *            {@link ExtendedResourceSetImpl resource set}.
	 * @return The {@link EObject} behind given fragment-based {@link URI}, or <code>null</code> if given {@link URI}
	 *         cannot be resolved.
	 */
	protected EObject safeFindEObjectInResources(List<Resource> resources, URI uri, boolean loadOnDemand) {
		if (uri == null) {
			return null;
		}

		Set<Resource> resourcesWithProblems = new HashSet<Resource>();
		// Target element may be in any of the specified resources
		EObject resolvedEObject = null;
		for (Resource resource : resources) {
			try {
				EObject eObject = resource.getEObject(uri.fragment());
				if (eObject != null) {
					resolvedEObject = eObject;
					break;
				}
			} catch (Exception ex) {
				resource.getErrors().add(new ProxyURIIntegrityException(NLS.bind(Messages.error_problemOccurredWhenResolvingProxyURI, uri), ex));
				resourcesWithProblems.add(resource);
			}
		}

		// Handle problems that may have been encountered during proxy resolution
		ResourceProblemMarkerService.INSTANCE.addProblemMarkers(resourcesWithProblems, null);

		return resolvedEObject;
	}

	/**
	 * Get {@link Resource resource} of given {@link URI}.
	 *
	 * @param uri
	 *            The {@link URI} to resolve.
	 * @param loadOnDemand
	 *            Whether to create and load the {@link Resource target resource}, if it isn't already present in this
	 *            {@link ExtendedResourceSetImpl resource set}.
	 */
	protected Resource safeGetResource(URI uri, boolean loadOnDemand) {
		Assert.isNotNull(uri);

		// Just get resource behind URI if it is already loaded
		Resource resource = getResource(uri, false);

		// Load it if not done so yet and a demand load has been requested
		if ((resource == null || !resource.isLoaded()) && loadOnDemand) {
			try {
				resource = getResource(uri, true);
			} catch (Exception ex) {
				try {
					// Check if some resource has been created for given URI and added to the resource set
					/*
					 * !! Important Note !! Don't rely on resource returned by previous call to
					 * ResourceSet#getResource() but try to retrieve it again. This is because ResourceSet#getResource()
					 * may fail by throwing an exception at a point where the resource for given URI has already been
					 * created and added to the resource set.
					 */
					resource = getResource(uri, false);
					if (resource != null) {
						// Make sure that no empty resources are kept in resource set
						if (resource.getContents().isEmpty()) {
							EcoreResourceUtil.unloadResource(resource, true);
						}

						// Ignore exceptions complaining about that resource does not exist - this actually no exception
						// but a valid situation during proxy resolution
						if (EcoreResourceUtil.exists(resource.getURI())) {
							// Leave an error about what has happened on resource
							Throwable cause = ex.getCause();
							if (cause instanceof XMIException) {
								resource.getErrors().add((XMIException) cause);
							} else {
								Exception causeEx = cause instanceof Exception ? (Exception) cause : null;
								resource.getErrors()
										.add(new XMIException(
												NLS.bind(Messages.error_problemOccurredWhenLoadingResource, resource.getURI().toString()), causeEx,
												resource.getURI().toString(), 1, 1));
							}
						}
					} else {
						// Leave a trace about what has happened in error log
						PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
					}
				} catch (Exception e) {
					// Log original exception in error log if something goes wrong
					PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				}
			}
		}

		return resource;
	}

	/**
	 * Resolves given {@link URI} against given {@link Resource resource}.
	 *
	 * @param resource
	 *            The {@link Resource resource} to resolve given fragment-based {@link URI} against.
	 * @param uriFragment
	 *            The fragment-based {@link URI} to resolve.
	 * @return The {@link EObject} behind given fragment-based {@link URI}, or <code>null</code> if given {@link URI}
	 *         cannot be resolved.
	 */
	protected EObject safeGetEObjectFromResource(Resource resource, String uriFragment) {
		Assert.isNotNull(resource);

		if (resource.isLoaded() && uriFragment != null) {
			try {
				return resource.getEObject(uriFragment);
			} catch (Exception ex) {
				// Leave an error about what has happened on resource
				resource.getErrors()
						.add(new ProxyURIIntegrityException(NLS.bind(Messages.error_problemOccurredWhenResolvingProxyURI, uriFragment), ex));
			}

			// Handle problems that may have been encountered during proxy resolution
			ResourceProblemMarkerService.INSTANCE.addProblemMarkers(resource, null);
		}

		return null;
	}

	/**
	 * Retrieves a delegate {@link ResourceSet resource set} corresponding to the provided {@link IMetaModelDescriptor
	 * metamodel descriptor} and context {@link Object object}.
	 */
	protected ResourceSet getDelegateResourceSet(IMetaModelDescriptor metaModelDescriptor, Object contextObject) {
		IContainer contextContainer = getContextContainer(contextObject);
		TransactionalEditingDomain otherEditingDomain = WorkspaceEditingDomainUtil.getEditingDomain(contextContainer, metaModelDescriptor);
		if (otherEditingDomain != null) {
			return otherEditingDomain.getResourceSet();
		}

		return null;
	}

	/**
	 * Loads all models of given {@link IMetaModelDescriptor metamodel} within provided {@link Object context}.
	 *
	 * @param metaModelDescriptor
	 *            The {@link IMetaModelDescriptor metamodel descriptor} of the models to be loaded.
	 * @param contextObject
	 *            The {@link Object object} that identifies the context within which the models to be loaded are
	 *            located.
	 */
	protected void loadModels(IMetaModelDescriptor metaModelDescriptor, Object contextObject) {
		// Retrieve target model(s) that is (are) in the same scope as context object
		IContainer contextContainer = getContextContainer(contextObject);
		if (contextContainer != null) {
			Collection<IModelDescriptor> targetModelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels(contextContainer, metaModelDescriptor);

			// Ignore target models that are already loaded
			for (Iterator<IModelDescriptor> iter = targetModelDescriptors.iterator(); iter.hasNext();) {
				if (EcorePlatformUtil.isModelLoaded(iter.next())) {
					iter.remove();
				}
			}

			// Trigger asynchronous loading of all target models that are not loaded yet
			EcorePlatformUtil.loadModels(targetModelDescriptors, false, null);
		}
	}

	/**
	 * Get the container of the provided {@link Object context object}. If the context object is a project or container
	 * URI, the project or container is returned. The parent of the file that owns the given context object is returned,
	 * if the context object is a EObject. Otherwise null container is returned.
	 */
	protected IContainer getContextContainer(Object contextObject) {
		if (contextObject instanceof URI) {
			URI contextURI = (URI) contextObject;
			if (contextURI.isPlatformResource()) {
				IPath contextPath = new Path(contextURI.toPlatformString(true));
				IResource contextResource = ResourcesPlugin.getWorkspace().getRoot().findMember(contextPath);
				if (contextResource != null) {
					if (contextResource instanceof IContainer) {
						return (IContainer) contextResource;
					} else {
						return contextResource.getParent();
					}
				}
			}
		} else if (contextObject instanceof EObject) {
			IFile contextFile = EcorePlatformUtil.getFile((EObject) contextObject);
			if (contextFile != null) {
				return contextFile.getParent();
			}
		}
		return null;
	}

	protected IProxyResolverService getProxyResolverService(IMetaModelDescriptor descriptor) {
		if (descriptor != null) {
			return new DefaultMetaModelServiceProvider().getService(descriptor, IProxyResolverService.class);
		}
		return null;
	}
}
