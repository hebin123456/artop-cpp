/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [441970] Result returned by ExtendedResourceAdapter#getHREF(EObject) must default to complete object URI (edit)
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *     itemis - [460260] Expanded paths are collapsed on resource reload
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapter;

/**
 * {@link Adapter}-based implementation of {@link ExtendedResource} for Hummingbird.
 */
public class ExtendedHummingbirdResourceAdapter extends ExtendedResourceAdapter {

	/**
	 * Arbitrary scheme for Hummingbird URIs enabling to reference Hummingbird elements without having to precise the
	 * files in which they are located.
	 */
	public static final String HB_SCHEME = "hb"; //$NON-NLS-1$

	/**
	 * Creates a fragment-based Hummingbird 2.0 {@link URI} from given {@link URI#fragment() URI fragment}.
	 *
	 * @param uri
	 *            The URI fragment to be processed.
	 * @return The resulting fragment-based Hummingbird 2.0 {@link URI}.
	 */
	protected URI createHummingbirdURI(String uriFragment) {
		return URI.createURI(HB_SCHEME + URI_SCHEME_SEPARATOR + URI_SEGMENT_SEPARATOR + URI_FRAGMENT_SEPARATOR + uriFragment, true);
	}

	@Override
	protected URI getURI(URI resourceURI, String eObjectURIFragment, boolean resolve) {
		if (!resolve) {
			// Return fragment-based Hummingbird 2.0 URI
			return createHummingbirdURI(eObjectURIFragment);
		}

		// Return regular full URI
		return getURI(resourceURI, eObjectURIFragment);
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ExtendedResourceAdapter#createURI(java.lang.String,
	 * org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public URI createURI(String uriLiteral, EClass eClass) {
		// Is given URI literal a fragment-only URI string?
		if (uriLiteral.startsWith(URI_FRAGMENT_SEPARATOR)) {
			// Create and return corresponding fragment-based Hummingbird 2.0 URI
			return createHummingbirdURI(uriLiteral.substring(1));
		}

		// Backward compatibility: Is given URI literal a fragment-only URI string without leading fragment separator?
		if (!uriLiteral.contains(URI_FRAGMENT_SEPARATOR)) {
			// Create and return corresponding fragment-based Hummingbird 2.0 URI
			return createHummingbirdURI(uriLiteral);
		}

		return super.createURI(uriLiteral, eClass);
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ExtendedResourceAdapter#getHREF(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public URI getHREF(EObject eObject) {
		// Return a fragment-only URI of given object as HREF
		URI uri = getURI(eObject);
		return URI.createURI(URI_FRAGMENT_SEPARATOR + uri.fragment());
	}
}
