/**
 * <copyright>
 *
 * Copyright (c) 2014-2017 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *     itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore.proxymanagement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.services.AbstractMetaModelService;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSet;

public abstract class AbstractProxyResolverService extends AbstractMetaModelService implements IProxyResolverService {

	private List<IProxyResolver> proxyResolvers = new ArrayList<IProxyResolver>();

	public AbstractProxyResolverService(Collection<IMetaModelDescriptor> mmDescriptors) {
		super(mmDescriptors);
		initProxyResolvers();
	}

	protected abstract void initProxyResolvers();

	protected EClass getTargetEClass(URI uri) {
		return null;
	}

	protected List<IProxyResolver> getProxyResolvers() {
		if (proxyResolvers == null) {
			proxyResolvers = new ArrayList<IProxyResolver>();
		}
		return proxyResolvers;
	}

	protected IProxyResolver getProxyResolver(EClass eType) {
		for (IProxyResolver resolver : getProxyResolvers()) {
			if (resolver.canResolve(eType)) {
				return resolver;
			}
		}
		return null;
	}

	@Override
	public EObject getEObject(EObject proxy, EObject contextObject, boolean loadOnDemand) {
		IProxyResolver proxyResolver = getProxyResolver(proxy.eClass());
		if (proxyResolver != null) {
			return proxyResolver.getEObject(proxy, contextObject, loadOnDemand);
		}
		return null;
	}

	@Override
	public EObject getEObject(URI uri, ExtendedResourceSet contextResourceSet, Object contextObject, boolean loadOnDemand) {
		EClass targetEClass = getTargetEClass(uri);
		if (targetEClass != null) {
			IProxyResolver proxyResolver = getProxyResolver(targetEClass);
			if (proxyResolver != null) {
				return proxyResolver.getEObject(uri, targetEClass, contextResourceSet, contextObject, loadOnDemand);
			}
		}
		return null;
	}
}
