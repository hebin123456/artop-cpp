/**
 * <copyright>
 * 
 * Copyright (c) 2013  itemis and others.
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
package org.eclipse.sphinx.tests.xtendxpand.integration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.sphinx.emf.mwe.IXtendXpandConstants;
import org.eclipse.sphinx.tests.xtendxpand.integration.internal.Activator;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandTestReferenceWorkspace;
import org.eclipse.xtend.shared.ui.core.internal.ResourceID;

public class WorkspaceStorageFinderTest extends XtendXpandIntegrationTestCase {

	/**
	 * Test method for {@link org.eclipse.xtend.shared.ui.Activator#findStorage(javaProject, resourceID, searchJars)}.
	 * Test findStorage() for template that is located in current workspace.
	 */
	public void testFindStorage_workspaceTemplate() throws Exception {
		// Check existence of template file
		IFile xptFile = refWks.codegenXpandProject.getFile(XtendXpandTestReferenceWorkspace.CONFIGH_XPT_FILE_PATH);
		assertNotNull(xptFile);
		assertTrue(xptFile.exists());

		String definitionName = XtendXpandTestReferenceWorkspace.XPAND_CONFIGH_DEFINITION_NAME;
		String templateExtension = IXtendXpandConstants.TEMPLATE_EXTENSION;

		// First case: test #findStorage() for template file that is located in current workspace but do not search in
		// referenced JAR files on the classpath
		IStorage storage = findStorage(refWks.codegenXpandProject, definitionName, templateExtension, false);

		// Make sure that this is supported
		assertNotNull(storage);
		assertEquals(xptFile, storage);

		// Second case: test #findStorage() for template file that is located in current workspace and search also in
		// referenced JAR files on the classpath
		storage = findStorage(refWks.codegenXpandProject, definitionName, templateExtension, true);

		// Make sure that this is supported
		assertNotNull(storage);
		assertEquals(xptFile, storage);
	}

	/**
	 * Test method for {@link org.eclipse.xtend.shared.ui.Activator#findStorage(javaProject, resourceID, searchJars)}.
	 * Test findStorage() for template that is located in underlying plug-in.
	 */
	public void testFindStorage_pluginTemplate() throws Exception {
		// Check existence of template file
		assertTrue(FileLocator.find(Activator.getPlugin().getBundle(), new Path(XtendXpandTestTemplatesInPlugin.CONFIGH_XPT_FILE_PATH), null) != null);

		String definitionName = XtendXpandTestTemplatesInPlugin.XPAND_CONFIGH_DEFINITION_NAME;
		String templateExtension = IXtendXpandConstants.TEMPLATE_EXTENSION;

		// First case: test #findStorage() for template file that is located in plug-ins but do not search in
		// referenced JAR files on the classpath
		IStorage storage = findStorage(refWks.codegenXpandProject, definitionName, templateExtension, false);

		// Make sure that this is not supported
		assertNull(storage);

		// Second case: test #findStorage() for template file that is located in plug-in and search also in
		// referenced JAR files on the classpath
		storage = findStorage(refWks.codegenXpandProject, definitionName, templateExtension, true);

		// Make sure that this is not supported
		assertNull(storage);
	}

	private IStorage findStorage(IProject project, String resourceName, String resourceExtension, boolean searchJars) throws Exception {
		IJavaProject javaProject = JavaCore.create(project);
		ResourceID resourceID = new ResourceID(resourceName, resourceExtension);
		return org.eclipse.xtend.shared.ui.Activator.findStorage(javaProject, resourceID, searchJars);
	}
}
