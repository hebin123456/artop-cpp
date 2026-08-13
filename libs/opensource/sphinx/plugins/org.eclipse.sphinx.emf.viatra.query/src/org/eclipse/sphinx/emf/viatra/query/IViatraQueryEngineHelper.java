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
import org.eclipse.viatra.query.runtime.api.ViatraQueryEngine;
import org.eclipse.viatra.query.runtime.exception.ViatraQueryException;

public interface IViatraQueryEngineHelper {

	ViatraQueryEngine getEngine(EObject contextObject) throws ViatraQueryException;

	ViatraQueryEngine getEngine(Resource contextResource) throws ViatraQueryException;

	ViatraQueryEngine getEngine(Resource resource, boolean strict) throws ViatraQueryException;

	ViatraQueryEngine getEngine(ResourceSet resourceSet) throws ViatraQueryException;
}
