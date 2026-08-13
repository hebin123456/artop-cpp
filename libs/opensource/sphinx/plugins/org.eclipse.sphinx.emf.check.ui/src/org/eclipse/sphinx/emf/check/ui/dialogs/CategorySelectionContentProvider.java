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
 *     itemis - [458976] Validators are not singleton when they implement checks for different EPackages
 *     itemis - [458982] Improve check validation user experience
 *     itemis - [473260] Progress indication of check framework
 *     itemis - [473261] Check Validation: Cancel button unresponsive
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.check.CheckValidatorRegistry;
import org.eclipse.sphinx.emf.check.ICheckValidator;
import org.eclipse.sphinx.emf.check.catalog.Catalog;
import org.eclipse.sphinx.emf.check.catalog.Category;
import org.eclipse.sphinx.emf.check.catalog.CheckCatalogFactory;
import org.eclipse.sphinx.emf.check.ui.internal.messages.Messages;

public class CategorySelectionContentProvider implements IStructuredContentProvider {

	private Category otherCategory;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing
	}

	@Override
	public Object[] getElements(Object inputElement) {
		List<Category> elements = new ArrayList<Category>();

		Collection<Catalog> checkCatalogs = CheckValidatorRegistry.INSTANCE.getCheckCatalogs();
		Map<String, Category> categories = new HashMap<String, Category>();
		for (Catalog catalog : checkCatalogs) {
			for (Category category : catalog.getCategories()) {
				categories.put(category.getId(), category);
			}
		}
		elements.addAll(categories.values());

		// Add Other Category
		elements.add(getOtherCategory());

		return elements.toArray(new Category[elements.size()]);
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	protected Category getOtherCategory() {
		if (otherCategory == null) {
			otherCategory = createCategory(ICheckValidator.OPTION_CATEGORIES_OTHER_ID, Messages.other_category_label, Messages.other_category_desc);
		}
		return otherCategory;
	}

	private Category createCategory(String id, String label, String desc) {
		Category category = CheckCatalogFactory.eINSTANCE.createCategory();
		category.setId(id);
		category.setLabel(label);
		category.setDescription(desc);
		return category;
	}
}
