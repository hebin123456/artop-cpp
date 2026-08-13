/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [393477] Provider hook for unwrapping elements before letting BasicTabbedPropertySheetTitleProvider retrieve text or image for them
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.common.ui.providers;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.ui.properties.BasicTabbedPropertySheetTitleProvider;

public class AppearanceExampleTabbedPropertySheetTitleProvider extends BasicTabbedPropertySheetTitleProvider {

	private final TypeNameLabelDecorator typeNameLabelDecorator;

	public AppearanceExampleTabbedPropertySheetTitleProvider() {
		typeNameLabelDecorator = new TypeNameLabelDecorator();
	}

	@Override
	public String getText(Object element) {
		String text = super.getText(element);

		// Decorate label with element type name if possible and selection is no multi selection
		if (element instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) element;
			if (structuredSelection.size() == 1) {
				Object unwrapped = unwrap(structuredSelection.getFirstElement());
				return typeNameLabelDecorator.decorateText(text, unwrapped);
			}
		}

		return text;
	}
}
