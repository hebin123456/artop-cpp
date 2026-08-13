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
 *     Siemens - Initial API and implementation
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.xmi.XMLResource.ResourceHandler;
import org.eclipse.sphinx.emf.resource.ResourceHandlerRegistry;
import org.eclipse.sphinx.emf.resource.ResourceHandlerRegistry.DelegatingResourceHandler;
import org.junit.Test;

public class ResourceHandlerRegistryTest {

	private static final String NS_URI_PATTERN = "http://www.eclipse.org/sphinx/examples/hummingbird/2\\.0\\.\\d(/\\w+)*"; //$NON-NLS-1$
	private static final String NS_URI_EXAMPLE = "http://www.eclipse.org/sphinx/examples/hummingbird/2.0.1/common"; //$NON-NLS-1$

	@Test
	public void testExtensionPoint() {
		ResourceHandler handler = ResourceHandlerRegistry.INSTANCE.getHandler(NS_URI_EXAMPLE);
		assertNotNull(handler);
		// Registered through extension
		assertEquals(Hummingbird20ResourceHandler.class, handler.getClass());

		// Add a second one
		ResourceHandlerRegistry.INSTANCE.addHandlerType(NS_URI_PATTERN, Hummingbird20ResourceHandler2.class);
		handler = ResourceHandlerRegistry.INSTANCE.getHandler(NS_URI_EXAMPLE);

		assertNotNull(handler);
		assertEquals(DelegatingResourceHandler.class, handler.getClass());

		DelegatingResourceHandler delegatingHandler = (DelegatingResourceHandler) handler;

		assertEquals(2, delegatingHandler.getResourceHandlers().size());

		delegatingHandler.preLoad(null, null, null);
		delegatingHandler.postLoad(null, null, null);
		delegatingHandler.preSave(null, null, null);
		delegatingHandler.postSave(null, null, null);
		for (ResourceHandler resourceHandler : delegatingHandler.getResourceHandlers()) {
			assertTrue(resourceHandler instanceof Hummingbird20ResourceHandler);
			Hummingbird20ResourceHandler hummingbirdResourceHandler = (Hummingbird20ResourceHandler) resourceHandler;

			assertTrue(hummingbirdResourceHandler.isPreLoadCalled());
			assertTrue(hummingbirdResourceHandler.isPostLoadCalled());
			assertTrue(hummingbirdResourceHandler.isPreSaveCalled());
			assertTrue(hummingbirdResourceHandler.isPostSaveCalled());
		}
	}

	public static class Hummingbird20ResourceHandler2 extends Hummingbird20ResourceHandler {
	}

}
