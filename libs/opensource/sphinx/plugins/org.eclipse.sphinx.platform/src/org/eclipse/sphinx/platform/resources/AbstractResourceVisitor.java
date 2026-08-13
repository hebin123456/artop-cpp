/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
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
package org.eclipse.sphinx.platform.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * An {@link IResourceVisitor} implementation which skips platform private resources (i.e., team private resources,
 * project description files, and project properties folders and files).
 * 
 * @see IResource
 * @see IResourceVisitor
 * @see ExtendedPlatform#isPlatformPrivateResource(IResource)
 * @since 0.7.0
 */
public abstract class AbstractResourceVisitor implements IResourceVisitor {

	/*
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	@Override
	public final boolean visit(IResource resource) throws CoreException {
		if (!ExtendedPlatform.isPlatformPrivateResource(resource)) {
			return doVisit(resource);
		}
		return false;
	}

	/**
	 * Visits the given {@link IResource resource}. Only {@link IResource resource}s which are not platform private
	 * (i.e., no team private resources, project description files, or project properties folders and files) are passed.
	 * 
	 * @param resource
	 *            The non platform private {@link IResource resource} to visit.
	 * @return <code>true</code> if the {@link IResource resource}'s members should be visited; <code>false</code> if
	 *         they should be skipped.
	 * @exception CoreException
	 *                If the visit fails for some reason.
	 * @see ExtendedPlatform#isPlatformPrivateResource(IResource)
	 */
	protected abstract boolean doVisit(IResource resource) throws CoreException;
}
