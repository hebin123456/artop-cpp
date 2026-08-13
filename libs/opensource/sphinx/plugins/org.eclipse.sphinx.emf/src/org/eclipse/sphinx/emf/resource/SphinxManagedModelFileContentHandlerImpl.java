/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423687] Synchronize ExtendedPlatformContentHandlerImpl wrt latest changes in EMF's PlatformContentHandlerImpl
 *     
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.impl.ContentHandlerImpl;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * A performance-optimized {@link ContentHandler handler} for describing the contents of Sphinx-managed model files
 * referenced by a given {@link URI}.
 * <p>
 * Delegates to the performance-optimized content type id detection support provided by {@link ExtendedPlatform} in case
 * that provided URI refers to a Sphinx-managed model file inside the workspace and only the content type id but no
 * other content description properties are requested. In all other cases, i.e., when the provided URI references some
 * file that is apparently not managed by Sphinx or when additional content description properties are explicitly
 * requested, this class delegates to its super implementation. The latter retrieves and returns an indeterminate best
 * guess content description which may subsequently be overridden by another content handler (e.g.,
 * PlatformContentHandlerImpl) with some more appropriate content description (see
 * org.eclipse.emf.ecore.resource.impl.URIHandlerImpl#contentDescription(URI, Map<?, ?>) for details).
 * </p>
 */
public class SphinxManagedModelFileContentHandlerImpl extends ContentHandlerImpl {

	/*
	 * @see org.eclipse.emf.ecore.resource.impl.ContentHandlerImpl#canHandle(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public boolean canHandle(URI uri) {
		// Return true for all potential Sphinx-managed model files inside the workspace
		if (uri.isPlatformResource()) {
			IFile file = EcorePlatformUtil.getFile(uri);
			if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.emf.ecore.resource.impl.ContentHandlerImpl#contentDescription(org.eclipse.emf.common.util.URI,
	 * java.io.InputStream, java.util.Map, java.util.Map)
	 */
	@Override
	public Map<String, Object> contentDescription(URI uri, InputStream inputStream, Map<?, ?> options, Map<Object, Object> context)
			throws IOException {
		// Explicit requests for specific content description properties (but not only the content type id)?
		Set<String> requestedProperties = getRequestedProperties(options);
		if (requestedProperties != null) {
			// Proceed as usually, i.e., let super implementation retrieve an indeterminate best guess content
			// description which may subsequently be be overridden by other content handlers (e.g.,
			// PlatformContentHandlerImpl, see
			// org.eclipse.emf.ecore.resource.impl.URIHandlerImpl#contentDescription(URI, Map<?, ?>) for details).
			return super.contentDescription(uri, inputStream, options, context);
		}

		try {
			// Determine content type id
			/*
			 * Performance optimization: Use performance-optimized API provided through ExtendedPlatform instead of
			 * relying on the platform's native content description support.
			 */
			IFile file = EcorePlatformUtil.getFile(uri);
			String contentTypeId = ExtendedPlatform.getContentTypeId(file);

			// Create and return corresponding content description
			/*
			 * !! Important Note !! Return valid content type description even if no content type could be established
			 * so as to avoid that other content handlers (e.g., PlatformContentHandlerImpl) override the result
			 * computed by this one (see org.eclipse.emf.ecore.resource.impl.URIHandlerImpl#contentDescription(URI,
			 * Map<?, ?>) for details).
			 */
			Map<String, Object> result = createContentDescription(ContentHandler.Validity.VALID);
			result.put(ContentHandler.CONTENT_TYPE_PROPERTY, contentTypeId);
			return result;
		} catch (CoreException ex) {
			throw new IOException(ex.getCause());
		}
	}
}
