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
package org.eclipse.sphinx.emf.workspace.loading;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.workspace.internal.loading.ModelLoadJob;
import org.eclipse.sphinx.emf.workspace.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileReloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IFileUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.ILoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IModelUnloadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectLoadOperation;
import org.eclipse.sphinx.emf.workspace.loading.operations.IProjectReloadOperation;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;

@SuppressWarnings("rawtypes")
public class LoadJobScheduler {

	public void scheduleModelLoadJob(ILoadOperation operation) {
		if (operation instanceof IFileLoadOperation) {
			scheduleModelLoadJob((IFileLoadOperation) operation);
		} else if (operation instanceof IProjectLoadOperation) {
			scheduleModelLoadJob((IProjectLoadOperation) operation);
		} else if (operation instanceof IModelLoadOperation) {
			scheduleModelLoadJob((IModelLoadOperation) operation);
		} else if (operation instanceof IFileReloadOperation) {
			scheduleModelLoadJob((IFileReloadOperation) operation);
		} else if (operation instanceof IProjectReloadOperation) {
			scheduleModelLoadJob((IProjectReloadOperation) operation);
		} else if (operation instanceof IFileUnloadOperation) {
			scheduleModelLoadJob((IFileUnloadOperation) operation);
		} else if (operation instanceof IModelUnloadOperation) {
			scheduleModelLoadJob((IModelUnloadOperation) operation);
		} else {
			throw new UnsupportedOperationException(NLS.bind(Messages.error_unsupportedLoadOperation, operation.getClass().getSimpleName()));
		}
	}

	protected void scheduleModelLoadJob(IFileLoadOperation fileLoadOperation) {
		// Check first if an existing IFileLoadOperation job covers the files
		if (coveredByExistingLoadJob(fileLoadOperation)) {
			return;
		}

		// Add the files to an existing scheduled job if any. Otherwise, create new job.
		if (!addToExistingLoadJob(fileLoadOperation)) {
			Job job = createModelLoadJob(fileLoadOperation);
			job.setPriority(Job.BUILD);
			job.setRule(fileLoadOperation.getRule());
			job.schedule();
		}
	}

	protected void scheduleModelLoadJob(IProjectLoadOperation prjLoadOperation) {
		// Check first if an existing IProjectLoadOperation job covers the projects
		if (coveredByExistingLoadJob(prjLoadOperation)) {
			return;
		}
		// Add the projects to an existing scheduled job if any. Otherwise, create new job.
		if (!addToExistingLoadJob(prjLoadOperation)) {
			Job job = createModelLoadJob(prjLoadOperation);
			job.setPriority(Job.BUILD);
			job.setRule(prjLoadOperation.getRule());
			job.schedule();
		}
	}

	protected void scheduleModelLoadJob(IModelLoadOperation modelLoadOperation) {
		// Check first if an existing IModelLoadOperation job covers the model
		if (coveredByExistingLoadJob(modelLoadOperation)) {
			return;
		}

		// Otherwise, create new job
		Job job = createModelLoadJob(modelLoadOperation);
		job.setPriority(Job.BUILD);
		job.setRule(modelLoadOperation.getRule());
		job.schedule();
	}

	protected void scheduleModelLoadJob(IFileReloadOperation fileReloadOperation) {
		// Check first if an existing IFileReloadOperation job covers the files
		if (coveredByExistingReloadJob(fileReloadOperation)) {
			return;
		}

		// Add the files to an existing scheduled job if any. Otherwise, create new job.
		if (!addToExistingReLoadJob(fileReloadOperation)) {
			Job job = createModelLoadJob(fileReloadOperation);
			job.setPriority(Job.BUILD);
			job.setRule(fileReloadOperation.getRule());
			job.schedule();
		}
	}

	protected void scheduleModelLoadJob(IProjectReloadOperation projectReloadOperation) {
		// Check first if an existing IProjectReloadOperation job covers the projects
		if (coveredByExistingReloadJob(projectReloadOperation)) {
			return;
		}

		// Add the projects to an existing scheduled job if any. Otherwise, create new job.
		if (!addToExistingReLoadJob(projectReloadOperation)) {
			Job job = createModelLoadJob(projectReloadOperation);
			job.setPriority(Job.BUILD);
			job.setRule(projectReloadOperation.getRule());
			job.schedule();
		}
	}

	protected void scheduleModelLoadJob(IFileUnloadOperation fileUnloadOperation) {
		Job job = createModelLoadJob(fileUnloadOperation);
		job.setPriority(Job.BUILD);
		job.setRule(fileUnloadOperation.getRule());
		job.schedule();
	}

	protected void scheduleModelLoadJob(IModelUnloadOperation modelUnloadOperation) {
		Job job = createModelLoadJob(modelUnloadOperation);
		job.setPriority(Job.BUILD);
		job.setRule(modelUnloadOperation.getRule());
		job.schedule();
	}

	/**
	 * @param fileLoadOperation
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public boolean coveredByExistingLoadJob(IFileLoadOperation fileLoadOperation) {
		if (fileLoadOperation != null) {
			Collection<IFile> files = fileLoadOperation.getFiles();
			IMetaModelDescriptor metaModelDescriptor = fileLoadOperation.getMetaModelDescriptor();
			for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
				if (job instanceof ModelLoadJob) {
					ModelLoadJob loadJob = (ModelLoadJob) job;
					ILoadOperation operation = loadJob.getOperation();
					if ((operation instanceof IFileLoadOperation || operation instanceof IProjectLoadOperation)
							&& loadJob.covers(files, metaModelDescriptor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param prjLoadOperation
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public boolean coveredByExistingLoadJob(IProjectLoadOperation prjLoadOperation) {
		if (prjLoadOperation != null) {
			Collection<IProject> projects = prjLoadOperation.getProjects();
			boolean includeReferencedProjects = prjLoadOperation.isIncludeReferencedProjects();
			IMetaModelDescriptor metaModelDescriptor = prjLoadOperation.getMetaModelDescriptor();
			for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
				if (job instanceof ModelLoadJob) {
					ModelLoadJob loadJob = (ModelLoadJob) job;
					ILoadOperation operation = loadJob.getOperation();
					if (operation instanceof IProjectLoadOperation && loadJob.covers(projects, includeReferencedProjects, metaModelDescriptor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param modelLoadOperation
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public boolean coveredByExistingLoadJob(IModelLoadOperation modelLoadOperation) {
		if (modelLoadOperation != null) {
			final IModelDescriptor modelDescriptor = modelLoadOperation.getModelDescriptor();
			final boolean includeReferencedProjects = modelLoadOperation.isIncludeReferencedScopes();
			final Collection<IFile> persistedFiles = modelDescriptor.getPersistedFiles(includeReferencedProjects);
			IMetaModelDescriptor metaModelDescriptor = modelLoadOperation.getMetaModelDescriptor();
			for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
				if (job instanceof ModelLoadJob) {
					ModelLoadJob loadJob = (ModelLoadJob) job;
					ILoadOperation operation = loadJob.getOperation();
					if (operation instanceof IModelLoadOperation && loadJob.covers(persistedFiles, metaModelDescriptor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param fileReloadOperation
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public boolean coveredByExistingReloadJob(IFileReloadOperation fileReloadOperation) {
		if (fileReloadOperation != null) {
			Collection<IFile> files = fileReloadOperation.getFiles();
			IMetaModelDescriptor metaModelDescriptor = fileReloadOperation.getMetaModelDescriptor();
			for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
				if (job instanceof ModelLoadJob) {
					ModelLoadJob loadJob = (ModelLoadJob) job;
					ILoadOperation operation = loadJob.getOperation();
					if ((operation instanceof IFileReloadOperation || operation instanceof IProjectReloadOperation)
							&& loadJob.covers(files, metaModelDescriptor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param projectReloadOperation
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public boolean coveredByExistingReloadJob(IProjectReloadOperation projectReloadOperation) {
		if (projectReloadOperation != null) {
			Collection<IProject> projects = projectReloadOperation.getProjects();
			boolean includeReferencedProjects = projectReloadOperation.isIncludeReferencedProjects();
			IMetaModelDescriptor metaModelDescriptor = projectReloadOperation.getMetaModelDescriptor();
			for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
				if (job instanceof ModelLoadJob) {
					ModelLoadJob loadJob = (ModelLoadJob) job;
					ILoadOperation operation = loadJob.getOperation();
					if (operation instanceof IProjectReloadOperation && loadJob.covers(projects, includeReferencedProjects, metaModelDescriptor)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean addToExistingLoadJob(IFileLoadOperation fileLoadOperation) {
		for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
			if (job instanceof ModelLoadJob && job.getState() != Job.RUNNING) {
				ILoadOperation operation = ((ModelLoadJob) job).getOperation();
				if (operation instanceof IFileLoadOperation) {
					((IFileLoadOperation) operation).addFiles(fileLoadOperation.getFiles());
					return true;
				}
			}
		}
		return false;
	}

	private boolean addToExistingLoadJob(IProjectLoadOperation prjLoadOperation) {
		for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
			if (job instanceof ModelLoadJob && job.getState() != Job.RUNNING) {
				ILoadOperation operation = ((ModelLoadJob) job).getOperation();
				if (operation instanceof IProjectLoadOperation) {
					((IProjectLoadOperation) operation).addProjects(prjLoadOperation.getProjects());
					return true;
				}
			}
		}
		return false;
	}

	private boolean addToExistingReLoadJob(IFileReloadOperation fileReloadOperation) {
		for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
			if (job instanceof ModelLoadJob) {
				ILoadOperation operation = ((ModelLoadJob) job).getOperation();
				if (operation instanceof IFileReloadOperation && job.getState() != Job.RUNNING) {
					((IFileReloadOperation) operation).addFiles(fileReloadOperation.getFiles());
					return true;
				}
			}
		}
		return false;
	}

	private boolean addToExistingReLoadJob(IProjectReloadOperation projectReloadOperation) {
		for (Job job : Job.getJobManager().find(IExtendedPlatformConstants.FAMILY_MODEL_LOADING)) {
			if (job instanceof ModelLoadJob && job.getState() != Job.RUNNING) {
				ILoadOperation operation = ((ModelLoadJob) job).getOperation();
				if (operation instanceof IProjectReloadOperation) {
					((IProjectReloadOperation) operation).addProjects(projectReloadOperation.getProjects());
					return true;
				}
			}
		}
		return false;
	}

	private <T extends ILoadOperation> Job createModelLoadJob(T operation) {
		return new ModelLoadJob<T>(operation);
	}
}
