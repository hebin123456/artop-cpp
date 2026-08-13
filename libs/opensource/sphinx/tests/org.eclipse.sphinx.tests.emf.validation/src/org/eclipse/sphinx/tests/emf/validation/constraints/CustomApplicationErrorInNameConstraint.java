/**
 * <copyright>
 *
 * Copyright (c) 2021 Elektrobit and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Elektrobit - initial implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.validation.constraints;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.impl.ApplicationImpl;

public class CustomApplicationErrorInNameConstraint extends AbstractModelConstraint {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.validation.AbstractModelConstraint#validate(org.eclipse.emf.validation.IValidationContext)
	 */
	@SuppressWarnings("nls")
	@Override
	public IStatus validate(IValidationContext ctx) {

		EObject targetObject = ctx.getTarget();
		if (targetObject instanceof ApplicationImpl) {
			EStructuralFeature eStructuralFeature = targetObject.eClass().getEStructuralFeature("name");

			if (targetObject.eGet(eStructuralFeature).equals("CustomApplicationError")) {
				return ctx.createFailureStatus(new Object[] { targetObject.eClass().getName() });
			}

		}

		return ctx.createSuccessStatus();
	}
}
