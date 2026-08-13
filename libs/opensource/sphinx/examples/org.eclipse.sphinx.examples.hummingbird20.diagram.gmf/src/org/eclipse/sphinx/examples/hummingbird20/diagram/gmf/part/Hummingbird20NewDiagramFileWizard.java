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
 * 
 * </copyright>
 */
package org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.part;

import java.io.IOException;
import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.diagram.core.services.view.CreateDiagramViewOperation;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sphinx.examples.hummingbird20.diagram.gmf.edit.parts.ApplicationEditPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * @generated
 */
public class Hummingbird20NewDiagramFileWizard extends Wizard {

	/**
	 * @generated
	 */
	private WizardNewFileCreationPage myFileCreationPage;

	/**
	 * @generated
	 */
	private ModelElementSelectionPage diagramRootElementSelectionPage;

	/**
	 * @generated
	 */
	private TransactionalEditingDomain myEditingDomain;

	/**
	 * @generated
	 */
	public Hummingbird20NewDiagramFileWizard(URI domainModelURI, EObject diagramRoot, TransactionalEditingDomain editingDomain) {
		assert domainModelURI != null : "Domain model uri must be specified"; //$NON-NLS-1$
		assert diagramRoot != null : "Doagram root element must be specified"; //$NON-NLS-1$
		assert editingDomain != null : "Editing domain must be specified"; //$NON-NLS-1$

		myFileCreationPage = new WizardNewFileCreationPage(Messages.Hummingbird20NewDiagramFileWizard_CreationPageName, StructuredSelection.EMPTY);
		myFileCreationPage.setTitle(Messages.Hummingbird20NewDiagramFileWizard_CreationPageTitle);
		myFileCreationPage.setDescription(NLS.bind(Messages.Hummingbird20NewDiagramFileWizard_CreationPageDescription, ApplicationEditPart.MODEL_ID));
		IPath filePath;
		String fileName = URI.decode(domainModelURI.trimFileExtension().lastSegment());
		if (domainModelURI.isPlatformResource()) {
			filePath = new Path(domainModelURI.trimSegments(1).toPlatformString(true));
		} else if (domainModelURI.isFile()) {
			filePath = new Path(domainModelURI.trimSegments(1).toFileString());
		} else {
			// TODO : use some default path
			throw new IllegalArgumentException("Unsupported URI: " + domainModelURI); //$NON-NLS-1$
		}
		myFileCreationPage.setContainerFullPath(filePath);
		myFileCreationPage.setFileName(Hummingbird20DiagramEditorUtil.getUniqueFileName(filePath, fileName, "instancemodel_diagram")); //$NON-NLS-1$

		diagramRootElementSelectionPage = new DiagramRootElementSelectionPage(Messages.Hummingbird20NewDiagramFileWizard_RootSelectionPageName);
		diagramRootElementSelectionPage.setTitle(Messages.Hummingbird20NewDiagramFileWizard_RootSelectionPageTitle);
		diagramRootElementSelectionPage.setDescription(Messages.Hummingbird20NewDiagramFileWizard_RootSelectionPageDescription);
		diagramRootElementSelectionPage.setModelElement(diagramRoot);

		myEditingDomain = editingDomain;
	}

	/**
	 * @generated
	 */
	@Override
	public void addPages() {
		addPage(myFileCreationPage);
		addPage(diagramRootElementSelectionPage);
	}

	/**
	 * @generated
	 */
	@Override
	public boolean performFinish() {
		LinkedList<IFile> affectedFiles = new LinkedList<IFile>();
		IFile diagramFile = myFileCreationPage.createNewFile();
		Hummingbird20DiagramEditorUtil.setCharset(diagramFile);
		affectedFiles.add(diagramFile);
		URI diagramModelURI = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
		ResourceSet resourceSet = myEditingDomain.getResourceSet();
		final Resource diagramResource = resourceSet.createResource(diagramModelURI);
		AbstractTransactionalCommand command = new AbstractTransactionalCommand(myEditingDomain,
				Messages.Hummingbird20NewDiagramFileWizard_InitDiagramCommand, affectedFiles) {

			@Override
			protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
				int diagramVID = Hummingbird20VisualIDRegistry.getDiagramVisualID(diagramRootElementSelectionPage.getModelElement());
				if (diagramVID != ApplicationEditPart.VISUAL_ID) {
					return CommandResult.newErrorCommandResult(Messages.Hummingbird20NewDiagramFileWizard_IncorrectRootError);
				}
				Diagram diagram = ViewService.createDiagram(diagramRootElementSelectionPage.getModelElement(), ApplicationEditPart.MODEL_ID,
						Hummingbird20DiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
				diagramResource.getContents().add(diagram);
				return CommandResult.newOKCommandResult();
			}
		};
		try {
			OperationHistoryFactory.getOperationHistory().execute(command, new NullProgressMonitor(), null);
			diagramResource.save(Hummingbird20DiagramEditorUtil.getSaveOptions());
			Hummingbird20DiagramEditorUtil.openDiagram(diagramResource);
		} catch (ExecutionException e) {
			Hummingbird20DiagramEditorPlugin.getInstance().logError("Unable to create model and diagram", e); //$NON-NLS-1$
		} catch (IOException ex) {
			Hummingbird20DiagramEditorPlugin.getInstance().logError("Save operation failed for: " + diagramModelURI, ex); //$NON-NLS-1$
		} catch (PartInitException ex) {
			Hummingbird20DiagramEditorPlugin.getInstance().logError("Unable to open editor", ex); //$NON-NLS-1$
		}
		return true;
	}

	/**
	 * @generated
	 */
	private static class DiagramRootElementSelectionPage extends ModelElementSelectionPage {

		/**
		 * @generated
		 */
		protected DiagramRootElementSelectionPage(String pageName) {
			super(pageName);
		}

		/**
		 * @generated
		 */
		@Override
		protected String getSelectionTitle() {
			return Messages.Hummingbird20NewDiagramFileWizard_RootSelectionPageSelectionTitle;
		}

		/**
		 * @generated
		 */
		@Override
		protected boolean validatePage() {
			if (selectedModelElement == null) {
				setErrorMessage(Messages.Hummingbird20NewDiagramFileWizard_RootSelectionPageNoSelectionMessage);
				return false;
			}
			boolean result = ViewService.getInstance().provides(
					new CreateDiagramViewOperation(new EObjectAdapter(selectedModelElement), ApplicationEditPart.MODEL_ID,
							Hummingbird20DiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT));
			setErrorMessage(result ? null : Messages.Hummingbird20NewDiagramFileWizard_RootSelectionPageInvalidSelectionMessage);
			return result;
		}
	}
}
