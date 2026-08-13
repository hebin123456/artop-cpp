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

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.uml2.uml.internal.resource.UMLLoadImpl;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("restriction")
public class ExtendedUMLLoadImpl extends UMLLoadImpl {

	public ExtendedUMLLoadImpl(XMLHelper helper) {
		super(helper);
	}

	/*
	 * @see org.eclipse.sphinx.emf.resource.ExtendedXMLLoadImpl#makeDefaultHandler()
	 */
	@Override
	protected DefaultHandler makeDefaultHandler() {
		return new ExtendedUMLHandler(resource, helper, options);
	}
}
