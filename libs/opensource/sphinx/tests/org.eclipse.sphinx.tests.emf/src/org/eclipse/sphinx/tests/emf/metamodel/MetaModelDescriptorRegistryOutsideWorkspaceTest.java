/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.tests.emf.metamodel;

import java.io.File;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.tests.emf.internal.Activator;
import org.eclipse.sphinx.testutils.AbstractTestCase;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.internal.resource.UMLResourceImpl;

@SuppressWarnings({ "nls", "restriction" })
public class MetaModelDescriptorRegistryOutsideWorkspaceTest extends AbstractTestCase {

	private static final String UML2_FILE_NAME = "uml2File.uml";

	@Override
	protected Plugin getTestPlugin() {
		return Activator.getPlugin();
	}

	public void testGetDescriptorForFileResource() throws Exception {

		// Create copy of UML2 test file in working directory
		File file = getTestFileAccessor().createWorkingCopyOfInputFile(UML2_FILE_NAME);
		assertTrue(file.exists());

		// Load UML2 test file from working directory
		Resource resource = new UMLResourceImpl(URI.createFileURI(file.getAbsolutePath()));
		resource.load(null);
		assertFalse(resource.getContents().isEmpty());
		EObject modelRoot = resource.getContents().get(0);
		assertTrue(modelRoot instanceof Model);

		// Retrieve metamodel descriptor from loaded UML2 resource
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
		assertTrue(mmDescriptor instanceof UML2MMDescriptor);
	}

	public void testGetDescriptorForPlatformPluginResource() throws Exception {

		// Load UML2 test file from resources/input folder of test plug-in
		EObject modelRoot = loadInputFile(UML2_FILE_NAME, new UMLResourceFactoryImpl(), null);
		assertTrue(modelRoot instanceof Model);
		Resource resource = modelRoot.eResource();
		assertNotNull(resource);

		// Retrieve metamodel descriptor from loaded UML2 resource
		IMetaModelDescriptor mmDescriptor = MetaModelDescriptorRegistry.INSTANCE.getDescriptor(resource);
		assertTrue(mmDescriptor instanceof UML2MMDescriptor);
	}
}
