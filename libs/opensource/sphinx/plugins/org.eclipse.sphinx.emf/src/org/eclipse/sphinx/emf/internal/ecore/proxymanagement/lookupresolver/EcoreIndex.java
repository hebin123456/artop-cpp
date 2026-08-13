/**
 * <copyright>
 *
 * Copyright (c) 2008-2016 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [485407] Enable eager post-load proxy resolution to support manifold URI fragments referring to the same object
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.ecore.proxymanagement.lookupresolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

/**
 * Simple implementation of Ecore Index (associating fragmend-based URIs with EObjects). Used at the end of model
 * loading operations in order to optimize proxy resolution for performance.
 *
 * @deprecated Will be removed as soon as a full-fledged model indexing service is in place and can be used to overcome
 *             performance bottlenecks due to proxy resolution.
 */
@Deprecated
public final class EcoreIndex {

	// TODO Precise the resource for which we have to maintain this map.
	private Map<String, EObject> index = null;

	public void init(Collection<Resource> resources) {
		if (index == null) {
			index = new HashMap<String, EObject>();
		}
		for (Resource resource : resources) {
			for (Iterator<EObject> i = resource.getAllContents(); i.hasNext();) {
				EObject eObject = i.next();

				// Index both the regular as well as the normalized URI fragment
				String fragment = resource.getURIFragment(eObject);
				index.put(fragment, eObject);
				String normalizedFragment = EcoreResourceUtil.normalizeURIFragment(resource, fragment);
				index.put(normalizedFragment, eObject);
			}
		}
	}

	/**
	 * @return <code>true</code> if index is available; <code>false</code> otherwise.
	 */
	public boolean isAvailable() {
		return index != null;
	}

	public EObject get(URI uri) {
		Assert.isNotNull(uri);
		Assert.isLegal(index != null);

		return index.get(uri.fragment());
	}

	public void clear() {
		Assert.isLegal(index != null);

		index.clear();
		index = null;
	}
}
