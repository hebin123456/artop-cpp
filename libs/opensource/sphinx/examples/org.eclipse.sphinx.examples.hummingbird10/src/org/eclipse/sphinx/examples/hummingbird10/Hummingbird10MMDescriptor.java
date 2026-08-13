/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [406203] Enable navigation from a version-specific metamodel descriptor to the underlying base metamodel descriptor
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird10;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelVersionData;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;

/**
 * Implementation of {@linkplain IMetaModelDescriptor} for the Hummingbird 1.0 meta-model.
 */
public class Hummingbird10MMDescriptor extends HummingbirdMMDescriptor {

	/**
	 * The id of the content type for Hummingbird 1.0 XMI files.
	 */
	/*
	 * Performance optimization: Don't retrieve content type id with Hummingbird10Package.eCONTENT_TYPE so as to avoid
	 * unnecessary initialization of the Hummingbird 1.0 metamodel's EPackage. Clients may want to consult the
	 * Hummingbird 1.0 metamodel descriptor even if no Hummingbird 1.0 XMI file actually exists, and the initialization
	 * of the Hummingbird 1.0 metamodel's EPackage in such situations would entail useless runtime and memory
	 * consumption overhead.
	 */
	public static final String XMI_CONTENT_TYPE_ID = "org.eclipse.sphinx.examples.hummingbird10.hummingbird10XMIFile"; //$NON-NLS-1$

	private static final String ID = "org.eclipse.sphinx.examples.hummingbird10"; //$NON-NLS-1$
	private static final String NS_POSTFIX = "1.0.0"; //$NON-NLS-1$
	private static final String EPKG_NS_URI_POSTFIX_PATTERN = "1\\.0\\.0(/\\w+)*"; //$NON-NLS-1$
	private static final String NAME = BASE_NAME + " 1.0"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	public static final Hummingbird10MMDescriptor INSTANCE = new Hummingbird10MMDescriptor();

	/**
	 * Private default constructor for singleton pattern.
	 */
	private Hummingbird10MMDescriptor() {
		super(ID, new MetaModelVersionData(NS_POSTFIX, EPKG_NS_URI_POSTFIX_PATTERN, NAME, HummingbirdMMDescriptor.INSTANCE));
	}

	/*
	 * @see org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor#getDefaultContentTypeId()
	 */
	@Override
	public String getDefaultContentTypeId() {
		return XMI_CONTENT_TYPE_ID;
	}
}
