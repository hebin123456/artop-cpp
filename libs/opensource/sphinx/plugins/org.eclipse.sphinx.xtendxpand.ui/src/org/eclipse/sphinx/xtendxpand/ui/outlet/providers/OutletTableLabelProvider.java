/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
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
package org.eclipse.sphinx.xtendxpand.ui.outlet.providers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;
import org.eclipse.swt.graphics.Image;
import org.eclipse.xpand2.output.Outlet;

public class OutletTableLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Outlet) {
			ExtendedOutlet outlet = (ExtendedOutlet) element;
			switch (columnIndex) {
			case 0:
				return outlet.getName() == null ? "<default>" : outlet.getName();//$NON-NLS-1$
			case 1:
				return outlet.getPathExpression();
			case 2:
				return Boolean.toString(outlet.isProtectedRegion());
			default:
				return ""; //$NON-NLS-1$
			}
		}
		return element.toString();
	}

}
