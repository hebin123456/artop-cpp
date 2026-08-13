/**
 * <copyright>
 *
 * Copyright (c) 2008-2012 See4sys, BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     BMW Car IT - Avoid usage of Object.finalize
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.model;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizer;

public class ModelDescriptorSynchronizer extends AbstractResourceSynchronizer<IModelDescriptorSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final ModelDescriptorSynchronizer INSTANCE = new ModelDescriptorSynchronizer();

	public void start() {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.addResourceChangeListener(ModelDescriptorSynchronizer.this, IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE
					| IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE);
		});
	}

	public void stop() {
		ServiceCaller.callOnce(getClass(), IWorkspace.class, ws -> {
			ws.removeResourceChangeListener(ModelDescriptorSynchronizer.this);
		});
	}

	/**
	 * Protected constructor for singleton pattern.
	 */
	protected ModelDescriptorSynchronizer() {
		addDelegate(BasicModelDescriptorSynchronizerDelegate.INSTANCE);
	}

	@Override
	protected IModelDescriptorSyncRequest createSyncRequest() {
		return new ModelDescriptorSyncRequest();
	}
}
