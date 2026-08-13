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
package org.eclipse.sphinx.emf.workspace.syncing;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sphinx.emf.workspace.internal.syncing.BasicModelSynchronizerDelegate;
import org.eclipse.sphinx.emf.workspace.internal.syncing.ModelSyncRequest;
import org.eclipse.sphinx.platform.resources.syncing.AbstractResourceSynchronizer;

/**
 * Supports loading/unloading/reloading of complete models when underlying projects are
 * created/opened/renamed/closed/deleted or their description or settings are changed as well as
 * loading/unloading/reloading of individual model resources when underlying files are created/changed/deleted.
 */
public class ModelSynchronizer extends AbstractResourceSynchronizer<IModelSyncRequest> {

	/**
	 * The singleton instance.
	 */
	public static final ModelSynchronizer INSTANCE = new ModelSynchronizer();

	/**
	 * Starts automatic synchronization of models wrt resource changes in the workspace.
	 */
	public void start() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				this,
				IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_BUILD
						| IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Stops automatic synchronization of models wrt resource changes in the workspace.
	 * 
	 * @see #start()
	 */
	public void stop() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Protected constructor for singleton pattern.
	 */
	protected ModelSynchronizer() {
		addDelegate(BasicModelSynchronizerDelegate.INSTANCE);
	}

	@Override
	protected IModelSyncRequest createSyncRequest() {
		return new ModelSyncRequest();
	}
}
