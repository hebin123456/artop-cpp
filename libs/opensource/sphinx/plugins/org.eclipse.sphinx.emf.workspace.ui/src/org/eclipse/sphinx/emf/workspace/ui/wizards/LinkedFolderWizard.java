/**
 * <copyright>
 * 
 * Copyright (c) 2008-2010 See4sys and others.
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
package org.eclipse.sphinx.emf.workspace.ui.wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.LinkedFolderCreationMainPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * Standard workbench wizard that create a new folder resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration; this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewFolderResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * 
 * During the call to <code>open</code>, the wizard dialog is presented to the user. When the user hits Finish, a folder
 * resource at the user-specified workspace path is created, the dialog closes, and the call to <code>open</code>
 * returns.
 * </p>
 */
@SuppressWarnings("restriction")
public class LinkedFolderWizard extends BasicNewResourceWizard {
	private LinkedFolderCreationMainPage mainPage;

	/**
	 * Creates a wizard for creating a new folder resource in the workspace.
	 */
	public LinkedFolderWizard() {
		super();
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public void addPages() {
		super.addPages();
		mainPage = new LinkedFolderCreationMainPage(getSelection());
		addPage(mainPage);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchWizard.
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(Messages.wizardNewLinkedFolder_title);
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc) Method declared on BasicNewResourceWizard.
	 */
	@Override
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfolder_wiz.png");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);

	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public boolean performFinish() {
		IFolder folder = mainPage.createNewLinkedFolder();
		if (folder == null) {
			return false;
		}

		selectAndReveal(folder);

		return true;
	}
}
