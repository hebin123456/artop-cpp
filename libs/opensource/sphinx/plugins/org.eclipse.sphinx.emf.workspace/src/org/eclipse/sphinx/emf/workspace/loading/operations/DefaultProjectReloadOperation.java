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
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.loading.operations;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

public class DefaultProjectReloadOperation extends AbstractProjectLoadOperation implements IProjectReloadOperation {

	public DefaultProjectReloadOperation(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor) {
		super(mmDescriptor != null ? Messages.job_reloadingModel : Messages.job_reloadingModels, projects, includeReferencedProjects, mmDescriptor);
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		runReloadProjects(getProjects(), isIncludeReferencedProjects(), getMetaModelDescriptor(), monitor);
	}

	protected void runReloadProjects(Collection<IProject> projects, boolean includeReferencedProjects, IMetaModelDescriptor mmDescriptor,
			IProgressMonitor monitor) throws OperationCanceledException {
		Assert.isNotNull(projects);

		for (IProject project : projects) {
			String taskName = mmDescriptor != null ? NLS.bind(Messages.task_reloadingModelInProject, mmDescriptor.getName(), project.getName())
					: NLS.bind(Messages.task_reloadingModelsInProject, project.getName());
			SubMonitor progress = SubMonitor.convert(monitor, taskName, 100);
			if (progress.isCanceled()) {
				throw new OperationCanceledException();
			}

			// Collect files in given project and its referenced projects
			Collection<IFile> files = ExtendedPlatform.getAllFiles(project, includeReferencedProjects);
			progress.worked(1);

			// No files found?
			if (files.size() == 0) {
				progress.done();
				return;
			}

			// Reload files; perform memory-optimized unloading if given project is a root project, i.e. is not
			// referenced
			// by any other project
			runDetectAndReloadModelFiles(files, mmDescriptor, ExtendedPlatform.isRootProject(project), progress.newChild(99));
		}
	}
}
