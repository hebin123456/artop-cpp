/**
 * <copyright>
 *
 * Copyright (c) 2008-2013 itemis, See4sys and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     See4sys - Initial API and implementation
 *     itemis - Added messages for creating new model projects and files
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages"; //$NON-NLS-1$

	/*
	 * Other messages
	 */
	public static String error_failedToSaveModelsDuringWorkbenchClosing;

	// New Linked Folder
	public static String wizardNewLinkedFolder_title;
	public static String wizardNewLinkedFolderCreationPage_title;
	public static String wizardNewLinkedFolderCreationPage_description;
	public static String wizardNewLinkedFolderCreationPage_errorTitle;
	public static String wizardNewLinkedFolderCreationPage_internalErrorTitle;
	public static String wizardNewLinkedFolderCreationPage_targetFolderLabel;
	public static String wizardNewLinkedFolderCreationPage_internalErrorMessage;

	public static String wizardNewLinkedFolderCreationPage_noParentSelected;
	public static String wizardNewLinkedFolderCreationPage_selectedParentProjectNotOpen;

	// New Linked File
	public static String wizardNewLinkedFile_title;
	public static String wizardNewLinkedFileCreationPage_title;
	public static String wizardNewLinkedFileCreationPage_description;
	public static String wizardNewLinkedFileCreationPage_errorTitle;
	public static String wizardNewLinkedFileCreationPage_internalErrorTitle;
	public static String wizardNewLinkedFileCreationPage_targetFileLabel;
	public static String wizardNewLinkedFileCreationPage_internalErrorMessage;

	public static String wizardNewLinkedFileCreationPage_noParentSelected;
	public static String wizardNewLinkedFileCreationPage_selectedParentProjectNotOpen;

	// New Model Project
	public static String wizard_newModelProject_title;

	public static String page_newModelProjectCreation_title;
	public static String page_newModelProjectCreation_description;

	public static String group_metaModelVersion_label;
	public static String button_alternateMetaModelVersion_label;
	public static String button_workspaceDefaultMetaModelVersion_labelPrefix;
	public static String button_workspaceDefaultMetaModelVersion_label;
	public static String combo_metaModelVersion_item;
	public static String link_configureWorkspaceSettings_label;
	public static String default_metaModelVersionLabel;

	public static String page_newProjectReference_title;
	public static String page_newProjectReference_description;

	public static String job_createNewModelProject_name;

	// New Model File
	public static String wizard_newModelFile_title;

	public static String page_newInitialModelCreation_title;
	public static String page_newInitialModelCreation_defaultTitle;
	public static String page_newInitialModelCreation_description;

	public static String combo_metaModelDescriptor_label;
	public static String combo_ePackage_label;
	public static String combo_eClassifier_label;
	public static String button_browse_label;
	public static String dialog_metaModelDescriptorSelection_title;
	public static String dialog_ePackageSelection_title;
	public static String dialog_eClassifierSelection_title;
	public static String dialog_selection_message;
	public static String error_noMetaModelDescriptorSelected;
	public static String error_noEPackageSelected;
	public static String error_noEClassifierSelected;

	public static String page_newModelFileCreation_title;
	public static String page_newModelFileCreation_description;

	public static String error_requiredProjectNature;
	public static String error_matchingMetaModelVersion;
	public static String error_fileExtension;
	public static String default_modelFileBaseName;
	public static String default_requiredProjectType;

	public static String job_creatingNewModelFile_name;

	public static String default_metamodelName;
	public static String default_metamodelName_cap;

	public static String contentProviderInterrupted;

	public static String action_toggleReferencesMode_referenced_label;
	public static String action_toggleReferencesMode_referenced_toolTip;
	public static String action_toggleReferencesMode_referenced_description;

	public static String action_toggleReferencesMode_referencing_label;
	public static String action_toggleReferencesMode_referencing_toolTip;
	public static String action_toggleReferencesMode_referencing_description;

	public static String action_basicOpenReferencesAction_label;

	public static String error_UnsupportedMode;
	public static String label_ReferencesHierarchyEmpty;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
