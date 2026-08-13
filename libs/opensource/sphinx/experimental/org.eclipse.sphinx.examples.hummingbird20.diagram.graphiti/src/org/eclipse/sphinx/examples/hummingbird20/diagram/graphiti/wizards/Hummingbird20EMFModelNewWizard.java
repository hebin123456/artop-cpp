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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sphinx.examples.hummingbird20.Hummingbird20MMDescriptor;
import org.eclipse.sphinx.examples.hummingbird20.diagram.graphiti.internal.Activator;
import org.eclipse.sphinx.examples.hummingbird20.typemodel.TypeModel20Package;
import org.eclipse.sphinx.graphiti.workspace.ui.wizards.AbstractGraphitiDiagramNewWizard;
import org.eclipse.ui.IWorkbench;

// FIXME Derive this class directly from AbstractModelNewWizard and move it to org.eclipse.sphinx.examples.hummingbird20.ide.ui
public class Hummingbird20EMFModelNewWizard extends AbstractGraphitiDiagramNewWizard {

	protected static final String MODEL_WIZARD_NAME = "Hummingbird20"; //$NON-NLS-1$
	public static List<String> FILE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(Activator.INSTANCE.getString(
			"_UI_DefaultModelEditorFilenameExtensions").split("\\s*,\\s*"))); //$NON-NLS-1$ //$NON-NLS-2$;

	public Hummingbird20EMFModelNewWizard() {
		super(Hummingbird20MMDescriptor.INSTANCE);
	}

	@Override
	protected void initMetamodelPackage() {
		metamodelPackage = TypeModel20Package.eINSTANCE;
	}

	@Override
	protected void initMetamodelFactory() {
		metamodelFactory = TypeModel20Package.eINSTANCE.getTypeModel20Factory();
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setDefaultPageImageDescriptor(ExtendedImageRegistry.INSTANCE.getImageDescriptor(Activator.INSTANCE.getImage("wizban16/NewModel"))); //$NON-NLS-1$
		// Create the first page
		createFileCreationPage(MODEL_WIZARD_NAME, FILE_EXTENSIONS);
		// Create the second page
		createInitialObjectCreationPage(MODEL_WIZARD_NAME);
		// Set window title
		setWindowTitle(Activator.INSTANCE.getString("_UI_Wizard_label")); //$NON-NLS-1$
	}

	@Override
	protected void initDiagramType() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setEditPluginActivator() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initFileExtensions() {
		FILE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(editPlugin
				.getString("_UI_DefaultModelEditorFilenameExtensions").split("\\s*,\\s*"))); //$NON-NLS-1$ //$NON-NLS-2$;	
	}

}