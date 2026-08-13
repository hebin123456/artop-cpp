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
package org.eclipse.sphinx.emf.internal.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
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
import org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.StatusUtil;

public class ModelDescriptorSyncRequest implements IModelDescriptorSyncRequest {

	private Map<IProject, IProject> projectsToMoveModelDescriptorsFor = new HashMap<IProject, IProject>();

	private Set<IFile> filesToAddModelDescriptorsFor = new HashSet<IFile>();

	private Set<IFile> filesToUpdateModelDescriptorsFor = new HashSet<IFile>();

	private Set<IModelDescriptor> modelDescriptorsToRemove = new HashSet<IModelDescriptor>();

	@Override
	public void init() {
		// Nothing to do
	}

	@Override
	public void addProjectToMoveModelDescriptorsFor(IProject oldProject, IProject newProject) {
		if (oldProject != null && newProject != null) {
			IProject storedNewProject = projectsToMoveModelDescriptorsFor.get(oldProject);
			if (storedNewProject != null) {
				projectsToMoveModelDescriptorsFor.put(oldProject, newProject);
			} else {
				if (!newProject.equals(storedNewProject)) {
					projectsToMoveModelDescriptorsFor.remove(oldProject);
					projectsToMoveModelDescriptorsFor.put(oldProject, newProject);
				}
			}
		}
	}

	@Override
	public void addProjectToRemoveModelDescriptorsFor(IProject project) {
		if (project != null) {
			Collection<IModelDescriptor> modelDescriptorsForProject = ModelDescriptorRegistry.INSTANCE.getModels(project);
			if (modelDescriptorsForProject != null && !modelDescriptorsForProject.isEmpty()) {
				modelDescriptorsToRemove.addAll(modelDescriptorsForProject);
			}
		}
	}

	@Override
	public void addFileToAddModelDescriptorFor(IFile file) {
		if (file != null) {
			// Exclude obvious non-model files and model files which are out of scope
			if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
				filesToAddModelDescriptorsFor.add(file);
			}
		}
	}

	@Override
	public void addFileToUpdateModelDescriptorFor(IFile file) {
		if (file != null) {
			// Exclude obvious non-model files and model files which are out of scope
			if (!ResourceScopeProviderRegistry.INSTANCE.isNotInAnyScope(file)) {
				filesToUpdateModelDescriptorsFor.add(file);
			}
		}
	}

	@Override
	public void addFileToRemoveModelDescriptorFor(IFile file) {
		if (file != null) {
			IModelDescriptor modelDescriptor = ModelDescriptorRegistry.INSTANCE.getOldModel(file);
			if (modelDescriptor != null) {
				Collection<IResource> referencingRoots = modelDescriptor.getReferencingRoots();
				for (IResource referencingRoot : referencingRoots) {
					Collection<IModelDescriptor> referencingModelDescriptors = new HashSet<IModelDescriptor>();

					if (referencingRoot instanceof IContainer) {
						referencingModelDescriptors = ModelDescriptorRegistry.INSTANCE.getModels((IContainer) referencingRoot,
								modelDescriptor.getMetaModelDescriptor());
					} else if (referencingRoot instanceof IFile) {
						IModelDescriptor model = ModelDescriptorRegistry.INSTANCE.getModel((IFile) referencingRoot);
						if (model.getMetaModelDescriptor().equals(modelDescriptor.getMetaModelDescriptor())) {
							referencingModelDescriptors.add(model);
						}
					}
					if (referencingModelDescriptors != null) {
						modelDescriptorsToRemove.addAll(referencingModelDescriptors);
					}
				}
				modelDescriptorsToRemove.add(modelDescriptor);
			}
		}
	}

	@Override
	public boolean canPerform() {
		return projectsToMoveModelDescriptorsFor.size() > 0 || filesToAddModelDescriptorsFor.size() > 0
				|| filesToUpdateModelDescriptorsFor.size() > 0 || modelDescriptorsToRemove.size() > 0;
	}

	@Override
	public void perform() {
		if (!canPerform()) {
			return;
		}
		if (projectsToMoveModelDescriptorsFor.size() > 0) {
			moveModelDescriptors(new HashMap<IProject, IProject>(projectsToMoveModelDescriptorsFor));
			projectsToMoveModelDescriptorsFor.clear();
		}
		if (filesToAddModelDescriptorsFor.size() > 0) {
			addModelDescriptors(new HashSet<IFile>(filesToAddModelDescriptorsFor));
			filesToAddModelDescriptorsFor.clear();
		}
		if (filesToUpdateModelDescriptorsFor.size() > 0) {
			updateModelDescriptors(new HashSet<IFile>(filesToUpdateModelDescriptorsFor));
			filesToUpdateModelDescriptorsFor.clear();
		}
		if (modelDescriptorsToRemove.size() > 0) {
			removeModelDescriptors(new HashSet<IModelDescriptor>(modelDescriptorsToRemove));
			modelDescriptorsToRemove.clear();
		}
	}

	@Override
	public void dispose() {
		projectsToMoveModelDescriptorsFor.clear();
		filesToAddModelDescriptorsFor.clear();
		filesToUpdateModelDescriptorsFor.clear();
		modelDescriptorsToRemove.clear();
	}

	private void addModelDescriptors(final Set<IFile> files) {
		Assert.isNotNull(files);

		if (files.size() > 0) {
			/*
			 * !! Important Note !! Perform as asynchronous operation with exclusive access to the affected files for
			 * the following two reasons: 1/ In order to avoid deadlocks. The workspace is locked while
			 * IResourceChangeListeners are processed (exclusive workspace access) and updating the model descriptor
			 * registry may involve creating transactions (exclusive model access). In cases where another thread is
			 * around while we are called here which already has exclusive model access but waits for exclusive
			 * workspace access we would end up in a deadlock otherwise. 2/ In order to make sure that the model
			 * descriptor registry gets updated only AFTER all other IResourceChangeListeners have been processed which
			 * may be present and rely on the model descriptor registry's state BEFORE the update.
			 */
			Job job = new Job(Messages.job_addingModelDescriptors) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						SubMonitor progress = SubMonitor.convert(monitor, files.size());
						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}

						for (IFile file : files) {
							ModelDescriptorRegistry.INSTANCE.addModel(file);

							progress.worked(1);
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
						}

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
			job.setRule(ExtendedPlatform.createModifySchedulingRule(files));
			job.setSystem(true);
			job.schedule();
		}
	}

	private void moveModelDescriptors(final Map<IProject, IProject> projects) {
		Assert.isNotNull(projects);

		if (projects.size() > 0) {
			/*
			 * !! Important Note !! Perform as asynchronous operation with exclusive access to workspace root for the
			 * following two reasons: 1/ In order to avoid deadlocks. The workspace is locked while
			 * IResourceChangeListeners are processed (exclusive workspace access) and updating the model descriptor
			 * registry may involve creating transactions (exclusive model access). In cases where another thread is
			 * around while we are called here which already has exclusive model access but waits for exclusive
			 * workspace access we would end up in a deadlock otherwise. 2/ In order to make sure that the model
			 * descriptor registry gets updated only AFTER all other IResourceChangeListeners have been processed which
			 * may be present and rely on the model descriptor registry's state BEFORE the update.
			 */
			Job job = new Job(Messages.job_movingModelDescriptors) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						SubMonitor progress = SubMonitor.convert(monitor, projects.size());
						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}

						for (IProject oldProject : projects.keySet()) {
							IProject newProject = projects.get(oldProject);
							ModelDescriptorRegistry.INSTANCE.moveModels(oldProject, newProject);

							progress.worked(1);
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
						}

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
			job.setRule(ResourcesPlugin.getWorkspace().getRoot());
			job.setSystem(true);
			job.schedule();
		}
	}

	private void updateModelDescriptors(final Set<IFile> files) {
		Assert.isNotNull(files);

		if (files.size() > 0) {
			/*
			 * !! Important Note !! Perform as asynchronous operation with exclusive access to the affected files for
			 * the following two reasons: 1/ In order to avoid deadlocks. The workspace is locked while
			 * IResourceChangeListeners are processed (exclusive workspace access) and updating the model descriptor
			 * registry may involve creating transactions (exclusive model access). In cases where another thread is
			 * around while we are called here which already has exclusive model access but waits for exclusive
			 * workspace access we would end up in a deadlock otherwise. 2/ In order to make sure that the model
			 * descriptor registry gets updated only AFTER all other IResourceChangeListeners have been processed which
			 * may be present and rely on the model descriptor registry's state BEFORE the update.
			 */
			Job job = new Job(Messages.job_updatingModelDescriptors) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						SubMonitor progress = SubMonitor.convert(monitor, files.size());
						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}

						for (IFile file : files) {
							ModelDescriptorRegistry.INSTANCE.removeModel(file);
							ModelDescriptorRegistry.INSTANCE.addModel(file);

							progress.worked(1);
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
						}

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
			job.setRule(ExtendedPlatform.createModifySchedulingRule(files));
			job.setSystem(true);
			job.schedule();
		}
	}

	private void removeModelDescriptors(final Set<IModelDescriptor> modelDescriptors) {
		Assert.isNotNull(modelDescriptors);

		if (modelDescriptors.size() > 0) {
			/*
			 * !! Important Note !! Perform as asynchronous operation with exclusive access to workspace root for the
			 * following two reasons: 1/ In order to avoid deadlocks. The workspace is locked while
			 * IResourceChangeListeners are processed (exclusive workspace access) and updating the model descriptor
			 * registry may involve creating transactions (exclusive model access). In cases where another thread is
			 * around while we are called here which already has exclusive model access but waits for exclusive
			 * workspace access we would end up in a deadlock otherwise. 2/ In order to make sure that the model
			 * descriptor registry gets updated only AFTER all other IResourceChangeListeners have been processed which
			 * may be present and rely on the model descriptor registry's state BEFORE the update.
			 */
			Job job = new Job(Messages.job_removingModelDescriptors) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						SubMonitor progress = SubMonitor.convert(monitor, modelDescriptors.size());
						if (progress.isCanceled()) {
							throw new OperationCanceledException();
						}

						for (IModelDescriptor modelDescriptor : modelDescriptors) {
							ModelDescriptorRegistry.INSTANCE.removeModel(modelDescriptor);

							progress.worked(1);
							if (progress.isCanceled()) {
								throw new OperationCanceledException();
							}
						}

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
			job.setRule(ResourcesPlugin.getWorkspace().getRoot());
			job.setSystem(true);
			job.schedule();
		}
	}
}
