/**
 * <copyright>
 *
 * Copyright (c) 2013 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - Bug [397949] Notification sequence is not guaranteed when resources in workspace change
 * </copyright>
 */
package org.eclipse.sphinx.platform.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * The resource change listeners registered to the Eclipse platform are notified of workspace changes. When a resource
 * change event is triggered, the listeners are notified in broadcast, but the notification sequence is not guaranteed.
 * This new resource change dispatcher listener is added for tracking resource changes. All the other listeners are
 * registered to this dispatcher and marked with a phase of PRE, MAIN or POST. When a workspace change happens, the
 * resource change dispatcher listener is notified, and it will dispatch the resource change event to the registered
 * listeners in the sequence of PRE, MAIN, POST.
 */
public class ResourceChangeDispatcher implements IResourceChangeListener {

	/**
	 * A resource change listener descriptor. This descriptor is composed of a {@link IResourceChangeListener resource
	 * change listener} and its event mask.
	 */
	public class ResourceChangeListenerDescriptor {
		private IResourceChangeListener resourceChangeListener;
		private int eventMask;

		/**
		 * Constructor for singleton pattern.
		 */
		public ResourceChangeListenerDescriptor(IResourceChangeListener resourceChangeListener, int eventMask) {
			this.resourceChangeListener = resourceChangeListener;
			this.eventMask = eventMask;
		}

		public IResourceChangeListener getResourceChangeListener() {
			return resourceChangeListener;
		}

		public int getEventMask() {
			return eventMask;
		}
	}

	/**
	 * An enumeration phase to indicate the priority that the resource change event will be dispatched to the registered
	 * listeners. The registered resource change listeners with phase PRE will be dispatched firstly, then the ones with
	 * phase MAIN, and finally the ones with phase POST.
	 */
	public static enum ResourceChangeDispatchPhase {
		PRE, MAIN, POST
	}

	private Map<ResourceChangeDispatchPhase, Collection<ResourceChangeListenerDescriptor>> resourceChangeListenerDescriptors = new HashMap<ResourceChangeDispatcher.ResourceChangeDispatchPhase, Collection<ResourceChangeListenerDescriptor>>();

	/**
	 * The singleton instance.
	 */
	public static final ResourceChangeDispatcher INSTANCE = new ResourceChangeDispatcher();

	/**
	 * Constructor for singleton pattern.
	 */
	private ResourceChangeDispatcher() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				this,
				IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_REFRESH
						| IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE);
	}

	/*
	 * @see
	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent
	 * )
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// Dispatch resource change event to registered resource change listeners honoring their respective dispatch
		// phases
		dispatchResourceChanged(event, resourceChangeListenerDescriptors.get(ResourceChangeDispatchPhase.PRE));
		dispatchResourceChanged(event, resourceChangeListenerDescriptors.get(ResourceChangeDispatchPhase.MAIN));
		dispatchResourceChanged(event, resourceChangeListenerDescriptors.get(ResourceChangeDispatchPhase.POST));
	}

	/**
	 * Dispatches resource change event to the collection of {@linkplain ResourceChangeListenerDescriptor listener
	 * descriptors}.
	 * 
	 * @param event
	 *            resource change event
	 * @param listenerDescriptors
	 *            a collection of {@linkplain ResourceChangeListenerDescriptor listener descriptors}
	 */
	protected void dispatchResourceChanged(IResourceChangeEvent event, Collection<ResourceChangeListenerDescriptor> listenerDescriptors) {
		if (listenerDescriptors != null) {
			int type = event.getType();
			for (ResourceChangeListenerDescriptor descriptor : listenerDescriptors) {
				IResourceChangeListener listener = descriptor.getResourceChangeListener();
				if ((type & descriptor.getEventMask()) != 0) {
					listener.resourceChanged(event);
				}
			}
		}
	}

	/**
	 * Adds the given {@linkplain IResourceChangeListener listener for resource change events} to this dispatcher. A
	 * {@linkplain ResourceChangeDispatchPhase resource change dispatch phase} may be used to specify the dispatch
	 * priority wrt to other registered listeners. Has no effect if an identical listener is already registered for the
	 * same dispatch phase.
	 * <p>
	 * This method is equivalent to:
	 * 
	 * <pre>
	 * addResourceChangeListener(listener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE,
	 * 		dispatchPhase);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param listener
	 *            the listener
	 * @param dispatchPhase
	 *            the {@linkplain ResourceChangeDispatchPhase resource change dispatch phase} that indicates the
	 *            dispatch priority wrt to other registered listeners
	 * @see IResourceChangeListener
	 * @see IResourceChangeEvent
	 * @see #addResourceChangeListener(IResourceChangeListener, int)
	 * @see #removeResourceChangeListener(IResourceChangeListener)
	 */
	public void addResourceChangeListener(IResourceChangeListener listener, ResourceChangeDispatchPhase dispatchPhase) {
		addResourceChangeListener(listener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE,
				dispatchPhase);
	}

	/**
	 * Adds the given {@linkplain IResourceChangeListener listener for resource change events} to this dispatcher. A
	 * {@linkplain ResourceChangeDispatchPhase resource change dispatch phase} may be used to specify the dispatch
	 * priority wrt to other registered listeners. Has no effect if an identical listener is already registered for
	 * these events and the same dispatch phase. After completion of this method, the given listener will be registered
	 * for exactly the specified events. If they were previously registered for other events, they will be
	 * de-registered.
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to resources in the workspace. The resource
	 * deltas in the resource change event are rooted at the workspace root. Most resource change notifications occur
	 * well after the fact; the exception is pre-notification of impending project closures and deletions. The listener
	 * continues to receive notifications until it is replaced or removed.
	 * </p>
	 * <p>
	 * Listeners can listen for several types of event as defined in <code>IResourceChangeEvent</code>. Clients are free
	 * to register for any number of event types however if they register for more than one, it is their responsibility
	 * to ensure they correctly handle the case where the same resource change shows up in multiple notifications.
	 * Clients are guaranteed to receive only the events for which they are registered.
	 * </p>
	 * 
	 * @param listener
	 *            the listener
	 * @param eventMask
	 *            the bit-wise OR of all event types of interest to the listener
	 * @param dispatchPhase
	 *            the {@linkplain ResourceChangeDispatchPhase resource change dispatch phase} that indicates the
	 *            dispatch priority wrt to other registered listeners
	 * @see IResourceChangeListener
	 * @see IResourceChangeEvent
	 * @see #removeResourceChangeListener(IResourceChangeListener)
	 */
	public void addResourceChangeListener(IResourceChangeListener listener, int eventMask, ResourceChangeDispatchPhase dispatchPhase) {
		if (dispatchPhase == null) {
			dispatchPhase = ResourceChangeDispatchPhase.MAIN;
		}

		Collection<ResourceChangeListenerDescriptor> listenerdescriptorsForPhase = resourceChangeListenerDescriptors.get(dispatchPhase);
		if (listenerdescriptorsForPhase == null) {
			listenerdescriptorsForPhase = new HashSet<ResourceChangeListenerDescriptor>();
			resourceChangeListenerDescriptors.put(dispatchPhase, listenerdescriptorsForPhase);
		}

		ResourceChangeListenerDescriptor listenerdescriptor = new ResourceChangeListenerDescriptor(listener, eventMask);
		listenerdescriptorsForPhase.add(listenerdescriptor);
	}

	public Map<ResourceChangeDispatchPhase, Collection<ResourceChangeListenerDescriptor>> getResourceChangeListeners() {
		return resourceChangeListenerDescriptors;
	}

	/**
	 * Removes the given {@linkplain IResourceChangeListener resource change listener} from this dispatcher. Has no
	 * effect if an identical listener is not registered.
	 * 
	 * @param listener
	 *            the listener
	 * @see IResourceChangeListener
	 * @see #addResourceChangeListener(IResourceChangeListener)
	 */
	public void removeResourceChangeListener(IResourceChangeListener listener) {

		Collection<ResourceChangeListenerDescriptor> listenerDescriptorsForPRE = resourceChangeListenerDescriptors
				.get(ResourceChangeDispatchPhase.PRE);
		if (listenerDescriptorsForPRE != null) {
			for (ResourceChangeListenerDescriptor preDescriptor : listenerDescriptorsForPRE) {
				if (preDescriptor.resourceChangeListener == listener) {
					listenerDescriptorsForPRE.remove(preDescriptor);
					return;
				}
			}
		}

		Collection<ResourceChangeListenerDescriptor> listenerDescriptorsForMAIN = resourceChangeListenerDescriptors
				.get(ResourceChangeDispatchPhase.MAIN);
		if (listenerDescriptorsForMAIN != null) {
			for (ResourceChangeListenerDescriptor mainDescriptor : listenerDescriptorsForMAIN) {
				if (mainDescriptor.resourceChangeListener == listener) {
					listenerDescriptorsForMAIN.remove(mainDescriptor);
					return;
				}
			}
		}

		Collection<ResourceChangeListenerDescriptor> listenerDescriptorsForPOST = resourceChangeListenerDescriptors
				.get(ResourceChangeDispatchPhase.POST);
		if (listenerDescriptorsForPOST != null) {
			for (ResourceChangeListenerDescriptor postDescriptor : listenerDescriptorsForPOST) {
				if (postDescriptor.resourceChangeListener == listener) {
					listenerDescriptorsForPOST.remove(postDescriptor);
					return;
				}
			}
		}
	}
}
