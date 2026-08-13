/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys and others.
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
package org.eclipse.sphinx.graphiti.workspace.metamodel;

import org.eclipse.emf.common.util.URI;
import org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;

/**
 * Implementation of {@linkplain IMetaModelDescriptor} for meta-model Graphiti Pictogram that declares the descriptor
 * identifier and the name-space {@linkplain URI} describing the Pictogram meta-model.
 */
public class GraphitiMMDescriptor extends AbstractMetaModelDescriptor {

	/**
	 * The default file extension for Graphiti diagram files.
	 */
	public static final String GRAPHITI_DIAGRAM_DEFAULT_FILE_EXTENSION = "diag"; //$NON-NLS-1$

	/**
	 * The id of the content type for Graphiti diagram files.
	 */
	public static final String GRAPHITI_DIAGRAM_CONTENT_TYPE_ID = "org.eclipse.sphinx.graphiti.diagramFile"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	public static final GraphitiMMDescriptor INSTANCE = new GraphitiMMDescriptor();

	private static final String ID = "org.eclipse.graphiti.mm"; //$NON-NLS-1$
	/*
	 * Performance optimization: Don't retrieve namespace with MmPackage.eNS_URI so as to avoid unnecessary
	 * initialization of the Graphiti MetaModel's EPackages. Clients may want to consult the Graphiti metamodel
	 * descriptor even if no Graphiti diagram file actually exists, and the initialization of the Graphiti MetaModel's
	 * EPackages in such situations would entail useless runtime and memory consumption overhead.
	 */
	private static final String NAMESPACE = "http://eclipse.org/graphiti/mm"; //$NON-NLS-1$
	private static final String EPKG_NS_URI_POSTFIX_PATTERN = "(pictograms|algorithms)(/\\w)*"; //$NON-NLS-1$
	private static final String NAME = "Graphiti MetaModel"; //$NON-NLS-1$

	/**
	 * Private default constructor for singleton pattern.
	 */
	private GraphitiMMDescriptor() {
		super(ID, NAMESPACE, EPKG_NS_URI_POSTFIX_PATTERN, NAME);
	}

	/*
	 * @see org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor#getDefaultContentTypeId()
	 */
	@Override
	public String getDefaultContentTypeId() {
		return GRAPHITI_DIAGRAM_CONTENT_TYPE_ID;
	}
}
