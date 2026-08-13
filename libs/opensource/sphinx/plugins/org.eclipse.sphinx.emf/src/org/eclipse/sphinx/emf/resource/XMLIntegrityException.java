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

import org.eclipse.emf.ecore.xmi.XMIException;

/**
 * Encapsulates an XML integrity error or warning that is not related to XML parsing or schema validation. Such
 * integrity problems include mainly runtime exceptions raised during deserialization of corrupted XML documents.
 */
public class XMLIntegrityException extends XMIException {

	private static final long serialVersionUID = 1L;

	public XMLIntegrityException(String message, String location, int line, int column) {
		super(message, location, line, column);
	}

	public XMLIntegrityException(Exception exception, String location, int line, int column) {
		super(exception, location, line, column);
	}

	public XMLIntegrityException(String message, Exception exception, String location, int line, int column) {
		super(message, exception, location, line, column);
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
