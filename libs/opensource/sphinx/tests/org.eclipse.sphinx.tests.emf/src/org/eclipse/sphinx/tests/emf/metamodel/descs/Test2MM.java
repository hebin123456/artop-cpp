/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *     itemis - [409367] Add a custom URI scheme to metamodel descriptor allowing mapping URI scheme to metamodel descriptor
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.metamodel.descs;

import org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelVersionData;

@SuppressWarnings("nls")
public class Test2MM extends AbstractMetaModelDescriptor {

	public static final String ID = "org.eclipse.sphinx.emf.internal.tests.test2mm";

	public static final String NS = "http://testB.sphinx.org";

	public static final String URI_SCHEME = "tr2"; //$NON-NLS-1$

	public static final Test2MM INSTANCE = new Test2MM();

	public Test2MM() {
		this(ID, null);
	}

	protected Test2MM(String identifier, MetaModelVersionData versionData) {
		super(identifier, NS, versionData);
	}

	@Override
	public String getDefaultContentTypeId() {
		return "";
	}

	/*
	 * @see org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor# getCustomURIScheme()
	 */
	@Override
	public String getCustomURIScheme() {
		return URI_SCHEME;
	}
}
