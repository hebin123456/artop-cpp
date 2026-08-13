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
package org.eclipse.sphinx.examples.hummingbird20.editors.nebula.providers;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerLabelProvider;
import org.eclipse.nebula.widgets.xviewer.core.model.XViewerColumn;
import org.eclipse.sphinx.examples.hummingbird20.editors.nebula.factory.ParameterValuesXViewerFactory;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.ParameterValue;
import org.eclipse.swt.graphics.Image;

public class ParameterValuesXViewerLabelProvider extends XViewerLabelProvider {

	public ParameterValuesXViewerLabelProvider(XViewer viewer) {
		super(viewer);
	}

	@Override
	public String getColumnText(Object element, XViewerColumn xCol, int columnIndex) throws Exception {
		if (element instanceof ParameterValue) {
			if (xCol.getName().equals(ParameterValuesXViewerFactory.PARAMETER_NAME_COLUMN_NAME)) {
				return ((ParameterValue) element).getName();
			}
			if (xCol.getName().equals(ParameterValuesXViewerFactory.PARAMETER_VALUE_COLUMN_NAME)) {
				return ((ParameterValue) element).getValue();
			}
		}

		return ""; //$NON-NLS-1$
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
}
