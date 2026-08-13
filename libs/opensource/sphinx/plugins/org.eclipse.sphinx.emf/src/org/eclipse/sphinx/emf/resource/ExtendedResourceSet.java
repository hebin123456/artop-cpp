/**
 * <copyright>
 *
 * Copyright (c) 2013-2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.scoping.IResourceScope;

public interface ExtendedResourceSet extends ResourceSet {

	/**
	 * Retrieves the {@linkplain EObject object} for specified {@link EObject proxy}. Allows to use provided
	 * <code>contextObject</code> - i.e., the object referencing the proxy - to customize or optimize the way how the
	 * resolution of the proxy is done.
	 *
	 * @param proxy
	 *            The {@EObject proxy} to be resolved.
	 * @param contextObject
	 *            The {@link EObject context object} that can be used customize or optimize the way how the resolution
	 *            of the proxy is done.
	 * @param loadOnDemand
	 *            Whether to load the resource or model containing the object that is referenced by given
	 *            <code>proxy</code> if it is not already loaded.
	 * @return The object that is represented by given <code>proxy</code> or <code>null</code> if given
	 *         <code>proxy</code> cannot be resolved.
	 */
	EObject getEObject(EObject proxy, EObject contextObject, boolean loadOnDemand);

	/**
	 * Augments given {@link InternalEObject proxy} to a context-aware proxy by adding key/value pairs that contain the
	 * target {@link IMetaModelDescriptor metamodel descriptor} and a context {@link URI} to the {@link URI#query()
	 * query string} of the proxy URI. Those are required to support the resolution of proxified references between
	 * objects from different metamodels and to honor the {@link IResourceScope resource scope} of the proxy URI when it
	 * is being resolved.
	 *
	 * @param proxy
	 *            The proxy to be handled.
	 * @param contextResource
	 *            The resource that identifies the context of the proxy (typically the resource containing it).
	 * @see #trimProxyContextInfo(URI)
	 */
	void augmentToContextAwareProxy(EObject proxy, Resource contextResource);

	/**
	 * If given {@link URI proxy URI} contains proxy context-related key/value pairs on its {@link URI#query() query
	 * string}, returns the URI formed by removing those key/value pairs or removing the query string entirely in case
	 * that no other key/value pairs exist; returns given proxy URI unchanged, otherwise.
	 *
	 * @param proxyURI
	 *            The context-aware proxy URI to be handled.
	 * @return The trimmed proxy URI.
	 * @see #augmentToContextAwareProxy(EObject)
	 */
	URI trimProxyContextInfo(URI proxyURI);
}
