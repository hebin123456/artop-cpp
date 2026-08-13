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
package org.eclipse.sphinx.tests.emf.check.internal.mocks;

import org.eclipse.sphinx.tests.emf.check.internal.TestableHummingbird20ConnectionsCheckValidator;
import org.eclipse.sphinx.tests.emf.check.internal.TestableHummingbird20NamingAndValuesCheckValidator;
import org.eclipse.sphinx.tests.emf.check.internal.TestableSimpleHummingbird20NamingCheckValidator;

public class ValidatorContribution {

	public static final ValidatorContribution testableSimpleHummingbird20NamingCheckValidator = new ValidatorContribution(
			TestableSimpleHummingbird20NamingCheckValidator.class.getName(), null);

	public static final ValidatorContribution testableHummingbird20NamingAndValuesCheckValidator = new ValidatorContribution(
			TestableHummingbird20NamingAndValuesCheckValidator.class.getName(), "resources/input/Hummingbird20.checkcatalog"); //$NON-NLS-1$

	public static final ValidatorContribution testableHummingbird20ConnectionsCheckValidator = new ValidatorContribution(
			TestableHummingbird20ConnectionsCheckValidator.class.getName(),
			"platform:/plugin/org.eclipse.sphinx.examples.hummingbird20.check/model/Hummingbird20.checkcatalog"); //$NON-NLS-1$

	private String validatorClass;

	private String catalog;

	public ValidatorContribution(String validatorClass, String catalog) {
		this.validatorClass = validatorClass;
		this.catalog = catalog;
	}

	public String getValidatorClass() {
		return validatorClass;
	}

	public String getCatalog() {
		return catalog;
	}
}
