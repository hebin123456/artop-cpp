/**
 * <copyright>
 * 
 * Copyright (c) 2014 itemis and others.
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
package org.eclipse.sphinx.emf.resource;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.resource.Resource;

public interface IResourceProblemMarkerFactory {

	/**
	 * Creates a problem marker for the given resource from the given diagnostic.
	 * 
	 * @param resource
	 *            The {@link IResource resource} to create problem markers for.
	 * @param diagnostic
	 *            The issue in the document.
	 * @param severity
	 *            Severity marker attribute. A number from the set of error, warning and info severities defined by the
	 *            platform.
	 * @param problemHandlingOptions
	 *            The map of options that are used to control the handling of problems encountered while the resource
	 *            has been loaded or saved.
	 * @see #SEVERITY_ERROR
	 * @see #SEVERITY_WARNING
	 * @see #SEVERITY_INFO
	 */
	void createProblemMarker(IResource resource, Resource.Diagnostic diagnostic, int severity, Map<Object, Object> problemHandlingOptions);

	/**
	 * Creates a problem marker for the given resource from the given exception.
	 * 
	 * @param resource
	 *            The {@link IResource resource} to create problem markers for.
	 * @param exception
	 *            The issue in the document.
	 * @param severity
	 *            Severity marker attribute. A number from the set of error, warning and info severities defined by the
	 *            platform.
	 * @see #SEVERITY_ERROR
	 * @see #SEVERITY_WARNING
	 * @see #SEVERITY_INFO
	 */
	void createProblemMarker(IResource resource, Exception exception, int severity);

	/**
	 * Deletes markers on this resource. It's up to the developer to specify the type for which markers are to be
	 * removed.
	 * 
	 * @param resource
	 *            The resource for which markers will be deleted.
	 * @see IResource#deleteMarkers(String, boolean, int)
	 */
	void deleteMarkers(IResource resource);

}
