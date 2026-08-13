/**
 * <copyright>
 *
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Extracted AbstractResultObjectHandler and derived SaveAsNewFileHandler from it.
 *     itemis - [358591] ResultObjectHandler and ResultMessageHandler used by M2xConfigurationWizards are difficult to customize and should be usable in BasicM2xActions too
 *
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.jobs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.resource.ModelResourceDescriptor;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.xtendxpand.internal.Activator;

/**
 * An {@link IJobChangeListener} implementation that can be registered on an {@link XtendJob} instance or a
 * {@link M2MJob} instance that encloses the latter and saves the {@link XtendJob#getResultObjects() result objects}
 * produced by the {@link XtendJob} as new files in the workspace.
 *
 * @see XtendJob
 * @see M2MJob
 */
public class SaveAsNewFileHandler extends AbstractResultObjectHandler {

	/*
	 * @see org.eclipse.sphinx.xtendxpand.jobs.AbstractResultObjectHandler#handleResultObjects(java.util.Map)
	 */
	@Override
	protected void handleResultObjects(Map<Object, Collection<?>> resultObjects) {
		// Create descriptors for resources in which to save result objects
		Set<ModelResourceDescriptor> allModelResourceDescriptors = new HashSet<ModelResourceDescriptor>();
		Set<IPath> allocatedResultPaths = new HashSet<IPath>();
		for (Object inputObject : resultObjects.keySet()) {
			for (Object resultObject : resultObjects.get(inputObject)) {
				if (resultObject instanceof EObject && shouldSave((EObject) resultObject)) {
					IPath resultPath = getUniqueResultPath(inputObject, (EObject) resultObject, allocatedResultPaths);
					if (resultPath != null) {
						allocatedResultPaths.add(resultPath);

						String resultContentTypeId = getContentTypeIdFor((EObject) resultObject);
						allModelResourceDescriptors.add(new ModelResourceDescriptor(resultPath, resultContentTypeId, (EObject) resultObject));
					}
				}
			}
		}

		// Sort model resource descriptors according to editing domain which they should belong to
		Map<TransactionalEditingDomain, Collection<ModelResourceDescriptor>> modelResourceDescriptors = new HashMap<TransactionalEditingDomain, Collection<ModelResourceDescriptor>>();
		for (ModelResourceDescriptor modelResourceDescriptor : allModelResourceDescriptors) {
			IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(modelResourceDescriptor.getContents().iterator()
					.next());
			IFile modelFile = ResourcesPlugin.getWorkspace().getRoot().getFile(modelResourceDescriptor.getPath());
			TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getEditingDomain(modelFile.getParent(), mmDescriptor);

			Collection<ModelResourceDescriptor> modelResourceDescriptorsForEditingDomain = modelResourceDescriptors.get(editingDomain);
			if (modelResourceDescriptorsForEditingDomain == null) {
				modelResourceDescriptorsForEditingDomain = new HashSet<ModelResourceDescriptor>();
				modelResourceDescriptors.put(editingDomain, modelResourceDescriptorsForEditingDomain);
			}
			modelResourceDescriptorsForEditingDomain.add(modelResourceDescriptor);
		}

		// Save model resources
		for (TransactionalEditingDomain editingDomain : modelResourceDescriptors.keySet()) {
			try {
				EcorePlatformUtil.saveNewModelResources(editingDomain, modelResourceDescriptors.get(editingDomain), true, null);
			} catch (Exception ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}

	protected boolean shouldSave(EObject object) {
		Assert.isNotNull(object);

		// Return true if given object is not already contained in any resource
		return object.eResource() == null;
	}

	protected IPath getUniqueResultPath(Object inputObject, EObject resultObject, Set<IPath> allocatedResultPaths) {
		IPath candidateResultPath = getResultPath(inputObject, resultObject);
		if (candidateResultPath != null) {
			return ExtendedPlatform.createUniquePath(candidateResultPath, allocatedResultPaths);
		}
		return null;
	}

	protected IPath getResultPath(Object inputObject, EObject resultObject) {
		IFile inputFile = EcorePlatformUtil.getFile(inputObject);
		IPath projectRelativeResultPath = getProjectRelativeResultPath(inputFile, resultObject);
		if (projectRelativeResultPath != null) {
			return inputFile.getProject().getFullPath().append(projectRelativeResultPath);
		}
		return null;

	}

	protected IPath getProjectRelativeResultPath(IFile inputFile, EObject resultObject) {
		if (inputFile != null) {
			String resultFileExtension = getFileExtensionFor(resultObject);
			return inputFile.getProjectRelativePath().removeFileExtension().addFileExtension(resultFileExtension);
		}
		return null;
	}

	protected String getFileExtensionFor(EObject object) {
		// Try to retrieve file extension supported by content type behind given EObject
		String contentTypeId = getContentTypeIdFor(object);
		if (contentTypeId != null) {
			Collection<String> possibleExtensions = ExtendedPlatform.getContentTypeFileExtensions(contentTypeId);
			if (!possibleExtensions.isEmpty()) {
				return possibleExtensions.iterator().next();
			}
		}

		// Return name of given EObject's EPackage as file extension
		return object.eClass().getEPackage().getName();
	}

	protected String getContentTypeIdFor(EObject object) {
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(object.eClass());
		if (mmDescriptor != null) {
			return mmDescriptor.getDefaultContentTypeId();
		}
		return null;
	}
}
