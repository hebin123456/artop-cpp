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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ModelStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.emf.workspace.saving.ModelSaveManager;
import org.eclipse.sphinx.platform.resources.ResourceDeltaFlagsAnalyzer;

/**
 * Makes sure that all dirty models are saved before underlying {@link IProject project} gets closed. Doesn't prompt the
 * user for anything before doing so.
 * <p>
 * TODO In order to prompting user before proceeding with save and enable him to cancel the close operation override
 * ResourceMgmtActionProvider and provide a custom implementation of CloseResourceAction.
 */
public class ModelSaveProvider extends ModelProvider {

	public ModelSaveProvider() {
	}

	@Override
	public IStatus validateChange(IResourceDelta delta, IProgressMonitor monitor) {
		// Validate projects being closed
		IResourceDelta[] childDeltas = delta.getAffectedChildren(IResourceDelta.REMOVED);
		for (IResourceDelta childDelta : childDeltas) {
			IResource childResource = childDelta.getResource();
			ResourceDeltaFlagsAnalyzer flags = new ResourceDeltaFlagsAnalyzer(childDelta);
			if (childResource instanceof IProject) {
				IProject project = (IProject) childResource;
				if (flags.OPEN && project.isOpen()) {
					handleProjectClosed(project);
				}
			}
		}

		return new ModelStatus(IStatus.OK, Activator.getPlugin().getSymbolicName(), getId(), Status.OK_STATUS.getMessage());
	}

	protected void handleProjectClosed(IProject project) {
		// Save dirty models in given project; do it synchronously in order to make sure that model resources still
		// exist while this operation is executed
		ModelSaveManager.INSTANCE.saveProject(project, false, null);
	}
}
