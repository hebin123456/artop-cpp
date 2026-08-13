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
package org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Indexing service used for blacklisting unresolvable proxies. Used to avoid repeated attempts to resolve such proxies
 * which would useless but potentially very expensive.
 * 
 * @deprecated Will be removed as soon as a full-fledged model indexing service is in place and can be used to overcome
 *             performance bottlenecks due to proxy resolution.
 */
@Deprecated
public class ModelIndex {

	private MapModelIndex mapModelIndex;
	private boolean enabled;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ModelIndex() {
		mapModelIndex = new MapModelIndex();
		enabled = true;
	}

	public void clearAll() {
		if (enabled) {
			mapModelIndex.clearAll();
		}
	}

	public void dispose() {
		mapModelIndex.dispose();
	}

	public boolean exists(URI fragmentUri) {
		if (enabled) {
			return mapModelIndex.exists(fragmentUri);
		}
		return false;
	}

	public boolean existsProxyURI(URI proxyURI) {
		if (enabled) {
			return mapModelIndex.existsProxyURI(proxyURI);
		}
		return false;
	}

	public boolean addProxyURI(URI proxyURI) {
		if (enabled) {
			return mapModelIndex.addProxyURI(proxyURI);
		}
		return false;
	}

	public boolean removeProxyURI(URI proxyURI) {
		if (enabled) {
			return mapModelIndex.removeProxyURI(proxyURI);
		}
		return false;
	}

	public void updateIndexOnResourceLoaded(Resource notifier) {
		if (enabled) {
			mapModelIndex.updateIndexOnResourceLoaded(notifier);
		}
	}

	public void updateIndexOnResourceUnloaded(Resource notifier) {
		if (enabled) {
			mapModelIndex.updateIndexOnResourceUnloaded(notifier);
		}
	}

	public Collection<URI> findInstances(EClass eClass, IProject scope) {
		if (enabled) {
			return mapModelIndex.findInstances(eClass, scope);
		}
		return Collections.emptySet();
	}

	public Collection<URI> findReferencesTo(EObject object, IProject scope) {
		if (enabled) {
			return mapModelIndex.findReferencesTo(object, scope);
		}
		return Collections.emptySet();
	}

	public void reIndex() throws Exception {
		if (enabled) {
			mapModelIndex.reIndex();
		}
	}
}
