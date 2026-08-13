/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.sheet;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator.Hummingbird20NavigatorGroup;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.providers.Hummingbird20ElementTypes;
import org.eclipse.swt.graphics.Image;

/**
 * @generated
 */
public class Hummingbird20SheetLabelProvider extends BaseLabelProvider implements ILabelProvider {

	/**
	 * @generated
	 */
	@Override
	public String getText(Object element) {
		element = unwrap(element);
		if (element instanceof Hummingbird20NavigatorGroup) {
			return ((Hummingbird20NavigatorGroup) element).getGroupName();
		}
		IElementType etype = getElementType(getView(element));
		return etype == null ? "" : etype.getDisplayName();
	}

	/**
	 * @generated
	 */
	@Override
	public Image getImage(Object element) {
		IElementType etype = getElementType(getView(unwrap(element)));
		return etype == null ? null : Hummingbird20ElementTypes.getImage(etype);
	}

	/**
	 * @generated
	 */
	private Object unwrap(Object element) {
		if (element instanceof IStructuredSelection) {
			return ((IStructuredSelection) element).getFirstElement();
		}
		return element;
	}

	/**
	 * @generated
	 */
	private View getView(Object element) {
		if (element instanceof View) {
			return (View) element;
		}
		if (element instanceof IAdaptable) {
			return (View) ((IAdaptable) element).getAdapter(View.class);
		}
		return null;
	}

	/**
	 * @generated
	 */
	private IElementType getElementType(View view) {
		// For intermediate views climb up the containment hierarchy to find the one associated with an element type.
		while (view != null) {
			int vid = Hummingbird20VisualIDRegistry.getVisualID(view);
			IElementType etype = Hummingbird20ElementTypes.getElementType(vid);
			if (etype != null) {
				return etype;
			}
			view = view.eContainer() instanceof View ? (View) view.eContainer() : null;
		}
		return null;
	}

}
