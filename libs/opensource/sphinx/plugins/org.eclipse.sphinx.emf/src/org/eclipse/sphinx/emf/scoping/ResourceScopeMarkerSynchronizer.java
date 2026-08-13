/**
 * <copyright>
 *
 * Copyright (c) 2008-2012 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - [374883] Improve handling of out-of-sync workspace files during descriptor initialization
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.scoping;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.sphinx.emf.internal.scoping.IResourceScopeMarker;
import org.eclipse.sphinx.emf.internal.scoping.ResourceScopeMarkerSyncRequest;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizer;

/**
 * The ResourceScopeMarkerSynchronizer is in charge of synchronizing all the tasks relatives to
 * {@link IResourceScopeMarker Resource Scope Marker}s. see also {@link AbstractResourceSynchronizer}
 */
public class ResourceScopeMarkerSynchronizer extends AbstractResourceSynchronizer<IResourceScopeMarkerSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final ResourceScopeMarkerSynchronizer INSTANCE = new ResourceScopeMarkerSynchronizer();

	/**
	 * Protected constructor for singleton pattern.
	 */
	protected ResourceScopeMarkerSynchronizer() {
	}

	public void start() {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.addResourceChangeListener(this, IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE);
		});
	}

	public void stop() {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.removeResourceChangeListener(this);
		});
	}

	/*
	 * @see org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizer#createSyncRequest()
	 */
	@Override
	protected IResourceScopeMarkerSyncRequest createSyncRequest() {
		return new ResourceScopeMarkerSyncRequest();
	}
}
