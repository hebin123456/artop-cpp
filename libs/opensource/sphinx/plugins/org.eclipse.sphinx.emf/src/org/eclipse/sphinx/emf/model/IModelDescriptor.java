/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [421205] Model descriptor registry does not return correct model descriptor for (shared) plugin resources
 *     itemis - [425854] The diagram created in the Artop is not saved after being updated to "sphinx-Update-0.8.0M4".
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.model;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.scoping.IResourceScope;

/**
 * Model descriptor that is used to identify and differentiate models.
 * <p>
 * One model is identified by a root {@link IProject project} and a {@link IMetaModelDescriptor meta-model descriptor}.
 * This descriptor also references the editing domain owning the resource set containing the resources from the
 * described model.
 * <p>
 * This {@link IModelDescriptor model descriptor} provides a method indicating if a specified {@link IFile file} belongs
 * or not to the described model.
 * <p>
 * Model descriptors are used in registry of {@link IModelDescriptor model descriptor}s (
 * {@linkplain ModelDescriptorRegistry}) in order to keep references of loaded models.
 * 
 * @see org.eclipse.sphinx.emf.model.IDescriptor
 * @see org.eclipse.sphinx.emf.model.ModelDescriptor
 * @see org.eclipse.sphinx.emf.model.ModelDescriptorRegistry
 */
public interface IModelDescriptor extends IAdaptable {

	/**
	 * @return The {@link IMetaModelDescriptor meta-model descriptor}.
	 */
	IMetaModelDescriptor getMetaModelDescriptor();

	/**
	 * @return The {@link IMetaModelDescriptor target meta-model descriptor}.
	 */
	IMetaModelDescriptor getTargetMetaModelDescriptor();

	/**
	 * @return The editing domain of the model described here.
	 */
	TransactionalEditingDomain getEditingDomain();

	/**
	 * @return The resourceScope {@link IResourceScope resourceScope} of the model described here.
	 */

	IResourceScope getScope();

	/**
	 * @return The root {@link IResource resource} of the model described here.
	 */

	IResource getRoot();

	/**
	 * @return The list of root{@link IResource resource}s constituting the model described here.
	 * @since 0.7.0
	 */
	Collection<IResource> getReferencedRoots();

	/**
	 * @return The list of parent root{@link IResource resource}s referencing the model described here.
	 * @since 0.7.0
	 */
	Collection<IResource> getReferencingRoots();

	/**
	 * @return The resources inside the editing domain's resource set that belong to this model.
	 */
	Collection<Resource> getLoadedResources(boolean includeReferencedScopes);

	/**
	 * @return The {@link IFile file}(s) that belong to this model.
	 */
	Collection<IFile> getPersistedFiles(boolean includeReferencedScopes);

	/**
	 * Indicates if the given {@link IFile file} is part of the model identified by this {@link IModelDescriptor model
	 * descriptor}. Criteria applied in order to know that are the following ones:
	 * <ul>
	 * <li>encapsulated {@link IMetaModelDescriptor meta-model descriptor} must be equal to {@link IFile file}'s one;</li>
	 * <li>{@link IFile file} must be a member of the encapsulated {@link IProject project}<br>
	 * (can be member of a referenced {@link IProject project}).</li>
	 * </ul>
	 * 
	 * @param file
	 *            The {@link IFile file} that may belongs to the model.
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if the given {@link IFile file} belongs to the identified model;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean belongsTo(IFile file, boolean includeReferencedScopes);

	/**
	 * Indicates if the given {@link Resource resource} is part of the model identified by this {@link IModelDescriptor
	 * model descriptor}. Criteria applied in order to know that are the following ones:
	 * <ul>
	 * <li>encapsulated {@link IMetaModelDescriptor meta-model descriptor} must be equal to {@link IFile file}'s one;</li>
	 * <li>{@link IFile file} must be a member of the encapsulated {@link IProject project}<br>
	 * (can be member of a referenced {@link IProject project}).</li>
	 * </ul>
	 * 
	 * @param resource
	 *            The {@link Resource resource} that may belongs to the model.
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if the given {@link IFile file} belongs to the identified model;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean belongsTo(Resource resource, boolean includeReferencedScopes);

	/**
	 * Indicates if the given {@link URI uri} is part of the model identified by this {@link IModelDescriptor model
	 * descriptor}. Criteria applied in order to know that are the following ones:
	 * <ul>
	 * <li>encapsulated {@link IMetaModelDescriptor meta-model descriptor} must be equal to {@link IFile file}'s one;</li>
	 * <li>{@link IFile file} must be a member of the encapsulated {@link IProject project}<br>
	 * (can be member of a referenced {@link IProject project}).</li>
	 * </ul>
	 * 
	 * @param uri
	 *            The {@link URI uri} that may belongs to the model.
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if the given {@link URI uri} belongs to the identified model;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean belongsTo(URI uri, boolean includeReferencedScopes);

	/**
	 * Indicates if the given {@link IFile file} has been part of the model identified by this {@link IModelDescriptor
	 * model descriptor} before it was changed or deleted. Criteria applied in order to know that are the following
	 * ones:
	 * <ul>
	 * <li>encapsulated {@link IMetaModelDescriptor meta-model descriptor} must be equal to {@link IFile file}'s old
	 * {@link IMetaModelDescriptor meta-model descriptor};</li>
	 * <li>{@link IFile file} must be a member of the encapsulated {@link IProject project}<br>
	 * (can be member of a referenced {@link IProject project}).</li>
	 * </ul>
	 * !Important note! The information will only be available during processing of the method
	 * org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry.FileMetaModelDescriptorCache.removeDescriptor(IFile)
	 * called from org.eclipse.sphinx.emf.internal.MetaModelDescriptorCacheAndModelDescriptorRegistryUpdater.
	 * handleModelResourceUnloaded(Collection<Resource>).In any other use case the method will behave as if there was no
	 * old meta-model descriptor available. The reason is that old meta-model descriptor is removed as soon as model
	 * descriptor has been removed. The {@link IFile file} that may belongs to the model.
	 * 
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if the given {@link IFile file} belongs to the identified model;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean didBelongTo(IFile file, boolean includeReferencedScopes);

	/**
	 * Indicates if the given {@link Resource resource} has been part of the model identified by this
	 * {@link IModelDescriptor model descriptor} before it was changed or deleted. Criteria applied in order to know
	 * that are the following ones:
	 * <ul>
	 * <li>encapsulated {@link IMetaModelDescriptor meta-model descriptor} must be equal to {@link Resource resource}'s
	 * old {@link IMetaModelDescriptor meta-model descriptor};</li> !Important note! The information will only be
	 * available during processing of the method
	 * org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry.FileMetaModelDescriptorCache.removeDescriptor(IFile)
	 * called from org.eclipse.sphinx.emf.internal.MetaModelDescriptorCacheAndModelDescriptorRegistryUpdater.
	 * handleModelResourceUnloaded(Collection<Resource>).In any other use case the method will behave as if there was no
	 * old meta-model descriptor available. The reason is that old meta-model descriptor is removed as soon as model
	 * descriptor has been removed.
	 * 
	 * @param uri
	 *            The {@link Resource resource} that may belongs to the model.
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if the given {@link Resource resource} belongs to the identified
	 *         model;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean didBelongTo(Resource resource, boolean includeReferencedScopes);

	/**
	 * if the given {@link URI uri} has been part of the model identified by this {@link IModelDescriptor model
	 * descriptor} before relative file it was changed or deleted. Criteria applied in order to know that are the
	 * following ones:
	 * <ul>
	 * <li>encapsulated {@link IMetaModelDescriptor meta-model descriptor} must be equal to {@link URI uri}'s old
	 * {@link IMetaModelDescriptor meta-model descriptor};</li>
	 * <li>{@link URI uri} must point on a member of the encapsulated {@link IProject project}<br>
	 * (can be member of a referenced {@link IProject project}).</li>
	 * </ul>
	 * !Important note! The information will only be available during processing of the method
	 * org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry.FileMetaModelDescriptorCache.removeDescriptor(IFile)
	 * called from org.eclipse.sphinx.emf.internal.MetaModelDescriptorCacheAndModelDescriptorRegistryUpdater.
	 * handleModelResourceUnloaded(Collection<Resource>).In any other use case the method will behave as if there was no
	 * old meta-model descriptor available. The reason is that old meta-model descriptor is removed as soon as model
	 * descriptor has been removed.
	 * 
	 * @param file
	 *            The {@link IFile file} that may belongs to the model.
	 * @return <ul>
	 *         <li><tt><b>true</b>&nbsp;&nbsp;</tt> if the given {@link URI uri} point on an element that belongs to the
	 *         identified model;</li>
	 *         <li><tt><b>false</b>&nbsp;</tt> otherwise.</li>
	 *         </ul>
	 */
	boolean didBelongTo(URI uri, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link IFile file} is shared among multiple models, i.e., can simultaneously belong to
	 * multiple models, or not.
	 * 
	 * @param file
	 *            The file to be investigated.
	 * @return <code>true</code> if given file is shared across multiple models, or <code>false</code> otherwise.
	 */
	boolean isShared(IFile file);

	/**
	 * Determines if given {@link Resource resource} is shared among multiple models, i.e., can simultaneously belong to
	 * multiple models, or not.
	 * 
	 * @param file
	 *            The resource to be investigated.
	 * @return <code>true</code> if given resource is shared across multiple models, or <code>false</code> otherwise.
	 */
	boolean isShared(Resource resource);

	/**
	 * Determines if given {@link URI} is shared among multiple models, i.e., can simultaneously belong to multiple
	 * models, or not.
	 * 
	 * @param file
	 *            The URI to be investigated.
	 * @return <code>true</code> if the resource behind given URI is shared across multiple models, or
	 *         <code>false</code> otherwise.
	 */
	boolean isShared(URI uri);
}
