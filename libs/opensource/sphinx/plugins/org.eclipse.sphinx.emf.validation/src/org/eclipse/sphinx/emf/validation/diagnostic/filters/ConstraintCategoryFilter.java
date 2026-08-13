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
package org.eclipse.sphinx.emf.validation.diagnostic.filters;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.model.Category;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintFilter;

/**
 * A constraint filter that accepts only constraints where at least one of their category ids matches one of the
 * provided category id patterns.
 */
public class ConstraintCategoryFilter implements IConstraintFilter {

	private Set<String> categoryIdPatterns = new HashSet<String>();

	/**
	 * Default constructor.
	 */
	public ConstraintCategoryFilter() {
	}

	/**
	 * Constructor.
	 * 
	 * @param categoryIdPattern
	 *            A regular expression representing the category id pattern that the constraints' category ids must
	 *            match to make it through this filter.
	 */
	public ConstraintCategoryFilter(String categoryIdPattern) {
		Assert.isNotNull(categoryIdPattern);

		categoryIdPatterns.add(categoryIdPattern);
	}

	public void addCategory(String categoryIdPattern) {
		Assert.isNotNull(categoryIdPattern);

		categoryIdPatterns.add(categoryIdPattern);
	}

	/*
	 * @see
	 * org.eclipse.emf.validation.service.IConstraintFilter#accept(org.eclipse.emf.validation.service.IConstraintDescriptor
	 * , org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public boolean accept(IConstraintDescriptor constraint, EObject target) {
		if (constraint != null) {
			for (Category category : constraint.getCategories()) {
				// Iterate over category and its ancestors
				while (category != null) {
					for (String categoryIdPattern : categoryIdPatterns) {
						if (category.getId().matches(categoryIdPattern)) {
							return true;
						}
					}
					// Get the parent category
					category = category.getParent();
				}
			}
		}
		return false;
	}
}
