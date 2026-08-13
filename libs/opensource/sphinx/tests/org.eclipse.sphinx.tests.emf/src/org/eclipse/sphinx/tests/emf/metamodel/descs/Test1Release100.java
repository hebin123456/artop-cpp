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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelVersionData;

@SuppressWarnings("nls")
public class Test1Release100 extends Test1MM {

	public static final String ID = "org.eclipse.sphinx.emf.internal.tests.test1mm100";
	private static final String NS_POSTFIX = "1.0.0";
	private static final String EPKG_NS_PATTERN = "1\\.0\\.0/\\d+";
	public static final String NAME = "Test1 Metamodel Release 1.0.0";
	private static final MetaModelVersionData RELEASE_DATA = new MetaModelVersionData(NS_POSTFIX, EPKG_NS_PATTERN, NAME);

	public static final Test1Release100 INSTANCE = new Test1Release100();

	public Test1Release100() {
		super(ID, RELEASE_DATA);
	}

	@Override
	public Collection<IMetaModelDescriptor> getCompatibleResourceVersionDescriptors() {
		List<IMetaModelDescriptor> result = new ArrayList<IMetaModelDescriptor>();
		result.add(Test1Release101.INSTANCE);
		result.add(Test1Release102.INSTANCE);
		return Collections.unmodifiableList(result);
	}

}
