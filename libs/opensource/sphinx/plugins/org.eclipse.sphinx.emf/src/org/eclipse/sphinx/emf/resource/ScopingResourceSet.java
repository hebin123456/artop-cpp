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
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.scoping.IResourceScope;

/**
 * A scoping {@linkplain ResourceSet resource set} that uses {@linkplain IResourceScope scopes}
 */
public interface ScopingResourceSet extends ExtendedResourceSet {
	/**
	 * Retrieves all {@link Resource resource}s in this {@link ResourceSet resourceset} owned by the model containing
	 * the context object and its referenced models.
	 *
	 * @param contextObject
	 *            A context object for which a list of resources should be retrieved.
	 * @return A list of resources for the given context object.
	 */
	List<Resource> getResourcesInModel(Object contextObject);

	/**
	 * Retrieves all {@link Resource resource}s in this {@link ResourceSet resourceset} owned by the model containing
	 * the context object.
	 *
	 * @param contextObject
	 *            A context object for which a list of resources should be retrieved.
	 * @param includeReferencedScopes
	 *            A boolean value that let to decide if resources must be retrieved under depending roots of the
	 *            concerned model
	 * @return A list of resources for the given context object.
	 */
	List<Resource> getResourcesInModel(Object contextObject, boolean includeReferencedScopes);

	/**
	 * Retrieves all {@link Resource resource}s in this {@link ResourceSet resourceset} owned by the
	 * {@link IResourceScope resource scope} containing the context object and its referenced {@link IResourceScope
	 * resource scope}s.
	 *
	 * @param contextObject
	 *            A context object for which a list of resources should be retrieved.
	 * @return
	 */
	List<Resource> getResourcesInScope(Object contextObject);

	/**
	 * Retrieves all {@link Resource resource}s in this {@link ResourceSet resourceset} owned by the
	 * {@link IResourceScope resource scope} containing the context object.
	 *
	 * @param contextObject
	 *            A context object for which a list of resources should be retrieved.
	 * @param includeReferencedScopes
	 *            A boolean value that let to decide if resources must be retrieved under depending roots of the
	 *            concerned model.
	 * @return
	 */
	List<Resource> getResourcesInScope(Object contextObject, boolean includeReferencedScopes);

	/**
	 * Tests if given {@link Resource resource} belongs to the {@link IResourceScope resource scope}(s) behind provided
	 * <code>contextObject</code>.
	 *
	 * @param resource
	 *            The resource to be investigated.
	 * @param contextObject
	 *            The context object identifying the resource scope to refer to.
	 * @return <code>true</code> if given resource belongs to the resource scope behind provided
	 *         <code>contextObject</code>, or <code>false</code> otherwise.
	 */
	boolean isResourceInScope(Resource resource, Object contextObject);

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
	 * @return <code>true</code> if given resource belongs to the resource scope behind provided
	 *         <code>contextObject</code>, or <code>false</code> otherwise.
	 */
	boolean isResourceInScope(Resource resource, Object contextObject, boolean includeReferencedScopes);
}
