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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.emf.workspace.ui.internal.messages.Messages;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.LinkedFileCreationMainPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

@SuppressWarnings("restriction")
public class LinkedFileWizard extends BasicNewResourceWizard implements INewWizard {

	private String[] filterExtensions = new String[0];

	private LinkedFileCreationMainPage mainPage;

	/**
	 * Creates a wizard for creating a new file resource in the workspace.
	 */
	public LinkedFileWizard() {
		super();
	}

	public LinkedFileWizard(String[] filterExtensions) {
		super();
		this.filterExtensions = filterExtensions;
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public void addPages() {
		super.addPages();
		mainPage = new LinkedFileCreationMainPage(getSelection());
		mainPage.setFilterExtensions(filterExtensions);
		addPage(mainPage);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchWizard.
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(Messages.wizardNewLinkedFile_title);
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc) Method declared on BasicNewResourceWizard.
	 */
	@Override
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfile_wiz.png");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	@Override
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if (file == null) {
			return false;
		}

		selectAndReveal(file);

		return true;
	}
}
