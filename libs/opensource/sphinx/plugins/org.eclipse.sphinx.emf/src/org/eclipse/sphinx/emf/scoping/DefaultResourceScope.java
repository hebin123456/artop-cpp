/**
 * <copyright>
 * 
 * Copyright (c) 2013 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     itemis - Initial API and implementation
 *     itemis - [421205] Model descriptor registry does not return correct model descriptor for (shared) plugin resources
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;

/**
 * Provides a default implementation for the {@link IResourceScope} interface.
 * <p>
 * Clients that wish to implement custom resource scopes can extend this class and override only the methods which they
 * are interested in.
 * </p>
 * 
 * @see IResourceScope
 * @since 0.8.0
 */
public class DefaultResourceScope implements IResourceScope {

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#exists()
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getRoot()
	 */
	@Override
	public IResource getRoot() {
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getReferencedRoots()
	 */
	@Override
	public Collection<IResource> getReferencedRoots() {
		return Collections.emptyList();
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getReferencingRoots()
	 */
	@Override
	public Collection<IResource> getReferencingRoots() {
		return Collections.emptyList();
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getPersistedFiles(boolean)
	 */
	@Override
	public Collection<IFile> getPersistedFiles(boolean includeReferencedScopes) {
		return Collections.emptyList();
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getLoadedResources(org.eclipse.emf.transaction.
	 * TransactionalEditingDomain, boolean)
	 */
	@Override
	public Collection<Resource> getLoadedResources(TransactionalEditingDomain editingDomain, boolean includeReferencedScopes) {
		return Collections.emptyList();
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#belongsTo(org.eclipse.core.resources.IFile, boolean)
	 */
	@Override
	public boolean belongsTo(IFile file, boolean includeReferencedScopes) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#belongsTo(org.eclipse.emf.ecore.resource.Resource, boolean)
	 */
	@Override
	public boolean belongsTo(Resource resource, boolean includeReferencedScopes) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#belongsTo(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public boolean belongsTo(URI uri, boolean includeReferencedScopes) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.core.resources.IFile, boolean)
	 */
	@Override
	public boolean didBelongTo(IFile file, boolean includeReferencedScopes) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.emf.ecore.resource.Resource, boolean)
	 */
	@Override
	public boolean didBelongTo(Resource resource, boolean includeReferencedScopes) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public boolean didBelongTo(URI uri, boolean includeReferencedScopes) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#isShared(org.eclipse.core.resources.IFile)
	 */
	@Override
	public boolean isShared(IFile file) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#isShared(org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	public boolean isShared(Resource resource) {
		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#isShared(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public boolean isShared(URI uri) {
		return false;
	}
}
