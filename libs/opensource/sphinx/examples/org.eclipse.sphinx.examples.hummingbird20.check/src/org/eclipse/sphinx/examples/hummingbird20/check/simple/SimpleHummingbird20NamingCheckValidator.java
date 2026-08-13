/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.check.simple;

import org.eclipse.sphinx.emf.check.AbstractCheckValidator;
import org.eclipse.sphinx.emf.check.Check;
import org.eclipse.sphinx.emf.check.CheckValidatorRegistry;
import org.eclipse.sphinx.examples.hummingbird20.common.Common20Package;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;

/**
 * An example of check validator which does <em>not</em> make use of a catalog. Severities, error messages, and
 * additional information to be displayed in the problems view are specified inside the validator using explicit error,
 * warning, and info methods.
 *
 * @see org.eclipse.sphinx.emf.check.AbstractCheckValidator
 */
public class SimpleHummingbird20NamingCheckValidator extends AbstractCheckValidator {

	private final static String INVALID_NAME_PREFIX = "_"; //$NON-NLS-1$
	public static final String ISSUE_MSG = "The application name has an invalid prefix"; //$NON-NLS-1$

	public SimpleHummingbird20NamingCheckValidator() {

	}

	public SimpleHummingbird20NamingCheckValidator(CheckValidatorRegistry checkValidatorRegistry) {
		super(checkValidatorRegistry);
	}

	@Check
	void checkApplicationName(Application application) {
		String name = application.getName();
		if (name != null && name.startsWith(INVALID_NAME_PREFIX)) {
			// error(ISSUE_MSG, application, Common20Package.Literals.IDENTIFIABLE__NAME);
			warning(ISSUE_MSG, application, Common20Package.Literals.IDENTIFIABLE__NAME);
			// info(ISSUE_MSG, application, Common20Package.Literals.IDENTIFIABLE__NAME);
		}
	}
}
