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
 *     itemis - [343844] Enable multiple Xtend MetaModels to be configured on BasicM2xAction, M2xConfigurationWizard, and Xtend/Xpand/CheckJob
 *     itemis - [406564] BasicWorkspaceResourceLoader#getResource should not delegate to super
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.xtendxpand.integration;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.mwe.resources.BasicWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird20.instancemodel.Application;
import org.eclipse.sphinx.tests.xtendxpand.integration.internal.Activator;
import org.eclipse.sphinx.testutils.TestFileAccessor;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandTestReferenceWorkspace;
import org.eclipse.sphinx.xtend.typesystem.emf.SphinxManagedEmfMetaModel;
import org.eclipse.sphinx.xtendxpand.XpandEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.jobs.XpandJob;
import org.eclipse.sphinx.xtendxpand.outlet.ExtendedOutlet;

public class XpandJobTest extends XtendXpandIntegrationTestCase {

	public XpandJobTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_NAME);
	}

	public void testHummingbird20Codegen_workspaceTemplate() throws Exception {
		// Check existence of Hummingbird 2.0 instance model file
		IFile hb20InstanceModelFile = refWks.codegenXpandProject
				.getFile(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_HB_INSTANCE_MODEL_PATH);
		assertNotNull(hb20InstanceModelFile);
		assertTrue(hb20InstanceModelFile.exists());

		Resource resource = EcorePlatformUtil.getResource(hb20InstanceModelFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject application = resource.getContents().get(0);
		assertNotNull(application);
		assertTrue(application instanceof Application);

		/*
		 * Execute an Xpand job that generates current working directory
		 */

		// Check existence of template file
		IFile xptFile = refWks.codegenXpandProject.getFile(XtendXpandTestReferenceWorkspace.CONFIGH_XPT_FILE_PATH);
		assertNotNull(xptFile);
		assertTrue(xptFile.exists());

		// Xpand execution
		XpandEvaluationRequest xpandEvaluationRequest = new XpandEvaluationRequest(XtendXpandTestReferenceWorkspace.XPAND_CONFIGH_DEFINITION_NAME,
				application);
		SphinxManagedEmfMetaModel metaModel = new SphinxManagedEmfMetaModel(hb20InstanceModelFile.getProject());
		XpandJob xpandJob = new XpandJob("Xpand Job", metaModel, xpandEvaluationRequest); //$NON-NLS-1$
		xpandJob.setWorkspaceResourceLoader(new BasicWorkspaceResourceLoader());
		IStatus xpandStatus = xpandJob.runInWorkspace(new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, xpandStatus);

		// Load generated file from current working directory and verify its content
		File file = new File(XtendXpandTestReferenceWorkspace.CONFIGH_FILE_NAME);
		assertNotNull(file);
		assertTrue(file.exists());
		String contents = TestFileAccessor.readAsString(file);
		assertTrue(contents.indexOf("#define ParamVal1 111") != -1); //$NON-NLS-1$
		assertTrue(contents.indexOf("#define ParamVal2 222") != -1); //$NON-NLS-1$
		assertTrue(contents.indexOf("#define ParamVal3 333") != -1); //$NON-NLS-1$

		/*
		 * Execute another Xpand job that generates into 'HOUTLET' folder
		 */

		// Check existence of template file
		IFile xptFileHOutlet = refWks.codegenXpandProject.getFile(XtendXpandTestReferenceWorkspace.CONFIGH_TO_HOUTLET_XPT_FILE_PATH);
		assertNotNull(xptFileHOutlet);
		assertTrue(xptFileHOutlet.exists());

		// Create outlet
		IFolder outletFolder = refWks.codegenXpandProject.getFolder(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_HOUTLET_FOLDER_NAME);
		ExtendedOutlet outlet = new ExtendedOutlet(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_HOUTLET_FOLDER_NAME, outletFolder);
		outlet.setOverwrite(true);

		// Xpand execution
		xpandEvaluationRequest = new XpandEvaluationRequest(XtendXpandTestReferenceWorkspace.XPAND_CONFIGH_TOHOUTLET_DEFINITION_NAME, application);
		xpandJob = new XpandJob("Xpand Job", metaModel, xpandEvaluationRequest); //$NON-NLS-1$
		xpandJob.setWorkspaceResourceLoader(new BasicWorkspaceResourceLoader());
		xpandJob.getOutlets().add(outlet);
		xpandStatus = xpandJob.runInWorkspace(new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, xpandStatus);

		// Load generated file from 'HOUTLET' folder and verify its content
		IFile genFile = outletFolder.getFile(XtendXpandTestReferenceWorkspace.CONFIGH_FILE_NAME);
		assertNotNull(genFile);
		assertTrue(genFile.exists());
		contents = TestFileAccessor.readAsString(genFile);
		assertTrue(contents.indexOf("#define ParamVal1 111") != -1); //$NON-NLS-1$
		assertTrue(contents.indexOf("#define ParamVal2 222") != -1); //$NON-NLS-1$
		assertTrue(contents.indexOf("#define ParamVal3 333") != -1); //$NON-NLS-1$
	}

	public void testHummingbird20Codegen_pluginTemplate() throws Exception {
		// Check existence of Hummingbird 2.0 instance model file
		IFile hb20InstanceModelFile = refWks.codegenXpandProject
				.getFile(XtendXpandTestReferenceWorkspace.HB_CODEGEN_XPAND_PROJECT_HB_INSTANCE_MODEL_PATH);
		assertNotNull(hb20InstanceModelFile);
		assertTrue(hb20InstanceModelFile.exists());

		// Load Hummingbird 2.0 instance model file
		Resource resource = EcorePlatformUtil.getResource(hb20InstanceModelFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject application = resource.getContents().get(0);
		assertNotNull(application);
		assertTrue(application instanceof Application);

		/*
		 * Execute an Xpand job that generates current working directory
		 */

		// Check existence of template file
		URL xptURL = FileLocator.find(Activator.getPlugin().getBundle(), new Path(XtendXpandTestTemplatesInPlugin.CONFIGH_XPT_FILE_PATH), null);
		assertTrue(xptURL != null);

		// Xpand execution
		XpandEvaluationRequest xpandEvaluationRequest = new XpandEvaluationRequest(XtendXpandTestTemplatesInPlugin.XPAND_CONFIGH_DEFINITION_NAME,
				application);
		SphinxManagedEmfMetaModel metaModel = new SphinxManagedEmfMetaModel(hb20InstanceModelFile.getProject());
		XpandJob xpandJob = new XpandJob("Xpand Job", metaModel, xpandEvaluationRequest); //$NON-NLS-1$
		xpandJob.setWorkspaceResourceLoader(new BasicWorkspaceResourceLoader());
		IStatus xpandStatus = xpandJob.runInWorkspace(new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, xpandStatus);

		// Load generated file from current working directory and verify its content
		File file = new File(XtendXpandTestReferenceWorkspace.CONFIGH_FILE_NAME);
		assertNotNull(file);
		assertTrue(file.exists());
		String contents = TestFileAccessor.readAsString(file);
		assertTrue(contents.indexOf("#define ParamVal1 111") != -1); //$NON-NLS-1$
		assertTrue(contents.indexOf("#define ParamVal2 222") != -1); //$NON-NLS-1$
		assertTrue(contents.indexOf("#define ParamVal3 333") != -1); //$NON-NLS-1$
	}
}
