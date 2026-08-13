/**
 * <copyright>
 *
 * Copyright (c) 2008-2010 BMW Car IT and others.
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
package org.eclipse.sphinx.tests.emf.metamodel.descs;

import org.eclipse.sphinx.emf.metamodel.MetaModelVersionData;

@SuppressWarnings("nls")
public class Test1Release200 extends Test1MM {

	public static final String ID = "org.eclipse.sphinx.emf.internal.tests.test1mm200";
	private static final String NS_POSTFIX = "2.0.0";
	private static final String EPKG_NS_PATTERN = "2\\.0\\.0/\\d+";
	public static final String NAME = "Test1 Metamodel Release 2.0.0";
	private static final MetaModelVersionData RELEASE_DATA = new MetaModelVersionData(NS_POSTFIX, EPKG_NS_PATTERN, NAME);

	public static final Test1Release200 INSTANCE = new Test1Release200();

	public Test1Release200() {
		super(ID, RELEASE_DATA);
	}

}
