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
package org.eclipse.sphinx.platform.resources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * An abstract {@link IProblemMarkerFinder problem marker finder} implementation that implements
 * {@link #getSeverity(Object)} and caches the resulting severity to increase the severity computation performance when
 * the severity for the same object is requested to be computed multiple times.
 */
public abstract class AbstractProblemMarkerFinder implements IProblemMarkerFinder {

	protected Map<Object, Integer> severityCache = new HashMap<Object, Integer>();

	protected boolean canHaveProblemMarkers(Object object) {
		return true;
	}

	/*
	 * @see org.eclipse.sphinx.emf.validation.ui.decorators.IProblemMarkerFinder#getSeverity(java.lang.Object)
	 */
	@Override
	public int getSeverity(Object object) throws CoreException {
		if (!canHaveProblemMarkers(object)) {
			return -1;
		}

		Integer overallSeverity = severityCache.get(object);
		if (overallSeverity != null) {
			return overallSeverity;
		}

		overallSeverity = -1;
		for (IMarker problemMarker : getProblemMarkers(object)) {
			int severity = problemMarker.getAttribute(IMarker.SEVERITY, -1);
			if (severity > overallSeverity) {
				overallSeverity = severity;
			}
			if (overallSeverity == IMarker.SEVERITY_ERROR) {
				break;
			}
		}

		severityCache.put(object, overallSeverity);
		return overallSeverity;
	}

	/*
	 * @see org.eclipse.sphinx.emf.validation.ui.decorators.IProblemMarkerFinder#reset()
	 */
	@Override
	public void reset() {
		severityCache.clear();
	}
}