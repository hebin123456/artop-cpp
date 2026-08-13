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
 * Encapsulates a proxy URI integrity error or warning. Such integrity problems include mainly runtime exceptions raised
 * during resolution of bad proxy URIs.
 */
public class ProxyURIIntegrityException extends XMIException {

	private static final long serialVersionUID = 1L;

	public ProxyURIIntegrityException(String message) {
		super(message, "", 1, 1); //$NON-NLS-1$
	}

	public ProxyURIIntegrityException(Exception exception) {
		super(exception, "", 1, 1); //$NON-NLS-1$
	}

	public ProxyURIIntegrityException(String message, Exception exception) {
		super(message, exception, "", 1, 1); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.emf.ecore.xmi.XMIException#getMessage()
	 */
	@Override
	public String getMessage() {
		int oldLine = line;
		line = 0;
		String msg = super.getMessage();
		line = oldLine;
		return msg;
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
