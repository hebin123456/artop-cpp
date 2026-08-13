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

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.sphinx.emf.mwe.resources.BasicWorkspaceResourceLoader;
import org.eclipse.sphinx.tests.xtendxpand.integration.internal.Activator;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandTestReferenceWorkspace;

public class BasicWorkspaceResourceLoaderTest extends XtendXpandIntegrationTestCase {

	public void testGetResource_inWorkspace() throws Exception {
		// Check existence of template file
		IFile xptFile = refWks.codegenXpandProject.getFile(XtendXpandTestReferenceWorkspace.CONFIGH_XPT_FILE_PATH);
		assertNotNull(xptFile);
		assertTrue(xptFile.exists());

		// Setup workspace resource loader
		BasicWorkspaceResourceLoader workspaceResourceLoader = new BasicWorkspaceResourceLoader();
		workspaceResourceLoader.setContextProject(refWks.codegenXpandProject);

		// First case: test #getResource() for template file that is located in current workspace but do not search in
		// JAR archives of required plug-ins
		workspaceResourceLoader.setSearchArchives(false);
		URL url = workspaceResourceLoader.getResource(XtendXpandTestReferenceWorkspace.CONFIGH_XPT_FILE_PATH);

		// Make sure that this is not supported
		assertNotNull(url);
		assertEquals(xptFile.getLocationURI().toURL(), url);

		// Second case: test #getResource() for template file that is located in current workspace and search also in
		// JAR archives of required plug-ins
		workspaceResourceLoader.setSearchArchives(true);
		url = workspaceResourceLoader.getResource(XtendXpandTestReferenceWorkspace.CONFIGH_XPT_FILE_PATH);

		// Make sure that this is supported
		assertNotNull(url);
		assertEquals(xptFile.getLocationURI().toURL(), url);
	}

	public void testGetResource_inPlugin() throws Exception {
		// Check existence of template file
		URL xptURL = FileLocator.find(Activator.getPlugin().getBundle(), new Path(XtendXpandTestTemplatesInPlugin.CONFIGH_XPT_FILE_PATH), null);
		assertTrue(xptURL != null);

		// Setup workspace resource loader
		BasicWorkspaceResourceLoader workspaceResourceLoader = new BasicWorkspaceResourceLoader();
		workspaceResourceLoader.setContextProject(refWks.codegenXpandProject);

		// First case: test #getResource() for template file that is located in plug-in but do not search in
		// JAR archives of required plug-ins
		workspaceResourceLoader.setSearchArchives(false);
		URL url = workspaceResourceLoader.getResource(XtendXpandTestTemplatesInPlugin.CONFIGH_XPT_FILE_PATH);

		// Make sure that this is not supported
		assertNull(url);

		// Second case: test #getResource() for template file that is located in plug-in and search also in
		// JAR archives of required plug-ins
		workspaceResourceLoader.setSearchArchives(true);
		url = workspaceResourceLoader.getResource(XtendXpandTestTemplatesInPlugin.CONFIGH_XPT_FILE_PATH);

		// Make sure that this is supported
		assertNotNull(url);
		assertEquals(xptURL.getPath(), url.getPath());
	}
}
