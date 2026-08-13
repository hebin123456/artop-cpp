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
 *     itemis - [463895] org.eclipse.sphinx.emf.check.AbstractCheckValidator.validate(EClass, EObject, DiagnosticChain, Map<Object, Object>) throws NPE
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.sphinx.emf.check.Check;
import org.eclipse.sphinx.emf.check.CheckValidatorRegistry;
import org.eclipse.sphinx.emf.check.CheckValidatorState;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.emf.check.catalog.Catalog;
import org.eclipse.sphinx.emf.check.catalog.Category;

public class CheckMethodWrapper {

	private Method method;
	private Check checkAnnotation;
	private ICheckValidator validator;
	private CheckValidatorRegistry checkValidatorRegistry;

	public CheckMethodWrapper(ICheckValidator validator, Method method, CheckValidatorRegistry checkValidatorRegistry) {
		Assert.isNotNull(validator);
		Assert.isNotNull(method);
		Assert.isNotNull(checkValidatorRegistry);
		Assert.isNotNull(method.getAnnotation(Check.class));

		this.validator = validator;
		this.method = method;
		checkAnnotation = method.getAnnotation(Check.class);
		this.checkValidatorRegistry = checkValidatorRegistry;
	}

	public ICheckValidator getValidator() {
		return validator;
	}

	public Method getMethod() {
		return method;
	}

	public String getAnnotatedConstraint() {
		return checkAnnotation.constraint();
	}

	public Set<String> getAnnotatedCategories() {
		String[] categories = checkAnnotation.categories();
		if (categories.length > 0) {
			// Ignore isolate empty categories
			if (categories.length > 1 || !categories[0].isEmpty()) {
				return new HashSet<String>(Arrays.asList(categories));
			}
		}
		return Collections.emptySet();
	}

	private boolean isOtherCategorySelected(Set<String> selectedCategories) {
		Assert.isNotNull(selectedCategories);

		for (String categoryId : selectedCategories) {
			if (categoryId.equals(ICheckValidator.OPTION_CATEGORIES_OTHER_ID)) {
				return true;
			}
		}
		return false;
	}

	public void invoke(CheckValidatorState state, Set<String> selectedCategories) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		if (validator.getState().get() != null && validator.getState().get() != state) {
			throw new IllegalStateException("State is already assigned."); //$NON-NLS-1$
		}
		boolean wasNull = validator.getState().get() == null;
		if (wasNull) {
			validator.getState().set(state);
		}
		try {
			if (!state.checkValidationMode.shouldCheck(checkAnnotation.value())) {
				return;
			}

			if (selectedCategories.isEmpty()) {
				// Invoke the validation using all existing categories if no categories are selected
				invokeInternal(state);
			} else {
				Set<String> categories = new HashSet<String>();
				categories.addAll(selectedCategories);
				Set<String> annotatedCategories = getAnnotatedCategories();
				Catalog catalog = checkValidatorRegistry.getCheckCatalog(validator);

				// If no categories are specified in the check method's @Check annotation, invoke the check method on
				// all categories as per the underlying constraint in the check catalog; otherwise invoke the check
				// method on the intersection of categories provided by the user, and the categories defined in the
				// check catalog.

				// Case 1: Only @Check annotation without constraint
				if (getAnnotatedConstraint().isEmpty()) {
					if (isOtherCategorySelected(selectedCategories)) {
						invokeInternal(state);
					}
				}

				// Case 2: @Check annotation with a constraint and without any category
				else if (annotatedCategories.isEmpty()) {
					if (catalog != null && !catalog.getCategories().isEmpty()) {
						retainAll(categories, catalog.getCategories());
						// Go ahead if scope is not empty
						if (!categories.isEmpty()) {
							invokeInternal(state);
						}
					}
				}

				// Case 3: @Check annotation with a constraint and categories
				else {
					// Make intersection with annotated categories
					categories.retainAll(annotatedCategories);

					// Make intersection with categories associated with this validator in check catalog
					if (catalog != null && !catalog.getCategories().isEmpty()) {
						retainAll(categories, catalog.getCategories());
					}

					// Go ahead if scope is not empty
					if (!categories.isEmpty()) {
						invokeInternal(state);
					}
				}
			}
		} finally {
			if (wasNull) {
				validator.getState().set(null);
			}
		}
	}

	protected void invokeInternal(CheckValidatorState state) throws IllegalAccessException, InvocationTargetException {
		state.currentMethod = method;
		state.currentCheckType = checkAnnotation.value();
		state.constraint = getAnnotatedConstraint();
		method.setAccessible(true);
		method.invoke(validator, state.currentObject);
	}

	private void retainAll(Set<String> categories, EList<Category> categoryList) {
		Set<String> categoryIDs = new HashSet<String>();
		for (Category category : categoryList) {
			categoryIDs.add(category.getId());
		}
		categories.retainAll(categoryIDs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CheckMethodWrapper other = (CheckMethodWrapper) obj;
		if (!method.equals(other.method)) {
			return false;
		}
		if (!validator.equals(other.validator)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + method.hashCode();
		result = prime * result + validator.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "CheckMethodWrapper [method=" + method + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
