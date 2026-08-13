/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.tests.emf.check;

import java.util.List;

import org.eclipse.sphinx.emf.check.internal.CheckMethodWrapper;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Connection;
import org.eclipse.sphinx.tests.emf.check.internal.TestableHummingbird20ConnectionsCheckValidator;
import org.junit.Assert;
import org.junit.Test;

public class Hummingbird20ConnectionsCheckValidatorTest {

	@Test
	public void testInitCheckMethods() {
		TestableHummingbird20ConnectionsCheckValidator validator = new TestableHummingbird20ConnectionsCheckValidator();
		validator.initCheckMethods();

		List<CheckMethodWrapper> checkMethodsForConnection = validator.getCheckMethodsForModelObjectType(Connection.class);
		Assert.assertEquals(1, checkMethodsForConnection.size());
	}
}
