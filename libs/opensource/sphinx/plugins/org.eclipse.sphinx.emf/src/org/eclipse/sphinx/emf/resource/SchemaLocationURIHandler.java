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
package org.eclipse.sphinx.emf.resource;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl.PlatformSchemeAware;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

public class SchemaLocationURIHandler extends PlatformSchemeAware {

	protected static final String SCHEMA_FILE_EXTENSION = "xsd"; //$NON-NLS-1$

	protected Set<URI> schemaLocationBaseURIs = new HashSet<URI>(1);

	protected Map<String, String> schemaLocationCatalog;

	public SchemaLocationURIHandler(Map<String, String> schemaLocationCatalog) {
		this.schemaLocationCatalog = schemaLocationCatalog;
	}

	public void addSchemaLocationBaseURI(Plugin plugin, String path) {
		if (plugin != null) {
			URI uri = URI.createPlatformPluginURI(plugin.getBundle().getSymbolicName() + ExtendedResource.URI_SEGMENT_SEPARATOR + path, true);
			addSchemaLocationBaseURI(uri);
		}
	}

	public void addSchemaLocationBaseURI(URI uri) {
		if (uri != null) {
			schemaLocationBaseURIs.add(uri);
		}
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl#resolve(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public URI resolve(URI uri) {
		// Test if given URI is actually a namespace matching one of the schema location catalog entries; retrieve
		// and proceed with URI of associated system identifer in this case
		if (schemaLocationCatalog != null) {
			String systemId = schemaLocationCatalog.get(uri.toString());
			if (systemId != null) {
				uri = URI.createURI(systemId);
			}
		}

		// Is URI an unresolved schema URI?
		if (uri.isRelative() && uri.hasRelativePath() && SCHEMA_FILE_EXTENSION.equals(getFileExtension(uri))) {
			// Try to resolve given schema URI against schema location base URIs
			for (Iterator<URI> iter = schemaLocationBaseURIs.iterator(); iter.hasNext();) {
				URI schemaLocationBaseURI = iter.next();
				URI resolvedURI = schemaLocationBaseURI.appendSegments(uri.segments());

				// Has resolution been successful?
				if (!resolvedURI.isRelative()) {
					// Return resolved schema URI immediately if there is no other schema location base URI
					// candidate; otherwise check if schema behind URI actually exists and attempt resolution against
					// next schema location base URI candidate if necessary
					if (!iter.hasNext() || EcoreResourceUtil.exists(resolvedURI)) {
						return resolvedURI;
					}
				}
			}
		}

		// Try to resolve URI against base URI (i.e., typically URI of resource being loaded/saved)
		return super.resolve(uri);
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.URIHandlerImpl.PlatformSchemeAware#deresolve(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public URI deresolve(URI uri) {
		// Is URI a resolved schema URI?
		if (!uri.isRelative() && SCHEMA_FILE_EXTENSION.equals(getFileExtension(uri))) {
			// Deresolve by extracting schema name
			return URI.createURI(uri.lastSegment());
		}

		// Try to deresolve URI relative to base URI (i.e., typically URI of resource being loaded/saved)
		return super.deresolve(uri);
	}

	protected String getFileExtension(URI uri) {
		return EcorePlatformUtil.createPath(uri).getFileExtension();
	}
}
