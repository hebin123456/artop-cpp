/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.platform.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.impl.Constants;
import org.eclipse.core.runtime.OperationCanceledException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An XML event handler for detecting the root element's namespace, target namespace, and schema location.
 */
/**
 *
 */
public class XMLRootElementHandler extends DefaultHandler implements LexicalHandler {

	/**
	 * An exception indicating that the parsing should stop.
	 */
	private class StopParsingException extends SAXException {
		/**
		 * All serializable objects should have a stable serialVersionUID
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructs an instance of <code>StopParsingException</code> with a <code>null</code> detail message.
		 */
		public StopParsingException() {
			super((String) null);
		}
	}

	private static final String ATTRIBUTE_NAME_TARGET_NAMESPACE = "targetNamespace"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_SCHEMA_LOCATION = "schemaLocation"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PREFIX_XMLNS = XMLConstants.XMLNS_ATTRIBUTE;
	private static final String ATTRIBUTE_NAME_XMI = "xmi"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_XSI = "xsi"; //$NON-NLS-1$

	private final String FIELD_NAME_ATTRIBUTES = "fAttributes"; //$NON-NLS-1$
	private final String FIELD_NAME_NAME = "name"; //$NON-NLS-1$
	private final String FIELD_NAME_VALUE = "value"; //$NON-NLS-1$

	private SAXParserFactory parserFactory;

	private String rootElementNamespace = null;
	private String targetNamespace = null;
	private String xsiSchemaLocation = null;
	private ArrayList<String> rootElementComments = new ArrayList<String>();
	private String[] targetNamespaceExcludePatterns;

	protected SAXParser getParser(boolean useLexicalHandler) throws ParserConfigurationException, SAXException {
		if (parserFactory == null) {
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			parserFactory.setValidating(false);
			parserFactory.setXIncludeAware(false);
			try {
				parserFactory.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE, true);
				parserFactory.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
			} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
				// These exceptions are expected if Xerces is not used as the underlying parser.
			}
			parserFactory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
			parserFactory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);

		}
		SAXParser parser = createParser(parserFactory);
		if (useLexicalHandler) {
			parser.setProperty(Constants.SAX_PROPERTY_PREFIX + Constants.LEXICAL_HANDLER_PROPERTY, this);
		}
		return parser;
	}

	protected SAXParser createParser(SAXParserFactory parserFactory)
			throws ParserConfigurationException, SAXException, SAXNotRecognizedException, SAXNotSupportedException {
		return parserFactory.newSAXParser();
	}

	/*
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
	 * org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException, OperationCanceledException {
		// Retrieve root element namespace
		rootElementNamespace = uri.length() > 0 ? uri : null;

		// Retrieve XSI schema location
		xsiSchemaLocation = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, ATTRIBUTE_NAME_SCHEMA_LOCATION);

		// Retrieve target namespace
		targetNamespace = attributes.getValue("", ATTRIBUTE_NAME_TARGET_NAMESPACE); //$NON-NLS-1$
		if (targetNamespace == null) {
			targetNamespace = getTargetNamespaceFromDeclaredXMLNamespaces(qName, attributes);
		}

		// For the sake of performance stop parsing here!
		throw new StopParsingException();
	}

	// TODO Avoid reflective accesses to internal fields by leveraging SAX parser feature
	// http://xml.org/sax/features/namespace-prefixes; see http://xerces.apache.org/xerces-j/features.html for details
	private String getTargetNamespaceFromDeclaredXMLNamespaces(String qName, Attributes attributes) {
		try {
			int separatorIndex = qName.indexOf(":");//$NON-NLS-1$
			String prefix = separatorIndex != -1 ? qName.substring(0, separatorIndex) : ""; //$NON-NLS-1$

			Object xmlAttributesObject = ReflectUtil.getInvisibleFieldValue(attributes, FIELD_NAME_ATTRIBUTES);
			Object[] xmlAttributesArray = (Object[]) ReflectUtil.getInvisibleFieldValue(xmlAttributesObject, FIELD_NAME_ATTRIBUTES);
			for (Object xmlAttribute : xmlAttributesArray) {
				org.apache.xerces.xni.QName xQName = (org.apache.xerces.xni.QName) ReflectUtil.getInvisibleFieldValue(xmlAttribute, FIELD_NAME_NAME);
				if ((ATTRIBUTE_PREFIX_XMLNS.equals(xQName.prefix) || ATTRIBUTE_PREFIX_XMLNS.equals(xQName.localpart))
						&& !prefix.equals(xQName.localpart)) {
					if (!ATTRIBUTE_NAME_XMI.equals(xQName.localpart) && !ATTRIBUTE_NAME_XSI.equals(xQName.localpart)) {
						if (xQName.localpart != null) {
							String value = (String) ReflectUtil.getInvisibleFieldValue(xmlAttribute, FIELD_NAME_VALUE);
							if (!isExcludedTargetNamespace(value)) {
								return value;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			// Ignore exception, just return null
		}
		return null;
	}

	protected boolean isExcludedTargetNamespace(String value) {
		if (value != null && value.trim().length() > 0 && targetNamespaceExcludePatterns != null) {
			for (String excludePattern : targetNamespaceExcludePatterns) {
				if (value.matches(excludePattern)) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		// we store comments inside document
		if (ch != null && length > 0 && start <= ch.length) {
			String comment = new String(ch, start, length);
			rootElementComments.add(comment);
		}

	}

	/*
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
	public void endCDATA() throws SAXException {
	}

	/*
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
	public void endDTD() throws SAXException {

	}

	/*
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
	public void endEntity(String name) throws SAXException {

	}

	/*
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
	public void startCDATA() throws SAXException {

	}

	/*
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {

	}

	/*
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
	public void startEntity(String name) throws SAXException {
	}

	public void parseContents(InputStream inputStream) throws IOException, ParserConfigurationException, SAXException {
		parseContents(inputStream, false);
	}

	public void parseContents(InputStream inputStream, boolean useLexicalHandler) throws IOException, ParserConfigurationException, SAXException {
		try {
			if (inputStream != null) {
				SAXParser parser = getParser(useLexicalHandler);
				parser.parse(inputStream, this);

			}
		} catch (StopParsingException ex) {
			// This exception is thrown when namespaces have been detected and parsing doesn't need to be
			// proceeded
		}
	}

	public void parseContents(InputSource inputSource) throws IOException, ParserConfigurationException, SAXException {
		parseContents(inputSource, false);
	}

	public void parseContents(InputSource inputSource, boolean useLexicalHandler) throws IOException, ParserConfigurationException, SAXException {
		try {
			if (inputSource != null && inputSource.getByteStream() != null) {
				SAXParser parser = getParser(useLexicalHandler);
				parser.parse(inputSource.getByteStream(), this);
			}
		} catch (StopParsingException ex) {
			// This exception is thrown when namespaces have been detected and parsing doesn't need to be
			// proceeded
		}
	}

	public String getRootElementNamespace() {
		return rootElementNamespace;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public String getSchemaLocation() {
		return xsiSchemaLocation;
	}

	/**
	 * Retrieves all comments located before the root element of the document.
	 *
	 * @return Collection of strings representing the retrieved comments or empty collection if no such could be found.
	 */
	public Collection<String> getRootElementComments() {
		return rootElementComments;
	}

	public void setTargetNamespaceExcludePatterns(String... targetNamespaceExcludePatterns) {
		this.targetNamespaceExcludePatterns = targetNamespaceExcludePatterns;
	}
}
