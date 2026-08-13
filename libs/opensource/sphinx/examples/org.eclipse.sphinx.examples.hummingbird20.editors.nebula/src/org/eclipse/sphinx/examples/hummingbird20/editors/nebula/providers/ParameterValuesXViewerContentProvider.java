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

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;

public class ParameterValuesXViewerContentProvider implements ITreeContentProvider {

	public ParameterValuesXViewerContentProvider() {
		super();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof String) {
			return new Object[] { inputElement };
		}
		return getChildren(inputElement);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Object[]) {
			return (Object[]) parentElement;
		}
		if (parentElement instanceof Collection) {
			return ((Collection) parentElement).toArray();
		}
		if (parentElement instanceof Component) {
			return ((Component) parentElement).getParameterValues().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public void dispose() {
		// do nothing
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}
}
