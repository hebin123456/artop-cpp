/**
 * <copyright>
 *
 * Copyright (c) 2008-2021 See4sys, Siemens and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     Siemens - [577073] URI change detector delegate extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.referentialintegrity;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Interface that determines URIChangeDetectorDelegate API.see also {@link URIChangeDetectorDelegateRegistry}
 */
public interface IURIChangeDetectorDelegate {

	/**
	 * Detects all the model changed {@link URI}s from the given resource set events {@link Notification}. This method
	 * deprecates the {@link IURIChangeDetectorDelegate#detectChangedURIs(Notification)}.
	 *
	 * @param notification
	 *            The notification from the resource set event to use for computing the changed {@link URI}s
	 * @return A map containing entries of {@link Resource} to new {@link EObject new EObject} and {@link URI old URI}
	 *         pairs. Null return value is interpreted by the caller as this method is not implemented. Empty map return
	 *         value shall be used to signal that there were no changed {@link URI}s.
	 */
	public Map<Resource, List<URIChangeNotification>> detectChangedURIs(List<Notification> notifications);

	/**
	 * Detects all the model changed {@link URI}s from a given resource set event {@link Notification}.
	 *
	 * @deprecated Use {@link IURIChangeDetectorDelegate#detectChangedURIs(List)} instead. However, if that method
	 *             returns null value, this method is called to detect {@link URI} changes. This behavior is kept until
	 *             this method is completely removed from the API.
	 * @param notification
	 *            The notification from the resource set event to use for computing the changed {@link URI}s
	 * @return A list containing new {@link EObject new EObject} , {@link URI old URI} pairs.
	 */
	@Deprecated
	public List<URIChangeNotification> detectChangedURIs(Notification notification);

	/**
	 * Detects all the model changed {@link URI}s from a given {@link IFile file} moved event .
	 *
	 * @param oldFile
	 *            The {@link IFile file} before modification.
	 * @param newFile
	 *            The {@link IFile file} after modification.
	 * @return A map containing {@link EObject new EObject} , {@link URI old URI} pairs.
	 */
	public List<URIChangeNotification> detectChangedURIs(IFile oldFile, IFile newFile);
}
