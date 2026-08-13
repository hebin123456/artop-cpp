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
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;

public interface IResourceScope {

	/**
	 * Returns true if this {@link IResourceScope resource scope} exists.
	 * <p>
	 * This method is guaranteed to have a very little performance overhead.
	 * </p>
	 * 
	 * @return <code>true</code> if the model exist (i.e if all user defined conditions required are full filled)
	 */
	boolean exists();

	/**
	 * Returns the root {@link IResource} of that {@link IResourceScope resource scope}.
	 * 
	 * @return The root {@link IResource resource} of that scope.
	 */
	IResource getRoot();

	/**
	 * Returns the roots of other {@link IResourceScope resource scope}s which are referenced by this
	 * {@link IResourceScope resource scope}.
	 * 
	 * @return {@link Collection} of resources containing all {@link IResourceScope resource scope}'s roots referenced
	 *         by this scope.
	 */
	Collection<IResource> getReferencedRoots();

	/**
	 * Returns the roots of other {@link IResourceScope resource scope}s which reference this {@link IResourceScope
	 * resource scope}.
	 * 
	 * @return {@link Collection} of {@link IResource workspac resources} containing all {@link IResourceScope resource
	 *         scope}'s roots referencing this scope.
	 */
	Collection<IResource> getReferencingRoots();

	/**
	 * Returns all the files in this scope persisted in the workspace.
	 * 
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return {@link Collection} of persisted {@link IFile file}s owned by this scope.
	 */
	Collection<IFile> getPersistedFiles(boolean includeReferencedScopes);

	/**
	 * Returns {@link IResource resource}s loaded in the given editing domain being part of that scope.
	 * 
	 * @param editingDomain
	 *            The {@link TransactionalEditingDomain editing domain} to be investigated.
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return {@link Collection} of {@link Resource resource}s owned by this scope and loaded in the provided
	 *         {@link TransactionalEditingDomain editing domain}.
	 */
	Collection<Resource> getLoadedResources(TransactionalEditingDomain editingDomain, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link IFile file} belongs to this scope.
	 * 
	 * @param file
	 *            The {@link IFile file} to be investigated.
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return <code>true</code> if the {@link IFile file} is in this resource scope, or <code>false</code> otherwise.
	 */
	boolean belongsTo(IFile file, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link Resource resource} belongs to this scope.
	 * 
	 * @param resource
	 *            The {@link Resource resource} to be investigated.
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return <code>true</code> if the {@link Resource resource} is in this resource scope, or <code>false</code>
	 *         otherwise.
	 */
	boolean belongsTo(Resource resource, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link URI uri} references a {@link Resource resource} that belongs to this scope.
	 * 
	 * @param uri
	 *            The {@link URI uri} to be investigated.
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return <code>true</code> if the {@link URI uri} is in this resource scope, or <code>false</code> otherwise.
	 */
	boolean belongsTo(URI uri, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link IFile file} not belonging to this resource scope did belong to it before.
	 * 
	 * @param file
	 *            The {@link IFile file} to be investigated.
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return <code>true</code> if the {@link IFile file} was in this resource scope, or <code>false</code> otherwise.
	 */
	boolean didBelongTo(IFile file, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link Resource resource} not belonging to this resource scope did belong to it before.
	 * 
	 * @param resource
	 *            The {@link Resource resource} to be investigated.
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return <code>true</code> if the {@link Resource resource} was in this resource scope, or <code>false</code>
	 *         otherwise.
	 */
	boolean didBelongTo(Resource resource, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link URI uri} did reference a {@link Resource resource} that does no longer belong to this
	 * resource scope but did so before.
	 * 
	 * @param uri
	 *            The {@link URI uri} to be investigated.
	 * @param includeReferencedScopes
	 *            Determines if scopes referenced by the current {@link IResourceScope resource scope} must be
	 *            investigated.
	 * @return <code>true</code> if the {@link URI uri} was in this resource scope, or <code>false</code> otherwise.
	 */
	boolean didBelongTo(URI uri, boolean includeReferencedScopes);

	/**
	 * Determines if given {@link IFile file} is shared among multiple resource scopes, i.e., can simultaneously belong
	 * to multiple resource scopes, or not.
	 * 
	 * @param file
	 *            The file to be investigated.
	 * @return <code>true</code> if given file is shared across multiple resource scopes, or <code>false</code>
	 *         otherwise.
	 */
	boolean isShared(IFile file);

	/**
	 * Determines if given {@link Resource resource} is shared among multiple resource scopes, i.e., can simultaneously
	 * belong to multiple resource scopes, or not.
	 * 
	 * @param file
	 *            The resource to be investigated.
	 * @return <code>true</code> if given resource is shared across multiple resource scopes, or <code>false</code>
	 *         otherwise.
	 */
	boolean isShared(Resource resource);

	/**
	 * Determines if given {@link URI} is shared among multiple resource scopes, i.e., can simultaneously belong to
	 * multiple resource scopes, or not.
	 * 
	 * @param file
	 *            The URI to be investigated.
	 * @return <code>true</code> if the resource behind given URI is shared across multiple resource scopes, or
	 *         <code>false</code> otherwise.
	 */
	boolean isShared(URI uri);
}
