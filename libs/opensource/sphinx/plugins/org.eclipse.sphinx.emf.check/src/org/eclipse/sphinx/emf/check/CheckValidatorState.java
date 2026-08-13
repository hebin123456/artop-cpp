/**
 * <copyright>
 *
 * Copyright (c) 2014-2015 itemis and others.
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
package org.eclipse.sphinx.emf.check;

import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.emf.common.util.DiagnosticChain;

public class CheckValidatorState {
	public DiagnosticChain chain = null;
	public Object currentObject = null;
	public Method currentMethod = null;
	public CheckValidationMode checkValidationMode = null;
	public CheckValidationType currentCheckType = null;
	public boolean hasErrors = false;
	public Map<Object, Object> context;
	// TODO Rename to contraintId
	public String constraint = null;
}