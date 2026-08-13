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
package org.eclipse.sphinx.emf.scoping;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * A provider for {@link IResourceScope resource scope}s of {@link IResource workspace resource}s and {@link Resource
 * model resource}s.
 * 
 * @see IResourceScope
 */
public interface IResourceScopeProvider {

	/**
	 * Tests if this {@link IResourceScopeProvider resource scope provider} is applicable to given {@link IFile file},
	 * i.e., if this {@link IResourceScopeProvider resource scope provider} is able or likely to be able to provide a
	 * {@link IResourceScope resource scope} for the given {@link IFile file}. This typically involves testing if a
	 * {@link IResourceScope resource scope} can be created for given {@link IFile file}, exists, and
	 * {@link #hasApplicableFileExtension(IFile) is applicable} to {@link IFile file}s having the extension of the given
	 * {@link IFile file}.
	 * <p>
	 * This method is guaranteed to have a very little performance overhead.
	 * </p>
	 * 
	 * @param file
	 *            The {@link IFile file} to be investigated.
	 * @return <code>true</code> if this {@link IResourceScopeProvider resource scope provider} is applicable to given
	 *         {@link IFile file}, <code>false</code> otherwise.
	 * @see #hasApplicableFileExtension(IFile)
	 */
	boolean isApplicableTo(IFile file);

	/**
	 * Tests if the given {@link IFile file}'s extension corresponds to a file type which is subject to
	 * {@link IResourceScopeProvider resource scope provider}.
	 * <p>
	 * This method is guaranteed to have a very little performance overhead.
	 * </p>
	 * 
	 * @param file
	 *            The {@link IFile file} to be investigated.
	 * @return <code>true</code> if this {@link IResourceScopeProvider resource scope provider} is applicable to
	 *         {@link IFile file}s having the extension of the given {@link IFile file}, <code>false</code> otherwise.
	 * @see #isApplicableTo(IFile)
	 */
	boolean hasApplicableFileExtension(IFile file);

	/**
	 * Returns an {@link IResourceScope resource scope} for given {@link IResource workspace resource}.
	 * 
	 * @param resource
	 *            The {@link IResource workspace resource} to retrieve the {@link IResourceScope resource scope} for.
	 * @return The {@link IResourceScope resource scope} for given {@link IResource workspace resource}.
	 */
	IResourceScope getScope(IResource resource);

	/**
	 * Returns an {@link IResourceScope resource scope} for given {@link Resource model resource}.
	 * 
	 * @param resource
	 *            The {@link Resource model resource} to retrieve the {@link IResourceScope resource scope} for.
	 * @return The {@link IResourceScope resource scope} for given {@link Resource model resource}.
	 */
	IResourceScope getScope(Resource resource);

	/**
	 * Validates given {@link IFile workspace file} with regard to their {@link IResourceScope resource scope} and
	 * returns a {@link Diagnostic diagnostic} as validation result.
	 * 
	 * @return {@link Diagnostic} indicating {@link IResourceScope resource scoping} related problems, if any, or
	 *         {@link Diagnostic#OK_INSTANCE} otherwise.
	 */
	Diagnostic validate(IFile file);
}
