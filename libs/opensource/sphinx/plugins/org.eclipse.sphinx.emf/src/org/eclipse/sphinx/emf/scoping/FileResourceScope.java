/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.scoping;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

public class FileResourceScope extends AbstractResourceScope {

	protected IFile rootFile;

	public FileResourceScope(IResource resource) {
		Assert.isNotNull(resource);
		rootFile = EcorePlatformUtil.getFile(resource);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getRoot()
	 */
	@Override
	public IResource getRoot() {
		return rootFile;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getReferencedRoots()
	 */
	@Override
	public Collection<IResource> getReferencedRoots() {
		return Collections.emptySet();
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getReferencingRoots()
	 */
	@Override
	public Collection<IResource> getReferencingRoots() {
		return Collections.emptySet();
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#belongsTo(org.eclipse.core.resources.IFile, boolean)
	 */
	@Override
	public boolean belongsTo(IFile file, boolean includeReferencedScopes) {
		if (belongsToRootFile(file, includeReferencedScopes)) {
			return true;
		}

		if (isShared(file)) {
			return true;
		}

		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#belongsTo(org.eclipse.emf.ecore.resource.Resource, boolean)
	 */
	@Override
	public boolean belongsTo(Resource resource, boolean includeReferencedScopes) {
		IFile file = EcorePlatformUtil.getFile(resource);
		if (belongsToRootFile(file, includeReferencedScopes)) {
			return true;
		}

		if (isShared(resource)) {
			return true;
		}

		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#belongsTo(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public boolean belongsTo(URI uri, boolean includeReferencedScopes) {
		IFile file = EcorePlatformUtil.getFile(uri);
		if (belongsToRootFile(file, includeReferencedScopes)) {
			return true;
		}

		if (isShared(uri)) {
			return true;
		}

		return false;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.core.resources.IFile, boolean)
	 */
	@Override
	public boolean didBelongTo(IFile file, boolean includeReferencedScopes) {
		return belongsToRootFile(file, includeReferencedScopes);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.emf.ecore.resource.Resource, boolean)
	 */
	@Override
	public boolean didBelongTo(Resource resource, boolean includeReferencedScopes) {
		IFile file = EcorePlatformUtil.getFile(resource);
		return belongsToRootFile(file, includeReferencedScopes);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public boolean didBelongTo(URI uri, boolean includeReferencedScopes) {
		IFile file = EcorePlatformUtil.getFile(uri);
		return belongsToRootFile(file, includeReferencedScopes);
	}

	protected boolean belongsToRootFile(IFile file, boolean includeReferencedScopes) {
		return rootFile.equals(file);
	}
}
