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

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.sphinx.emf.explorer.IModelCommonContentProvider;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

public class ModelObjectRefreshStrategy extends AbstractRefreshStrategy<EObject> implements Runnable {

	private static final int MAX_INDIVIDUAL_MODEL_OBJECT_REFRESHES = 100;

	public ModelObjectRefreshStrategy(IModelCommonContentProvider contentProvider) {
		super(contentProvider, false);
	}

	@Override
	protected boolean shouldPerformSelectiveRefresh() {
		return getTreeElementsToRefresh().size() < MAX_INDIVIDUAL_MODEL_OBJECT_REFRESHES;
	}

	@Override
	protected void performSelectiveRefresh(StructuredViewer viewer) {
		for (EObject modelObject : getTreeElementsToRefresh()) {
			if (contentProvider.isPossibleChild(modelObject)) {
				// Is current object a model content root?
				Resource modelResource = EcoreResourceUtil.getResource(modelObject);
				List<Object> modelContentRoots = contentProvider.getModelContentRoots(modelResource);
				if (modelContentRoots.contains(modelObject)) {
					// Refresh corresponding workspace resource
					IResource workspaceResource = contentProvider.getWorkspaceResource(modelResource);
					if (workspaceResource != null && workspaceResource.isAccessible()) {
						if (contentProvider.isTriggerPoint(workspaceResource)) {
							viewer.refresh(workspaceResource, true);
						}
					}
				} else {
					// Directly refresh the object
					viewer.refresh(modelObject, true);
				}
			}
		}
	}
}