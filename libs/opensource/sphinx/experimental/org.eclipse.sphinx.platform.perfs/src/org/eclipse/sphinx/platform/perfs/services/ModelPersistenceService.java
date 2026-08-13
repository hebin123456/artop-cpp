/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
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
package org.eclipse.sphinx.platform.perfs.services;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class ModelPersistenceService {

	private ResourceSet resourceSet = new ResourceSetImpl();

	/**
	 * Singleton instance.
	 */
	public static ModelPersistenceService INSTANCE = new ModelPersistenceService();

	/*
	 * Private default constructor for singleton pattern
	 */
	private ModelPersistenceService() {
	}

	/**
	 * Returns the EMF resource corresponding to the given file.
	 * 
	 * @param file
	 *            a file.
	 * @return an EMF resource corresponding to the given file.
	 */
	public Resource getResource(File file) {
		Assert.isNotNull(file);

		URI uri = URI.createFileURI(file.getAbsolutePath());
		try {
			return resourceSet.getResource(uri, true);
		} catch (RuntimeException ex) {
			// Remove potentially created resource for problematic file from resource set
			Resource resource = resourceSet.getResource(uri, false);
			if (resource != null) {
				resourceSet.getResources().remove(resource);
			}
			throw ex;
		}
	}

	/**
	 * Created an EMF resource corresponding to the given file.
	 * 
	 * @param file
	 *            a file.
	 * @return the created EMF resource corresponding to the given file.
	 */
	public Resource createResource(File file) {
		Assert.isNotNull(file);

		return resourceSet.createResource(URI.createFileURI(file.getAbsolutePath()));
	}

	/**
	 * Returns the resource set to be used.
	 * 
	 * @return the resource set to be used.
	 */
	public ResourceSet getResourceSet() {
		return resourceSet;
	}
}
