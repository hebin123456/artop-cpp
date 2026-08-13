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
package org.eclipse.sphinx.tests.emf.metamodel;

import junit.framework.TestCase;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.sphinx.emf.metamodel.MetaModelVersionData;

@SuppressWarnings("nls")
public class MetaModelVersionDataTest extends TestCase {

	private static final String NS_POSTFIX100 = "1.0.0";
	private static final String PATTERN100 = "1\\.0\\.0/\\d+";
	private static final String NAME100 = "Release 1.0.0";

	private static final String NS_POSTFIX200 = "2.0.0";
	private static final String PATTERN200 = "2\\.0\\.0/\\d+";
	private static final String NAME200 = "Release 2.0.0";

	private MetaModelVersionData fReleaseData100;
	private MetaModelVersionData fReleaseData200;

	@Override
	protected void setUp() throws Exception {
		fReleaseData100 = new MetaModelVersionData(NS_POSTFIX100, PATTERN100, NAME100);
		fReleaseData200 = new MetaModelVersionData(NS_POSTFIX200, PATTERN200, NAME200);
	}

	public void testCreation() {
		try {
			new MetaModelVersionData(null, PATTERN100, "Release 1.0.0");
		} catch (AssertionFailedException afe) {
			fail("No Exception expected as the nsPostfix may be set to null.");
		}
		try {
			new MetaModelVersionData("", PATTERN100, "Release 1.0.0");
		} catch (AssertionFailedException afe) {
			fail("No Exception expected as the nsPostfix may be empty.");
		}
		try {
			new MetaModelVersionData(NS_POSTFIX100, PATTERN100, null);
			fail("AssertionFailedException expected as the name may not be set to null.");
		} catch (AssertionFailedException afe) {
		}
		try {
			new MetaModelVersionData(NS_POSTFIX100, PATTERN100, NAME100);
		} catch (AssertionFailedException afe) {
			fail("No Exception expected as the correct parameters are provided.");
		}
	}

	public void testGetNsPostfix() {
		assertEquals(NS_POSTFIX100, fReleaseData100.getNsPostfix());
		assertEquals(NS_POSTFIX200, fReleaseData200.getNsPostfix());
	}

	public void testGetLabel() {
		assertEquals(NAME100, fReleaseData100.getName());
		assertEquals(NAME200, fReleaseData200.getName());
	}
}
