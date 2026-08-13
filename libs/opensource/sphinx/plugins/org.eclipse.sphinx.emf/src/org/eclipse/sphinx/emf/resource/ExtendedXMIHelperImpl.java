/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [443647] Enable HREF representing serialized cross-document references to be customized through ExtendedResource of resource being serialized
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;

public class ExtendedXMIHelperImpl extends XMIHelperImpl {

	protected ExtendedResource extendedResource;

	public ExtendedXMIHelperImpl() {
		this(null);
	}

	public ExtendedXMIHelperImpl(XMLResource resource) {
		super(resource);

		extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
	}

	/*
	 * Overridden to enable delegation of actual HREF URI creation to {@link ExtendedResourceAdapter extended resource
	 * adapter} and to trim all potentially present proxy context information.
	 * @see org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl#getHREF(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public String getHREF(EObject obj) {
		URI objectURI;

		if (!obj.eIsProxy()) {
			Resource otherResource = obj.eResource();
			if (otherResource == null) {
				if (resource != null && resource.getID(obj) != null) {
					if (extendedResource != null) {
						objectURI = extendedResource.getHREF(obj);
					} else {
						objectURI = getHREF(resource, obj);
					}
				} else {
					objectURI = handleDanglingHREF(obj);
					if (objectURI == null) {
						return null;
					}
				}
			} else {
				if (extendedResource != null) {
					objectURI = extendedResource.getHREF(obj);
				} else {
					objectURI = getHREF(otherResource, obj);
				}
			}
		} else {
			if (extendedResource != null) {
				objectURI = extendedResource.getHREF(obj);
				objectURI = extendedResource.trimProxyContextInfo(objectURI);
			} else {
				objectURI = ((InternalEObject) obj).eProxyURI();
			}
		}

		objectURI = deresolve(objectURI);

		return objectURI.toString();
	}
}
