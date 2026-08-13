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

import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An {@link ErrorHandlerWrapper} implementation that evaluates the domain parameter passed to
 * {@link ErrorHandlerWrapper#error(String, String, XMLParseException)},
 * {@link ErrorHandlerWrapper#warning(String, String, XMLParseException)}, and
 * {@link ErrorHandlerWrapper#fatalError(String, String, XMLParseException)} methods and creates, if applicable,
 * domain-specific {@link XMLWellformednessException}s or {@link XMLValidityException}s rather than ordinary
 * {@link javax.sql.rowset.spi.SyncProviderException}s.
 *
 * @see #createXMLWellformednessException(XMLParseException)
 * @see #createXMLValidityException(XMLParseException)
 */
public class ExtendedErrorHandlerWrapper extends ErrorHandlerWrapper implements XMLErrorHandler {

	public ExtendedErrorHandlerWrapper() {
	}

	public ExtendedErrorHandlerWrapper(ErrorHandler errorHandler) {
		super(errorHandler);
	}

	/*
	 * @see org.apache.xerces.util.ErrorHandlerWrapper#warning(java.lang.String, java.lang.String,
	 * org.apache.xerces.xni.parser.XMLParseException)
	 */
	@Override
	public void warning(String domain, String key, XMLParseException exception) throws XNIException {
		if (fErrorHandler != null) {
			SAXParseException saxException = createSAXParseException(domain, exception);

			try {
				fErrorHandler.warning(saxException);
			} catch (SAXParseException e) {
				throw createXMLParseException(e);
			} catch (SAXException e) {
				throw createXNIException(e);
			}
		}
	}

	/*
	 * @see org.apache.xerces.util.ErrorHandlerWrapper#error(java.lang.String, java.lang.String,
	 * org.apache.xerces.xni.parser.XMLParseException)
	 */
	@Override
	public void error(String domain, String key, XMLParseException exception) throws XNIException {
		if (fErrorHandler != null) {
			SAXParseException saxException = createSAXParseException(domain, exception);

			try {
				fErrorHandler.error(saxException);
			} catch (SAXParseException e) {
				throw createXMLParseException(e);
			} catch (SAXException e) {
				throw createXNIException(e);
			}
		}
	}

	/*
	 * @see org.apache.xerces.util.ErrorHandlerWrapper#fatalError(java.lang.String, java.lang.String,
	 * org.apache.xerces.xni.parser.XMLParseException)
	 */
	@Override
	public void fatalError(String domain, String key, XMLParseException exception) throws XNIException {
		if (fErrorHandler != null) {
			SAXParseException saxException = createSAXParseException(domain, exception);

			try {
				fErrorHandler.fatalError(saxException);
			} catch (SAXParseException e) {
				throw createXMLParseException(e);
			} catch (SAXException e) {
				throw createXNIException(e);
			}
		}
	}

	/**
	 * Creates a {@link SAXParseException} from an given domain and {@link XMLParseException}.
	 */
	protected SAXParseException createSAXParseException(String domain, XMLParseException exception) {
		if (XSMessageFormatter.SCHEMA_DOMAIN.equals(domain)) {
			return createXMLValidityException(exception);
		} else if (XMLMessageFormatter.XML_DOMAIN.equals(domain) || XMLMessageFormatter.XMLNS_DOMAIN.equals(domain)) {
			return createXMLWellformednessException(exception);
		}
		return createSAXParseException(exception);
	}

	/**
	 * Creates an {@linkplain XMLWellformednessException} from given {@link XMLParseException}.
	 */
	protected SAXParseException createXMLWellformednessException(XMLParseException exception) {
		return new XMLWellformednessException(exception.getMessage(), exception.getPublicId(), exception.getExpandedSystemId(),
				exception.getLineNumber(), exception.getColumnNumber(), exception.getException());
	}

	/**
	 * Creates an {@linkplain XMLValidityException} from given {@link XMLParseException}.
	 */
	protected SAXParseException createXMLValidityException(XMLParseException exception) {
		return new XMLValidityException(exception.getMessage(), exception.getPublicId(), exception.getExpandedSystemId(), exception.getLineNumber(),
				exception.getColumnNumber(), exception.getException());
	}
}
