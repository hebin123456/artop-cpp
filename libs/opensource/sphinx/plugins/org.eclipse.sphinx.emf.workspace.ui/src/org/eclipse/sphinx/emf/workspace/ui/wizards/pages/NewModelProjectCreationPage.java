/**
 * <copyright>
 *
 * Copyright (c) 2013-2016 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [403693] NewModelProjectCreationPage#createMetaModelVersionGroup() should not return the group object being created
 *     itemis - [403728] NewModelProjectCreationPage and NewModelFileCreationPage should provided hooks for creating additional controls
 *     itemis - [405059] Enable BasicMetaModelVersionGroup to open appropriate model version preference page
 *     itemis - [405075] Improve type safety of NewModelProjectCreationPage and BasicMetaModelVersionGroup wrt base metamodel descriptor and metamodel version preference
 *     itemis - [406062] Removal of the required project nature parameter in NewModelFileCreationPage constructor and CreateNewModelProjectJob constructor
 *     itemis - [406194] Enable title and descriptions of model project and file creation wizards to be calculated automatically
 *     itemis - [503063] Provide launching support for Sphinx Workflows
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.wizards.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.ui.wizards.groups.BasicMetaModelVersionGroup;
import org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Basic main page for a wizard that creates a {@linkplain IProject project} containing model files.
 * <p>
 * This page may be used by clients as it is; it may also be subclassed to suit. Subclasses may override these methods
 * if required:
 * <p>
 * <code>createControl</code> method to create the specific controls for this project creation page.
 * <code>createMetaModelVersionGroup</code> - method to create the specific metamodel version group for this project
 * creation page
 */
public class NewModelProjectCreationPage<T extends IMetaModelDescriptor> extends WizardNewProjectCreationPage {

	protected IStructuredSelection selection;
	protected boolean createWorkingSetGroup;

	protected T baseMetaModelDescriptor;
	protected IProjectWorkspacePreference<T> metaModelVersionPreference;
	protected String metaModelVersionPreferencePageId;

	protected BasicMetaModelVersionGroup<T> metaModelVersionGroup;

	/**
	 * Creates a new instance of the new model project creation wizard page for the specified base metamodel.
	 *
	 * @param pageName
	 *            the name of this page
	 * @param baseMetaModelDescriptor
	 *            the base {@linkplain IMetaModelDescriptor metamodel} of the model project to be created
	 * @param metaModelVersionPreference
	 *            the meta-model version that the model project will be used for; when set to <code>null</code>
	 *            metamodel version selection controls will omitted
	 * @param metaModelVersionPreferencePageId
	 *            the id of the metamodel version preference page
	 */
	public NewModelProjectCreationPage(String pageName, T baseMetaModelDescriptor, IProjectWorkspacePreference<T> metaModelVersionPreference,
			String metaModelVersionPreferencePageId) {
		this(pageName, null, false, baseMetaModelDescriptor, metaModelVersionPreference, metaModelVersionPreferencePageId);
	}

	/**
	 * Creates a new instance of the new model project creation wizard page for the specified base metamodel.
	 *
	 * @param pageName
	 *            the name of this page
	 * @param selection
	 *            the current resource selection
	 * @param createWorkingSetGroup
	 *            <code>true</code> if a group for choosing a working set for the new model project should be added to
	 *            the page, false otherwise
	 * @param baseMetaModelDescriptor
	 *            the base {@linkplain IMetaModelDescriptor meta-model} of the model project to be created
	 * @param metaModelVersionPreference
	 *            the meta-model version that the model project will be used for; when set to <code>null</code>
	 *            metamodel version selection controls will omitted
	 * @param metaModelVersionPreferencePageId
	 *            the id of the metamodel version preference page
	 */
	public NewModelProjectCreationPage(String pageName, IStructuredSelection selection, boolean createWorkingSetGroup, T baseMetaModelDescriptor,
			IProjectWorkspacePreference<T> metaModelVersionPreference, String metaModelVersionPreferencePageId) {
		super(pageName);
		Assert.isLegal(baseMetaModelDescriptor != MetaModelDescriptorRegistry.ANY_MM);
		Assert.isLegal(baseMetaModelDescriptor != MetaModelDescriptorRegistry.NO_MM);

		this.selection = selection;
		this.createWorkingSetGroup = createWorkingSetGroup;
		this.baseMetaModelDescriptor = baseMetaModelDescriptor;
		this.metaModelVersionPreference = metaModelVersionPreference;
		this.metaModelVersionPreferencePageId = metaModelVersionPreferencePageId;

		setTitle(NLS.bind(Messages.page_newModelProjectCreation_title,
				baseMetaModelDescriptor != null ? baseMetaModelDescriptor.getName() : Messages.default_metamodelName_cap));
		setDescription(NLS.bind(Messages.page_newModelProjectCreation_description,
				baseMetaModelDescriptor != null ? baseMetaModelDescriptor.getName() : Messages.default_metamodelName));
	}

	/*
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		createAdditionalControls((Composite) getControl());
		Dialog.applyDialogFont(getControl());
	}

	/**
	 * Creates controls for specific project creation options to be placed behind those for project name and location
	 * (which are created by {@link WizardNewProjectCreationPage#createControl(Composite)}).
	 * <p>
	 * This implementation creates a {@link BasicMetaModelVersionGroup group} for selecting the metamodel version to be
	 * assigned to the new project.
	 * </p>
	 * This method may be overridden by subclasses to provide enhanced or custom implementations.
	 *
	 * @param parent
	 *            the parent composite
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#createControl(Composite)
	 */
	protected void createAdditionalControls(Composite parent) {
		if (createWorkingSetGroup) {
			createWorkingSetGroup((Composite) getControl(), selection, new String[] { "org.eclipse.ui.resourceWorkingSetPage" }); //$NON-NLS-1$
		}
		createMetaModelVersionGroup(parent);
	}

	/**
	 * Creates a {@link BasicMetaModelVersionGroup metamodel version group} enabling the metamodel version of the model
	 * {@link IProject project} under creation to be chosen.
	 *
	 * @param parent
	 *            the parent {@linkplain Composite composite}
	 */
	protected void createMetaModelVersionGroup(Composite parent) {
		if (baseMetaModelDescriptor != null && metaModelVersionPreference != null) {
			metaModelVersionGroup = new BasicMetaModelVersionGroup<T>("BasicMetaModelVersionGroup", baseMetaModelDescriptor, //$NON-NLS-1$
					metaModelVersionPreference, metaModelVersionPreferencePageId);
			metaModelVersionGroup.createContent(parent, 3);
		}
	}

	public T getMetaModelVersionDescriptor() {
		if (metaModelVersionPreference != null) {
			return metaModelVersionGroup.getMetaModelVersionDescriptor();
		}
		return null;
	}
}
