/**
 * <copyright>
 *
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.validation.constraints;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.sphinx.examples.hummingbird20.common.Identifiable;

public class IdentifiableNamesMustNotContainIllegalCharactersConstraint extends AbstractModelConstraint {

	private static final Pattern ILLEGAL_CHARACTERS_PATTERN = Pattern.compile("[\\W]"); //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.validation.AbstractModelConstraint#validate(org.eclipse.emf.validation.IValidationContext)
	 */
	@Override
	public IStatus validate(IValidationContext ctx) {
		// Retrieve target object and see if we have to do anything with it
		EObject targetObject = ctx.getTarget();
		if (isApplicable(targetObject)) {
			Identifiable targetEntity = (Identifiable) targetObject;
			if (!isValid(targetEntity)) {
				return ctx.createFailureStatus(new Object[] { targetEntity.getName() });
			}
		}

		return ctx.createSuccessStatus();
	}

	/**
	 * Tests if given {@link EObject} is applicable to this constraint.
	 *
	 * @param eObject
	 *            The target {@link EObject}.
	 * @return true if given {@link EObject} is applicable to this constraint, false otherwise.
	 */
	private boolean isApplicable(EObject eObject) {
		// The given object must be an entity
		if (!(eObject instanceof Identifiable)) {
			return false;
		}

		// Ignore entities that have no name
		String name = ((Identifiable) eObject).getName();
		if (name == null || name.length() == 0) {
			return false;
		}

		return true;
	}

	/**
	 * The constraint itself.
	 *
	 * @param identifiable
	 *            The target {@link Identifiable}.
	 * @return true if given {@link Identifiable} satisfies this constraint, false otherwise.
	 */
	private boolean isValid(Identifiable identifiable) {
		Assert.isNotNull(identifiable);
		String name = identifiable.getName();
		if (name != null) {
			return !ILLEGAL_CHARACTERS_PATTERN.matcher(name).find();
		}
		return true;
	}
}
