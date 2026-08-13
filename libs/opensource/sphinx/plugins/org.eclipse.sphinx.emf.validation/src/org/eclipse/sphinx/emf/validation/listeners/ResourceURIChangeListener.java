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
package org.eclipse.sphinx.emf.validation.listeners;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.sphinx.emf.validation.Activator;
import org.eclipse.sphinx.emf.validation.markers.ValidationMarkerManager;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public class ResourceURIChangeListener implements IResourceChangeListener {

	/**
	 * store the resource which we have to update.
	 */
	private static ArrayList<IResource> cache = new ArrayList<IResource>();

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		switch (event.getType()) {
		case IResourceChangeEvent.POST_CHANGE:
			updateCache(event);
			break;
		case IResourceChangeEvent.POST_BUILD:
			updateMarkers();
			cleanCache();
		default:
			break;
		}

	}

	/**
	 * clean the resource list cache
	 */
	private void cleanCache() {
		if (cache != null) {
			cache.clear();
		}
	}

	/**
	 * update the problem markers of resource referenced into the resource list cache.}
	 */
	private void updateMarkers() {
		// Let's update problem markers.
		ValidationMarkerManager markermanager = ValidationMarkerManager.getInstance();

		for (IResource resource : cache) {
			try {
				markermanager.updateMarkersURI(resource);
			} catch (CoreException ex) {
				PlatformLogUtil.logAsWarning(Activator.getDefault(), ex);
			}
		}
	}

	/**
	 * update the resource cache with impacted resource
	 * 
	 * @param event
	 *            the source {@link IResourceChangeEvent}
	 */
	private void updateCache(IResourceChangeEvent event) {
		IResourceDelta rootDelta = event.getDelta();

		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) {
				IResource resource = delta.getResource();
				// Changes into resource are not interesting here
				if (delta.getKind() == IResourceDelta.CHANGED) {
					return true;
				}
				// We are not interested about change on markers.
				if ((delta.getFlags() & IResourceDelta.MARKERS) == 0) {
					return true;
				}
				// only interested in files that exist
				if (resource.getType() == IResource.FILE && resource.exists()) {
					cache.add(resource);
				}
				return true;
			}
		};

		try {
			rootDelta.accept(visitor);
		} catch (CoreException ex) {
		}

	}
}
