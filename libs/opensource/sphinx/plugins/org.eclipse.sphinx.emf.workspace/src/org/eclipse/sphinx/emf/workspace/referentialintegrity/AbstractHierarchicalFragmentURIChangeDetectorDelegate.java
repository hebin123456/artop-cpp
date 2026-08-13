/**
 * <copyright>
 *
 * Copyright (c) 2016-2021 itemis, Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     Siemens - [577073] URI change detector delegate extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.referentialintegrity;

import static org.eclipse.sphinx.emf.util.URIExtensions.replaceBaseURI;
import static org.eclipse.sphinx.emf.util.URIExtensions.replaceLastFragmentSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.util.EcoreResourceUtil;
import org.eclipse.sphinx.emf.workspace.internal.referentialintegrity.IntermittentRemoveTracker;

/**
 * An abstract {@link IURIChangeDetectorDelegate} implementation the detection of changes in URIs with
 * {@link URI#isHierarchical() hierarchical} {@link URI#fragment() fragments}.
 *
 * @see IURIChangeDetectorDelegate
 */
public abstract class AbstractHierarchicalFragmentURIChangeDetectorDelegate implements IURIChangeDetectorDelegate {

	// Used for tracking contents being removed and added back elsewhere later on
	protected final IntermittentRemoveTracker removedContentsTracker;

	public AbstractHierarchicalFragmentURIChangeDetectorDelegate() {
		removedContentsTracker = createIntermittentRemoveTracker();
	}

	protected IntermittentRemoveTracker createIntermittentRemoveTracker() {
		return new IntermittentRemoveTracker();
	}

	protected abstract boolean affectsURIFragmentSegmentOfChangedObject(Notification notification);

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.referentialintegrity.IURIChangeDetectorDelegate#detectChangedURIs(java.util.
	 * List)
	 */
	@Override
	public Map<Resource, List<URIChangeNotification>> detectChangedURIs(List<Notification> notifications) {
		if (notifications != null) {
			Map<Resource, List<URIChangeNotification>> resourceToUriChangeNotification = new HashMap<>(notifications.size());

			for (Notification notification : notifications) {
				Object notifierObj = notification.getNotifier();
				if (notifierObj instanceof EObject) {
					EObject notifier = (EObject) notifierObj;
					Resource resource = notifier.eResource();

					List<URIChangeNotification> uriNotifications = handleNotification(notification, notifier);

					if (uriNotifications != null && !uriNotifications.isEmpty()) {
						if (resourceToUriChangeNotification.get(resource) == null) {
							resourceToUriChangeNotification.put(resource, uriNotifications);
						} else {
							resourceToUriChangeNotification.get(resource).addAll(uriNotifications);
						}
					}
				}
			}
			return resourceToUriChangeNotification;
		}
		return Collections.emptyMap();
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.referencialintegrity.IURIChangeDetectorDelegate#detectChangedURIs(org.eclipse.
	 * emf.common.notify.Notification)
	 */
	@Override
	public List<URIChangeNotification> detectChangedURIs(Notification notification) {
		if (notification != null) {
			Object notifierObj = notification.getNotifier();
			if (notifierObj instanceof EObject) {
				return handleNotification(notification, (EObject) notifierObj);
			}
		}
		return Collections.emptyList();
	}

	protected List<URIChangeNotification> handleNotification(Notification notification, EObject notifier) {
		EStructuralFeature feature = (EStructuralFeature) notification.getFeature();

		// Detect object URI changes due to modifications that affect the URIs of the modified object and its
		// contents
		if (affectsURIFragmentSegmentOfChangedObject(notification)) {
			return handleURIFragmentSegmentChange(notification, notifier);
		}
		// Detect object URI changes due to contents being removed and added back elsewhere
		else if (feature instanceof EReference && ((EReference) feature).isContainment()) {
			return handleContainmentChange(notification, notifier, feature);
		}

		return Collections.emptyList();
	}

	protected List<URIChangeNotification> handleURIFragmentSegmentChange(Notification notification, EObject eObject) {
		URI newURI = EcoreResourceUtil.getURI(eObject);
		URI oldURI = replaceLastFragmentSegment(newURI, notification.getNewStringValue(), notification.getOldStringValue());
		if (oldURI != null && !oldURI.equals(newURI)) {
			return addURIChangeNotification(eObject, oldURI, newURI);
		}
		return Collections.emptyList();
	}

	protected List<URIChangeNotification> handleContainmentChange(Notification notification, EObject eObject, EStructuralFeature feature) {
		removedContentsTracker.clearObsoleteEntries();

		if (Notification.REMOVE == notification.getEventType()) {
			URI containerURI = EcoreResourceUtil.getURI(eObject);
			if (notification.getOldValue() instanceof EObject) {
				handleRemovedContent(eObject, containerURI, feature, (EObject) notification.getOldValue());
			}
		} else if (Notification.ADD == notification.getEventType()) {
			return handleAddedContent((EObject) notification.getNewValue());
		} else if (Notification.REMOVE_MANY == notification.getEventType()) {
			URI containerURI = EcoreResourceUtil.getURI(eObject);
			List<?> oldValues = (List<?>) notification.getOldValue();
			for (Object oldValue : oldValues) {
				if (oldValue instanceof EObject) {
					handleRemovedContent(eObject, containerURI, feature, (EObject) oldValue);
				}
			}
		} else if (Notification.ADD_MANY == notification.getEventType()) {
			List<?> newValues = (List<?>) notification.getNewValue();
			List<URIChangeNotification> notifications = new ArrayList<>(newValues.size());
			for (Object newValue : newValues) {
				if (newValue instanceof EObject) {
					notifications.addAll(handleAddedContent((EObject) newValue));
				}
			}
			return notifications;
		}
		return Collections.emptyList();
	}

	protected void handleRemovedContent(EObject oldContainer, URI oldContainerURI, EStructuralFeature oldFeature, EObject oldContent) {
		URI oldContentURI = null;

		// Old content just removed but not yet added back elsewhere?
		if (oldContent.eResource() == null) {
			// Restore old content URI using old container and old containing feature
			oldContentURI = EcoreResourceUtil.getURI(oldContainer, oldFeature, oldContent);
		} else {
			// Restore old content URI by replacing the URI of the new container at the beginning of the old
			// content's new URI with the URI of its old container
			URI newContentURI = EcoreResourceUtil.getURI(oldContent);
			URI newContainerURI = EcoreResourceUtil.getURI(oldContent.eContainer());
			oldContentURI = replaceBaseURI(newContentURI, newContainerURI, oldContainerURI);
		}

		// Keep track of the removed object and its old URI so that we can issue an appropriately initialized URI
		// change notification in case it will get or already has been added back somewhere else
		if (oldContentURI != null) {
			removedContentsTracker.put(oldContent, oldContentURI);
		}
	}

	protected List<URIChangeNotification> handleAddedContent(EObject newContent) {
		URI oldContentURI = removedContentsTracker.get(newContent);
		if (oldContentURI != null) {
			URI newContentURI = EcoreResourceUtil.getURI(newContent);
			if (!oldContentURI.equals(newContentURI)) {
				return addURIChangeNotification(newContent, oldContentURI, newContentURI);
			}
		}
		return Collections.emptyList();
	}

	protected List<URIChangeNotification> addURIChangeNotification(EObject eObject, URI oldURI, URI newURI) {
		List<URIChangeNotification> notifications = new LinkedList<>();

		// Add URI change notification for given EObject
		notifications.add(new URIChangeNotification(eObject, oldURI));

		// Add URI change notifications for all EObjects that are directly or indirectly contained by given EObject and
		// have URIs that are affected by the change on given EObject
		TreeIterator<EObject> eAllContents = eObject.eAllContents();
		while (eAllContents.hasNext()) {
			EObject contentObject = eAllContents.next();
			URI newContentURI = EcoreResourceUtil.getURI(contentObject);
			URI oldContentURI = replaceBaseURI(newContentURI, newURI, oldURI);
			if (oldContentURI != null && !oldContentURI.equals(newContentURI)) {
				notifications.add(new URIChangeNotification(contentObject, oldContentURI));
			}
		}

		return notifications;
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.referencialintegrity.IURIChangeDetectorDelegate#detectChangedURIs(org.eclipse
	 * .core .resources.IFile, org.eclipse.core.resources.IFile)
	 */
	@Override
	public List<URIChangeNotification> detectChangedURIs(IFile oldFile, IFile newFile) {
		return Collections.emptyList();
	}
}
