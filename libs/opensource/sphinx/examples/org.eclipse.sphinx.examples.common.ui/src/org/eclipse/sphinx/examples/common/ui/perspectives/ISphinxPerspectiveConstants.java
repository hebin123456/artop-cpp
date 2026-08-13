/**
 * <copyright>
 * 
 * Copyright (c) 2010 See4sys and others.
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
package org.eclipse.sphinx.examples.common.ui.perspectives;

/**
 * Interface defining constants used for the definition of the Sphinx perspective.
 */
// TODO Remove all ID constants creating implicit dependencies on other examples plug-ins and let the example plug-ins
// contribute perpective extensions instead
public interface ISphinxPerspectiveConstants {

	String LEFT = "left"; //$NON-NLS-1$
	String TOP_LEFT = "topLeft"; //$NON-NLS-1$
	String BOTTOM_RIGHT = "bottomRight"; //$NON-NLS-1$

	/***************************
	 * --- Perspective IDs --- *
	 ***************************/

	/**
	 * The id for the Sphinx perspective.
	 */
	String ID_SPHINX_PERSPECTIVE = "org.eclipse.sphinx.examples.common.ui.perspectives.sphinx"; //$NON-NLS-1$

	/**
	 * The id for the Eclipse Resource perspective.
	 */
	String ID_RESOURCE_PERSPECTIVE = "org.eclipse.ui.resourcePerspective"; //$NON-NLS-1$

	/*********************
	 * --- Views IDs --- *
	 *********************/

	/**
	 * The view id for the Eclipse Problems view.
	 */
	String ID_PROBLEMS_VIEW = "org.eclipse.ui.views.ProblemView";//$NON-NLS-1$

	/**
	 * The view id for the Eclipse Error Log view.
	 */
	String ID_ERROR_LOG_VIEW = "org.eclipse.pde.runtime.LogView"; //$NON-NLS-1$

	/**
	 * The view id for the Eclipse Console view.
	 */
	String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$

	/**
	 * The view id for the Sphinx Model Explorer view.
	 */
	String ID_MODEL_EXPLORER = "org.eclipse.sphinx.examples.explorer.views.modelExplorer"; //$NON-NLS-1$

	/**
	 * The view id for the Sphinx Validation view.
	 */
	String ID_VALIDATION_VIEW = "org.eclipse.sphinx.examples.validation.ui.views.validation"; //$NON-NLS-1$

	/***********************
	 * --- Wizards IDs --- *
	 ***********************/

	/**
	 * The wizard id for the Sphinx new Hummingbird project wizard.
	 */
	String ID_HUMMINGBIRD_NEW_PROJECT = "org.eclipse.sphinx.examples.hummingbird.ide.ui.newWizards.hummingbirdProject"; //$NON-NLS-1$

	/**
	 * The wizard id for the Sphinx new Hummingbird 1.0 model wizard.
	 */
	String ID_HUMMINGBIRD10_NEW_MODEL = "org.eclipse.sphinx.examples.hummingbird10.editor.Hummingbird10ModelWizardID"; //$NON-NLS-1$

	/**
	 * The wizard id for the Sphinx new Hummingbird 2.0 type model wizard.
	 */
	String ID_HUMMINGBIRD20_NEW_TYPE_MODEL = "org.eclipse.sphinx.examples.hummingbird20.typemodel.editor.TypeModel20ModelWizardID"; //$NON-NLS-1$

	/**
	 * The wizard id for the Sphinx new Hummingbird 2.0 instance model wizard.
	 */
	String ID_HUMMINGBIRD20_NEW_INSTANCE_MODEL = "org.eclipse.sphinx.examples.hummingbird20.instancemodel.editor.InstanceModel20ModelWizardID"; //$NON-NLS-1$

	/**
	 * The wizard id for the Sphinx new Hummingbird file wizard.
	 */
	String ID_HUMMINGBIRD_NEW_FILE = "org.eclipse.sphinx.examples.common.ui.newWizards.hummingbirdFile"; //$NON-NLS-1$

	/**
	 * The wizard id for the Sphinx new linked file wizard.
	 */
	String ID_NEW_LINKED_FILE = "org.eclipse.sphinx.examples.common.ui.newWizards.linkedFile";//$NON-NLS-1$

	/**
	 * The wizard id for the Sphinx new linked folder wizard.
	 */
	String ID_NEW_LINKED_FOLDER = "org.eclipse.sphinx.examples.common.ui.newWizards.linkedFolder";//$NON-NLS-1$

	/**
	 * The wizard id for the Eclipse new file wizard.
	 */
	String ID_ECLIPSE_NEW_FILE = "org.eclipse.ui.wizards.new.file"; //$NON-NLS-1$

	/**
	 * The wizard id for the Eclipse new folder wizard.
	 */
	String ID_ECLIPSE_NEW_FOLDER = "org.eclipse.ui.wizards.new.folder"; //$NON-NLS-1$

	/**************************
	 * --- ActionSets IDs --- *
	 **************************/
	String ID_TEAM_ACTIONSET = "org.eclipse.team.ui.actionSet";//$NON-NLS-1$
	String ID_LAUNCH_ACTIONSET = "org.eclipse.debug.ui.launchActionSet";//$NON-NLS-1$

	/**************************
	 * --- Preference IDs --- *
	 **************************/
	String ID_HUMMINGBIRD_PREFERENCE_PAGE = "org.eclipse.sphinx.workspace.ui.preferences.hummingbird"; //$NON-NLS-1$
}
