/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Added is proxy check to eResolveProxy
 *     itemis - [397357] [EMF Runtime Extensions] Add org.eclipse.sphinx.emf.ecore.proxymanagement.ProxyResolutionBehavior to handle EObject proxy resolution
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.ecore;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.sphinx.emf.ecore.proxymanagement.ProxyResolutionBehavior;

/**
 * This class redefines the EObjectImpl to override method <code>eResolveProxy</code>.
 */
public class ExtendedEObjectImpl extends EObjectImpl {

	/*
	 * @see org.eclipse.emf.ecore.impl.BasicEObjectImpl#eResolveProxy(org.eclipse.emf.ecore.InternalEObject)
	 */
	@Override
	public EObject eResolveProxy(InternalEObject proxy) {
		return ProxyResolutionBehavior.INSTANCE.eResolveProxy(this, proxy);
	}
}
