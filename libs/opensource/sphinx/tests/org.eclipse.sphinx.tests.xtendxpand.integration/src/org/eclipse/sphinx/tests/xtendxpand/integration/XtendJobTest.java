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
 *     itemis - [358131] Make Xtend/Xpand/CheckJobs more robust against template file encoding mismatches
 *     itemis - [418005] Add support for model files with multiple root elements
 *     itemis - [423676] AbstractIntegrationTestCase unable to remove project references that are no longer needed
 *
 * </copyright>
 */
package org.eclipse.sphinx.tests.xtendxpand.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sphinx.emf.mwe.resources.BasicWorkspaceResourceLoader;
import org.eclipse.sphinx.emf.util.EcorePlatformUtil;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.Platform;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandIntegrationTestCase;
import org.eclipse.sphinx.testutils.integration.referenceworkspace.xtendxpand.XtendXpandTestReferenceWorkspace;
import org.eclipse.sphinx.xtend.typesystem.emf.SphinxManagedEmfMetaModel;
import org.eclipse.sphinx.xtendxpand.XtendEvaluationRequest;
import org.eclipse.sphinx.xtendxpand.jobs.XtendJob;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.xtend.typesystem.MetaModel;
import org.eclipse.xtend.typesystem.uml2.UML2MetaModel;

public class XtendJobTest extends XtendXpandIntegrationTestCase {

	public XtendJobTest() {
		// Set subset of projects to load
		Set<String> projectsToLoad = getProjectSubsetToLoad();
		projectsToLoad.add(XtendXpandTestReferenceWorkspace.HB_TRANSFORM_XTEND_PROJECT_NAME);
	}

	public void testUML2ToHummingbird20Transform() throws Exception {
		// Check existence of UML2 file
		IFile umlModelFile = refWks.transformXtendProject.getFile(XtendXpandTestReferenceWorkspace.HB_TRANSFORM_XTEND_PROJECT_UML_MODEL_PATH);
		assertNotNull(umlModelFile);
		assertTrue(umlModelFile.exists());

		// Check existence of extension file
		IFile extFile = refWks.transformXtendProject.getFile(XtendXpandTestReferenceWorkspace.UML2_HB20_EXT_FILE_PATH);
		assertNotNull(extFile);
		assertTrue(extFile.exists());

		// Get the first package of the UML2 file
		Resource resource = EcorePlatformUtil.getResource(umlModelFile);
		assertNotNull(resource);
		assertFalse(resource.getContents().isEmpty());
		EObject model = resource.getContents().get(0);
		assertNotNull(model);
		assertTrue(model instanceof Model);

		PackageableElement fistPackage = ((Model) model).getPackagedElements().get(0);
		assertNotNull(fistPackage);

		// Xtend execution
		XtendEvaluationRequest xtendEvaluationRequest = new XtendEvaluationRequest(XtendXpandTestReferenceWorkspace.XTEND_UML2_HB20_EXTENSION_NAME,
				model);
		List<MetaModel> metaModels = new ArrayList<MetaModel>(2);
		metaModels.add(new UML2MetaModel());
		metaModels.add(new SphinxManagedEmfMetaModel(umlModelFile.getProject()));
		XtendJob xtendJob = new XtendJob("Xtend Job", metaModels, xtendEvaluationRequest); //$NON-NLS-1$
		xtendJob.setWorkspaceResourceLoader(new BasicWorkspaceResourceLoader());
		IStatus xtendStatus = xtendJob.run(new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, xtendStatus);

		// Xtend result verification
		Map<Object, Collection<?>> result = xtendJob.getResultObjects();
		assertEquals(1, result.size());
		Object inputObject = result.keySet().iterator().next();
		assertEquals(model, inputObject);
		Collection<?> resultCollection = result.values().iterator().next();
		assertEquals(1, resultCollection.size());
		Object resultObject = resultCollection.iterator().next();
		assertTrue(resultObject instanceof Platform);
		assertEquals(fistPackage.getName(), ((Platform) resultObject).getName());
	}
}
