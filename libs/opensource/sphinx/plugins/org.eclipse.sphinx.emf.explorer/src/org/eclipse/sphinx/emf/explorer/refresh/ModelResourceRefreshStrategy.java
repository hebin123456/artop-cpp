/**
 * <copyright>
 *
 * Copyright (c) 2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [501109] The tree viewer state restoration upon Eclipse startup and viewer refreshed still running in cases where it is not needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.explorer.refresh;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.sphinx.emf.explorer.IModelCommonContentProvider;

public class ModelResourceRefreshStrategy extends AbstractRefreshStrategy<Resource> implements Runnable {

	private static final int MAX_INDIVIDUAL_MODEL_RESOURCE_REFRESHES = 20;

	public ModelResourceRefreshStrategy(IModelCommonContentProvider contentProvider, boolean preserveTreeViewerState) {
		super(contentProvider, preserveTreeViewerState);
	}

	@Override
	protected boolean shouldPerformSelectiveRefresh() {
		return getTreeElementsToRefresh().size() < MAX_INDIVIDUAL_MODEL_RESOURCE_REFRESHES;
	}

	@Override
	protected void performSelectiveRefresh(StructuredViewer viewer) {
		for (Resource modelResource : getTreeElementsToRefresh()) {
			IResource workspaceResource = contentProvider.getWorkspaceResource(modelResource);
			if (workspaceResource != null && workspaceResource.isAccessible()) {
				/*
				 * !! Important Note !! Refresh viewer if file behind resource matches trigger point condition. Refresh
				 * viewer regardless of that if resource has just been unloaded because the underlying file might not
				 * match the trigger condition anymore in this case (e.g. if the file's XML namespace or content type
				 * has been changed)
				 */
				if (contentProvider.isTriggerPoint(workspaceResource) || !modelResource.isLoaded()) {
					viewer.refresh(workspaceResource, true);
				}
			}
		}
	}
}