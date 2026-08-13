/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     BMW Car IT - Initial API and implementation
 *     itemis - [409367] Add a custom URI scheme to metamodel descriptor allowing mapping URI scheme to metamodel descriptor
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.metamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10Package;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.tests.emf.internal.mocks.MetaModelDescriptorExtensionRegistryMockFactory;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test1MM;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test1Release100;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test1Release101;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test1Release200;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test2MM;
import org.eclipse.sphinx.tests.emf.metamodel.descs.Test2Release100;
import org.junit.AfterClass;
import org.junit.Test;

@SuppressWarnings("nls")
public class MetaModelDescriptorRegistryTest {

	private MetaModelDescriptorExtensionRegistryMockFactory mockFactory = new MetaModelDescriptorExtensionRegistryMockFactory();

	@AfterClass
	public static void tearDown() throws Exception {
		MetaModelDescriptorRegistry.INSTANCE.setExtensionRegistry(null);
	}

	private void initMetaModelDescriptorRegistryWith(IMetaModelDescriptor... mmDescriptors) {
		IExtensionRegistry extensionRegistry = mockFactory.createExtensionRegistryMock(Activator.getPlugin(), mmDescriptors);
		MetaModelDescriptorRegistry.INSTANCE.setExtensionRegistry(extensionRegistry);
	}

	@Test
	public void testGetDescriptors() {
		initMetaModelDescriptorRegistryWith();
		List<? extends IMetaModelDescriptor> descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(new Test1MM());
		assertEmptyList(descriptors);

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE);
		descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(Test1Release100.INSTANCE);
		assertNotNull(descriptors);
		assertEquals(1, descriptors.size());
		assertTrue(descriptors.contains(Test1Release100.INSTANCE));

		descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(Test2MM.INSTANCE);
		assertEmptyList(descriptors);

		initMetaModelDescriptorRegistryWith(Test2MM.INSTANCE);
		descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(Test2MM.INSTANCE);
		assertNotNull(descriptors);
		assertEquals(1, descriptors.size());
		assertTrue(descriptors.contains(Test2MM.INSTANCE));

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE, Test1MM.INSTANCE);
		descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(Test1MM.INSTANCE);
		assertDescriptors(descriptors, Test1Release100.INSTANCE, Test1MM.INSTANCE);

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE, Test1MM.INSTANCE);
		descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(Test1Release100.INSTANCE);
		assertDescriptors(descriptors, Test1Release100.INSTANCE);

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE, Test2MM.INSTANCE);
		descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors(MetaModelDescriptorRegistry.ANY_MM);
		assertDescriptors(descriptors, Test1Release100.INSTANCE, Test2MM.INSTANCE);

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE, Test1MM.INSTANCE);
		descriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptors((IMetaModelDescriptor) null);
		assertEmptyList(descriptors);
	}

	@Test
	public void testGetResolvedDescriptors() {
		initMetaModelDescriptorRegistryWith();
		List<IMetaModelDescriptor> resolvedReleases = MetaModelDescriptorRegistry.INSTANCE.getResolvedDescriptors(new Test1MM());
		assertEmptyList(resolvedReleases);

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE);
		resolvedReleases = MetaModelDescriptorRegistry.INSTANCE.getResolvedDescriptors(new Test1MM());
		assertNotNull(resolvedReleases);
		if (Test1Release100.INSTANCE.getRootEPackage() == null) {
			assertEquals(0, resolvedReleases.size());
		} else {
			assertEquals(1, resolvedReleases.size());
			assertTrue(resolvedReleases.contains(Test1Release100.INSTANCE));
		}

		Test1MM test1MM = new Test1MM();
		Test1Release100 test1Release100 = new Test1Release100();

		EPackageRegistryImpl ePackageRegistry = new EPackageRegistryImpl();
		test1MM.setEPackageRegistry(ePackageRegistry);

		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackageRegistry.put(test1Release100.getNamespace(), ePackage);

		resolvedReleases = MetaModelDescriptorRegistry.INSTANCE.getResolvedDescriptors(new Test1MM());
		assertNotNull(resolvedReleases);
		assertEquals(1, resolvedReleases.size());
		assertTrue(resolvedReleases.contains(Test1Release100.INSTANCE));
	}

	@Test
	public void testGetDescriptorByName() {
		initMetaModelDescriptorRegistryWith();
		IMetaModelDescriptor version = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(Test1MM.INSTANCE, Test1Release100.NAME);
		assertNull(version);

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE);
		version = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(Test1MM.INSTANCE, Test1Release100.NAME);
		assertEquals(Test1Release100.INSTANCE, version);

		version = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(Test1MM.INSTANCE, Test1Release200.NAME);
		assertNull(version);
	}

	@Test
	public void testGetDescriptorByNamespace() {
		initMetaModelDescriptorRegistryWith();
		IMetaModelDescriptor version = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(Test1Release100.INSTANCE.getNamespaceURI());
		assertNull(version);

		initMetaModelDescriptorRegistryWith(Test1Release100.INSTANCE);
		version = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(Test1Release100.INSTANCE.getNamespaceURI());
		assertEquals(Test1Release100.INSTANCE, version);

		version = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(Test1MM.INSTANCE.getNamespaceURI());
		assertNull(version);
	}

	@Test
	public void testGetDescriptorByUri() {
		initMetaModelDescriptorRegistryWith(Test1MM.INSTANCE, Test1Release200.INSTANCE, Test1Release100.INSTANCE, Test2MM.INSTANCE,
				Test2Release100.INSTANCE);
		// Input match MM Namespace
		URI uri = URI.create("http://testA.sphinx.org");
		IMetaModelDescriptor metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNotNull(metaModelDescriptor);
		assertEquals(Test1MM.INSTANCE, metaModelDescriptor);

		uri = URI.create("http://testA.sphinx.org/2.0.0");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNotNull(metaModelDescriptor);
		assertEquals(Test1Release200.INSTANCE, metaModelDescriptor);

		uri = URI.create("http://testA.sphinx.org/1.0.0");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNotNull(metaModelDescriptor);
		assertEquals(Test1Release100.INSTANCE, metaModelDescriptor);
		// Input match EPackage NS URI pattern
		uri = URI.create("http://testA.sphinx.org/2.0.0/1");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNotNull(metaModelDescriptor);
		assertEquals(Test1Release200.INSTANCE, metaModelDescriptor);

		uri = URI.create("http://testA.sphinx.org/2.0.1");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNull(metaModelDescriptor);

		uri = URI.create("http://testA.sphinx.org/2.0.0/1a");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNull(metaModelDescriptor);

		uri = URI.create("2.0.0");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNull(metaModelDescriptor);
		// Input match compatible namespace
		// Input matches compatible resource version desciptors
		uri = URI.create("http://testA.sphinx.org/1.0.1");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNotNull(metaModelDescriptor);
		assertEquals(Test1Release100.INSTANCE, metaModelDescriptor);

		uri = URI.create("http://testA.sphinx.org/1.0.2");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNotNull(metaModelDescriptor);
		assertEquals(Test1Release100.INSTANCE, metaModelDescriptor);

		// Input Uri is address of ecore model without modelDescriptor
		// New metamodel descriptor is created and add to MetaModelDescriptor
		uri = URI.create(Hummingbird10Package.eNS_URI);
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNotNull(metaModelDescriptor);
		assertEquals(Hummingbird10MMDescriptor.INSTANCE.getNamespaceURI(), metaModelDescriptor.getNamespaceURI());

		// Input Uri is un-existing
		uri = URI.create("http://testA.sphinx.org/3.0.0");
		metaModelDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(uri);
		assertNull(metaModelDescriptor);
		// ====================
		// Null input
		URI nullUri = null;
		assertNull(MetaModelDescriptorRegistry.INSTANCE.getDescriptor(nullUri));
	}

	@Test
	public void testCompareDescriptor() {
		Test1MM version100 = Test1Release100.INSTANCE;
		Test1MM version101 = Test1Release101.INSTANCE;
		Test1MM version200 = Test1Release200.INSTANCE;

		int result;
		result = version100.compareTo(version101);
		assertEquals(-1, result);
		result = version101.compareTo(version200);
		assertEquals(-1, result);
		result = version200.compareTo(version100);
		assertEquals(1, result);

	}

	@Test
	public void testGetDescriptorsFromURIScheme() {
		List<IMetaModelDescriptor> targetMMDescriptors = new ArrayList<IMetaModelDescriptor>();

		// Register two meta model descriptors
		initMetaModelDescriptorRegistryWith(Test1MM.INSTANCE, Test2MM.INSTANCE);
		// Get meta model descriptors that use URIs with given scheme "tr1" as proxy URIs.
		org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createURI("tr1:/#test1Release.sphinx.org/scheme", true);
		String uriScheme = uri.scheme();
		assertEquals(uriScheme, "tr1");
		List<IMetaModelDescriptor> mmDescriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptorsFromURIScheme(uriScheme);
		targetMMDescriptors.add(Test1MM.INSTANCE);
		assertEquals(targetMMDescriptors, mmDescriptors);

		// Get meta model descriptors that use URIs with given scheme "tr2" as proxy URIs.
		uri = org.eclipse.emf.common.util.URI.createURI("tr2:/#test2Release.sphinx.org/scheme", true);
		uriScheme = uri.scheme();
		assertEquals(uriScheme, "tr2");
		mmDescriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptorsFromURIScheme(uriScheme);
		targetMMDescriptors.clear();
		targetMMDescriptors.add(Test2MM.INSTANCE);
		assertEquals(targetMMDescriptors, mmDescriptors);

		// Register three meta model descriptors
		initMetaModelDescriptorRegistryWith(Test1MM.INSTANCE, Test1Release100.INSTANCE, Test2MM.INSTANCE);
		// Get meta model descriptors that use URIs with given scheme "tr1" as proxy URIs.
		uri = org.eclipse.emf.common.util.URI.createURI("tr1:/#test1Release100.sphinx.org/scheme", true);
		uriScheme = uri.scheme();
		assertEquals(uriScheme, "tr1");
		mmDescriptors = MetaModelDescriptorRegistry.INSTANCE.getDescriptorsFromURIScheme(uriScheme);
		targetMMDescriptors.clear();
		targetMMDescriptors.add(Test1MM.INSTANCE);
		targetMMDescriptors.add(Test1Release100.INSTANCE);
		assertEquals(targetMMDescriptors, mmDescriptors);
	}

	private void assertDescriptors(List<? extends IMetaModelDescriptor> descriptors, IMetaModelDescriptor... expDescriptors) {
		assertNotNull(descriptors);
		assertEquals(expDescriptors.length, descriptors.size());
		for (IMetaModelDescriptor expDescriptor : expDescriptors) {
			descriptors.contains(expDescriptor);
		}
	}

	private void assertEmptyList(List<? extends IMetaModelDescriptor> versions) {
		assertNotNull(versions);
		assertTrue(versions.isEmpty());
	}
}
