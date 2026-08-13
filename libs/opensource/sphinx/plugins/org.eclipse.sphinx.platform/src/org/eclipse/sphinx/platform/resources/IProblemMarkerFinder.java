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

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * Provides services for finding {@link IMarker#PROBLEM problem marker}s and determining the combined severity of all
 * problem markers for a given object.
 */
public interface IProblemMarkerFinder {

	Collection<IMarker> getProblemMarkers(Object object) throws CoreException;

	int getSeverity(Object object) throws CoreException;

	void reset();
}