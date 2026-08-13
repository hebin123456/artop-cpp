/**
 * <copyright>
 * 
 * Copyright (c) 2012 BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.resource;

public class ExtendedResourceConstants {
	/**
	 * EClass feature name for the mixed featur map attribute that is used (if present) during loading/saving to handle
	 * XML content (text, comments, processing instructions and CDATA) that is found before the model/XML root element.
	 * If this attribute does not exists such content will be silently ignored.
	 */
	public static final String OUTER_CONTENT_ATTRIBUTE_NAME = "mixedOuterContent"; //$NON-NLS-1$
}
