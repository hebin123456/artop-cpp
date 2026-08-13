/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.uml2.ide.internal;

import java.util.Map;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.uml2.uml.internal.resource.UMLHandler;

@SuppressWarnings("restriction")
public class ExtendedUMLHandler extends UMLHandler {

	protected ExtendedResource extendedResource;

	public ExtendedUMLHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
		super(xmiResource, helper, options);

		extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(xmlResource);
	}

	/*
	 * Overridden to augment proxy URIs to context-aware proxy URIs required to honor their {@link IResourceScope
	 * resource scope}s when they are being resolved and to support the resolution of proxified references between
	 * objects from different metamodels.
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLHandler#handleProxy(org.eclipse.emf.ecore.InternalEObject,
	 * java.lang.String)
	 */
	@Override
	protected void handleProxy(InternalEObject proxy, String uriLiteral) {
		super.handleProxy(proxy, uriLiteral);

		if (extendedResource != null) {
			extendedResource.augmentToContextAwareProxy(proxy);
		}
	}
}
