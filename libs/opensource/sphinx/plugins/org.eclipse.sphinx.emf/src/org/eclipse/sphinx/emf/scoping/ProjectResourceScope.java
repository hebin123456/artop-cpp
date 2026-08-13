/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, itemis, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [346715] IMetaModelDescriptor methods of MetaModelDescriptorRegistry taking EObject or Resource arguments should not start new EMF transactions
 *     BMW Car IT - [373481] Performance optimizations for model loading. Added referenced projects cache.
 *     itemis - [421205] Model descriptor registry does not return correct model descriptor for (shared) plugin resources
 *     itemis - [425252] UML property section hangs when accessing reference property of a stereotype application
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScopeProvider.IReferencedProjectsProvider;
import org.eclipse.sphinx.emf.scoping.ProjectResourceScopeProvider.ReferencedProjectsProvider;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

public class ProjectResourceScope extends AbstractResourceScope {

	protected IProject rootProject;

	// Use a non-caching provider by default
	protected IReferencedProjectsProvider referencedProjectsProvider = new ReferencedProjectsProvider();

	public ProjectResourceScope(IResource resource) {
		Assert.isNotNull(resource);
		rootProject = resource.getProject();
	}

	protected void setReferencedProjectsProvider(IReferencedProjectsProvider referencedProjectsProvider) {
		this.referencedProjectsProvider = referencedProjectsProvider;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getRoot()
	 */
	@Override
	public IResource getRoot() {
		return rootProject;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getReferencedRoots()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<IResource> getReferencedRoots() {
		if (rootProject != null) {
			Collection<?> allReferencedProjects = referencedProjectsProvider.get(rootProject);
			return (Collection<IResource>) allReferencedProjects;
		} else {
			return Collections.emptySet();
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#getReferencingRoots()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<IResource> getReferencingRoots() {
		if (rootProject != null) {
			Collection<?> allReferencingProjects = ExtendedPlatform.getAllReferencingProjects(rootProject);
			return (Collection<IResource>) allReferencingProjects;
		} else {
			return Collections.emptySet();
		}
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#belongsTo(org.eclipse.core.resources.IFile, boolean)
	 */
	@Override
	public boolean belongsTo(IFile file, boolean includeReferencedScopes) {
		if (belongsToRootOrReferencedProjects(file, includeReferencedScopes)) {
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
		if (belongsToRootOrReferencedProjects(resource.getURI(), includeReferencedScopes)) {
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
		if (belongsToRootOrReferencedProjects(uri, includeReferencedScopes)) {
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
		return belongsToRootOrReferencedProjects(file, includeReferencedScopes);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.emf.ecore.resource.Resource, boolean)
	 */
	@Override
	public boolean didBelongTo(Resource resource, boolean includeReferencedScopes) {
		return belongsToRootOrReferencedProjects(resource.getURI(), includeReferencedScopes);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScope#didBelongTo(org.eclipse.emf.common.util.URI, boolean)
	 */
	@Override
	public boolean didBelongTo(URI uri, boolean includeReferencedScopes) {
		return belongsToRootOrReferencedProjects(uri, includeReferencedScopes);
	}

	protected boolean belongsToRootOrReferencedProjects(URI uri, boolean includeReferencedScopes) {
		IFile file = EcorePlatformUtil.getFile(uri);
		return belongsTo(file, includeReferencedScopes);
	}

	protected boolean belongsToRootOrReferencedProjects(IFile file, boolean includeReferencedScopes) {
		if (file != null) {
			return rootProject.equals(file.getProject()) || includeReferencedScopes && getReferencedRoots().contains(file.getProject());
		}
		return false;
	}

	/**
	 * @deprecated Use {@link #belongsToRootOrReferencedProjects(IFile, boolean)} instead.
	 */
	@Deprecated
	protected boolean belongsToRootOrDependingProjects(IFile file, boolean includeReferencedScopes) {
		return belongsToRootOrReferencedProjects(file, includeReferencedScopes);
	}

}
