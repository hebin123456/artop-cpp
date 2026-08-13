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
 *     itemis - [409014] Listener URIChangeDetector registered for all transactional editing domains
 *     itemis - [409510] Enable resource scope-sensitive proxy resolutions without forcing metamodel implementations to subclass EObjectImpl
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.sphinx.emf.Activator;
import org.eclipse.sphinx.emf.domain.factory.AbstractResourceSetListenerInstaller;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelper;
import org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelperAdapterFactory;
import org.eclipse.sphinx.emf.model.IModelDescriptor;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.sphinx.platform.util.StatusUtil;

@SuppressWarnings("deprecation")
public class ModelIndexUpdater extends ResourceSetListenerImpl implements IResourceChangeListener {

	public static class ModelIndexUpdaterInstaller extends AbstractResourceSetListenerInstaller<ModelIndexUpdater> {
		public ModelIndexUpdaterInstaller() {
			super(ModelIndexUpdater.class);
		}
	}

	public ModelIndexUpdater() {
		super(NotificationFilter.ANY);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	protected void finalize() throws Throwable {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.finalize();
	}

	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {
		ProxyHelper proxyHelper = ProxyHelperAdapterFactory.INSTANCE.adapt(event.getEditingDomain().getResourceSet());
		List<?> notifications = event.getNotifications();
		for (Object object : notifications) {
			if (object instanceof Notification) {
				Notification notification = (Notification) object;
				Object notifier = notification.getNotifier();
				if (notifier instanceof Resource) {
					Resource resource = (Resource) notifier;
					if (notification.getFeatureID(Resource.class) == Resource.RESOURCE__IS_LOADED) {
						if (resource.isLoaded()) {
							proxyHelper.getBlackList().updateIndexOnResourceLoaded(resource);
						} else {
							// FIXME when called on post commit, resource content is empty
							proxyHelper.getBlackList().updateIndexOnResourceUnloaded(resource);
						}
					}
				} else if (notifier instanceof EObject) {
					// Check if new model objects that are potential targets for black-listed proxy URIs have been added
					EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
					if (feature instanceof EReference) {
						EReference reference = (EReference) feature;
						if (reference.isContainment()) {
							if (notification.getEventType() == Notification.SET || notification.getEventType() == Notification.ADD
									|| notification.getEventType() == Notification.ADD_MANY) {
								// Get black-listed proxy URI pointing at changed model object as well as all
								// black-listed proxy URIs pointing at model objects that are directly and indirectly
								// contained by the former removed
								proxyHelper.getBlackList().updateIndexOnResourceLoaded(((EObject) notifier).eResource());
							}
						}
					}

					// Check if existing model objects have been renamed and thereby became potential targets for
					// black-listed proxy URIs (this is necessary for metamodels that derive proxy URIs from the values
					// of certain string attributes on the target object and/or its containers)
					else if (feature instanceof EAttribute) {
						EAttribute attribute = (EAttribute) feature;
						if (attribute.getEType().getInstanceClass() == String.class) {
							if (notification.getEventType() == Notification.SET) {
								// Get black-listed proxy URI pointing at changed model object as well as all
								// black-listed proxy URIs pointing at model objects that are directly and indirectly
								// contained by the former removed
								proxyHelper.getBlackList().updateIndexOnResourceLoaded(((EObject) notifier).eResource());
							}
						}
					}
				}
			}
		}
	}

	private void handleProjectDescriptionChanged(final IProject project) {
		/*
		 * !! Important Note !! Handle project description change in an asynchronous operation with exclusive access to
		 * the affected project for the following two reasons: 1/ In order to avoid deadlocks. The workspace is locked
		 * while IResourceChangeListeners are processed (exclusive workspace access) and updating the model descriptor
		 * registry may involve creating transactions (exclusive model access). In cases where another thread is around
		 * while we are called here which already has exclusive model access but waits for exclusive workspace access we
		 * would end up in a deadlock otherwise. 2/ In order to make sure that the model descriptor registry gets
		 * updated only AFTER all other IResourceChangeListeners have been processed which may be present and rely on
		 * the model descriptor registry's state BEFORE the update.
		 */
		if (project != null) {
			// TODO externalize job label string
			Job job = new Job("Update model index") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						HashSet<Resource> updatedResources = new HashSet<Resource>();
						IProjectDescription description = project.getDescription();
						if (description != null) {
							IProject[] referencedProjects = description.getReferencedProjects();
							for (IProject refrencedProject : referencedProjects) {
								Collection<IModelDescriptor> projectModels = ModelDescriptorRegistry.INSTANCE.getModels(refrencedProject);
								for (IModelDescriptor modelDescriptor : projectModels) {
									updatedResources.addAll(modelDescriptor.getLoadedResources(true));
								}
							}

						}
						for (Resource resource : updatedResources) {
							if (resource.isLoaded()) {
								ProxyHelper proxyHelper = ProxyHelperAdapterFactory.INSTANCE.adapt(resource.getResourceSet());
								proxyHelper.getBlackList().updateIndexOnResourceLoaded(resource);
							}
						}
						return Status.OK_STATUS;
					} catch (OperationCanceledException ex) {
						return Status.CANCEL_STATUS;
					} catch (CoreException ex) {
						return ex.getStatus();
					} catch (Exception ex) {
						return StatusUtil.createErrorStatus(Activator.getPlugin(), ex);
					}
				}

				@Override
				public boolean belongsTo(Object family) {
					return IExtendedPlatformConstants.FAMILY_MODEL_LOADING.equals(family)
							|| IExtendedPlatformConstants.FAMILY_LONG_RUNNING.equals(family);
				}
			};
			job.setPriority(Job.BUILD);
			job.setRule(project);
			job.setSystem(true);
			job.schedule();
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDeltaVisitor visitor = new ResourceDeltaVisitor(event.getType(), new DefaultResourceChangeHandler() {
					@Override
					public void handleProjectDescriptionChanged(int eventType, IProject project) {
						ModelIndexUpdater.this.handleProjectDescriptionChanged(project);
					}
				});
				delta.accept(visitor);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}
}
