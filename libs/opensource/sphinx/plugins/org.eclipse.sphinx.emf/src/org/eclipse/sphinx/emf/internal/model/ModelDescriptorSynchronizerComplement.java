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
 *     itemis - [423669] Asynchronous cleanup of old metamodel descriptor cache occasionally done too early
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.sphinx.emf.domain.factory.AbstractResourceSetListenerInstaller;
import org.eclipse.sphinx.emf.internal.metamodel.InternalMetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.model.ModelDescriptorRegistry;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;

public class ModelDescriptorSynchronizerComplement extends ResourceSetListenerImpl {

	public static class ModelDescriptorSynchronizerComplementInstaller extends
			AbstractResourceSetListenerInstaller<ModelDescriptorSynchronizerComplement> {
		public ModelDescriptorSynchronizerComplementInstaller() {
			super(ModelDescriptorSynchronizerComplement.class);
		}
	}

	public ModelDescriptorSynchronizerComplement() {
		super(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__IS_LOADED).or(
				NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResourceSet(), ResourceSet.RESOURCE_SET__RESOURCES)));
	}

	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {
		Set<Resource> loadedResources = new HashSet<Resource>();
		Set<Resource> unloadedResources = new HashSet<Resource>();
		Set<Resource> addedResources = new HashSet<Resource>();
		Set<Resource> removedResources = new HashSet<Resource>();

		// Analyze notifications for loaded and unloaded resources; record only resources which have not got
		// unloaded/loaded again later on
		for (Notification notification : event.getNotifications()) {
			Object notifier = notification.getNotifier();
			if (notifier instanceof Resource) {
				Resource resource = (Resource) notifier;
				Boolean newValue = (Boolean) notification.getNewValue();
				if (newValue) {
					if (unloadedResources.contains(resource)) {
						unloadedResources.remove(resource);
					} else {
						loadedResources.add(resource);
					}
				} else {
					if (loadedResources.contains(resource)) {
						loadedResources.remove(resource);
					} else {
						unloadedResources.add(resource);
					}
				}
			} else if (notifier instanceof ResourceSet) {
				if (notification.getEventType() == Notification.ADD || notification.getEventType() == Notification.ADD_MANY) {
					List<Resource> newResources = new ArrayList<Resource>();
					Object newValue = notification.getNewValue();
					if (newValue instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<Resource> newResourcesValue = (List<Resource>) newValue;
						newResources.addAll(newResourcesValue);
					} else if (newValue instanceof Resource) {
						newResources.add((Resource) newValue);
					}

					for (Resource newResource : newResources) {
						if (removedResources.contains(newResource)) {
							removedResources.remove(newResource);
						} else {
							addedResources.add(newResource);
						}
					}
				} else if (notification.getEventType() == Notification.REMOVE || notification.getEventType() == Notification.REMOVE_MANY) {
					List<Resource> oldResources = new ArrayList<Resource>();
					Object oldValue = notification.getOldValue();
					if (oldValue instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<Resource> oldResourcesValue = (List<Resource>) oldValue;
						oldResources.addAll(oldResourcesValue);
					} else if (oldValue instanceof Resource) {
						oldResources.add((Resource) oldValue);
					}

					for (Resource oldResource : oldResources) {
						if (addedResources.contains(oldResource)) {
							addedResources.remove(oldResource);
						} else {
							removedResources.add(oldResource);
						}
					}
				}
			}
		}
		loadedResources.addAll(addedResources);
		unloadedResources.addAll(removedResources);

		// Handle loaded and unloaded resources
		handleModelResourceLoaded(loadedResources);
		handleModelResourceUnloaded(unloadedResources);
	}

	/**
	 * Handles the case where the specified set of {@link Resource resource}s has been loaded.
	 * 
	 * @param resources
	 *            The set of {@linkplain Resource resource}s that has been loaded.
	 */
	private void handleModelResourceLoaded(final Collection<Resource> resources) {
		if (!resources.isEmpty()) {
			for (Resource resource : resources) {
				// Add descriptor for model behind loaded resource to ModelDescriptorRegistry if not
				// already done so
				IFile file = EcorePlatformUtil.getFile(resource);
				if (file == null || !file.exists()) {
					ModelDescriptorRegistry.INSTANCE.addModel(resource);
				}
			}
		}
	}

	/**
	 * Handles the case where the specified set of {@link Resource resource}s has been unloaded.
	 * 
	 * @param resources
	 *            The set of {@linkplain Resource resource} that has been unloaded.
	 */
	private void handleModelResourceUnloaded(final Collection<Resource> resources) {
		if (!resources.isEmpty()) {
			for (Resource resource : resources) {
				// Remove underlying model file from file meta-model descriptor cache if resource exists
				// only in memory
				/*
				 * !! Important Note !! This should normally be the business of MetaModelDescriptorCacheUpdater.
				 * However, we have to do so here as well because we depend on that cached metamodel descriptors are up
				 * to date but cannot know which of both BasicModelDescriptorSynchronizerDelegate or
				 * MetaModelDescriptorCacheUpdater gets called first.
				 */
				IFile file = EcorePlatformUtil.getFile(resource);
				if (!EcoreResourceUtil.exists(resource.getURI())) {
					InternalMetaModelDescriptorRegistry.INSTANCE.removeCachedDescriptor(file);
				}
			}

			for (Resource resource : resources) {
				// Remove descriptor for model behind unloaded only in memory resource from
				// ModelDescriptorRegistry if it is the last resource of the given model
				IFile file = EcorePlatformUtil.getFile(resource);
				if (file == null || !file.exists()) {
					ModelDescriptorRegistry.INSTANCE.removeModel(resource);
				}
			}
		}
	}
}
