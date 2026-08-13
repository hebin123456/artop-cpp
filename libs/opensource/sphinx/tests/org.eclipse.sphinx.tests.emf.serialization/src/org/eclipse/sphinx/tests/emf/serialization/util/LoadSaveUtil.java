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
package org.eclipse.sphinx.tests.emf.serialization.util;

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

public class LoadSaveUtil {
	public static Resource loadResource(String fileName, ResourceFactoryImpl factory, Map<?, ?> options) throws IOException {
		URI emfURI = URI.createURI(fileName, true);
		Resource resource = factory.createResource(emfURI);
		resource.load(options);

		return resource;
	}

	public static void saveAsXMI(Resource inputResource, String fileName, ResourceFactoryImpl factory, Map<?, ?> options) throws IOException {
		URI emfURI = URI.createURI(fileName, true);
		Resource resource = factory.createResource(emfURI);
		resource.getContents().addAll(inputResource.getContents());
		resource.save(options);
	}

}
