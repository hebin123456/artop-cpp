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
package org.eclipse.sphinx.graphiti.workspace.ui.providers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sphinx.emf.explorer.BasicExplorerContentProvider;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.ui.model.WorkbenchContentProvider;

public class ElementTreeContentProvider implements ITreeContentProvider {

	WorkbenchContentProvider workbenchContentProvider;
	BasicExplorerContentProvider emfContentProvider;

	public ElementTreeContentProvider() {
		workbenchContentProvider = new WorkbenchContentProvider();
		emfContentProvider = new BasicExplorerContentProvider() {
			@Override
			protected boolean isTriggerPoint(IResource resource) {
				return WorkspaceEditingDomainUtil.getEditingDomain(resource) != null;
			};
		};
	}

	@Override
	public void dispose() {
		workbenchContentProvider.dispose();
		emfContentProvider.dispose();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		workbenchContentProvider.inputChanged(viewer, oldInput, newInput);
		emfContentProvider.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IContainer || parentElement instanceof IWorkspace) {
			return workbenchContentProvider.getChildren(parentElement);
		}
		return emfContentProvider.getChildren(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IResource || element instanceof IWorkspace) {
			return workbenchContentProvider.getParent(element);
		}
		return emfContentProvider.getParent(element);
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainer || element instanceof IWorkspace) {
			return workbenchContentProvider.hasChildren(element);
		}
		boolean hasChildren = emfContentProvider.hasChildren(element);
		return hasChildren;
	}
}
