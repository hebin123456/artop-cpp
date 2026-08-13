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
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore.proxymanagement;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSet;

public class ProxyResolutionBehavior {

	/**
	 * Singleton instance
	 */
	public static final ProxyResolutionBehavior INSTANCE = new ProxyResolutionBehavior();

	public EObject eResolveProxy(EObject contextObject, EObject proxy) {
		if (proxy == null) {
			return null;
		}
		if (((InternalEObject) proxy).eProxyURI() == null) {
			return proxy;
		}

		Resource resource = contextObject.eResource();
		if (resource != null) {
			ResourceSet resourceSet = resource.getResourceSet();
			if (resourceSet != null) {
				return eResolveProxyInResourceSet(resourceSet, contextObject, proxy);
			}
		}

		return EcoreUtil.resolve(proxy, contextObject);
	}

	protected EObject eResolveProxyInResourceSet(ResourceSet resourceSet, EObject contextObject, EObject proxy) {
		Assert.isNotNull(resourceSet);
		Assert.isNotNull(proxy);

		EObject resolvedEObject = null;
		if (resourceSet instanceof ExtendedResourceSet) {
			ExtendedResourceSet extendedResourceSet = (ExtendedResourceSet) resourceSet;
			resolvedEObject = extendedResourceSet.getEObject(proxy, contextObject, true);
		} else {
			resolvedEObject = resourceSet.getEObject(((InternalEObject) proxy).eProxyURI(), true);
		}
		if (resolvedEObject != null) {
			return resolvedEObject;
		}

		return proxy;
	}
}
