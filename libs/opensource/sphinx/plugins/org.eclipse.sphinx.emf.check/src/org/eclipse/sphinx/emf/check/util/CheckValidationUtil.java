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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.sphinx.emf.check.Check;
import org.eclipse.sphinx.emf.check.ICheckValidator;

public class CheckValidationUtil {

	public static Collection<Method> getDeclaredCheckMethods(Class<? extends ICheckValidator> validatorType) {
		Assert.isNotNull(validatorType);

		Set<Class<?>> visitedValidatorTypes = new HashSet<Class<?>>();
		List<Method> result = new ArrayList<Method>();
		collectDeclaredCheckMethods(validatorType, visitedValidatorTypes, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private static void collectDeclaredCheckMethods(Class<? extends ICheckValidator> validatorType, Collection<Class<?>> visitedValidatorTypes,
			Collection<Method> result) {

		if (!visitedValidatorTypes.add(validatorType)) {
			return;
		}

		Method[] methods = validatorType.getDeclaredMethods();
		for (Method method : methods) {
			// Current method being a check method, i.e. a method annotated with @Check and having one parameter?
			Check annotation = method.getAnnotation(Check.class);
			if (annotation != null && method.getParameterTypes().length == 1) {
				result.add(method);
			}
		}
		Class<?> superClass = validatorType.getSuperclass();
		if (superClass != null && ICheckValidator.class.isAssignableFrom(superClass)) {
			collectDeclaredCheckMethods((Class<ICheckValidator>) superClass, visitedValidatorTypes, result);
		}
	}
}
