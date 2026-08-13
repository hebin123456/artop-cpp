/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - added support for problem markers on model objects (rather than 
 *               only on workspace resources). Unfortunately, there was no other 
 *               choice than copying the whole code from 
 *               org.eclipse.ui.views.markers.internal for that purpose because 
 *               many of the relevant classes, methods, and fields are private or
 *               package private.
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * The TableViewLabelProvider is the content provider for marker views.
 */
public class TableViewLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

	IField[] fields;

	/**
	 * Create a neew instance of the receiver.
	 * 
	 * @param fields
	 */
	public TableViewLabelProvider(IField[] fields) {
		this.fields = fields;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (fields == null || columnIndex < 0 || columnIndex >= fields.length) {
			return null;
		}
		return fields[columnIndex].getImage(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (fields == null || columnIndex < 0 || columnIndex >= fields.length) {
			return null;
		}
		return fields[columnIndex].getValue(element);
	}

	@Override
	public Font getFont(Object element) {
		MarkerNode node = (MarkerNode) element;
		if (node.isConcrete()) {
			return null;
		}
		return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	}

}
