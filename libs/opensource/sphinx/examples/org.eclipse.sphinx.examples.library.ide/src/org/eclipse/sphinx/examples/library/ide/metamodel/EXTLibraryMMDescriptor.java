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
package org.eclipse.sphinx.examples.library.ide.metamodel;

import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor;

/**
 * Extended Library metamodel descriptor.
 */
public class EXTLibraryMMDescriptor extends AbstractMetaModelDescriptor {

	/**
	 * The id of the content type for Extended Library XMI files.
	 * <p>
	 * Provides fix for wrong {@link EXTLibraryPackage#eCONTENT_TYPE} value which should be
	 * <code>org.eclipse.emf.examples.library.extendedLibrary</code> but actually is <code>extendedLibrary</code>.
	 * </p>
	 */
	/*
	 * Performance optimization: Don't retrieve content type id with EXTLibraryPackage.eCONTENT_TYPE so as to avoid
	 * unnecessary initialization of the Extended Library metamodel's EPackage. Clients may want to consult the Extended
	 * Library metamodel descriptor even if no Extended Library XMI file actually exists, and the initialization of the
	 * Extended Library metamodel's EPackage in such situations would entail useless runtime and memory consumption
	 * overhead.
	 */
	public static final String XMI_CONTENT_TYPE_ID = "org.eclipse.emf.examples.library.extendedLibrary"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	public static final EXTLibraryMMDescriptor INSTANCE = new EXTLibraryMMDescriptor();

	private static final String ID = "org.eclipse.sphinx.examples.extlibrary"; //$NON-NLS-1$
	/*
	 * Performance optimization: Don't retrieve namespace with EXTLibraryPackage.eNS_URI so as to avoid unnecessary
	 * initialization of the Extended Library metamodel's EPackage. Clients may want to consult the Extended Library
	 * metamodel descriptor even if no Extended Library XMI file actually exists, and the initialization of the Extended
	 * Library metamodel's EPackage in such situations would entail useless runtime and memory consumption overhead.
	 */
	private static final String NAMESPACE = "http:///org/eclipse/emf/examples/library/extlibrary.ecore/1.0.0"; //$NON-NLS-1$
	private static final String NAME = "Extended Library"; //$NON-NLS-1$

	/**
	 * Private default constructor for singleton pattern.
	 */
	private EXTLibraryMMDescriptor() {
		super(ID, NAMESPACE, NAME);
	}

	/*
	 * @see org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor#getDefaultContentTypeId()
	 */
	@Override
	public String getDefaultContentTypeId() {
		return XMI_CONTENT_TYPE_ID;
	}
}
