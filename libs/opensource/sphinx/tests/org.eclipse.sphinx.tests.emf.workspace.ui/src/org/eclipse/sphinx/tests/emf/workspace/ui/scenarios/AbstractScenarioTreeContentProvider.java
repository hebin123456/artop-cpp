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
package org.eclipse.sphinx.tests.emf.workspace.ui.scenarios;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.edit.provider.IWrapperItemProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.edit.ExtendedDelegatingWrapperItemProvider;
import org.eclipse.sphinx.emf.edit.TransientItemProvider;

public abstract class AbstractScenarioTreeContentProvider implements ITreeContentProvider {

	private AdapterFactory adapterFactory = new AdapterFactoryImpl();

	protected TransientItemProvider createTransientItemProvider() {
		return new TransientItemProvider(adapterFactory);
	}

	protected IWrapperItemProvider createWrapperItemProvider(Object value) {
		return new ExtendedDelegatingWrapperItemProvider(value, null, null, -1, adapterFactory);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children != null && children.length > 0;
	}

	@Override
	public void dispose() {
	}
}
