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
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.resource;

import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.sphinx.emf.domain.factory.AbstractResourceSetListenerInstaller;

public class URIResourceCacheUpdater extends ResourceSetListenerImpl {

	public static class URIResourceCacheUpdaterInstaller extends AbstractResourceSetListenerInstaller<URIResourceCacheUpdater> {
		public URIResourceCacheUpdaterInstaller() {
			super(URIResourceCacheUpdater.class);
		}
	}

	/**
	 * Constructs a {@link ResourceSetListener} that listens to changes of {@link Resource#getURI()}.
	 */
	public URIResourceCacheUpdater() {
		super(NotificationFilter.createFeatureFilter(EcorePackage.eINSTANCE.getEResource(), Resource.RESOURCE__URI));
	}

	@Override
	public void resourceSetChanged(ResourceSetChangeEvent event) {
		for (Notification notification : event.getNotifications()) {
			handleModelResourceMoved((Resource) notification.getNotifier(), (URI) notification.getOldValue(), (URI) notification.getNewValue());
		}
	}

	private void handleModelResourceMoved(Resource resource, URI oldURI, URI newURI) {
		// Update the moved resource's URI in the ResourceSet's uriResourceMap
		ResourceSet resourceSet = resource.getResourceSet();
		if (resourceSet instanceof ResourceSetImpl) {
			Map<URI, Resource> uriResourceMap = ((ResourceSetImpl) resourceSet).getURIResourceMap();
			if (uriResourceMap != null && uriResourceMap.remove(oldURI) != null) {
				uriResourceMap.put(newURI, resource);
			}
		}
	}

	@Override
	public boolean isPostcommitOnly() {
		return true;
	}
}