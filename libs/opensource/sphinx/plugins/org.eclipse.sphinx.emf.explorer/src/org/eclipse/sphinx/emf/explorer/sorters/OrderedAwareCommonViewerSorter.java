/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.emf.explorer.sorters;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.resource.ExtendedBasicExtendedMetaData;
import org.eclipse.ui.navigator.CommonViewerSorter;

/**
 * Enhancement of {@link CommonViewerSorter} for model elements. Makes sure that the "ordered" information of the Ecore
 * feature behind the collection containing or referencing the elements to be sorted gets honored. If the "ordered" flag
 * is set the element order in the containing/referencing collection is maintained and no sorting is performed.
 * Otherwise, the elements are sorted using the behavior inherited from {@link CommonViewerSorter}, i.e., based on the
 * defined org.eclipse.ui.navigator.navigatorContent/navigatorContent/commonSorter elements that are available in the
 * set of visible content extensions.
 */
public class OrderedAwareCommonViewerSorter extends NonFinalCommonViewerSorter {

	/*
	 * @see org.eclipse.jface.viewers.TreePathViewerSorter#sort(org.eclipse.jface.viewers.Viewer,
	 * org.eclipse.jface.viewers.TreePath, java.lang.Object[])
	 */
	@Override
	public void sort(Viewer viewer, TreePath parentPath, Object[] elements) {
		if (elements != null && elements.length > 0) {
			// Investigate all elements but not just the first one because they could be combined from different
			// containing/referencing collections
			for (Object element : elements) {
				// Retrieve Ecore feature behind current element if any
				EStructuralFeature feature = getUnderlyingFeature(element);
				if (feature != null) {
					// Is is an ordered feature?
					if (ExtendedBasicExtendedMetaData.INSTANCE.isOrdered(feature)) {
						// Don't sort elements but keep their order as is
						return;
					}
				}
			}
			super.sort(viewer, parentPath, elements);
		}
	}

	protected EStructuralFeature getUnderlyingFeature(Object element) {
		if (element instanceof EObject) {
			EObject eObject = (EObject) element;
			return eObject.eContainingFeature();
		} else if (element instanceof IWrapperItemProvider) {
			IWrapperItemProvider provider = (IWrapperItemProvider) element;
			return provider.getFeature();
		}
		return null;
	}
}
