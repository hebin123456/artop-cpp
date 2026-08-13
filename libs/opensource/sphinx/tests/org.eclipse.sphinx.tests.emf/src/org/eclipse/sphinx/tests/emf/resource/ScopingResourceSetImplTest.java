/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 BMW Car IT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.resource;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class ScopingResourceSetImplTest {

	private static final int HASH_1 = 1;
	private static final int HASH_2 = 2;
	private static final String HB20_CONTENTTYPE_ID = "org.eclipse.sphinx.examples.hummingbird20.hummingbird20XMIFile";

	private static final String TEST_PLUGIN_ID = Activator.getPlugin().getSymbolicName();
	private static final String INPUT_FILE_DIR_PATH_RELATIVE = "resources/input/ScopingResourceSetImplTest/";
	private static final String INPUT_FILE_DIR_PATH_ABSOLUTE = TEST_PLUGIN_ID + "/" + INPUT_FILE_DIR_PATH_RELATIVE;

	private static final URI URI_FILE_1 = URI.createPlatformPluginURI(INPUT_FILE_DIR_PATH_ABSOLUTE + "hbFile20_1.instancemodel", true);
	private static final URI URI_FILE_2 = URI.createPlatformPluginURI(INPUT_FILE_DIR_PATH_ABSOLUTE + "hbFile20_2.instancemodel", true);

	private ScopingResourceSetImpl fResourceSetUT;

	@Before
	public void createResourceSetUnderTest() throws Exception {
		fResourceSetUT = new ScopingResourceSetImpl();
	}

	@Test
	public void shouldReturnResourcesInOrderAsTheyWereAdded() {
		List<Resource> expectedResources = addResources(resource(URI_FILE_1).withHash(HASH_2), resource(URI_FILE_2).withHash(HASH_1));
		assertOrderedEquals(expectedResources, fResourceSetUT.getResourcesInScope(URI_FILE_1));
	}

	private void assertOrderedEquals(List<Resource> expectedResources, List<Resource> actualResources) {
		assertEquals(expectedResources.size(), actualResources.size());
		for (int i = 0; i < expectedResources.size(); i++) {
			assertEquals(expectedResources.get(i), actualResources.get(i));
		}
	}

	private List<Resource> addResources(Resource... resources) {
		Resource.Factory.Registry.INSTANCE.getContentTypeToFactoryMap().put(HB20_CONTENTTYPE_ID, new PrefilledResourceFactory(resources));
		loadResources(resources);
		return Arrays.asList(resources);
	}

	private void loadResources(Resource[] resources) {
		for (Resource resource : resources) {
			fResourceSetUT.getResource(resource.getURI(), true);
		}
	}

	private HashableResource resource(URI uri) {
		return new HashableResource(uri);
	}

	/**
	 * A Resource which allows its hashCode to be set explicitly.
	 */
	private static class HashableResource extends XMIResourceImpl {

		private int fHashCode;

		public HashableResource(URI uri) {
			setURI(uri);
		}

		public HashableResource withHash(int hashCode) {
			fHashCode = hashCode;
			return this;
		}

		@Override
		public int hashCode() {
			return fHashCode;
		}

	}

	/**
	 * A Resource.Factory which simply returns the already created created Resources with which it was initialized.
	 */
	private static class PrefilledResourceFactory implements Resource.Factory {

		private Map<URI, Resource> fResources = new HashMap<URI, Resource>();

		public PrefilledResourceFactory(Resource... resources) {
			for (Resource resource : resources) {
				fResources.put(resource.getURI(), resource);
			}
		}

		@Override
		public Resource createResource(URI uri) {
			return fResources.get(uri);
		}

	}

}
