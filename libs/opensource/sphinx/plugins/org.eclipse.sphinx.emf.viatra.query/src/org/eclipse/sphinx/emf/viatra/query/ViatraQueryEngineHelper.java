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
package org.eclipse.sphinx.emf.viatra.query;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.emf.EMFScope;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

public class ViatraQueryEngineHelper implements IViatraQueryEngineHelper {

	@Override
	public ViatraQueryEngine getEngine(EObject contextObject) throws ViatraQueryException {
		if (contextObject != null) {
			Resource contextResource = contextObject.eResource();
			if (contextResource != null) {
				return getEngine(contextResource);
			}
			EObject rootContainer = EcoreUtil.getRootContainer(contextObject);
			return ViatraQueryEngine.on(new EMFScope(rootContainer));
		}
		return null;
	}

	@Override
	public ViatraQueryEngine getEngine(Resource contextResource) throws ViatraQueryException {
		return getEngine(contextResource, false);
	}

	@Override
	public ViatraQueryEngine getEngine(Resource resource, boolean strict) throws ViatraQueryException {
		if (resource != null) {
			ResourceSet resourceSet = resource.getResourceSet();
			if (resourceSet != null && !strict) {
				return ViatraQueryEngine.on(new EMFScope(resourceSet));
			}
			return ViatraQueryEngine.on(new EMFScope(resource));
		}
		return null;
	}

	@Override
	public ViatraQueryEngine getEngine(ResourceSet resourceSet) throws ViatraQueryException {
		if (resourceSet != null) {
			return ViatraQueryEngine.on(new EMFScope(resourceSet));
		}
		return null;
	}
}
