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

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.scoping.IResourceScope;
import org.eclipse.sphinx.emf.scoping.IResourceScopeProvider;
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.platform.resources.MarkerJob;

/**
 * Provides methods validating for validating {@link IFile}s with regard to their {@link IResourceScope resource scope}
 * and creating or cleaning corresponding problem markers. The validation itself is delegated to the
 * {@link IResourceScopeProvider resource scope provider} which is associated with the type of the {@link IFile file} in
 * question.
 * 
 * @see IResourceScopeMarker#RESOURCE_SCOPING_PROBLEM
 */
public class ResourceScopeValidationService {

	/**
	 * Singleton instance.
	 */
	public static ResourceScopeValidationService INSTANCE = new ResourceScopeValidationService();

	// Private default constructor for singleton pattern
	private ResourceScopeValidationService() {
	}

	/**
	 * Validates the provided collection of {@link IFile file}s with regard to their {@link IResourceScope resource
	 * scope}. The validation itself is delegated to the {@link IResourceScopeProvider resource scope provider} which is
	 * associated with the type of each {@link IFile file}. If it results in indicating any problem a
	 * {@link IResourceScopeMarker#RESOURCE_SCOPING_PROBLEM} marker is created for the {@link IFile file} in question.
	 * 
	 * @param files
	 *            The collection of {@link IFile file}s to be validated in terms of resource scoping.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	public void validateFiles(final Collection<IFile> files, final IProgressMonitor monitor) {
		Assert.isNotNull(files);

		if (files.size() > 0) {
			SubMonitor progress = SubMonitor.convert(monitor, Messages.task_validatingResourceScopes, files.size());
			for (IFile file : files) {
				if (progress.isCanceled()) {
					return;
				}

				if (file != null && file.isAccessible() && file.isSynchronized(IResource.DEPTH_ONE)) {
					// Delete old resource scoping problem marker if any
					MarkerJob.INSTANCE.addDeleteMarkerTask(file, IResourceScopeMarker.RESOURCE_SCOPING_PROBLEM);

					/*
					 * Performance optimization: Check if current file is a potential model file by investigating it's
					 * extension. This helps excluding obvious non-model files right away and avoids potentially lengthy
					 * but useless processing of the same.
					 */
					if (ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file)) {
						// Retrieve resource scope provider associated with given file
						IMetaModelDescriptor effectiveDescriptor = MetaModelDescriptorRegistry.INSTANCE.getEffectiveDescriptor(file);
						IResourceScopeProvider resourceScopeProvider = ResourceScopeProviderRegistry.INSTANCE
								.getResourceScopeProvider(effectiveDescriptor);
						if (resourceScopeProvider != null) {
							// Validate file in terms of resource scoping
							Diagnostic diagnostic = resourceScopeProvider.validate(file);
							if (diagnostic != null && !diagnostic.equals(Diagnostic.OK_INSTANCE)) {
								// Delete all other old problem markers - as resource is a model resource and
								// out of scope they most likely make no longer any sense
								MarkerJob.INSTANCE.addDeleteMarkerTask(file, null);

								// Create new resource scoping problem maker
								createProblemMarkerForDiagnostic(file, diagnostic);
							}
						}
					}
				}

				progress.worked(1);
			}

			MarkerJob.INSTANCE.schedule();
		}
	}

	private void createProblemMarkerForDiagnostic(IFile file, Diagnostic diagnostic) {
		Assert.isNotNull(file);
		Assert.isLegal(file.isAccessible());
		Assert.isNotNull(diagnostic);

		int severity = diagnostic.getSeverity();
		int mseverity = IMarker.SEVERITY_INFO;
		if (severity == Diagnostic.ERROR) {
			mseverity = IMarker.SEVERITY_ERROR;
		} else if (severity == Diagnostic.WARNING) {
			mseverity = IMarker.SEVERITY_WARNING;
		}

		MarkerJob.INSTANCE.addCreateMarkerTask(file, IResourceScopeMarker.RESOURCE_SCOPING_PROBLEM, mseverity, diagnostic.getMessage());
	}

	/**
	 * Removes all {@link IResourceScopeMarker#RESOURCE_SCOPING_PROBLEM resource scoping problem marker}s from the
	 * {@link IFile file}s in provided collection.
	 * 
	 * @param files
	 *            The collection of {@link IFile file}s to be cleaned.
	 * @param monitor
	 *            A {@link IProgressMonitor progress monitor}, or <code>null</code> if progress reporting is not
	 *            desired.
	 */
	public void cleanFiles(final Collection<IFile> files, final IProgressMonitor monitor) {
		Assert.isNotNull(files);

		if (files.size() > 0) {
			SubMonitor progress = SubMonitor.convert(monitor, Messages.task_cleaningResourceScopeMarkers, files.size());
			for (IFile file : files) {
				if (progress.isCanceled()) {
					return;
				}

				if (file != null && file.isAccessible()) {
					/*
					 * Performance optimization: Check if current file is a potential model file by investigating it's
					 * extension. This helps excluding obvious non-model files right away and avoids potentially lengthy
					 * but useless processing of the same.
					 */
					if (ResourceScopeProviderRegistry.INSTANCE.hasApplicableFileExtension(file)) {
						// Delete old resource scoping problem maker if any
						MarkerJob.INSTANCE.addDeleteMarkerTask(file, IResourceScopeMarker.RESOURCE_SCOPING_PROBLEM);
					}
				}

				progress.worked(1);
			}
		}

		MarkerJob.INSTANCE.schedule();
	}
}
