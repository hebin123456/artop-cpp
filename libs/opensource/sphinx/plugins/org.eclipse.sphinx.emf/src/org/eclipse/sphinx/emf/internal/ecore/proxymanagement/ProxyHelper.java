/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.ecore.proxymanagement;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist.ModelIndex;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.lookupresolver.EcoreIndex;

/**
 * A set of {@link ResourceSet} level services which can be used to optimize proxy resolution for performance.
 * 
 * @deprecated Will be removed as soon as a full-fledged model indexing service is in place and can be used to overcome
 *             performance bottlenecks due to proxy resolution.
 */
@Deprecated
public interface ProxyHelper {

	boolean isIgnoreFragmentBasedProxies();

	void setIgnoreFragmentBasedProxies(boolean ignoreFragmentBasedProxies);

	EcoreIndex getLookupResolver();

	ModelIndex getBlackList();
}
