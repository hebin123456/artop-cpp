/**
 * <copyright>
 *
 * Copyright (c) 2008-2016 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Removed obsolete messages and improved message names
 *     itemis - [485407] Enable eager post-load proxy resolution to support manifold URI fragments referring to the same object
 *     Siemens - [574930] Model load manager extension
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.workspace.internal.messages.Messages"; //$NON-NLS-1$

	/*
	 * Model loading messages
	 */
	public static String job_loadingModels;
	public static String task_loadingModelsInProject;
	public static String job_loadingModel;
	public static String task_loadingModelInProject;
	public static String job_loadingModelResources;
	public static String task_loadingModelFiles;
	public static String subtask_analyzingFile;
	public static String subtask_loadingFile;
	public static String subtask_initializingProxyResolutionForModelInRoot;
	public static String subtask_resolvingProxiesInResource;

	public static String task_unloadingModelsInProject;
	public static String task_unloadingModelInProject;
	public static String job_unloadingModelResources;
	public static String task_unloadingModelFiles;

	public static String job_reloadingModels;
	public static String task_reloadingModelsInProject;
	public static String job_reloadingModel;
	public static String task_reloadingModelInProject;
	public static String job_reloadingModelResources;
	public static String task_reloadingModelFiles;

	public static String job_unresolvingUnreachableCrossProjectReferences;
	public static String task_unresolvingUnreachableCrossProjectReferencesInProject;
	public static String operation_unresolvingUnreachableCrossProjectReferencesInModel;
	public static String subtask_unresolvingUnreachableCrossProjectReferencesInResource;

	public static String job_updatingResourceURIs;
	public static String task_updatingResourceURIs;
	public static String subtask_updatingResourceURI;

	/*
	 * Model project and file creation
	 */
	public static String subTask_addingProjectNatures;
	public static String subTask_creatingNewModelProject;

	/*
	 * Model splitting
	 */
	public static String operation_splitModel_label;
	public static String subTask_savingModels;

	/*
	 * Error messages
	 */
	public static String error_createEditingDomainFactory;

	public static String error_createMapping;

	public static String error_createModelLoadManager;

	public static String error_multipleMappingsConfigured;

	public static String error_notFound_editingDomainFactory;

	public static String error_unexpectedSourceType;

	public static String error_unsupportedLoadOperation;

	public static String warning_multipleOverridesForSameURIChangeDetectorDelegate;

	public static String warning_multipleURIChangeDetectorDelegatesContributedForSameResourceType;

	public static String warning_multipleOverridesForSameURIChangeListener;

	/*
	 * Warning messages
	 */
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
