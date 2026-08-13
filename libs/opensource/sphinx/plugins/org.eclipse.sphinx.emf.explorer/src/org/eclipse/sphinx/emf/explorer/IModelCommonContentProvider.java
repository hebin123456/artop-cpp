/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
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
package org.eclipse.sphinx.emf.explorer;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.workspace.ui.viewers.IExtendedCommonContentProvider;

/**
 * Extended version of {@link IExtendedCommonContentProvider} interface that includes extra services that are dedicated
 * to providing content of EMF models.
 */
public interface IModelCommonContentProvider extends IExtendedCommonContentProvider, IViewerProvider {

	/**
	 * Retrieves the model {@link Resource resource} behind specified workspace {@link IResource resource}. Returns
	 * <code>null</code> if no such is available or the {@link IModelDescriptor model} behind specified workspace
	 * resource has not been loaded yet.
	 * <p>
	 * Default implementation supports the handling of {@link IFile file} resources including lazy loading of the
	 * underlying {@link IModelDescriptor model}s: if the given file belongs to some model that has not been loaded yet
	 * then the loading of that model, i.e., the given file and all other files belonging to the same, will be
	 * triggered. The model loading will be performed asynchronously and therefore won't block the UI. When the model
	 * loading has been completed, the {@link #resourceChangedListener} automatically refreshes the underlying
	 * {@link #viewer viewer} so that the model elements contained by the given file become visible.
	 * <p>
	 * Clients may override this method so as to add support for other resource types (e.g., {@link IProject project}s
	 * or {@link IFolder folder}s) or implement different lazy or eager loading strategies.
	 *
	 * @param workspaceResource
	 *            The workspace resource of which the model resource is to be retrieved.
	 * @return The model resource behind specified workspace resource or <code>null</code> if no such is available or
	 *         the model behind specified workspace resource has not been loaded yet.
	 */
	Resource getModelResource(IResource workspaceResource);

	/**
	 * Returns a list of objects which are to be used as roots of the model content provided by this
	 * {@link BasicExplorerContentProvider content provider} for the given model resource. They may or may not be the
	 * actual root objects of the model resource.
	 * <p>
	 * The model content roots can be thought of being mapped to the workspace {@link IResource resource} behind given
	 * model resource and are the actual parent objects of the model objects to be displayed as virtual children of the
	 * workspace resource. The model content roots themselves will not become visible in the underlying viewer, only the
	 * workspace resource they are mapped to and their children will.
	 * </p>
	 * <p>
	 * This implementation returns a list containing the provided model resource itself as default. Clients are free to
	 * override and implement alternative behaviors as appropriate.
	 * </p>
	 *
	 * @param modelResource
	 *            The model resource of which the model content roots are to be retrieved.
	 * @return The list of notifier objects which are to be used as roots of the model content provided by this
	 *         {@link BasicExplorerContentProvider content provider}.
	 */
	List<Object> getModelContentRoots(Resource modelResource);

	/**
	 * Returns the {@link IResource resource} corresponding to given model {@link Resource resource}.
	 *
	 * @param modelResource
	 *            The model resource of which the workspace resource it to be returned.
	 * @return The workspace {@link IResource resource} corresponding to given model resource.
	 * @see #getModelResource(IResource)
	 */
	IResource getWorkspaceResource(Resource modelResource);
}
