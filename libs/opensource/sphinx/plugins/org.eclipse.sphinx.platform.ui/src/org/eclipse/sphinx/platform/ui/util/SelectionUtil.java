/**
 * <copyright>
 * 
 * Copyright (c) 2008-2011 See4sys and others.
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
package org.eclipse.sphinx.platform.ui.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

public class SelectionUtil {

	public static IStructuredSelection getStructuredSelection(ISelection selection) {
		return selection instanceof IStructuredSelection ? (IStructuredSelection) selection : StructuredSelection.EMPTY;
	}

	public static boolean containsOnlyProjects(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		for (Object obj : selection.toList()) {
			if (!(obj instanceof IProject)) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasOnlyElementsOfSameType(ISelection selection) {
		return hasOnlyElementsOfSameType(getStructuredSelection(selection));
	}

	@SuppressWarnings("unchecked")
	public static boolean hasOnlyElementsOfSameType(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		List<Object> elements = selection.toList();
		Set<String> classNameSet = new HashSet<String>();
		for (Object obj : elements) {
			classNameSet.add(obj.getClass().getName());
		}
		return classNameSet.size() == 1;
	}
}
