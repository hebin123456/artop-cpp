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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

/**
 * <code>IMarkerResourceAdapter</code> is an adapter interface that supplies the resource to query for markers to
 * display in the marker view or any of its subclasses. Implementors of this interface are typically registered with an
 * IAdapterFactory for lookup via the getAdapter() mechanism.
 */
public interface IMarkerResourceAdapter {

	/**
	 * Returns the resource to query for the markers to display for the given adaptable.
	 * 
	 * @param adaptable
	 *            the adaptable being queried.
	 * @return the resource or <code>null</code> if there is no adapted resource for this object.
	 */
	public IResource getAffectedResource(IAdaptable adaptable);

}
