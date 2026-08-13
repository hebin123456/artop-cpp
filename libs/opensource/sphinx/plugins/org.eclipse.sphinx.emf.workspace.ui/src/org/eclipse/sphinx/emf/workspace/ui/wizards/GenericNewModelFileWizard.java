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
package org.eclipse.sphinx.emf.workspace.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry;
import org.eclipse.sphinx.emf.workspace.jobs.CreateNewModelFileJob;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.InitialModelCreationPage;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.InitialModelProperties;
import org.eclipse.sphinx.emf.workspace.ui.wizards.pages.NewModelFileCreationPage;

/**
 * Generic wizard that creates a new model file in the workspace. Two pages are added for selecting the properties of
 * the initial model and the model file to be created. When being finished a {@linkplain CreateNewModelFileJob new model
 * file job} is run to create and save the new model file in the workspace.
 */
public class GenericNewModelFileWizard extends AbstractNewModelFileWizard<IMetaModelDescriptor> {

	protected InitialModelProperties<IMetaModelDescriptor> initialModelProperties = new InitialModelProperties<IMetaModelDescriptor>();

	protected InitialModelCreationPage<IMetaModelDescriptor> initialModelCreationPage;

	/*
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		// Create and add initial model creation page
		initialModelCreationPage = new InitialModelCreationPage<IMetaModelDescriptor>(
				"InitialModelCreationPage", selection, initialModelProperties, MetaModelDescriptorRegistry.ANY_MM); //$NON-NLS-1$
		addPage(initialModelCreationPage);

		super.addPages();
	}

	/*
	 * @see org.eclipse.sphinx.emf.workspace.ui.wizards.AbstractNewModelFileWizard#createMainPage()
	 */
	@Override
	protected NewModelFileCreationPage<IMetaModelDescriptor> createMainPage() {
		return new NewModelFileCreationPage<IMetaModelDescriptor>("NewModelFileCreationPage", selection, initialModelProperties); //$NON-NLS-1$
	}

	/*
	 * @see
	 * org.eclipse.sphinx.emf.workspace.ui.wizards.AbstractNewModelFileWizard#doCreateCreateNewModelFileJob(java.lang
	 * .String, org.eclipse.core.resources.IFile)
	 */
	@Override
	protected Job createCreateNewModelFileJob(String jobName, IFile newFile) {
		return new CreateNewModelFileJob(jobName, newFile, initialModelProperties.getMetaModelDescriptor(),
				initialModelProperties.getRootObjectEPackage(), initialModelProperties.getRootObjectEClassifier());
	}
}
