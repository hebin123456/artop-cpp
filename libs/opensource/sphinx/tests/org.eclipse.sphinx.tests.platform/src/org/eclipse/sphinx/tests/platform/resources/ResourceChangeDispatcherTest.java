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
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.platform.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.internal.events.ResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sphinx.platform.resources.ResourceChangeDispatcher;
import org.eclipse.sphinx.platform.resources.ResourceChangeDispatcher.ResourceChangeDispatchPhase;
import org.eclipse.sphinx.platform.resources.ResourceChangeDispatcher.ResourceChangeListenerDescriptor;

@SuppressWarnings("restriction")
public class ResourceChangeDispatcherTest extends TestCase {
	private Map<ResourceChangeDispatchPhase, Collection<ResourceChangeListenerDescriptor>> resourceChangeListeners = ResourceChangeDispatcher.INSTANCE
			.getResourceChangeListeners();
	private List<ResourceChangeDispatchPhase> phaseInvocations = new ArrayList<ResourceChangeDispatchPhase>();

	/**
	 * Listener1
	 */
	private class Listener1 implements IResourceChangeListener {
		private ResourceChangeDispatchPhase phase;

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			// changedListenerInvocations.add(this);
			phaseInvocations.add(phase);
		}

		public Listener1(String name, ResourceChangeDispatchPhase phase) {
			this.phase = phase;
		}
	}

	/**
	 * Listener2
	 */
	private class Listener2 implements IResourceChangeListener {
		private ResourceChangeDispatchPhase phase;

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			phaseInvocations.add(phase);
		}

		public Listener2(String name, ResourceChangeDispatchPhase phase) {
			this.phase = phase;
		}
	}

	/**
	 * Listener3
	 */
	private class Listener3 implements IResourceChangeListener {
		private ResourceChangeDispatchPhase phase;

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			phaseInvocations.add(phase);
		}

		public Listener3(String name, ResourceChangeDispatchPhase phase) {
			this.phase = phase;
		}
	}

	/**
	 * Gets if the resourceChangeListeners contains the listener
	 * 
	 * @param resourceChangeListeners
	 * @param expectedlListener
	 * @param dispatchPhase
	 * @return
	 */
	private boolean resourceChangeListenersContainsListener(
			Map<ResourceChangeDispatchPhase, Collection<ResourceChangeListenerDescriptor>> resourceChangeListeners,
			IResourceChangeListener expectedlListener, ResourceChangeDispatchPhase dispatchPhase) {
		Collection<ResourceChangeListenerDescriptor> listenerdescriptors = resourceChangeListeners.get(dispatchPhase);

		if (listenerdescriptors != null) {
			for (ResourceChangeListenerDescriptor descriptor : listenerdescriptors) {
				IResourceChangeListener listener = descriptor.getResourceChangeListener();
				if (listener == expectedlListener) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Test method for
	 * {@link ResourceChangeDispatcherTest#addResourceChangeListener(IResourceChangeListener, ResourceChangeDispatchPhase)}
	 */
	public void testAddResourceChangeListener() {
		// TEST add a PRE listener
		Listener1 listenerPRE = new Listener1("listenerPRE1", ResourceChangeDispatchPhase.PRE); //$NON-NLS-1$
		assertFalse(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPRE, ResourceChangeDispatchPhase.PRE));

		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPRE, ResourceChangeDispatchPhase.PRE);

		assertTrue(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPRE, ResourceChangeDispatchPhase.PRE));

		// add a MAIN listener
		Listener2 listenerMAIN = new Listener2("listenerMAIN1", ResourceChangeDispatchPhase.MAIN); //$NON-NLS-1$
		assertFalse(resourceChangeListenersContainsListener(resourceChangeListeners, listenerMAIN, ResourceChangeDispatchPhase.MAIN));

		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerMAIN, ResourceChangeDispatchPhase.MAIN);

		assertTrue(resourceChangeListenersContainsListener(resourceChangeListeners, listenerMAIN, ResourceChangeDispatchPhase.MAIN));

		// add a POST listener
		Listener3 listenerPOST = new Listener3("listenerPOST1", ResourceChangeDispatchPhase.POST); //$NON-NLS-1$
		assertFalse(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPOST, ResourceChangeDispatchPhase.POST));

		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPOST, ResourceChangeDispatchPhase.POST);

		assertTrue(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPOST, ResourceChangeDispatchPhase.POST));
	}

	/**
	 * Test method for
	 * {@link ResourceChangeDispatcherTest#addResourceChangeListener(IResourceChangeListener, int, ResourceChangeDispatchPhase)}
	 */
	public void testAddResourceChangeListenerWithMask() {
		// TEST add a PRE listener
		Listener1 listenerPRE = new Listener1("listenerPRE2", ResourceChangeDispatchPhase.PRE); //$NON-NLS-1$
		assertFalse(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPRE, ResourceChangeDispatchPhase.PRE));

		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPRE, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
				| IResourceChangeEvent.POST_CHANGE, ResourceChangeDispatchPhase.PRE);

		assertTrue(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPRE, ResourceChangeDispatchPhase.PRE));

		// add a MAIN listener
		Listener2 listenerMAIN = new Listener2("listenerMAIN2", ResourceChangeDispatchPhase.MAIN); //$NON-NLS-1$
		assertFalse(resourceChangeListenersContainsListener(resourceChangeListeners, listenerMAIN, ResourceChangeDispatchPhase.MAIN));

		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerMAIN, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
				| IResourceChangeEvent.POST_CHANGE, ResourceChangeDispatchPhase.MAIN);

		assertTrue(resourceChangeListenersContainsListener(resourceChangeListeners, listenerMAIN, ResourceChangeDispatchPhase.MAIN));

		// add a POST listener
		Listener3 listenerPOST = new Listener3("listenerPOST2", ResourceChangeDispatchPhase.POST); //$NON-NLS-1$
		assertFalse(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPOST, ResourceChangeDispatchPhase.POST));

		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPOST, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE
				| IResourceChangeEvent.POST_CHANGE, ResourceChangeDispatchPhase.POST);

		assertTrue(resourceChangeListenersContainsListener(resourceChangeListeners, listenerPOST, ResourceChangeDispatchPhase.POST));
	}

	/**
	 * Test method for {@link ResourceChangeDispatcherTest#resourceChanged(IResourceChangeEvent)}
	 */
	public void testResourceChanged1() {
		Listener1 listenerPRE = new Listener1("listenerPRE3", ResourceChangeDispatchPhase.PRE); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPRE, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.PRE);

		ResourceChangeDispatcher.INSTANCE.resourceChanged(new ResourceChangeEvent(ResourcesPlugin.getWorkspace(), IResourceChangeEvent.PRE_CLOSE, 0,
				null));

		assertEquals(phaseInvocations.size(), 1);
		assertEquals(phaseInvocations.get(0), ResourceChangeDispatchPhase.PRE);
	}

	/**
	 * Test method for {@link ResourceChangeDispatcherTest#resourceChanged(IResourceChangeEvent)}
	 */
	public void testResourceChanged2() {
		Listener2 listenerPOST = new Listener2("listenerPOST4", ResourceChangeDispatchPhase.POST); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPOST, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.POST);

		Listener2 listenerMAIN = new Listener2("listenerMAIN3", ResourceChangeDispatchPhase.MAIN); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerMAIN, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.MAIN);

		Listener1 listenerPRE = new Listener1("listenerPRE4", ResourceChangeDispatchPhase.PRE); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPRE, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.PRE);

		ResourceChangeDispatcher.INSTANCE.resourceChanged(new ResourceChangeEvent(ResourcesPlugin.getWorkspace(), IResourceChangeEvent.PRE_CLOSE, 0,
				null));

		assertEquals(phaseInvocations.size(), 3);
		assertEquals(phaseInvocations.get(0), ResourceChangeDispatchPhase.PRE);
		assertEquals(phaseInvocations.get(1), ResourceChangeDispatchPhase.MAIN);
		assertEquals(phaseInvocations.get(2), ResourceChangeDispatchPhase.POST);
	}

	/**
	 * Test method for {@link ResourceChangeDispatcherTest#resourceChanged(IResourceChangeEvent)}
	 */
	public void testResourceChanged3() {
		// add 3 PRE listeners, 3 MAIN listeners, 1 POST listener
		Listener3 listenerPOST = new Listener3("listenerPOST3", ResourceChangeDispatchPhase.POST); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPOST, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.POST);

		Listener2 listenerMAIN = new Listener2("listenerMAIN4", ResourceChangeDispatchPhase.MAIN); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerMAIN, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.MAIN);

		Listener1 listenerPRE = new Listener1("listenerPRE5", ResourceChangeDispatchPhase.PRE); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPRE, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.PRE);

		Listener2 listenerMAIN2 = new Listener2("listenerMAIN5", ResourceChangeDispatchPhase.MAIN); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerMAIN2, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.MAIN);

		Listener1 listenerPRE2 = new Listener1("listenerPRE6", ResourceChangeDispatchPhase.PRE); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPRE2, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.PRE);

		Listener2 listenerMAIN3 = new Listener2("listenerMAIN6", ResourceChangeDispatchPhase.MAIN); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerMAIN3, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.MAIN);

		Listener3 listenerPRE3 = new Listener3("listenerPRE7", ResourceChangeDispatchPhase.PRE); //$NON-NLS-1$
		ResourceChangeDispatcher.INSTANCE.addResourceChangeListener(listenerPRE3, IResourceChangeEvent.PRE_CLOSE, ResourceChangeDispatchPhase.PRE);

		ResourceChangeDispatcher.INSTANCE.resourceChanged(new ResourceChangeEvent(ResourcesPlugin.getWorkspace(), IResourceChangeEvent.PRE_CLOSE, 0,
				null));

		assertEquals(phaseInvocations.size(), 7);
		// PRE
		assertEquals(phaseInvocations.get(0), ResourceChangeDispatchPhase.PRE);
		assertEquals(phaseInvocations.get(1), ResourceChangeDispatchPhase.PRE);
		assertEquals(phaseInvocations.get(2), ResourceChangeDispatchPhase.PRE);

		// MAIN
		assertEquals(phaseInvocations.get(3), ResourceChangeDispatchPhase.MAIN);
		assertEquals(phaseInvocations.get(4), ResourceChangeDispatchPhase.MAIN);
		assertEquals(phaseInvocations.get(5), ResourceChangeDispatchPhase.MAIN);

		// POST
		assertEquals(phaseInvocations.get(6), ResourceChangeDispatchPhase.POST);
	}
}