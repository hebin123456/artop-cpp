/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.tests.emf.resource;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.resource.ExtendedResource;
import org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory;
import org.eclipse.sphinx.emf.util.EObjectUtil;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Component;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.InstanceModel20Factory;
import org.eclipse.sphinx.examples.hummingbird20.util.ExtendedHummingbirdResourceAdapter;
import org.eclipse.sphinx.examples.hummingbird20.util.Hummingbird20ResourceImpl;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.testutils.AbstractTestCase;
import org.junit.Test;

@SuppressWarnings("nls")
public class ExtendedResourceAdapterTest extends AbstractTestCase {

	private static final String HB_ROOT_OBJECT_URI = ExtendedHummingbirdResourceAdapter.HB_SCHEME + ExtendedResource.URI_SCHEME_SEPARATOR
			+ ExtendedResource.URI_SEGMENT_SEPARATOR + ExtendedResource.URI_FRAGMENT_SEPARATOR + ExtendedResource.URI_SEGMENT_SEPARATOR;

	private static final String HB_COMPONENT1_OBJECT_URI = HB_ROOT_OBJECT_URI + ExtendedResource.URI_SEGMENT_SEPARATOR + "component1";

	private static final String HB_FILE_NAME = "testdata" + ExtendedResource.URI_SEGMENT_SEPARATOR + "dummy.instancemodel";

	private static final String HB_RESOLVED_ROOT_OBJECT_URI = HB_FILE_NAME + ExtendedResource.URI_FRAGMENT_SEPARATOR
			+ ExtendedResource.URI_SEGMENT_SEPARATOR;

	private static final String HB_RESOLVED_COMPONENT1_OBJECT_URI = HB_RESOLVED_ROOT_OBJECT_URI + ExtendedResource.URI_SEGMENT_SEPARATOR
			+ "component1";

	@Override
	protected Plugin getTestPlugin() {
		return Activator.getPlugin();
	}

	@Test
	public void testGetURI_NormalEObject_WithoutResourceURI() {
		Application application = InstanceModel20Factory.eINSTANCE.createApplication();
		application.setName("application1");
		Component component = InstanceModel20Factory.eINSTANCE.createComponent();
		component.setName("component1");
		application.getComponents().add(component);
		Resource resource = new Hummingbird20ResourceImpl(URI.createURI(HB_FILE_NAME));
		resource.getContents().add(application);

		ExtendedResource resourceAdapter = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
		assertTrue(resourceAdapter instanceof ExtendedHummingbirdResourceAdapter);

		// resolve = false
		URI uri = resourceAdapter.getURI(application, false);
		assertEquals(HB_ROOT_OBJECT_URI, uri.toString());
		uri = resourceAdapter.getURI(component, false);
		assertEquals(HB_COMPONENT1_OBJECT_URI, uri.toString());
	}

	@Test
	public void testGetURI_NormalEObject_WithResourceURI() {
		Application application = InstanceModel20Factory.eINSTANCE.createApplication();
		application.setName("application1");
		Component component = InstanceModel20Factory.eINSTANCE.createComponent();
		component.setName("component1");
		application.getComponents().add(component);
		Resource resource = new Hummingbird20ResourceImpl(URI.createURI(HB_FILE_NAME));
		resource.getContents().add(application);

		ExtendedResource resourceAdapter = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
		assertTrue(resourceAdapter instanceof ExtendedHummingbirdResourceAdapter);

		// resolve = true
		URI uri = resourceAdapter.getURI(application, true);
		assertEquals(HB_RESOLVED_ROOT_OBJECT_URI, uri.toString());
		uri = resourceAdapter.getURI(component, true);
		assertEquals(HB_RESOLVED_COMPONENT1_OBJECT_URI, uri.toString());
	}

	@Test
	public void testGetURI_Proxy_WithoutResourceURI() {
		Application application = InstanceModel20Factory.eINSTANCE.createApplication();
		application.setName("application1");
		Component component = InstanceModel20Factory.eINSTANCE.createComponent();
		component.setName("component1");
		application.getComponents().add(component);
		Resource resource = new Hummingbird20ResourceImpl(URI.createURI(HB_FILE_NAME));
		resource.getContents().add(application);

		ExtendedResource resourceAdapter = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
		assertTrue(resourceAdapter instanceof ExtendedHummingbirdResourceAdapter);
		EObject applicationProxify = EObjectUtil.proxify(application);
		EObject componentProxify = EObjectUtil.proxify(component);

		// resolve = false
		URI uri = resourceAdapter.getURI(applicationProxify, false);
		assertEquals(HB_ROOT_OBJECT_URI, uri.toString());
		uri = resourceAdapter.getURI(componentProxify, false);
		assertEquals(HB_COMPONENT1_OBJECT_URI, uri.toString());
	}

	@Test
	public void testGetURI_Proxy_WithResourceURI() {
		Application application = InstanceModel20Factory.eINSTANCE.createApplication();
		application.setName("application1");
		Component component = InstanceModel20Factory.eINSTANCE.createComponent();
		component.setName("component1");
		application.getComponents().add(component);
		Resource resource = new Hummingbird20ResourceImpl(URI.createURI(HB_FILE_NAME));
		resource.getContents().add(application);

		ExtendedResource resourceAdapter = ExtendedResourceAdapterFactory.INSTANCE.adapt(resource);
		assertTrue(resourceAdapter instanceof ExtendedHummingbirdResourceAdapter);
		EObject applicationProxify = EObjectUtil.proxify(application);
		EObject componentProxify = EObjectUtil.proxify(component);

		// resolve = true
		URI uri = resourceAdapter.getURI(applicationProxify, true);
		assertEquals(HB_RESOLVED_ROOT_OBJECT_URI, uri.toString());
		uri = resourceAdapter.getURI(componentProxify, true);
		assertEquals(HB_RESOLVED_COMPONENT1_OBJECT_URI, uri.toString());
	}

	@Test
	public void testGetURI_NullResource_WithoutOldResourceURI() {
		Application application1 = InstanceModel20Factory.eINSTANCE.createApplication();
		application1.setName("application1");
		Component component1 = InstanceModel20Factory.eINSTANCE.createComponent();
		component1.setName("component1");
		application1.getComponents().add(component1);

		Application application2 = InstanceModel20Factory.eINSTANCE.createApplication();
		application2.setName("application2");

		Resource oldResource = new UnloadingHummingbird20ResourceImpl(URI.createURI(HB_FILE_NAME), Collections.singletonList((EObject) application2));
		oldResource.getContents().add(application1);
		ExtendedResource resourceAdapter = ExtendedResourceAdapterFactory.INSTANCE.adapt(oldResource);
		assertTrue(resourceAdapter instanceof ExtendedHummingbirdResourceAdapter);
		((ExtendedHummingbirdResourceAdapter) resourceAdapter).setTarget(oldResource);

		assertTrue(component1.eResource() != null);
		application2.getComponents().add(component1);
		assertTrue(component1.eResource() == null);

		// resolve = false
		URI uri = resourceAdapter.getURI(component1, false);
		assertEquals(HB_COMPONENT1_OBJECT_URI, uri.toString());
	}

	@Test
	public void testGetURI_NullResource_WithOldResourceURI() {
		Application application1 = InstanceModel20Factory.eINSTANCE.createApplication();
		application1.setName("application1");
		Component component1 = InstanceModel20Factory.eINSTANCE.createComponent();
		component1.setName("component1");
		application1.getComponents().add(component1);

		Application application2 = InstanceModel20Factory.eINSTANCE.createApplication();
		application2.setName("application2");

		Resource oldResource = new UnloadingHummingbird20ResourceImpl(URI.createURI(HB_FILE_NAME), Collections.singletonList((EObject) application2));
		oldResource.getContents().add(application1);
		ExtendedResource resourceAdapter = ExtendedResourceAdapterFactory.INSTANCE.adapt(oldResource);
		assertTrue(resourceAdapter instanceof ExtendedHummingbirdResourceAdapter);
		((ExtendedHummingbirdResourceAdapter) resourceAdapter).setTarget(oldResource);

		assertTrue(component1.eResource() != null);
		application2.getComponents().add(component1);
		assertTrue(component1.eResource() == null);

		// resolve = true
		URI uri = resourceAdapter.getURI(component1, true);
		assertEquals(HB_RESOLVED_COMPONENT1_OBJECT_URI, uri.toString());
	}

	public class UnloadingHummingbird20ResourceImpl extends Hummingbird20ResourceImpl {

		public UnloadingHummingbird20ResourceImpl(URI uri, List<EObject> initialUnloadingContents) {
			super(uri);

			if (unloadingContents == null) {
				unloadingContents = new BasicEList.FastCompare<EObject>();
			}
			unloadingContents.addAll(initialUnloadingContents);
		}
	}
}
