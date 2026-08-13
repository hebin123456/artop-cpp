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
package org.eclipse.sphinx.emf.serialization.internal;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLPersistenceMappingLoadImpl extends XMLLoadImpl {

	public XMLPersistenceMappingLoadImpl(XMLHelper helper) {
		super(helper);
	}

	@Override
	protected DefaultHandler makeDefaultHandler() {
		XMLPersistenceMappingHandler handler = new XMLPersistenceMappingHandler(resource, helper, options);

		return handler;
	}

	@Override
	protected SAXParser makeParser() throws ParserConfigurationException, SAXException {
		// Create an instance of org.apache.xerces.parsers.SAXParser
		/*
		 * !! Important Note !! We must override makeParser() - even if we wouldn't have any functional changes to apply
		 * - in order to make sure that SAXParserFactory.newInstance() gets invoked from this plug-in which has a
		 * dependency to the org.apache.xerces plug-in and all its classes on the classpath. Otherwise we wouldn't
		 * obtain an instance of org.apache.xerces.jaxp.SAXParserFactoryImpl as intended but fall back to the default
		 * implementation com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl.
		 */
		SAXParserFactory factory = SAXParserFactory.newInstance();
		return factory.newSAXParser();
	}

	@Override
	protected void handleErrors() throws IOException {
		// avoid throwing exception even if errors occur during load
	}

}
