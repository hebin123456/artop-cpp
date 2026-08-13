/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
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
package org.eclipse.sphinx.emf.ui.viewers.filters;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ElementTypeViewerFilter extends ViewerFilter {

	protected boolean accept;
	protected Set<Class<?>> types = new HashSet<Class<?>>();

	public ElementTypeViewerFilter(final Set<Class<?>> types, final boolean accept) {
		this.types = types;
		this.accept = accept;
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element != null) {
			Class<?> elementType = element.getClass();
			if (types.contains(elementType)) {
				return accept;
			} else {
				if (matchesSupertype(elementType)) {
					return accept;
				}
			}
			return !accept;
		}
		return true;
	}

	protected boolean matchesSupertype(Class<?> elementType) {
		for (Class<?> type : types) {
			if (type.isAssignableFrom(elementType)) {
				types.add(elementType);
				return true;
			}
		}
		return false;
	}
}
