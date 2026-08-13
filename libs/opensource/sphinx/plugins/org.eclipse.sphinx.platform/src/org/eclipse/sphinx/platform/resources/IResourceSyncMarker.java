/**
 * <copyright>
 * 
 * Copyright (c) 2012 BMW Car IT, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     BMW Car IT - Initial API and implementation
 *     itemis - [416830] Harmful dependency on running OSGi environment
 * 
 * </copyright>
 */
package org.eclipse.sphinx.platform.resources;

public interface IResourceSyncMarker {

	/**
	 * Resource out-of-sync problem marker type.
	 * <p>
	 * !! Important Note !! Don't use Activator.getPlugin().getSymbolicName() instead of hard-coded plug-in name because
	 * this would prevent this class from being loaded in Java standalone applications.
	 * </p>
	 */
	public static final String RESOURCE_SYNC_PROBLEM = "org.eclipse.sphinx.platform.resourcesyncproblemmarker"; //$NON-NLS-1$

}
