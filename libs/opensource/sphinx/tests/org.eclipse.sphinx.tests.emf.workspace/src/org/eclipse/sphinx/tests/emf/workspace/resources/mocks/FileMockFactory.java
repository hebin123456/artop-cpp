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
package org.eclipse.sphinx.tests.emf.workspace.resources.mocks;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EValidator;

public class FileMockFactory {

	public static final FileMockFactory INSTANCE = new FileMockFactory();

	private FileMockFactory() {
		super();
	}

	public IFile createFileMock(URI eObjectURI, int errorCount, int warningCount, int infoCount) throws CoreException {
		IFile file = createNiceMock(IFile.class);
		expect(file.exists()).andReturn(true).anyTimes();
		expect(file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO))
				.andReturn(createProblemMarkerMocks(eObjectURI, errorCount, warningCount, infoCount)).anyTimes();
		replay(file);
		return file;
	}

	protected IMarker[] createProblemMarkerMocks(URI eObjectURI, int errorCount, int warningCount, int infoCount) throws CoreException {
		List<IMarker> problemMarkers = new ArrayList<IMarker>(errorCount + warningCount + infoCount);
		addProblemMarkerMocks(problemMarkers, IMarker.SEVERITY_ERROR, eObjectURI, errorCount);
		addProblemMarkerMocks(problemMarkers, IMarker.SEVERITY_WARNING, eObjectURI, warningCount);
		addProblemMarkerMocks(problemMarkers, IMarker.SEVERITY_INFO, eObjectURI, infoCount);
		return problemMarkers.toArray(new IMarker[problemMarkers.size()]);
	}

	protected void addProblemMarkerMocks(List<IMarker> problemMarkers, int severity, URI eObjectURI, int count) throws CoreException {
		for (int i = 0; i < count; i++) {
			IMarker marker = createNiceMock(IMarker.class);
			expect(marker.exists()).andReturn(true).anyTimes();
			expect(marker.getType()).andReturn(IMarker.PROBLEM).anyTimes();
			expect(marker.getAttribute(IMarker.SEVERITY, -1)).andReturn(severity).anyTimes();
			expect(marker.getAttribute(EValidator.URI_ATTRIBUTE)).andReturn(eObjectURI.toString()).anyTimes();
			replay(marker);

			problemMarkers.add(marker);
		}
	}
}
