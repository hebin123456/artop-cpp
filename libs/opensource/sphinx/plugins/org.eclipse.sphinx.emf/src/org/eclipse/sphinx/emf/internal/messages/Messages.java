/**
 * <copyright>
 *
 * Copyright (c) 2008-2019 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - [409458] Enhance ScopingResourceSetImpl#getEObjectInScope() to enable cross-document references between model files with different metamodels
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.internal.messages.Messages"; //$NON-NLS-1$

	public static String job_addingNewModelResources;
	public static String job_addingNewModelResource;
	public static String task_addingNewModelResources;
	public static String task_addingNewModelResource;
	public static String operation_addingNewModelResources;
	public static String operation_addingNewModelResource;

	public static String job_savingNewModelResources;
	public static String job_savingNewModelResource;
	public static String task_savingNewModelResources;
	public static String task_savingNewModelResource;
	public static String operation_savingNewModelResources;
	public static String operation_savingNewModelResource;

	public static String job_savingModels;
	public static String job_savingModel;
	public static String task_savingModelResources;
	public static String task_savingModelResource;
	public static String operation_savingModelResources;
	public static String operation_savingModelResource;

	public static String subtask_addingResource;
	public static String subtask_savingResource;

	public static String task_unloadingModelFiles;
	public static String subtask_unloadingModelFile;

	public static String task_unloadingModelResources;
	public static String subtask_unloadingModelResource;

	public static String job_addingModelDescriptors;
	public static String job_movingModelDescriptors;
	public static String job_updatingModelDescriptors;
	public static String job_updatingReferencedModelDescriptors;
	public static String job_removingModelDescriptors;
	public static String job_clearingOldMetaModelDescriptors;
	public static String job_initializingModelDescriptorRegistry;
	public static String job_addingProblemMarkers;
	public static String job_removingProblemMarkers;
	public static String task_analyzingProjects;
	public static String subtask_analyzingFile;
	public static String task_validatingResourceScopes;
	public static String task_cleaningResourceScopeMarkers;

	public static String propertyDescriptionPostfix_mustBeADataTypeEgDefaultValue;
	public static String propertyDescriptionPostfix_mustBeADataType;

	public static String problem_transactionFailed;

	public static String error_cannotCompareObjects;
	public static String error_mmDescriptorIdentifierNotEqual;

	public static String warning_mmDescriptorHasNoIdentifier;
	public static String warning_mmDescriptorIdentifierNotUnique;
	public static String warning_mmNsURIPatternNotUnique;

	public static String warning_targetMetaModelDescriptorProviderWithoutId;
	public static String warning_targetMetaModelDescriptorProviderIdNotUnique;
	public static String warning_multipleTargetMetaModelDescriptorProvidersOverride;
	public static String warning_fileExtensionForTargetMetaModelDescriptorProviderMustNotBeNull;
	public static String warning_fileExtensionForTargetMetaModelDescriptorProviderNotUnique;
	public static String warning_contentTypeIdForTargetMetaModelDescriptorProviderMustNotBeNull;
	public static String warning_contentTypeIdForTargetMetaModelDescriptorProviderNotUnique;
	public static String warning_serviceIdNotUnique;
	public static String warning_noServiceToOverride;

	public static String warning_multipleOverridesForSameResourceScopeProvider;
	public static String warning_multipleResourceScopeProvidersContributedForSameMetaModelDescriptor;

	public static String error_invalidEditingDomainFactoryListenerObject;

	public static String label_unknownProxyURI;

	public static String attribute_line;

	public static String msg_xmlWellformednessProblemFormatString;
	public static String msg_xmlValidityProblemFormatString;
	public static String msg_waitingForModelsBeingLoaded;

	public static String error_problemOccurredWhenLoadingResource;
	public static String error_problemOccurredWhenResolvingReferencesOfObject;
	public static String error_problemOccurredWhenResolvingProxyURI;
	public static String error_problemOccurredWhenSavingResource;

	public static String error_metaModelServiceAlreadyExists;
	public static String error_unknownMetaModel;
	public static String error_invalidMetaModelServiceClass;
	public static String error_missingMetaModelServiceType;
	public static String error_missingMetaModelDescriptor;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
