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
package org.eclipse.sphinx.tests.emf.serialization.generators.util;

import static org.junit.Assert.assertSame;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public class LoadSaveUtil {

	public static EPackage loadEcorePackage(String fileName) throws IOException {
		Resource resource = LoadSaveUtil.loadResource(fileName, new EcoreResourceFactoryImpl(), null);
		assertSame(1, resource.getContents().size());
		assertSame(EcorePackage.Literals.EPACKAGE, resource.getContents().get(0).eClass());
		return (EPackage) resource.getContents().get(0);
	}

	public static Resource loadResource(String fileName, ResourceFactoryImpl factory, Map<?, ?> options) throws IOException {
		File file = new File(fileName);
		String path = file.getAbsolutePath();
		URI emfURI = URI.createFileURI(path);
		Resource resource = factory.createResource(emfURI);
		resource.load(options);

		return resource;
	}

	public static void saveEObject(EObject eObject, String fileName, ResourceFactoryImpl factory, Map<?, ?> options) throws IOException {
		File file = new File(fileName);
		String path = file.getAbsolutePath();
		URI emfURI = URI.createFileURI(path);
		// we need an uri that is not relative => otherwise deresolve doesn't work properly and we get long external
		// file references in the model
		Resource resource = factory.createResource(emfURI);
		resource.getContents().add(eObject);
		resource.save(options);

	}

	public static void saveAsXMI(Resource inputResource, String fileName, ResourceFactoryImpl factory, Map<?, ?> options) throws IOException {
		URI emfURI = URI.createURI(fileName, true);
		Resource resource = factory.createResource(emfURI);
		resource.getContents().addAll(inputResource.getContents());
		resource.save(options);
	}

	public static String loadFileAsString(String fileName) throws Exception {
		URI emfURI = URI.createURI(fileName, true);
		InputStream inputStream = new BufferedInputStream(URIConverter.INSTANCE.createInputStream(emfURI));
		try {
			byte[] buffer = new byte[1024];
			int bufferLength;
			StringBuilder content = new StringBuilder();
			while ((bufferLength = inputStream.read(buffer)) > -1) {
				content.append(new String(buffer, 0, bufferLength));
			}
			return content.toString();
		} finally {
			inputStream.close();
		}
	}

}
