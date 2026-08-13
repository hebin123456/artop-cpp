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
package org.eclipse.sphinx.emf.serialization.generators.util;

import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.ecore.EcorePackage;

public interface IGeneratorConstants {
	String UNINITIALIZED_STRING = "uninitialized";//$NON-NLS-1$

	/* Suffix */
	String SUFFIX_SINGULAR_REF = "-REF"; //$NON-NLS-1$
	String SUFFIX_PLURAL_REF = "-REFS"; //$NON-NLS-1$
	String SUFFIX_SINGULAR_TYPE_REF = "-TREF"; //$NON-NLS-1$
	String SUFFIX_PLURAL_TYPE_REF = "-TREFS"; //$NON-NLS-1$

	/* Boolean */
	String BOOLEAN_TRUE = Boolean.TRUE.toString();
	String BOOLEAN_FALSE = Boolean.FALSE.toString();

	/* Suffix */
	String SUFFIX_NAME = "-IREF"; //$NON-NLS-1$
	String SUFFIX_NAME_PLURAL = "-IREFS"; //$NON-NLS-1$
	String SUFFIX_SUBTPES_ENUM = "--SUBTYPES-ENUM"; //$NON-NLS-1$
	String SUFFIX_SIMPLE = "--SIMPLE"; //$NON-NLS-1$

	/**
	 * The prefix to be used for the schema for schema namespace version in new schemas.
	 */
	String DEFAULT_XML_SCHEMA_NAMESPACE_PREFIX = "xsd"; //$NON-NLS-1$

	/**
	 * The token to be used for the extension attribute name
	 */
	String TOKEN = "TYPE"; //$NON-NLS-1$

	String GEN_MODEL_PACKAGE_NS_URI = GenModelPackage.eNS_URI;

	/**
	 * The Ecore namespace version to be used for new schemas.
	 */
	String DEFAULT_ECORE_NAMESPACE = EcorePackage.eNS_URI;

	/**
	 * The XML schema location to be used for new schemas.
	 */
	String DEFAULT_XML_SCHEMA_LOCATION = "http://www.w3.org/2001/xml.xsd"; //$NON-NLS-1$
}
