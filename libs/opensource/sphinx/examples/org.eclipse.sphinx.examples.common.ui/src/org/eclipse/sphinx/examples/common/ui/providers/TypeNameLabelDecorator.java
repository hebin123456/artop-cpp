/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.common.ui.providers;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.swt.graphics.Image;

public class TypeNameLabelDecorator extends BaseLabelProvider implements ILabelDecorator {

	public static String getTypeName(Object element) {
		if (element instanceof EObject) {
			return ((EObject) element).eClass().getName();
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		return null;
	}

	@Override
	public String decorateText(String text, Object element) {
		if (text != null && text.length() > 0) {
			String typeName = getTypeName(element);
			if (typeName.length() > 0) {
				return text + " [" + typeName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return text;
	}
}
