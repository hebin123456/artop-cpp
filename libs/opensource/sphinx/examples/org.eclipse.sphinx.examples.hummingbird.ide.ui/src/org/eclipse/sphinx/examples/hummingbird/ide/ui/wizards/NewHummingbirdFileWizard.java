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
 *     itemis - [406194] Enable title and descriptions of model project and file creation wizards to be calculated automatically
 *
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird.ide.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.emf.workspace.jobs.CreateNewModelFileJob;
import org.eclipse.sphinx.emf.workspace.ui.wizards.AbstractNewModelFileWizard;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.InitialModelCreationPage;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.InitialModelProperties;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.NewModelFileCreationPage;
import org.eclipse.sphinx.examples.hummingbird.ide.metamodel.HummingbirdMMDescriptor;
import org.eclipse.sphinx.examples.hummingbird.ide.ui.wizards.pages.NewHummingbirdFileCreationPage;

/**
 * Basic wizard that creates a new Hummingbird file in the workspace. Two pages are added for selecting the properties
 * of the initial Hummingbird model and the Hummingbird file to be created. When being finished a
 * {@linkplain CreateNewModelFileJob new model file job} is run to create and save the new Hummingbird file in the
 * workspace.
 */
public class NewHummingbirdFileWizard extends AbstractNewModelFileWizard<HummingbirdMMDescriptor> {

	protected InitialModelProperties<HummingbirdMMDescriptor> initialModelProperties = new InitialModelProperties<HummingbirdMMDescriptor>();

	protected InitialModelCreationPage<HummingbirdMMDescriptor> initialModelCreationPage;

	public NewHummingbirdFileWizard() {
		super(HummingbirdMMDescriptor.BASE_NAME);
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.wizards.AbstractNewModelFileWizard#addPages()
	 */
	@Override
	public void addPages() {
		initialModelProperties = new InitialModelProperties<HummingbirdMMDescriptor>();

		// Create and add initial model creation page
		initialModelCreationPage = new InitialModelCreationPage<HummingbirdMMDescriptor>("InitialHummingbirdModelCreationPage", selection, //$NON-NLS-1$
				initialModelProperties, HummingbirdMMDescriptor.INSTANCE);
		addPage(initialModelCreationPage);

		super.addPages();
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.wizards.AbstractNewModelFileWizard#createMainPage()
	 */
	@Override
	protected NewModelFileCreationPage<HummingbirdMMDescriptor> createMainPage() {
		return new NewHummingbirdFileCreationPage("NewHummingbirdFileCreationPage", selection, initialModelProperties); //$NON-NLS-1$
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.ui.wizards.AbstractNewModelFileWizard#createCreateNewModelFileJob(java.lang.
	 * String, org.eclipse.core.resources.IFile)
	 */
	@Override
	protected Job createCreateNewModelFileJob(String jobName, IFile newFile) {
		return new CreateNewModelFileJob(jobName, newFile, initialModelProperties.getMetaModelDescriptor(),
				initialModelProperties.getRootObjectEPackage(), initialModelProperties.getRootObjectEClassifier());
	}
}
