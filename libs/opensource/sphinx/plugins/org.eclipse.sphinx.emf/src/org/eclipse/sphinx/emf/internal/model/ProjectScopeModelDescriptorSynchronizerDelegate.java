/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423662] Prevent creation of duplicated model descriptors by design
 *     itemis - [425854] The diagram created in the Artop is not saved after being updated to "sphinx-Update-0.8.0M4".
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizerDelegate;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class ProjectScopeModelDescriptorSynchronizerDelegate extends AbstractResourceSynchronizerDelegate<IModelDescriptorSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final ProjectScopeModelDescriptorSynchronizerDelegate INSTANCE = new ProjectScopeModelDescriptorSynchronizerDelegate();

	@Override
	public void handleProjectDescriptionChanged(int eventType, IProject project) {
		if (eventType == IResourceChangeEvent.POST_CHANGE) {
			updateReferencedModels(project);
		}
	}

	protected void updateReferencedModels(final IProject project) {
		Job job = new Job(Messages.job_updatingReferencedModelDescriptors) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					SubMonitor progress = SubMonitor.convert(monitor, 2);
					// Remove descriptors of obsolete models on affected project and referencing projects in case that
					// project reference(s) have been removed
					ModelDescriptorRegistry.INSTANCE.removeModels(project);
					ModelDescriptorRegistry.INSTANCE.addModels(project);

					// Add descriptors for models on referenced projects in case that new project reference(s) have been
					// added
					for (IProject referencedProject : ExtendedPlatform.getReferencedProjectsSafely(project)) {
						for (IModelDescriptor referencedModelDescriptor : ModelDescriptorRegistry.INSTANCE.getModels(referencedProject)) {
							ModelDescriptorRegistry.INSTANCE.addModel(referencedModelDescriptor.getMetaModelDescriptor(),
									referencedModelDescriptor.getTargetMetaModelDescriptor(), project);
						}
					}
					progress.worked(1);

					return Status.OK_STATUS;
				} catch (OperationCanceledException ex) {
					return Status.CANCEL_STATUS;
				} catch (Exception ex) {
					return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
				}
			}

			@Override
			public boolean belongsTo(Object family) {
				return IExtendedPlatformConstants.FAMILY_MODEL_LOADING.equals(family);
			}
		};
		job.setPriority(Job.SHORT);
		job.setRule(project);
		job.setSystem(true);
		job.schedule();
	}
}
