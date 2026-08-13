/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.check.ui.markers.tests;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sphinx.emf.check.ICheckValidationMarker;
import org.eclipse.ui.statushandlers.StatusManager;

public class DeleteErrorMarkersHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IMarker[] markers = root.findMarkers(ICheckValidationMarker.CHECK_VALIDATION_PROBLEM, false, IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				marker.delete();
			}
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
		}
		return this;
	}
}