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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui.dialogs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.sphinx.emf.check.catalog.Category;
import org.eclipse.swt.graphics.Image;

public class CategorySelectionLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// Do nothing
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// Do nothing
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Category) {
			return ((Category) element).getLabel();
		}
		return element != null ? element.toString() : ""; //$NON-NLS-1$
	}
}
