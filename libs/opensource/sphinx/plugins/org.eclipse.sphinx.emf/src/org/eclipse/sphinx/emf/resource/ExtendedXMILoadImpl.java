/**
 * <copyright>
 *
 * Copyright (c) 2002-2011 IBM Corporation, See4sys, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *   IBM - Initial API and implementation
 *   See4sys - copied from org.eclipse.emf.ecore.xmi.impl.XMILoadImpl 
 *             for letting it extend {@link ExtendedXMLLoadImpl} instead 
 *             of {@link XMLLoadImpl}
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is a replacement for {@link XMILoadImpl}. It extends {@link ExtendedXMLLoadImpl} instead of
 * {@link XMLLoadImpl} and creates an {@link ExtendedSAXXMIHandler} rather than a {@link SAXXMIHandler}.
 */
public class ExtendedXMILoadImpl extends ExtendedXMLLoadImpl {

	/**
	 * Constructor for XMILoad.
	 */
	public ExtendedXMILoadImpl(XMLHelper helper) {
		super(helper);
	}

	@Override
	protected DefaultHandler makeDefaultHandler() {
		return new ExtendedSAXXMIHandler(resource, helper, options);
	}
}
