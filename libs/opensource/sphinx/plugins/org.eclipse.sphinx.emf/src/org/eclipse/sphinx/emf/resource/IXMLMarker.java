/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [416830] Harmful dependency on running OSGi environment
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

import org.eclipse.core.resources.IMarker;

public interface IXMLMarker {

	/**
	 * XML well-formedness problem marker type.
	 * <p>
	 * !! Important Note !! Don't use Activator.getPlugin().getSymbolicName() instead of hard-coded plug-in name because
	 * this would prevent this class from being loaded in Java standalone applications.
	 * </p>
	 * 
	 * @see IMarker#getType()
	 */
	public static final String XML_WELLFORMEDNESS_PROBLEM = "org.eclipse.sphinx.emf.xmlwellformednessproblemmarker"; //$NON-NLS-1$

	/**
	 * XML Integrity problem marker type.
	 * <p>
	 * !! Important Note !! Don't use Activator.getPlugin().getSymbolicName() instead of hard-coded plug-in name because
	 * this would prevent this class from being loaded in Java standalone applications.
	 * </p>
	 * 
	 * @see IMarker#getType()
	 */
	public static final String XML_INTEGRITY_PROBLEM = "org.eclipse.sphinx.emf.xmlintegrityproblemmarker"; //$NON-NLS-1$

	/**
	 * XML Validity problem marker type.
	 * <p>
	 * !! Important Note !! Don't use Activator.getPlugin().getSymbolicName() instead of hard-coded plug-in name because
	 * this would prevent this class from being loaded in Java standalone applications.
	 * </p>
	 * 
	 * @see IMarker#getType()
	 */
	public static final String XML_VALIDITY_PROBLEM = "org.eclipse.sphinx.emf.xmlvalidityproblemmarker"; //$NON-NLS-1$
}
