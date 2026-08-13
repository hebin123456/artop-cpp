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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.sphinx.emf.resource.ExtendedXMIHelperImpl;
import org.eclipse.uml2.uml.internal.resource.UMLResourceImpl;

@SuppressWarnings("restriction")
public class ExtendedUMLResourceImpl extends UMLResourceImpl {

	public ExtendedUMLResourceImpl(URI uri) {
		super(uri);
	}

	@Override
	protected XMLLoad createXMLLoad() {
		// Use extended XMILoad implementation to enable augmentation of proxy URIs created at resource loading time to
		// context-aware proxy URIs required to honor their {@link IResourceScope resource scope}s when they are being
		// resolved and to support the resolution of proxified references between objects from different metamodels
		return new ExtendedUMLLoadImpl(createXMLHelper());
	}

	@Override
	protected XMLHelper createXMLHelper() {
		// Use extended XMIHelper implementation to enable trimming of all potentially present proxy context information
		// from HREFs representing cross-document references to objects in other resources at resource save time.
		return new ExtendedXMIHelperImpl(this);
	}
}
