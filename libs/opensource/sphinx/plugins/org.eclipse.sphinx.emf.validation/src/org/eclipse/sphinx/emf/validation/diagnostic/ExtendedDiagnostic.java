/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.validation.diagnostic;

import java.util.List;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.validation.model.IModelConstraint;

public class ExtendedDiagnostic extends BasicDiagnostic {

	/**
	 * ID of the violated constraint for which a diagnostic is being created.
	 */
	private String constraintId;

	public ExtendedDiagnostic() {
		super();
	}

	public ExtendedDiagnostic(String source, int code, String message, Object[] data) {
		super(source, code, message, data);
	}

	public ExtendedDiagnostic(int severity, String source, IModelConstraint constraint, int code, String message, Object[] data) {
		super(severity, source, code, message, data);
		constraintId = constraint.getDescriptor().getId();
	}

	public ExtendedDiagnostic(String source, int code, List<? extends Diagnostic> children, String message, Object[] data) {
		super(source, code, children, message, data);
	}

	public String getConstraintId() {
		return constraintId;
	}

}
