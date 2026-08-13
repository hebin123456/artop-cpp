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
import org.eclipse.sphinx.emf.internal.scoping.IResourceScopeMarker;
import org.eclipse.sphinx.platform.resources.syncing.IResourceSyncRequest;

public interface IResourceScopeMarkerSyncRequest extends IResourceSyncRequest {

	/**
	 * Record a new cleaning {@link IResourceScopeMarker Resource Scope Marker} task for the provided file.
	 * 
	 * @param file
	 *            the {@link IFile file} to clean the {@link IResourceScopeMarker Resource Scope Marker} for.
	 */
	void addFileToClean(IFile file);

	/**
	 * Record a new validating {@link IResourceScopeMarker Resource Scope Marker} task for the provided file.
	 * 
	 * @param file
	 *            the {@link IFile file} to validate the {@link IResourceScopeMarker Resource Scope Marker} for.
	 */
	void addFileToValidate(IFile file);
}