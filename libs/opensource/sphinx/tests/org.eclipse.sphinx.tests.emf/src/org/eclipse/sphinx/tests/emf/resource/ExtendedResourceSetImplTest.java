/**
 * <copyright>
 *
 * Copyright (c) 2014 itemis and others.
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

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.testutils.AbstractTestCase;
import org.junit.Test;

public class ExtendedResourceSetImplTest extends AbstractTestCase {

	private static final String UML_FILEA_NAME = "umlFileA.uml";//$NON-NLS-1$
	private static final String UML2_FILEA_NAME = "uml2FileA.uml"; //$NON-NLS-1$
	private static final String UML_FILEB_NAME = "umlFileB.uml"; //$NON-NLS-1$
	private static final String UML2_FILEB_NAME = "uml2FileB.uml"; //$NON-NLS-1$

	@Override
	protected Plugin getTestPlugin() {
		return Activator.getPlugin();
	}

	@Test
	/**
	 * Verify that getResource() returns the same resource with normalized and non-normalized URI
	 */
	public void testGetResource() throws IOException {

		java.net.URI fileURI = getTestFileAccessor().getWorkingFileURI(UML_FILEA_NAME);
		// emfURI:
		// file:/C:/Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/umlFileA.uml
		URI emfURI = getTestFileAccessor().convertToEMFURI(fileURI);

		ExtendedResourceSetImpl fResourceSetUT = new ExtendedResourceSetImpl();
		Resource resource = fResourceSetUT.createResource(emfURI);
		resource.save(null);

		// create a new path that replace the file name of the resource path
		IPath path = new Path(emfURI.trimFragment().path());
		String uml2Path = path.toString().replace(UML_FILEA_NAME, UML2_FILEA_NAME);
		// uml2URI:
		// /Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/uml2FileA.uml
		URI uml2URI = URI.createURI(uml2Path);
		// uml2PlatformPluginURI:
		// platform:/plugin/Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/uml2FileA.uml
		URI uml2PlatformPluginURI = URI.createPlatformPluginURI(uml2Path, true);

		// add the URIs to the URIMap
		URIConverter uriConverter = fResourceSetUT.getURIConverter();
		uriConverter.getURIMap().put(uml2URI, emfURI);
		uriConverter.getURIMap().put(uml2PlatformPluginURI, emfURI);

		// test the following three cases:
		// a) Lookup under the same unnormalized URI that has been used for loading the resource: resource will be
		// immediately found in the cache => fast lookup
		Resource resource1 = fResourceSetUT.getResource(emfURI, true);
		assertSame(resource, resource1);

		// b) Lookup under normalized URI: resource will be immediately found in the cache => fast lookup
		Resource resource2 = fResourceSetUT.getResource(uriConverter.normalize(emfURI), true);
		assertSame(resource, resource2);

		// c) Lookup under another unnormalized URI as that which has been used for loading the resource: resource will
		// not be found in the cache => slow lookup by normalizing provided URI and performing second lookup using the
		// latter; if lookup succeeds the "new" unnormalized URI is added to the cache as well (just as proposed in
		// description)
		Resource resource3 = fResourceSetUT.getResource(uml2URI, true);
		assertSame(resource, resource3);
		resource3 = fResourceSetUT.getResource(uml2PlatformPluginURI, true);
		assertSame(resource, resource3);
	}

	@Test
	/**
	 * Verify that getResource() returns the loaded/unloaded resource as the loadOnDemand option indicates
	 */
	public void testGetResource_LoadOnDemand() throws IOException {

		java.net.URI fileA_URI = getTestFileAccessor().getWorkingFileURI(UML_FILEA_NAME);
		// emfURI:
		// file:/C:/Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/umlFileA.uml
		URI emfURI = getTestFileAccessor().convertToEMFURI(fileA_URI);

		// ---------------------------------------------------------------------------------------------------
		// resource created with non-normalized URI, verify the returned resource is loaded/unloaded as the
		// loadOnDemand option
		// create a resource with a EMF URI
		ExtendedResourceSetImpl fResourceSetUT = new ExtendedResourceSetImpl();
		// create and add the new resource to the resource list
		Resource resourceA = fResourceSetUT.createResource(emfURI);
		resourceA.save(null);

		// create a new path that replace the file name of the resource path
		IPath pathA = new Path(emfURI.trimFragment().path());
		String uml2PathA = pathA.toString().replace(UML_FILEA_NAME, UML2_FILEA_NAME);
		// uml2aURI:
		// /Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/uml2FileA.uml
		URI uml2aURI = URI.createURI(uml2PathA);
		// uml2aPlatformResourceURI:
		// platform:/resource/Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/uml2FileA.uml
		URI uml2aPlatformResourceURI = URI.createPlatformResourceURI(uml2PathA, true);

		// add the URIs to the URIMap
		URIConverter uriConverter = fResourceSetUT.getURIConverter();
		uriConverter.getURIMap().put(uml2aPlatformResourceURI, emfURI);

		// getResource with normalized uri and loadOnDemand=false
		Resource resource1 = fResourceSetUT.getResource(uriConverter.normalize(emfURI), false);
		assertSame(resourceA, resource1);
		// verify resource is not loaded
		assertFalse(resource1.isLoaded());

		// getResource with other non-normalized uri and loadOnDemand=false
		Resource resource2 = fResourceSetUT.getResource(uml2aPlatformResourceURI, false);
		assertSame(resourceA, resource2);
		// verify resource is not loaded
		assertFalse(resource2.isLoaded());

		// getResource with other non-normalized uri and loadOnDemand=true
		resource2 = fResourceSetUT.getResource(uml2aURI, true);
		assertSame(resourceA, resource2);
		// verify resource is loaded
		assertTrue(resource2.isLoaded());

		// ---------------------------------------------------------------------------------------------------
		// resource created with normalized URI, verify the returned resource is loaded/unloaded as the
		// loadOnDemand option
		java.net.URI fileB_URI = getTestFileAccessor().getWorkingFileURI(UML_FILEB_NAME);
		// emfURI2:file:/C:/Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/umlFileB.uml
		URI emfURI2 = getTestFileAccessor().convertToEMFURI(fileB_URI);

		// create a resource with a normalized URI
		URI normalizedEmfURI2 = uriConverter.normalize(emfURI2);
		Resource resourceB = fResourceSetUT.createResource(normalizedEmfURI2);
		resourceB.save(null);

		IPath pathB = new Path(normalizedEmfURI2.trimFragment().path());
		String uml2PathB = pathB.toString().replace(UML_FILEB_NAME, UML2_FILEB_NAME);
		// uml2bPlatformResourceURI:
		// platform:/resource/Eclipse/Kepler/WS-Eclipse-Kepler/org.eclipse.sphinx/tests/org.eclipse.sphinx.tests.emf/working-dir/uml2FileB.uml
		URI uml2bPlatformResourceURI = URI.createPlatformPluginURI(uml2PathB, true);

		// add the URIs to the URIMap
		uriConverter.getURIMap().put(uml2bPlatformResourceURI, normalizedEmfURI2);

		// getResource with other non-normalized uri and loadOnDemand=false
		resource2 = fResourceSetUT.getResource(uml2bPlatformResourceURI, false);
		assertSame(resourceB, resource2);
		// verify resource is not loaded
		assertFalse(resource2.isLoaded());

		// getResource with normalized uri and loadOnDemand=false
		resource1 = fResourceSetUT.getResource(normalizedEmfURI2, false);
		assertSame(resourceB, resource1);
		// verify resource is not loaded
		assertFalse(resource1.isLoaded());

		// getResource with normalized uri and loadOnDemand=true
		resource1 = fResourceSetUT.getResource(normalizedEmfURI2, true);
		assertSame(resourceB, resource1);
		// verify resource is loaded
		assertTrue(resource1.isLoaded());
	}
}
