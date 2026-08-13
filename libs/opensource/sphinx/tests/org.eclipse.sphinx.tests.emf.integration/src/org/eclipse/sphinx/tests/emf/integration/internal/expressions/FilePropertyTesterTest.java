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
package org.eclipse.sphinx.tests.emf.integration.internal.expressions;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.sphinx.emf.internal.expressions.FilePropertyTester;
import org.eclipse.sphinx.examples.hummingbird10.Hummingbird10MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.uml2.ide.metamodel.UML2MMDescriptor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.DefaultTestReferenceWorkspace;

@SuppressWarnings("restriction")
public class FilePropertyTesterTest extends DefaultIntegrationTestCase {

	public FilePropertyTesterTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_10_A);
		projectsToLoad.add(DefaultTestReferenceWorkspace.HB_PROJECT_NAME_20_A);
	}

	/**
	 * Test method for {@link FilePropertyTester#test(Object, String, Object[], Object)}
	 */
	public void testTest() {
		assertNotNull(refWks.hbProject20_A);
		assertNotNull(refWks.hbProject10_A);
		assertTrue(refWks.hbProject20_A.exists());
		assertTrue(refWks.hbProject10_A.exists());

		FilePropertyTester tester = new FilePropertyTester();
		String validProperty = "metaModelIdMatches"; //$NON-NLS-1$
		String invalidProperty1 = "hummingbirdReleaseProperty"; //$NON-NLS-1$
		String invalidProperty2 = "isHummingbirdContent"; //$NON-NLS-1$
		String anyHummingbirdReleaseDescriptorIdPattern = "org.eclipse.sphinx.examples.hummingbird\\d\\w"; //$NON-NLS-1$
		Object[] args = new Object[] {};

		// No receiver object
		assertFalse(tester.test(null, validProperty, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(null, invalidProperty1, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(null, invalidProperty2, args, anyHummingbirdReleaseDescriptorIdPattern));

		// Invalid receiver object
		assertFalse(tester.test(new Object(), validProperty, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(new Object(), invalidProperty1, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(new Object(), invalidProperty2, args, anyHummingbirdReleaseDescriptorIdPattern));

		// HB20 file
		IFile receiver = refWks.hbProject20_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_20_20A_1);
		assertNotNull(receiver);
		assertTrue(receiver.exists());

		assertTrue(tester.test(receiver, validProperty, args, Hummingbird20MMDescriptor.INSTANCE.getIdentifier()));
		assertTrue(tester.test(receiver, validProperty, null, Hummingbird20MMDescriptor.INSTANCE.getIdentifier()));
		assertTrue(tester.test(receiver, validProperty, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertTrue(tester.test(receiver, validProperty, null, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(receiver, validProperty, args, Hummingbird10MMDescriptor.INSTANCE.getIdentifier()));
		assertFalse(tester.test(receiver, validProperty, null, Hummingbird10MMDescriptor.INSTANCE.getIdentifier()));
		assertFalse(tester.test(receiver, validProperty, args, UML2MMDescriptor.INSTANCE.getIdentifier()));
		assertFalse(tester.test(receiver, validProperty, null, UML2MMDescriptor.INSTANCE.getIdentifier()));

		assertFalse(tester.test(receiver, invalidProperty1, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(receiver, invalidProperty1, null, anyHummingbirdReleaseDescriptorIdPattern));

		assertFalse(tester.test(receiver, invalidProperty2, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(receiver, invalidProperty2, null, anyHummingbirdReleaseDescriptorIdPattern));

		// HB10 file
		receiver = refWks.hbProject10_A.getFile(DefaultTestReferenceWorkspace.HB_FILE_NAME_10_10A_1);
		assertNotNull(receiver);
		assertTrue(receiver.exists());

		assertTrue(tester.test(receiver, validProperty, args, Hummingbird10MMDescriptor.INSTANCE.getIdentifier()));
		assertTrue(tester.test(receiver, validProperty, null, Hummingbird10MMDescriptor.INSTANCE.getIdentifier()));
		assertTrue(tester.test(receiver, validProperty, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertTrue(tester.test(receiver, validProperty, null, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(receiver, validProperty, args, Hummingbird20MMDescriptor.INSTANCE.getIdentifier()));
		assertFalse(tester.test(receiver, validProperty, null, Hummingbird20MMDescriptor.INSTANCE.getIdentifier()));
		assertFalse(tester.test(receiver, validProperty, args, UML2MMDescriptor.INSTANCE.getIdentifier()));
		assertFalse(tester.test(receiver, validProperty, null, UML2MMDescriptor.INSTANCE.getIdentifier()));

		assertFalse(tester.test(receiver, invalidProperty1, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(receiver, invalidProperty1, null, anyHummingbirdReleaseDescriptorIdPattern));

		assertFalse(tester.test(receiver, invalidProperty2, args, anyHummingbirdReleaseDescriptorIdPattern));
		assertFalse(tester.test(receiver, invalidProperty2, null, anyHummingbirdReleaseDescriptorIdPattern));
	}
}
