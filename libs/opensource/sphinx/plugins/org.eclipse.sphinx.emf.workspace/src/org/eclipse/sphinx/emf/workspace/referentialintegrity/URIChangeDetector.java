/**
 * <copyright>
 *
 * Copyright (c) 2008-2021 See4sys, BMW Car IT, itemis, Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Avoid usage of Object.finalize
 *     itemis - [409014] Listener URIChangeDetector registered for all transactional editing domains
 *     Siemens - [577073] URI change detector delegate extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.referentialintegrity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sphinx.emf.domain.factory.AbstractResourceSetListenerInstaller;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.emf.workspace.Activator;
import org.eclipse.sphinx.platform.resources.DefaultResourceChangeHandler;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * URIChangeDetector is in charge of Handling URI Change Detection.It converts {@link IResourceChangeEvent} and
 * {@link ResourceSetChangeEvent} into {@link URIChangeEvent} and notifies registered {@link URIChangeListener}.see also
 * {@link URIChangeListenerRegistry}.
 */
public class URIChangeDetector extends ResourceSetListenerImpl implements IResourceChangeListener {

	public static class URIChangeDetectorInstaller extends AbstractResourceSetListenerInstaller<URIChangeDetector> {
		public URIChangeDetectorInstaller() {
			super(URIChangeDetector.class);
		}
	}

	/**
	 * Default constructor.
	 */
	public URIChangeDetector() {
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
	 * @see org.eclipse.emf.transaction.ResourceSetListener#resourceSetChanged(org.eclipse.emf.transaction.
	 * ResourceSetChangeEvent )
	 */
	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {
		List<?> notifications = event.getNotifications();

		// Split the notifications based on their delegate handler into smaller lists
		Map<IURIChangeDetectorDelegate, List<Notification>> delegateToNotifications = new HashMap<>();
		for (Object o : notifications) {
			if (o instanceof Notification) {
				Notification notification = (Notification) o;
				if (notification.getNotifier() instanceof EObject) {
					Resource resource = ((EObject) notification.getNotifier()).eResource();
					IURIChangeDetectorDelegate delegate = URIChangeDetectorDelegateRegistry.INSTANCE.getDetectorDelegate(resource);

					if (delegate != null) {
						delegateToNotifications.computeIfAbsent(delegate, d -> new ArrayList<Notification>()).add(notification);
					}
				}
			}
		}

		for (IURIChangeDetectorDelegate delegate : delegateToNotifications.keySet()) {

			List<Notification> delegateNotifications = delegateToNotifications.get(delegate);
			Map<Resource, List<URIChangeNotification>> resourceToUriNotifications = delegate.detectChangedURIs(delegateNotifications);

			if (resourceToUriNotifications == null) {
				/** assume that the new API is not implemented, call the deprecated API */
				for (Notification notification : delegateNotifications) {
					if (notification.getNotifier() instanceof EObject) {
						EObject newEObject = (EObject) notification.getNotifier();
						Resource resource = newEObject.eResource();

						@SuppressWarnings("deprecation")
						List<URIChangeNotification> uriNotifications = delegate.detectChangedURIs(notification);
						if (uriNotifications != null && !uriNotifications.isEmpty()) {
							fireURIChanged(createURIChangeEvent(resource, uriNotifications));
						}
					}
				}
			} else if (!resourceToUriNotifications.isEmpty()) {
				for (Resource resource : resourceToUriNotifications.keySet()) {
					fireURIChanged(createURIChangeEvent(resource, resourceToUriNotifications.get(resource)));
				}
			}
		}
	}

	@Override
	public boolean isPostcommitOnly() {
		return true;
	}

	/*
	 * @seeorg.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.
	 * IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDeltaVisitor visitor = new ResourceDeltaVisitor(event.getType(), new DefaultResourceChangeHandler() {

					@Override
					public void handleFileMoved(int eventType, IFile oldFile, IFile newFile) {
						if (eventType == IResourceChangeEvent.POST_CHANGE) {
							Resource resource = EcorePlatformUtil.getResource(oldFile);
							if (resource != null) {
								IURIChangeDetectorDelegate delegate = URIChangeDetectorDelegateRegistry.INSTANCE.getDetectorDelegate(resource);
								if (delegate != null) {
									List<URIChangeNotification> notifications = delegate.detectChangedURIs(oldFile, newFile);
									if (!notifications.isEmpty()) {
										fireURIChanged(createURIChangeEvent(resource, notifications));
									}
								}
							}
						}
					}
				});
				delta.accept(visitor);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	// TODO integrate notification creation into Delegates
	protected URIChangeEvent createURIChangeEvent(Resource source, List<URIChangeNotification> notifications) {
		if (source != null && notifications != null && notifications.isEmpty()) {
			return null;
		}

		return new URIChangeEvent(source, notifications);
	}

	private void fireURIChanged(URIChangeEvent event) {
		if (event != null) {
			for (IURIChangeListener listener : URIChangeListenerRegistry.INSTANCE.getListeners()) {
				listener.uriChanged(event);
			}
		}
	}
}
