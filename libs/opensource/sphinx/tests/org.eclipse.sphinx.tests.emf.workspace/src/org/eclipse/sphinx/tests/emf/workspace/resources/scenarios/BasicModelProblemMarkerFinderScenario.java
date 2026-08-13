/**
 * <copyright>
 *
 * Copyright (c) 2015 itemis and others.
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
package org.eclipse.sphinx.tests.emf.workspace.resources.scenarios;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sphinx.tests.emf.workspace.resources.mocks.FileMockFactory;

@SuppressWarnings("nls")
public class BasicModelProblemMarkerFinderScenario {

	private static class SimpleEObjectImpl extends EObjectImpl {
	}

	private static class URIAdapter extends AdapterImpl {
		private URI uri;

		public URIAdapter(URI uri) {
			this.uri = uri;
		}

		public URI getURI() {
			return uri;
		}

		@Override
		public boolean isAdapterForType(Object type) {
			return URIAdapter.class == type;
		}
	}

	public static EObject createTestEObject(String uriString) {
		EObject eObject = new SimpleEObjectImpl();
		eObject.eAdapters().add(new URIAdapter(URI.createURI(uriString)));
		return eObject;
	}

	private Map<EObject, IFile> eObjectToFileMap = new HashMap<EObject, IFile>();

	public URI getURI(EObject eObject) {
		URIAdapter uriAdapter = (URIAdapter) EcoreUtil.getExistingAdapter(eObject, URIAdapter.class);
		if (uriAdapter == null) {
			throw new IllegalStateException("No URIAdapter on eObject: " + eObject.toString());
		}
		URI uri = uriAdapter.getURI();
		if (uri == null) {
			throw new IllegalStateException("No URI for eObject: " + eObject.toString());
		}
		return uri;
	}

	public IFile getFile(EObject eObject) {
		IFile file = eObjectToFileMap.get(eObject);
		if (file == null) {
			throw new IllegalStateException("No file for eObject: " + eObject.toString());
		}
		return file;
	}

	public IFile addExpectedProblemMarkers(EObject eObject, int errorCount, int warningCount, int infoCount) throws CoreException {
		URI eObjectURI = getURI(eObject);
		return eObjectToFileMap.put(eObject, FileMockFactory.INSTANCE.createFileMock(eObjectURI, errorCount, warningCount, infoCount));
	}
}