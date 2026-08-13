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
 *     itemis - [501108] The tree viewer state restoration upon Eclipse startup not working for model elements being added after the loading of the underlying has been finished
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.viewers.state.providers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.loading.ModelLoadManager;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.util.ModelOperationRunner;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.ui.IMemento;

public class EObjectElementStateProvider extends AbstractTreeElementStateProvider {

	private URI uri = null;
	private EObject eObject = null;

	public EObjectElementStateProvider(TreeViewer viewer, IMemento memento) {
		super(viewer);

		Assert.isNotNull(memento);
		String uriAsString = memento.getString(TreeElementStateProviderFactory.MEMENTO_KEY_URI);
		if (uriAsString != null) {
			uri = URI.createURI(uriAsString, true);
		}
	}

	public EObjectElementStateProvider(TreeViewer viewer, URI uri) {
		super(viewer);
		this.uri = uri;
	}

	@Override
	public boolean hasUnderlyingModel() {
		return true;
	}

	@Override
	public boolean canUnderlyingModelBeLoaded() {
		return EcoreResourceUtil.exists(uri);
	}

	@Override
	public boolean isUnderlyingModelLoaded() {
		final Resource resource = EcorePlatformUtil.getResource(uri);
		if (resource != null) {
			try {
				RunnableWithResult<Boolean> runnable = new RunnableWithResult.Impl<Boolean>() {
					@Override
					public void run() {
						ExtendedResource extendedResource = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
						if (extendedResource != null) {
							setResult(extendedResource.isFullyLoaded());
						} else {
							setResult(true);
						}
					}
				};
				ModelOperationRunner.performModelAccess(resource, runnable);
				return runnable.getResult();
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
				return false;
			}
		}
		return false;
	}

	@Override
	public void loadUnderlyingModel() {
		IFile file = EcorePlatformUtil.getFile(uri);
		IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getModel(file);
		if (modelDescriptor != null) {
			// Request asynchronous loading of model behind given model object
			ModelLoadManager.INSTANCE.loadModel(modelDescriptor, true, null);
		}
	}

	@Override
	public boolean isStale() {
		return getTreeElement() == null;
	}

	@Override
	public Object getTreeElement() {
		if (eObject == null) {
			eObject = EcorePlatformUtil.getEObject(uri);
		}
		return eObject;
	}

	@Override
	public void appendToMemento(IMemento parentMemento) {
		if (uri != null) {
			IMemento memento = parentMemento.createChild(TreeElementStateProviderFactory.MEMENTO_TYPE_ELEMENT_EOBJECT);
			memento.putString(TreeElementStateProviderFactory.MEMENTO_KEY_URI, uri.toString());
		}
	}

	@Override
	public String toString() {
		return "EObjectElementProvider [uri=" + uri + ", eObject=" + eObject + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
