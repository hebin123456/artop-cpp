/**
 * <copyright>
 * 
 * Copyright (c) 2011 See4sys, itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     See4sys - Initial API and implementation
 *     itemis - [358082] Precedence of Xtend MetaModels gets lost in Xtend/Xpand runtime enhancements implemented in Sphinx
 *     itemis - [358591] ResultObjectHandler and ResultMessageHandler used by M2xConfigurationWizards are difficult to customize and should be usable in BasicM2xActions too
 * 
 * </copyright>
 */
package org.eclipse.sphinx.xtendxpand.ui.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.sphinx.xtendxpand.ui.internal.messages.messages"; //$NON-NLS-1$

	public static String job_generatingCode;
	public static String job_transformingModel;
	public static String job_convertingToXtendXpandEnabledPluginProject;

	public static String label_template;
	public static String label_extension;
	public static String label_output;
	public static String label_templateFile;
	public static String label_extensionFile;
	public static String label_templateSelection;
	public static String label_extensionSelection;
	public static String label_definition;
	public static String label_useDefaultPath;
	public static String label_path;
	public static String label_browse;
	public static String label_name;
	public static String label_configPageName;
	public static String label_xtendPageName;
	public static String label_definitionName;
	public static String label_extensionName;
	public static String label_functionFieldName;
	public static String label_useCheckModelButton;
	public static String label_checkModelBlock;
	public static String label_configureProjectSpecificSettings;
	public static String label_outletsGroupName;
	public static String label_default;
	public static String label_location;
	public static String label_workspaceBrowse;
	public static String label_fileSystemBrowse;
	public static String label_variablesBrowse;
	public static String label_restoreDefaultButtons;
	public static String label_useAsProtectedRegion;
	public static String label_prExcludes;
	public static String label_prDefaultExcludes;
	public static String label_protectedRegionGroupName;
	public static String label_AddButton;
	public static String label_EditButton;
	public static String label_RemoveButton;
	public static String label_OutletsGroup_TableColumn_Name;
	public static String label_OutletsGroup_TableColumn_Path;
	public static String label_Protected_Region;

	public static String msg_chooseTemplate;
	public static String msg_chooseExtension;
	public static String msg_chooseTemplateError;
	public static String msg_chooseExtensionError;
	public static String msg_containerSelection;
	public static String msg_variableSelectionWarning;
	public static String msg_outletNameEmptyValidationError;
	public static String msg_outletNameExistValidationError;
	public static String msg_outletLocationEmptyValidationError;
	public static String msg_codeGen;
	public static String msg_modelTransformation;
	public static String msg_M2x_operation_successful;
	public static String msg_M2x_operation_canceled;
	public static String msg_M2x_Check_operation_failed;

	public static String title_launchGen;
	public static String title_codeGen;
	public static String title_editOutletDialog;
	public static String title_newOutletDialog;
	public static String title_containerSelection;
	public static String title_variableSelection;
	public static String title_modelTransformation;
	public static String title_launchModelTransformation;
	public static String title_outletConfigurationDialog;
	public static String title_checkConfigurationPage;

	public static String desc_config;
	public static String desc_modelTransformation;
	public static String desc_configOutlets;
	public static String desc_checkConfigurationPage;

	public static String task_ConvertingToXtendXpandEnabledPluginProject;
	public static String task_ConvertingToJavaProject;
	public static String task_ConvertingToPluginProject;
	public static String task_ConvertingToXtendXpandProject;

	public static String tooltip_prExcludesField;
	public static String tooltip_prDefaultExcludes;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
