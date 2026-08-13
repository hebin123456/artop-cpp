/**
 * <copyright>
 *
 * Copyright (c) 2014-2017 itemis, IncQuery Labs and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *     IncQuery Labs, itemis - [501899] Use base index instead of IncQuery patterns
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.viatra.query.proxymanagement;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.resource.ScopingResourceSet;
import org.eclipse.sphinx.emf.viatra.query.IViatraQueryEngineHelper;
import org.eclipse.sphinx.emf.viatra.query.proxymanagment.AbstractViatraQueryProxyResolver;
import org.eclipse.sphinx.emf.workspace.viatra.query.WorkspaceViatraQueryEngineHelper;

public abstract class AbstractScopingViatraQueryProxyResolver extends AbstractViatraQueryProxyResolver {

	@Override
	protected IViatraQueryEngineHelper createViatraQueryEngineHelper() {
		// TODO Add scoping of matches returned by Viatra/Query also to model query and search capabilities
		return new WorkspaceViatraQueryEngineHelper();
	}

	protected boolean isResourceInScope(Resource resource, Object contextObject) {
		ResourceSet resourceSet = resource.getResourceSet();
		if (resourceSet instanceof ScopingResourceSet) {
			return ((ScopingResourceSet) resourceSet).isResourceInScope(resource, contextObject);
		}
		return true;
	}

	@Override
	protected boolean matchesEObjectCandidate(URI uri, Object contextObject, EObject candidate) {
		if (contextObject != null) {
			if (isResourceInScope(candidate.eResource(), contextObject)) {
				return matchesEObjectCandidate(uri, candidate);
			}
			return false;
		} else {
			return matchesEObjectCandidate(uri, candidate);
		}
	}
}
