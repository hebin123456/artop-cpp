/**
 * <copyright>
 * 
 * Copyright (c) 2012-2014 BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 *     itemis - [434954] Hook for overwriting conversion of EMF Diagnostics to IMarkers
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.resources;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.platform.internal.Activator;
import org.eclipse.sphinx.platform.messages.PlatformMessages;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;

/**
 * A job which can be used to asynchronously create and delete markers.
 * <p>
 * The job is useful to avoid deadlocks related to the global workspace lock which is needed to manipulate resource
 * markers.
 * <p>
 * The job maintains an internal queue of marker manipulation tasks when the job gets executed this queue will be
 * processed. To execute the job asynchronously use the {@link MarkerJob#schedule() schedule method}.
 */
public class MarkerJob extends WorkspaceJob {

	protected static abstract class MarkerTask extends MarkerDescriptor {
		protected IResource resource;

		public MarkerTask(IResource resource, String type) {
			super(type);
			this.resource = resource;
		}

		public MarkerTask(IResource resource, String type, Map<String, Object> attributes) {
			super(type, attributes);
			this.resource = resource;
		}

		abstract void execute() throws CoreException;
	}

	protected static class CreateMarkerTask extends MarkerTask {

		public CreateMarkerTask(IResource resource, String type, Map<String, Object> attributes) {
			super(resource, type, attributes);
		}

		public CreateMarkerTask(IResource resource, String type, int severity, String message) {
			super(resource, type);
			getAttributes().put(IMarker.SEVERITY, severity);
			getAttributes().put(IMarker.MESSAGE, message);
		}

		@Override
		void execute() throws CoreException {
			IMarker marker = resource.createMarker(getType());
			marker.setAttributes(getAttributes());
		}
	}

	protected static class DeleteMarkerTask extends MarkerTask {

		public DeleteMarkerTask(IResource resource, String type) {
			super(resource, type);
		}

		@Override
		void execute() throws CoreException {
			resource.deleteMarkers(getType(), false, IResource.DEPTH_ZERO);
		}
	}

	/**
	 * Singleton instance of marker job that can be used to manipulate resource markers asynchronously without risking
	 * to run into deadlocks. After queuing marker manipulations with this marker job instance it must be explicitly
	 * scheduled by the caller.
	 * 
	 * @see MarkerJob#schedule()
	 */
	public static final MarkerJob INSTANCE = new MarkerJob();

	protected Queue<MarkerTask> taskQueue = new ConcurrentLinkedQueue<MarkerTask>();

	/**
	 * Protected constructor for the singleton pattern that prevents from instantiation by clients.
	 */
	protected MarkerJob() {
		super(PlatformMessages.job_updatingProblemMarkers);
		setSystem(true);
		setPriority(Job.BUILD);
		setRule(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Processes the internal task queue.
	 */
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		MarkerTask mt = null;
		while ((mt = taskQueue.poll()) != null) {
			try {
				mt.execute();
			} catch (CoreException ex) {
				if (mt.resource.isAccessible() == false || mt.resource.isSynchronized(IResource.DEPTH_ZERO) == false) {
					// do not log errors for inaccessible or out-of-sync resources
					continue;
				}
				PlatformLogUtil.logAsError(Activator.getDefault(), ex);
			}
		}

		return Status.OK_STATUS;
	}

	/**
	 * Adds a create marker task to the internal queue.
	 * 
	 * @param resource
	 *            the resource for which the marker will be created
	 * @param type
	 *            the marker type
	 * @param severity
	 *            the marker severity
	 * @param severity
	 *            the marker message
	 * @see IResource#createMarker(String)
	 */
	public void addCreateMarkerTask(IResource resource, String type, int severity, String message) {
		CreateMarkerTask cmt = new CreateMarkerTask(resource, type, severity, message);
		synchronized (taskQueue) {
			taskQueue.add(cmt);
		}
	}

	/**
	 * Adds a create marker task to the internal queue.
	 * 
	 * @param resource
	 *            the resource for which the marker will be created
	 * @param type
	 *            the marker type
	 * @param attributes
	 *            marker attributes that will be passed to the newly created marker using
	 *            {@link IMarker#setAttributes(Map)}.
	 * @see IResource#createMarker(String)
	 */
	public void addCreateMarkerTask(IResource resource, String type, Map<String, Object> attributes) {
		CreateMarkerTask cmt = new CreateMarkerTask(resource, type, attributes);
		synchronized (taskQueue) {
			taskQueue.add(cmt);
		}
	}

	/**
	 * Adds a create marker task to the internal queue.
	 * 
	 * @param resource
	 *            the resource for which the marker will be created
	 * @param markerDescriptor
	 *            the marker descriptor which holds the marker type and the marker attributes that will be passed to the
	 *            newly created marker using {@link IMarker#setAttributes(Map)}.
	 * @see IResource#createMarker(String)
	 */
	public void addCreateMarkerTask(IResource resource, MarkerDescriptor markerDescriptor) {
		addCreateMarkerTask(resource, markerDescriptor.getType(), markerDescriptor.getAttributes());
	}

	/**
	 * Adds a delete marker task to the internal queue which will delete all existing markers of the specified type.
	 * 
	 * @param resource
	 *            the resource for which markers will be deleted
	 * @param type
	 *            the marker type to delete
	 * @see IResource#deleteMarkers(String, boolean, int)
	 */
	public void addDeleteMarkerTask(IResource resource, String type) {
		DeleteMarkerTask dmt = new DeleteMarkerTask(resource, type);
		synchronized (taskQueue) {
			taskQueue.add(dmt);
		}
	}
}
