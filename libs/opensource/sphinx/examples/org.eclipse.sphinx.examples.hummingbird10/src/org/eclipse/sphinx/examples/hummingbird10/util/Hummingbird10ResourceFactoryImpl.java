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
 *     itemis - Enhancements and maintenance
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird10.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

/**
 * The <b>Resource Factory</b> associated with the package.
 *
 * @see org.eclipse.sphinx.examples.hummingbird10.util.Hummingbird10ResourceImpl
 */
public class Hummingbird10ResourceFactoryImpl extends ResourceFactoryImpl {

	/**
	 * Creates an instance of the resource factory.
	 */
	public Hummingbird10ResourceFactoryImpl() {
		super();
	}

	/**
	 * Creates an instance of the resource.
	 */
	@Override
	public Resource createResource(URI uri) {
		Resource result = new Hummingbird10ResourceImpl(uri);
		return result;
	}

} // Hummingbird10ResourceFactoryImpl
