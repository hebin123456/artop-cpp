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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * An service that keeps track of the former {@link Resource resource} of unloaded {@link EObject}s. Useful in cases
 * where the URI of the former resource is not part of the {@link InternalEObject#eProxyURI() proxy URI} of the unloaded
 * {@link EObject}s.
 */
public interface OldResourceProvider {

	/**
	 * Returns the {@link Resource resource} which contained the {@link EObject} behind this {@link OldResourceProvider
	 * provider} before it has been unloaded.
	 *
	 * @return The former {@link Resource resource} of the {@link EObject} behind this {@link OldResourceProvider
	 *         provider}.
	 */
	Resource getOldResource();
}
