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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ElementTypeNameViewerFilter extends ViewerFilter {

	protected boolean accept;
	protected Set<String> typeNames;

	public ElementTypeNameViewerFilter(boolean accept) {
		this(null, accept);
	}

	public ElementTypeNameViewerFilter(Set<String> typeNames, boolean accept) {
		if (typeNames != null) {
			this.typeNames = typeNames;
		} else {
			this.typeNames = new HashSet<String>(Arrays.asList(getTypeNames()));
		}
		this.accept = accept;
	}

	protected String[] getTypeNames() {
		return new String[0];
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element != null) {
			if (typeNames.contains(element.getClass().getSimpleName())) {
				return accept;
			} else {
				if (matchesSupertypeOf(element.getClass())) {
					return accept;
				}
			}
			return !accept;
		}
		return true;
	}

	protected boolean matchesSupertypeOf(Class<?> elementType) {

		// Try to find matching super class
		Set<Class<?>> superTypes = new HashSet<Class<?>>();
		if (!elementType.isInterface()) {
			Class<?> superClass = elementType.getSuperclass();
			if (superClass != null) {
				superTypes.add(superClass);
				if (typeNames.contains(superClass.getSimpleName())) {
					typeNames.add(superClass.getSimpleName());
					return true;
				}
			}
		}

		// Try to find matching interface
		Class<?>[] interfaces = elementType.getInterfaces();
		for (Class<?> interfaze : interfaces) {
			superTypes.add(interfaze);
			if (typeNames.contains(interfaze.getSimpleName())) {
				typeNames.add(interfaze.getSimpleName());
				return true;
			}
		}

		// Try to find matching super type of super class and interfaces
		for (Class<?> superType : superTypes) {
			if (matchesSupertypeOf(superType)) {
				return true;
			}
		}

		return false;
	}
}
