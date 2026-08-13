/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     See4sys - added support for problem markers on model objects (rather than 
 *               only on workspace resources). Unfortunately, there was no other 
 *               choice than copying the whole code from 
 *               org.eclipse.ui.views.markers.internal for that purpose because 
 *               many of the relevant classes, methods, and fields are private or
 *               package private.
 *******************************************************************************/
package org.eclipse.sphinx.emf.validation.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

/**
 * The DefaultMarkerResourceAdapter is the default implementation of the IMarkerResourceAdapter used by the MarkerView
 * for resource adaption.
 */
public class DefaultMarkerResourceAdapter implements ITaskListResourceAdapter {

	private static ITaskListResourceAdapter singleton;

	/**
	 * Constructor for DefaultMarkerResourceAdapter.
	 */
	DefaultMarkerResourceAdapter() {
		super();
	}

	/**
	 * Return the default instance used for MarkerView adapting.
	 */
	public static ITaskListResourceAdapter getDefault() {
		if (singleton == null) {
			singleton = new DefaultMarkerResourceAdapter();
		}
		return singleton;
	}

	/**
	 * @see IMarkerResourceAdapter#getAffectedResource(IAdaptable)
	 */
	@Override
	public IResource getAffectedResource(IAdaptable adaptable) {
		IResource resource = (IResource) adaptable.getAdapter(IResource.class);

		if (resource == null) {
			return (IFile) adaptable.getAdapter(IFile.class);
		} else {
			return resource;
		}
	}
}
