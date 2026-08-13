/**
 * <copyright>
 *
 * Copyright (c) 2013 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *     itemis - Initial API and implementation
 *     itemis - [406062] Removal of the required project nature parameter in NewModelFileCreationPage constructor and CreateNewModelProjectJob constructor
 *     itemis - [406194] Enable title and descriptions of model project and file creation wizards to be calculated automatically
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.jobs.CreateNewModelProjectJob;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.NewModelProjectCreationPage;
import org.eclipse.sphinx.platform.preferences.IProjectWorkspacePreference;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * Abstract wizard for creating a new model project in the workspace. Two pages are added for selecting the properties
 * of the model project and references to other projects. When being finished a
 * {@linkplain createCreateNewModelProjectJob new model project job} is run to create the model project in the
 * workspace.
 */
public abstract class AbstractNewModelProjectWizard<T extends IMetaModelDescriptor> extends BasicNewProjectResourceWizard {

	protected WizardNewProjectCreationPage mainPage;
	protected WizardNewProjectReferencePage referencePage;

	protected String metaModelName;
	protected boolean createWorkingSetGroup;
	protected T baseMetaModelDescriptor;
	protected IProjectWorkspacePreference<T> metaModelVersionPreference;
	protected String metaModelVersionPreferencePageId;

	/**
	 * Creates a wizard for creating a new model project in the workspace.
	 */
	public AbstractNewModelProjectWizard() {
		this(false, null, null, null);
	}

	/**
	 * Creates a wizard for creating a new model project in the workspace.
	 * 
	 * @param metaModelName
	 *            the name of the metamodel the new model project should be dedicated to
	 */
	public AbstractNewModelProjectWizard(String metaModelName) {
		this(false, null, null, null);
		this.metaModelName = metaModelName;
	}

	/**
	 * Creates a wizard for creating a new model project in the workspace with required metamodel version descriptor and
	 * metamodel version preference.
	 * 
	 * @param createWorkingSetGroup
	 *            <code>true</code> if a group for choosing a working set for the new model project should be added to
	 *            the wizard's main page, false otherwise
	 * @param baseMetaModelDescriptor
	 *            the base {@linkplain IMetaModelDescriptor meta-model} of the model project to be created; when set to
	 *            <code>null</code> no metamodel version will be configured
	 * @param metaModelVersionPreference
	 *            the metamodel version preference of the model project; when set to <code>null</code> no metamodel
	 *            version will be configured
	 * @param metaModelVersionPreferencePageId
	 *            the id of the metamodel version preference page
	 */
	public AbstractNewModelProjectWizard(boolean createWorkingSetGroup, T baseMetaModelDescriptor,
			IProjectWorkspacePreference<T> metaModelVersionPreference, String metaModelVersionPreferencePageId) {
		this.createWorkingSetGroup = createWorkingSetGroup;
		this.baseMetaModelDescriptor = baseMetaModelDescriptor;
		this.metaModelVersionPreference = metaModelVersionPreference;
		this.metaModelVersionPreferencePageId = metaModelVersionPreferencePageId;
		metaModelName = baseMetaModelDescriptor != null ? baseMetaModelDescriptor.getName() : null;
	}

	/*
	 * @see org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);

		setWindowTitle(NLS.bind(Messages.wizard_newModelProject_title, metaModelName != null ? metaModelName : Messages.default_metamodelName_cap));
	}

	/*
	 * @see org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard#addPages()
	 */
	@Override
	public void addPages() {
		mainPage = createMainPage();
		Assert.isNotNull(mainPage);
		addPage(mainPage);

		// Only add page if there are already projects in the workspace
		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = createReferencePage();
			addPage(referencePage);
		}
	}

	/**
	 * Creates the {@link NewModelProjectCreationPage main page} for the creation of the new model project. This method
	 * may be overridden by clients to create a specific main page as appropriate.
	 * 
	 * @return a main page for the creation of the new model project
	 */
	protected WizardNewProjectCreationPage createMainPage() {
		return new NewModelProjectCreationPage<T>("NewModelProjectCreationPage", getSelection(), createWorkingSetGroup, baseMetaModelDescriptor, //$NON-NLS-1$
				metaModelVersionPreference, metaModelVersionPreferencePageId);
	}

	/**
	 * Creates the reference page.
	 */
	protected WizardNewProjectReferencePage createReferencePage() {
		WizardNewProjectReferencePage referencePage = new WizardNewProjectReferencePage("WizardNewProjectReferencePage"); //$NON-NLS-1$
		referencePage.setTitle(Messages.page_newProjectReference_title);
		referencePage.setDescription(Messages.page_newProjectReference_description);
		return referencePage;
	}

	/*
	 * Creates a new project, update the perspective, and open in new perspective.
	 * @see org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		final IProject projectHandle = mainPage.getProjectHandle();
		URI location = !mainPage.useDefaults() ? mainPage.getLocationURI() : null;
		IProject[] referencedProjects = referencePage != null ? referencePage.getReferencedProjects() : null;

		// Create a new model project creation job
		String jobName = NLS.bind(Messages.job_createNewModelProject_name, metaModelName != null ? metaModelName : Messages.default_metamodelName);
		CreateNewModelProjectJob<T> job = createCreateNewModelProjectJob(jobName, projectHandle, location);
		job.setReferencedProjects(referencedProjects);
		job.setUIInfoAdaptable(WorkspaceUndoUtil.getUIInfoAdapter(getShell()));

		// Setup post creation actions
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult().getSeverity() == IStatus.OK) {
					Display display = ExtendedPlatformUI.getDisplay();
					if (display != null) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								updatePerspective();

								// Reveal and select the new model project in current view
								selectAndReveal(projectHandle);
							}
						});
					}
				}
			}
		});
		job.schedule();

		return true;
	}

	/**
	 * Creates a new instance of {@linkplain CreateNewModelProjectJob}. This method may be overridden by clients to
	 * create a specific model project creation job as appropriate.
	 * 
	 * @param jobName
	 *            the pre-calculated name of the job
	 * @param newProject
	 *            the {@linkplain IProject project} resource to be created
	 * @param location
	 *            the {@linkplain URI location} where the project will be created. If null the default location will be
	 *            used.
	 * @return a new instance of job that creates a new model project. This job is a unit of runnable work that can be
	 *         scheduled to be run with the job manager.
	 */
	protected CreateNewModelProjectJob<T> createCreateNewModelProjectJob(String jobName, IProject newProject, URI location) {
		@SuppressWarnings("unchecked")
		NewModelProjectCreationPage<T> newModelProjectCreationPage = (NewModelProjectCreationPage<T>) mainPage;

		return new CreateNewModelProjectJob<T>(jobName, newProject, location, newModelProjectCreationPage.getMetaModelVersionDescriptor(),
				metaModelVersionPreference);
	}
}
