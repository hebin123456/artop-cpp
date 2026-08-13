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
package org.eclipse.sphinx.graphiti.workspace.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.graphiti.dt.IDiagramType;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor;
import org.eclipse.sphinx.emf.ui.wizards.AbstractModelNewWizard;
import org.eclipse.sphinx.emf.ui.wizards.pages.AbstractModelInitialObjectCreationPage;
import org.eclipse.sphinx.emf.ui.wizards.pages.AbstractModelNewFileCreationPage;
import org.eclipse.sphinx.graphiti.workspace.ui.internal.Activator;
import org.eclipse.sphinx.graphiti.workspace.ui.util.DiagramUtil;

/**
 * An abstract implementation of a Graphiti diagram creation wizard.
 */
// TODO Rename to AbstractDiagramNewWizard
public abstract class AbstractGraphitiDiagramNewWizard extends AbstractModelNewWizard {

	protected String graphitiDiagramType;

	// TODO Eliminate this field and retrieve content type id from metamodel descriptor instead
	protected String emfContentType;

	// TODO Eliminate this field and retrieve business model file extensions from metamodel descriptor instead
	protected List<String> businessObjectsFileExtension;

	protected Diagram diagram;

	public AbstractGraphitiDiagramNewWizard(IMetaModelDescriptor mmDescriptor) {
		super(mmDescriptor);

		// Set diagram type if redefined in subclasses
		initDiagramType();
	}

	/**
	 * This initializes the metamodel factory
	 */
	protected abstract void initDiagramType();

	/**
	 * @return the emfContentType
	 */
	public String getContentType() {
		return emfContentType;
	}

	/**
	 * @param emfContentType
	 *            the emfContentType to set
	 */
	public void setContentType(String contentType) {
		emfContentType = contentType;
	}

	/**
	 * @param fileExtension
	 *            : the extension of the EMF model file tha holds the BO model
	 */
	public void setBOFileExtension(List<String> fileExtension) {
		businessObjectsFileExtension = fileExtension;
	}

	/**
	 * @return the business objects file extension
	 */
	public String getBOFileExtension() {
		return businessObjectsFileExtension.get(0);
	}

	/**
	 * Verifiy diagram type against string declared in extension point
	 * 
	 * @return
	 */
	protected boolean verifyDiagramType() {
		for (IDiagramType candidateDiagramType : GraphitiUi.getExtensionManager().getDiagramTypes()) {
			if (candidateDiagramType.getId().toString() == graphitiDiagramType) {
				return true;
			}
		}
		return false;
	}

	public void setDiagramType(String diagramType) {
		graphitiDiagramType = diagramType;
	}

	@Override
	public IFile getModelFile() {
		// FIXME The diagram file should not come in through getModelFile()
		IPath path = super.getModelFile().getFullPath().removeFileExtension().addFileExtension(getBOFileExtension());
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}

	@Override
	protected void createAndSaveNewModel() {
		// Create and save business model
		super.createAndSaveNewModel();

		// Create and save diagram model
		String diagramName = newFileCreationPage.getFileName();
		IPath containerPath = newFileCreationPage.getContainerFullPath();
		diagram = DiagramUtil.createDiagram(containerPath, diagramName, graphitiDiagramType, modelRoot);
	}

	@Override
	protected void openNewModelInEditor() {
		DiagramUtil.openBasicDiagramEditor(diagram);
	}

	@Override
	protected void createInitialObjectCreationPage(String MODEL_WIZARD_NAME) {
		// Create the initial object selection page
		initialObjectCreationPage = new AbstractModelInitialObjectCreationPage("Whatever2", getInitialObjectNames(), editPlugin); //$NON-NLS-1$
		initialObjectCreationPage.setTitle(Activator.INSTANCE.getString("_UI_DiagramWizard_label", new Object[] { MODEL_WIZARD_NAME })); //$NON-NLS-1$
		initialObjectCreationPage.setDescription(Activator.INSTANCE.getString("_UI_Wizard_initial_business_object_description")); //$NON-NLS-1$

	}

	@Override
	protected void createFileCreationPage(String MODEL_WIZARD_NAME, List<String> FILE_EXTENSIONS) {
		// Create the creation page
		newFileCreationPage = new AbstractModelNewFileCreationPage("Whatever", selection); //$NON-NLS-1$
		newFileCreationPage.setTitle(Activator.INSTANCE.getString("_UI_DiagramWizard_label", new Object[] { MODEL_WIZARD_NAME })); //$NON-NLS-1$
		newFileCreationPage.setDescription(Activator.INSTANCE.getString("_UI_DiagramWizard_description", new Object[] { MODEL_WIZARD_NAME })); //$NON-NLS-1$
		newFileCreationPage.setFileName(Activator.INSTANCE.getString("_UI_DiagramEditorFilenameDefaultBase") + "." + FILE_EXTENSIONS.get(0)); //$NON-NLS-1$ //$NON-NLS-2$
		newFileCreationPage.setFileExtensions(FILE_EXTENSIONS);
	}
}
