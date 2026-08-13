/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *     BMW Car IT - Avoid usage of Object.finalize
 *     itemis - [409014] Listener URIChangeDetector registered for all transactional editing domains
 *     Elektrobit - [570596] - Eliminate risk of deadlocks related to ResourceProblemHandler
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.domain.factory.AbstractResourceSetListenerInstaller;
import org.eclipse.sphinx.emf.internal.messages.Messages;
import org.eclipse.sphinx.emf.saving.SaveIndicatorUtil;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.WorkspaceEditingDomainUtil;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler;
import org.eclipse.sphinx.platform.resources.MarkerJob;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

/**
 * Listens for {@link Resource resource}s that have been loaded or saved and requests the problem markers of underlying
 * {@link IFile file}s to be updated according to the {@link Resource#getErrors() errors} and
 * {@link Resource#getWarnings() warnings} of each loaded or saved {@link Resource} resource.
 *
 * @see ResourceProblemMarkerService#updateProblemMarkers(Collection, boolean,
 *      org.eclipse.core.runtime.IProgressMonitor)
 */
public class ResourceProblemHandler extends ResourceSetListenerImpl implements IResourceChangeListener {

	public static class ResourceProblemHandlerInstaller extends AbstractResourceSetListenerInstaller<ResourceProblemHandler> {
		public ResourceProblemHandlerInstaller() {
			super(ResourceProblemHandler.class);
		}
	}

	/**
	 * Default constructor.
	 */
	public ResourceProblemHandler() {
		super(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__IS_LOADED)
				.or(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResourceSet(), ResourceSet.RESOURCE_SET__RESOURCES)));
	}

	@Override
	public void setTarget(TransactionalEditingDomain domain) {
		super.setTarget(domain);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	public void unsetTarget(TransactionalEditingDomain domain) {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

		super.unsetTarget(domain);
	}

	/*
	 * @see org.eclipse.emf.transaction.ResourceSetListenerImpl#resourceSetChanged(org.eclipse.emf.transaction.
	 * ResourceSetChangeEvent)
	 */
	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {
		Set<Resource> loadedResources = new HashSet<Resource>();

		// Analyze notifications for loaded resources; record loaded resources regardless of whether or not they have
		// got unloaded subsequently or not
		for (Notification notification : event.getNotifications()) {
			Object notifier = notification.getNotifier();
			if (notifier instanceof Resource) {
				Resource resource = (Resource) notifier;
				Boolean newValue = (Boolean) notification.getNewValue();
				if (newValue) {
					loadedResources.add(resource);
				}
			} else if (notifier instanceof ResourceSet) {
				Object newValue = notification.getNewValue();

				if (notification.getEventType() == Notification.ADD || notification.getEventType() == Notification.ADD_MANY) {
					List<Resource> newResources = new ArrayList<Resource>();
					if (newValue instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<Resource> newResourcesValue = (List<Resource>) newValue;
						newResources.addAll(newResourcesValue);
					} else if (newValue instanceof Resource) {
						newResources.add((Resource) newValue);
					}

					loadedResources.addAll(newResources);
				}
			}
		}

		// Handle loaded resources
		handleLoadedResources(loadedResources);
	}

	/*
	 * @see org.eclipse.emf.transaction.ResourceSetListenerImpl#isPostcommitOnly()
	 */
	@Override
	public boolean isPostcommitOnly() {
		return true;
	}

	protected void handleLoadedResources(Collection<Resource> resources) {
		if (!resources.isEmpty()) {
			Job job = new Job(Messages.job_addingProblemMarkers) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourceProblemMarkerService.INSTANCE.addProblemMarkers(resources, monitor);
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
			};
			job.setPriority(Job.SHORT);
			job.setRule(ExtendedPlatform.createModifySchedulingRule(getFiles(resources)));
			job.setSystem(true);
			job.schedule();
		}
	}

	private Collection<IFile> getFiles(Collection<Resource> resources) {
		Collection<IFile> files = new ArrayList<>();
		for (Resource resource : resources) {
			IFile file = EcorePlatformUtil.getFile(resource);
			files.add(file);
		}
		return files;
	}

	/*
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				final Set<IFile> changedFiles = new HashSet<IFile>();
				final Set<IFile> savedFiles = new HashSet<IFile>();

				// Investigate resource delta on saved files
				IResourceDeltaVisitor visitor = new ResourceDeltaVisitor(event.getType(), new DefaultResourceChangeHandler() {
					@Override
					public void handleFileChanged(int eventType, IFile file) {
						changedFiles.add(file);

						/*
						 * !! Important Note !! We must not try to obtain the model resource behind the changed file in
						 * the present execution context. This would require requesting exclusive access to underlying
						 * editing domain by creating a read transaction. However, the workspace is locked during
						 * resource change event processing. Any attempt of obtaining exclusive editing domain access
						 * while this is the case would therefore introduce a major risk of deadlocks. Some other thread
						 * might be waiting for exclusive workspace access but already have exclusive editing domain
						 * access.
						 */
						TransactionalEditingDomain editingDomain = WorkspaceEditingDomainUtil.getMappedEditingDomain(file);
						URI uri = EcorePlatformUtil.createURI(file.getFullPath());
						if (SaveIndicatorUtil.isSaved(editingDomain, uri)) {
							savedFiles.add(file);
						}
					}
				});
				delta.accept(visitor);

				// Handle changed files
				handleChangedFiles(changedFiles);

				// Handle saved files
				handleSavedFiles(savedFiles);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	protected void handleChangedFiles(Collection<IFile> files) {
		Assert.isNotNull(files);

		if (files.size() > 0) {
			/*
			 * !! Important Note !! Perform as asynchronous operation with exclusive access to the affected files in
			 * order to avoid deadlocks. The workspace is locked while IResourceChangeListeners are processed (exclusive
			 * workspace access) and updating a resource's problem markers may involve creating transactions (exclusive
			 * model access). In cases where another thread is around while we are called here which already has
			 * exclusive model access but waits for exclusive workspace access we would end up in a deadlock otherwise.
			 */
			Job job = new Job(Messages.job_removingProblemMarkers) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourceProblemMarkerService.INSTANCE.removeProblemMarkers(files, monitor);
						MarkerJob.INSTANCE.schedule();
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
			};
			job.setPriority(Job.SHORT);
			job.setRule(ExtendedPlatform.createModifySchedulingRule(files));
			job.setSystem(true);
			job.schedule();
		}
	}

	protected void handleSavedFiles(Collection<IFile> files) {
		Assert.isNotNull(files);

		if (files.size() > 0) {
			/*
			 * !! Important Note !! Perform as asynchronous operation with exclusive access to the affected files in
			 * order to avoid deadlocks. The workspace is locked while IResourceChangeListeners are processed (exclusive
			 * workspace access) and updating a resource's problem markers may involve creating transactions (exclusive
			 * model access). In cases where another thread is around while we are called here which already has
			 * exclusive model access but waits for exclusive workspace access we would end up in a deadlock otherwise.
			 */
			Job job = new Job(Messages.job_addingProblemMarkers) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						Set<Resource> resources = new HashSet<Resource>();
						for (IFile file : files) {
							Resource resource = EcorePlatformUtil.getResource(file);
							if (resource != null) {
								resources.add(resource);
							}
						}

						ResourceProblemMarkerService.INSTANCE.addProblemMarkers(resources, monitor);
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
			};
			job.setPriority(Job.SHORT);
			job.setRule(ExtendedPlatform.createModifySchedulingRule(files));
			job.setSystem(true);
			job.schedule();
		}
	}
}