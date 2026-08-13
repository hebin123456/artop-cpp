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
package org.eclipse.sphinx.gmf.workspace.metamodel;

import org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor;

/**
 * GMF Diagram Notation metamodel descriptor.
 */
public class GMFNotationDescriptor extends AbstractMetaModelDescriptor {

	/**
	 * The id of the content type for GMF diagram files.
	 */
	public static final String GMF_DIAGRAM_CONTENT_TYPE_ID = "org.eclipse.sphinx.gmf.diagramFile"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	public static final GMFNotationDescriptor INSTANCE = new GMFNotationDescriptor();

	private static final String ID = "org.eclipse.gmf.runtime.notation"; //$NON-NLS-1$
	/*
	 * Performance optimization: Don't retrieve namespace with NotationPackage.eNS_URI so as to avoid unnecessary
	 * initialization of the GMF Notation metamodel's EPackage. Clients may want to consult the GMF Notation metamodel
	 * descriptor even if no GMF diagram file actually exists, and the initialization of the GMF Notation metamodel's
	 * EPackage in such situations would entail useless runtime and memory consumption overhead.
	 */
	private static final String NAMESPACE = "http://www.eclipse.org/gmf/runtime/1.0.2/notation"; //$NON-NLS-1$
	private static final String NAME = "GMF Notation"; //$NON-NLS-1$

	/**
	 * Private default constructor for singleton pattern.
	 */
	private GMFNotationDescriptor() {
		super(ID, NAMESPACE, NAME);
	}

	/*
	 * @see org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor#getDefaultContentTypeId()
	 */
	@Override
	public String getDefaultContentTypeId() {
		return GMF_DIAGRAM_CONTENT_TYPE_ID;
	}
}
