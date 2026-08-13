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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.serialization;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

/**
 * <!-- begin-user-doc --> The <b>Resource Factory</b> associated with the package. <!-- end-user-doc -->
 *
 * @see org.eclipse.rmf.reqif10.util.Reqif10ResourceImpl
 * @generated
 */
public class XMLPersistenceMappingResourceFactoryImpl extends ResourceFactoryImpl {
	/**
	 * Creates an instance of the resource factory. <!-- begin-user-doc --> <!-- end-user-doc -->
	 *
	 * @generated
	 */
	public XMLPersistenceMappingResourceFactoryImpl() {
		super();
	}

	/**
	 * Creates an instance of the resource. <!-- begin-user-doc --> <!-- end-user-doc -->
	 */
	@Override
	public Resource createResource(URI uri) {
		Resource result = new XMLPersistenceMappingResourceImpl(uri);
		return result;
	}

} // RMFResourceFactoryImpl
