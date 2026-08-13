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
 *     itemis - [405023] Enable NewModelFileCreationPage to be used without having to pass an instance of NewModelFileProperties to its constructor
 *     itemis - [406062] Removal of the required project nature parameter in NewModelFileCreationPage constructor and CreateNewModelProjectJob constructor
 *     itemis - [406194] Enable title and descriptions of model project and file creation wizards to be calculated automatically
 *
 * </copyright>
 */
package org.eclipse.sphinx.emf.workspace.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.workspace.jobs.CreateNewModelFileJob;
import org.eclipse.sphinx.emf.workspace.ui.internal.Activator;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.NewModelFileCreationPage;
import org.eclipse.sphinx.platform.ui.util.ExtendedPlatformUI;
import org.eclipse.sphinx.platform.util.PlatformLogUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * Abstract wizard for creating a new model file in the workspace. No pages are added by default. When being finished a
 * {@linkplain CreateNewModelFileJob new model file job} is run to create and save the new model file in the workspace.
 */
public abstract class AbstractNewModelFileWizard<T extends IMetaModelDescriptor> extends BasicNewResourceWizard {

	protected NewModelFileCreationPage<T> mainPage;

	protected String metaModelName;

	/**
	 * Creates a wizard for creating a new model file in the workspace.
	 */
	public AbstractNewModelFileWizard() {
		this(null);
	}

	/**
	 * Creates a wizard for creating a new model file in the workspace.
	 * 
	 * @param metaModelName
	 *            the name of the metamodel the new model file should be based on
	 */
	public AbstractNewModelFileWizard(String metaModelName) {
		this.metaModelName = metaModelName;
	}

	/*
	 * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);

		setWindowTitle(NLS.bind(Messages.wizard_newModelFile_title, metaModelName != null ? metaModelName : Messages.default_metamodelName_cap));
	}

	/*
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		mainPage = createMainPage();
		Assert.isNotNull(mainPage);
		addPage(mainPage);
	}

	/**
	 * Creates the {@link NewModelFileCreationPage main page} for the creation of the new model file. This method must
	 * be overridden by clients to create a specific main page as appropriate.
	 * 
	 * @return a main page for the creation of the new model file
	 */
	protected abstract NewModelFileCreationPage<T> createMainPage();

	/*
	 * Creates a new model file, selects it in the current view, and opens it in an editor
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// Make required wizard result information accessible for asynchronous operation
		final IFile newFile = mainPage.getNewFile();

		// Create a new model file creation job
		String jobName = NLS.bind(Messages.job_creatingNewModelFile_name, metaModelName != null ? metaModelName : Messages.default_metamodelName);
		Job job = createCreateNewModelFileJob(jobName, newFile);

		// Setup post creation actions
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult() != null && event.getResult().isOK()) {
					Display display = ExtendedPlatformUI.getDisplay();
					if (display != null) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								// Reveal and select new model file in current view
								selectAndReveal(newFile);

								// Open new model file in an editor
								openNewModelInEditor(newFile);
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
	 * Creates a new instance of {@linkplain CreateNewModelFileJob}. This method must be overridden by clients to create
	 * a specific model file creation job as appropriate.
	 * 
	 * @param jobName
	 *            the pre-calculated name of the job
	 * @param newFile
	 *            the new model {@linkplain IFile file} to be created
	 * @return a new instance of job that creates a new model file. This job is a unit of runnable work that can be
	 *         scheduled to be run with the job manager.
	 */
	protected abstract Job createCreateNewModelFileJob(String jobName, IFile newFile);

	/**
	 * Opens newly created model in an editor.
	 * 
	 * @param newFile
	 *            the new model {@linkplain IFile file} to be opened
	 */
	protected void openNewModelInEditor(IFile newFile) {
		try {
			IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorDescriptor defaultEditor = getWorkbench().getEditorRegistry().getDefaultEditor(newFile.getFullPath().toString());
					if (defaultEditor != null) {
						page.openEditor(new FileEditorInput(newFile), defaultEditor.getId());
					}
				}
			}
		} catch (PartInitException exception) {
			PlatformLogUtil.logAsError(Activator.getPlugin(), exception);
		}
	}
}
