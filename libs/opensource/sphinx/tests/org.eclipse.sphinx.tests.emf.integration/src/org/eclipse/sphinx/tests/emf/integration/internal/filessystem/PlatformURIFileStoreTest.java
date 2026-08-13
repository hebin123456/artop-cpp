/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.integration.internal.filessystem;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sphinx.emf.internal.filesystem.PlatformURIFileStore;
import org.eclipse.sphinx.tests.emf.integration.internal.Activator;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLFactory;

@SuppressWarnings({ "nls", "restriction" })
public class PlatformURIFileStoreTest extends DefaultIntegrationTestCase {

	private static final String SAMPLE_RESOURCE_NAME = "sampleResource.uml";
	private static final String SAMPLE_RESOURCE_URI_PLATFORM_META = "platform:/meta/" + Activator.getPlugin().getSymbolicName() + "/"
			+ SAMPLE_RESOURCE_NAME;
	private static final String SAMPLE_RESOURCE_URI_PLATFORM_RESOURCE = "platform:/resource/" + DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A
			+ "/" + SAMPLE_RESOURCE_NAME;
	private static final String SAMPLE_RESOURCE_URI_PLATFORM_PLUGIN = "platform:/plugin/" + "org.eclipse.sphinx.examples.hummingbird10" + "/"
			+ SAMPLE_RESOURCE_NAME;
	private static final String SAMPLE_RESOURCE_URI_FILE = "file:/C:/" + SAMPLE_RESOURCE_NAME;

	private static final String SAMPLE_MODEL_NAME = "UML object";

	public PlatformURIFileStoreTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	@Override
	protected void tearDown() throws Exception {
		deleteExternalResource(SAMPLE_RESOURCE_URI_PLATFORM_META);
		deleteExternalResource(SAMPLE_RESOURCE_URI_FILE);

		super.tearDown();
	}

	public void testExist() throws Exception {

		// #####################################################################
		// Test resource creation with uri of type meta

		// we fetch info before resource creation exist must return false.
		java.net.URI uri_meta = java.net.URI.create(SAMPLE_RESOURCE_URI_PLATFORM_META);
		IFileStore store = EFS.getStore(uri_meta);
		assertNotNull(store);
		assertTrue(store instanceof PlatformURIFileStore);
		IFileInfo info = store.fetchInfo();
		assertNotNull(info);
		assertFalse(info.exists());
		assertTrue(info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertFalse(info.getAttribute(EFS.ATTRIBUTE_HIDDEN));

		java.net.URI uri_resource = java.net.URI.create(SAMPLE_RESOURCE_URI_PLATFORM_RESOURCE);
		store = EFS.getStore(uri_resource);
		assertNotNull(store);
		assertTrue(store instanceof PlatformURIFileStore);
		info = store.fetchInfo();
		assertNotNull(info);
		assertFalse(info.exists());
		assertTrue(info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertFalse(info.getAttribute(EFS.ATTRIBUTE_HIDDEN));

		java.net.URI uri_plugin = java.net.URI.create(SAMPLE_RESOURCE_URI_PLATFORM_PLUGIN);
		store = EFS.getStore(uri_plugin);
		assertNotNull(store);
		assertTrue(store instanceof PlatformURIFileStore);
		info = store.fetchInfo();
		assertNotNull(info);
		assertFalse(info.exists());
		assertTrue(info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertFalse(info.getAttribute(EFS.ATTRIBUTE_HIDDEN));

		java.net.URI fileUri_plugin = java.net.URI.create(SAMPLE_RESOURCE_URI_FILE);
		store = EFS.getStore(fileUri_plugin);
		assertNotNull(store);
		assertFalse(store instanceof PlatformURIFileStore);
		// we fecth info after resource creation

		// resource creation
		Resource resource_meta = new XMIResourceImpl(URI.createURI(SAMPLE_RESOURCE_URI_PLATFORM_META, true));

		org.eclipse.uml2.uml.Model uml2Model = UMLFactory.eINSTANCE.createModel();
		resource_meta.getContents().add(uml2Model);

		// check if content successfully added
		assertEquals(1, resource_meta.getContents().size());

		// resource saved to uri :"platform:/meta/org.eclipse.sphinx.examples.hummingbird10/sampleResource.uml"
		resource_meta.save(Collections.emptyMap());

		store = EFS.getStore(uri_meta);
		assertNotNull(store);
		assertTrue(store instanceof PlatformURIFileStore);
		info = store.fetchInfo();
		assertNotNull(info);
		assertTrue(info.exists());
		assertFalse(info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertFalse(info.getAttribute(EFS.ATTRIBUTE_HIDDEN));
	}

	public void testSaveModelToMeta() throws Exception {

		// #####################################################################
		// Test resource creation with uri of type meta

		URI uri_meta = URI.createURI(SAMPLE_RESOURCE_URI_PLATFORM_META, true);

		// resource creation
		Resource resource_meta = new XMIResourceImpl(uri_meta);

		Model uml2Model = UMLFactory.eINSTANCE.createModel();
		uml2Model.setName(SAMPLE_MODEL_NAME);
		resource_meta.getContents().add(uml2Model);

		// check if content successfully added
		assertEquals(1, resource_meta.getContents().size());

		// resource saved to uri :"platform:/meta/org.eclipse.sphinx.examples.hummingbird10/sampleResource.uml"
		resource_meta.save(Collections.emptyMap());
		java.net.URI uri_meta_javanet = java.net.URI.create(SAMPLE_RESOURCE_URI_PLATFORM_META);
		IFileStore store = EFS.getStore(uri_meta_javanet);
		assertNotNull(store);
		assertTrue(store instanceof PlatformURIFileStore);
		IFileInfo info = store.fetchInfo();
		assertNotNull(info);
		assertTrue(info.exists());
		assertFalse(info.getAttribute(EFS.ATTRIBUTE_READ_ONLY));
		assertFalse(info.getAttribute(EFS.ATTRIBUTE_HIDDEN));
		// resource unloaded
		resource_meta.unload();

		// check if resource successfully unloaded
		assertEquals(0, resource_meta.getContents().size());

		// resource reloaded
		resource_meta.load(Collections.emptyMap());

		// check if content successfully reloaded
		assertEquals(1, resource_meta.getContents().size());

		// check if resource was correctly saved by comparing created object to retrieved object after loading.
		EObject eObject = resource_meta.getContents().get(0);

		assertTrue(eObject instanceof Model);

		Model savedModel = (Model) eObject;
		assertNotNull(savedModel);
		assertTrue(uml2Model.getName().equals(savedModel.getName()));
		// #####################################################################
		// Test resource creation with uri of type resource

		URI uri_resource = URI.createURI(SAMPLE_RESOURCE_URI_PLATFORM_RESOURCE, true);

		// resource creation
		Resource resource_resource = new XMIResourceImpl(uri_resource);

		uml2Model = UMLFactory.eINSTANCE.createModel();

		uml2Model.setName(SAMPLE_MODEL_NAME);
		resource_resource.getContents().add(uml2Model);

		// check if content successfully added
		assertEquals(1, resource_resource.getContents().size());

		// resource saved to uri :"platform:/meta/org.eclipse.sphinx.examples.hummingbird10/sampleResource.uml"
		resource_resource.save(Collections.emptyMap());

		// resource unloaded
		resource_resource.unload();

		// check if resource successfully unloaded
		assertEquals(0, resource_resource.getContents().size());

		// resource reloaded
		resource_resource.load(Collections.emptyMap());

		// check if content successfully reloaded
		assertEquals(1, resource_resource.getContents().size());

		// check if resource was correctly saved by comparing created object to retrieved object after loading.
		eObject = resource_resource.getContents().get(0);

		assertTrue(eObject instanceof Model);

		savedModel = (Model) eObject;

		assertTrue(uml2Model.getName().equals(savedModel.getName()));

		// #####################################################################
		// Test resource creation with uri of type plugin

		URI uri_plugin = URI.createURI(SAMPLE_RESOURCE_URI_PLATFORM_PLUGIN, true);

		// resource creation
		Resource resource_plugin = new XMIResourceImpl(uri_plugin);

		uml2Model = UMLFactory.eINSTANCE.createModel();

		resource_plugin.getContents().add(uml2Model);

		// check if content successfully added
		assertEquals(1, resource_plugin.getContents().size());

		try {
			resource_plugin.save(Collections.emptyMap());
			assertTrue("Save a resource under /plugin must be read only.", false);
		} catch (Exception ex) {
			assertTrue(true);
		}
	}
}
