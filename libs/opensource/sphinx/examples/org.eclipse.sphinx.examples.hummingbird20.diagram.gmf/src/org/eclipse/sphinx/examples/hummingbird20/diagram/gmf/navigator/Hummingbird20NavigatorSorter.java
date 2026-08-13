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
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.navigator;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part.Hummingbird20VisualIDRegistry;

/**
 * @generated
 */
public class Hummingbird20NavigatorSorter extends ViewerSorter {

	/**
	 * @generated
	 */
	private static final int GROUP_CATEGORY = 4003;

	/**
	 * @generated
	 */
	@Override
	public int category(Object element) {
		if (element instanceof Hummingbird20NavigatorItem) {
			Hummingbird20NavigatorItem item = (Hummingbird20NavigatorItem) element;
			return Hummingbird20VisualIDRegistry.getVisualID(item.getView());
		}
		return GROUP_CATEGORY;
	}

}
