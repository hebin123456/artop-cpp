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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.ui.IMemento;

public class FileElementStateProvider extends AbstractTreeElementStateProvider {

	private IFile file = null;

	public FileElementStateProvider(TreeViewer viewer, IMemento memento) {
		super(viewer);

		Assert.isNotNull(memento);
		String pathAsString = memento.getString(TreeElementStateProviderFactory.MEMENTO_KEY_PATH);
		if (pathAsString != null) {
			file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(pathAsString));
		}
	}

	public FileElementStateProvider(TreeViewer viewer, IFile file) {
		super(viewer);
		this.file = file;
	}

	@Override
	public boolean hasUnderlyingModel() {
		return ModelDescriptorRegistry.INSTANCE.isModelFile(file);
	}

	@Override
	public boolean canUnderlyingModelBeLoaded() {
		return !isStale();
	}

	@Override
	public boolean isUnderlyingModelLoaded() {
		return EcorePlatformUtil.isFileLoaded(file);
	}

	@Override
	public void loadUnderlyingModel() {
		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(file);
		if (modelDescriptor != null) {
			// Request asynchronous loading of model behind given workspace file
			ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);
		}
	}

	@Override
	public boolean isStale() {
		if (file != null) {
			return !file.exists();
		}
		return true;
	}

	@Override
	public Object getTreeElement() {
		return file;
	}

	@Override
	public void appendToMemento(IMemento parentMemento) {
		if (file != null) {
			IMemento memento = parentMemento.createChild(TreeElementStateProviderFactory.MEMENTO_TYPE_ELEMENT_FILE);
			memento.putString(TreeElementStateProviderFactory.MEMENTO_KEY_PATH, file.getFullPath().toString());
		}
	}

	@Override
	public String toString() {
		return "FileElementProvider [file=" + file.getFullPath() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
