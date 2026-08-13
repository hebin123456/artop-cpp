/**
 * <copyright>
 *
 * Copyright (c) 2011-2013 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.xtendxpand.integration;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.mwe.resources.BasicWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandTestReferenceWorkspace;
import org.eclipse.sphinx.xtend.typesystem.emf.SphinxManagedEmfMetaModel;
import org.eclipse.sphinx.xtendxpand.CheckEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.jobs.CheckJob;

public class CheckJobTest extends XtendXpandIntegrationTestCase {

	public CheckJobTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_NAME);
	}

	public void testHummingbird20Check() throws Exception {
		// Check existence of Hummingbird 2.0 type model file
		IFile hb20TypeModelFile = refWks.codegenXpandProject.getFile(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_HB_TYPE_MODEL_PATH);
		assertNotNull(hb20TypeModelFile);
		assertTrue(hb20TypeModelFile.exists());

		// Load Hummingbird 2.0 type model file
		Resource resource = EcorePlatformUtil.getResource(hb20TypeModelFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject platform = resource.getContents().get(0);
		assertNotNull(platform);
		assertTrue(platform instanceof Platform);

		// Check existence of Hummingbird 2.0 instance model file
		IFile hb20InstanceModelFile = refWks.codegenXpandProject
				.getFile(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_HB_INSTANCE_MODEL_PATH);
		assertNotNull(hb20InstanceModelFile);
		assertTrue(hb20InstanceModelFile.exists());

		// Load Hummingbird 2.0 instance model file
		resource = EcorePlatformUtil.getResource(hb20InstanceModelFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject application = resource.getContents().get(0);
		assertNotNull(application);
		assertTrue(application instanceof Application);

		// Check existence of check file
		IFile checkFile = refWks.codegenXpandProject.getFile(XtendXpandTestReferenceWorkspace.HB_CHK_FILE_PATH);
		assertNotNull(checkFile);
		assertTrue(checkFile.exists());

		// Check execution
		CheckEvaluationRequest checkEvaluationRequest = new CheckEvaluationRequest(checkFile, application);
		CheckJob checkJob = new CheckJob("Check Job", new SphinxManagedEmfMetaModel(hb20InstanceModelFile.getProject()), checkEvaluationRequest); //$NON-NLS-1$
		checkJob.setWorkspaceResourceLoader(new BasicWorkspaceResourceLoader());
		IStatus checkStatus = checkJob.run(new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, checkStatus);
	}
}
