/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
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
package org.eclipse.sphinx.testutils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl;

@SuppressWarnings("nls")
public abstract class AbstractTestCase extends TestCase {

	private TestFileAccessor testFileAccessor = null;

	private boolean ignoreLoadProblems;
	private boolean ignoreSaveProblems;

	@Override
	protected void setUp() throws Exception {

		// HACK: Enable workspace preference Window > Preferences > General > Always run in background so as to avoid
		// excessive creation of progress dialogs by
		// org.eclipse.sphinx.emf.workspace.ui.internal.ModelLoadingProgressIndicator#aboutToRun(IJobChangeEvent) during
		// testing.
		/*
		 * !! Important Note !! The ModelLoadingProgressIndicator is there for opening a dialog which shows the progress
		 * of model loading jobs unless this has been deactivated by enabling above named workspace preference. The
		 * problem is that org.eclipse.ui.progress.IProgressService#showInDialog(Shell, Job) used for that purpose
		 * attempts to recreate a new progress dialog each time being invoked. This causes the platform to run out of
		 * SWT handles when too many of such invocations come across within too short intervals (see
		 * org.eclipse.ui.internal.progress.ProgressMonitorFocusJobDialog#show(Job, Shell) and
		 * org.eclipse.jface.dialogs.ProgressMonitorDialog#aboutToRun() for details)
		 */
		IEclipsePreferences workbenchPrefs = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
		workbenchPrefs.put("RUN_IN_BACKGROUND", Boolean.TRUE.toString());

		ignoreLoadProblems = false;
		ignoreSaveProblems = false;
	}

	protected final TestFileAccessor getTestFileAccessor() {
		if (testFileAccessor == null) {
			testFileAccessor = new TestFileAccessor(getTestPlugin(), new File("working-dir"));
		}
		return testFileAccessor;
	}

	protected abstract Plugin getTestPlugin();

	protected boolean isIgnoreLoadProblems() {
		return ignoreLoadProblems;
	}

	public void setIgnoreLoadProblems(boolean ignoreLoadProblems) {
		this.ignoreLoadProblems = ignoreLoadProblems;
	}

	protected boolean isIgnoreSaveProblems() {
		return ignoreSaveProblems;
	}

	public void setIgnoreSaveProblems(boolean ignoreSaveProblems) {
		this.ignoreSaveProblems = ignoreSaveProblems;
	}

	protected EObject loadInputFile(String inputFileName, ResourceFactoryImpl resourceFactory, Map<?, ?> options) throws Exception {
		return loadFile(getTestFileAccessor().getInputFileURI(inputFileName), resourceFactory, options);
	}

	protected EObject loadWorkingFile(String workingFileName, ResourceFactoryImpl resourceFactory, Map<?, ?> options) throws Exception {
		return loadFile(getTestFileAccessor().getWorkingFileURI(workingFileName), resourceFactory, options);
	}

	// TODO Enable external resourceSet to be handed in
	private EObject loadFile(java.net.URI fileURI, ResourceFactoryImpl resourceFactory, Map<?, ?> options) throws Exception {
		URI emfURI = getTestFileAccessor().convertToEMFURI(fileURI);
		XMLResource resource = (XMLResource) resourceFactory.createResource(emfURI);
		resource.load(options);

		ResourceSet resourceSet = createDefaultResourceSet();
		resourceSet.getResources().add(resource);

		assertHasNoLoadProblems(resource);

		return resource.getContents().get(0);
	}

	protected void saveWorkingFile(String fileName, EObject modelRoot, ResourceFactoryImpl resourceFactory, Map<?, ?> options) throws Exception {
		saveFile(getTestFileAccessor().getWorkingFileURI(fileName), modelRoot, resourceFactory, options);
	}

	// TODO Enable external resourceSet to be handed in
	private void saveFile(java.net.URI fileURI, EObject modelRoot, ResourceFactoryImpl resourceFactory, Map<?, ?> options) throws Exception {
		URI emfURI = getTestFileAccessor().convertToEMFURI(fileURI);
		XMLResource resource = (XMLResource) resourceFactory.createResource(emfURI);
		resource.getContents().add(modelRoot);
		resource.save(options);

		assertHasNoSaveProblems(resource);
	}

	protected String loadInputFileAsString(String fileName) throws Exception {
		return loadFileAsString(getTestFileAccessor().openInputFileInputStream(fileName));
	}

	protected String loadWorkingFileAsString(String fileName) throws Exception {
		return loadFileAsString(getTestFileAccessor().openWorkingFileInputStream(fileName));
	}

	protected String loadFileAsString(InputStream inputStream) throws Exception {
		inputStream = new BufferedInputStream(inputStream);
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

	protected ScopingResourceSetImpl createDefaultResourceSet() {
		return new ScopingResourceSetImpl();
	}

	public void assertEquals(EObject eObject1, EObject eObject2) {
		EcoreEqualityAssert.assertEquals(eObject1, eObject2);
	}

	protected void assertHasNoLoadProblems(Resource resource) {
		assertNotNull(resource);
		if (!isIgnoreLoadProblems()) {
			assertTrue("Errors encountered during resource loading: " + formatDiagnosticMessages(resource.getErrors()),
					resource.getErrors().size() == 0);
			assertTrue("Warnings encountered during resource loading: " + formatDiagnosticMessages(resource.getWarnings()), resource.getWarnings()
					.size() == 0);
		}
	}

	protected void assertHasNoSaveProblems(Resource resource) {
		assertNotNull(resource);
		if (!isIgnoreSaveProblems()) {
			assertTrue("Errors encountered during resource saving: " + formatDiagnosticMessages(resource.getErrors()),
					resource.getErrors().size() == 0);
			assertTrue("Warnings encountered during resource saving: " + formatDiagnosticMessages(resource.getWarnings()), resource.getWarnings()
					.size() == 0);
		}
	}

	protected String formatDiagnosticMessages(EList<Diagnostic> diagnostics) {
		StringBuilder msg = new StringBuilder();
		for (Diagnostic diagnostic : diagnostics) {
			if (msg.length() > 0) {
				msg.append("; "); //$NON-NLS-1$
			}
			msg.append(diagnostic.getMessage());
		}
		return msg.toString();
	}
}
