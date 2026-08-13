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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.AbstractModelConstraint;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Interface;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;

public class InterfaceNamesMustBeUniqueConstraint extends AbstractModelConstraint {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.emf.validation.AbstractModelConstraint#validate(org.eclipse.emf.validation.IValidationContext)
	 */
	@Override
	public IStatus validate(IValidationContext ctx) {
		// Retrieve target object and see if we have to do anything with it
		EObject targetObject = ctx.getTarget();
		if (isApplicable(targetObject)) {
			Interface targetClass = (Interface) targetObject;
			if (!isValid(targetClass)) {
				return ctx.createFailureStatus(new Object[] { targetClass.getName() });
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
		// The given object must be a class
		if (!(eObject instanceof Interface)) {
			return false;
		}

		// Ignore classes that have no name
		String name = ((Interface) eObject).getName();
		if (name == null || name.length() == 0) {
			return false;
		}

		return true;
	}

	/**
	 * The constraint itself.
	 * 
	 * @param entity
	 *            The target {@link Interface}.
	 * @return true if given {@link Interface} satisfies this constraint, false otherwise.
	 */
	private boolean isValid(Interface interfaze) {
		Assert.isNotNull(interfaze);

		List<Interface> interfaces = Collections.emptyList();
		EObject container = interfaze.eContainer();
		if (container instanceof Platform) {
			interfaces = ((Platform) container).getInterfaces();
		}

		for (Interface otherInterface : interfaces) {
			if (otherInterface == interfaze) {
				continue;
			}
			if (interfaze.getName().equals(otherInterface.getName())) {
				return false;
			}
		}

		return true;
	}
}
