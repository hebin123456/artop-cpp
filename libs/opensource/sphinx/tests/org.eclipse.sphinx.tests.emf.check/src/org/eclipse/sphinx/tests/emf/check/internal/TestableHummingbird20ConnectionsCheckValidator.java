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
package org.eclipse.sphinx.tests.emf.check.internal;

import java.util.List;

import org.eclipse.sphinx.emf.check.internal.CheckMethodWrapper;
import org.eclipse.sphinx.examples.hummingbird20.check.withcatalog.Hummingbird20ConnectionsCheckValidator;

public class TestableHummingbird20ConnectionsCheckValidator extends Hummingbird20ConnectionsCheckValidator {

	public TestableHummingbird20ConnectionsCheckValidator() {
		super(TestableCheckValidatorRegistry.INSTANCE);
	}

	@Override
	public void initCheckMethods() {
		super.initCheckMethods();
	}

	@Override
	public List<CheckMethodWrapper> getCheckMethodsForModelObjectType(Class<?> modelObjectType) {
		return super.getCheckMethodsForModelObjectType(modelObjectType);
	}
}
