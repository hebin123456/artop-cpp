/**
 * <copyright>
 *
 * Copyright (c) 2021 Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     Siemens - [577073] Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.workspace.referentialintegrity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.impl.TransactionalCommandStackImpl;
import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.IURIChangeDetectorDelegate;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.IURIChangeListener;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.URIChangeDetector;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.URIChangeDetectorDelegateRegistry;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.URIChangeEvent;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.URIChangeListenerRegistry;
import org.eclipse.sphinx.emf.workspace.referentialintegrity.URIChangeNotification;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UriChangeDetectorTest {

	private static class TestEOjbect extends EObjectImpl {
	}

	private static class TestResource1 extends ResourceImpl {
	}

	private static class TestResource2 extends ResourceImpl {
	}

	private static class AbstractTestURIChangeDetectorDelegate implements IURIChangeDetectorDelegate {

		List<Notification> notifications = null;
		Notification notification = null;
		IFile oldFile = null;
		IFile newFile = null;

		Map<Resource, List<URIChangeNotification>> returnValue1 = null;
		List<URIChangeNotification> returnValue2 = null;
		List<URIChangeNotification> returnValue3 = null;

		@Override
		public Map<Resource, List<URIChangeNotification>> detectChangedURIs(List<Notification> notifications) {
			this.notifications = notifications;
			return returnValue1;
		}

		@Override
		public List<URIChangeNotification> detectChangedURIs(Notification notification) {
			this.notification = notification;
			return returnValue2;
		}

		@Override
		public List<URIChangeNotification> detectChangedURIs(IFile oldFile, IFile newFile) {
			this.oldFile = oldFile;
			this.newFile = newFile;
			return returnValue3;
		}
	}

	private static class TestURIChangeDetectorDelegate1 extends AbstractTestURIChangeDetectorDelegate {
	}

	private static class TestURIChangeDetectorDelegate2 extends AbstractTestURIChangeDetectorDelegate {
	}

	private static class TestURIChangeListener implements IURIChangeListener {
		final List<URIChangeEvent> events = new ArrayList<>();

		@Override
		public void uriChanged(URIChangeEvent event) {
			events.add(event);
		}
	}

	private static final URIChangeDetector uriChangeDetector = new URIChangeDetector();
	private static final TestURIChangeDetectorDelegate1 delegate1 = new TestURIChangeDetectorDelegate1();
	private static final TestURIChangeDetectorDelegate2 delegate2 = new TestURIChangeDetectorDelegate2();
	private static final TestURIChangeListener listener = new TestURIChangeListener();

	@BeforeClass
	public static void init() {
		URIChangeDetectorDelegateRegistry.INSTANCE.addDelegate(TestResource1.class, delegate1);
		URIChangeDetectorDelegateRegistry.INSTANCE.addDelegate(TestResource2.class, delegate2);
		URIChangeListenerRegistry.INSTANCE.addListener(listener);
	}

	@AfterClass
	public static void clean() {
		URIChangeDetectorDelegateRegistry.INSTANCE.removeDelegate(delegate1);
		URIChangeDetectorDelegateRegistry.INSTANCE.removeDelegate(delegate2);
		URIChangeListenerRegistry.INSTANCE.removeListener(listener);
	}

	@Before
	public void reset() {
		delegate1.notifications = null;
		delegate1.notification = null;
		delegate1.newFile = null;
		delegate1.oldFile = null;
		delegate1.returnValue1 = null;
		delegate1.returnValue2 = null;
		delegate1.returnValue3 = null;
		delegate2.notifications = null;
		delegate2.notification = null;
		delegate2.newFile = null;
		delegate2.oldFile = null;
		delegate2.returnValue1 = null;
		delegate2.returnValue2 = null;
		delegate2.returnValue3 = null;
		listener.events.clear();
	}

	@Test
	public void testResourceSetChangedSimple() {
		TestResource1 testResource1 = new TestResource1();
		testResource1.getContents().add(new TestEOjbect());

		List<Notification> notifications = new ArrayList<>();
		notifications.add(createNotification(testResource1));

		delegate1.returnValue1 = createMockedDelegateReturnValue1(testResource1);

		uriChangeDetector.resourceSetChanged(createResourceSetChangedEvent(notifications));

		assertEquals(notifications, delegate1.notifications);
		assertEquals(1, listener.events.size());

		// deprecated API not called
		assertNull(delegate1.notification);

		assertSame(testResource1, listener.events.get(0).getSource());
		assertSame(delegate1.returnValue1.get(testResource1), listener.events.get(0).getNotifications());
	}

	@Test
	public void testResourceSetChangedMultipleResource() {
		TestResource1 testResource1 = new TestResource1();
		testResource1.getContents().add(new TestEOjbect());
		TestResource2 testResource2 = new TestResource2();
		testResource2.getContents().add(new TestEOjbect());

		List<Notification> notifications = new ArrayList<>();
		notifications.add(createNotification(testResource1));
		notifications.add(createNotification(testResource2));

		delegate1.returnValue1 = createMockedDelegateReturnValue1(testResource1);
		delegate2.returnValue1 = createMockedDelegateReturnValue1(testResource2);

		uriChangeDetector.resourceSetChanged(createResourceSetChangedEvent(notifications));

		assertEquals(1, delegate1.notifications.size());
		assertEquals(notifications.get(0), delegate1.notifications.get(0));

		assertEquals(1, delegate2.notifications.size());
		assertEquals(notifications.get(1), delegate2.notifications.get(0));

		// deprecated API not called
		assertNull(delegate1.notification);
		assertNull(delegate2.notification);

		assertEquals(2, listener.events.size());

		for (URIChangeEvent event : listener.events) {
			if (testResource1 == event.getSource()) {
				assertSame(testResource1, event.getSource());
				assertSame(delegate1.returnValue1.get(testResource1), event.getNotifications());
			} else {
				assertSame(testResource2, event.getSource());
				assertSame(delegate2.returnValue1.get(testResource2), event.getNotifications());
			}
		}
	}

	@Test
	public void testResourceSetChangedDeprecated() {
		TestResource1 testResource1 = new TestResource1();
		testResource1.getContents().add(new TestEOjbect());
		TestResource2 testResource2 = new TestResource2();
		testResource2.getContents().add(new TestEOjbect());

		List<Notification> notifications = new ArrayList<>();
		notifications.add(createNotification(testResource1));
		notifications.add(createNotification(testResource2));

		// Signal that the new API is not implemented
		delegate1.returnValue1 = null;
		delegate2.returnValue1 = null;

		// deprecated API
		delegate1.returnValue2 = createMockedDelegateReturnValue2();
		delegate2.returnValue2 = createMockedDelegateReturnValue2();

		uriChangeDetector.resourceSetChanged(createResourceSetChangedEvent(notifications));

		// non deprecated API called
		assertEquals(1, delegate1.notifications.size());
		assertEquals(notifications.get(0), delegate1.notifications.get(0));

		assertEquals(1, delegate2.notifications.size());
		assertEquals(notifications.get(1), delegate2.notifications.get(0));

		// deprecated API called
		assertNotNull(delegate1.notification);
		assertEquals(notifications.get(0), delegate1.notifications.get(0));

		assertNotNull(delegate2.notification);
		assertEquals(notifications.get(1), delegate2.notifications.get(0));

		assertEquals(2, listener.events.size());

		for (URIChangeEvent event : listener.events) {
			if (testResource1 == event.getSource()) {
				assertSame(testResource1, event.getSource());
				assertSame(delegate1.returnValue2, event.getNotifications());
			} else {
				assertSame(testResource2, event.getSource());
				assertSame(delegate2.returnValue2, event.getNotifications());
			}
		}
	}

	private Notification createNotification(ResourceImpl testResource) {
		return new ENotificationImpl((InternalEObject) testResource.getContents().get(0), Notification.ADD, null, null, null);
	}

	private ResourceSetChangeEvent createResourceSetChangedEvent(List<Notification> notifications) {
		return new ResourceSetChangeEvent(new TransactionalEditingDomainImpl(null, new TransactionalCommandStackImpl()), null, notifications);
	}

	private Map<Resource, List<URIChangeNotification>> createMockedDelegateReturnValue1(ResourceImpl testResource) {
		Map<Resource, List<URIChangeNotification>> returnValue = new HashMap<>();
		List<URIChangeNotification> uriNotifications = new ArrayList<URIChangeNotification>();
		uriNotifications.add(new URIChangeNotification(new TestEOjbect(), URI.createURI("test"))); //$NON-NLS-1$
		returnValue.put(testResource, uriNotifications);
		return returnValue;
	}

	private List<URIChangeNotification> createMockedDelegateReturnValue2() {
		List<URIChangeNotification> uriNotifications = new ArrayList<URIChangeNotification>();
		uriNotifications.add(new URIChangeNotification(new TestEOjbect(), URI.createURI("test"))); //$NON-NLS-1$
		return uriNotifications;
	}
}
