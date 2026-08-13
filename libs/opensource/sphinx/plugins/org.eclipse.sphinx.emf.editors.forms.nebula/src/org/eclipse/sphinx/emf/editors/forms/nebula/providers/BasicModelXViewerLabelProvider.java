/**
 * <copyright>
 *
 * Copyright (c) 2012-2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [434230] ParseException when trying to sort BasicXViewerSection for columns displaying Date-typed EAttributes
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.editors.forms.nebula.providers;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class BasicModelXViewerLabelProvider extends XViewerLabelProvider implements ITableFontProvider {

	private final XViewer viewer;
	private final AdapterFactoryItemDelegator itemDelegator;

	public BasicModelXViewerLabelProvider(XViewer viewer) {
		this(viewer, null);
	}

	public BasicModelXViewerLabelProvider(XViewer viewer, AdapterFactoryItemDelegator itemDelegator) {
		super(viewer);
		this.viewer = viewer;
		this.itemDelegator = itemDelegator;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// do nothing
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// do nothing
	}

	@Override
	public void dispose() {
		// do nothing
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public Image getColumnImage(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
		return null;
	}

	@Override
	public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
		IItemPropertyDescriptor propertyDescriptor = findPropertyDescriptorFor(element, xCol.getId());
		if (propertyDescriptor != null) {
			Object propertyValue = propertyDescriptor.getPropertyValue(element);
			return propertyDescriptor.getLabelProvider(element).getText(propertyValue);
		}
		return ""; //$NON-NLS-1$
	}

	protected IItemPropertyDescriptor findPropertyDescriptorFor(Object object, String id) {
		Assert.isNotNull(id);

		if (itemDelegator != null) {
			List<IItemPropertyDescriptor> propertyDescriptors = itemDelegator.getPropertyDescriptors(object);
			if (propertyDescriptors != null) {
				for (IItemPropertyDescriptor propertyDescriptor : propertyDescriptors) {
					if (id.equals(propertyDescriptor.getId(object))) {
						return propertyDescriptor;
					}
				}
			}
		}
		return null;
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
		return viewer.getControl().getFont();
	}
}