/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys and others.
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
package org.eclipse.sphinx.platform;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.sphinx.platform.util.ExtendedPlatform;

/**
 * @since 0.7.0
 */
public interface IExtendedPlatformConstants {

	// ----- Jobs families -----

	/**
	 * Identifier of job family for long running jobs. All jobs which belong to this family will be automatically
	 * canceled upon workbench shutdown.
	 *
	 * @see IJobManager#join(Object, IProgressMonitor)
	 */
	public static final Object FAMILY_LONG_RUNNING = "LONG_RUNNING"; //$NON-NLS-1$

	/**
	 * Identifier of job family for model loading jobs.
	 *
	 * @see IJobManager#join(Object, IProgressMonitor)
	 */
	public static final Object FAMILY_MODEL_LOADING = "MODEL_LOADING"; //$NON-NLS-1$

	/**
	 * Identifier of job family for model saving jobs.
	 *
	 * @see IJobManager#join(Object, IProgressMonitor)
	 */
	public static final Object FAMILY_MODEL_SAVING = "MODEL_SAVING"; //$NON-NLS-1$

	/**
	 * Identifier of job family for automatic validation jobs.
	 *
	 * @see IJobManager#join(Object, IProgressMonitor)
	 */
	public static final Object FAMILY_AUTOMATIC_VALIDATION = "AUTOMATIC_VALIDATION"; //$NON-NLS-1$

	/**
	 * Identifier of job family for label decoration jobs.
	 *
	 * @see IJobManager#join(Object, IProgressMonitor)
	 */
	public static final Object FAMILY_LABEL_DECORATION = "LABEL_DECORATION"; //$NON-NLS-1$

	// ----- Session property keys -----

	/**
	 * The key for retrieving the content type identifier of an {@linkplain IFile file} being cached as session property
	 * or persistent property.
	 */
	public static final String RESOURCE_PROPERTY_CONTENT_TYPE_ID = "org.eclipse.sphinx.platform.resourceProperties.contentTypeId"; //$NON-NLS-1$

	// ---- System property keys ----

	/**
	 * The key for retrieving the system property indicating the feature version of the running instance of the Eclipse
	 * platform (e.g., 3.4 for Eclipse 3.4, 3.5 for Eclipse 3.5)
	 */
	public static final String SYSTEM_PROPERTY_PLATFORM_FEATURE_VERSION = "org.eclipse.platform.featureVersion"; //$NON-NLS-1$

	/**
	 * A constant used to indicate that a {@link org.eclipse.emf.ecore.resource.Resource resource} or {@link IFile file}
	 * is non-model XML document.
	 *
	 * @see ExtendedPlatform#getContentTypeId(IFile)
	 */
	public static final String CONTENT_TYPE_ID_NON_MODEL_XML_FILE = "org.eclipse.sphinx.platform.nonModelXMLFile"; //$NON-NLS-1$ ;
}
