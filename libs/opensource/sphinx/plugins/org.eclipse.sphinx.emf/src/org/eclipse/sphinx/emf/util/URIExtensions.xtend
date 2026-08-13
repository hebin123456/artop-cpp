/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.util

import org.eclipse.core.runtime.Assert
import org.eclipse.emf.common.util.URI

/**
 * Useful API extensions for {@link URI}.
 */
class URIExtensions {

	public static final String URI_SEGMENT_SEPARATOR = "/" // $NON-NLS-1$
	public static final String URI_AUTHORITY_SEPARATOR = URI_SEGMENT_SEPARATOR + URI_SEGMENT_SEPARATOR

	public static final String URI_QUERY_SEPARATOR = "?" // $NON-NLS-1$
	public static final String URI_FRAGMENT_SEPARATOR = "#" // $NON-NLS-1$

   /**
    * Indicates whether the given URI is an actual URI. An actual URI is a URI that "looks like" a URI and can be clearly distinguished from a simple string.
    * This is generally the case for all URIs that have at least one of the following components: a {@link URI#scheme() scheme}, a {@link URI#hasDevice() device},
    * an {@link URI#hasAuthority() authority}, a {@link URI#hasPath() path} with more than 1 {@link URI#segments() segment},
    * a {@link URI#hasTrailingPathSeparator() trailing separator}, a {@link URI#hasQuery() query}, or a {@link URI#hasFragment() fragment}.
    *
    * @param uri The URI to be examined.
    * @return <code>true</code> if the given URI is an actual URI, <code>false</code> otherwise.
    */
    def static boolean isActual(URI uri) {
		Assert::isNotNull(uri)
		return !uri.scheme.nullOrEmpty ||
		       uri.hasDevice ||
		       uri.hasAuthority ||
		       (uri.hasPath && uri.segmentCount > 1) ||
		       uri.hasTrailingPathSeparator ||
		       uri.hasQuery ||
		       uri.hasFragment
	}

	/**
	 * Retrieves the {@link URI#fragment() fragment} of the given {@link URI} and converted into a URI of its own.
	 *
	 * @param uri The URI whose fragment is to be returned as URI.
	 * @return The given URI's fragment as URI, or <code>null</code> if given URI has no fragment.
	 */
	def static URI getFragment(URI uri) {
		Assert::isNotNull(uri)
		if (uri.hasFragment) {
			var fragment = uri.fragment()
			// Fragment with empty root segment?
			if (fragment.startsWith(URI_SEGMENT_SEPARATOR + "" + URI_SEGMENT_SEPARATOR)) {
				// Don't use URI#createURI to make sure that empty root segment plus subsequent segment don't get interpreted as authority
				val queryIdx = fragment.indexOf(URI_QUERY_SEPARATOR)
				val path = if(queryIdx != -1) fragment.substring(0, queryIdx) else fragment
				val query = if (queryIdx != -1 && fragment.length > queryIdx + 1)
						fragment.substring(queryIdx + 1, fragment.length)
					else
						null
				return URI::createHierarchicalURI(path.split(URI_SEGMENT_SEPARATOR), query, null)
			} else {
				return URI::createURI(fragment, true)
			}
		}
		return null
	}

	/**
	 * Returns a copy of the given {@link URI} where the {@link URI#fragment() fragment} has been substituted
	 * with the string value of provided fragment URI. The returned URI will have no fragment
	 * if the provided fragment URI is <code>null</code>.
	 *
	 * @param uri The URI to be manipulated.
	 * @param fragment A URI specifying the fragment that the given URI's fragment is to be substituted with; may be <code>null</code>.
	 * @return A URI formed by replacing the fragment of the given URI with the string value of provided fragment URI
	 *         in case that the latter is not <code>null</code>; a copy of the given URI without its fragment otherwise.
	 */
	def static URI substituteFragment(URI uri, URI fragment) {
		Assert::isNotNull(uri)

		var result = uri.trimFragment
		if (fragment != null) {
			result = result.appendFragment(fragment.toString)
		}
		return result
	}

	/**
	 * Returns a copy of the given {@link URI} that has been converted into a {@link URI#isPrefix() prefix}
	 * and can be used in {@link URI#replacePrefix(URI, URI)} operations.
	 *
	 * @param uri The URI to be returned as prefix URI.
	 * @return The given URI as prefix URI.
	 * @see URI#isPrefix()
	 * @see URI#replacePrefix(URI, URI)
	 */
	def static URI asPrefix(URI uri) {
		Assert::isNotNull(uri)

		var result = uri.trimQuery
		result = result.trimFragment
		if (!result.hasTrailingPathSeparator) {
			// Append empty segment to enforce presence of trailing path separator
			result = result.appendSegment("") // $NON-NLS-1$
		}
		return result;
	}

	/**
	 * If given old last segment matches the {@link URI#lastSegment() last segment} of given {@link URI},
	 * this returns a copy of the given URI where the last segment has been replaced by the provided new last segment.
	 *
	 * @param uri The URI to be manipulated.
	 * @param oldLastSegment The expected current last segment of the URI's path.
	 * @param newLastSegment The intended future last segment of the URI's path.
	 * @return A URI formed by replacing the last segment of the given URI with the provided new last segment
	 *         in case that the given URI's existing last segment matches the provided old last segment,
	 *         or <code>null</code> otherwise.
	 */
	def static URI replaceLastSegment(URI uri, String oldLastSegment, String newLastSegment) {
		Assert::isNotNull(uri)
		Assert::isLegal(uri.segmentCount > 0)

		if (uri.lastSegment.equals(oldLastSegment)) {
			return uri.trimSegments(1).appendSegment(newLastSegment)
		}
		return null
	}

	/**
	 * If given old base URI matches the given {@link URI}, this returns a copy of the given URI
	 * where all components included in the old base URI have been replaced by the provided new base URI.
	 * The old and new base URI may not only include a {@link URI#path() path} but also
	 * a {@link URI#query() query} and/or a {@link URI#fragment() fragment}.
	 *
	 * @param uri The URI to be manipulated.
	 * @param oldBaseURI The URI's expected current base URI.
	 * @param newBaseURI The URI's intended future base URI.
	 * @return A URI formed by replacing the provided old base URI in the given URI with the provided new base URI
	 *         in case that the old base URI matches the given URI, or <code>null</code> otherwise.
	 */
	def static URI replaceBaseURI(URI uri, URI oldBaseURI, URI newBaseURI) {
		Assert::isNotNull(uri)
		Assert::isNotNull(oldBaseURI)
		Assert::isNotNull(newBaseURI)

		if (uri.hasFragment && oldBaseURI.hasFragment && newBaseURI.hasFragment) {
			if (uri.trimFragment.equals(oldBaseURI.trimFragment)) {
				var fragment = uri.getFragment.replacePrefix(oldBaseURI.getFragment.asPrefix,
					newBaseURI.getFragment.asPrefix)
				if (fragment != null) {
					return newBaseURI.substituteFragment(fragment)
				}
			}
		} else {
			return uri.replacePrefix(oldBaseURI.asPrefix, newBaseURI.asPrefix)
		}
		return null
	}

	/**
	 * If given {@link URI} has a {@link URI#isHierarchical() hierarchical} {@link URI#fragment() fragment}
	 * and given old last fragment segment matches the {@link URI#lastSegment() last segment} of that fragment,
	 * this returns a copy of the given URI where the last segment of its fragment
	 * has been replaced by the provided new last fragment segment.
	 *
	 * @param uri The URI to be manipulated.
	 * @param oldLastSegment The expected current last segment of the URI's fragment.
	 * @param newLastSegment The intended future last segment of the URI's fragment.
	 * @return A URI formed by replacing the last segment in the fragment of the given URI
	 *         with the provided new last fragment segment in case that the existing last segment
	 *         in the fragment of the given URI matches the provided old last fragment segment,
	 *         or <code>null</code> otherwise.
	 */
	def static URI replaceLastFragmentSegment(URI uri, String oldLastFragmentSegment, String newLastFragmentSegment) {
		Assert::isNotNull(uri)

		var fragment = uri.getFragment
		if (fragment != null) {
			fragment = fragment.replaceLastSegment(oldLastFragmentSegment, newLastFragmentSegment)
			if (fragment != null) {
				return uri.substituteFragment(fragment)
			}
		}
		return null
	}
}
