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
public class Test2Release100 extends Test2MM {

	private static final String ID = "org.eclipse.sphinx.emf.internal.tests.test2mm100";
	private static final String NS_POSTFIX = "1.0.0";
	private static final String EPKG_NS_PATTERN = "1\\.0\\.0/\\d+";
	private static final String NAME = "Test2 Metamodel Release 1.0.0";
	private static final MetaModelVersionData RELEASE_DATA = new MetaModelVersionData(NS_POSTFIX, EPKG_NS_PATTERN, NAME);

	public static final Test2Release100 INSTANCE = new Test2Release100();

	public Test2Release100() {
		super(ID, RELEASE_DATA);
	}

}
