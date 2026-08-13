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
package org.eclipse.sphinx.examples.hummingbird20.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapter;
import org.eclipse.sphinx.emf.resource.ExtendedXMIHelperImpl;
import org.eclipse.sphinx.emf.resource.ExtendedXMILoadImpl;
import org.eclipse.sphinx.emf.resource.ExtendedXMISaveImpl;

/**
 * The <b>Resource </b> associated with the package.
 *
 * @see org.eclipse.sphinx.examples.hummingbird20.util.Hummingbird20ResourceFactoryImpl
 */
public class Hummingbird20ResourceImpl extends XMIResourceImpl {

	/**
	 * {@link Adapter} providing Hummingbird-specific implementation of {@link ExtendedResource extended resource}
	 * services.
	 *
	 * @see ExtendedResource
	 */
	protected ExtendedResourceAdapter extendedResource;

	/**
	 * Creates an instance of the resource.
	 *
	 * @param uri
	 *            the URI of the new resource.
	 */
	public Hummingbird20ResourceImpl(URI uri) {
		super(uri);

		// Install adapter providing Hummingbird-specific implementation of extended resource services
		extendedResource = new ExtendedHummingbirdResourceAdapter();
		eAdapters().add(extendedResource);
	}

	/*
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceImpl#unloaded(org.eclipse.emf.ecore.InternalEObject)
	 */
	@Override
	protected void unloaded(InternalEObject internalEObject) {
		// Delegate to implementation provided by extended resource for enabling memory-optimized unloading
		extendedResource.unloaded(internalEObject);
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl#createXMLLoad()
	 */
	@Override
	protected XMLLoad createXMLLoad() {
		// Use extended XMILoad implementation - and in turn extended SAXXMIHandler implementation - to include support
		// for on-the-fly resource migration, resource-centric problem handling, enhanced entity resolution, ignorable
		// whitespace suppression, and to enable creation of proxy URIs from serialized cross-document references to be
		// customized through extended resource service
		return new ExtendedXMILoadImpl(createXMLHelper());
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl#createXMLSave()
	 */
	@Override
	protected XMLSave createXMLSave() {
		// Use extended XMISave implementation for XMI to include support for on-the-fly resource migration and enhanced
		// schema location support
		return new ExtendedXMISaveImpl(createXMLHelper());
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl#createXMLHelper()
	 */
	@Override
	protected XMLHelper createXMLHelper() {
		// Use extended XMIHelper implementation to enable creation of HREFs used for serializing cross-document
		// references to be customized through extended resource service
		return new ExtendedXMIHelperImpl(this);
	}
} // Hummingbird20ResourceImpl
