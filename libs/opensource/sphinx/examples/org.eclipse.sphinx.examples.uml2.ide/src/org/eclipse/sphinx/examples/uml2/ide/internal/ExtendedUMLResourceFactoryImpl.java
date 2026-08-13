/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 itemis and others.
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
package org.eclipse.sphinx.examples.uml2.ide.internal;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.resource.UMLResource;

@SuppressWarnings("restriction")
public class ExtendedUMLResourceFactoryImpl extends UMLResourceFactoryImpl {

	@Override
	public Resource createResourceGen(URI uri) {
		UMLResource result = new ExtendedUMLResourceImpl(uri);
		result.setEncoding(UMLResource.DEFAULT_ENCODING);
		return result;
	}
}
