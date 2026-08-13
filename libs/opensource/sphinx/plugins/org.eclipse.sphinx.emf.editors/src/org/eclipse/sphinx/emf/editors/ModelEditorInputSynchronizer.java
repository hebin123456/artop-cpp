/**
 * <copyright>
 *
 * Copyright (c) 2015-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [501112] Temporary model loading form editor page sometimes happens to remain in place forever even when underlying model has been loaded

 * </copyright>
 */
package org.eclipse.sphinx.emf.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.ui.IEditorInput;

public class ModelEditorInputSynchronizer implements IDisposable {

	private ResourceSetListener resourceLoadedListener;
	private ResourceSetListener resourceMovedListener;
	private ResourceSetListener resourceRemovedListener;
	private ResourceSetListener objectChangedListener;

	protected IEditorInput editorInput;
	protected TransactionalEditingDomain editingDomain;
	protected IModelEditorInputChangeAnalyzer editorInputChangeAnalyzer;
	protected IModelEditorInputChangeHandler editorInputChangeHandler;

	public ModelEditorInputSynchronizer(IEditorInput editorInput, TransactionalEditingDomain editingDomain,
			IModelEditorInputChangeAnalyzer editorInputChangeAnalyzer, IModelEditorInputChangeHandler editorInputChangeHandler) {
		Assert.isNotNull(editorInput);
		Assert.isNotNull(editingDomain);
		Assert.isNotNull(editorInputChangeAnalyzer);
		Assert.isNotNull(editorInputChangeHandler);

		this.editorInput = editorInput;
		this.editingDomain = editingDomain;
		this.editorInputChangeAnalyzer = editorInputChangeAnalyzer;
		this.editorInputChangeHandler = editorInputChangeHandler;

		installModelChangeListeners();
	}

	protected void installModelChangeListeners() {
		// Create and register listener that detects loaded resources
		resourceLoadedListener = createResourceLoadedListener();
		Assert.isNotNull(resourceLoadedListener);
		editingDomain.addResourceSetListener(resourceLoadedListener);

		// Create and register listener that detects renamed or moved resources
		resourceMovedListener = createResourceMovedListener();
		Assert.isNotNull(resourceMovedListener);
		editingDomain.addResourceSetListener(resourceMovedListener);

		// Create and register listener that detects removed resources
		resourceRemovedListener = createResourceRemovedListener();
		Assert.isNotNull(resourceRemovedListener);
		editingDomain.addResourceSetListener(resourceRemovedListener);

		// Create and register listener that detects changed objects
		objectChangedListener = createObjectChangedListener();
		Assert.isNotNull(objectChangedListener);
		editingDomain.addResourceSetListener(objectChangedListener);
	}

	protected void uninstallModelChangeListeners() {
		editingDomain.removeResourceSetListener(resourceLoadedListener);
		editingDomain.removeResourceSetListener(resourceMovedListener);
		editingDomain.removeResourceSetListener(resourceRemovedListener);
		editingDomain.removeResourceSetListener(objectChangedListener);
	}

	protected ResourceSetListener createResourceLoadedListener() {
		return new ResourceSetListenerImpl(
				NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__IS_LOADED)) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				// Retrieve loaded resources from notification
				List<?> notifications = event.getNotifications();
				for (Object object : notifications) {
					if (object instanceof Notification) {
						Notification notification = (Notification) object;
						if (notification.getNewBooleanValue()) {
							Resource loadedResource = (Resource) notification.getNotifier();
							// Is loaded resource containing editor input object?
							if (editorInputChangeAnalyzer.containsEditorInputResourceURI(editorInput,
									Collections.singleton(loadedResource.getURI()))) {
								// Has loaded resource not been unloaded again subsequently?
								if (loadedResource.isLoaded()) {
									// Handle (re-)loaded editor input resource
									editorInputChangeHandler.handleEditorInputResourceLoaded(editorInput);
									break;
								} else {
									editorInputChangeHandler.handleEditorInputResourceUnloaded(editorInput);
									break;
								}
							}
						}
					}
				}
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	protected ResourceSetListener createResourceMovedListener() {
		return new ResourceSetListenerImpl(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__URI)) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				// Retrieve moved resources from notification
				List<?> notifications = event.getNotifications();
				for (Object object : notifications) {
					if (object instanceof Notification) {
						Notification notification = (Notification) object;
						// Is moved resource the resource containing editor input object?
						if (notification.getOldValue() instanceof URI) {
							URI oldResourceURI = (URI) notification.getOldValue();
							if (oldResourceURI != null) {
								if (editorInputChangeAnalyzer.containsEditorInputResourceURI(editorInput, Collections.singleton(oldResourceURI))) {
									// Handle moved editor input resource
									editorInputChangeHandler.handleEditorInputResourceMoved(editorInput, oldResourceURI,
											(URI) notification.getNewValue());
									break;
								}
							}
						}
					}
				}
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	protected ResourceSetListener createResourceRemovedListener() {
		return new ResourceSetListenerImpl(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__IS_LOADED)
				.or(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResourceSet(), ResourceSet.RESOURCE_SET__RESOURCES))) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				// Retrieve removed and added resources from notification
				Set<Resource> removedResources = new HashSet<Resource>();
				Set<Resource> addedResources = new HashSet<Resource>();

				// Analyze notifications for changed resources; record only added and removed resources which have not
				// got removed/added again later on
				List<?> notifications = event.getNotifications();
				for (Object object : notifications) {
					if (object instanceof Notification) {
						Notification notification = (Notification) object;
						Object notifier = notification.getNotifier();
						if (notifier instanceof ResourceSet) {
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
									Resource removedResource = findEquivalentResource(removedResources, newResource);
									// If the newResource has been removed, then remove the equivalent resource from
									// removedResource, otherwise add it to addedResources
									if (removedResource != null) {
										removedResources.remove(removedResource);
									} else {
										addedResources.add(newResource);
									}
								}
							} else if (notification.getEventType() == Notification.REMOVE
									|| notification.getEventType() == Notification.REMOVE_MANY) {
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
									Resource oldAddedResource = findEquivalentResource(addedResources, oldResource);
									// If the oldResource has been added, then remove the equivalent resource from
									// addedResources, otherwise add it to removedResources
									if (oldAddedResource != null) {
										addedResources.remove(oldAddedResource);
									} else {
										removedResources.add(oldResource);
									}
								}
							}
						}
					}
				}

				// Include removed resources the resource containing editor input object?
				if (!removedResources.isEmpty()) {
					Set<URI> removedResourceURIs = new HashSet<URI>(removedResources.size());
					for (Resource removedResource : removedResources) {
						removedResourceURIs.add(removedResource.getURI());
					}
					if (editorInputChangeAnalyzer.containsEditorInputResourceURI(editorInput, removedResourceURIs)) {
						// Handle removed editor input resource
						editorInputChangeHandler.handleEditorInputResourceRemoved(editorInput);
					}
				}
			}

			/**
			 * Returns a resource from the given set of resources that is "equal to" the indicated one. The "equals"
			 * method detects an URI equivalence relation on non-null resources: if the resource URI equals to the URI
			 * of the specified resource, then the resource is returned.
			 */
			protected Resource findEquivalentResource(Set<Resource> resources, Resource resource) {
				URI uri = resource.getURI();
				for (Resource equivalentResourceCandidate : resources) {
					if (equivalentResourceCandidate.getURI().equals(uri)) {
						return equivalentResourceCandidate;
					}
				}
				return null;
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	protected ResourceSetListener createObjectChangedListener() {
		return new ResourceSetListenerImpl(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__CONTENTS)
				.or(NotificationFilter.createNotifierTypeFilter(EObject.class))) {
			@Override
			public void resourceSetChanged(ResourceSetChangeEvent event) {
				Set<EObject> addedObjects = new HashSet<EObject>();
				Set<EObject> removedObjects = new HashSet<EObject>();
				Set<EObject> movedObjects = new HashSet<EObject>();
				Set<EObject> changedObjects = new HashSet<EObject>();

				// Analyze notifications for changed objects; record only set/added and unset/removed objects which have
				// not got unset/removed or set/added again later on
				for (Notification notification : event.getNotifications()) {
					if (notification.getEventType() == Notification.SET || notification.getEventType() == Notification.ADD
							|| notification.getEventType() == Notification.ADD_MANY) {
						List<EObject> newValues = new ArrayList<EObject>();
						Object newValue = notification.getNewValue();
						if (newValue instanceof List<?>) {
							@SuppressWarnings("unchecked")
							List<EObject> newValueList = (List<EObject>) newValue;
							newValues.addAll(newValueList);
						} else if (newValue instanceof EObject) {
							newValues.add((EObject) newValue);
						}

						for (EObject value : newValues) {
							changedObjects.add(value);
							if (removedObjects.contains(value)) {
								movedObjects.add(value);
								removedObjects.remove(value);
							} else {
								addedObjects.add(value);
							}
						}
					} else if (notification.getEventType() == Notification.UNSET || notification.getEventType() == Notification.REMOVE
							|| notification.getEventType() == Notification.REMOVE_MANY) {
						List<EObject> oldValues = new ArrayList<EObject>();
						Object oldValue = notification.getOldValue();
						if (oldValue instanceof List<?>) {
							@SuppressWarnings("unchecked")
							List<EObject> oldValueList = (List<EObject>) oldValue;
							oldValues.addAll(oldValueList);
						} else if (oldValue instanceof EObject) {
							oldValues.add((EObject) oldValue);
						}

						for (EObject value : oldValues) {
							changedObjects.add(value);
							if (addedObjects.contains(value)) {
								movedObjects.add(value);
								addedObjects.remove(value);
							} else {
								removedObjects.add(value);
							}
						}
					}
				}

				// Is editor input object part of the added objects or contained by one or them?
				if (!addedObjects.isEmpty()) {
					if (editorInputChangeAnalyzer.containsEditorInputObject(editorInput, addedObjects)) {
						// Handle added editor input object
						editorInputChangeHandler.handleEditorInputObjectAdded(editorInput, addedObjects);
					}
				}

				// Is editor input object part of the removed objects or contained by one or them?
				if (!removedObjects.isEmpty()) {
					if (editorInputChangeAnalyzer.containsEditorInputObject(editorInput, removedObjects)) {
						// Handle removed editor input object
						editorInputChangeHandler.handleEditorInputObjectRemoved(editorInput, removedObjects);
					}
				}

				// Is editor input object part of the moved objects or contained by one or them?
				if (!movedObjects.isEmpty()) {
					if (editorInputChangeAnalyzer.containsEditorInputObject(editorInput, movedObjects)) {
						// Handle moved editor input object
						editorInputChangeHandler.handleEditorInputObjectMoved(editorInput, movedObjects);
					}
				}

				// Is editor input object part of the changed objects or contained by one or them?
				if (!changedObjects.isEmpty()) {
					if (editorInputChangeAnalyzer.containsEditorInputObject(editorInput, changedObjects)) {
						// Handle changed editor input object
						editorInputChangeHandler.handleEditorInputObjectChanged(editorInput, changedObjects);
					}
				}
			}

			@Override
			public boolean isPostcommitOnly() {
				return true;
			}
		};
	}

	/*
	 * @see org.eclipse.emf.edit.provider.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		uninstallModelChangeListeners();
	}
}
