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
package org.eclipse.sphinx.emf.internal.model;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.internal.scoping.ResourceScopeValidationService;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.resources.MarkerJob;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * A {@link Job job} that enables the {@link ModelDescriptorRegistry#INSTANCE} to be initialized with
 * {@link IModelDescriptor)s according to the {@link IFile model files} that exist in the workspace.
 * <p>
 * The {@link ModelDescriptorRegistryInitializer} job typically needs to be executed only once after workbench startup.
 * It is therefore automatically invoked when the
 * {@link Activator.Implementation#start(org.osgi.framework.BundleContext) activator} of this plug-in is started. All
 * subsequent synchronization of the {@link ModelDescriptorRegistry#INSTANCE} wrt resource changes in the workspace are
 * handled by the {@link ModelDescriptorSynchronizer#INSTANCE}.
 * </p>
 * 
 * @see ModelDescriptorSynchronizer
 */
public class ModelDescriptorRegistryInitializer extends Job {

	public ModelDescriptorRegistryInitializer() {
		super(Messages.job_initializingModelDescriptorRegistry);
		setPriority(Job.BUILD);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			SubMonitor progress = SubMonitor.convert(monitor, 100);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			Collection<IFile> analyzedFiles = new HashSet<IFile>();
			Collection<IProject> rootProjects = ExtendedPlatform.getRootProjects();

			SubMonitor progress1 = progress.newChild(90);
			progress1.beginTask(Messages.task_analyzingProjects, rootProjects.size());
			for (IProject project : rootProjects) {
				Collection<IFile> files = ExtendedPlatform.getAllFiles(project, true);

				SubMonitor progress2 = progress1.newChild(1).setWorkRemaining(files.size());
				for (IFile file : files) {
					progress2.subTask(NLS.bind(Messages.subtask_analyzingFile, file.getFullPath().toString()));

					if (analyzedFiles.add(file)) {
						// Don't create model descriptors for files that do not exist at this point
						if (file.isAccessible()) {
							ModelDescriptorRegistry.INSTANCE.addModel(file);
						}
					}

					progress2.worked(1);
					if (progress2.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
			}

			// schedule the marker job in order to process markers that might have been created in
			// org.eclipse.sphinx.emf.model.ModelDescriptorRegistry.addModel(IFile)
			MarkerJob.INSTANCE.schedule();

			ResourceScopeValidationService.INSTANCE.validateFiles(analyzedFiles, progress.newChild(10));

			ExtendedPlatform.persistContentTypeIdProperties(analyzedFiles, true, null);

			return Status.OK_STATUS;
		} catch (OperationCanceledException ex) {
			return Status.CANCEL_STATUS;
		} catch (Exception ex) {
			return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
		}
	}

	@Override
	public boolean belongsTo(Object family) {
		return IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
	}
}
