/**
 * <copyright>
 *
 * Copyright (c) 2008-2015 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [480135] Introduce metamodel and view content agnostic problem decorator for model elements
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;
import org.eclipse.sphinx.emf.util.EObjectUtil;

/**
 * A property tester for various properties of EMF Objects.
 */
public class EMFObjectPropertyTester extends PropertyTester {

	/**
	 * A property testing that an object is an {@link EObject} of the specified type or a subtype of the same.
	 */
	private static final String INSTANCE_OF = "instanceOf"; //$NON-NLS-1$

	/**
	 * A property testing that an object is an {@link EObject} whose qualified class name matches the specified regular
	 * expression.
	 */
	private static final String CLASS_NAME_MATCHES = "classNameMatches"; //$NON-NLS-1$

	/**
	 * A property testing that an object is an {@link IWrapperItemProvider} with an
	 * {@link IWrapperItemProvider#getOwner() owner} whose qualified class name matches the specified regular
	 * expression.
	 *
	 * @deprecated Use {@link #VALUE_CLASS_NAME_MATCHES} instead.
	 */
	@Deprecated
	private static final String OWNER_CLASS_NAME_MATCHES = "ownerClassNameMatches"; //$NON-NLS-1$

	/**
	 * A property testing that an object is an {@link IWrapperItemProvider} with a
	 * {@link IWrapperItemProvider#getValue() value} whose qualified class name matches the specified regular
	 * expression.
	 */
	private static final String VALUE_CLASS_NAME_MATCHES = "valueClassNameMatches"; //$NON-NLS-1$

	/**
	 * A property testing that an object is an {@link TransientItemProvider} with a
	 * {@link TransientItemProvider#getTarget() parent} whose qualified class name matches the specified regular
	 * expression.
	 */
	private static final String PARENT_CLASS_NAME_MATCHES = "parentClassNameMatches";//$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// parentClassNameMatches property
		if (receiver instanceof TransientItemProvider && PARENT_CLASS_NAME_MATCHES.equals(property)) {
			TransientItemProvider provider = (TransientItemProvider) receiver;
			Notifier target = provider.getTarget();
			if (target != null) {
				return target.getClass().getName().matches(expectedValue.toString());
			}
			return false;
		}

		// valueClassNameMatches property
		if (receiver instanceof IWrapperItemProvider && (VALUE_CLASS_NAME_MATCHES.equals(property) || OWNER_CLASS_NAME_MATCHES.equals(property))) {
			// Retrieve the given IWrapperItemProvider's value
			Object value = AdapterFactoryEditingDomain.unwrap(receiver);
			if (value != null) {
				// If value is a TransientItemProvider, i.e., an intermediate category node, we must look at it's parent
				if (value instanceof TransientItemProvider) {
					TransientItemProvider provider = (TransientItemProvider) value;
					Notifier target = provider.getTarget();
					if (target != null) {
						// Test if parent class name matches
						return target.getClass().getName().matches(expectedValue.toString());
					}
				}

				// Test if value class name matches
				return value.getClass().getName().matches(expectedValue.toString());
			}
			return false;
		}

		// Unwrap wrapped model objects
		receiver = AdapterFactoryEditingDomain.unwrap(receiver);

		// instanceOf property
		if (receiver instanceof EObject && INSTANCE_OF.equals(property)) {
			EObject eObject = (EObject) receiver;
			return EObjectUtil.isAssignableFrom(eObject.eClass(), expectedValue.toString());
		}

		// classNameMatches property
		else if (receiver instanceof EObject && CLASS_NAME_MATCHES.equals(property)) {
			EObject eObject = (EObject) receiver;
			String instanceClassName = eObject.eClass().getInstanceClassName();
			if (instanceClassName != null) {
				return instanceClassName.matches(expectedValue.toString());
			}
		}

		return false;
	}
}
