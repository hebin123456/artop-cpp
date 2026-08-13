/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [475954] Proxies with fragment-based proxy URIs may get resolved across model boundaries
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore.proxymanagement;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.metamodel.services.IMetaModelService;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSet;

public interface IProxyResolverService extends IMetaModelService {

	EObject getEObject(EObject proxy, EObject contextObject, boolean loadOnDemand);

	EObject getEObject(URI uri, ExtendedResourceSet contextResourceSet, Object contextObject, boolean loadOnDemand);
}
