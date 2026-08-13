/**
 * <copyright>
 *
 * Copyright (c) 2008-2012 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [373481] Performance optimizations for model loading
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * An abstract {@link IResourceScopeProvider} implementation providing common behavior an overriding points.
 */
public abstract class AbstractResourceScopeProvider implements IResourceScopeProvider {

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScopeProvider#isApplicableTo(org.eclipse.core.resources.IFile)
	 */
	@Override
	public boolean isApplicableTo(IFile file) {
		IResourceScope scope = createScope(file);
		if (scope == null || !scope.exists()) {
			return false;
		}

		return hasApplicableFileExtension(file);
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.scoping.IResourceScopeProvider#hasApplicableFileExtension(org.eclipse.core.resources.IFile
	 * )
	 */
	@Override
	public boolean hasApplicableFileExtension(IFile file) {
		Assert.isNotNull(file);
		// Reject files without extension
		String extension = file.getFullPath().getFileExtension();
		if (extension == null) {
			return false;
		}

		extension = extension.toLowerCase();

		// Check if the given file's extension matches one of the extensions associated with one of the content types
		// that are supported by one of the metamodel descriptors which this resource scope provider is used for
		for (IMetaModelDescriptor mmDescriptor : ResourceScopeProviderRegistry.INSTANCE.getMetaModelDescriptorsFor(this)) {
			for (String contentTypeId : mmDescriptor.getContentTypeIds()) {
				if (isContentTypeContainsFileExtension(contentTypeId, extension)) {
					return true;
				}
			}
			for (String compatibleContentTypeId : mmDescriptor.getCompatibleContentTypeIds()) {
				if (isContentTypeContainsFileExtension(compatibleContentTypeId, extension)) {
					return true;
				}
			}
		}

		// Check if the given file's extension matches one of the extensions which are associated with a target
		// metamodel descriptor
		/*
		 * !! Important Note !! Theoretically we could try to implement a more sophisticated algorithm here in order to
		 * take only file extensions of strictly relevant target metamodel descriptors into account. However, in
		 * practice this would involve retrieving the target metamodel descriptor for the given file and turn this
		 * method into a performance killer. We therefore refer to all file extensions being associated with any target
		 * metamodel descriptor instead.
		 */
		return MetaModelDescriptorRegistry.INSTANCE.isContentTypeOfTargetDescriptorsApplicable(file);
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScopeProvider#getScope(org.eclipse.core.resources.IResource)
	 */
	@Override
	public IResourceScope getScope(IResource resource) {
		IResourceScope scope = createScope(resource);
		if (scope == null || !scope.exists()) {
			return null;
		}
		if (resource instanceof IFile) {
			if (!scope.belongsTo((IFile) resource, false)) {
				return null;
			}

		}
		return scope;
	}

	/**
	 * Creates a new {@link IResourceScope resource scope} for given workspace resource.
	 *
	 * @param resource
	 *            The {@link IResource workspace resource} to create the {@link IResourceScope resource scope} for.
	 * @return The newly created {@link IResourceScope resource scope}.
	 */
	protected abstract IResourceScope createScope(IResource resource);

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScopeProvider#getScope(org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	public IResourceScope getScope(Resource resource) {
		IFile file = EcorePlatformUtil.getFile(resource);
		if (file != null) {
			return getScope(file);
		}
		return null;
	}

	/*
	 * @see org.eclipse.sphinx.emf.scoping.IResourceScopeProvider#validate(org.eclipse.core.resources.IFile)
	 */
	@Override
	public Diagnostic validate(IFile file) {
		return Diagnostic.OK_INSTANCE;
	}

	private boolean isContentTypeContainsFileExtension(String contentTypeID, String fileExtension) {
		for (String contentTypeFileExtension : ExtendedPlatform.getContentTypeFileExtensions(contentTypeID)) {
			if (contentTypeFileExtension != null && fileExtension.equals(contentTypeFileExtension.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
