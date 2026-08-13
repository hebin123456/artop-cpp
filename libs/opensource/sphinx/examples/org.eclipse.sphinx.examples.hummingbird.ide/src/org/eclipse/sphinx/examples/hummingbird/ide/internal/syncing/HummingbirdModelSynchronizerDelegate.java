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
package org.eclipse.sphinx.examples.hummingbird.ide.internal.syncing;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.syncing.IModelSyncRequest;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird.ide.natures.HummingbirdNature;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizerDelegate;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class HummingbirdModelSynchronizerDelegate extends AbstractResourceSynchronizerDelegate<IModelSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final HummingbirdModelSynchronizerDelegate INSTANCE = new HummingbirdModelSynchronizerDelegate();

	@Override
	public void handleProjectDescriptionChanged(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			try {
				if (project.hasNature(HummingbirdNature.ID)
						&& ModelDescriptorRegistry.INSTANCE.getModels(project, HummingbirdMMDescriptor.INSTANCE).isEmpty()) {
					// TODO Enhance IModelSyncRequest such that handling of individual models in projects becomes
					// possible
					syncRequest.addProjectToLoad(project);
				}
				if (!project.hasNature(HummingbirdNature.ID)
						&& !ModelDescriptorRegistry.INSTANCE.getModels(project, HummingbirdMMDescriptor.INSTANCE).isEmpty()) {
					// TODO Enhance IModelSyncRequest such that handling of individual models in projects becomes
					// possible
					syncRequest.addProjectToUnload(project);
				}
			} catch (CoreException ex) {
				PlatformLogUtil.logAsError(Activator.getPlugin(), ex);
			}
		}
	}
}
