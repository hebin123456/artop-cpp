/**
 * <copyright>
 * 
 * Copyright (c) 2012 itemis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 * 
 * Contributors: 
 *     itemis - Initial API and implementation
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.wizards;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.internal.Activator;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.providers.Hummingbird20PlatformDiagramTypeProvider;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.graphiti.workspace.metamodel.GraphitiMMDescriptor;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.AbstractGraphitiDiagramNewWizard;
import org.eclipse.ui.IWorkbench;

public class Hummingbird20GraphitiDiagramNewWizard extends AbstractGraphitiDiagramNewWizard {

	protected static final String MODEL_WIZARD_NAME = "Hummingbird20"; //$NON-NLS-1$

	protected static final List<String> FILE_EXTENSIONS = Collections.singletonList(GraphitiMMDescriptor.GRAPHITI_DIAGRAM_DEFAULT_FILE_EXTENSION);

	protected static final List<String> BO_FILE_EXTENSIONS = Collections.singletonList("typemodel"); //$NON-NLS-1$

	public Hummingbird20GraphitiDiagramNewWizard() {
		super(Hummingbird20MMDescriptor.INSTANCE);
	}

	@Override
	protected void setEditPluginActivator() {
		editPlugin = org.eclipse.sphinx.examples.hummingbird20.edit.Activator.INSTANCE;
	}

	@Override
	protected void initMetamodelPackage() {
		metamodelPackage = TypeModel20Package.eINSTANCE;
	}

	@Override
	protected void initMetamodelFactory() {
		metamodelFactory = TypeModel20Package.eINSTANCE.getTypeModel20Factory();
	}

	@Override
	protected void initDiagramType() {
		graphitiDiagramType = Hummingbird20PlatformDiagramTypeProvider.DIAGRAM_TYPE_TYPE;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		// Create the first page
		createFileCreationPage(MODEL_WIZARD_NAME, FILE_EXTENSIONS);
		// Create the second page
		createInitialObjectCreationPage(MODEL_WIZARD_NAME);
		// Set window title
		setWindowTitle(Activator.INSTANCE.getString("_UI_Wizard_label")); //$NON-NLS-1$
		// Set the BO file extension
		setBOFileExtension(BO_FILE_EXTENSIONS);
		// Set the wizard image
		setDefaultPageImageDescriptor(ExtendedImageRegistry.INSTANCE.getImageDescriptor(Activator.INSTANCE.getImage("wizban16/NewModel"))); //$NON-NLS-1$
	}

	@Override
	protected void initFileExtensions() {
	}
}