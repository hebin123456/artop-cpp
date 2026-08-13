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

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;

/**
 * Enhanced implementation of {@link Diagnostician} providing progress monitor support and other things.
 */
public class ExtendedDiagnostician extends Diagnostician {

	public Diagnostic validate(EObject eObject, Map<Object, Object> contextEntries, IProgressMonitor monitor) {
		CheckValidationContextHelper helper = new CheckValidationContextHelper(contextEntries);
		helper.addProgressMonitorOption(monitor);

		return super.validate(eObject, contextEntries);
	}

	@Override
	protected boolean doValidate(EValidator eValidator, EClass eClass, EObject eObject, DiagnosticChain diagnostics,
			java.util.Map<Object, Object> context) {
		CheckValidationContextHelper helper = new CheckValidationContextHelper(context);
		IProgressMonitor monitor = helper.getProgressMonitor();
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		boolean result = super.doValidate(eValidator, eClass, eObject, diagnostics, context);

		monitor.worked(1);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		return result;
	};
}
