/**
 * <copyright>
 *
 * Copyright (c) 2008-2014 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [442342] Sphinx doen't trim context information from proxy URIs when serializing proxyfied cross-document references
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird10.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sphinx.emf.resource.ExtendedXMIHelperImpl;
import org.eclipse.sphinx.emf.resource.ExtendedXMILoadImpl;

/**
 * The <b>Resource </b> associated with the package.
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.util.Hummingbird10ResourceFactoryImpl
 */
public class Hummingbird10ResourceImpl extends XMIResourceImpl {

	/**
	 * Creates an instance of the resource. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @param uri
	 *            the URI of the new resource.
	 */
	public Hummingbird10ResourceImpl(URI uri) {
		super(uri);
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl#createXMLLoad()
	 */
	@Override
	protected XMLLoad createXMLLoad() {
		// Use extended XMILoad implementation to enable augmentation of proxy URIs created at resource load time to
		// context-aware proxy URIs required to honor their {@link IResourceScope resource scope}s when they are being
		// resolved and to support the resolution of proxified references between objects from different metamodels
		return new ExtendedXMILoadImpl(createXMLHelper());
	}

	@Override
	protected XMLHelper createXMLHelper() {
		// Use extended XMIHelper implementation to enable trimming of all potentially present proxy context information
		// from HREFs representing cross-document references to objects in other resources at resource save time.
		return new ExtendedXMIHelperImpl(this);
	}
} // Hummingbird10ResourceImpl
