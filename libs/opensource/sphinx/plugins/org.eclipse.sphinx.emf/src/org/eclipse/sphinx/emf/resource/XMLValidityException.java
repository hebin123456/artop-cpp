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
package org.eclipse.sphinx.emf.resource;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Encapsulate an XML schema validation error or warning.
 * <p>
 * This exception may include information for locating the error in the original XML document, as if it came from a
 * {@link Locator} object. Note that although the application will receive a {@link XMLValidityException} as the
 * argument to the handlers in the {@link org.xml.sax.ErrorHandler ErrorHandler} interface, the application is not
 * actually required to throw the exception; instead, it can simply read the information in it and take a different
 * action.
 * </p>
 * <p>
 * Since this exception is a subclass of {@link org.xml.sax.SAXParseException SAXParseException}, it inherits the
 * ability to wrap another exception.
 * </p>
 * 
 * @since 0.7.0
 * @see org.xml.sax.SAXException
 * @see org.xml.sax.Locator
 * @see org.xml.sax.ErrorHandler
 */
public class XMLValidityException extends SAXParseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new XMLValidityException from a message and a Locator.
	 * <p>
	 * This constructor is especially useful when an application is creating its own exception from within a
	 * {@link org.xml.sax.ContentHandler ContentHandler} callback.
	 * </p>
	 * 
	 * @param message
	 *            The error or warning message.
	 * @param locator
	 *            The locator object for the error or warning (may be null).
	 * @see org.xml.sax.Locator
	 */
	public XMLValidityException(String message, Locator locator) {
		super(message, locator);
	}

	/**
	 * Wrap an existing exception in a XMLValidityException.
	 * <p>
	 * This constructor is especially useful when an application is creating its own exception from within a
	 * {@link org.xml.sax.ContentHandler ContentHandler} callback, and needs to wrap an existing exception that is not a
	 * subclass of {@link org.xml.sax.SAXException SAXException}.
	 * </p>
	 * 
	 * @param message
	 *            The error or warning message, or null to use the message from the embedded exception.
	 * @param locator
	 *            The locator object for the error or warning (may be null).
	 * @param e
	 *            Any exception.
	 * @see org.xml.sax.Locator
	 */
	public XMLValidityException(String message, Locator locator, Exception e) {
		super(message, locator, e);
	}

	/**
	 * Create a new XMLValidityException.
	 * <p>
	 * This constructor is most useful for parser writers.
	 * </p>
	 * <p>
	 * All parameters except the message are as if they were provided by a {@link Locator}. For example, if the system
	 * identifier is a URL (including relative filename), the caller must resolve it fully before creating the
	 * exception.
	 * </p>
	 * 
	 * @param message
	 *            The error or warning message.
	 * @param publicId
	 *            The public identifier of the entity that generated the error or warning.
	 * @param systemId
	 *            The system identifier of the entity that generated the error or warning.
	 * @param lineNumber
	 *            The line number of the end of the text that caused the error or warning.
	 * @param columnNumber
	 *            The column number of the end of the text that cause the error or warning.
	 */
	public XMLValidityException(String message, String publicId, String systemId, int lineNumber, int columnNumber) {
		super(message, publicId, systemId, lineNumber, columnNumber);
	}

	/**
	 * Create a new XMLValidityException with an embedded exception.
	 * <p>
	 * This constructor is most useful for parser writers who need to wrap an exception that is not a subclass of
	 * {@link org.xml.sax.SAXException SAXException}.
	 * </p>
	 * <p>
	 * All parameters except the message and exception are as if they were provided by a {@link Locator}. For example,
	 * if the system identifier is a URL (including relative filename), the caller must resolve it fully before creating
	 * the exception.
	 * </p>
	 * 
	 * @param message
	 *            The error or warning message, or null to use the message from the embedded exception.
	 * @param publicId
	 *            The public identifier of the entity that generated the error or warning.
	 * @param systemId
	 *            The system identifier of the entity that generated the error or warning.
	 * @param lineNumber
	 *            The line number of the end of the text that caused the error or warning.
	 * @param columnNumber
	 *            The column number of the end of the text that cause the error or warning.
	 * @param e
	 *            Another exception to embed in this one.
	 */
	public XMLValidityException(String message, String publicId, String systemId, int lineNumber, int columnNumber, Exception e) {
		super(message, publicId, systemId, lineNumber, columnNumber, e);
	}

	/*
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		String name = getClass().getName();
		String msg = getLocalizedMessage();
		return msg != null ? msg : name;
	}
}
