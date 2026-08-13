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
package org.eclipse.sphinx.emf.check.util;

import java.lang.reflect.Method;

public class SourceLocation {

	private Class<?> checkValidator;

	private Method checkMethod;

	private String constraintId;

	public SourceLocation(Class<?> checkValidator, Method checkMethod, String constraintId) {
		this.checkValidator = checkValidator;
		this.checkMethod = checkMethod;
		this.constraintId = constraintId;
	}

	public Class<?> getCheckValidator() {
		return checkValidator;
	}

	public void setCheckValidator(Class<?> checkValidator) {
		this.checkValidator = checkValidator;
	}

	public Method getCheckMethod() {
		return checkMethod;
	}

	public void setCheckMethod(Method checkMethod) {
		this.checkMethod = checkMethod;
	}

	public String getConstraintId() {
		return constraintId;
	}

	public void setConstraintId(String constraintId) {
		this.constraintId = constraintId;
	}
}
