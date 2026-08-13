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
package org.eclipse.sphinx.emf.workspace.internal.saving;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.saving.IModelSaveIndicator;
import org.eclipse.sphinx.emf.saving.IResourceSaveIndicator;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;

/**
 * Default implementation of {@linkplain IModelSaveIndicator}.
 * <p>
 * This implementation works on a model level to avoid performance overhead.
 * </p>
 * 
 * @see ResourceSaveIndicator
 * @see SaveIndicatorUtil
 */
public class ModelSaveIndicator implements IModelSaveIndicator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty(IModelDescriptor modelDescriptor) {
		Assert.isNotNull(modelDescriptor);
		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(modelDescriptor.getEditingDomain());
		if (resourceSaveIndicator != null) {
			Collection<Resource> dirtyResources = new HashSet<Resource>(resourceSaveIndicator.getDirtyResources());
			Collection<Resource> modelResources = EcorePlatformUtil.getResourcesInModel(modelDescriptor, true);
			dirtyResources.retainAll(modelResources);
			return !dirtyResources.isEmpty();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSaved(IModelDescriptor modelDescriptor) {
		Assert.isNotNull(modelDescriptor);
		IResourceSaveIndicator resourceSaveIndicator = SaveIndicatorUtil.getResourceSaveIndicator(modelDescriptor.getEditingDomain());
		if (resourceSaveIndicator != null) {
			Collection<Resource> dirtyResources = new ArrayList<Resource>(resourceSaveIndicator.getDirtyResources());
			Collection<Resource> modelResources = EcorePlatformUtil.getResourcesInModel(modelDescriptor, true);
			dirtyResources.retainAll(modelResources);
			resourceSaveIndicator.setSaved(dirtyResources);
		}
	}
}
