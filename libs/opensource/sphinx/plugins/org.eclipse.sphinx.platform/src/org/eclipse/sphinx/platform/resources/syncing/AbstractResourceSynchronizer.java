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
package org.eclipse.sphinx.platform.resources.syncing;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.resources.ResourceDeltaVisitor;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

public abstract class AbstractResourceSynchronizer<T extends IResourceSyncRequest> implements IResourceChangeListener {

	private T fSyncRequest;

	protected Set<IResourceSynchronizerDelegate<T>> fSynchronizerDelegates = new HashSet<IResourceSynchronizerDelegate<T>>();

	private int lastEventType;

	public void addDelegate(IResourceSynchronizerDelegate<T> delegate) {
		if (delegate != null) {
			if (fSynchronizerDelegates.add(delegate)) {
				delegate.setSyncRequest(getSyncRequest());
			}
		}
	}

	public void removeDelegate(IResourceSynchronizerDelegate<T> delegate) {
		if (delegate != null) {
			fSynchronizerDelegates.remove(delegate);
		}
	}

	protected T getSyncRequest() {
		if (fSyncRequest == null) {
			fSyncRequest = createSyncRequest();
		}
		return fSyncRequest;
	}

	protected abstract T createSyncRequest();

	/*
	 * @see
	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent
	 * )
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		try {
			switch (event.getType()) {
			case IResourceChangeEvent.PRE_CLOSE:
				getSyncRequest().init();
				doPreClose(event);
				getSyncRequest().perform();
				break;
			case IResourceChangeEvent.PRE_DELETE:
				getSyncRequest().init();
				doPreDelete(event);
				getSyncRequest().perform();
				break;
			case IResourceChangeEvent.POST_CHANGE:
				if (lastEventType != IResourceChangeEvent.PRE_CLOSE && lastEventType != IResourceChangeEvent.PRE_DELETE) {
					getSyncRequest().init();
				}
				doPostChange(event);
				getSyncRequest().perform();
				getSyncRequest().dispose();
				break;
			case IResourceChangeEvent.PRE_BUILD:
				getSyncRequest().init();
				doPreBuild(event);
				getSyncRequest().perform();
				getSyncRequest().dispose();
				break;
			case IResourceChangeEvent.POST_BUILD:
				getSyncRequest().init();
				doPostBuild(event);
				getSyncRequest().perform();
				getSyncRequest().dispose();
				break;
			case IResourceChangeEvent.PRE_REFRESH:
				getSyncRequest().init();
				doPreRefresh(event);
				getSyncRequest().perform();
				getSyncRequest().dispose();
				break;
			default:
				break;
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		} finally {
			lastEventType = event.getType();
		}
	}

	protected void doPreClose(final IResourceChangeEvent event) {
		try {
			IResource resource = event.getResource();
			if (resource instanceof IProject) {
				for (IResourceSynchronizerDelegate<T> delegate : fSynchronizerDelegates) {
					delegate.handleProjectAboutToBeClosed((IProject) resource);
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	protected void doPreDelete(final IResourceChangeEvent event) {
		try {
			IResource resource = event.getResource();
			if (resource instanceof IProject) {
				for (IResourceSynchronizerDelegate<T> delegate : fSynchronizerDelegates) {
					delegate.handleProjectAboutToBeDeleted((IProject) resource);
				}
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}

	protected void doPostChange(final IResourceChangeEvent event) {
		visitResourceDelta(event);
	}

	protected final void doPreBuild(IResourceChangeEvent event) {
		visitResourceDelta(event);
	}

	protected final void doPostBuild(IResourceChangeEvent event) {
		visitResourceDelta(event);
	}

	protected void doPreRefresh(IResourceChangeEvent event) {
		// Nothing to do
	}

	protected final void visitResourceDelta(IResourceChangeEvent event) {
		try {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDeltaVisitor visitor = new ResourceDeltaVisitor(event.getType(), fSynchronizerDelegates);
				delta.accept(visitor);
			}
		} catch (Exception ex) {
			PlatformLogUtil.logAsError(Activator.getDefault(), ex);
		}
	}
}
