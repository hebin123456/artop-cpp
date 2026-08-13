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
package org.eclipse.sphinx.testutils.integration;

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
import org.eclipse.sphinx.emf.internal.resource.ResourceProblemMarkerService;

/**
 * Listens for {@link Resource resource}s that have been loaded or saved and requests the problem markers of underlying
 * {@link IFile file}s to be updated according to the {@link Resource#getErrors() errors} and
 * {@link Resource#getWarnings() warnings} of each loaded or saved {@link Resource} resource.
 * 
 * @see ResourceProblemMarkerService#updateProblemMarkers(Collection, boolean,
 *      org.eclipse.core.runtime.IProgressMonitor)
 */
@SuppressWarnings("restriction")
public class ResourceProblemListener extends ResourceSetListenerImpl {

	protected List<Resource> errorResources = new ArrayList<Resource>();

	/**
	 * Default constructor.
	 */
	public ResourceProblemListener() {
		super(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__IS_LOADED).or(
				NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResourceSet(), ResourceSet.RESOURCE_SET__RESOURCES)));

	}

	/*
	 * @seeorg.eclipse.emf.transaction.ResourceSetListenerImpl#resourceSetChanged(org.eclipse.emf.transaction.
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
		for (Resource resource : resources) {
			if (!resource.getErrors().isEmpty()) {
				errorResources.add(resource);
			}
		}
	}

	public List<Resource> getErrorResources() {
		return errorResources;
	}

	public void clearHistory() {
		errorResources.clear();
	}
}