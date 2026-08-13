/**
 * <copyright>
 * 
 * Copyright (c) 2008-2012 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 * 
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.scoping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.emf.scoping.IResourceScopeMarkerSyncRequest;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;

/**
 * Stores and performs all requests relatives to {@link IResourceScopeMarker} update.
 */
public class ResourceScopeMarkerSyncRequest implements IResourceScopeMarkerSyncRequest {

	private Set<IFile> filesToClean = new HashSet<IFile>();
	private Set<IFile> filesToValidate = new HashSet<IFile>();

	/*
	 * @see org.eclipse.sphinx.platform.resources.syncing.IResourceSyncRequest#init()
	 */
	@Override
	public void init() {
		// Do nothing
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.internal.scoping.IResourceScopeMarkerSyncRequest#addFileToValidate(org.eclipse.core.resources
	 * .IFile)
	 */
	@Override
	public void addFileToValidate(IFile file) {
		if (file != null) {
			// Exclude inaccessible files
			if (file.isAccessible()) {
				// Exclude obvious non-model files
				if (ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file)) {
					filesToValidate.add(file);
				}
			}
		}
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.internal.scoping.IResourceScopeMarkerSyncRequest#addFileToClean(org.eclipse.core.resources
	 * . IFile)
	 */
	@Override
	public void addFileToClean(IFile file) {
		if (file != null) {
			// Exclude inaccessible files
			if (file.isAccessible()) {
				// Exclude obvious non-model files
				if (ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file)) {
					filesToClean.add(file);
				}
			}
		}
	}

	@Override
	public boolean canPerform() {
		return filesToValidate.size() > 0 || filesToClean.size() > 0;
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.syncing.IResourceSyncRequest#perform()
	 */
	@Override
	public void perform() {
		if (!canPerform()) {
			return;
		}

		if (filesToValidate.size() > 0) {
			ResourceScopeValidationService.INSTANCE.validateFiles(new HashSet<IFile>(filesToValidate), null);
			filesToValidate.clear();
		}

		if (filesToClean.size() > 0) {
			ResourceScopeValidationService.INSTANCE.cleanFiles(new HashSet<IFile>(filesToClean), null);
			filesToClean.clear();
		}
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.syncing.IResourceSyncRequest#dispose()
	 */
	@Override
	public void dispose() {
		// Do nothing
	}
}