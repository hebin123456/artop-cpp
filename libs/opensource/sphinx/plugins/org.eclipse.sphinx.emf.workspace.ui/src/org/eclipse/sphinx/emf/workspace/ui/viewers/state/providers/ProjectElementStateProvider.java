/**
 * <copyright>
 *
 * Copyright (c) 2015-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [480105] Occasional ConcurrentModificationException when re-launching Sphinx on previously used workspace
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IMemento;

public class ProjectElementStateProvider extends AbstractTreeElementStateProvider {

	private IProject project = null;

	public ProjectElementStateProvider(TreeViewer viewer, IMemento memento) {
		super(viewer);

		Assert.isNotNull(memento);
		String name = memento.getString(TreeElementStateProviderFactory.MEMENTO_KEY_NAME);
		if (name != null) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		}
	}

	public ProjectElementStateProvider(TreeViewer viewer, IProject project) {
		super(viewer);
		this.project = project;
	}

	@Override
	public boolean hasUnderlyingModel() {
		return false;
	}

	@Override
	public boolean canUnderlyingModelBeLoaded() {
		return false;
	}

	@Override
	public boolean isUnderlyingModelLoaded() {
		return false;
	}

	@Override
	public void loadUnderlyingModel() {
		// Do nothing
	}

	@Override
	public boolean isStale() {
		if (project != null) {
			return !project.exists();
		}
		return true;
	}

	@Override
	public Object getTreeElement() {
		return project;
	}

	@Override
	public void appendToMemento(IMemento parentMemento) {
		if (project != null) {
			IMemento memento = parentMemento.createChild(TreeElementStateProviderFactory.MEMENTO_TYPE_ELEMENT_PROJECT);
			memento.putString(TreeElementStateProviderFactory.MEMENTO_KEY_NAME, project.getName());
		}
	}

	@Override
	public String toString() {
		return "ProjectElementProvider [project=" + project + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
